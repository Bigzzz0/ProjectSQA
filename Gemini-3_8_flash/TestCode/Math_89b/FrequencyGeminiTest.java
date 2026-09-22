package org.apache.commons.math.stat;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Iterator;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target: org.apache.commons.math.stat.Frequency
 *
 * 1. BRANCH COVERAGE STRATEGY:
 * - addValue(Comparable<?> v):
 *   * Branch 1: v instanceof Integer -> convert to Long
 *   * Branch 2: v not instanceof Integer -> use as-is
 *   * Branch 3: count == null -> put Long.valueOf(1)
 *   * Branch 4: count != null -> put Long.valueOf(count + 1)
 *   * Branch 5: ClassCastException caught -> throw IllegalArgumentException
 * - addValue overloads:
 *   * int, Integer, long, char, Object
 * - clear(): reset state
 * - valuesIterator(): natural order iteration over keys
 * - getSumFreq(): sum up counts from empty to multiple elements
 * - getCount(Object v):
 *   * Branch 1: v instanceof Integer -> delegate to long
 *   * Branch 2: count != null -> return count
 *   * Branch 3: count == null -> return 0
 *   * Branch 4: ClassCastException caught -> return 0
 * - getCount overloads: int, long, char
 * - getPct(Object v):
 *   * Branch 1: sumFreq == 0 -> return Double.NaN
 *   * Branch 2: sumFreq > 0 -> return count / sumFreq
 * - getCumFreq(Object v):
 *   * Branch 1: sumFreq == 0 -> return 0
 *   * Branch 2: v instanceof Integer -> delegate to long
 *   * Branch 3: comparator == null -> instantiate NaturalComparator
 *   * Branch 4: comparator != null -> use custom comparator
 *   * Branch 5: freqTable.get(v) throws ClassCastException -> return 0
 *   * Branch 6: c.compare(v, firstKey()) < 0 -> return 0
 *   * Branch 7: c.compare(v, lastKey()) >= 0 -> return sumFreq
 *   * Branch 8: iteration loop: c.compare(v, nextValue) > 0 vs <= 0 (accumulate vs return)
 * - getCumPct(Object v):
 *   * Branch 1: sumFreq == 0 -> return Double.NaN
 *   * Branch 2: sumFreq > 0 -> return cumFreq / sumFreq
 * - toString(): empty vs populated formatting
 *
 * 2. DEFECT TARGETING (Defects4J Ground Truth):
 * - Bug: FrequencyTest::testAddNonComparable
 *   * addValue(Object v) directly casts `(Comparable<?>) v`, which throws ClassCastException
 *     instead of the contractually specified IllegalArgumentException when v does not
 *     implement Comparable.
 *   * Partition C directly triggers this contract violation.
 * =========================================================================
 */
public class FrequencyGeminiTest {

    private static final double EPSILON = 1e-15;

    // -------------------------------------------------------------------------
    // PARTITION A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCoreIntegralConversionsAndCounts() {
        Frequency freq = new Frequency();
        assertEquals(0L, freq.getSumFreq());

        // Add various integral representations of 1
        freq.addValue(1);                       // int
        freq.addValue(Integer.valueOf(1));      // Integer
        freq.addValue(1L);                      // long
        freq.addValue(Long.valueOf(1L));        // Long

        assertEquals(4L, freq.getSumFreq());
        assertEquals(4L, freq.getCount(1));
        assertEquals(4L, freq.getCount(1L));
        assertEquals(4L, freq.getCount(Integer.valueOf(1)));
        assertEquals(4L, freq.getCount(Long.valueOf(1L)));

        assertEquals(1.0, freq.getPct(1), EPSILON);
        assertEquals(1.0, freq.getPct(1L), EPSILON);
        assertEquals(1.0, freq.getPct(Integer.valueOf(1)), EPSILON);
        assertEquals(1.0, freq.getPct(Long.valueOf(1L)), EPSILON);

        assertEquals(4L, freq.getCumFreq(1));
        assertEquals(4L, freq.getCumFreq(1L));
        assertEquals(4L, freq.getCumFreq(Integer.valueOf(1)));
        assertEquals(4L, freq.getCumFreq(Long.valueOf(1L)));

        assertEquals(1.0, freq.getCumPct(1), EPSILON);
        assertEquals(1.0, freq.getCumPct(1L), EPSILON);
        assertEquals(1.0, freq.getCumPct(Integer.valueOf(1)), EPSILON);
        assertEquals(1.0, freq.getCumPct(Long.valueOf(1L)), EPSILON);
    }

    @Test(timeout = 4000)
    public void testCharOperations() {
        Frequency freq = new Frequency();
        freq.addValue('a');
        freq.addValue('b');
        freq.addValue('b');

        assertEquals(3L, freq.getSumFreq());
        assertEquals(1L, freq.getCount('a'));
        assertEquals(2L, freq.getCount('b'));
        assertEquals(0L, freq.getCount('c'));

        assertEquals(1.0 / 3.0, freq.getPct('a'), EPSILON);
        assertEquals(2.0 / 3.0, freq.getPct('b'), EPSILON);
        assertEquals(0.0, freq.getPct('c'), EPSILON);

        assertEquals(1L, freq.getCumFreq('a'));
        assertEquals(3L, freq.getCumFreq('b'));
        assertEquals(3L, freq.getCumFreq('c'));

        assertEquals(1.0 / 3.0, freq.getCumPct('a'), EPSILON);
        assertEquals(1.0, freq.getCumPct('b'), EPSILON);
        assertEquals(1.0, freq.getCumPct('c'), EPSILON);
    }

    @Test(timeout = 4000)
    public void testCustomComparator() {
        // Case-insensitive order
        Frequency freq = new Frequency(String.CASE_INSENSITIVE_ORDER);
        freq.addValue("abc");
        freq.addValue("ABC");
        freq.addValue("xyz");

        assertEquals(3L, freq.getSumFreq());
        assertEquals(2L, freq.getCount("abc"));
        assertEquals(2L, freq.getCount("ABC"));
        assertEquals(1L, freq.getCount("xyz"));

        assertEquals(2L, freq.getCumFreq("abc"));
        assertEquals(3L, freq.getCumFreq("xyz"));
        assertEquals(3L, freq.getCumFreq("XYZ"));
    }

    @Test(timeout = 4000)
    public void testValuesIteratorOrder() {
        Frequency freq = new Frequency();
        freq.addValue(30);
        freq.addValue(10);
        freq.addValue(20);

        Iterator it = freq.valuesIterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals(Long.valueOf(10L), it.next());
        assertTrue(it.hasNext());
        assertEquals(Long.valueOf(20L), it.next());
        assertTrue(it.hasNext());
        assertEquals(Long.valueOf(30L), it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testClearStateTransition() {
        Frequency freq = new Frequency();
        freq.addValue("one");
        assertEquals(1L, freq.getSumFreq());

        freq.clear();

        assertEquals(0L, freq.getSumFreq());
        assertEquals(0L, freq.getCount("one"));
        assertTrue(Double.isNaN(freq.getPct("one")));
        assertEquals(0L, freq.getCumFreq("one"));
        assertTrue(Double.isNaN(freq.getCumPct("one")));
        assertFalse(freq.valuesIterator().hasNext());
    }

    @Test(timeout = 4000)
    public void testToStringFormatting() {
        Frequency freq = new Frequency();
        String emptyStr = freq.toString();
        assertNotNull(emptyStr);
        assertTrue(emptyStr.startsWith("Value \t Freq. \t Pct. \t Cum Pct