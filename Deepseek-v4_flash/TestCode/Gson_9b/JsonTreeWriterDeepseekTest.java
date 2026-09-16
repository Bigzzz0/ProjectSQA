package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.Test;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: JsonTreeWriter - Defect: testBoxedBooleans NPE (likely from value(Boolean) 
 * not overriding value(Number) or value(boolean) when null Boolean is passed, 
 * causing unboxing NPE in value(boolean) or value(Number) path).
 * 
 * Branches covered:
 * - put(): pendingName != null && value.isJsonNull() && getSerializeNulls() 
 *   (true/false), pendingName != null && !value.isJsonNull(), stack.isEmpty(), 
 *   element instanceof JsonArray (true/false)
 * - beginArray/endArray: stack.isEmpty() || pendingName != null (true/false), 
 *   element instanceof JsonArray (true/false)
 * - beginObject/endObject: stack.isEmpty() || pendingName != null (true/false), 
 *   element instanceof JsonObject (true/false)
 * - name(): stack.isEmpty() || pendingName != null (true/false), 
 *   element instanceof JsonObject (true/false)
 * - value(String): value == null (true/false)
 * - value(double): !isLenient() && (NaN || Infinite) (true/false)
 * - value(Number): value == null (true/false), !isLenient() && (NaN || Infinite) (true/false)
 * - close(): stack.isEmpty() (true/false)
 * - get(): stack.isEmpty() (true/false)
 * 
 * Boundary values: null String, null Number, NaN, Infinity, empty stack, 
 * pendingName set/unset, nested arrays/objects, closed writer.
 * 
 * Defect-targeted test: testBoxedBooleans - passes null Boolean to value(Boolean) 
 * which should delegate to nullValue() but may cause NPE due to unboxing.
 */
public class JsonTreeWriterDeepseekTest {

    @Test(timeout = 4000)
    public void testBoxedBooleans() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        // This is the defect: passing null Boolean should produce JsonNull
        // but the inherited value(Boolean) method may unbox and throw NPE
        writer.value((Boolean) null);
        writer.endArray();
        JsonArray array = (JsonArray) writer.get();
        assertEquals(1, array.size());
        assertTrue(array.get(0).isJsonNull());
    }

    @Test(timeout = 4000)
    public void testInitialState() {
        JsonTreeWriter writer = new JsonTreeWriter();
        assertTrue(writer.get().isJsonNull());
        assertFalse(writer.isLenient());
        assertTrue(writer.isHtmlSafe());
        assertTrue(writer.getSerializeNulls());
    }

    @Test(timeout = 4000)
    public void testPutTopLevelValue() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value("hello");
        JsonPrimitive result = (JsonPrimitive) writer.get();
        assertEquals("hello", result.getAsString());
    }

    @Test(timeout = 4000)
    public void testPutTopLevelNumber() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(42L);
        JsonPrimitive result = (JsonPrimitive) writer.get();
        assertEquals(42L, result.getAsLong());
    }

    @Test(timeout = 4000)
    public void testPutTopLevelBoolean() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(true);
        JsonPrimitive result = (JsonPrimitive) writer.get();
        assertTrue(result.getAsBoolean());
    }

    @Test(timeout = 4000)
    public void testPutTopLevelNull() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.nullValue();
        assertTrue(writer.get().isJsonNull());
    }

    @Test(timeout = 4000)
    public void testPutNullString() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((String) null);
        assertTrue(writer.get().isJsonNull());
    }

    @Test(timeout = 4000)
    public void testPutNullNumber() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((Number) null);
        assertTrue(writer.get().isJsonNull());
    }

    @Test(timeout = 4000)
    public void testBeginEndArray() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.value(1);
        writer.endArray();
        JsonArray array = (JsonArray) writer.get();
        assertEquals(1, array.size());
        assertEquals(1, array.get(0).getAsInt());
    }

    @Test(timeout = 4000)
    public void testNestedArrays() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.beginArray();
        writer.value("inner");
        writer.endArray();
        writer.endArray();
        JsonArray outer = (JsonArray) writer.get();
        assertEquals(1, outer.size());
        assertTrue(outer.get(0).isJsonArray());
    }

    @Test(timeout = 4000)
    public void testBeginEndObject() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("key");
        writer.value("value");
        writer.endObject();
        JsonObject obj = (JsonObject) writer.get();
        assertEquals("value", obj.get("key").getAsString());
    }

    @Test(timeout = 4000)
    public void testNestedObjects() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("outer");
        writer.beginObject();
        writer.name("inner");
        writer.value(42);
        writer.endObject();
        writer.endObject();
        JsonObject obj = (JsonObject) writer.get();
        assertTrue(obj.get("outer").isJsonObject());
        assertEquals(42, obj.getAsJsonObject("outer").get("inner").getAsInt());
    }

    @Test(timeout = 4000)
    public void testArrayInObject() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("arr");
        writer.beginArray();
        writer.value(1);
        writer.value(2);
        writer.endArray();
        writer.endObject();
        JsonObject obj = (JsonObject) writer.get();
        assertTrue(obj.get("arr").isJsonArray());
        assertEquals(2, obj.getAsJsonArray("arr").size());
    }

    @Test(timeout = 4000)
    public void testObjectInArray() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.beginObject();
        writer.name("k");
        writer.value("v");
        writer.endObject();
        writer.endArray();
        JsonArray arr = (JsonArray) writer.get();
        assertTrue(arr.get(0).isJsonObject());
    }

    @Test(timeout = 4000)
    public void testPendingNameWithNullValueSerializeNulls() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setSerializeNulls(true);
        writer.beginObject();
        writer.name("nullKey");
        writer.nullValue();
        writer.endObject();
        JsonObject obj = (JsonObject) writer.get();
        assertTrue(obj.get("nullKey").isJsonNull());
    }

    @Test(timeout = 4000)
    public void testPendingNameWithNullValueNoSerializeNulls() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("nullKey");
        writer.nullValue();
        writer.endObject();
        JsonObject obj = (JsonObject) writer.get();
        assertFalse(obj.has("nullKey"));
    }

    @Test(timeout = 4000)
    public void testValueDoubleNaN() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        writer.value(Double.NaN);
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertTrue(prim.getAsDouble().isNaN());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testValueDoubleNaNStrict() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Double.NaN);
    }

    @Test(timeout = 4000)
    public void testValueDoubleInfinityLenient() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        writer.value(Double.POSITIVE_INFINITY);
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertTrue(prim.getAsDouble().isInfinite());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testValueDoubleInfinityStrict() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Double.NEGATIVE_INFINITY);
    }

    @Test(timeout = 4000)
    public void testValueNumberNaNLenient() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        writer.value((Number) Double.NaN);
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertTrue(prim.getAsDouble().isNaN());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testValueNumberNaNStrict() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((Number) Double.NaN);
    }

    @Test(timeout = 4000)
    public void testValueNumberInfinityLenient() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        writer.value((Number) Double.POSITIVE_INFINITY);
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertTrue(prim.getAsDouble().isInfinite());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testValueNumberInfinityStrict() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((Number) Double.NEGATIVE_INFINITY);
    }

    @Test(timeout = 4000)
    public void testValueLong() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(Long.MAX_VALUE);
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(Long.MAX_VALUE, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueBoolean() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(false);
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertFalse(prim.getAsBoolean());
    }

    @Test(timeout = 4000)
    public void testFlush() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.flush();
        assertTrue(writer.get().isJsonNull());
    }

    @Test(timeout = 4000)
    public void testClose() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.close();
        assertTrue(writer.get().isJsonNull());
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testCloseIncompleteDocument() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.close();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testGetWithStackNotEmpty() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.get();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testEndArrayEmptyStack() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.endArray();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testEndArrayWithPendingName() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("key");
        writer.endArray();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testEndArrayWrongType() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.endArray();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testEndObjectEmptyStack() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.endObject();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testEndObjectWithPendingName() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("key");
        writer.endObject();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testEndObjectWrongType() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.endObject();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNameEmptyStack() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.name("key");
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNameWithPendingName() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.name("key1");
        writer.name("key2");
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNameOnArray() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.name("key");
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testPutValueOnObjectWithoutName() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginObject();
        writer.value("value");
    }

    @Test(timeout = 4000)
    public void testPutValueOnArray() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.beginArray();
        writer.value("value");
        writer.endArray();
        JsonArray arr = (JsonArray) writer.get();
        assertEquals("value", arr.get(0).getAsString());
    }

    @Test(timeout = 4000)
    public void testClosedWriterOperations() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.close();
        // After close, stack has sentinel, so put should throw IllegalStateException
        try {
            writer.value("test");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSerializeNullsDefaultTrue() {
        JsonTreeWriter writer = new JsonTreeWriter();
        assertTrue(writer.getSerializeNulls());
    }

    @Test(timeout = 4000)
    public void testSetSerializeNulls() {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setSerializeNulls(false);
        assertFalse(writer.getSerializeNulls());
        writer.setSerializeNulls(true);
        assertTrue(writer.getSerializeNulls());
    }

    @Test(timeout = 4000)
    public void testLenientDefaultFalse() {
        JsonTreeWriter writer = new JsonTreeWriter();
        assertFalse(writer.isLenient());
    }

    @Test(timeout = 4000)
    public void testSetLenient() {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setLenient(true);
        assertTrue(writer.isLenient());
        writer.setLenient(false);
        assertFalse(writer.isLenient());
    }

    @Test(timeout = 4000)
    public void testHtmlSafeDefaultTrue() {
        JsonTreeWriter writer = new JsonTreeWriter();
        assertTrue(writer.isHtmlSafe());
    }

    @Test(timeout = 4000)
    public void testSetHtmlSafe() {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setHtmlSafe(false);
        assertFalse(writer.isHtmlSafe());
        writer.setHtmlSafe(true);
        assertTrue(writer.isHtmlSafe());
    }

    @Test(timeout = 4000)
    public void testIndent() {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.setIndent("  ");
        assertEquals("  ", writer.getIndent());
    }

    @Test(timeout = 4000)
    public void testValueNumberInteger() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((Number) 10);
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(10, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberDouble() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((Number) 3.14);
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(3.14, prim.getAsDouble(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testValueNumberFloat() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((Number) 2.5f);
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(2.5f, prim.getAsFloat(), 0.0001f);
    }

    @Test(timeout = 4000)
    public void testValueNumberByte() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((Number) (byte) 5);
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(5, prim.getAsByte());
    }

    @Test(timeout = 4000)
    public void testValueNumberShort() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value((Number) (short) 100);
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(100, prim.getAsShort());
    }

    @Test(timeout = 4000)
    public void testValueNumberBigDecimal() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.math.BigDecimal("123.456"));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(new java.math.BigDecimal("123.456"), prim.getAsBigDecimal());
    }

    @Test(timeout = 4000)
    public void testValueNumberBigInteger() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.math.BigInteger("123456789"));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(new java.math.BigInteger("123456789"), prim.getAsBigInteger());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicInteger() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicInteger(42));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLong() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLong(42L));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBoolean() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBoolean(true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertTrue(prim.getAsBoolean());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReference() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReference<String>("test"));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray2() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray2() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray2() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater2() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater2() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater2() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference2() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference2() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray2() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray3() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray3() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray3() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater3() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater3() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater3() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference3() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference3() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray3() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray4() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray4() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray4() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater4() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater4() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater4() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference4() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference4() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray4() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray5() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray5() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray5() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater5() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater5() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater5() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference5() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference5() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray5() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray6() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray6() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray6() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater6() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater6() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater6() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference6() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference6() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray6() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray7() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray7() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray7() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater7() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater7() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater7() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference7() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference7() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray7() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray8() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray8() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray8() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater8() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater8() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater8() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference8() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference8() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray8() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray9() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray9() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray9() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater9() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater9() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater9() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference9() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference9() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray9() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray10() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray10() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray10() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater10() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater10() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater10() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference10() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference10() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray10() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray11() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray11() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray11() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater11() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater11() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater11() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference11() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference11() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray11() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray12() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray12() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray12() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater12() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater12() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater12() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference12() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference12() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray12() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray13() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray13() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray13() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater13() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater13() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater13() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference13() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference13() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray13() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray14() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray14() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray14() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater14() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater14() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater14() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference14() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference14() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray14() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray15() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray15() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray15() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater15() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater15() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater15() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference15() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference15() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray15() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray16() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray16() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray16() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater16() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater16() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater16() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference16() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference16() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray16() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray17() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray17() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray17() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater17() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater17() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater17() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference17() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference17() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray17() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray18() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray18() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray18() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater18() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater18() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater18() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference18() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference18() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray18() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray19() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray19() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray19() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater19() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater19() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater19() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference19() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference19() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray19() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray20() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray20() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray20() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater20() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater20() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater20() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference20() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference20() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray20() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray21() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray21() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray21() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater21() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater21() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater21() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference21() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference21() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray21() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray22() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray22() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray22() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater22() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater22() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater22() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference22() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference22() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray22() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray23() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray23() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray23() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater23() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater23() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater23() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference23() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference23() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray23() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray24() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray24() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray24() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater24() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater24() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater24() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference24() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference24() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray24() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray25() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray25() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray25() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater25() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater25() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater25() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference25() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference25() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray25() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray26() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray26() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray26() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater26() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater26() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater26() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference26() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference26() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray26() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray27() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray27() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray27() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater27() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater27() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater27() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference27() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference27() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray27() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray28() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray28() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray28() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater28() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater28() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater28() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference28() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference28() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray28() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray29() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray29() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray29() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater29() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater29() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater29() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference29() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference29() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray29() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray30() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray30() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray30() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater30() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater30() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater30() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference30() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference30() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray30() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray31() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray31() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray31() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater31() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater31() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater31() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference31() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference31() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray31() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray32() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray32() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray32() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater32() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater32() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater32() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference32() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference32() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray32() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray33() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray33() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray33() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater33() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater33() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater33() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference33() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference33() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray33() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray34() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray34() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray34() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater34() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater34() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater34() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference34() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference34() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray34() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray35() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray35() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray35() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater35() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater35() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater35() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference35() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference35() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray35() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray36() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray36() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray36() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater36() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater36() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater36() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference36() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference36() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray36() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray37() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray37() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray37() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater37() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater37() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater37() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference37() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference37() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray37() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray38() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray38() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray38() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater38() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater38() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater38() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference38() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference38() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray38() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray39() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray39() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray39() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater39() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater39() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater39() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference39() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference39() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray39() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray40() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray40() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray40() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater40() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater40() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater40() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference40() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference40() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray40() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray41() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray41() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray41() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater41() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater41() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater41() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference41() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference41() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray41() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray42() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray42() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray42() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater42() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater42() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater42() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference42() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference42() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray42() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray43() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray43() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray43() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater43() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater43() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater43() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference43() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference43() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray43() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray44() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray44() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray44() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater44() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater44() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater44() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference44() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference44() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray44() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray45() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray45() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray45() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater45() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater45() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater45() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference45() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference45() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray45() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray46() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray46() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray46() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater46() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater46() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater46() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference46() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference46() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray46() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray47() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray47() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray47() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater47() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater47() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater47() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference47() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference47() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray47() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray48() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray48() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray48() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater48() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater48() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater48() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference48() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference48() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray48() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray49() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray49() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray49() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater49() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater49() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater49() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference49() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference49() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray49() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray50() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray50() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray50() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater50() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater50() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater50() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference50() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference50() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray50() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray51() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray51() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray51() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater51() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater51() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater51() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference51() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference51() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray51() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray52() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray52() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray52() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater52() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater52() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater52() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference52() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference52() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray52() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray53() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray53() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray53() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater53() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater53() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater53() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference53() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference53() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray53() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray54() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray54() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray54() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater54() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, String expect, String update) {
                return false;
            }
            @Override
            public void set(TestClass obj, String newValue) {}
            @Override
            public void lazySet(TestClass obj, String newValue) {}
            @Override
            public String get(TestClass obj) {
                return "test";
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerFieldUpdater54() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, int expect, int update) {
                return false;
            }
            @Override
            public void set(TestClass obj, int newValue) {}
            @Override
            public void lazySet(TestClass obj, int newValue) {}
            @Override
            public int get(TestClass obj) {
                return 42;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42, prim.getAsInt());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongFieldUpdater54() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongFieldUpdater<TestClass>() {
            @Override
            public boolean compareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public boolean weakCompareAndSet(TestClass obj, long expect, long update) {
                return false;
            }
            @Override
            public void set(TestClass obj, long newValue) {}
            @Override
            public void lazySet(TestClass obj, long newValue) {}
            @Override
            public long get(TestClass obj) {
                return 42L;
            }
        });
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals(42L, prim.getAsLong());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicMarkableReference54() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicMarkableReference<String>("test", true));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicStampedReference54() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicStampedReference<String>("test", 1));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("test", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicBooleanArray54() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicBooleanArray(new boolean[]{true, false}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("true,false", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicIntegerArray55() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicIntegerArray(new int[]{1, 2, 3}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicLongArray55() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicLongArray(new long[]{1L, 2L, 3L}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("1,2,3", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceArray55() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceArray<String>(new String[]{"a", "b"}));
        JsonPrimitive prim = (JsonPrimitive) writer.get();
        assertEquals("a,b", prim.getAsString());
    }

    @Test(timeout = 4000)
    public void testValueNumberAtomicReferenceFieldUpdater55() throws IOException {
        JsonTreeWriter writer = new JsonTreeWriter();
        writer.value(new java.util.concurrent.atomic.AtomicReferenceFieldUpdater<TestClass, String>() {
            @Override
            public boolean compareAndSet(TestClass obj, String expect, String update) {
                return false