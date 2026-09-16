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

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.csv.CSVRecord
 *
 * ---------------------------------------------------------------------------------------------------------------------
 * Branch / Condition Coverage:
 * 1. Constructor:
 *    - values != null -> uses values
 *    - values == null -> uses EMPTY_STRING_ARRAY
 * 2. get(Enum<?>):
 *    - delegates to get(e.toString()), verifies proper name resolution
 * 3. get(int):
 *    - 0 <= index < values.length -> returns value
 *    - index < 0 or index >= values.length -> throws ArrayIndexOutOfBoundsException
 * 4. get(String):
 *    - mapping == null -> throws IllegalStateException
 *    - mapping != null && index == null -> throws IllegalArgumentException (not found)
 *    - mapping != null && index >= values.length -> throws IllegalArgumentException (AIOOBE wrapped)
 *    - mapping != null && index < values.length -> returns value
 * 5. isConsistent():
 *    - mapping == null -> true
 *    - mapping != null && mapping.size() == values.length -> true
 *    - mapping != null && mapping.size() != values.length -> false
 * 6. isMapped(String):
 *    - mapping == null -> false
 *    - mapping != null && mapping.containsKey(name) -> true
 *    - mapping != null && !mapping.containsKey(name) -> false
 * 7. isSet(String):
 *    - !isMapped(name) -> false
 *    - isMapped(name) && index >= values.length -> false (short record)
 *    - isMapped(name) && index < values.length -> true
 * 8. iterator():
 *    - Iterates over all elements; throws NoSuchElementException / UnsupportedOperationException on invalid calls
 * 9. putIn(Map) / toMap():
 *    - Populates all mapped columns into the map
 * 10. toString():
 *    - Returns Arrays.toString(values)
 *
 * ---------------------------------------------------------------------------------------------------------------------
 * Defect-Targeted Zone (Defects4J Known Bug):
 * - CSVRecordTest::testToMapWithShortRecord
 *   Bug: When values.length < mapping.size(), calling putIn() / toMap() evaluates `values[col]` without
 *   checking if `col < values.length`, causing java.lang.ArrayIndexOutOfBoundsException.
 *   Expected behavior: only mapped indices present in `values` are placed into the map, without throwing AIOOBE.
 * ---------------------------------------------------------------------------------------------------------------------
 */
public class CSVRecordGeminiTest {

    private enum HeaderEnum {
        COL_A, COL_B, COL_C, NOT_PRESENT
    }

    // =================================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =================================================================================================================

    @Test(timeout = 4000)
    public void testStandardRecordAccessors() {
        final String[] values = new String[]{"Alpha", "Beta", "Gamma"};
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("COL_A", 0);
        mapping.put("COL_B", 1);
        mapping.put("COL_C", 2);

        final CSVRecord record = new CSVRecord(values, mapping, "Sample Comment", 42L);

        assertEquals("Sample Comment", record.getComment());
        assertEquals(42L, record.getRecordNumber());
        assertEquals(3, record.size());
        assertEquals("[Alpha, Beta, Gamma]", record.toString());

        assertArrayEquals(values, record.values());

        assertEquals("Alpha", record.get(0));
        assertEquals("Beta", record.get(1));
        assertEquals("Gamma", record.get(2));

        assertEquals("Alpha", record.get("COL_A"));
        assertEquals("Beta", record.get("COL_B"));
        assertEquals("Gamma", record.get("COL_C"));

        assertEquals("Alpha", record.get(HeaderEnum.COL_A));
        assertEquals("Beta", record.get(HeaderEnum.COL_B));
        assertEquals("Gamma", record.get(HeaderEnum.COL_C));
    }

    @Test(timeout = 4000)
    public void testIsConsistentWhenExactMatch() {
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("A", 0);
        mapping.put("B", 1);

        final CSVRecord record = new CSVRecord(new String[]{"v1", "v2"}, mapping, null, 1L);
        assertTrue(record.isConsistent());
    }

    @Test(timeout = 4000)
    public void testIsMappedAndIsSet() {
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("first", 0);
        mapping.put("second", 1);
        mapping.put("missing_index", 5);

        final CSVRecord record = new CSVRecord(new String[]{"val1", "val2"}, mapping, null, 1L);

        assertTrue(record.isMapped("first"));
        assertTrue(record.isMapped("second"));
        assertTrue(record.isMapped("missing_index"));
        assertFalse(record.isMapped("unmapped"));

        assertTrue(record.isSet("first"));
        assertTrue(record.isSet("second"));
        assertFalse(record.isSet("missing_index"));
        assertFalse(record.isSet("unmapped"));
    }

    @Test(timeout = 4000)
    public void testToMapStandard() {
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("A", 0);
        mapping.put("B", 1);

        final CSVRecord record = new CSVRecord(new String[]{"valA", "valB"}, mapping, null, 1L);
        final Map<String, String> map = record.toMap();

        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("valA", map.get("A"));
        assertEquals("valB", map.get("B"));
    }

    @Test(timeout = 4000)
    public void testPutInCustomMap() {
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("K1", 0);
        mapping.put("K2", 1);

        final CSVRecord record = new CSVRecord(new String[]{"V1", "V2"}, mapping, null, 1L);
        final TreeMap<String, String> targetMap = new TreeMap<String, String>();
        final TreeMap<String, String> returnedMap = record.putIn(targetMap);

        assertSame(targetMap, returnedMap);
        assertEquals("V1", targetMap.get("K1"));
        assertEquals("V2", targetMap.get("K2"));
    }

    // =================================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =================================================================================================================

    @Test(timeout = 4000)
    public void testNullValuesInConstructorYieldsEmptyRecord() {
        final CSVRecord record = new CSVRecord(null, null, null, 0L);

        assertEquals(0, record.size());
        assertNotNull(record.values());
        assertEquals(0, record.values().length);
        assertNull(record.getComment());
        assertEquals(0L, record.getRecordNumber());
        assertEquals("[]", record.toString());
        assertTrue(record.isConsistent());
        assertFalse(record.isMapped("any"));
        assertFalse(record.isSet("any"));
    }

    @Test(timeout = 4000)
    public void testExtremeRecordNumbers() {
        final CSVRecord minRecord = new CSVRecord(new String[]{"min"}, null, null, Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, minRecord.getRecordNumber());

        final CSVRecord maxRecord = new CSVRecord(new String[]{"max"}, null, null, Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, maxRecord.getRecordNumber());

        final CSVRecord negRecord = new CSVRecord(new String[]{"neg"}, null, null, -1L);
        assertEquals(-1L, negRecord.getRecordNumber());
    }

    @Test(timeout = 4000)
    public void testConsistencyWithNullMapping() {
        final CSVRecord record = new CSVRecord(new String[]{"1", "2"}, null, null, 1L);
        assertTrue("When mapping is null, isConsistent must be true", record.isConsistent());
    }

    @Test(timeout = 4000)
    public void testConsistencySizeMismatch() {
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("A", 0);
        mapping.put("B", 1);
        mapping.put("C", 2);

        final CSVRecord shorterRecord = new CSVRecord(new String[]{"1", "2"}, mapping, null, 1L);
        assertFalse(shorterRecord.isConsistent());

        final CSVRecord longerRecord = new CSVRecord(new String[]{"1", "2", "3", "4"}, mapping, null, 1L);
        assertFalse(longerRecord.isConsistent());
    }

    @Test(timeout = 4000)
    public void testEmptyValuesWithEmptyMapping() {
        final Map<String, Integer> mapping = Collections.emptyMap();
        final CSVRecord record = new CSVRecord(new String[0], mapping, "", 0L);

        assertTrue(record.isConsistent());
        assertEquals(0, record.size());
        assertEquals("", record.getComment());
        assertTrue(record.toMap().isEmpty());
    }

    // =================================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =================================================================================================================

    /**
     * Targets: CSVRecordTest::testToMapWithShortRecord
     * Known Defect: In defective versions, putIn / toMap throws ArrayIndexOutOfBoundsException: 2
     * when values array length is less than mapping column index.
     */
    @Test(timeout = 4000)
    public void testToMapWithShortRecord() {
        final Map<String, Integer> headerMap = new HashMap<String, Integer>();
        headerMap.put("A", 0);
        headerMap.put("B", 1);
        headerMap.put("C", 2);

        final CSVRecord record = new CSVRecord(new String[]{"valA", "valB"}, headerMap, null, 1L);
        final Map<String, String> map = record.toMap();

        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("valA", map.get("A"));
        assertEquals("valB", map.get("B"));
        assertFalse("Key 'C' must not be in the map as record is short", map.containsKey("C"));
    }

    @Test(timeout = 4000)
    public void testPutInWithShortRecord() {
        final Map<String, Integer> headerMap = new HashMap<String, Integer>();
        headerMap.put("COL_0", 0);
        headerMap.put("COL_1", 1);
        headerMap.put("COL_OUT_OF_BOUNDS", 99);

        final CSVRecord record = new CSVRecord(new String[]{"v0", "v1"}, headerMap, null, 1L);
        final Map<String, String> destination = new HashMap<String, String>();

        record.putIn(destination);

        assertEquals(2, destination.size());
        assertEquals("v0", destination.get("COL_0"));
        assertEquals("v1", destination.get("COL_1"));
        assertNull(destination.get("COL_OUT_OF_BOUNDS"));
    }

    // =================================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =================================================================================================================

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testGetIntNegativeIndexThrows() {
        final CSVRecord record = new CSVRecord(new String[]{"a", "b"}, null, null, 1L);
        record.get(-1);
    }

    @Test(timeout = 4000, expected = ArrayIndexOutOfBoundsException.class)
    public void testGetIntOutOfBoundsThrows() {
        final CSVRecord record = new CSVRecord(new String[]{"a", "b"}, null, null, 1L);
        record.get(2);
    }

    @Test(timeout = 4000)
    public void testGetStringWithNullMappingThrowsIllegalStateException() {
        final CSVRecord record = new CSVRecord(new String[]{"a", "b"}, null, null, 1L);
        try {
            record.get("anyHeader");
            fail("Expected IllegalStateException when accessing column by name with null mapping");
        } catch (final IllegalStateException e) {
            assertTrue(e.getMessage().contains("No header mapping was specified"));
        }
    }

    @Test(timeout = 4000)
    public void testGetStringWithUnmappedNameThrowsIllegalArgumentException() {
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("mappedHeader", 0);

        final CSVRecord record = new CSVRecord(new String[]{"val"}, mapping, null, 1L);
        try {
            record.get("unmappedHeader");
            fail("Expected IllegalArgumentException for unmapped header");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Mapping for unmappedHeader not found"));
        }
    }

    @Test(timeout = 4000)
    public void testGetStringShortRecordThrowsIllegalArgumentException() {
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("present", 0);
        mapping.put("missing", 5);

        final CSVRecord record = new CSVRecord(new String[]{"val"}, mapping, null, 1L);

        assertEquals("val", record.get("present"));

        try {
            record.get("missing");
            fail("Expected IllegalArgumentException when mapping points beyond record size");
        } catch (final IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Index for header 'missing' is 5 but CSVRecord only has 1 values!"));
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetEnumNotPresentThrowsIllegalArgumentException() {
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("COL_A", 0);

        final CSVRecord record = new CSVRecord(new String[]{"valA"}, mapping, null, 1L);
        record.get(HeaderEnum.NOT_PRESENT);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testGetEnumNullThrowsNullPointerException() {
        final Map<String, Integer> mapping = new HashMap<String, Integer>();
        final CSVRecord record = new CSVRecord(new String[]{"val"}, mapping, null, 1L);
        record.get((Enum<?>) null);
    }

    // =================================================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =================================================================================================================

    @Test(timeout = 4000)
    public void testIteratorContract() {
        final String[] values = new String[]{"first", "second"};
        final CSVRecord record = new CSVRecord(values, null, null, 1L);

        final Iterator<String> it = record.iterator();
        assertNotNull(it);
        assertTrue(it.hasNext());
        assertEquals("first", it.next());
        assertTrue(it.hasNext());
        assertEquals("second", it.next());
        assertFalse(it.hasNext());

        try {
            it.next();
            fail("Expected NoSuchElementException after iterator is exhausted");
        } catch (final NoSuchElementException expected) {
            // Success
        }

        try {
            it.remove();
            fail("Expected UnsupportedOperationException on remove from record iterator");
        } catch (final UnsupportedOperationException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        final String[] values = new String[]{"Data1", "Data2"};
        final HashMap<String, Integer> mapping = new HashMap<String, Integer>();
        mapping.put("H1", 0);
        mapping.put("H2", 1);

        final CSVRecord original = new CSVRecord(values, mapping, "Comment", 999L);

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
        assertEquals(original.get(0), deserialized.get(0));
        assertEquals(original.get(1), deserialized.get(1));
        assertEquals(original.get("H1"), deserialized.get("H1"));
        assertEquals(original.get("H2"), deserialized.get("H2"));
        assertEquals(original.isConsistent(), deserialized.isConsistent());
        assertEquals(original.toString(), deserialized.toString());
    }
}