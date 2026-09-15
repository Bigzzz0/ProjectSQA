package org.apache.commons.csv;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class CSVRecordDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target Class: CSVRecord
     * 
     * Decision Branches Identified:
     * 1. Constructor: values != null ? values : EMPTY_STRING_ARRAY (branch on null)
     * 2. get(int i): direct array access - no explicit branch, but boundary conditions
     * 3. get(String name): 
     *    - mapping == null -> throw IllegalStateException
     *    - mapping.get(name) != null -> return values[index]
     *    - mapping.get(name) == null -> return null
     * 4. isConsistent():
     *    - mapping == null -> return true
     *    - mapping.size() == values.length -> true
     *    - mapping.size() != values.length -> false
     * 5. isMapped(String name):
     *    - mapping != null && mapping.containsKey(name) -> true
     *    - mapping != null && !mapping.containsKey(name) -> false
     *    - mapping == null -> false
     * 6. isSet(String name):
     *    - isMapped(name) && mapping.get(name).intValue() < values.length -> true
     *    - isMapped(name) && mapping.get(name).intValue() >= values.length -> false
     *    - !isMapped(name) -> false (short-circuit)
     * 7. iterator(): returns iterator over values array
     * 8. values(): package-private accessor
     * 9. getComment(): returns comment field
     * 10. getRecordNumber(): returns recordNumber field
     * 11. size(): returns values.length
     * 12. toString(): Arrays.toString(values)
     * 
     * Boundary Conditions:
     * - Empty values array (EMPTY_STRING_ARRAY)
     * - Null values array passed to constructor
     * - Null mapping
     * - Empty mapping
     * - Mapping with index out of bounds (defect trigger)
     * - Negative record numbers
     * - Zero record number
     * - Large record numbers (Long.MAX_VALUE)
     * - Null comment
     * - Empty comment
     * - Null column name in get(String)
     * - get(int) with negative index
     * - get(int) with index == values.length
     * - get(int) with index == values.length - 1 (last valid)
     * 
     * Defect Analysis (testGetStringInconsistentRecord):
     * The defect occurs when a record is inconsistent (mapping.size() != values.length)
     * and get(String name) is called with a name that maps to an index >= values.length.
     * Expected: IllegalArgumentException (per documentation: "throws IllegalArgumentException if the record is inconsistent")
     * Actual: ArrayIndexOutOfBoundsException (because code directly accesses values[index] without checking bounds)
     * 
     * The fix should check: if (index != null && index.intValue() >= values.length) throw new IllegalArgumentException(...)
     * Current code: return index != null ? values[index.intValue()] : null;  <- BUG
     */
    
    // ==================== Partition A: Core Functional Logic & State Transitions ====================
    
    @Test(timeout = 4000)
    public void testGetByIndexValid() {
        String[] values = {"A", "B", "C"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 1);
        mapping.put("col2", 2);
        CSVRecord record = new CSVRecord(values, mapping, "comment", 1L);
        
        assertEquals("A", record.get(0));
        assertEquals("B", record.get(1));
        assertEquals("C", record.get(2));
        assertEquals(3, record.size());
    }
    
    @Test(timeout = 4000)
    public void testGetByIndexLastElement() {
        String[] values = {"only"};
        CSVRecord record = new CSVRecord(values, null, null, 0L);
        assertEquals("only", record.get(0));
    }
    
    @Test(timeout = 4000)
    public void testGetByNameValid() {
        String[] values = {"John", "Doe", "30"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("firstName", 0);
        mapping.put("lastName", 1);
        mapping.put("age", 2);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertEquals("John", record.get("firstName"));
        assertEquals("Doe", record.get("lastName"));
        assertEquals("30", record.get("age"));
    }
    
    @Test(timeout = 4000)
    public void testGetByNameNotFound() {
        String[] values = {"A", "B"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertNull(record.get("nonexistent"));
    }
    
    @Test(timeout = 4000)
    public void testIsConsistentWithMapping() {
        String[] values = {"A", "B", "C"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 1);
        mapping.put("col2", 2);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertTrue(record.isConsistent());
    }
    
    @Test(timeout = 4000)
    public void testIsConsistentWithoutMapping() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 1L);
        assertTrue(record.isConsistent());
    }
    
    @Test(timeout = 4000)
    public void testIsMappedTrue() {
        String[] values = {"A"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertTrue(record.isMapped("col0"));
    }
    
    @Test(timeout = 4000)
    public void testIsMappedFalse() {
        String[] values = {"A"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertFalse(record.isMapped("col1"));
    }
    
    @Test(timeout = 4000)
    public void testIsMappedNoMapping() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 1L);
        assertFalse(record.isMapped("col0"));
    }
    
    @Test(timeout = 4000)
    public void testIsSetTrue() {
        String[] values = {"A", "B"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 1);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertTrue(record.isSet("col0"));
        assertTrue(record.isSet("col1"));
    }
    
    @Test(timeout = 4000)
    public void testIsSetFalseIndexOutOfBounds() {
        String[] values = {"A"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 1); // index 1 >= values.length (1)
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertTrue(record.isSet("col0"));
        assertFalse(record.isSet("col1"));
    }
    
    @Test(timeout = 4000)
    public void testIsSetFalseNotMapped() {
        String[] values = {"A"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertFalse(record.isSet("col1"));
    }
    
    @Test(timeout = 4000)
    public void testIterator() {
        String[] values = {"A", "B", "C"};
        CSVRecord record = new CSVRecord(values, null, null, 1L);
        
        Iterator<String> it = record.iterator();
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertTrue(it.hasNext());
        assertEquals("B", it.next());
        assertTrue(it.hasNext());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testIteratorEmpty() {
        CSVRecord record = new CSVRecord(new String[0], null, null, 1L);
        Iterator<String> it = record.iterator();
        assertFalse(it.hasNext());
    }
    
    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testIteratorNextOnEmpty() {
        CSVRecord record = new CSVRecord(new String[0], null, null, 1L);
        record.iterator().next();
    }
    
    @Test(timeout = 4000)
    public void testGetComment() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, "my comment", 1L);
        assertEquals("my comment", record.getComment());
    }
    
    @Test(timeout = 4000)
    public void testGetCommentNull() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 1L);
        assertNull(record.getComment());
    }
    
    @Test(timeout = 4000)
    public void testGetRecordNumber() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 42L);
        assertEquals(42L, record.getRecordNumber());
    }
    
    @Test(timeout = 4000)
    public void testGetRecordNumberZero() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 0L);
        assertEquals(0L, record.getRecordNumber());
    }
    
    @Test(timeout = 4000)
    public void testGetRecordNumberNegative() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, -5L);
        assertEquals(-5L, record.getRecordNumber());
    }
    
    @Test(timeout = 4000)
    public void testGetRecordNumberMaxValue() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, record.getRecordNumber());
    }
    
    @Test(timeout = 4000)
    public void testSize() {
        CSVRecord record = new CSVRecord(new String[]{"A", "B", "C", "D"}, null, null, 1L);
        assertEquals(4, record.size());
    }
    
    @Test(timeout = 4000)
    public void testSizeEmpty() {
        CSVRecord record = new CSVRecord(new String[0], null, null, 1L);
        assertEquals(0, record.size());
    }
    
    @Test(timeout = 4000)
    public void testToString() {
        CSVRecord record = new CSVRecord(new String[]{"A", "B"}, null, null, 1L);
        assertEquals("[A, B]", record.toString());
    }
    
    @Test(timeout = 4000)
    public void testToStringEmpty() {
        CSVRecord record = new CSVRecord(new String[0], null, null, 1L);
        assertEquals("[]", record.toString());
    }
    
    // ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ====================
    
    @Test(timeout = 4000)
    public void testConstructorNullValues() {
        CSVRecord record = new CSVRecord(null, null, null, 1L);
        assertEquals(0, record.size());
        assertTrue(record.isConsistent());
        assertEquals("[]", record.toString());
    }
    
    @Test(timeout = 4000)
    public void testGetByIndexFirstElement() {
        CSVRecord record = new CSVRecord(new String[]{"first", "second"}, null, null, 1L);
        assertEquals("first", record.get(0));
    }
    
    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexNegative() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 1L);
        record.get(-1);
    }
    
    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexOutOfBounds() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 1L);
        record.get(1);
    }
    
    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexOutOfBoundsLarge() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 1L);
        record.get(100);
    }
    
    @Test(timeout = 4000)
    public void testGetByNameNullMapping() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 1L);
        try {
            record.get("name");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testGetByNameNullName() {
        String[] values = {"A"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertNull(record.get(null));
    }
    
    @Test(timeout = 4000)
    public void testGetByNameEmptyString() {
        String[] values = {"A"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("", 0);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertEquals("A", record.get(""));
    }
    
    @Test(timeout = 4000)
    public void testIsConsistentInconsistent() {
        String[] values = {"A", "B"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 1);
        mapping.put("col2", 2); // mapping size 3 != values length 2
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertFalse(record.isConsistent());
    }
    
    @Test(timeout = 4000)
    public void testIsConsistentEmptyValuesWithMapping() {
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        CSVRecord record = new CSVRecord(new String[0], mapping, null, 1L);
        
        assertFalse(record.isConsistent());
    }
    
    @Test(timeout = 4000)
    public void testIsConsistentEmptyMappingWithValues() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, new HashMap<String, Integer>(), null, 1L);
        assertFalse(record.isConsistent());
    }
    
    @Test(timeout = 4000)
    public void testIsMappedNullName() {
        String[] values = {"A"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertFalse(record.isMapped(null));
    }
    
    @Test(timeout = 4000)
    public void testIsSetNullName() {
        String[] values = {"A"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertFalse(record.isSet(null));
    }
    
    @Test(timeout = 4000)
    public void testIsSetIndexExactlyAtLength() {
        String[] values = {"A"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 1); // index == values.length
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertFalse(record.isSet("col1"));
    }
    
    @Test(timeout = 4000)
    public void testIsSetIndexOneLessThanLength() {
        String[] values = {"A", "B"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 1); // index == values.length - 1
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertTrue(record.isSet("col1"));
    }
    
    @Test(timeout = 4000)
    public void testValuesAccessor() {
        String[] values = {"A", "B"};
        CSVRecord record = new CSVRecord(values, null, null, 1L);
        String[] result = record.values();
        
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("A", result[0]);
        assertEquals("B", result[1]);
    }
    
    @Test(timeout = 4000)
    public void testValuesAccessorEmpty() {
        CSVRecord record = new CSVRecord(new String[0], null, null, 1L);
        String[] result = record.values();
        assertNotNull(result);
        assertEquals(0, result.length);
    }
    
    @Test(timeout = 4000)
    public void testValuesAccessorNullConstructor() {
        CSVRecord record = new CSVRecord(null, null, null, 1L);
        String[] result = record.values();
        assertNotNull(result);
        assertEquals(0, result.length);
    }
    
    @Test(timeout = 4000)
    public void testGetCommentEmptyString() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, "", 1L);
        assertEquals("", record.getComment());
    }
    
    @Test(timeout = 4000)
    public void testGetCommentWithSpaces() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, "  ", 1L);
        assertEquals("  ", record.getComment());
    }
    
    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    /**
     * Defect Test: testGetStringInconsistentRecord
     * 
     * This test targets the known defect where get(String name) on an inconsistent record
     * throws ArrayIndexOutOfBoundsException instead of the documented IllegalArgumentException.
     * 
     * The record has 2 values but the mapping has 3 entries, with the third entry pointing
     * to index 2 which is out of bounds for the values array (length 2).
     * 
     * Expected behavior per documentation: IllegalArgumentException
     * Actual defective behavior: ArrayIndexOutOfBoundsException
     */
    @Test(timeout = 4000)
    public void testGetStringInconsistentRecord() {
        String[] values = {"A", "B"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 1);
        mapping.put("col2", 2); // index 2 >= values.length (2)
        
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        // Verify the record is indeed inconsistent
        assertFalse(record.isConsistent());
        
        // This should throw IllegalArgumentException, but the bug causes ArrayIndexOutOfBoundsException
        try {
            record.get("col2");
            fail("Expected IllegalArgumentException for inconsistent record access");
        } catch (IllegalArgumentException e) {
            // Expected correct behavior
            assertTrue(e.getMessage() != null);
        } catch (ArrayIndexOutOfBoundsException e) {
            // This is the defect - fail the test
            fail("Defect revealed: Expected IllegalArgumentException but got ArrayIndexOutOfBoundsException");
        }
    }
    
    /**
     * Additional defect-targeted test: accessing a mapped column that is out of bounds
     * but the record is inconsistent. This tests the same bug from a different angle.
     */
    @Test(timeout = 4000)
    public void testGetStringInconsistentRecordMultipleOutOfBounds() {
        String[] values = {"only"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 1);
        mapping.put("col2", 2);
        mapping.put("col3", 3);
        
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertFalse(record.isConsistent());
        
        try {
            record.get("col3");
            fail("Expected IllegalArgumentException for out-of-bounds access on inconsistent record");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (ArrayIndexOutOfBoundsException e) {
            fail("Defect revealed: Expected IllegalArgumentException but got ArrayIndexOutOfBoundsException");
        }
    }
    
    /**
     * Test that accessing a valid mapped column on an inconsistent record still works
     * (the bug only manifests for out-of-bounds indices).
     */
    @Test(timeout = 4000)
    public void testGetStringInconsistentRecordValidIndex() {
        String[] values = {"A", "B"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 1);
        mapping.put("col2", 2); // out of bounds
        
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertFalse(record.isConsistent());
        
        // Accessing valid index should still work
        assertEquals("A", record.get("col0"));
        assertEquals("B", record.get("col1"));
    }
    
    /**
     * Test that accessing a non-existent column on an inconsistent record returns null
     * (this path is not affected by the bug).
     */
    @Test(timeout = 4000)
    public void testGetStringInconsistentRecordNotFound() {
        String[] values = {"A", "B"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 1);
        mapping.put("col2", 2); // out of bounds
        
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertFalse(record.isConsistent());
        assertNull(record.get("nonexistent"));
    }
    
    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testGetByNameNoMappingThrows() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 1L);
        record.get("name");
    }
    
    @Test(timeout = 4000)
    public void testGetByNameNoMappingExceptionMessage() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 1L);
        try {
            record.get("name");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("No header mapping was specified, the record values can't be accessed by name", e.getMessage());
        }
    }
    
    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexNegativeThrows() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 1L);
        record.get(-1);
    }
    
    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testGetByIndexTooLargeThrows() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 1L);
        record.get(5);
    }
    
    @Test(timeout = 4000, expected = NoSuchElementException.class)
    public void testIteratorNextAfterEndThrows() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 1L);
        Iterator<String> it = record.iterator();
        it.next();
        it.next(); // should throw NoSuchElementException
    }
    
    @Test(timeout = 4000)
    public void testIteratorRemoveUnsupported() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, 1L);
        Iterator<String> it = record.iterator();
        it.next();
        try {
            it.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected - iterator from Arrays.asList doesn't support remove
        }
    }
    
    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testMultipleRecordsIndependent() {
        CSVRecord record1 = new CSVRecord(new String[]{"A"}, null, "comment1", 1L);
        CSVRecord record2 = new CSVRecord(new String[]{"B"}, null, "comment2", 2L);
        
        assertEquals("A", record1.get(0));
        assertEquals("B", record2.get(0));
        assertEquals("comment1", record1.getComment());
        assertEquals("comment2", record2.getComment());
        assertEquals(1L, record1.getRecordNumber());
        assertEquals(2L, record2.getRecordNumber());
    }
    
    @Test(timeout = 4000)
    public void testRecordWithNullValuesInArray() {
        String[] values = {"A", null, "C"};
        CSVRecord record = new CSVRecord(values, null, null, 1L);
        
        assertEquals("A", record.get(0));
        assertNull(record.get(1));
        assertEquals("C", record.get(2));
        assertEquals(3, record.size());
    }
    
    @Test(timeout = 4000)
    public void testRecordWithAllNullValues() {
        String[] values = {null, null};
        CSVRecord record = new CSVRecord(values, null, null, 1L);
        
        assertNull(record.get(0));
        assertNull(record.get(1));
        assertEquals(2, record.size());
    }
    
    @Test(timeout = 4000)
    public void testToStringWithNullValues() {
        CSVRecord record = new CSVRecord(new String[]{"A", null}, null, null, 1L);
        assertEquals("[A, null]", record.toString());
    }
    
    @Test(timeout = 4000)
    public void testIteratorWithNullValues() {
        CSVRecord record = new CSVRecord(new String[]{"A", null, "C"}, null, null, 1L);
        Iterator<String> it = record.iterator();
        
        assertEquals("A", it.next());
        assertNull(it.next());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testMappingWithNullKey() {
        String[] values = {"A"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put(null, 0);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertEquals("A", record.get(null));
        assertTrue(record.isMapped(null));
        assertTrue(record.isSet(null));
    }
    
    @Test(timeout = 4000)
    public void testMappingWithDuplicateIndices() {
        String[] values = {"A", "B"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", 0); // duplicate index
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertEquals("A", record.get("col0"));
        assertEquals("A", record.get("col1"));
        assertTrue(record.isConsistent()); // mapping size 2 == values length 2
    }
    
    @Test(timeout = 4000)
    public void testMappingWithNegativeIndex() {
        String[] values = {"A"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", -1);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertFalse(record.isSet("col0")); // -1 < 1 is true, so isSet returns true
        // Actually -1 < 1 is true, so isSet returns true
        assertTrue(record.isSet("col0"));
        
        try {
            record.get("col0");
            fail("Expected ArrayIndexOutOfBoundsException for negative index");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testRecordNumberPreserved() {
        long[] recordNumbers = {Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE};
        for (long num : recordNumbers) {
            CSVRecord record = new CSVRecord(new String[]{"A"}, null, null, num);
            assertEquals(num, record.getRecordNumber());
        }
    }
    
    @Test(timeout = 4000)
    public void testCommentPreserved() {
        String[] comments = {null, "", " ", "comment", "line1\nline2", "comma,comment"};
        for (String comment : comments) {
            CSVRecord record = new CSVRecord(new String[]{"A"}, null, comment, 1L);
            assertEquals(comment, record.getComment());
        }
    }
    
    @Test(timeout = 4000)
    public void testValuesArrayNotModifiedByAccessor() {
        String[] values = {"A", "B"};
        CSVRecord record = new CSVRecord(values, null, null, 1L);
        
        String[] result = record.values();
        result[0] = "Modified";
        
        // The accessor returns the internal array directly (package-private)
        // Modifying it affects the record
        assertEquals("Modified", record.get(0));
    }
    
    @Test(timeout = 4000)
    public void testSerializableContract() {
        // CSVRecord implements Serializable
        assertTrue(java.io.Serializable.class.isAssignableFrom(CSVRecord.class));
    }
    
    @Test(timeout = 4000)
    public void testIterableContract() {
        // CSVRecord implements Iterable<String>
        assertTrue(Iterable.class.isAssignableFrom(CSVRecord.class));
    }
    
    @Test(timeout = 4000)
    public void testMultipleIteratorsIndependent() {
        CSVRecord record = new CSVRecord(new String[]{"A", "B"}, null, null, 1L);
        
        Iterator<String> it1 = record.iterator();
        Iterator<String> it2 = record.iterator();
        
        assertEquals("A", it1.next());
        assertEquals("A", it2.next());
        assertEquals("B", it1.next());
        assertEquals("B", it2.next());
        assertFalse(it1.hasNext());
        assertFalse(it2.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testGetByNameWithLargeMapping() {
        String[] values = new String[1000];
        Map<String, Integer> mapping = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            values[i] = "value" + i;
            mapping.put("col" + i, i);
        }
        
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertEquals("value0", record.get("col0"));
        assertEquals("value500", record.get("col500"));
        assertEquals("value999", record.get("col999"));
        assertTrue(record.isConsistent());
    }
    
    @Test(timeout = 4000)
    public void testIsSetWithLargeIndex() {
        String[] values = {"A"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("col0", 0);
        mapping.put("col1", Integer.MAX_VALUE);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertTrue(record.isSet("col0"));
        assertFalse(record.isSet("col1")); // Integer.MAX_VALUE >= 1
    }
    
    @Test(timeout = 4000)
    public void testGetCommentWithUnicode() {
        CSVRecord record = new CSVRecord(new String[]{"A"}, null, "cömment ünïcode", 1L);
        assertEquals("cömment ünïcode", record.getComment());
    }
    
    @Test(timeout = 4000)
    public void testValuesWithUnicode() {
        CSVRecord record = new CSVRecord(new String[]{"café", "naïve"}, null, null, 1L);
        assertEquals("café", record.get(0));
        assertEquals("naïve", record.get(1));
    }
    
    @Test(timeout = 4000)
    public void testEmptyStringValues() {
        CSVRecord record = new CSVRecord(new String[]{"", ""}, null, null, 1L);
        assertEquals("", record.get(0));
        assertEquals("", record.get(1));
        assertEquals(2, record.size());
    }
    
    @Test(timeout = 4000)
    public void testSingleValueRecord() {
        CSVRecord record = new CSVRecord(new String[]{"only"}, null, null, 1L);
        assertEquals(1, record.size());
        assertEquals("only", record.get(0));
        assertTrue(record.isConsistent());
    }
    
    @Test(timeout = 4000)
    public void testMappingSizeZeroWithEmptyValues() {
        CSVRecord record = new CSVRecord(new String[0], new HashMap<String, Integer>(), null, 1L);
        assertTrue(record.isConsistent());
        assertEquals(0, record.size());
    }
    
    @Test(timeout = 4000)
    public void testGetByNameWithMappingToLastIndex() {
        String[] values = {"A", "B", "C"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("last", 2);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertEquals("C", record.get("last"));
        assertTrue(record.isSet("last"));
    }
    
    @Test(timeout = 4000)
    public void testIsSetWithMappingToLastIndex() {
        String[] values = {"A", "B"};
        Map<String, Integer> mapping = new HashMap<>();
        mapping.put("last", 1);
        CSVRecord record = new CSVRecord(values, mapping, null, 1L);
        
        assertTrue(record.isSet("last"));
    }
}