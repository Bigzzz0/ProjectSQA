package org.apache.commons.math3.genetics;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;

/*
 * [Branch & Defect Analysis Matrix]
 * ------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.math3.genetics.ElitisticListPopulation
 * 
 * Identified Defect (Defects4J):
 * - ElitisticListPopulation(List<Chromosome>, int, double) and ElitisticListPopulation(int, double)
 *   fail to validate the `elitismRate` parameter upon construction, allowing rates outside [0, 1]
 *   (e.g., negative or > 1.0) without throwing OutOfRangeException.
 *
 * Decision / Condition Coverage Targets:
 * 1. Constructor (chromosomes, populationLimit, elitismRate):
 *    - elitismRate < 0.0 -> Must throw OutOfRangeException
 *    - elitismRate > 1.0 -> Must throw OutOfRangeException
 *    - 0.0 <= elitismRate <= 1.0 -> Successfully created with proper rate
 * 2. Constructor (populationLimit, elitismRate):
 *    - elitismRate < 0.0 -> Must throw OutOfRangeException
 *    - elitismRate > 1.0 -> Must throw OutOfRangeException
 *    - 0.0 <= elitismRate <= 1.0 -> Successfully created with proper rate
 * 3. Method setElitismRate(double):
 *    - elitismRate < 0.0 -> Throws OutOfRangeException
 *    - elitismRate > 1.0 -> Throws OutOfRangeException
 *    - elitismRate == 0.0 -> Success (Boundary: Lower)
 *    - elitismRate == 1.0 -> Success (Boundary: Upper)
 *    - 0.0 < elitismRate < 1.0 -> Success (Interior)
 * 4. Method nextGeneration():
 *    - elitismRate = 0.0 -> 0 chromosomes retained
 *    - elitismRate = 1.0 -> All chromosomes retained
 *    - 0.0 < elitismRate < 1.0 -> Exactly ceil((1 - rate) * size) to size copied in ascending fitness order
 *    - Verify state transition: new instance created with identical limit and elitism rate
 * ------------------------------------------------------------------------------------------------
 */
public class ElitisticListPopulationGeminiTest {

    private static final double DELTA = 1e-9;

    /**
     * Concrete chromosome implementation for deterministic testing.
     */
    private static class DummyChromosome extends Chromosome {
        private final double fitness;

        DummyChromosome(final double fitness) {
            this.fitness = fitness;
        }

        @Override
        public double getFitness() {
            return this.fitness;
        }

        @Override
        protected boolean isSame(final Chromosome another) {
            if (!(another instanceof DummyChromosome)) {
                return false;
            }
            return Double.compare(this.fitness, ((DummyChromosome) another).fitness) == 0;
        }

        @Override
        public String toString() {
            return "DummyChromosome(" + fitness + ")";
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testValidConstructorWithCapacity() {
        final int limit = 100;
        final double rate = 0.25;
        final ElitisticListPopulation pop = new ElitisticListPopulation(limit, rate);

        assertEquals(limit, pop.getPopulationLimit());
        assertEquals(rate, pop.getElitismRate(), DELTA);
        assertTrue(pop.getChromosomes().isEmpty());
    }

    @Test(timeout = 4000)
    public void testValidConstructorWithList() {
        final List<Chromosome> list = new ArrayList<Chromosome>();
        list.add(new DummyChromosome(10.0));
        list.add(new DummyChromosome(20.0));

        final int limit = 10;
        final double rate = 0.35;
        final ElitisticListPopulation pop = new ElitisticListPopulation(list, limit, rate);

        assertEquals(limit, pop.getPopulationLimit());
        assertEquals(rate, pop.getElitismRate(), DELTA);
        assertEquals(2, pop.getChromosomes().size());
    }

    @Test(timeout = 4000)
    public void testSetAndGetElitismRate() {
        final ElitisticListPopulation pop = new ElitisticListPopulation(50, 0.5);
        assertEquals(0.5, pop.getElitismRate(), DELTA);

        pop.setElitismRate(0.85);
        assertEquals(0.85, pop.getElitismRate(), DELTA);

        pop.setElitismRate(0.0);
        assertEquals(0.0, pop.getElitismRate(), DELTA);

        pop.setElitismRate(1.0);
        assertEquals(1.0, pop.getElitismRate(), DELTA);
    }

    @Test(timeout = 4000)
    public void testNextGenerationPreservesBestChromosomes() {
        final ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.3);
        for (int i = 1; i <= 10; i++) {
            pop.addChromosome(new DummyChromosome(i * 1.0));
        }

        // boundIndex = ceil((1.0 - 0.3) * 10) = ceil(7.0) = 7
        // Chromosomes sorted ascending: 1.0, 2.0, ..., 10.0
        // Indices 7, 8, 9 correspond to fitness 8.0, 9.0, 10.0 (3 chromosomes)
        final Population nextGen = pop.nextGeneration();
        assertTrue(nextGen instanceof ElitisticListPopulation);

        final ElitisticListPopulation elitisticNextGen = (ElitisticListPopulation) nextGen;
        assertEquals(pop.getPopulationLimit(), elitisticNextGen.getPopulationLimit());
        assertEquals(pop.getElitismRate(), elitisticNextGen.getElitismRate(), DELTA);

        final List<Chromosome> survivors = elitisticNextGen.getChromosomes();
        assertEquals(3, survivors.size());
        assertEquals(8.0, survivors.get(0).getFitness(), DELTA);
        assertEquals(9.0, survivors.get(1).getFitness(), DELTA);
        assertEquals(10.0, survivors.get(2).getFitness(), DELTA);
    }

    @Test(timeout = 4000)
    public void testNextGenerationWithUnsortedInput() {
        final List<Chromosome> initial = new ArrayList<Chromosome>();
        initial.add(new DummyChromosome(50.0));
        initial.add(new DummyChromosome(10.0));
        initial.add(new DummyChromosome(100.0));
        initial.add(new DummyChromosome(25.0));

        final ElitisticListPopulation pop = new ElitisticListPopulation(initial, 10, 0.5);
        // boundIndex = ceil((1.0 - 0.5) * 4) = 2
        // Sorted: 10.0, 25.0, 50.0, 100.0 -> indices 2 and 3 copied (50.0 and 100.0)
        final Population nextGen = pop.nextGeneration();
        final List<Chromosome> survivors = ((ElitisticListPopulation) nextGen).getChromosomes();

        assertEquals(2, survivors.size());
        assertEquals(50.0, survivors.get(0).getFitness(), DELTA);
        assertEquals(100.0, survivors.get(1).getFitness(), DELTA);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testBoundaryElitismRateZeroNextGeneration() {
        final ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.0);
        for (int i = 0; i < 5; i++) {
            pop.addChromosome(new DummyChromosome(i + 1));
        }

        // boundIndex = ceil((1.0 - 0.0) * 5) = 5 -> loop from 5 to 5 transfers 0 chromosomes
        final Population nextGen = pop.nextGeneration();
        assertTrue(nextGen.getChromosomes().isEmpty());
    }

    @Test(timeout = 4000)
    public void testBoundaryElitismRateOneNextGeneration() {
        final ElitisticListPopulation pop = new ElitisticListPopulation(10, 1.0);
        for (int i = 0; i < 5; i++) {
            pop.addChromosome(new DummyChromosome(i + 1));
        }

        // boundIndex = ceil((1.0 - 1.0) * 5) = 0 -> transfers all 5 chromosomes
        final Population nextGen = pop.nextGeneration();
        assertEquals(5, nextGen.getChromosomes().size());
        for (int i = 0; i < 5; i++) {
            assertEquals((double) (i + 1), nextGen.getChromosomes().get(i).getFitness(), DELTA);
        }
    }

    @Test(timeout = 4000)
    public void testNextGenerationWithCeilFractional() {
        final ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.25);
        for (int i = 1; i <= 3; i++) {
            pop.addChromosome(new DummyChromosome(i * 10.0));
        }

        // size = 3, rate = 0.25 -> (1 - 0.25) * 3 = 0.75 * 3 = 2.25
        // ceil(2.25) = 3 -> loop 3 to 3 transfers 0 chromosomes
        final Population nextGen = pop.nextGeneration();
        assertEquals(0, nextGen.getChromosomes().size());
    }

    @Test(timeout = 4000)
    public void testNextGenerationWithEmptyPopulation() {
        final ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        final Population nextGen = pop.nextGeneration();
        assertTrue(nextGen.getChromosomes().isEmpty());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testConstructorTooLow() {
        // Constructor (limit, rate) must fail when rate < 0
        new ElitisticListPopulation(10, -0.1);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testConstructorTooHigh() {
        // Constructor (limit, rate) must fail when rate > 1.0
        new ElitisticListPopulation(10, 1.0001);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testChromosomeListConstructorTooLow() {
        // Constructor (chromosomes, limit, rate) must fail when rate < 0
        final List<Chromosome> list = Collections.singletonList((Chromosome) new DummyChromosome(1.0));
        new ElitisticListPopulation(list, 10, -0.0001);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testChromosomeListConstructorTooHigh() {
        // Constructor (chromosomes, limit, rate) must fail when rate > 1.0
        final List<Chromosome> list = Collections.singletonList((Chromosome) new DummyChromosome(1.0));
        new ElitisticListPopulation(list, 10, 1.05);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testSetElitismRateNegative() {
        final ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        pop.setElitismRate(-1e-6);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testSetElitismRateGreaterThanOne() {
        final ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        pop.setElitismRate(1.0000001);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testSetElitismRateExtremelyLarge() {
        final ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        pop.setElitismRate(Double.POSITIVE_INFINITY);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testSetElitismRateExtremelyNegative() {
        final ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.5);
        pop.setElitismRate(Double.NEGATIVE_INFINITY);
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testConstructorNonPositiveLimit() {
        new ElitisticListPopulation(0, 0.5);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testConstructorListExceedsLimit() {
        final List<Chromosome> list = new ArrayList<Chromosome>();
        list.add(new DummyChromosome(1.0));
        list.add(new DummyChromosome(2.0));
        new ElitisticListPopulation(list, 1, 0.5);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testFittestChromosomeRetrieval() {
        final ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.2);
        pop.addChromosome(new DummyChromosome(10.0));
        pop.addChromosome(new DummyChromosome(50.0));
        pop.addChromosome(new DummyChromosome(30.0));

        final Chromosome fittest = pop.getFittestChromosome();
        assertNotNull(fittest);
        assertEquals(50.0, fittest.getFitness(), DELTA);
    }

    @Test(timeout = 4000)
    public void testPopulationLimitMutation() {
        final ElitisticListPopulation pop = new ElitisticListPopulation(10, 0.2);
        pop.setPopulationLimit(20);
        assertEquals(20, pop.getPopulationLimit());

        final Population nextGen = pop.nextGeneration();
        assertEquals(20, nextGen.getPopulationLimit());
    }

    @Test(timeout = 4000)
    public void testIteratorIntegrity() {
        final ElitisticListPopulation pop = new ElitisticListPopulation(5, 0.5);
        pop.addChromosome(new DummyChromosome(1.0));
        pop.addChromosome(new DummyChromosome(2.0));

        int count = 0;
        for (Chromosome c : pop) {
            assertNotNull(c);
            count++;
        }
        assertEquals(2, count);
    }
}