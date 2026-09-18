/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jfree.chart.axis.Axis
 *
 * 1. Defect Coverage (Defects4J / Chart testDrawWithNullInfo issue):
 *    - In method `drawLabel(String, Graphics2D, Rectangle2D, Rectangle2D, RectangleEdge, AxisState, PlotRenderingInfo)`:
 *      When `plotState != null`, it attempts `plotState.getOwner().getEntityCollection()`.
 *      If `plotState.getOwner()` returns `null` (e.g., when `new PlotRenderingInfo(null)` is used),
 *      a `NullPointerException` occurs in unfixed code because `owner != null` is not checked.
 *      Test `testDrawLabelDefectNullChartRenderingInfoOwner` targets this defect explicitly.
 *
 * 2. Decision & Condition Branch Coverage:
 *    - `drawLabel`:
 *      * state == null (IllegalArgumentException guard)
 *      * label == null (returns state immediately)
 *      * label.equals("") (returns state immediately)
 *      * edge == TOP, BOTTOM, LEFT, RIGHT
 *      * plotState == null vs plotState != null (with owner == null, entities == null, and entities != null)
 *    - `getLabelEnclosure`:
 *      * label == null vs label == "" vs non-empty label
 *      * edge == LEFT / RIGHT (angle adjustment: angle - Math.PI / 2.0) vs TOP / BOTTOM
 *    - `drawAxisLine`:
 *      * edge == TOP, BOTTOM, LEFT, RIGHT
 *    - Change Notification Branches:
 *      * setVisible(flag == visible vs flag != visible)
 *      * setLabel (existing == null vs existing != null; label == null vs equal vs different)
 *      * setLabelFont, setLabelInsets, setTickLabelFont, setTickLabelInsets, setTickMarkStroke:
 *        null check guard, value equality check
 *      * addChangeListener, removeChangeListener, hasListener, notifyListeners
 *    - Equality Contract (`equals`):
 *      * this == obj, !(obj instanceof Axis)
 *      * 21 distinct fields checked for inequality:
 *        visible, label, labelFont, labelPaint, labelInsets, labelAngle, labelToolTip, labelURL,
 *        axisLineVisible, axisLineStroke, axisLinePaint, tickLabelsVisible, tickLabelFont,
 *        tickLabelPaint, tickLabelInsets, tickMarksVisible, tickMarkInsideLength,
 *        tickMarkOutsideLength, tickMarkPaint, tickMarkStroke, fixedDimension.
 *    - Lifecycle Integrity:
 *      * `clone`: plot reset to null, listenerList reinitialized.
 *      * `readObject` / `writeObject`: custom serialization of transient Paint and Stroke fields.
 */

package org.jfree.chart.axis;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.entity.AxisLabelEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.util.RectangleEdge;
import org.jfree.chart.util.RectangleInsets;

public class AxisGeminiTest {

    /**
     * Concrete Axis subclass for white-box testing of the abstract Axis class.
     */
    private static class ConcreteTestAxis extends Axis {
        private static final long serialVersionUID = 1L;
        public boolean configureCalled = false;

        public ConcreteTestAxis(String label) {
            super(label);
        }

        @Override
        public void configure() {
            this.configureCalled = true;
        }

        @Override
        public AxisSpace reserveSpace(Graphics2D g2, Plot plot, Rectangle2D plotArea, 
                                      RectangleEdge edge, AxisSpace space) {
            return space;
        }

        @Override
        public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea, 
                              Rectangle2D dataArea, RectangleEdge edge, 
                              PlotRenderingInfo plotState) {
            return new AxisState(cursor);
        }

        @Override
        public List refreshTicks(Graphics2D g2, AxisState state, Rectangle2D dataArea, 
                                 RectangleEdge edge) {
            return Collections.emptyList();
        }
    }

    /**
     * Simple listener to record axis change events.
     */
    private static class RecordingAxisChangeListener implements AxisChangeListener {
        public int eventCount = 0;
        public AxisChangeEvent lastEvent = null;

        @Override
        public void axisChanged(AxisChangeEvent event) {
            this.eventCount++;
            this.lastEvent = event;
        }
    }

    private Graphics2D createTestGraphics2D() {
        BufferedImage image = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        return image.createGraphics();
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultStateAndInitialGetters() {
        ConcreteTestAxis axis = new ConcreteTestAxis("Initial Label");

        assertEquals("Initial Label", axis.getLabel());
        assertTrue(axis.isVisible());
        assertEquals(Axis.DEFAULT_AXIS_LABEL_FONT, axis.getLabelFont());
        assertEquals(Axis.DEFAULT_AXIS_LABEL_PAINT, axis.getLabelPaint());
        assertEquals(Axis.DEFAULT_AXIS_LABEL_INSETS, axis.getLabelInsets());
        assertEquals(0.0, axis.getLabelAngle(), 1e-9);
        assertNull(axis.getLabelToolTip());
        assertNull(axis.getLabelURL());

        assertTrue(axis.isAxisLineVisible());
        assertEquals(Axis.DEFAULT_AXIS_LINE_PAINT, axis.getAxisLinePaint());
        assertEquals(Axis.DEFAULT_AXIS_LINE_STROKE, axis.getAxisLineStroke());

        assertTrue(axis.isTickLabelsVisible());
        assertEquals(Axis.DEFAULT_TICK_LABEL_FONT, axis.getTickLabelFont());
        assertEquals(Axis.DEFAULT_TICK_LABEL_PAINT, axis.getTickLabelPaint());
        assertEquals(Axis.DEFAULT_TICK_LABEL_INSETS, axis.getTickLabelInsets());

        assertTrue(axis.isTickMarksVisible());
        assertEquals(Axis.DEFAULT_TICK_MARK_STROKE, axis.getTickMarkStroke());
        assertEquals(Axis.DEFAULT_TICK_MARK_PAINT, axis.getTickMarkPaint());
        assertEquals(Axis.DEFAULT_TICK_MARK_INSIDE_LENGTH, axis.getTickMarkInsideLength(), 1e-9);
        assertEquals(Axis.DEFAULT_TICK_MARK_OUTSIDE_LENGTH, axis.getTickMarkOutsideLength(), 1e-9);

        assertNull(axis.getPlot());
        assertEquals(0.0, axis.getFixedDimension(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSetVisibleStateAndNotification() {
        ConcreteTestAxis axis = new ConcreteTestAxis("Label");
        RecordingAxisChangeListener listener = new RecordingAxisChangeListener();
        axis.addChangeListener(listener);

        axis.setVisible(true); // Same as default
        assertEquals(0, listener.eventCount);

        axis.setVisible(false);
        assertFalse(axis.isVisible());
        assertEquals(1, listener.eventCount);

        axis.setVisible(false); // No change
        assertEquals(1, listener.eventCount);

        axis.setVisible(true);
        assertTrue(axis.isVisible());
        assertEquals(2, listener.eventCount);
    }

    @Test(timeout = 4000)
    public void testSetLabelVariationsAndNotification() {
        ConcreteTestAxis axis = new ConcreteTestAxis(null);
        RecordingAxisChangeListener listener = new RecordingAxisChangeListener();
        axis.addChangeListener(listener);

        // existing == null, setting non-null
        axis.setLabel("Alpha");
        assertEquals("Alpha", axis.getLabel());
        assertEquals(1, listener.eventCount);

        // existing != null, setting same value
        axis.setLabel("Alpha");
        assertEquals(1, listener.eventCount);

        // existing != null, setting different value
        axis.setLabel("Beta");
        assertEquals("Beta", axis.getLabel());
        assertEquals(2, listener.eventCount);

        // existing != null, setting null
        axis.setLabel(null);
        assertNull(axis.getLabel());
        assertEquals(3, listener.eventCount);

        // existing == null, setting null
        axis.setLabel(null);
        assertEquals(3, listener.eventCount);
    }

    @Test(timeout = 4000)
    public void testSetLabelPropertiesAndNotification() {
        ConcreteTestAxis axis = new ConcreteTestAxis("Label");
        RecordingAxisChangeListener listener = new RecordingAxisChangeListener();
        axis.addChangeListener(listener);

        Font newFont = new Font("Dialog", Font.BOLD, 14);
        axis.setLabelFont(newFont);
        assertEquals(newFont, axis.getLabelFont());
        assertEquals(1, listener.eventCount);
        axis.setLabelFont(newFont); // Unchanged
        assertEquals(1, listener.eventCount);

        Paint newPaint = Color.BLUE;
        axis.setLabelPaint(newPaint);
        assertEquals(newPaint, axis.getLabelPaint());
        assertEquals(2, listener.eventCount);

        RectangleInsets newInsets = new RectangleInsets(5.0, 5.0, 5.0, 5.0);
        axis.setLabelInsets(newInsets);
        assertEquals(newInsets, axis.getLabelInsets());
        assertEquals(3, listener.eventCount);
        axis.setLabelInsets(newInsets); // Unchanged
        assertEquals(3, listener.eventCount);

        axis.setLabelAngle(Math.PI / 4.0);
        assertEquals(Math.PI / 4.0, axis.getLabelAngle(), 1e-9);
        assertEquals(4, listener.eventCount);

        axis.setLabelToolTip("Tooltip");
        assertEquals("Tooltip", axis.getLabelToolTip());
        assertEquals(5, listener.eventCount);

        axis.setLabelURL("http://example.com");
        assertEquals("http://example.com", axis.getLabelURL());
        assertEquals(6, listener.eventCount);
    }

    @Test(timeout = 4000)
    public void testSetAxisLinePropertiesAndNotification() {
        ConcreteTestAxis axis = new ConcreteTestAxis("Label");
        RecordingAxisChangeListener listener = new RecordingAxisChangeListener();
        axis.addChangeListener(listener);

        axis.setAxisLineVisible(false);
        assertFalse(axis.isAxisLineVisible());
        assertEquals(1, listener.eventCount);

        Paint paint = Color.RED;
        axis.setAxisLinePaint(paint);
        assertEquals(paint, axis.getAxisLinePaint());
        assertEquals(2, listener.eventCount);

        Stroke stroke = new BasicStroke(2.5f);
        axis.setAxisLineStroke(stroke);
        assertEquals(stroke, axis.getAxisLineStroke());
        assertEquals(3, listener.eventCount);
    }

    @Test(timeout = 4000)
    public void testSetTickLabelPropertiesAndNotification() {
        ConcreteTestAxis axis = new ConcreteTestAxis("Label");
        RecordingAxisChangeListener listener = new RecordingAxisChangeListener();
        axis.addChangeListener(listener);

        axis.setTickLabelsVisible(false);
        assertFalse(axis.isTickLabelsVisible());
        assertEquals(1, listener.eventCount);
        axis.setTickLabelsVisible(false); // Unchanged
        assertEquals(1, listener.eventCount);

        Font font = new Font("Serif", Font.ITALIC, 11);
        axis.setTickLabelFont(font);
        assertEquals(font, axis.getTickLabelFont());
        assertEquals(2, listener.eventCount);
        axis.setTickLabelFont(font); // Unchanged
        assertEquals(2, listener.eventCount);

        Paint paint = Color.MAGENTA;
        axis.setTickLabelPaint(paint);
        assertEquals(paint, axis.getTickLabelPaint());
        assertEquals(3, listener.eventCount);

        RectangleInsets insets = new RectangleInsets(1.0, 1.0, 1.0, 1.0);
        axis.setTickLabelInsets(insets);
        assertEquals(insets, axis.getTickLabelInsets());
        assertEquals(4, listener.eventCount);
        axis.setTickLabelInsets(insets); // Unchanged
        assertEquals(4, listener.eventCount);
    }

    @Test(timeout = 4000)
    public void testSetTickMarkPropertiesAndNotification() {
        ConcreteTestAxis axis = new ConcreteTestAxis("Label");
        RecordingAxisChangeListener listener = new RecordingAxisChangeListener();
        axis.addChangeListener(listener);

        axis.setTickMarksVisible(false);
        assertFalse(axis.isTickMarksVisible());
        assertEquals(1, listener.eventCount);
        axis.setTickMarksVisible(false);
        assertEquals(1, listener.eventCount);

        axis.setTickMarkInsideLength(3.5f);
        assertEquals(3.5f, axis.getTickMarkInsideLength(), 1e-9);
        assertEquals(2, listener.eventCount);

        axis.setTickMarkOutsideLength(4.5f);
        assertEquals(4.5f, axis.getTickMarkOutsideLength(), 1e-9);
        assertEquals(3, listener.eventCount);

        Stroke stroke = new BasicStroke(3.0f);
        axis.setTickMarkStroke(stroke);
        assertEquals(stroke, axis.getTickMarkStroke());
        assertEquals(4, listener.eventCount);
        axis.setTickMarkStroke(stroke);
        assertEquals(4, listener.eventCount);

        Paint paint = Color.GREEN;
        axis.setTickMarkPaint(paint);
        assertEquals(paint, axis.getTickMarkPaint());
        assertEquals(5, listener.eventCount);
    }

    @Test(timeout = 4000)
    public void testSetPlotAndFixedDimension() {
        ConcreteTestAxis axis = new ConcreteTestAxis("Label");
        assertFalse(axis.configureCalled);

        axis.setPlot(null);
        assertTrue(axis.configureCalled);
        assertNull(axis.getPlot());

        axis.setFixedDimension(42.0);
        assertEquals(42.0, axis.getFixedDimension(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testListenerManagement() {
        ConcreteTestAxis axis = new ConcreteTestAxis("Label");
        RecordingAxisChangeListener listener1 = new RecordingAxisChangeListener();
        RecordingAxisChangeListener listener2 = new RecordingAxisChangeListener();

        assertFalse(axis.hasListener(listener1));
        axis.addChangeListener(listener1);
        assertTrue(axis.hasListener(listener1));
        assertFalse(axis.hasListener(listener2));

        axis.setVisible(!axis.isVisible());
        assertEquals(1, listener1.eventCount);
        assertEquals(0, listener2.eventCount);

        axis.removeChangeListener(listener1);
        assertFalse(axis.hasListener(listener1));

        axis.setVisible(!axis.isVisible());
        assertEquals(1, listener1.eventCount);
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis & Drawing Logic
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetLabelEnclosureNullAndEmptyLabel() {
        Graphics2D g2 = createTestGraphics2D();
        ConcreteTestAxis axis = new ConcreteTestAxis(null);

        Rectangle2D boundsNull = axis.getLabelEnclosure(g2, RectangleEdge.BOTTOM);
        assertEquals(0.0, boundsNull.getWidth(), 1e-9);
        assertEquals(0.0, boundsNull.getHeight(), 1e-9);

        axis.setLabel("");
        Rectangle2D boundsEmpty = axis.getLabelEnclosure(g2, RectangleEdge.BOTTOM);
        assertEquals(0.0, boundsEmpty.getWidth(), 1e-9);
        assertEquals(0.0, boundsEmpty.getHeight(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testGetLabelEnclosureAllEdges() {
        Graphics2D g2 = createTestGraphics2D();
        ConcreteTestAxis axis = new ConcreteTestAxis("Axis Label Enclosure");

        Rectangle2D topBounds = axis.getLabelEnclosure(g2, RectangleEdge.TOP);
        assertTrue(topBounds.getWidth() > 0.0);
        assertTrue(topBounds.getHeight() > 0.0);

        Rectangle2D leftBounds = axis.getLabelEnclosure(g2, RectangleEdge.LEFT);
        assertTrue(leftBounds.getWidth() > 0.0);
        assertTrue(leftBounds.getHeight() > 0.0);

        Rectangle2D rightBounds = axis.getLabelEnclosure(g2, RectangleEdge.RIGHT);
        assertTrue(rightBounds.getWidth() > 0.0);
        assertTrue(rightBounds.getHeight() > 0.0);

        Rectangle2D bottomBounds = axis.getLabelEnclosure(g2, RectangleEdge.BOTTOM);
        assertTrue(bottomBounds.getWidth() > 0.0);
        assertTrue(bottomBounds.getHeight() > 0.0);
    }

    @Test(timeout = 4000)
    public void testDrawLabelNullOrEmptyLabelReturnsStateDirectly() {
        Graphics2D g2 = createTestGraphics2D();
        ConcreteTestAxis axis = new ConcreteTestAxis("Original");
        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 200, 200);
        Rectangle2D dataArea = new Rectangle2D.Double(20, 20, 160, 160);
        AxisState state = new AxisState(50.0);

        AxisState resNull = axis.drawLabel(null, g2, plotArea, dataArea, 
                RectangleEdge.TOP, state, null);
        assertSame(state, resNull);
        assertEquals(50.0, resNull.getCursor(), 1e-9);

        AxisState resEmpty = axis.drawLabel("", g2, plotArea, dataArea, 
                RectangleEdge.TOP, state, null);
        assertSame(state, resEmpty);
        assertEquals(50.0, resEmpty.getCursor(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testDrawLabelAllEdgesWithoutEntities() {
        Graphics2D g2 = createTestGraphics2D();
        ConcreteTestAxis axis = new ConcreteTestAxis("Label");
        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 200, 200);
        Rectangle2D dataArea = new Rectangle2D.Double(20, 20, 160, 160);

        // TOP
        AxisState stateTop = new AxisState(100.0);
        axis.drawLabel("Label", g2, plotArea, dataArea, RectangleEdge.TOP, stateTop, null);
        assertTrue(stateTop.getCursor() < 100.0);

        // BOTTOM
        AxisState stateBottom = new AxisState(100.0);
        axis.drawLabel("Label", g2, plotArea, dataArea, RectangleEdge.BOTTOM, stateBottom, null);
        assertTrue(stateBottom.getCursor() > 100.0);

        // LEFT
        AxisState stateLeft = new AxisState(100.0);
        axis.drawLabel("Label", g2, plotArea, dataArea, RectangleEdge.LEFT, stateLeft, null);
        assertTrue(stateLeft.getCursor() < 100.0);

        // RIGHT
        AxisState stateRight = new AxisState(100.0);
        axis.drawLabel("Label", g2, plotArea, dataArea, RectangleEdge.RIGHT, stateRight, null);
        assertTrue(stateRight.getCursor() > 100.0);
    }

    @Test(timeout = 4000)
    public void testDrawLabelWithEntitiesCollected() {
        Graphics2D g2 = createTestGraphics2D();
        ConcreteTestAxis axis = new ConcreteTestAxis("Label With Entities");
        axis.setLabelToolTip("Tooltip Test");
        axis.setLabelURL("http://test.url");

        ChartRenderingInfo chartInfo = new ChartRenderingInfo();
        PlotRenderingInfo plotInfo = new PlotRenderingInfo(chartInfo);
        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 200, 200);
        Rectangle2D dataArea = new Rectangle2D.Double(20, 20, 160, 160);
        AxisState state = new AxisState(100.0);

        axis.drawLabel("Label With Entities", g2, plotArea, dataArea, 
                RectangleEdge.BOTTOM, state, plotInfo);

        EntityCollection entities = chartInfo.getEntityCollection();
        assertNotNull(entities);
        assertEquals(1, entities.getEntityCount());
        assertTrue(entities.getEntity(0) instanceof AxisLabelEntity);
        AxisLabelEntity entity = (AxisLabelEntity) entities.getEntity(0);
        assertEquals("Tooltip Test", entity.getToolTipText());
        assertEquals("http://test.url", entity.getURLText());
        assertSame(axis, entity.getAxis());
    }

    @Test(timeout = 4000)
    public void testDrawLabelWithNullEntityCollection() {
        Graphics2D g2 = createTestGraphics2D();
        ConcreteTestAxis axis = new ConcreteTestAxis("Label");

        // ChartRenderingInfo initialized without entity collection
        ChartRenderingInfo chartInfo = new ChartRenderingInfo(null);
        PlotRenderingInfo plotInfo = new PlotRenderingInfo(chartInfo);
        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 200, 200);
        Rectangle2D dataArea = new Rectangle2D.Double(20, 20, 160, 160);
        AxisState state = new AxisState(100.0);

        // entities == null path
        AxisState result = axis.drawLabel("Label", g2, plotArea, dataArea, 
                RectangleEdge.BOTTOM, state, plotInfo);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testDrawAxisLineAllEdges() {
        Graphics2D g2 = createTestGraphics2D();
        ConcreteTestAxis axis = new ConcreteTestAxis("Label");
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 100, 100);

        axis.drawAxisLine(g2, 50.0, dataArea, RectangleEdge.TOP);
        axis.drawAxisLine(g2, 50.0, dataArea, RectangleEdge.BOTTOM);
        axis.drawAxisLine(g2, 50.0, dataArea, RectangleEdge.LEFT);
        axis.drawAxisLine(g2, 50.0, dataArea, RectangleEdge.RIGHT);
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where `plotState != null`, but `plotState.getOwner() == null`.
     * Unfixed code directly calls `plotState.getOwner().getEntityCollection()`,
     * which throws a `NullPointerException`. Correct code checks `if (owner != null)`.
     */
    @Test(timeout = 4000)
    public void testDrawLabelDefectNullChartRenderingInfoOwner() {
        Graphics2D g2 = createTestGraphics2D();
        ConcreteTestAxis axis = new ConcreteTestAxis("Defect Test Label");

        // PlotRenderingInfo initialized with null ChartRenderingInfo
        PlotRenderingInfo plotState = new PlotRenderingInfo(null);
        assertNull("Owner ChartRenderingInfo must be null", plotState.getOwner());

        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 200, 200);
        Rectangle2D dataArea = new Rectangle2D.Double(20, 20, 160, 160);
        AxisState state = new AxisState(50.0);

        // On buggy versions, this throws NullPointerException!
        AxisState returnedState = axis.drawLabel("Defect Test Label", g2, plotArea, 
                dataArea, RectangleEdge.BOTTOM, state, plotState);
        assertNotNull(returnedState);
        assertTrue(returnedState.getCursor() > 50.0);
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetLabelFontNull() {
        new ConcreteTestAxis("Label").setLabelFont(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetLabelPaintNull() {
        new ConcreteTestAxis("Label").setLabelPaint(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetLabelInsetsNull() {
        new ConcreteTestAxis("Label").setLabelInsets(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetAxisLinePaintNull() {
        new ConcreteTestAxis("Label").setAxisLinePaint(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetAxisLineStrokeNull() {
        new ConcreteTestAxis("Label").setAxisLineStroke(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetTickLabelFontNull() {
        new ConcreteTestAxis("Label").setTickLabelFont(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetTickLabelPaintNull() {
        new ConcreteTestAxis("Label").setTickLabelPaint(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetTickLabelInsetsNull() {
        new ConcreteTestAxis("Label").setTickLabelInsets(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetTickMarkStrokeNull() {
        new ConcreteTestAxis("Label").setTickMarkStroke(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetTickMarkPaintNull() {
        new ConcreteTestAxis("Label").setTickMarkPaint(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDrawLabelNullState() {
        Graphics2D g2 = createTestGraphics2D();
        ConcreteTestAxis axis = new ConcreteTestAxis("Label");
        Rectangle2D plotArea = new Rectangle2D.Double(0, 0, 100, 100);
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 80, 80);
        axis.drawLabel("Label", g2, plotArea, dataArea, RectangleEdge.TOP, null, null);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity (Equals, Clone, Ser)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsBasicContracts() {
        ConcreteTestAxis a1 = new ConcreteTestAxis("Label");
        ConcreteTestAxis a2 = new ConcreteTestAxis("Label");

        assertTrue(a1.equals(a1));
        assertTrue(a1.equals(a2));
        assertTrue(a2.equals(a1));

        assertFalse(a1.equals(null));
        assertFalse(a1.equals("Not an Axis"));
    }

    @Test(timeout = 4000)
    public void testEqualsAllFieldBranches() {
        ConcreteTestAxis a1 = new ConcreteTestAxis("Label");
        ConcreteTestAxis a2 = new ConcreteTestAxis("Label");

        // 1. visible
        a2.setVisible(false);
        assertFalse(a1.equals(a2));
        a2.setVisible(true);
        assertTrue(a1.equals(a2));

        // 2. label
        a2.setLabel("Diff");
        assertFalse(a1.equals(a2));
        a2.setLabel("Label");
        assertTrue(a1.equals(a2));

        // 3. labelFont
        a2.setLabelFont(new Font("Dialog", Font.BOLD, 18));
        assertFalse(a1.equals(a2));
        a2.setLabelFont(a1.getLabelFont());
        assertTrue(a1.equals(a2));

        // 4. labelPaint
        a2.setLabelPaint(Color.CYAN);
        assertFalse(a1.equals(a2));
        a2.setLabelPaint(a1.getLabelPaint());
        assertTrue(a1.equals(a2));

        // 5. labelInsets
        a2.setLabelInsets(new RectangleInsets(10, 10, 10, 10));
        assertFalse(a1.equals(a2));
        a2.setLabelInsets(a1.getLabelInsets());
        assertTrue(a1.equals(a2));

        // 6. labelAngle
        a2.setLabelAngle(1.23);
        assertFalse(a1.equals(a2));
        a2.setLabelAngle(a1.getLabelAngle());
        assertTrue(a1.equals(a2));

        // 7. labelToolTip
        a2.setLabelToolTip("TT");
        assertFalse(a1.equals(a2));
        a2.setLabelToolTip(a1.getLabelToolTip());
        assertTrue(a1.equals(a2));

        // 8. labelURL
        a2.setLabelURL("http://abc.com");
        assertFalse(a1.equals(a2));
        a2.setLabelURL(a1.getLabelURL());
        assertTrue(a1.equals(a2));

        // 9. axisLineVisible
        a2.setAxisLineVisible(false);
        assertFalse(a1.equals(a2));
        a2.setAxisLineVisible(true);
        assertTrue(a1.equals(a2));

        // 10. axisLineStroke
        a2.setAxisLineStroke(new BasicStroke(4.0f));
        assertFalse(a1.equals(a2));
        a2.setAxisLineStroke(a1.getAxisLineStroke());
        assertTrue(a1.equals(a2));

        // 11. axisLinePaint
        a2.setAxisLinePaint(Color.ORANGE);
        assertFalse(a1.equals(a2));
        a2.setAxisLinePaint(a1.getAxisLinePaint());
        assertTrue(a1.equals(a2));

        // 12. tickLabelsVisible
        a2.setTickLabelsVisible(false);
        assertFalse(a1.equals(a2));
        a2.setTickLabelsVisible(true);
        assertTrue(a1.equals(a2));

        // 13. tickLabelFont
        a2.setTickLabelFont(new Font("Monospaced", Font.PLAIN, 8));
        assertFalse(a1.equals(a2));
        a2.setTickLabelFont(a1.getTickLabelFont());
        assertTrue(a1.equals(a2));

        // 14. tickLabelPaint
        a2.setTickLabelPaint(Color.PINK);
        assertFalse(a1.equals(a2));
        a2.setTickLabelPaint(a1.getTickLabelPaint());
        assertTrue(a1.equals(a2));

        // 15. tickLabelInsets
        a2.setTickLabelInsets(new RectangleInsets(6, 6, 6, 6));
        assertFalse(a1.equals(a2));
        a2.setTickLabelInsets(a1.getTickLabelInsets());
        assertTrue(a1.equals(a2));

        // 16. tickMarksVisible
        a2.setTickMarksVisible(false);
        assertFalse(a1.equals(a2));
        a2.setTickMarksVisible(true);
        assertTrue(a1.equals(a2));

        // 17. tickMarkInsideLength
        a2.setTickMarkInsideLength(9.0f);
        assertFalse(a1.equals(a2));
        a2.setTickMarkInsideLength(a1.getTickMarkInsideLength());
        assertTrue(a1.equals(a2));

        // 18. tickMarkOutsideLength
        a2.setTickMarkOutsideLength(9.0f);
        assertFalse(a1.equals(a2));
        a2.setTickMarkOutsideLength(a1.getTickMarkOutsideLength());
        assertTrue(a1.equals(a2));

        // 19. tickMarkPaint
        a2.setTickMarkPaint(Color.YELLOW);
        assertFalse(a1.equals(a2));
        a2.setTickMarkPaint(a1.getTickMarkPaint());
        assertTrue(a1.equals(a2));

        // 20. tickMarkStroke
        a2.setTickMarkStroke(new BasicStroke(5.0f));
        assertFalse(a1.equals(a2));
        a2.setTickMarkStroke(a1.getTickMarkStroke());
        assertTrue(a1.equals(a2));

        // 21. fixedDimension
        a2.setFixedDimension(77.0);
        assertFalse(a1.equals(a2));
        a2.setFixedDimension(a1.getFixedDimension());
        assertTrue(a1.equals(a2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithGradientPaint() {
        ConcreteTestAxis a1 = new ConcreteTestAxis("Label");
        ConcreteTestAxis a2 = new ConcreteTestAxis("Label");

        GradientPaint gp1 = new GradientPaint(0f, 0f, Color.BLUE, 10f, 10f, Color.RED);
        GradientPaint gp2 = new GradientPaint(0f, 0f, Color.BLUE, 10f, 10f, Color.RED);
        GradientPaint gp3 = new GradientPaint(0f, 0f, Color.GREEN, 10f, 10f, Color.YELLOW);

        a1.setLabelPaint(gp1);
        a2.setLabelPaint(gp2);
        assertTrue(a1.equals(a2));

        a2.setLabelPaint(gp3);
        assertFalse(a1.equals(a2));
    }

    @Test(timeout = 4000)
    public void testCloneIntegrity() throws Exception {
        ConcreteTestAxis original = new ConcreteTestAxis("Clone Label");
        RecordingAxisChangeListener listener = new RecordingAxisChangeListener();
        original.addChangeListener(listener);

        ConcreteTestAxis cloned = (ConcreteTestAxis) original.clone();

        assertNotSame(original, cloned);
        assertEquals(original.getClass(), cloned.getClass());
        assertTrue(original.equals(cloned));

        // Cloned axis has its plot reference nullified
        assertNull(cloned.getPlot());

        // Cloned axis has an independent listener list
        assertFalse(cloned.hasListener(listener));

        // Changing clone does not trigger original's listener
        cloned.setVisible(!cloned.isVisible());
        assertEquals(0, listener.eventCount);
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        ConcreteTestAxis original = new ConcreteTestAxis("Serialized Label");
        original.setLabelPaint(new GradientPaint(1f, 2f, Color.RED, 3f, 4f, Color.BLACK));
        original.setTickLabelPaint(Color.BLUE);
        original.setAxisLinePaint(Color.DARK_GRAY);
        original.setTickMarkPaint(Color.LIGHT_GRAY);
        original.setAxisLineStroke(new BasicStroke(1.5f));
        original.setTickMarkStroke(new BasicStroke(0.5f));
        original.setLabelToolTip("TT");
        original.setLabelURL("http://test.org");
        original.setFixedDimension(100.5);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(original);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        ConcreteTestAxis deserialized = (ConcreteTestAxis) in.readObject();
        in.close();

        assertNotSame(original, deserialized);
        assertTrue(original.equals(deserialized));

        // Listener list reconstructed after deserialization
        RecordingAxisChangeListener listener = new RecordingAxisChangeListener();
        deserialized.addChangeListener(listener);
        assertTrue(deserialized.hasListener(listener));
        deserialized.setVisible(!deserialized.isVisible());
        assertEquals(1, listener.eventCount);
    }
}