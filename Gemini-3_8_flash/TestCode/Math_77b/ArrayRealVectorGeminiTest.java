package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * 1. Target Class: org.apache.commons.math.linear.ArrayRealVector
 * 2. Defect Analysis (Defects4J):
 *    - Method: getLInfNorm()
 *    - Defect Pattern:
 *        double max = 0;
 *        for (double a : data) {
 *            max += Math.max(max, Math.abs(a)); // BUG: Accumulates instead of updating max!
 *        }
 *        return max;
 *    - Ground Truth Failure:
 *        ArrayRealVectorTest::testBasicFunctions -> expected:<6.0> but was:<128.0>
 *        SparseRealVectorTest::testBasicFunctions -> expected:<6.0> but was:<-3.0>
 *    - Trigger Mechanism:
 *        Given vector [2.0, 6.0, 1.0, 2.0, 3.0, 4.0], the true L-infinity norm is max|x_i| = 6.0.
 *        Due to 'max += Math.max(max, Math.abs(a))', the buggy code doubles max in subsequent steps
 *        yielding 128.0 instead of 6.0.
 * 3. Branch & Condition Matrix:
 *    - Constructors: empty, size only, preset, double[], (double[], boolean) with null/empty/clone/ref,
 *      sub-array constructors (fitting and non-fitting pos/size), Double[] wrappers, copy constructors
 *      (deep vs shallow), compound append constructors (ArrayRealVector, RealVector, double[] permutations).
 *    - Polymorphism branches (instanceof ArrayRealVector vs other RealVector like OpenMapRealVector):
 *      add, subtract, ebeMultiply, ebeDivide, dotProduct, getDistance, getL1Distance, getLInfDistance,
 *      outerProduct, append, setSubVector.
 *    - Self mapping unary functions: mapAddToSelf, mapSubtractToSelf, mapMultiplyToSelf, mapDivideToSelf,
 *      mapPowToSelf, exp, expm1, log, log10, log1p, cosh, sinh, tanh, cos, sin, tan, acos, asin, atan,
 *      inv, abs, sqrt, cbrt, ceil, floor, rint, signum, ulp.
 *    - Edge states: NaN handling in isNaN, isInfinite (NaN short-circuit vs Infinite checks), equals, hashCode.
 *    - Exception Paths: Dimension mismatch in all binary ops, zero norm in unitVector/unitize,
 *      out-of-bounds in getEntry/setEntry/getSubVector/setSubVector.
 * ====================================================================================================
 */
public class ArrayRealVectorGeminiTest {

    private static final double EPS = 1e-11;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndAccessors() {
        // Default constructor (0 length)
        ArrayRealVector v0 = new ArrayRealVector();
        assertEquals(0, v0.getDimension());
        assertEquals(0, v0.getData().length);

        // Size constructor
        ArrayRealVector vSize = new ArrayRealVector(4);
        assertEquals(4, vSize.getDimension());
        for (int i = 0; i < 4; i++) {
            assertEquals(0.0, vSize.getEntry(i), EPS);
        }

        // Preset constructor
        ArrayRealVector vPreset = new ArrayRealVector(3, 7.5);
        assertEquals(3, vPreset.getDimension());
        for (int i = 0; i < 3; i++) {
            assertEquals(7.5, vPreset.getEntry(i), EPS);
        }

        // double[] copy constructor
        double[] raw = new double[]{1.0, 2.0, 3.0};
        ArrayRealVector vRaw = new ArrayRealVector(raw);
        assertNotSame(raw, vRaw.getDataRef());
        assertArrayEquals(raw, vRaw.getData(), EPS);

        // double[] with copyArray flag
        ArrayRealVector vRef = new ArrayRealVector(raw, false);
        assertSame(raw, vRef.getDataRef());

        ArrayRealVector vCopy = new ArrayRealVector(raw, true);
        assertNotSame(raw, vCopy.getDataRef());
        assertArrayEquals(raw, vCopy.getDataRef(), EPS);

        // Sub-array constructor
        double[] larger = new double[]{10.0, 20.0, 30.0, 40.0, 50.0};
        ArrayRealVector vSub = new ArrayRealVector(larger, 1, 3);
        assertEquals(3, vSub.getDimension());
        assertArrayEquals(new double[]{20.0, 30.0, 40.0}, vSub.getData(), EPS);

        // Double[] boxed constructors
        Double[] boxed = new Double[]{1.1, 2.2, 3.3, 4.4};
        ArrayRealVector vBoxed = new ArrayRealVector(boxed);
        assertEquals(4, vBoxed.getDimension());
        assertEquals(2.2, vBoxed.getEntry(1), EPS);

        ArrayRealVector vBoxedSub = new ArrayRealVector(boxed, 2, 2);
        assertEquals(2, vBoxedSub.getDimension());
        assertEquals(3.3, vBoxedSub.getEntry(0), EPS);
        assertEquals(4.4, vBoxedSub.getEntry(1), EPS);
    }

    @Test(timeout = 4000)
    public void testCompoundConstructors() {
        ArrayRealVector av1 = new ArrayRealVector(new double[]{1.0, 2.0});
        ArrayRealVector av2 = new ArrayRealVector(new double[]{3.0, 4.0});
        RealVector sv = new OpenMapRealVector(new double[]{5.0, 6.0});
        double[] d = new double[]{7.0, 8.0};

        // ArrayRealVector + ArrayRealVector
        ArrayRealVector c1 = new ArrayRealVector(av1, av2);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, c1.getData(), EPS);

        // ArrayRealVector + RealVector
        ArrayRealVector c2 = new ArrayRealVector(av1, sv);
        assertArrayEquals(new double[]{1.0, 2.0, 5.0, 6.0}, c2.getData(), EPS);

        // RealVector + ArrayRealVector
        ArrayRealVector c3 = new ArrayRealVector(sv, av1);
        assertArrayEquals(new double[]{5.0, 6.0, 1.0, 2.0}, c3.getData(), EPS);

        // ArrayRealVector + double[]
        ArrayRealVector c4 = new ArrayRealVector(av1, d);
        assertArrayEquals(new double[]{1.0, 2.0, 7.0, 8.0}, c4.getData(), EPS);

        // double[] + ArrayRealVector
        ArrayRealVector c5 = new ArrayRealVector(d, av1);
        assertArrayEquals(new double[]{7.0, 8.0, 1.0, 2.0}, c5.getData(), EPS);

        // double[] + double[]
        ArrayRealVector c6 = new ArrayRealVector(new double[]{0.5}, d);
        assertArrayEquals(new double[]{0.5, 7.0, 8.0}, c6.getData(), EPS);

        // Copy constructor deep vs shallow
        ArrayRealVector orig = new ArrayRealVector(new double[]{9.0, 10.0});
        ArrayRealVector deepCopy = new ArrayRealVector(orig, true);
        assertNotSame(orig.getDataRef(), deepCopy.getDataRef());

        ArrayRealVector shallowCopy = new ArrayRealVector(orig, false);
        assertSame(orig.getDataRef(), shallowCopy.getDataRef());

        ArrayRealVector copyFromRv = new ArrayRealVector((RealVector) orig);
        assertArrayEquals(orig.getData(), copyFromRv.getData(), EPS);

        AbstractRealVector copied = orig.copy();
        assertTrue(copied instanceof ArrayRealVector);
        assertNotSame(orig.getDataRef(), ((ArrayRealVector) copied).getDataRef());
        assertArrayEquals(orig.getData(), copied.getData(), EPS);
    }

    @Test(timeout = 4000)
    public void testBasicAlgebraicOperations() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{4.0, 5.0, 6.0});
        RealVector sv = new OpenMapRealVector(new double[]{4.0, 5.0, 6.0});
        double[] raw = new double[]{4.0, 5.0, 6.0};

        // Add
        assertArrayEquals(new double[]{5.0, 7.0, 9.0}, v1.add(v2).getData(), EPS);
        assertArrayEquals(new double[]{5.0, 7.0, 9.0}, v1.add(sv).getData(), EPS);
        assertArrayEquals(new double[]{5.0, 7.0, 9.0}, v1.add(raw).getData(), EPS);

        // Subtract
        assertArrayEquals(new double[]{-3.0, -3.0, -3.0}, v1.subtract(v2).getData(), EPS);
        assertArrayEquals(new double[]{-3.0, -3.0, -3.0}, v1.subtract(sv).getData(), EPS);
        assertArrayEquals(new double[]{-3.0, -3.0, -3.0}, v1.subtract(raw).getData(), EPS);

        // Multiply element-by-element
        assertArrayEquals(new double[]{4.0, 10.0, 18.0}, v1.ebeMultiply(v2).getData(), EPS);
        assertArrayEquals(new double[]{4.0, 10.0, 18.0}, v1.ebeMultiply(sv).getData(), EPS);
        assertArrayEquals(new double[]{4.0, 10.0, 18.0}, v1.ebeMultiply(raw).getData(), EPS);

        // Divide element-by-element
        assertArrayEquals(new double[]{0.25, 0.4, 0.5}, v1.ebeDivide(v2).getData(), EPS);
        assertArrayEquals(new double[]{0.25, 0.4, 0.5}, v1.ebeDivide(sv).getData(), EPS);
        assertArrayEquals(new double[]{0.25, 0.4, 0.5}, v1.ebeDivide(raw).getData(), EPS);

        // Dot product
        double expectedDot = 1.0 * 4.0 + 2.0 * 5.0 + 3.0 * 6.0; // 32.0
        assertEquals(expectedDot, v1.dotProduct(v2), EPS);
        assertEquals(expectedDot, v1.dotProduct(sv), EPS);
        assertEquals(expectedDot, v1.dotProduct(raw), EPS);

        // Norms and distances
        assertEquals(Math.sqrt(14.0), v1.getNorm(), EPS);
        assertEquals(6.0, v1.getL1Norm(), EPS);

        double expectedDist = Math.sqrt(27.0);
        assertEquals(expectedDist, v1.getDistance(v2), EPS);
        assertEquals(expectedDist, v1.getDistance(sv), EPS);
        assertEquals(expectedDist, v1.getDistance(raw), EPS);

        assertEquals(9.0, v1.getL1Distance(v2), EPS);
        assertEquals(9.0, v1.getL1Distance(sv), EPS);
        assertEquals(9.0, v1.getL1Distance(raw), EPS);

        assertEquals(3.0, v1.getLInfDistance(v2), EPS);
        assertEquals(3.0, v1.getLInfDistance(sv), EPS);
        assertEquals(3.0, v1.getLInfDistance(raw), EPS);
    }

    @Test(timeout = 4000)
    public void testMapToSelfOperations() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});

        v.mapAddToSelf(2.0);
        assertArrayEquals(new double[]{3.0, 4.0, 5.0}, v.getData(), EPS);

        v.mapSubtractToSelf(1.0);
        assertArrayEquals(new double[]{2.0, 3.0, 4.0}, v.getData(), EPS);

        v.mapMultiplyToSelf(2.0);
        assertArrayEquals(new double[]{4.0, 6.0, 8.0}, v.getData(), EPS);

        v.mapDivideToSelf(2.0);
        assertArrayEquals(new double[]{2.0, 3.0, 4.0}, v.getData(), EPS);

        v.mapPowToSelf(2.0);
        assertArrayEquals(new double[]{4.0, 9.0, 16.0}, v.getData(), EPS);

        v.mapSqrtToSelf();
        assertArrayEquals(new double[]{2.0, 3.0, 4.0}, v.getData(), EPS);

        v.mapInvToSelf();
        assertArrayEquals(new double[]{0.5, 1.0 / 3.0, 0.25}, v.getData(), EPS);

        ArrayRealVector vTrig = new ArrayRealVector(new double[]{0.0, Math.PI / 6.0, Math.PI / 4.0});
        vTrig.mapSinToSelf();
        assertEquals(0.0, vTrig.getEntry(0), EPS);
        assertEquals(0.5, vTrig.getEntry(1), EPS);

        ArrayRealVector vCos = new ArrayRealVector(new double[]{0.0, Math.PI});
        vCos.mapCosToSelf();
        assertEquals(1.0, vCos.getEntry(0), EPS);
        assertEquals(-1.0, vCos.getEntry(1), EPS);

        ArrayRealVector vTan = new ArrayRealVector(new double[]{0.0, Math.PI / 4.0});
        vTan.mapTanToSelf();
        assertEquals(0.0, vTan.getEntry(0), EPS);
        assertEquals(1.0, vTan.getEntry(1), EPS);

        ArrayRealVector vExp = new ArrayRealVector(new double[]{0.0, 1.0});
        vExp.mapExpToSelf();
        assertEquals(1.0, vExp.getEntry(0), EPS);
        assertEquals(Math.E, vExp.getEntry(1), EPS);

        ArrayRealVector vExpm1 = new ArrayRealVector(new double[]{0.0});
        vExpm1.mapExpm1ToSelf();
        assertEquals(0.0, vExpm1.getEntry(0), EPS);

        ArrayRealVector vLog = new ArrayRealVector(new double[]{1.0, Math.E});
        vLog.mapLogToSelf();
        assertEquals(0.0, vLog.getEntry(0), EPS);
        assertEquals(1.0, vLog.getEntry(1), EPS);

        ArrayRealVector vLog10 = new ArrayRealVector(new double[]{1.0, 100.0});
        vLog10.mapLog10ToSelf();
        assertEquals(0.0, vLog10.getEntry(0), EPS);
        assertEquals(2.0, vLog10.getEntry(1), EPS);

        ArrayRealVector vLog1p = new ArrayRealVector(new double[]{0.0, Math.E - 1.0});
        vLog1p.mapLog1pToSelf();
        assertEquals(0.0, vLog1p.getEntry(0), EPS);
        assertEquals(1.0, vLog1p.getEntry(1), EPS);

        ArrayRealVector vHyp = new ArrayRealVector(new double[]{0.0});
        vHyp.mapCoshToSelf();
        assertEquals(1.0, vHyp.getEntry(0), EPS);
        vHyp.mapSinhToSelf();
        assertEquals(Math.sinh(1.0), vHyp.getEntry(0), EPS);
        vHyp.mapTanhToSelf();
        assertEquals(Math.tanh(Math.sinh(1.0)), vHyp.getEntry(0), EPS);

        ArrayRealVector vInvTrig = new ArrayRealVector(new double[]{0.5});
        vInvTrig.mapAcosToSelf();
        assertEquals(Math.acos(0.5), vInvTrig.getEntry(0), EPS);
        vInvTrig.setEntry(0, 0.5);
        vInvTrig.mapAsinToSelf();
        assertEquals(Math.asin(0.5), vInvTrig.getEntry(0), EPS);
        vInvTrig.setEntry(0, 1.0);
        vInvTrig.mapAtanToSelf();
        assertEquals(Math.atan(1.0), vInvTrig.getEntry(0), EPS);

        ArrayRealVector vMisc = new ArrayRealVector(new double[]{-2.7, 3.2, 8.0});
        vMisc.mapAbsToSelf();
        assertEquals(2.7, vMisc.getEntry(0), EPS);

        ArrayRealVector vCbrt = new ArrayRealVector(new double[]{8.0, 27.0});
        vCbrt.mapCbrtToSelf();
        assertEquals(2.0, vCbrt.getEntry(0), EPS);
        assertEquals(3.0, vCbrt.getEntry(1), EPS);

        ArrayRealVector vRound = new ArrayRealVector(new double[]{-1.6, 2.3, 3.5});
        vRound.mapCeilToSelf();
        assertEquals(-1.0, vRound.getEntry(0), EPS);
        assertEquals(3.0, vRound.getEntry(1), EPS);

        vRound.setEntry(0, -1.6);
        vRound.setEntry(1, 2.3);
        vRound.mapFloorToSelf();
        assertEquals(-2.0, vRound.getEntry(0), EPS);
        assertEquals(2.0, vRound.getEntry(1), EPS);

        vRound.setEntry(0, 2.5);
        vRound.mapRintToSelf();
        assertEquals(2.0, vRound.getEntry(0), EPS);

        ArrayRealVector vSign = new ArrayRealVector(new double[]{-15.0, 0.0, 20.0});
        vSign.mapSignumToSelf();
        assertEquals(-1.0, vSign.getEntry(0), EPS);
        assertEquals(0.0, vSign.getEntry(1), EPS);
        assertEquals(1.0, vSign.getEntry(2), EPS);

        ArrayRealVector vUlp = new ArrayRealVector(new double[]{1.0});
        vUlp.mapUlpToSelf();
        assertEquals(Math.ulp(1.0), vUlp.getEntry(0), EPS);
    }

    @Test(timeout = 4000)
    public void testUnitVectorAndProjection() {
        ArrayRealVector v = new ArrayRealVector(new double[]{3.0, 4.0});
        RealVector unit = v.unitVector();
        assertEquals(0.6, unit.getEntry(0), EPS);
        assertEquals(0.8, unit.getEntry(1), EPS);

        v.unitize();
        assertEquals(0.6, v.getEntry(0), EPS);
        assertEquals(0.8, v.getEntry(1), EPS);
        assertEquals(1.0, v.getNorm(), EPS);

        ArrayRealVector a = new ArrayRealVector(new double[]{3.0, 1.0});
        ArrayRealVector b = new ArrayRealVector(new double[]{2.0, 0.0});
        RealVector proj1 = a.projection(b);
        assertEquals(3.0, proj1.getEntry(0), EPS);
        assertEquals(0.0, proj1.getEntry(1), EPS);

        RealVector proj2 = a.projection((RealVector) b);
        assertEquals(3.0, proj2.getEntry(0), EPS);

        RealVector proj3 = a.projection(new double[]{2.0, 0.0});
        assertEquals(3.0, proj3.getEntry(0), EPS);
    }

    @Test(timeout = 4000)
    public void testOuterProduct() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{3.0, 4.0});
        RealVector sv2 = new OpenMapRealVector(new double[]{3.0, 4.0});
        double[] raw2 = new double[]{3.0, 4.0};

        RealMatrix m1 = v1.outerProduct(v2);
        assertEquals(2, m1.getRowDimension());
        assertEquals(2, m1.getColumnDimension());
        assertEquals(3.0, m1.getEntry(0, 0), EPS);
        assertEquals(4.0, m1.getEntry(0, 1), EPS);
        assertEquals(6.0, m1.getEntry(1, 0), EPS);
        assertEquals(8.0, m1.getEntry(1, 1), EPS);

        RealMatrix m2 = v1.outerProduct(sv2);
        assertEquals(3.0, m2.getEntry(0, 0), EPS);
        assertEquals(8.0, m2.getEntry(1, 1), EPS);

        RealMatrix m3 = v1.outerProduct(raw2);
        assertEquals(3.0, m3.getEntry(0, 0), EPS);
        assertEquals(8.0, m3.getEntry(1, 1), EPS);
    }

    @Test(timeout = 4000)
    public void testAppendAndSubVectorMethods() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});

        RealVector appScalar = v.append(3.0);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, appScalar.getData(), EPS);

        RealVector appArray = v.append(new double[]{3.0, 4.0});
        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, appArray.getData(), EPS);

        RealVector appArv = v.append(new ArrayRealVector(new double[]{3.0, 4.0}));
        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, appArv.getData(), EPS);

        RealVector appRv = v.append((RealVector) new OpenMapRealVector(new double[]{3.0, 4.0}));
        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, appRv.getData(), EPS);

        ArrayRealVector base = new ArrayRealVector(new double[]{10.0, 20.0, 30.0, 40.0, 50.0});
        RealVector sub = base.getSubVector(1, 3);
        assertArrayEquals(new double[]{20.0, 30.0, 40.0}, sub.getData(), EPS);

        base.setSubVector(1, new double[]{22.0, 33.0});
        assertEquals(22.0, base.getEntry(1), EPS);
        assertEquals(33.0, base.getEntry(2), EPS);

        base.set(1, new ArrayRealVector(new double[]{222.0, 333.0}));
        assertEquals(222.0, base.getEntry(1), EPS);
        assertEquals(333.0, base.getEntry(2), EPS);

        base.setSubVector(3, new OpenMapRealVector(new double[]{444.0, 555.0}));
        assertEquals(444.0, base.getEntry(3), EPS);
        assertEquals(555.0, base.getEntry(4), EPS);

        base.set(99.0);
        for (int i = 0; i < base.getDimension(); i++) {
            assertEquals(99.0, base.getEntry(i), EPS);
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsNaNAndIsInfiniteConditions() {
        ArrayRealVector normal = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        assertFalse(normal.isNaN());
        assertFalse(normal.isInfinite());

        ArrayRealVector withNaN = new ArrayRealVector(new double[]{1.0, Double.NaN, 3.0});
        assertTrue(withNaN.isNaN());
        assertFalse(withNaN.isInfinite());

        ArrayRealVector withInf = new ArrayRealVector(new double[]{1.0, Double.POSITIVE_INFINITY, 3.0});
        assertFalse(withInf.isNaN());
        assertTrue(withInf.isInfinite());

        ArrayRealVector withNegInf = new ArrayRealVector(new double[]{Double.NEGATIVE_INFINITY, 2.0});
        assertFalse(withNegInf.isNaN());
        assertTrue(withNegInf.isInfinite());

        // Both NaN and Infinite: isInfinite() MUST return false per contract if isNaN() is true
        ArrayRealVector both = new ArrayRealVector(new double[]{Double.NaN, Double.POSITIVE_INFINITY});
        assertTrue(both.isNaN());
        assertFalse(both.isInfinite());
    }

    @Test(timeout = 4000)
    public void testToStringAndToArray() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        String s = v.toString();
        assertNotNull(s);
        assertTrue(s.contains("1") && s.contains("2"));

        double[] arr = v.toArray();
        assertNotSame(v.getDataRef(), arr);
        assertArrayEquals(v.getData(), arr, EPS);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (getLInfNorm Defect Detection)
    // =========================================================================

    /**
     * Target Defect Ground Truth:
     * ArrayRealVectorTest::testBasicFunctions expected:<6.0> but was:<128.0>
     *
     * In defective ArrayRealVector:
     *   for (double a : data) {
     *       max += Math.max(max, Math.abs(a));
     *   }
     *
     * For input [2.0, 6.0, 1.0, 2.0, 3.0, 4.0]:
     *   Step 0: max = 0 + max(0, 2) = 2.0
     *   Step 1: max = 2 + max(2, 6) = 8.0
     *   Step 2: max = 8 + max(8, 1) = 16.0
     *   Step 3: max = 16 + max(16, 2) = 32.0
     *   Step 4: max = 32 + max(32, 3) = 64.0
     *   Step 5: max = 64 + max(64, 4) = 128.0
     *
     * The correct mathematical L-infinity norm is 6.0.
     * This test strictly asserts the specification value 6.0, deterministically exposing the defect.
     */
    @Test(timeout = 4000)
    public void testDefectTargetLInfNormAccumulationBug() {
        double[] testData = new double[]{2.0, 6.0, 1.0, 2.0, 3.0, 4.0};
        ArrayRealVector vector = new ArrayRealVector(testData);

        double actualLInfNorm = vector.getLInfNorm();

        assertEquals("getLInfNorm must return max(|x_i|), not accumulated partial sums",
                     6.0, actualLInfNorm, EPS);
    }

    @Test(timeout = 4000)
    public void testDefectTargetLInfNormNegativeValues() {
        double[] testData = new double[]{-1.0, 2.0, 3.0, -4.0, 5.0, -6.0};
        ArrayRealVector vector = new ArrayRealVector(testData);

        assertEquals(6.0, vector.getLInfNorm(), EPS);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorNullArrayThrowsNPE() {
        new ArrayRealVector((double[]) null, true);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorEmptyArrayThrowsIAE() {
        new ArrayRealVector(new double[0], true);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorInvalidPosAndSizeDouble() {
        new ArrayRealVector(new double[]{1.0, 2.0, 3.0}, 2, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorInvalidPosAndSizeBoxedDouble() {
        new ArrayRealVector(new Double[]{1.0, 2.0, 3.0}, 1, 3);
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testUnitVectorZeroNormThrowsArithmeticException() {
        ArrayRealVector zero = new ArrayRealVector(new double[]{0.0, 0.0});
        zero.unitVector();
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testUnitizeZeroNormThrowsArithmeticException() {
        ArrayRealVector zero = new ArrayRealVector(new double[]{0.0, 0.0, 0.0});
        zero.unitize();
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddDimensionMismatchArrayRealVector() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0});
        v1.add(v2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddDimensionMismatchGenericRealVector() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        RealVector v2 = new OpenMapRealVector(new double[]{1.0});
        v1.add(v2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubtractDimensionMismatchArrayRealVector() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        v1.subtract(v2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubtractDimensionMismatchGenericRealVector() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        RealVector v2 = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        v1.subtract(v2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEbeMultiplyDimensionMismatch() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        v1.ebeMultiply(new double[]{1.0});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEbeMultiplyDimensionMismatchGeneric() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        v1.ebeMultiply(new OpenMapRealVector(new double[]{1.0}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEbeDivideDimensionMismatch() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        v1.ebeDivide(new double[]{1.0});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEbeDivideDimensionMismatchGeneric() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        v1.ebeDivide(new OpenMapRealVector(new double[]{1.0}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDotProductDimensionMismatch() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        v1.dotProduct(new double[]{1.0, 2.0, 3.0});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDotProductDimensionMismatchGeneric() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        v1.dotProduct(new OpenMapRealVector(new double[]{1.0, 2.0, 3.0}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDistanceDimensionMismatch() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        v1.getDistance(new OpenMapRealVector(new double[]{1.0}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testL1DistanceDimensionMismatch() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        v1.getL1Distance(new OpenMapRealVector(new double[]{1.0}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLInfDistanceDimensionMismatch() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        v1.getLInfDistance(new OpenMapRealVector(new double[]{1.0}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testOuterProductDimensionMismatch() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        v1.outerProduct(new OpenMapRealVector(new double[]{1.0}));
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetEntryNegativeIndexThrows() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        v.getEntry(-1);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetEntryOverIndexThrows() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        v.getEntry(2);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testSetEntryNegativeIndexThrows() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        v.setEntry(-1, 5.0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testSetEntryOverIndexThrows() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        v.setEntry(2, 5.0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetSubVectorOutOfBoundsThrows() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        v.getSubVector(2, 2);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testSetSubVectorOutOfBoundsArrayThrows() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        v.setSubVector(2, new double[]{4.0, 5.0});
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testSetSubVectorOutOfBoundsGenericThrows() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        v.setSubVector(2, new OpenMapRealVector(new double[]{4.0, 5.0}));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        ArrayRealVector vDiffVal = new ArrayRealVector(new double[]{1.0, 2.0, 4.0});
        ArrayRealVector vDiffDim = new ArrayRealVector(new double[]{1.0, 2.0});

        // Reflexivity
        assertEquals(v1, v1);

        // Symmetry
        assertEquals(v1, v2);
        assertEquals(v2, v1);
        assertEquals(v1.hashCode(), v2.hashCode());

        // Incompatible comparisons
        assertFalse(v1.equals(null));
        assertFalse(v1.equals("non-vector"));
        assertFalse(v1.equals(vDiffDim));
        assertFalse(v1.equals(vDiffVal));

        // Equals with other RealVector types
        RealVector svEqual = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        assertEquals(v1, svEqual);

        // NaN Equality Contract
        ArrayRealVector vNaN1 = new ArrayRealVector(new double[]{1.0, Double.NaN, 3.0});
        ArrayRealVector vNaN2 = new ArrayRealVector(new double[]{Double.NaN, 5.0, 6.0});
        ArrayRealVector vNaN3 = new ArrayRealVector(new double[]{1.0, Double.NaN});

        // Vectors with NaN coordinates are considered equal regardless of which index has NaN
        assertEquals(vNaN1, vNaN2);
        assertEquals(9, vNaN1.hashCode());
        assertEquals(9, vNaN2.hashCode());

        // Dimension difference between NaNs still returns false
        assertFalse(vNaN1.equals(vNaN3));

        // Comparing NaN vector with non-NaN vector
        assertFalse(v1.equals(vNaN1));
        assertFalse(vNaN1.equals(v1));
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        ArrayRealVector original = new ArrayRealVector(new double[]{12.34, 56.78, -90.12});

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertTrue(deserialized instanceof ArrayRealVector);
        assertEquals(original, deserialized);
        assertArrayEquals(original.getData(), ((ArrayRealVector) deserialized).getData(), EPS);
    }
}