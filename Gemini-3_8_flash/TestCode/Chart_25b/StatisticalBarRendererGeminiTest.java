package org.jfree.chart.renderer.category;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Decision / Branch Condition                               Targeted Inputs & Edge Cases
 * ----------------------------------------------------------------------------------------------------
 * drawItem: data !instanceof StatisticalCategoryDataset     Pass DefaultCategoryDataset -> IllegalArgumentException
 * orientation: HORIZONTAL vs VERTICAL                       PlotOrientation.HORIZONTAL and VERTICAL coverage
 * Mean value: null (Defects4J Bug Trigger)                  dataset.add(null, 1.0, "R", "C") -> NullPointerException
 * Std dev value: null (Defects4J Bug Trigger)               dataset.add(1.0, null, "R", "C") -> NullPointerException
 * Series count: seriesCount > 1 vs == 1                     Dataset with 1 row vs 2 rows
 * Clipping intervals:
 *   - uclip <= 0.0 (value >= uclip, value <= lclip)         Negative/Zero ranges on range axis
 *   - lclip <= 0.0 (value >= uclip, value <= lclip)         Normal mixed range crossing zero
 *   - uclip > 0 && lclip > 0 (value <= lclip, >= uclip)     Strictly positive axis clip boundaries
 * Outline drawing: isDrawBarOutline() & barWidth > 3        setDrawBarOutline(true/false), narrow/wide bars
 * ErrorIndicatorStroke: null vs custom Stroke               setErrorIndicatorStroke(null) / non-null
 * ErrorIndicatorPaint: null vs custom Paint                 setErrorIndicatorPaint(null) / non-null
 * Item label generator: null vs non-null visible label      setItemLabelsVisible(true) with generator
 * EntityCollection: null vs active collection               ChartRenderingInfo with StandardEntityCollection
 * Object Lifecycle: equals, hashCode, clone, serialization  Identity, null, class-cast, deep copy, byte streams
 * ----------------------------------------------------------------------------------------------------
 */
public class StatisticalBarRendererGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetAndSetErrorIndicatorPaint() {
        StatisticalBarRenderer renderer = new StatisticalBarRenderer();
        assertEquals(Color.gray, renderer.getErrorIndicatorPaint());

        renderer.setErrorIndicatorPaint(Color.red);
        assertEquals(Color.red,