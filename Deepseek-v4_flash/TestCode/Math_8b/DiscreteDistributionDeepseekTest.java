package org.apache.commons.math3.distribution;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructor normalization: probabilities array normalized to sum 1.0
 *   - probability() method: null handling, equality comparison, cumulative probability for duplicate keys
 *   - sample() method: random double selection, boundary conditions at sum edges
 *   - sample(int) method: array creation using generic type from first singleton
 *   - getSamples() returns correct pairs
 *   - reseedRandomGenerator produces deterministic output
 * 
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - Empty samples list
 *   - Single element with probability 1.0
 *   - Probabilities summing exactly to 1.0 vs needing normalization (normalization bug potential)
 *   - Random value exactly 0.0 and just below sum thresholds
 *   - Zero probability entries (valid but affects sampling)
 *   - Negative probabilities → NotPositiveException
 *   - sample(0) → NotStrictlyPositiveException
 *   - sample(-1) → NotStrictlyPositiveException
 *   - sample() when probabilities have floating point summation issues (fallback case)
 * 
 * Partition C: Defect-Targeted Branch Zone (Defects4J issue 942)
 *   - ArrayStoreException when distribution contains anonymous/inner class instances
 *   - Bug: sample(int) uses singletons.get(0) to determine array type, but may fail when
 *     the first element's runtime class differs from actual type parameter due to erasure
 *   - Test: create distribution with instances of a local class, ensure sample(int) works
 *   - Also test when first singleton is null → NPE in sample(int)
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Null samples list
 *   - Null pairs in list
 *   - Null keys or null values in pairs
 *   - Infinite probability values
 *   - Non-positive sample size
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Deterministic reseeding → same sample sequence
 *   - Multiple calls to getSamples → independent list copies
 *   - probability() consistency with constructor input
 */

public class DiscreteDistributionDeepseekTest {

    // Helper: creates a Pair for cleaner test code
    private static <T> Pair<T, Double> p(T key, Double value) {
        return new Pair<T, Double>(key, value);
    }

    // ===== Partition A: Core Functional Logic =====

    @Test(timeout = 4000)
    public void testProbability() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(p("A", 0.25));
        samples.add(p("B", 0.25));
        samples.add(p("C", 0.25));
        samples.add(p("D", 0.25));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        assertEquals(0.25, dist.probability("A"), 1e-15);
        assertEquals(0.25, dist.probability("B"), 1e-15);
        assertEquals(0.25, dist.probability("C"), 1e-15);
        assertEquals(0.25, dist.probability("D"), 1e-15);
        assertEquals(0.0, dist.probability("E"), 1e-15);
    }

    @Test(timeout = 4000)
    public void testProbabilityWithDuplicateKeys() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(p("X", 0.3));
        samples.add(p("X", 0.2));
        samples.add(p("Y", 0.5));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        // Probability for "X" should be sum of both entries (0.5 after normalization)
        assertEquals(0.5, dist.probability("X"), 1e-15);
        assertEquals(0.5, dist.probability("Y"), 1e-15);
    }

    @Test(timeout = 4000)
    public void testProbabilityWithNullKey() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(p(null, 0.5));
        samples.add(p("B", 0.5));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        assertEquals(0.5, dist.probability(null), 1e-15);
        assertEquals(0.5, dist.probability("B"), 1e-15);
        assertEquals(0.0, dist.probability("A"), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSampleDeterministic() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(p("A", 0.5));
        samples.add(p("B", 0.5));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        dist.reseedRandomGenerator(12345L);
        
        // First call should produce deterministic result
        String first = dist.sample();
        dist.reseedRandomGenerator(12345L);
        String second = dist.sample();
        
        assertEquals(first, second);
    }

    @Test(timeout = 4000)
    public void testGetSamples() {
        List<Pair<Integer, Double>> samples = new ArrayList<Pair<Integer, Double>>();
        samples.add(p(1, 0.2));
        samples.add(p(2, 0.8));
        
        DiscreteDistribution<Integer> dist = new DiscreteDistribution<Integer>(samples);
        List<Pair<Integer, Double>> result = dist.getSamples();
        
        assertEquals(2, result.size());
        assertEquals(Integer.valueOf(1), result.get(0).getKey());
        assertEquals(0.2, result.get(0).getValue(), 1e-15);
        assertEquals(Integer.valueOf(2), result.get(1).getKey());
        assertEquals(0.8, result.get(1).getValue(), 1e-15);
    }

    // ===== Partition B: Boundary Value Analysis =====

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testNegativeProbability() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(p("A", -0.1));
        new DiscreteDistribution<String>(samples);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testZeroSumProbability() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(p("A", 0.0));
        samples.add(p("B", 0.0));
        new DiscreteDistribution<String>(samples);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testSampleNonPositive() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(p("A", 1.0));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        dist.sample(0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testSampleNegative() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(p("A", 1.0));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        dist.sample(-1);
    }

    @Test(timeout = 4000)
    public void testSampleWithSingleElement() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(p("Only", 1.0));
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        // Always returns the same element
        for (int i = 0; i < 100; i++) {
            assertEquals("Only", dist.sample());
        }
    }

    @Test(timeout = 4000)
    public void testSampleWithMultipleSamples() {
        List<Pair<Integer, Double>> samples = new ArrayList<Pair<Integer, Double>>();
        samples.add(p(1, 0.25));
        samples.add(p(2, 0.25));
        samples.add(p(3, 0.25));
        samples.add(p(4, 0.25));
        
        DiscreteDistribution<Integer> dist = new DiscreteDistribution<Integer>(samples);
        Integer[] result = dist.sample(1000);
        
        assertEquals(1000, result.length);
        for (int i = 0; i < 1000; i++) {
            assertTrue(result[i] >= 1 && result[i] <= 4);
        }
    }

    // ===== Partition C: Defect-Targeted Branch Zone (Defects4J issue 942) =====

    @Test(timeout = 4000)
    public void testSampleWithAnonymousClass() {
        // This test targets the ArrayStoreException described in Defects4J issue 942.
        // The bug occurs when sample(int) tries to create an array using the class of
        // the first singleton (which may be an anonymous class) rather than the 
        // compile-time type parameter.
        
        // Create a simple anonymous class that doesn't cause issues
        Object obj = new Object() {
            @Override
            public String toString() {
                return "anonymous";
            }
        };
        
        List<Pair<Object, Double>> samples = new ArrayList<Pair<Object, Double>>();
        samples.add(p(obj, 0.5));
        samples.add(p("String", 0.5));
        
        DiscreteDistribution<Object> dist = new DiscreteDistribution<Object>(samples);
        
        // This should not throw ArrayStoreException
        Object[] result = dist.sample(10);
        assertEquals(10, result.length);
    }

    @Test(timeout = 4000)
    public void testSampleWithAnonymousClassAsOnlyType() {
        // Edge case: all singletons are anonymous class instances
        // This can cause ArrayStoreException in the buggy version
        
        class LocalClass {
            int value;
            LocalClass(int v) { value = v; }
        }
        
        LocalClass a = new LocalClass(1);
        LocalClass b = new LocalClass(2);
        
        List<Pair<LocalClass, Double>> samples = new ArrayList<Pair<LocalClass, Double>>();
        samples.add(p(a, 0.5));
        samples.add(p(b, 0.5));
        
        DiscreteDistribution<LocalClass> dist = new DiscreteDistribution<LocalClass>(samples);
        
        // This should not throw
        LocalClass[] result = dist.sample(5);
        assertEquals(5, result.length);
    }

    @Test(timeout = 4000)
    public void testSampleWithInnerClass() {
        // Inner class as the first element triggers ArrayStoreException in the buggy version
        
        class Inner {
            String name;
            Inner(String n) { name = n; }
        }
        
        Inner first = new Inner("first");
        String second = "second";
        
        List<Pair<Object, Double>> samples = new ArrayList<Pair<Object, Double>>();
        samples.add(p(first, 0.5));
        samples.add(p(second, 0.5));
        
        DiscreteDistribution<Object> dist = new DiscreteDistribution<Object>(samples);
        
        // Should not throw ArrayStoreException
        Object[] result = dist.sample(10);
        assertEquals(10, result.length);
    }

    @Test(timeout = 4000)
    public void testSampleWhenFirstSingletonIsNull() {
        // Edge case: first singleton is null
        // This would cause NPE in sample(int) on buggy version accessing getClass()
        
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(p(null, 0.5));
        samples.add(p("B", 0.5));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        
        // Fallback case: sample() should handle null first singleton correctly
        // But sample(int) may fail. If it fails, we verify the expected exception.
        try {
            String[] result = dist.sample(5);
            assertEquals(5, result.length);
            // If it succeeds, verify we didn't get ArrayStoreException
        } catch (ArrayStoreException e) {
            // This is the known defect behavior - acceptable for bug detection
            fail("ArrayStoreException thrown - defect detected!");
        } catch (NullPointerException e) {
            // Also a defect symptom from first element being null
            fail("NullPointerException thrown - defect detected!");
        }
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testInfiniteProbability() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(p("A", Double.POSITIVE_INFINITY));
        new DiscreteDistribution<String>(samples);
    }

    @Test(timeout = 4000)
    public void testReseedRandomGenerator() {
        List<Pair<Integer, Double>> samples = new ArrayList<Pair<Integer, Double>>();
        samples.add(p(1, 0.5));
        samples.add(p(2, 0.5));
        
        DiscreteDistribution<Integer> dist1 = new DiscreteDistribution<Integer>(samples);
        DiscreteDistribution<Integer> dist2 = new DiscreteDistribution<Integer>(samples);
        
        dist1.reseedRandomGenerator(42L);
        dist2.reseedRandomGenerator(42L);
        
        // Both should produce identical sequence
        for (int i = 0; i < 10; i++) {
            assertEquals(dist1.sample(), dist2.sample());
        }
    }

    @Test(timeout = 4000)
    public void testNormalizedProbabilities() {
        // Sum is 0.3 + 0.3 = 0.6, should normalize to 0.5 and 0.5
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(p("A", 0.3));
        samples.add(p("B", 0.3));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        List<Pair<String, Double>> result = dist.getSamples();
        
        // After normalization, probabilities should be 0.5 each
        assertEquals(0.5, result.get(0).getValue(), 1e-15);
        assertEquals(0.5, result.get(1).getValue(), 1e-15);
    }

    // ===== Partition E: Object Lifecycle & Contract =====

    @Test(timeout = 4000)
    public void testGetSamplesIndependentCopy() {
        List<Pair<String, Double>> samples = new ArrayList<Pair<String, Double>>();
        samples.add(p("A", 0.5));
        samples.add(p("B", 0.5));
        
        DiscreteDistribution<String> dist = new DiscreteDistribution<String>(samples);
        List<Pair<String, Double>> result1 = dist.getSamples();
        List<Pair<String, Double>> result2 = dist.getSamples();
        
        // They should be independent lists
        result1.remove(0);
        assertEquals(2, result2.size()); // result2 unaffected
    }
}