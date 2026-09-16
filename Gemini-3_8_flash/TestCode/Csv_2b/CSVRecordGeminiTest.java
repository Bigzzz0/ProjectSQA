package org.apache.commons.csv;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Method Under Test      | Branch / Condition                | Input / State               | Expected Outcome
 * ====================================================================================================
 * CSVRecord (constructor)| values == null                    | values = null               | values becomes EMPTY_STRING_ARRAY
 * CSVRecord (constructor)| values != null                    | values = ["a", "b"]         | values preserved
 * get(int)               | 0 <= i < values.length            | i = 0, values = ["val"]     | returns "val"
 * get(int)               | i < 0                             | i = -1                      | ArrayIndexOutOfBoundsException
 * get(int)               | i >= values.length                | i = 5, length = 1           | ArrayIndexOutOfBoundsException
 * get(String)            | mapping == null                   | mapping = null              | IllegalStateException
 * get(String)            | mapping != null, key not found    | name = "unknown"            | returns null
 * get(String)            | mapping != null, index valid      | name = "col", index = 0     | returns values[0]
 * get(String) [DEFECT]   | mapping != null, index >= values  | name = "col", index = 5 > 1 | IllegalArgumentException (Defects4J ground truth: testGetStringInconsistentRecord)
 * isConsistent()         | mapping == null                   | mapping = null              | returns true
 * isConsistent()         | mapping != null, size == values   | map.size = 2, values.len = 2| returns true
 * isConsistent()         | mapping != null, size != values   | map.size = 3, values.len = 2| returns false
 * isMapped(String)       | mapping == null                   | mapping = null              | returns false
 * isMapped(String)       | mapping != null, containsKey true | key = "A"                   | returns true
 * isMapped(String)       | mapping != null, containsKey false| key = "Z"                   | returns false
 * isSet(String)          | !isMapped(name)                   | mapping = null / not in map | returns false
 * isSet(String)          | isMapped(name) && idx < len       | index = 0, len = 2          | returns true
 * isSet(String)          | isMapped(name) && idx >= len      | index = 2, len = 2          | returns false
 * iterator()             | standard array traversal          | values = ["x", "y"]         | iterates over exact array elements
 * values()               | package-private getter            | values = ["a"]              | returns array reference
 * getComment()           | null and non-null comments        | comment = "hdr", or null    | returns comment string or null
 * getRecordNumber()      | any long value                    | 0L, 42L, -1L                | returns recordNumber
 * size()                 | empty or populated values         | values.length               | returns integer length
 * toString()             | standard array representation     | values = ["a", "b"]         | returns "[a, b]"
 * Serialization          | Serializable contract             | full record state           | round-trip object equality
 * ====================================================================================================
 */
public class CSVRecordGeminiTest {

    // ================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================================================

    @Test(timeout = 4000)
    public void testGetByIndex() {
        final String[] values = new String[]{"first", "second", "third"};
        final CSVRecord record = new CSVRecord(values, null, null, 1L);

        assertEquals("first", record.get(0));
        assertEquals("second", record.get(1));
        assertEquals("third", record.get(2));
    }

    @Test(timeout = 4000)
    public void testGetByNameSuccess() {
        final Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("firstCol", Integer.valueOf(0));
        map.put("secondCol", Integer.valueOf(1));

        final CSVRecord record = new CSVRecord(new String[]{"alpha", "beta"}, map, "a comment", 100L);

        assertEquals("alpha", record.get("firstCol"));
        assertEquals("beta", record.get("secondCol"));
    }

    @Test(timeout = 4000)
    public void testGetComment() {
        final CSVRecord recordWithComment = new CSVRecord(new String[]{"v"}, null, "Record note", 1L);
        assertEquals("Record note", recordWithComment.getComment());

        final CSVRecord recordNoComment = new CSVRecord(new String[]{"v"}, null, null, 1L);
        assertNull(recordNoComment.getComment());
    }

    @Test(timeout = 4000)
    public void testGetRecordNumber() {
        final CSVRecord recordPositive = new CSVRecord(new String[0], null, null, 42L);
        assertEquals(42L, recordPositive.getRecordNumber());

        final CSVRecord recordZero = new CSVRecord(new String[0], null, null, 0L);
        assertEquals(0L, recordZero.getRecordNumber());

        final CSVRecord recordNegative = new CSVRecord(new String[0], null, null, -15L);
        assertEquals(-15L, recordNegative.getRecordNumber());
    }

    @Test(timeout = 4000)
    public void testSize() {
        final CSVRecord emptyRecord = new CSVRecord(new String[0], null, null, 1L);
        assertEquals(0, emptyRecord.size());

        final CSVRecord recordWithValues = new CSVRecord(new String[]{"1", "2", "3", "4"}, null, null, 1L);
        assertEquals(4, recordWithValues.size());
    }

    @Test(timeout = 4000)
    public void testValues() {
        final String[] inputValues = new String[]{"val1", "val2"};
        final CSVRecord record = new CSVRecord(inputValues, null, null, 1L);
        final String[] internalValues = record.values();

        assertArrayEquals(inputValues, internalValues);
        assertSame(inputValues, internalValues);
    }

    @Test(timeout = 4000)
    public void testIterator() {
        final String[] values = new String[]{"one", "two", "three"};
        final CSVRecord record = new CSVRecord(values, null, null, 1L);

        final List<String> extracted = new ArrayList<String>();
        final Iterator<String> iterator = record.iterator();
        while (iterator.hasNext()) {
            extracted.add(iterator.next());
        }

        assertEquals(3, extracted.size());
        assertEquals("one", extracted.get(0));
        assertEquals("two", extracted.get(1));
        assertEquals("three", extracted.get(2));
    }

    @Test(timeout = 4000)
    public void testToString() {
        final CSVRecord record = new CSVRecord(new String[]{"A", "B", "C"}, null, null, 1L);
        assertEquals("[A, B, C]", record.toString());

        final CSVRecord emptyRecord = new CSVRecord(new String[0], null, null, 1L);
        assertEquals("[]", emptyRecord.toString());
    }

    // ================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================

    @Test(timeout = 4000)
    public void testConstructorWithNullValues() {
        final CSVRecord record = new CSVRecord(null, null, null, 0L);

        assertEquals(0, record.size());
        assertNotNull(record.values());
        assertEquals(0, record.values().length);
        assertFalse(record.iterator().hasNext());
        assertEquals("[]", record.toString());
    }

    @Test(timeout = 4000)
    public void testIsConsistentWithNullMapping() {
        final CSVRecord record = new CSVRecord(new String[]{"a", "b"}, null, null, 1L);
        assertTrue(record.isConsistent());
    }

    @Test(timeout = 4000)
    public void testIsConsistentWhenLengthsMatch() {
        final Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("A", Integer.valueOf(0));
        map.put("B", Integer.valueOf(1));

        final CSVRecord record = new CSVRecord(new String[]{"x", "y"}, map, null, 1L);
        assertTrue(record.isConsistent());
    }

    @Test(timeout = 4000)
    public void testIsConsistentWhenLengthsDiffer() {
        final Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("A", Integer.valueOf(0));
        map.put("B", Integer.valueOf(1));
        map.put("C", Integer.valueOf(2));

        final CSVRecord recordFewerValues = new CSVRecord(new String[]{"x", "y"}, map, null, 1L);
        assertFalse(recordFewerValues.isConsistent());

        final CSVRecord recordMoreValues = new CSVRecord(new String[]{"x", "y", "z", "w"}, map, null, 1L);
        assertFalse(recordMoreValues.isConsistent());
    }

    @Test(timeout = 4000)
    public void testIsMapped() {
        final CSVRecord recordNullMap = new CSVRecord(new String[]{"a"}, null, null, 1L);
        assertFalse(recordNullMap.isMapped("col"));

        final Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("present", Integer.valueOf(0));

        final CSVRecord record = new CSVRecord(new String[]{"a"}, map, null, 1L);
        assertTrue(record.isMapped("present"));
        assertFalse(record.isMapped("absent"));
        assertFalse(record.isMapped(null));
    }

    @Test(timeout = 4000)
    public void testIsSet() {
        final Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("inRange0", Integer.valueOf(0));
        map.put("inRange1", Integer.valueOf(1));
        map.put("outOfRange2", Integer.valueOf(2));
        map.put("outOfRange10", Integer.valueOf(10));

        final CSVRecord record = new CSVRecord(new String[]{"val0", "val1"}, map, null, 1L);

        assertTrue(record.isSet("inRange0"));
        assertTrue(record.isSet("inRange1"));
        assertFalse(record.isSet("outOfRange2"));
        assertFalse(record.isSet("outOfRange10"));
        assertFalse(record.isSet("notMapped"));
    }

    @Test(timeout = 4000)
    public void testIsSetWithNullMapping() {
        final CSVRecord record = new CSVRecord(new String[]{"val"}, null, null, 1L);
        assertFalse(record.isSet("anyColumn"));
    }

    // ================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // ================================================================================================

    /**
     * Defects4J Target: org.apache.commons.csv.CSVRecordTest::testGetStringInconsistentRecord
     * When a column header exists in mapping with index >= values.length, get(String) must throw
     * IllegalArgumentException indicating record inconsistency, not ArrayIndexOutOfBoundsException.
     */
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetStringInconsistentRecord() {
        final Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("colA", Integer.valueOf(0));
        map.put("colB", Integer.valueOf(1));
        map.put("colC", Integer.valueOf(2));

        // Record only has 2 values, but "colC" maps to index 2
        final CSVRecord record = new CSVRecord(new String[]{"valA", "valB"}, map, "note", 1L);
        record.get("colC");
    }

    // ================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================================================

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testGetByNameWithNullMappingThrowsIllegalStateException() {
        final CSVRecord record = new CSVRecord(new String[]{"a", "b"}, null, null, 1L);
        record.get("anyColumn");
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetByIndexNegativeThrowsArrayIndexOutOfBoundsException() {
        final CSVRecord record = new CSVRecord(new String[]{"a", "b"}, null, null, 1L);
        record.get(-1);
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class, timeout = 4000)
    public void testGetByIndexBeyondLengthThrowsArrayIndexOutOfBoundsException() {
        final CSVRecord record = new CSVRecord(new String[]{"a", "b"}, null, null, 1L);
        record.get(2);
    }

    @Test(timeout = 4000)
    public void testGetByNameUnmappedReturnsNull() {
        final Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("existing", Integer.valueOf(0));

        final CSVRecord record = new CSVRecord(new String[]{"value"}, map, null, 1L);
        assertNull(record.get("nonExisting"));
    }

    // ================================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ================================================================================================

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        final Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("name", Integer.valueOf(0));
        map.put("age", Integer.valueOf(1));

        final CSVRecord original = new CSVRecord(
                new String[]{"Alice", "30"},
                map,
                "Serializable record",
                42L
        );

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.flush();
        oos.close();

        final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final Object deserializedObj = ois.readObject();
        ois.close();

        assertNotNull(deserializedObj);
        assertTrue(deserializedObj instanceof CSVRecord);

        final CSVRecord restored = (CSVRecord) deserializedObj;
        assertEquals(original.getRecordNumber(), restored.getRecordNumber());
        assertEquals(original.getComment(), restored.getComment());
        assertEquals(original.size(), restored.size());
        assertEquals(original.get(0), restored.get(0));
        assertEquals(original.get(1), restored.get(1));
        assertEquals(original.get("name"), restored.get("name"));
        assertEquals(original.get("age"), restored.get("age"));
        assertEquals(original.isConsistent(), restored.isConsistent());
        assertEquals(original.toString(), restored.toString());
    }
}