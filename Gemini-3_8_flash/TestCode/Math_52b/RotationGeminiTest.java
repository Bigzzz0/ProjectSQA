/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math.geometry.euclidean.threed;

import org.apache.commons.math.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: org.apache.commons.math.geometry.euclidean.threed.Rotation
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. Quaternion normalization branch in Rotation(double, double, double, double, boolean):
 *    - needsNormalization == true (exercises FastMath.sqrt normalizer)
 *    - needsNormalization == false (direct coordinate assignment)
 * 2. Axis & Angle constructor:
 *    - norm == 0 -> ArithmeticException
 *    - valid axis, various angles (positive, negative, zero)
 * 3. Matrix constructor:
 *    - Invalid row/column dimensions (length != 3) -> NotARotationMatrixException
 *    - Negative determinant check (det < 0.0) -> NotARotationMatrixException
 *    - Failure to orthogonalize within 10 iterations -> NotARotationMatrixException
 *    - Quaternion reconstruction 4-way conditional branch:
 *      * s = ort[0][0] + ort[1][1] + ort[2][2] > -0.19 (q0 dominant)
 *      * s = ort[0][0] - ort[1][1] - ort[2][2] > -0.19 (q1 dominant / rotation around X)
 *      * s = ort[1][1] - ort[0][0] - ort[2][2] > -0.19 (q2 dominant / rotation around Y)
 *      * fallback (q3 dominant / rotation around Z)
 * 4. Two-vector pairs constructor:
 *    - Zero norm vector check -> IllegalArgumentException
 *    - Singular / degenerate vector alignments (c == 0 cascade):
 *      * c == 0 on k . u3
 *      * c == 0 on second attempt (k . u2Prime)
 *      * c == 0 on third attempt (aligned with everything -> IDENTITY rotation branch)
 *      * fallback to uRef = u2, vRef = v2
 * 5. Two single vectors constructor:
 *    - normProduct == 0 -> IllegalArgumentException
 *    - Opposite vectors (dot < (2e-15 - 1.0) * normProduct) -> orthogonal PI rotation
 *    - Non-collinear vectors -> standard cross product branch
 * 6. Axis & Angle retrieval:
 *    - getAxis(): squaredSine == 0 -> (1, 0, 0), q0 < 0 branch, q0 >= 0 branch
 *    - getAngle(): q0 < -0.1 || q0 > 0.1 (asin), q0 in [-0.1, 0) (acos(-q0)), q0 in [0, 0.1] (acos(q0))
 * 7. Cardan / Euler Angles:
 *    - All 12 RotationOrder types: XYZ, XZY, YXZ, YZX, ZXY, ZYX, XYX, XZX, YXY, YZY, ZXZ, ZYZ
 *    - Singularities (CardanEulerSingularityException) for Cardan and Euler orders
 * 8. Known Defect Reproduction (MATH-639):
 *    - Two vector pairs constructor failing with NaN due to negative inner product under square root.
 */
public class RotationGeminiTest {

    private static final double EPSILON = 1.0e-10;

    // ----------------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ----------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testIdentityConstantAndAccessors() {
        Rotation identity = Rotation.IDENTITY;
        assertEquals(1.0, identity.getQ0(), EPSILON);
        assertEquals(0.0, identity.getQ1(), EPSILON);
        assertEquals(0.0, identity.getQ2(), EPSILON);
        assertEquals(0.0, identity.getQ3(), EPSILON);
        assertEquals(0.0, identity.getAngle(), EPSILON);

        Vector3D axis = identity.getAxis();
        assertEquals(1.0, axis.getX(), EPSILON);
        assertEquals(0.0, axis.getY(), EPSILON);
        assertEquals(0.0, axis.getZ(), EPSILON);

        Vector3D v = new Vector3D(1.0, 2.0, 3.0);
        Vector3D applied = identity.applyTo(v);
        assertEquals(v.getX(), applied.getX(), EPSILON);
        assertEquals(v.getY(), applied.getY(), EPSILON);
        assertEquals(v.getZ(), applied.getZ(), EPSILON);

        Vector3D invApplied = identity.applyInverseTo(v);
        assertEquals(v.getX(), invApplied.getX(), EPSILON);
        assertEquals(v.getY(), invApplied.getY(), EPSILON);
        assertEquals(v.getZ(), invApplied.getZ(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testQuaternionNormalization() {
        // Needs normalization = true
        Rotation r = new Rotation(2.0, 2.0, -2.0, -2.0, true);
        double expected = 0.5;
        assertEquals(expected, r.getQ0(), EPSILON);
        assertEquals(expected, r.getQ1(), EPSILON);
        assertEquals(-expected, r.getQ2(), EPSILON);
        assertEquals(-expected, r.getQ3(), EPSILON);

        // Needs normalization = false
        Rotation rDirect = new Rotation(0.5, 0.5, -0.5, -0.5, false);
        assertEquals(0.5, rDirect.getQ0(), EPSILON);
        assertEquals(0.5, rDirect.getQ1(), EPSILON);
        assertEquals(-0.5, rDirect.getQ2(), EPSILON);
        assertEquals(-0.5, rDirect.getQ3(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testAxisAngleConstructor() {
        Vector3D axis = new Vector3D(0.0, 1.0, 0.0);
        double angle = FastMath.PI / 2.0;
        Rotation r = new Rotation(axis, angle);

        assertEquals(angle, r.getAngle(), EPSILON);
        Vector3D resAxis = r.getAxis();
        assertEquals(0.0, resAxis.getX(), EPSILON);
        assertEquals(1.0, resAxis.getY(), EPSILON);
        assertEquals(0.0, resAxis.getZ(), EPSILON);

        Vector3D transformed = r.applyTo(Vector3D.PLUS_I);
        assertEquals(0.0, transformed.getX(), EPSILON);
        assertEquals(0.0, transformed.getY(), EPSILON);
        assertEquals(-1.0, transformed.getZ(), EPSILON);

        Vector3D reverted = r.revert().applyTo(transformed);
        assertEquals(Vector3D.PLUS_I.getX(), reverted.getX(), EPSILON);
        assertEquals(Vector3D.PLUS_I.getY(), reverted.getY(), EPSILON);
        assertEquals(Vector3D.PLUS_I.getZ(), reverted.getZ(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testRotationComposition() {
        Rotation r1 = new Rotation(Vector3D.PLUS_K, FastMath.PI / 2.0);
        Rotation r2 = new Rotation(Vector3D.PLUS_I, FastMath.PI / 2.0);

        Rotation composed = r2.applyTo(r1);
        Vector3D v = Vector3D.PLUS_I;
        Vector3D result1 = composed.applyTo(v);
        Vector3D result2 = r2.applyTo(r1.applyTo(v));

        assertEquals(result2.getX(), result1.getX(), EPSILON);
        assertEquals(result2.getY(), result1.getY(), EPSILON);
        assertEquals(result2.getZ(), result1.getZ(), EPSILON);

        Rotation invComposed = r2.applyInverseTo(r1);
        Vector3D resultInv = invComposed.applyTo(v);
        Vector3D expectedInv = r2.revert().applyTo(r1.applyTo(v));
        assertEquals(expectedInv.getX(), resultInv.getX(), EPSILON);
        assertEquals(expectedInv.getY(), resultInv.getY(), EPSILON);
        assertEquals(expectedInv.getZ(), resultInv.getZ(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testMatrixRepresentation() throws NotARotationMatrixException {
        Rotation r = new Rotation(new Vector3D(1.0, 2.0, 3.0), 1.2);
        double[][] m = r.getMatrix();
        Rotation rRebuilt = new Rotation(m, 1.0e-10);

        assertEquals(0.0, Rotation.distance(r, rRebuilt), EPSILON);
    }

    // ----------------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ----------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetAxisAndAngleBranches() {
        // Branch: squaredSine == 0
        Rotation identity = new Rotation(1.0, 0.0, 0.0, 0.0, false);
        Vector3D axis = identity.getAxis();
        assertEquals(1.0, axis.getX(), EPSILON);
        assertEquals(0.0, axis.getY(), EPSILON);
        assertEquals(0.0, axis.getZ(), EPSILON);
        assertEquals(0.0, identity.getAngle(), EPSILON);

        // Branch: q0 < 0 in getAxis() and getAngle() with small negative q0 (between -0.1 and 0)
        // angle close to PI, e.g. angle = 3.10 rad -> halfAngle = 1.55, cos(halfAngle) = 0.02
        // If we negate quaternion coordinates (-q0, -q1, -q2, -q3), it represents the same rotation but q0 < 0
        Rotation rNearPi = new Rotation(Vector3D.PLUS_I, 3.1);
        Rotation rNegated = new Rotation(-rNearPi.getQ0(), -rNearPi.getQ1(), -rNearPi.getQ2(), -rNearPi.getQ3(), false);
        assertTrue(rNegated.getQ0() < 0.0 && rNegated.getQ0() > -0.1);
        assertEquals(3.1, rNegated.getAngle(), 1.0e-5);
        Vector3D axisNeg = rNegated.getAxis();
        assertEquals(1.0, FastMath.abs(axisNeg.getX()), EPSILON);

        // Branch: q0 in [0, 0.1] in getAngle()
        assertTrue(rNearPi.getQ0() > 0.0 && rNearPi.getQ0() < 0.1);
        assertEquals(3.1, rNearPi.getAngle(), 1.0e-5);

        // Branch: q0 < -0.1 in getAngle()
        Rotation rNegativeDominant = new Rotation(-0.8, 0.6, 0.0, 0.0, false);
        double ang = rNegativeDominant.getAngle();
        assertFalse(Double.isNaN(ang));
        assertTrue(ang > 0.0);
    }

    @Test(timeout = 4000)
    public void testTwoVectorsOppositeCollinear() {
        // dot < (2.0e-15 - 1.0) * normProduct branch
        Vector3D u = new Vector3D(1.0, 0.0, 0.0);
        Vector3D v = new Vector3D(-1.0, 0.0, 0.0);
        Rotation r = new Rotation(u, v);

        Vector3D transformed = r.applyTo(u);
        assertEquals(v.getX(), transformed.getX(), EPSILON);
        assertEquals(v.getY(), transformed.getY(), EPSILON);
        assertEquals(v.getZ(), transformed.getZ(), EPSILON);
        assertEquals(FastMath.PI, r.getAngle(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testTwoVectorsIdenticalCollinear() {
        Vector3D u = new Vector3D(1.0, 2.0, 3.0);
        Vector3D v = new Vector3D(1.0, 2.0, 3.0);
        Rotation r = new Rotation(u, v);

        assertEquals(0.0, r.getAngle(), EPSILON);
        Vector3D transformed = r.applyTo(u);
        assertEquals(u.getX(), transformed.getX(), EPSILON);
        assertEquals(u.getY(), transformed.getY(), EPSILON);
        assertEquals(u.getZ(), transformed.getZ(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testTwoVectorPairsDegenerateCases() {
        // Case: Identity mapping (c == 0 on third check)
        Vector3D u1 = Vector3D.PLUS_I;
        Vector3D u2 = Vector3D.PLUS_J;
        Rotation rId = new Rotation(u1, u2, u1, u2);
        assertEquals(1.0, rId.getQ0(), EPSILON);
        assertEquals(0.0, rId.getQ1(), EPSILON);
        assertEquals(0.0, rId.getQ2(), EPSILON);
        assertEquals(0.0, rId.getQ3(), EPSILON);

        // Case: c == 0 on first check, requiring fallback
        Vector3D v1 = Vector3D.PLUS_I;
        Vector3D v2 = Vector3D.MINUS_J;
        Rotation r = new Rotation(u1, u2, v1, v2);
        Vector3D transformedU1 = r.applyTo(u1);
        Vector3D transformedU2 = r.applyTo(u2);
        assertEquals(v1.getX(), transformedU1.getX(), EPSILON);
        assertEquals(v1.getY(), transformedU1.getY(), EPSILON);
        assertEquals(v1.getZ(), transformedU1.getZ(), EPSILON);
        assertEquals(v2.getX(), transformedU2.getX(), EPSILON);
        assertEquals(v2.getY(), transformedU2.getY(), EPSILON);
        assertEquals(v2.getZ(), transformedU2.getZ(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testMatrixConstructorAllDominantBranches() throws NotARotationMatrixException {
        // Branch 1: s > -0.19 with q0 dominant (Identity-like)
        double[][] m0 = new double[][] {
            {1.0, 0.0, 0.0},
            {0.0, 1.0, 0.0},
            {0.0, 0.0, 1.0}
        };
        Rotation r0 = new Rotation(m0, 1.0e-10);
        assertEquals(1.0, r0.getQ0(), EPSILON);

        // Branch 2: s > -0.19 with q1 dominant (180 deg around X: diag(1, -1, -1))
        double[][] m1 = new double[][] {
            {1.0,  0.0,  0.0},
            {0.0, -1.0,  0.0},
            {0.0,  0.0, -1.0}
        };
        Rotation r1 = new Rotation(m1, 1.0e-10);
        assertEquals(0.0, r1.getQ0(), EPSILON);
        assertEquals(1.0, FastMath.abs(r1.getQ1()), EPSILON);

        // Branch 3: s > -0.19 with q2 dominant (180 deg around Y: diag(-1, 1, -1))
        double[][] m2 = new double[][] {
            {-1.0,  0.0,  0.0},
            { 0.0,  1.0,  0.0},
            { 0.0,  0.0, -1.0}
        };
        Rotation r2 = new Rotation(m2, 1.0e-10);
        assertEquals(0.0, r2.getQ0(), EPSILON);
        assertEquals(1.0, FastMath.abs(r2.getQ2()), EPSILON);

        // Branch 4: Fallback with q3 dominant (180 deg around Z: diag(-1, -1, 1))
        double[][] m3 = new double[][] {
            {-1.0,  0.0,  0.0},
            { 0.0, -1.0,  0.0},
            { 0.0,  0.0,  1.0}
        };
        Rotation r3 = new Rotation(m3, 1.0e-10);
        assertEquals(0.0, r3.getQ0(), EPSILON);
        assertEquals(1.0, FastMath.abs(r3.getQ3()), EPSILON);
    }

    // ----------------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (MATH-639)
    // ----------------------------------------------------------------------------------

    /**
     * Targets Commons-Math issue 639 (MATH-639).
     * Two pairs of vectors that cause c to become negative / NaN in Rotation(u1, u2, v1, v2).
     */
    @Test(timeout = 4000)
    public void testIssue639() {
        Vector3D u1 = new Vector3D(-1321008684645961.0 / 268435456.0,
                                   -5774608829631843.0 / 268435456.0,
                                   -3845780896744725.0 / 16777216.0);
        Vector3D u2 = new Vector3D(-5774608829631843.0 / 268435456.0,
                                   1321008684645961.0 / 268435456.0,
                                   1234017215628888.0 / 16777216.0);
        Vector3D v1 = new Vector3D(1.0, 0.0, 0.0);
        Vector3D v2 = new Vector3D(0.0, 0.0, 1.0);

        Rotation r = new Rotation(u1, u2, v1, v2);

        assertFalse("Q0 must not be NaN", Double.isNaN(r.getQ0()));
        assertFalse("Q1 must not be NaN", Double.isNaN(r.getQ1()));
        assertFalse("Q2 must not be NaN", Double.isNaN(r.getQ2()));
        assertFalse("Q3 must not be NaN", Double.isNaN(r.getQ3()));

        assertEquals(0.6228370359608201, FastMath.abs(r.getQ0()), 1.0e-10);

        Vector3D normU1 = u1.scalarMultiply(1.0 / u1.getNorm());
        Vector3D normU2 = u2.scalarMultiply(1.0 / u2.getNorm());
        Vector3D normV1 = v1.scalarMultiply(1.0 / v1.getNorm());
        Vector3D normV2 = v2.scalarMultiply(1.0 / v2.getNorm());

        Vector3D transformedU1 = r.applyTo(normU1);
        Vector3D transformedU2 = r.applyTo(normU2);

        assertEquals(normV1.getX(), transformedU1.getX(), 1.0e-10);
        assertEquals(normV1.getY(), transformedU1.getY(), 1.0e-10);
        assertEquals(normV1.getZ(), transformedU1.getZ(), 1.0e-10);

        assertEquals(normV2.getX(), transformedU2.getX(), 1.0e-10);
        assertEquals(normV2.getY(), transformedU2.getY(), 1.0e-10);
        assertEquals(normV2.getZ(), transformedU2.getZ(), 1.0e-10);
    }

    // ----------------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ----------------------------------------------------------------------------------

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testZeroNormAxisThrowsException() {
        new Rotation(Vector3D.ZERO, FastMath.PI / 2.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTwoVectorsZeroOriginThrowsException() {
        new Rotation(Vector3D.ZERO, Vector3D.PLUS_I);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTwoVectorsZeroTargetThrowsException() {
        new Rotation(Vector3D.PLUS_I, Vector3D.ZERO);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTwoVectorPairsZeroU1ThrowsException() {
        new Rotation(Vector3D.ZERO, Vector3D.PLUS_J, Vector3D.PLUS_I, Vector3D.PLUS_K);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTwoVectorPairsZeroU2ThrowsException() {
        new Rotation(Vector3D.PLUS_I, Vector3D.ZERO, Vector3D.PLUS_I, Vector3D.PLUS_K);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTwoVectorPairsZeroV1ThrowsException() {
        new Rotation(Vector3D.PLUS_I, Vector3D.PLUS_J, Vector3D.ZERO, Vector3D.PLUS_K);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testTwoVectorPairsZeroV2ThrowsException() {
        new Rotation(Vector3D.PLUS_I, Vector3D.PLUS_J, Vector3D.PLUS_I, Vector3D.ZERO);
    }

    @Test(expected = NotARotationMatrixException.class, timeout = 4000)
    public void testMatrixInvalidRowDimension() throws NotARotationMatrixException {
        double[][] m = new double[][] {
            {1.0, 0.0},
            {0.0, 1.0}
        };
        new Rotation(m, 1.0e-10);
    }

    @Test(expected = NotARotationMatrixException.class, timeout = 4000)
    public void testMatrixInvalidColumnDimension() throws NotARotationMatrixException {
        double[][] m = new double[][] {
            {1.0, 0.0, 0.0, 0.0},
            {0.0, 1.0, 0.0, 0.0},
            {0.0, 0.0, 1.0, 0.0}
        };
        new Rotation(m, 1.0e-10);
    }

    @Test(expected = NotARotationMatrixException.class, timeout = 4000)
    public void testMatrixNegativeDeterminantThrows() throws NotARotationMatrixException {
        // Reflection matrix (det = -1)
        double[][] m = new double[][] {
            {-1.0,  0.0,  0.0},
            { 0.0,  1.0,  0.0},
            { 0.0,  0.0,  1.0}
        };
        new Rotation(m, 1.0e-10);
    }

    @Test(expected = NotARotationMatrixException.class, timeout = 4000)
    public void testMatrixNonOrthogonalNonConvergentThrows() throws NotARotationMatrixException {
        // Singular zero matrix cannot be orthogonalized
        double[][] m = new double[][] {
            {0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0}
        };
        new Rotation(m, 1.0e-10);
    }

    // ----------------------------------------------------------------------------------
    // Partition E: Rotation Orders & Singularities
    // ----------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAllCardanOrders() throws CardanEulerSingularityException {
        RotationOrder[] cardanOrders = new RotationOrder[] {
            RotationOrder.XYZ, RotationOrder.XZY, RotationOrder.YXZ,
            RotationOrder.YZX, RotationOrder.ZXY, RotationOrder.ZYX
        };

        double a1 = 0.2;
        double a2 = -0.3;
        double a3 = 0.4;

        for (RotationOrder order : cardanOrders) {
            Rotation r = new Rotation(order, a1, a2, a3);
            double[] angles = r.getAngles(order);
            assertEquals("Order " + order + " angle 1 mismatch", a1, angles[0], 1.0e-9);
            assertEquals("Order " + order + " angle 2 mismatch", a2, angles[1], 1.0e-9);
            assertEquals("Order " + order + " angle 3 mismatch", a3, angles[2], 1.0e-9);
        }
    }

    @Test(timeout = 4000)
    public void testAllEulerOrders() throws CardanEulerSingularityException {
        RotationOrder[] eulerOrders = new RotationOrder[] {
            RotationOrder.XYX, RotationOrder.XZX, RotationOrder.YXY,
            RotationOrder.YZY, RotationOrder.ZXZ, RotationOrder.ZYZ
        };

        double a1 = 0.2;
        double a2 = 0.5; // Must be in (0, PI) to avoid singularity
        double a3 = 0.4;

        for (RotationOrder order : eulerOrders) {
            Rotation r = new Rotation(order, a1, a2, a3);
            double[] angles = r.getAngles(order);
            assertEquals("Order " + order + " angle 1 mismatch", a1, angles[0], 1.0e-9);
            assertEquals("Order " + order + " angle 2 mismatch", a2, angles[1], 1.0e-9);
            assertEquals("Order " + order + " angle 3 mismatch", a3, angles[2], 1.0e-9);
        }
    }

    @Test(expected = CardanEulerSingularityException.class, timeout = 4000)
    public void testCardanSingularityXYZ() throws CardanEulerSingularityException {
        // In XYZ order, angle 2 at PI / 2 is singular
        Rotation r = new Rotation(RotationOrder.XYZ, 0.1, FastMath.PI / 2.0, 0.2);
        r.getAngles(RotationOrder.XYZ);
    }

    @Test(expected = CardanEulerSingularityException.class, timeout = 4000)
    public void testCardanSingularityXZY() throws CardanEulerSingularityException {
        Rotation r = new Rotation(RotationOrder.XZY, 0.1, FastMath.PI / 2.0, 0.2);
        r.getAngles(RotationOrder.XZY);
    }

    @Test(expected = CardanEulerSingularityException.class, timeout = 4000)
    public void testCardanSingularityYXZ() throws CardanEulerSingularityException {
        Rotation r = new Rotation(RotationOrder.YXZ, 0.1, FastMath.PI / 2.0, 0.2);
        r.getAngles(RotationOrder.YXZ);
    }

    @Test(expected = CardanEulerSingularityException.class, timeout = 4000)
    public void testCardanSingularityYZX() throws CardanEulerSingularityException {
        Rotation r = new Rotation(RotationOrder.YZX, 0.1, FastMath.PI / 2.0, 0.2);
        r.getAngles(RotationOrder.YZX);
    }

    @Test(expected = CardanEulerSingularityException.class, timeout = 4000)
    public void testCardanSingularityZXY() throws CardanEulerSingularityException {
        Rotation r = new Rotation(RotationOrder.ZXY, 0.1, FastMath.PI / 2.0, 0.2);
        r.getAngles(RotationOrder.ZXY);
    }

    @Test(expected = CardanEulerSingularityException.class, timeout = 4000)
    public void testCardanSingularityZYX() throws CardanEulerSingularityException {
        Rotation r = new Rotation(RotationOrder.ZYX, 0.1, FastMath.PI / 2.0, 0.2);
        r.getAngles(RotationOrder.ZYX);
    }

    @Test(expected = CardanEulerSingularityException.class, timeout = 4000)
    public void testEulerSingularityIdentity() throws CardanEulerSingularityException {
        // Identity rotation is always singular for Euler angles (middle angle = 0)
        Rotation.IDENTITY.getAngles(RotationOrder.ZXZ);
    }

    @Test(expected = CardanEulerSingularityException.class, timeout = 4000)
    public void testEulerSingularityXYX() throws CardanEulerSingularityException {
        Rotation r = new Rotation(RotationOrder.XYX, 0.1, 0.0, 0.2);
        r.getAngles(RotationOrder.XYX);
    }

    @Test(expected = CardanEulerSingularityException.class, timeout = 4000)
    public void testEulerSingularityXZX() throws CardanEulerSingularityException {
        Rotation r = new Rotation(RotationOrder.XZX, 0.1, 0.0, 0.2);
        r.getAngles(RotationOrder.XZX);
    }

    @Test(expected = CardanEulerSingularityException.class, timeout = 4000)
    public void testEulerSingularityYXY() throws CardanEulerSingularityException {
        Rotation r = new Rotation(RotationOrder.YXY, 0.1, 0.0, 0.2);
        r.getAngles(RotationOrder.YXY);
    }

    @Test(expected = CardanEulerSingularityException.class, timeout = 4000)
    public void testEulerSingularityYZY() throws CardanEulerSingularityException {
        Rotation r = new Rotation(RotationOrder.YZY, 0.1, 0.0, 0.2);
        r.getAngles(RotationOrder.YZY);
    }

    @Test(expected = CardanEulerSingularityException.class, timeout = 4000)
    public void testEulerSingularityZYZ() throws CardanEulerSingularityException {
        Rotation r = new Rotation(RotationOrder.ZYZ, 0.1, 0.0, 0.2);
        r.getAngles(RotationOrder.ZYZ);
    }

    // ----------------------------------------------------------------------------------
    // Partition F: Distance & Geometric Operations
    // ----------------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDistanceCalculation() {
        Rotation r1 = new Rotation(Vector3D.PLUS_I, 0.3);
        Rotation r2 = new Rotation(Vector3D.PLUS_I, 0.7);

        assertEquals(0.0, Rotation.distance(r1, r1), EPSILON);
        assertEquals(0.4, Rotation.distance(r1, r2), EPSILON);

        // Opposite representation of same rotation: distance must still be 0
        Rotation r1Opposite = new Rotation(-r1.getQ0(), -r1.getQ1(), -r1.getQ2(), -r1.getQ3(), false);
        assertEquals(0.0, Rotation.distance(r1, r1Opposite), EPSILON);
    }
}