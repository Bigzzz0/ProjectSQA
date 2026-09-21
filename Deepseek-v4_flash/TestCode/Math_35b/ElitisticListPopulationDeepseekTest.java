package org.apache.commons.math3.genetics;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - nextGeneration(): valid rates (0.0, 0.5, 1.0) with non-empty population.
 *   - getElitismRate() after construction and setElitismRate().
 *   - Constructor with chromosome list and valid rate.
 *   - Constructor with population limit and valid rate.
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Population limit: 0, 1, Integer.MAX_VALUE (via ListPopulation superclass).
 *   - Empty chromosome list with valid rate.
 *   - Elitism rate boundaries: 0.0, 1.0.
 * 
 * Partition C: Defect-Targeted Branch Zone (KNOWNDEFECT)
 *   - Constructors with elitismRate < 0 (e.g., -0.1) should throw OutOfRangeException.
 *   - Constructors with elitismRate > 1 (e.g., 1.1) should throw OutOfRangeException.
 *   - Bug: constructors do NOT validate rate, so these tests fail on the defective version.
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - setElitismRate() with invalid rates (<0 or >1) throws OutOfRangeException.
 *   - nextGeneration() with population limit violated? (ListPopulation tracks limit, but rate validation here).
 *   - Chromosome list constructor with populationLimit < list size (superclass throws).
 *   - Population limit constructor with negative limit (superclass throws? Actually ListPopulation throws NotPositiveException).
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Not applicable: no equals/hashCode override, no clone/serialization.
 */
public class ElitisticListPopulationDeepseekTest {

    // -----------------------------------------------------------------------
    // Helper class for testing – DummyChromosome
    // -----------------------------------------------------------------------
    private static class DummyChromosome extends Chromosome {
        private final double fitness;
        DummyChromosome(double fitness) { this.fitness = fitness; }
        @Override public double fitness() { return fitness; }
    }

    private List<Chromosome> createChromosomeList(double... fitnesses) {
        List<Chromosome> list = new ArrayList<>(fitnesses.length);
        for (double f : fitnesses) {
            list.add(new DummyChromosome(f));
        }
        return list;
    }

    // =======================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =======================================================================

    @Test(timeout = 4000)
    public void testNextGenerationWithRateHalf() {
        List<Chromosome> chromosomes = createChromosomeList(5.0, 3.0, 1.0, 4.0, 2.0);
        ElitisticListPopulation pop = new ElitisticListPopulation(chromosomes, 10, 0.5);
        Population nextGen = pop.nextGeneration();
        // With 5 chromosomes and rate 0.5:
        // boundIndex = ceil((1-0.5)*5) = ceil(2.5)=3
        // sorted descending: 5.0,4.0,3.0,2.0,1.0 -> last 2 (index3,4) => 2.0,1.0
        assertEquals(2, nextGen.getPopulationSize());
        assertTrue(nextGen instanceof ElitisticListPopulation);
        // verify the best two chromosomes were kept (but note: nextGen just has the "not good enough" ones? Actually the loop copies from boundIndex to end, which are the best ones because sorted ascending? Wait: Collections.sort sorts ascending by fitness (since Chromosome.compareTo uses fitness). So after sorting, the list is ascending: 1.0,2.0,3.0,4.0,5.0. Then boundIndex=3, so indices 3 and 4 are 4.0 and 5.0. So the two best are copied. Good.
    }

    @Test(timeout = 4000)
    public void testNextGenerationWithRateZero() {
        List<Chromosome> chromosomes = createChromosomeList(9.0, 8.0);
        ElitisticListPopulation pop = new ElitisticListPopulation(chromosomes, 5, 0.0);
        Population nextGen = pop.nextGeneration();
        // rate=0 => boundIndex = ceil(1*2)=2 => no elements copied
        assertEquals(0, nextGen.getPopulationSize());
    }

    @Test(timeout = 4000)
    public void testNextGenerationWithRateOne() {
        List<Chromosome> chromosomes = createChromosomeList(7.0, 6.0, 5.0);
        ElitisticListPopulation pop = new ElitisticListPopulation(chromosomes, 5, 1.0);
        Population nextGen = pop.nextGeneration();
        // rate=1 => boundIndex = ceil(0*3)=0 => all copied
        assertEquals(3, nextGen.getPopulationSize());
    }

    @Test(timeout = 4000)
    public void testGetElitismRateAfterConstruction() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.75);
        assertEquals(0.75, pop.getElitismRate(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSetElitismRateValid() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        pop.setElitismRate(0.3);
        assertEquals(0.3, pop.getElitismRate(), 0.0);
        pop.setElitismRate(0.0);
        assertEquals(0.0, pop.getElitismRate(), 0.0);
        pop.setElitismRate(1.0);
        assertEquals(1.0, pop.getElitismRate(), 0.0);
    }

    @Test(timeout = 4000)
    public void testConstructorWithChromosomesAndValidRate() {
        List<Chromosome> list = createChromosomeList(1.0);
        ElitisticListPopulation pop = new ElitisticListPopulation(list, 10, 0.9);
        assertEquals(1, pop.getPopulationSize());
        assertEquals(0.9, pop.getElitismRate(), 0.0);
    }

    @Test(timeout = 4000)
    public void testConstructorWithPopulationLimitAndValidRate() {
        ElitisticListPopulation pop = new ElitisticListPopulation(20, 0.2);
        assertEquals(0.2, pop.getElitismRate(), 0.0);
        assertEquals(0, pop.getPopulationSize());
        assertEquals(20, pop.getPopulationLimit());
    }

    // =======================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =======================================================================

    @Test(timeout = 4000)
    public void testEmptyChromosomeList() {
        ElitisticListPopulation pop = new ElitisticListPopulation(new ArrayList<Chromosome>(), 5, 0.5);
        assertEquals(0, pop.getPopulationSize());
        Population nextGen = pop.nextGeneration();
        assertEquals(0, nextGen.getPopulationSize());
    }

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.NotPositiveException.class)
    public void testPopulationLimitZeroInvalid() {
        // populationLimit=0 is invalid because ListPopulation requires limit > 0.
        new ElitisticListPopulation(0, 0.5);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.NotPositiveException.class)
    public void testPopulationLimitNegativeInvalid() {
        new ElitisticListPopulation(-5, 0.5);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.NumberIsTooLargeException.class)
    public void testChromosomeListExceedsLimit() {
        List<Chromosome> list = createChromosomeList(1.0, 2.0, 3.0);
        new ElitisticListPopulation(list, 2, 0.5);
    }

    // =======================================================================
    // Partition C: Defect-Targeted Branch Zone (KNOWN DEFECT)
    // These tests fail on the defective version because constructors
    // do not validate elitismRate.
    // =======================================================================

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.OutOfRangeException.class)
    public void testConstructorWithChromosomesRateTooHigh() {
        List<Chromosome> list = createChromosomeList(1.0);
        new ElitisticListPopulation(list, 10, 1.5);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.OutOfRangeException.class)
    public void testConstructorWithChromosomesRateTooLow() {
        List<Chromosome> list = createChromosomeList(1.0);
        new ElitisticListPopulation(list, 10, -0.1);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.OutOfRangeException.class)
    public void testConstructorWithLimitRateTooHigh() {
        new ElitisticListPopulation(10, 1.1);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.OutOfRangeException.class)
    public void testConstructorWithLimitRateTooLow() {
        new ElitisticListPopulation(10, -0.01);
    }

    // =======================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =======================================================================

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.OutOfRangeException.class)
    public void testSetElitismRateTooHigh() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        pop.setElitismRate(1.5);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.OutOfRangeException.class)
    public void testSetElitismRateTooLow() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        pop.setElitismRate(-0.5);
    }

    // Testing that nextGeneration works even with only one chromosome
    @Test(timeout = 4000)
    public void testNextGenerationSingleChromosome() {
        List<Chromosome> list = createChromosomeList(42.0);
        ElitisticListPopulation pop = new ElitisticListPopulation(list, 5, 0.0);
        Population nextGen = pop.nextGeneration();
        assertEquals(0, nextGen.getPopulationSize());

        pop = new ElitisticListPopulation(list, 5, 1.0);
        nextGen = pop.nextGeneration();
        assertEquals(1, nextGen.getPopulationSize());
    }

    // =======================================================================
    // Partition E: Object Lifecycle (minimal) – ensure no NPE
    // =======================================================================

    @Test(timeout = 4000)
    public void testToStringNotNull() {
        ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        assertNotNull(pop.toString());
    }

    // Additional edge: rate exactly 0.5 and odd number of chromosomes
    @Test(timeout = 4000)
    public void testNextGenerationBoundaryIndex() {
        // 3 chromosomes, rate 0.5 -> boundIndex = ceil(0.5*3)=ceil(1.5)=2 => last 1 copied
        List<Chromosome> list = createChromosomeList(3.0, 2.0, 1.0);
        ElitisticListPopulation pop = new ElitisticListPopulation(list, 10, 0.5);
        Population nextGen = pop.nextGeneration();
        assertEquals(1, nextGen.getPopulationSize());
        // sorted: 1.0,2.0,3.0 -> best is 3.0 at index2
        assertEquals(3.0, nextGen.getChromosomes().get(0).fitness(), 0.0);
    }
}