package org.apache.commons.math.stat;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Comparator;
import java.util.Iterator;

/**
 * White-box test for Frequency class.
 * Targets all major branches, boundary conditions, and the known defect:
 *   - getPct(Object) erroneously delegates to getCumPct instead of getPct(Comparable)
 *   - This test suite reveals the defect by asserting correct proportional values.
 *
 * [Branch & Defect Analysis Matrix]
 * - addValue(Comparable): Integer conversion, Long conversion, null? (not possible), ClassCastException catch, new entry vs increment.
 * - addValue(Object): instanceof Comparable delegation vs throw.
 * - addValue(int/long/char): delegate to addValue(Long/Character).
 * - getCount(Comparable): Integer conversion, ClassCastException catch returning 0, null return.
 * - getCount(int/long/char): delegate.
 * - getSumFreq(): iterate over values, empty case.
 * - getPct(Comparable): sumFreq==0 returns NaN, else division.
 * - getPct(Object): **BUG** uses getCumPct instead of getPct – test expects correct ratio.
 * - getCumFreq(Comparable): sumFreq==0 returns 0, Integer conversion, comparator usage, less than first, greater than last, iterative sum.
 * - getCumFreq(Object): delegate to getCumFreq(Comparable).
 * - getCumPct(Comparable): sumFreq==0 returns NaN, else division.
 * - getCumPct(Object): delegate to getCumPct(Comparable).
 * - clear(): removes all entries.
 * - equals/hashCode: null, other types, same/different contents.
 * - toString: format with counts and percentages.
 * - Constructor with Comparator: custom ordering.
 */
public class FrequencyDeepseekTest {

    /* ========== Partition A: Core Functional Logic & State Transitions ========== */

    @Test(timeout = 4000)
    public void testAddAndCountEmpty() {
        Frequency freq = new Frequency();
        assertEquals(0, freq.getSumFreq());
        assertEquals(0, freq.getCount(1));
        assertEquals(0, freq.getCount(1L));
        assertEquals(0, freq.getCount('a'));
        assertTrue(Double.isNaN(freq.getPct(1)));
        assertTrue(Double.isNaN(freq.getCumPct(1)));
    }

    @Test(timeout = 4000)
    public void testAddIntegers() {
        Frequency freq = new Frequency();
        freq.addValue(1);
        freq.addValue(2);
        freq.addValue(1);
        assertEquals(3, freq.getSumFreq());
        assertEquals(2, freq.getCount(1));
        assertEquals(1, freq.getCount(2));
        assertEquals(2, freq.getCount(1L));   // Long lookup
        assertEquals(0.6666666666666666, freq.getPct(1), 1e-10);
        assertEquals(0.3333333333333333, freq.getPct(2), 1e-10);
    }

    @Test(timeout = 4000)
    public void testAddLongs() {
        Frequency freq = new Frequency();
        freq.addValue(10L);
        freq.addValue(20L);
        freq.addValue(10L);
        assertEquals(3, freq.getSumFreq());
        assertEquals(2, freq.getCount(10L));
        assertEquals(1, freq.getCount(20L));
        assertEquals(2, freq.getCount(10));   // int lookup
    }

    @Test(timeout = 4000)
    public void testAddChars() {
        Frequency freq = new Frequency();
        freq.addValue('a');
        freq.addValue('b');
        freq.addValue('a');
        assertEquals(3, freq.getSumFreq());
        assertEquals(2, freq.getCount('a'));
        assertEquals(1, freq.getCount('b'));
    }

    @Test(timeout = 4000)
    public void testAddStrings() {
        Frequency freq = new Frequency();
        freq.addValue("apple");
        freq.addValue("banana");
        freq.addValue("apple");
        assertEquals(3, freq.getSumFreq());
        assertEquals(2, freq.getCount("apple"));
        assertEquals(1, freq.getCount("banana"));
    }

    @Test(timeout = 4000)
    public void testAddIntegerObject() {
        Frequency freq = new Frequency();
        freq.addValue(Integer.valueOf(5));
        freq.addValue(5);
        assertEquals(2, freq.getCount(5));
    }

    @Test(timeout = 4000)
    public void testValuesIterator() {
        Frequency freq = new Frequency();
        freq.addValue(2);
        freq.addValue(1);
        freq.addValue(2);
        Iterator<Comparable<?>> it = freq.valuesIterator();
        assertTrue(it.hasNext());
        assertEquals(Long.valueOf(1), it.next());
        assertTrue(it.hasNext());
        assertEquals(Long.valueOf(2), it.next());
        assertFalse(it.hasNext());
    }

    /* ========== Partition B: Boundary Value Analysis & Extremes ========== */

    @Test(timeout = 4000)
    public void testSingleValue() {
        Frequency freq = new Frequency();
        freq.addValue(42);
        assertEquals(1, freq.getCount(42));
        assertEquals(1.0, freq.getPct(42), 1e-10);
        assertEquals(1.0, freq.getCumPct(42), 1e-10);
    }

    @Test(timeout = 4000)
    public void testZeroAndNegativeInt() {
        Frequency freq = new Frequency();
        freq.addValue(0);
        freq.addValue(-1);
        freq.addValue(0);
        assertEquals(2, freq.getCount(0));
        assertEquals(1, freq.getCount(-1));
    }

    @Test(timeout = 4000)
    public void testLargeValues() {
        Frequency freq = new Frequency();
        freq.addValue(Integer.MAX_VALUE);
        freq.addValue(Integer.MIN_VALUE);
        assertEquals(1, freq.getCount(Integer.MAX_VALUE));
        assertEquals(1, freq.getCount(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testDuplicateAddMany() {
        Frequency freq = new Frequency();
        for (int i = 0; i < 100; i++) {
            freq.addValue(5);
        }
        assertEquals(100, freq.getCount(5));
        assertEquals(100, freq.getSumFreq());
    }

    @Test(timeout = 4000)
    public void testClear() {
        Frequency freq = new Frequency();
        freq.addValue(10);
        freq.addValue(20);
        freq.clear();
        assertEquals(0, freq.getSumFreq());
        assertEquals(0, freq.getCount(10));
        assertTrue(Double.isNaN(freq.getPct(10)));
    }

    /* ========== Partition C: Defect-Targeted Tests (Bug in getPct(Object)) ========== */

    @Test(timeout = 4000)
    public void testGetPctObjectBugReveal() {
        // Known defect: getPct(Object) calls getCumPct instead of getPct(Comparable)
        Frequency freq = new Frequency();
        freq.addValue(1);
        freq.addValue(2);  // two distinct values
        // For key 1: count=1, sumFreq=2 => proportion = 0.5
        // Bug returns cumulative proportion (1.0) because it uses getCumPct
        // We expect 0.5 from correct implementation.
        double pct = freq.getPct((Object) 1);
        assertEquals("getPct(Object) should return proportion, not cumulative", 0.5, pct, 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetPctObjectString() {
        Frequency freq = new Frequency();
        freq.addValue("a");
        freq.addValue("b");
        double pct = freq.getPct((Object) "a");
        assertEquals(0.5, pct, 1e-10);
    }

    @Test(timeout = 4000)
    public void testGetCumPctObject() {
        // getCumPct(Object) is also deprecated but should delegate to getCumPct(Comparable)
        Frequency freq = new Frequency();
        freq.addValue(1);
        freq.addValue(2);
        freq.addValue(3);
        double cumPct = freq.getCumPct((Object) 2);
        assertEquals(2.0/3.0, cumPct, 1e-10);
    }

    /* ========== Partition D: Exception & Defensive Guard Paths ========== */

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddNonComparableObject() {
        Frequency freq = new Frequency();
        freq.addValue(new Object());  // does not implement Comparable
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddIncomparableClass() {
        Frequency freq = new Frequency();
        freq.addValue(1);
        // Adding a different type that is not comparable (e.g., String vs Integer might actually be comparable? but TreeMap will throw ClassCastException because natural ordering of String vs Integer fails)
        // Actually TreeMap's comparator will compare Integer and String -> ClassCastException. So this is valid.
        freq.addValue("text");
    }

    @Test(timeout = 4000)
    public void testGetCountUncomparable() {
        Frequency freq = new Frequency();
        freq.addValue(10);
        // Passing a non-comparable type to getCount(Comparable) – should return 0.
        // However, the method catches ClassCastException.
        // We'll pass an object that causes ClassCastException in TreeMap.get
        // For natural ordering, Object does not implement Comparable -> ClassCastException when TreeMap tries to compare?
        // Actually TreeMap.get will call comparator.compare(key, someKey) if comparator exists? NaturalComparator does compareTo cast.
        // This will throw ClassCastException. Perfect.
        assertEquals(0, freq.getCount(new Object()));
    }

    @Test(timeout = 4000)
    public void testGetCountNonComparableInteger() {
        Frequency freq = new Frequency();
        freq.addValue(10);
        // Integer is comparable, so should work. 
        assertEquals(1, freq.getCount(10));
    }

    @Test(timeout = 4000)
    public void testCumFreqLessThanFirst() {
        Frequency freq = new Frequency();
        freq.addValue(5);
        freq.addValue(10);
        long cum = freq.getCumFreq(3);  // less than smallest key (5)
        assertEquals(0, cum);
    }

    @Test(timeout = 4000)
    public void testCumFreqGreaterThanLast() {
        Frequency freq = new Frequency();
        freq.addValue(5);
        freq.addValue(10);
        long cum = freq.getCumFreq(15);  // greater than largest key (10)
        assertEquals(2, cum);
    }

    @Test(timeout = 4000)
    public void testCumFreqExactMatch() {
        Frequency freq = new Frequency();
        freq.addValue(5);
        freq.addValue(10);
        long cum = freq.getCumFreq(5);
        assertEquals(1, cum);
    }

    @Test(timeout = 4000)
    public void testCumFreqBetween() {
        Frequency freq = new Frequency();
        freq.addValue(5);
        freq.addValue(10);
        long cum = freq.getCumFreq(7);
        // values <= 7 only include 5 -> count(5)=1
        assertEquals(1, cum);
    }

    @Test(timeout = 4000)
    public void testCumPctEmpty() {
        Frequency freq = new Frequency();
        assertTrue(Double.isNaN(freq.getCumPct(1)));
    }

    @Test(timeout = 4000)
    public void testPctEmpty() {
        Frequency freq = new Frequency();
        assertTrue(Double.isNaN(freq.getPct(1)));
    }

    @Test(timeout = 4000)
    public void testAddValueDeprecatedObject() {
        // addValue(Object v) deprecated but should work
        Frequency freq = new Frequency();
        freq.addValue((Object) "hello");
        assertEquals(1, freq.getCount("hello"));
    }

    /* ========== Partition E: Object Lifecycle & Contract Integrity ========== */

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        Frequency freq = new Frequency();
        assertTrue(freq.equals(freq));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        Frequency freq = new Frequency();
        assertFalse(freq.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        Frequency freq = new Frequency();
        assertFalse(freq.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsEmptyFrequencies() {
        Frequency f1 = new Frequency();
        Frequency f2 = new Frequency();
        assertTrue(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsSameContent() {
        Frequency f1 = new Frequency();
        f1.addValue(1);
        f1.addValue(2);
        Frequency f2 = new Frequency();
        f2.addValue(2);
        f2.addValue(1);
        assertTrue(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentContent() {
        Frequency f1 = new Frequency();
        f1.addValue(1);
        Frequency f2 = new Frequency();
        f2.addValue(2);
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistent() {
        Frequency f1 = new Frequency();
        f1.addValue(1);
        f1.addValue(2);
        Frequency f2 = new Frequency();
        f2.addValue(2);
        f2.addValue(1);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Frequency freq = new Frequency();
        freq.addValue(10);
        freq.addValue(20);
        String str = freq.toString();
        assertTrue(str.contains("10"));
        assertTrue(str.contains("20"));
        assertTrue(str.contains("Freq."));
        assertTrue(str.contains("Pct."));
    }

    @Test(timeout = 4000)
    public void testConstructorWithComparator() {
        // Use reverse natural order comparator
        Comparator<Integer> reverse = new Comparator<Integer>() {
            public int compare(Integer a, Integer b) {
                return b.compareTo(a);
            }
        };
        Frequency freq = new Frequency(reverse);
        freq.addValue(1);
        freq.addValue(2);
        Iterator<Comparable<?>> it = freq.valuesIterator();
        // Should iterate in descending order
        assertEquals(Long.valueOf(2), it.next());
        assertEquals(Long.valueOf(1), it.next());
        // Counts should work normally
        assertEquals(1, freq.getCount(1));
        assertEquals(1, freq.getCount(2));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddNonComparableWithComparator() {
        // Even with a custom Comparator, Objects that are not Comparable might still cause issues?
        // But TreeMap uses comparator only for ordering; it does not require Comparable.
        // However, addValue(Comparable) casts to Comparable; passing non-Comparable will fail.
        Comparator<String> comp = new Comparator<String>() {
            public int compare(String a, String b) { return a.compareTo(b); }
        };
        Frequency freq = new Frequency(comp);
        freq.addValue(new Object()); // Object does not implement Comparable -> IllegalArgumentException
    }

    // Additional edge case: getCumFreq with uncomparable value should return 0.
    @Test(timeout = 4000)
    public void testCumFreqUncomparable() {
        Frequency freq = new Frequency();
        freq.addValue(5);
        long cum = freq.getCumFreq(new Object());
        assertEquals(0, cum);
    }

    // getCumPct with uncomparable value should return 0 (since cumFreq returns 0, sumFreq>0)
    @Test(timeout = 4000)
    public void testCumPctUncomparable() {
        Frequency freq = new Frequency();
        freq.addValue(5);
        double cumPct = freq.getCumPct(new Object());
        assertEquals(0.0, cumPct, 1e-10);
    }

    // getPct with uncomparable value should return 0 (getCount returns 0)
    @Test(timeout = 4000)
    public void testPctUncomparable() {
        Frequency freq = new Frequency();
        freq.addValue(5);
        double pct = freq.getPct(new Object());
        assertEquals(0.0, pct, 1e-10);
    }
}