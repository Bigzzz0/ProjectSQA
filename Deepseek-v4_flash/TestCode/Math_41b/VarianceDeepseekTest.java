package org.apache.commons.math.stat.descriptive.moment;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Default constructor (isBiasCorrected = true)
 *   - Constructor with SecondMoment (incMoment = false)
 *   - Constructor with boolean isBiasCorrected
 *   - Constructor with boolean and SecondMoment
 *   - Copy constructor
 *   - increment() with incMoment = true/false
 *   - getResult() with n=0, n=1, n>1 (bias corrected and uncorrected)
 *   - clear() with incMoment = true/false
 *   - getN()
 *   - isBiasCorrected() / setBiasCorrected()
 *   - copy() / copy(static)
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - evaluate(double[]) with null, empty, single element, multiple elements
 *   - evaluate(double[], int, int) with various begin/length combinations
 *   - evaluate(double[], double, int, int) with precomputed mean
 *   - evaluate(double[], double) with precomputed mean
 *   - evaluate(double[], double[], int, int) weighted
 *   - evaluate(double[], double[]) weighted
 *   - evaluate(double[], double[], double, int, int) weighted with precomputed mean
 *   - evaluate(double[], double[], double) weighted with precomputed mean
 *   - Edge cases: length=0, length=1, length>1
 *   - Large values, negative values, zero values
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - KNOWN DEFECT: testEvaluateArraySegmentWeighted
 *     Expected: 1.6644508338125354 but got: 0.31909161062727365
 *     Root cause: In evaluate(double[], double[], double, int, int) method,
 *     the sumWts calculation iterates over entire weights array instead of
 *     the specified segment [begin, begin+length). This causes incorrect
 *     denominator when computing weighted variance on a subarray.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - NullArgumentException for null arrays
 *   - IllegalArgumentException for invalid indices
 *   - NullArgumentException in copy() for null source/dest
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Copy constructor preserves state
 *   - copy() method preserves state
 *   - Static copy preserves state
 *   - Multiple increment/getResult cycles
 *   - State isolation between instances
 */

public class VarianceDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        Variance v = new Variance();
        assertTrue("Default should be bias corrected", v.isBiasCorrected());
        assertTrue("incMoment should be true", v.incMoment);
        assertNotNull("moment should not be null", v.moment);
        assertEquals("getN should be 0", 0L, v.getN());
        assertEquals("getResult should be NaN", Double.NaN, v.getResult(), 0.0);
    }

    @Test(timeout = 4000)
    public void testConstructorWithSecondMoment() {
        SecondMoment m2 = new SecondMoment();
        Variance v = new Variance(m2);
        assertTrue("Default should be bias corrected", v.isBiasCorrected());
        assertFalse("incMoment should be false", v.incMoment);
        assertSame("moment should be the same object", m2, v.moment);
    }

    @Test(timeout = 4000)
    public void testConstructorWithBiasCorrectedFalse() {
        Variance v = new Variance(false);
        assertFalse("isBiasCorrected should be false", v.isBiasCorrected());
        assertTrue("incMoment should be true", v.incMoment);
    }

    @Test(timeout = 4000)
    public void testConstructorWithBiasCorrectedAndSecondMoment() {
        SecondMoment m2 = new SecondMoment();
        Variance v = new Variance(false, m2);
        assertFalse("isBiasCorrected should be false", v.isBiasCorrected());
        assertFalse("incMoment should be false", v.incMoment);
        assertSame("moment should be the same object", m2, v.moment);
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() {
        Variance original = new Variance(false);
        original.increment(1.0);
        original.increment(2.0);
        Variance copy = new Variance(original);
        assertEquals("Copy should have same result", original.getResult(), copy.getResult(), 1e-15);
        assertEquals("Copy should have same N", original.getN(), copy.getN());
        assertEquals("Copy should have same bias correction", original.isBiasCorrected(), copy.isBiasCorrected());
    }

    @Test(timeout = 4000)
    public void testIncrementWithIncMomentTrue() {
        Variance v = new Variance();
        v.increment(1.0);
        assertEquals("N should be 1", 1L, v.getN());
        assertEquals("Result should be 0 for single value", 0.0, v.getResult(), 0.0);
    }

    @Test(timeout = 4000)
    public void testIncrementWithIncMomentFalse() {
        SecondMoment m2 = new SecondMoment();
        Variance v = new Variance(m2);
        v.increment(1.0); // Should do nothing
        assertEquals("N should be 0", 0L, v.getN());
        assertEquals("Result should be NaN", Double.NaN, v.getResult(), 0.0);
        // Increment via moment directly
        m2.increment(1.0);
        m2.increment(2.0);
        assertEquals("N should be 2", 2L, v.getN());
        double expected = 0.5; // (1-1.5)^2 + (2-1.5)^2 / (2-1) = 0.25+0.25 = 0.5
        assertEquals("Result should be 0.5", expected, v.getResult(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetResultWithZeroN() {
        Variance v = new Variance();
        assertEquals("Result should be NaN for n=0", Double.NaN, v.getResult(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetResultWithOneN() {
        Variance v = new Variance();
        v.increment(5.0);
        assertEquals("Result should be 0 for n=1", 0.0, v.getResult(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetResultWithMultipleValuesBiasCorrected() {
        Variance v = new Variance();
        v.increment(1.0);
        v.increment(2.0);
        v.increment(3.0);
        // mean = 2, deviations: -1, 0, 1 => sum squares = 2, n-1 = 2 => var = 1
        assertEquals("Bias corrected variance should be 1.0", 1.0, v.getResult(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetResultWithMultipleValuesBiasUncorrected() {
        Variance v = new Variance(false);
        v.increment(1.0);
        v.increment(2.0);
        v.increment(3.0);
        // mean = 2, deviations: -1, 0, 1 => sum squares = 2, n = 3 => var = 2/3
        assertEquals("Uncorrected variance should be 2/3", 2.0/3.0, v.getResult(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testClearWithIncMomentTrue() {
        Variance v = new Variance();
        v.increment(1.0);
        v.increment(2.0);
        v.clear();
        assertEquals("N should be 0 after clear", 0L, v.getN());
        assertEquals("Result should be NaN after clear", Double.NaN, v.getResult(), 0.0);
    }

    @Test(timeout = 4000)
    public void testClearWithIncMomentFalse() {
        SecondMoment m2 = new SecondMoment();
        Variance v = new Variance(m2);
        m2.increment(1.0);
        m2.increment(2.0);
        v.clear(); // Should do nothing since incMoment is false
        assertEquals("N should still be 2", 2L, v.getN());
    }

    @Test(timeout = 4000)
    public void testGetN() {
        Variance v = new Variance();
        assertEquals("Initial N should be 0", 0L, v.getN());
        v.increment(1.0);
        assertEquals("N should be 1", 1L, v.getN());
        v.increment(2.0);
        assertEquals("N should be 2", 2L, v.getN());
    }

    @Test(timeout = 4000)
    public void testIsBiasCorrectedAndSetBiasCorrected() {
        Variance v = new Variance();
        assertTrue("Default should be bias corrected", v.isBiasCorrected());
        v.setBiasCorrected(false);
        assertFalse("Should be uncorrected after set", v.isBiasCorrected());
        v.setBiasCorrected(true);
        assertTrue("Should be corrected again", v.isBiasCorrected());
    }

    @Test(timeout = 4000)
    public void testCopyMethod() {
        Variance original = new Variance(false);
        original.increment(10.0);
        original.increment(20.0);
        Variance copy = original.copy();
        assertEquals("Copy should have same result", original.getResult(), copy.getResult(), 1e-15);
        assertEquals("Copy should have same N", original.getN(), copy.getN());
        assertEquals("Copy should have same bias correction", original.isBiasCorrected(), copy.isBiasCorrected());
        // Verify independence
        original.increment(30.0);
        assertNotEquals("Original should change after increment", original.getN(), copy.getN());
    }

    @Test(timeout = 4000)
    public void testStaticCopy() {
        Variance source = new Variance(false);
        source.increment(5.0);
        source.increment(7.0);
        Variance dest = new Variance();
        Variance.copy(source, dest);
        assertEquals("Dest should have same result", source.getResult(), dest.getResult(), 1e-15);
        assertEquals("Dest should have same N", source.getN(), dest.getN());
        assertEquals("Dest should have same bias correction", source.isBiasCorrected(), dest.isBiasCorrected());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testEvaluateNullArray() {
        Variance v = new Variance();
        try {
            v.evaluate((double[]) null);
            fail("Should throw NullArgumentException");
        } catch (NullArgumentException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testEvaluateEmptyArray() {
        Variance v = new Variance();
        double result = v.evaluate(new double[0]);
        assertEquals("Empty array should return NaN", Double.NaN, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testEvaluateSingleElement() {
        Variance v = new Variance();
        double result = v.evaluate(new double[]{42.0});
        assertEquals("Single element should return 0", 0.0, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testEvaluateMultipleElements() {
        Variance v = new Variance();
        double result = v.evaluate(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
        // mean = 3, deviations: -2, -1, 0, 1, 2 => sum squares = 10, n-1 = 4 => var = 2.5
        assertEquals("Variance of 1..5 should be 2.5", 2.5, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testEvaluateWithBeginAndLength() {
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        double result = v.evaluate(values, 1, 3); // {2.0, 3.0, 4.0}
        // mean = 3, deviations: -1, 0, 1 => sum squares = 2, n-1 = 2 => var = 1
        assertEquals("Variance of {2,3,4} should be 1.0", 1.0, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testEvaluateWithBeginAndLengthZero() {
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0};
        double result = v.evaluate(values, 0, 0);
        assertEquals("Zero length should return NaN", Double.NaN, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testEvaluateWithBeginAndLengthOne() {
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0};
        double result = v.evaluate(values, 1, 1); // {2.0}
        assertEquals("Single element should return 0", 0.0, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testEvaluateWithPrecomputedMean() {
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        double result = v.evaluate(values, 3.0, 0, 5);
        // mean = 3, deviations: -2, -1, 0, 1, 2 => sum squares = 10, n-1 = 4 => var = 2.5
        assertEquals("Variance with precomputed mean should be 2.5", 2.5, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testEvaluateWithPrecomputedMeanSingleElement() {
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0};
        double result = v.evaluate(values, 2.0, 1, 1); // {2.0} with mean 2.0
        assertEquals("Single element should return 0", 0.0, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testEvaluateWithPrecomputedMeanZeroLength() {
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0};
        double result = v.evaluate(values, 2.0, 0, 0);
        assertEquals("Zero length should return NaN", Double.NaN, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testEvaluateWithPrecomputedMeanFullArray() {
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        double result = v.evaluate(values, 3.0);
        assertEquals("Variance with precomputed mean should be 2.5", 2.5, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testEvaluateWeightedFullArray() {
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0, 4.0};
        double[] weights = {1.0, 1.0, 1.0, 1.0};
        double result = v.evaluate(values, weights);
        // mean = 2.5, deviations: -1.5, -0.5, 0.5, 1.5 => weighted sum squares = 5.0
        // sumWts = 4, denominator = 3 => var = 5/3 ≈ 1.6666667
        assertEquals("Weighted variance with equal weights should be 5/3", 5.0/3.0, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testEvaluateWeightedWithBeginAndLength() {
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        double[] weights = {1.0, 2.0, 1.0, 2.0, 1.0};
        double result = v.evaluate(values, weights, 1, 3); // {2.0, 3.0, 4.0} with weights {2.0, 1.0, 2.0}
        // weighted mean = (2*2 + 1*3 + 2*4) / (2+1+2) = (4+3+8)/5 = 15/5 = 3
        // deviations: -1, 0, 1 => weighted sum squares = 2*1 + 1*0 + 2*1 = 4
        // sumWts = 5, denominator = 4 => var = 4/4 = 1.0
        assertEquals("Weighted variance of segment should be 1.0", 1.0, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testEvaluateWeightedWithPrecomputedMean() {
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0};
        double[] weights = {1.0, 2.0, 1.0};
        double result = v.evaluate(values, weights, 2.0, 0, 3);
        // weighted mean = 2, deviations: -1, 0, 1 => weighted sum squares = 1*1 + 2*0 + 1*1 = 2
        // sumWts = 4, denominator = 3 => var = 2/3 ≈ 0.6666667
        assertEquals("Weighted variance with precomputed mean should be 2/3", 2.0/3.0, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testEvaluateWeightedWithPrecomputedMeanFullArray() {
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0};
        double[] weights = {1.0, 2.0, 1.0};
        double result = v.evaluate(values, weights, 2.0);
        assertEquals("Weighted variance with precomputed mean should be 2/3", 2.0/3.0, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testEvaluateWeightedSingleElement() {
        Variance v = new Variance();
        double[] values = {5.0};
        double[] weights = {3.0};
        double result = v.evaluate(values, weights);
        assertEquals("Single element weighted should return 0", 0.0, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testEvaluateWeightedZeroLength() {
        Variance v = new Variance();
        double[] values = {1.0, 2.0};
        double[] weights = {1.0, 1.0};
        double result = v.evaluate(values, weights, 0, 0);
        assertEquals("Zero length weighted should return NaN", Double.NaN, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testEvaluateWithLargeValues() {
        Variance v = new Variance();
        double[] values = {1e10, 1e10 + 1, 1e10 + 2};
        double result = v.evaluate(values);
        // mean ≈ 1e10 + 1, deviations: -1, 0, 1 => sum squares = 2, n-1 = 2 => var = 1
        assertEquals("Variance of large numbers should be 1.0", 1.0, result, 1e-5);
    }

    @Test(timeout = 4000)
    public void testEvaluateWithNegativeValues() {
        Variance v = new Variance();
        double[] values = {-5.0, -3.0, -1.0};
        double result = v.evaluate(values);
        // mean = -3, deviations: -2, 0, 2 => sum squares = 8, n-1 = 2 => var = 4
        assertEquals("Variance of negative numbers should be 4.0", 4.0, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testEvaluateWithAllSameValues() {
        Variance v = new Variance();
        double[] values = {7.0, 7.0, 7.0, 7.0};
        double result = v.evaluate(values);
        assertEquals("Variance of identical values should be 0", 0.0, result, 0.0);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testEvaluateArraySegmentWeighted() {
        // This test targets the known defect: sumWts calculation iterates over
        // entire weights array instead of the specified segment [begin, begin+length)
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        double[] weights = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
        
        // Evaluate on segment [2, 5) i.e., values {3.0, 4.0, 5.0} with weights {1.0, 1.0, 1.0}
        // weighted mean = 4.0, deviations: -1, 0, 1 => weighted sum squares = 1+0+1 = 2
        // sumWts should be 3 (only segment weights), denominator = 2 => var = 1.0
        // BUG: sumWts iterates over entire array (length 10) => sumWts = 10, denominator = 9 => var = 2/9 ≈ 0.2222
        double result = v.evaluate(values, weights, 2, 3);
        assertEquals("Weighted variance of segment should be 1.0", 1.0, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testEvaluateArraySegmentWeightedWithNonUniformWeights() {
        // More complex case to expose the bug
        Variance v = new Variance();
        double[] values = {10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0};
        double[] weights = {2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0, 2.0};
        
        // Evaluate on segment [1, 4) i.e., values {20.0, 30.0, 40.0} with weights {2.0, 2.0, 2.0}
        // weighted mean = (40+60+80)/(2+2+2) = 180/6 = 30
        // deviations: -10, 0, 10 => weighted sum squares = 2*100 + 2*0 + 2*100 = 400
        // sumWts should be 6, denominator = 5 => var = 400/5 = 80.0
        // BUG: sumWts = 16, denominator = 15 => var = 400/15 ≈ 26.6667
        double result = v.evaluate(values, weights, 1, 3);
        assertEquals("Weighted variance of segment should be 80.0", 80.0, result, 1e-12);
    }

    @Test(timeout = 4000)
    public void testEvaluateArraySegmentWeightedWithPrecomputedMean() {
        // Test the same bug in the overload with precomputed mean
        Variance v = new Variance();
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0};
        double[] weights = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
        
        // Evaluate on segment [3, 5) i.e., values {4.0, 5.0} with weights {1.0, 1.0}
        // weighted mean = 4.5, deviations: -0.5, 0.5 => weighted sum squares = 0.25 + 0.25 = 0.5
        // sumWts should be 2, denominator = 1 => var = 0.5
        // BUG: sumWts = 8, denominator = 7 => var = 0.5/7 ≈ 0.0714
        double result = v.evaluate(values, weights, 4.5, 3, 2);
        assertEquals("Weighted variance of segment with precomputed mean should be 0.5", 0.5, result, 1e-15);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testEvaluateNullArrayWithBeginLength() {
        Variance v = new Variance();
        v.evaluate(null, 0, 1);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testEvaluateNullArrayWithMean() {
        Variance v = new Variance();
        v.evaluate(null, 0.0);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testEvaluateNullArrayWithMeanBeginLength() {
        Variance v = new Variance();
        v.evaluate(null, 0.0, 0, 1);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testEvaluateWeightedNullValues() {
        Variance v = new Variance();
        v.evaluate(null, new double[]{1.0});
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testEvaluateWeightedNullWeights() {
        Variance v = new Variance();
        v.evaluate(new double[]{1.0}, null);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testEvaluateWeightedNullValuesWithBeginLength() {
        Variance v = new Variance();
        v.evaluate(null, new double[]{1.0}, 0, 1);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testEvaluateWeightedNullWeightsWithBeginLength() {
        Variance v = new Variance();
        v.evaluate(new double[]{1.0}, null, 0, 1);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testEvaluateWeightedNullValuesWithMean() {
        Variance v = new Variance();
        v.evaluate(null, new double[]{1.0}, 0.0);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testEvaluateWeightedNullWeightsWithMean() {
        Variance v = new Variance();
        v.evaluate(new double[]{1.0}, null, 0.0);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testEvaluateWeightedNullValuesWithMeanBeginLength() {
        Variance v = new Variance();
        v.evaluate(null, new double[]{1.0}, 0.0, 0, 1);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testEvaluateWeightedNullWeightsWithMeanBeginLength() {
        Variance v = new Variance();
        v.evaluate(new double[]{1.0}, null, 0.0, 0, 1);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testStaticCopyWithNullSource() {
        Variance.copy(null, new Variance());
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testStaticCopyWithNullDest() {
        Variance.copy(new Variance(), null);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testStateIsolationBetweenInstances() {
        Variance v1 = new Variance();
        Variance v2 = new Variance();
        v1.increment(1.0);
        v1.increment(2.0);
        assertEquals("v1 should have N=2", 2L, v1.getN());
        assertEquals("v2 should have N=0", 0L, v2.getN());
        assertEquals("v2 result should be NaN", Double.NaN, v2.getResult(), 0.0);
    }

    @Test(timeout = 4000)
    public void testMultipleIncrementGetResultCycles() {
        Variance v = new Variance();
        v.increment(1.0);
        v.increment(2.0);
        assertEquals("First cycle variance should be 0.5", 0.5, v.getResult(), 1e-15);
        v.clear();
        assertEquals("After clear N should be 0", 0L, v.getN());
        v.increment(10.0);
        v.increment(20.0);
        v.increment(30.0);
        assertEquals("Second cycle variance should be 100.0", 100.0, v.getResult(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testCopyPreservesState() {
        Variance v = new Variance(false);
        v.increment(1.0);
        v.increment(2.0);
        v.increment(3.0);
        Variance copy = v.copy();
        assertEquals("Original and copy should have same result", v.getResult(), copy.getResult(), 1e-15);
        assertEquals("Original and copy should have same N", v.getN(), copy.getN());
        assertEquals("Original and copy should have same bias correction", v.isBiasCorrected(), copy.isBiasCorrected());
        // Modify original
        v.increment(4.0);
        assertNotEquals("Copy should be independent", v.getN(), copy.getN());
    }

    @Test(timeout = 4000)
    public void testBiasCorrectedToggleAffectsResult() {
        Variance v = new Variance();
        v.increment(1.0);
        v.increment(2.0);
        v.increment(3.0);
        double corrected = v.getResult(); // 1.0
        v.setBiasCorrected(false);
        double uncorrected = v.getResult(); // 2/3 ≈ 0.6667
        assertEquals("Corrected should be 1.0", 1.0, corrected, 1e-15);
        assertEquals("Uncorrected should be 2/3", 2.0/3.0, uncorrected, 1e-15);
    }

    @Test(timeout = 4000)
    public void testEvaluateDoesNotAffectInternalState() {
        Variance v = new Variance();
        v.increment(1.0);
        v.increment(2.0);
        double internalBefore = v.getResult();
        double evalResult = v.evaluate(new double[]{10.0, 20.0, 30.0});
        double internalAfter = v.getResult();
        assertEquals("Internal state should not change after evaluate", internalBefore, internalAfter, 1e-15);
        assertNotEquals("Evaluate result should differ from internal", internalBefore, evalResult, 1e-15);
    }

    @Test(timeout = 4000)
    public void testEvaluateWeightedDoesNotAffectInternalState() {
        Variance v = new Variance();
        v.increment(1.0);
        v.increment(2.0);
        double internalBefore = v.getResult();
        double evalResult = v.evaluate(new double[]{10.0, 20.0, 30.0}, new double[]{1.0, 1.0, 1.0});
        double internalAfter = v.getResult();
        assertEquals("Internal state should not change after weighted evaluate", internalBefore, internalAfter, 1e-15);
    }
}