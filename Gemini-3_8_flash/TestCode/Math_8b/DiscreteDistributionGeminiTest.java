package org.apache.commons.math3.distribution;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.Pair;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET METHOD / BRANCH                              | TEST METHOD                         | INTENT
 * ----------------------------------------------------------------------------------------------------
 * DiscreteDistribution(List)                          | testDefaultRngConstructor()         | Verify Well19937c default RNG init
 * sample.getValue() < 0                               | testConstructorNegativeProbability()| Math NotPositiveException trigger
 * MathArrays.normalizeArray() (sum == 0)             | testConstructorZeroSumProbability() | MathArithmeticException trigger
 * MathArrays.normalizeArray() (infinite/NaN)          | testConstructorInfiniteProbability()| MathIllegalArgumentException
 * MathArrays.normalizeArray() (NaN probability)       | testConstructorNaNProbability()     | MathIllegalArgumentException
 * probability(T x) [x == null && elem == null]        | testProbabilityNullHandling()       | Match null target with null sample
 * probability(T x) [x != null && x.equals(elem)]      | testProbabilityCalculations()       | Accumulated probabilities on duplicate keys
 * probability(T x) [not found]                        | testProbabilityNotFound()           | Return 0.0 for absent target
 * getSamples()                                        | testGetSamplesIntegrity()           | Check normalized output pairs
 * sample() [randomValue < sum traversal]              | testSampleDeterministic()           | Well-seeded deterministic sampling
 * sample() [fallback to singletons.get(size - 1)]     | testSampleBoundaryFallback()        | Verify upper edge sampling behavior
 * sample(sampleSize <= 0)                             | testSampleSizeZeroOrNegative()      | NotStrictlyPositiveException check
 * sample(sampleSize) [Issue MATH-942 bug exposure]    | testIssue942ArrayStoreException()   | Expose ArrayStoreException on subtype polymorphism
 * reseedRandomGenerator(long)                         | testReseedRandomGenerator()         | Verify seed repeatability
 * ----------------------------------------------------------------------------------------------------
 */
public class DiscreteDistributionGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultRngConstructor() {
        final List<Pair<String, Double>> pmf = new ArrayList<Pair<String, Double>>();
        pmf.add(new Pair<String, Double>("single", 1.0));

        final DiscreteDistribution<String> distribution = new DiscreteDistribution<String>(pmf);
        assertNotNull(distribution);
        assertEquals("single", distribution.sample());
    }

    @Test(timeout = 4000)
    public void testProbabilityCalculations() {
        final List<Pair<Integer, Double>> pmf = new ArrayList<Pair<Integer, Double>>();
        pmf.add(new Pair<Integer, Double>(1, 1.0));
        pmf.add(new Pair<Integer, Double>(2, 2.0));
        pmf.add(new Pair<Integer, Double>(1, 3.0)); // Duplicate key: 1 (weight 1 + 3 = 4)
        pmf.add(new Pair<Integer, Double>(3, 4.0)); // Total sum = 10.0

        final DiscreteDistribution<Integer> distribution = new DiscreteDistribution<Integer>(pmf);

        assertEquals(0.40, distribution.probability(1), 1e-9);
        assertEquals(0.20, distribution.probability(2), 1e-9);
        assertEquals(0.40, distribution.probability(3), 1e-9);
        assertEquals(0.00, distribution.probability(99), 1e-9);
    }

    @Test(timeout = 4000)
    public void testGetSamplesIntegrity() {
        final List<Pair<String, Double>> pmf = new ArrayList<Pair<String, Double>>();
        pmf.add(new Pair<String, Double>("A", 2.0));
        pmf.add(new Pair<String, Double>("B", 8.0));

        final DiscreteDistribution<String> distribution = new DiscreteDistribution<String>(pmf);
        final List<Pair<String, Double>> returnedSamples = distribution.getSamples();

        assertEquals(2, returnedSamples.size());
        assertEquals("A", returnedSamples.get(0).getKey());
        assertEquals(0.2, returnedSamples.get(0).getValue(), 1e-9);
        assertEquals("B", returnedSamples.get(1).getKey());
        assertEquals(0.8, returnedSamples.get(1).getValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSampleDeterministic() {
        final List<Pair<String, Double>> pmf = new ArrayList<Pair<String, Double>>();
        pmf.add(new Pair<String, Double>("A", 0.5));
        pmf.add(new Pair<String, Double>("B", 0.5));

        final DiscreteDistribution<String> dist1 = new DiscreteDistribution<String>(new Well19937c(12345L), pmf);
        final DiscreteDistribution<String> dist2 = new DiscreteDistribution<String>(new Well19937c(12345L), pmf);

        for (int i = 0; i < 25; i++) {
            assertEquals(dist1.sample(), dist2.sample());
        }
    }

    @Test(timeout = 4000)
    public void testReseedRandomGenerator() {
        final List<Pair<Integer, Double>> pmf = new ArrayList<Pair<Integer, Double>>();
        pmf.add(new Pair<Integer, Double>(10, 0.3));
        pmf.add(new Pair<Integer, Double>(20, 0.7));

        final DiscreteDistribution<Integer> distribution = new DiscreteDistribution<Integer>(new Well19937c(100L), pmf);
        final int firstVal = distribution.sample();

        distribution.reseedRandomGenerator(100L);
        final int secondVal = distribution.sample();

        assertEquals(firstVal, secondVal);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testProbabilityNullHandling() {
        final List<Pair<String, Double>> pmf = new ArrayList<Pair<String, Double>>();
        pmf.add(new Pair<String, Double>(null, 2.0));
        pmf.add(new Pair<String, Double>("not-null", 3.0));

        final DiscreteDistribution<String> distribution = new DiscreteDistribution<String>(pmf);

        assertEquals(0.4, distribution.probability(null), 1e-9);
        assertEquals(0.6, distribution.probability("not-null"), 1e-9);
        assertEquals(0.0, distribution.probability("absent"), 1e-9);
    }

    @Test(timeout = 4000)
    public void testProbabilityNotFound() {
        final List<Pair<Integer, Double>> pmf = new ArrayList<Pair<Integer, Double>>();
        pmf.add(new Pair<Integer, Double>(1, 1.0));

        final DiscreteDistribution<Integer> distribution = new DiscreteDistribution<Integer>(pmf);

        assertEquals(0.0, distribution.probability(2), 1e-9);
        assertEquals(0.0, distribution.probability(null), 1e-9);
    }

    @Test(timeout = 4000)
    public void testSampleBoundaryFallback() {
        // Zero probability for the first element, full probability for the last element
        final List<Pair<String, Double>> pmf = new ArrayList<Pair<String, Double>>();
        pmf.add(new Pair<String, Double>("ZERO", 0.0));
        pmf.add(new Pair<String, Double>("LAST", 1.0));

        final DiscreteDistribution<String> distribution = new DiscreteDistribution<String>(pmf);

        for (int i = 0; i < 10; i++) {
            assertEquals("LAST", distribution.sample());
        }
    }

    @Test(timeout = 4000)
    public void testSingleElementSample() {
        final List<Pair<Double, Double>> pmf = new ArrayList<Pair<Double, Double>>();
        pmf.add(new Pair<Double, Double>(3.14159, 100.0));

        final DiscreteDistribution<Double> distribution = new DiscreteDistribution<Double>(pmf);
        final Double[] sampled = distribution.sample(5);

        assertEquals(5, sampled.length);
        for (Double val : sampled) {
            assertEquals(3.14159, val, 1e-9);
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defects4J Issue 942)
    // =========================================================================

    /**
     * Targets Commons-Math Issue 942 (Defects4J ground truth failure):
     * DiscreteDistribution.sample(int) creates array using singletons.get(0).getClass().
     * If elements are heterogeneous or polymorphic (e.g., Number, Object, interface),
     * and singletons.get(0) is a specific subtype (e.g. Integer), sampling another subtype
     * (e.g. Double) causes java.lang.ArrayStoreException when storing into the array.
     */
    @Test(timeout = 4000)
    public void testIssue942ArrayStoreException() {
        final List<Pair<Object, Double>> pmf = new ArrayList<Pair<Object, Double>>();
        // Element 0 has type String with 0 probability
        pmf.add(new Pair<Object, Double>("firstElementIsString", 0.0));
        // Element 1 has type Integer with 1.0 probability
        pmf.add(new Pair<Object, Double>(Integer.valueOf(42), 1.0));

        final DiscreteDistribution<Object> distribution = new DiscreteDistribution<Object>(pmf);

        // In the defective implementation: Array.newInstance(singletons.get(0).getClass(), sampleSize)
        // allocates a String[], but sample() returns an Integer, triggering ArrayStoreException.
        final Object[] samples = distribution.sample(1);

        assertNotNull(samples);
        assertEquals(1, samples.length);
        assertEquals(Integer.valueOf(42), samples[0]);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testConstructorNegativeProbability() {
        final List<Pair<String, Double>> pmf = new ArrayList<Pair<String, Double>>();
        pmf.add(new Pair<String, Double>("Valid", 1.0));
        pmf.add(new Pair<String, Double>("Invalid", -0.0001));

        new DiscreteDistribution<String>(pmf);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testConstructorZeroSumProbability() {
        final List<Pair<String, Double>> pmf = new ArrayList<Pair<String, Double>>();
        pmf.add(new Pair<String, Double>("Zero1", 0.0));
        pmf.add(new Pair<String, Double>("Zero2", 0.0));

        new DiscreteDistribution<String>(pmf);
    }

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testConstructorInfiniteProbability() {
        final List<Pair<String, Double>> pmf = new ArrayList<Pair<String, Double>>();
        pmf.add(new Pair<String, Double>("Inf", Double.POSITIVE_INFINITY));

        new DiscreteDistribution<String>(pmf);
    }

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testConstructorNaNProbability() {
        final List<Pair<String, Double>> pmf = new ArrayList<Pair<String, Double>>();
        pmf.add(new Pair<String, Double>("NaN", Double.NaN));

        new DiscreteDistribution<String>(pmf);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testSampleSizeZero() {
        final List<Pair<String, Double>> pmf = new ArrayList<Pair<String, Double>>();
        pmf.add(new Pair<String, Double>("Item", 1.0));

        final DiscreteDistribution<String> distribution = new DiscreteDistribution<String>(pmf);
        distribution.sample(0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testSampleSizeNegative() {
        final List<Pair<String, Double>> pmf = new ArrayList<Pair<String, Double>>();
        pmf.add(new Pair<String, Double>("Item", 1.0));

        final DiscreteDistribution<String> distribution = new DiscreteDistribution<String>(pmf);
        distribution.sample(-5);
    }
}