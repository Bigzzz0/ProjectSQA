package org.jfree.chart.block;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.Size2D;
import org.jfree.data.Range;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: BorderArrangement
 * 
 * Decision Branches Targeted:
 * 1. add() method: key == null (center), edge == TOP/BOTTOM/LEFT/RIGHT
 * 2. arrange() method: w/h constraint type combinations (NONE, FIXED, RANGE)
 * 3. arrangeNN(): null checks for each block position
 * 4. arrangeFN(): null checks, width calculations for left/right/center
 * 5. arrangeFR(): height range containment check
 * 6. arrangeRR(): range shifting operations, null checks
 * 7. arrangeFF(): width/height calculations, null checks
 * 8. clear(): null assignment to all blocks
 * 9. equals(): null checks, instanceof check, field comparisons
 * 
 * Boundary Conditions:
 * - Null blocks at each position
 * - Zero width/height constraints
 * - Negative width/height values (defect trigger)
 * - Range constraints with lower > upper (defect trigger)
 * - Maximum width/height values
 * 
 * Defect-Specific Target (Defects4J):
 * - arrangeFF() with constraint where width is too small for left+right blocks
 * - This causes negative remaining width, leading to Range(double, double) 
 *   with lower > upper in RectangleConstraint creation
 * - Test: testArrangeFFWithInsufficientWidth()
 */
public class BorderArrangementDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================
    
    @Test(timeout = 4000)
    public void testAddCenterBlock() {
        BorderArrangement arrangement = new BorderArrangement();
        Block block = new EmptyBlock(10, 10);
        arrangement.add(block, null);
        // Verify center block was set (indirectly through arrange)
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        Size2D size = container.arrange(g2);
        assertEquals(10.0, size.getWidth(), 0.001);
        assertEquals(10.0, size.getHeight(), 0.001);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testAddTopBlock() {
        BorderArrangement arrangement = new BorderArrangement();
        Block block = new EmptyBlock(50, 20);
        arrangement.add(block, RectangleEdge.TOP);
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        Size2D size = container.arrange(g2);
        assertEquals(50.0, size.getWidth(), 0.001);
        assertEquals(20.0, size.getHeight(), 0.001);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testAddBottomBlock() {
        BorderArrangement arrangement = new BorderArrangement();
        Block block = new EmptyBlock(60, 30);
        arrangement.add(block, RectangleEdge.BOTTOM);
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        Size2D size = container.arrange(g2);
        assertEquals(60.0, size.getWidth(), 0.001);
        assertEquals(30.0, size.getHeight(), 0.001);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testAddLeftBlock() {
        BorderArrangement arrangement = new BorderArrangement();
        Block block = new EmptyBlock(20, 40);
        arrangement.add(block, RectangleEdge.LEFT);
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        Size2D size = container.arrange(g2);
        assertEquals(20.0, size.getWidth(), 0.001);
        assertEquals(40.0, size.getHeight(), 0.001);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testAddRightBlock() {
        BorderArrangement arrangement = new BorderArrangement();
        Block block = new EmptyBlock(30, 50);
        arrangement.add(block, RectangleEdge.RIGHT);
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        Size2D size = container.arrange(g2);
        assertEquals(30.0, size.getWidth(), 0.001);
        assertEquals(50.0, size.getHeight(), 0.001);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testClear() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        arrangement.add(new EmptyBlock(10, 10), RectangleEdge.BOTTOM);
        arrangement.add(new EmptyBlock(10, 10), RectangleEdge.LEFT);
        arrangement.add(new EmptyBlock(10, 10), RectangleEdge.RIGHT);
        arrangement.add(new EmptyBlock(10, 10), null);
        arrangement.clear();
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        Size2D size = container.arrange(g2);
        assertEquals(0.0, size.getWidth(), 0.001);
        assertEquals(0.0, size.getHeight(), 0.001);
        g2.dispose();
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testArrangeNNWithAllBlocks() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(100, 20), RectangleEdge.TOP);
        arrangement.add(new EmptyBlock(100, 30), RectangleEdge.BOTTOM);
        arrangement.add(new EmptyBlock(40, 50), RectangleEdge.LEFT);
        arrangement.add(new EmptyBlock(60, 50), RectangleEdge.RIGHT);
        arrangement.add(new EmptyBlock(50, 40), null);
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB).createGraphics();
        Size2D size = container.arrange(g2);
        // width = max(100, 100, 40+50+60) = 150
        assertEquals(150.0, size.getWidth(), 0.001);
        // height = 20 + 30 + max(50, 50, 40) = 100
        assertEquals(100.0, size.getHeight(), 0.001);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testArrangeFNWithFixedWidth() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(100, 20), RectangleEdge.TOP);
        arrangement.add(new EmptyBlock(100, 30), RectangleEdge.BOTTOM);
        arrangement.add(new EmptyBlock(40, 50), RectangleEdge.LEFT);
        arrangement.add(new EmptyBlock(60, 50), RectangleEdge.RIGHT);
        arrangement.add(new EmptyBlock(50, 40), null);
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB).createGraphics();
        RectangleConstraint constraint = new RectangleConstraint(200.0, null, 
                LengthConstraintType.FIXED, 0.0, null, LengthConstraintType.NONE);
        Size2D size = container.arrange(g2, constraint);
        assertEquals(200.0, size.getWidth(), 0.001);
        // height = 20 + 30 + max(50, 50, 40) = 100
        assertEquals(100.0, size.getHeight(), 0.001);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testArrangeFNWithNarrowWidth() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(100, 20), RectangleEdge.TOP);
        arrangement.add(new EmptyBlock(100, 30), RectangleEdge.BOTTOM);
        arrangement.add(new EmptyBlock(80, 50), RectangleEdge.LEFT);
        arrangement.add(new EmptyBlock(80, 50), RectangleEdge.RIGHT);
        arrangement.add(new EmptyBlock(50, 40), null);
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB).createGraphics();
        RectangleConstraint constraint = new RectangleConstraint(100.0, null, 
                LengthConstraintType.FIXED, 0.0, null, LengthConstraintType.NONE);
        Size2D size = container.arrange(g2, constraint);
        assertEquals(100.0, size.getWidth(), 0.001);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testArrangeFRWithHeightInRange() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(100, 50), RectangleEdge.TOP);
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB).createGraphics();
        RectangleConstraint constraint = new RectangleConstraint(100.0, null, 
                LengthConstraintType.FIXED, 0.0, new Range(40.0, 60.0), LengthConstraintType.RANGE);
        Size2D size = container.arrange(g2, constraint);
        assertEquals(100.0, size.getWidth(), 0.001);
        assertEquals(50.0, size.getHeight(), 0.001);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testArrangeFRWithHeightOutOfRange() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(100, 80), RectangleEdge.TOP);
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB).createGraphics();
        RectangleConstraint constraint = new RectangleConstraint(100.0, null, 
                LengthConstraintType.FIXED, 0.0, new Range(40.0, 60.0), LengthConstraintType.RANGE);
        Size2D size = container.arrange(g2, constraint);
        assertEquals(100.0, size.getWidth(), 0.001);
        assertEquals(60.0, size.getHeight(), 0.001);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testArrangeRRWithAllBlocks() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(100, 20), RectangleEdge.TOP);
        arrangement.add(new EmptyBlock(100, 30), RectangleEdge.BOTTOM);
        arrangement.add(new EmptyBlock(40, 50), RectangleEdge.LEFT);
        arrangement.add(new EmptyBlock(60, 50), RectangleEdge.RIGHT);
        arrangement.add(new EmptyBlock(50, 40), null);
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB).createGraphics();
        RectangleConstraint constraint = new RectangleConstraint(
                new Range(100.0, 200.0), new Range(50.0, 150.0));
        Size2D size = container.arrange(g2, constraint);
        assertTrue(size.getWidth() >= 100.0 && size.getWidth() <= 200.0);
        assertTrue(size.getHeight() >= 50.0 && size.getHeight() <= 150.0);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testArrangeFFWithExactFit() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(100, 20), RectangleEdge.TOP);
        arrangement.add(new EmptyBlock(100, 30), RectangleEdge.BOTTOM);
        arrangement.add(new EmptyBlock(40, 50), RectangleEdge.LEFT);
        arrangement.add(new EmptyBlock(60, 50), RectangleEdge.RIGHT);
        arrangement.add(new EmptyBlock(50, 40), null);
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB).createGraphics();
        RectangleConstraint constraint = new RectangleConstraint(200.0, 100.0);
        Size2D size = container.arrange(g2, constraint);
        assertEquals(200.0, size.getWidth(), 0.001);
        assertEquals(100.0, size.getHeight(), 0.001);
        g2.dispose();
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect-specific test targeting the known bug in arrangeFF().
     * When the fixed width is too small to accommodate left and right blocks,
     * the remaining width for center becomes negative, causing 
     * Range(double, double) to receive lower > upper.
     */
    @Test(timeout = 4000)
    public void testArrangeFFWithInsufficientWidth() {
        BorderArrangement arrangement = new BorderArrangement();
        // Left block requires 80 width
        arrangement.add(new EmptyBlock(80, 50), RectangleEdge.LEFT);
        // Right block requires 80 width
        arrangement.add(new EmptyBlock(80, 50), RectangleEdge.RIGHT);
        // Center block
        arrangement.add(new EmptyBlock(50, 40), null);
        // Top block
        arrangement.add(new EmptyBlock(100, 20), RectangleEdge.TOP);
        // Bottom block
        arrangement.add(new EmptyBlock(100, 30), RectangleEdge.BOTTOM);
        
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB).createGraphics();
        
        // Fixed width of 100 is insufficient for left(80) + right(80) = 160
        // This should trigger the bug where remaining width = 100 - 80 - 80 = -60
        // leading to Range(0.0, -60.0) which throws IllegalArgumentException
        RectangleConstraint constraint = new RectangleConstraint(100.0, 100.0);
        
        try {
            Size2D size = container.arrange(g2, constraint);
            // If no exception, verify the result is reasonable
            assertEquals(100.0, size.getWidth(), 0.001);
            assertEquals(100.0, size.getHeight(), 0.001);
        } catch (IllegalArgumentException e) {
            // Expected: the bug causes this exception
            // In a fixed version, this should not throw
            fail("Bug detected: IllegalArgumentException thrown due to negative range: " + e.getMessage());
        }
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testArrangeFFWithZeroWidth() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(80, 50), RectangleEdge.LEFT);
        arrangement.add(new EmptyBlock(80, 50), RectangleEdge.RIGHT);
        arrangement.add(new EmptyBlock(50, 40), null);
        
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB).createGraphics();
        
        // Zero width should also trigger the bug
        RectangleConstraint constraint = new RectangleConstraint(0.0, 100.0);
        
        try {
            Size2D size = container.arrange(g2, constraint);
            assertEquals(0.0, size.getWidth(), 0.001);
        } catch (IllegalArgumentException e) {
            fail("Bug detected: IllegalArgumentException thrown for zero width: " + e.getMessage());
        }
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testArrangeFFWithLeftBlockOnlyAndNarrowWidth() {
        BorderArrangement arrangement = new BorderArrangement();
        arrangement.add(new EmptyBlock(80, 50), RectangleEdge.LEFT);
        arrangement.add(new EmptyBlock(50, 40), null);
        
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB).createGraphics();
        
        // Width less than left block width
        RectangleConstraint constraint = new RectangleConstraint(50.0, 100.0);
        
        try {
            Size2D size = container.arrange(g2, constraint);
            assertEquals(50.0, size.getWidth(), 0.001);
        } catch (IllegalArgumentException e) {
            fail("Bug detected: IllegalArgumentException thrown: " + e.getMessage());
        }
        g2.dispose();
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testArrangeWithUnimplementedCombinations() {
        BorderArrangement arrangement = new BorderArrangement();
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        
        // NONE width + FIXED height - throws RuntimeException
        try {
            RectangleConstraint c1 = new RectangleConstraint(null, 
                    LengthConstraintType.NONE, 100.0, LengthConstraintType.FIXED);
            container.arrange(g2, c1);
            fail("Expected RuntimeException for NONE width + FIXED height");
        } catch (RuntimeException e) {
            // Expected
        }
        
        // NONE width + RANGE height - throws RuntimeException
        try {
            RectangleConstraint c2 = new RectangleConstraint(null, 
                    LengthConstraintType.NONE, new Range(50.0, 100.0), LengthConstraintType.RANGE);
            container.arrange(g2, c2);
            fail("Expected RuntimeException for NONE width + RANGE height");
        } catch (RuntimeException e) {
            // Expected
        }
        
        // RANGE width + NONE height - throws RuntimeException
        try {
            RectangleConstraint c3 = new RectangleConstraint(new Range(50.0, 100.0), 
                    LengthConstraintType.RANGE, null, LengthConstraintType.NONE);
            container.arrange(g2, c3);
            fail("Expected RuntimeException for RANGE width + NONE height");
        } catch (RuntimeException e) {
            // Expected
        }
        
        // RANGE width + FIXED height - throws RuntimeException
        try {
            RectangleConstraint c4 = new RectangleConstraint(new Range(50.0, 100.0), 
                    LengthConstraintType.RANGE, 100.0, LengthConstraintType.FIXED);
            container.arrange(g2, c4);
            fail("Expected RuntimeException for RANGE width + FIXED height");
        } catch (RuntimeException e) {
            // Expected
        }
        
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testAddWithInvalidEdge() {
        BorderArrangement arrangement = new BorderArrangement();
        // Adding with an invalid key should not set any block
        arrangement.add(new EmptyBlock(10, 10), "INVALID");
        BlockContainer container = new BlockContainer(arrangement);
        Graphics2D g2 = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
        Size2D size = container.arrange(g2);
        assertEquals(0.0, size.getWidth(), 0.001);
        assertEquals(0.0, size.getHeight(), 0.001);
        g2.dispose();
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        BorderArrangement arrangement = new BorderArrangement();
        assertTrue(arrangement.equals(arrangement));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        BorderArrangement arrangement = new BorderArrangement();
        assertFalse(arrangement.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        BorderArrangement arrangement = new BorderArrangement();
        assertFalse(arrangement.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentBlocks() {
        BorderArrangement arrangement1 = new BorderArrangement();
        BorderArrangement arrangement2 = new BorderArrangement();
        
        // Initially equal
        assertTrue(arrangement1.equals(arrangement2));
        
        // Different top block
        arrangement1.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        assertFalse(arrangement1.equals(arrangement2));
        
        // Make equal again
        arrangement2.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        assertTrue(arrangement1.equals(arrangement2));
        
        // Different bottom block
        arrangement1.add(new EmptyBlock(20, 20), RectangleEdge.BOTTOM);
        assertFalse(arrangement1.equals(arrangement2));
        
        arrangement2.add(new EmptyBlock(20, 20), RectangleEdge.BOTTOM);
        assertTrue(arrangement1.equals(arrangement2));
        
        // Different left block
        arrangement1.add(new EmptyBlock(30, 30), RectangleEdge.LEFT);
        assertFalse(arrangement1.equals(arrangement2));
        
        arrangement2.add(new EmptyBlock(30, 30), RectangleEdge.LEFT);
        assertTrue(arrangement1.equals(arrangement2));
        
        // Different right block
        arrangement1.add(new EmptyBlock(40, 40), RectangleEdge.RIGHT);
        assertFalse(arrangement1.equals(arrangement2));
        
        arrangement2.add(new EmptyBlock(40, 40), RectangleEdge.RIGHT);
        assertTrue(arrangement1.equals(arrangement2));
        
        // Different center block
        arrangement1.add(new EmptyBlock(50, 50), null);
        assertFalse(arrangement1.equals(arrangement2));
        
        arrangement2.add(new EmptyBlock(50, 50), null);
        assertTrue(arrangement1.equals(arrangement2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullBlocks() {
        BorderArrangement arrangement1 = new BorderArrangement();
        BorderArrangement arrangement2 = new BorderArrangement();
        
        // Both have null blocks - should be equal
        assertTrue(arrangement1.equals(arrangement2));
        
        // One has a block, other doesn't
        arrangement1.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        assertFalse(arrangement1.equals(arrangement2));
        
        // Both have blocks
        arrangement2.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        assertTrue(arrangement1.equals(arrangement2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentBlockTypes() {
        BorderArrangement arrangement1 = new BorderArrangement();
        BorderArrangement arrangement2 = new BorderArrangement();
        
        // Use different block implementations
        arrangement1.add(new EmptyBlock(10, 10), RectangleEdge.TOP);
        arrangement2.add(new EmptyBlock(10, 10) {
            // Anonymous subclass
        }, RectangleEdge.TOP);
        
        // Should not be equal if blocks are not equal
        assertFalse(arrangement1.equals(arrangement2));
    }
}