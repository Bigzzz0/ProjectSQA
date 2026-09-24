package com.fasterxml.jackson.databind;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Test;

import com.fasterxml.jackson.core.FormatSchema;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Partition A: Core Functional Logic & State Transitions
 *   - Iteration through sequences of root values (readValues) via hasNextValue/nextValue.
 *   - Iteration through wrapped array values via hasNext/next.
 *   - Updating existing instances via readerForUpdating (_updatedValue != null branch).
 *   - Direct next/nextValue call without preceding hasNext/hasNextValue (_hasNextChecked == false branch).
 *   - Accessors: getParser(), getCurrentLocation(), getParserSchema().
 *   - Aggregation: readAll(), readAll(List), readAll(Collection).
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - EMPTY_ITERATOR via emptyIterator(): hasNext()==false, hasNextValue()==false, readAll() empty,
 *     next()/nextValue() throw NoSuchElementException, getParser()==null, close() safe no-op.
 *   - Empty JSON Array "[]": Immediate termination, parser closed when managed.
 *   - Empty JSON document "" / whitespace: Immediate termination.
 *   - Single element input.
 *   - Repeated calls to hasNextValue() / hasNext() (idempotency check without token advancement).
 *   - Calling nextValue() / next() after sequence exhaustion -> NoSuchElementException.
 *
 * Partition C: Defect-Targeted Branch Zone (Jackson seq. ReadRecoveryTest)
 *   - Defect target: Resynchronization / recovery when deserialization fails on an intermediate element
 *     in a sequence (both root-level sequence and array-wrapped sequence).
 *   - testSimpleRootRecovery: Read Bean sequence "{\"a\":3} 1 {\"a\":7}", expect failure on 2nd value,
 *     and verify successful recovery and retrieval of 3rd value.
 *   - testSimpleArrayRecovery: Read Bean array "[{\"a\":3}, 1, {\"a\":7}]", expect failure on 2nd value,
 *     and verify successful recovery to read remaining elements.
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - remove() throws UnsupportedOperationException.
 *   - hasNext() / next() wrapping JsonMappingException into RuntimeJsonMappingException.
 *   - hasNext() / next() wrapping low-level IOException / JsonParseException into RuntimeException.
 *   - Parser management: managedParser=true closes JsonParser on EOF/END_ARRAY; managedParser=false
 *     leaves underlying caller-provided JsonParser open.
 *
 * Partition E: Object Lifecycle & Explicit Closing
 *   - Calling close() on MappingIterator explicitly closes underlying parser.
 *   - Calling nextValue() after explicit close() throws NoSuchElementException.
 * ----------------------------------------------------------------------------------------------------
 */
public class MappingIteratorGeminiTest {

    public static class Bean {
        public int a;
        public String b;

        public Bean() { }

        public Bean(int a, String b) {
            this.a = a;
            this.b = b;
        }
    }

    private final ObjectMapper mapper = new ObjectMapper();

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardIterationWithHasNextAndNext() throws Exception {
        MappingIterator<Integer> it = mapper.readerFor(Integer.class).readValues("[10, 20, 30]");

        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(10), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(20), it.next());
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(30), it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testDirectNextWithoutHasNext() throws Exception {
        MappingIterator<Integer> it = mapper.readerFor(Integer.class).readValues("100 200");

        assertEquals(Integer.valueOf(100), it.nextValue());
        assertEquals(Integer.valueOf(200), it.nextValue());
        assertFalse(it.hasNextValue());
    }

    @Test(timeout = 4000)
    public void testDirectNextIteratorContractWithoutHasNext() throws Exception {
        MappingIterator<String> it = mapper.readerFor(String.class).readValues("\"foo\" \"bar\"");

        assertEquals("foo", it.next());
        assertEquals("bar", it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testValueToUpdateFlow() throws Exception {
        Bean target = new Bean(1, "initial");
        MappingIterator<Bean> it = mapper.readerForUpdating(target).readValues("{\"a\":10} {\"b\":\"updated\"}");

        assertTrue(it.hasNextValue());
        Bean b1 = it.nextValue();
        assertSame("Updated instance must be the exact same instance passed in", target, b1);
        assertEquals(10, target.a);
        assertEquals("initial", target.b);

        assertTrue(it.hasNextValue());
        Bean b2 = it.nextValue();
        assertSame(target, b2);
        assertEquals(10, target.a);
        assertEquals("updated", target.b);

        assertFalse(it.hasNextValue());
    }

    @Test(timeout = 4000)
    public void testReadAllDefault() throws Exception {
        MappingIterator<Integer> it = mapper.readerFor(Integer.class).readValues("[1, 2, 3, 4]");
        List<Integer> list = it.readAll();

        assertNotNull(list);
        assertEquals(Arrays.asList(1, 2, 3, 4), list);
        assertFalse(it.hasNextValue());
    }

    @Test(timeout = 4000)
    public void testReadAllCustomList() throws Exception {
        MappingIterator<String> it = mapper.readerFor(String.class).readValues("[\"a\", \"b\"]");
        LinkedList<String> targetList = new LinkedList<String>();
        targetList.add("initial");

        LinkedList<String> result = it.readAll(targetList);
        assertSame(targetList, result);
        assertEquals(Arrays.asList("initial", "a", "b"), result);
    }

    @Test(timeout = 4000)
    public void testReadAllCustomCollection() throws Exception {
        MappingIterator<Integer> it = mapper.readerFor(Integer.class).readValues("[10, 20, 10]");
        Set<Integer> targetSet = new HashSet<Integer>();

        Set<Integer> result = it.readAll(targetSet);
        assertSame(targetSet, result);
        assertEquals(2, result.size());
        assertTrue(result.contains(10));
        assertTrue(result.contains(20));
    }

    @Test(timeout = 4000)
    public void testAccessorsAndLocation() throws Exception {
        MappingIterator<Integer> it = mapper.readerFor(Integer.class).readValues("  123");

        JsonParser parser = it.getParser();
        assertNotNull(parser);
        JsonLocation loc = it.getCurrentLocation();
        assertNotNull(loc);
        FormatSchema schema = it.getParserSchema();
        assertNull(schema); // Standard JSON parser does not use schema by default

        assertEquals(Integer.valueOf(123), it.nextValue());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyIteratorContract() throws Exception {
        MappingIterator<Object> empty = MappingIterator.emptyIterator();

        assertFalse(empty.hasNext());
        assertFalse(empty.hasNextValue());
        assertNull(empty.getParser());
        assertTrue(empty.readAll().isEmpty());
        assertTrue(empty.readAll(new ArrayList<Object>()).isEmpty());
        assertTrue(empty.readAll(new HashSet<Object>()).isEmpty());

        empty.close(); // No-op, should not fail

        try {
            empty.next();
            fail("Expected NoSuchElementException on emptyIterator().next()");
        } catch (NoSuchElementException expected) {
            assertNotNull(expected);
        }

        try {
            empty.nextValue();
            fail("Expected NoSuchElementException on emptyIterator().nextValue()");
        } catch (NoSuchElementException expected) {
            assertNotNull(expected);
        }
    }

    @Test(timeout = 4000)
    public void testEmptyArray() throws Exception {
        MappingIterator<Object> it = mapper.readerFor(Object.class).readValues("[]");

        assertFalse(it.hasNextValue());
        assertFalse(it.hasNext());
        assertNull(it.getParser());

        try {
            it.nextValue();
            fail("Expected NoSuchElementException when empty array is exhausted");
        } catch (NoSuchElementException expected) {
            assertNotNull(expected);
        }
    }

    @Test(timeout = 4000)
    public void testEmptyAndWhitespaceInput() throws Exception {
        MappingIterator<String> it = mapper.readerFor(String.class).readValues("   ");

        assertFalse(it.hasNextValue());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testHasNextIdempotence() throws Exception {
        MappingIterator<Integer> it = mapper.readerFor(Integer.class).readValues("42");

        assertTrue(it.hasNextValue());
        assertTrue(it.hasNextValue());
        assertTrue(it.hasNext());
        assertTrue(it.hasNext());

        assertEquals(Integer.valueOf(42), it.nextValue());

        assertFalse(it.hasNextValue());
        assertFalse(it.hasNextValue());
        assertFalse(it.hasNext());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testNextExhaustionThrowsNoSuchElementException() throws Exception {
        MappingIterator<Integer> it = mapper.readerFor(Integer.class).readValues("1");

        assertEquals(Integer.valueOf(1), it.next());
        assertFalse(it.hasNext());

        try {
            it.next();
            fail("Expected NoSuchElementException after iterator exhausted");
        } catch (NoSuchElementException expected) {
            assertNotNull(expected);
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Seq Read Recovery)
    // =========================================================================

    /**
     * Targets Defects4J known issue: ReadRecoveryTest::testSimpleRootRecovery
     * When reading a sequence of root-level objects, if one element causes a
     * JsonMappingException, the iterator must allow continuing to read subsequent
     * valid elements.
     */
    @Test(timeout = 4000)
    public void testSimpleRootRecovery() throws Exception {
        String json = "{\"a\":3} 1 {\"a\":7}";
        MappingIterator<Bean> it = mapper.readerFor(Bean.class).readValues(json);

        assertTrue(it.hasNextValue());
        Bean b1 = it.nextValue();
        assertEquals(3, b1.a);

        try {
            it.nextValue();
            fail("Expected JsonMappingException deserializing scalar 1 into Bean");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }

        assertTrue("Iterator should recover to find next root bean", it.hasNextValue());
        Bean b2 = it.nextValue();
        assertEquals(7, b2.a);
        assertFalse(it.hasNextValue());
    }

    /**
     * Targets Defects4J known issue: ReadRecoveryTest::testSimpleArrayRecovery
     * When reading elements from an array, if an element fails deserialization,
     * the iterator should clear/advance and be able to read subsequent valid elements.
     */
    @Test(timeout = 4000)
    public void testSimpleArrayRecovery() throws Exception {
        String json = "[{\"a\":3}, 1, {\"a\":7}]";
        MappingIterator<Bean> it = mapper.readerFor(Bean.class).readValues(json);

        assertTrue(it.hasNextValue());
        Bean b1 = it.nextValue();
        assertEquals(3, b1.a);

        try {
            it.nextValue();
            fail("Expected JsonMappingException deserializing array element 1 into Bean");
        } catch (JsonMappingException expected) {
            assertNotNull(expected.getMessage());
        }

        assertTrue("Iterator should recover to find next element in array", it.hasNextValue());
        Bean b2 = it.nextValue();
        assertEquals(7, b2.a);
        assertFalse(it.hasNextValue());
    }

    /**
     * Targets root bean sequence recovery under testRootBeans scenarios.
     */
    @Test(timeout = 4000)
    public void testRootBeansMultipleRecoveries() throws Exception {
        String json = "{\"a\":1} true {\"a\":2} \"invalid\" {\"a\":3}";
        MappingIterator<Bean> it = mapper.readerFor(Bean.class).readValues(json);

        assertTrue(it.hasNextValue());
        assertEquals(1, it.nextValue().a);

        try {
            it.nextValue();
            fail("Expected failure on boolean value");
        } catch (JsonMappingException expected) {
            // Expected failure
        }

        assertTrue(it.hasNextValue());
        assertEquals(2, it.nextValue().a);

        try {
            it.nextValue();
            fail("Expected failure on string value");
        } catch (JsonMappingException expected) {
            // Expected failure
        }

        assertTrue(it.hasNextValue());
        assertEquals(3, it.nextValue().a);
        assertFalse(it.hasNextValue());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testRemoveThrowsUnsupportedOperationException() throws Exception {
        MappingIterator<Integer> it = mapper.readerFor(Integer.class).readValues("1 2");
        it.next();
        it.remove();
    }

    @Test(timeout = 4000)
    public void testHasNextWrapsJsonMappingException() throws Exception {
        // Construct an input where hasNext triggers JsonMappingException
        MappingIterator<Bean> it = mapper.readerFor(Bean.class).readValues("123");
        try {
            it.next();
            fail("Expected RuntimeJsonMappingException");
        } catch (RuntimeJsonMappingException expected) {
            assertTrue(expected.getCause() instanceof JsonMappingException);
        }
    }

    @Test(timeout = 4000)
    public void testNextWrapsIOException() throws Exception {
        // Malformed JSON syntax to trigger low-level JsonParseException (IOException)
        MappingIterator<Object> it = mapper.readerFor(Object.class).readValues("{\"invalid\": ");
        try {
            it.next();
            fail("Expected RuntimeException wrapping IOException");
        } catch (RuntimeException expected) {
            assertFalse(expected instanceof RuntimeJsonMappingException);
            assertTrue(expected.getCause() instanceof IOException);
        }
    }

    @Test(timeout = 4000)
    public void testUnmanagedParserNotClosedByIterator() throws Exception {
        JsonParser parser = mapper.getFactory().createParser("[10, 20]");
        // When passing parser directly, managedParser is false
        MappingIterator<Integer> it = mapper.readerFor(Integer.class).readValues(parser);

        assertTrue(it.hasNextValue());
        assertEquals(Integer.valueOf(10), it.nextValue());
        assertTrue(it.hasNextValue());
        assertEquals(Integer.valueOf(20), it.nextValue());
        assertFalse(it.hasNextValue());

        assertFalse("Unmanaged parser should not be closed automatically by MappingIterator", parser.isClosed());
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testManagedParserClosedByIterator() throws Exception {
        MappingIterator<Integer> it = mapper.readerFor(Integer.class).readValues("[1, 2]");
        JsonParser parser = it.getParser();
        assertNotNull(parser);
        assertFalse(parser.isClosed());

        it.readAll(); // Exhaust iterator
        assertTrue("Managed parser must be closed upon exhaustion", parser.isClosed());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Explicit Closing
    // =========================================================================

    @Test(timeout = 4000)
    public void testExplicitCloseClosesParser() throws Exception {
        MappingIterator<Integer> it = mapper.readerFor(Integer.class).readValues("1 2 3");
        JsonParser parser = it.getParser();
        assertNotNull(parser);
        assertFalse(parser.isClosed());

        assertEquals(Integer.valueOf(1), it.nextValue());
        it.close();

        assertTrue("Parser should be closed after it.close()", parser.isClosed());
        assertFalse("hasNextValue should return false after close()", it.hasNextValue());

        try {
            it.nextValue();
            fail("Expected NoSuchElementException after close");
        } catch (NoSuchElementException expected) {
            assertNotNull(expected);
        }
    }

    @Test(timeout = 4000)
    public void testCloseMultipleTimesIsSafe() throws Exception {
        MappingIterator<Integer> it = mapper.readerFor(Integer.class).readValues("[1]");
        it.close();
        it.close();
        assertFalse(it.hasNext());
    }
}