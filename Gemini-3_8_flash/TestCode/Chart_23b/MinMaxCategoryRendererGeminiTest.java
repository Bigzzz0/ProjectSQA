package org.jfree.chart.renderer.category;

import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.swing.Icon;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: org.jfree.chart.renderer.category.MinMaxCategoryRenderer
 *
 * 1. DEFECT-TARGETED ZONE (Defects4J ground truth: MinMaxCategoryRendererTests::testEquals):
 *    - The JFreeChart 1.0.7 release documented an equals(Object) method override in the Javadoc,
 *      but the method declaration and body were accidentally omitted from MinMaxCategoryRenderer.java!
 *    - As a result, MinMaxCategoryRenderer relies on AbstractCategoryItemRenderer.equals(Object),
 *      which does NOT compare 'plotLines', 'groupPaint', or 'groupStroke'.
 *    - Targeted Tests:
 *      * testEqualsDefect_PlotLines(): Changes plotLines; expects r1.equals(r2) to be false.
 *      * testEqualsDefect_GroupPaint(): Changes groupPaint; expects r1.equals(r2) to be false.
 *      * testEqualsDefect_GroupStroke(): Changes groupStroke; expects r1.equals(r2) to be false.
 *
 * 2. CORE FUNCTIONAL & BRANCH COVERAGE MATRIX:
 *    - isDrawLines() / setDrawLines(boolean):
 *      * Branch: this.plotLines != draw (true -> notifyListeners; false -> no-op).
 *    - getGroupPaint() / setGroupPaint(Paint):
 *      * Branch: paint == null (throw IllegalArgumentException) vs valid Paint (set and notify).
 *    - getGroupStroke() / setGroupStroke(Stroke):
 *      * Branch: stroke == null (throw IllegalArgumentException) vs valid Stroke (set and notify).
 *    - getObjectIcon() / setObjectIcon(Icon):
 *      * Branch: icon == null (throw IllegalArgumentException) vs valid Icon (set and notify).
 *    - getMaxIcon() / setMaxIcon(Icon):
 *      * Branch: icon == null (throw IllegalArgumentException) vs valid Icon (set and notify).
 *    - getMinIcon() / setMinIcon(Icon):
 *      * Branch: icon == null (throw IllegalArgumentException) vs valid Icon (set and notify).
 *    - drawItem(...) branches:
 *      * value == null vs value != null
 *      * orient == PlotOrientation.VERTICAL vs PlotOrientation.HORIZONTAL
 *      * lastCategory == column vs lastCategory != column (reset min and max)
 *      * min > value (update min), max < value (update max)
 *      * dataset.getRowCount() - 1 == row (last series: draw group line, minIcon, maxIcon)
 *      * plotLines == true:
 *          - column == 0 (no predecessor)
 *          - column != 0 and previousValue == null
 *          - column != 0 and previousValue != null (draw line in VERTICAL and HORIZONTAL)
 *      * EntityCollection: entities != null (addItemEntity) vs entities == null
 *    - getIcon(...) private helpers (via minIcon, maxIcon, objectIcon paintIcon, getIconWidth, getIconHeight):
 *      * Shape bounds translation, fill != null, outline != null, fillPaint != null, outlinePaint != null.
 *    - Serialization: writeObject / readObject custom serialization for groupStroke, groupPaint, and icons.
 *    - Cloning: clone() contract integrity and deep/shallow field checks.
 * -----------------------------------------------------------------------------------------
 */
public class MinMaxCategoryRendererGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultStateAndProperties() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        assertFalse(renderer.isDrawLines());
        assertEquals(Color.black, renderer.getGroupPaint());
        assertEquals(new BasicStroke(1.0f), renderer.getGroupStroke());
        assertNotNull(renderer.getMinIcon());
        assertNotNull(renderer.getMaxIcon());
        assertNotNull(renderer.getObjectIcon());
    }

    @Test(timeout = 4000)
    public void testSetDrawLinesNotifiesListenerOnlyOnChange() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        TestRendererChangeListener listener = new TestRendererChangeListener();
        renderer.addChangeListener(listener);

        // Same value (false -> false): no event
        renderer.setDrawLines(false);
        assertEquals(0, listener.eventCount);

        // State change (false -> true): triggers event
        renderer.setDrawLines(true);
        assertTrue(renderer.isDrawLines());
        assertEquals(1, listener.eventCount);
        assertSame(renderer, listener.lastEvent.getRenderer());

        // State change (true -> false): triggers event
        renderer.setDrawLines(false);
        assertFalse(renderer.isDrawLines());
        assertEquals(2, listener.eventCount);
    }

    @Test(timeout = 4000)
    public void testSetGroupPaintAndListener() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        TestRendererChangeListener listener = new TestRendererChangeListener();
        renderer.addChangeListener(listener);

        renderer.setGroupPaint(Color.red);
        assertEquals(Color.red, renderer.getGroupPaint());
        assertEquals(1, listener.eventCount);
    }

    @Test(timeout = 4000)
    public void testSetGroupStrokeAndListener() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        TestRendererChangeListener listener = new TestRendererChangeListener();
        renderer.addChangeListener(listener);

        Stroke stroke = new BasicStroke(2.5f);
        renderer.setGroupStroke(stroke);
        assertEquals(stroke, renderer.getGroupStroke());
        assertEquals(1, listener.eventCount);
    }

    @Test(timeout = 4000)
    public void testSetIconsAndListeners() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        TestRendererChangeListener listener = new TestRendererChangeListener();
        renderer.addChangeListener(listener);

        DummyIcon icon1 = new DummyIcon(12, 14);
        DummyIcon icon2 = new DummyIcon(15, 16);
        DummyIcon icon3 = new DummyIcon(18, 20);

        renderer.setMinIcon(icon1);
        assertSame(icon1, renderer.getMinIcon());
        assertEquals(1, listener.eventCount);

        renderer.setMaxIcon(icon2);
        assertSame(icon2, renderer.getMaxIcon());
        assertEquals(2, listener.eventCount);

        renderer.setObjectIcon(icon3);
        assertSame(icon3, renderer.getObjectIcon());
        assertEquals(3, listener.eventCount);
    }

    @Test(timeout = 4000)
    public void testIconDimensionsAndPainting() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        Icon minIcon = renderer.getMinIcon();
        Icon maxIcon = renderer.getMaxIcon();
        Icon objIcon = renderer.getObjectIcon();

        assertTrue(minIcon.getIconWidth() > 0);
        assertTrue(minIcon.getIconHeight() > 0);
        assertTrue(maxIcon.getIconWidth() > 0);
        assertTrue(maxIcon.getIconHeight() > 0);
        assertTrue(objIcon.getIconWidth() > 0);

        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        minIcon.paintIcon(null, g2, 5, 5);
        maxIcon.paintIcon(null, g2, 10, 10);
        objIcon.paintIcon(null, g2, 15, 15);
        g2.dispose();
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Comprehensive Branch Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testDrawItemVerticalWithMinMaxAndLines() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        renderer.setDrawLines(true);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R0", "C0");
        dataset.addValue(30.0, "R1", "C0");
        dataset.addValue(5.0, "R0", "C1");
        dataset.addValue(40.0, "R1", "C1");

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("X"), new NumberAxis("Y"), renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        JFreeChart chart = new JFreeChart(plot);

        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0, 0, 300, 200));
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testDrawItemHorizontalWithMinMaxAndLines() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        renderer.setDrawLines(true);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10.0, "R0", "C0");
        dataset.addValue(30.0, "R1", "C0");
        dataset.addValue(5.0, "R0", "C1");
        dataset.addValue(40.0, "R1", "C1");

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("X"), new NumberAxis("Y"), renderer);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        JFreeChart chart = new JFreeChart(plot);

        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0, 0, 300, 200));
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testDrawItemWithNullValuesAndMissingPredecessor() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        renderer.setDrawLines(true);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // C0 has null for R0, so in C1 the previousValue for R0 is null
        dataset.addValue(null, "R0", "C0");
        dataset.addValue(15.0, "R1", "C0");
        dataset.addValue(25.0, "R0", "C1");
        dataset.addValue(35.0, "R1", "C1");

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("X"), new NumberAxis("Y"), renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        JFreeChart chart = new JFreeChart(plot);

        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0, 0, 300, 200));
        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testDrawItemDirectInvocationsEntityAndNullStateBranches() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        renderer.setDrawLines(true);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(12.0, "R0", "C0");
        dataset.addValue(22.0, "R1", "C0");
        dataset.addValue(8.0, "R0", "C1");
        dataset.addValue(18.0, "R1", "C1");

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("Domain"), new NumberAxis("Range"), renderer);
        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        Rectangle2D dataArea = new Rectangle2D.Double(10, 10, 280, 180);

        ChartRenderingInfo chartInfo = new ChartRenderingInfo(new StandardEntityCollection());
        PlotRenderingInfo plotInfo = new PlotRenderingInfo(chartInfo);
        CategoryItemRendererState stateWithEntities = renderer.initialise(g2, dataArea, plot, 0, plotInfo);

        // Execute pass 0 for all items to populate entities
        for (int c = 0; c < dataset.getColumnCount(); c++) {
            for (int r = 0; r < dataset.getRowCount(); r++) {
                renderer.drawItem(g2, stateWithEntities, dataArea, plot,
                        plot.getDomainAxis(), plot.getRangeAxis(), dataset, r, c, 0);
            }
        }
        assertTrue("Entities should have been recorded",
                stateWithEntities.getEntityCollection().getEntityCount() > 0);

        // Test with state having null EntityCollection
        CategoryItemRendererState stateWithoutEntities = new CategoryItemRendererState(null);
        renderer.drawItem(g2, stateWithoutEntities, dataArea, plot,
                plot.getDomainAxis(), plot.getRangeAxis(), dataset, 0, 0, 0);

        g2.dispose();
    }

    @Test(timeout = 4000)
    public void testMinMaxReversalBranchCoverage() {
        // Test dataset where second series has a smaller value (min branch triggered)
        // and where second series has a larger value (max branch triggered)
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // C0: R0=50, R1=10 (min updated from 50 to 10)
        dataset.addValue(50.0, "R0", "C0");
        dataset.addValue(10.0, "R1", "C0");
        // C1: R0=20, R1=80 (max updated from 20 to 80)
        dataset.addValue(20.0, "R0", "C1");
        dataset.addValue(80.0, "R1", "C1");

        CategoryPlot plot = new CategoryPlot(dataset, new CategoryAxis("X"), new NumberAxis("Y"), renderer);
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        JFreeChart chart = new JFreeChart(plot);

        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        chart.draw(g2, new Rectangle2D.Double(0, 0, 300, 200));
        g2.dispose();
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the missing equals() method in MinMaxCategoryRenderer.
     * In the defective version, MinMaxCategoryRenderer does NOT override equals(),
     * causing r1.equals(r2) to call AbstractCategoryItemRenderer.equals(obj),
     * which ignores plotLines, groupPaint, and groupStroke!
     */
    @Test(timeout = 4000)
    public void testEqualsDefect_PlotLines() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        assertEquals(r1, r2);

        r1.setDrawLines(true);
        assertFalse("equals() must detect different 'plotLines' setting!", r1.equals(r2));
        r2.setDrawLines(true);
        assertTrue(r1.equals(r2));
    }

    @Test(timeout = 4000)
    public void testEqualsDefect_GroupPaint() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();

        r1.setGroupPaint(Color.red);
        assertFalse("equals() must detect different 'groupPaint'!", r1.equals(r2));
        r2.setGroupPaint(Color.red);
        assertTrue(r1.equals(r2));
    }

    @Test(timeout = 4000)
    public void testEqualsDefect_GroupStroke() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();

        r1.setGroupStroke(new BasicStroke(2.2f));
        assertFalse("equals() must detect different 'groupStroke'!", r1.equals(r2));
        r2.setGroupStroke(new BasicStroke(2.2f));
        assertTrue(r1.equals(r2));
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetGroupPaintNullThrowsException() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        renderer.setGroupPaint(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetGroupStrokeNullThrowsException() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        renderer.setGroupStroke(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetObjectIconNullThrowsException() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        renderer.setObjectIcon(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetMaxIconNullThrowsException() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        renderer.setMaxIcon(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetMinIconNullThrowsException() {
        MinMaxCategoryRenderer renderer = new MinMaxCategoryRenderer();
        renderer.setMinIcon(null);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity (Serialization, Cloning)
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        r1.setDrawLines(true);
        r1.setGroupPaint(Color.blue);
        r1.setGroupStroke(new BasicStroke(3.0f));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(r1);
        oos.flush();
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        MinMaxCategoryRenderer r2 = (MinMaxCategoryRenderer) ois.readObject();
        ois.close();

        assertEquals(r1.isDrawLines(), r2.isDrawLines());
        assertEquals(r1.getGroupPaint(), r2.getGroupPaint());
        assertEquals(r1.getGroupStroke(), r2.getGroupStroke());
        assertNotNull(r2.getMinIcon());
        assertNotNull(r2.getMaxIcon());
        assertNotNull(r2.getObjectIcon());
    }

    @Test(timeout = 4000)
    public void testCloningContract() throws CloneNotSupportedException {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        r1.setDrawLines(true);
        r1.setGroupPaint(Color.green);
        r1.setGroupStroke(new BasicStroke(1.5f));

        MinMaxCategoryRenderer r2 = (MinMaxCategoryRenderer) r1.clone();
        assertNotSame(r1, r2);
        assertSame(r1.getClass(), r2.getClass());
        assertEquals(r1.isDrawLines(), r2.isDrawLines());
        assertEquals(r1.getGroupPaint(), r2.getGroupPaint());
        assertEquals(r1.getGroupStroke(), r2.getGroupStroke());
    }

    // =========================================================================
    // Test Helpers
    // =========================================================================

    private static class TestRendererChangeListener implements RendererChangeListener {
        int eventCount = 0;
        RendererChangeEvent lastEvent = null;

        @Override
        public void rendererChanged(RendererChangeEvent event) {
            this.eventCount++;
            this.lastEvent = event;
        }
    }

    private static class DummyIcon implements Icon {
        private final int width;
        private final int height;

        public DummyIcon(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            // No-op for test dummy
        }

        @Override
        public int getIconWidth() {
            return this.width;
        }

        @Override
        public int getIconHeight() {
            return this.height;
        }
    }
}