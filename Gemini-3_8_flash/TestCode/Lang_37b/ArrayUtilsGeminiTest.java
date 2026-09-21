package org.apache.commons.lang3;

import org.junit.Test;

import java.util.AbstractMap;
import java.util.Map;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: org.apache.commons.lang3.ArrayUtils
 * Known Defect (LANG-567):
 *   ArrayUtils.addAll(T[], T...) uses array1.getClass().getComponentType() to instantiate the combined
 *   array. When array1 is a subtype array (e.g., Integer[]) and array2 elements are of another subtype
 *   (e.g., Long[]), Java generics infer the common supertype T (e.g., Number), but runtime array creation
 *   creates Integer[], causing System.arraycopy to fail with java.lang.ArrayStoreException.
 *
 * Systematic Branch Coverage Plan:
 * - Partition A: Core Functional Logic & Conversions
 *   * Primitive <-> Object converters (Character, Long, Integer, Short, Byte, Double, Float, Boolean)
 *   * toMap() with Map.Entry, 2-element Object[], and invalid lengths/types
 *   * reverse(), clone(), toString(), isEquals() across primitives and Object[]
 * - Partition B: Boundary Value Analysis (BVA) & Extremes
 *   * null arrays, empty arrays, length 1 arrays
 *   * subarray() with underflow (< 0), overflow (> length), inverted indices (start > end)
 *   * isSameLength() truth table with nulls, empties, mismatched lengths
 *   * getLength() with null, primitive array, Object[], and non-array object
 * - Partition C: Defect-Targeted Branch Zone
 *   * testJira567: ArrayUtils.addAll with Integer[] and Long[] typed as Number[] triggering ArrayStoreException
 * - Partition D: Exception & Defensive Guard Paths
 *   * add(array, index, element) out-of-bounds guards (index < 0, index > length, null array index != 0)
 *   * remove(array, index) out-of-bounds guards (index < 0, index >= length, null array)
 *   * toPrimitive() null element handling throwing NPE vs default value fallback
 *   * toMap() throwing IllegalArgumentException for non-entry/array elements or arrays with length < 2
 *   * isSameType() null arguments throwing IllegalArgumentException
 * - Partition E: Object Lifecycle & Contract Integrity
 *   * Public constructor instantiation (JavaBean support)
 *   * Static empty arrays identity and length verification
 * ====================================================================================================
 */
public class ArrayUtilsGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (LANG-567 / Jira 567)
    // =========================================================================

    /**
     * Targets LANG-567: ArrayUtils.addAll(T[] array1, T... array2) fails with ArrayStoreException
     * when array1 is Integer[] and array2 is Long[], inferred as T = Number.
     */
    @Test(timeout = 4000)
    public void testJira567() {
        Number[] n = ArrayUtils.addAll(new Integer[]{Integer.valueOf(1)}, new Long[]{Long.valueOf(2)});
        assertNotNull("Resulting array should not be null", n);
        assertEquals("Length should be 2", 2, n.length);
        assertEquals("First element should be Integer(1)", Integer.valueOf(1), n[0]);
        assertEquals("Second element should be Long(2)", Long.valueOf(2), n[1]);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testToMap_ValidEntriesAndArrays() {
        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<String, String>("k1", "v1");
        Object[] array = new Object[]{
                entry,
                new Object[]{"k2", "v2"},
                new Object[]{"k3", "v3", "extraIgnored"}
        };
        Map<Object, Object> map = ArrayUtils.toMap(array);
        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
        assertEquals("v3", map.get("k3"));
    }

    @Test(timeout = 4000)
    public void testToStringAndIsEquals() {
        String[] strArray = new String[]{"a", "b"};
        assertEquals("{a,b}", ArrayUtils.toString(strArray));
        assertEquals("{}", ArrayUtils.toString(null));
        assertEquals("default", ArrayUtils.toString(null, "default"));
        assertEquals("{a,b}", ArrayUtils.toString(strArray, "default"));

        assertTrue(ArrayUtils.isEquals(new int[]{1, 2}, new int[]{1, 2}));
        assertFalse(ArrayUtils.isEquals(new int[]{1, 2}, new int[]{1, 3}));
        assertTrue(ArrayUtils.isEquals(null, null));
        assertFalse(ArrayUtils.isEquals(new int[]{1}, null));
    }

    @Test(timeout = 4000)
    public void testClone_AllTypes() {
        assertNull(ArrayUtils.clone((Object[]) null));
        assertNull(ArrayUtils.clone((long[]) null));
        assertNull(ArrayUtils.clone((int[]) null));
        assertNull(ArrayUtils.clone((short[]) null));
        assertNull(ArrayUtils.clone((char[]) null));
        assertNull(ArrayUtils.clone((byte[]) null));
        assertNull(ArrayUtils.clone((double[]) null));
        assertNull(ArrayUtils.clone((float[]) null));
        assertNull(ArrayUtils.clone((boolean[]) null));

        assertArrayEquals(new String[]{"a"}, ArrayUtils.clone(new String[]{"a"}));
        assertArrayEquals(new long[]{1L}, ArrayUtils.clone(new long[]{1L}));
        assertArrayEquals(new int[]{1}, ArrayUtils.clone(new int[]{1}));
        assertArrayEquals(new short[]{1}, ArrayUtils.clone(new short[]{1}));
        assertArrayEquals(new char[]{'a'}, ArrayUtils.clone(new char[]{'a'}));
        assertArrayEquals(new byte[]{1}, ArrayUtils.clone(new byte[]{1}));
        assertArrayEquals(new double[]{1.0}, ArrayUtils.clone(new double[]{1.0}), 0.0);
        assertArrayEquals(new float[]{1.0f}, ArrayUtils.clone(new float[]{1.0f}), 0.0f);
        assertTrue(ArrayUtils.clone(new boolean[]{true})[0]);
    }

    @Test(timeout = 4000)
    public void testReverse_AllTypes() {
        ArrayUtils.reverse((Object[]) null);
        String[] str = new String[]{"a", "b", "c"};
        ArrayUtils.reverse(str);
        assertArrayEquals(new String[]{"c", "b", "a"}, str);

        ArrayUtils.reverse((long[]) null);
        long[] l = new long[]{1L, 2L};
        ArrayUtils.reverse(l);
        assertArrayEquals(new long[]{2L, 1L}, l);

        ArrayUtils.reverse((int[]) null);
        int[] i = new int[]{1, 2, 3};
        ArrayUtils.reverse(i);
        assertArrayEquals(new int[]{3, 2, 1}, i);

        ArrayUtils.reverse((short[]) null);
        short[] s = new short[]{1, 2};
        ArrayUtils.reverse(s);
        assertArrayEquals(new short[]{2, 1}, s);

        ArrayUtils.reverse((char[]) null);
        char[] c = new char[]{'a', 'b', 'c'};
        ArrayUtils.reverse(c);
        assertArrayEquals(new char[]{'c', 'b', 'a'}, c);

        ArrayUtils.reverse((byte[]) null);
        byte[] b = new byte[]{1, 2};
        ArrayUtils.reverse(b);
        assertArrayEquals(new byte[]{2, 1}, b);

        ArrayUtils.reverse((double[]) null);
        double[] d = new double[]{1.0, 2.0, 3.0};
        ArrayUtils.reverse(d);
        assertArrayEquals(new double[]{3.0, 2.0, 1.0}, d, 0.0);

        ArrayUtils.reverse((float[]) null);
        float[] f = new float[]{1.0f, 2.0f};
        ArrayUtils.reverse(f);
        assertArrayEquals(new float[]{2.0f, 1.0f}, f, 0.0f);

        ArrayUtils.reverse((boolean[]) null);
        boolean[] bool = new boolean[]{true, false};
        ArrayUtils.reverse(bool);
        assertFalse(bool[0]);
        assertTrue(bool[1]);
    }

    @Test(timeout = 4000)
    public void testConverters_ToPrimitiveAndToObject() {
        // Character
        assertNull(ArrayUtils.toPrimitive((Character[]) null));
        assertArrayEquals(new char[0], ArrayUtils.toPrimitive(new Character[0]));
        assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.toPrimitive(new Character[]{Character.valueOf('a'), Character.valueOf('b')}));
        assertArrayEquals(new char[]{'a', 'z'}, ArrayUtils.toPrimitive(new Character[]{Character.valueOf('a'), null}, 'z'));
        assertNull(ArrayUtils.toPrimitive((Character[]) null, 'z'));
        assertArrayEquals(new char[0], ArrayUtils.toPrimitive(new Character[0], 'z'));
        assertNull(ArrayUtils.toObject((char[]) null));
        assertArrayEquals(new Character[0], ArrayUtils.toObject(new char[0]));
        assertArrayEquals(new Character[]{Character.valueOf('x')}, ArrayUtils.toObject(new char[]{'x'}));

        // Long
        assertNull(ArrayUtils.toPrimitive((Long[]) null));
        assertArrayEquals(new long[0], ArrayUtils.toPrimitive(new Long[0]));
        assertArrayEquals(new long[]{10L}, ArrayUtils.toPrimitive(new Long[]{Long.valueOf(10L)}));
        assertArrayEquals(new long[]{10L, 99L}, ArrayUtils.toPrimitive(new Long[]{Long.valueOf(10L), null}, 99L));
        assertNull(ArrayUtils.toPrimitive((Long[]) null, 99L));
        assertArrayEquals(new long[0], ArrayUtils.toPrimitive(new Long[0], 99L));
        assertNull(ArrayUtils.toObject((long[]) null));
        assertArrayEquals(new Long[0], ArrayUtils.toObject(new long[0]));
        assertArrayEquals(new Long[]{Long.valueOf(5L)}, ArrayUtils.toObject(new long[]{5L}));

        // Integer
        assertNull(ArrayUtils.toPrimitive((Integer[]) null));
        assertArrayEquals(new int[0], ArrayUtils.toPrimitive(new Integer[0]));
        assertArrayEquals(new int[]{1}, ArrayUtils.toPrimitive(new Integer[]{Integer.valueOf(1)}));
        assertArrayEquals(new int[]{1, -1}, ArrayUtils.toPrimitive(new Integer[]{Integer.valueOf(1), null}, -1));
        assertNull(ArrayUtils.toPrimitive((Integer[]) null, -1));
        assertArrayEquals(new int[0], ArrayUtils.toPrimitive(new Integer[0], -1));
        assertNull(ArrayUtils.toObject((int[]) null));
        assertArrayEquals(new Integer[0], ArrayUtils.toObject(new int[0]));
        assertArrayEquals(new Integer[]{Integer.valueOf(7)}, ArrayUtils.toObject(new int[]{7}));

        // Short
        assertNull(ArrayUtils.toPrimitive((Short[]) null));
        assertArrayEquals(new short[0], ArrayUtils.toPrimitive(new Short[0]));
        assertArrayEquals(new short[]{2}, ArrayUtils.toPrimitive(new Short[]{Short.valueOf((short) 2)}));
        assertArrayEquals(new short[]{2, 0}, ArrayUtils.toPrimitive(new Short[]{Short.valueOf((short) 2), null}, (short) 0));
        assertNull(ArrayUtils.toPrimitive((Short[]) null, (short) 0));
        assertArrayEquals(new short[0], ArrayUtils.toPrimitive(new Short[0], (short) 0));
        assertNull(ArrayUtils.toObject((short[]) null));
        assertArrayEquals(new Short[0], ArrayUtils.toObject(new short[0]));
        assertArrayEquals(new Short[]{Short.valueOf((short) 3)}, ArrayUtils.toObject(new short[]{3}));

        // Byte
        assertNull(ArrayUtils.toPrimitive((Byte[]) null));
        assertArrayEquals(new byte[0], ArrayUtils.toPrimitive(new Byte[0]));
        assertArrayEquals(new byte[]{4}, ArrayUtils.toPrimitive(new Byte[]{Byte.valueOf((byte) 4)}));
        assertArrayEquals(new byte[]{4, 8}, ArrayUtils.toPrimitive(new Byte[]{Byte.valueOf((byte) 4), null}, (byte) 8));
        assertNull(ArrayUtils.toPrimitive((Byte[]) null, (byte) 8));
        assertArrayEquals(new byte[0], ArrayUtils.toPrimitive(new Byte[0], (byte) 8));
        assertNull(ArrayUtils.toObject((byte[]) null));
        assertArrayEquals(new Byte[0], ArrayUtils.toObject(new byte[0]));
        assertArrayEquals(new Byte[]{Byte.valueOf((byte) 6)}, ArrayUtils.toObject(new byte[]{6}));

        // Double
        assertNull(ArrayUtils.toPrimitive((Double[]) null));
        assertArrayEquals(new double[0], ArrayUtils.toPrimitive(new Double[0]), 0.0);
        assertArrayEquals(new double[]{1.5}, ArrayUtils.toPrimitive(new Double[]{Double.valueOf(1.5)}), 0.0);
        assertArrayEquals(new double[]{1.5, 0.5}, ArrayUtils.toPrimitive(new Double[]{Double.valueOf(1.5), null}, 0.5), 0.0);
        assertNull(ArrayUtils.toPrimitive((Double[]) null, 0.5));
        assertArrayEquals(new double[0], ArrayUtils.toPrimitive(new Double[0], 0.5), 0.0);
        assertNull(ArrayUtils.toObject((double[]) null));
        assertArrayEquals(new Double[0], ArrayUtils.toObject(new double[0]));
        assertArrayEquals(new Double[]{Double.valueOf(2.5)}, ArrayUtils.toObject(new double[]{2.5}));

        // Float
        assertNull(ArrayUtils.toPrimitive((Float[]) null));
        assertArrayEquals(new float[0], ArrayUtils.toPrimitive(new Float[0]), 0.0f);
        assertArrayEquals(new float[]{1.2f}, ArrayUtils.toPrimitive(new Float[]{Float.valueOf(1.2f)}), 0.0f);
        assertArrayEquals(new float[]{1.2f, 9.9f}, ArrayUtils.toPrimitive(new Float[]{Float.valueOf(1.2f), null}, 9.9f), 0.0f);
        assertNull(ArrayUtils.toPrimitive((Float[]) null, 9.9f));
        assertArrayEquals(new float[0], ArrayUtils.toPrimitive(new Float[0], 9.9f), 0.0f);
        assertNull(ArrayUtils.toObject((float[]) null));
        assertArrayEquals(new Float[0], ArrayUtils.toObject(new float[0]));
        assertArrayEquals(new Float[]{Float.valueOf(3.4f)}, ArrayUtils.toObject(new float[]{3.4f}));

        // Boolean
        assertNull(ArrayUtils.toPrimitive((Boolean[]) null));
        assertArrayEquals(new boolean[0], ArrayUtils.toPrimitive(new Boolean[0]));
        assertTrue(ArrayUtils.toPrimitive(new Boolean[]{Boolean.TRUE})[0]);
        assertArrayEquals(new boolean[]{true, false}, ArrayUtils.toPrimitive(new Boolean[]{Boolean.TRUE, null}, false));
        assertNull(ArrayUtils.toPrimitive((Boolean[]) null, false));
        assertArrayEquals(new boolean[0], ArrayUtils.toPrimitive(new Boolean[0], false));
        assertNull(ArrayUtils.toObject((boolean[]) null));
        assertArrayEquals(new Boolean[0], ArrayUtils.toObject(new boolean[0]));
        assertArrayEquals(new Boolean[]{Boolean.TRUE, Boolean.FALSE}, ArrayUtils.toObject(new boolean[]{true, false}));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsEmpty_AllTypes() {
        assertTrue(ArrayUtils.isEmpty((Object[]) null));
        assertTrue(ArrayUtils.isEmpty(new Object[0]));
        assertFalse(ArrayUtils.isEmpty(new Object[]{new Object()}));

        assertTrue(ArrayUtils.isEmpty((long[]) null));
        assertTrue(ArrayUtils.isEmpty(new long[0]));
        assertFalse(ArrayUtils.isEmpty(new long[]{1L}));

        assertTrue(ArrayUtils.isEmpty((int[]) null));
        assertTrue(ArrayUtils.isEmpty(new int[0]));
        assertFalse(ArrayUtils.isEmpty(new int[]{1}));

        assertTrue(ArrayUtils.isEmpty((short[]) null));
        assertTrue(ArrayUtils.isEmpty(new short[0]));
        assertFalse(ArrayUtils.isEmpty(new short[]{1}));

        assertTrue(ArrayUtils.isEmpty((char[]) null));
        assertTrue(ArrayUtils.isEmpty(new char[0]));
        assertFalse(ArrayUtils.isEmpty(new char[]{'a'}));

        assertTrue(ArrayUtils.isEmpty((byte[]) null));
        assertTrue(ArrayUtils.isEmpty(new byte[0]));
        assertFalse(ArrayUtils.isEmpty(new byte[]{1}));

        assertTrue(ArrayUtils.isEmpty((double[]) null));
        assertTrue(ArrayUtils.isEmpty(new double[0]));
        assertFalse(ArrayUtils.isEmpty(new double[]{1.0}));

        assertTrue(ArrayUtils.isEmpty((float[]) null));
        assertTrue(ArrayUtils.isEmpty(new float[0]));
        assertFalse(ArrayUtils.isEmpty(new float[]{1.0f}));

        assertTrue(ArrayUtils.isEmpty((boolean[]) null));
        assertTrue(ArrayUtils.isEmpty(new boolean[0]));
        assertFalse(ArrayUtils.isEmpty(new boolean[]{true}));
    }

    @Test(timeout = 4000)
    public void testSubarray_Boundaries() {
        String[] s = new String[]{"a", "b", "c"};
        assertNull(ArrayUtils.subarray((String[]) null, 0, 1));
        assertArrayEquals(new String[0], ArrayUtils.subarray(s, 2, 1)); // start > end
        assertArrayEquals(new String[0], ArrayUtils.subarray(s, 5, 6)); // start > length
        assertArrayEquals(new String[]{"a", "b"}, ArrayUtils.subarray(s, -2, 2)); // start < 0
        assertArrayEquals(new String[]{"b", "c"}, ArrayUtils.subarray(s, 1, 10)); // end > length

        assertNull(ArrayUtils.subarray((long[]) null, 0, 1));
        assertArrayEquals(new long[0], ArrayUtils.subarray(new long[]{1L, 2L}, 1, 0));
        assertArrayEquals(new long[]{1L}, ArrayUtils.subarray(new long[]{1L, 2L}, -1, 1));
        assertArrayEquals(new long[]{2L}, ArrayUtils.subarray(new long[]{1L, 2L}, 1, 5));

        assertNull(ArrayUtils.subarray((int[]) null, 0, 1));
        assertArrayEquals(new int[0], ArrayUtils.subarray(new int[]{1, 2}, 1, 0));
        assertArrayEquals(new int[]{1}, ArrayUtils.subarray(new int[]{1, 2}, -1, 1));
        assertArrayEquals(new int[]{2}, ArrayUtils.subarray(new int[]{1, 2}, 1, 5));

        assertNull(ArrayUtils.subarray((short[]) null, 0, 1));
        assertArrayEquals(new short[0], ArrayUtils.subarray(new short[]{1, 2}, 1, 0));
        assertArrayEquals(new short[]{1}, ArrayUtils.subarray(new short[]{1, 2}, -1, 1));
        assertArrayEquals(new short[]{2}, ArrayUtils.subarray(new short[]{1, 2}, 1, 5));

        assertNull(ArrayUtils.subarray((char[]) null, 0, 1));
        assertArrayEquals(new char[0], ArrayUtils.subarray(new char[]{'a', 'b'}, 1, 0));
        assertArrayEquals(new char[]{'a'}, ArrayUtils.subarray(new char[]{'a', 'b'}, -1, 1));
        assertArrayEquals(new char[]{'b'}, ArrayUtils.subarray(new char[]{'a', 'b'}, 1, 5));

        assertNull(ArrayUtils.subarray((byte[]) null, 0, 1));
        assertArrayEquals(new byte[0], ArrayUtils.subarray(new byte[]{1, 2}, 1, 0));
        assertArrayEquals(new byte[]{1}, ArrayUtils.subarray(new byte[]{1, 2}, -1, 1));
        assertArrayEquals(new byte[]{2}, ArrayUtils.subarray(new byte[]{1, 2}, 1, 5));

        assertNull(ArrayUtils.subarray((double[]) null, 0, 1));
        assertArrayEquals(new double[0], ArrayUtils.subarray(new double[]{1.0, 2.0}, 1, 0), 0.0);
        assertArrayEquals(new double[]{1.0}, ArrayUtils.subarray(new double[]{1.0, 2.0}, -1, 1), 0.0);
        assertArrayEquals(new double[]{2.0}, ArrayUtils.subarray(new double[]{1.0, 2.0}, 1, 5), 0.0);

        assertNull(ArrayUtils.subarray((float[]) null, 0, 1));
        assertArrayEquals(new float[0], ArrayUtils.subarray(new float[]{1.0f, 2.0f}, 1, 0), 0.0f);
        assertArrayEquals(new float[]{1.0f}, ArrayUtils.subarray(new float[]{1.0f, 2.0f}, -1, 1), 0.0f);
        assertArrayEquals(new float[]{2.0f}, ArrayUtils.subarray(new float[]{1.0f, 2.0f}, 1, 5), 0.0f);

        assertNull(ArrayUtils.subarray((boolean[]) null, 0, 1));
        assertArrayEquals(new boolean[0], ArrayUtils.subarray(new boolean[]{true, false}, 1, 0));
        assertArrayEquals(new boolean[]{true}, ArrayUtils.subarray(new boolean[]{true, false}, -1, 1));
        assertArrayEquals(new boolean[]{false}, ArrayUtils.subarray(new boolean[]{true, false}, 1, 5));
    }

    @Test(timeout = 4000)
    public void testIsSameLength_AllTypes() {
        assertTrue(ArrayUtils.isSameLength((Object[]) null, (Object[]) null));
        assertTrue(ArrayUtils.isSameLength(new Object[0], (Object[]) null));
        assertTrue(ArrayUtils.isSameLength((Object[]) null, new Object[0]));
        assertFalse(ArrayUtils.isSameLength(new Object[1], (Object[]) null));
        assertFalse(ArrayUtils.isSameLength((Object[]) null, new Object[1]));
        assertTrue(ArrayUtils.isSameLength(new Object[2], new Object[2]));
        assertFalse(ArrayUtils.isSameLength(new Object[2], new Object[3]));

        assertTrue(ArrayUtils.isSameLength((long[]) null, (long[]) null));
        assertFalse(ArrayUtils.isSameLength(new long[1], (long[]) null));
        assertFalse(ArrayUtils.isSameLength((long[]) null, new long[1]));
        assertFalse(ArrayUtils.isSameLength(new long[1], new long[2]));
        assertTrue(ArrayUtils.isSameLength(new long[1], new long[1]));

        assertTrue(ArrayUtils.isSameLength((int[]) null, (int[]) null));
        assertFalse(ArrayUtils.isSameLength(new int[1], (int[]) null));
        assertFalse(ArrayUtils.isSameLength((int[]) null, new int[1]));
        assertFalse(ArrayUtils.isSameLength(new int[1], new int[2]));
        assertTrue(ArrayUtils.isSameLength(new int[1], new int[1]));

        assertTrue(ArrayUtils.isSameLength((short[]) null, (short[]) null));
        assertFalse(ArrayUtils.isSameLength(new short[1], (short[]) null));
        assertFalse(ArrayUtils.isSameLength((short[]) null, new short[1]));
        assertFalse(ArrayUtils.isSameLength(new short[1], new short[2]));
        assertTrue(ArrayUtils.isSameLength(new short[1], new short[1]));

        assertTrue(ArrayUtils.isSameLength((char[]) null, (char[]) null));
        assertFalse(ArrayUtils.isSameLength(new char[1], (char[]) null));
        assertFalse(ArrayUtils.isSameLength((char[]) null, new char[1]));
        assertFalse(ArrayUtils.isSameLength(new char[1], new char[2]));
        assertTrue(ArrayUtils.isSameLength(new char[1], new char[1]));

        assertTrue(ArrayUtils.isSameLength((byte[]) null, (byte[]) null));
        assertFalse(ArrayUtils.isSameLength(new byte[1], (byte[]) null));
        assertFalse(ArrayUtils.isSameLength((byte[]) null, new byte[1]));
        assertFalse(ArrayUtils.isSameLength(new byte[1], new byte[2]));
        assertTrue(ArrayUtils.isSameLength(new byte[1], new byte[1]));

        assertTrue(ArrayUtils.isSameLength((double[]) null, (double[]) null));
        assertFalse(ArrayUtils.isSameLength(new double[1], (double[]) null));
        assertFalse(ArrayUtils.isSameLength((double[]) null, new double[1]));
        assertFalse(ArrayUtils.isSameLength(new double[1], new double[2]));
        assertTrue(ArrayUtils.isSameLength(new double[1], new double[1]));

        assertTrue(ArrayUtils.isSameLength((float[]) null, (float[]) null));
        assertFalse(ArrayUtils.isSameLength(new float[1], (float[]) null));
        assertFalse(ArrayUtils.isSameLength((float[]) null, new float[1]));
        assertFalse(ArrayUtils.isSameLength(new float[1], new float[2]));
        assertTrue(ArrayUtils.isSameLength(new float[1], new float[1]));

        assertTrue(ArrayUtils.isSameLength((boolean[]) null, (boolean[]) null));
        assertFalse(ArrayUtils.isSameLength(new boolean[1], (boolean[]) null));
        assertFalse(ArrayUtils.isSameLength((boolean[]) null, new boolean[1]));
        assertFalse(ArrayUtils.isSameLength(new boolean[1], new boolean[2]));
        assertTrue(ArrayUtils.isSameLength(new boolean[1], new boolean[1]));
    }

    @Test(timeout = 4000)
    public void testGetLength_AllCases() {
        assertEquals(0, ArrayUtils.getLength(null));
        assertEquals(0, ArrayUtils.getLength(new int[0]));
        assertEquals(3, ArrayUtils.getLength(new int[]{1, 2, 3}));
        assertEquals(2, ArrayUtils.getLength(new String[]{"a", "b"}));
    }

    @Test(timeout = 4000)
    public void testIndexOfAndContains_Object() {
        String[] array = new String[]{"a", null, "b", "a"};
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(null, "a"));
        assertEquals(0, ArrayUtils.indexOf(array, "a"));
        assertEquals(3, ArrayUtils.indexOf(array, "a", 1));
        assertEquals(0, ArrayUtils.indexOf(array, "a", -5));
        assertEquals(1, ArrayUtils.indexOf(array, null));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(array, "not_in_array"));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(array, Integer.valueOf(1))); // Incompatible type

        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(null, "a"));
        assertEquals(3, ArrayUtils.lastIndexOf(array, "a"));
        assertEquals(0, ArrayUtils.lastIndexOf(array, "a", 2));
        assertEquals(1, ArrayUtils.lastIndexOf(array, null));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(array, "a", -1));
        assertEquals(3, ArrayUtils.lastIndexOf(array, "a", 10));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(array, Integer.valueOf(1)));

        assertTrue(ArrayUtils.contains(array, "a"));
        assertTrue(ArrayUtils.contains(array, null));
        assertFalse(ArrayUtils.contains(array, "z"));
        assertFalse(ArrayUtils.contains(null, "a"));
    }

    @Test(timeout = 4000)
    public void testIndexOfAndContains_Primitives() {
        // long
        long[] l = new long[]{10L, 20L, 10L};
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((long[]) null, 10L));
        assertEquals(0, ArrayUtils.indexOf(l, 10L));
        assertEquals(2, ArrayUtils.indexOf(l, 10L, 1));
        assertEquals(0, ArrayUtils.indexOf(l, 10L, -1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(l, 99L));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf((long[]) null, 10L));
        assertEquals(2, ArrayUtils.lastIndexOf(l, 10L));
        assertEquals(0, ArrayUtils.lastIndexOf(l, 10L, 1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(l, 10L, -1));
        assertEquals(2, ArrayUtils.lastIndexOf(l, 10L, 5));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(l, 99L));
        assertTrue(ArrayUtils.contains(l, 10L));
        assertFalse(ArrayUtils.contains(l, 99L));
        assertFalse(ArrayUtils.contains((long[]) null, 10L));

        // int
        int[] i = new int[]{1, 2, 1};
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((int[]) null, 1));
        assertEquals(0, ArrayUtils.indexOf(i, 1));
        assertEquals(2, ArrayUtils.indexOf(i, 1, 1));
        assertEquals(0, ArrayUtils.indexOf(i, 1, -1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(i, 99));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf((int[]) null, 1));
        assertEquals(2, ArrayUtils.lastIndexOf(i, 1));
        assertEquals(0, ArrayUtils.lastIndexOf(i, 1, 1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(i, 1, -1));
        assertEquals(2, ArrayUtils.lastIndexOf(i, 1, 5));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(i, 99));
        assertTrue(ArrayUtils.contains(i, 1));
        assertFalse(ArrayUtils.contains(i, 99));
        assertFalse(ArrayUtils.contains((int[]) null, 1));

        // short
        short[] s = new short[]{1, 2, 1};
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((short[]) null, (short) 1));
        assertEquals(0, ArrayUtils.indexOf(s, (short) 1));
        assertEquals(2, ArrayUtils.indexOf(s, (short) 1, 1));
        assertEquals(0, ArrayUtils.indexOf(s, (short) 1, -1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(s, (short) 99));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf((short[]) null, (short) 1));
        assertEquals(2, ArrayUtils.lastIndexOf(s, (short) 1));
        assertEquals(0, ArrayUtils.lastIndexOf(s, (short) 1, 1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(s, (short) 1, -1));
        assertEquals(2, ArrayUtils.lastIndexOf(s, (short) 1, 5));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(s, (short) 99));
        assertTrue(ArrayUtils.contains(s, (short) 1));
        assertFalse(ArrayUtils.contains(s, (short) 99));
        assertFalse(ArrayUtils.contains((short[]) null, (short) 1));

        // char
        char[] c = new char[]{'a', 'b', 'a'};
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((char[]) null, 'a'));
        assertEquals(0, ArrayUtils.indexOf(c, 'a'));
        assertEquals(2, ArrayUtils.indexOf(c, 'a', 1));
        assertEquals(0, ArrayUtils.indexOf(c, 'a', -1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(c, 'z'));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf((char[]) null, 'a'));
        assertEquals(2, ArrayUtils.lastIndexOf(c, 'a'));
        assertEquals(0, ArrayUtils.lastIndexOf(c, 'a', 1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(c, 'a', -1));
        assertEquals(2, ArrayUtils.lastIndexOf(c, 'a', 5));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(c, 'z'));
        assertTrue(ArrayUtils.contains(c, 'a'));
        assertFalse(ArrayUtils.contains(c, 'z'));
        assertFalse(ArrayUtils.contains((char[]) null, 'a'));

        // byte
        byte[] b = new byte[]{1, 2, 1};
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((byte[]) null, (byte) 1));
        assertEquals(0, ArrayUtils.indexOf(b, (byte) 1));
        assertEquals(2, ArrayUtils.indexOf(b, (byte) 1, 1));
        assertEquals(0, ArrayUtils.indexOf(b, (byte) 1, -1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(b, (byte) 99));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf((byte[]) null, (byte) 1));
        assertEquals(2, ArrayUtils.lastIndexOf(b, (byte) 1));
        assertEquals(0, ArrayUtils.lastIndexOf(b, (byte) 1, 1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(b, (byte) 1, -1));
        assertEquals(2, ArrayUtils.lastIndexOf(b, (byte) 1, 5));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(b, (byte) 99));
        assertTrue(ArrayUtils.contains(b, (byte) 1));
        assertFalse(ArrayUtils.contains(b, (byte) 99));
        assertFalse(ArrayUtils.contains((byte[]) null, (byte) 1));

        // float
        float[] f = new float[]{1.0f, 2.0f, 1.0f};
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((float[]) null, 1.0f));
        assertEquals(0, ArrayUtils.indexOf(f, 1.0f));
        assertEquals(2, ArrayUtils.indexOf(f, 1.0f, 1));
        assertEquals(0, ArrayUtils.indexOf(f, 1.0f, -1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(f, 9.9f));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf((float[]) null, 1.0f));
        assertEquals(2, ArrayUtils.lastIndexOf(f, 1.0f));
        assertEquals(0, ArrayUtils.lastIndexOf(f, 1.0f, 1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(f, 1.0f, -1));
        assertEquals(2, ArrayUtils.lastIndexOf(f, 1.0f, 5));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(f, 9.9f));
        assertTrue(ArrayUtils.contains(f, 1.0f));
        assertFalse(ArrayUtils.contains(f, 9.9f));
        assertFalse(ArrayUtils.contains((float[]) null, 1.0f));

        // boolean
        boolean[] bool = new boolean[]{true, false, true};
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((boolean[]) null, true));
        assertEquals(0, ArrayUtils.indexOf(bool, true));
        assertEquals(2, ArrayUtils.indexOf(bool, true, 1));
        assertEquals(0, ArrayUtils.indexOf(bool, true, -1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf((boolean[]) null, true));
        assertEquals(2, ArrayUtils.lastIndexOf(bool, true));
        assertEquals(0, ArrayUtils.lastIndexOf(bool, true, 1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(bool, true, -1));
        assertEquals(2, ArrayUtils.lastIndexOf(bool, true, 5));
        assertTrue(ArrayUtils.contains(bool, false));
        assertFalse(ArrayUtils.contains(new boolean[]{true}, false));
        assertFalse(ArrayUtils.contains((boolean[]) null, true));
    }

    @Test(timeout = 4000)
    public void testIndexOfAndContains_DoubleAndTolerance() {
        double[] d = new double[]{1.0, 2.0, 1.0};
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((double[]) null, 1.0));
        assertEquals(0, ArrayUtils.indexOf(d, 1.0));
        assertEquals(2, ArrayUtils.indexOf(d, 1.0, 1));
        assertEquals(0, ArrayUtils.indexOf(d, 1.0, -1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(d, 9.9));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf((double[]) null, 1.0));
        assertEquals(2, ArrayUtils.lastIndexOf(d, 1.0));
        assertEquals(0, ArrayUtils.lastIndexOf(d, 1.0, 1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(d, 1.0, -1));
        assertEquals(2, ArrayUtils.lastIndexOf(d, 1.0, 5));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(d, 9.9));
        assertTrue(ArrayUtils.contains(d, 1.0));
        assertFalse(ArrayUtils.contains(d, 9.9));
        assertFalse(ArrayUtils.contains((double[]) null, 1.0));

        // Tolerance variants
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf((double[]) null, 1.0, 0.1));
        assertEquals(0, ArrayUtils.indexOf(d, 1.05, 0.1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(d, 1.5, 0.1));
        assertEquals(2, ArrayUtils.indexOf(d, 1.05, 1, 0.1));
        assertEquals(0, ArrayUtils.indexOf(d, 1.05, -1, 0.1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf((double[]) null, 1.0, 0.1));
        assertEquals(2, ArrayUtils.lastIndexOf(d, 1.05, 0.1));
        assertEquals(0, ArrayUtils.lastIndexOf(d, 1.05, 1, 0.1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(d, 1.05, -1, 0.1));
        assertEquals(2, ArrayUtils.lastIndexOf(d, 1.05, 10, 0.1));
        assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.lastIndexOf(d, 5.0, 0.1));
        assertTrue(ArrayUtils.contains(d, 1.05, 0.1));
        assertFalse(ArrayUtils.contains(d, 5.0, 0.1));
        assertFalse(ArrayUtils.contains((double[]) null, 1.0, 0.1));
    }

    @Test(timeout = 4000)
    public void testAddAll_PrimitivesAndObjects() {
        assertNull(ArrayUtils.addAll((String[]) null, (String[]) null));
        assertArrayEquals(new String[]{"a"}, ArrayUtils.addAll(new String[]{"a"}, (String[]) null));
        assertArrayEquals(new String[]{"b"}, ArrayUtils.addAll((String[]) null, new String[]{"b"}));
        assertArrayEquals(new String[]{"a", "b"}, ArrayUtils.addAll(new String[]{"a"}, new String[]{"b"}));

        assertNull(ArrayUtils.addAll((boolean[]) null, (boolean[]) null));
        assertArrayEquals(new boolean[]{true}, ArrayUtils.addAll(new boolean[]{true}, (boolean[]) null));
        assertArrayEquals(new boolean[]{false}, ArrayUtils.addAll((boolean[]) null, new boolean[]{false}));
        assertArrayEquals(new boolean[]{true, false}, ArrayUtils.addAll(new boolean[]{true}, new boolean[]{false}));

        assertNull(ArrayUtils.addAll((char[]) null, (char[]) null));
        assertArrayEquals(new char[]{'a'}, ArrayUtils.addAll(new char[]{'a'}, (char[]) null));
        assertArrayEquals(new char[]{'b'}, ArrayUtils.addAll((char[]) null, new char[]{'b'}));
        assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.addAll(new char[]{'a'}, new char[]{'b'}));

        assertNull(ArrayUtils.addAll((byte[]) null, (byte[]) null));
        assertArrayEquals(new byte[]{1}, ArrayUtils.addAll(new byte[]{1}, (byte[]) null));
        assertArrayEquals(new byte[]{2}, ArrayUtils.addAll((byte[]) null, new byte[]{2}));
        assertArrayEquals(new byte[]{1, 2}, ArrayUtils.addAll(new byte[]{1}, new byte[]{2}));

        assertNull(ArrayUtils.addAll((short[]) null, (short[]) null));
        assertArrayEquals(new short[]{1}, ArrayUtils.addAll(new short[]{1}, (short[]) null));
        assertArrayEquals(new short[]{2}, ArrayUtils.addAll((short[]) null, new short[]{2}));
        assertArrayEquals(new short[]{1, 2}, ArrayUtils.addAll(new short[]{1}, new short[]{2}));

        assertNull(ArrayUtils.addAll((int[]) null, (int[]) null));
        assertArrayEquals(new int[]{1}, ArrayUtils.addAll(new int[]{1}, (int[]) null));
        assertArrayEquals(new int[]{2}, ArrayUtils.addAll((int[]) null, new int[]{2}));
        assertArrayEquals(new int[]{1, 2}, ArrayUtils.addAll(new int[]{1}, new int[]{2}));

        assertNull(ArrayUtils.addAll((long[]) null, (long[]) null));
        assertArrayEquals(new long[]{1L}, ArrayUtils.addAll(new long[]{1L}, (long[]) null));
        assertArrayEquals(new long[]{2L}, ArrayUtils.addAll((long[]) null, new long[]{2L}));
        assertArrayEquals(new long[]{1L, 2L}, ArrayUtils.addAll(new long[]{1L}, new long[]{2L}));

        assertNull(ArrayUtils.addAll((float[]) null, (float[]) null));
        assertArrayEquals(new float[]{1.0f}, ArrayUtils.addAll(new float[]{1.0f}, (float[]) null), 0.0f);
        assertArrayEquals(new float[]{2.0f}, ArrayUtils.addAll((float[]) null, new float[]{2.0f}), 0.0f);
        assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.addAll(new float[]{1.0f}, new float[]{2.0f}), 0.0f);

        assertNull(ArrayUtils.addAll((double[]) null, (double[]) null));
        assertArrayEquals(new double[]{1.0}, ArrayUtils.addAll(new double[]{1.0}, (double[]) null), 0.0);
        assertArrayEquals(new double[]{2.0}, ArrayUtils.addAll((double[]) null, new double[]{2.0}), 0.0);
        assertArrayEquals(new double[]{1.0, 2.0}, ArrayUtils.addAll(new double[]{1.0}, new double[]{2.0}), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddAndRemove_PrimitivesAndObjects() {
        // Object add & remove
        assertArrayEquals(new Object[]{null}, ArrayUtils.add((Object[]) null, null));
        assertArrayEquals(new String[]{"a"}, ArrayUtils.add((String[]) null, "a"));
        assertArrayEquals(new String[]{"a", "b"}, ArrayUtils.add(new String[]{"a"}, "b"));

        assertArrayEquals(new String[]{"a"}, ArrayUtils.add((String[]) null, 0, "a"));
        assertArrayEquals(new Object[]{null}, ArrayUtils.add((Object[]) null, 0, null));
        assertArrayEquals(new String[]{"a", "b", "c"}, ArrayUtils.add(new String[]{"a", "c"}, 1, "b"));
        assertArrayEquals(new String[]{"b", "a"}, ArrayUtils.add(new String[]{"a"}, 0, "b"));

        assertArrayEquals(new String[]{"a", "c"}, ArrayUtils.remove(new String[]{"a", "b", "c"}, 1));
        assertArrayEquals(new String[]{"b", "c"}, ArrayUtils.remove(new String[]{"a", "b", "c"}, 0));
        assertArrayEquals(new String[]{"a", "b"}, ArrayUtils.remove(new String[]{"a", "b", "c"}, 2));

        assertNull(ArrayUtils.removeElement((String[]) null, "a"));
        assertArrayEquals(new String[]{"a", "b"}, ArrayUtils.removeElement(new String[]{"a", "b"}, "c"));
        assertArrayEquals(new String[]{"b"}, ArrayUtils.removeElement(new String[]{"a", "b"}, "a"));

        // Primitives add & remove
        assertArrayEquals(new boolean[]{true}, ArrayUtils.add((boolean[]) null, true));
        assertArrayEquals(new boolean[]{true, false}, ArrayUtils.add(new boolean[]{true}, false));
        assertArrayEquals(new boolean[]{true}, ArrayUtils.add((boolean[]) null, 0, true));
        assertArrayEquals(new boolean[]{true, false}, ArrayUtils.add(new boolean[]{true}, 1, false));
        assertArrayEquals(new boolean[]{true}, ArrayUtils.remove(new boolean[]{true, false}, 1));
        assertNull(ArrayUtils.removeElement((boolean[]) null, true));
        assertArrayEquals(new boolean[]{false}, ArrayUtils.removeElement(new boolean[]{true, false}, true));
        assertArrayEquals(new boolean[]{true}, ArrayUtils.removeElement(new boolean[]{true}, false));

        assertArrayEquals(new byte[]{1}, ArrayUtils.add((byte[]) null, (byte) 1));
        assertArrayEquals(new byte[]{1, 2}, ArrayUtils.add(new byte[]{1}, (byte) 2));
        assertArrayEquals(new byte[]{1}, ArrayUtils.add((byte[]) null, 0, (byte) 1));
        assertArrayEquals(new byte[]{1, 2}, ArrayUtils.add(new byte[]{1}, 1, (byte) 2));
        assertArrayEquals(new byte[]{1}, ArrayUtils.remove(new byte[]{1, 2}, 1));
        assertNull(ArrayUtils.removeElement((byte[]) null, (byte) 1));
        assertArrayEquals(new byte[]{2}, ArrayUtils.removeElement(new byte[]{1, 2}, (byte) 1));
        assertArrayEquals(new byte[]{1}, ArrayUtils.removeElement(new byte[]{1}, (byte) 2));

        assertArrayEquals(new char[]{'a'}, ArrayUtils.add((char[]) null, 'a'));
        assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.add(new char[]{'a'}, 'b'));
        assertArrayEquals(new char[]{'a'}, ArrayUtils.add((char[]) null, 0, 'a'));
        assertArrayEquals(new char[]{'a', 'b'}, ArrayUtils.add(new char[]{'a'}, 1, 'b'));
        assertArrayEquals(new char[]{'a'}, ArrayUtils.remove(new char[]{'a', 'b'}, 1));
        assertNull(ArrayUtils.removeElement((char[]) null, 'a'));
        assertArrayEquals(new char[]{'b'}, ArrayUtils.removeElement(new char[]{'a', 'b'}, 'a'));
        assertArrayEquals(new char[]{'a'}, ArrayUtils.removeElement(new char[]{'a'}, 'b'));

        assertArrayEquals(new short[]{1}, ArrayUtils.add((short[]) null, (short) 1));
        assertArrayEquals(new short[]{1, 2}, ArrayUtils.add(new short[]{1}, (short) 2));
        assertArrayEquals(new short[]{1}, ArrayUtils.add((short[]) null, 0, (short) 1));
        assertArrayEquals(new short[]{1, 2}, ArrayUtils.add(new short[]{1}, 1, (short) 2));
        assertArrayEquals(new short[]{1}, ArrayUtils.remove(new short[]{1, 2}, 1));
        assertNull(ArrayUtils.removeElement((short[]) null, (short) 1));
        assertArrayEquals(new short[]{2}, ArrayUtils.removeElement(new short[]{1, 2}, (short) 1));
        assertArrayEquals(new short[]{1}, ArrayUtils.removeElement(new short[]{1}, (short) 2));

        assertArrayEquals(new int[]{1}, ArrayUtils.add((int[]) null, 1));
        assertArrayEquals(new int[]{1, 2}, ArrayUtils.add(new int[]{1}, 2));
        assertArrayEquals(new int[]{1}, ArrayUtils.add((int[]) null, 0, 1));
        assertArrayEquals(new int[]{1, 2}, ArrayUtils.add(new int[]{1}, 1, 2));
        assertArrayEquals(new int[]{1}, ArrayUtils.remove(new int[]{1, 2}, 1));
        assertNull(ArrayUtils.removeElement((int[]) null, 1));
        assertArrayEquals(new int[]{2}, ArrayUtils.removeElement(new int[]{1, 2}, 1));
        assertArrayEquals(new int[]{1}, ArrayUtils.removeElement(new int[]{1}, 2));

        assertArrayEquals(new long[]{1L}, ArrayUtils.add((long[]) null, 1L));
        assertArrayEquals(new long[]{1L, 2L}, ArrayUtils.add(new long[]{1L}, 2L));
        assertArrayEquals(new long[]{1L}, ArrayUtils.add((long[]) null, 0, 1L));
        assertArrayEquals(new long[]{1L, 2L}, ArrayUtils.add(new long[]{1L}, 1, 2L));
        assertArrayEquals(new long[]{1L}, ArrayUtils.remove(new long[]{1L, 2L}, 1));
        assertNull(ArrayUtils.removeElement((long[]) null, 1L));
        assertArrayEquals(new long[]{2L}, ArrayUtils.removeElement(new long[]{1L, 2L}, 1L));
        assertArrayEquals(new long[]{1L}, ArrayUtils.removeElement(new long[]{1L}, 2L));

        assertArrayEquals(new float[]{1.0f}, ArrayUtils.add((float[]) null, 1.0f), 0.0f);
        assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.add(new float[]{1.0f}, 2.0f), 0.0f);
        assertArrayEquals(new float[]{1.0f}, ArrayUtils.add((float[]) null, 0, 1.0f), 0.0f);
        assertArrayEquals(new float[]{1.0f, 2.0f}, ArrayUtils.add(new float[]{1.0f}, 1, 2.0f), 0.0f);
        assertArrayEquals(new float[]{1.0f}, ArrayUtils.remove(new float[]{1.0f, 2.0f}, 1), 0.0f);
        assertNull(ArrayUtils.removeElement((float[]) null, 1.0f));
        assertArrayEquals(new float[]{2.0f}, ArrayUtils.removeElement(new float[]{1.0f, 2.0f}, 1.0f), 0.0f);
        assertArrayEquals(new float[]{1.0f}, ArrayUtils.removeElement(new float[]{1.0f}, 2.0f), 0.0f);

        assertArrayEquals(new double[]{1.0}, ArrayUtils.add((double[]) null, 1.0), 0.0);
        assertArrayEquals(new double[]{1.0, 2.0}, ArrayUtils.add(new double[]{1.0}, 2.0), 0.0);
        assertArrayEquals(new double[]{1.0}, ArrayUtils.add((double[]) null, 0, 1.0), 0.0);
        assertArrayEquals(new double[]{1.0, 2.0}, ArrayUtils.add(new double[]{1.0}, 1, 2.0), 0.0);
        assertArrayEquals(new double[]{1.0}, ArrayUtils.remove(new double[]{1.0, 2.0}, 1), 0.0);
        assertNull(ArrayUtils.removeElement((double[]) null, 1.0));
        assertArrayEquals(new double[]{2.0}, ArrayUtils.removeElement(new double[]{1.0, 2.0}, 1.0), 0.0);
        assertArrayEquals(new double[]{1.0}, ArrayUtils.removeElement(new double[]{1.0}, 2.0), 0.0);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToMap_NonEntryOrArray_ThrowsIAE() {
        ArrayUtils.toMap(new Object[]{"invalid_element"});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToMap_ShortArray_ThrowsIAE() {
        ArrayUtils.toMap(new Object[]{new Object[]{"key_without_value"}});
    }

    @Test(timeout = 4000)
    public void testToMap_NullArray() {
        assertNull(ArrayUtils.toMap(null));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetLength_NonArrayObject_ThrowsIAE() {
        ArrayUtils.getLength("string_is_not_an_array");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsSameType_FirstNull_ThrowsIAE() {
        ArrayUtils.isSameType(null, new int[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsSameType_SecondNull_ThrowsIAE() {
        ArrayUtils.isSameType(new int[0], null);
    }

    @Test(timeout = 4000)
    public void testIsSameType_ValidComparisons() {
        assertTrue(ArrayUtils.isSameType(new int[0], new int[2]));
        assertFalse(ArrayUtils.isSameType(new int[0], new long[0]));
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToPrimitive_CharacterNullElement_ThrowsNPE() {
        ArrayUtils.toPrimitive(new Character[]{null});
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToPrimitive_LongNullElement_ThrowsNPE() {
        ArrayUtils.toPrimitive(new Long[]{null});
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToPrimitive_IntegerNullElement_ThrowsNPE() {
        ArrayUtils.toPrimitive(new Integer[]{null});
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToPrimitive_ShortNullElement_ThrowsNPE() {
        ArrayUtils.toPrimitive(new Short[]{null});
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToPrimitive_ByteNullElement_ThrowsNPE() {
        ArrayUtils.toPrimitive(new Byte[]{null});
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToPrimitive_DoubleNullElement_ThrowsNPE() {
        ArrayUtils.toPrimitive(new Double[]{null});
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToPrimitive_FloatNullElement_ThrowsNPE() {
        ArrayUtils.toPrimitive(new Float[]{null});
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToPrimitive_BooleanNullElement_ThrowsNPE() {
        ArrayUtils.toPrimitive(new Boolean[]{null});
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAdd_NullArrayNonZeroIndex_ThrowsIOOBE() {
        ArrayUtils.add(null, 1, "a");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAdd_IndexNegative_ThrowsIOOBE() {
        ArrayUtils.add(new String[]{"a"}, -1, "b");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAdd_IndexGreaterThanLength_ThrowsIOOBE() {
        ArrayUtils.add(new String[]{"a"}, 2, "b");
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testAdd_PrimitiveNullArrayNonZeroIndex_ThrowsIOOBE() {
        ArrayUtils.add((int[]) null, 1, 5);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemove_NullArray_ThrowsIOOBE() {
        ArrayUtils.remove((Object[]) null, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemove_IndexNegative_ThrowsIOOBE() {
        ArrayUtils.remove(new String[]{"a"}, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testRemove_IndexEqualToLength_ThrowsIOOBE() {
        ArrayUtils.remove(new String[]{"a"}, 1);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndConstants() {
        ArrayUtils utils = new ArrayUtils();
        assertNotNull(utils);

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