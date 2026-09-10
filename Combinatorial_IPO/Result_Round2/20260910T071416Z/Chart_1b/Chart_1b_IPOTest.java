package org.jfree.chart.renderer.category;

import org.junit.Test;
import static org.junit.Assert.*;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.category.DefaultCategoryDataset;

/** Generated from approved defect-focused scenarios using native IPO. */
public class Chart_1b_IPOTest {

    @Test(timeout = 4000)
    public void test_legend_items_for_dataset_001() throws Exception {
        // Native IPO combination: dataset_size=missing, renderer_index=primary, column_count=one
        int seriesCount = -1;
        int rendererIndex = 0;
        int columns = 1;
        AbstractCategoryItemRenderer renderer = new LineAndShapeRenderer();
        CategoryPlot plot = new CategoryPlot();
        plot.setRenderer(rendererIndex, renderer);
        if (seriesCount >= 0) {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int row = 0; row < seriesCount; row++) {
                for (int column = 0; column < columns; column++) { dataset.addValue(row + column + 1, "S" + row, "C" + column); }
            }
            plot.setDataset(rendererIndex, dataset);
        }
        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(Math.max(0, seriesCount), items.getItemCount());
    }

    @Test(timeout = 4000)
    public void test_legend_items_for_dataset_002() throws Exception {
        // Native IPO combination: dataset_size=missing, renderer_index=secondary, column_count=two
        int seriesCount = -1;
        int rendererIndex = 1;
        int columns = 2;
        AbstractCategoryItemRenderer renderer = new LineAndShapeRenderer();
        CategoryPlot plot = new CategoryPlot();
        plot.setRenderer(rendererIndex, renderer);
        if (seriesCount >= 0) {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int row = 0; row < seriesCount; row++) {
                for (int column = 0; column < columns; column++) { dataset.addValue(row + column + 1, "S" + row, "C" + column); }
            }
            plot.setDataset(rendererIndex, dataset);
        }
        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(Math.max(0, seriesCount), items.getItemCount());
    }

    @Test(timeout = 4000)
    public void test_legend_items_for_dataset_003() throws Exception {
        // Native IPO combination: dataset_size=empty, renderer_index=primary, column_count=two
        int seriesCount = 0;
        int rendererIndex = 0;
        int columns = 2;
        AbstractCategoryItemRenderer renderer = new LineAndShapeRenderer();
        CategoryPlot plot = new CategoryPlot();
        plot.setRenderer(rendererIndex, renderer);
        if (seriesCount >= 0) {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int row = 0; row < seriesCount; row++) {
                for (int column = 0; column < columns; column++) { dataset.addValue(row + column + 1, "S" + row, "C" + column); }
            }
            plot.setDataset(rendererIndex, dataset);
        }
        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(Math.max(0, seriesCount), items.getItemCount());
    }

    @Test(timeout = 4000)
    public void test_legend_items_for_dataset_004() throws Exception {
        // Native IPO combination: dataset_size=empty, renderer_index=secondary, column_count=one
        int seriesCount = 0;
        int rendererIndex = 1;
        int columns = 1;
        AbstractCategoryItemRenderer renderer = new LineAndShapeRenderer();
        CategoryPlot plot = new CategoryPlot();
        plot.setRenderer(rendererIndex, renderer);
        if (seriesCount >= 0) {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int row = 0; row < seriesCount; row++) {
                for (int column = 0; column < columns; column++) { dataset.addValue(row + column + 1, "S" + row, "C" + column); }
            }
            plot.setDataset(rendererIndex, dataset);
        }
        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(Math.max(0, seriesCount), items.getItemCount());
    }

    @Test(timeout = 4000)
    public void test_legend_items_for_dataset_005() throws Exception {
        // Native IPO combination: dataset_size=one_series, renderer_index=primary, column_count=one
        int seriesCount = 1;
        int rendererIndex = 0;
        int columns = 1;
        AbstractCategoryItemRenderer renderer = new LineAndShapeRenderer();
        CategoryPlot plot = new CategoryPlot();
        plot.setRenderer(rendererIndex, renderer);
        if (seriesCount >= 0) {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int row = 0; row < seriesCount; row++) {
                for (int column = 0; column < columns; column++) { dataset.addValue(row + column + 1, "S" + row, "C" + column); }
            }
            plot.setDataset(rendererIndex, dataset);
        }
        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(Math.max(0, seriesCount), items.getItemCount());
    }

    @Test(timeout = 4000)
    public void test_legend_items_for_dataset_006() throws Exception {
        // Native IPO combination: dataset_size=one_series, renderer_index=secondary, column_count=two
        int seriesCount = 1;
        int rendererIndex = 1;
        int columns = 2;
        AbstractCategoryItemRenderer renderer = new LineAndShapeRenderer();
        CategoryPlot plot = new CategoryPlot();
        plot.setRenderer(rendererIndex, renderer);
        if (seriesCount >= 0) {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int row = 0; row < seriesCount; row++) {
                for (int column = 0; column < columns; column++) { dataset.addValue(row + column + 1, "S" + row, "C" + column); }
            }
            plot.setDataset(rendererIndex, dataset);
        }
        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(Math.max(0, seriesCount), items.getItemCount());
    }

    @Test(timeout = 4000)
    public void test_legend_items_for_dataset_007() throws Exception {
        // Native IPO combination: dataset_size=two_series, renderer_index=primary, column_count=one
        int seriesCount = 2;
        int rendererIndex = 0;
        int columns = 1;
        AbstractCategoryItemRenderer renderer = new LineAndShapeRenderer();
        CategoryPlot plot = new CategoryPlot();
        plot.setRenderer(rendererIndex, renderer);
        if (seriesCount >= 0) {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int row = 0; row < seriesCount; row++) {
                for (int column = 0; column < columns; column++) { dataset.addValue(row + column + 1, "S" + row, "C" + column); }
            }
            plot.setDataset(rendererIndex, dataset);
        }
        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(Math.max(0, seriesCount), items.getItemCount());
    }

    @Test(timeout = 4000)
    public void test_legend_items_for_dataset_008() throws Exception {
        // Native IPO combination: dataset_size=two_series, renderer_index=secondary, column_count=two
        int seriesCount = 2;
        int rendererIndex = 1;
        int columns = 2;
        AbstractCategoryItemRenderer renderer = new LineAndShapeRenderer();
        CategoryPlot plot = new CategoryPlot();
        plot.setRenderer(rendererIndex, renderer);
        if (seriesCount >= 0) {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (int row = 0; row < seriesCount; row++) {
                for (int column = 0; column < columns; column++) { dataset.addValue(row + column + 1, "S" + row, "C" + column); }
            }
            plot.setDataset(rendererIndex, dataset);
        }
        LegendItemCollection items = renderer.getLegendItems();
        assertNotNull(items);
        assertEquals(Math.max(0, seriesCount), items.getItemCount());
    }

}
