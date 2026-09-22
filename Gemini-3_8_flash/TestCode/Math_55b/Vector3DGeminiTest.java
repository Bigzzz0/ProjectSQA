/*
 * [Branch & Defect Analysis Matrix]
 *
 * TARGET CLASS: org.apache.commons.math.geometry.Vector3D
 *
 * 1. DEFECT-TARGETED BRANCH ZONE:
 *    - crossProduct(Vector3D, Vector3D):
 *      Known defect in Commons Math (Defects4J): Floating point cancellation in crossProduct
 *      where vectors with large coordinates nearly parallel along an axis fail to preserve precision,
 *      resulting in 0.0 instead of the accurate cross product component (e.g. 1.0).
 *      Targeted by: testCrossProductCancellation()
 *
 * 2. DECISION / BRANCH COVERAGE MATRIX:
 *    - normalize():
 *        * norm == 0: MathArithmeticException
 *        * norm != 0: valid unit vector
 *    - orthogonal():
 *        * threshold == 0: MathArithmeticException
 *        * x in [-threshold, threshold]: Branch 1 (0, inverse*z, -inverse*y)
 *        * y in [-threshold, threshold]: Branch 2 (-inverse*z, 0, inverse*x)
 *        * fallback: Branch 3 (inverse*y, -inverse*x, 0)
 *    - angle(Vector3D, Vector3D):
 *        * normProduct == 0: MathArithmeticException
 *        * dot > threshold (almost aligned, dot >= 0): asin branch
 *        * dot < -threshold (almost opposite, dot < 0): PI - asin branch
 *        * separated vectors (-threshold <= dot <= threshold): acos branch
 *    - isNaN():
 *        * x is NaN, y is NaN, z is NaN, none is NaN
 *    - isInfinite():
 *        * isNaN() == true: returns false immediately
 *        * !isNaN() and x is Infinite: true
 *        * !isNaN() and y is Infinite: true
 *        * !isNaN() and z is Infinite: true
 *        * none infinite: false
 *    - equals(Object):
 *        * this == other: true
 *        * other == null: false
 *        * !(other instanceof Vector3D): false
 *        * rhs.isNaN() && this.isNaN(): true
 *        * rhs.isNaN() && !this.isNaN(): false
 *        * !rhs.isNaN() && this.isNaN(): false
 *        * x != rhs.x: false
 *        * y != rhs.y: false
 *        * z != rhs.z: false
 *        * x == rhs.x && y == rhs.y && z == rhs.z: true
 *    - hashCode():
 *        * isNaN() == true: returns 8
 *        * normal vector: deterministic formula
 *    - Linear constructors:
 *        * 1-argument scale, 2-argument combination, 3-argument combination, 4-argument combination
 *    - Spherical constructor:
 *        * Vector3D(alpha, delta) verifying trigonometry
 */

package org.apache.commons.math.geometry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.commons.math.exception.MathArithmeticException;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.*;

public class Vector3DGeminiTest {

    private static final double EPSILON = 1e-12;

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the known defect in Vector3D.crossProduct() where catastrophic cancellation
     * causes precision loss (expected: 1.0, was: 0.0) when computing cross products of vectors
     * with large components.
     */
    @Test(timeout = 4000)
    public void testCrossProductCancellation() {
        Vector3D v1 = new Vector3D(9070467121.0, 9070467120.0, 1.0);
        Vector3D v2 = new Vector3D(9070467122.0, 9070467121.0, 1.0);

        Vector3D v3 = Vector3D.crossProduct(v1, v2);

        // Expected mathematical values:
        // v3.x = 9070467120 * 1 - 1 * 9070467121 = -1.0
        // v3.y = 1 * 9070467122 - 9070467121 * 1 = 1.0
        // v3.z = 9070467121 * 9070467121 - 9070467120 * 9070467122 = 1.0
        assertEquals(-1.0, v3.getX(), 1.0e-10);
        assertEquals(1.0, v3.getY(), 1.0e-10);
        assertEquals(1.0, v3.getZ(), 1.0e-10);
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & CONSTRUCTORS
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstants() {
        assertEquals(0.0, Vector3D.ZERO.getX(), EPSILON);
        assertEquals(0.0, Vector3D.ZERO.getY(), EPSILON);
        assertEquals(0.0, Vector3D.ZERO.getZ(), EPSILON);

        assertEquals(1.0, Vector3D.PLUS_I.getX(), EPSILON);
        assertEquals(0.0, Vector3D.PLUS_I.getY(), EPSILON);
        assertEquals(0.0, Vector3D.PLUS_I.getZ(), EPSILON);

        assertEquals(-1.0, Vector3D.MINUS_I.getX(), EPSILON);
        assertEquals(0.0, Vector3D.MINUS_I.getY(), EPSILON);
        assertEquals(0.0, Vector3D.MINUS_I.getZ(), EPSILON);

        assertEquals(0.0, Vector3D.PLUS_J.getX(), EPSILON);
        assertEquals(1.0, Vector3D.PLUS_J.getY(), EPSILON);
        assertEquals(0.0, Vector3D.PLUS_J.getZ(), EPSILON);

        assertEquals(0.0, Vector3D.MINUS_J.getX(), EPSILON);
        assertEquals(-1.0, Vector3D.MINUS_J.getY(), EPSILON);
        assertEquals(0.0, Vector3D.MINUS_J.getZ(), EPSILON);

        assertEquals(0.0, Vector3D.PLUS_K.getX(), EPSILON);
        assertEquals(0.0, Vector3D.PLUS_K.getY(), EPSILON);
        assertEquals(1.0, Vector3D.PLUS_K.getZ(), EPSILON);

        assertEquals(0.0, Vector3D.MINUS_K.getX(), EPSILON);
        assertEquals(0.0, Vector3D.MINUS_K.getY(), EPSILON);
        assertEquals(-1.0, Vector3D.MINUS_K.getZ(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testCoordinatesConstructorAndGetters() {
        Vector3D v = new Vector3D(1.5, -2.5, 3.75);
        assertEquals(1.5, v.getX(), EPSILON);
        assertEquals(-2.5, v.getY(), EPSILON);
        assertEquals(3.75, v.getZ(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testSphericalConstructor() {
        double alpha = FastMath.PI / 4.0;
        double delta = FastMath.PI / 6.0;
        Vector3D v = new Vector3D(alpha, delta);

        double cosDelta = FastMath.cos(delta);
        assertEquals(FastMath.cos(alpha) * cosDelta, v.getX(), EPSILON);
        assertEquals(FastMath.sin(alpha) * cosDelta, v.getY(), EPSILON);
        assertEquals(FastMath.sin(delta), v.getZ(), EPSILON);

        assertEquals(alpha, v.getAlpha(), EPSILON);
        assertEquals(delta, v.getDelta(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testLinearConstructors() {
        Vector3D u1 = new Vector3D(1, 2, 3);
        Vector3D u2 = new Vector3D(-2, 1, 4);
        Vector3D u3 = new Vector3D(0, -1, 2);
        Vector3D u4 = new Vector3D(3, 3, -1);

        // 1 vector scaled
        Vector3D v1 = new Vector3D(2.5, u1);
        assertEquals(2.5, v1.getX(), EPSILON);
        assertEquals(5.0, v1.getY(), EPSILON);
        assertEquals(7.5, v1.getZ(), EPSILON);

        // 2 linear combination
        Vector3D v2 = new Vector3D(2.0, u1, -3.0, u2);
        assertEquals(2.0 * 1 - 3.0 * (-2), v2.getX(), EPSILON);
        assertEquals(2.0 * 2 - 3.0 * 1, v2.getY(), EPSILON);
        assertEquals(2.0 * 3 - 3.0 * 4, v2.getZ(), EPSILON);

        // 3 linear combination
        Vector3D v3 = new Vector3D(2.0, u1, -3.0, u2, 4.0, u3);
        assertEquals(2.0 * 1 - 3.0 * (-2) + 4.0 * 0, v3.getX(), EPSILON);
        assertEquals(2.0 * 2 - 3.0 * 1 + 4.0 * (-1), v3.getY(), EPSILON);
        assertEquals(2.0 * 3 - 3.0 * 4 + 4.0 * 2, v3.getZ(), EPSILON);

        // 4 linear combination
        Vector3D v4 = new Vector3D(2.0, u1, -3.0, u2, 4.0, u3, -1.0, u4);
        assertEquals(2.0 * 1 - 3.0 * (-2) + 4.0 * 0 - 1.0 * 3, v4.getX(), EPSILON);
        assertEquals(2.0 * 2 - 3.0 * 1 + 4.0 * (-1) - 1.0 * 3, v4.getY(), EPSILON);
        assertEquals(2.0 * 3 - 3.0 * 4 + 4.0 * 2 - 1.0 * (-1), v4.getZ(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testNorms() {
        Vector3D v = new Vector3D(2.0, -3.0, 6.0);
        assertEquals(11.0, v.getNorm1(), EPSILON);
        assertEquals(7.0, v.getNorm(), EPSILON);
        assertEquals(49.0, v.getNormSq(), EPSILON);
        assertEquals(6.0, v.getNormInf(), EPSILON);

        Vector3D vInf = new Vector3D(-8.0, 3.0, 5.0);
        assertEquals(8.0, vInf.getNormInf(), EPSILON);
        Vector3D vInfY = new Vector3D(2.0, -9.0, 1.0);
        assertEquals(9.0, vInfY.getNormInf(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAddAndSubtract() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(4.0, -5.0, 6.0);

        Vector3D sum = v1.add(v2);
        assertEquals(5.0, sum.getX(), EPSILON);
        assertEquals(-3.0, sum.getY(), EPSILON);
        assertEquals(9.0, sum.getZ(), EPSILON);

        Vector3D scaledSum = v1.add(2.0, v2);
        assertEquals(9.0, scaledSum.getX(), EPSILON);
        assertEquals(-8.0, scaledSum.getY(), EPSILON);
        assertEquals(15.0, scaledSum.getZ(), EPSILON);

        Vector3D diff = v1.subtract(v2);
        assertEquals(-3.0, diff.getX(), EPSILON);
        assertEquals(7.0, diff.getY(), EPSILON);
        assertEquals(-3.0, diff.getZ(), EPSILON);

        Vector3D scaledDiff = v1.subtract(2.0, v2);
        assertEquals(-7.0, scaledDiff.getX(), EPSILON);
        assertEquals(12.0, scaledDiff.getY(), EPSILON);
        assertEquals(-9.0, scaledDiff.getZ(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testScalarMultiplyAndNegate() {
        Vector3D v = new Vector3D(2.0, -3.0, 4.0);
        Vector3D scaled = v.scalarMultiply(2.5);
        assertEquals(5.0, scaled.getX(), EPSILON);
        assertEquals(-7.5, scaled.getY(), EPSILON);
        assertEquals(10.0, scaled.getZ(), EPSILON);

        Vector3D negated = v.negate();
        assertEquals(-2.0, negated.getX(), EPSILON);
        assertEquals(3.0, negated.getY(), EPSILON);
        assertEquals(-4.0, negated.getZ(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testDistances() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(4.0, 6.0, 3.0);

        assertEquals(7.0, Vector3D.distance1(v1, v2), EPSILON);
        assertEquals(5.0, Vector3D.distance(v1, v2), EPSILON);
        assertEquals(25.0, Vector3D.distanceSq(v1, v2), EPSILON);
        assertEquals(4.0, Vector3D.distanceInf(v1, v2), EPSILON);

        Vector3D v3 = new Vector3D(1.0, 2.0, 10.0);
        assertEquals(7.0, Vector3D.distanceInf(v1, v3), EPSILON);
    }

    @Test(timeout = 4000)
    public void testDotProduct() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(4.0, -5.0, 6.0);
        assertEquals(12.0, Vector3D.dotProduct(v1, v2), EPSILON);
    }

    @Test(timeout = 4000)
    public void testNormalizeSuccess() {
        Vector3D v = new Vector3D(3.0, 0.0, 4.0);
        Vector3D unit = v.normalize();
        assertEquals(1.0, unit.getNorm(), EPSILON);
        assertEquals(0.6, unit.getX(), EPSILON);
        assertEquals(0.0, unit.getY(), EPSILON);
        assertEquals(0.8, unit.getZ(), EPSILON);
    }

    // =========================================================================
    // PARTITION B: ORTHOGONAL & ANGLE BRANCH COVERAGE
    // =========================================================================

    @Test(timeout = 4000)
    public void testOrthogonalBranch1() {
        // v = (1, 2, 3), norm = sqrt(14) ~ 3.74, threshold = 0.6 * 3.74 ~ 2.24
        // x = 1.0 is within [-threshold, threshold]
        Vector3D v = new Vector3D(1.0, 2.0, 3.0);
        Vector3D o = v.orthogonal();
        assertEquals(1.0, o.getNorm(), EPSILON);
        assertEquals(0.0, Vector3D.dotProduct(v, o), EPSILON);
        assertEquals(0.0, o.getX(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testOrthogonalBranch2() {
        // v = (3, 1, 2), norm = sqrt(14) ~ 3.74, threshold ~ 2.24
        // x = 3.0 is outside [-threshold, threshold], y = 1.0 is within
        Vector3D v = new Vector3D(3.0, 1.0, 2.0);
        Vector3D o = v.orthogonal();
        assertEquals(1.0, o.getNorm(), EPSILON);
        assertEquals(0.0, Vector3D.dotProduct(v, o), EPSILON);
        assertEquals(0.0, o.getY(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testOrthogonalBranch3() {
        // v = (3, 3, 1), norm = sqrt(19) ~ 4.358, threshold ~ 2.615
        // x = 3.0 and y = 3.0 are outside [-threshold, threshold]
        Vector3D v = new Vector3D(3.0, 3.0, 1.0);
        Vector3D o = v.orthogonal();
        assertEquals(1.0, o.getNorm(), EPSILON);
        assertEquals(0.0, Vector3D.dotProduct(v, o), EPSILON);
        assertEquals(0.0, o.getZ(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAngleSeparated() {
        Vector3D v1 = new Vector3D(1.0, 0.0, 0.0);
        Vector3D v2 = new Vector3D(0.0, 1.0, 0.0);
        assertEquals(FastMath.PI / 2.0, Vector3D.angle(v1, v2), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAngleAlmostAlignedSameDirection() {
        Vector3D v1 = new Vector3D(1.0, 0.0, 0.0);
        Vector3D v2 = new Vector3D(1.0, 1.0e-6, 0.0);
        double angle = Vector3D.angle(v1, v2);
        assertTrue(angle > 0.0);
        assertTrue(angle < 1.0e-4);
    }

    @Test(timeout = 4000)
    public void testAngleAlmostAlignedOppositeDirection() {
        Vector3D v1 = new Vector3D(1.0, 0.0, 0.0);
        Vector3D v2 = new Vector3D(-1.0, 1.0e-6, 0.0);
        double angle = Vector3D.angle(v1, v2);
        assertTrue(angle < FastMath.PI);
        assertTrue(angle > FastMath.PI - 1.0e-4);
    }

    // =========================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // =========================================================================

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testNormalizeZeroNorm() {
        Vector3D.ZERO.normalize();
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testOrthogonalZeroNorm() {
        Vector3D.ZERO.orthogonal();
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testAngleFirstVectorZeroNorm() {
        Vector3D.angle(Vector3D.ZERO, Vector3D.PLUS_I);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testAngleSecondVectorZeroNorm() {
        Vector3D.angle(Vector3D.PLUS_I, Vector3D.ZERO);
    }

    // =========================================================================
    // PARTITION E: SPECIAL VALUES, PREDICATES & OBJECT CONTRACT
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsNaN() {
        assertFalse(new Vector3D(1.0, 2.0, 3.0).isNaN());
        assertTrue(Vector3D.NaN.isNaN());
        assertTrue(new Vector3D(Double.NaN, 1.0, 1.0).isNaN());
        assertTrue(new Vector3D(1.0, Double.NaN, 1.0).isNaN());
        assertTrue(new Vector3D(1.0, 1.0, Double.NaN).isNaN());
    }

    @Test(timeout = 4000)
    public void testIsInfinite() {
        assertFalse(new Vector3D(1.0, 2.0, 3.0).isInfinite());
        assertFalse(Vector3D.NaN.isInfinite());

        // NaN presence suppresses isInfinite
        Vector3D infWithNaN = new Vector3D(Double.POSITIVE_INFINITY, Double.NaN, 0.0);
        assertFalse(infWithNaN.isInfinite());

        assertTrue(Vector3D.POSITIVE_INFINITY.isInfinite());
        assertTrue(Vector3D.NEGATIVE_INFINITY.isInfinite());
        assertTrue(new Vector3D(Double.POSITIVE_INFINITY, 0.0, 0.0).isInfinite());
        assertTrue(new Vector3D(0.0, Double.NEGATIVE_INFINITY, 0.0).isInfinite());
        assertTrue(new Vector3D(0.0, 0.0, Double.POSITIVE_INFINITY).isInfinite());
    }

    @Test(timeout = 4000)
    public void testEqualsContract() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v1Same = new Vector3D(1.0, 2.0, 3.0);
        Vector3D vDiffX = new Vector3D(0.0, 2.0, 3.0);
        Vector3D vDiffY = new Vector3D(1.0, 0.0, 3.0);
        Vector3D vDiffZ = new Vector3D(1.0, 2.0, 0.0);

        // Reflexive
        assertTrue(v1.equals(v1));
        // Symmetric
        assertTrue(v1.equals(v1Same));
        assertTrue(v1Same.equals(v1));

        // Differing coordinates
        assertFalse(v1.equals(vDiffX));
        assertFalse(v1.equals(vDiffY));
        assertFalse(v1.equals(vDiffZ));

        // Null and foreign type
        assertFalse(v1.equals(null));
        assertFalse(v1.equals("NotAVector"));

        // NaN handling in equals
        Vector3D nan1 = new Vector3D(Double.NaN, 0.0, 0.0);
        Vector3D nan2 = new Vector3D(0.0, Double.NaN, 0.0);
        assertTrue(Vector3D.NaN.equals(nan1));
        assertTrue(nan1.equals(nan2));
        assertFalse(v1.equals(nan1));
        assertFalse(nan1.equals(v1));
    }

    @Test(timeout = 4000)
    public void testHashCodeContract() {
        Vector3D v1 = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v2 = new Vector3D(1.0, 2.0, 3.0);
        assertEquals(v1.hashCode(), v2.hashCode());

        // NaN hash codes are always 8
        assertEquals(8, Vector3D.NaN.hashCode());
        assertEquals(8, new Vector3D(Double.NaN, 0.0, 0.0).hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Vector3D v = new Vector3D(1.0, 2.0, 3.0);
        String s = v.toString();
        assertNotNull(s);
        assertTrue(s.length() > 0);
        assertTrue(s.contains("1") && s.contains("2") && s.contains("3"));
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        Vector3D v = new Vector3D(1.23, -4.56, 7.89);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(v);
        oos.flush();

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        Vector3D deserialized = (Vector3D) ois.readObject();

        assertEquals(v, deserialized);
        assertEquals(v.getX(), deserialized.getX(), EPSILON);
        assertEquals(v.getY(), deserialized.getY(), EPSILON);
        assertEquals(v.getZ(), deserialized.getZ(), EPSILON);
    }
}