package org.apache.commons.math3.genetics;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * White-box test suite for ListPopulation targeting maximum coverage and the known iterator defect.
 *
 * Branch & Defect Analysis Matrix:
 * - Constructor(int): populationLimit <= 0 → NotPositiveException
 * - Constructor(List, int): null list → NullArgumentException; limit <= 0 → NotPositiveException; list.size() > limit → NumberIsTooLargeException
 * - setChromosomes: null → NullArgumentException; list.size() > limit → NumberIsTooLargeException; normal replacement
 * - addChromosomes: resulting size > limit → NumberIsTooLargeException; normal addition
 * - addChromosome: current size >= limit → NumberIsTooLargeException; normal addition
 * - getFittestChromosome: empty list → IndexOutOfBoundsException (defect? but not specified); normal comparison
 * - setPopulationLimit: limit <= 0 → NotPositiveException; limit < current size → NumberIsTooSmallException; normal set
 * - iterator: should return unmodifiable iterator; calling remove() must throw UnsupportedOperationException (KNOWN DEFECT)
 * - getChromosomes: returns unmodifiable list; remove() throws UnsupportedOperationException
 * - getChromosomeList: returns modifiable internal list (protected)
 * - toString: delegates to list toString
 * - getPopulationSize: returns size
 * - getPopulationLimit: returns limit
 */
public class ListPopulationDeepseekTest {

    // --- Helper stubs ---

    private static class DummyChromosome extends Chromosome {
        private final double fitness;

        DummyChromosome(double fitness) {
            this.fitness = fitness;
        }

        @Override
        public double fitness() {
            return fitness;
        }

        @Override
        public int compareTo(Chromosome other) {
            return Double.compare(fitness, ((DummyChromosome) other).fitness);
        }
    }

    private static class TestPopulation extends ListPopulation {
        TestPopulation(int populationLimit) {
            super(populationLimit);
        }

        TestPopulation(List<Chromosome> chromosomes, int populationLimit) {
            super(chromosomes, populationLimit);
        }
    }

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testConstructorWithLimitOnly() {
        TestPopulation pop = new TestPopulation(10);
        assertEquals(0, pop.getPopulationSize());
        assertEquals(10, pop.getPopulationLimit());
        assertTrue(pop.getChromosomes().isEmpty());
    }

    @Test(timeout = 4000)
    public void testConstructorWithChromosomes() {
        List<Chromosome> list = new ArrayList<Chromosome>();
        list.add(new DummyChromosome(1.0));
        list.add(new DummyChromosome(2.0));
        TestPopulation pop = new TestPopulation(list, 5);
        assertEquals(2, pop.getPopulationSize());
        assertEquals(5, pop.getPopulationLimit());
        assertEquals(2, pop.getChromosomes().size());
    }

    @Test(timeout = 4000)
    public void testAddChromosomeNormal() {
        TestPopulation pop = new TestPopulation(3);
        pop.addChromosome(new DummyChromosome(1.0));
        assertEquals(1, pop.getPopulationSize());
        pop.addChromosome(new DummyChromosome(2.0));
        assertEquals(2, pop.getPopulationSize());
    }

    @Test(timeout = 4000)
    public void testAddChromosomesNormal() {
        TestPopulation pop = new TestPopulation(5);
        List<Chromosome> toAdd = new ArrayList<Chromosome>();
        toAdd.add(new DummyChromosome(1.0));
        toAdd.add(new DummyChromosome(2.0));
        pop.addChromosomes(toAdd);
        assertEquals(2, pop.getPopulationSize());
    }

    @Test(timeout = 4000)
    public void testSetChromosomesNormal() {
        TestPopulation pop = new TestPopulation(5);
        List<Chromosome> newList = new ArrayList<Chromosome>();
        newList.add(new DummyChromosome(10.0));
        pop.setChromosomes(newList);
        assertEquals(1, pop.getPopulationSize());
        assertEquals(10.0, pop.getFittestChromosome().fitness(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetFittestChromosome() {
        List<Chromosome> list = new ArrayList<Chromosome>();
        list.add(new DummyChromosome(5.0));
        list.add(new DummyChromosome(3.0));
        list.add(new DummyChromosome(8.0));
        TestPopulation pop = new TestPopulation(list, 10);
        Chromosome fittest = pop.getFittestChromosome();
        assertEquals(8.0, fittest.fitness(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetPopulationLimitAndSize() {
        TestPopulation pop = new TestPopulation(100);
        assertEquals(100, pop.getPopulationLimit());
        assertEquals(0, pop.getPopulationSize());
        pop.addChromosome(new DummyChromosome(1.0));
        assertEquals(1, pop.getPopulationSize());
    }

    @Test(timeout = 4000)
    public void testSetPopulationLimitNormal() {
        TestPopulation pop = new TestPopulation(10);
        pop.setPopulationLimit(20);
        assertEquals(20, pop.getPopulationLimit());
    }

    @Test(timeout = 4000)
    public void testToString() {
        TestPopulation pop = new TestPopulation(5);
        assertEquals("[]", pop.toString());
        pop.addChromosome(new DummyChromosome(1.0));
        assertTrue(pop.toString().contains("1.0"));
    }

    @Test(timeout = 4000)
    public void testGetChromosomeListModifiable() {
        TestPopulation pop = new TestPopulation(5);
        List<Chromosome> internal = pop.getChromosomeList();
        internal.add(new DummyChromosome(99.0));
        assertEquals(1, pop.getPopulationSize());
    }

    @Test(timeout = 4000)
    public void testGetChromosomesUnmodifiable() {
        TestPopulation pop = new TestPopulation(5);
        List<Chromosome> unmod = pop.getChromosomes();
        try {
            unmod.add(new DummyChromosome(1.0));
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testConstructorLimitOne() {
        TestPopulation pop = new TestPopulation(1);
        assertEquals(1, pop.getPopulationLimit());
        pop.addChromosome(new DummyChromosome(1.0));
        assertEquals(1, pop.getPopulationSize());
    }

    @Test(timeout = 4000)
    public void testConstructorEmptyChromosomes() {
        List<Chromosome> empty = new ArrayList<Chromosome>();
        TestPopulation pop = new TestPopulation(empty, 10);
        assertEquals(0, pop.getPopulationSize());
    }

    @Test(timeout = 4000)
    public void testAddChromosomeAtLimit() {
        TestPopulation pop = new TestPopulation(2);
        pop.addChromosome(new DummyChromosome(1.0));
        pop.addChromosome(new DummyChromosome(2.0));
        assertEquals(2, pop.getPopulationSize());
        // Adding one more should throw
        try {
            pop.addChromosome(new DummyChromosome(3.0));
            fail("Should have thrown NumberIsTooLargeException");
        } catch (NumberIsTooLargeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddChromosomesExactFit() {
        TestPopulation pop = new TestPopulation(3);
        List<Chromosome> toAdd = new ArrayList<Chromosome>();
        toAdd.add(new DummyChromosome(1.0));
        toAdd.add(new DummyChromosome(2.0));
        toAdd.add(new DummyChromosome(3.0));
        pop.addChromosomes(toAdd);
        assertEquals(3, pop.getPopulationSize());
    }

    @Test(timeout = 4000)
    public void testSetChromosomesEmpty() {
        TestPopulation pop = new TestPopulation(5);
        pop.addChromosome(new DummyChromosome(1.0));
        pop.setChromosomes(new ArrayList<Chromosome>());
        assertEquals(0, pop.getPopulationSize());
    }

    @Test(timeout = 4000)
    public void testSetPopulationLimitEqualToSize() {
        TestPopulation pop = new TestPopulation(5);
        pop.addChromosome(new DummyChromosome(1.0));
        pop.addChromosome(new DummyChromosome(2.0));
        pop.setPopulationLimit(2); // should succeed
        assertEquals(2, pop.getPopulationLimit());
    }

    @Test(timeout = 4000)
    public void testGetFittestChromosomeSingle() {
        TestPopulation pop = new TestPopulation(5);
        pop.addChromosome(new DummyChromosome(42.0));
        assertEquals(42.0, pop.getFittestChromosome().fitness(), 0.0);
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    /**
     * KNOWN DEFECT: iterator() returns the internal list's iterator which supports remove(),
     * but the documentation states that Iterator.remove() should throw UnsupportedOperationException.
     * This test expects the exception, so it will fail on the buggy version.
     */
    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testIteratorRemoveShouldThrow() {
        TestPopulation pop = new TestPopulation(5);
        pop.addChromosome(new DummyChromosome(1.0));
        Iterator<Chromosome> it = pop.iterator();
        it.next(); // move to first element
        it.remove(); // should throw UnsupportedOperationException
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000, expected = NotPositiveException.class)
    public void testConstructorLimitZero() {
        new TestPopulation(0);
    }

    @Test(timeout = 4000, expected = NotPositiveException.class)
    public void testConstructorLimitNegative() {
        new TestPopulation(-1);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testConstructorNullChromosomes() {
        new TestPopulation((List<Chromosome>) null, 10);
    }

    @Test(timeout = 4000, expected = NumberIsTooLargeException.class)
    public void testConstructorChromosomesExceedLimit() {
        List<Chromosome> list = new ArrayList<Chromosome>();
        list.add(new DummyChromosome(1.0));
        list.add(new DummyChromosome(2.0));
        new TestPopulation(list, 1);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testSetChromosomesNull() {
        TestPopulation pop = new TestPopulation(5);
        pop.setChromosomes(null);
    }

    @Test(timeout = 4000, expected = NumberIsTooLargeException.class)
    public void testSetChromosomesExceedLimit() {
        TestPopulation pop = new TestPopulation(2);
        List<Chromosome> bigList = new ArrayList<Chromosome>();
        bigList.add(new DummyChromosome(1.0));
        bigList.add(new DummyChromosome(2.0));
        bigList.add(new DummyChromosome(3.0));
        pop.setChromosomes(bigList);
    }

    @Test(timeout = 4000, expected = NumberIsTooLargeException.class)
    public void testAddChromosomesExceedLimit() {
        TestPopulation pop = new TestPopulation(2);
        pop.addChromosome(new DummyChromosome(1.0));
        List<Chromosome> toAdd = new ArrayList<Chromosome>();
        toAdd.add(new DummyChromosome(2.0));
        toAdd.add(new DummyChromosome(3.0)); // would make total 3 > 2
        pop.addChromosomes(toAdd);
    }

    @Test(timeout = 4000, expected = NumberIsTooLargeException.class)
    public void testAddChromosomeWhenFull() {
        TestPopulation pop = new TestPopulation(1);
        pop.addChromosome(new DummyChromosome(1.0));
        pop.addChromosome(new DummyChromosome(2.0)); // should throw
    }

    @Test(timeout = 4000, expected = NotPositiveException.class)
    public void testSetPopulationLimitZero() {
        TestPopulation pop = new TestPopulation(5);
        pop.setPopulationLimit(0);
    }

    @Test(timeout = 4000, expected = NotPositiveException.class)
    public void testSetPopulationLimitNegative() {
        TestPopulation pop = new TestPopulation(5);
        pop.setPopulationLimit(-3);
    }

    @Test(timeout = 4000, expected = NumberIsTooSmallException.class)
    public void testSetPopulationLimitSmallerThanSize() {
        TestPopulation pop = new TestPopulation(5);
        pop.addChromosome(new DummyChromosome(1.0));
        pop.addChromosome(new DummyChromosome(2.0));
        pop.setPopulationLimit(1); // current size is 2 > 1
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetFittestChromosomeEmptyPopulation() {
        TestPopulation pop = new TestPopulation(5);
        pop.getFittestChromosome(); // should throw because list is empty
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testIteratorIteration() {
        TestPopulation pop = new TestPopulation(5);
        pop.addChromosome(new DummyChromosome(1.0));
        pop.addChromosome(new DummyChromosome(2.0));
        Iterator<Chromosome> it = pop.iterator();
        assertTrue(it.hasNext());
        assertEquals(1.0, it.next().fitness(), 0.0);
        assertTrue(it.hasNext());
        assertEquals(2.0, it.next().fitness(), 0.0);
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testIteratorNextOnEmpty() {
        TestPopulation pop = new TestPopulation(5);
        pop.iterator().next();
    }

    @Test(timeout = 4000)
    public void testChromosomeListUnmodifiableViaIterator() {
        // Additional check: the iterator from getChromosomes() should also be unmodifiable
        TestPopulation pop = new TestPopulation(5);
        pop.addChromosome(new DummyChromosome(1.0));
        List<Chromosome> unmod = pop.getChromosomes();
        Iterator<Chromosome> it = unmod.iterator();
        it.next();
        try {
            it.remove();
            fail("Should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }
}