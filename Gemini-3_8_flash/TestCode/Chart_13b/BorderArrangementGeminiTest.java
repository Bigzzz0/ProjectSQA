package org.jfree.chart.block;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.Size2D;
import org.jfree.data.Range;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jfree.chart.block.BorderArrangement
 * Defect Identified: arrangeFF() constructs new Range(0.0, constraint.getWidth() - w[2]) without guarding
 *                    against width - w[2] < 0. When left block width exceeds container width, Range constructor
 *                    throws IllegalArgumentException: Range(double, double): require lower (0.0) <= upper (-X).
 *
 * Branch & Coverage Coverage Matrix:
 * 1. add(Block, Object key):
 *    - key == null (centerBlock)
 *    - key == RectangleEdge.TOP (topBlock)
 *    - key == RectangleEdge.BOTTOM (bottomBlock)
 *    - key == RectangleEdge.LEFT (leftBlock)
 *    - key == RectangleEdge.RIGHT (rightBlock)
 *    - key != null and not RectangleEdge (ClassCastException branch)
 * 2. arrange(BlockContainer, Graphics2D, RectangleConstraint):
 *    - w=NONE, h=NONE -> arrangeNN
 *    - w=NONE, h=FIXED -> RuntimeException ("Not implemented.")
 *    - w=NONE, h=RANGE -> RuntimeException ("Not implemented.")
 *    - w=FIXED, h=NONE -> arrangeFN
 *    - w=FIXED, h=FIXED -> arrangeFF
 *    - w=FIXED, h=RANGE -> arrangeFR
 *    - w=RANGE, h=NONE -> RuntimeException ("Not implemented.")
 *    - w=RANGE, h=FIXED -> RuntimeException ("Not implemented.")
 *    - w=RANGE, h=RANGE -> arrangeRR
 * 3. arrangeNN:
 *    - null containers / all blocks null
 *    - isolated blocks (top only, bottom only, left only, right only, center only)
 *    - all 5 blocks populated, verifying bounds assignment
 * 4. arrangeFR:
 *    - heightRange contains size1.getHeight() -> returns size1
 *    - heightRange does not contain size1.getHeight() -> constrains height, invokes arrangeFF
 * 5. arrangeFN:
 *    - left block width < container width (maxW > 0)
 *    - left block width >= container width (maxW == 0)
 * 6. arrangeRR:
 *    - all blocks null
 *    - all 5 blocks populated, range shifts and bound allocations
 * 7. arrangeFF:
 *    - all 5 blocks populated under normal sizing
 *    - width < leftBlock.width with rightBlock present (Defects4J ground truth fault trigger)
 * 8. clear():
 *    - resets centerBlock, topBlock, bottomBlock, leftBlock, rightBlock
 * 9. equals():
 *    - this == obj, obj == null, non-BorderArrangement instance
 *    - mismatch on topBlock, bottomBlock, leftBlock, rightBlock, centerBlock
 *    - deep structural equivalence
 * 10. Serialization:
 *    - Round-trip writeObject and readObject integrity
 */
public class BorderArrangementGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddAndClearAllEdges() {
        BorderArrangement arrangement = new BorderArrangement();
        Block top = new EmptyBlock(10.0, 10.0);
        Block bottom = new EmptyBlock(10.0, 10.0);
        Block left = new EmptyBlock(10.0, 10.0);
        Block right = new EmptyBlock(10.0, 10.0);
        Block center = new EmptyBlock(10.0, 10.0);

        arrangement.add(top, RectangleEdge.TOP);
        arrangement.add(bottom, RectangleEdge.BOTTOM);
        arrangement.add(left, RectangleEdge.LEFT);
        arrangement.add(right, RectangleEdge.RIGHT);
        arrangement.add(center, null);

        BorderArrangement emptyArrangement = new BorderArrangement();
        assertFalse(arrangement.equals(emptyArrangement));

        arrangement.clear();
        assertTrue(arrangement.equals(emptyArrangement));
    }

    @Test(timeout = 4000)
    public void testArrangeNNWithAllBlocks() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);

        Block top = new EmptyBlock(100.0, 20.0);
        Block bottom = new EmptyBlock(100.0, 30.0);
        Block left = new EmptyBlock(25.0, 50.0);
        Block right = new EmptyBlock(35.0, 40.0);
        Block center = new EmptyBlock(40.0, 60.0);

        arrangement.add(top, RectangleEdge.TOP);
        arrangement.add(bottom, RectangleEdge.BOTTOM);
        arrangement.add(left, RectangleEdge.LEFT);
        arrangement.add(right, RectangleEdge.RIGHT);
        arrangement.add(center, null);

        Size2D size = arrangement.arrange(container, null, RectangleConstraint.NONE);
        assertNotNull(size);
        // width = max(100, 100, 25 + 40 + 35 = 100) = 100.0
        // centerHeight = max(50, 50, 60) = 60.0
        // height = 20 + 30 + 60 = 110.0
        assertEquals(100.0, size.getWidth(), 0.0001);
        assertEquals(110.0, size.getHeight(), 0.0001);

        assertEquals(new Rectangle2D.Double(0.0, 0.0, 100.0, 20.0), top.getBounds());
        assertEquals(new Rectangle2D.Double(0.0, 80.0, 100.0, 30.0), bottom.getBounds());
        assertEquals(new Rectangle2D.Double(0.0, 20.0, 25.0, 60.0), left.getBounds());
        assertEquals(new Rectangle2D.Double(65.0, 20.0, 35.0, 60.0), right.getBounds());
        assertEquals(new Rectangle2D.Double(25.0, 20.0, 40.0, 60.0), center.getBounds());
    }

    @Test(timeout = 4000)
    public void testArrangeNNIndividualBlocks() {
        // Top only
        BorderArrangement arrTop = new BorderArrangement();
        BlockContainer cTop = new BlockContainer(arrTop);
        Block top = new EmptyBlock(40.0, 15.0);
        arrTop.add(top, RectangleEdge.TOP);
        Size2D sizeTop = arrTop.arrange(cTop, null, RectangleConstraint.NONE);
        assertEquals(40.0, sizeTop.getWidth(), 0.0001);
        assertEquals(15.0, sizeTop.getHeight(), 0.0001);
        assertEquals(new Rectangle2D.Double(0.0, 0.0, 40.0, 15.0), top.getBounds());

        // Bottom only
        BorderArrangement arrBottom = new BorderArrangement();
        BlockContainer cBottom = new BlockContainer(arrBottom);
        Block bottom = new EmptyBlock(50.0, 25.0);
        arrBottom.add(bottom, RectangleEdge.BOTTOM);
        Size2D sizeBottom = arrBottom.arrange(cBottom, null, RectangleConstraint.NONE);
        assertEquals(50.0, sizeBottom.getWidth(), 0.0001);
        assertEquals(25.0, sizeBottom.getHeight(), 0.0001);
        assertEquals(new Rectangle2D.Double(0.0, 0.0, 50.0, 25.0), bottom.getBounds());

        // Left only
        BorderArrangement arrLeft = new BorderArrangement();
        BlockContainer cLeft = new BlockContainer(arrLeft);
        Block left = new EmptyBlock(30.0, 45.0);
        arrLeft.add(left, RectangleEdge.LEFT);
        Size2D sizeLeft = arrLeft.arrange(cLeft, null, RectangleConstraint.NONE);
        assertEquals(30.0, sizeLeft.getWidth(), 0.0001);
        assertEquals(45.0, sizeLeft.getHeight(), 0.0001);
        assertEquals(new Rectangle2D.Double(0.0, 0.0, 30.0, 45.0), left.getBounds());

        // Right only
        BorderArrangement arrRight = new BorderArrangement();
        BlockContainer cRight = new BlockContainer(arrRight);
        Block right = new EmptyBlock(35.0, 55.0);
        arrRight.add(right, RectangleEdge.RIGHT);
        Size2D sizeRight = arrRight.arrange(cRight, null, RectangleConstraint.NONE);
        assertEquals(35.0, sizeRight.getWidth(), 0.0001);
        assertEquals(55.0, sizeRight.getHeight(), 0.0001);
        assertEquals(new Rectangle2D.Double(0.0, 0.0, 35.0, 55.0), right.getBounds());

        // Center only
        BorderArrangement arrCenter = new BorderArrangement();
        BlockContainer cCenter = new BlockContainer(arrCenter);
        Block center = new EmptyBlock(60.0, 70.0);
        arrCenter.add(center, null);
        Size2D sizeCenter = arrCenter.arrange(cCenter, null, RectangleConstraint.NONE);
        assertEquals(60.0, sizeCenter.getWidth(), 0.0001);
        assertEquals(70.0, sizeCenter.getHeight(), 0.0001);
        assertEquals(new Rectangle2D.Double(0.0, 0.0, 60.0, 70.0), center.getBounds());
    }

    @Test(timeout = 4000)
    public void testArrangeFNNormal() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);

        Block top = new EmptyBlock(50.0, 10.0);
        Block bottom = new EmptyBlock(50.0, 15.0);
        Block left = new EmptyBlock(20.0, 30.0);
        Block right = new EmptyBlock(25.0, 20.0);
        Block center = new EmptyBlock(10.0, 25.0);

        arrangement.add(top, RectangleEdge.TOP);
        arrangement.add(bottom, RectangleEdge.BOTTOM);
        arrangement.add(left, RectangleEdge.LEFT);
        arrangement.add(right, RectangleEdge.RIGHT);
        arrangement.add(center, null);

        RectangleConstraint constraint = new RectangleConstraint(100.0, null,
                LengthConstraintType.FIXED, 0.0, null, LengthConstraintType.NONE);
        Size2D size = arrangement.arrange(container, null, constraint);

        assertNotNull(size);
        assertEquals(100.0, size.getWidth(), 0.0001);
        assertEquals(55.0, size.getHeight(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testArrangeFRHeightWithinRange() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);

        Block center = new EmptyBlock(50.0, 40.0);
        arrangement.add(center, null);

        // Fixed width 100.0, Range height [20.0, 80.0]. Center height is 40.0 (in range).
        RectangleConstraint constraint = new RectangleConstraint(100.0, null,
                LengthConstraintType.FIXED, 0.0, new Range(20.0, 80.0), LengthConstraintType.RANGE);

        Size2D size = arrangement.arrange(container, null, constraint);
        assertNotNull(size);
        assertEquals(100.0, size.getWidth(), 0.0001);
        assertEquals(40.0, size.getHeight(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testArrangeFRHeightOutOfRangeRequiresConstrain() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);

        Block center = new EmptyBlock(50.0, 40.0);
        arrangement.add(center, null);

        // Fixed width 100.0, Range height [60.0, 100.0]. Center height 40.0 is below min range.
        RectangleConstraint constraint = new RectangleConstraint(100.0, null,
                LengthConstraintType.FIXED, 0.0, new Range(60.0, 100.0), LengthConstraintType.RANGE);

        Size2D size = arrangement.arrange(container, null, constraint);
        assertNotNull(size);
        assertEquals(100.0, size.getWidth(), 0.0001);
        assertEquals(60.0, size.getHeight(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testArrangeRRWithAllBlocks() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);

        Block top = new EmptyBlock(30.0, 10.0);
        Block bottom = new EmptyBlock(40.0, 15.0);
        Block left = new EmptyBlock(20.0, 40.0);
        Block right = new EmptyBlock(25.0, 35.0);
        Block center = new EmptyBlock(30.0, 20.0);

        arrangement.add(top, RectangleEdge.TOP);
        arrangement.add(bottom, RectangleEdge.BOTTOM);
        arrangement.add(left, RectangleEdge.LEFT);
        arrangement.add(right, RectangleEdge.RIGHT);
        arrangement.add(center, null);

        RectangleConstraint constraint = new RectangleConstraint(
                new Range(50.0, 200.0), new Range(50.0, 200.0));
        Size2D size = arrangement.arrange(container, null, constraint);

        assertNotNull(size);
        // width = max(30, 40, 20 + 30 + 25) = 75.0
        // height = 10 + 15 + max(40, 40, 20) = 65.0
        assertEquals(75.0, size.getWidth(), 0.0001);
        assertEquals(65.0, size.getHeight(), 0.0001);

        assertEquals(new Rectangle2D.Double(0.0, 0.0, 75.0, 10.0), top.getBounds());
        assertEquals(new Rectangle2D.Double(0.0, 50.0, 75.0, 15.0), bottom.getBounds());
        assertEquals(new Rectangle2D.Double(0.0, 10.0, 20.0, 40.0), left.getBounds());
        assertEquals(new Rectangle2D.Double(50.0, 10.0, 25.0, 40.0), right.getBounds());
        assertEquals(new Rectangle2D.Double(20.0, 10.0, 30.0, 40.0), center.getBounds());
    }

    @Test(timeout = 4000)
    public void testArrangeFFNormal() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);

        Block top = new EmptyBlock(200.0, 20.0);
        Block bottom = new EmptyBlock(200.0, 30.0);
        Block left = new EmptyBlock(40.0, 50.0);
        Block right = new EmptyBlock(50.0, 50.0);
        Block center = new EmptyBlock(110.0, 50.0);

        arrangement.add(top, RectangleEdge.TOP);
        arrangement.add(bottom, RectangleEdge.BOTTOM);
        arrangement.add(left, RectangleEdge.LEFT);
        arrangement.add(right, RectangleEdge.RIGHT);
        arrangement.add(center, null);

        RectangleConstraint constraint = new RectangleConstraint(200.0, 150.0);
        Size2D size = arrangement.arrange(container, null, constraint);

        assertNotNull(size);
        assertEquals(200.0, size.getWidth(), 0.0001);
        assertEquals(150.0, size.getHeight(), 0.0001);

        // h[2] = 150 - 30 - 20 = 100.0
        assertEquals(new Rectangle2D.Double(0.0, 0.0, 200.0, 20.0), top.getBounds());
        assertEquals(new Rectangle2D.Double(0.0, 120.0, 200.0, 30.0), bottom.getBounds());
        assertEquals(new Rectangle2D.Double(0.0, 20.0, 40.0, 100.0), left.getBounds());
        assertEquals(new Rectangle2D.Double(150.0, 20.0, 50.0, 100.0), right.getBounds());
        assertEquals(new Rectangle2D.Double(40.0, 20.0, 110.0, 100.0), center.getBounds());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testArrangeNNEmptyContainer() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);
        Size2D size = arrangement.arrange(container, null, RectangleConstraint.NONE);
        assertNotNull(size);
        assertEquals(0.0, size.getWidth(), 0.0001);
        assertEquals(0.0, size.getHeight(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testArrangeFFEmptyContainer() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);
        RectangleConstraint constraint = new RectangleConstraint(50.0, 80.0);
        Size2D size = arrangement.arrange(container, null, constraint);
        assertNotNull(size);
        assertEquals(50.0, size.getWidth(), 0.0001);
        assertEquals(80.0, size.getHeight(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testArrangeRREmptyContainer() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);
        RectangleConstraint constraint = new RectangleConstraint(new Range(0.0, 100.0), new Range(0.0, 100.0));
        Size2D size = arrangement.arrange(container, null, constraint);
        assertNotNull(size);
        assertEquals(0.0, size.getWidth(), 0.0001);
        assertEquals(0.0, size.getHeight(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testArrangeFNLeftBlockLargerThanWidth() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);

        // w[2] (50.0) >= width (30.0), triggering maxW = 0.0 branch
        Block left = new EmptyBlock(50.0, 20.0);
        Block right = new EmptyBlock(10.0, 20.0);
        arrangement.add(left, RectangleEdge.LEFT);
        arrangement.add(right, RectangleEdge.RIGHT);

        RectangleConstraint constraint = new RectangleConstraint(30.0, null,
                LengthConstraintType.FIXED, 0.0, null, LengthConstraintType.NONE);
        Size2D size = arrangement.arrange(container, null, constraint);
        assertNotNull(size);
        assertEquals(30.0, size.getWidth(), 0.0001);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Exposes defect in arrangeFF() where width is smaller than left block width:
     * When constraint.getWidth() - w[2] < 0 and rightBlock != null, constructing
     * Range(0.0, constraint.getWidth() - w[2]) throws IllegalArgumentException
     * (e.g., "Range(double, double): require lower (0.0) <= upper (-X)").
     *
     * The test asserts that arrangeFF() cleanly handles width constraint smaller
     * than left block without throwing IllegalArgumentException.
     */
    @Test(timeout = 4000)
    public void testSizingWithWidthConstraintDefect() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);

        // Left block returns width 20.0 unconditionally
        Block left = new EmptyBlock(20.0, 10.0) {
            @Override
            public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
                return new Size2D(20.0, 10.0);
            }
        };
        Block right = new EmptyBlock(10.0, 10.0);
        arrangement.add(left, RectangleEdge.LEFT);
        arrangement.add(right, RectangleEdge.RIGHT);

        // Container constrained to fixed width 15.0 and fixed height 40.0.
        // width (15.0) - w[2] (20.0) = -5.0 < 0.0!
        RectangleConstraint constraint = new RectangleConstraint(15.0, 40.0);
        Size2D size = arrangement.arrange(container, null, constraint);

        assertNotNull("Arrangement should return a non-null size even when width is tight", size);
        assertEquals(15.0, size.getWidth(), 0.0001);
        assertEquals(40.0, size.getHeight(), 0.0001);
    }

    /**
     * Secondary defect check via arrangeFR() pathway that flows into arrangeFF().
     */
    @Test(timeout = 4000)
    public void testArrangeFRDelegatingToFFWithNarrowWidthDefect() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);

        Block left = new EmptyBlock(30.0, 20.0) {
            @Override
            public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
                return new Size2D(30.0, 20.0);
            }
        };
        Block right = new EmptyBlock(15.0, 20.0);
        arrangement.add(left, RectangleEdge.LEFT);
        arrangement.add(right, RectangleEdge.RIGHT);

        // Fixed width 20.0 < left block (30.0), Range height [5.0, 10.0] forcing height constraint to FF
        RectangleConstraint constraint = new RectangleConstraint(20.0, null,
                LengthConstraintType.FIXED, 0.0, new Range(5.0, 10.0), LengthConstraintType.RANGE);

        Size2D size = arrangement.arrange(container, null, constraint);
        assertNotNull(size);
        assertEquals(20.0, size.getWidth(), 0.0001);
        assertEquals(10.0, size.getHeight(), 0.0001);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testArrangeWidthNoneHeightFixedThrows() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);
        RectangleConstraint constraint = new RectangleConstraint(0.0, null,
                LengthConstraintType.NONE, 50.0, null, LengthConstraintType.FIXED);
        arrangement.arrange(container, null, constraint);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testArrangeWidthNoneHeightRangeThrows() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);
        RectangleConstraint constraint = new RectangleConstraint(0.0, null,
                LengthConstraintType.NONE, 0.0, new Range(10.0, 50.0), LengthConstraintType.RANGE);
        arrangement.arrange(container, null, constraint);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testArrangeWidthRangeHeightNoneThrows() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);
        RectangleConstraint constraint = new RectangleConstraint(0.0, new Range(10.0, 50.0),
                LengthConstraintType.RANGE, 0.0, null, LengthConstraintType.NONE);
        arrangement.arrange(container, null, constraint);
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testArrangeWidthRangeHeightFixedThrows() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);
        RectangleConstraint constraint = new RectangleConstraint(0.0, new Range(10.0, 50.0),
                LengthConstraintType.RANGE, 50.0, null, LengthConstraintType.FIXED);
        arrangement.arrange(container, null, constraint);
    }

    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testAddInvalidKeyTypeThrows() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(10.0, 10.0), "INVALID_KEY");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        BorderArrangement ba1 = new BorderArrangement();
        BorderArrangement ba2 = new BorderArrangement();

        // Reflexive and symmetric empty
        assertTrue(ba1.equals(ba1));
        assertTrue(ba1.equals(ba2));
        assertTrue(ba2.equals(ba1));
        assertFalse(ba1.equals(null));
        assertFalse(ba1.equals("Some String"));

        Block b1 = new EmptyBlock(10.0, 10.0);
        Block b2 = new EmptyBlock(20.0, 20.0);

        // Top edge differentiation
        ba1.add(b1, RectangleEdge.TOP);
        assertFalse(ba1.equals(ba2));
        ba2.add(b2, RectangleEdge.TOP);
        assertFalse(ba1.equals(ba2));
        ba2.add(b1, RectangleEdge.TOP);
        assertTrue(ba1.equals(ba2));

        // Bottom edge differentiation
        ba1.add(b1, RectangleEdge.BOTTOM);
        assertFalse(ba1.equals(ba2));
        ba2.add(b1, RectangleEdge.BOTTOM);
        assertTrue(ba1.equals(ba2));

        // Left edge differentiation
        ba1.add(b1, RectangleEdge.LEFT);
        assertFalse(ba1.equals(ba2));
        ba2.add(b1, RectangleEdge.LEFT);
        assertTrue(ba1.equals(ba2));

        // Right edge differentiation
        ba1.add(b1, RectangleEdge.RIGHT);
        assertFalse(ba1.equals(ba2));
        ba2.add(b1, RectangleEdge.RIGHT);
        assertTrue(ba1.equals(ba2));

        // Center edge differentiation
        ba1.add(b1, null);
        assertFalse(ba1.equals(ba2));
        ba2.add(b1, null);
        assertTrue(ba1.equals(ba2));
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        BorderArrangement original = new BorderArrangement();
        original.add(new EmptyBlock(10.0, 20.0), RectangleEdge.TOP);
        original.add(new EmptyBlock(30.0, 40.0), RectangleEdge.BOTTOM);
        original.add(new EmptyBlock(50.0, 60.0), RectangleEdge.LEFT);
        original.add(new EmptyBlock(70.0, 80.0), RectangleEdge.RIGHT);
        original.add(new EmptyBlock(90.0, 100.0), null);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(original);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        BorderArrangement deserialized = (BorderArrangement) in.readObject();
        in.close();

        assertEquals(original, deserialized);
        assertNotSame(original, deserialized);
    }
}