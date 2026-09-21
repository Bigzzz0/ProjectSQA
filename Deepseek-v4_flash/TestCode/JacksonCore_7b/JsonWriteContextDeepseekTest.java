package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Comprehensive JUnit 4 test suite for JsonWriteContext.
 * Targets line/branch coverage and known defect: generator allowing
 * writeString() in place of writeFieldName() – the context must correctly
 * signal STATUS_EXPECT_VALUE when a field name is written out of order.
 * 
 * Branch & Defect Analysis Matrix:
 * - writeFieldName(): branch on _gotName, _dups, _index
 * - writeValue(): branches on TYPE_OBJECT, TYPE_ARRAY, root
 * - reset(): resets _type, _index, _currentName, _gotName, _currentValue, _dups
 * - createChildArrayContext/createChildObjectContext: reuse child if not null
 * - appendDesc(): branches on TYPE_OBJECT, TYPE_ARRAY, root
 * - getCurrentName, getCurrentValue, setCurrentValue, getParent, getDupDetector
 * - DupDetector integration: _checkDup throws JsonGenerationException on duplicate
 */
public class JsonWriteContextDeepseekTest {

    // -------- Helper DupDetector for duplicate detection tests ----------
    static class TestDupDetector extends DupDetector {
        private final Set<String> names = new HashSet<>();

        @Override
        public DupDetector child() {
            return new TestDupDetector();
        }

        @Override
        public boolean isDup(String name) {
            return !names.add(name);
        }

        @Override
        public void reset() {
            names.clear();
        }
    }

    // ====================== SECTION A: Core Functional Logic =====================

    @Test(timeout = 4000)
    public void testRootContextCreation() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertNotNull(root);
        assertEquals(JsonStreamContext.TYPE_ROOT, root.getType());
        assertNull(root.getParent());
        assertEquals(-1, root.getCurrentIndex());
        assertNull(root.getCurrentName());
        assertNull(root.getCurrentValue());
        assertNull(root.getDupDetector()); }

    @Test(timeout = 4000)
    public void testChildArrayContext() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext child = root.createChildArrayContext();
        assertNotNull(child);
        assertEquals(JsonStreamContext.TYPE_ARRAY, child.getType());
        assertSame(root, child.getParent());
        assertEquals(-1, child.getCurrentIndex());
        assertNull(child.getCurrentName());
        assertNull(child.getCurrentValue());
    }

    @Test(timeout = 4000)
    public void testChildObjectContext() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext child = root.createChildObjectContext();
        assertNotNull(child);
        assertEquals(JsonStreamContext.TYPE_OBJECT, child.getType());
        assertSame(root, child.getParent());
        assertEquals(-1, child.getCurrentIndex());
        assertNull(child.getCurrentName());
        assertNull(child.getCurrentValue());
    }

    @Test(timeout = 4000)
    public void testRootWriteValue() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        // First call: _index becomes 0 --> STATUS_OK_AS_IS
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, root.writeValue());
        assertEquals(0, root.getCurrentIndex());
        // Second call: _index becomes 1 --> STATUS_OK_AFTER_SPACE
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_SPACE, root.writeValue());
        assertEquals(1, root.getCurrentIndex());
    }

    @Test(timeout = 4000)
    public void testArrayWriteValue() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext child = root.createChildArrayContext();
        // First value: _index was -1, becomes 0 --> STATUS_OK_AS_IS
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, child.writeValue());
        assertEquals(0, child.getCurrentIndex());
        // Second value: _index becomes 1 --> STATUS_OK_AFTER_COMMA
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, child.writeValue());
        assertEquals(1, child.getCurrentIndex());
    }

    @Test(timeout = 4000)
    public void testObjectWriteFieldName() throws JsonProcessingException {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext child = root.createChildObjectContext();
        // Initially _gotName false, _index -1 --> STATUS_OK_AS_IS
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, child.writeFieldName("a"));
        assertEquals("a", child.getCurrentName());
        assertTrue(child._gotName);
        // Two consecutive field names: _gotName true --> STATUS_EXPECT_VALUE
        assertEquals(JsonWriteContext.STATUS_EXPECT_VALUE, child.writeFieldName("b"));
        // Ensure _currentName still "a" (not overwritten)
        assertEquals("a", child.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testObjectWriteValue() throws JsonProcessingException {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext child = root.createChildObjectContext();
        // Write field name first
        child.writeFieldName("x");
        // Write value: _gotName becomes false, _index increments to 0 --> STATUS_OK_AFTER_COLON
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COLON, child.writeValue());
        assertEquals(0, child.getCurrentIndex());
        assertFalse(child._gotName);
        // Now write another field name (should succeed)
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, child.writeFieldName("y"));
        assertEquals("y", child.getCurrentName());
    }

    // ===================== SECTION B: Boundary Value Analysis =====================

    @Test(timeout = 4000)
    public void testWriteFieldNameWithNullAndEmpty() throws JsonProcessingException {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext child = root.createChildObjectContext();
        // Null name
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, child.writeFieldName(null));
        assertNull(child.getCurrentName());
        // Empty string
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, child.writeFieldName(""));
        assertEquals("", child.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testRootWriteFieldName() throws JsonProcessingException {
        // Root context: writeFieldName should behave as _gotName logic but type is not OBJECT
        // Actually writeFieldName only checks _gotName, not type. It will set _gotName true and return status based on _index.
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, root.writeFieldName("rootField"));
        // Second call: _gotName true -> STATUS_EXPECT_VALUE
        assertEquals(JsonWriteContext.STATUS_EXPECT_VALUE, root.writeFieldName("another"));
    }

    @Test(timeout = 4000)
    public void testBoundaryIndexNegativeOne() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertEquals(-1, root.getCurrentIndex());
    }

    // ===================== SECTION C: Defect-Targeted Branch =====================

    /** 
     * Directly targets the defect: when writeFieldName is called twice consecutively
     * in an object context, the second call must return STATUS_EXPECT_VALUE.
     * The generator should then reject the call as invalid (the defect is that it does not).
     * This test asserts the correct context behavior.
     */
    @Test(timeout = 4000)
    public void testDefectOutOfOrderFieldName() throws JsonProcessingException {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext obj = root.createChildObjectContext();
        // First call to write a field name
        int firstStatus = obj.writeFieldName("correct");
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, firstStatus);
        // Second call without an intervening writeValue -> must indicate error
        int secondStatus = obj.writeFieldName("wrong");
        assertEquals(JsonWriteContext.STATUS_EXPECT_VALUE, secondStatus);
    }

    // ===================== SECTION D: Exception & Defensive Paths =====================

    @Test(timeout = 4000, expected = JsonProcessingException.class)
    public void testDuplicateDetectionThrows() throws JsonProcessingException {
        // Use a DupDetector that flags duplicates
        TestDupDetector dd = new TestDupDetector();
        JsonWriteContext root = JsonWriteContext.createRootContext(dd);
        JsonWriteContext obj = root.createChildObjectContext(); // inherits dupDetector
        // First field name is OK
        obj.writeFieldName("dup");
        // Second must throw
        obj.writeFieldName("dup"); // registration in _checkDup throw JsonGenerationException
    }

    @Test(timeout = 4000)    
    public void testDuplicateDetectionNoThrowForDifferentNames() throws JsonProcessingException {
        TestDupDetector dd = new TestDupDetector();
        JsonWriteContext root = JsonWriteContext.createRootContext(dd);
        JsonWriteContext obj = root.createChildObjectContext();
        // Two different names must succeed
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, obj.writeFieldName("first"));
        assertEquals(JsonWriteContext.STATUS_OK_AFTER_COMMA, obj.writeFieldName("second"));
    }

    @Test(timeout = 4000)
    public void testDuplicateDetectionAfterReset() throws JsonProcessingException {
        TestDupDetector dd = new TestDupDetector();
        JsonWriteContext ctxt = new JsonWriteContext(JsonStreamContext.TYPE_OBJECT, null, dd);
        ctxt.writeFieldName("dup");
        // reset clears the dup detector state
        ctxt.reset(JsonStreamContext.TYPE_ARRAY);
        assertEquals(-1, ctxt.getCurrentIndex());
        assertNull(ctxt.getCurrentName());
        assertFalse(ctxt._gotName);
        // Now writing "dup" again should not throw
        // (It is now array context, but writeFieldName still uses _dups)
        // For array context, _gotName check still applies; after reset, _gotName false
        ctxt.writeFieldName("dup"); // should be OK (no exception)
    }

    // ===================== SECTION E: Lifecycle & Contract Integrity =====================

    @Test(timeout = 4000)
    public void testToStringRoot() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertEquals("/", root.toString());
    }

    @Test(timeout = 4000)
    public void testToStringArray() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext arr = root.createChildArrayContext();
        // After creating, index is -1
        assertEquals("[-1]", arr.toString());
        arr.writeValue();
        assertEquals("[0]", arr.toString());
    }

    @Test(timeout = 4000)
    public void testToStringObject() throws JsonProcessingException {
        JsonWriteContext root = JsonWriteContext.createRootContext        JsonWriteContext obj = root.createChildObjectContext();
        assertEquals("{?}", obj.toString()); // no current name
        obj.writeFieldName("myField");
        assertEquals("{\"myField\"}", obj.toString()); // with name
    }

    @Test(timeout = 4000)
    public void testGetCurrentValueSetter() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        assertNull(root.getCurrentValue());
        Object val = new Object();
        root.setCurrentValue(val);
        assertSame(val, root.getCurrentValue());
    }

    @Test(imeout = 4000)
    public void testGetDupDetector() {
        TestDupDetector dd = new TestDupDetector();
        JsonWriteContext root = JsonWriteContext.createRootContext(dd);
        assertSame(dd, root.getDupDetector());
    }

    @Test(timeout = 4000)
    public void testChildReuse() {
        JsonWriteContext root = JsonWriteContext.createRootContext();
        JsonWriteContext first = root.createChildArrayContext();
        // Second call reuses the same child object (reset)
        JsonWriteContext second = root.createChildArrayContext();
        assertSame(first, second);
        // But internal state is reset
        assertEquals(-1, second.getCurrentIndex());
    }

    @Test(timeout = 4000)
    public void testObsoleteFactory() {
        // Deprecated method calls createRootContext(null)
        JsonWriteContext ctx = JsonWriteContext.createRootContext();
        assertNull(ctx.getDupDetector());
        assertEquals(JsonStreamContext.TYPE_ROOT, ctx.getType());
    }
}