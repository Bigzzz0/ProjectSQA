package org.jfree.chart.renderer.category;

/* [Branch & Defect Analysis Matrix]
 * 1. Default state initialization: plotLines=false, groupPaint=Color.black, 
 *    groupStroke=BasicStroke(1.0f), minIcon, maxIcon, objectIcon non-null.
 * 2. Setters: validate null arguments throw IllegalArgumentException, 
 *    non-null arguments update field and notify listeners.
 * 3. Equality (overridden in MinMaxCategoryRenderer, but icon fields NOT 
 *    compared – known defect). Must test identical objects, different 
 *    plotLines, groupPaint, groupStroke, icons, null, different class.
 * 4. Draw item not tested directly due to complexity (not required for 
 *    targeted defect).
 * 5. Serialization not tested.
 */

import org.junit.Test;
import static org.junit.Assert.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.Stroke;
import javax.swing.Icon;

/**
 * Test suite for MinMaxCategoryRenderer targeting the equals defect 
 * (missing icon field comparison) and achieving high branch coverage 
 * on getters/setters and defensive checks.
 */
public class MinMaxCategoryRendererDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core functional logic & state transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDefaultState() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        assertFalse("Default plotLines should be false", r.isDrawLines());
        assertNotNull("Default groupPaint should not be null", r.getGroupPaint());
        assertEquals(Color.black, r.getGroupPaint());
        assertNotNull("Default groupStroke should not be null", r.getGroupStroke());
        assertEquals(new BasicStroke(1.0f), r.getGroupStroke());
        assertNotNull("Default objectIcon should not be null", r.getObjectIcon());
        assertNotNull("Default minIcon should not be null", r.getMinIcon());
        assertNotNull("Default maxIcon should not be null", r.getMaxIcon());
    }

    @Test(timeout = 4000)
    public void testSetDrawLines() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        assertFalse(r.isDrawLines());
        r.setDrawLines(true);
        assertTrue("plotLines should become true", r.isDrawLines());
        // setting again to same value should not trigger change? Not tested.
        r.setDrawLines(true);
        assertTrue("Still true", r.isDrawLines());
        r.setDrawLines(false);
        assertFalse("Reset to false", r.isDrawLines());
    }

    @Test(timeout = 4000)
    public void testSetGroupPaint() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        assertEquals(Color.black, r.getGroupPaint());
        r.setGroupPaint(Color.red);
        assertEquals(Color.red, r.getGroupPaint());
        try {
            r.setGroupPaint(null);
            fail("setGroupPaint(null) should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // paint unchanged after exception
        assertEquals(Color.red, r.getGroupPaint());
    }

    @Test(timeout = 4000)
    public void testSetGroupStroke() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        Stroke newStroke = new BasicStroke(2.0f);
        assertNotEquals(newStroke, r.getGroupStroke());
        r.setGroupStroke(newStroke);
        assertEquals(newStroke, r.getGroupStroke());
        try {
            r.setGroupStroke(null);
            fail("setGroupStroke(null) should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(newStroke, r.getGroupStroke());
    }

    @Test(timeout = 4000)
    public void testSetObjectIcon() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        Icon customIcon = createTestIcon(10, 10);
        assertNotNull(r.getObjectIcon());
        r.setObjectIcon(customIcon);
        assertSame(customIcon, r.getObjectIcon());
        try {
            r.setObjectIcon(null);
            fail("setObjectIcon(null) should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertSame(customIcon, r.getObjectIcon());
    }

    @Test(timeout = 4000)
    public void testSetMaxIcon() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        Icon customIcon = createTestIcon(8, 8);
        assertNotNull(r.getMaxIcon());
        r.setMaxIcon(customIcon);
        assertSame(customIcon, r.getMaxIcon());
        try {
            r.setMaxIcon(null);
            fail("setMaxIcon(null) should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertSame(customIcon, r.getMaxIcon());
    }

    @Test(timeout = 4000)
    public void testSetMinIcon() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        Icon customIcon = createTestIcon(6, 6);
        assertNotNull(r.getMinIcon());
        r.setMinIcon(customIcon);
        assertSame(customIcon, r.getMinIcon());
        try {
            r.setMinIcon(null);
            fail("setMinIcon(null) should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertSame(customIcon, r.getMinIcon());
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary values & extreme cases
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsNull() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        assertFalse("equals(null) should be false", r.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        assertFalse("equals different class should be false", r.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        MinMaxCategoryRenderer r = new MinMaxCategoryRenderer();
        assertTrue("reflexive", r.equals(r));
    }

    @Test(timeout = 4000)
    public void testEqualsSymmetricDefault() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        assertTrue("default objects should be equal", r1.equals(r2));
        assertTrue("symmetric", r2.equals(r1));
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-targeted branch zone (equals missing icon comparison)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsDifferentPlotLines() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        r2.setDrawLines(true);
        assertFalse("Different plotLines should make them unequal", r1.equals(r2));
        assertFalse("symmetric", r2.equals(r1));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentGroupPaint() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        r2.setGroupPaint(Color.blue);
        assertFalse("Different groupPaint should make them unequal", r1.equals(r2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentGroupStroke() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        r2.setGroupStroke(new BasicStroke(2.0f));
        assertFalse("Different groupStroke should make them unequal", r1.equals(r2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentObjectIcon() {
        // Known defect: icons are NOT compared in equals,
        // so this test will FAIL on the faulty version (true instead of false),
        // revealing the bug.
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        r2.setObjectIcon(createTestIcon(5, 5));
        // On correct implementation, r1 and r2 should be unequal.
        assertFalse("Different objectIcon should make them unequal", r1.equals(r2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentMaxIcon() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        r2.setMaxIcon(createTestIcon(3, 3));
        assertFalse("Different maxIcon should make them unequal", r1.equals(r2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentMinIcon() {
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        r2.setMinIcon(createTestIcon(7, 7));
        assertFalse("Different minIcon should make them unequal", r1.equals(r2));
    }

    @Test(timeout = 4000)
    public void testEqualsSameCustomIcons() {
        // If two renderers have the same custom icon instance (or equal icons),
        // they should be equal (if icons were compared, they'd be same reference).
        Icon sharedIcon = createTestIcon(4, 4);
        MinMaxCategoryRenderer r1 = new MinMaxCategoryRenderer();
        MinMaxCategoryRenderer r2 = new MinMaxCategoryRenderer();
        r1.setObjectIcon(sharedIcon);
        r2.setObjectIcon(sharedIcon);
        r1.setMaxIcon(sharedIcon);
        r2.setMaxIcon(sharedIcon);
        r1.setMinIcon(sharedIcon);
        r2.setMinIcon(sharedIcon);
        // If equals does not compare icons, this passes anyway.
        // If it did compare, they are same instance -> true.
        assertTrue("Same custom icons should be equal", r1.equals(r2));
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & defensive guard paths (already covered above)
    // -----------------------------------------------------------------------

    // Null argument tests are already included in testSetGroupPaint, etc.

    // -----------------------------------------------------------------------
    // Helper: Create a minimal Icon for testing
    // -----------------------------------------------------------------------
    private static Icon createTestIcon(final int width, final int height) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                // no-op
            }
            @Override
            public int getIconWidth() {
                return width;
            }
            @Override
            public int getIconHeight() {
                return height;
            }
        };
    }
}