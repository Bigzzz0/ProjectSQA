/*
 * [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------
 * Target Class: com.google.gson.internal.bind.JsonTreeReader
 * Primary Known Defects (Defects4J):
 *  - ArrayIndexOutOfBoundsException: -1 in skipValue() when invoked on top-level elements (empty or
 *    filled JsonObject / JsonArray / primitives) where stackSize becomes 0 after popStack() or where
 *    decrement causes negative index access on pathNames / pathIndices.
 *
 * Decision / Condition Coverage Targets:
 *  1. peek():
 *     - stackSize == 0 -> END_DOCUMENT
 *     - peekStack() instanceof Iterator:
 *         - stack[stackSize - 2] instanceof JsonObject (true/false)
 *         - iterator.hasNext() (true -> NAME or recursed push, false -> END_OBJECT or END_ARRAY)
 *     - peekStack() instanceof JsonObject -> BEGIN_OBJECT
 *     - peekStack() instanceof JsonArray -> BEGIN_ARRAY
 *     - peekStack() instanceof JsonPrimitive:
 *         - isString() -> STRING
 *         - isBoolean() -> BOOLEAN
 *         - isNumber() -> NUMBER
 *     - peekStack() instanceof JsonNull -> NULL
 *     - peekStack() == SENTINEL_CLOSED -> IllegalStateException("JsonReader is closed")
 *  2. beginArray() / endArray():
 *     - Valid transition, pathIndices increment when stackSize > 0.
 *     - IllegalStateException when token mismatch.
 *  3. beginObject() / endObject():
 *     - Valid transition, pathNames tracking, pathIndices increment when stackSize > 0.
 *     - IllegalStateException when token mismatch.
 *  4. hasNext():
 *     - Checks END_OBJECT and END_ARRAY conditions.
 *  5. nextName():
 *     - Correct extraction, updating pathNames.
 *     - Exception on non-NAME token.
 *  6. nextString(), nextBoolean(), nextNull(), nextDouble(), nextLong(), nextInt():
 *     - Token compatibility: STRING and NUMBER inter-conversion where supported.
 *     - Lenient vs strict mode handling for NaN and Infinity in nextDouble().
 *     - Exceptions on token mismatch.
 *  7. promoteNameToValue():
 *     - Invocation when peek() == NAME: promotes key and value to stack.
 *     - Exception when peek() != NAME.
 *  8. skipValue():
 *     - Branch 1: peek() == NAME -> advances key, sets pathNames to "null".
 *     - Branch 2: non-NAME -> pops stack and adjusts pathNames / pathIndices.
 *     - Boundary: top-level object/array/primitive skipping (defect trigger).
 *  9. push() resizing:
 *     - Array expansion when stackSize reaches initial capacity (32).
 *  10. getPath() & locationString():
 *     - Structural nesting across arrays (with index) and objects (with key).
 * --------------------------------------------------------------------------------------------------
 */

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

public class JsonTreeReaderGeminiTest {

  // ==============================================================================================
  // Partition C: Defect-Targeted Branch Zone (Ground Truth Defects4J Target)
  // ==============================================================================================

  /**
   * Targets Defect: testSkipValue_emptyJsonObject
   * Ground truth: skipValue() on an empty root JsonObject decrements stackSize to 0,
   * causing ArrayIndexOutOfBoundsException: -1 when attempting pathNames[stackSize - 1] / pathIndices.
   */
  @Test(timeout = 4000)
  public void testSkipValue_emptyJsonObject() throws IOException {
    JsonObject emptyObject = new JsonObject();
    JsonTreeReader reader = new JsonTreeReader(emptyObject);
    reader.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    assertEquals("$", reader.getPath());
  }

  /**
   * Targets Defect: testSkipValue_filledJsonObject
   * Ground truth: skipValue() on a filled root JsonObject triggers the same root popStack() bug.
   */
  @Test(timeout = 4000)
  public void testSkipValue_filledJsonObject() throws IOException {
    JsonObject filledObject = new JsonObject();
    filledObject.addProperty("key", "value");
    JsonTreeReader reader = new JsonTreeReader(filledObject);
    reader.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    assertEquals("$", reader.getPath());
  }

  /**
   * Targets Defect: skipValue() on a root primitive or array.
   */
  @Test(timeout = 4000)
  public void testSkipValue_rootPrimitiveAndArray() throws IOException {
    JsonTreeReader reader1 = new JsonTreeReader(new JsonPrimitive("scalar"));
    reader1.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader1.peek());

    JsonTreeReader reader2 = new JsonTreeReader(new JsonArray());
    reader2.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader2.peek());
  }

  // ==============================================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ==============================================================================================

  @Test(timeout = 4000)
  public void testObjectTraversal() throws IOException {
    JsonObject obj = new JsonObject();
    obj.addProperty("name", "Gson");
    obj.addProperty("age", 10);
    obj.add("empty", JsonNull.INSTANCE);

    JsonTreeReader reader = new JsonTreeReader(obj);
    assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
    reader.beginObject();

    assertTrue(reader.hasNext());
    assertEquals(JsonToken.NAME, reader.peek());
    assertEquals("name", reader.nextName());
    assertEquals("$.name", reader.getPath());
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals("Gson", reader.nextString());

    assertTrue(reader.hasNext());
    assertEquals("age", reader.nextName());
    assertEquals(JsonToken.NUMBER, reader.peek());
    assertEquals(10, reader.nextInt());

    assertTrue(reader.hasNext());
    assertEquals("empty", reader.nextName());
    assertEquals(JsonToken.NULL, reader.peek());
    reader.nextNull();

    assertFalse(reader.hasNext());
    assertEquals(JsonToken.END_OBJECT, reader.peek());
    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test(timeout = 4000)
  public void testArrayTraversal() throws IOException {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(true));
    array.add(new JsonPrimitive(1234567890123L));
    array.add(new JsonPrimitive(3.14159));

    JsonTreeReader reader = new JsonTreeReader(array);
    assertEquals(JsonToken.BEGIN_ARRAY, reader.peek());
    reader.beginArray();

    assertTrue(reader.hasNext());
    assertEquals(JsonToken.BOOLEAN, reader.peek());
    assertTrue(reader.nextBoolean());

    assertTrue(reader.hasNext());
    assertEquals(JsonToken.NUMBER, reader.peek());
    assertEquals(1234567890123L, reader.nextLong());

    assertTrue(reader.hasNext());
    assertEquals(JsonToken.NUMBER, reader.peek());
    assertEquals(3.14159, reader.nextDouble(), 1e-6);

    assertFalse(reader.hasNext());
    assertEquals(JsonToken.END_ARRAY, reader.peek());
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test(timeout = 4000)
  public void testPromoteNameToValue() throws IOException {
    JsonObject obj = new JsonObject();
    obj.addProperty("key1", "val1");
    JsonTreeReader reader = new JsonTreeReader(obj);
    reader.beginObject();

    assertEquals(JsonToken.NAME, reader.peek());
    reader.promoteNameToValue();

    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals("key1", reader.nextString());
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals("val1", reader.nextString());

    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test(timeout = 4000)
  public void testSkipValueInsideObjectAndArray() throws IOException {
    JsonObject obj = new JsonObject();
    obj.addProperty("skipMeKey", "val");
    obj.addProperty("keepMeKey", "kept");

    JsonTreeReader reader = new JsonTreeReader(obj);
    reader.beginObject();

    // Skip key and its value
    assertEquals("skipMeKey", reader.nextName());
    reader.skipValue(); // skips "val"

    // Next entry
    assertEquals("keepMeKey", reader.nextName());
    assertEquals("kept", reader.nextString());

    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test(timeout = 4000)
  public void testSkipValueWhenPeekIsName() throws IOException {
    JsonObject obj = new JsonObject();
    obj.addProperty("keyToSkip", "value");

    JsonTreeReader reader = new JsonTreeReader(obj);
    reader.beginObject();

    assertEquals(JsonToken.NAME, reader.peek());
    // skipValue() when peek() == NAME consumes the name and sets pathNames[stackSize - 2] to "null"
    reader.skipValue();

    // Now value remains to be read
    assertEquals("value", reader.nextString());
    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testNumberStringInterchangeability() throws IOException {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive("100"));
    array.add(new JsonPrimitive(200));

    JsonTreeReader reader = new JsonTreeReader(array);
    reader.beginArray();

    // Reading string formatted as number via nextInt / nextLong / nextDouble
    assertEquals(100, reader.nextInt());

    // Reading number formatted as string via nextString
    assertEquals("200", reader.nextString());

    reader.endArray();
  }

  // ==============================================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // ==============================================================================================

  @Test(timeout = 4000)
  public void testDeeplyNestedStackExpansion() throws IOException {
    // Initial stack size is 32. Nesting > 32 forces push() array expansion.
    final int depth = 40;
    JsonArray root = new JsonArray();
    JsonArray current = root;
    for (int i = 0; i < depth; i++) {
      JsonArray child = new JsonArray();
      current.add(child);
      current = child;
    }
    current.add(new JsonPrimitive("deepValue"));

    JsonTreeReader reader = new JsonTreeReader(root);
    for (int i = 0; i < depth + 1; i++) {
      reader.beginArray();
    }
    assertEquals("deepValue", reader.nextString());
    for (int i = 0; i < depth + 1; i++) {
      reader.endArray();
    }
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test(timeout = 4000)
  public void testPathFormatAcrossMixedNesting() throws IOException {
    JsonObject root = new JsonObject();
    JsonArray arr = new JsonArray();
    JsonObject nested = new JsonObject();
    nested.addProperty("prop", 99);
    arr.add(nested);
    root.add("items", arr);

    JsonTreeReader reader = new JsonTreeReader(root);
    assertEquals("$", reader.getPath());
    reader.beginObject();
    assertEquals("$.items", reader.getPath());
    reader.nextName();
    reader.beginArray();
    assertEquals("$.items[0]", reader.getPath());
    reader.beginObject();
    assertEquals("$.items[0].prop", reader.getPath());
    reader.nextName();
    assertEquals("$.items[0].prop", reader.getPath());
    assertEquals(99, reader.nextInt());
    reader.endObject();
    reader.endArray();
    reader.endObject();
    assertEquals("$", reader.getPath());
  }

  @Test(timeout = 4000)
  public void testLenientDoubles() throws IOException {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive(Double.NaN));
    array.add(new JsonPrimitive(Double.POSITIVE_INFINITY));
    array.add(new JsonPrimitive(Double.NEGATIVE_INFINITY));

    JsonTreeReader reader = new JsonTreeReader(array);
    reader.setLenient(true);
    reader.beginArray();

    assertTrue(Double.isNaN(reader.nextDouble()));
    assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble(), 0.0);
    assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble(), 0.0);

    reader.endArray();
  }

  @Test(expected = NumberFormatException.class, timeout = 4000)
  public void testStrictDoubleDisallowsNaN() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(Double.NaN));
    reader.setLenient(false);
    reader.nextDouble();
  }

  @Test(expected = NumberFormatException.class, timeout = 4000)
  public void testStrictDoubleDisallowsInfinity() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(Double.POSITIVE_INFINITY));
    reader.setLenient(false);
    reader.nextDouble();
  }

  // ==============================================================================================
  // Partition D: Exception & Defensive Guard Paths
  // ==============================================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testExpectMismatchBeginArray() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonObject());
    reader.beginArray();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testExpectMismatchEndArray() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonArray());
    reader.beginArray();
    reader.endObject(); // Mismatch
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testExpectMismatchBeginObject() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonArray());
    reader.beginObject();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testExpectMismatchEndObject() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonObject());
    reader.beginObject();
    reader.endArray(); // Mismatch
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNextNameThrowsWhenNotInObject() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonArray());
    reader.beginArray();
    reader.nextName();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNextStringMismatch() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(true));
    reader.nextString();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNextBooleanMismatch() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("notABool"));
    reader.nextBoolean();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNextNullMismatch() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(42));
    reader.nextNull();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNextDoubleMismatch() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(false));
    reader.nextDouble();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNextLongMismatch() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(false));
    reader.nextLong();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNextIntMismatch() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(true));
    reader.nextInt();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPromoteNameToValueThrowsWhenNotName() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonArray());
    reader.promoteNameToValue();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testOperationsAfterCloseThrowIllegalStateException() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("test"));
    reader.close();
    reader.peek();
  }

  // ==============================================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // ==============================================================================================

  @Test(timeout = 4000)
  public void testCloseIdempotency() throws IOException {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive("val"));
    reader.close();
    reader.close(); // Second close should not throw unexpected exception
    try {
      reader.peek();
      fail("Expected IllegalStateException after close");
    } catch (IllegalStateException expected) {
      assertEquals("JsonReader is closed", expected.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testToStringContract() {
    JsonTreeReader reader = new JsonTreeReader(new JsonPrimitive(1));
    assertEquals("JsonTreeReader", reader.toString());
  }
}