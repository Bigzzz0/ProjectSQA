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
package org.apache.commons.collections4.keyvalue;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * 1. Constructor Branches:
 *    - MultiKey(K, K): fixed-size 2, cloned=false.
 *    - MultiKey(K, K, K): fixed-size 3, cloned=false.
 *    - MultiKey(K, K, K, K): fixed-size 4, cloned=false.
 *    - MultiKey(K, K, K, K, K): fixed-size 5, cloned=false.
 *    - MultiKey(K[]): delegates with makeClone=true.
 *    - MultiKey(K[], boolean):
 *        * keys == null -> IllegalArgumentException
 *        * makeClone == true -> this.keys = keys.clone()
 *        * makeClone == false -> this.keys = keys (direct assignment)
 * 2. Hash Calculation & calculateHashCode:
 *    - Iteration over keys: key == null (ignored in XOR), key != null (XORed into total).
 * 3. Accessors:
 *    - getKeys(): returns clone, defensive copy verification.
 *    - getKey(int): valid index, boundary index (0, size - 1), out of bounds (< 0, >= size).
 *    - size(): verifies length of array.
 * 4. Contractual Methods:
 *    - equals(Object):
 *        * other == this (true)
 *        * other == null (false)
 *        * other is not instanceof MultiKey (false)
 *        * other is MultiKey with equal elements (true)
 *        * other is MultiKey with different lengths / elements (false)
 *    - hashCode(): returns cached XOR value.
 *    - toString(): returns "MultiKey" + Arrays.toString(keys).
 * 5. Serialization & Defect Zone (Defects4J Ground Truth):
 *    - Base MultiKey serialization: readResolve() restores transient hashCode.
 *    - Subclassed MultiKey serialization: readResolve() declared private prevents
 *      subclasses from inheriting hash code recalculation on deserialization, leaving
 *      transient hashCode as 0 instead of the expected value.
 * =========================================================================
 */
public class MultiKeyGeminiTest {

    // Helper subclass for testing inheritance and serialization lifecycle
    static class DerivedMultiKey<K> extends MultiKey<K> {
        private static final long serialVersionUID = 1L;

        public DerivedMultiKey(final K key1, final K key2) {
            super(key1, key2);
        }

        public DerivedMultiKey(final K[] keys, final boolean makeClone) {
            super(keys, makeClone);
        }
    }

    private static Object serializeAndDeserialize(final Object obj) throws Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
        }
        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            return ois.readObject();
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTwoKeyConstructorAndAccessors() {
        final MultiKey<String> mk = new MultiKey<>("A", "B");
        assertEquals(2, mk.size());
        assertEquals("A", mk.getKey(0));
        assertEquals("B", mk.getKey(1));
        assertArrayEquals(new String[]{"A", "B"}, mk.getKeys());
    }

    @Test(timeout = 4000)
    public void testThreeKeyConstructorAndAccessors() {
        final MultiKey<String> mk = new MultiKey<>("A", "B", "C");
        assertEquals(3, mk.size());
        assertEquals("A", mk.getKey(0));
        assertEquals("B", mk.getKey(1));
        assertEquals("C", mk.getKey(2));
        assertArrayEquals(new String[]{"A", "B", "C"}, mk.getKeys());
    }

    @Test(timeout = 4000)
    public void testFourKeyConstructorAndAccessors() {
        final MultiKey<Integer> mk = new MultiKey<>(1, 2, 3, 4);
        assertEquals(4, mk.size());
        assertEquals(Integer.valueOf(1), mk.getKey(0));
        assertEquals(Integer.valueOf(2), mk.getKey(1));
        assertEquals(Integer.valueOf(3), mk.getKey(2));
        assertEquals(Integer.valueOf(4), mk.getKey(3));
        assertArrayEquals(new Integer[]{1, 2, 3, 4}, mk.getKeys());
    }

    @Test(timeout = 4000)
    public void testFiveKeyConstructorAndAccessors() {
        final MultiKey<String> mk = new MultiKey<>("1", "2", "3", "4", "5");
        assertEquals(5, mk.size());
        assertEquals("1", mk.getKey(0));
        assertEquals("2", mk.getKey(1));
        assertEquals("3", mk.getKey(2));
        assertEquals("4", mk.getKey(3));
        assertEquals("5", mk.getKey(4));
        assertArrayEquals(new String[]{"1", "2", "3", "4", "5"}, mk.getKeys());
    }

    @Test(timeout = 4000)
    public void testArrayConstructorClonedByDefault() {
        final Integer[] source = new Integer[]{10, 20, 30};
        final MultiKey<Integer> mk = new MultiKey<>(source);
        assertEquals(3, mk.size());

        source[0] = 999;
        assertEquals(Integer.valueOf(10), mk.getKey(0));
    }

    @Test(timeout = 4000)
    public void testArrayConstructorExplicitNoClone() {
        final Integer[] source = new Integer[]{10, 20, 30};
        final MultiKey<Integer> mk = new MultiKey<>(source, false);
        assertEquals(3, mk.size());

        source[0] = 999;
        assertEquals(Integer.valueOf(999), mk.getKey(0));
    }

    @Test(timeout = 4000)
    public void testArrayConstructorExplicitClone() {
        final Integer[] source = new Integer[]{100, 200};
        final MultiKey<Integer> mk = new MultiKey<>(source, true);

        source[0] = 500;
        assertEquals(Integer.valueOf(100), mk.getKey(0));
    }

    @Test(timeout = 4000)
    public void testGetKeysReturnsDefensiveCopy() {
        final MultiKey<String> mk = new MultiKey<>("alpha", "beta");
        final String[] returnedKeys = mk.getKeys();
        returnedKeys[0] = "mutated";
        assertEquals("alpha", mk.getKey(0));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyArrayKeys() {
        final MultiKey<String> mk = new MultiKey<>(new String[0]);
        assertEquals(0, mk.size());
        assertEquals(0, mk.hashCode());
        assertEquals("MultiKey[]", mk.toString());
        assertEquals(0, mk.getKeys().length);
    }

    @Test(timeout = 4000)
    public void testSingleElementArray() {
        final MultiKey<String> mk = new MultiKey<>(new String[]{"solo"});
        assertEquals(1, mk.size());
        assertEquals("solo", mk.getKey(0));
        assertEquals("solo".hashCode(), mk.hashCode());
        assertEquals("MultiKey[solo]", mk.toString());
    }

    @Test(timeout = 4000)
    public void testKeysContainingNullValues() {
        final MultiKey<String> mk = new MultiKey<>(null, "value", null);
        assertEquals(3, mk.size());
        assertNull(mk.getKey(0));
        assertEquals("value", mk.getKey(1));
        assertNull(mk.getKey(2));

        final int expectedHash = "value".hashCode();
        assertEquals(expectedHash, mk.hashCode());
        assertEquals("MultiKey[null, value, null]", mk.toString());
    }

    @Test(timeout = 4000)
    public void testAllNullKeys() {
        final MultiKey<Object> mk = new MultiKey<>(null, null, null, null);
        assertEquals(4, mk.size());
        for (int i = 0; i < 4; i++) {
            assertNull(mk.getKey(i));
        }
        assertEquals(0, mk.hashCode());
        assertEquals("MultiKey[null, null, null, null]", mk.toString());
    }

    @Test(timeout = 4000)
    public void testHashCodeCalculatedCorrectlyWithMultipleValues() {
        final Integer k1 = 1;
        final Integer k2 = 2;
        final MultiKey<Integer> mk = new MultiKey<>(k1, k2);
        final int expectedHashCode = k1.hashCode() ^ k2.hashCode();
        assertEquals(expectedHashCode, mk.hashCode());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug where readResolve is private in MultiKey.
     * When a subclass is serialized and deserialized, private readResolve() is
     * not invoked by Java serialization, resulting in a zero hashCode.
     */
    @Test(timeout = 4000)
    public void testEqualsAfterSerializationOfDerivedClass() throws Exception {
        final Integer k1 = 1;
        final Integer k2 = 2;
        final DerivedMultiKey<Integer> derived = new DerivedMultiKey<>(k1, k2);
        final int originalHash = derived.hashCode();
        assertEquals(k1.hashCode() ^ k2.hashCode(), originalHash);

        @SuppressWarnings("unchecked")
        final DerivedMultiKey<Integer> deserialized =
                (DerivedMultiKey<Integer>) serializeAndDeserialize(derived);

        assertEquals(derived, deserialized);
        assertEquals(originalHash, deserialized.hashCode());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullArrayConstructorThrowsException() {
        new MultiKey<Object>((Object[]) null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullArrayWithCloneFlagThrowsException() {
        new MultiKey<Object>((Object[]) null, true);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullArrayWithoutCloneFlagThrowsException() {
        new MultiKey<Object>((Object[]) null, false);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetKeyNegativeIndexThrowsException() {
        final MultiKey<String> mk = new MultiKey<>("a", "b");
        mk.getKey(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetKeyIndexEqualsSizeThrowsException() {
        final MultiKey<String> mk = new MultiKey<>("a", "b");
        mk.getKey(2);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetKeyIndexGreaterThanSizeThrowsException() {
        final MultiKey<String> mk = new MultiKey<>("a", "b");
        mk.getKey(5);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsContract() {
        final MultiKey<String> mk1 = new MultiKey<>("A", "B");
        final MultiKey<String> mk2 = new MultiKey<>("A", "B");
        final MultiKey<String> mk3 = new MultiKey<>("A", "C");
        final MultiKey<String> mkDiffLen = new MultiKey<>("A", "B", "C");

        // Reflexive
        assertEquals(mk1, mk1);

        // Symmetric
        assertEquals(mk1.equals(mk2), mk2.equals(mk1));
        assertTrue(mk1.equals(mk2));

        // Inequality with different elements
        assertNotEquals(mk1, mk3);
        assertNotEquals(mk2, mk3);

        // Inequality with different lengths
        assertNotEquals(mk1, mkDiffLen);

        // Incompatible types
        assertFalse(mk1.equals("Not a MultiKey"));
        assertFalse(mk1.equals(null));
    }

    @Test(timeout = 4000)
    public void testHashCodeContract() {
        final MultiKey<String> mk1 = new MultiKey<>("X", "Y");
        final MultiKey<String> mk2 = new MultiKey<>("X", "Y");
        assertEquals(mk1, mk2);
        assertEquals(mk1.hashCode(), mk2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToStringContract() {
        final MultiKey<String> mk = new MultiKey<>("one", "two");
        assertEquals("MultiKey[one, two]", mk.toString());
    }

    @Test(timeout = 4000)
    public void testBaseClassSerializationReadResolve() throws Exception {
        final MultiKey<String> mk = new MultiKey<>("first", "second");
        final int originalHash = mk.hashCode();

        @SuppressWarnings("unchecked")
        final MultiKey<String> deserialized = (MultiKey<String>) serializeAndDeserialize(mk);

        assertEquals(mk, deserialized);
        assertEquals(originalHash, deserialized.hashCode());
        assertEquals("first", deserialized.getKey(0));
        assertEquals("second", deserialized.getKey(1));
    }
}