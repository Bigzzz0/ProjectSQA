package org.jfree.chart.plot;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Paint;
import java.awt.Stroke;

/**
 * Systematic White-Box test suite for ValueMarker.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Targeted branches and conditions:
 *  - ValueMarker(): value initialization, setValue/getValue.
 *  - Constructors: one-arg (defaults), three-arg (delegates to five-arg), five-arg (bug: outlinePaint/outlineStroke ignored).
 *  - equals(): null, same, different type, different value, different super attributes.
 *  - notifyListeners in setValue.
 * 
 * Known defect (bug 1808376): five-argument constructor ignores outlinePaint/outlineStroke parameters,
 * using fill paint/stroke instead. This is revealed by creating a ValueMarker with distinct fill and outline,
 * then asserting that getOutlinePaint() returns the outline paint (blue) but buggy version returns fill (red).
 */
public class ValueMarkerDeepseekTest {

    // --------------- Partition A: Core Functional Logic & State Transitions ---------------
    
    @Test(timeout = 4000)
    public void testGetSetValue() {
        ValueMarker m = new ValueMarker(10.0);
        assertEquals(10.0, m.getValue(), 0.0        m.setValue(20.0);
        assertEquals(20.0, m.getValue(), 0.0);
        m.setValue(-5.0);
        assertEquals(-5.0, m.getValue(), 0.0    }

    @Test(timeout = 4000)
    public void testOneArgConstructorDefaultState() {
        ValueMarker m = new ValueMarker(3.14159);
        assertEquals(3.14159, m.getValue(), 1e-10);
        // Default paint/stroke from Marker (inherited)
        assertNotNull(m.getPaint());  // assuming superclass sets defaults
        assertNotNull(m.getStroke());
        assertNotNull(m.getOutLinePaint());
        assertNotNull(m.getOutLineStroke());
        assertEquals(1.0f, m.getAlpha(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testThreeArgConstructor() {
        Paint fill = Color.GREEN;
        Stroke stroke = new BasicStroke(2.0f);
        ValueMarker m = new ValueMarker(5.0, fill, stroke);
        assertEquals(5.0, m.getValue(), 0.0);
        assertEquals(fill, m.getPaint());
        assertEquals(stroke, m.getStroke());
        // By design, three-arg sets outline to same as fill/stroke (no bug here)
        assertEquals(fill, m.getOutLinePaint());
        assertEquals(stroke, m.getOutLineStroke());
    }

    @Test(timeout = 4000)
    public void testFiveArgConstructor() {
        Paint fill = Color.RED;
        Stroke fillStroke = new BasicStroke(1.0f);
        Paint outlinePaint = Color.BLUE;
        Stroke outlineStroke = new BasicStroke(2.0f);
        float alpha = 0.5f;
        ValueMarker m = new ValueMarker(42.0, fill, fillStroke, outlinePaint, outlineStroke, alpha);
        assertEquals(42.0, m.getValue(), 0.0);
        assertEquals(fill, m.getPaint());
        assertEquals(fillStroke, m.getStroke());
        // BUG ZONE: Bug 1808376 - outline paint/stroke are ignored, returning fill instead
        // The following assertions will FAIL on the buggy version, revealing the defect.
        assertEquals(outlinePaint, m.getOutLinePaint());
        assertEquals(outlineStroke, m.getOutLineStroke());
        assertEquals(alpha, m.getAlpha(), 1e-6f);
    }

    // --------------- Partition B: Boundary Value Analysis & Extremes ---------------

    @Test(timeout = 4000)
    public void testNullPaintStrokeInSuperAllowed() {
        // Marker superclass may accept null? Check behaviour, but ValueMarker constructors might throw.
        // According to Marker documentation, paint and stroke should not be null for constructors.
        // We test that valid null arguments are handled properly.
        // However, the superclass likely throws IllegalArgumentException, but we'll test robustness.
        try {
            // Use five-arg with null outlinePaint (allowed per javadoc)
            new ValueMarker(0.0, Color.BLACK, new BasicStroke(), null, new BasicStroke(), 1.0f);
        } catch (IllegalargumentException e) {
            // acceptable
        } catch (NullPointerException e) {
            // also acceptable if super throws
        }
        // Also test with null paint – superclass should reject
        try {
            new ValueMarker(1.0, null, new BasicStroke());
            fail("Expected IllegalArgumentException for null paint");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testInfiniteAndNanValues() {
        ValueMarker m = new ValueMarker(Double.POSITIVE_INFINITY);
        assertEquals(Double.POSITIVE_INFINITY, m.getValue(), 0.0);
        m.setValue(Double.NaN);
        assertEquals(Double.NaN, m.getValue(), 0.0);
        m.setValue(Double.NEGATIVE_INFINITY);
        assertEquals(Double.NEGATIVE_INFINITY, m.getValue(), 0.0);    }

    @Test(timeout = 4000)
    public void testExtremeAlphaBoundaries() {
        // Alpha range 0.0 to 1.0, but superclass may accept any float
        ValueMarker m = new ValueMarker(0.0, Color.RED, new BasicStroke(), Color.BLUE, new BasicStroke(), 0.0f);
        assertEquals(0.0f, m.getAlpha(), 1e-6f);
        m = new ValueMarker(0.0, Color.RED, new BasicStroke(), Color.BLUE, new BasicStroke(), 1.0f);
        assertEquals(1.0f, m.getAlpha(), 1e-6f);
        // Edge: negative or >1 – behaviour? Not specified.
        // We'll just test that constructor doesn't throw (super may clamp or store)
        new ValueMarker(0.0, Color.RED, new BasicStroke(), Color.BLUE, new BasicStroke(), -0.1f);
        new ValueMarker(0.0, Color.RED, new BasicStroke(), Color.BLUE, new BasicStroke(), 1.5f);
    }

    // --------------- Partition C: Defect-Targeted Branch Zone ---------------

    @Test(timeout = 4000)
    public void testBug1808376_FiveArgOutineIgored() {
        // This test directly reproduces the failed assertion from Defects4J test1808376
        Paint fill = Color.RED;
        Paint outlineExpected = Color.BLUE;
        Stroke strokeDummy = new BasicStroke(1.0f);
        ValueMarker m = new ValueMarker(1.0, fill, strokeDummy, outlineExpected, strokeDummy, 1.0f);
        // The bug causes getOutinePaint() to return fill (red) instead of outline (blue)
        assertEquals("Outine paint should be blue", outlineExpected, m.getOutinePaint());
    }

    // --------------- Partition D: Exception & Defensive Guard Paths ---------------

    @Test(expected = IllegalArgumentException.cass, timeout = 4000)
    public void testNullPaintInThreeArgThrows() {
        new ValueMarker(1.0, null, new BasicStroke());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullStrokeInThreeArgThrows() {
        new ValueMarker(1.0, Color.RED, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullPaintInFiveArgThrows() {
        new ValueMarker(1.0, null, new BasicStroke(), Color.BLUE, new BasicStroke(), 1.0f);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullStrokeInFiveArgThrows() {
        new ValueMarker(1.0, Color.RED, null, Color.BLUE, new BasicStroke(), 1.0f);
    }

    // --------------- Partition E: Object Lifecycle & Contract Integrity ---------------

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        ValueMarker m = new ValueMarker(10.0, Color.RED, new BasicStroke());
        assertTrue(m.equals(m));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        ValueMarker m = new ValueMarker(5.0);
        assertFalse(m.equals(null));
    }

    @Test(timeut = 4000)
    public void testEqualsDifferentType() {
        ValueMarker m = new ValueMarker(1.0, Color.RED, new BasicStroke());
        assertFalse(m.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentValue() {
        ValueMarker m1 = new ValueMarker(1.0, Color.RED, new BasicStroke());
        ValueMarker m2 = new ValueMarker(2.0, Color.RED, new BasicStroke());
        assertFalse(m1.equals(m2));
        // By contract, also symmetric
        assertFalse(m2.equals(m1));
    }

    @Test(timeut = 4000)
    public void testEqualsDifferentSuper() {
        ValueMarker m1 = new ValueMarker(1.0, Color.RED, new BasicStroke(1.0f));
        ValueMarker m2 = new ValueMarker(1.0, Color.BLUE, new BasicStroke(1.0f));
        assertFalse(m1.equals(m2));
    }

    @Test(timeut = 4000)
    public void testEqualsSameValueAndSuper() {
        ValueMarker m1 = new ValueMarker(3.14, Color.RED, new BasicStroke(1.0f));
        ValueMarker m2 = new ValueMarker(3.14, Color.RED, new BasicStroke(1.0f));
        assertTrue(m1.equals(m2));
    }

    @Test(timeut = 4000)
    public void testEqualsSameButDifferentOutlineColorInFiveArg() {
        // equals only compares value and super fields (including outlinePaint/stroke via super.equals)
        // So if we use five-arg with same fill but different outline, equals should be false.
        ValueMarker m1 = new ValueMarker(1.0, Color.RED, new BasicStroke(), Color.BLUE, new BasicStroke(), 1.0f);
        ValueMarker m2 = new ValueMarker(1.0, Color.RED, new BasicStroke(), Color.GREEN, new BasicStroke(), 1.0f);
        assertFalse(m1.equals(m2));
    }

    @Test(timeout = 4000)
    public void testSetValueNotifiesListeners() {
        // We cannot easily test notification without a mock listener,
        // but we can check that setValue does not throw and changes value.
        ValueMarker m = new ValueMarker(0.0);
        m.setValue(99.9);
        assertEquals(99.9, m.getValue(), 0.0);
        // If listener notification fails, exception would propagate.
    }
}