package org.apache.commons.math.stat;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * White-box test suite for Frequency.
 * Targets: line coverage, branch coverage, and the known defect:
 * ClassCastException when adding a non-Comparable object.
 */
public class FrequencyDeepseekTest {

    /* ============================================================
     * [Branch & Defect Analysis Matrix]
     * 
     * 1. addValue(Comparable<?>):
     *    - Branch: v instanceof Integer -> convert to Long
     *    - Branch: freqTable.get(obj) == null -> put 1
     *    - Branch: else -> increment
     *    - Exception: ClassCastException -> throw IllegalArgumentException
     * 
     * 2. addValue(Object) deprecated: delegates to addValue(Comparable)
     * 
     * 3. addValue(int/long/Integer/char): convert to Long/Character then call addValue(Comparable)
     * 
     * 4. getCount(Object):
     *    - Branch: v instanceof Integer -> delegate to getCount(long)
     *    - Branch: freqTable.get(v) returns null -> result=0
     *    - Branch: ClassCastException -> return 0
     * 
     * 5. getCumFreq(Object):
     *    - Branch: getSumFreq() == 0 -> return 0
     *    - Branch: v instanceof Integer -> delegate to getCumFreq(long)
     *    - Branch: comparator == null -> use NaturalComparator
     *    - Branch: freqTable.get(v) != null -> add its count
     *    - Branch: ClassCastException -> return result (0)
     *    - Branch: c.compare(v, firstKey) < 0 -> return 0
     *    - Branch: c.compare(v, lastKey) >= 0 -> return sumFreq
     *    - Loop: iterate values, compare, accumulate
     * 
     * 6. getPct / getCumPct: delegate to getCount/getCumFreq, handle sumFreq==0 -> NaN
     * 
     * 7. clear(), valuesIterator(), toString()
     * 
     * Defect: addValue(Comparable) does not check if v is actually Comparable;
     *         it casts to Comparable<?> in the method signature, but if a non-Comparable
     *         object is passed (e.g., via the deprecated addValue(Object)), the cast
     *         in the method call itself will throw ClassCastException before any handling.
     *         The test must verify that an IllegalArgumentException is thrown.
     * ============================================================ */

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testAddAndGetCount_Integer() {
        Frequency freq = new Frequency();
        freq.addValue(1);
        freq.addValue(2);
        freq.addValue(1);
        assertEquals(2L, freq.getCount(1));
        assertEquals(1L, freq.getCount(2));
        assertEquals(0L, freq.getCount(3));
    }

    @Test(timeout = 4000)
    public void testAddAndGetCount_Long() {
        Frequency freq = new Frequency();
        freq.addValue(10L);
        freq.addValue(20L);
        freq.addValue(10L);
        assertEquals(2L, freq.getCount(10L));
        assertEquals(1L, freq.getCount(20L));
    }

    @Test(timeout = 4000)
    public void testAddAndGetCount_Char() {
        Frequency freq = new Frequency();
        freq.addValue('a');
        freq.addValue('b');
        freq.addValue('a');
        assertEquals(2L, freq.getCount('a'));
        assertEquals(1L, freq.getCount('b'));
    }

    @Test(timeout = 4000)
    public void testAddAndGetCount_String() {
        Frequency freq = new Frequency();
        freq.addValue("foo");
        freq.addValue("bar");
        freq.addValue("foo");
        assertEquals(2L, freq.getCount("foo"));
        assertEquals(1L, freq.getCount("bar"));
    }

    @Test(timeout = 4000)
    public void testGetCount_ObjectWithInteger() {
        Frequency freq = new Frequency();
        freq.addValue(5);
        // getCount(Object) with Integer should convert to Long
        assertEquals(1L, freq.getCount((Object) Integer.valueOf(5)));
    }

    @Test(timeout = 4000)
    public void testGetSumFreq() {
        Frequency freq = new Frequency();
        assertEquals(0L, freq.getSumFreq());
        freq.addValue(1);
        freq.addValue(2);
        freq.addValue(1);
        assertEquals(3L, freq.getSumFreq());
    }

    @Test(timeout = 4000)
    public void testClear() {
        Frequency freq = new Frequency();
        freq.addValue(1);
        freq.addValue(2);
        freq.clear();
        assertEquals(0L, freq.getSumFreq());
        assertEquals(0L, freq.getCount(1));
    }

    @Test(timeout = 4000)
    public void testValuesIterator() {
        Frequency freq = new Frequency();
        freq.addValue(2);
        freq.addValue(1);
        Iterator iter = freq.valuesIterator();
        assertTrue(iter.hasNext());
        assertEquals(Long.valueOf(1), iter.next());
        assertTrue(iter.hasNext());
        assertEquals(Long.valueOf(2), iter.next());
        assertFalse(iter.hasNext());
    }

    @Test(timeout = 4000)
    public void testToString() {
        Frequency freq = new Frequency();
        freq.addValue(1);
        String result = freq.toString();
        assertTrue(result.contains("Value \t Freq. \t Pct. \t Cum Pct."));
        assertTrue(result.contains("1"));
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testEmptyFrequency() {
        Frequency freq = new Frequency();
        assertEquals(0L, freq.getSumFreq());
        assertEquals(0L, freq.getCount(1));
        assertEquals(Double.NaN, freq.getPct(1), 0.0);
        assertEquals(Double.NaN, freq.getCumPct(1), 0.0);
        assertEquals(0L, freq.getCumFreq(1));
    }

    @Test(timeout = 4000)
    public void testLargeValues() {
        Frequency freq = new Frequency();
        freq.addValue(Long.MAX_VALUE);
        freq.addValue(Long.MIN_VALUE);
        assertEquals(1L, freq.getCount(Long.MAX_VALUE));
        assertEquals(1L, freq.getCount(Long.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testNegativeValues() {
        Frequency freq = new Frequency();
        freq.addValue(-1);
        freq.addValue(-2);
        freq.addValue(-1);
        assertEquals(2L, freq.getCount(-1));
        assertEquals(1L, freq.getCount(-2));
    }

    @Test(timeout = 4000)
    public void testZeroValues() {
        Frequency freq = new Frequency();
        freq.addValue(0);
        freq.addValue(0);
        assertEquals(2L, freq.getCount(0));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Directly targets the known defect: adding a non-Comparable object
     * (e.g., Object) should throw IllegalArgumentException, not ClassCastException.
     * The deprecated addValue(Object) method casts to Comparable<?> without checking.
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddNonComparable() {
        Frequency freq = new Frequency();
        // Object is not Comparable -> should trigger IllegalArgumentException
        freq.addValue(new Object());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddNonComparableViaDeprecated() {
        Frequency freq = new Frequency();
        // Using deprecated addValue(Object) – same underlying code path
        freq.addValue((Object) new Object());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddNonComparableAfterValidAdd() {
        Frequency freq = new Frequency();
        freq.addValue("valid");
        // Adding a non-Comparable after valid entries should also throw
        freq.addValue(new Object());
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testGetCountWithNonComparable() {
        Frequency freq = new Frequency();
        freq.addValue(1);
        // getCount with non-Comparable should return 0 (ClassCastException caught)
        assertEquals(0L, freq.getCount(new Object()));
    }

    @Test(timeout = 4000)
    public void testGetCumFreqWithNonComparable() {
        Frequency freq = new Frequency();
        freq.addValue(1);
        // getCumFreq with non-Comparable should return 0
        assertEquals(0L, freq.getCumFreq(new Object()));
    }

    @Test(timeout = 4000)
    public void testGetPctWithNonComparable() {
        Frequency freq = new Frequency();
        freq.addValue(1);
        // getPct with non-Comparable should return 0.0 (since getCount returns 0)
        assertEquals(0.0, freq.getPct(new Object()), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetCumPctWithNonComparable() {
        Frequency freq = new Frequency();
        freq.addValue(1);
        // getCumPct with non-Comparable should return 0.0
        assertEquals(0.0, freq.getCumPct(new Object()), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetCumFreqEmpty() {
        Frequency freq = new Frequency();
        assertEquals(0L, freq.getCumFreq(1));
    }

    @Test(timeout = 4000)
    public void testGetCumFreqLessThanFirst() {
        Frequency freq = new Frequency();
        freq.addValue(10);
        freq.addValue(20);
        // value less than first key (10) -> should return 0
        assertEquals(0L, freq.getCumFreq(5));
    }

    @Test(timeout = 4000)
    public void testGetCumFreqGreaterThanLast() {
        Frequency freq = new Frequency();
        freq.addValue(10);
        freq.addValue(20);
        // value greater than last key (20) -> should return sumFreq (2)
        assertEquals(2L, freq.getCumFreq(30));
    }

    @Test(timeout = 4000)
    public void testGetCumFreqBetweenValues() {
        Frequency freq = new Frequency();
        freq.addValue(10);
        freq.addValue(20);
        freq.addValue(10);
        // value between 10 and 20 -> cumulative freq of values <= 15 = count(10) = 2
        assertEquals(2L, freq.getCumFreq(15));
    }

    @Test(timeout = 4000)
    public void testGetCumFreqEqualToExisting() {
        Frequency freq = new Frequency();
        freq.addValue(10);
        freq.addValue(20);
        freq.addValue(10);
        // value equal to 10 -> cumulative freq = count(10) = 2
        assertEquals(2L, freq.getCumFreq(10));
    }

    @Test(timeout = 4000)
    public void testGetCumFreqWithCustomComparator() {
        // Use reverse order comparator
        Comparator<Integer> reverse = (a, b) -> b.compareTo(a);
        Frequency freq = new Frequency(reverse);
        freq.addValue(10);
        freq.addValue(20);
        freq.addValue(10);
        // With reverse comparator, firstKey=20, lastKey=10
        // value 15: compare(15, firstKey=20) > 0? Actually reverse: 15 vs 20 -> 20>15 -> compare returns positive? Let's compute: b.compareTo(a) => 20.compareTo(15)=5 >0, so c.compare(15,20)=5>0 => not <0.
        // Then compare(15, lastKey=10): 10.compareTo(15)=-5 => c.compare(15,10) = -5 <0 => not >=0.
        // Then iterate: keys in order: 20,10. For 20: c.compare(15,20)=5>0 => result+=count(20)=1. Next 10: c.compare(15,10)=-5 <=0 => return result=1.
        assertEquals(1L, freq.getCumFreq(15));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testMultipleTypesConsistency() {
        Frequency freq = new Frequency();
        freq.addValue(1);          // int -> Long
        freq.addValue(1L);         // long -> Long
        freq.addValue(Integer.valueOf(1)); // Integer -> Long
        // All three should be treated as same value
        assertEquals(3L, freq.getCount(1));
        assertEquals(3L, freq.getCount(1L));
        assertEquals(3L, freq.getCount(Integer.valueOf(1)));
    }

    @Test(timeout = 4000)
    public void testCharacterAndIntegerDistinct() {
        Frequency freq = new Frequency();
        freq.addValue('1');   // Character
        freq.addValue(1);     // Long
        // They should be distinct because Character and Long are not comparable
        assertEquals(1L, freq.getCount('1'));
        assertEquals(1L, freq.getCount(1));
    }

    @Test(timeout = 4000)
    public void testGetPctAndCumPct() {
        Frequency freq = new Frequency();
        freq.addValue(1);
        freq.addValue(2);
        freq.addValue(1);
        assertEquals(2.0/3.0, freq.getPct(1), 1e-15);
        assertEquals(1.0/3.0, freq.getPct(2), 1e-15);
        assertEquals(2.0/3.0, freq.getCumPct(1), 1e-15);
        assertEquals(1.0, freq.getCumPct(2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetPctEmpty() {
        Frequency freq = new Frequency();
        assertEquals(Double.NaN, freq.getPct(1), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetCumPctEmpty() {
        Frequency freq = new Frequency();
        assertEquals(Double.NaN, freq.getCumPct(1), 0.0);
    }

    @Test(timeout = 4000)
    public void testConstructorWithComparator() {
        Comparator<String> comp = String.CASE_INSENSITIVE_ORDER;
        Frequency freq = new Frequency(comp);
        freq.addValue("A");
        freq.addValue("a");
        // Case-insensitive: both "A" and "a" are considered same
        assertEquals(2L, freq.getCount("A"));
        assertEquals(2L, freq.getCount("a"));
    }

    @Test(timeout = 4000)
    public void testNaturalComparatorUsedWhenNull() {
        // Indirectly test NaturalComparator via getCumFreq with no custom comparator
        Frequency freq = new Frequency();
        freq.addValue(10);
        freq.addValue(20);
        // getCumFreq uses NaturalComparator internally
        assertEquals(1L, freq.getCumFreq(15));
    }
}