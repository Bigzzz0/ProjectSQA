/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Class Under Test: org.apache.commons.math3.genetics.ListPopulation
 * ----------------------------------------------------------------------------------------------------
 * Methods & Decision Branches Targeted:
 * 1. ListPopulation(int populationLimit)
 *    - Delegates to ListPopulation(emptyList, populationLimit)
 *    - Branch: populationLimit <= 0 -> NotPositiveException
 *    - Branch: populationLimit > 0  -> Success, empty population
 *
 * 2. ListPopulation(List<Chromosome> chromosomes, int populationLimit)
 *    - Branch: chromosomes == null -> NullArgumentException
 *    - Branch: populationLimit <= 0 (0, -1) -> NotPositiveException
 *    - Branch: chromosomes.size() > populationLimit -> NumberIsTooLargeException
 *    - Branch: chromosomes.size() <= populationLimit -> Success, state correctly copied
 *
 * 3. setChromosomes(List<Chromosome> chromosomes)
 *    - Branch: chromosomes == null -> NullArgumentException
 *    - Branch: chromosomes.size() > populationLimit -> NumberIsTooLargeException
 *    - Branch: chromosomes.size() <= populationLimit -> Success, clears and adds all
 *
 * 4. addChromosomes(Collection<Chromosome> chromosomeColl)
 *    - Branch: (size + coll.size()) > populationLimit -> NumberIsTooLargeException
 *    - Branch: (size + coll.size()) <= populationLimit -> Success, adds all
 *
 * 5. getChromosomes() & getChromosomeList()
 *    - getChromosomes(): Returns unmodifiable view; modifications must throw UnsupportedOperationException
 *    - getChromosomeList(): Protected access returns internal list
 *
 * 6. addChromosome(Chromosome chromosome)
 *    - Branch: size >= populationLimit -> NumberIsTooLargeException
 *    - Branch: size < populationLimit  -> Success, chromosome appended
 *
 * 7. getFittestChromosome()
 *    - Branch: bestChromosome initial (index 0) vs subsequent comparisons
 *    - Branch: chromosome.compareTo(bestChromosome) > 0 (strictly better chromosome found)
 *    - Branch: chromosome.compareTo(bestChromosome) <= 0 (worse or equal chromosome ignored)
 *    - Boundary: Empty population throws IndexOutOfBoundsException
 *
 * 8. setPopulationLimit(int populationLimit)
 *    - Branch: populationLimit <= 0 -> NotPositiveException
 *    - Branch: populationLimit < chromosomes.size() -> NumberIsTooSmallException
 *    - Branch: populationLimit >= chromosomes.size() -> Success, updates limit
 *
 * 9. toString() & iterator()
 *    - toString(): String representation of chromosomes
 *    - iterator(): Defects4J Defect Target (MATH-779 / ListPopulationTest#testIterator).
 *      Spec states: "Any call to Iterator#remove() will result in a UnsupportedOperationException".
 *      Defect: iterator() returns chromosomes.iterator() directly, failing to guard against removal.
 * ====================================================================================================
 */
package org.apache.commons.math3.genetics;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.junit.Test;

public class ListPopulationGeminiTest {

    // Concrete test implementation of abstract ListPopulation
    private static class ConcreteListPopulation extends ListPopulation {
        public ConcreteListPopulation(final int populationLimit) {
            super(populationLimit);
        }

        public ConcreteListPopulation(final List<Chromosome> chromosomes, final int populationLimit) {
            super(chromosomes, populationLimit);
        }

        @Override
        public Population nextGeneration() {
            return new ConcreteListPopulation(getPopulationLimit());
        }

        public List<Chromosome> callProtectedGetChromosomeList() {
            return super.getChromosomeList();
        }
    }

    // Concrete dummy Chromosome for deterministic testing
    private static class DummyChromosome extends Chromosome {
        private final double fitness;

        public DummyChromosome(final double fitness) {
            this.fitness = fitness;
        }

        @Override
        public double getFitness() {
            return fitness;
        }

        @Override
        protected boolean isSame(final Chromosome another) {
            return this == another;
        }

        @Override
        public String toString() {
            return "DummyChromosome[" + fitness + "]";
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorWithLimitInitializesEmptyPopulation() {
        final ConcreteListPopulation population = new ConcreteListPopulation(10);
        assertEquals(10, population.getPopulationLimit());
        assertEquals(0, population.getPopulationSize());
        assertTrue(population.getChromosomes().isEmpty());
    }

    @Test(timeout = 4000)
    public void testConstructorWithListAndLimitInitializesCorrectly() {
        final DummyChromosome c1 = new DummyChromosome(1.0);
        final DummyChromosome c2 = new DummyChromosome(2.0);
        final List<Chromosome> list = Arrays.asList(c1, c2);

        final ConcreteListPopulation population = new ConcreteListPopulation(list, 5);
        assertEquals(5, population.getPopulationLimit());
        assertEquals(2, population.getPopulationSize());
        assertEquals(list, population.getChromosomes());
    }

    @Test(timeout = 4000)
    public void testAddChromosomeIncrementsSize() {
        final ConcreteListPopulation population = new ConcreteListPopulation(3);
        final DummyChromosome c1 = new DummyChromosome(1.0);
        final DummyChromosome c2 = new DummyChromosome(2.0);

        population.addChromosome(c1);
        assertEquals(1, population.getPopulationSize());
        assertEquals(c1, population.getChromosomes().get(0));

        population.addChromosome(c2);
        assertEquals(2, population.getPopulationSize());
        assertEquals(c2, population.getChromosomes().get(1));
    }

    @Test(timeout = 4000)
    public void testAddChromosomesCollection() {
        final ConcreteListPopulation population = new ConcreteListPopulation(5);
        final DummyChromosome c1 = new DummyChromosome(1.0);
        final DummyChromosome c2 = new DummyChromosome(2.0);
        final List<Chromosome> batch = Arrays.asList(c1, c2);

        population.addChromosomes(batch);
        assertEquals(2, population.getPopulationSize());
        assertTrue(population.getChromosomes().containsAll(batch));

        final DummyChromosome c3 = new DummyChromosome(3.0);
        population.addChromosomes(Collections.singletonList(c3));
        assertEquals(3, population.getPopulationSize());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testSetChromosomesReplacesExistingContent() {
        final ConcreteListPopulation population = new ConcreteListPopulation(5);
        final DummyChromosome c1 = new DummyChromosome(1.0);
        population.addChromosome(c1);
        assertEquals(1, population.getPopulationSize());

        final DummyChromosome c2 = new DummyChromosome(2.0);
        final DummyChromosome c3 = new DummyChromosome(3.0);
        final List<Chromosome> newList = Arrays.asList(c2, c3);

        population.setChromosomes(newList);
        assertEquals(2, population.getPopulationSize());
        assertEquals(newList, population.getChromosomes());
        assertFalse(population.getChromosomes().contains(c1));
    }

    @Test(timeout = 4000)
    public void testProtectedGetChromosomeListReturnsInternalList() {
        final ConcreteListPopulation population = new ConcreteListPopulation(5);
        final DummyChromosome c1 = new DummyChromosome(10.0);
        population.addChromosome(c1);

        final List<Chromosome> internalList = population.callProtectedGetChromosomeList();
        assertNotNull(internalList);
        assertEquals(1, internalList.size());
        assertEquals(c1, internalList.get(0));
    }

    @Test(timeout = 4000)
    public void testGetFittestChromosomeSelectsHighestFitness() {
        final ConcreteListPopulation population = new ConcreteListPopulation(10);
        final DummyChromosome cLow = new DummyChromosome(1.5);
        final DummyChromosome cMid = new DummyChromosome(5.0);
        final DummyChromosome cHigh = new DummyChromosome(10.0);
        final DummyChromosome cTie = new DummyChromosome(10.0);

        // Case 1: Highest is added in the middle
        population.addChromosome(cLow);
        population.addChromosome(cHigh);
        population.addChromosome(cMid);
        assertEquals(cHigh, population.getFittestChromosome());

        // Case 2: Highest is first
        final ConcreteListPopulation pop2 = new ConcreteListPopulation(5);
        pop2.addChromosome(cHigh);
        pop2.addChromosome(cLow);
        pop2.addChromosome(cMid);
        assertEquals(cHigh, pop2.getFittestChromosome());

        // Case 3: Ties in fitness (first occurrence kept if strictly > is used)
        final ConcreteListPopulation pop3 = new ConcreteListPopulation(5);
        pop3.addChromosome(cHigh);
        pop3.addChromosome(cTie);
        assertEquals(cHigh, pop3.getFittestChromosome());
    }

    @Test(timeout = 4000)
    public void testToStringMatchesChromosomesListToString() {
        final ConcreteListPopulation population = new ConcreteListPopulation(5);
        final DummyChromosome c1 = new DummyChromosome(1.0);
        population.addChromosome(c1);
        assertEquals(population.getChromosomes().toString(), population.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddChromosomesExactlyReachingLimit() {
        final ConcreteListPopulation population = new ConcreteListPopulation(2);
        population.addChromosomes(Arrays.asList(new DummyChromosome(1.0), new DummyChromosome(2.0)));
        assertEquals(2, population.getPopulationSize());
        assertEquals(2, population.getPopulationLimit());
    }

    @Test(timeout = 4000)
    public void testSetPopulationLimitBoundaryValues() {
        final ConcreteListPopulation population = new ConcreteListPopulation(5);
        population.addChromosome(new DummyChromosome(1.0));
        population.addChromosome(new DummyChromosome(2.0));

        // Lower boundary: exactly equal to current size (2)
        population.setPopulationLimit(2);
        assertEquals(2, population.getPopulationLimit());

        // Increase limit to large boundary
        population.setPopulationLimit(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, population.getPopulationLimit());
    }

    @Test(timeout = 4000)
    public void testEmptyPopulationToString() {
        final ConcreteListPopulation population = new ConcreteListPopulation(10);
        assertEquals("[]", population.toString());
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetFittestChromosomeOnEmptyPopulationThrowsException() {
        final ConcreteListPopulation population = new ConcreteListPopulation(10);
        population.getFittestChromosome();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-779 / Defects4J Target)
    // =========================================================================

    /**
     * Targets the documented contract of {@link ListPopulation#iterator()}:
     * "Returns an iterator over the unmodifiable list of chromosomes. Any call to
     * Iterator#remove() will result in an UnsupportedOperationException."
     *
     * In the defective implementation, iterator() delegates directly to chromosomes.iterator()
     * instead of getChromosomes().iterator(), allowing remove() to execute without throwing
     * UnsupportedOperationException.
     */
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testIteratorRemoveThrowsUnsupportedOperationException() {
        final ConcreteListPopulation population = new ConcreteListPopulation(10);
        population.addChromosome(new DummyChromosome(10.0));
        population.addChromosome(new DummyChromosome(20.0));

        final Iterator<Chromosome> iterator = population.iterator();
        assertTrue(iterator.hasNext());
        final Chromosome first = iterator.next();
        assertNotNull(first);

        // This operation MUST throw UnsupportedOperationException per specification.
        iterator.remove();
    }

    @Test(timeout = 4000)
    public void testIteratorTraversalIntegrity() {
        final DummyChromosome c1 = new DummyChromosome(10.0);
        final DummyChromosome c2 = new DummyChromosome(20.0);
        final ConcreteListPopulation population = new ConcreteListPopulation(Arrays.asList(c1, c2), 10);

        final Iterator<Chromosome> iterator = population.iterator();
        assertTrue(iterator.hasNext());
        assertEquals(c1, iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals(c2, iterator.next());
        assertFalse(iterator.hasNext());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testConstructorNullChromosomesThrowsException() {
        new ConcreteListPopulation(null, 10);
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testConstructorZeroLimitThrowsException() {
        new ConcreteListPopulation(0);
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testConstructorNegativeLimitThrowsException() {
        new ConcreteListPopulation(-5);
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testConstructorWithListZeroLimitThrowsException() {
        new ConcreteListPopulation(Collections.<Chromosome>emptyList(), 0);
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testConstructorWithListNegativeLimitThrowsException() {
        new ConcreteListPopulation(Collections.<Chromosome>emptyList(), -1);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testConstructorChromosomesExceedingLimitThrowsException() {
        final List<Chromosome> list = Arrays.asList(new DummyChromosome(1.0), new DummyChromosome(2.0));
        new ConcreteListPopulation(list, 1);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testSetChromosomesNullThrowsException() {
        final ConcreteListPopulation population = new ConcreteListPopulation(10);
        population.setChromosomes(null);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testSetChromosomesExceedingLimitThrowsException() {
        final ConcreteListPopulation population = new ConcreteListPopulation(1);
        final List<Chromosome> list = Arrays.asList(new DummyChromosome(1.0), new DummyChromosome(2.0));
        population.setChromosomes(list);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testAddChromosomesExceedingLimitThrowsException() {
        final ConcreteListPopulation population = new ConcreteListPopulation(2);
        population.addChromosome(new DummyChromosome(1.0));
        final List<Chromosome> batch = Arrays.asList(new DummyChromosome(2.0), new DummyChromosome(3.0));
        // 1 current + 2 batch = 3 > limit 2
        population.addChromosomes(batch);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testAddSingleChromosomeExceedingLimitThrowsException() {
        final ConcreteListPopulation population = new ConcreteListPopulation(1);
        population.addChromosome(new DummyChromosome(1.0));
        // Adding second chromosome when size == limit must throw exception
        population.addChromosome(new DummyChromosome(2.0));
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testSetPopulationLimitZeroThrowsException() {
        final ConcreteListPopulation population = new ConcreteListPopulation(10);
        population.setPopulationLimit(0);
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testSetPopulationLimitNegativeThrowsException() {
        final ConcreteListPopulation population = new ConcreteListPopulation(10);
        population.setPopulationLimit(-10);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testSetPopulationLimitLessThanCurrentSizeThrowsException() {
        final ConcreteListPopulation population = new ConcreteListPopulation(10);
        population.addChromosome(new DummyChromosome(1.0));
        population.addChromosome(new DummyChromosome(2.0));
        population.addChromosome(new DummyChromosome(3.0));

        // Attempting to set limit to 2 when size is 3 must fail
        population.setPopulationLimit(2);
    }

    // =========================================================================
    // Partition E: Object Contract & Encapsulation Integrity
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetChromosomesIsUnmodifiableOnAdd() {
        final ConcreteListPopulation population = new ConcreteListPopulation(10);
        population.getChromosomes().add(new DummyChromosome(1.0));
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetChromosomesIsUnmodifiableOnRemove() {
        final ConcreteListPopulation population = new ConcreteListPopulation(10);
        population.addChromosome(new DummyChromosome(1.0));
        population.getChromosomes().remove(0);
    }

    @Test(timeout = 4000)
    public void testNextGenerationImplementationHook() {
        final ConcreteListPopulation population = new ConcreteListPopulation(15);
        final Population nextGen = population.nextGeneration();
        assertNotNull(nextGen);
        assertEquals(15, nextGen.getPopulationLimit());
        assertEquals(0, nextGen.getPopulationSize());
    }
}