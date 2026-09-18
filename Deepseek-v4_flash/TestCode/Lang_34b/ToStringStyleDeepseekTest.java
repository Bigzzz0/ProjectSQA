package org.apache.commons.lang3.builder;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 * - appendStart/appendEnd: Tests basic begin/end output formatting
 * - append(String, Object): Tests field-value output with null handling
 * - appendInternal: Object routing for Collection, Map, arrays, cycles
 * - register/unregister: Thread-local cycle detection lifecycle
 * 
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - Empty arrays (all primitive and Object[]): boundary for array handling
 * - Null objects, null arrays, null fields
 * - Zero-length arrays and collections
 * - high/low and MAX values for primitive arrays (e.g., Long.MAX_VALUE)
 * 
 * Partition C: Defect-Targeted Branch Zone
 * - Critical: Object cycles in appendInternal must append cyclicObject
 *   without infinite recursion. The known defect is that cycle detection
 *   fails for Objects that are registered but NOT Number/Boolean/Character,
 *   causing infinite recursion and stack overflow in debug mode.
 *   The REGISTRY-based detection returns early, but the null output is
 *   inconsistent (“<null>” vs “{}”). Test specifically verifies that
 *   self-referencing objects (cycles) produce correct output.
 * - Reflection-related cycles: various array/object cycle combos.
 * 
 * Partition D: Exception & Defensive Guard Paths
 * - Null contentStart/contentEnd getters with setter null conversion
 * - Field separator conditions (fieldSeparatorAtStart/End)
 * - removeLastFieldSeparator with empty buffer, short field sep
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 * - ThreadLocal REGISTRY independence: run two tests that check
 *   register/unregister does not leak between threads
 * 
 * The defect targets: org.apache.commons.lang3.builder.ToStringBuilderTest
 *   test*Cycle tests fail with expected: <null> but was: {}.
 * This indicates that cycle detection logic returns "{}" instead of
 * cycling gracefully. We test: register an object, then call
 * appendInternal with that same object to force the cycle branch.
 */
public class ToStringStyleDeepseekTest {

    // ---------- Partition A: Core Functional Logic ----------

    @Test(timeout = 4000)
    public void testAppendStartEndWithNullObject() {
        // Covers appendStart with null, appendEnd with null
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.appendStart(buf, null);
        assertEquals("", buf.toString());
        style.appendEnd(buf, null);
        assertEquals("", buf.toString());
    }

    @Test(timeout = 4000)
    public void testAppendStartEndWithObject() {
        // Normal start/end with non-null object
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        Object obj = new Object();
        style.appendStart(buf, obj);
        String s1 = buf.toString();
        assertTrue(s1.startsWith("java.lang.Object@"));
        assertTrue(s1.endsWith("["));
        style.appendEnd(buf, obj);
        String s2 = buf.toString();
        assertTrue(s2.startsWith("java.lang.Object@"));
        assertTrue(s2.endsWith("]"));
    }

    @Test(timeout = 4000)
    public void testAppendObjectField() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.appendStart(buf, "dummy");
        style.append(buf, "name", "John", null);
        style.appendEnd(buf, "dummy");
        String result = buf.toString();
        // Contains field name and value
        assertTrue(result.contains("name"));
        assertTrue(result.contains("John"));
    }

    @Test(timeout = 4000)
    public void testAppendNullObjectField() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.append(buf, "nullField", null, null);
        String result = buf.toString();
        assertTrue(result.contains("<null>"));
    }

    @Test(timeout = 4000)
    public void testAppendLongField() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.append(buf, "count", 42L);
        String result = buf.toString();
        assertTrue(result.contains("count=42"));
    }

    @Test(timeout = 4000)
    public void testAppendIntField() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.append(buf, "num", 7);
        String result = buf.toString();
        assertTrue(result.contains("num=7"));
    }

    @Test(timeout = 4000)
    public void testAppendShortField() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.append(buf, "s", (short) 3);
        String result = buf.toString();
        assertTrue(result.contains("s=3"));
    }

    @Test(timeout = 4000)
    public void testAppendByteField() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.append(buf, "b", (byte) 1);
        String result = buf.toString();
        assertTrue(result.contains("b=1"));
    }

    @Test(timeout = 4000)
    public void testAppendCharField() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.append(buf, "c", 'X');
        String result = buf.toString();
        assertTrue(result.contains("c=X"));
    }

    @Test(timeout = 4000)
    public void testAppendDoubleField() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.append(buf, "d", 3.14);
        String result = buf.toString();
        assertTrue(result.contains("d=3.14"));
    }

    @Test(timeout = 4000)
    public void testAppendFloatField() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.append(buf, "f", 2.5f);
        String result = buf.toString();
        assertTrue(result.contains("f=2.5"));
    }

    @Test(timeout = 4000)
    public void testAppendBooleanField() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.append(buf, "flag", true);
        String result = buf.toString();
        assertTrue(result.contains("flag=true"));
    }

    // ---------- Partition B: Boundary Value Analysis ----------

    @Test(timeout = 4000)
    public void testAppendNullObjectArray() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.append(buf, "arr", (Object[]) null, null);
        String result = buf.toString();
        assertTrue(result.contains("<null>"));
    }

    @Test(timeout = 4000)
    public void testAppendEmptyObjectArray() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.append(buf, "arr", new Object[0], true);
        String result = buf.toString();
        assertTrue(result.contains("arr={}"));
    }

    @Test(timeout = 4000)
    public void testAppendEmptyLongArray() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.append(buf, "arr", new long[0], true);
        String result = buf.toString();
        assertTrue(result.contains("arr={}"));
    }

    @Test(timeout = 4000)
    public void testAppendLongArrayDetail() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        long[] arr = {1L, 2L, 3L};
        style.append(buf, "arr", arr, true);
        String result = buf.toString();
        assertTrue(result.contains("arr={1,2,3}"));
    }

    @Test(timeout = 4000)
    public void testAppendLongArraySummary() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        long[] arr = {1L, 2L, 3L};
        style.append(buf, "arr", arr, false);
        String result = buf.toString();
        assertTrue(result.contains("arr=<size=3>"));
    }

    @Test(timeout = 4000)
    public void testAppendIntArrayDetail() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        int[] arr = {10, 20};
        style.append(buf, "arr", arr, true);
        String result = buf.toString();
        assertTrue(result.contains("arr={10,20}"));
    }

    @Test(timeout = 4000)
    public void testAppendIntArraySummary() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        int[] arr = {1, 2, 3, 4};
        style.append(buf, "arr", arr, false);
        String result = buf.toString();
        assertTrue(result.contains("arr=<size=4>"));
    }

    @Test(timeout = 4000)
    public void testAppendShortArrayDetail() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        short[] arr = {1, 2};
        style.append(buf, "arr", arr, true);
        String result = buf.toString();
        assertTrue(result.contains("arr={1,2}"));
    }

    @Test(timeout = 4000)
    public void testAppendByteArrayDetail() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        byte[] arr = {0x41, 0x42};
        style.append(buf, "arr", arr, true);
        String result = buf.toString();
        assertTrue(result.contains("arr={65,66}"));
    }

    @Test(timeout = 4000)
    public void testAppendCharArrayDetail() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        char[] arr = {'a', 'b'};
        style.append(buf, "arr", arr, true);
        String result = buf.toString();
        assertTrue(result.contains("arr={a,b}"));
    }

    @Test(timeout = 4000)
    public void testAppendDoubleArrayDetail() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        double[] arr = {1.5, 2.5};
        style.append(buf, "arr", arr, true);
        String result = buf.toString();
        assertTrue(result.contains("arr={1.5,2.5}"));
    }

    @Test(timeout = 4000)
    public void testAppendFloatArrayDetail() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        float[] arr = {1.0f, 2.0f};
        style.append(buf, "arr", arr, true);
        String result = buf.toString();
        assertTrue(result.contains("arr={1.0,2.0}"));
    }

    @Test(timeout = 4000)
    public void testAppendBooleanArrayDetail() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        boolean[] arr = {true, false};
        style.append(buf, "arr", arr, true);
        String result = buf.toString();
        assertTrue(result.contains("arr={true,false}"));
    }

    @Test(timeout = 4000)
    public void testAppendBooleanArraySummary() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        boolean[] arr = {true, false};
        style.append(buf, "arr", arr, false);
        String result = buf.toString();
        assertTrue(result.contains("arr=<size=2>"));
    }

    @Test(timeout = 4000)
    public void testAppendNullPrimitiveArrays() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf;
        
        buf = new StringBuffer();
        style.append(buf, "arr", (long[]) null, true);
        assertTrue(buf.toString().contains("<null>"));
        
        buf = new StringBuffer();
        style.append(buf, "arr", (int[]) null, true);
        assertTrue(buf.toString().contains("<null>"));
        
        buf = new StringBuffer();
        style.append(buf, "arr", (short[]) null, true);
        assertTrue(buf.toString().contains("<null>"));
        
        buf = new StringBuffer();
        style.append(buf, "arr", (byte[]) null, true);
        assertTrue(buf.toString().contains("<null>"));
        
        buf = new StringBuffer();
        style.append(buf, "arr", (char[]) null, true);
        assertTrue(buf.toString().contains("<null>"));
        
        buf = new StringBuffer();
        style.append(buf, "arr", (double[]) null, true);
        assertTrue(buf.toString().contains("<null>"));
        
        buf = new StringBuffer();
        style.append(buf, "arr", (float[]) null, true);
        assertTrue(buf.toString().contains("<null>"));
        
        buf = new StringBuffer();
        style.append(buf, "arr", (boolean[]) null, true);
        assertTrue(buf.toString().contains("<null>"));
    }

    @Test(timeout = 4000)
    public void testAppendCollection() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.appendStart(buf, "dummy");
        List<String> list = Arrays.asList("a", "b");
        style.append(buf, "list", list, true);
        style.appendEnd(buf, "dummy");
        String result = buf.toString();
        assertTrue(result.contains("list={a,b}"));
    }

    @Test(timeout = 4000)
    public void testAppendMap() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.appendStart(buf, "dummy");
        Map<String, Integer> map = new HashMap<>();
        map.put("x", 1);
        map.put("y", 2);
        style.append(buf, "map", map, true);
        style.appendEnd(buf, "dummy");
        String result = buf.toString();
        assertTrue(result.contains("map="));
        assertTrue(result.contains("{x=1, y=2}") || result.contains("{y=2, x=1}"));
    }

    // ---------- Partition C: Defect-Targeted Branch Zone ----------

    @Test(timeout = 4000)
    public void testObjectCycle() {
        // This test targets the known defect: cycle detection failure
        // Creates a self-referencing object and ensures no infinite loop
        // and output is correct (should not be "{}")
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        
        // Create a simple object that will be registered as cycle
        Object cycleObj = new Object();
        
        // Manually register the object to simulate a cycle scenario
        ToStringStyle.register(cycleObj);
        
        // Now if we try to append this object, appendInternal should detect
        // the cycle and call appendCyclicObject
        style.appendStart(buf, "dummy");
        style.append(buf, "cycleField", cycleObj, true);
        style.appendEnd(buf, "dummy");
        
        // Unregister after test
        ToStringStyle.unregister(cycleObj);
        
        String result = buf.toString();
        // The cycle should produce identity string like "java.lang.Object@xxxx"
        // NOT "{}" or "<null>"
        assertFalse("Cycle should not produce empty braces", result.contains("{}"));
        assertTrue("Cycle should contain Object identity", 
                    result.contains("java.lang.Object@"));
    }

    @Test(timeout = 4000)
    public void testObjectCycleWithNumber() {
        // Numbers should NOT be treated as cycles (bypassed in condition)
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        Integer intObj = 42;
        ToStringStyle.register(intObj);
        style.append(buf, "num", intObj, true);
        ToStringStyle.unregister(intObj);
        String result = buf.toString();
        assertTrue(result.contains("num=42"));
    }

    @Test(timeout = 4000)
    public void testObjectCycleWithBoolean() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        Boolean boolObj = Boolean.TRUE;
        ToStringStyle.register(boolObj);
        style.append(buf, "flag", boolObj, true);
        ToStringStyle.unregister(boolObj);
        String result = buf.toString();
        assertTrue(result.contains("flag=true"));
    }

    @Test(timeout = 4000)
    public void testObjectCycleWithCharacter() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        Character charObj = 'Z';
        ToStringStyle.register(charObj);
        style.append(buf, "ch", charObj, true);
        ToStringStyle.unregister(charObj);
        String result = buf.toString();
        assertTrue(result.contains("ch=Z"));
    }

    @Test(timeout = 4000)
    public void testCycleDetectionInAppendInternal() {
        // Simulate a cycle during appending collection
        // The collection itself is registered when passed to appendInternal
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        
        List<Object> list = new ArrayList<>();
        list.add("item1");
        list.add(list);  // self-reference!
        
        // This should not cause infinite loop due to registry
        style.appendStart(buf, "dummy");
        style.append(buf, "cycleList", list, true);
        style.appendEnd(buf, "dummy");
        String result = buf.toString();
        
        // The result should handle the cycle gracefully
        assertFalse("Cycle should not cause stack overflow", result.isEmpty());
        assertTrue(result.contains("cycleList"));
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------

    @Test(timeout = 4000)
    public void testSetNullArrayStart() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.setArrayStart(null);
        assertEquals("", style.getArrayStart());
    }

    @Test(timeout = 4000)
    public void testSetNullArrayEnd() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.setArrayEnd(null);
        assertEquals("", style.getArrayEnd());
    }

    @Test(timeout = 4000)
    public void testSetNullArraySeparator() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.setArraySeparator(null);
        assertEquals("", style.getArraySeparator());
    }

    @Test(timeout = 4000)
    public void testSetNullContentStart() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.setContentStart(null);
        assertEquals("", style.getContentStart());
    }

    @Test(timeout = 4000)
    public void testSetNullContentEnd() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.setContentEnd(null);
        assertEquals("", style.getContentEnd());
    }

    @Test(timeout = 4000)
    public void testSetNullFieldNameValueSeparator() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.setFieldNameValueSeparator(null);
        assertEquals("", style.getFieldNameValueSeparator());
    }

    @Test(timeout = 4000)
    public void testSetNullFieldSeparator() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.setFieldSeparator(null);
        assertEquals("", style.getFieldSeparator());
    }

    @Test(timeout = 4000)
    public void testSetNullNullText() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.setNullText(null);
        assertEquals("", style.getNullText());
    }

    @Test(timeout = 4000)
    public void testSetNullSizeStartText() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.setSizeStartText(null);
        assertEquals("", style.getSizeStartText());
    }

    @Test(timeout = 4000)
    public void testSetNullSizeEndText() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.setSizeEndText(null);
        assertEquals("", style.getSizeEndText());
    }

    @Test(timeout = 4000)
    public void testSetNullSummaryObjectStartText() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.setSummaryObjectStartText(null);
        assertEquals("", style.getSummaryObjectStartText());
    }

    @Test(timeout = 4000)
    public void testSetNullSummaryObjectEndText() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        style.setSummaryObjectEndText(null);
        assertEquals("", style.getSummaryObjectEndText());
    }

    @Test(timeout = 4000)
    public void testRemoveLastFieldSeparatorNoTrailingSep() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer("name=John");
        style.removeLastFieldSeparator(buf);
        assertEquals("name=John", buf.toString());  // unchanged
    }

    @Test(timeout = 4000)
    public void testRemoveLastFieldSeparatorWithTrailingSep() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer("name=John,");
        style.removeLastFieldSeparator(buf);
        assertEquals("name=John", buf.toString());
    }

    @Test(timeout = 4000)
    public void testRemoveLastFieldSeparatorEmptyBuffer() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.removeLastFieldSeparator(buf);
        assertEquals("", buf.toString());
    }

    @Test(timeout = 4000)
    public void testFieldSeparatorAtStartEndFlags() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        assertFalse(style.isFieldSeparatorAtStart());
        assertFalse(style.isFieldSeparatorAtEnd());
        
        style.setFieldSeparatorAtStart(true);
        assertTrue(style.isFieldSeparatorAtStart());
        
        style.setFieldSeparatorAtEnd(true);
        assertTrue(style.isFieldSeparatorAtEnd());
    }

    @Test(timeout = 4000)
    public void testFieldSeparatorAtStartBehavior() {
        // Custom style emulating fieldSeparatorAtStart
        ToStringStyle style = new ToStringStyle() {
            private static final long serialVersionUID = 1L;
        };
        style.setFieldSeparatorAtStart(true);
        style.setFieldSeparator(",");
        style.setContentStart("[");
        style.setContentEnd("]");
        style.setUseClassName(false);
        style.setUseIdentityHashCode(false);
        
        StringBuffer buf = new StringBuffer();
        style.appendStart(buf, "dummy");
        style.append(buf, "name", "test", null);
        style.appendEnd(buf, "dummy");
        String result = buf.toString();
        // Should start with field separator after content start
        assertTrue(result.startsWith("[,"));
    }

    @Test(timeout = 4000)
    public void testAppendSuperWithNull() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer("existing");
        style.appendSuper(buf, null);
        assertEquals("existing", buf.toString());
    }

    @Test(timeout = 4000)
    public void testAppendSuperWithValid() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.appendStart(buf, "dummy");
        style.appendSuper(buf, "java.lang.Object@1234[name=John]");
        style.appendEnd(buf, "dummy");
        String result = buf.toString();
        assertTrue(result.contains("name=John"));
    }

    @Test(timeout = 4000)
    public void testAppendToStringWithNull() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.appendToString(buf, null);
        assertEquals("", buf.toString());
    }

    @Test(timeout = 4000)
    public void testAppendToStringWithInvalidFormat() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        style.appendToString(buf, "no brackets here");
        assertEquals("", buf.toString());
    }

    @Test(timeout = 4000)
    public void testAppendFieldStartEndWithNullFieldName() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        StringBuffer buf = new StringBuffer();
        // appendFieldStart is protected, but test through append which calls it
        style.append(buf, null, "value", null);
        String result = buf.toString();
        // When fieldName is null and useFieldNames is true, appendFieldStart
        // should not append anything
        assertFalse(result.startsWith("null"));
    }

    // ---------- Partition E: Object Lifecycle & Contract ----------

    @Test(timeout = 4000)
    public void testRegisterAndIsRegistered() {
        Object obj = new Object();
        assertFalse(ToStringStyle.isRegistered(obj));
        ToStringStyle.register(obj);
        assertTrue(ToStringStyle.isRegistered(obj));
        ToStringStyle.unregister(obj);
        assertFalse(ToStringStyle.isRegistered(obj));
    }

    @Test(timeout = 4000)
    public void testRegisterNullDoesNothing() {
        ToStringStyle.register(null);
        // Should not throw
    }

    @Test(timeout = 4000)
    public void testUnregisterNullDoesNothing() {
        ToStringStyle.unregister(null);
        // Should not throw
    }

    @Test(timeout = 4000)
    public void testGetRegistryReturnsEmptyMapInitially() {
        Map<Object, Object> registry = ToStringStyle.getRegistry();
        assertTrue(registry.isEmpty());
    }

    @Test(timeout = 4000)
    public void testGetRegistryReturnsCurrentMap() {
        Object obj = new Object();
        ToStringStyle.register(obj);
        Map<Object, Object> registry = ToStringStyle.getRegistry();
        assertTrue(registry.containsKey(obj));
        ToStringStyle.unregister(obj);
    }

    @Test(timeout = 4000)
    public void testThreadLocalIsolation() throws InterruptedException {
        final Object obj = new Object();
        final boolean[] resultHolder = new boolean[2];
        
        Thread t1 = new Thread(() -> {
            ToStringStyle.register(obj);
            resultHolder[0] = ToStringStyle.isRegistered(obj);
        });
        
        Thread t2 = new Thread(() -> {
            resultHolder[1] = ToStringStyle.isRegistered(obj);
        });
        
        t1.start();
        t1.join();
        t2.start();
        t2.join();
        
        assertTrue(resultHolder[0]);  // T1 registered
        assertFalse(resultHolder[1]); // T2 should not see T1's registration
    }

    @Test(timeout = 4000)
    public void testUnregisterFromEmptyRegistry() {
        // Unregistering an object that was never registered should be safe
        ToStringStyle.unregister(new Object());
    }

    @Test(timeout = 4000)
    public void testMultipleRegisterUnregister() {
        Object obj1 = new Object();
        Object obj2 = new Object();
        ToStringStyle.register(obj1);
        ToStringStyle.register(obj2);
        assertTrue(ToStringStyle.isRegistered(obj1));
        assertTrue(ToStringStyle.isRegistered(obj2));
        ToStringStyle.unregister(obj1);
        assertFalse(ToStringStyle.isRegistered(obj1));
        assertTrue(ToStringStyle.isRegistered(obj2));
        ToStringStyle.unregister(obj2);
        assertFalse(ToStringStyle.isRegistered(obj2));
    }

    @Test(timeout = 4000)
    public void testSettersAndGetters() {
        ToStringStyle style = ToStringStyle.DEFAULT_STYLE;
        
        // Toggle each boolean field