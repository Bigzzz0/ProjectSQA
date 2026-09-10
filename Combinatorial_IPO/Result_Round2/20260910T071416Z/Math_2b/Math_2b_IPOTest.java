package org.apache.commons.math3.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

/** Generated from approved defect-focused scenarios using native IPO. */
public class Math_2b_IPOTest {

    @Test(timeout = 4000)
    public void test_hypergeometric_mean_001() throws Exception {
        // Native IPO combination: population=100000, successes=50, sample=10
        int population = 100000;
        int successes = 50;
        int sample = 10;
        HypergeometricDistribution distribution = new HypergeometricDistribution(population, successes, sample);
        double expected = sample * (successes / (double) population);
        assertEquals(expected, distribution.getNumericalMean(), 0.0d);
        assertTrue(distribution.getNumericalMean() >= 0.0d);
    }

    @Test(timeout = 4000)
    public void test_hypergeometric_mean_002() throws Exception {
        // Native IPO combination: population=43130568, successes=50, sample=50
        int population = 43130568;
        int successes = 50;
        int sample = 50;
        HypergeometricDistribution distribution = new HypergeometricDistribution(population, successes, sample);
        double expected = sample * (successes / (double) population);
        assertEquals(expected, distribution.getNumericalMean(), 0.0d);
        assertTrue(distribution.getNumericalMean() >= 0.0d);
    }

    @Test(timeout = 4000)
    public void test_hypergeometric_mean_003() throws Exception {
        // Native IPO combination: population=43130568, successes=42976365, sample=10
        int population = 43130568;
        int successes = 42976365;
        int sample = 10;
        HypergeometricDistribution distribution = new HypergeometricDistribution(population, successes, sample);
        double expected = sample * (successes / (double) population);
        assertEquals(expected, distribution.getNumericalMean(), 0.0d);
        assertTrue(distribution.getNumericalMean() >= 0.0d);
    }

    @Test(timeout = 4000)
    public void test_hypergeometric_mean_004() throws Exception {
        // Native IPO combination: population=50000000, successes=50, sample=100000
        int population = 50000000;
        int successes = 50;
        int sample = 100000;
        HypergeometricDistribution distribution = new HypergeometricDistribution(population, successes, sample);
        double expected = sample * (successes / (double) population);
        assertEquals(expected, distribution.getNumericalMean(), 0.0d);
        assertTrue(distribution.getNumericalMean() >= 0.0d);
    }

    @Test(timeout = 4000)
    public void test_hypergeometric_mean_005() throws Exception {
        // Native IPO combination: population=50000000, successes=42976365, sample=50
        int population = 50000000;
        int successes = 42976365;
        int sample = 50;
        HypergeometricDistribution distribution = new HypergeometricDistribution(population, successes, sample);
        double expected = sample * (successes / (double) population);
        assertEquals(expected, distribution.getNumericalMean(), 0.0d);
        assertTrue(distribution.getNumericalMean() >= 0.0d);
    }

    @Test(timeout = 4000)
    public void test_hypergeometric_mean_006() throws Exception {
        // Native IPO combination: population=50000000, successes=45000000, sample=10
        int population = 50000000;
        int successes = 45000000;
        int sample = 10;
        HypergeometricDistribution distribution = new HypergeometricDistribution(population, successes, sample);
        double expected = sample * (successes / (double) population);
        assertEquals(expected, distribution.getNumericalMean(), 0.0d);
        assertTrue(distribution.getNumericalMean() >= 0.0d);
    }

    @Test(timeout = 4000)
    public void test_hypergeometric_mean_007() throws Exception {
        // Native IPO combination: population=43130568, successes=42976365, sample=100000
        int population = 43130568;
        int successes = 42976365;
        int sample = 100000;
        HypergeometricDistribution distribution = new HypergeometricDistribution(population, successes, sample);
        double expected = sample * (successes / (double) population);
        assertEquals(expected, distribution.getNumericalMean(), 0.0d);
        assertTrue(distribution.getNumericalMean() >= 0.0d);
    }

    @Test(timeout = 4000)
    public void test_hypergeometric_mean_008() throws Exception {
        // Native IPO combination: population=100000, successes=50, sample=50
        int population = 100000;
        int successes = 50;
        int sample = 50;
        HypergeometricDistribution distribution = new HypergeometricDistribution(population, successes, sample);
        double expected = sample * (successes / (double) population);
        assertEquals(expected, distribution.getNumericalMean(), 0.0d);
        assertTrue(distribution.getNumericalMean() >= 0.0d);
    }

    @Test(timeout = 4000)
    public void test_hypergeometric_mean_009() throws Exception {
        // Native IPO combination: population=100000, successes=50, sample=100000
        int population = 100000;
        int successes = 50;
        int sample = 100000;
        HypergeometricDistribution distribution = new HypergeometricDistribution(population, successes, sample);
        double expected = sample * (successes / (double) population);
        assertEquals(expected, distribution.getNumericalMean(), 0.0d);
        assertTrue(distribution.getNumericalMean() >= 0.0d);
    }

    @Test(timeout = 4000)
    public void test_hypergeometric_mean_010() throws Exception {
        // Native IPO combination: population=50000000, successes=45000000, sample=50
        int population = 50000000;
        int successes = 45000000;
        int sample = 50;
        HypergeometricDistribution distribution = new HypergeometricDistribution(population, successes, sample);
        double expected = sample * (successes / (double) population);
        assertEquals(expected, distribution.getNumericalMean(), 0.0d);
        assertTrue(distribution.getNumericalMean() >= 0.0d);
    }

    @Test(timeout = 4000)
    public void test_hypergeometric_mean_011() throws Exception {
        // Native IPO combination: population=50000000, successes=45000000, sample=100000
        int population = 50000000;
        int successes = 45000000;
        int sample = 100000;
        HypergeometricDistribution distribution = new HypergeometricDistribution(population, successes, sample);
        double expected = sample * (successes / (double) population);
        assertEquals(expected, distribution.getNumericalMean(), 0.0d);
        assertTrue(distribution.getNumericalMean() >= 0.0d);
    }

    @Test(timeout = 4000)
    public void test_hypergeometric_mean_012() throws Exception {
        // Native IPO combination: population=43130568, successes=42976365, sample=50
        int population = 43130568;
        int successes = 42976365;
        int sample = 50;
        HypergeometricDistribution distribution = new HypergeometricDistribution(population, successes, sample);
        double expected = sample * (successes / (double) population);
        assertEquals(expected, distribution.getNumericalMean(), 0.0d);
        assertTrue(distribution.getNumericalMean() >= 0.0d);
    }

}
