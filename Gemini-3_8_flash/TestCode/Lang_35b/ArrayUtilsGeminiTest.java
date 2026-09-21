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
package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.AbstractMap;
import java.util.Map;

/**
 * /* [Branch & Defect Analysis Matrix]
 * **********************************************************************************************
 * TARGET CLASS: org.apache.commons.lang3.ArrayUtils
 *
 * DEFECT TARGETED:
 * - LANG-571: ArrayUtils.add(T[], T) and ArrayUtils.add(T[], int, T) with both null array and
 *   null element. The contract/specification mandates throwing IllegalArgumentException when
 *   both arguments are null. On the defective implementation, ArrayUtils.add(null, null) returns
 *   new Object[]{null}, which causes java.lang.ClassCastException: [Ljava.lang.Object; cannot
 *   be cast to [L<Type>; when caller assigns to a typed array (e.g. String[]).
 *
 * BRANCH COVERAGE COVERED:
 * - toString: null vs non-null, 1D/2D arrays, custom stringIfNull.
 * - isEquals: nulls, identical arrays, differing lengths, multidimensional arrays.
 * - toMap: null array, Map.Entry items, Object[] pairs, elements < 2 length (IAE), invalid type (IAE).
 * - toArray: generic varargs creation.
 * - clone: null vs non-null for all 8 primitives and Object[].
 * - subarray: null array, startIndex < 0, endIndex > length, startIndex >= endIndex, normal slicing
 *   for Object[] and all 8 primitives (long, int, short, char, byte, double, float, boolean).
 * - isSameLength: null/null, null/empty, null/populated, matching lengths, mismatched lengths for
 *   Object[] and all 8 primitives.
 * - getLength: null vs non-null arrays vs non-array argument (IAE).
 * - isSameType: null checks (IAE on either null), identical types, different types.
 * - reverse: null, empty, single element, even length, odd length for Object[] and all 8 primitives.
 * - indexOf / lastIndexOf / contains:
 *   - Object[]: null array, null search element, non-null search element with type match / mismatch,
 *     startIndex negative, startIndex past bounds, not found, duplicates.
 *   - Primitives: negative startIndex, past-bounds startIndex, bounds checks, found / not-found.
 *   - double / float with tolerances: within tolerance min/max, outside tolerance, negative start.
 * - toPrimitive / toObject: null array, empty array, non-empty array with/without null elements,
 *   unboxing with defaultValue for all 8 primitive pairs.
 * - isEmpty: null vs empty vs non-empty for Object[] and all 8 primitives.
 * - addAll: null/null, null/array2, array1/null, array1/array2, type mismatch ArrayStoreException -> IAE.
 * - add(T[], T) & add(T[], int, T):
 *   - null array + non-null element, populated array + null/non-null element.
 *   - Primitive add() for all 8 primitives (append and indexed insert).
 *   - add with invalid index (< 0 or > length) -> IndexOutOfBoundsException.
 *   - Defect LANG-571: (null, null) handling throwing IllegalArgumentException.
 * - remove(T[], int) & removeElement(T[], Object):
 *   - Valid index removal, index out-of-bounds (< 0, >= length, null array), element found vs not found.
 *   - Primitive remove and removeElement for all 8 primitives.
 * **********************************************************************************************
 */
public class ArrayUtilsGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-571)
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLANG571AddNullArrayAndElement() {
        // Contract states: @throws IllegalArgumentException if both arguments are null
        // Defect: returns Object[]{null}, triggering ClassCastException on assignment to String[]
        String[] stringArray = null;
        String aString = null;
        String[] result = ArrayUtils.add(stringArray, aString);
        assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testLANG571AddNullArrayAndElementWithIndex() {
        // Contract states: @throws IllegalArgumentException if both array and element are null
        // Defect: returns new Object[]{null}, throwing ClassCastException when assigned to String[]
        String[] stringArray = null;
        String aString = null;
        String[] result = ArrayUtils.add(stringArray, 0, aString);
        assertNotNull(result);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        assertNotNull(new ArrayUtils());
    }

    @Test(timeout = 4000)
    public void testToString() {
        assertEquals("{}", ArrayUtils.toString(null));
        assertEquals("default", ArrayUtils.toString(null, "default"));
        assertEquals("{1,2}", ArrayUtils.toString(new int[] {1, 2}));
        assertEquals("{a,b}", ArrayUtils.toString(new String[] {"a", "b"}));
        assertEquals("{{1,2},{3,4}}", ArrayUtils.toString(new int[][] {{1, 2}, {3, 4}}));
    }

    @Test(timeout = 4000)
    public void testIsEquals() {
        assertTrue(ArrayUtils.isEquals(null, null));
        assertFalse(ArrayUtils.isEquals(new int[] {1}, null));
        assertFalse(ArrayUtils.isEquals(null, new int[] {1}));
        assertTrue(ArrayUtils.isEquals(new int[] {1, 2}, new int[] {1, 2}));
        assertFalse(ArrayUtils.isEquals(new int[] {1, 2}, new int[] {1, 3}));
        assertTrue(ArrayUtils.isEquals(new Object[] {"a", new int[] {1}}, new Object[] {"a", new int[] {1}}));
    }

    @Test(timeout = 4000)
    public void testToMap() {
        assertNull(ArrayUtils.toMap(null));

        Map<Object, Object> emptyMap = ArrayUtils.toMap(new Object[0]);
        assertTrue(emptyMap.isEmpty());

        Object[] input = new Object[] {
            new Object[] {"key1", "val1"},
            new AbstractMap.SimpleEntry<String, String>("key2", "val2")
        };
        Map<Object, Object> map = ArrayUtils.toMap(input);
        assertEquals(2, map.size());
        assertEquals("val1", map.get("key1"));
        assertEquals("val2", map.get("key2"));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToMapElementTooShort() {
        ArrayUtils.toMap(new Object[] { new Object[] {"onlyKey"} });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToMapInvalidElementType() {
        ArrayUtils.toMap(new Object[] { "justAString" });
    }

    @Test(timeout = 4000)
    public void testToArray() {
        String[] array = ArrayUtils.toArray("one", "two");
        assertArrayEquals(new String[] {"one", "two"}, array);
        Number[] numbers = ArrayUtils.<Number>toArray(1, 2.5);
        assertEquals(2, numbers.length);
        assertEquals(1, numbers[0]);
        assertEquals(2.5, numbers[1]);
    }

    @Test(timeout = 4000)
    public void testGetLength() {
        assertEquals(0, ArrayUtils.getLength(null));
        assertEquals(0, ArrayUtils.getLength(new int[0]));
        assertEquals(3, ArrayUtils.getLength(new String[] {"a", "b", "c"}));
        assertEquals(2, ArrayUtils.getLength(new boolean[] {true, false}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetLengthNotAnArray() {
        ArrayUtils.getLength("notAnArray");
    }

    @Test(timeout = 4000)
    public void testIsSameType() {
        assertTrue(ArrayUtils.isSameType(new String[0], new String[1]));
        assertFalse(ArrayUtils.isSameType(new String[0], new Object[0]));
        assertTrue(ArrayUtils.isSameType(new int[1][1], new int[2][2]));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsSameTypeNullFirst() {
        ArrayUtils.isSameType(null, new int[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsSameTypeNullSecond() {
        ArrayUtils.isSameType(new int[0], null);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloneObjectAndPrimitives() {
        assertNull(ArrayUtils.clone((Object[]) null));
        assertNull(ArrayUtils.clone((long[]) null));
        assertNull(ArrayUtils.clone((int[]) null));
        assertNull(ArrayUtils.clone((short[]) null));
        assertNull(ArrayUtils.clone((char[]) null));
        assertNull(ArrayUtils.clone((byte[]) null));
        assertNull(ArrayUtils.clone((double[]) null));
        assertNull(ArrayUtils.clone((float[]) null));
        assertNull(ArrayUtils.clone((boolean[]) null));

        assertArrayEquals(new String[] {"a"}, ArrayUtils.clone(new String[] {"a"}));
        assertArrayEquals(new long[] {1L}, ArrayUtils.clone(new long[] {1L}));
        assertArrayEquals(new int[] {1}, ArrayUtils.clone(new int[] {1}));
        assertArrayEquals(new short[] {1}, ArrayUtils.clone(new short[] {1}));
        assertArrayEquals(new char[] {'c'}, ArrayUtils.clone(new char[] {'c'}));
        assertArrayEquals(new byte[] {1}, ArrayUtils.clone(new byte[] {1}));
        assertArrayEquals(new double[] {1.0}, ArrayUtils.clone(new double[] {1.0}), 0.0);
        assertArrayEquals(new float[] {1.0f}, ArrayUtils.clone(new float[] {1.0f}), 0.0f);
        assertArrayEquals(new boolean[] {true}, ArrayUtils.clone(new boolean[] {true}));
    }

    @Test(timeout = 4000)
    public void testSubarrayObject() {
        assertNull(ArrayUtils.subarray((String[]) null, 0, 1));
        String[] src = new String[] {"a", "b", "c"};
        assertArrayEquals(new String[0], ArrayUtils.subarray(src, 2, 1));
        assertArrayEquals(new String[0], ArrayUtils.subarray(src, 5, 6));
        assertArrayEquals(new String[] {"a", "b"}, ArrayUtils.subarray(src, -1, 2));
        assertArrayEquals(new String[] {"b", "c"}, ArrayUtils.subarray(src, 1, 10));
        assertArrayEquals(new String[] {"a", "b", "c"}, ArrayUtils.subarray(src, -2, 10));
    }

    @Test(timeout = 4000)
    public void testSubarrayPrimitives() {
        assertNull(ArrayUtils.subarray((long[]) null, 0, 1));
        assertArrayEquals(new long[] {2L}, ArrayUtils.subarray(new long[] {1L, 2L, 3L}, 1, 2));
        assertArrayEquals(ArrayUtils.EMPTY_LONG_ARRAY, ArrayUtils.subarray(new long[] {1L}, 2, 1));

        assertNull(ArrayUtils.subarray((int[]) null, 0, 1));
        assertArrayEquals(new int[] {2}, ArrayUtils.subarray(new int[] {1, 2, 3}, 1, 2));
        assertArrayEquals(ArrayUtils.EMPTY_INT_ARRAY, ArrayUtils.subarray(new int[] {1}, 2, 1));

        assertNull(ArrayUtils.subarray((short[]) null, 0, 1));
        assertArrayEquals(new short[] {2}, ArrayUtils.subarray(new short[] {1, 2, 3}, 1, 2));
        assertArrayEquals(ArrayUtils.EMPTY_SHORT_ARRAY, ArrayUtils.subarray(new short[] {1}, 2, 1));

        assertNull(ArrayUtils.subarray((char[]) null, 0, 1));
        assertArrayEquals(new char[] {'b'}, ArrayUtils.subarray(new char[] {'a', 'b', 'c'}, 1, 2));
        assertArrayEquals(ArrayUtils.EMPTY_CHAR_ARRAY, ArrayUtils.subarray(new char[] {'a'}, 2, 1));

        assertNull(ArrayUtils.subarray((byte[]) null, 0, 1));
        assertArrayEquals(new byte[] {2}, ArrayUtils.subarray(new byte[] {1, 2, 3}, 1, 2));
        assertArrayEquals(ArrayUtils.EMPTY_BYTE_ARRAY, ArrayUtils.subarray(new byte[] {1}, 2, 1));

        assertNull(ArrayUtils.subarray((double[]) null, 0, 1));
        assertArrayEquals(new double[] {2.0}, ArrayUtils.subarray(new double[] {1.0, 2.0, 3.0}, 1, 2), 0.0);
        assertArrayEquals(ArrayUtils.EMPTY_DOUBLE_ARRAY, ArrayUtils.subarray(new double[] {1.0}, 2, 1), 0.0);

        assertNull(ArrayUtils.subarray((float[]) null, 0, 1));
        assertArrayEquals(new float[] {2.0f}, ArrayUtils.subarray(new float[] {1.0f, 2.0f, 3.0f}, 1, 2), 0.0f);
        assertArrayEquals(ArrayUtils.EMPTY_FLOAT_ARRAY, ArrayUtils.subarray(new float[] {1.0f}, 2, 1), 0.0f);

        assertNull(ArrayUtils.subarray((boolean[]) null, 0, 1));
        assertArrayEquals(new boolean[] {false}, ArrayUtils.subarray(new boolean[] {true, false, true}, 1, 2));
        assertArrayEquals(ArrayUtils.EMPTY_BOOLEAN_ARRAY, ArrayUtils.subarray(new boolean[] {true}, 2, 1));
    }

    @Test(timeout = 4000)
    public void testIsSameLength() {
        assertTrue(ArrayUtils.isSameLength((Object[]) null, (Object[]) null));
        assertTrue(ArrayUtils.isSameLength(new Object[0], (Object[]) null));
        assertFalse(ArrayUtils.isSameLength(new Object[1], (Object[]) null));
        assertFalse(ArrayUtils.isSameLength((Object[]) null, new Object[1]));
        assertTrue(ArrayUtils.isSameLength(new Object[2], new Object[2]));
        assertFalse(ArrayUtils.isSameLength(new Object[1], new Object[2]));

        assertTrue(ArrayUtils.isSameLength((long[]) null, (long[]) null));
        assertFalse(ArrayUtils.isSameLength(new long[1], (long[]) null));
        assertTrue(ArrayUtils.isSameLength((int[]) null, (int[]) null));
        assertFalse(ArrayUtils.isSameLength((int[]) null, new int[1]));
        assertTrue(ArrayUtils.isSameLength((short[]) null, (short[]) null));
        assertFalse(ArrayUtils.isSameLength(new short[1], (short[]) null));
        assertTrue(ArrayUtils.isSameLength((char[]) null, (char[]) null));
        assertFalse(ArrayUtils.isSameLength((char[]) null, new char[1]));
        assertTrue(ArrayUtils.isSameLength((byte[]) null, (byte[]) null));
        assertFalse(ArrayUtils.isSameLength(new byte[1], (byte[]) null));
        assertTrue(ArrayUtils.isSameLength((double[]) null, (double[]) null));
        assertFalse(ArrayUtils.isSameLength((double[]) null, new double[1]));
        assertTrue(ArrayUtils.isSameLength((float[]) null, (float[]) null));
        assertFalse(ArrayUtils.isSameLength(new float[1], (float[]) null));
        assertTrue(ArrayUtils.isSameLength((boolean[]) null, (boolean[]) null));
        assertFalse(ArrayUtils.isSameLength((boolean[]) null, new boolean[1]));
    }

    @Test(timeout = 4000)
    public void testReverse() {
        ArrayUtils.reverse((Object[]) null);
        String[] str = new String[] {"a", "b", "c"};
        ArrayUtils.reverse(str);
        assertArrayEquals(new String[] {"c", "b", "a"}, str);

        ArrayUtils.reverse((long[]) null);
        long[] l = new long[] {1, 2};
        ArrayUtils.reverse(l);
        assertArrayEquals(new long[] {2, 1}, l);

        ArrayUtils.reverse((int[]) null);
        int[] i = new int[] {1, 2};
        ArrayUtils.reverse(i);
        assertArrayEquals(new int[] {2, 1}, i);

        ArrayUtils.reverse((short[]) null);
        short[] s = new short[] {1, 2};
        ArrayUtils.reverse(s);
        assertArrayEquals(new short[] {2, 1}, s);

        ArrayUtils.reverse((char[]) null);
        char[] c = new char[] {'a', 'b'};
        ArrayUtils.reverse(c);
        assertArrayEquals(new char[] {'b', 'a'}, c);

        ArrayUtils.reverse((byte[]) null);
        byte[] b = new byte[] {1, 2};
        ArrayUtils.reverse(b);
        assertArrayEquals(new byte[] {2, 1}, b);

        ArrayUtils.reverse((double[]) null);
        double[] d = new double[] {1.0, 2.0};
        ArrayUtils.reverse(d);
        assertArrayEquals(new double[] {2.0, 1.0}, d, 0.0);

        ArrayUtils.reverse((float[]) null);
        float[] f = new float[] {1.0f, 2.0f};
        ArrayUtils.reverse(f);
        assertArrayEquals(new float[] {2.0f, 1.0f}, f, 0.0f);

        ArrayUtils.reverse((boolean[]) null);
        boolean[] bool = new boolean[] {true, false};
        ArrayUtils.reverse(bool);
        assertArrayEquals(new boolean[] {false, true}, bool);
    }

    @Test(timeout = 4000)
    public void testIndexOfObject() {
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(null, "a"));
        String[] array = new String[] {"a", null, "b", "a"};
        assertEquals(0, ArrayUtils.indexOf(array, "a"));
        assertEquals(1, ArrayUtils.indexOf(array, null));
        assertEquals(3, ArrayUtils.indexOf(array, "a", 1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(array, "c"));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(array, 123)); // Incompatible type branch
        assertEquals(0, ArrayUtils.indexOf(array, "a", -5));

        assertTrue(ArrayUtils.contains(array, "b"));
        assertFalse(ArrayUtils.contains(array, "z"));
        assertFalse(ArrayUtils.contains(null, "a"));

        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(null, "a"));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(array, "a", -1));
        assertEquals(3, ArrayUtils.lastIndexOf(array, "a"));
        assertEquals(3, ArrayUtils.lastIndexOf(array, "a", 100));
        assertEquals(0, ArrayUtils.lastIndexOf(array, "a", 2));
        assertEquals(1, ArrayUtils.lastIndexOf(array, null));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(array, 123));
    }

    @Test(timeout = 4000)
    public void testIndexOfPrimitives() {
        // long
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((long[]) null, 1L));
        long[] l = new long[] {1L, 2L, 1L};
        assertEquals(0, ArrayUtils.indexOf(l, 1L));
        assertEquals(2, ArrayUtils.indexOf(l, 1L, 1));
        assertEquals(0, ArrayUtils.indexOf(l, 1L, -1));
        assertEquals(2, ArrayUtils.lastIndexOf(l, 1L));
        assertEquals(0, ArrayUtils.lastIndexOf(l, 1L, 1));
        assertEquals(2, ArrayUtils.lastIndexOf(l, 1L, 10));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(l, 1L, -1));
        assertTrue(ArrayUtils.contains(l, 2L));

        // int
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((int[]) null, 1));
        int[] in = new int[] {1, 2, 1};
        assertEquals(0, ArrayUtils.indexOf(in, 1));
        assertEquals(2, ArrayUtils.indexOf(in, 1, 1));
        assertEquals(0, ArrayUtils.indexOf(in, 1, -1));
        assertEquals(2, ArrayUtils.lastIndexOf(in, 1));
        assertEquals(0, ArrayUtils.lastIndexOf(in, 1, 1));
        assertEquals(2, ArrayUtils.lastIndexOf(in, 1, 10));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(in, 1, -1));
        assertTrue(ArrayUtils.contains(in, 2));

        // short
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((short[]) null, (short) 1));
        short[] s = new short[] {1, 2, 1};
        assertEquals(0, ArrayUtils.indexOf(s, (short) 1));
        assertEquals(2, ArrayUtils.indexOf(s, (short) 1, 1));
        assertEquals(0, ArrayUtils.indexOf(s, (short) 1, -1));
        assertEquals(2, ArrayUtils.lastIndexOf(s, (short) 1));
        assertEquals(0, ArrayUtils.lastIndexOf(s, (short) 1, 1));
        assertEquals(2, ArrayUtils.lastIndexOf(s, (short) 1, 10));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(s, (short) 1, -1));
        assertTrue(ArrayUtils.contains(s, (short) 2));

        // char
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((char[]) null, 'a'));
        char[] c = new char[] {'a', 'b', 'a'};
        assertEquals(0, ArrayUtils.indexOf(c, 'a'));
        assertEquals(2, ArrayUtils.indexOf(c, 'a', 1));
        assertEquals(0, ArrayUtils.indexOf(c, 'a', -1));
        assertEquals(2, ArrayUtils.lastIndexOf(c, 'a'));
        assertEquals(0, ArrayUtils.lastIndexOf(c, 'a', 1));
        assertEquals(2, ArrayUtils.lastIndexOf(c, 'a', 10));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(c, 'a', -1));
        assertTrue(ArrayUtils.contains(c, 'b'));

        // byte
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((byte[]) null, (byte) 1));
        byte[] b = new byte[] {1, 2, 1};
        assertEquals(0, ArrayUtils.indexOf(b, (byte) 1));
        assertEquals(2, ArrayUtils.indexOf(b, (byte) 1, 1));
        assertEquals(0, ArrayUtils.indexOf(b, (byte) 1, -1));
        assertEquals(2, ArrayUtils.lastIndexOf(b, (byte) 1));
        assertEquals(0, ArrayUtils.lastIndexOf(b, (byte) 1, 1));
        assertEquals(2, ArrayUtils.lastIndexOf(b, (byte) 1, 10));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(b, (byte) 1, -1));
        assertTrue(ArrayUtils.contains(b, (byte) 2));

        // boolean
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((boolean[]) null, true));
        boolean[] bool = new boolean[] {true, false, true};
        assertEquals(0, ArrayUtils.indexOf(bool, true));
        assertEquals(2, ArrayUtils.indexOf(bool, true, 1));
        assertEquals(0, ArrayUtils.indexOf(bool, true, -1));
        assertEquals(2, ArrayUtils.lastIndexOf(bool, true));
        assertEquals(0, ArrayUtils.lastIndexOf(bool, true, 1));
        assertEquals(2, ArrayUtils.lastIndexOf(bool, true, 10));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(bool, true, -1));
        assertTrue(ArrayUtils.contains(bool, false));
    }

    @Test(timeout = 4000)
    public void testIndexOfDoubleFloatTolerance() {
        double[] d = new double[] {1.0, 2.0, 3.0, 2.05};
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((double[]) null, 1.0));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(new double[0], 1.0));
        assertEquals(1, ArrayUtils.indexOf(d, 2.0));
        assertEquals(1, ArrayUtils.indexOf(d, 2.02, 0.05));
        assertEquals(1, ArrayUtils.indexOf(d, 2.02, 0, 0.05));
        assertEquals(3, ArrayUtils.lastIndexOf(d, 2.02, 0.05));
        assertEquals(1, ArrayUtils.lastIndexOf(d, 2.02, 2, 0.05));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(d, 2.0, -1));
        assertTrue(ArrayUtils.contains(d, 2.04, 0.05));
        assertFalse(ArrayUtils.contains(d, 5.0, 0.01));

        float[] f = new float[] {1.0f, 2.0f, 3.0f, 2.0f};
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((float[]) null, 1.0f));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(new float[0], 1.0f));
        assertEquals(1, ArrayUtils.indexOf(f, 2.0f));
        assertEquals(3, ArrayUtils.lastIndexOf(f, 2.0f));
        assertEquals(1, ArrayUtils.lastIndexOf(f, 2.0f, 2));
        assertEquals(3, ArrayUtils.lastIndexOf(f, 2.0f, 10));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(f, 2.0f, -1));
        assertTrue(ArrayUtils.contains(f, 2.0f));
    }

    @Test(timeout = 4000)
    public void testConverters() {
        assertNull(ArrayUtils.toPrimitive((Character[]) null));
        assertArrayEquals(new char[0], ArrayUtils.toPrimitive(new Character[0]));
        assertArrayEquals(new char[] {'a', 'b'}, ArrayUtils.toPrimitive(new Character[] {'a', 'b'}));
        assertArrayEquals(new char[] {'a', 'x'}, ArrayUtils.toPrimitive(new Character[] {'a', null}, 'x'));
        assertNull(ArrayUtils.toObject((char[]) null));
        assertArrayEquals(new Character[0], ArrayUtils.toObject(new char[0]));
        assertArrayEquals(new Character[] {'a'}, ArrayUtils.toObject(new char[] {'a'}));

        assertNull(ArrayUtils.toPrimitive((Long[]) null));
        assertArrayEquals(new long[0], ArrayUtils.toPrimitive(new Long[0]));
        assertArrayEquals(new long[] {1L}, ArrayUtils.toPrimitive(new Long[] {1L}));
        assertArrayEquals(new long[] {1L, 0L}, ArrayUtils.toPrimitive(new Long[] {1L, null}, 0L));
        assertNull(ArrayUtils.toObject((long[]) null));
        assertArrayEquals(new Long[0], ArrayUtils.toObject(new long[0]));
        assertArrayEquals(new Long[] {1L}, ArrayUtils.toObject(new long[] {1L}));

        assertNull(ArrayUtils.toPrimitive((Integer[]) null));
        assertArrayEquals(new int[0], ArrayUtils.toPrimitive(new Integer[0]));
        assertArrayEquals(new int[] {1}, ArrayUtils.toPrimitive(new Integer[] {1}));
        assertArrayEquals(new int[] {1, -1}, ArrayUtils.toPrimitive(new Integer[] {1, null}, -1));
        assertNull(ArrayUtils.toObject((int[]) null));
        assertArrayEquals(new Integer[0], ArrayUtils.toObject(new int[0]));
        assertArrayEquals(new Integer[] {1}, ArrayUtils.toObject(new int[] {1}));

        assertNull(ArrayUtils.toPrimitive((Short[]) null));
        assertArrayEquals(new short[0], ArrayUtils.toPrimitive(new Short[0]));
        assertArrayEquals(new short[] {1}, ArrayUtils.toPrimitive(new Short[] {(short) 1}));
        assertArrayEquals(new short[] {1, 0}, ArrayUtils.toPrimitive(new Short[] {(short) 1, null}, (short) 0));
        assertNull(ArrayUtils.toObject((short[]) null));
        assertArrayEquals(new Short[0], ArrayUtils.toObject(new short[0]));
        assertArrayEquals(new Short[] {(short) 1}, ArrayUtils.toObject(new short[] {1}));

        assertNull(ArrayUtils.toPrimitive((Byte[]) null));
        assertArrayEquals(new byte[0], ArrayUtils.toPrimitive(new Byte[0]));
        assertArrayEquals(new byte[] {1}, ArrayUtils.toPrimitive(new Byte[] {(byte) 1}));
        assertArrayEquals(new byte[] {1, 0}, ArrayUtils.toPrimitive(new Byte[] {(byte) 1, null}, (byte) 0));
        assertNull(ArrayUtils.toObject((byte[]) null));
        assertArrayEquals(new Byte[0], ArrayUtils.toObject(new byte[0]));
        assertArrayEquals(new Byte[] {(byte) 1}, ArrayUtils.toObject(new byte[] {1}));

        assertNull(ArrayUtils.toPrimitive((Double[]) null));
        assertArrayEquals(new double[0], ArrayUtils.toPrimitive(new Double[0]), 0.0);
        assertArrayEquals(new double[] {1.0}, ArrayUtils.toPrimitive(new Double[] {1.0}), 0.0);
        assertArrayEquals(new double[] {1.0, Double.NaN}, ArrayUtils.toPrimitive(new Double[] {1.0, null}, Double.NaN), 0.0);
        assertNull(ArrayUtils.toObject((double[]) null));
        assertArrayEquals(new Double[0], ArrayUtils.toObject(new double[0]));
        assertArrayEquals(new Double[] {1.0}, ArrayUtils.toObject(new double[] {1.0}));

        assertNull(ArrayUtils.toPrimitive((Float[]) null));
        assertArrayEquals(new float[0], ArrayUtils.toPrimitive(new Float[0]), 0.0f);
        assertArrayEquals(new float[] {1.0f}, ArrayUtils.toPrimitive(new Float[] {1.0f}), 0.0f);
        assertArrayEquals(new float[] {1.0f, Float.NaN}, ArrayUtils.toPrimitive(new Float[] {1.0f, null}, Float.NaN), 0.0f);
        assertNull(ArrayUtils.toObject((float[]) null));
        assertArrayEquals(new Float[0], ArrayUtils.toObject(new float[0]));
        assertArrayEquals(new Float[] {1.0f}, ArrayUtils.toObject(new float[] {1.0f}));

        assertNull(ArrayUtils.toPrimitive((Boolean[]) null));
        assertArrayEquals(new boolean[0], ArrayUtils.toPrimitive(new Boolean[0]));
        assertArrayEquals(new boolean[] {true}, ArrayUtils.toPrimitive(new Boolean[] {Boolean.TRUE}));
        assertArrayEquals(new boolean[] {true, false}, ArrayUtils.toPrimitive(new Boolean[] {Boolean.TRUE, null}, false));
        assertNull(ArrayUtils.toObject((boolean[]) null));
        assertArrayEquals(new Boolean[0], ArrayUtils.toObject(new boolean[0]));
        assertArrayEquals(new Boolean[] {Boolean.TRUE}, ArrayUtils.toObject(new boolean[] {true}));
    }

    @Test(timeout = 4000)
    public void testIsEmpty() {
        assertTrue(ArrayUtils.isEmpty((Object[]) null));
        assertTrue(ArrayUtils.isEmpty(new Object[0]));
        assertFalse(ArrayUtils.isEmpty(new Object[] {"a"}));

        assertTrue(ArrayUtils.isEmpty((long[]) null));
        assertTrue(ArrayUtils.isEmpty(new long[0]));
        assertFalse(ArrayUtils.isEmpty(new long[] {1L}));

        assertTrue(ArrayUtils.isEmpty((int[]) null));
        assertTrue(ArrayUtils.isEmpty(new int[0]));
        assertFalse(ArrayUtils.isEmpty(new int[] {1}));

        assertTrue(ArrayUtils.isEmpty((short[]) null));
        assertTrue(ArrayUtils.isEmpty(new short[0]));
        assertFalse(ArrayUtils.isEmpty(new short[] {1}));

        assertTrue(ArrayUtils.isEmpty((char[]) null));
        assertTrue(ArrayUtils.isEmpty(new char[0]));
        assertFalse(ArrayUtils.isEmpty(new char[] {'a'}));

        assertTrue(ArrayUtils.isEmpty((byte[]) null));
        assertTrue(ArrayUtils.isEmpty(new byte[0]));
        assertFalse(ArrayUtils.isEmpty(new byte[] {1}));

        assertTrue(ArrayUtils.isEmpty((double[]) null));
        assertTrue(ArrayUtils.isEmpty(new double[0]));
        assertFalse(ArrayUtils.isEmpty(new double[] {1.0}));

        assertTrue(ArrayUtils.isEmpty((float[]) null));
        assertTrue(ArrayUtils.isEmpty(new float[0]));
        assertFalse(ArrayUtils.isEmpty(new float[] {1.0f}));

        assertTrue(ArrayUtils.isEmpty((boolean[]) null));
        assertTrue(ArrayUtils.isEmpty(new boolean[0]));
        assertFalse(ArrayUtils.isEmpty(new boolean[] {true}));
    }

    @Test(timeout = 4000)
    public void testAddAll() {
        assertNull(ArrayUtils.addAll((String[]) null, (String[]) null));
        assertArrayEquals(new String[] {"a"}, ArrayUtils.addAll(null, "a"));
        assertArrayEquals(new String[] {"a"}, ArrayUtils.addAll(new String[] {"a"}, (String[]) null));
        assertArrayEquals(new String[] {"a", "b"}, ArrayUtils.addAll(new String[] {"a"}, "b"));

        assertArrayEquals(new boolean[] {true, false}, ArrayUtils.addAll(new boolean[] {true}, false));
        assertArrayEquals(new boolean[] {true}, ArrayUtils.addAll(null, new boolean[] {true}));
        assertArrayEquals(new boolean[] {true}, ArrayUtils.addAll(new boolean[] {true}, (boolean[]) null));

        assertArrayEquals(new char[] {'a', 'b'}, ArrayUtils.addAll(new char[] {'a'}, 'b'));
        assertArrayEquals(new byte[] {1, 2}, ArrayUtils.addAll(new byte[] {1}, (byte) 2));
        assertArrayEquals(new short[] {1, 2}, ArrayUtils.addAll(new short[] {1}, (short) 2));
        assertArrayEquals(new int[] {1, 2}, ArrayUtils.addAll(new int[] {1}, 2));
        assertArrayEquals(new long[] {1L, 2L}, ArrayUtils.addAll(new long[] {1L}, 2L));
        assertArrayEquals(new float[] {1.0f, 2.0f}, ArrayUtils.addAll(new float[] {1.0f}, 2.0f), 0.0f);
        assertArrayEquals(new double[] {1.0, 2.0}, ArrayUtils.addAll(new double[] {1.0}, 2.0), 0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddAllIncompatibleTypes() {
        String[] s1 = new String[] {"a"};
        Object[] s2 = new Object[] {Integer.valueOf(1)};
        ArrayUtils.addAll(s1, (String[]) (Object) s2);
    }

    @Test(timeout = 4000)
    public void testAddSingle() {
        assertArrayEquals(new String[] {"a"}, ArrayUtils.add((String[]) null, "a"));
        assertArrayEquals(new String[] {"a", "b"}, ArrayUtils.add(new String[] {"a"}, "b"));
        assertArrayEquals(new String[] {"a", null}, ArrayUtils.add(new String[] {"a"}, null));

        assertArrayEquals(new boolean[] {true}, ArrayUtils.add((boolean[]) null, true));
        assertArrayEquals(new boolean[] {true, false}, ArrayUtils.add(new boolean[] {true}, false));

        assertArrayEquals(new byte[] {1}, ArrayUtils.add((byte[]) null, (byte) 1));
        assertArrayEquals(new byte[] {1, 2}, ArrayUtils.add(new byte[] {1}, (byte) 2));

        assertArrayEquals(new char[] {'a'}, ArrayUtils.add((char[]) null, 'a'));
        assertArrayEquals(new char[] {'a', 'b'}, ArrayUtils.add(new char[] {'a'}, 'b'));

        assertArrayEquals(new double[] {1.0}, ArrayUtils.add((double[]) null, 1.0), 0.0);
        assertArrayEquals(new double[] {1.0, 2.0}, ArrayUtils.add(new double[] {1.0}, 2.0), 0.0);

        assertArrayEquals(new float[] {1.0f}, ArrayUtils.add((float[]) null, 1.0f), 0.0f);
        assertArrayEquals(new float[] {1.0f, 2.0f}, ArrayUtils.add(new float[] {1.0f}, 2.0f), 0.0f);

        assertArrayEquals(new int[] {1}, ArrayUtils.add((int[]) null, 1));
        assertArrayEquals(new int[] {1, 2}, ArrayUtils.add(new int[] {1}, 2));

        assertArrayEquals(new long[] {1L}, ArrayUtils.add((long[]) null, 1L));
        assertArrayEquals(new long[] {1L, 2L}, ArrayUtils.add(new long[] {1L}, 2L));

        assertArrayEquals(new short[] {1}, ArrayUtils.add((short[]) null, (short) 1));
        assertArrayEquals(new short[] {1, 2}, ArrayUtils.add(new short[] {1}, (short) 2));
    }

    @Test(timeout = 4000)
    public void testAddWithIndex() {
        assertArrayEquals(new String[] {"a"}, ArrayUtils.add((String[]) null, 0, "a"));
        assertArrayEquals(new String[] {"a", "b"}, ArrayUtils.add(new String[] {"b"}, 0, "a"));
        assertArrayEquals(new String[] {"a", "b"}, ArrayUtils.add(new String[] {"a"}, 1, "b"));
        assertArrayEquals(new String[] {"a", "c", "b"}, ArrayUtils.add(new String[] {"a", "b"}, 1, "c"));

        assertArrayEquals(new boolean[] {true}, ArrayUtils.add((boolean[]) null, 0, true));
        assertArrayEquals(new boolean[] {true, false}, ArrayUtils.add(new boolean[] {true}, 1, false));

        assertArrayEquals(new char[] {'a'}, ArrayUtils.add((char[]) null, 0, 'a'));
        assertArrayEquals(new char[] {'a', 'b'}, ArrayUtils.add(new char[] {'b'}, 0, 'a'));

        assertArrayEquals(new byte[] {1}, ArrayUtils.add((byte[]) null, 0, (byte) 1));
        assertArrayEquals(new byte[] {1, 2}, ArrayUtils.add(new byte[] {1}, 1, (byte) 2));

        assertArrayEquals(new short[] {1}, ArrayUtils.add((short[]) null, 0, (short) 1));
        assertArrayEquals(new short[] {1, 2}, ArrayUtils.add(new short[] {1}, 1, (short) 2));

        assertArrayEquals(new int[] {1}, ArrayUtils.add((int[]) null, 0, 1));
        assertArrayEquals(new int[] {1, 2}, ArrayUtils.add(new int[] {1}, 1, 2));

        assertArrayEquals(new long[] {1L}, ArrayUtils.add((long[]) null, 0, 1L));
        assertArrayEquals(new long[] {1L, 2L}, ArrayUtils.add(new long[] {1L}, 1, 2L));

        assertArrayEquals(new float[] {1.0f}, ArrayUtils.add((float[]) null, 0, 1.0f), 0.0f);
        assertArrayEquals(new float[] {1.0f, 2.0f}, ArrayUtils.add(new float[] {1.0f}, 1, 2.0f), 0.0f);

        assertArrayEquals(new double[] {1.0}, ArrayUtils.add((double[]) null, 0, 1.0), 0.0);
        assertArrayEquals(new double[] {1.0, 2.0}, ArrayUtils.add(new double[] {1.0}, 1, 2.0), 0.0);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAddNullArrayInvalidIndex() {
        ArrayUtils.add((String[]) null, 1, "a");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAddIndexOutOfBoundsNegative() {
        ArrayUtils.add(new String[] {"a"}, -1, "b");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAddIndexOutOfBoundsOverLength() {
        ArrayUtils.add(new String[] {"a"}, 2, "b");
    }

    @Test(timeout = 4000)
    public void testRemoveAndRemoveElement() {
        assertArrayEquals(new String[0], ArrayUtils.remove(new String[] {"a"}, 0));
        assertArrayEquals(new String[] {"a", "c"}, ArrayUtils.remove(new String[] {"a", "b", "c"}, 1));
        assertArrayEquals(new String[] {"b", "c"}, ArrayUtils.remove(new String[] {"a", "b", "c"}, 0));
        assertArrayEquals(new String[] {"a", "b"}, ArrayUtils.remove(new String[] {"a", "b", "c"}, 2));

        assertNull(ArrayUtils.removeElement((String[]) null, "a"));
        assertArrayEquals(new String[] {"a"}, ArrayUtils.removeElement(new String[] {"a"}, "b"));
        assertArrayEquals(new String[] {"b"}, ArrayUtils.removeElement(new String[] {"a", "b"}, "a"));

        // boolean
        assertArrayEquals(new boolean[] {false}, ArrayUtils.remove(new boolean[] {true, false}, 0));
        assertNull(ArrayUtils.removeElement((boolean[]) null, true));
        assertArrayEquals(new boolean[] {false}, ArrayUtils.removeElement(new boolean[] {true, false}, true));
        assertArrayEquals(new boolean[] {true}, ArrayUtils.removeElement(new boolean[] {true}, false));

        // byte
        assertArrayEquals(new byte[] {2}, ArrayUtils.remove(new byte[] {1, 2}, 0));
        assertNull(ArrayUtils.removeElement((byte[]) null, (byte) 1));
        assertArrayEquals(new byte[] {2}, ArrayUtils.removeElement(new byte[] {1, 2}, (byte) 1));
        assertArrayEquals(new byte[] {1}, ArrayUtils.removeElement(new byte[] {1}, (byte) 2));

        // char
        assertArrayEquals(new char[] {'b'}, ArrayUtils.remove(new char[] {'a', 'b'}, 0));
        assertNull(ArrayUtils.removeElement((char[]) null, 'a'));
        assertArrayEquals(new char[] {'b'}, ArrayUtils.removeElement(new char[] {'a', 'b'}, 'a'));
        assertArrayEquals(new char[] {'a'}, ArrayUtils.removeElement(new char[] {'a'}, 'b'));

        // short
        assertArrayEquals(new short[] {2}, ArrayUtils.remove(new short[] {1, 2}, 0));
        assertNull(ArrayUtils.removeElement((short[]) null, (short) 1));
        assertArrayEquals(new short[] {2}, ArrayUtils.removeElement(new short[] {1, 2}, (short) 1));
        assertArrayEquals(new short[] {1}, ArrayUtils.removeElement(new short[] {1}, (short) 2));

        // int
        assertArrayEquals(new int[] {2}, ArrayUtils.remove(new int[] {1, 2}, 0));
        assertNull(ArrayUtils.removeElement((int[]) null, 1));
        assertArrayEquals(new int[] {2}, ArrayUtils.removeElement(new int[] {1, 2}, 1));
        assertArrayEquals(new int[] {1}, ArrayUtils.removeElement(new int[] {1}, 2));

        // long
        assertArrayEquals(new long[] {2L}, ArrayUtils.remove(new long[] {1L, 2L}, 0));
        assertNull(ArrayUtils.removeElement((long[]) null, 1L));
        assertArrayEquals(new long[] {2L}, ArrayUtils.removeElement(new long[] {1L, 2L}, 1L));
        assertArrayEquals(new long[] {1L}, ArrayUtils.removeElement(new long[] {1L}, 2L));

        // float
        assertArrayEquals(new float[] {2.0f}, ArrayUtils.remove(new float[] {1.0f, 2.0f}, 0), 0.0f);
        assertNull(ArrayUtils.removeElement((float[]) null, 1.0f));
        assertArrayEquals(new float[] {2.0f}, ArrayUtils.removeElement(new float[] {1.0f, 2.0f}, 1.0f), 0.0f);
        assertArrayEquals(new float[] {1.0f}, ArrayUtils.removeElement(new float[] {1.0f}, 2.0f), 0.0f);

        // double
        assertArrayEquals(new double[] {2.0}, ArrayUtils.remove(new double[] {1.0, 2.0}, 0), 0.0);
        assertNull(ArrayUtils.removeElement((double[]) null, 1.0));
        assertArrayEquals(new double[] {2.0}, ArrayUtils.removeElement(new double[] {1.0, 2.0}, 1.0), 0.0);
        assertArrayEquals(new double[] {1.0}, ArrayUtils.removeElement(new double[] {1.0}, 2.0), 0.0);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemoveNullArrayThrowsException() {
        ArrayUtils.remove((Object[]) null, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemoveNegativeIndex() {
        ArrayUtils.remove(new String[] {"a"}, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemoveIndexEqualsLength() {
        ArrayUtils.remove(new String[] {"a"}, 1);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstantsIntegrity() {
        assertEquals(0, ArrayUtils.EMPTY_OBJECT_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_CLASS_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_STRING_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_LONG_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_LONG_OBJECT_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_INT_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_SHORT_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_SHORT_OBJECT_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_BYTE_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_BYTE_OBJECT_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_DOUBLE_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_DOUBLE_OBJECT_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_FLOAT_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_FLOAT_OBJECT_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_BOOLEAN_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_BOOLEAN_OBJECT_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_CHAR_ARRAY.length);
        assertEquals(0, ArrayUtils.EMPTY_CHARACTER_OBJECT_ARRAY.length);
        assertEquals(-1, ArrayUtils.INDEX_NOT_FOUND);
    }
}