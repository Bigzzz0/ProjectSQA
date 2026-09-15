package org.apache.commons.csv;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.Test;

/**
 * Comprehensive JUnit 4 test suite for CSVRecord, targeting line and branch coverage,
 * and specifically the known defect: ArrayIndexOutOfBoundsException in putIn/toMap
 * when record values are shorter than the header mapping indexes.
 * 
 * <p>Branch & Defect Analysis Matrix:</p>
 * <ul>
 *   <li>get(Enum): null enum → NPE; valid enum → delegates to get(String)</li>
 *   <li>get(int): negative index → AIOoBE; index >= values.length → AIOoBE; normal</li>
 *   <li>get(String): mapping == null → ISE; name not mapped → IAE; mapped but index out of bounds → IAE; normal</li>
 *   <li>isConsistent: mapping null → true; mapping.size() == values.length → true; else false</li>
 *   <li>isMapped: mapping null → false; else containsKey</li>
 *   <li>isSet: mapping null → false; mapped && index < values.length → true; else false</li>
 *   <li>putIn / toMap: normal; short record → AIOoBE (defect); null mapping → empty map; null values not expected</li>
 *   <li>iterator: delegates to toList().iterator()</li>
 *   <li>size: values.length</li>
 *   <li>toString: Arrays.toString(values)</li>
 *   <li>values() : returns the internal array</li>
 * </ul>
 */
public class CSVRecordDeepseekTest {

    // ======================== Partition A: Core Functional Logic & State Transitions ========================

    @Test(timeout = 4000)
    public void testGetByEnum() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("COL1", 0);
        mapping.put("COL2", 1);
        CSVRecord rec = new CSVRecord(new String[]{"val1", "val2"}, mapping, null, 1);
        assertEquals("val1", rec.get(TestEnum.COL1));
        assertEquals("val2", rec.get(TestEnum.COL2));
    }

    @Test(timeout = 4000)
    public void testGetByIndex() {
        CSVRecord rec = new CSVRecord(new String[]{"a", "b", "c"}, null, null, 1);
        assertEquals("a", rec.get(0));
        assertEquals("b", rec.get(1));
        assertEquals("c", rec.get(2));
    }

    @Test(timeout = 4000)
    public void testGetByString() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        mapping.put("B", 1);
        CSVRecord rec = new CSVRecord(new String[]{"x", "y"}, mapping, null, 1);
        assertEquals("x", rec.get("A"));
        assertEquals("y", rec.get("B"));
    }

    @Test(timeout = 4000)
    public void testGetComment() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, "my comment", 1);
        assertEquals("my comment", rec.getComment());
        CSVRecord recNoComment = new CSVRecord(new String[]{"a"}, null, null, 1);
        assertNull(recNoComment.getComment());
    }

    @Test(timeout = 4000)
    public void testGetRecordNumber() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, null, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, rec.getRecordNumber());
        CSVRecord recNeg = new CSVRecord(new String[]{"a"}, null, null, -100);
        assertEquals(-100L, recNeg.getRecordNumber());
    }

    @Test(timeout = 4000)
    public void testIsConsistentNullMapping() {
        CSVRecord rec = new CSVRecord(new String[]{"a", "b"}, null, null, 1);
        assertTrue(rec.isConsistent());
    }

    @Test(timeout = 4000)
    public void testIsConsistentEqualSize() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        mapping.put("B", 1);
        CSVRecord rec = new CSVRecord(new String[]{"a", "b"}, mapping, null, 1);
        assertTrue(rec.isConsistent());
    }

    @Test(timeout = 4000)
    public void testIsConsistentMismatch() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        mapping.put("B", 1);
        mapping.put("C", 2);
        CSVRecord rec = new CSVRecord(new String[]{"a", "b"}, mapping, null, 1); // values length 2, mapping size 3
        assertFalse(rec.isConsistent());
    }

    @Test(timeout = 4000)
    public void testIsMappedNullMapping() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, null, 1);
        assertFalse(rec.isMapped("any"));
    }

    @Test(timeout = 4000)
    public void testIsMappedWithMapping() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("X", 0);
        CSVRecord rec = new CSVRecord(new String[]{"a"}, mapping, null, 1);
        assertTrue(rec.isMapped("X"));
        assertFalse(rec.isMapped("Y"));
    }

    @Test(timeout = 4000)
    public void testIsSetNullMapping() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, null, 1);
        assertFalse(rec.isSet("any"));
    }

    @Test(timeout = 4000)
    public void testIsSetMappedAndWithinBounds() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        CSVRecord rec = new CSVRecord(new String[]{"a"}, mapping, null, 1);
        assertTrue(rec.isSet("A"));
    }

    @Test(timeout = 4000)
    public void testIsSetMappedButOutOfBounds() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 5); // index beyond values length
        CSVRecord rec = new CSVRecord(new String[]{"a"}, mapping, null, 1);
        assertFalse(rec.isSet("A"));
    }

    @Test(timeout = 4000)
    public void testIterator() {
        CSVRecord rec = new CSVRecord(new String[]{"a", "b", "c"}, null, null, 1);
        java.util.Iterator<String> it = rec.iterator();
        assertTrue(it.hasNext());
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertEquals("c", it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testSize() {
        CSVRecord rec = new CSVRecord(new String[]{"a", "b"}, null, null, 1);
        assertEquals(2, rec.size());
        CSVRecord empty = new CSVRecord(new String[0], null, null, 1);
        assertEquals(0, empty.size());
    }

    @Test(timeout = 4000)
    public void testPutInNormal() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        mapping.put("B", 1);
        CSVRecord rec = new CSVRecord(new String[]{"x", "y"}, mapping, null, 1);
        Map<String, String> target = new HashMap<>();
        Map<String, String> result = rec.putIn(target);
        assertSame(target, result);
        assertEquals("x", result.get("A"));
        assertEquals("y", result.get("B"));
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testToMapNormal() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        mapping.put("B", 1);
        CSVRecord rec = new CSVRecord(new String[]{"x", "y"}, mapping, null, 1);
        Map<String, String> map = rec.toMap();
        assertEquals(2, map.size());
        assertEquals("x", map.get("A"));
        assertEquals("y", map.get("B"));
    }

    @Test(timeout = 4000)
    public void testToString() {
        CSVRecord rec = new CSVRecord(new String[]{"a", "b"}, null, null, 1);
        assertEquals("[a, b]", rec.toString());
    }

    @Test(timeout = 4000)
    public void testValues() {
        String[] vals = new String[]{"a", "b"};
        CSVRecord rec = new CSVRecord(vals, null, null, 1);
        assertSame(vals, rec.values());
    }

    // ======================== Partition B: Boundary Value Analysis & Extremes ========================

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexNegative() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, null, 1);
        rec.get(-1);
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexOutOfBounds() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, null, 1);
        rec.get(1);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testGetByEnumNull() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, null, 1);
        rec.get((Enum<?>) null);
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testGetByStringNoMapping() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, null, 1);
        rec.get("any");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetByStringNameNotFound() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("X", 0);
        CSVRecord rec = new CSVRecord(new String[]{"a"}, mapping, null, 1);
        rec.get("Y");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetByStringIndexOutOfBounds() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("X", 5); // index out of bounds
        CSVRecord rec = new CSVRecord(new String[]{"a"}, mapping, null, 1);
        rec.get("X");
    }

    @Test(timeout = 4000)
    public void testIsMappedWithNullMappingReturnsFalse() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, null, 1);
        assertFalse(rec.isMapped("anything"));
    }

    @Test(timeout = 4000)
    public void testIsSetWithNullMappingReturnsFalse() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, null, 1);
        assertFalse(rec.isSet("anything"));
    }

    // ======================== Partition C: Defect-Targeted Branch Zone ========================

    /**
     * Directly targets the known defect: ArrayIndexOutOfBoundsException in toMap()
     * when a mapping entry points to an index >= values.length.
     * The expected correct behavior is to throw an IllegalArgumentException (or handle gracefully).
     * On the defective version, this test will fail because an ArrayIndexOutOfBoundsException is thrown instead.
     */
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testToMapWithShortRecord() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        mapping.put("B", 2); // index 2 is out of bounds (values length = 2, last index = 1)
        CSVRecord rec = new CSVRecord(new String[]{"a", "b"}, mapping, null, 1);
        rec.toMap(); // Should throw IAE, not AIOoBE
    }

    // Additional test for putIn with short record - same defect
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPutInWithShortRecord() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("A", 0);
        mapping.put("B", 3); // out of bounds
        CSVRecord rec = new CSVRecord(new String[]{"a", "b"}, mapping, null, 1);
        Map<String, String> target = new HashMap<>();
        rec.putIn(target);
    }

    // ======================== Partition D: Exception & Defensive Guard Paths ========================

    @Test(timeout = 4000)
    public void testConstructorNullValues() {
        // values null is replaced with empty array
        CSVRecord rec = new CSVRecord(null, null, null, 1);
        assertEquals(0, rec.size());
        assertArrayEquals(new String[0], rec.values());
    }

    @Test(timeout = 4000)
    public void testConstructorNullMapping() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, null, 1);
        assertNull(rec.getComment()); // comment null
        // ensure no NPE when calling methods that check mapping
        assertTrue(rec.isConsistent());
        assertFalse(rec.isMapped("any"));
        assertFalse(rec.isSet("any"));
    }

    @Test(timeout = 4000)
    public void testIsMappedWithNullMapping() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, null, 1);
        assertFalse(rec.isMapped("test"));
    }

    @Test(timeout = 4000)
    public void testIsSetWithNullMapping() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, null, 1);
        assertFalse(rec.isSet("test"));
    }

    @Test(timeout = 4000)
    public void testToMapWithNullMapping() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, null, 1);
        Map<String, String> map = rec.toMap();
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testPutInWithNullMapping() {
        CSVRecord rec = new CSVRecord(new String[]{"a"}, null, null, 1);
        Map<String, String> target = new HashMap<>();
        Map<String, String> result = rec.putIn(target);
        assertTrue(result.isEmpty());
        assertSame(target, result);
    }

    // ======================== Partition E: Object Lifecycle & Contract Integrity ========================

    @Test(timeout = 4000)
    public void testIterableIterator() {
        CSVRecord rec = new CSVRecord(new String[]{"a", "b"}, null, null, 1);
        for (String val : rec) {
            assertNotNull(val);
        }
        // Verify all values are visited
        int count = 0;
        for (@SuppressWarnings("unused") String s : rec) {
            count++;
        }
        assertEquals(2, count);
    }

    // Helper enum for get(Enum) tests
    private enum TestEnum {
        COL1, COL2
    }
}