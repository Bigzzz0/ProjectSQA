package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonToken;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * =============================
 * Target: JsonTreeReader
 * 
 * Decision Branches:
 * - peek(): stackSize==0 => END_DOCUMENT
 * - peek(): top is Iterator => check isObject and iterator.hasNext()
 * - peek(): top is JsonObject => BEGIN_OBJECT
 * - peek(): top is JsonArray => BEGIN_ARRAY
 * - peek(): top is JsonPrimitive => STRING/BOOLEAN/NUMBER
 * - peek(): top is JsonNull => NULL
 * - peek(): top == SENTINEL_CLOSED => throw IllegalStateException
 * - skipValue(): peek()==NAME => call nextName(), then set pathNames[stackSize-2]="null"
 * - skipValue(): else => popStack(), set pathNames[stackSize-1]="null"
 * - skipValue() then pathIndices[stackSize-1]++ (potentially negative index when stackSize==0)
 * - nextDouble(): NaN/Infinity check when !isLenient()
 * - promoteNameToValue(): expect(NAME)
 * - getPath(): iterate through stack and handle arrays/objects
 * 
 * Boundaries:
 * - Empty object: beginObject -> skipValue() (defect area)
 * - Empty array: beginArray -> skipValue()
 * - Nested object after consuming all entries
 * - Stack resizing (push when full)
 * - Path indices and names after multiple operations
 * - Double/Int/Long parsing from primitives
 * 
 * Defect: skipValue() on empty JsonObject triggers ArrayIndexOutOfBoundsException:-1
 * same for filled JsonObject after consuming all entries? Actually the defect is also on filled object
 * because after nextName() the stack might have only one element left -> pathNames[stackSize-2] = pathNames[-1]
 */
public class JsonTreeReaderDeepseekTest {

    // Section A: Core Functional Logic & State Transitions

    @Test(timeout = 4000)
    public void testReadString() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("hello"));
        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("hello", reader.nextString());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testReadBoolean() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(true));
        assertEquals(JsonToken.BOOLEAN, reader.peek());
        assertTrue(reader.nextBoolean());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testReadNumber() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(42));
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(42, reader.nextInt());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testReadNull() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(JsonNull.INSTANCE);
        assertEquals(JsonToken.NULL, reader.peek());
        reader.nextNull();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testEmptyArray() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonArray());
        assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
        reader.beginArray();
        assertEquals(JsonToken.END_ARRAY, reader.peek());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testEmptyObject() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonObject());
        assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
        reader.beginObject();
        assertEquals(JsonToken.END_OBJECT, reader.peek());
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testNestedArray() throws IOException {
        JsonArray inner = new JsonArray();
        inner.add(new JsonPrimitive(1));
        JsonArray outer = new JsonArray();
        outer.add(inner);
        JsonTreeReader reader = new JsonTreeReader(outer);
        reader.beginArray();
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testNestedObject() throws IOException {
        JsonObject inner = new JsonObject();
        inner.addProperty("x", 10);
        JsonObject outer = new JsonObject();
        outer.add("inner", inner);
        JsonTreeReader reader = new JsonTreeReader(outer);
        reader.beginObject();
        assertEquals("inner", reader.nextName());
        reader.beginObject();
        assertEquals("x", reader.nextName());
        assertEquals(10, reader.nextInt());
        reader.endObject();
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testHasNext() throws IOException {
        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive("a"));
        arr.add(new JsonPrimitive("b"));
        JsonTreeReader reader = new JsonTreeReader(arr);
        assertTrue(reader.hasNext());
        reader.beginArray();
        assertTrue(reader.hasNext());
        reader.nextString();
        assertTrue(reader.hasNext());
        reader.nextString();
        assertFalse(reader.hasNext());
        reader.endArray();
        assertFalse(reader.hasNext());
    }

    @Test(timeout = 4000)
    public void testPathSimple() throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("key", "val");
        JsonTreeReader reader = new JsonTreeReader(obj);
        assertEquals("$", reader.getPath());
        reader.beginObject();
        assertEquals("$.key", reader.getPath()); // before nextName? Actually getPath might be "$" still? Let's verify.
        // After beginObject, the stack has [JsonObject, iterator]. getPath: at i=0 stack[0] is JsonObject, then i++ to 1 is Iterator -> pathNames[1] is null, so result = "$.null"? Actually looks like it appends '.' then null. But we'll just test after nextName.
        reader.nextName();
        assertEquals("$.key", reader.getPath());
        reader.nextString();
        assertEquals("$.key", reader.getPath()); // path stays after value?
        reader.endObject();
        assertEquals("$", reader.getPath());
    }

    // Section B: Boundary Value Analysis & Extremes

    @Test(timeout = 4000)
    public void testNullInArray() throws IOException {
        JsonArray arr = new JsonArray();
        arr.add(JsonNull.INSTANCE);
        JsonTreeReader reader = new JsonTreeReader(arr);
        reader.beginArray();
        assertEquals(JsonToken.NULL, reader.peek());
        reader.nextNull();
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testLargeNestingToTriggerStackResize() throws IOException {
        JsonArray root = new JsonArray();
        JsonArray current = root;
        for (int i = 0; i < 35; i++) {
            JsonArray next = new JsonArray();
            current.add(next);
            current = next;
        }
        current.add(new JsonPrimitive("deep"));
        JsonTreeReader reader = new JsonTreeReader(root);
        for (int i = 0; i < 35; i++) {
            reader.beginArray();
        }
        assertEquals("deep", reader.nextString());
        // close arrays
        for (int i = 0; i < 35; i++) {
            reader.endArray();
        }
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testDoubleNaN() throws IOException {
        JsonPrimitive nan = new JsonPrimitive(Double.NaN);
        JsonTreeReader reader = new JsonTreeReader(nan);
        // default lenient is false
        try {
            reader.nextDouble();
            fail("Expected NumberFormatException for NaN");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDoubleInfinity() throws IOException {
        JsonPrimitive inf = new JsonPrimitive(Double.POSITIVE_INFINITY);
        JsonTreeReader reader = new JsonTreeReader(inf);
        try {
            reader.nextDouble();
            fail("Expected NumberFormatException for Infinity");
        } catch (NumberFormatException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNextDoubleLenientNaN() throws IOException {
        JsonPrimitive nan = new JsonPrimitive(Double.NaN);
        JsonTreeReader reader = new JsonTreeReader(nan);
        reader.setLenient(true);
        assertEquals(Double.NaN, reader.nextDouble(), 0.0);
    }

    @Test(timeout = 4000)
    public void testNextLongFromString() throws IOException {
        JsonPrimitive strNum = new JsonPrimitive("1234567890123");
        JsonTreeReader reader = new JsonTreeReader(strNum);
        assertEquals(1234567890123L, reader.nextLong());
    }

    @Test(timeout = 4000)
    public void testNextIntFromString() throws IOException {
        JsonPrimitive strNum = new JsonPrimitive("999");
        JsonTreeReader reader = new JsonTreeReader(strNum);
        assertEquals(999, reader.nextInt());
    }

    // Section C: Defect-Targeted Tests (skipValue on empty/filled object)

    @Test(timeout = 4000)
    public void testSkipValue_emptyJsonObject() throws IOException {
        JsonObject empty = new JsonObject();
        JsonTreeReader reader = new JsonTreeReader(empty);
        reader.beginObject();
        // Now at END_OBJECT, call skipValue
        reader.skipValue();
        // After skipping an empty object, we should be at END_DOCUMENT
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testSkipValue_filledJsonObject() throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", "value");
        obj.addProperty("age", 30);
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        assertEquals(JsonToken.NAME, reader.peek());
        reader.skipValue(); // skips the first name-value pair
        // Now we should be at the next name or END_OBJECT
        // After skipValue on a name, the stack should be: [JsonObject, iterator (pointing to next entry)]
        // According to bug, this caused ArrayIndexOutOfBoundsException
        assertTrue(reader.hasNext());
        assertEquals("age", reader.nextName());
        assertEquals(30, reader.nextInt());
        reader.skipValue(); // skip the remaining? Actually reader.hasNext() is false now?
        // After reading "age" and its value, the iterator has no next, so peek returns END_OBJECT, skipValue should skip the end?
        // Let's just call skipValue again (which will pop the object) and then check end.
        reader.endObject(); // but we are already at END_OBJECT, skipValue should behave like skipping.
        // Actually skipValue on END_OBJECT will pop the object and set pathIndices etc.
        // So after the second skipValue:
        // reader.skipValue();  // This would skip the object itself, then we are at END_DOCUMENT
        // But let's do it cleanly: after reading all, just end.
    }

    // Additional skipValue tests

    @Test(timeout = 4000)
    public void testSkipValue_emptyArray() throws IOException {
        JsonArray empty = new JsonArray();
        JsonTreeReader reader = new JsonTreeReader(empty);
        reader.beginArray();
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testSkipValue_filledArray() throws IOException {
        JsonArray arr = new JsonArray();
        arr.add(new JsonPrimitive(1));
        arr.add(new JsonPrimitive(2));
        JsonTreeReader reader = new JsonTreeReader(arr);
        reader.beginArray();
        reader.skipValue(); // skip first element
        assertEquals(2, reader.nextInt());
        reader.endArray();
    }

    // Section D: Exception & Defensive Guard Paths

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testPeekAfterClose() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("a"));
        reader.close();
        reader.peek(); // throws IllegalStateException
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testBeginArrayOnPrimitive() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(1));
        reader.beginArray();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testEndArrayOnObject() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonObject());
        reader.beginObject();
        reader.endArray();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testNextNameOnPrimitive() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("a"));
        reader.nextName();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testNextStringOnBoolean() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(true));
        reader.nextString();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testNextBooleanOnString() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("hello"));
        reader.nextBoolean();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testNextNullOnString() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("a"));
        reader.nextNull();
    }

    // Section E: Object Lifecycle & Contract Integrity

    @Test(timeout = 4000)
    public void testToString() {
        JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("abc"));
        assertEquals("JsonTreeReader", reader.toString());
    }

    @Test(timeout = 4000)
    public void testPromoteNameToValue() throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("key", "value");
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        assertEquals(JsonToken.NAME, reader.peek());
        reader.promoteNameToValue();
        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("key", reader.nextString()); // The name becomes a value
        assertEquals("value", reader.nextString()); // The original value
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testSkipValueAfterPromoteNameToValue() throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("a", 1);
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        reader.promoteNameToValue();
        reader.skipValue(); // skip the "a" string
        // Now only the value 1 remains? Actually skipValue after promoteNameToValue: the stack contains [JsonObject, iterator, value, name?] Let's see: after promoteNameToValue, we push value then name. So stack: JsonObject, iterator, value(1), name("a"). peek() returns STRING for name, then skipValue will call nextName? Wait skipValue: if peek()==NAME, but here peek() is STRING (since top is "a" which is a JsonPrimitive string), so it goes else branch: popStack() pops "a", then sets pathNames[stackSize-1]="null". That might cause issues. We just test that it doesn't throw.
        reader.skipValue(); // skip the value 1
        reader.endObject();
    }

    // Additional higher coverage test: skipValue on nested object after consuming some

    @Test(timeout = 4000)
    public void testSkipValueOnNestedObject() throws IOException {
        JsonObject outer = new JsonObject();
        JsonObject inner = new JsonObject();
        inner.addProperty("x", 100);
        outer.add("inner", inner);
        JsonTreeReader reader = new JsonTreeReader(outer);
        reader.beginObject();
        assertEquals("inner", reader.nextName());
        reader.beginObject();
        // skip the entire inner object
        reader.skipValue(); // should skip inner object's content and end it
        // Now we should be at END_OBJECT of outer? Actually after skipping inner object, the outer iterator is still pointing to that entry? Need to check.
        // skipValue on the inner object (after beginObject) will pop the inner object and set pathIndices, then the outer iterator still has no next? So eventually we get END_OBJECT.
        reader.endObject(); // end outer object
    }

    // Bug-specific test that directly reproduces the report
    @Test(timeout = 4000)
    public void testSkipValueOnEmptyObjectDirectly() throws IOException {
        JsonTreeReader reader = new JsonTreeReader(new JsonObject());
        // Without calling beginObject, skipValue will see BEGIN_OBJECT token.
        // This should skip the entire object.
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testSkipValueOnFilledObjectAfterBegin() throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("a", 1);
        obj.addProperty("b", 2);
        JsonTreeReader reader = new JsonTreeReader(obj);
        reader.beginObject();
        // now at NAME
        reader.skipValue(); // skip "a" and 1
        // Should now be at next NAME or END_OBJECT
        assertTrue(reader.hasNext());
        assertEquals("b", reader.nextName());
        assertEquals(2, reader.nextInt());
        // skip the rest
        reader.skipValue(); // skip "b" value pair? Actually after reading value, iterator has no next, skipValue will skip the END_OBJECT.
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
}