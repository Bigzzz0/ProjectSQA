package com.google.gson.internal.bind;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target: com.google.gson.internal.bind.JsonTreeWriter
 *
 * 1. Defect-Targeted Zone (Known Defect: JsonWriterTest::testBoxedBooleans -> NPE / AssertionError):
 *    - value(Boolean value): Missing override in JsonTreeWriter causes delegation to JsonWriter
 *      which either fails unboxing null (NullPointerException) or writes to UNWRITABLE_WRITER (AssertionError).
 *      Branch paths evaluated: value((Boolean) null), value(Boolean.TRUE), value(Boolean.FALSE).
 *
 * 2. Decision Branches & State Transition Matrix:
 *    - get():
 *      * Branch !stack.isEmpty() -> throws IllegalStateException
 *      * Branch stack.isEmpty() -> returns product (defaults to JsonNull.INSTANCE)
 *    - put(JsonElement value):
 *      * Branch pendingName != null:
 *        - !value.isJsonNull() || getSerializeNulls() == true -> object.add(pendingName, value)
 *        - value.isJsonNull() && getSerializeNulls() == false -> ignored (not added to object)
 *      * Branch pendingName == null && stack.isEmpty() -> product = value
 *      * Branch pendingName == null && !stack.isEmpty():
 *        - peek() instanceof JsonArray -> array.add(value)
 *        - peek() not JsonArray (e.g., JsonObject with no name, or SENTINEL_CLOSED) -> IllegalStateException
 *    - beginArray() & endArray():
 *      * endArray on empty stack -> IllegalStateException
 *      * endArray with pendingName != null -> IllegalStateException
 *      * endArray when top is not JsonArray (e.g., JsonObject) -> IllegalStateException
 *      * endArray normal transition -> pops stack
 *    - beginObject() & endObject():
 *      * endObject on empty stack -> IllegalStateException
 *      * endObject with pendingName != null -> IllegalStateException
 *      * endObject when top is not JsonObject (e.g., JsonArray) -> IllegalStateException
 *      * endObject normal transition -> pops stack
 *    - name(String name):
 *      * name on empty stack -> IllegalStateException
 *      * name when pendingName != null (consecutive names) -> IllegalStateException
 *      * name when top is not JsonObject -> IllegalStateException
 *      * name normal -> sets pendingName
 *    - value(String):
 *      * null -> delegates to nullValue()
 *      * non-null -> new JsonPrimitive(value)
 *    - value(double) & value(Number):
 *      * !isLenient() && (NaN || Infinite) -> IllegalArgumentException
 *      * isLenient() && (NaN || Infinite) -> succeeds
 *      * value(Number) == null -> delegates to nullValue()
 *    - close():
 *      * !stack.isEmpty() -> IOException("Incomplete document")
 *      * stack.isEmpty() -> adds SENTINEL_CLOSED, subsequent operations fail
 */
public class JsonTreeWriterGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefaultInitialProductIsJsonNull() {
    JsonTreeWriter writer = new JsonTreeWriter();
    JsonElement element = writer.get();
    assertNotNull(element);
    assertTrue(element.isJsonNull());
    assertSame(JsonNull.INSTANCE, element);
  }

  @Test(timeout = 4000)
  public void testSinglePrimitiveRootValues() throws IOException {
    JsonTreeWriter w1 = new JsonTreeWriter();
    w1.value("hello");
    assertEquals(new JsonPrimitive("hello"), w1.get());

    JsonTreeWriter w2 = new JsonTreeWriter();
    w2.value(true);
    assertEquals(new JsonPrimitive(true), w2.get());

    JsonTreeWriter w3 = new JsonTreeWriter();
    w3.value(false);
    assertEquals(new JsonPrimitive(false), w3.get());

    JsonTreeWriter w4 = new JsonTreeWriter();
    w4.value(12345L);
    assertEquals(new JsonPrimitive(12345L), w4.get());

    JsonTreeWriter w5 = new JsonTreeWriter();
    w5.value(3.14159);
    assertEquals(new JsonPrimitive(3.14159), w5.get());

    JsonTreeWriter w6 = new JsonTreeWriter();
    w6.nullValue();
    assertEquals(JsonNull.INSTANCE, w6.get());
  }

  @Test(timeout = 4000)
  public void testArrayTransitionsAndNesting() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    assertSame(writer, writer.beginArray());
    assertSame(writer, writer.value("first"));
    assertSame(writer, writer.beginArray());
    assertSame(writer, writer.value(2L));
    assertSame(writer, writer.endArray());
    assertSame(writer, writer.endArray());

    JsonElement result = writer.get();
    assertTrue(result.isJsonArray());
    JsonArray rootArray = result.getAsJsonArray();
    assertEquals(2, rootArray.size());
    assertEquals("first", rootArray.get(0).getAsString());

    JsonArray nestedArray = rootArray.get(1).getAsJsonArray();
    assertEquals(1, nestedArray.size());
    assertEquals(2, nestedArray.get(0).getAsInt());
  }

  @Test(timeout = 4000)
  public void testObjectTransitionsAndNesting() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    assertSame(writer, writer.beginObject());
    assertSame(writer, writer.name("k1"));
    assertSame(writer, writer.value("v1"));
    assertSame(writer, writer.name("nested"));
    assertSame(writer, writer.beginObject());
    assertSame(writer, writer.name("innerKey"));
    assertSame(writer, writer.value(99L));
    assertSame(writer, writer.endObject());
    assertSame(writer, writer.endObject());

    JsonElement result = writer.get();
    assertTrue(result.isJsonObject());
    JsonObject rootObj = result.getAsJsonObject();
    assertEquals("v1", rootObj.get("k1").getAsString());

    JsonObject innerObj = rootObj.getAsJsonObject("nested");
    assertNotNull(innerObj);
    assertEquals(99, innerObj.get("innerKey").getAsInt());
  }

  @Test(timeout = 4000)
  public void testFlushDoesNotAlterState() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.value("item");
    writer.flush();
    writer.endArray();
    writer.flush();

    JsonArray array = (JsonArray) writer.get();
    assertEquals(1, array.size());
    assertEquals("item", array.get(0).getAsString());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyArrayAndObject() throws IOException {
    JsonTreeWriter w1 = new JsonTreeWriter();
    w1.beginArray().endArray();
    assertTrue(w1.get().isJsonArray());
    assertEquals(0, w1.get().getAsJsonArray().size());

    JsonTreeWriter w2 = new JsonTreeWriter();
    w2.beginObject().endObject();
    assertTrue(w2.get().isJsonObject());
    assertEquals(0, w2.get().getAsJsonObject().entrySet().size());
  }

  @Test(timeout = 4000)
  public void testSerializeNullsTruePreservesNullsInObject() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setSerializeNulls(true);
    writer.beginObject();
    writer.name("explicitNull");
    writer.nullValue();
    writer.name("stringNull");
    writer.value((String) null);
    writer.name("numberNull");
    writer.value((Number) null);
    writer.endObject();

    JsonObject obj = (JsonObject) writer.get();
    assertEquals(3, obj.entrySet().size());
    assertTrue(obj.get("explicitNull").isJsonNull());
    assertTrue(obj.get("stringNull").isJsonNull());
    assertTrue(obj.get("numberNull").isJsonNull());
  }

  @Test(timeout = 4000)
  public void testSerializeNullsFalseOmitsNullsInObject() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setSerializeNulls(false);
    writer.beginObject();
    writer.name("kept");
    writer.value("present");
    writer.name("omitted1");
    writer.nullValue();
    writer.name("omitted2");
    writer.value((String) null);
    writer.name("omitted3");
    writer.value((Number) null);
    writer.endObject();

    JsonObject obj = (JsonObject) writer.get();
    assertEquals(1, obj.entrySet().size());
    assertEquals("present", obj.get("kept").getAsString());
    assertNull(obj.get("omitted1"));
    assertNull(obj.get("omitted2"));
    assertNull(obj.get("omitted3"));
  }

  @Test(timeout = 4000)
  public void testSerializeNullsFalseStillIncludesNullsInArray() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setSerializeNulls(false);
    writer.beginArray();
    writer.nullValue();
    writer.value((String) null);
    writer.value((Number) null);
    writer.endArray();

    JsonArray array = (JsonArray) writer.get();
    assertEquals(3, array.size());
    assertTrue(array.get(0).isJsonNull());
    assertTrue(array.get(1).isJsonNull());
    assertTrue(array.get(2).isJsonNull());
  }

  @Test(timeout = 4000)
  public void testLongBoundaryValues() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.value(Long.MIN_VALUE);
    writer.value(0L);
    writer.value(Long.MAX_VALUE);
    writer.endArray();

    JsonArray array = (JsonArray) writer.get();
    assertEquals(Long.MIN_VALUE, array.get(0).getAsLong());
    assertEquals(0L, array.get(1).getAsLong());
    assertEquals(Long.MAX_VALUE, array.get(2).getAsLong());
  }

  @Test(timeout = 4000)
  public void testDoubleBoundaryValuesNonLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.value(Double.MIN_VALUE);
    writer.value(-0.0);
    writer.value(0.0);
    writer.value(Double.MAX_VALUE);
    writer.endArray();

    JsonArray array = (JsonArray) writer.get();
    assertEquals(Double.MIN_VALUE, array.get(0).getAsDouble(), 0.0);
    assertEquals(-0.0, array.get(1).getAsDouble(), 0.0);
    assertEquals(0.0, array.get(2).getAsDouble(), 0.0);
    assertEquals(Double.MAX_VALUE, array.get(3).getAsDouble(), 0.0);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Boxed Booleans Defect)
  // =========================================================================

  @Test(timeout = 4000)
  public void testBoxedBooleansInArray() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.value(Boolean.TRUE);
    writer.value(Boolean.FALSE);
    writer.value((Boolean) null);
    writer.endArray();

    JsonArray array = (JsonArray) writer.get();
    assertEquals(3, array.size());
    assertEquals(Boolean.TRUE, array.get(0).getAsBoolean());
    assertEquals(Boolean.FALSE, array.get(1).getAsBoolean());
    assertTrue(array.get(2).isJsonNull());
  }

  @Test(timeout = 4000)
  public void testBoxedBooleanRootNull() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value((Boolean) null);
    JsonElement element = writer.get();
    assertTrue(element.isJsonNull());
  }

  @Test(timeout = 4000)
  public void testBoxedBooleanRootValues() throws IOException {
    JsonTreeWriter w1 = new JsonTreeWriter();
    w1.value(Boolean.TRUE);
    assertEquals(new JsonPrimitive(true), w1.get());

    JsonTreeWriter w2 = new JsonTreeWriter();
    w2.value(Boolean.FALSE);
    assertEquals(new JsonPrimitive(false), w2.get());
  }

  @Test(timeout = 4000)
  public void testBoxedBooleansInObject() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("bTrue").value(Boolean.TRUE);
    writer.name("bFalse").value(Boolean.FALSE);
    writer.name("bNull").value((Boolean) null);
    writer.endObject();

    JsonObject obj = (JsonObject) writer.get();
    assertEquals(Boolean.TRUE, obj.get("bTrue").getAsBoolean());
    assertEquals(Boolean.FALSE, obj.get("bFalse").getAsBoolean());
    assertTrue(obj.get("bNull").isJsonNull());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testGetThrowsWhenDocumentUnclosedArray() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.get();
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testGetThrowsWhenDocumentUnclosedObject() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.get();
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testEndArrayOnEmptyStack() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.endArray();
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testEndArrayWhenObjectOnTop() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.endArray();
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testEndArrayWithPendingName() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    // Simulate invalid state using reflection or invalid order
    writer.beginObject();
    writer.name("key");
    writer.endArray();
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testEndObjectOnEmptyStack() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.endObject();
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testEndObjectWhenArrayOnTop() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.endObject();
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testEndObjectWithPendingName() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("orphanName");
    writer.endObject();
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testNameOnEmptyStack() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.name("nameWithoutObject");
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testNameInsideArray() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.name("nameInArray");
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testConsecutiveNamesWithoutValue() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.name("firstName");
    writer.name("secondName");
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testPutValueInObjectWithoutName() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.value("namelessValue");
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testDoubleNaNThrowsWhenNonLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(false);
    writer.value(Double.NaN);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testDoublePositiveInfinityThrowsWhenNonLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(false);
    writer.value(Double.POSITIVE_INFINITY);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testDoubleNegativeInfinityThrowsWhenNonLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(false);
    writer.value(Double.NEGATIVE_INFINITY);
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testNumberNaNThrowsWhenNonLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(false);
    writer.value(Double.valueOf(Double.NaN));
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testNumberPositiveInfinityThrowsWhenNonLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(false);
    writer.value(Float.valueOf(Float.POSITIVE_INFINITY));
  }

  @Test(timeout = 4000, expected = IllegalArgumentException.class)
  public void testNumberNegativeInfinityThrowsWhenNonLenient() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(false);
    writer.value(Double.valueOf(Double.NEGATIVE_INFINITY));
  }

  // =========================================================================
  // Partition E: Lenient Mode, Number Subtypes & Close Lifecycle
  // =========================================================================

  @Test(timeout = 4000)
  public void testLenientDoubleNaNAndInfinities() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.beginArray();
    writer.value(Double.NaN);
    writer.value(Double.POSITIVE_INFINITY);
    writer.value(Double.NEGATIVE_INFINITY);
    writer.endArray();

    JsonArray array = (JsonArray) writer.get();
    assertEquals(Double.NaN, array.get(0).getAsDouble(), 0.0);
    assertEquals(Double.POSITIVE_INFINITY, array.get(1).getAsDouble(), 0.0);
    assertEquals(Double.NEGATIVE_INFINITY, array.get(2).getAsDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testLenientNumberNaNAndInfinities() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.setLenient(true);
    writer.beginArray();
    writer.value(Double.valueOf(Double.NaN));
    writer.value(Float.valueOf(Float.POSITIVE_INFINITY));
    writer.value(Double.valueOf(Double.NEGATIVE_INFINITY));
    writer.endArray();

    JsonArray array = (JsonArray) writer.get();
    assertEquals(Double.NaN, array.get(0).getAsDouble(), 0.0);
    assertEquals(Float.POSITIVE_INFINITY, array.get(1).getAsFloat(), 0.0f);
    assertEquals(Double.NEGATIVE_INFINITY, array.get(2).getAsDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testNumberSubtypes() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.value(Byte.valueOf((byte) 12));
    writer.value(Short.valueOf((short) 1234));
    writer.value(Integer.valueOf(123456));
    writer.value(Long.valueOf(12345678901L));
    writer.value(Float.valueOf(1.25f));
    writer.value(new BigDecimal("12345678901234567890.123456789"));
    writer.value(new BigInteger("98765432109876543210"));
    writer.endArray();

    JsonArray array = (JsonArray) writer.get();
    assertEquals((byte) 12, array.get(0).getAsByte());
    assertEquals((short) 1234, array.get(1).getAsShort());
    assertEquals(123456, array.get(2).getAsInt());
    assertEquals(12345678901L, array.get(3).getAsLong());
    assertEquals(1.25f, array.get(4).getAsFloat(), 0.0f);
    assertEquals(new BigDecimal("12345678901234567890.123456789"), array.get(5).getAsBigDecimal());
    assertEquals(new BigInteger("98765432109876543210"), array.get(6).getAsBigInteger());
  }

  @Test(timeout = 4000, expected = IOException.class)
  public void testCloseThrowsOnIncompleteArray() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginArray();
    writer.close();
  }

  @Test(timeout = 4000, expected = IOException.class)
  public void testCloseThrowsOnIncompleteObject() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.beginObject();
    writer.close();
  }

  @Test(timeout = 4000)
  public void testCloseSuccessAndSentinelPreventsFurtherOps() throws IOException {
    JsonTreeWriter writer = new JsonTreeWriter();
    writer.value("complete");
    writer.close();

    try {
      writer.value("afterClose");
      fail("Expected IllegalStateException on value() after close");
    } catch (IllegalStateException expected) {
      // Success: Sentinel blocked mutation
    }

    try {
      writer.beginArray();
      fail("Expected IllegalStateException on beginArray() after close");
    } catch (IllegalStateException expected) {
      // Success: Sentinel blocked mutation
    }

    try {
      writer.beginObject();
      fail("Expected IllegalStateException on beginObject() after close");
    } catch (IllegalStateException expected) {
      // Success: Sentinel blocked mutation
    }

    try {
      writer.get();
      fail("Expected IllegalStateException on get() after close");
    } catch (IllegalStateException expected) {
      // Success: stack not empty because of SENTINEL_CLOSED
    }

    try {
      writer.close();
      fail("Expected IOException on second close()");
    } catch (IOException expected) {
      // Success: stack has SENTINEL_CLOSED -> "Incomplete document"
    }
  }
}