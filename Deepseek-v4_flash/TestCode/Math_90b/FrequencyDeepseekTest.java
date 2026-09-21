package org.apache.commons.math.stat;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: Frequency.java (Defects4J bug: addValue(Object) fails to throw IllegalArgumentException for non-comparable values)
 * 
 * Branches covered:
 * - addValue(Object): Integer conversion, freqTable.get null/not null, ClassCastException catch
 * - addValue(int/long/char/Integer): delegation to addValue(Object)
 * - getCount(Object): Integer conversion, freqTable.get null/not null, ClassCastException catch
 * - getCount(int/long/char): delegation
 * - getSumFreq(): iteration over values, empty map
 * - getPct(Object): sumFreq==0 (NaN), else division
 * - getCumFreq(Object): sumFreq==0, Integer conversion, comparator null (NaturalComparator), 
 *   freqTable.get null/not null, ClassCastException, compare to firstKey (<0), compare to lastKey (>=0),
 *   iteration with compare >0 and else
 * - getCumPct(Object): sumFreq==0 (NaN), else division
 * - clear(): freqTable.clear()
 * - valuesIterator(): keySet iterator
 * - toString(): iteration, NumberFormat
 * - Constructor with Comparator
 * 
 * Defect-targeted test: testAddNonComparable() – verifies that adding a non-comparable value after
 * a comparable value throws IllegalArgumentException. The buggy version may not throw the exception.
 */
public class FrequencyDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testAddAndGetCountInt() {
        Frequency f = new Frequency();
        f.addValue(1);
        f.addValue(2);
        f.addValue(1);
        assertEquals(2, f.getCount(1));
        assertEquals(1, f.getCount(2));
        assertEquals(0, f.getCount(3));
    }

    @Test(timeout = 4000)
    public void testAddAndGetCountLong() {
        Frequency f = new Frequency();
        f.addValue(10L);
        f.addValue(20L);
        f.addValue(10L);
        assertEquals(2, f.getCount(10L));
        assertEquals(1, f.getCount(20L));
    }

    @Test(timeout = 4000)
    public void testAddAndGetCountInteger() {
        Frequency f = new Frequency();
        f.addValue(Integer.valueOf(5));
        f.addValue(Integer.valueOf(5));
        f.addValue(Integer.valueOf(6));
        assertEquals(2, f.getCount(Integer.valueOf(5)));
        assertEquals(1, f.getCount(Integer.valueOf(6)));
    }

    @Test(timeout = 4000)
    public void testAddAndGetCountChar() {
        Frequency f = new Frequency();
        f.addValue('a');
        f.addValue('b');
        f.addValue('a');
        assertEquals(2, f.getCount('a'));
        assertEquals(1, f.getCount('b'));
        assertEquals(0, f.getCount('c'));
    }

    @Test(timeout = 4000)
    public void testAddAndGetCountMixedTypes() {
        Frequency f = new Frequency();
        f.addValue(1);          // int -> Long
        f.addValue(1L);         // long -> Long (same)
        f.addValue(Integer.valueOf(1)); // Integer -> Long (same)
        assertEquals(3, f.getCount(1));
        assertEquals(3, f.getCount(1L));
        assertEquals(3, f.getCount(Integer.valueOf(1)));
    }

    @Test(timeout = 4000)
    public void testGetSumFreq() {
        Frequency f = new Frequency();
        assertEquals(0, f.getSumFreq());
        f.addValue(1);
        f.addValue(2);
        f.addValue(1);
        assertEquals(3, f.getSumFreq());
    }

    @Test(timeout = 4000)
    public void testClear() {
        Frequency f = new Frequency();
        f.addValue(1);
        f.addValue(2);
        f.clear();
        assertEquals(0, f.getSumFreq());
        assertEquals(0, f.getCount(1));
    }

    @Test(timeout = 4000)
    public void testValuesIterator() {
        Frequency f = new Frequency();
        f.addValue(3);
        f.addValue(1);
        f.addValue(2);
        java.util.Iterator it = f.valuesIterator();
        assertTrue(it.hasNext());
        assertEquals(Long.valueOf(1), it.next());
        assertEquals(Long.valueOf(2), it.next());
        assertEquals(Long.valueOf(3), it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Frequency f = new Frequency();
        f.addValue(1);
        f.addValue(2);
        String s = f.toString();
        assertTrue(s.contains("Value \t Freq. \t Pct. \t Cum Pct."));
        assertTrue(s.contains("1\t1\t"));
        assertTrue(s.contains("2\t1\t"));
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testEmptyFrequency() {
        Frequency f = new Frequency();
        assertEquals(0, f.getSumFreq());
        assertEquals(0, f.getCount(1));
        assertEquals(Double.NaN, f.getPct(1), 0.0);
        assertEquals(0, f.getCumFreq(1));
        assertEquals(Double.NaN, f.getCumPct(1), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetPctWithValues() {
        Frequency f = new Frequency();
        f.addValue(1);
        f.addValue(2);
        f.addValue(1);
        assertEquals(2.0/3.0, f.getPct(1), 1e-10);
        assertEquals(1.0/3.0, f.getPct(2), 1e-10);
        assertEquals(0.0, f.getPct(3), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetCumFreqBoundaries() {
        Frequency f = new Frequency();
        f.addValue(10);
        f.addValue(20);
        f.addValue(30);
        // less than first
        assertEquals(0, f.getCumFreq(5));
        // equal to first
        assertEquals(1, f.getCumFreq(10));
        // between
        assertEquals(1, f.getCumFreq(15));
        // equal to last
        assertEquals(3, f.getCumFreq(30));
        // greater than last
        assertEquals(3, f.getCumFreq(35));
    }

    @Test(timeout = 4000)
    public void testGetCumPctBoundaries() {
        Frequency f = new Frequency();
        f.addValue(10);
        f.addValue(20);
        f.addValue(30);
        assertEquals(0.0, f.getCumPct(5), 0.0);
        assertEquals(1.0/3.0, f.getCumPct(10), 1e-10);
        assertEquals(1.0/3.0, f.getCumPct(15), 1e-10);
        assertEquals(1.0, f.getCumPct(30), 0.0);
        assertEquals(1.0, f.getCumPct(35), 0.0);
    }

    @Test(timeout = 4000)
    public void testLargeValues() {
        Frequency f = new Frequency();
        f.addValue(Integer.MAX_VALUE);
        f.addValue(Long.MAX_VALUE);
        f.addValue(Integer.MAX_VALUE);
        assertEquals(2, f.getCount(Integer.MAX_VALUE));
        assertEquals(1, f.getCount(Long.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testNegativeValues() {
        Frequency f = new Frequency();
        f.addValue(-1);
        f.addValue(-2);
        f.addValue(-1);
        assertEquals(2, f.getCount(-1));
        assertEquals(1, f.getCount(-2));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testAddNonComparable() {
        // This test targets the known defect: adding a non-comparable value after a comparable one
        // should throw IllegalArgumentException. The buggy version may not throw.
        Frequency f = new Frequency();
        f.addValue(1); // comparable
        // Create a non-comparable object (does not implement Comparable)
        Object nonComparable = new Object() {};
        try {
            f.addValue(nonComparable);
            fail("Expected IllegalArgumentException for non-comparable value");
        } catch (IllegalArgumentException e) {
            // expected
            assertTrue(e.getMessage().contains("not comparable"));
        }
    }

    @Test(timeout = 4000)
    public void testAddNonComparableFirst() {
        // Adding non-comparable first should succeed (no comparison needed)
        Frequency f = new Frequency();
        Object nonComparable = new Object() {};
        f.addValue(nonComparable);
        assertEquals(1, f.getCount(nonComparable));
        // Now adding a comparable value should throw
        try {
            f.addValue(1);
            fail("Expected IllegalArgumentException when adding comparable after non-comparable");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetCountNonComparable() {
        Frequency f = new Frequency();
        f.addValue(1);
        Object nonComparable = new Object() {};
        // getCount should return 0 for non-comparable (catches ClassCastException)
        assertEquals(0, f.getCount(nonComparable));
    }

    @Test(timeout = 4000)
    public void testGetCumFreqNonComparable() {
        Frequency f = new Frequency();
        f.addValue(10);
        f.addValue(20);
        Object nonComparable = new Object() {};
        // getCumFreq should return 0 for non-comparable (catches ClassCastException)
        assertEquals(0, f.getCumFreq(nonComparable));
    }

    @Test(timeout = 4000)
    public void testGetPctNonComparable() {
        Frequency f = new Frequency();
        f.addValue(10);
        Object nonComparable = new Object() {};
        // getPct returns (double) getCount(v)/sumFreq; getCount returns 0 => 0.0
        assertEquals(0.0, f.getPct(nonComparable), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetCumPctNonComparable() {
        Frequency f = new Frequency();
        f.addValue(10);
        Object nonComparable = new Object() {};
        // getCumPct returns (double) getCumFreq(v)/sumFreq; getCumFreq returns 0 => 0.0
        assertEquals(0.0, f.getCumPct(nonComparable), 0.0);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddValueObjectWithNonComparableAfterComparable() {
        Frequency f = new Frequency();
        f.addValue("string"); // Comparable
        f.addValue(new Object()); // non-Comparable -> should throw
    }

    @Test(timeout = 4000)
    public void testGetCountWithNull() {
        Frequency f = new Frequency();
        f.addValue(1);
        // getCount(null) should return 0 (TreeMap.get(null) returns null, no exception)
        assertEquals(0, f.getCount(null));
    }

    @Test(timeout = 4000)
    public void testGetCumFreqWithNull() {
        Frequency f = new Frequency();
        f.addValue(1);
        // getCumFreq(null) should return 0 (since sumFreq>0, but null is not comparable)
        assertEquals(0, f.getCumFreq(null));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testConstructorWithComparator() {
        java.util.Comparator<String> comp = String.CASE_INSENSITIVE_ORDER;
        Frequency f = new Frequency(comp);
        f.addValue("A");
        f.addValue("a");
        // With case-insensitive comparator, "A" and "a" are considered equal
        assertEquals(2, f.getCount("A"));
        assertEquals(2, f.getCount("a"));
    }

    @Test(timeout = 4000)
    public void testNaturalComparator() {
        // Indirectly test NaturalComparator via getCumFreq
        Frequency f = new Frequency();
        f.addValue(10);
        f.addValue(20);
        // getCumFreq uses NaturalComparator when no custom comparator
        assertEquals(1, f.getCumFreq(15));
    }

    @Test(timeout = 4000)
    public void testMultipleAddSameValue() {
        Frequency f = new Frequency();
        for (int i = 0; i < 100; i++) {
            f.addValue(1);
        }
        assertEquals(100, f.getCount(1));
        assertEquals(100, f.getSumFreq());
    }

    @Test(timeout = 4000)
    public void testDeprecatedAddValueObject() {
        Frequency f = new Frequency();
        f.addValue((Object) Integer.valueOf(5));
        f.addValue((Object) Long.valueOf(5));
        assertEquals(2, f.getCount(5));
    }
}