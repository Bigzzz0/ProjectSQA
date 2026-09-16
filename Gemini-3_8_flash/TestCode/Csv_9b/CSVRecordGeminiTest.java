package org.apache.commons.csv;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.csv.CSVRecord
 * Defect Target: CSVRecordTest::testToMapWithNoHeader -> NPE in putIn(Map) when mapping is null
 *
 * Method Branches & Scenarios Covered:
 * 1. Constructor:
 *    - values != null vs values == null (EMPTY_STRING_ARRAY fallback)
 *    - mapping, comment, recordNumber assignment
 * 2. get(Enum<?>):
 *    - Delegates to get(e.toString())
 * 3. get(int):
 *    - Valid index
 *    - Negative / Out-of-bounds index (ArrayIndexOutOfBoundsException)
 * 4. get(String):
 *    - Branch: mapping == null -> IllegalStateException
 *    - Branch: index == null (column not mapped) -> IllegalArgumentException
 *    - Branch: index >= values.length (AIOOBE caught) -> IllegalArgumentException
 *    - Normal hit -> values[index] returned
 * 5. getComment() & getRecordNumber():
 *    - Correct state retrieval across permutations (null comment, positive/negative record numbers)
 * 6. isConsistent():
 *    - Branch: mapping == null -> true
 *    - Branch: mapping.size() == values.length -> true
 *    - Branch: mapping.size() != values.length -> false
 * 7. isMapped(String):
 *    - Branch: mapping == null -> false
 *    - Branch: mapping != null && mapping.containsKey(name) == true -> true
 *    - Branch: mapping != null && mapping.containsKey(name) == false -> false
 * 8. isSet(String):
 *    - Branch: !isMapped(name) -> false
 *    - Branch: isMapped(name) && index < values.length -> true
 *    - Branch: isMapped(name) && index >= values.length -> false
 * 9. iterator():
 *    - Iterate across elements, verify order and termination
 * 10. putIn(M) / toMap():
 *    - Defect Path: mapping == null -> must return empty map without throwing NullPointerException
 *    - Branch: col < values.length (put) vs col >= values.length (skipped)
 *    - Generic type preservation for custom map
 * 11. values() & size() & toString():
 *    - Array equality, empty checks, and string representation matches Arrays.toString
 * 12. Serialization:
 *    - Serializable contract integrity check
 * ----------------------------------------------------------------------------------------------------
 */
public class CSVRecordGeminiTest {

    private enum HeaderEnum {
        FIRST_NAME,
        LAST_NAME,
        AGE
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardRecordAccessors() {
        final String[] values = new String[]{"John", "Doe", "30"};
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("first", 0);
        mapping.put("last", 1);
        mapping.put("age", 2);

        final CSVRecord record = new CSVRecord(values, mapping, "sample comment", 42L);

        assertEquals("sample comment", record.getComment());
        assertEquals(42L, record.getRecordNumber());
        assertEquals(3, record.size());
        assertEquals("John", record.get(0));
        assertEquals("Doe", record.get(1));
        assertEquals("30", record.get(2));
        assertEquals("John", record.get("first"));
        assertEquals("Doe", record.get("last"));
        assertEquals("30", record.get("age"));
        assertArrayEquals(values, record.values());
        assertEquals("[John, Doe, 30]", record.toString());
    }

    @Test(timeout = 4000)
    public void testGetByEnum() {
        final String[] values = new String[]{"Alice", "Smith", "25"};
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put(HeaderEnum.FIRST_NAME.toString(), 0);
        mapping.put(HeaderEnum.LAST_NAME.toString(), 1);
        mapping.put(HeaderEnum.AGE.toString(), 2);

        final CSVRecord record = new CSVRecord(values, mapping, null, 1L);

        assertEquals("Alice", record.get(HeaderEnum.FIRST_NAME));
        assertEquals("Smith", record.get(HeaderEnum.LAST_NAME));
        assertEquals("25", record.get(HeaderEnum.AGE));
    }

    @Test(timeout = 4000)
    public void testIterator() {
        final String[] values = new String[]{"alpha", "beta", "gamma"};
        final CSVRecord record = new CSVRecord(values, null, null, 1L);

        final Iterator<String> it = record.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals("alpha", it.next());
        assertTrue(it.hasNext());
        assertEquals("beta", it.next());
        assertTrue(it.hasNext());
        assertEquals("gamma", it.next());
        assertFalse(it.hasNext());

        try {
            it.next();
            fail("Expected NoSuchElementException");
        } catch (final NoSuchElementException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testToMapPopulated() {
        final String[] values = new String[]{"val1", "val2"};
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("k1", 0);
        mapping.put("k2", 1);

        final CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        final Map<String, String> map = record.toMap();

        assertEquals(2, map.size());
        assertEquals("val1", map.get("k1"));
        assertEquals("val2", map.get("k2"));
    }

    @Test(timeout = 4000)
    public void testPutInCustomMap() {
        final String[] values = new String[]{"val1", "val2"};
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("k1", 0);
        mapping.put("k2", 1);

        final CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        final TreeMap<String, String> customMap = new TreeMap<String, String>();
        final TreeMap<String, String> result = record.putIn(customMap);

        assertSame(customMap, result);
        assertEquals(2, result.size());
        assertEquals("val1", result.get("k1"));
        assertEquals("val2", result.get("k2"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorWithNullValues() {
        final CSVRecord record = new CSVRecord(null, null, null, 0L);

        assertEquals(0, record.size());
        assertNotNull(record.values());
        assertEquals(0, record.values().length);
        assertEquals(0L, record.getRecordNumber());
        assertNull(record.getComment());
        assertTrue(record.isConsistent());
        assertFalse(record.isMapped("any"));
        assertFalse(record.isSet("any"));
        assertEquals("[]", record.toString());
        assertFalse(record.iterator().hasNext());
    }

    @Test(timeout = 4000)
    public void testRecordNumberBoundaries() {
        final CSVRecord recMin = new CSVRecord(new String[0], null, null, Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, recMin.getRecordNumber());

        final CSVRecord recMax = new CSVRecord(new String[0], null, null, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, recMax.getRecordNumber());
    }

    @Test(timeout = 4000)
    public void testIsConsistentBranches() {
        // Branch 1: mapping == null -> true
        final CSVRecord recNullMap = new CSVRecord(new String[]{"a"}, null, null, 1L);
        assertTrue(recNullMap.isConsistent());

        // Branch 2: mapping.size() == values.length -> true
        final Map<String, Integer> map2 = new HashMap<String, Integer>();
        map2.put("h1", 0);
        map2.put("h2", 1);
        final CSVRecord recConsistent = new CSVRecord(new String[]{"a", "b"}, map2, null, 1L);
        assertTrue(recConsistent.isConsistent());

        // Branch 3: mapping.size() != values.length -> false
        final CSVRecord recInconsistent1 = new CSVRecord(new String[]{"a"}, map2, null, 1L);
        assertFalse(recInconsistent1.isConsistent());

        final CSVRecord recInconsistent2 = new CSVRecord(new String[]{"a", "b", "c"}, map2, null, 1L);
        assertFalse(recInconsistent2.isConsistent());
    }

    @Test(timeout = 4000)
    public void testIsMappedBranches() {
        // mapping == null -> false
        final CSVRecord recordNullMap = new CSVRecord(new String[]{"a"}, null, null, 1L);
        assertFalse(recordNullMap.isMapped("col"));

        // mapping != null but key not present -> false
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col1", 0);
        final CSVRecord record = new CSVRecord(new String[]{"a"}, mapping, null, 1L);
        assertFalse(record.isMapped("col2"));

        // mapping != null and key present -> true
        assertTrue(record.isMapped("col1"));
    }

    @Test(timeout = 4000)
    public void testIsSetBranches() {
        // Branch 1: mapping is null -> false
        final CSVRecord recordNullMap = new CSVRecord(new String[]{"a"}, null, null, 1L);
        assertFalse(recordNullMap.isSet("col"));

        // Branch 2: mapping does not contain key -> false
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col1", 0);
        mapping.put("col2", 2); // index 2 is out of bounds for values length 1
        final CSVRecord record = new CSVRecord(new String[]{"val1"}, mapping, null, 1L);

        assertFalse(record.isSet("absent"));

        // Branch 3: mapping contains key, index < values.length -> true
        assertTrue(record.isSet("col1"));

        // Branch 4: mapping contains key, index >= values.length -> false
        assertFalse(record.isSet("col2"));
    }

    @Test(timeout = 4000)
    public void testPutInWithSparseMappingIndices() {
        // Map defines an index >= values.length (should be skipped in putIn)
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("k1", 0);
        mapping.put("k2", 5);

        final CSVRecord record = new CSVRecord(new String[]{"v1"}, mapping, null, 1L);
        final Map<String, String> map = record.toMap();

        assertEquals(1, map.size());
        assertEquals("v1", map.get("k1"));
        assertFalse(map.containsKey("k2"));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets known defect: org.apache.commons.csv.CSVRecordTest::testToMapWithNoHeader
     * When CSVRecord has mapping == null, toMap() must return an empty Map rather than
     * throwing NullPointerException.
     */
    @Test(timeout = 4000)
    public void testToMapWithNoHeader() {
        final CSVRecord record = new CSVRecord(new String[]{"A", "B"}, null, null, 1L);
        final Map<String, String> map = record.toMap();
        assertNotNull("toMap() must not return null when mapping is null", map);
        assertTrue("toMap() must be empty when mapping is null", map.isEmpty());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testGetStringWithNullMappingThrowsIllegalStateException() {
        final CSVRecord record = new CSVRecord(new String[]{"val"}, null, null, 1L);
        record.get("col");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetStringNotMappedThrowsIllegalArgumentException() {
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("present", 0);

        final CSVRecord record = new CSVRecord(new String[]{"val"}, mapping, null, 1L);
        record.get("absent");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetStringIndexOutOfBoundsThrowsIllegalArgumentException() {
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("outOfBoundsCol", 10);

        final CSVRecord record = new CSVRecord(new String[]{"onlyOneVal"}, mapping, null, 1L);
        record.get("outOfBoundsCol");
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetIntNegativeIndexThrowsException() {
        final CSVRecord record = new CSVRecord(new String[]{"val"}, null, null, 1L);
        record.get(-1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetIntOutOfBoundsIndexThrowsException() {
        final CSVRecord record = new CSVRecord(new String[]{"val"}, null, null, 1L);
        record.get(1);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("col1", 0);
        mapping.put("col2", 1);

        final CSVRecord original = new CSVRecord(new String[]{"data1", "data2"}, mapping, "comment", 99L);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final CSVRecord deserialized = (CSVRecord) ois.readObject();
        ois.close();

        assertEquals(original.getComment(), deserialized.getComment());
        assertEquals(original.getRecordNumber(), deserialized.getRecordNumber());
        assertEquals(original.size(), deserialized.size());
        assertEquals(original.get("col1"), deserialized.get("col1"));
        assertEquals(original.get("col2"), deserialized.get("col2"));
        assertArrayEquals(original.values(), deserialized.values());
        assertEquals(original.toString(), deserialized.toString());
    }
}