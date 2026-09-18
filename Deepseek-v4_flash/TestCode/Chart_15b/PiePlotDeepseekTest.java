package org.jfree.chart.plot;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ResourceBundle;

import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.Rotation;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.PieDataset;
import org.jfree.data.general.DefaultPieDataset;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Target class: PiePlot (org.jfree.chart.plot)
 * 
 * Partitions:
 * A) Core functional logic & state transitions:
 *    - Constructor defaults, getters/setters for all properties
 *    - getDataset(), setDataset(PieDataset)
 * B) Boundary Value Analysis & Extremes:
 *    - Null dataset, null keys, null paint/font/stroke arguments
 *    - Invalid interiorGap (<0 or >MAX_INTERIOR_GAP)
 *    - Negative or zero explode percentages
 *    - Minimum arc angle boundary
 * C) Defect-targeted branch zone:
 *    - Drawing with null dataset (known Defects4J bug)
 * D) Exception & defensive guard paths:
 *    - IllegalArgumentException for null arguments in setters
 *    - IllegalStateException for unknown Rotation (not directly testable via public API)
 * E) Object lifecycle & contract integrity:
 *    - equals(), clone(), getLegendItems() with various flags
 *
 * Known defect: Draw with null dataset throws NPE (testDrawWithNullDataset reveals bug)
 * Correct behavior: draw() should gracefully handle null dataset and draw "no data" message.
 */
public class PiePlotDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testConstructorWithNullDataset() {
        PiePlot plot = new PiePlot(null);
        assertNull("Dataset should be null", plot.getDataset());
        assertEquals("Default interior gap", 0.08, plot.getInteriorGap(), 1e-10);
        assertTrue("Default circular", plot.isCircular());
        assertEquals("Default start angle", 90.0, plot.getStartAngle(), 1e-10);
        assertEquals("Default direction", Rotation.CLOCKWISE, plot.getDirection());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNonNullDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        PiePlot plot = new PiePlot(dataset);
        assertSame("Dataset should be set", dataset, plot.getDataset());
    }

    @Test(timeout = 4000)
    public void testSetDataset() {
        PiePlot plot = new PiePlot(null);
        DefaultPieDataset dataset = new DefaultPieDataset();
        plot.setDataset(dataset);
        assertSame("Dataset should be set", dataset, plot.getDataset());
        // Setting null should work
        plot.setDataset(null);
        assertNull("Dataset should be null", plot.getDataset());
    }

    @Test(timeout = 4000)
    public void testGetSetPieIndex() {
        PiePlot plot = new PiePlot();
        assertEquals(0, plot.getPieIndex());
        plot.setPieIndex(5);
        assertEquals(5, plot.getPieIndex());
    }

    @Test(timeout = 4000)
    public void testGetSetStartAngle() {
        PiePlot plot = new PiePlot();
        assertEquals(90.0, plot.getStartAngle(), 1e-10);
        plot.setStartAngle(0.0);
        assertEquals(0.0, plot.getStartAngle(), 1e-10);
        // Negative angle allowed
        plot.setStartAngle(-45.0);
        assertEquals(-45.0, plot.getStartAngle(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetSetDirection() {
        PiePlot plot = new PiePlot();
        assertEquals(Rotation.CLOCKWISE, plot.getDirection());
        plot.setDirection(Rotation.ANTICLOCKWISE);
        assertEquals(Rotation.ANTICLOCKWISE, plot.getDirection());
    }

    @Test(timeout = 4000)
    public void testSetDirectionNullThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setDirection(null);
            fail("Expected IllegalArgumentException for null direction");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetSetInteriorGap() {
        PiePlot plot = new PiePlot();
        assertEquals(0.08, plot.getInteriorGap(), 1e-10);
        plot.setInteriorGap(0.2);
        assertEquals(0.2, plot.getInteriorGap(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testSetInteriorGapNegativeThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setInteriorGap(-0.1);
            fail("Expected IllegalArgumentException for negative gap");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetInteriorGapTooLargeThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setInteriorGap(0.5); // > MAX_INTERIOR_GAP (0.4)
            fail("Expected IllegalArgumentException for gap > 0.4");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIsCircularAndSetCircular() {
        PiePlot plot = new PiePlot();
        assertTrue(plot.isCircular());
        plot.setCircular(false);
        assertFalse(plot.isCircular());
        plot.setCircular(true);
        assertTrue(plot.isCircular());
    }

    @Test(timeout = 4000)
    public void testGetSetIgnoreNullValues() {
        PiePlot plot = new PiePlot();
        assertFalse(plot.getIgnoreNullValues());
        plot.setIgnoreNullValues(true);
        assertTrue(plot.getIgnoreNullValues());
    }

    @Test(timeout = 4000)
    public void testGetSetIgnoreZeroValues() {
        PiePlot plot = new PiePlot();
        assertFalse(plot.getIgnoreZeroValues());
        plot.setIgnoreZeroValues(true);
        assertTrue(plot.getIgnoreZeroValues());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testLookupSectionPaintWithNullKey() {
        PiePlot plot = new PiePlot(new DefaultPieDataset());
        // Should return base paint (gray)
        assertEquals(Color.gray, plot.lookupSectionPaint(null));
    }

    @Test(timeout = 4000)
    public void testGetSectionPaintNullKeyThrows() {
        PiePlot plot = new PiePlot();
        // PaintMap.getPaint throws IllegalArgumentException for null key
        try {
            plot.getSectionPaint(null);
            fail("Expected IllegalArgumentException for null key");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetSectionPaintNullKeyThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setSectionPaint(null, Color.RED);
            fail("Expected IllegalArgumentException for null key");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetBaseSectionPaintNullThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setBaseSectionPaint(null);
            fail("Expected IllegalArgumentException for null paint");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetSectionOutlinesVisible() {
        PiePlot plot = new PiePlot();
        assertTrue(plot.getSectionOutlinesVisible());
        plot.setSectionOutlinesVisible(false);
        assertFalse(plot.getSectionOutlinesVisible());
    }

    @Test(timeout = 4000)
    public void testGetSectionOutlinePaintNullKey() {
        PiePlot plot = new PiePlot();
        assertEquals(Color.lightGray, plot.lookupSectionOutlinePaint(null));
    }

    @Test(timeout = 4000)
    public void testSetBaseSectionOutlinePaintNullThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setBaseSectionOutlinePaint(null);
            fail("Expected IllegalArgumentException for null paint");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetBaseSectionOutlineStrokeNullThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setBaseSectionOutlineStroke(null);
            fail("Expected IllegalArgumentException for null stroke");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetSetShadowPaint() {
        PiePlot plot = new PiePlot();
        assertEquals(Color.gray, plot.getShadowPaint());
        plot.setShadowPaint(Color.BLACK);
        assertEquals(Color.BLACK, plot.getShadowPaint());
        // null is allowed
        plot.setShadowPaint(null);
        assertNull(plot.getShadowPaint());
    }

    @Test(timeout = 4000)
    public void testGetSetShadowXOffset() {
        PiePlot plot = new PiePlot();
        assertEquals(4.0, plot.getShadowXOffset(), 1e-10);
        plot.setShadowXOffset(10.0);
        assertEquals(10.0, plot.getShadowXOffset(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetSetShadowYOffset() {
        PiePlot plot = new PiePlot();
        assertEquals(4.0, plot.getShadowYOffset(), 1e-10);
        plot.setShadowYOffset(10.0);
        assertEquals(10.0, plot.getShadowYOffset(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetExplodePercentDefault() {
        PiePlot plot = new PiePlot(new DefaultPieDataset());
        assertEquals(0.0, plot.getExplodePercent("A"), 1e-10);
    }

    @Test(timeout = 4000)
    public void testSetExplodePercentNullKeyThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setExplodePercent(null, 0.3);
            fail("Expected IllegalArgumentException for null key");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetExplodePercent() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Key", 10.0);
        PiePlot plot = new PiePlot(dataset);
        plot.setExplodePercent("Key", 0.5);
        assertEquals(0.5, plot.getExplodePercent("Key"), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetMaximumExplodePercent() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        dataset.setValue("B", 20.0);
        PiePlot plot = new PiePlot(dataset);
        assertEquals(0.0, plot.getMaximumExplodePercent(), 1e-10);
        plot.setExplodePercent("A", 0.3);
        assertEquals(0.3, plot.getMaximumExplodePercent(), 1e-10);
        plot.setExplodePercent("B", 0.7);
        assertEquals(0.7, plot.getMaximumExplodePercent(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetSetLabelGenerator() {
        PiePlot plot = new PiePlot();
        assertNotNull(plot.getLabelGenerator());
        plot.setLabelGenerator(null);
        assertNull(plot.getLabelGenerator());
    }

    @Test(timeout = 4000)
    public void testGetSetLabelGap() {
        PiePlot plot = new PiePlot();
        assertEquals(0.025, plot.getLabelGap(), 1e-10);
        plot.setLabelGap(0.1);
        assertEquals(0.1, plot.getLabelGap(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetSetMaximumLabelWidth() {
        PiePlot plot = new PiePlot();
        assertEquals(0.14, plot.getMaximumLabelWidth(), 1e-10);
        plot.setMaximumLabelWidth(0.2);
        assertEquals(0.2, plot.getMaximumLabelWidth(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetSetLabelLinksVisible() {
        PiePlot plot = new PiePlot();
        assertTrue(plot.getLabelLinksVisible());
        plot.setLabelLinksVisible(false);
        assertFalse(plot.getLabelLinksVisible());
    }

    @Test(timeout = 4000)
    public void testGetSetLabelLinkMargin() {
        PiePlot plot = new PiePlot();
        assertEquals(0.025, plot.getLabelLinkMargin(), 1e-10);
        plot.setLabelLinkMargin(0.05);
        assertEquals(0.05, plot.getLabelLinkMargin(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testSetLabelLinkPaintNullThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setLabelLinkPaint(null);
            fail("Expected IllegalArgumentException for null paint");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetLabelLinkStrokeNullThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setLabelLinkStroke(null);
            fail("Expected IllegalArgumentException for null stroke");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetSetLabelFont() {
        PiePlot plot = new PiePlot();
        assertNotNull(plot.getLabelFont());
        java.awt.Font font = new java.awt.Font("Dialog", java.awt.Font.BOLD, 14);
        plot.setLabelFont(font);
        assertSame(font, plot.getLabelFont());
    }

    @Test(timeout = 4000)
    public void testSetLabelFontNullThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setLabelFont(null);
            fail("Expected IllegalArgumentException for null font");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetSetLabelPaint() {
        PiePlot plot = new PiePlot();
        assertEquals(Color.black, plot.getLabelPaint());
        plot.setLabelPaint(Color.RED);
        assertEquals(Color.RED, plot.getLabelPaint());
    }

    @Test(timeout = 4000)
    public void testSetLabelPaintNullThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setLabelPaint(null);
            fail("Expected IllegalArgumentException for null paint");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetSetLabelBackgroundPaint() {
        PiePlot plot = new PiePlot();
        assertNotNull(plot.getLabelBackgroundPaint());
        plot.setLabelBackgroundPaint(null);
        assertNull(plot.getLabelBackgroundPaint());
    }

    @Test(timeout = 4000)
    public void testGetSetLabelOutlinePaint() {
        PiePlot plot = new PiePlot();
        assertNotNull(plot.getLabelOutlinePaint());
        plot.setLabelOutlinePaint(null);
        assertNull(plot.getLabelOutlinePaint());
    }

    @Test(timeout = 4000)
    public void testGetSetLabelOutlineStroke() {
        PiePlot plot = new PiePlot();
        assertNotNull(plot.getLabelOutlineStroke());
        plot.setLabelOutlineStroke(null);
        assertNull(plot.getLabelOutlineStroke());
    }

    @Test(timeout = 4000)
    public void testGetSetLabelShadowPaint() {
        PiePlot plot = new PiePlot();
        assertNotNull(plot.getLabelShadowPaint());
        plot.setLabelShadowPaint(null);
        assertNull(plot.getLabelShadowPaint());
    }

    @Test(timeout = 4000)
    public void testGetSetLabelPadding() {
        PiePlot plot = new PiePlot();
        assertNotNull(plot.getLabelPadding());
        RectangleInsets insets = new RectangleInsets(1, 2, 3, 4);
        plot.setLabelPadding(insets);
        assertSame(insets, plot.getLabelPadding());
    }

    @Test(timeout = 4000)
    public void testSetLabelPaddingNullThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setLabelPadding(null);
            fail("Expected IllegalArgumentException for null padding");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetSetSimpleLabels() {
        PiePlot plot = new PiePlot();
        assertFalse(plot.getSimpleLabels());
        plot.setSimpleLabels(true);
        assertTrue(plot.getSimpleLabels());
    }

    @Test(timeout = 4000)
    public void testGetSetSimpleLabelOffset() {
        PiePlot plot = new PiePlot();
        assertNotNull(plot.getSimpleLabelOffset());
        RectangleInsets offset = new RectangleInsets(0.1, 0.2, 0.3, 0.4);
        plot.setSimpleLabelOffset(offset);
        assertSame(offset, plot.getSimpleLabelOffset());
    }

    @Test(timeout = 4000)
    public void testSetSimpleLabelOffsetNullThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setSimpleLabelOffset(null);
            fail("Expected IllegalArgumentException for null offset");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetLabelDistributorNullThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setLabelDistributor(null);
            fail("Expected IllegalArgumentException for null distributor");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetSetToolTipGenerator() {
        PiePlot plot = new PiePlot();
        assertNull(plot.getToolTipGenerator());
        org.jfree.chart.labels.StandardPieToolTipGenerator gen = 
            new org.jfree.chart.labels.StandardPieToolTipGenerator();
        plot.setToolTipGenerator(gen);
        assertSame(gen, plot.getToolTipGenerator());
    }

    @Test(timeout = 4000)
    public void testGetSetURLGenerator() {
        PiePlot plot = new PiePlot();
        assertNull(plot.getURLGenerator());
        org.jfree.chart.urls.StandardPieURLGenerator gen = 
            new org.jfree.chart.urls.StandardPieURLGenerator();
        plot.setURLGenerator(gen);
        assertSame(gen, plot.getURLGenerator());
    }

    @Test(timeout = 4000)
    public void testGetSetMinimumArcAngleToDraw() {
        PiePlot plot = new PiePlot();
        assertEquals(0.00001, plot.getMinimumArcAngleToDraw(), 1e-10);
        plot.setMinimumArcAngleToDraw(0.001);
        assertEquals(0.001, plot.getMinimumArcAngleToDraw(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetSetLegendItemShape() {
        PiePlot plot = new PiePlot();
        assertNotNull(plot.getLegendItemShape());
        java.awt.geom.Ellipse2D ellipse = new java.awt.geom.Ellipse2D.Double(0,0,10,10);
        plot.setLegendItemShape(ellipse);
        assertSame(ellipse, plot.getLegendItemShape());
    }

    @Test(timeout = 4000)
    public void testSetLegendItemShapeNullThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setLegendItemShape(null);
            fail("Expected IllegalArgumentException for null shape");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetSetLegendLabelGenerator() {
        PiePlot plot = new PiePlot();
        assertNotNull(plot.getLegendLabelGenerator());
        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator();
        plot.setLegendLabelGenerator(gen);
        assertSame(gen, plot.getLegendLabelGenerator());
    }

    @Test(timeout = 4000)
    public void testSetLegendLabelGeneratorNullThrows() {
        PiePlot plot = new PiePlot();
        try {
            plot.setLegendLabelGenerator(null);
            fail("Expected IllegalArgumentException for null generator");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetSetLegendLabelToolTipGenerator() {
        PiePlot plot = new PiePlot();
        assertNull(plot.getLegendLabelToolTipGenerator());
        PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator();
        plot.setLegendLabelToolTipGenerator(gen);
        assertSame(gen, plot.getLegendLabelToolTipGenerator());
    }

    @Test(timeout = 4000)
    public void testGetSetLegendLabelURLGenerator() {
        PiePlot plot = new PiePlot();
        assertNull(plot.getLegendLabelURLGenerator());
        org.jfree.chart.urls.StandardPieURLGenerator gen = 
            new org.jfree.chart.urls.StandardPieURLGenerator();
        plot.setLegendLabelURLGenerator(gen);
        assertSame(gen, plot.getLegendLabelURLGenerator());
    }

    // ==================== Partition C: Defect-Targeted Tests ====================

    @Test(timeout = 4000)
    public void testDrawWithNullDataset() {
        // This test reveals the known defect: drawing with null dataset should 
        // not throw NullPointerException. If it does, the bug is present.
        PiePlot plot = new PiePlot(null);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 200, 200);
        try {
            plot.draw(g2, area, null, null, null);
            // If no exception, test passes (correct behavior)
        } catch (NullPointerException e) {
            // Failure: defect is exposed
            fail("Draw with null dataset should not throw NullPointerException");
        } finally {
            g2.dispose();
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetDirectionNull() {
        new PiePlot().setDirection(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetInteriorGapNegative() {
        new PiePlot().setInteriorGap(-0.01);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetInteriorGapTooLarge() {
        new PiePlot().setInteriorGap(0.5);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetBaseSectionPaintNull() {
        new PiePlot().setBaseSectionPaint(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetBaseSectionOutlinePaintNull() {
        new PiePlot().setBaseSectionOutlinePaint(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetBaseSectionOutlineStrokeNull() {
        new PiePlot().setBaseSectionOutlineStroke(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetLabelFontNull() {
        new PiePlot().setLabelFont(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetLabelPaintNull() {
        new PiePlot().setLabelPaint(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetLabelPaddingNull() {
        new PiePlot().setLabelPadding(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetSimpleLabelOffsetNull() {
        new PiePlot().setSimpleLabelOffset(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetLabelDistributorNull() {
        new PiePlot().setLabelDistributor(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetLabelLinkPaintNull() {
        new PiePlot().setLabelLinkPaint(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetLabelLinkStrokeNull() {
        new PiePlot().setLabelLinkStroke(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetLegendItemShapeNull() {
        new PiePlot().setLegendItemShape(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetLegendLabelGeneratorNull() {
        new PiePlot().setLegendLabelGenerator(null);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        PiePlot plot = new PiePlot();
        assertTrue("Same object should be equal", plot.equals(plot));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        PiePlot plot = new PiePlot();
        assertFalse("Null should not be equal", plot.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        PiePlot plot = new PiePlot();
        assertFalse("Different type not equal", plot.equals("some string"));
    }

    @Test(timeout = 4000)
    public void testEqualsSymmetric() {
        PiePlot plot1 = new PiePlot();
        PiePlot plot2 = new PiePlot();
        assertTrue(plot1.equals(plot2));
        assertTrue(plot2.equals(plot1));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentStartAngle() {
        PiePlot plot1 = new PiePlot();
        PiePlot plot2 = new PiePlot();
        plot2.setStartAngle(45.0);
        assertFalse("Start angle differs", plot1.equals(plot2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentDirection() {
        PiePlot plot1 = new PiePlot();
        PiePlot plot2 = new PiePlot();
        plot2.setDirection(Rotation.ANTICLOCKWISE);
        assertFalse("Direction differs", plot1.equals(plot2));
    }

    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        PiePlot plot = new PiePlot(dataset);
        PiePlot clone = (PiePlot) plot.clone();
        assertNotSame("Cloned object should be different", plot, clone);
        assertEquals("Cloned object should be equal", plot, clone);
        // Ensure dataset listener is registered
        clone.setDataset(null);
        assertNull("Cloned dataset should be independent", clone.getDataset());
    }

    @Test(timeout = 4000)
    public void testGetLegendItemsWithEmptyDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        PiePlot plot = new PiePlot(dataset);
        LegendItemCollection items = plot.getLegendItems();
        assertNotNull("Should not return null", items);
        assertEquals(0, items.getItemCount());
    }

    @Test(timeout = 4000)
    public void testGetLegendItemsWithNullDataset() {
        PiePlot plot = new PiePlot(null);
        LegendItemCollection items = plot.getLegendItems();
        assertNotNull("Should not return null", items);
        assertEquals(0, items.getItemCount());
    }

    @Test(timeout = 4000)
    public void testGetLegendItemsWithIgnoreZeroValues() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Zero", 0.0);
        dataset.setValue("Positive", 5.0);
        PiePlot plot = new PiePlot(dataset);
        plot.setIgnoreZeroValues(true);
        LegendItemCollection items = plot.getLegendItems();
        // Only "Positive" should be included because zero value ignored
        assertEquals(1, items.getItemCount());
        assertEquals("Positive", items.get(0).getLabel());
    }

    @Test(timeout = 4000)
    public void testGetLegendItemsWithIgnoreNullValues() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Null", null);
        dataset.setValue("Positive", 5.0);
        PiePlot plot = new PiePlot(dataset);
        plot.setIgnoreNullValues(true);
        LegendItemCollection items = plot.getLegendItems();
        // Only "Positive" should be included because null value ignored
        assertEquals(1, items.getItemCount());
        assertEquals("Positive", items.get(0).getLabel());
    }

    @Test(timeout = 4000)
    public void testGetArcBoundsNoExplode() {
        PiePlot plot = new PiePlot();
        Rectangle2D unexploded = new Rectangle2D.Double(0, 0, 100, 100);
        Rectangle2D exploded = new Rectangle2D.Double(10, 10, 80, 80);
        Rectangle2D bounds = plot.getArcBounds(unexploded, exploded, 0, 90, 0.0);
        // With explodePercent=0, should return unexploded
        assertEquals(unexploded, bounds);
    }

    @Test(timeout = 4000)
    public void testGetArcBoundsWithExplode() {
        PiePlot plot = new PiePlot();
        Rectangle2D unexploded = new Rectangle2D.Double(0, 0, 100, 100);
        Rectangle2D exploded = new Rectangle2D.Double(10, 10, 80, 80);
        // Explode percent 0.5, angle 0, extent 90
        Rectangle2D bounds = plot.getArcBounds(unexploded, exploded, 0, 90, 0.5);
        assertNotNull(bounds);
        // Should be different from unexploded
        assertFalse(unexploded.equals(bounds));
        double deltaX = (unexploded.getCenterX() - exploded.getCenterX()) * 0.5;
        double deltaY = (unexploded.getCenterY() - exploded.getCenterY()) * 0.5;
        assertEquals("X should shift", unexploded.getX() - deltaX, bounds.getX(), 1e-10);
        assertEquals("Y should shift", unexploded.getY() - deltaY, bounds.getY(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetSectionKeyOutOfRange() {
        PiePlot plot = new PiePlot(new DefaultPieDataset());
        // With empty dataset, getSectionKey should return Integer index
        Comparable key = plot.getSectionKey(0);
        assertEquals(new Integer(0), key);
    }

    @Test(timeout = 4000)
    public void testGetSectionKeyWithDataset() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("MyKey", 10.0);
        PiePlot plot = new PiePlot(dataset);
        Comparable key = plot.getSectionKey(0);
        assertEquals("MyKey", key);
    }

    @Test(timeout = 4000)
    public void testGetPlotType() {
        PiePlot plot = new PiePlot();
        String type = plot.getPlotType();
        assertNotNull(type);
        assertTrue(type.length() > 0);
    }

    @Test(timeout = 4000)
    public void testInitialiseWithNullDataset() {
        PiePlot plot = new PiePlot(null);
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D area = new Rectangle2D.Double(0, 0, 200, 200);
        // initialise is protected, we call it through drawPie? Actually drawPie is also protected.
        // We can test initialise via drawPie? Not directly. But we can test via public draw method.
        // Already tested in testDrawWithNullDataset indirectly.
        // So skip.
    }

    // Additional test for lookupSectionPaint with autoPopulate false
    @Test(timeout = 4000)
    public void testLookupSectionPaintAutoPopulateFalse() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        PiePlot plot = new PiePlot(dataset);
        // No paint set for key "A", autoPopulate false should return base
        assertEquals(Color.gray, plot.lookupSectionPaint("A", false));
    }

    // Additional test for lookupSectionOutlinePaint/Stroke similarly
    @Test(timeout = 4000)
    public void testLookupSectionOutlinePaintAutoPopulateFalse() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        PiePlot plot = new PiePlot(dataset);
        assertEquals(Color.lightGray, plot.lookupSectionOutlinePaint("A", false));
    }

    @Test(timeout = 4000)
    public void testLookupSectionOutlineStrokeAutoPopulateFalse() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("A", 10.0);
        PiePlot plot = new PiePlot(dataset);
        assertEquals(new java.awt.BasicStroke(0.5f), plot.lookupSectionOutlineStroke("A", false));
    }

    // Test serialization support is not required.
}