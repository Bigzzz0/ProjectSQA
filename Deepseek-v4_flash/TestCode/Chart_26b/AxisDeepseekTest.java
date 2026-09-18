package org.jfree.chart.axis;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.AxisLabelEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;

/**
 * White-box test suite for Axis.java targeting the known Defects4J defect
 * (NullPointerException when drawing with null plot info).
 * 
 * Branch & Defect Analysis Matrix:
 * - Partition A: Core functional logic (getters/setters, state transitions)
 * - Partition B: Boundary values (null label, empty string, zero/negative lengths)
 * - Partition C: Defect-targeted branch (drawLabel with null plotState owner)
 * - Partition D: Exception paths (null arguments to setters)
 * - Partition E: Object contract (equals, clone, listener registration)
 */
public class AxisDeepseekTest {

    // ---------- Helper: concrete Axis subclass for testing ----------
    private static class TestAxis extends Axis {
        TestAxis(String label) {
            super(label);
        }
        @Override
        public void configure() {}
        @Override
        public AxisSpace reserveSpace(Graphics2D g2, Plot plot,
                                      Rectangle2D plotArea, RectangleEdge edge,
                                      AxisSpace space) {
            return new AxisSpace();
        }
        @Override
        public AxisState draw(Graphics2D g2, double cursor,
                              Rectangle2D plotArea, Rectangle2D dataArea,
                              RectangleEdge edge, PlotRenderingInfo plotState) {
            return new AxisState();
        }
        @Override
        public List refreshTicks(Graphics2D g2, AxisState state,
                                 Rectangle2D dataArea, RectangleEdge edge) {
            return new java.util.ArrayList();
        }
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testDefaultValues() {
        TestAxis axis = new TestAxis("Test");
        assertTrue("Default visible", axis.isVisible());
        assertEquals("Default label", "Test", axis.getLabel());
        assertEquals("Default label font", Axis.DEFAULT_AXIS_LABEL_FONT, axis.getLabelFont());
        assertEquals("Default label paint", Axis.DEFAULT_AXIS_LABEL_PAINT, axis.getLabelPaint());
        assertEquals("Default label insets", Axis.DEFAULT_AXIS_LABEL_INSETS, axis.getLabelInsets());
        assertEquals("Default label angle", 0.0, axis.getLabelAngle(), 0.0);
        assertNull("Default label tooltip", axis.getLabelToolTip());
        assertNull("Default label URL", axis.getLabelURL());
        assertTrue("Default axis line visible", axis.isAxisLineVisible());
        assertEquals("Default axis line paint", Axis.DEFAULT_AXIS_LINE_PAINT, axis.getAxisLinePaint());
        assertEquals("Default axis line stroke", Axis.DEFAULT_AXIS_LINE_STROKE, axis.getAxisLineStroke());
        assertTrue("Default tick labels visible", axis.isTickLabelsVisible());
        assertEquals("Default tick label font", Axis.DEFAULT_TICK_LABEL_FONT, axis.getTickLabelFont());
        assertEquals("Default tick label paint", Axis.DEFAULT_TICK_LABEL_PAINT, axis.getTickLabelPaint());
        assertEquals("Default tick label insets", Axis.DEFAULT_TICK_LABEL_INSETS, axis.getTickLabelInsets());
        assertTrue("Default tick marks visible", axis.isTickMarksVisible());
        assertEquals("Default tick mark inside length", Axis.DEFAULT_TICK_MARK_INSIDE_LENGTH, axis.getTickMarkInsideLength(), 0.0f);
        assertEquals("Default tick mark outside length", Axis.DEFAULT_TICK_MARK_OUTSIDE_LENGTH, axis.getTickMarkOutsideLength(), 0.0f);
        assertEquals("Default tick mark stroke", Axis.DEFAULT_TICK_MARK_STROKE, axis.getTickMarkStroke());
        assertEquals("Default tick mark paint", Axis.DEFAULT_TICK_MARK_PAINT, axis.getTickMarkPaint());
        assertEquals("Default fixed dimension", 0.0, axis.getFixedDimension(), 0.0);
        assertNull("Default plot", axis.getPlot());
    }

    @Test(timeout = 4000)
    public void testSetVisible() {
        TestAxis axis = new TestAxis("Test");
        axis.setVisible(false);
        assertFalse(axis.isVisible());
        axis.setVisible(true);
        assertTrue(axis.isVisible());
    }

    @Test(timeout = 4000)
    public void testSetLabel() {
        TestAxis axis = new TestAxis("Initial");
        assertEquals("Initial label", "Initial", axis.getLabel());
        axis.setLabel("NewLabel");
        assertEquals("Updated label", "NewLabel", axis.getLabel());
        axis.setLabel(null);
        assertNull("Null label", axis.getLabel());
        axis.setLabel("AfterNull");
        assertEquals("After null", "AfterNull", axis.getLabel());
    }

    @Test(timeout = 4000)
    public void testSetLabelFont() {
        TestAxis axis = new TestAxis("Test");
        Font newFont = new Font("Serif", Font.BOLD, 14);
        axis.setLabelFont(newFont);
        assertEquals("Label font", newFont, axis.getLabelFont());
    }

    @Test(timeout = 4000)
    public void testSetLabelPaint() {
        TestAxis axis = new TestAxis("Test");
        Paint newPaint = Color.RED;
        axis.setLabelPaint(newPaint);
        assertEquals("Label paint", newPaint, axis.getLabelPaint());
    }

    @Test(timeout = 4000)
    public void testSetLabelInsets() {
        TestAxis axis = new TestAxis("Test");
        RectangleInsets newInsets = new RectangleInsets(5, 5, 5, 5);
        axis.setLabelInsets(newInsets);
        assertEquals("Label insets", newInsets, axis.getLabelInsets());
    }

    @Test(timeout = 4000)
    public void testSetLabelAngle() {
        TestAxis axis = new TestAxis("Test");
        axis.setLabelAngle(Math.PI / 4);
        assertEquals("Label angle", Math.PI / 4, axis.getLabelAngle(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSetLabelToolTip() {
        TestAxis axis = new TestAxis("Test");
        assertNull(axis.getLabelToolTip());
        axis.setLabelToolTip("Tooltip");
        assertEquals("Tooltip", "Tooltip", axis.getLabelToolTip());
        axis.setLabelToolTip(null);
        assertNull(axis.getLabelToolTip());
    }

    @Test(timeout = 4000)
    public void testSetLabelURL() {
        TestAxis axis = new TestAxis("Test");
        assertNull(axis.getLabelURL());
        axis.setLabelURL("http://example.com");
        assertEquals("URL", "http://example.com", axis.getLabelURL());
        axis.setLabelURL(null);
        assertNull(axis.getLabelURL());
    }

    @Test(timeout = 4000)
    public void testSetAxisLineVisible() {
        TestAxis axis = new TestAxis("Test");
        axis.setAxisLineVisible(false);
        assertFalse(axis.isAxisLineVisible());
        axis.setAxisLineVisible(true);
        assertTrue(axis.isAxisLineVisible());
    }

    @Test(timeout = 4000)
    public void testSetAxisLinePaint() {
        TestAxis axis = new TestAxis("Test");
        Paint newPaint = Color.BLUE;
        axis.setAxisLinePaint(newPaint);
        assertEquals("Axis line paint", newPaint, axis.getAxisLinePaint());
    }

    @Test(timeout = 4000)
    public void testSetAxisLineStroke() {
        TestAxis axis = new TestAxis("Test");
        Stroke newStroke = new BasicStroke(2.0f);
        axis.setAxisLineStroke(newStroke);
        assertEquals("Axis line stroke", newStroke, axis.getAxisLineStroke());
    }

    @Test(timeout = 4000)
    public void testSetTickLabelsVisible() {
        TestAxis axis = new TestAxis("Test");
        axis.setTickLabelsVisible(false);
        assertFalse(axis.isTickLabelsVisible());
        axis.setTickLabelsVisible(true);
        assertTrue(axis.isTickLabelsVisible());
    }

    @Test(timeout = 4000)
    public void testSetTickLabelFont() {
        TestAxis axis = new TestAxis("Test");
        Font newFont = new Font("Monospaced", Font.ITALIC, 8);
        axis.setTickLabelFont(newFont);
        assertEquals("Tick label font", newFont, axis.getTickLabelFont());
    }

    @Test(timeout = 4000)
    public void testSetTickLabelPaint() {
        TestAxis axis = new TestAxis("Test");
        Paint newPaint = Color.GREEN;
        axis.setTickLabelPaint(newPaint);
        assertEquals("Tick label paint", newPaint, axis.getTickLabelPaint());
    }

    @Test(timeout = 4000)
    public void testSetTickLabelInsets() {
        TestAxis axis = new TestAxis("Test");
        RectangleInsets newInsets = new RectangleInsets(1, 2, 3, 4);
        axis.setTickLabelInsets(newInsets);
        assertEquals("Tick label insets", newInsets, axis.getTickLabelInsets());
    }

    @Test(timeout = 4000)
    public void testSetTickMarksVisible() {
        TestAxis axis = new TestAxis("Test");
        axis.setTickMarksVisible(false);
        assertFalse(axis.isTickMarksVisible());
        axis.setTickMarksVisible(true);
        assertTrue(axis.isTickMarksVisible());
    }

    @Test(timeout = 4000)
    public void testSetTickMarkInsideLength() {
        TestAxis axis = new TestAxis("Test");
        axis.setTickMarkInsideLength(5.0f);
        assertEquals(5.0f, axis.getTickMarkInsideLength(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testSetTickMarkOutsideLength() {
        TestAxis axis = new TestAxis("Test");
        axis.setTickMarkOutsideLength(10.0f);
        assertEquals(10.0f, axis.getTickMarkOutsideLength(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testSetTickMarkStroke() {
        TestAxis axis = new TestAxis("Test");
        Stroke newStroke = new BasicStroke(3.0f);
        axis.setTickMarkStroke(newStroke);
        assertEquals("Tick mark stroke", newStroke, axis.getTickMarkStroke());
    }

    @Test(timeout = 4000)
    public void testSetTickMarkPaint() {
        TestAxis axis = new TestAxis("Test");
        Paint newPaint = Color.MAGENTA;
        axis.setTickMarkPaint(newPaint);
        assertEquals("Tick mark paint", newPaint, axis.getTickMarkPaint());
    }

    @Test(timeout = 4000)
    public void testSetFixedDimension() {
        TestAxis axis = new TestAxis("Test");
        axis.setFixedDimension(100.0);
        assertEquals(100.0, axis.getFixedDimension(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSetPlot() {
        TestAxis axis = new TestAxis("Test");
        // We cannot instantiate Plot (abstract), but we can test null
        axis.setPlot(null);
        assertNull(axis.getPlot());
        // For coverage, we can create a simple mock-like subclass
        Plot dummyPlot = new Plot() {
            @Override
            public String getPlotType() { return "dummy"; }
            @Override
            public void draw(Graphics2D g2, Rectangle2D area, java.awt.geom.Point2D anchor,
                             PlotRenderingInfo state) {}
        };
        axis.setPlot(dummyPlot);
        assertSame(dummyPlot, axis.getPlot());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testNullLabelConstructor() {
        TestAxis axis = new TestAxis(null);
        assertNull("Null label", axis.getLabel());
    }

    @Test(timeout = 4000)
    public void testEmptyLabel() {
        TestAxis axis = new TestAxis("");
        assertEquals("Empty label", "", axis.getLabel());
        // setLabel with empty string should not fire change if already empty
        axis.setLabel("");
        assertEquals("Still empty", "", axis.getLabel());
    }

    @Test(timeout = 4000)
    public void testSetLabelSameValue() {
        TestAxis axis = new TestAxis("Same");
        // Should not fire change event (we can verify by listener count)
        final boolean[] changed = {false};
        axis.addChangeListener(new AxisChangeListener() {
            @Override
            public void axisChanged(AxisChangeEvent event) {
                changed[0] = true;
            }
        });
        axis.setLabel("Same");
        assertFalse("No change event for same label", changed[0]);
        axis.setLabel("Different");
        assertTrue("Change event for different label", changed[0]);
    }

    @Test(timeout = 4000)
    public void testSetLabelNullToNull() {
        TestAxis axis = new TestAxis(null);
        final boolean[] changed = {false};
        axis.addChangeListener(e -> changed[0] = true);
        axis.setLabel(null);
        assertFalse("No change event for null->null", changed[0]);
    }

    @Test(timeout = 4000)
    public void testSetLabelFromNullToNonNull() {
        TestAxis axis = new TestAxis(null);
        final boolean[] changed = {false};
        axis.addChangeListener(e -> changed[0] = true);
        axis.setLabel("New");
        assertTrue("Change event for null->nonnull", changed[0]);
    }

    @Test(timeout = 4000)
    public void testZeroTickMarkLengths() {
        TestAxis axis = new TestAxis("Test");
        axis.setTickMarkInsideLength(0.0f);
        assertEquals(0.0f, axis.getTickMarkInsideLength(), 0.0f);
        axis.setTickMarkOutsideLength(0.0f);
        assertEquals(0.0f, axis.getTickMarkOutsideLength(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testNegativeTickMarkLengths() {
        TestAxis axis = new TestAxis("Test");
        axis.setTickMarkInsideLength(-5.0f);
        assertEquals(-5.0f, axis.getTickMarkInsideLength(), 0.0f);
        axis.setTickMarkOutsideLength(-10.0f);
        assertEquals(-10.0f, axis.getTickMarkOutsideLength(), 0.0f);
    }

    // ==================== Partition C: Defect-Targeted Branch ====================

    /**
     * This test targets the known Defects4J defect: NullPointerException when
     * drawLabel is called with a non-null PlotRenderingInfo whose owner is null.
     * The bug is in drawLabel: it accesses plotState.getOwner().getEntityCollection()
     * without checking if owner is null.
     */
    @Test(timeout = 4000)
    public void testDrawLabelWithNullOwner() {
        TestAxis axis = new TestAxis("Label");
        // Create a PlotRenderingInfo with null owner
        PlotRenderingInfo nullOwnerInfo = new PlotRenderingInfo(null);
        // We need a Graphics2D stub – use a minimal BufferedImage
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 100, 100);
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        AxisState state = new AxisState();
        state.setCursor(50.0);
        // This should not throw NullPointerException (defect)
        try {
            axis.drawLabel("Test", g2, plotArea, dataArea, RectangleEdge.TOP, state, nullOwnerInfo);
        } catch (NullPointerException e) {
            fail("drawLabel should handle null owner gracefully, but threw NPE: " + e.getMessage());
        }
        g2.dispose();
    }

    /**
     * Additional test: drawLabel with null plotState (should be safe).
     */
    @Test(timeout = 4000)
    public void testDrawLabelWithNullPlotState() {
        TestAxis axis = new TestAxis("Label");
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 100, 100);
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        AxisState state = new AxisState();
        state.setCursor(50.0);
        // Should not throw
        AxisState result = axis.drawLabel("Test", g2, plotArea, dataArea, RectangleEdge.TOP, state, null);
        assertNotNull("Result state should not be null", result);
        g2.dispose();
    }

    /**
     * Test drawLabel with empty label (should return state unchanged).
     */
    @Test(timeout = 4000)
    public void testDrawLabelWithEmptyLabel() {
        TestAxis axis = new TestAxis("Label");
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 100, 100);
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        AxisState state = new AxisState();
        state.setCursor(50.0);
        AxisState result = axis.drawLabel("", g2, plotArea, dataArea, RectangleEdge.TOP, state, null);
        assertSame("State should be same object", state, result);
        g2.dispose();
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetLabelFontNull() {
        TestAxis axis = new TestAxis("Test");
        axis.setLabelFont(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetLabelPaintNull() {
        TestAxis axis = new TestAxis("Test");
        axis.setLabelPaint(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetLabelInsetsNull() {
        TestAxis axis = new TestAxis("Test");
        axis.setLabelInsets(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetAxisLinePaintNull() {
        TestAxis axis = new TestAxis("Test");
        axis.setAxisLinePaint(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetAxisLineStrokeNull() {
        TestAxis axis = new TestAxis("Test");
        axis.setAxisLineStroke(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetTickLabelFontNull() {
        TestAxis axis = new TestAxis("Test");
        axis.setTickLabelFont(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetTickLabelPaintNull() {
        TestAxis axis = new TestAxis("Test");
        axis.setTickLabelPaint(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetTickLabelInsetsNull() {
        TestAxis axis = new TestAxis("Test");
        axis.setTickLabelInsets(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetTickMarkStrokeNull() {
        TestAxis axis = new TestAxis("Test");
        axis.setTickMarkStroke(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetTickMarkPaintNull() {
        TestAxis axis = new TestAxis("Test");
        axis.setTickMarkPaint(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDrawLabelNullState() {
        TestAxis axis = new TestAxis("Label");
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 100, 100);
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        axis.drawLabel("Test", g2, plotArea, dataArea, RectangleEdge.TOP, null, null);
        g2.dispose();
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEquals() {
        TestAxis a1 = new TestAxis("Axis1");
        TestAxis a2 = new TestAxis("Axis1");
        assertEquals("Equal axes", a1, a2);
        a2.setLabel("Different");
        assertFalse("Different labels", a1.equals(a2));
        // Test null and non-Axis
        assertFalse("Null not equal", a1.equals(null));
        assertFalse("String not equal", a1.equals("string"));
        // Test all fields
        TestAxis a3 = new TestAxis("Axis1");
        a3.setVisible(false);
        assertFalse("Visible different", a1.equals(a3));
        a3.setVisible(true);
        a3.setLabelFont(new Font("Serif", Font.PLAIN, 11));
        assertFalse("LabelFont different", a1.equals(a3));
        // Reset
        a3.setLabelFont(Axis.DEFAULT_AXIS_LABEL_FONT);
        a3.setLabelPaint(Color.RED);
        assertFalse("LabelPaint different", a1.equals(a3));
        a3.setLabelPaint(Axis.DEFAULT_AXIS_LABEL_PAINT);
        a3.setLabelInsets(new RectangleInsets(1,1,1,1));
        assertFalse("LabelInsets different", a1.equals(a3));
        a3.setLabelInsets(Axis.DEFAULT_AXIS_LABEL_INSETS);
        a3.setLabelAngle(0.5);
        assertFalse("LabelAngle different", a1.equals(a3));
        a3.setLabelAngle(0.0);
        a3.setLabelToolTip("tip");
        assertFalse("LabelToolTip different", a1.equals(a3));
        a3.setLabelToolTip(null);
        a3.setLabelURL("url");
        assertFalse("LabelURL different", a1.equals(a3));
        a3.setLabelURL(null);
        a3.setAxisLineVisible(false);
        assertFalse("AxisLineVisible different", a1.equals(a3));
        a3.setAxisLineVisible(true);
        a3.setAxisLineStroke(new BasicStroke(2));
        assertFalse("AxisLineStroke different", a1.equals(a3));
        a3.setAxisLineStroke(Axis.DEFAULT_AXIS_LINE_STROKE);
        a3.setAxisLinePaint(Color.GREEN);
        assertFalse("AxisLinePaint different", a1.equals(a3));
        a3.setAxisLinePaint(Axis.DEFAULT_AXIS_LINE_PAINT);
        a3.setTickLabelsVisible(false);
        assertFalse("TickLabelsVisible different", a1.equals(a3));
        a3.setTickLabelsVisible(true);
        a3.setTickLabelFont(new Font("Monospaced", Font.PLAIN, 9));
        assertFalse("TickLabelFont different", a1.equals(a3));
        a3.setTickLabelFont(Axis.DEFAULT_TICK_LABEL_FONT);
        a3.setTickLabelPaint(Color.YELLOW);
        assertFalse("TickLabelPaint different", a1.equals(a3));
        a3.setTickLabelPaint(Axis.DEFAULT_TICK_LABEL_PAINT);
        a3.setTickLabelInsets(new RectangleInsets(5,5,5,5));
        assertFalse("TickLabelInsets different", a1.equals(a3));
        a3.setTickLabelInsets(Axis.DEFAULT_TICK_LABEL_INSETS);
        a3.setTickMarksVisible(false);
        assertFalse("TickMarksVisible different", a1.equals(a3));
        a3.setTickMarksVisible(true);
        a3.setTickMarkInsideLength(3.0f);
        assertFalse("TickMarkInsideLength different", a1.equals(a3));
        a3.setTickMarkInsideLength(Axis.DEFAULT_TICK_MARK_INSIDE_LENGTH);
        a3.setTickMarkOutsideLength(5.0f);
        assertFalse("TickMarkOutsideLength different", a1.equals(a3));
        a3.setTickMarkOutsideLength(Axis.DEFAULT_TICK_MARK_OUTSIDE_LENGTH);
        a3.setTickMarkPaint(Color.CYAN);
        assertFalse("TickMarkPaint different", a1.equals(a3));
        a3.setTickMarkPaint(Axis.DEFAULT_TICK_MARK_PAINT);
        a3.setTickMarkStroke(new BasicStroke(4));
        assertFalse("TickMarkStroke different", a1.equals(a3));
        a3.setTickMarkStroke(Axis.DEFAULT_TICK_MARK_STROKE);
        a3.setFixedDimension(50.0);
        assertFalse("FixedDimension different", a1.equals(a3));
        a3.setFixedDimension(0.0);
        assertTrue("Fully equal", a1.equals(a3));
    }

    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        TestAxis axis = new TestAxis("Original");
        axis.setFixedDimension(123.0);
        TestAxis clone = (TestAxis) axis.clone();
        assertNotSame("Different object", axis, clone);
        assertEquals("Cloned label", axis.getLabel(), clone.getLabel());
        assertEquals("Cloned fixed dimension", axis.getFixedDimension(), clone.getFixedDimension(), 0.0);
        assertNull("Cloned plot is null", clone.getPlot());
        // Verify listener list is independent
        final boolean[] changed = {false};
        clone.addChangeListener(e -> changed[0] = true);
        clone.setLabel("Changed");
        assertTrue("Clone listener works", changed[0]);
    }

    @Test(timeout = 4000)
    public void testListenerRegistration() {
        TestAxis axis = new TestAxis("Test");
        AxisChangeListener listener = e -> {};
        assertFalse("No listener initially", axis.hasListener(listener));
        axis.addChangeListener(listener);
        assertTrue("Listener registered", axis.hasListener(listener));
        axis.removeChangeListener(listener);
        assertFalse("Listener removed", axis.hasListener(listener));
    }

    @Test(timeout = 4000)
    public void testNotifyListeners() {
        TestAxis axis = new TestAxis("Test");
        final boolean[] notified = {false};
        axis.addChangeListener(e -> notified[0] = true);
        axis.setVisible(false); // should fire event
        assertTrue("Listener notified", notified[0]);
    }

    @Test(timeout = 4000)
    public void testGetLabelEnclosure() {
        TestAxis axis = new TestAxis("Test");
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        Rectangle2D enclosure = axis.getLabelEnclosure(g2, RectangleEdge.TOP);
        assertNotNull("Enclosure not null", enclosure);
        // With empty label, should return empty rectangle
        TestAxis emptyAxis = new TestAxis("");
        Rectangle2D emptyEnclosure = emptyAxis.getLabelEnclosure(g2, RectangleEdge.LEFT);
        assertEquals("Empty label enclosure width", 0.0, emptyEnclosure.getWidth(), 0.0);
        assertEquals("Empty label enclosure height", 0.0, emptyEnclosure.getHeight(), 0.0);
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testDrawAxisLine() {
        TestAxis axis = new TestAxis("Test");
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(100, 100, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        // Should not throw
        axis.drawAxisLine(g2, 50.0, dataArea, RectangleEdge.TOP);
        axis.drawAxisLine(g2, 50.0, dataArea, RectangleEdge.BOTTOM);
        axis.drawAxisLine(g2, 50.0, dataArea, RectangleEdge.LEFT);
        axis.drawAxisLine(g2, 50.0, dataArea, RectangleEdge.RIGHT);
        g2.dispose();
    }
}