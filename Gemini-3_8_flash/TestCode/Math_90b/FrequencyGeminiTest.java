package org.apache.commons.math.stat;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.math.stat.Frequency
 * Defect Under Analysis:
 *   - org.apache.commons.math.stat.FrequencyTest::testAddNonComparable
 *     Root Cause: addValue(Object v) catches ClassCastException thrown by TreeMap and rethrows
 *     IllegalArgumentException("Value not comparable to existing values.") even when the object
 *     itself violates the contract by not implementing Comparable. The class specification / javadoc
 *     declares: "@throws ClassCastException if v is not Comparable" and
 *     "@throws IllegalArgumentException if v is not comparable with previous entries".
 *
 * Branch & Condition Coverage Matrix:
 *   1. addValue(Object/int/Integer/long/char):
 *      - v instanceof Integer -> converted to Long (Branch: true / false)
 *      - freqTable.get(obj) == null -> put 1L (Branch: true / false)
 *      - freqTable.get(obj) != null -> put count + 1 (Branch: true / false)
 *      - catch ClassCastException -> throw IllegalArgumentException (Branch: caught)
 *   2. getSumFreq():
 *      - freqTable empty -> return 0 (Branch: empty / non-empty)
 *      - sum calculation loop -> multiple elements accumulation
 *   3. getCount(Object/int/Integer/long/char):
 *      - v instanceof Integer -> unbox to long (Branch: true / false)
 *      - count != null vs count == null (Branch: true / false)
 *      - catch ClassCastException -> return 0 (Branch: caught / not thrown)
 *   4. getPct(Object/int/long/char):
 *      - sumFreq == 0 -> return Double.NaN (Branch: true / false)
 *      - sumFreq > 0 -> compute ratio
 *   5. getCumFreq(Object/int/long/char):
 *      - sumFreq == 0 -> return 0 (Branch: true / false)
 *      - v instanceof Integer -> delegate to long (Branch: true / false)
 *      - comparator == null -> fallback to NaturalComparator (Branch: true / false)
 *      - catch ClassCastException on freqTable.get(v) -> return 0 (Branch: caught)
 *      - c.compare(v, firstKey()) < 0 -> return 0 (Branch: true / false)
 *      - c.compare(v, lastKey()) >= 0 -> return getSumFreq() (Branch: true / false)
 *      - while (values.hasNext()) -> loop through entries
 *      - c.compare(v, nextValue) > 0 -> accumulate count (Branch: true / false)
 *      - c.compare(v, nextValue) <= 0 -> early return (Branch: true / false)
 *   6. getCumPct(Object/int/long/char):
 *      - sumFreq == 0 -> return Double.NaN (Branch: true / false)
 *      - sumFreq > 0 -> compute ratio
 *   7. toString():
 *      - empty table -> header only
 *      - populated table -> formatted rows with tab spacing and percentages
 *   8. Lifecycle & Contracts:
 *      - clear() -> empties the underlying structure
 *      - valuesIterator() -> keyset iterator
 *      - Serializable integrity with default and custom comparators
 * ----------------------------------------------------------------------------------------------------
 */
public class FrequencyGeminiTest {

    private static final double DELTA = 1e-15;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testIntegralTypeInterchangeability() {
        Frequency f = new Frequency();
        f.addValue(10);                    // int
        f.addValue(Long.valueOf(10L));     // Long
        f.addValue(10L);                   // long
        f.addValue(Integer.valueOf(10));   // Integer

        assertEquals("Integral types should be unified as Long", 4L, f.getCount(10));
        assertEquals(4L, f.getCount(10L));
        assertEquals(4L, f.getCount(Integer.valueOf(10)));
        assertEquals(4L, f.getCount(Long.valueOf(10L)));
        assertEquals(4L, f.getSumFreq());
        assertEquals(1.0, f.getPct(10), DELTA);
        assertEquals(1.0, f.getCumPct(10L), DELTA);
        assertEquals(4L, f.getCumFreq(10));
    }

    @Test(timeout = 4000)
    public void testCharSupport() {
        Frequency f = new Frequency();
        f.addValue('a');
        f.addValue('b');
        f.addValue('a');
        f.addValue(Character.valueOf('c'));

        assertEquals(2L, f.getCount('a'));
        assertEquals(1L, f.getCount('b'));
        assertEquals(1L, f.getCount('c'));
        assertEquals(0L, f.getCount('z'));
        assertEquals(4L, f.getSumFreq());

        assertEquals(0.5, f.getPct('a'), DELTA);
        assertEquals(0.25, f.getPct('b'), DELTA);
        assertEquals(2L, f.getCumFreq('a'));
        assertEquals(3L, f.getCumFreq('b'));
        assertEquals(4L, f.getCumFreq('c'));
        assertEquals(0.75, f.getCumPct('b'), DELTA);
    }

    @Test(timeout = 4000)
    public void testCumulativeFrequencyComputationIntermediate() {
        Frequency f = new Frequency();
        f.addValue(10L);
        f.addValue(20L);
        f.addValue(30L);
        f.addValue(40L);

        // Value strictly less than first key
        assertEquals(0L, f.getCumFreq(5L));
        assertEquals(0.0, f.getCumPct(5L), DELTA);

        // Value equal to first key
        assertEquals(1L, f.getCumFreq(10L));
        assertEquals(0.25, f.getCumPct(10L), DELTA);

        // Value between keys (not present in table)
        assertEquals(2L, f.getCumFreq(25L));
        assertEquals(0.5, f.getCumPct(25L), DELTA);

        // Value equal to intermediate key
        assertEquals(3L, f.getCumFreq(30L));
        assertEquals(0.75, f.getCumPct(30L), DELTA);

        // Value equal to last key
        assertEquals(4L, f.getCumFreq(40L));
        assertEquals(1.0, f.getCumPct(40L), DELTA);

        // Value strictly greater than last key
        assertEquals(4L, f.getCumFreq(50L));
        assertEquals(1.0, f.getCumPct(50L), DELTA);
    }

    @Test(timeout = 4000)
    public void testClearAndReuse() {
        Frequency f = new Frequency();
        f.addValue(1);
        f.addValue(2);
        assertEquals(2L, f.getSumFreq());

        f.clear();

        assertEquals(0L, f.getSumFreq());
        assertEquals(0L, f.getCount(1));
        assertTrue(Double.isNaN(f.getPct(1)));
        assertEquals(0L, f.getCumFreq(1));
        assertTrue(Double.isNaN(f.getCumPct(1)));

        f.addValue(5);
        assertEquals(1L, f.getSumFreq());
        assertEquals(1L, f.getCount(5));
    }

    @Test(timeout = 4000)
    public void testCustomComparator() {
        // Reverse order comparator for Strings
        Frequency f = new Frequency(Collections.reverseOrder());
        f.addValue("Z");
        f.addValue("A");
        f.addValue("M");

        assertEquals(3L, f.getSumFreq());
        // In reverse order, "Z" comes first
        assertEquals(1L, f.getCumFreq("Z"));
        assertEquals(2L, f.getCumFreq("M"));
        assertEquals(3L, f.getCumFreq("A"));
        // "ZZ" is greater than "Z" naturally, so in reverse order it is less than "Z" (before firstKey)
        assertEquals(0L, f.getCumFreq("ZZ"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyDistribution() {
        Frequency f = new Frequency();

        assertEquals(0L, f.getSumFreq());
        assertEquals(0L, f.getCount(0));
        assertEquals(0L, f.getCount(0L));
        assertEquals(0L, f.getCount('a'));
        assertEquals(0L, f.getCount("any"));

        assertTrue(Double.isNaN(f.getPct(0)));
        assertTrue(Double.isNaN(f.getPct(0L)));
        assertTrue(Double.isNaN(f.getPct('a')));
        assertTrue(Double.isNaN(f.getPct("any")));

        assertEquals(0L, f.getCumFreq(0));
        assertEquals(0L, f.getCumFreq(0L));
        assertEquals(0L, f.getCumFreq('a'));
        assertEquals(0L, f.getCumFreq("any"));

        assertTrue(Double.isNaN(f.getCumPct(0)));
        assertTrue(Double.isNaN(f.getCumPct(0L)));
        assertTrue(Double.isNaN(f.getCumPct('a')));
        assertTrue(Double.isNaN(f.getCumPct("any")));
    }

    @Test(timeout = 4000)
    public void testExtremeBoundaryValues() {
        Frequency f = new Frequency();
        f.addValue(Long.MIN_VALUE);
        f.addValue(0L);
        f.addValue(Long.MAX_VALUE);

        assertEquals(1L, f.getCount(Long.MIN_VALUE));
        assertEquals(1L, f.getCount(0L));
        assertEquals(1L, f.getCount(Long.MAX_VALUE));
        assertEquals(3L, f.getSumFreq());

        assertEquals(1L, f.getCumFreq(Long.MIN_VALUE));
        assertEquals(2L, f.getCumFreq(0L));
        assertEquals(3L, f.getCumFreq(Long.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testExtremeCharBoundaries() {
        Frequency f = new Frequency();
        f.addValue(Character.MIN_VALUE);
        f.addValue(Character.MAX_VALUE);

        assertEquals(1L, f.getCount(Character.MIN_VALUE));
        assertEquals(1L, f.getCount(Character.MAX_VALUE));
        assertEquals(2L, f.getSumFreq());

        assertEquals(1L, f.getCumFreq(Character.MIN_VALUE));
        assertEquals(2L, f.getCumFreq(Character.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testIntegerBoundaries() {
        Frequency f = new Frequency();
        f.addValue(Integer.MIN_VALUE);
        f.addValue(Integer.MAX_VALUE);

        assertEquals(1L, f.getCount(Integer.MIN_VALUE));
        assertEquals(1L, f.getCount(Integer.MAX_VALUE));
        assertEquals(2L, f.getCumFreq(Integer.MAX_VALUE));
        assertEquals(1.0, f.getCumPct(Integer.MAX_VALUE), DELTA);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets the documented Defects4J defect:
     * org.apache.commons.math.stat.FrequencyTest::testAddNonComparable
     *
     * The contract dictates that adding an object that is NOT Comparable must throw
     * a ClassCastException (as explicitly specified in the Javadoc). The defective
     * implementation caught ClassCastException and inappropriately re-threw
     * IllegalArgumentException("Value not comparable to existing values.").
     */
    @Test(expected = ClassCastException.class, timeout = 4000)
    public void testAddNonComparable() {
        Frequency f = new Frequency();
        f.addValue(new Object());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddIncompatibleComparableTypesThrowsIllegalArgumentException() {
        Frequency f = new Frequency();
        f.addValue("StringValue");
        // Long is Comparable, but not mutually comparable to String in natural order
        f.addValue(100L);
    }

    @Test(timeout = 4000)
    public void testGetCountWithIncompatibleTypeReturnsZero() {
        Frequency f = new Frequency();
        f.addValue("Alpha");
        f.addValue("Beta");

        // Lookup with an incompatible type should not crash; must return 0
        assertEquals(0L, f.getCount(100L));
        assertEquals(0.0, f.getPct(100L), DELTA);
    }

    @Test(timeout = 4000)
    public void testGetCumFreqWithIncompatibleTypeReturnsZero() {
        Frequency f = new Frequency();
        f.addValue("Alpha");
        f.addValue("Beta");

        // getCumFreq on an incompatible type should safely return 0
        assertEquals(0L, f.getCumFreq(100L));
        assertEquals(0.0, f.getCumPct(100L), DELTA);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Iteration & Serialization Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testValuesIterator() {
        Frequency f = new Frequency();
        f.addValue(30);
        f.addValue(10);
        f.addValue(20);

        Iterator it = f.valuesIterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        // Natural ordering for integral values converted to Long
        assertEquals(Long.valueOf(10L), it.next());
        assertEquals(Long.valueOf(20L), it.next());
        assertEquals(Long.valueOf(30L), it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testToStringFormatting() {
        Frequency f = new Frequency();
        String emptyString = f.toString();
        assertNotNull(emptyString);
        assertTrue(emptyString.startsWith("Value \t Freq. \t Pct. \t Cum Pct. \n"));

        f.addValue("A");
        f.addValue("B");
        String populatedString = f.toString();
        assertTrue(populatedString.contains("A\t1\t50%"));
        assertTrue(populatedString.contains("B\t1\t50%"));
    }

    @Test(timeout = 4000)
    public void testSerializationWithDefaultComparator() throws Exception {
        Frequency original = new Frequency();
        original.addValue(10);
        original.addValue(20);
        original.addValue(10);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Frequency deserialized = (Frequency) ois.readObject();
        ois.close();

        assertEquals(original.getSumFreq(), deserialized.getSumFreq());
        assertEquals(original.getCount(10), deserialized.getCount(10));
        assertEquals(original.getCount(20), deserialized.getCount(20));
        assertEquals(original.getCumFreq(15), deserialized.getCumFreq(15));
        assertEquals(original.getCumPct(20), deserialized.getCumPct(20), DELTA);
    }

    @Test(timeout = 4000)
    public void testSerializationWithCustomComparator() throws Exception {
        Frequency original = new Frequency(String.CASE_INSENSITIVE_ORDER);
        original.addValue("alpha");
        original.addValue("ALPHA");
        original.addValue("beta");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Frequency deserialized = (Frequency) ois.readObject();
        ois.close();

        assertEquals(2L, deserialized.getCount("Alpha"));
        assertEquals(1L, deserialized.getCount("BETA"));
        assertEquals(3L, deserialized.getSumFreq());
    }
}