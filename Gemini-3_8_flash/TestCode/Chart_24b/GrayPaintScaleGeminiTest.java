package org.jfree.chart.renderer;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Paint;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: org.jfree.chart.renderer.GrayPaintScale
 *
 * Decision / Condition Coverage Targets:
 * 1. Default constructor: GrayPaintScale() -> delegates to this(0.0, 1.0)
 * 2. Parameterized constructor: GrayPaintScale(double, double)
 *    - Branch: lowerBound >= upperBound (True -> throws IllegalArgumentException)
 *      * Condition: lowerBound == upperBound (Boundary)
 *      * Condition: lowerBound > upperBound
 *    - Branch: lowerBound < upperBound (False -> successful initialization)
 * 3. getLowerBound() & getUpperBound(): Correct getters returning constructor inputs
 * 4. getPaint(double value):
 *    - Boundary: value == lowerBound -> shade 0 (Color(0, 0, 0))
 *    - Boundary: value == upperBound -> shade 255 (Color(255, 255, 255))
 *    - Midpoint: value == (lowerBound + upperBound) / 2 -> shade 127 (Color(127, 127, 127))
 *    - Clamping Branch: value < lowerBound (Defects4J Bug 1767315 / testGetPaint failure)
 *      * DEFECT: 'v' is clamped via Math.max / Math.min, but the calculation erroneously
 *        uses raw 'value' instead of clamped 'v', producing negative RGB and throwing IAE.
 *    - Clamping Branch: value > upperBound (Defects4J Bug 1767315)
 *      * DEFECT: using raw 'value' exceeds 255, producing out-of-range RGB and throwing IAE.
 * 5. equals(Object obj):
 *    - Branch: obj == this (True -> returns true)
 *    - Branch: !(obj instanceof GrayPaintScale) (True -> returns false for null and other types)
 *    - Branch: this.lowerBound != that.lowerBound (True -> returns false)
 *    - Branch: this.upperBound != that.upperBound (True -> returns false)
 *    - Branch: all fields match (returns true)
 * 6. clone():
 *    - PublicCloneable implementation returning a distinct, equal GrayPaintScale instance.
 * 7. Serialization:
 *    - Round-trip serialization ensuring state consistency.
 */
public class GrayPaintScaleGeminiTest {

    private static final double EPSILON = 1e-9;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        GrayPaintScale scale = new GrayPaintScale();
        assertEquals(0.0, scale.getLowerBound(), EPSILON);
        assertEquals(1.0, scale.getUpperBound(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testCustomRangeConstructor() {
        GrayPaintScale scale = new GrayPaintScale(-10.0, 50.0);
        assertEquals(-10.0, scale.getLowerBound(), EPSILON);
        assertEquals(50.0, scale.getUpperBound(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testGetPaintAtMidpoint() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 100.0);
        Paint paint = scale.getPaint(50.0);
        assertTrue("Paint must be instance of Color", paint instanceof Color);
        Color color = (Color) paint;
        assertEquals(127, color.getRed());
        assertEquals(127, color.getGreen());
        assertEquals(127, color.getBlue());
        assertEquals(255, color.getAlpha());
    }

    @Test(timeout = 4000)
    public void testGetPaintWithNegativeRange() {
        GrayPaintScale scale = new GrayPaintScale(-100.0, -50.0);
        Paint paint = scale.getPaint(-75.0);
        assertTrue(paint instanceof Color);
        Color color = (Color) paint;
        assertEquals(127, color.getRed());
        assertEquals(127, color.getGreen());
        assertEquals(127, color.getBlue());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetPaintAtExactLowerBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(0.0);
        assertTrue(paint instanceof Color);
        Color color = (Color) paint;
        assertEquals(0, color.getRed());
        assertEquals(0, color.getGreen());
        assertEquals(0, color.getBlue());
        assertEquals(Color.BLACK, color);
    }

    @Test(timeout = 4000)
    public void testGetPaintAtExactUpperBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(1.0);
        assertTrue(paint instanceof Color);
        Color color = (Color) paint;
        assertEquals(255, color.getRed());
        assertEquals(255, color.getGreen());
        assertEquals(255, color.getBlue());
        assertEquals(Color.WHITE, color);
    }

    @Test(timeout = 4000)
    public void testConstructorWithVerySmallInterval() {
        double lower = 1.0;
        double upper = 1.0 + 1e-6;
        GrayPaintScale scale = new GrayPaintScale(lower, upper);
        assertEquals(lower, scale.getLowerBound(), EPSILON);
        assertEquals(upper, scale.getUpperBound(), EPSILON);
        Paint lowerPaint = scale.getPaint(lower);
        assertEquals(Color.BLACK, lowerPaint);
        Paint upperPaint = scale.getPaint(upper);
        assertEquals(Color.WHITE, upperPaint);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // =========================================================================

    /**
     * Targets the defect where values below lowerBound cause getPaint() to compute
     * a negative RGB color component (e.g. g < 0) because 'value' is used instead
     * of clamped 'v', triggering:
     * java.lang.IllegalArgumentException: Color parameter outside of expected range: Red Green Blue
     */
    @Test(timeout = 4000)
    public void testGetPaintClampingBelowLowerBoundDefect() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(-0.5);
        assertNotNull("Paint should not be null when value < lowerBound", paint);
        assertTrue("Paint must be instance of Color", paint instanceof Color);
        assertEquals("Value below lower bound must be clamped to lower bound color (Black)",
                Color.BLACK, paint);
    }

    /**
     * Targets the defect where values above upperBound cause getPaint() to compute
     * an RGB color component exceeding 255 (e.g. g > 255) because 'value' is used
     * instead of clamped 'v', triggering:
     * java.lang.IllegalArgumentException: Color parameter outside of expected range: Red Green Blue
     */
    @Test(timeout = 4000)
    public void testGetPaintClampingAboveUpperBoundDefect() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint paint = scale.getPaint(1.5);
        assertNotNull("Paint should not be null when value > upperBound", paint);
        assertTrue("Paint must be instance of Color", paint instanceof Color);
        assertEquals("Value above upper bound must be clamped to upper bound color (White)",
                Color.WHITE, paint);
    }

    /**
     * Targets defect with extreme out-of-range negative and positive values on a shifted range.
     */
    @Test(timeout = 4000)
    public void testGetPaintClampingExtremes() {
        GrayPaintScale scale = new GrayPaintScale(10.0, 20.0);
        assertEquals(Color.BLACK, scale.getPaint(Double.NEGATIVE_INFINITY));
        assertEquals(Color.BLACK, scale.getPaint(-100.0));
        assertEquals(Color.BLACK, scale.getPaint(9.999999));
        assertEquals(Color.WHITE, scale.getPaint(20.000001));
        assertEquals(Color.WHITE, scale.getPaint(100.0));
        assertEquals(Color.WHITE, scale.getPaint(Double.POSITIVE_INFINITY));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorThrowsExceptionWhenLowerEqualsUpper() {
        new GrayPaintScale(5.0, 5.0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorThrowsExceptionWhenLowerGreaterThanUpper() {
        new GrayPaintScale(10.0, 5.0);
    }

    @Test(timeout = 4000)
    public void testConstructorThrowsExceptionDetailedMessage() {
        try {
            new GrayPaintScale(2.0, 1.0);
            fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException ex) {
            assertEquals("Requires lowerBound < upperBound.", ex.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsSameInstance() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        assertTrue(scale.equals(scale));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        assertFalse(scale.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        assertFalse(scale.equals("A String Object"));
    }

    @Test(timeout = 4000)
    public void testEqualsEqualInstances() {
        GrayPaintScale scale1 = new GrayPaintScale(2.5, 7.5);
        GrayPaintScale scale2 = new GrayPaintScale(2.5, 7.5);
        assertTrue(scale1.equals(scale2));
        assertTrue(scale2.equals(scale1));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentLowerBound() {
        GrayPaintScale scale1 = new GrayPaintScale(2.5, 7.5);
        GrayPaintScale scale2 = new GrayPaintScale(2.0, 7.5);
        assertFalse(scale1.equals(scale2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentUpperBound() {
        GrayPaintScale scale1 = new GrayPaintScale(2.5, 7.5);
        GrayPaintScale scale2 = new GrayPaintScale(2.5, 8.0);
        assertFalse(scale1.equals(scale2));
    }

    @Test(timeout = 4000)
    public void testCloneMethod() throws CloneNotSupportedException {
        GrayPaintScale original = new GrayPaintScale(10.0, 20.0);
        Object clonedObj = original.clone();

        assertNotNull(clonedObj);
        assertNotSame(original, clonedObj);
        assertEquals(original.getClass(), clonedObj.getClass());
        assertEquals(original, clonedObj);

        GrayPaintScale clone = (GrayPaintScale) clonedObj;
        assertEquals(original.getLowerBound(), clone.getLowerBound(), EPSILON);
        assertEquals(original.getUpperBound(), clone.getUpperBound(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        GrayPaintScale original = new GrayPaintScale(-50.0, 50.0);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.flush();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();

        assertNotNull(deserialized);
        assertNotSame(original, deserialized);
        assertEquals(original, deserialized);

        GrayPaintScale scale = (GrayPaintScale) deserialized;
        assertEquals(-50.0, scale.getLowerBound(), EPSILON);
        assertEquals(50.0, scale.getUpperBound(), EPSILON);
    }
}