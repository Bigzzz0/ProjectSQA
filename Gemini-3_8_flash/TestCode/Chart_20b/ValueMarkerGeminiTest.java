package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.chart.event.MarkerChangeListener;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.jfree.chart.plot.ValueMarker
 *
 * ---------------------------------------------------------------------------------------------------------
 * Target ID     | Branch / Condition Description                 | Targeted Test Method(s)
 * ---------------------------------------------------------------------------------------------------------
 * DEFECT-1808376| 6-arg constructor passing paint/stroke twice   | test1808376_OutlinePaintAndStrokeRetained()
 *               | instead of outlinePaint and outlineStroke      |
 * ---------------------------------------------------------------------------------------------------------
 * CONSTRUCTORS  | 1-arg: ValueMarker(double)                     | testSingleArgConstructor(),
 *               |                                                | testSingleArgConstructorBoundaries()
 *               | 3-arg: ValueMarker(double, Paint, Stroke)      | testThreeArgConstructor()
 *               | 6-arg: ValueMarker(..., Paint, Stroke, float)  | testSixArgConstructorStandard()
 * ---------------------------------------------------------------------------------------------------------
 * GET / SET     | getValue() accessor check                      | testGetValue()
 *               | setValue(double) state update & listener firing| testSetValueNotifiesListener(),
 *               |                                                | testSetValueStateTransition()
 * ---------------------------------------------------------------------------------------------------------
 * EQUALS        | obj == this                                    | testEqualsSameInstance()
 *               | !super.equals(obj) [null, diff super props]    | testEqualsNull(), testEqualsDiffSuperProperties()
 *               | !(obj instanceof ValueMarker)                  | testEqualsDifferentClass()
 *               | this.value != that.value                       | testEqualsDifferentValue()
 *               | identical values & properties (true)           | testEqualsIdenticalObjects()
 * ---------------------------------------------------------------------------------------------------------
 * BOUNDARIES    | Extreme double values (0.0, -0.0, NaN, +/-Inf) | testBoundaryDoubleValues(), testEqualsWithNaN()
 *               | Alpha edge values (0.0f, 1.0f, out of bounds)  | testAlphaBoundaries(), testInvalidAlphaException()
 *               | Null Paint / Stroke in 3-arg & 6-arg ctors     | testNullPaintThrowsException(),
 *               |                                                | testNullStrokeThrowsException()
 * ---------------------------------------------------------------------------------------------------------
 * LIFECYCLE     | Cloneable contract: clone() state & separation | testCloneIndependence()
 *               | Serializable contract: round-trip fidelity     | testSerializationRoundTrip()
 * ---------------------------------------------------------------------------------------------------------
 */
public class ValueMarkerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingleArgConstructor() {
        ValueMarker marker = new ValueMarker(42.5);
        assertEquals(42.5, marker.getValue(), 0.0);
        // Default super properties defined in Marker()
        assertNotNull(marker.getPaint());
        assertNotNull(marker.getStroke());
        assertEquals(1.0f, marker.getAlpha(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testThreeArgConstructor() {
        Paint paint = Color.GREEN;
        Stroke stroke = new BasicStroke(1.5f);
        ValueMarker marker = new ValueMarker(100.0, paint, stroke);

        assertEquals(100.0, marker.getValue(), 0.0);
        assertEquals(paint, marker.getPaint());
        assertEquals(stroke, marker.getStroke());
        assertEquals(paint, marker.getOutlinePaint());
        assertEquals(stroke, marker.getOutlineStroke());
        assertEquals(1.0f, marker.getAlpha(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testSixArgConstructorStandard() {
        Paint paint = Color.BLACK;
        Stroke stroke = new BasicStroke(0.5f);
        Paint outlinePaint = Color.WHITE;
        Stroke outlineStroke = new BasicStroke(2.5f);
        float alpha = 0.75f;

        ValueMarker marker = new ValueMarker(12.34, paint, stroke, outlinePaint, outlineStroke, alpha);

        assertEquals(12.34, marker.getValue(), 0.0);
        assertEquals(paint, marker.getPaint());
        assertEquals(stroke, marker.getStroke());
        assertEquals(alpha, marker.getAlpha(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testGetValue() {
        ValueMarker marker = new ValueMarker(-99.99);
        assertEquals(-99.99, marker.getValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSetValueStateTransition() {
        ValueMarker marker = new ValueMarker(10.0);
        assertEquals(10.0, marker.getValue(), 0.0);

        marker.setValue(20.0);
        assertEquals(20.0, marker.getValue(), 0.0);

        marker.setValue(-5.5);
        assertEquals(-5.5, marker.getValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSetValueNotifiesListener() {
        ValueMarker marker = new ValueMarker(1.0);

        class CountingMarkerListener implements MarkerChangeListener {
            private int eventCount = 0;
            private MarkerChangeEvent lastEvent = null;

            @Override
            public void markerChanged(MarkerChangeEvent event) {
                this.eventCount++;
                this.lastEvent = event;
            }
        }

        CountingMarkerListener listener = new CountingMarkerListener();
        marker.addChangeListener(listener);

        assertEquals(0, listener.eventCount);
        assertNull(listener.lastEvent);

        marker.setValue(5.0);

        assertEquals(1, listener.eventCount);
        assertNotNull(listener.lastEvent);
        assertSame(marker, listener.lastEvent.getMarker());

        marker.setValue(5.0); // setting same value still fires event
        assertEquals(2, listener.eventCount);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingleArgConstructorBoundaries() {
        ValueMarker vmZero = new ValueMarker(0.0);
        assertEquals(0.0, vmZero.getValue(), 0.0);

        ValueMarker vmNegZero = new ValueMarker(-0.0);
        assertEquals(-0.0, vmNegZero.getValue(), 0.0);

        ValueMarker vmMax = new ValueMarker(Double.MAX_VALUE);
        assertEquals(Double.MAX_VALUE, vmMax.getValue(), 0.0);

        ValueMarker vmMin = new ValueMarker(Double.MIN_VALUE);
        assertEquals(Double.MIN_VALUE, vmMin.getValue(), 0.0);

        ValueMarker vmPosInf = new ValueMarker(Double.POSITIVE_INFINITY);
        assertTrue(Double.isInfinite(vmPosInf.getValue()));
        assertTrue(vmPosInf.getValue() > 0);

        ValueMarker vmNegInf = new ValueMarker(Double.NEGATIVE_INFINITY);
        assertTrue(Double.isInfinite(vmNegInf.getValue()));
        assertTrue(vmNegInf.getValue() < 0);

        ValueMarker vmNaN = new ValueMarker(Double.NaN);
        assertTrue(Double.isNaN(vmNaN.getValue()));
    }

    @Test(timeout = 4000)
    public void testAlphaBoundaries() {
        Paint p = Color.RED;
        Stroke s = new BasicStroke(1.0f);

        ValueMarker minAlpha = new ValueMarker(1.0, p, s, p, s, 0.0f);
        assertEquals(0.0f, minAlpha.getAlpha(), 0.0f);

        ValueMarker maxAlpha = new ValueMarker(1.0, p, s, p, s, 1.0f);
        assertEquals(1.0f, maxAlpha.getAlpha(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testOutlinePaintAndStrokeNullable() {
        // According to Javadoc: outlinePaint and outlineStroke null is permitted
        Paint p = Color.YELLOW;
        Stroke s = new BasicStroke(1.0f);
        ValueMarker marker = new ValueMarker(5.0, p, s, null, null, 0.5f);

        assertEquals(5.0, marker.getValue(), 0.0);
        assertEquals(p, marker.getPaint());
        assertEquals(s, marker.getStroke());
        assertNull(marker.getOutlinePaint());
        assertNull(marker.getOutlineStroke());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Bug 1808376)
    // =========================================================================

    /**
     * Targets Defects4J bug 1808376:
     * In ValueMarker's 6-argument constructor, super(paint, stroke, paint, stroke, alpha)
     * was erroneously called instead of super(paint, stroke, outlinePaint, outlineStroke, alpha).
     * This test explicitly asserts that outlinePaint and outlineStroke match the constructor arguments.
     */
    @Test(timeout = 4000)
    public void test1808376_OutlinePaintAndStrokeRetained() {
        Paint paint = Color.RED;
        Stroke stroke = new BasicStroke(1.0f);
        Paint outlinePaint = Color.BLUE;
        Stroke outlineStroke = new BasicStroke(2.0f);

        ValueMarker vm = new ValueMarker(1.0, paint, stroke, outlinePaint, outlineStroke, 0.5f);

        // Expected: Color.BLUE; Defective implementation returns: Color.RED
        assertEquals("Bug 1808376: outlinePaint should match the argument passed to constructor",
                outlinePaint, vm.getOutlinePaint());
        // Expected: outlineStroke (width 2.0f); Defective implementation returns: stroke (width 1.0f)
        assertEquals("Bug 1808376: outlineStroke should match the argument passed to constructor",
                outlineStroke, vm.getOutlineStroke());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullPaintThrowsException3Arg() {
        new ValueMarker(1.0, null, new BasicStroke(1.0f));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullStrokeThrowsException3Arg() {
        new ValueMarker(1.0, Color.BLACK, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullPaintThrowsException6Arg() {
        new ValueMarker(1.0, null, new BasicStroke(1.0f), Color.BLACK, new BasicStroke(1.0f), 0.5f);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullStrokeThrowsException6Arg() {
        new ValueMarker(1.0, Color.BLACK, null, Color.BLACK, new BasicStroke(1.0f), 0.5f);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidAlphaNegativeThrowsException() {
        new ValueMarker(1.0, Color.BLACK, new BasicStroke(1.0f), Color.BLACK, new BasicStroke(1.0f), -0.01f);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInvalidAlphaGreaterThanOneThrowsException() {
        new ValueMarker(1.0, Color.BLACK, new BasicStroke(1.0f), Color.BLACK, new BasicStroke(1.0f), 1.01f);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsSameInstance() {
        ValueMarker vm = new ValueMarker(15.0);
        assertTrue(vm.equals(vm));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        ValueMarker vm = new ValueMarker(15.0);
        assertFalse(vm.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        ValueMarker vm = new ValueMarker(15.0);
        assertFalse(vm.equals("Not a ValueMarker"));
        assertFalse(vm.equals(Double.valueOf(15.0)));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentSuperClassMarker() {
        ValueMarker vm = new ValueMarker(15.0);
        // An anonymous Marker instance is not an instance of ValueMarker
        Marker otherMarker = new Marker(vm.getPaint(), vm.getStroke()) {};
        assertFalse(vm.equals(otherMarker));
    }

    @Test(timeout = 4000)
    public void testEqualsDiffSuperProperties() {
        ValueMarker vm1 = new ValueMarker(15.0, Color.RED, new BasicStroke(1.0f));
        ValueMarker vm2 = new ValueMarker(15.0, Color.BLUE, new BasicStroke(1.0f));
        assertFalse(vm1.equals(vm2));

        ValueMarker vm3 = new ValueMarker(15.0, Color.RED, new BasicStroke(1.0f));
        ValueMarker vm4 = new ValueMarker(15.0, Color.RED, new BasicStroke(2.0f));
        assertFalse(vm3.equals(vm4));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentValue() {
        ValueMarker vm1 = new ValueMarker(15.0);
        ValueMarker vm2 = new ValueMarker(15.1);
        assertFalse(vm1.equals(vm2));
    }

    @Test(timeout = 4000)
    public void testEqualsIdenticalObjects() {
        ValueMarker vm1 = new ValueMarker(100.5, Color.CYAN, new BasicStroke(1.2f));
        ValueMarker vm2 = new ValueMarker(100.5, Color.CYAN, new BasicStroke(1.2f));
        assertTrue(vm1.equals(vm2));
        assertTrue(vm2.equals(vm1));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNaN() {
        ValueMarker vm1 = new ValueMarker(Double.NaN);
        ValueMarker vm2 = new ValueMarker(Double.NaN);
        // Per Java's primitive double comparison: NaN != NaN is true, so equals evaluates to false
        assertFalse(vm1.equals(vm2));
    }

    @Test(timeout = 4000)
    public void testCloneIndependence() throws CloneNotSupportedException {
        ValueMarker vm1 = new ValueMarker(55.5, Color.DARK_GRAY, new BasicStroke(1.0f));
        ValueMarker vm2 = (ValueMarker) vm1.clone();

        assertNotSame(vm1, vm2);
        assertSame(vm1.getClass(), vm2.getClass());
        assertEquals(vm1, vm2);
        assertEquals(vm1.getValue(), vm2.getValue(), 0.0);

        // Modifying clone does not mutate original
        vm2.setValue(77.7);
        assertEquals(55.5, vm1.getValue(), 0.0);
        assertEquals(77.7, vm2.getValue(), 0.0);
        assertFalse(vm1.equals(vm2));
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        ValueMarker original = new ValueMarker(123.456, Color.MAGENTA, new BasicStroke(3.0f));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(buffer);
        out.writeObject(original);
        out.close();

        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        ValueMarker deserialized = (ValueMarker) in.readObject();
        in.close();

        assertNotSame(original, deserialized);
        assertEquals(original, deserialized);
        assertEquals(original.getValue(), deserialized.getValue(), 0.0);
    }
}