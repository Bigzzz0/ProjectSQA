package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive white-box test suite for ArrayUtils.
 * Targets line/branch coverage and the known Defects4J defect (ArrayStoreException in add).
 *
 * [Branch & Defect Analysis Matrix]
 * - toMap: null array, Map.Entry elements, Object[] elements (length<2, length>=2), non-array/non-Map element
 * - clone: null, non-null for all primitive and Object types
 * - subarray: null, start<0, end>length, start>end (newSize<=0), normal
 * - isSameLength: both null, one null with length>0, both non-null equal/unequal
 * - getLength: null, non-array (IllegalArgumentException), array
 * - isSameType: null argument (IllegalArgumentException), same type, different type
 * - reverse: null, single element, even/odd length
 * - indexOf/lastIndexOf/contains: null array, empty array, object null, object not instance of component type, startIndex bounds
 * - toPrimitive/toObject: null, empty, with/without null handling
 * - isEmpty: null, empty, non-empty
 * - addAll: null arrays, both non-null
 * - add (end): null array, non-null array, element type mismatch (defect target)
 * - add (index): null array, index out of bounds, normal insertion
 * - remove: null array (IndexOutOfBoundsException), index out of bounds, normal
 * - removeElement: element not found, found
 * - Defect-specific: add(String to Integer[]) should throw IllegalArgumentException (bug throws ArrayStoreException)
 */
public class ArrayUtilsDeepseekTest {

    // ===== Partition A: Core Functional Logic =====

    @Test(timeout = 4000)
    public void testToString() {
        assertEquals("{}", ArrayUtils.toString(null));
        assertEquals("{}", ArrayUtils.toString(new int[]{}));
        assertEquals("{1,2,3}", ArrayUtils.toString(new int[]{1,2,3}));
        assertEquals("null", ArrayUtils.toString(null, "null"));
    }

    @Test(timeout = 4000)
    public void testIsEquals() {
        assertTrue(ArrayUtils.isEquals(null, null));
        assertFalse(ArrayUtils.isEquals(new int[]{1}, null));
        assertTrue(ArrayUtils.isEquals(new int[]{1,2}, new int[]{1,2}));
        assertFalse(ArrayUtils.isEquals(new int[]{1}, new int[]{2}));
    }

    @Test(timeout = 4000)
    public void testToMap() {
        assertNull(ArrayUtils.toMap(null));
        // Map.Entry elements
        Map.Entry<String,String> entry = new java.util.AbstractMap.SimpleEntry<>("key","value");
        Map<?,?> map = ArrayUtils.toMap(new Object[]{entry});
        assertEquals(1, map.size());
        assertEquals("value", map.get("key"));
        // Object[] elements
        map = ArrayUtils.toMap(new Object[][]{{"a","1"},{"b","2"}});
        assertEquals(2, map.size());
        assertEquals("1", map.get("a"));
        // length < 2
        try {
            ArrayUtils.toMap(new Object[]{new Object[]{"only"}});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
        // non-array non-Map
        try {
            ArrayUtils.toMap(new Object[]{"string"});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testClone() {
        assertNull(ArrayUtils.clone((Object[])null));
        assertNull(ArrayUtils.clone((int[])null));
        String[] orig = {"a","b"};
        String[] cloned = ArrayUtils.clone(orig);
        assertNotSame(orig, cloned);
        assertArrayEquals(orig, cloned);
        int[] iorig = {1,2};
        int[] iclone = ArrayUtils.clone(iorig);
        assertNotSame(iorig, iclone);
        assertArrayEquals(iorig, iclone);
    }

    @Test(timeout = 4000)
    public void testSubarray() {
        assertNull(ArrayUtils.subarray((Object[])null, 0, 1));
        assertNull(ArrayUtils.subarray((int[])null, 0, 1));
        // start < 0
        assertArrayEquals(new String[]{"a","b"}, ArrayUtils.subarray(new String[]{"a","b","c"}, -1, 2));
        // end > length
        assertArrayEquals(new String[]{"b","c"}, ArrayUtils.subarray(new String[]{"a","b","c"}, 1, 10));
        // newSize <= 0
        assertArrayEquals(new String[0], ArrayUtils.subarray(new String[]{"a","b"}, 2, 1));
        // normal
        assertArrayEquals(new String[]{"b"}, ArrayUtils.subarray(new String[]{"a","b","c"}, 1, 2));
        // primitive
        assertArrayEquals(new int[]{2}, ArrayUtils.subarray(new int[]{1,2,3}, 1, 2));
    }

    @Test(timeout = 4000)
    public void testIsSameLength() {
        assertTrue(ArrayUtils.isSameLength((Object[])null, (Object[])null));
        assertTrue(ArrayUtils.isSameLength(new Object[0], new Object[0]));
        assertFalse(ArrayUtils.isSameLength(new Object[1], null));
        assertFalse(ArrayUtils.isSameLength(null, new Object[1]));
        assertTrue(ArrayUtils.isSameLength(new Object[2], new Object[2]));
        assertFalse(ArrayUtils.isSameLength(new Object[2], new Object[3]));
        // primitive variants
        assertTrue(ArrayUtils.isSameLength(new int[0], new int[0]));
        assertFalse(ArrayUtils.isSameLength(new int[1], new int[2]));
    }

    @Test(timeout = 4000)
    public void testGetLength() {
        assertEquals(0, ArrayUtils.getLength(null));
        assertEquals(0, ArrayUtils.getLength(new Object[0]));
        assertEquals(3, ArrayUtils.getLength(new int[]{1,2,3}));
        try {
            ArrayUtils.getLength("not an array");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIsSameType() {
        try {
            ArrayUtils.isSameType(null, new int[0]);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) { }
        try {
            ArrayUtils.isSameType(new int[0], null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) { }
        assertTrue(ArrayUtils.isSameType(new int[0], new int[5]));
        assertFalse(ArrayUtils.isSameType(new int[0], new long[0]));
    }

    @Test(timeout = 4000)
    public void testReverse() {
        // null does nothing
        ArrayUtils.reverse((Object[])null);
        // single element
        String[] single = {"a"};
        ArrayUtils.reverse(single);
        assertArrayEquals(new String[]{"a"}, single);
        // even length
        Integer[] even = {1,2,3,4};
        ArrayUtils.reverse(even);
        assertArrayEquals(new Integer[]{4,3,2,1}, even);
        // odd length
        int[] odd = {1,2,3};
        ArrayUtils.reverse(odd);
        assertArrayEquals(new int[]{3,2,1}, odd);
    }

    // ===== Partition B: Boundary Value Analysis =====

    @Test(timeout = 4000)
    public void testIndexOfObject() {
        assertEquals(-1, ArrayUtils.indexOf((Object[])null, "a"));
        assertEquals(-1, ArrayUtils.indexOf(new Object[0], "a"));
        // null object
        assertEquals(0, ArrayUtils.indexOf(new Object[]{null, "a"}, null));
        // object not instance of component type
        assertEquals(-1, ArrayUtils.indexOf(new Integer[]{1,2}, "a"));
        // normal
        assertEquals(1, ArrayUtils.indexOf(new String[]{"a","b","c"}, "b"));
        // startIndex
        assertEquals(-1, ArrayUtils.indexOf(new String[]{"a","b"}, "a", 1));
        assertEquals(0, ArrayUtils.indexOf(new String[]{"a","b"}, "a", -1));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfObject() {
        assertEquals(-1, ArrayUtils.lastIndexOf((Object[])null, "a"));
        assertEquals(-1, ArrayUtils.lastIndexOf(new Object[0], "a"));
        // null object
        assertEquals(2, ArrayUtils.lastIndexOf(new Object[]{"a",null,"b",null}, null));
        // startIndex negative
        assertEquals(-1, ArrayUtils.lastIndexOf(new String[]{"a"}, "a", -1));
        // startIndex >= length
        assertEquals(1, ArrayUtils.lastIndexOf(new String[]{"a","b"}, "b", 10));
        // normal
        assertEquals(1, ArrayUtils.lastIndexOf(new String[]{"a","b","a"}, "a"));
    }

    @Test(timeout = 4000)
    public void testContainsObject() {
        assertFalse(ArrayUtils.contains((Object[])null, "a"));
        assertFalse(ArrayUtils.contains(new Object[0], "a"));
        assertTrue(ArrayUtils.contains(new String[]{"a","b"}, "a"));
        assertFalse(ArrayUtils.contains(new String[]{"a","b"}, "c"));
    }

    @Test(timeout = 4000)
    public void testIndexOfPrimitive() {
        assertEquals(-1, ArrayUtils.indexOf((int[])null, 1));
        assertEquals(-1, ArrayUtils.indexOf(new int[0], 1));
        assertEquals(1, ArrayUtils.indexOf(new int[]{1,2,3}, 2));
        assertEquals(-1, ArrayUtils.indexOf(new int[]{1,2}, 3));
        // startIndex
        assertEquals(-1, ArrayUtils.indexOf(new int[]{1,2,1}, 1, 2));
        assertEquals(0, ArrayUtils.indexOf(new int[]{1,2}, 1, -1));
    }

    @Test(timeout = 4000)
    public void testLastIndexOfPrimitive() {
        assertEquals(-1, ArrayUtils.lastIndexOf((int[])null, 1));
        assertEquals(-1, ArrayUtils.lastIndexOf(new int[0], 1));
        assertEquals(2, ArrayUtils.lastIndexOf(new int[]{1,2,1}, 1));
        assertEquals(-1, ArrayUtils.lastIndexOf(new int[]{1,2}, 3));
        // startIndex negative
        assertEquals(-1, ArrayUtils.lastIndexOf(new int[]{1}, 1, -1));
        // startIndex >= length
        assertEquals(1, ArrayUtils.lastIndexOf(new int[]{1,2}, 2, 10));
    }

    @Test(timeout = 4000)
    public void testContainsPrimitive() {
        assertFalse(ArrayUtils.contains((int[])null, 1));
        assertFalse(ArrayUtils.contains(new int[0], 1));
        assertTrue(ArrayUtils.contains(new int[]{1,2}, 1));
        assertFalse(ArrayUtils.contains(new int[]{1,2}, 3));
    }

    @Test(timeout = 4000)
    public void testIndexOfDoubleWithTolerance() {
        assertEquals(-1, ArrayUtils.indexOf((double[])null, 1.0, 0.1));
        assertEquals(-1, ArrayUtils.indexOf(new double[0], 1.0, 0.1));
        assertEquals(0, ArrayUtils.indexOf(new double[]{1.0, 2.0}, 1.05, 0.1));
        assertEquals(-1, ArrayUtils.indexOf(new double[]{1.0, 2.0}, 1.2, 0.1));
    }

    @Test(timeout = 4000)
    public void testToPrimitiveObject() {
        assertNull(ArrayUtils.toPrimitive((Character[])null));
        assertArrayEquals(new char[0], ArrayUtils.toPrimitive(new Character[0]));
        assertArrayEquals(new char[]{'a','b'}, ArrayUtils.toPrimitive(new Character[]{'a','b'}));
        // with null handling
        assertArrayEquals(new char[]{'a',' '}, ArrayUtils.toPrimitive(new Character[]{'a', null}, ' '));
    }

    @Test(timeout = 4000)
    public void testToObjectPrimitive() {
        assertNull(ArrayUtils.toObject((char[])null));
        assertArrayEquals(new Character[0], ArrayUtils.toObject(new char[0]));
        assertArrayEquals(new Character[]{'a','b'}, ArrayUtils.toObject(new char[]{'a','b'}));
    }

    @Test(timeout = 4000)
    public void testIsEmpty() {
        assertTrue(ArrayUtils.isEmpty((Object[])null));
        assertTrue(ArrayUtils.isEmpty(new Object[0]));
        assertFalse(ArrayUtils.isEmpty(new Object[]{1}));
        assertTrue(ArrayUtils.isEmpty((int[])null));
        assertTrue(ArrayUtils.isEmpty(new int[0]));
        assertFalse(ArrayUtils.isEmpty(new int[]{1}));
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    @Test(timeout = 4000)
    public void testAddAll() {
        assertNull(ArrayUtils.addAll((Object[])null, (Object[])null));
        assertArrayEquals(new String[]{"a"}, ArrayUtils.addAll((String[])null, new String[]{"a"}));
        assertArrayEquals(new String[]{"a"}, ArrayUtils.addAll(new String[]{"a"}, (String[])null));
        assertArrayEquals(new String[]{"a","b"}, ArrayUtils.addAll(new String[]{"a"}, new String[]{"b"}));
        // primitive
        assertArrayEquals(new int[]{1,2}, ArrayUtils.addAll(new int[]{1}, new int[]{2}));
    }

    @Test(timeout = 4000)
    public void testAddEnd() {
        // null array, non-null element
        assertArrayEquals(new String[]{"a"}, ArrayUtils.add((String[])null, "a"));
        // null array, null element
        assertArrayEquals(new Object[]{null}, ArrayUtils.add((Object[])null, null));
        // non-null array
        assertArrayEquals(new String[]{"a","b"}, ArrayUtils.add(new String[]{"a"}, "b"));
        // primitive
        assertArrayEquals(new int[]{1,2}, ArrayUtils.add(new int[]{1}, 2));
    }

    @Test(timeout = 4000)
    public void testAddAtIndex() {
        // null array, index 0
        assertArrayEquals(new String[]{"a"}, ArrayUtils.add((String[])null, 0, "a"));
        // null array, index != 0
        try {
            ArrayUtils.add((String[])null, 1, "a");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) { }
        // index out of bounds
        try {
            ArrayUtils.add(new String[]{"a"}, 2, "b");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) { }
        // normal insertion
        assertArrayEquals(new String[]{"a","c","b"}, ArrayUtils.add(new String[]{"a","b"}, 1, "c"));
        // primitive
        assertArrayEquals(new int[]{1,3,2}, ArrayUtils.add(new int[]{1,2}, 1, 3));
    }

    @Test(timeout = 4000)
    public void testRemove() {
        try {
            ArrayUtils.remove((Object[])null, 0);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) { }
        try {
            ArrayUtils.remove(new String[]{"a"}, -1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) { }
        try {
            ArrayUtils.remove(new String[]{"a"}, 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) { }
        assertArrayEquals(new String[0], ArrayUtils.remove(new String[]{"a"}, 0));
        assertArrayEquals(new String[]{"a","c"}, ArrayUtils.remove(new String[]{"a","b","c"}, 1));
        // primitive
        assertArrayEquals(new int[]{1,3}, ArrayUtils.remove(new int[]{1,2,3}, 1));
    }

    @Test(timeout = 4000)
    public void testRemoveElement() {
        assertNull(ArrayUtils.removeElement((Object[])null, "a"));
        assertArrayEquals(new String[0], ArrayUtils.removeElement(new String[0], "a"));
        assertArrayEquals(new String[]{"a"}, ArrayUtils.removeElement(new String[]{"a"}, "b"));
        assertArrayEquals(new String[]{"b"}, ArrayUtils.removeElement(new String[]{"a","b"}, "a"));
        assertArrayEquals(new String[]{"b","a"}, ArrayUtils.removeElement(new String[]{"a","b","a"}, "a"));
        // primitive
        assertArrayEquals(new int[]{2,3}, ArrayUtils.removeElement(new int[]{1,2,3}, 1));
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToMapWithNonArrayNonEntry() {
        ArrayUtils.toMap(new Object[]{"string"});
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToMapWithShortArray() {
        ArrayUtils.toMap(new Object[]{new Object[]{"only"}});
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testIsSameTypeWithNull() {
        ArrayUtils.isSameType(null, new int[0]);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetLengthWithNonArray() {
        ArrayUtils.getLength("not array");
    }

    // ===== Partition E: Defect-Specific Test (Jira567) =====

    /**
     * Targets the known Defects4J defect: ArrayStoreException when adding an element
     * of incompatible type to an array. The correct behavior is to throw IllegalArgumentException.
     * On the defective version, ArrayStoreException is thrown instead.
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddIncompatibleType() {
        // Attempt to add a String to an Integer[] - should throw IllegalArgumentException
        Integer[] array = new Integer[]{1, 2};
        ArrayUtils.add(array, "string");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddAtIndexIncompatibleType() {
        Integer[] array = new Integer[]{1, 2};
        ArrayUtils.add(array, 1, "string");
    }
}