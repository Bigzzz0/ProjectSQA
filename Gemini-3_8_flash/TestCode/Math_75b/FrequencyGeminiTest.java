/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math.stat;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * /* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.math.stat.Frequency
 *
 * Decision / Branch Points Targeted:
 * 1. Constructor: Default (natural comparator) vs custom Comparator (e.g., String.CASE_INSENSITIVE_ORDER).
 * 2. addValue(Object):
 *    - Branch: v instanceof Comparable (delegates to addValue(Comparable))
 *    - Branch: !(v instanceof Comparable) -> throws IllegalArgumentException
 * 3. addValue(Comparable):
 *    - Branch: v instanceof Integer -> unwraps to Long
 *    - Branch: count == null (first insertion: count = 1) vs count != null (increment: count + 1)
 *    - Branch: ClassCastException caught -> rethrown as MathRuntimeException.createIllegalArgumentException
 * 4. Primitive / Wrapper addValue delegates:
 *    - int, Integer, long, char (converts to Long or Character).
 * 5. getCount:
 *    - Branch: v instanceof Integer -> delegates to getCount(long)
 *    - Branch: count == null (returns 0) vs count != null (returns count)
 *    - Branch: ClassCastException caught (incompatible query type) -> returns 0
 * 6. getPct:
 *    - Branch: sumFreq == 0 -> returns Double.NaN
 *    - Branch: sumFreq > 0 -> returns getCount(v) / sumFreq
 *    - Overloads: int, long, char, Comparable, Object
 * 7. getCumFreq:
 *    - Branch: sumFreq == 0 -> returns 0
 *    - Branch: v instanceof Integer -> delegates to getCumFreq(long)
 *    - Branch: comparator == null (falls back to NaturalComparator) vs comparator != null
 *    - Branch: ClassCastException caught on freqTable.get(v) -> returns 0
 *    - Branch: compare(v, firstKey) < 0 -> returns 0
 *    - Branch: compare(v, lastKey) >= 0 -> returns sumFreq
 *    - Loop Branch: compare(v, nextValue) > 0 vs <= 0 (early exit returning accumulated frequency)
 * 8. getCumPct:
 *    - Branch: sumFreq == 0 -> returns Double.NaN
 *    - Branch: sumFreq > 0 -> returns getCumFreq(v) / sumFreq
 * 9. toString(): Empty vs non-empty formatting loop with percent format.
 * 10. equals() & hashCode():
 *     - Reflexive (this == obj)
 *     - Null check (obj == null)
 *     - Type check (!(obj instanceof Frequency))
 *     - State equality (same frequency maps, different maps, null map handling)
 * 11. Serialization: Round-trip state validation.
 *
 * Defects4J Bug Target:
 * - org.apache.commons.math.stat.FrequencyTest::testPcts
 * - Defect: getPct(Object v) mistakenly invokes getCumPct((Comparable<?>) v) instead of getPct((Comparable<?>) v).
 * - Target Test: testDefectTargetedGetPctObjectCall verifies getPct(Object) accurately computes relative frequency,
 *   not cumulative percentage.
 */
public class FrequencyGeminiTest {

    private static final double EPSILON = 1e-9;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicIntegralLifecycle() {
        Frequency f = new Frequency();
        assertEquals(0L, f.getSumFreq());

        f.addValue(10);
        f.addValue(20L);
        f.addValue(Integer.valueOf(10));
        f.addValue(Long.valueOf(30L));

        assertEquals(4L, f.getSumFreq());
        assertEquals(2L, f.getCount(10));
        assertEquals(2L, f.getCount(10L));
        assertEquals(2L, f.getCount(Integer.valueOf(10)));
        assertEquals(1L, f.getCount(20));
        assertEquals(1L, f.getCount(30L));
        assertEquals(0L, f.getCount(40));

        assertEquals(0.5, f.getPct(10), EPSILON);
        assertEquals(0.25, f.getPct(20L), EPSILON);
        assertEquals(0.0, f.getPct(99), EPSILON);

        assertEquals(2L, f.getCumFreq(10));
        assertEquals(3L, f.getCumFreq(20L));
        assertEquals(4L, f.getCumFreq(30));

        assertEquals(0.5, f.getCumPct(10), EPSILON);
        assertEquals(0.75, f.getCumPct(20), EPSILON);
        assertEquals(1.0, f.getCumPct(30L), EPSILON);
    }

    @Test(timeout = 4000)
    public void testCharLifecycle() {
        Frequency f = new Frequency();
        f.addValue('a');
        f.addValue('b');
        f.addValue('b');
        f.addValue(Character.valueOf('c'));

        assertEquals(4L, f.getSumFreq());
        assertEquals(1L, f.getCount('a'));
        assertEquals(2L, f.getCount('b'));
        assertEquals(1L, f.getCount('c'));
        assertEquals(0L, f.getCount('z'));

        assertEquals(0.25, f.getPct('a'), EPSILON);
        assertEquals(0.50, f.getPct('b'), EPSILON);

        assertEquals(1L, f.getCumFreq('a'));
        assertEquals(3L, f.getCumFreq('b'));
        assertEquals(4L, f.getCumFreq('c'));

        assertEquals(0.25, f.getCumPct('a'), EPSILON);
        assertEquals(0.75, f.getCumPct('b'), EPSILON);
        assertEquals(1.00, f.getCumPct('c'), EPSILON);
    }

    @Test(timeout = 4000)
    public void testClearAndValuesIterator() {
        Frequency f = new Frequency();
        f.addValue("alpha");
        f.addValue("beta");
        f.addValue("gamma");

        Iterator<Comparable<?>> it = f.valuesIterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals("alpha", it.next());
        assertEquals("beta", it.next());
        assertEquals("gamma", it.next());
        assertFalse(it.hasNext());

        f.clear();
        assertEquals(0L, f.getSumFreq());
        assertFalse(f.valuesIterator().hasNext());
    }

    @Test(timeout = 4000)
    public void testCustomComparatorCaseInsensitive() {
        Frequency f = new Frequency(String.CASE_INSENSITIVE_ORDER);
        f.addValue("abc");
        f.addValue("ABC");
        f.addValue("aBc");
        f.addValue("def");

        assertEquals(4L, f.getSumFreq());
        assertEquals(3L, f.getCount("abc"));
        assertEquals(3L, f.getCount("ABC"));
        assertEquals(1L, f.getCount("DEF"));
        assertEquals(0.75, f.getPct("ABC"), EPSILON);
        assertEquals(3L, f.getCumFreq("abc"));
        assertEquals(4L, f.getCumFreq("xyz"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyTableQueries() {
        Frequency f = new Frequency();

        assertEquals(0L, f.getSumFreq());
        assertEquals(0L, f.getCount(1));
        assertEquals(0L, f.getCount(1L));
        assertEquals(0L, f.getCount('a'));
        assertEquals(0L, f.getCount("none"));
        assertEquals(0L, f.getCount((Object) "none"));

        assertTrue(Double.isNaN(f.getPct(1)));
        assertTrue(Double.isNaN(f.getPct(1L)));
        assertTrue(Double.isNaN(f.getPct('a')));
        assertTrue(Double.isNaN(f.getPct("none")));
        assertTrue(Double.isNaN(f.getPct((Object) "none")));

        assertEquals(0L, f.getCumFreq(1));
        assertEquals(0L, f.getCumFreq(1L));
        assertEquals(0L, f.getCumFreq('a'));
        assertEquals(0L, f.getCumFreq("none"));
        assertEquals(0L, f.getCumFreq((Object) "none"));

        assertTrue(Double.isNaN(f.getCumPct(1)));
        assertTrue(Double.isNaN(f.getCumPct(1L)));
        assertTrue(Double.isNaN(f.getCumPct('a')));
        assertTrue(Double.isNaN(f.getCumPct("none")));
        assertTrue(Double.isNaN(f.getCumPct((Object) "none")));
    }

    @Test(timeout = 4000)
    public void testCumFreqBoundaryValues() {
        Frequency f = new Frequency();
        f.addValue(10L);
        f.addValue(20L);
        f.addValue(30L);

        // Value strictly less than first key
        assertEquals(0L, f.getCumFreq(5L));
        assertEquals(0.0, f.getCumPct(5L), EPSILON);

        // Value equal to first key
        assertEquals(1L, f.getCumFreq(10L));
        assertEquals(1.0 / 3.0, f.getCumPct(10L), EPSILON);

        // Intermediate value not in table
        assertEquals(1L, f.getCumFreq(15L));
        assertEquals(1.0 / 3.0, f.getCumPct(15L), EPSILON);

        // Value equal to last key
        assertEquals(3L, f.getCumFreq(30L));
        assertEquals(1.0, f.getCumPct(30L), EPSILON);

        // Value strictly greater than last key
        assertEquals(3L, f.getCumFreq(100L));
        assertEquals(1.0, f.getCumPct(100L), EPSILON);
    }

    @Test(timeout = 4000)
    public void testNumericalExtremes() {
        Frequency f = new Frequency();
        f.addValue(Long.MIN_VALUE);
        f.addValue(Long.MAX_VALUE);
        f.addValue(0L);

        assertEquals(3L, f.getSumFreq());
        assertEquals(1L, f.getCount(Long.MIN_VALUE));
        assertEquals(1L, f.getCount(Long.MAX_VALUE));
        assertEquals(1L, f.getCumFreq(Long.MIN_VALUE));
        assertEquals(2L, f.getCumFreq(0L));
        assertEquals(3L, f.getCumFreq(Long.MAX_VALUE));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Bug Detection)
    // =========================================================================

    /**
     * Targets Defects4J bug where getPct(Object) erroneously delegated to getCumPct(Comparable)
     * instead of getPct(Comparable).
     *
     * In this scenario:
     * Values: "one" (1), "two" (1), "three" (2).
     * Total sum = 4.
     * Natural order keys: "one", "three", "two".
     * getCount("three") = 2, so getPct("three") MUST be 2 / 4 = 0.5.
     * However, cumulative frequency for "three" is 3 (keys "one" + "three"),
     * or for "two" is 4 (all keys).
     * The defective code returns getCumPct("three") = 0.75 or similar, failing this assertion.
     */
    @Test(timeout = 4000)
    public void testDefectTargetedGetPctObjectCall() {
        Frequency f = new Frequency();
        f.addValue("one");
        f.addValue("two");
        f.addValue("three");
        f.addValue("three");

        Object threeObj = "three";

        // Defect check: f.getPct((Object) "three") must match f.getPct((Comparable<?>) "three")
        double expectedPct = 0.5;
        double actualPct = f.getPct(threeObj);

        assertEquals("getPct(Object) must return relative frequency (0.5), not cumulative pct",
                expectedPct, actualPct, EPSILON);
    }

    @Test(timeout = 4000)
    public void testDefectTargetedIntegerObjectPct() {
        Frequency f = new Frequency();
        f.addValue(1);
        f.addValue(2);
        f.addValue(3);
        f.addValue(3);

        Object target = Integer.valueOf(3);
        assertEquals(0.5, f.getPct(target), EPSILON);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddNonComparableObjectThrowsException() {
        Frequency f = new Frequency();
        Object nonComparable = new Object();
        try {
            f.addValue(nonComparable);
            fail("Expected IllegalArgumentException when adding non-comparable object");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("does not implement Comparable"));
        }
    }

    @Test(timeout = 4000)
    public void testAddIncompatibleComparableThrowsException() {
        Frequency f = new Frequency();
        f.addValue("hello");
        try {
            // Integer is Comparable, but not comparable to String
            f.addValue(Integer.valueOf(123));
            fail("Expected IllegalArgumentException when adding incompatible Comparable to non-empty table");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("not comparable to existing values"));
        }
    }

    @Test(timeout = 4000)
    public void testIncompatibleLookupReturnsZero() {
        Frequency f = new Frequency();
        f.addValue("alpha");
        f.addValue("beta");

        // getCount with incompatible Comparable should catch ClassCastException and return 0
        assertEquals(0L, f.getCount(100L));
        assertEquals(0L, f.getCount(Integer.valueOf(100)));

        // getCumFreq with incompatible type should return 0
        assertEquals(0L, f.getCumFreq(100L));
        assertEquals(0.0, f.getCumPct(100L), EPSILON);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testToStringFormatting() {
        Frequency f = new Frequency();
        String emptyStr = f.toString();
        assertNotNull(emptyStr);
        assertTrue(emptyStr.startsWith("Value \t Freq. \t Pct. \t Cum Pct. \n"));

        f.addValue("A");
        f.addValue("B");
        String populatedStr = f.toString();
        assertTrue(populatedStr.contains("A\t1\t"));
        assertTrue(populatedStr.contains("B\t1\t"));
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        Frequency f1 = new Frequency();
        Frequency f2 = new Frequency();

        // Reflexive
        assertTrue(f1.equals(f1));
        assertEquals(f1.hashCode(), f1.hashCode());

        // Both empty
        assertTrue(f1.equals(f2));
        assertTrue(f2.equals(f1));
        assertEquals(f1.hashCode(), f2.hashCode());

        // Incompatible object / null
        assertFalse(f1.equals(null));
        assertFalse(f1.equals("Some String"));

        // Unequal content
        f1.addValue(1);
        assertFalse(f1.equals(f2));
        assertFalse(f2.equals(f1));

        f2.addValue(1);
        assertTrue(f1.equals(f2));
        assertEquals(f1.hashCode(), f2.hashCode());

        // Different frequencies for same key
        f1.addValue(1);
        assertFalse(f1.equals(f2));

        // Different keys
        f2.addValue(2);
        assertFalse(f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        Frequency original = new Frequency();
        original.addValue("apple");
        original.addValue("banana");
        original.addValue("banana");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Frequency deserialized = (Frequency) ois.readObject();
        ois.close();

        assertEquals(original, deserialized);
        assertEquals(original.hashCode(), deserialized.hashCode());
        assertEquals(3L, deserialized.getSumFreq());
        assertEquals(2L, deserialized.getCount("banana"));
        assertEquals(0.5, deserialized.getCumPct("apple"), EPSILON);
    }
}