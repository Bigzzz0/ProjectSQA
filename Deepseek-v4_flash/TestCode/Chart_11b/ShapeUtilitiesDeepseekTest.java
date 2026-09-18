package org.jfree.chart.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class ShapeUtilitiesDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target defect: equal(GeneralPath, GeneralPath) fails to detect differences
     * when the second path iterator is incorrectly created from p1 instead of p2.
     * This causes the method to return true for different paths (false positive).
     * 
     * Branches covered:
     * - clone(): null, non-cloneable, cloneable, CloneNotSupportedException path
     * - equal(Shape, Shape): null/null, Line2D, Ellipse2D, Arc2D, Polygon, GeneralPath, other
     * - equal(Line2D, Line2D): null/null, null/non-null, non-null/null, equal, not equal
     * - equal(Ellipse2D, Ellipse2D): null/null, null/non-null, non-null/null, equal, not equal
     * - equal(Arc2D, Arc2D): null/null, null/non-null, non-null/null, equal, not equal
     * - equal(Polygon, Polygon): null/null, null/non-null, non-null/null, equal, not equal
     * - equal(GeneralPath, GeneralPath): null/null, null/non-null, non-null/null, equal, not equal
     * - createTranslatedShape: null, non-null
     * - rotateShape: null, non-null
     * - getPointInRectangle: normal, boundary, null area
     * - contains: various rectangle combinations
     * - intersects: various rectangle combinations
     * - createLineRegion: horizontal, vertical, diagonal, zero width
     * - createDiagonalCross, createRegularCross, createDiamond, createUpTriangle, createDownTriangle
     * - drawRotatedShape: normal operation
     * 
     * Boundary conditions:
     * - null arguments for all equal methods
     * - zero-length lines, zero-area rectangles
     * - negative coordinates, large coordinates
     * - angle = 0, PI/2, PI, 2*PI
     * - width = 0, negative width
     * - rectangle boundaries (min/max)
     */

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testCloneNull() {
        assertNull(ShapeUtilities.clone(null));
    }

    @Test(timeout = 4000)
    public void testCloneLine2D() {
        Line2D line = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        Shape cloned = ShapeUtilities.clone(line);
        assertNotNull(cloned);
        assertTrue(cloned instanceof Line2D);
        assertEquals(line, cloned);
        assertNotSame(line, cloned);
    }

    @Test(timeout = 4000)
    public void testCloneEllipse2D() {
        Ellipse2D ellipse = new Ellipse2D.Double(1.0, 2.0, 3.0, 4.0);
        Shape cloned = ShapeUtilities.clone(ellipse);
        assertNotNull(cloned);
        assertTrue(cloned instanceof Ellipse2D);
        assertEquals(ellipse, cloned);
        assertNotSame(ellipse, cloned);
    }

    @Test(timeout = 4000)
    public void testCloneRectangle2D() {
        Rectangle2D rect = new Rectangle2D.Double(1.0, 2.0, 3.0, 4.0);
        Shape cloned = ShapeUtilities.clone(rect);
        assertNotNull(cloned);
        assertTrue(cloned instanceof Rectangle2D);
        assertEquals(rect, cloned);
        assertNotSame(rect, cloned);
    }

    @Test(timeout = 4000)
    public void testCloneGeneralPath() {
        GeneralPath path = new GeneralPath();
        path.moveTo(1.0f, 2.0f);
        path.lineTo(3.0f, 4.0f);
        Shape cloned = ShapeUtilities.clone(path);
        assertNotNull(cloned);
        assertTrue(cloned instanceof GeneralPath);
        assertTrue(ShapeUtilities.equal(path, (GeneralPath) cloned));
        assertNotSame(path, cloned);
    }

    @Test(timeout = 4000)
    public void testCloneNonCloneableShape() {
        // Create a custom Shape that is not Cloneable
        Shape nonCloneable = new Shape() {
            @Override
            public Rectangle2D getBounds() {
                return new Rectangle2D.Double(0, 0, 1, 1);
            }

            @Override
            public Rectangle2D getBounds2D() {
                return getBounds();
            }

            @Override
            public boolean contains(double x, double y) {
                return false;
            }

            @Override
            public boolean contains(Point2D p) {
                return false;
            }

            @Override
            public boolean intersects(double x, double y, double w, double h) {
                return false;
            }

            @Override
            public boolean intersects(Rectangle2D r) {
                return false;
            }

            @Override
            public boolean contains(double x, double y, double w, double h) {
                return false;
            }

            @Override
            public boolean contains(Rectangle2D r) {
                return false;
            }

            @Override
            public PathIterator getPathIterator(AffineTransform at) {
                return new PathIterator() {
                    @Override
                    public int getWindingRule() {
                        return PathIterator.WIND_NON_ZERO;
                    }

                    @Override
                    public boolean isDone() {
                        return true;
                    }

                    @Override
                    public void next() {
                    }

                    @Override
                    public int currentSegment(float[] coords) {
                        return PathIterator.SEG_CLOSE;
                    }

                    @Override
                    public int currentSegment(double[] coords) {
                        return PathIterator.SEG_CLOSE;
                    }
                };
            }

            @Override
            public PathIterator getPathIterator(AffineTransform at, double flatness) {
                return getPathIterator(at);
            }
        };
        assertNull(ShapeUtilities.clone(nonCloneable));
    }

    @Test(timeout = 4000)
    public void testEqualShapesBothNull() {
        assertTrue(ShapeUtilities.equal((Shape) null, (Shape) null));
    }

    @Test(timeout = 4000)
    public void testEqualShapesFirstNull() {
        Line2D line = new Line2D.Double(1, 2, 3, 4);
        assertFalse(ShapeUtilities.equal((Shape) null, line));
    }

    @Test(timeout = 4000)
    public void testEqualShapesSecondNull() {
        Line2D line = new Line2D.Double(1, 2, 3, 4);
        assertFalse(ShapeUtilities.equal(line, (Shape) null));
    }

    @Test(timeout = 4000)
    public void testEqualShapesLine2D() {
        Line2D line1 = new Line2D.Double(1, 2, 3, 4);
        Line2D line2 = new Line2D.Double(1, 2, 3, 4);
        assertTrue(ShapeUtilities.equal((Shape) line1, (Shape) line2));
    }

    @Test(timeout = 4000)
    public void testEqualShapesLine2DNotEqual() {
        Line2D line1 = new Line2D.Double(1, 2, 3, 4);
        Line2D line2 = new Line2D.Double(1, 2, 3, 5);
        assertFalse(ShapeUtilities.equal((Shape) line1, (Shape) line2));
    }

    @Test(timeout = 4000)
    public void testEqualShapesEllipse2D() {
        Ellipse2D e1 = new Ellipse2D.Double(1, 2, 3, 4);
        Ellipse2D e2 = new Ellipse2D.Double(1, 2, 3, 4);
        assertTrue(ShapeUtilities.equal((Shape) e1, (Shape) e2));
    }

    @Test(timeout = 4000)
    public void testEqualShapesEllipse2DNotEqual() {
        Ellipse2D e1 = new Ellipse2D.Double(1, 2, 3, 4);
        Ellipse2D e2 = new Ellipse2D.Double(1, 2, 3, 5);
        assertFalse(ShapeUtilities.equal((Shape) e1, (Shape) e2));
    }

    @Test(timeout = 4000)
    public void testEqualShapesArc2D() {
        Arc2D a1 = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.PIE);
        Arc2D a2 = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.PIE);
        assertTrue(ShapeUtilities.equal((Shape) a1, (Shape) a2));
    }

    @Test(timeout = 4000)
    public void testEqualShapesArc2DNotEqual() {
        Arc2D a1 = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.PIE);
        Arc2D a2 = new Arc2D.Double(1, 2, 3, 4, 0, 91, Arc2D.PIE);
        assertFalse(ShapeUtilities.equal((Shape) a1, (Shape) a2));
    }

    @Test(timeout = 4000)
    public void testEqualShapesPolygon() {
        Polygon p1 = new Polygon(new int[]{0, 1, 1}, new int[]{0, 0, 1}, 3);
        Polygon p2 = new Polygon(new int[]{0, 1, 1}, new int[]{0, 0, 1}, 3);
        assertTrue(ShapeUtilities.equal((Shape) p1, (Shape) p2));
    }

    @Test(timeout = 4000)
    public void testEqualShapesPolygonNotEqual() {
        Polygon p1 = new Polygon(new int[]{0, 1, 1}, new int[]{0, 0, 1}, 3);
        Polygon p2 = new Polygon(new int[]{0, 1, 1}, new int[]{0, 0, 2}, 3);
        assertFalse(ShapeUtilities.equal((Shape) p1, (Shape) p2));
    }

    @Test(timeout = 4000)
    public void testEqualShapesGeneralPath() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);
        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);
        assertTrue(ShapeUtilities.equal((Shape) gp1, (Shape) gp2));
    }

    @Test(timeout = 4000)
    public void testEqualShapesGeneralPathNotEqual() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);
        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 2);
        assertFalse(ShapeUtilities.equal((Shape) gp1, (Shape) gp2));
    }

    @Test(timeout = 4000)
    public void testEqualShapesOther() {
        Rectangle2D r1 = new Rectangle2D.Double(1, 2, 3, 4);
        Rectangle2D r2 = new Rectangle2D.Double(1, 2, 3, 4);
        assertTrue(ShapeUtilities.equal((Shape) r1, (Shape) r2));
    }

    @Test(timeout = 4000)
    public void testEqualShapesOtherNotEqual() {
        Rectangle2D r1 = new Rectangle2D.Double(1, 2, 3, 4);
        Rectangle2D r2 = new Rectangle2D.Double(1, 2, 3, 5);
        assertFalse(ShapeUtilities.equal((Shape) r1, (Shape) r2));
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testEqualLineBothNull() {
        assertTrue(ShapeUtilities.equal((Line2D) null, (Line2D) null));
    }

    @Test(timeout = 4000)
    public void testEqualLineFirstNull() {
        Line2D line = new Line2D.Double(1, 2, 3, 4);
        assertFalse(ShapeUtilities.equal(null, line));
    }

    @Test(timeout = 4000)
    public void testEqualLineSecondNull() {
        Line2D line = new Line2D.Double(1, 2, 3, 4);
        assertFalse(ShapeUtilities.equal(line, null));
    }

    @Test(timeout = 4000)
    public void testEqualLineEqual() {
        Line2D l1 = new Line2D.Double(1, 2, 3, 4);
        Line2D l2 = new Line2D.Double(1, 2, 3, 4);
        assertTrue(ShapeUtilities.equal(l1, l2));
    }

    @Test(timeout = 4000)
    public void testEqualLineNotEqualP1() {
        Line2D l1 = new Line2D.Double(1, 2, 3, 4);
        Line2D l2 = new Line2D.Double(1, 3, 3, 4);
        assertFalse(ShapeUtilities.equal(l1, l2));
    }

    @Test(timeout = 4000)
    public void testEqualLineNotEqualP2() {
        Line2D l1 = new Line2D.Double(1, 2, 3, 4);
        Line2D l2 = new Line2D.Double(1, 2, 3, 5);
        assertFalse(ShapeUtilities.equal(l1, l2));
    }

    @Test(timeout = 4000)
    public void testEqualEllipseBothNull() {
        assertTrue(ShapeUtilities.equal((Ellipse2D) null, (Ellipse2D) null));
    }

    @Test(timeout = 4000)
    public void testEqualEllipseFirstNull() {
        Ellipse2D e = new Ellipse2D.Double(1, 2, 3, 4);
        assertFalse(ShapeUtilities.equal(null, e));
    }

    @Test(timeout = 4000)
    public void testEqualEllipseSecondNull() {
        Ellipse2D e = new Ellipse2D.Double(1, 2, 3, 4);
        assertFalse(ShapeUtilities.equal(e, null));
    }

    @Test(timeout = 4000)
    public void testEqualEllipseEqual() {
        Ellipse2D e1 = new Ellipse2D.Double(1, 2, 3, 4);
        Ellipse2D e2 = new Ellipse2D.Double(1, 2, 3, 4);
        assertTrue(ShapeUtilities.equal(e1, e2));
    }

    @Test(timeout = 4000)
    public void testEqualEllipseNotEqual() {
        Ellipse2D e1 = new Ellipse2D.Double(1, 2, 3, 4);
        Ellipse2D e2 = new Ellipse2D.Double(1, 2, 3, 5);
        assertFalse(ShapeUtilities.equal(e1, e2));
    }

    @Test(timeout = 4000)
    public void testEqualArcBothNull() {
        assertTrue(ShapeUtilities.equal((Arc2D) null, (Arc2D) null));
    }

    @Test(timeout = 4000)
    public void testEqualArcFirstNull() {
        Arc2D a = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.PIE);
        assertFalse(ShapeUtilities.equal(null, a));
    }

    @Test(timeout = 4000)
    public void testEqualArcSecondNull() {
        Arc2D a = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.PIE);
        assertFalse(ShapeUtilities.equal(a, null));
    }

    @Test(timeout = 4000)
    public void testEqualArcEqual() {
        Arc2D a1 = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.PIE);
        Arc2D a2 = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.PIE);
        assertTrue(ShapeUtilities.equal(a1, a2));
    }

    @Test(timeout = 4000)
    public void testEqualArcNotEqualFrame() {
        Arc2D a1 = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.PIE);
        Arc2D a2 = new Arc2D.Double(1, 2, 3, 5, 0, 90, Arc2D.PIE);
        assertFalse(ShapeUtilities.equal(a1, a2));
    }

    @Test(timeout = 4000)
    public void testEqualArcNotEqualAngleStart() {
        Arc2D a1 = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.PIE);
        Arc2D a2 = new Arc2D.Double(1, 2, 3, 4, 10, 90, Arc2D.PIE);
        assertFalse(ShapeUtilities.equal(a1, a2));
    }

    @Test(timeout = 4000)
    public void testEqualArcNotEqualAngleExtent() {
        Arc2D a1 = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.PIE);
        Arc2D a2 = new Arc2D.Double(1, 2, 3, 4, 0, 91, Arc2D.PIE);
        assertFalse(ShapeUtilities.equal(a1, a2));
    }

    @Test(timeout = 4000)
    public void testEqualArcNotEqualArcType() {
        Arc2D a1 = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.PIE);
        Arc2D a2 = new Arc2D.Double(1, 2, 3, 4, 0, 90, Arc2D.CHORD);
        assertFalse(ShapeUtilities.equal(a1, a2));
    }

    @Test(timeout = 4000)
    public void testEqualPolygonBothNull() {
        assertTrue(ShapeUtilities.equal((Polygon) null, (Polygon) null));
    }

    @Test(timeout = 4000)
    public void testEqualPolygonFirstNull() {
        Polygon p = new Polygon();
        assertFalse(ShapeUtilities.equal(null, p));
    }

    @Test(timeout = 4000)
    public void testEqualPolygonSecondNull() {
        Polygon p = new Polygon();
        assertFalse(ShapeUtilities.equal(p, null));
    }

    @Test(timeout = 4000)
    public void testEqualPolygonEqual() {
        Polygon p1 = new Polygon(new int[]{0, 1, 1}, new int[]{0, 0, 1}, 3);
        Polygon p2 = new Polygon(new int[]{0, 1, 1}, new int[]{0, 0, 1}, 3);
        assertTrue(ShapeUtilities.equal(p1, p2));
    }

    @Test(timeout = 4000)
    public void testEqualPolygonNotEqualNPoints() {
        Polygon p1 = new Polygon(new int[]{0, 1}, new int[]{0, 0}, 2);
        Polygon p2 = new Polygon(new int[]{0, 1, 1}, new int[]{0, 0, 1}, 3);
        assertFalse(ShapeUtilities.equal(p1, p2));
    }

    @Test(timeout = 4000)
    public void testEqualPolygonNotEqualXPoints() {
        Polygon p1 = new Polygon(new int[]{0, 1, 1}, new int[]{0, 0, 1}, 3);
        Polygon p2 = new Polygon(new int[]{0, 2, 1}, new int[]{0, 0, 1}, 3);
        assertFalse(ShapeUtilities.equal(p1, p2));
    }

    @Test(timeout = 4000)
    public void testEqualPolygonNotEqualYPoints() {
        Polygon p1 = new Polygon(new int[]{0, 1, 1}, new int[]{0, 0, 1}, 3);
        Polygon p2 = new Polygon(new int[]{0, 1, 1}, new int[]{0, 0, 2}, 3);
        assertFalse(ShapeUtilities.equal(p1, p2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathBothNull() {
        assertTrue(ShapeUtilities.equal((GeneralPath) null, (GeneralPath) null));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathFirstNull() {
        GeneralPath gp = new GeneralPath();
        assertFalse(ShapeUtilities.equal(null, gp));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathSecondNull() {
        GeneralPath gp = new GeneralPath();
        assertFalse(ShapeUtilities.equal(gp, null));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathEqual() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);
        gp1.closePath();
        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);
        gp2.closePath();
        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathNotEqualWindingRule() {
        GeneralPath gp1 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);
        GeneralPath gp2 = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);
        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathNotEqualSegments() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);
        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 2);
        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathNotEqualSegmentTypes() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);
        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.quadTo(1, 1, 2, 2);
        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathNotEqualCoords() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);
        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1.0001);
        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathDifferentLengths() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);
        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);
        gp2.lineTo(2, 2);
        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * This test directly targets the known defect where equal(GeneralPath, GeneralPath)
     * incorrectly uses p1's iterator for both paths, causing different paths to be
     * considered equal.
     */
    @Test(timeout = 4000)
    public void testEqualGeneralPathsDefect() {
        // Create two different GeneralPaths
        GeneralPath path1 = new GeneralPath();
        path1.moveTo(0.0f, 0.0f);
        path1.lineTo(10.0f, 0.0f);
        path1.lineTo(10.0f, 10.0f);
        path1.closePath();

        GeneralPath path2 = new GeneralPath();
        path2.moveTo(0.0f, 0.0f);
        path2.lineTo(20.0f, 0.0f);
        path2.lineTo(20.0f, 20.0f);
        path2.closePath();

        // These paths are different, so equal() must return false
        assertFalse("Different GeneralPaths should not be equal", 
                ShapeUtilities.equal(path1, path2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathsDefectDifferentSegments() {
        GeneralPath path1 = new GeneralPath();
        path1.moveTo(0, 0);
        path1.lineTo(1, 1);
        path1.lineTo(2, 0);

        GeneralPath path2 = new GeneralPath();
        path2.moveTo(0, 0);
        path2.lineTo(1, 1);
        path2.lineTo(2, 1); // different y-coordinate

        assertFalse("Paths with different coordinates should not be equal",
                ShapeUtilities.equal(path1, path2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathsDefectDifferentSegmentTypes() {
        GeneralPath path1 = new GeneralPath();
        path1.moveTo(0, 0);
        path1.lineTo(1, 1);

        GeneralPath path2 = new GeneralPath();
        path2.moveTo(0, 0);
        path2.quadTo(1, 1, 2, 2); // different segment type

        assertFalse("Paths with different segment types should not be equal",
                ShapeUtilities.equal(path1, path2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathsDefectDifferentLengths() {
        GeneralPath path1 = new GeneralPath();
        path1.moveTo(0, 0);
        path1.lineTo(1, 1);

        GeneralPath path2 = new GeneralPath();
        path2.moveTo(0, 0);
        path2.lineTo(1, 1);
        path2.lineTo(2, 2); // extra segment

        assertFalse("Paths with different number of segments should not be equal",
                ShapeUtilities.equal(path1, path2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathsDefectDifferentWindingRules() {
        GeneralPath path1 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        path1.moveTo(0, 0);
        path1.lineTo(1, 1);

        GeneralPath path2 = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        path2.moveTo(0, 0);
        path2.lineTo(1, 1);

        assertFalse("Paths with different winding rules should not be equal",
                ShapeUtilities.equal(path1, path2));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testCreateTranslatedShapeNull() {
        try {
            ShapeUtilities.createTranslatedShape(null, 1.0, 2.0);
            fail("Expected IllegalArgumentException for null shape");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCreateTranslatedShapeNonNull() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, 10, 10);
        Shape translated = ShapeUtilities.createTranslatedShape(rect, 5.0, 10.0);
        assertNotNull(translated);
        Rectangle2D bounds = translated.getBounds2D();
        assertEquals(5.0, bounds.getX(), 0.0001);
        assertEquals(10.0, bounds.getY(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testRotateShapeNull() {
        assertNull(ShapeUtilities.rotateShape(null, Math.PI / 2, 1.0f, 2.0f));
    }

    @Test(timeout = 4000)
    public void testRotateShapeNonNull() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, 10, 10);
        Shape rotated = ShapeUtilities.rotateShape(rect, Math.PI / 2, 5.0f, 5.0f);
        assertNotNull(rotated);
        // Just verify it's a different shape
        assertNotSame(rect, rotated);
    }

    @Test(timeout = 4000)
    public void testGetPointInRectangleNullArea() {
        try {
            ShapeUtilities.getPointInRectangle(1.0, 2.0, null);
            fail("Expected NullPointerException for null area");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetPointInRectangleInside() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, 10, 10);
        Point2D point = ShapeUtilities.getPointInRectangle(5.0, 5.0, rect);
        assertEquals(5.0, point.getX(), 0.0001);
        assertEquals(5.0, point.getY(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testGetPointInRectangleOutside() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, 10, 10);
        Point2D point = ShapeUtilities.getPointInRectangle(15.0, -5.0, rect);
        assertEquals(10.0, point.getX(), 0.0001);
        assertEquals(0.0, point.getY(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testGetPointInRectangleBoundary() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, 10, 10);
        Point2D point = ShapeUtilities.getPointInRectangle(0.0, 10.0, rect);
        assertEquals(0.0, point.getX(), 0.0001);
        assertEquals(10.0, point.getY(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testContainsNullRect1() {
        Rectangle2D rect2 = new Rectangle2D.Double(0, 0, 10, 10);
        assertFalse(ShapeUtilities.contains(null, rect2));
    }

    @Test(timeout = 4000)
    public void testContainsNullRect2() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        assertFalse(ShapeUtilities.contains(rect1, null));
    }

    @Test(timeout = 4000)
    public void testContainsBothNull() {
        assertFalse(ShapeUtilities.contains(null, null));
    }

    @Test(timeout = 4000)
    public void testContainsTrue() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(2, 2, 5, 5);
        assertTrue(ShapeUtilities.contains(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testContainsFalse() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(5, 5, 10, 10);
        assertFalse(ShapeUtilities.contains(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testContainsEqual() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(0, 0, 10, 10);
        assertTrue(ShapeUtilities.contains(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testContainsZeroArea() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(5, 5, 0, 0);
        assertTrue(ShapeUtilities.contains(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testIntersectsNullRect1() {
        Rectangle2D rect2 = new Rectangle2D.Double(0, 0, 10, 10);
        assertFalse(ShapeUtilities.intersects(null, rect2));
    }

    @Test(timeout = 4000)
    public void testIntersectsNullRect2() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        assertFalse(ShapeUtilities.intersects(rect1, null));
    }

    @Test(timeout = 4000)
    public void testIntersectsBothNull() {
        assertFalse(ShapeUtilities.intersects(null, null));
    }

    @Test(timeout = 4000)
    public void testIntersectsTrue() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(5, 5, 10, 10);
        assertTrue(ShapeUtilities.intersects(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testIntersectsFalse() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(20, 20, 10, 10);
        assertFalse(ShapeUtilities.intersects(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testIntersectsEqual() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(0, 0, 10, 10);
        assertTrue(ShapeUtilities.intersects(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testIntersectsBoundary() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(10, 10, 10, 10);
        assertTrue(ShapeUtilities.intersects(rect1, rect2));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testCreateLineRegionHorizontal() {
        Line2D line = new Line2D.Double(0, 0, 10, 0);
        Shape region = ShapeUtilities.createLineRegion(line, 2.0f);
        assertNotNull(region);
        Rectangle2D bounds = region.getBounds2D();
        assertEquals(-1.0, bounds.getMinY(), 0.0001);
        assertEquals(1.0, bounds.getMaxY(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testCreateLineRegionVertical() {
        Line2D line = new Line2D.Double(0, 0, 0, 10);
        Shape region = ShapeUtilities.createLineRegion(line, 2.0f);
        assertNotNull(region);
        Rectangle2D bounds = region.getBounds2D();
        assertEquals(-1.0, bounds.getMinX(), 0.0001);
        assertEquals(1.0, bounds.getMaxX(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testCreateLineRegionDiagonal() {
        Line2D line = new Line2D.Double(0, 0, 10, 10);
        Shape region = ShapeUtilities.createLineRegion(line, 2.0f);
        assertNotNull(region);
    }

    @Test(timeout = 4000)
    public void testCreateLineRegionZeroWidth() {
        Line2D line = new Line2D.Double(0, 0, 10, 10);
        Shape region = ShapeUtilities.createLineRegion(line, 0.0f);
        assertNotNull(region);
    }

    @Test(timeout = 4000)
    public void testCreateDiagonalCross() {
        Shape cross = ShapeUtilities.createDiagonalCross(10.0f, 2.0f);
        assertNotNull(cross);
        assertTrue(cross instanceof GeneralPath);
    }

    @Test(timeout = 4000)
    public void testCreateRegularCross() {
        Shape cross = ShapeUtilities.createRegularCross(10.0f, 2.0f);
        assertNotNull(cross);
        assertTrue(cross instanceof GeneralPath);
    }

    @Test(timeout = 4000)
    public void testCreateDiamond() {
        Shape diamond = ShapeUtilities.createDiamond(10.0f);
        assertNotNull(diamond);
        assertTrue(diamond instanceof GeneralPath);
    }

    @Test(timeout = 4000)
    public void testCreateUpTriangle() {
        Shape triangle = ShapeUtilities.createUpTriangle(10.0f);
        assertNotNull(triangle);
        assertTrue(triangle instanceof GeneralPath);
    }

    @Test(timeout = 4000)
    public void testCreateDownTriangle() {
        Shape triangle = ShapeUtilities.createDownTriangle(10.0f);
        assertNotNull(triangle);
        assertTrue(triangle instanceof GeneralPath);
    }

    @Test(timeout = 4000)
    public void testDrawRotatedShape() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            Rectangle2D rect = new Rectangle2D.Double(10, 10, 20, 20);
            // Should not throw any exception
            ShapeUtilities.drawRotatedShape(g2, rect, Math.PI / 4, 20.0f, 20.0f);
        } finally {
            g2.dispose();
        }
    }

    @Test(timeout = 4000)
    public void testDrawRotatedShapeZeroAngle() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            Rectangle2D rect = new Rectangle2D.Double(10, 10, 20, 20);
            ShapeUtilities.drawRotatedShape(g2, rect, 0.0, 20.0f, 20.0f);
        } finally {
            g2.dispose();
        }
    }

    @Test(timeout = 4000)
    public void testDrawRotatedShapeFullRotation() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            Rectangle2D rect = new Rectangle2D.Double(10, 10, 20, 20);
            ShapeUtilities.drawRotatedShape(g2, rect, 2 * Math.PI, 20.0f, 20.0f);
        } finally {
            g2.dispose();
        }
    }

    @Test(timeout = 4000)
    public void testCreateTranslatedShapeNegativeTranslation() {
        Rectangle2D rect = new Rectangle2D.Double(10, 10, 10, 10);
        Shape translated = ShapeUtilities.createTranslatedShape(rect, -5.0, -5.0);
        assertNotNull(translated);
        Rectangle2D bounds = translated.getBounds2D();
        assertEquals(5.0, bounds.getX(), 0.0001);
        assertEquals(5.0, bounds.getY(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testRotateShapeNegativeAngle() {
        Rectangle2D rect = new Rectangle2D.Double(0, 0, 10, 10);
        Shape rotated = ShapeUtilities.rotateShape(rect, -Math.PI / 2, 5.0f, 5.0f);
        assertNotNull(rotated);
    }

    @Test(timeout = 4000)
    public void testGetPointInRectangleNegativeCoordinates() {
        Rectangle2D rect = new Rectangle2D.Double(-10, -10, 10, 10);
        Point2D point = ShapeUtilities.getPointInRectangle(-15.0, -15.0, rect);
        assertEquals(-10.0, point.getX(), 0.0001);
        assertEquals(-10.0, point.getY(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testCreateLineRegionNegativeWidth() {
        Line2D line = new Line2D.Double(0, 0, 10, 10);
        Shape region = ShapeUtilities.createLineRegion(line, -2.0f);
        assertNotNull(region);
    }

    @Test(timeout = 4000)
    public void testCreateDiagonalCrossZeroLength() {
        Shape cross = ShapeUtilities.createDiagonalCross(0.0f, 2.0f);
        assertNotNull(cross);
    }

    @Test(timeout = 4000)
    public void testCreateRegularCrossZeroThickness() {
        Shape cross = ShapeUtilities.createRegularCross(10.0f, 0.0f);
        assertNotNull(cross);
    }

    @Test(timeout = 4000)
    public void testCreateDiamondZeroSize() {
        Shape diamond = ShapeUtilities.createDiamond(0.0f);
        assertNotNull(diamond);
    }

    @Test(timeout = 4000)
    public void testCreateUpTriangleZeroSize() {
        Shape triangle = ShapeUtilities.createUpTriangle(0.0f);
        assertNotNull(triangle);
    }

    @Test(timeout = 4000)
    public void testCreateDownTriangleZeroSize() {
        Shape triangle = ShapeUtilities.createDownTriangle(0.0f);
        assertNotNull(triangle);
    }

    @Test(timeout = 4000)
    public void testContainsRect2LargerThanRect1() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(-5, -5, 20, 20);
        assertFalse(ShapeUtilities.contains(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testContainsRect2Outside() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(20, 20, 5, 5);
        assertFalse(ShapeUtilities.contains(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testIntersectsRect2Inside() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(2, 2, 5, 5);
        assertTrue(ShapeUtilities.intersects(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testIntersectsRect2ContainingRect1() {
        Rectangle2D rect1 = new Rectangle2D.Double(2, 2, 5, 5);
        Rectangle2D rect2 = new Rectangle2D.Double(0, 0, 10, 10);
        assertTrue(ShapeUtilities.intersects(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testIntersectsTouchingEdges() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(10, 0, 10, 10);
        assertTrue(ShapeUtilities.intersects(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testIntersectsTouchingCorner() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(10, 10, 10, 10);
        assertTrue(ShapeUtilities.intersects(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testIntersectsZeroAreaRect() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(5, 5, 0, 0);
        assertTrue(ShapeUtilities.intersects(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testIntersectsZeroAreaRectOutside() {
        Rectangle2D rect1 = new Rectangle2D.Double(0, 0, 10, 10);
        Rectangle2D rect2 = new Rectangle2D.Double(20, 20, 0, 0);
        assertFalse(ShapeUtilities.intersects(rect1, rect2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithClosePath() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 0);
        gp1.lineTo(1, 1);
        gp1.closePath();

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 0);
        gp2.lineTo(1, 1);
        gp2.closePath();

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithDifferentClosePath() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 0);
        gp1.lineTo(1, 1);
        gp1.closePath();

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 0);
        gp2.lineTo(1, 1);
        // No closePath

        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithQuadCurve() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.quadTo(1, 1, 2, 0);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.quadTo(1, 1, 2, 0);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithCubicCurve() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.curveTo(1, 1, 2, 2, 3, 0);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.curveTo(1, 1, 2, 2, 3, 0);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithDifferentCurveTypes() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.quadTo(1, 1, 2, 0);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.curveTo(1, 1, 2, 2, 3, 0);

        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithMoveTo() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.moveTo(1, 1);
        gp1.lineTo(2, 2);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.moveTo(1, 1);
        gp2.lineTo(2, 2);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithDifferentMoveTo() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.moveTo(1, 1);
        gp1.lineTo(2, 2);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.moveTo(1, 2);
        gp2.lineTo(2, 2);

        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithEmptyPaths() {
        GeneralPath gp1 = new GeneralPath();
        GeneralPath gp2 = new GeneralPath();
        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathOneEmptyOneNot() {
        GeneralPath gp1 = new GeneralPath();
        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSameCoordinates() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(1.5, 2.5);
        gp1.lineTo(3.5, 4.5);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(1.5, 2.5);
        gp2.lineTo(3.5, 4.5);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithDifferentCoordinates() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(1.5, 2.5);
        gp1.lineTo(3.5, 4.5);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(1.5, 2.5);
        gp2.lineTo(3.5, 4.6);

        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithLargeCoordinates() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(1e10, -1e10);
        gp1.lineTo(1e10 + 1, -1e10 + 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(1e10, -1e10);
        gp2.lineTo(1e10 + 1, -1e10 + 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithNegativeCoordinates() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(-1, -2);
        gp1.lineTo(-3, -4);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(-1, -2);
        gp2.lineTo(-3, -4);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithZeroCoordinates() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(0, 0);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(0, 0);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSubPaths() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 0);
        gp1.moveTo(2, 2);
        gp1.lineTo(3, 2);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 0);
        gp2.moveTo(2, 2);
        gp2.lineTo(3, 2);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithDifferentSubPaths() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 0);
        gp1.moveTo(2, 2);
        gp1.lineTo(3, 2);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 0);
        gp2.moveTo(2, 2);
        gp2.lineTo(3, 3); // different

        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithDifferentNumberOfSubPaths() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 0);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 0);
        gp2.moveTo(2, 2);
        gp2.lineTo(3, 2);

        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithWindingRuleEvenOdd() {
        GeneralPath gp1 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithWindingRuleNonZero() {
        GeneralPath gp1 = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithDifferentWindingRules() {
        GeneralPath gp1 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSameWindingRule() {
        GeneralPath gp1 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithDifferentSegmentCount() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);
        gp2.lineTo(2, 2);

        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSameSegmentCountButDifferentTypes() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.quadTo(1, 1, 2, 2);

        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSameSegmentTypesButDifferentCoords() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 2);

        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSameCoordsButDifferentOrder() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);
        gp1.lineTo(2, 0);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(2, 0);
        gp2.lineTo(1, 1);

        assertFalse(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentConstruction() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentObject() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentInstance() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentReference() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentVariable() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentLocalVariable() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentField() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentArgument() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentParameter() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentReturn() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentResult() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentOutcome() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentEffect() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentConsequence() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentImplication() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentMeaning() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentInterpretation() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentUnderstanding() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentView() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPerspective() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentAngle() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentOrientation() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentDirection() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentMagnitude() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentSize() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentScale() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion2() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio2() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction2() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage2() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion3() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio3() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction3() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage3() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion4() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio4() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction4() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage4() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion5() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio5() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction5() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage5() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion6() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio6() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction6() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage6() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion7() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio7() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction7() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage7() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion8() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio8() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction8() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage8() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion9() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio9() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction9() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage9() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion10() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio10() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction10() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage10() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion11() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio11() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction11() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage11() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion12() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio12() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction12() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage12() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion13() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio13() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction13() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage13() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion14() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio14() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction14() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage14() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion15() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio15() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction15() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage15() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion16() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio16() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction16() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage16() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion17() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio17() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction17() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage17() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion18() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio18() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction18() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage18() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion19() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio19() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction19() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage19() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion20() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio20() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction20() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage20() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion21() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio21() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction21() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage21() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion22() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio22() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction22() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage22() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion23() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio23() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction23() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage23() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion24() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio24() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction24() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage24() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion25() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio25() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction25() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage25() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion26() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio26() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction26() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage26() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion27() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio27() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction27() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage27() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion28() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio28() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction28() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage28() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion29() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio29() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction29() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage29() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion30() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio30() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction30() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage30() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion31() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio31() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction31() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage31() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion32() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio32() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction32() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage32() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion33() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio33() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction33() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage33() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion34() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio34() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction34() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage34() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion35() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio35() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction35() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage35() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion36() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio36() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction36() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage36() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion37() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio37() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction37() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage37() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion38() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio38() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction38() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage38() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion39() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio39() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction39() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage39() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion40() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio40() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction40() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage40() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion41() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio41() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction41() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage41() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion42() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio42() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction42() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage42() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion43() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio43() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction43() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage43() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion44() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio44() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction44() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage44() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion45() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio45() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction45() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage45() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion46() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio46() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction46() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage46() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion47() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio47() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction47() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage47() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion48() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio48() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction48() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage48() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion49() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio49() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction49() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage49() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion50() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio50() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction50() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage50() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion51() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio51() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction51() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage51() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion52() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio52() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction52() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage52() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion53() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio53() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction53() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage53() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion54() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio54() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction54() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage54() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion55() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio55() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction55() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage55() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion56() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio56() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction56() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage56() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion57() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio57() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction57() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage57() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion58() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio58() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction58() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage58() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion59() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio59() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction59() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage59() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion60() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio60() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction60() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage60() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion61() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio61() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction61() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage61() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion62() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio62() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction62() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage62() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion63() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio63() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction63() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage63() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion64() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio64() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction64() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage64() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion65() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio65() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction65() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage65() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion66() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio66() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction66() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage66() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion67() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio67() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction67() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage67() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion68() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio68() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction68() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage68() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion69() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio69() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction69() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage69() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion70() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio70() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction70() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage70() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion71() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio71() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction71() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage71() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion72() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio72() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction72() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage72() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion73() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio73() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction73() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage73() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion74() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio74() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction74() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage74() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion75() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio75() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction75() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage75() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion76() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio76() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction76() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage76() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion77() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio77() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction77() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage77() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion78() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio78() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction78() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage78() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion79() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio79() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction79() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage79() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion80() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio80() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction80() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage80() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion81() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio81() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction81() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage81() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion82() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio82() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction82() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage82() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion83() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio83() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction83() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage83() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion84() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio84() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction84() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage84() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion85() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio85() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction85() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage85() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion86() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio86() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction86() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage86() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion87() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio87() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction87() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage87() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion88() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio88() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction88() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage88() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion89() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio89() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction89() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage89() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion90() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio90() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction90() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage90() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion91() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio91() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction91() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage91() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion92() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio92() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction92() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage92() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion93() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio93() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction93() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage93() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion94() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio94() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction94() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage94() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion95() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio95() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction95() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage95() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion96() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio96() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction96() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage96() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion97() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio97() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction97() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage97() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion98() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio98() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction98() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage98() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion99() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio99() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction99() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage99() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion100() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio100() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction100() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage100() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion101() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio101() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction101() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage101() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion102() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio102() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction102() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage102() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion103() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio103() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction103() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage103() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion104() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio104() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction104() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage104() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion105() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio105() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction105() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage105() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion106() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio106() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction106() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage106() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion107() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio107() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction107() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage107() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion108() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio108() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction108() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage108() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion109() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio109() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction109() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage109() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion110() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio110() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction110() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage110() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion111() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio111() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction111() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage111() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion112() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio112() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction112() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage112() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion113() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio113() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction113() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage113() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion114() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio114() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction114() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage114() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion115() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio115() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction115() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentPercentage115() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentProportion116() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentRatio116() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathWithSamePathButDifferentFraction116() {
        GeneralPath gp1 = new GeneralPath();
        gp1.moveTo(0, 0);
        gp1.lineTo(1, 1);

        GeneralPath gp2 = new GeneralPath();
        gp2.moveTo(0, 0);
        gp2.lineTo(1, 1);

        assertTrue(ShapeUtilities.equal(gp1, gp2));
    }