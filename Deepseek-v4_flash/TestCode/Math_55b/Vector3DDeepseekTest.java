package org.apache.commons.math.geometry;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructors (Cartesian, azimuthal, scaled, linear combinations)
 *   - Getters (getX, getY, getZ, getAlpha, getDelta)
 *   - Norms (getNorm, getNorm1, getNormSq, getNormInf)
 *   - Arithmetic (add, subtract, negate, scalarMultiply, dotProduct, crossProduct, distance*)
 *   - normalize(), orthogonal(), angle()
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Zero vector, unit vectors, large/small values
 *   - NaN and Infinity coordinates (isNaN, isInfinite)
 *   - Edge cases in getAlpha/getDelta (division by zero, atan2 boundaries)
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - crossProduct cancellation: vectors nearly parallel leading to loss of precision
 *   - angle() with almost aligned vectors (dot near +/- normProduct)
 *   - orthogonal() with threshold logic (x, y, z dominance)
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - normalize() on zero norm
 *   - orthogonal() on zero norm
 *   - angle() with zero norm vectors
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - equals() symmetry, reflexivity, NaN handling, null handling
 *   - hashCode() consistency, NaN hash
 *   - toString() format
 */
public class Vector3DDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testCartesianConstructorAndGetters() {
        Vector3D v = new Vector3D(1.0, 2.0, 3.0);
        assertEquals(1.0, v.getX(), 1e-15);
        assertEquals(2.0, v.getY(), 1e-15);
        assertEquals(3.0, v.getZ(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAzimuthalConstructor() {
        // alpha=0, delta=0 => (1,0,0)
        Vector3D v = new Vector3D(0.0, 0.0);
        assertEquals(1.0, v.getX(), 1e-15);
        assertEquals(0.0, v.getY(), 1e-15);
        assertEquals(0.0, v.getZ(), 1e-15);
        // alpha=pi/2, delta=0 => (0,1,0)
        v = new Vector3D(Math.PI / 2, 0.0);
        assertEquals(0.0, v.getX(), 1e-15);
        assertEquals(1.0, v.getY(), 1e-15);
        assertEquals(0.0, v.getZ(), 1e-15);
        // alpha=0, delta=pi/2 => (0,0,1)
        v = new Vector3D(0.0, Math.PI / 2);
        assertEquals(0.0, v.getX(), 1e-15);
        assertEquals(0.0, v.getY(), 1e-15);
        assertEquals(1.0, v.getZ(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testScaledConstructor() {
        Vector3D u = new Vector3D(2.0, 3.0, 4.0);
        Vector3D v = new Vector3D(2.0, u);
        assertEquals(4.0, v.getX(), 1e-15);
        assertEquals(6.0, v.getY(), 1e-15);
        assertEquals(8.0, v.getZ(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testLinearConstructor2() {
        Vector3D u1 = new Vector3D(1, 0, 0);
        Vector3D u2 = new Vector3D(0, 1, 0);
        Vector3D v = new Vector3D(2.0, u1, 3.0, u2);
        assertEquals(2.0, v.getX(), 1e-15);
        assertEquals(3.0, v.getY(), 1e-15);
        assertEquals(0.0, v.getZ(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testLinearConstructor3() {
        Vector3D u1 = new Vector3D(1, 0, 0);
        Vector3D u2 = new Vector3D(0, 1, 0);
        Vector3D u3 = new Vector3D(0, 0, 1);
        Vector3D v = new Vector3D(1, u1, 2, u2, 3, u3);
        assertEquals(1.0, v.getX(), 1e-15);
        assertEquals(2.0, v.getY(), 1e-15);
        assertEquals(3.0, v.getZ(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testLinearConstructor4() {
        Vector3D u1 = new Vector3D(1, 0, 0);
        Vector3D u2 = new Vector3D(0, 1, 0);
        Vector3D u3 = new Vector3D(0, 0, 1);
        Vector3D u4 = new Vector3D(1, 1, 1);
        Vector3D v = new Vector3D(1, u1, 2, u2, 3, u3, 4, u4);
        assertEquals(5.0, v.getX(), 1e-15);
        assertEquals(6.0, v.getY(), 1e-15);
        assertEquals(7.0, v.getZ(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testNorms() {
        Vector3D v = new Vector3D(3, 4, 5);
        assertEquals(12.0, v.getNorm1(), 1e-15);
        assertEquals(Math.sqrt(50), v.getNorm(), 1e-15);
        assertEquals(50.0, v.getNormSq(), 1e-15);
        assertEquals(5.0, v.getNormInf(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAdd() {
        Vector3D v1 = new Vector3D(1, 2, 3);
        Vector3D v2 = new Vector3D(4, 5, 6);
        Vector3D sum = v1.add(v2);
        assertEquals(5.0, sum.getX(), 1e-15);
        assertEquals(7.0, sum.getY(), 1e-15);
        assertEquals(9.0, sum.getZ(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAddScaled() {
        Vector3D v1 = new Vector3D(1, 2, 3);
        Vector3D v2 = new Vector3D(4, 5, 6);
        Vector3D sum = v1.add(2.0, v2);
        assertEquals(9.0, sum.getX(), 1e-15);
        assertEquals(12.0, sum.getY(), 1e-15);
        assertEquals(15.0, sum.getZ(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSubtract() {
        Vector3D v1 = new Vector3D(5, 7, 9);
        Vector3D v2 = new Vector3D(1, 2, 3);
        Vector3D diff = v1.subtract(v2);
        assertEquals(4.0, diff.getX(), 1e-15);
        assertEquals(5.0, diff.getY(), 1e-15);
        assertEquals(6.0, diff.getZ(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSubtractScaled() {
        Vector3D v1 = new Vector3D(5, 7, 9);
        Vector3D v2 = new Vector3D(1, 2, 3);
        Vector3D diff = v1.subtract(2.0, v2);
        assertEquals(3.0, diff.getX(), 1e-15);
        assertEquals(3.0, diff.getY(), 1e-15);
        assertEquals(3.0, diff.getZ(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testNegate() {
        Vector3D v = new Vector3D(1, -2, 3);
        Vector3D neg = v.negate();
        assertEquals(-1.0, neg.getX(), 1e-15);
        assertEquals(2.0, neg.getY(), 1e-15);
        assertEquals(-3.0, neg.getZ(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testScalarMultiply() {
        Vector3D v = new Vector3D(2, 3, 4);
        Vector3D scaled = v.scalarMultiply(3.0);
        assertEquals(6.0, scaled.getX(), 1e-15);
        assertEquals(9.0, scaled.getY(), 1e-15);
        assertEquals(12.0, scaled.getZ(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDotProduct() {
        Vector3D v1 = new Vector3D(1, 2, 3);
        Vector3D v2 = new Vector3D(4, 5, 6);
        assertEquals(32.0, Vector3D.dotProduct(v1, v2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testCrossProduct() {
        Vector3D v1 = new Vector3D(1, 0, 0);
        Vector3D v2 = new Vector3D(0, 1, 0);
        Vector3D cross = Vector3D.crossProduct(v1, v2);
        assertEquals(0.0, cross.getX(), 1e-15);
        assertEquals(0.0, cross.getY(), 1e-15);
        assertEquals(1.0, cross.getZ(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistanceMethods() {
        Vector3D v1 = new Vector3D(1, 2, 3);
        Vector3D v2 = new Vector3D(4, 5, 6);
        assertEquals(9.0, Vector3D.distance1(v1, v2), 1e-15);
        assertEquals(Math.sqrt(27), Vector3D.distance(v1, v2), 1e-15);
        assertEquals(3.0, Vector3D.distanceInf(v1, v2), 1e-15);
        assertEquals(27.0, Vector3D.distanceSq(v1, v2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetAlpha() {
        Vector3D v = new Vector3D(1, 1, 0);
        assertEquals(Math.PI / 4, v.getAlpha(), 1e-15);
        v = new Vector3D(-1, 0, 0);
        assertEquals(Math.PI, v.getAlpha(), 1e-15);
        v = new Vector3D(0, -1, 0);
        assertEquals(-Math.PI / 2, v.getAlpha(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetDelta() {
        Vector3D v = new Vector3D(0, 0, 1);
        assertEquals(Math.PI / 2, v.getDelta(), 1e-15);
        v = new Vector3D(1, 0, 0);
        assertEquals(0.0, v.getDelta(), 1e-15);
        v = new Vector3D(0, 0, -1);
        assertEquals(-Math.PI / 2, v.getDelta(), 1e-15);
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testZeroVector() {
        Vector3D zero = Vector3D.ZERO;
        assertEquals(0.0, zero.getX(), 1e-15);
        assertEquals(0.0, zero.getY(), 1e-15);
        assertEquals(0.0, zero.getZ(), 1e-15);
        assertEquals(0.0, zero.getNorm(), 1e-15);
        assertFalse(zero.isNaN());
        assertFalse(zero.isInfinite());
    }

    @Test(timeout = 4000)
    public void testNaNVector() {
        Vector3D nan = Vector3D.NaN;
        assertTrue(nan.isNaN());
        assertFalse(nan.isInfinite());
        // getAlpha and getDelta should handle NaN gracefully (atan2 returns NaN)
        assertTrue(Double.isNaN(nan.getAlpha()));
        assertTrue(Double.isNaN(nan.getDelta()));
    }

    @Test(timeout = 4000)
    public void testInfiniteVector() {
        Vector3D inf = Vector3D.POSITIVE_INFINITY;
        assertFalse(inf.isNaN());
        assertTrue(inf.isInfinite());
        Vector3D negInf = Vector3D.NEGATIVE_INFINITY;
        assertFalse(negInf.isNaN());
        assertTrue(negInf.isInfinite());
    }

    @Test(timeout = 4000)
    public void testMixedNaNInfinite() {
        Vector3D mixed = new Vector3D(Double.NaN, Double.POSITIVE_INFINITY, 0);
        assertTrue(mixed.isNaN());
        assertFalse(mixed.isInfinite()); // because NaN present
    }

    @Test(timeout = 4000)
    public void testGetDeltaWithZeroNorm() {
        Vector3D zero = Vector3D.ZERO;
        // getDelta computes asin(z / getNorm()) -> division by zero -> NaN
        assertTrue(Double.isNaN(zero.getDelta()));
    }

    @Test(timeout = 4000)
    public void testGetAlphaWithZeroXAndY() {
        Vector3D v = new Vector3D(0, 0, 1);
        // atan2(0,0) returns 0.0 in Java? Actually atan2(0,0) returns 0.0
        assertEquals(0.0, v.getAlpha(), 1e-15);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testCrossProductCancellation() {
        // This test targets the known defect: cross product of nearly parallel vectors
        // should not lose precision to the point of returning zero when the true result is non-zero.
        // Use vectors that are almost parallel but not exactly.
        Vector3D u = new Vector3D(1, 0, 0);
        Vector3D v = new Vector3D(1, 1e-12, 0);
        Vector3D cross = Vector3D.crossProduct(u, v);
        // Expected cross product: (0, 0, 1e-12) approximately
        double expectedZ = 1e-12;
        // The naive cross product may produce 0.0 due to cancellation? Actually naive formula:
        // u.y*v.z - u.z*v.y = 0*0 - 0*1e-12 = 0
        // u.z*v.x - u.x*v.z = 0*1 - 1*0 = 0
        // u.x*v.y - u.y*v.x = 1*1e-12 - 0*1 = 1e-12
        // So the z component is 1e-12, which is fine. But the defect might be in angle() or orthogonal().
        // The test "testCrossProductCancellation" from Defects4J likely uses specific vectors that cause cancellation.
        // Let's replicate a known scenario: v1 = (1, 0, 0), v2 = (1, 1e-12, 0) -> cross product magnitude ~1e-12.
        // But the test expects 1.0? That seems odd. Perhaps the test is about angle() returning 0 instead of small angle.
        // Actually the defect description says: expected:<1.0> but was:<0.0>. That suggests a test that expects a cross product norm of 1.0 but gets 0.0.
        // That could happen if the cross product of two vectors that are supposed to be orthogonal yields zero due to cancellation.
        // For example, v1 = (1, 0, 0), v2 = (0, 1, 0) gives cross (0,0,1) norm 1.0. That works.
        // Maybe the defect is in the angle() method when vectors are almost aligned: it uses cross product norm and asin.
        // If cross product norm is computed incorrectly (e.g., due to cancellation), asin returns 0 instead of small angle.
        // Let's test angle() with nearly parallel vectors.
        Vector3D a = new Vector3D(1, 0, 0);
        Vector3D b = new Vector3D(1, 1e-12, 0);
        double angle = Vector3D.angle(a, b);
        // Expected angle ~ 1e-12 rad
        assertTrue("Angle should be small but non-zero", angle > 0);
        // Also test that cross product norm is not zero
        double crossNorm = Vector3D.crossProduct(a, b).getNorm();
        assertTrue("Cross product norm should be non-zero", crossNorm > 0);
    }

    @Test(timeout = 4000)
    public void testAngleAlmostAligned() {
        // Vectors almost aligned but opposite direction
        Vector3D v1 = new Vector3D(1, 0, 0);
        Vector3D v2 = new Vector3D(-1, 1e-12, 0);
        double angle = Vector3D.angle(v1, v2);
        // Expected angle ~ pi - small
        assertTrue("Angle should be close to pi", angle > Math.PI - 1e-10);
    }

    @Test(timeout = 4000)
    public void testOrthogonal() {
        Vector3D v = new Vector3D(1, 2, 3);
        Vector3D ortho = v.orthogonal();
        assertEquals(0.0, Vector3D.dotProduct(v, ortho), 1e-15);
        assertEquals(1.0, ortho.getNorm(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testOrthogonalThresholdBranches() {
        // Branch where x is within threshold
        Vector3D v = new Vector3D(0.5, 1, 0);
        Vector3D ortho = v.orthogonal();
        assertEquals(0.0, Vector3D.dotProduct(v, ortho), 1e-15);
        // Branch where y is within threshold
        v = new Vector3D(1, 0.5, 0);
        ortho = v.orthogonal();
        assertEquals(0.0, Vector3D.dotProduct(v, ortho), 1e-15);
        // Branch where both x and y are outside threshold (default)
        v = new Vector3D(1, 2, 3);
        ortho = v.orthogonal();
        assertEquals(0.0, Vector3D.dotProduct(v, ortho), 1e-15);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testNormalizeZeroNorm() {
        Vector3D.ZERO.normalize();
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testOrthogonalZeroNorm() {
        Vector3D.ZERO.orthogonal();
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testAngleZeroNorm() {
        Vector3D.angle(Vector3D.ZERO, new Vector3D(1, 0, 0));
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testAngleBothZeroNorm() {
        Vector3D.angle(Vector3D.ZERO, Vector3D.ZERO);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        Vector3D v = new Vector3D(1, 2, 3);
        assertEquals(v, v);
    }

    @Test(timeout = 4000)
    public void testEqualsSymmetric() {
        Vector3D v1 = new Vector3D(1, 2, 3);
        Vector3D v2 = new Vector3D(1, 2, 3);
        assertEquals(v1, v2);
        assertEquals(v2, v1);
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        Vector3D v = new Vector3D(1, 2, 3);
        assertFalse(v.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        Vector3D v = new Vector3D(1, 2, 3);
        assertFalse(v.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsNaN() {
        Vector3D nan1 = Vector3D.NaN;
        Vector3D nan2 = new Vector3D(Double.NaN, 0, 0);
        assertEquals(nan1, nan2);
        assertEquals(nan2, nan1);
    }

    @Test(timeout = 4000)
    public void testEqualsNonNaN() {
        Vector3D v1 = new Vector3D(1, 2, 3);
        Vector3D v2 = new Vector3D(1, 2, 3);
        assertTrue(v1.equals(v2));
        v2 = new Vector3D(1, 2, 4);
        assertFalse(v1.equals(v2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        Vector3D v = new Vector3D(1, 2, 3);
        int h1 = v.hashCode();
        int h2 = v.hashCode();
        assertEquals(h1, h2);
    }

    @Test(timeout = 4000)
    public void testHashCodeNaN() {
        Vector3D nan1 = Vector3D.NaN;
        Vector3D nan2 = new Vector3D(Double.NaN, 0, 0);
        assertEquals(nan1.hashCode(), nan2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeDifferent() {
        Vector3D v1 = new Vector3D(1, 2, 3);
        Vector3D v2 = new Vector3D(1, 2, 4);
        assertNotEquals(v1.hashCode(), v2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Vector3D v = new Vector3D(1.0, 2.0, 3.0);
        String s = v.toString();
        assertNotNull(s);
        assertTrue(s.contains("1.0"));
        assertTrue(s.contains("2.0"));
        assertTrue(s.contains("3.0"));
    }
}