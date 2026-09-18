/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jfree.chart.util.ShapeUtilities
 *
 * 1. Defect-Targeted Branch Zone (Defects4J Ground Truth):
 *    - Method: equal(GeneralPath p1, GeneralPath p2)
 *    - Flaw: In defective code, 'iterator2' is initialized from 'p1' instead of 'p2':
 *            PathIterator iterator2 = p1.getPathIterator(null);
 *    - Impact: Comparing two different GeneralPath instances (with identical winding rules)
 *              incorrectly returns true because iterator2 iterates over p1.
 *    - Targeted Tests: testEqualGeneralPaths_DifferentPoints(), testEqualGeneralPaths_DifferentLengths(),
 *                      testEqualGeneralPaths_DifferentSegmentTypes()
 *
 * 2. Partition A: Core Functional Logic & Shape Creation:
 *    - diagonalCross, regularCross, diamond, upTriangle, downTriangle.
 *    - createLineRegion: (x2 - x1 != 0) sloped line vs (x2 - x1 == 0) vertical line.
 *    - rotateShape and drawRotatedShape with Graphics2D.
 *    - createTranslatedShape: by offsets and by RectangleAnchor.
 *
 * 3. Partition B: Boundary Value Analysis (BVA) & Extremes:
 *    - Null vs Null, Null vs Non-Null, Non-Null vs Null for all equal() overloads.
 *    - Geometry containment and intersection: inside, overlapping, outside, zero-width/height.
 *    - getPointInRectangle bounds clamping: inside, strictly below min, strictly above max.
 *
 * 4. Partition C: Shape Equality Dispatches & Detailed Attribute Branches:
 *    - equal(Shape, Shape): polymorphic dispatch to Line2D, Ellipse2D, Arc2D, Polygon, GeneralPath, default.
 *    - equal(Line2D, Line2D): P1 mismatch, P2 mismatch, identical.
 *    - equal(Ellipse2D, Ellipse2D): frame mismatch, identical.
 *    - equal(Arc2D, Arc2D): frame mismatch, start angle mismatch, extent mismatch, arc type mismatch, identical.
 *    - equal(Polygon, Polygon): npoints mismatch, xpoints mismatch, ypoints mismatch, identical.
 *    - equal(GeneralPath, GeneralPath): windingRule mismatch, iterator early-termination mismatch.
 *
 * 5. Partition D: Exception & Defensive Guard Paths:
 *    - createTranslatedShape with null shape or null anchor -> IllegalArgumentException.
 *    - getPointInRectangle with null area -> NullPointerException.
 *
 * 6. Partition E: Object Lifecycle & Contract Integrity:
 *    - ShapeUtilities constructor via reflection.
 *    - clone(Shape): null shape, cloneable shape, non-cloneable shape.
 */

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
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Constructor;

public class ShapeUtilitiesGeminiTest {

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Flaw: equal GeneralPath)
    // =========================================================================

    /**
     * Targets Defects4J bug where iterator2 was initialized as p1.getPathIterator(null).
     * Two different paths must NOT be considered equal.
     */
    @Test(timeout = 4000)
    public void testEqualGeneralPaths_DifferentPoints() {
        GeneralPath p1 = new GeneralPath();
        p1.moveTo(0.0f, 0.0f);
        p1.lineTo(10.0f, 10.0f);

        GeneralPath p2 = new GeneralPath();
        p2.moveTo(0.0f, 0.0f);
        p2.lineTo(20.0f, 20.0f);

        assertFalse("Paths with different coordinates must not be equal",
                ShapeUtilities.equal(p1, p2));
        assertFalse("Polymorphic equal(Shape, Shape) must also return false",
                ShapeUtilities.equal((Shape) p1, (Shape) p2));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPaths_DifferentLengths() {
        GeneralPath p1 = new GeneralPath();
        p1.moveTo(0.0f, 0.0f);
        p1.lineTo(5.0f, 5.0f);

        GeneralPath p2 = new GeneralPath();
        p2.moveTo(0.0f, 0.0f);
        p2.lineTo(5.0f, 5.0f);
        p2.lineTo(10.0f, 10.0f);

        assertFalse("Paths with different number of segments must not be equal",
                ShapeUtilities.equal(p1, p2));
        assertFalse("Reverse comparison must also be false",
                ShapeUtilities.equal(p2, p1));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPaths_DifferentSegmentTypes() {
        GeneralPath p1 = new GeneralPath();
        p1.moveTo(0.0f, 0.0f);
        p1.lineTo(10.0f, 10.0f);
        p1.closePath();

        GeneralPath p2 = new GeneralPath();
        p2.moveTo(0.0f, 0.0f);
        p2.quadTo(5.0f, 0.0f, 10.0f, 10.0f);
        p2.closePath();

        assertFalse("Paths with different segment types must not be equal",
                ShapeUtilities.equal(p1, p2));
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & SHAPE CREATION
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateDiagonalCross() {
        Shape cross = ShapeUtilities.createDiagonalCross(5.0f, 1.0f);
        assertNotNull(cross);
        Rectangle2D bounds = cross.getBounds2D();
        assertTrue(bounds.getWidth() > 0.0);
        assertTrue(bounds.getHeight() > 0.0);
    }

    @Test(timeout = 4000)
    public void testCreateRegularCross() {
        Shape cross = ShapeUtilities.createRegularCross(4.0f, 2.0f);
        assertNotNull(cross);
        Rectangle2D bounds = cross.getBounds2D();
        assertEquals(-4.0, bounds.getMinX(), 0.01);
        assertEquals(4.0, bounds.getMaxX(), 0.01);
    }

    @Test(timeout = 4000)
    public void testCreateDiamond() {
        Shape diamond = ShapeUtilities.createDiamond(3.0f);
        assertNotNull(diamond);
        Rectangle2D bounds = diamond.getBounds2D();
        assertEquals(-3.0, bounds.getMinX(), 0.01);
        assertEquals(3.0, bounds.getMaxX(), 0.01);
        assertEquals(-3.0, bounds.getMinY(), 0.01);
        assertEquals(3.0, bounds.getMaxY(), 0.01);
    }

    @Test(timeout = 4000)
    public void testCreateUpTriangle() {
        Shape upTri = ShapeUtilities.createUpTriangle(4.0f);
        assertNotNull(upTri);
        Rectangle2D bounds = upTri.getBounds2D();
        assertEquals(-4.0, bounds.getMinX(), 0.01);
        assertEquals(4.0, bounds.getMaxX(), 0.01);
        assertEquals(-4.0, bounds.getMinY(), 0.01);
        assertEquals(4.0, bounds.getMaxY(), 0.01);
    }

    @Test(timeout = 4000)
    public void testCreateDownTriangle() {
        Shape downTri = ShapeUtilities.createDownTriangle(4.0f);
        assertNotNull(downTri);
        Rectangle2D bounds = downTri.getBounds2D();
        assertEquals(-4.0, bounds.getMinX(), 0.01);
        assertEquals(4.0, bounds.getMaxX(), 0.01);
        assertEquals(-4.0, bounds.getMinY(), 0.01);
        assertEquals(4.0, bounds.getMaxY(), 0.01);
    }

    @Test(timeout = 4000)
    public void testCreateLineRegionSloped() {
        Line2D line = new Line2D.Double(0.0, 0.0, 10.0, 10.0);
        Shape region = ShapeUtilities.createLineRegion(line, 2.0f);
        assertNotNull(region);
        assertTrue(region.contains(5.0, 5.0));
    }

    @Test(timeout = 4000)
    public void testCreateLineRegionVertical() {
        // Triggers the special vertical line branch: (x2 - x1) == 0.0
        Line2D line = new Line2D.Double(5.0, 0.0, 5.0, 20.0);
        Shape region = ShapeUtilities.createLineRegion(line, 4.0f);
        assertNotNull(region);
        Rectangle2D bounds = region.getBounds2D();
        assertEquals(3.0, bounds.getMinX(), 0.01);
        assertEquals(7.0, bounds.getMaxX(), 0.01);
        assertEquals(0.0, bounds.getMinY(), 0.01);
        assertEquals(20.0, bounds.getMaxY(), 0.01);
    }

    @Test(timeout = 4000)
    public void testRotateShapeNormalAndNull() {
        assertNull(ShapeUtilities.rotateShape(null, Math.PI / 2.0, 0.0f, 0.0f));

        Shape rect = new Rectangle2D.Double(0, 0, 10, 5);
        Shape rotated = ShapeUtilities.rotateShape(rect, Math.PI / 2.0, 0.0f, 0.0f);
        assertNotNull(rotated);
        Rectangle2D bounds = rotated.getBounds2D();
        assertEquals(-5.0, bounds.getMinX(), 0.01);
        assertEquals(0.0, bounds.getMaxX(), 0.01);
        assertEquals(0.0, bounds.getMinY(), 0.01);
        assertEquals(10.0, bounds.getMaxY(), 0.01);
    }

    @Test(timeout = 4000)
    public void testDrawRotatedShape() {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Shape rect = new Rectangle2D.Double(10, 10, 20, 20);
        AffineTransform before = g2.getTransform();
        ShapeUtilities.drawRotatedShape(g2, rect, Math.PI / 4.0, 20.0f, 20.0f);
        AffineTransform after = g2.getTransform();
        assertEquals(before, after);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testCreateTranslatedShapeByOffsets() {
        Shape rect = new Rectangle2D.Double(10, 20, 30, 40);
        Shape translated = ShapeUtilities.createTranslatedShape(rect, 5.0, -10.0);
        assertNotNull(translated);
        Rectangle2D bounds = translated.getBounds2D();
        assertEquals(15.0, bounds.getX(), 0.001);
        assertEquals(10.0, bounds.getY(), 0.001);
        assertEquals(30.0, bounds.getWidth(), 0.001);
        assertEquals(40.0, bounds.getHeight(), 0.001);
    }

    @Test(timeout = 4000)
    public void testCreateTranslatedShapeByAnchor() {
        Shape rect = new Rectangle2D.Double(0, 0, 10, 10);
        Shape translated = ShapeUtilities.createTranslatedShape(rect, RectangleAnchor.CENTER, 50.0, 50.0);
        assertNotNull(translated);
        Rectangle2D bounds = translated.getBounds2D();
        assertEquals(45.0, bounds.getMinX(), 0.001);
        assertEquals(45.0, bounds.getMinY(), 0.001);
        assertEquals(55.0, bounds.getMaxX(), 0.001);
        assertEquals(55.0, bounds.getMaxY(), 0.001);
    }

    // =========================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetPointInRectangleClamping() {
        Rectangle2D rect = new Rectangle2D.Double(10.0, 20.0, 30.0, 40.0);

        // Point strictly inside
        Point2D inside = ShapeUtilities.getPointInRectangle(20.0, 30.0, rect);
        assertEquals(20.0, inside.getX(), 0.001);
        assertEquals(30.0, inside.getY(), 0.001);

        // Point smaller than min boundaries
        Point2D minClamped = ShapeUtilities.getPointInRectangle(0.0, 5.0, rect);
        assertEquals(10.0, minClamped.getX(), 0.001);
        assertEquals(20.0, minClamped.getY(), 0.001);

        // Point greater than max boundaries
        Point2D maxClamped = ShapeUtilities.getPointInRectangle(100.0, 100.0, rect);
        assertEquals(40.0, maxClamped.getX(), 0.001);
        assertEquals(60.0, maxClamped.getY(), 0.001);
    }

    @Test(timeout = 4000)
    public void testContains() {
        Rectangle2D r1 = new Rectangle2D.Double(10.0, 10.0, 50.0, 50.0);

        // Full containment
        Rectangle2D r2 = new Rectangle2D.Double(20.0, 20.0, 10.0, 10.0);
        assertTrue(ShapeUtilities.contains(r1, r2));

        // Exactly identical
        assertTrue(ShapeUtilities.contains(r1, new Rectangle2D.Double(10.0, 10.0, 50.0, 50.0)));

        // Zero-size inside
        assertTrue(ShapeUtilities.contains(r1, new Rectangle2D.Double(20.0, 20.0, 0.0, 0.0)));

        // Boundary violations
        assertFalse("X < X0", ShapeUtilities.contains(r1, new Rectangle2D.Double(9.0, 10.0, 10.0, 10.0)));
        assertFalse("Y < Y0", ShapeUtilities.contains(r1, new Rectangle2D.Double(10.0, 9.0, 10.0, 10.0)));
        assertFalse("X + W > X0 + W0", ShapeUtilities.contains(r1, new Rectangle2D.Double(55.0, 10.0, 10.0, 10.0)));
        assertFalse("Y + H > Y0 + H0", ShapeUtilities.contains(r1, new Rectangle2D.Double(10.0, 55.0, 10.0, 10.0)));
    }

    @Test(timeout = 4000)
    public void testIntersects() {
        Rectangle2D r1 = new Rectangle2D.Double(10.0, 10.0, 50.0, 50.0);

        // Overlapping
        assertTrue(ShapeUtilities.intersects(r1, new Rectangle2D.Double(0.0, 0.0, 20.0, 20.0)));
        // Touching edge
        assertTrue(ShapeUtilities.intersects(r1, new Rectangle2D.Double(60.0, 10.0, 10.0, 10.0)));

        // Non-overlapping violations
        assertFalse("x + width < x0", ShapeUtilities.intersects(r1, new Rectangle2D.Double(0.0, 10.0, 5.0, 10.0)));
        assertFalse("y + height < y0", ShapeUtilities.intersects(r1, new Rectangle2D.Double(10.0, 0.0, 10.0, 5.0)));
        assertFalse("x > x0 + w0", ShapeUtilities.intersects(r1, new Rectangle2D.Double(65.0, 10.0, 10.0, 10.0)));
        assertFalse("y > y0 + h0", ShapeUtilities.intersects(r1, new Rectangle2D.Double(10.0, 65.0, 10.0, 10.0)));
    }

    // =========================================================================
    // PARTITION C (Continued): EQUALITY CONTRACTS & SHAPE DISPATCH BRANCHES
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualShapePolymorphic() {
        Shape l1 = new Line2D.Double(0, 0, 1, 1);
        Shape l2 = new Line2D.Double(0, 0, 1, 1);
        Shape e1 = new Ellipse2D.Double(0, 0, 1, 1);
        Shape a1 = new Arc2D.Double(0, 0, 1, 1, 0, 90, Arc2D.OPEN);
        Shape p1 = new Polygon(new int[]{0, 1}, new int[]{0, 1}, 2);
        Shape g1 = new GeneralPath();
        Shape r1 = new Rectangle2D.Double(0, 0, 1, 1);
        Shape r2 = new Rectangle2D.Double(0, 0, 1, 1);

        assertTrue(ShapeUtilities.equal(l1, l2));
        assertFalse(ShapeUtilities.equal(l1, e1));
        assertFalse(ShapeUtilities.equal(e1, a1));
        assertFalse(ShapeUtilities.equal(a1, p1));
        assertFalse(ShapeUtilities.equal(p1, g1));
        assertFalse(ShapeUtilities.equal(g1, r1));
        assertTrue(ShapeUtilities.equal(r1, r2));
        assertTrue(ShapeUtilities.equal((Shape) null, (Shape) null));
        assertFalse(ShapeUtilities.equal(l1, null));
        assertFalse(ShapeUtilities.equal(null, l2));
    }

    @Test(timeout = 4000)
    public void testEqualLine2D() {
        Line2D l1 = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        Line2D l2 = new Line2D.Double(1.0, 2.0, 3.0, 4.0);
        Line2D diffP1 = new Line2D.Double(0.0, 2.0, 3.0, 4.0);
        Line2D diffP2 = new Line2D.Double(1.0, 2.0, 5.0, 4.0);

        assertTrue(ShapeUtilities.equal((Line2D) null, (Line2D) null));
        assertFalse(ShapeUtilities.equal(l1, (Line2D) null));
        assertFalse(ShapeUtilities.equal((Line2D) null, l2));
        assertTrue(ShapeUtilities.equal(l1, l2));
        assertFalse(ShapeUtilities.equal(l1, diffP1));
        assertFalse(ShapeUtilities.equal(l1, diffP2));
    }

    @Test(timeout = 4000)
    public void testEqualEllipse2D() {
        Ellipse2D e1 = new Ellipse2D.Double(1.0, 2.0, 3.0, 4.0);
        Ellipse2D e2 = new Ellipse2D.Double(1.0, 2.0, 3.0, 4.0);
        Ellipse2D eDiff = new Ellipse2D.Double(1.0, 2.0, 5.0, 4.0);

        assertTrue(ShapeUtilities.equal((Ellipse2D) null, (Ellipse2D) null));
        assertFalse(ShapeUtilities.equal(e1, (Ellipse2D) null));
        assertFalse(ShapeUtilities.equal((Ellipse2D) null, e2));
        assertTrue(ShapeUtilities.equal(e1, e2));
        assertFalse(ShapeUtilities.equal(e1, eDiff));
    }

    @Test(timeout = 4000)
    public void testEqualArc2D() {
        Arc2D a1 = new Arc2D.Double(0, 0, 10, 10, 0, 90, Arc2D.PIE);
        Arc2D a2 = new Arc2D.Double(0, 0, 10, 10, 0, 90, Arc2D.PIE);

        assertTrue(ShapeUtilities.equal((Arc2D) null, (Arc2D) null));
        assertFalse(ShapeUtilities.equal(a1, (Arc2D) null));
        assertFalse(ShapeUtilities.equal((Arc2D) null, a2));
        assertTrue(ShapeUtilities.equal(a1, a2));

        // Frame difference
        assertFalse(ShapeUtilities.equal(a1, new Arc2D.Double(1, 0, 10, 10, 0, 90, Arc2D.PIE)));
        // Angle start difference
        assertFalse(ShapeUtilities.equal(a1, new Arc2D.Double(0, 0, 10, 10, 5, 90, Arc2D.PIE)));
        // Angle extent difference
        assertFalse(ShapeUtilities.equal(a1, new Arc2D.Double(0, 0, 10, 10, 0, 45, Arc2D.PIE)));
        // Arc type difference
        assertFalse(ShapeUtilities.equal(a1, new Arc2D.Double(0, 0, 10, 10, 0, 90, Arc2D.OPEN)));
    }

    @Test(timeout = 4000)
    public void testEqualPolygon() {
        Polygon p1 = new Polygon(new int[]{0, 5, 10}, new int[]{0, 5, 0}, 3);
        Polygon p2 = new Polygon(new int[]{0, 5, 10}, new int[]{0, 5, 0}, 3);

        assertTrue(ShapeUtilities.equal((Polygon) null, (Polygon) null));
        assertFalse(ShapeUtilities.equal(p1, (Polygon) null));
        assertFalse(ShapeUtilities.equal((Polygon) null, p2));
        assertTrue(ShapeUtilities.equal(p1, p2));

        // Npoints difference
        assertFalse(ShapeUtilities.equal(p1, new Polygon(new int[]{0, 5}, new int[]{0, 5}, 2)));
        // Xpoints difference
        assertFalse(ShapeUtilities.equal(p1, new Polygon(new int[]{0, 6, 10}, new int[]{0, 5, 0}, 3)));
        // Ypoints difference
        assertFalse(ShapeUtilities.equal(p1, new Polygon(new int[]{0, 5, 10}, new int[]{0, 6, 0}, 3)));
    }

    @Test(timeout = 4000)
    public void testEqualGeneralPathsIdenticalAndNulls() {
        assertTrue(ShapeUtilities.equal((GeneralPath) null, (GeneralPath) null));
        GeneralPath p1 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        p1.moveTo(1.0f, 2.0f);
        p1.lineTo(3.0f, 4.0f);

        GeneralPath p2 = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        p2.moveTo(1.0f, 2.0f);
        p2.lineTo(3.0f, 4.0f);

        assertFalse(ShapeUtilities.equal(p1, (GeneralPath) null));
        assertFalse(ShapeUtilities.equal((GeneralPath) null, p2));
        assertTrue(ShapeUtilities.equal(p1, p2));

        // Winding rule difference
        GeneralPath pDiffWinding = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        pDiffWinding.moveTo(1.0f, 2.0f);
        pDiffWinding.lineTo(3.0f, 4.0f);
        assertFalse(ShapeUtilities.equal(p1, pDiffWinding));
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateTranslatedShapeNullShape() {
        ShapeUtilities.createTranslatedShape(null, 1.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateTranslatedShapeAnchorNullShape() {
        ShapeUtilities.createTranslatedShape(null, RectangleAnchor.CENTER, 0.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCreateTranslatedShapeNullAnchor() {
        ShapeUtilities.createTranslatedShape(new Rectangle2D.Double(), null, 0.0, 0.0);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGetPointInRectangleNullArea() {
        ShapeUtilities.getPointInRectangle(0.0, 0.0, null);
    }

    // =========================================================================
    // PARTITION E: OBJECT LIFECYCLE & CONTRACT INTEGRITY
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrivateConstructor() throws Exception {
        Constructor<ShapeUtilities> constructor = ShapeUtilities.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ShapeUtilities instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test(timeout = 4000)
    public void testClone() {
        assertNull(ShapeUtilities.clone(null));

        // Cloneable shapes
        Shape rect = new Rectangle2D.Double(10, 20, 30, 40);
        Shape clonedRect = ShapeUtilities.clone(rect);
        assertNotNull(clonedRect);
        assertEquals(rect, clonedRect);
        assertNotSame(rect, clonedRect);

        Shape roundRect = new RoundRectangle2D.Double(1, 2, 3, 4, 5, 6);
        Shape clonedRoundRect = ShapeUtilities.clone(roundRect);
        assertNotNull(clonedRoundRect);
        assertEquals(roundRect, clonedRoundRect);
        assertNotSame(roundRect, clonedRoundRect);

        // Non-cloneable custom shape
        Shape nonCloneableShape = new Shape() {
            @Override public java.awt.Rectangle getBounds() { return null; }
            @Override public Rectangle2D getBounds2D() { return null; }
            @Override public boolean contains(double x, double y) { return false; }
            @Override public boolean contains(Point2D p) { return false; }
            @Override public boolean intersects(double x, double y, double w, double h) { return false; }
            @Override public boolean intersects(Rectangle2D r) { return false; }
            @Override public boolean contains(double x, double y, double w, double h) { return false; }
            @Override public boolean contains(Rectangle2D r) { return false; }
            @Override public PathIterator getPathIterator(AffineTransform at) { return null; }
            @Override public PathIterator getPathIterator(AffineTransform at, double flatness) { return null; }
        };
        assertNull(ShapeUtilities.clone(nonCloneableShape));
    }
}