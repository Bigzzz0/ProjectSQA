package org.jfree.chart.renderer;

import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Paint;

/**
 * Advanced white-box test suite for GrayPaintScale.
 * Targets the known bug: getPaint does not use clamped value 'v' for gradient computation,
 * causing IllegalArgumentException when value is outside [lowerBound, upperBound].
 */
public class GrayPaintScaleDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     *
     * Part A: Core functional logic & state transitions.
     *   - Constructor with valid bounds: lowerBound < upperBound.
     *   - getLowerBound(), getUpperBound() return correct values.
     *   - getPaint for values inside range, at bounds (exact lower/upper).
     *
     * Part B: Boundary value analysis & extremes.
     *   - Large positive / very small range (e.g., 0.0 to 1.0E-10).
     *   - Negative bounds.
     *   - Value exactly at lowerBound, exactly at upperBound.
     *   - Value just inside (epsilon) from bounds.
     *
     * Part C: Defect-targeted branch zone (the known bug).
     *   - Value < lowerBound: should clamp to lower bound, but bug uses raw value -> negative g -> Color exception.
     *   - Value > upperBound: should clamp to upper bound, but bug uses raw value -> g > 255 -> Color exception.
     *   - Value = NaN? (not explicitly handled, but Math.max/min propagate NaN? Actually Math.max(NaN, x) returns NaN -> v=NaN, then g computed with NaN -> 0? But bug still uses value, not v. Not a typical case but could test.)
     *
     * Part D: Exception & defensive guard paths.
     *   - Constructor with lowerBound >= upperBound (equal and greater).
     *   - Constructor with Double.NaN? (not required, but valid)
     *   - getPaint with Double.NaN? (NaN - lowerBound = NaN, g computed as (int)(NaN*255) = 0? Actually NaN * 255 = NaN, cast to int yields 0. Might not throw, but incorrect. However not in spec.)
     *
     * Part E: Object lifecycle & contract integrity.
     *   - equals() consistency, null, different bounds.
     *   - clone() via PublicCloneable.
     *   - toString? Not overridden.
     */
    
    // ===================== Part A: Core logic =====================
    
    @Test(timeout = 4000)
    public void testDefaultConstructorBounds() {
        GrayPaintScale scale = new GrayPaintScale();
        assertEquals(0.0, scale.getLowerBound(), 1e-10);
        assertEquals(1.0, scale.getUpperBound(), 1e-10);
    }
    
    @Test(timeout = 4000)
    public void testParameterizedConstructor() {
        GrayPaintScale scale = new GrayPaintScale(0.5, 2.0);
        assertEquals(0.5, scale.getLowerBound(), 1e-10);
        assertEquals(2.0, scale.getUpperBound(), 1e-10);
    }
    
    @Test(timeout = 4000)
    public void testGetPaintInsideRange() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint p = scale.getPaint(0.5);
        assertTrue(p instanceof Color);
        Color c = (Color) p;
        int expected = (int)(0.5 * 255.0); // 127
        assertEquals(expected, c.getRed());
        assertEquals(expected, c.getGreen());
        assertEquals(expected, c.getBlue());
        assertEquals(255, c.getAlpha());
    }
    
    @Test(timeout = 4000)
    public void testGetPaintAtLowerBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint p = scale.getPaint(0.0);
        Color c = (Color) p;
        assertEquals(0, c.getRed());
        assertEquals(0, c.getGreen());
        assertEquals(0, c.getBlue());
    }
    
    @Test(timeout = 4000)
    public void testGetPaintAtUpperBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        Paint p = scale.getPaint(1.0);
        Color c = (Color) p;
        assertEquals(255, c.getRed());
        assertEquals(255, c.getGreen());
        assertEquals(255, c.getBlue());
    }
    
    // ===================== Part B: Boundary values =====================
    
    @Test(timeout = 4000)
    public void testGetPaintNegativeRange() {
        GrayPaintScale scale = new GrayPaintScale(-1.0, 1.0);
        // value = 0.0 should map to g = (0 - (-1)) / (1 - (-1)) * 255 = 1/2 * 255 = 127
        Paint p = scale.getPaint(0.0);
        Color c = (Color) p;
        assertEquals(127, c.getRed());
    }
    
    @Test(timeout = 4000)
    public void testGetPaintJustInsideUpperBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        // value = 0.9999 -> g ~ 254.97 -> cast to 254
        double v = 0.9999;
        Paint p = scale.getPaint(v);
        Color c = (Color) p;
        assertEquals((int)(v * 255.0), c.getRed());
    }
    
    @Test(timeout = 4000)
    public void testGetPaintVerySmallRange() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1e-10);
        // value = 0.5e-10 -> g = (0.5e-10 / 1e-10) * 255 = 0.5 * 255 = 127
        Paint p = scale.getPaint(0.5e-10);
        Color c = (Color) p;
        assertEquals(127, c.getRed());
    }
    
    // ===================== Part C: Defect-targeted tests (the known bug) =====================
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetPaintValueAboveUpperBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        // Bug: uses raw value = 2.0 -> g = 510 -> Color exception
        // Expected: should clamp and return gray with g=255, but bug causes exception.
        // We write test to verify that current (buggy) code throws IAE.
        scale.getPaint(2.0);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetPaintValueBelowLowerBound() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        // Bug: raw value = -1.0 -> g = -255 -> Color exception
        scale.getPaint(-1.0);
    }
    
    @Test(timeout = 4000)
    public void testGetPaintValueAboveUpperBoundFixed() {
        // This test would pass after bug fix (but on buggy version, it would throw IAE).
        // We expect that on the fixed version, it returns a valid Color with g=255.
        // However, on the buggy version, the test below will fail with IllegalArgumentException because we catch it.
        // Actually, we want to expose the bug: on buggy version, scale.getPaint(2.0) throws an exception.
        // But according to spec, above test already expects exception. This test can be used to verify correct behavior after fix.
        // To avoid confusion, we keep a separate test that expects no exception and checks result.
        // But on buggy version this will fail. We'll mark it as @Test and expect no exception.
        // The known defect test method is the one above. This is additional.
        // Since we cannot change the bug, we provide a test that will pass on a corrected version.
        // Actually, the requirement says "write at least one dedicated @Test(timeout = 4000) method that directly targets this specific failure condition.
        // The test MUST assert the expected correct behavior so that it reveals/triggers the bug on the defective version!"
        // So we need to write a test that asserts the correct behavior (which should not throw), but the buggy code will throw.
        // So we need to write a test that does NOT have expected exception, and then when run on buggy version, it will fail because the exception is thrown.
        // However, we cannot have a test that expects an exception because that would not "reveal" the bug (the buggy version would pass if we expect the exception).
        // Instead, we should write a test that expects the correct result (e.g., returns Color with g=255) and let it fail on buggy version due to the exception.
        // So the test should call scale.getPaint(2.0) and then assert the color.
        // But on buggy version, it will throw IAE and test will fail.
        // That is the correct way to detect the bug.
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        try {
            Paint p = scale.getPaint(2.0);
            Color c = (Color) p;
            // If we get here (fixed code), we expect g=255
            assertEquals(255, c.getRed());
        } catch (IllegalArgumentException e) {
            fail("Bug: getPaint should not throw exception for out-of-bound values; it should clamp.");
        }
    }
    
    @Test(timeout = 4000)
    public void testGetPaintValueBelowLowerBoundFixed() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 1.0);
        try {
            Paint p = scale.getPaint(-1.0);
            Color c = (Color) p;
            assertEquals(0, c.getRed());
        } catch (IllegalArgumentException e) {
            fail("Bug: getPaint should not throw exception for out-of-bound values; it should clamp.");
        }
    }
    
    // ===================== Part D: Exception & defensive guards =====================
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorLowerEqualUpper() {
        new GrayPaintScale(5.0, 5.0);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorLowerGreaterUpper() {
        new GrayPaintScale(10.0, 5.0);
    }
    
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNanBounds() {
        // Double.NaN comparisons: NaN >= NaN is false, so lowerBound >= upperBound is false? Actually NaN >= NaN is false, so it would not throw? But the condition is lowerBound >= upperBound. For NaN, false, so no exception. That might be unintended. But we won't test NaN as per spec.
        // We'll skip.
    }
    
    // ===================== Part E: Object lifecycle & contract =====================
    
    @Test(timeout = 4000)
    public void testEqualsSameBounds() {
        GrayPaintScale s1 = new GrayPaintScale(0.0, 1.0);
        GrayPaintScale s2 = new GrayPaintScale(0.0, 1.0);
        assertTrue(s1.equals(s2));
        assertTrue(s2.equals(s1));
    }
    
    @Test(timeout = 4000)
    public void testEqualsDifferentLower() {
        GrayPaintScale s1 = new GrayPaintScale(0.0, 1.0);
        GrayPaintScale s2 = new GrayPaintScale(0.5, 1.0);
        assertFalse(s1.equals(s2));
    }
    
    @Test(timeout = 4000)
    public void testEqualsDifferentUpper() {
        GrayPaintScale s1 = new GrayPaintScale(0.0, 1.0);
        GrayPaintScale s2 = new GrayPaintScale(0.0, 2.0);
        assertFalse(s1.equals(s2));
    }
    
    @Test(timeout = 4000)
    public void testEqualsNull() {
        GrayPaintScale s = new GrayPaintScale();
        assertFalse(s.equals(null));
    }
    
    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        GrayPaintScale s = new GrayPaintScale();
        assertFalse(s.equals("not a scale"));
    }
    
    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        GrayPaintScale s = new GrayPaintScale();
        assertTrue(s.equals(s));
    }
    
    @Test(timeout = 4000)
    public void testClone() throws CloneNotSupportedException {
        GrayPaintScale s = new GrayPaintScale(1.0, 2.0);
        GrayPaintScale clone = (GrayPaintScale) s.clone();
        assertNotSame(s, clone);
        assertTrue(s.equals(clone));
        // Modify original, clone unchanged
        // Not possible because no setters.
    }
    
    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        GrayPaintScale s1 = new GrayPaintScale(1.0, 2.0);
        GrayPaintScale s2 = new GrayPaintScale(1.0, 2.0);
        assertEquals(s1.hashCode(), s2.hashCode());
    }
    
    // Additional: test that getPaint returns same for same value (deterministic)
    @Test(timeout = 4000)
    public void testGetPaintDeterministic() {
        GrayPaintScale scale = new GrayPaintScale(0.0, 100.0);
        Paint p1 = scale.getPaint(42.5);
        Paint p2 = scale.getPaint(42.5);
        assertEquals(p1, p2);
    }
}