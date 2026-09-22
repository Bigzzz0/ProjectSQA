package org.apache.commons.math.stat.descriptive.moment;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.exception.NullArgumentException;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math.stat.descriptive.moment.Variance
 *
 * 1. Defect Root Cause (Defects4J - VarianceTest::testEvaluateArraySegmentWeighted):
 *    - In evaluate(double[] values, double[] weights, double mean, int begin, int length),
 *      sumWts was accumulated by looping over the entire weights array (0 to weights.length)
 *      rather than the subarray slice (begin to begin + length).
 *    - Targeting tests:
 *      * testEvaluateArraySegmentWeighted(): evaluates a slice of values and weights (begin > 0,
 *        length < weights.length) with bias correction (default).
 *      * testEvaluateArraySegmentWeightedUnbiasedAndBiased(): checks slice evaluation with both
 *        isBiasCorrected=true and isBiasCorrected=false against expected analytical calculations.
 *
 * 2. Coverage & Branch Analysis:
 *    - Constructors:
 *      * Default Variance() (biasCorrected=true, incMoment=true)
 *      * Variance(SecondMoment m2) (biasCorrected=true, incMoment=false)
 *      * Variance(boolean isBiasCorrected) (custom bias, incMoment=true)
 *      * Variance(boolean isBiasCorrected, SecondMoment m2) (custom bias, incMoment=false)
 *      * Variance(Variance original) (copy constructor)
 *    - State Mutations & Incremental Processing:
 *      * increment(double): incMoment=true vs incMoment=false (no-op)
 *      * clear(): incMoment=true vs incMoment=false (no-op)
 *      * getN(): delegates to SecondMoment.getN()
 *      * getResult(): moment.n == 0 (NaN), n == 1 (0.0), n > 1 with isBiasCorrected=true / false
 *    - Static / Dynamic Evaluations:
 *      * evaluate(double[]): null check (NullArgumentException)
 *      * evaluate(double[], int, int): length=0 (NaN), length=1 (0.0), length>1 (2-pass mean)
 *      * evaluate(double[], double): delegator to (values, mean, 0, length)
 *      * evaluate(double[], double, int, int): length=0 (NaN), length=1 (0.0), length>1 (bias=true/false)
 *      * evaluate(double[], double[]): delegator to (values, weights, 0, length)
 *      * evaluate(double[], double[], int, int): length=0 (NaN), length=1 (0.0), length>1
 *      * evaluate(double[], double[], double): delegator to (values, weights, mean, 0, length)
 *      * evaluate(double[], double[], double, int, int): length=0 (NaN), length=1 (0.0), length>1
 *    - Exception / Guard Paths:
 *      * null values or null weights
 *      * negative / infinite / NaN weights
 *      * mismatched array lengths
 *      * invalid begin / length indices (begin < 0, length < 0, begin + length > array.length)
 *      * copy(null, dest), copy(src, null)
 *    - Object Contracts & State Integrity:
 *      * copy() and copy(src, dest)
 *      * setBiasCorrected() toggling
 */
public class VarianceGeminiTest {

    private static final double TOLERANCE = 1e-12;

    // =========================================================================
    // Partition A: Core Functional Logic & Incremental Calculations
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyAndSingleValueState() {
        Variance v = new Variance();
        assertTrue(v.isBiasCorrected());
        assertEquals(0L, v.getN());
        assertTrue(Double.isNaN(v.getResult()));

        v.increment(42.0);
        assertEquals(1L, v.getN());
        assertEquals(0.0, v.getResult(), TOLERANCE);

        v.clear();
        assertEquals(0L, v.getN());
        assertTrue(Double.isNaN(v.getResult()));
    }

    @Test(timeout = 4000)
    public void testIncrementalSampleVariance() {
        Variance v = new Variance(); // default isBiasCorrected = true
        double[] values = { 1.0, 2.0, 4.0, 7.0, 11.0 };
        for (double val : values) {
            v.increment(val);
        }
        assertEquals(5L, v.getN());

        // Sample mean = 5.0
        // Deviations: -4, -3, -1, 2, 6 -> SqDev: 16, 9, 1, 4, 36 -> Sum = 66
        // Unbiased sample variance = 66 / (5 - 1) = 16.5
        assertEquals(16.5, v.getResult(), TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testIncrementalPopulationVariance() {
        Variance v = new Variance(false); // isBiasCorrected = false
        assertFalse(v.isBiasCorrected());

        double[] values = { 1.0, 2.0, 4.0, 7.0, 11.0 };
        for (double val : values) {
            v.increment(val);
        }
        // Population variance = 66 / 5 = 13.2
        assertEquals(13.2, v.getResult(), TOLERANCE);

        // Toggle bias correction flag dynamically
        v.setBiasCorrected(true);
        assertTrue(v.isBiasCorrected());
        assertEquals(16.5, v.getResult(), TOLERANCE);

        v.setBiasCorrected(false);
        assertFalse(v.isBiasCorrected());
        assertEquals(13.2, v.getResult(), TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testExternalSecondMomentInteraction() {
        SecondMoment m2 = new SecondMoment();
        Variance v = new Variance(m2);

        // increment() on v must be a no-op when external SecondMoment is used
        v.increment(100.0);
        assertEquals(0L, v.getN());
        assertTrue(Double.isNaN(v.getResult()));

        // clear() on v must also be a no-op
        m2.increment(10.0);
        m2.increment(20.0);
        assertEquals(2L, v.getN());
        assertEquals(50.0, v.getResult(), TOLERANCE); // (10-15)^2 + (20-15)^2 = 25+25 = 50 / (2-1) = 50

        v.clear(); // no-op on internal moment
        assertEquals(2L, v.getN());

        m2.clear();
        assertEquals(0L, v.getN());
    }

    @Test(timeout = 4000)
    public void testExternalSecondMomentWithBiasFlagConstructor() {
        SecondMoment m2 = new SecondMoment();
        Variance v = new Variance(false, m2);
        assertFalse(v.isBiasCorrected());

        m2.increment(10.0);
        m2.increment(20.0);
        // Population variance = 50 / 2 = 25.0
        assertEquals(25.0, v.getResult(), TOLERANCE);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Direct Array Evaluation
    // =========================================================================

    @Test(timeout = 4000)
    public void testEvaluateArrayEmptyAndSingle() {
        Variance v = new Variance();
        double[] empty = new double[0];
        assertTrue(Double.isNaN(v.evaluate(empty)));
        assertTrue(Double.isNaN(v.evaluate(empty, 0, 0)));
        assertTrue(Double.isNaN(v.evaluate(empty, 1.0)));
        assertTrue(Double.isNaN(v.evaluate(empty, 1.0, 0, 0)));

        double[] single = new double[] { 42.0 };
        assertEquals(0.0, v.evaluate(single), TOLERANCE);
        assertEquals(0.0, v.evaluate(single, 0, 1), TOLERANCE);
        assertEquals(0.0, v.evaluate(single, 42.0), TOLERANCE);
        assertEquals(0.0, v.evaluate(single, 42.0, 0, 1), TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testEvaluateArrayWithPrecomputedMean() {
        Variance v = new Variance();
        double[] values = { 2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0 };
        // Mean = 40 / 8 = 5.0
        double mean = 5.0;
        // Deviations: -3, -1, -1, -1, 0, 0, 2, 4 -> Sq: 9, 1, 1, 1, 0, 0, 4, 16 -> sum = 32
        // Sample variance = 32 / (8 - 1) = 32 / 7 = 4.571428571428571
        double expectedSampleVar = 32.0 / 7.0;
        assertEquals(expectedSampleVar, v.evaluate(values), TOLERANCE);
        assertEquals(expectedSampleVar, v.evaluate(values, mean), TOLERANCE);
        assertEquals(expectedSampleVar, v.evaluate(values, mean, 0, values.length), TOLERANCE);

        // Test with population variance (bias corrected = false)
        v.setBiasCorrected(false);
        double expectedPopVar = 32.0 / 8.0; // 4.0
        assertEquals(expectedPopVar, v.evaluate(values), TOLERANCE);
        assertEquals(expectedPopVar, v.evaluate(values, mean), TOLERANCE);
        assertEquals(expectedPopVar, v.evaluate(values, mean, 0, values.length), TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testEvaluateSubarray() {
        Variance v = new Variance();
        double[] values = { 999.0, 2.0, 4.0, 6.0, 888.0 };
        // Subarray: 2.0, 4.0, 6.0 (begin = 1, length = 3)
        // Mean = 4.0, Dev: -2, 0, 2 -> Sq: 4, 0, 4 -> sum = 8
        // Sample var = 8 / (3 - 1) = 4.0
        assertEquals(4.0, v.evaluate(values, 1, 3), TOLERANCE);
        assertEquals(4.0, v.evaluate(values, 4.0, 1, 3), TOLERANCE);

        v.setBiasCorrected(false);
        // Pop var = 8 / 3
        assertEquals(8.0 / 3.0, v.evaluate(values, 1, 3), TOLERANCE);
        assertEquals(8.0 / 3.0, v.evaluate(values, 4.0, 1, 3), TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testEvaluateWeightedWholeArray() {
        Variance v = new Variance();
        double[] values = { 1.0, 2.0, 3.0 };
        double[] weights = { 1.0, 2.0, 3.0 };

        // Weighted Mean = (1*1 + 2*2 + 3*3) / (1+2+3) = 14 / 6 = 7/3
        // sumWts = 6
        // Dev: 1 - 7/3 = -4/3; 2 - 7/3 = -1/3; 3 - 7/3 = 2/3
        // w * dev^2: 1 * 16/9 = 16/9; 2 * 1/9 = 2/9; 3 * 4/9 = 12/9
        // accum = 30/9 = 10/3
        // w * dev: 1 * -4/3 = -4/3; 2 * -1/3 = -2/3; 3 * 2/3 = 6/3 -> sum = 0 -> accum2 = 0
        // Sample weighted variance = (10/3) / (6 - 1) = (10/3) / 5 = 2/3
        double expectedWeightedVar = 2.0 / 3.0;

        assertEquals(expectedWeightedVar, v.evaluate(values, weights), TOLERANCE);
        assertEquals(expectedWeightedVar, v.evaluate(values, weights, 0, values.length), TOLERANCE);
        assertEquals(expectedWeightedVar, v.evaluate(values, weights, 7.0 / 3.0), TOLERANCE);
        assertEquals(expectedWeightedVar, v.evaluate(values, weights, 7.0 / 3.0, 0, values.length), TOLERANCE);

        // Biased (population) weighted variance = (10/3) / 6 = 10/18 = 5/9
        v.setBiasCorrected(false);
        double expectedPopWeightedVar = 5.0 / 9.0;
        assertEquals(expectedPopWeightedVar, v.evaluate(values, weights), TOLERANCE);
        assertEquals(expectedPopWeightedVar, v.evaluate(values, weights, 0, values.length), TOLERANCE);
        assertEquals(expectedPopWeightedVar, v.evaluate(values, weights, 7.0 / 3.0), TOLERANCE);
        assertEquals(expectedPopWeightedVar, v.evaluate(values, weights, 7.0 / 3.0, 0, values.length), TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testEvaluateWeightedSingleAndEmpty() {
        Variance v = new Variance();
        double[] emptyVals = new double[0];
        double[] emptyWeights = new double[0];
        assertTrue(Double.isNaN(v.evaluate(emptyVals, emptyWeights)));
        assertTrue(Double.isNaN(v.evaluate(emptyVals, emptyWeights, 0, 0)));
        assertTrue(Double.isNaN(v.evaluate(emptyVals, emptyWeights, 1.0)));
        assertTrue(Double.isNaN(v.evaluate(emptyVals, emptyWeights, 1.0, 0, 0)));

        double[] singleVal = new double[] { 5.0 };
        double[] singleWeight = new double[] { 2.0 };
        assertEquals(0.0, v.evaluate(singleVal, singleWeight), TOLERANCE);
        assertEquals(0.0, v.evaluate(singleVal, singleWeight, 0, 1), TOLERANCE);
        assertEquals(0.0, v.evaluate(singleVal, singleWeight, 5.0), TOLERANCE);
        assertEquals(0.0, v.evaluate(singleVal, singleWeight, 5.0, 0, 1), TOLERANCE);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Subarray Weights Sum)
    // =========================================================================

    /**
     * Targets the defect where `sumWts` was computed over the entire `weights` array
     * (0 to weights.length) rather than the segment (begin to begin + length).
     */
    @Test(timeout = 4000)
    public void testEvaluateArraySegmentWeighted() {
        Variance v = new Variance();

        // Arrays padded at the boundaries
        double[] values =  { 999.0, 1.0, 2.0, 3.0, 888.0 };
        double[] weights = { 100.0, 1.0, 2.0, 3.0, 200.0 };

        // Evaluate segment: values [1.0, 2.0, 3.0] with weights [1.0, 2.0, 3.0]
        // Segment sum of weights = 1.0 + 2.0 + 3.0 = 6.0
        // Expected weighted sample variance = 2.0 / 3.0 = 0.6666666666666666
        // If buggy, sumWts = 100 + 1 + 2 + 3 + 200 = 306, yielding ~0.0109
        double actual = v.evaluate(values, weights, 1, 3);
        assertEquals(2.0 / 3.0, actual, TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testEvaluateArraySegmentWeightedWithPrecomputedMean() {
        Variance v = new Variance();

        double[] values =  { 500.0, 1.0, 2.0, 3.0, 600.0 };
        double[] weights = {  50.0, 1.0, 2.0, 3.0,  70.0 };
        double weightedMean = 7.0 / 3.0; // Mean of the segment

        // Sample variance on segment:
        assertEquals(2.0 / 3.0, v.evaluate(values, weights, weightedMean, 1, 3), TOLERANCE);

        // Population variance on segment:
        v.setBiasCorrected(false);
        assertEquals(5.0 / 9.0, v.evaluate(values, weights, weightedMean, 1, 3), TOLERANCE);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testEvaluateNullValues() {
        Variance v = new Variance();
        v.evaluate((double[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEvaluateNullValuesWithIndices() {
        Variance v = new Variance();
        v.evaluate(null, 0, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEvaluateNullWeights() {
        Variance v = new Variance();
        v.evaluate(new double[] { 1.0, 2.0 }, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEvaluateMismatchedWeightsLength() {
        Variance v = new Variance();
        v.evaluate(new double[] { 1.0, 2.0 }, new double[] { 1.0 });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEvaluateNegativeWeight() {
        Variance v = new Variance();
        v.evaluate(new double[] { 1.0, 2.0 }, new double[] { 1.0, -0.5 });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEvaluateNaNWeight() {
        Variance v = new Variance();
        v.evaluate(new double[] { 1.0, 2.0 }, new double[] { 1.0, Double.NaN });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEvaluateInfiniteWeight() {
        Variance v = new Variance();
        v.evaluate(new double[] { 1.0, 2.0 }, new double[] { 1.0, Double.POSITIVE_INFINITY });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEvaluateInvalidSubarrayIndicesNegativeBegin() {
        Variance v = new Variance();
        v.evaluate(new double[] { 1.0, 2.0 }, -1, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEvaluateInvalidSubarrayIndicesNegativeLength() {
        Variance v = new Variance();
        v.evaluate(new double[] { 1.0, 2.0 }, 0, -1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEvaluateInvalidSubarrayIndicesOutOfBounds() {
        Variance v = new Variance();
        v.evaluate(new double[] { 1.0, 2.0 }, 1, 2);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testCopyNullSource() {
        Variance.copy(null, new Variance());
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testCopyNullDest() {
        Variance.copy(new Variance(), null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCopyConstructorAndClone() {
        Variance v1 = new Variance(false);
        v1.increment(2.0);
        v1.increment(4.0);
        v1.increment(6.0);

        // Test copy constructor
        Variance v2 = new Variance(v1);
        assertEquals(v1.getN(), v2.getN());
        assertEquals(v1.getResult(), v2.getResult(), TOLERANCE);
        assertEquals(v1.isBiasCorrected(), v2.isBiasCorrected());

        // Test copy() method
        Variance v3 = v1.copy();
        assertEquals(v1.getN(), v3.getN());
        assertEquals(v1.getResult(), v3.getResult(), TOLERANCE);
        assertEquals(v1.isBiasCorrected(), v3.isBiasCorrected());

        // Ensure deep copy of internal moment: mutating v1 should not mutate v2 or v3
        v1.increment(8.0);
        assertEquals(4L, v1.getN());
        assertEquals(3L, v2.getN());
        assertEquals(3L, v3.getN());
    }

    @Test(timeout = 4000)
    public void testStaticCopyMethod() {
        Variance src = new Variance(false);
        src.increment(10.0);
        src.increment(30.0);

        Variance dest = new Variance(true);
        Variance.copy(src, dest);

        assertEquals(src.getN(), dest.getN());
        assertEquals(src.getResult(), dest.getResult(), TOLERANCE);
        assertFalse(dest.isBiasCorrected());
    }
}