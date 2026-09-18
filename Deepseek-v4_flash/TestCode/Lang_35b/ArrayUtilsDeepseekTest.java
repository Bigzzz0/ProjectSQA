package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted branches and boundary conditions:
 *
 * 1. toMap(): null array, Map.Entry elements, Object[] elements (length >=2), length<2, non-Entry/non-Array
 * 2. clone(): null arrays, all primitive and Object types
 * 3. subarray(): null, startIndex<0, endIndex>len, startIndex>=endIndex, normal case, empty result
 * 4. isSameLength(): both null, one null+other non-empty, both non-null equal/diff length, empty arrays
 * 5. indexOf/lastIndexOf: null array, startIndex<0, startIndex>=len, object/value found/not found, null object
 * 6. contains: null/empty/non-empty arrays
 * 7. reverse(): null, single element, multiple elements (even/odd)
 * 8. addAll(): null first, null second, both non-null, type mismatch leading to ArrayStoreException
 * 9. add(T[], T): null array w/ non-null element, null array w/ null element, normal add
 * 10. add at index: null array w/ index 0, index<0, index>length, normal insert at start/middle/end
 * 11. remove(): null array (throws), index<0, index>=length, remove first/last/middle
 * 12. removeElement(): null array, element not found, found at first/last/multiple occurrences
 * 13. isEmpty(): null, empty, non-empty arrays of all types
 * 14. toPrimitive/toObject: null, empty, normal with/without null handling
 * 15. getLength(): null, non-array (throws), various arrays
 * 16. isSameType(): null (throws), matching/non-matching types
 * 17. toString(): null, non-null arrays
 * 18. isEquals(): null, equals/not equals arrays
 * 19. toArray(): varargs with various types
 *
 * KNOWN DEFECT: LANG-571 - ArrayUtils.add(T[], T) returns Object[] instead of T[]
 * when both array and element are null (method "add(T[] array, T element)" line with
 * "} else { type = Object.class; }" then "T[] newArray = (T[]) copyArrayGrow1(array, type);"
 * causes ClassCastException when trying to cast Object[] to String[].)
 * Test: testAddNullNullTypeSafety
 */
public class ArrayUtilsDeepseekTest {

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testToMapNull() {
        assertNull(ArrayUtils.toMap(null));
    }

    @Test(timeout = 4000)
    public void testToMapMapEntryElements() {
        java.util.Map<java.util.Map.Entry<Object, Object>, Object> mapEntryMap = new java.util.HashMap<>();
        java.util.Map.Entry<Object, Object> entry = new java.util.AbstractMap.SimpleEntry<>("key1", "value1");
        Object[] input = new Object[]{entry};
        java.util.Map<Object, Object> result = ArrayUtils.toMap(input);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("value1", result.get("key1"));
    }

    @Test(timeout = 4000)
    public void testToMapObjectArrayElements() {
        Object[] input = new Object[]{new Object[]{"key1", "value1"}, new Object[]{"key2", "value2"}};
        java.util.Map<Object, Object> result = ArrayUtils.toMap(input);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToMapShortArrayElement() {
        Object[] input = new Object[]{new Object[]{"onlyKey"}};
        ArrayUtils.toMap(input);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToMapInvalidElement() {
        Object[] input = new Object[]{"invalidString"};
        ArrayUtils.toMap(input);
    }

    @Test(timeout = 4000)
    public void testCloneObjectArrayNull() {
        assertNull(ArrayUtils.clone((String[]) null));
    }

    @Test(timeout = 4000)
    public void testCloneObjectArray() {
        String[] input = {"a", "b"};
        String[] result = ArrayUtils.clone(input);
        assertNotNull(result);
        assertNotSame(input, result);
        assertArrayEquals(input, result);
    }

    @Test(timeout = 4000)
    public void testCloneLongArrayNull() {
        assertNull(ArrayUtils.clone((long[]) null));
    }

    @Test(timeout = 4000)
    public void testCloneLongArray() {
        long[] input = {1L, 2L};
        long[] result = ArrayUtils.clone(input);
        assertNotNull(result);
        assertNotSame(input, result);
        assertArrayEquals(input, result);
    }

    @Test(timeout = 4000)
    public void testCloneIntArrayNull() {
        assertNull(ArrayUtils.clone((int[]) null));
    }

    @Test(timeout = 4000)
    public void testCloneIntArray() {
        int[] input = {1, 2};
        int[] result = ArrayUtils.clone(input);
        assertNotNull(result);
        assertNotSame(input, result);
        assertArrayEquals(input, result);
    }

    @Test(timeout = 4000)
    public void testCloneShortArrayNull() {
        assertNull(ArrayUtils.clone((short[]) null));
    }

    @Test(timeout = 4000)
    public void testCloneShortArray() {
        short[] input = {1, 2};
        short[] result = ArrayUtils.clone(input);
        assertNotNull(result);
        assertNotSame(input, result);
        assertArrayEquals(input, result);
    }

    @Test(timeout = 4000)
    public void testCloneCharArrayNull() {
        assertNull(ArrayUtils.clone((char[]) null));
    }

    @Test(timeout = 4000)
    public void testCloneCharArray() {
        char[] input = {'a', 'b'};
        char[] result = ArrayUtils.clone(input);
        assertNotNull(result);
        assertNotSame(input, result);
        assertArrayEquals(input, result);
    }

    @Test(timeout = 4000)
    public void testCloneByteArrayNull() {
        assertNull(ArrayUtils.clone((byte[]) null));
    }

    @Test(timeout = 4000)
    public void testCloneByteArray() {
        byte[] input = {1, 2};
        byte[] result = ArrayUtils.clone(input);
        assertNotNull(result);
        assertNotSame(input, result);
        assertArrayEquals(input, result);
    }

    @Test(timeout = 4000)
    public void testCloneDoubleArrayNull() {
        assertNull(ArrayUtils.clone((double[]) null));
    }

    @Test(timeout = 4000)
    public void testCloneDoubleArray() {
        double[] input = {1.0, 2.0};
        double[] result = ArrayUtils.clone(input);
        assertNotNull(result);
        assertNotSame(input, result);
        assertArrayEquals(input, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testCloneFloatArrayNull() {
        assertNull(ArrayUtils.clone((float[]) null));
    }

    @Test(timeout = 4000)
    public void testCloneFloatArray() {
        float[] input = {1.0f, 2.0f};
        float[] result = ArrayUtils.clone(input);
        assertNotNull(result);
        assertNotSame(input, result);
        assertArrayEquals(input, result, 0.0f);
    }

    @Test(timeout = 4000)
    public void testCloneBooleanArrayNull() {
        assertNull(ArrayUtils.clone((boolean[]) null));
    }

    @Test(timeout = 4000)
    public void testCloneBooleanArray() {
        boolean[] input = {true, false};
        boolean[] result = ArrayUtils.clone(input);
        assertNotNull(result);
        assertNotSame(input, result);
        assertArrayEquals(input, result);
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testSubarrayObjectNull() {
        assertNull(ArrayUtils.subarray((String[]) null, 0, 1));
    }

    @Test(timeout = 4000)
    public void testSubarrayObjectStartNegative() {
        String[] input = {"a", "b", "c"};
        String[] result = ArrayUtils.subarray(input, -1, 2);
        assertArrayEquals(new String[]{"a", "b"}, result);
    }

    @Test(timeout = 4000)
    public void testSubarrayObjectEndOverLength() {
        String[] input = {"a", "b", "c"};
        String[] result = ArrayUtils.subarray(input, 1, 10);
        assertArrayEquals(new String[]{"b", "c"}, result);
    }

    @Test(timeout = 4000)
    public void testSubarrayObjectEmptyResult() {
        String[] input = {"a", "b", "c"};
        String[] result = ArrayUtils.subarray(input, 2, 1);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testSubarrayObjectZeroLength() {
        String[] input = {"a"};
        String[] result = ArrayUtils.subarray(input, 0, 0);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testSubarrayObjectNormal() {
        String[] input = {"a", "b", "c", "d"};
        String[] result = ArrayUtils.subarray(input, 1, 3);
        assertArrayEquals(new String[]{"b", "c"}, result);
    }

    @Test(timeout = 4000)
    public void testSubarrayLongNull() {
        assertNull(ArrayUtils.subarray((long[]) null, 0, 1));
    }

    @Test(timeout = 4000)
    public void testSubarrayLongNormal() {
        long[] input = {1L, 2L, 3L};
        long[] result = ArrayUtils.subarray(input, 0, 2);
        assertArrayEquals(new long[]{1L, 2L}, result);
    }

    @Test(timeout = 4000)
    public void testSubarrayIntEmpty() {
        int[] input = {1, 2};
        int[] result = ArrayUtils.subarray(input, 1, 1);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testSubarrayShortNegativeStart() {
        short[] input = {1, 2, 3};
        short[] result = ArrayUtils.subarray(input, -5, 2);
        assertArrayEquals(new short[]{1, 2}, result);
    }

    @Test(timeout = 4000)
    public void testSubarrayCharEndOver() {
        char[] input = {'a', 'b'};
        char[] result = ArrayUtils.subarray(input, 0, 5);
        assertArrayEquals(new char[]{'a', 'b'}, result);
    }

    @Test(timeout = 4000)
    public void testSubarrayByteNormal() {
        byte[] input = {10, 20, 30};
        byte[] result = ArrayUtils.subarray(input, 1, 3);
        assertArrayEquals(new byte[]{20, 30}, result);
    }

    @Test(timeout = 4000)
    public void testSubarrayDoubleNormal() {
        double[] input = {1.1, 2.2, 3.3};
        double[] result = ArrayUtils.subarray(input, 0, 1);
        assertArrayEquals(new double[]{1.1}, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testSubarrayFloatEmpty() {
        float[] input = {1.0f};
        float[] result = ArrayUtils.subarray(input, 1, 0);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testSubarrayBooleanNormal() {
        boolean[] input = {true, false, true};
        boolean[] result = ArrayUtils.subarray(input, 1, 3);
        assertArrayEquals(new boolean[]{false, true}, result);
    }

    @Test(timeout = 4000)
    public void testIsSameLengthBothNull() {
        assertTrue(ArrayUtils.isSameLength((Object[]) null, (Object[]) null));
    }

    @Test(timeout = 4000)
    public void testIsSameLengthOneNullOtherEmpty() {
        assertTrue(ArrayUtils.isSameLength((Object[]) null, new Object[0]));
    }

    @Test(timeout = 4000)
    public void testIsSameLengthOneNullOtherNonEmpty() {
        assertFalse(ArrayUtils.isSameLength((Object[]) null, new Object[]{"a"}));
    }

    @Test(timeout = 4000)
    public void testIsSameLengthDifferentLength() {
        assertFalse(ArrayUtils.isSameLength(new Object[]{"a"}, new Object[]{"a", "b"}));
    }

    @Test(timeout = 4000)
    public void testIsSameLengthSameLength() {
        assertTrue(ArrayUtils.isSameLength(new Object[]{"a", "b"}, new Object[]{"c", "d"}));
    }

    @Test(timeout = 4000)
    public void testIsSameLengthPrimitiveLong() {
        assertTrue(ArrayUtils.isSameLength(new long[]{1L}, new long[]{2L}));
        assertFalse(ArrayUtils.isSameLength(new long[]{1L}, new long[]{1L, 2L}));
    }

    @Test(timeout = 4000)
    public void testIsSameLengthPrimitiveInt() {
        assertTrue(ArrayUtils.isSameLength(new int[0], new int[0]));
        assertFalse(ArrayUtils.isSameLength((int[]) null, new int[]{1}));
    }

    @Test(timeout = 4000)
    public void testIsSameLengthPrimitiveShort() {
        assertTrue(ArrayUtils.isSameLength((short[]) null, (short[]) null));
        assertFalse(ArrayUtils.isSameLength((short[]) null, new short[]{1}));
        assertTrue(ArrayUtils.isSameLength(new short[]{1, 2}, new short[]{3, 4}));
    }

    @Test(timeout = 4000)
    public void testIsSameLengthPrimitiveChar() {
        assertTrue(ArrayUtils.isSameLength((char[]) null, new char[0]));
        assertFalse(ArrayUtils.isSameLength((char[]) null, new char[]{'a'}));
    }

    @Test(timeout = 4000)
    public void testIsSameLengthPrimitiveByte() {
        assertTrue(ArrayUtils.isSameLength(new byte[0], new byte[0]));
        assertFalse(ArrayUtils.isSameLength(new byte[]{1}, new byte[]{1, 2}));
    }

    @Test(timeout = 4000)
    public void testIsSameLengthPrimitiveDouble() {
        assertTrue(ArrayUtils.isSameLength((double[]) null, (double[]) null));
        assertFalse(ArrayUtils.isSameLength((double[]) null, new double[]{1.0}));
    }

    @Test(timeout = 4000)
    public void testIsSameLengthPrimitiveFloat() {
        assertTrue(ArrayUtils.isSameLength(new float[]{1.0f}, new float[]{2.0f}));
        assertFalse(ArrayUtils.isSameLength(new float[]{1.0f}, new float[0]));
    }

    @Test(timeout = 4000)
    public void testIsSameLengthPrimitiveBoolean() {
        assertTrue(ArrayUtils.isSameLength((boolean[]) null, new boolean[0]));
        assertFalse(ArrayUtils.isSameLength(new boolean[]{true}, new boolean[]{true, false}));
    }

    @Test(timeout = 4000)
    public void testGetLengthNull() {
        assertEquals(0, ArrayUtils.getLength(null));
    }

    @Test(timeout = 4000)
    public void testGetLengthEmptyArray() {
        assertEquals(0, ArrayUtils.getLength(new Object[0]));
    }

    @Test(timeout = 4000)
    public void testGetLengthNonEmpty() {
        assertEquals(3, ArrayUtils.getLength(new String[]{"a", "b", "c"}));
    }

    @Test(timeout = 4000)
    public void testGetLengthPrimitive() {
        assertEquals(2, ArrayUtils.getLength(new int[]{1, 2}));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetLengthNonArray() {
        ArrayUtils.getLength("not an array");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameTypeNullFirst() {
        ArrayUtils.isSameType(null, new Object[0]);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameTypeNullSecond() {
        ArrayUtils.isSameType(new Object[0], null);
    }

    @Test(timeout = 4000)
    public void testIsSameTypeMatching() {
        assertTrue(ArrayUtils.isSameType(new String[0], new String[1]));
    }

    @Test(timeout = 4000)
    public void testIsSameTypeNotMatching() {
        assertFalse(ArrayUtils.isSameType(new String[0], new Integer[0]));
    }

    // ========== Partition C: Defect-Targeted Branch Zone (LANG-571) ==========

    @Test(timeout = 4000)
    public void testAddNullNullTypeSafety() {
        // This test targets the LANG-571 bug: when both array and element are null,
        // the method should return Object[] but instead is cast to T[] which causes ClassCastException
        Object[] result = ArrayUtils.add((String[]) null, null);
        assertNotNull(result);
        assertEquals(1, result.length);
        assertNull(result[0]);
        // The method should return Object[], not String[]
        assertEquals(Object[].class, result.getClass());
    }

    @Test(timeout = 4000)
    public void testAddNullNonNullElement() {
        String[] result = ArrayUtils.add((String[]) null, "test");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("test", result[0]);
        assertEquals(String[].class, result.getClass());
    }

    @Test(timeout = 4000)
    public void testAddNormal() {
        String[] input = {"a", "b"};
        String[] result = ArrayUtils.add(input, "c");
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test(timeout = 4000)
    public void testAddToEmptyArray() {
        String[] input = {};
        String[] result = ArrayUtils.add(input, "a");
        assertArrayEquals(new String[]{"a"}, result);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexNullArrayWithNonZeroIndex() {
        ArrayUtils.add((String[]) null, 1, "a");
    }

    @Test(timeout = 4000)
    public void testAddAtIndexNullArrayIndexZero() {
        String[] result = ArrayUtils.add((String[]) null, 0, "a");
        assertArrayEquals(new String[]{"a"}, result);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexNegativeIndex() {
        ArrayUtils.add(new String[]{"a"}, -1, "b");
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testAddAtIndexExceedsLength() {
        ArrayUtils.add(new String[]{"a"}, 2, "b");
    }

    @Test(timeout = 4000)
    public void testAddAtIndexStart() {
        String[] result = ArrayUtils.add(new String[]{"a", "b"}, 0, "c");
        assertArrayEquals(new String[]{"c", "a", "b"}, result);
    }

    @Test(timeout = 4000)
    public void testAddAtIndexMiddle() {
        String[] result = ArrayUtils.add(new String[]{"a", "c"}, 1, "b");
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test(timeout = 4000)
    public void testAddAtIndexEnd() {
        String[] result = ArrayUtils.add(new String[]{"a", "b"}, 2, "c");
        assertArrayEquals(new String[]{"a", "b", "c"}, result);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testRemoveNullArray() {
        ArrayUtils.remove((String[]) null, 0);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testRemoveNegativeIndex() {
        ArrayUtils.remove(new String[]{"a"}, -1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testRemoveIndexEqualsLength() {
        ArrayUtils.remove(new String[]{"a"}, 1);
    }

    @Test(timeout = 4000)
    public void testRemoveFirst() {
        String[] result = ArrayUtils.remove(new String[]{"a", "b", "c"}, 0);
        assertArrayEquals(new String[]{"b", "c"}, result);
    }

    @Test(timeout = 4000)
    public void testRemoveLast() {
        String[] result = ArrayUtils.remove(new String[]{"a", "b", "c"}, 2);
        assertArrayEquals(new String[]{"a", "b"}, result);
    }

    @Test(timeout = 4000)
    public void testRemoveMiddle() {
        String[] result = ArrayUtils.remove(new String[]{"a", "b", "c"}, 1);
        assertArrayEquals(new String[]{"a", "c"}, result);
    }

    @Test(timeout = 4000)
    public void testRemoveSingleElement() {
        String[] result = ArrayUtils.remove(new String[]{"a"}, 0);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testRemoveElementNullArray() {
        assertNull(ArrayUtils.removeElement((String[]) null, "a"));
    }

    @Test(timeout = 4000)
    public void testRemoveElementNotFound() {
        String[] input = {"a", "b"};
        String[] result = ArrayUtils.removeElement(input, "c");
        assertArrayEquals(input, result);
        assertNotSame(input, result);
    }

    @Test(timeout = 4000)
    public void testRemoveElementFoundFirst() {
        String[] result = ArrayUtils.removeElement(new String[]{"a", "b", "a"}, "a");
        assertArrayEquals(new String[]{"b", "a"}, result);
    }

    @Test(timeout = 4000)
    public void testRemoveElementFoundLast() {
        String[] result = ArrayUtils.removeElement(new String[]{"a", "b", "c"}, "c");
        assertArrayEquals(new String[]{"a", "b"}, result);
    }

    @Test(timeout = 4000)
    public void testRemoveElementOnPrimitiveBoolean() {
        boolean[] result = ArrayUtils.removeElement(new boolean[]{true, false, true}, true);
        assertArrayEquals(new boolean[]{false, true}, result);
    }

    @Test(timeout = 4000)
    public void testRemoveElementOnPrimitiveByte() {
        byte[] result = ArrayUtils.removeElement(new byte[]{1, 2, 3}, (byte) 2);
        assertArrayEquals(new byte[]{1, 3}, result);
    }

    @Test(timeout = 4000)
    public void testRemoveElementOnPrimitiveChar() {
        char[] result = ArrayUtils.removeElement(new char[]{'a', 'b', 'c'}, 'b');
        assertArrayEquals(new char[]{'a', 'c'}, result);
    }

    @Test(timeout = 4000)
    public void testRemoveElementOnPrimitiveDouble() {
        double[] result = ArrayUtils.removeElement(new double[]{1.1, 2.2, 3.3}, 2.2);
        assertArrayEquals(new double[]{1.1, 3.3}, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testRemoveElementOnPrimitiveFloat() {
        float[] result = ArrayUtils.removeElement(new float[]{1.0f, 2.0f}, 2.0f);
        assertArrayEquals(new float[]{1.0f}, result, 0.0f);
    }

    @Test(timeout = 4000)
    public void testRemoveElementOnPrimitiveInt() {
        int[] result = ArrayUtils.removeElement(new int[]{1, 2, 3}, 3);
        assertArrayEquals(new int[]{1, 2}, result);
    }

    @Test(timeout = 4000)
    public void testRemoveElementOnPrimitiveLong() {
        long[] result = ArrayUtils.removeElement(new long[]{1L, 2L, 3L}, 1L);
        assertArrayEquals(new long[]{2L, 3L}, result);
    }

    @Test(timeout = 4000)
    public void testRemoveElementOnPrimitiveShort() {
        short[] result = ArrayUtils.removeElement(new short[]{1, 2, 3}, (short) 1);
        assertArrayEquals(new short[]{2, 3}, result);
    }

    @Test(timeout = 4000)
    public void testAddAllObjectBothNull() {
        assertNull(ArrayUtils.addAll((String[]) null, (String[]) null));
    }

    @Test(timeout = 4000)
    public void testAddAllObjectFirstNull() {
        String[] result = ArrayUtils.addAll((String[]) null, "a", "b");
        assertArrayEquals(new String[]{"a", "b"}, result);
    }

    @Test(timeout = 4000)
    public void testAddAllObjectSecondNull() {
        String[] result = ArrayUtils.addAll(new String[]{"a"}, (String[]) null);
        assertArrayEquals(new String[]{"a"}, result);
    }

    @Test(timeout = 4000)
    public void testAddAllObjectBothNonNull() {
        String[] result = ArrayUtils.addAll(new String[]{"a", "b"}, "c", "d");
        assertArrayEquals(new String[]{"a", "b", "c", "d"}, result);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddAllTypeMismatch() {
        ArrayUtils.addAll(new Number[]{1}, "string");
    }

    @Test(timeout = 4000)
    public void testAddAllBoolean() {
        boolean[] result = ArrayUtils.addAll(new boolean[]{true}, false);
        assertArrayEquals(new boolean[]{true, false}, result);
    }

    @Test(timeout = 4000)
    public void testAddAllChar() {
        char[] result = ArrayUtils.addAll(new char[]{'a'}, 'b', 'c');
        assertArrayEquals(new char[]{'a', 'b', 'c'}, result);
    }

    @Test(timeout = 4000)
    public void testAddAllByte() {
        byte[] result = ArrayUtils.addAll(new byte[]{1}, (byte) 2);
        assertArrayEquals(new byte[]{1, 2}, result);
    }

    @Test(timeout = 4000)
    public void testAddAllShort() {
        short[] result = ArrayUtils.addAll(new short[]{1}, (short) 2);
        assertArrayEquals(new short[]{1, 2}, result);
    }

    @Test(timeout = 4000)
    public void testAddAllInt() {
        int[] result = ArrayUtils.addAll(new int[]{1}, 2, 3);
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    @Test(timeout = 4000)
    public void testAddAllLong() {
        long[] result = ArrayUtils.addAll(new long[]{1L}, 2L);
        assertArrayEquals(new long[]{1L, 2L}, result);
    }

    @Test(timeout = 4000)
    public void testAddAllFloat() {
        float[] result = ArrayUtils.addAll(new float[]{1.0f}, 2.0f);
        assertArrayEquals(new float[]{1.0f, 2.0f}, result, 0.0f);
    }

    @Test(timeout = 4000)
    public void testAddAllDouble() {
        double[] result = ArrayUtils.addAll(new double[]{1.0}, 2.0);
        assertArrayEquals(new double[]{1.0, 2.0}, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testAddPrimitiveBoolean() {
        boolean[] result = ArrayUtils.add((boolean[]) null, true);
        assertArrayEquals(new boolean[]{true}, result);
        boolean[] result2 = ArrayUtils.add(new boolean[]{false}, true);
        assertArrayEquals(new boolean[]{false, true}, result2);
    }

    @Test(timeout = 4000)
    public void testAddPrimitiveByte() {
        byte[] result = ArrayUtils.add((byte[]) null, (byte) 1);
        assertArrayEquals(new byte[]{1}, result);
    }

    @Test(timeout = 4000)
    public void testAddPrimitiveChar() {
        char[] result = ArrayUtils.add((char[]) null, 'a');
        assertArrayEquals(new char[]{'a'}, result);
    }

    @Test(timeout = 4000)
    public void testAddPrimitiveDouble() {
        double[] result = ArrayUtils.add((double[]) null, 1.0);
        assertArrayEquals(new double[]{1.0}, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testAddPrimitiveFloat() {
        float[] result = ArrayUtils.add((float[]) null, 1.0f);
        assertArrayEquals(new float[]{1.0f}, result, 0.0f);
    }

    @Test(timeout = 4000)
    public void testAddPrimitiveInt() {
        int[] result = ArrayUtils.add((int[]) null, 1);
        assertArrayEquals(new int[]{1}, result);
    }

    @Test(timeout = 4000)
    public void testAddPrimitiveLong() {
        long[] result = ArrayUtils.add((long[]) null, 1L);
        assertArrayEquals(new long[]{1L}, result);
    }

    @Test(timeout = 4000)
    public void testAddPrimitiveShort() {
        short[] result = ArrayUtils.add((short[]) null, (short) 1);
        assertArrayEquals(new short[]{1}, result);
    }

    @Test(timeout = 4000)
    public void testAddAtIndexPrimitiveBoolean() {
        boolean[] result = ArrayUtils.add(new boolean[]{true, true}, 1, false);
        assertArrayEquals(new boolean[]{true, false, true}, result);
    }

    @Test(timeout = 4000)
    public void testAddAtIndexPrimitiveByte() {
        byte[] result = ArrayUtils.add(new byte[]{1, 3}, 1, (byte) 2);
        assertArrayEquals(new byte[]{1, 2, 3}, result);
    }

    @Test(timeout = 4000)
    public void testAddAtIndexPrimitiveChar() {
        char[] result = ArrayUtils.add(new char[]{'a', 'c'}, 1, 'b');
        assertArrayEquals(new char[]{'a', 'b', 'c'}, result);
    }

    @Test(timeout = 4000)
    public void testAddAtIndexPrimitiveDouble() {
        double[] result = ArrayUtils.add(new double[]{1.0, 3.0}, 1, 2.0);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testAddAtIndexPrimitiveFloat() {
        float[] result = ArrayUtils.add(new float[]{1.0f, 3.0f}, 1, 2.0f);
        assertArrayEquals(new float[]{1.0f, 2.0f, 3.0f}, result, 0.0f);
    }

    @Test(timeout = 4000)
    public void testAddAtIndexPrimitiveInt() {
        int[] result = ArrayUtils.add(new int[]{1, 3}, 1, 2);
        assertArrayEquals(new int[]{1, 2, 3}, result);
    }

    @Test(timeout = 4000)
    public void testAddAtIndexPrimitiveLong() {
        long[] result = ArrayUtils.add(new long[]{1L, 3L}, 1, 2L);
        assertArrayEquals(new long[]{1L, 2L, 3L}, result);
    }

    @Test(timeout = 4000)
    public void testAddAtIndexPrimitiveShort() {
        short[] result = ArrayUtils.add(new short[]{1, 3}, 1, (short) 2);
        assertArrayEquals(new short[]{1, 2, 3}, result);
    }

    @Test(timeout = 4000)
    public void testIsEmptyObjectNull() { assertTrue(ArrayUtils.isEmpty((Object[]) null)); }

    @Test(timeout = 4000)
    public void testIsEmptyObjectEmpty() { assertTrue(ArrayUtils.isEmpty(new Object[0])); }

    @Test(timeout = 4000)
    public void testIsEmptyObjectNonEmpty() { assertFalse(ArrayUtils.isEmpty(new Object[]{"a"})); }

    @Test(timeout = 4000)
    public void testIsEmptyLong() { assertTrue(ArrayUtils.isEmpty(new long[0])); assertFalse(ArrayUtils.isEmpty(new long[]{1L})); }

    @Test(timeout = 4000)
    public void testIsEmptyInt() { assertTrue(ArrayUtils.isEmpty((int[]) null)); assertFalse(ArrayUtils.isEmpty(new int[]{1})); }

    @Test(timeout = 4000)
    public void testIsEmptyShort() { assertTrue(ArrayUtils.isEmpty(new short[0])); assertFalse(ArrayUtils.isEmpty(new short[]{1})); }

    @Test(timeout = 4000)
    public void testIsEmptyChar() { assertTrue(ArrayUtils.isEmpty((char[]) null)); assertFalse(ArrayUtils.isEmpty(new char[]{'a'})); }

    @Test(timeout = 4000)
    public void testIsEmptyByte() { assertTrue(ArrayUtils.isEmpty(new byte[0])); assertFalse(ArrayUtils.isEmpty(new byte[]{1})); }

    @Test(timeout = 4000)
    public void testIsEmptyDouble() { assertTrue(ArrayUtils.isEmpty((double[]) null)); assertFalse(ArrayUtils.isEmpty(new double[]{1.0})); }

    @Test(timeout = 4000)
    public void testIsEmptyFloat() { assertTrue(ArrayUtils.isEmpty(new float[0])); assertFalse(ArrayUtils.isEmpty(new float[]{1.0f})); }

    @Test(timeout = 4000)
    public void testIsEmptyBoolean() { assertTrue(ArrayUtils.isEmpty((boolean[]) null)); assertFalse(ArrayUtils.isEmpty(new boolean[]{true})); }

    // ========== IndexOf/LastIndexOf/Contains with all primitives and Object ==========

    @Test(timeout = 4000)
    public void testIndexOfObjectNullArray() {
        assertEquals(-1, ArrayUtils.indexOf((Object[]) null, "a"));
    }

    @Test(timeout = 4000)
    public void testIndexOfObjectNullValue() {
        assertEquals(0, ArrayUtils.indexOf(new Object[]{null, "a"}, null));
    }

    @Test(timeout = 4000)
    public void testIndexOfObjectFound() {
        assertEquals(1, ArrayUtils.indexOf(new Object[]{"a", "b", "c"}, "b"));
    }

    @Test(timeout = 4000)
    public void testIndexOfObjectNotFound() {
        assertEquals(-1, ArrayUtils.indexOf(new Object[]{"a", "b"}, "c"));
    }

    @Test(timeout = 4000)
    public void testIndexOfObjectStartIndexNegative() {
        assertEquals(0, ArrayUtils.indexOf(new Object[]{"a", "b"}, "a", -1));
    }

    @Test(timeout = 4000)
    public void testIndexOfObjectStartIndexTooLarge() {
        assertEquals(-1, ArrayUtils.indexOf(new Object[]{"a", "b"}, "a", 5));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfObjectNullArray() {
        assertEquals(-1, ArrayUtils.lastIndexOf((Object[]) null, "a"));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfObjectNullValue() {
        assertEquals(2, ArrayUtils.lastIndexOf(new Object[]{"a", null, "b", null}, null));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfObjectFound() {
        assertEquals(2, ArrayUtils.lastIndexOf(new Object[]{"a", "b", "c", "b"}, "b"));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfObjectStartIndexNegative() {
        assertEquals(-1, ArrayUtils.lastIndexOf(new Object[]{"a"}, "a", -1));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfObjectStartIndexExceedsLength() {
        assertEquals(0, ArrayUtils.lastIndexOf(new Object[]{"a"}, "a", 10));
    }

    @Test(timeout = 4000)
    public void testContainsObject() {
        assertTrue(ArrayUtils.contains(new Object[]{"a", "b"}, "a"));
        assertFalse(ArrayUtils.contains(new Object[]{"a", "b"}, "c"));
        assertFalse(ArrayUtils.contains((Object[]) null, "a"));
    }

    @Test(timeout = 4000)
    public void testIndexOfLong() {
        assertEquals(1, ArrayUtils.indexOf(new long[]{1L, 2L, 3L}, 2L));
        assertEquals(-1, ArrayUtils.indexOf(new long[]{1L}, 2L));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfLong() {
        assertEquals(2, ArrayUtils.lastIndexOf(new long[]{1L, 2L, 2L}, 2L));
    }

    @Test(timeout = 4000)
    public void testContainsLong() {
        assertTrue(ArrayUtils.contains(new long[]{1L, 2L}, 1L));
    }

    @Test(timeout = 4000)
    public void testIndexOfInt() {
        assertEquals(0, ArrayUtils.indexOf(new int[]{1, 2, 3}, 1));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfInt() {
        assertEquals(1, ArrayUtils.lastIndexOf(new int[]{1, 2, 2}, 2));
    }

    @Test(timeout = 4000)
    public void testContainsInt() {
        assertFalse(ArrayUtils.contains(new int[]{1, 2}, 3));
    }

    @Test(timeout = 4000)
    public void testIndexOfShort() {
        assertEquals(0, ArrayUtils.indexOf(new short[]{1, 2}, (short) 1));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfShort() {
        assertEquals(1, ArrayUtils.lastIndexOf(new short[]{1, 2}, (short) 2));
    }

    @Test(timeout = 4000)
    public void testContainsShort() {
        assertTrue(ArrayUtils.contains(new short[]{1, 2}, (short) 2));
    }

    @Test(timeout = 4000)
    public void testIndexOfChar() {
        assertEquals(1, ArrayUtils.indexOf(new char[]{'a', 'b', 'c'}, 'b'));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfChar() {
        assertEquals(2, ArrayUtils.lastIndexOf(new char[]{'a', 'b', 'b'}, 'b'));
    }

    @Test(timeout = 4000)
    public void testContainsChar() {
        assertFalse(ArrayUtils.contains(new char[]{'a'}, 'b'));
    }

    @Test(timeout = 4000)
    public void testIndexOfByte() {
        assertEquals(0, ArrayUtils.indexOf(new byte[]{1, 2}, (byte) 1));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfByte() {
        assertEquals(1, ArrayUtils.lastIndexOf(new byte[]{1, 2}, (byte) 2));
    }

    @Test(timeout = 4000)
    public void testContainsByte() {
        assertTrue(ArrayUtils.contains(new byte[]{1, 2}, (byte) 1));
    }

    @Test(timeout = 4000)
    public void testIndexOfDouble() {
        assertEquals(1, ArrayUtils.indexOf(new double[]{1.0, 2.0, 3.0}, 2.0));
    }

    @Test(timeout = 4000)
    public void testIndexOfDoubleWithTolerance() {
        assertEquals(1, ArrayUtils.indexOf(new double[]{1.0, 2.0, 3.0}, 2.0, 0.1));
    }

    @Test(timeout = 4000)
    public void testIndexOfDoubleWithToleranceFoundWithinRange() {
        assertEquals(0, ArrayUtils.indexOf(new double[]{1.05, 2.0}, 1.0, 0.1));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfDouble() {
        assertEquals(2, ArrayUtils.lastIndexOf(new double[]{1.0, 2.0, 2.0}, 2.0));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfDoubleWithTolerance() {
        assertEquals(2, ArrayUtils.lastIndexOf(new double[]{1.0, 2.0, 2.0}, 2.0, 0.1));
    }

    @Test(timeout = 4000)
    public void testContainsDouble() {
        assertTrue(ArrayUtils.contains(new double[]{1.0, 2.0}, 2.0));
    }

    @Test(timeout = 4000)
    public void testContainsDoubleWithTolerance() {
        assertTrue(ArrayUtils.contains(new double[]{1.0, 2.0}, 1.05, 0.1));
    }

    @Test(timeout = 4000)
    public void testIndexOfFloat() {
        assertEquals(0, ArrayUtils.indexOf(new float[]{1.0f, 2.0f}, 1.0f));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfFloat() {
        assertEquals(1, ArrayUtils.lastIndexOf(new float[]{1.0f, 2.0f}, 2.0f));
    }

    @Test(timeout = 4000)
    public void testContainsFloat() {
        assertTrue(ArrayUtils.contains(new float[]{1.0f, 2.0f}, 2.0f));
    }

    @Test(timeout = 4000)
    public void testIndexOfBoolean() {
        assertEquals(1, ArrayUtils.indexOf(new boolean[]{true, false, true}, false));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfBoolean() {
        assertEquals(2, ArrayUtils.lastIndexOf(new boolean[]{true, false, true}, true));
    }

    @Test(timeout = 4000)
    public void testContainsBoolean() {
        assertFalse(ArrayUtils.contains(new boolean[]{true}, false));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testToStringObjectNull() {
        assertEquals("{}", ArrayUtils.toString(null));
    }

    @Test(timeout = 4000)
    public void testToStringObject() {
        String result = ArrayUtils.toString(new String[]{"a", "b"});
        assertNotNull(result);
        assertTrue(result.contains("a") && result.contains("b"));
    }

    @Test(timeout = 4000)
    public void testToStringWithNullString() {
        assertEquals("default", ArrayUtils.toString(null, "default"));
    }

    @Test(timeout = 4000)
    public void testIsEqualsBothNull() {
        assertTrue(ArrayUtils.isEquals(null, null));
    }

    @Test(timeout = 4000)
    public void testIsEqualsEquals() {
        assertTrue(ArrayUtils.isEquals(new int[]{1, 2}, new int[]{1, 2}));
    }

    @Test(timeout = 4000)
    public void testIsEqualsNotEquals() {
        assertFalse(ArrayUtils.isEquals(new int[]{1, 2}, new int[]{1, 3}));
    }

    @Test(timeout = 4000)
    public void testToArray() {
        String[] result = ArrayUtils.toArray("a", "b");
        assertArrayEquals(new String[]{"a", "b"}, result);
    }

    @Test(timeout = 4000)
    public void testToArrayEmpty() {
        String[] result = ArrayUtils.toArray();
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testToArrayGeneric() {
        Number[] result = ArrayUtils.<Number>toArray(new Integer(1), new Double(2.0));
        assertEquals(2, result.length);
    }

    // ========== toPrimitive / toObject converters ==========

    @Test(timeout = 4000)
    public void testToPrimitiveCharacterNull() {
        assertNull(ArrayUtils.toPrimitive((Character[]) null));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveCharacterEmpty() {
        assertArrayEquals(new char[0], ArrayUtils.toPrimitive(new Character[0]));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveCharacter() {
        assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.toPrimitive(new Character[]{'a', 'b'}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveCharacterWithNull() {
        assertArrayEquals(new char[]{'a', 'x'}, ArrayUtils.toPrimitive(new Character[]{'a', null}, 'x'));
    }

    @Test(timeout = 4000)
    public void testToObjectChar() {
        assertNull(ArrayUtils.toObject((char[]) null));
        assertArrayEquals(new Character[]{'a', 'b'}, ArrayUtils.toObject(new char[]{'a', 'b'}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveLong() {
        assertArrayEquals(new long[]{1L, 2L}, ArrayUtils.toPrimitive(new Long[]{1L, 2L}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveLongWithNull() {
        assertArrayEquals(new long[]{1L, 0L}, ArrayUtils.toPrimitive(new Long[]{1L, null}, 0L));
    }

    @Test(timeout = 4000)
    public void testToObjectLong() {
        assertArrayEquals(new Long[]{1L, 2L}, ArrayUtils.toObject(new long[]{1L, 2L}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveInt() {
        assertArrayEquals(new int[]{1, 2}, ArrayUtils.toPrimitive(new Integer[]{1, 2}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveIntWithNull() {
        assertArrayEquals(new int[]{1, -1}, ArrayUtils.toPrimitive(new Integer[]{1, null}, -1));
    }

    @Test(timeout = 4000)
    public void testToObjectInt() {
        assertArrayEquals(new Integer[]{1, 2}, ArrayUtils.toObject(new int[]{1, 2}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveShort() {
        assertArrayEquals(new short[]{1, 2}, ArrayUtils.toPrimitive(new Short[]{1, 2}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveShortWithNull() {
        assertArrayEquals(new short[]{1, 0}, ArrayUtils.toPrimitive(new Short[]{1, null}, (short) 0));
    }

    @Test(timeout = 4000)
    public void testToObjectShort() {
        assertArrayEquals(new Short[]{1, 2}, ArrayUtils.toObject(new short[]{1, 2}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveByte() {
        assertArrayEquals(new byte[]{1, 2}, ArrayUtils.toPrimitive(new Byte[]{1, 2}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveByteWithNull() {
        assertArrayEquals(new byte[]{1, 0}, ArrayUtils.toPrimitive(new Byte[]{1, null}, (byte) 0));
    }

    @Test(timeout = 4000)
    public void testToObjectByte() {
        assertArrayEquals(new Byte[]{1, 2}, ArrayUtils.toObject(new byte[]{1, 2}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveDouble() {
        assertArrayEquals(new double[]{1.0, 2.0}, ArrayUtils.toPrimitive(new Double[]{1.0, 2.0}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveDoubleWithNull() {
        assertArrayEquals(new double[]{1.0, 0.0}, ArrayUtils.toPrimitive(new Double[]{1.0, null}, 0.0));
    }

    @Test(timeout = 4000)
    public void testToObjectDouble() {
        assertArrayEquals(new Double[]{1.0, 2.0}, ArrayUtils.toObject(new double[]{1.0, 2.0}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveFloat() {
        assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.toPrimitive(new Float[]{1.0f, 2.0f}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveFloatWithNull() {
        assertArrayEquals(new float[]{1.0f, 0.0f}, ArrayUtils.toPrimitive(new Float[]{1.0f, null}, 0.0f));
    }

    @Test(timeout = 4000)
    public void testToObjectFloat() {
        assertArrayEquals(new Float[]{1.0f, 2.0f}, ArrayUtils.toObject(new float[]{1.0f, 2.0f}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveBoolean() {
        assertArrayEquals(new boolean[]{true, false}, ArrayUtils.toPrimitive(new Boolean[]{true, false}));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveBooleanWithNull() {
        assertArrayEquals(new boolean[]{true, false}, ArrayUtils.toPrimitive(new Boolean[]{true, null}, false));
    }

    @Test(timeout = 4000)
    public void testToObjectBoolean() {
        assertArrayEquals(new Boolean[]{true, false}, ArrayUtils.toObject(new boolean[]{true, false}));
    }

    // ========== Reverse tests ==========

    @Test(timeout = 4000)
    public void testReverseObjectNull() {
        ArrayUtils.reverse((Object[]) null);  // should not throw
    }

    @Test(timeout = 4000)
    public void testReverseObjectSingle() {
        Object[] input = {"a"};
        ArrayUtils.reverse(input);
        assertArrayEquals(new Object[]{"a"}, input);
    }

    @Test(timeout = 4000)
    public void testReverseObjectEven() {
        Object[] input = {"a", "b", "c", "d"};
        ArrayUtils.reverse(input);
        assertArrayEquals(new Object[]{"d", "c", "b", "a"}, input);
    }

    @Test(timeout = 4000)
    public void testReverseObjectOdd() {
        Object[] input = {"a", "b", "c"};
        ArrayUtils.reverse(input);
        assertArrayEquals(new Object[]{"c", "b", "a"}, input);
    }

    @Test(timeout = 4000)
    public void testReverseLong() {
        long[] input = {1L, 2L, 3L};
        ArrayUtils.reverse(input);
        assertArrayEquals(new long[]{3L, 2L, 1L}, input);
    }

    @Test(timeout = 4000)
    public void testReverseInt() {
        int[] input = {1, 2};
        ArrayUtils.reverse(input);
        assertArrayEquals(new int[]{2, 1}, input);
    }

    @Test(timeout = 4000)
    public void testReverseShort() {
        short[] input = {1, 2, 3, 4};
        ArrayUtils.reverse(input);
        assertArrayEquals(new short[]{4, 3, 2, 1}, input);
    }

    @Test(timeout = 4000)
    public void testReverseChar() {
        char[] input = {'a', 'b'};
        ArrayUtils.reverse(input);
        assertArrayEquals(new char[]{'b', 'a'}, input);
    }

    @Test(timeout = 4000)
    public void testReverseByte() {
        byte[] input = {1, 2, 3};
        ArrayUtils.reverse(input);
        assertArrayEquals(new byte[]{3, 2, 1}, input);
    }

    @Test(timeout = 4000)
    public void testReverseDouble() {
        double[] input = {1.0, 2.0, 3.0};
        ArrayUtils.reverse(input);
        assertArrayEquals(new double[]{3.0, 2.0, 1.0}, input, 0.0);
    }

    @Test(timeout = 4000)
    public void testReverseFloat() {
        float[] input = {1.0f, 2.0f};
        ArrayUtils.reverse(input);
        assertArrayEquals(new float[]{2.0f, 1.0f}, input, 0.0f);
    }

    @Test(timeout = 4000)
    public void testReverseBoolean() {
        boolean[] input = {true, false, true};
        ArrayUtils.reverse(input);
        assertArrayEquals(new boolean[]{true, false, true}, input);
    }

    // ========== Additional edge cases ==========

    @Test(timeout = 4000)
    public void testAddAllBothEmpty() {
        String[] result = ArrayUtils.addAll(new String[0], new String[0]);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test(timeout = 4000)
    public void testRemovePrimitiveBoolean() {
        boolean[] result = ArrayUtils.remove(new boolean[]{true, false, true}, 1);
        assertArrayEquals(new boolean[]{true, true}, result);
    }

    @Test(timeout = 4000)
    public void testRemovePrimitiveByte() {
        byte[] result = ArrayUtils.remove(new byte[]{1, 2, 3}, 2);
        assertArrayEquals(new byte[]{1, 2}, result);
    }

    @Test(timeout = 4000)
    public void testRemovePrimitiveChar() {
        char[] result = ArrayUtils.remove(new char[]{'a', 'b', 'c'}, 0);
        assertArrayEquals(new char[]{'b', 'c'}, result);
    }

    @Test(timeout = 4000)
    public void testRemovePrimitiveDouble() {
        double[] result = ArrayUtils.remove(new double[]{1.0, 2.0, 3.0}, 2);
        assertArrayEquals(new double[]{1.0, 2.0}, result, 0.0);
    }

    @Test(timeout = 4000)
    public void testRemovePrimitiveFloat() {
        float[] result = ArrayUtils.remove(new float[]{1.0f, 2.0f}, 1);
        assertArrayEquals(new float[]{1.0f}, result, 0.0f);
    }

    @Test(timeout = 4000)
    public void testRemovePrimitiveInt() {
        int[] result = ArrayUtils.remove(new int[]{1, 2, 3}, 1);
        assertArrayEquals(new int[]{1, 3}, result);
    }

    @Test(timeout = 4000)
    public void testRemovePrimitiveLong() {
        long[] result = ArrayUtils.remove(new long[]{1L, 2L, 3L}, 0);
        assertArrayEquals(new long[]{2L, 3L}, result);
    }

    @Test(timeout = 4000)
    public void testRemovePrimitiveShort() {
        short[] result = ArrayUtils.remove(new short[]{1, 2, 3}, 2);
        assertArrayEquals(new short[]{1, 2}, result);
    }

    @Test(timeout = 4000)
    public void testAddAllObjectArrayStoreException() {
        // When types are incompatible, should throw IllegalArgumentException
        try {
            ArrayUtils.addAll(new Number[]{1}, "string");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}