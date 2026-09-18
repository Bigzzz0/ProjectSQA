package org.jfree.chart.plot;

import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.urls.StandardPieURLGenerator;
import org.jfree.chart.util.RectangleInsets;
import org.jfree.chart.util.Rotation;
import org.jfree.chart.util.UnitType;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Target Class: org.jfree.chart.plot.PiePlot
 *
 * Major Branches & Execution Paths Targeted:
 * 1. Defect-targeted Zone:
 *    - Drawing lifecycle with null / empty dataset (Bug reference: PiePlot3DTests#testDrawWithNullDataset).
 *    - Ensure initialize() and draw() paths execute without NullPointerException / IllegalArgumentException.
 *
 * 2. Partition A: Core Functional Logic & State Transitions:
 *    - Dataset assignment, listeners, pieIndex, startAngle, direction (CLOCKWISE vs ANTICLOCKWISE).
 *    - Section paint, outline paint, and stroke resolution (lookupSectionPaint with autoPopulate).
 *    - Explode percentages calculation (single section, max calculation across keys, empty/unset keys).
 *    - Multi-pass rendering pipeline: pass 0 (shadows) and pass 1 (fills, outlines, entity collections).
 *    - Simple label vs. extended two-side label distributor (mid-angle trigonometry for left/right branches).
 *    - Dynamic legend collection generation with null/zero value filters.
 *
 * 3. Partition B: Boundary Value Analysis (BVA):
 *    - Interior gap: 0.0 (boundary), MAX_INTERIOR_GAP (0.40 boundary), negative, and > 0.40.
 *    - Datasets with 0, 1, or multiple items; positive, 0.0, negative, and null values.
 *    - Minimum arc angle to draw threshold filtering (< min angle vs >= min angle).
 *    - Explode percent 0.0 vs > 0.0 (getArcBounds branch logic).
 *
 * 4. Partition C: Defensive Guard Paths:
 *    - Validation exceptions: setDirection(null), setInteriorGap(-0.01 / 0.41), setSectionPaint(null, ...),
 *      setBaseSectionPaint(null), setBaseSectionOutlinePaint(null), setBaseSectionOutlineStroke(null),
 *      setExplodePercent(null, ...), setLabelLinkPaint(null), setLabelLinkStroke(null), setLabelFont(null),
 *      setLabelPaint(null), setLabelPadding(null), setSimpleLabelOffset(null), setLabelDistributor(null),
 *      setLegendItemShape(null), setLegendLabelGenerator(null).
 *
 * 5. Partition D: Contract Integrity & Lifecycle:
 *    - equals(): exhaustively mutated checks across each individual field.
 *    - clone(): deep copy semantics, dataset listener detachment/reattachment, cloneable generators.
 *    - serialization: round-trip writeObject / readObject verification of transient paints and strokes.
 * -------------------------------------------------------------------------------------------------------
 */
public class PiePlotGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Null / Empty Dataset Draw Paths)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDrawWithNullDataset() {
        PiePlot plot = new PiePlot(null);
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 400, 300);

        try {
            plot.draw(g2, plotArea, null, null, new PlotRenderingInfo(new ChartRenderingInfo()));
        } catch (Exception e) {
            fail("Drawing with a null dataset should not throw an exception: " + e.getMessage());
        } finally {
            g2.dispose();
        }
        assertNull(plot.getDataset());
    }

    @Test(timeout = 4000)
    public void testDrawWithEmptyDataset() {
        PiePlot plot = new PiePlot(new DefaultPieDataset());
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 400, 300);

        try {
            plot.draw(g2, plotArea, null, null, new PlotRenderingInfo(new ChartRenderingInfo()));
        } catch (Exception e) {
            fail("Drawing with an empty dataset should not throw an exception: " + e.getMessage());
        } finally {
            g2.dispose();
        }
        assertEquals(0, plot.getDataset().getItemCount());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndDefaults() {
        PiePlot plot = new PiePlot();
        assertNull(plot.getDataset());
        assertEquals(0, plot.getPieIndex());
        assertEquals(PiePlot.DEFAULT_INTERIOR_GAP, plot.getInteriorGap(), 1e-9);
        assertTrue(plot.isCircular());
        assertEquals(PiePlot.DEFAULT_START_ANGLE, plot.getStartAngle(), 1e-9);
        assertEquals(Rotation.CLOCKWISE, plot.getDirection());
        assertEquals(PiePlot.DEFAULT_MINIMUM_ARC_ANGLE_TO_DRAW, plot.getMinimumArcAngleToDraw(), 1e-9);
        assertEquals(Color.gray, plot.getBaseSectionPaint());
        assertTrue(plot.getSectionOutlinesVisible());
        assertEquals(Plot.DEFAULT_OUTLINE_PAINT, plot.getBaseSectionOutlinePaint());
        assertEquals(Plot.DEFAULT_OUTLINE_STROKE, plot.getBaseSectionOutlineStroke());
        assertFalse(plot.getIgnoreNullValues());
        assertFalse(plot.getIgnoreZeroValues());
        assertFalse(plot.getSimpleLabels());
        assertEquals("Pie Plot", plot.getPlotType());
    }

    @Test(timeout = 4000)
    public void testSetDatasetAndListeners() {
        DefaultPieDataset ds1 = new DefaultPieDataset();
        ds1.setValue("A", 10.0);
        PiePlot plot = new PiePlot(ds1);
        assertSame(ds1, plot.getDataset());

        DefaultPieDataset ds2 = new DefaultPieDataset();
        ds2.setValue("B", 20.0);
        plot.setDataset(ds2);
        assertSame(ds2, plot.getDataset());

        plot.setDataset(null);
        assertNull(plot.getDataset());
    }

    @Test(timeout = 4000)
    public void testSectionPaintAndOutlineResolution() {
        DefaultPieDataset ds = new DefaultPieDataset();
        ds.setValue("Sec1", 10.0);
        ds.setValue("Sec2", 20.0);
        PiePlot plot = new PiePlot(ds);

        plot.setSectionPaint("Sec1", Color.RED);
        assertEquals(Color.RED, plot.getSectionPaint("Sec1"));
        assertEquals(Color.RED, plot.lookupSectionPaint("Sec1"));
        assertEquals(Color.RED, plot.lookupSectionPaint("Sec1", true));

        // Auto-populating paint when not explicitly set
        Paint autoPaint = plot.lookupSectionPaint("Sec2", true);
        assertNotNull(autoPaint);
        assertEquals(autoPaint, plot.getSectionPaint("Sec2"));

        // Fallback to baseSectionPaint when autoPopulate is false
        plot.setBaseSectionPaint(Color.MAGENTA);
        assertEquals(Color.MAGENTA, plot.lookupSectionPaint("NonExistent", false));

        // Section Outlines
        plot.setSectionOutlinePaint("Sec1", Color.BLUE);
        assertEquals(Color.BLUE, plot.getSectionOutlinePaint("Sec1"));
        assertEquals(Color.BLUE, plot.lookupSectionOutlinePaint("Sec1"));

        plot.setBaseSectionOutlinePaint(Color.CYAN);
        assertEquals(Color.CYAN, plot.lookupSectionOutlinePaint("NonExistent", false));

        Stroke stroke = new BasicStroke(2.0f);
        plot.setSectionOutlineStroke("Sec1", stroke);
        assertEquals(stroke, plot.getSectionOutlineStroke("Sec1"));
        assertEquals(stroke, plot.lookupSectionOutlineStroke("Sec1"));

        Stroke baseStroke = new BasicStroke(3.0f);
        plot.setBaseSectionOutlineStroke(baseStroke);
        assertEquals(baseStroke, plot.lookupSectionOutlineStroke("NonExistent