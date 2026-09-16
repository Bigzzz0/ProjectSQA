package com.google.gson.stream;

import com.google.gson.internal.JsonReaderInternalAccess;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: com.google.gson.stream.JsonReader
 * Defect Target (Defects4J):
 *   - Bug: nextInt() and nextLong() lack handling for PEEKED_UNQUOTED state. When unquoted
 *     keys/values are promoted via JsonReaderInternalAccess.promoteNameToValue() (or parsed
 *     in lenient mode), peeked is PEEKED_UNQUOTED. Calling nextInt() or nextLong() throws
 *     IllegalStateException ("Expected an int/long but was STRING").
 *   - Targeted in: Partition C (testPromotedUnquotedNameToInt, testPromotedUnquotedNameToLong).
 *
 * Partition Matrix:
 *   - Partition A: Core Functional Logic & State Transitions (Arrays, Objects, Literals, Paths)
 *   - Partition B: Boundary Value Analysis (Buffer boundaries, Long.MIN/MAX, Exponents, BOM)
 *   - Partition C: Defect-Targeted Branch Zone (Unquoted numeric promotion to int & long)
 *   - Partition D: Defensive Guard & Syntax Error Paths (Unterminated tokens, strict vs lenient)
 *   - Partition E: Object Lifecycle & Stream Protocol Integrity (Close, Comments, Prefixes)
 */
public class JsonReaderGeminiTest {

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone
  // =========================================================================

  @Test(timeout = 4000)
  public void testPromotedUnquotedNameToInt() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{123:\"value\"}"));
    reader.setLenient(true);
    reader.beginObject();
    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
    assertEquals(123, reader.nextInt());
    assertEquals("value", reader.nextString());
    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testPromotedUnquotedNameToLong() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{1234567890123:\"value\"}"));
    reader.setLenient(true);
    reader.beginObject();
    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
    assertEquals(1234567890123L, reader.nextLong());
    assertEquals("value", reader.nextString());
    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testPromoteDoubleQuotedNameToIntAndLong() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"100\":\"a\",\"20000000000\":\"b\"}"));
    reader.beginObject();

    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
    assertEquals(100, reader.nextInt());
    assertEquals("a", reader.nextString());

    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
    assertEquals(20000000000L, reader.nextLong());
    assertEquals("b", reader.nextString());

    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testPromoteSingleQuotedNameToIntAndLong() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{'50':'x','60000000000':'y'}"));
    reader.setLenient(true);
    reader.beginObject();

    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
    assertEquals(50, reader.nextInt());
    assertEquals("x", reader.nextString());

    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
    assertEquals(60000000000L, reader.nextLong());
    assertEquals("y", reader.nextString());

    reader.endObject();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPromoteNameToValueInvalidStateThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1, 2]"));
    reader.beginArray();
    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testReadBasicObjectAndArray() throws IOException {
    String json = "{\"name\":\"Alice\",\"age\":30,\"flags\":[true,false,null]}";
    JsonReader reader = new JsonReader(new StringReader(json));

    reader.beginObject();
    assertEquals("name", reader.nextName());
    assertEquals("Alice", reader.nextString());
    assertEquals("age", reader.nextName());
    assertEquals(30, reader.nextInt());
    assertEquals("flags", reader.nextName());

    reader.beginArray();
    assertTrue(reader.nextBoolean());
    assertFalse(reader.nextBoolean());
    reader.nextNull();
    reader.endArray();

    reader.endObject();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test(timeout = 4000)
  public void testPathTrackingTransitions() throws IOException {
    String json = "{\"users\":[{\"id\":1},{\"id\":2}]}";
    JsonReader reader = new JsonReader(new StringReader(json));

    assertEquals("$", reader.getPath());
    reader.beginObject();
    assertEquals("$.", reader.getPath());
    assertEquals("users", reader.nextName());
    assertEquals("$.users", reader.getPath());

    reader.beginArray();
    assertEquals("$.users[0]", reader.getPath());
    reader.beginObject();
    assertEquals("$.users[0].", reader.getPath());
    assertEquals("id", reader.nextName());
    assertEquals("$.users[0].id", reader.getPath());
    assertEquals(1, reader.nextInt());
    reader.endObject();
    assertEquals("$.users[1]", reader.getPath());

    reader.beginObject();
    assertEquals("id", reader.nextName());
    assertEquals(2, reader.nextInt());
    reader.endObject();
    assertEquals("$.users[2]", reader.getPath());

    reader.endArray();
    assertEquals("$.users", reader.getPath());
    reader.endObject();
    assertEquals("$", reader.getPath());
  }

  @Test(timeout = 4000)
  public void testSkipValueRecursiveObjectsAndArrays() throws IOException {
    String json = "{\"skipArray\":[1,2,[3,4]],\"skipObj\":{\"a\":{\"b\":2}},\"retain\":\"ok\"}";
    JsonReader reader = new JsonReader(new StringReader(json));

    reader.beginObject();
    assertEquals("skipArray", reader.nextName());
    reader.skipValue();
    assertEquals("skipObj", reader.nextName());
    reader.skipValue();
    assertEquals("retain", reader.nextName());
    assertEquals("ok", reader.nextString());
    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testSkipValuePrimitives() throws IOException {
    String json = "[123, \"str\", true, null, unquoted]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);

    reader.beginArray();
    reader.skipValue(); // 123
    reader.skipValue(); // "str"
    reader.skipValue(); // true
    reader.skipValue(); // null
    reader.skipValue(); // unquoted
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testDoubleParsingVariants() throws IOException {
    String json = "[0.0, -0.0, 1e5, 1E-5, 3.14159, \"4.5\", '6.7', NaN, -Infinity]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);

    reader.beginArray();
    assertEquals(0.0, reader.nextDouble(), 0.000001);
    assertEquals(-0.0, reader.nextDouble(), 0.000001);
    assertEquals(100000.0, reader.nextDouble(), 0.000001);
    assertEquals(0.00001, reader.nextDouble(), 0.00000001);
    assertEquals(3.14159, reader.nextDouble(), 0.000001);
    assertEquals(4.5, reader.nextDouble(), 0.000001);
    assertEquals(6.7, reader.nextDouble(), 0.000001);
    assertTrue(Double.isNaN(reader.nextDouble()));
    assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble(), 0.0);
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testNextStringFromVariousTokens() throws IOException {
    String json = "[123, 45.67, true, false]";
    JsonReader reader = new JsonReader(new StringReader(json));

    reader.beginArray();
    assertEquals("123", reader.nextString());
    assertEquals("45.67", reader.nextString());
    assertEquals("true", reader.nextString());
    assertEquals("false", reader.nextString());
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testBooleanKeywordsCaseInsensitiveInLenient() throws IOException {
    String json = "[TRUE, False, True, FALSE, NULL]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);

    reader.beginArray();
    assertTrue(reader.nextBoolean());
    assertFalse(reader.nextBoolean());
    assertTrue(reader.nextBoolean());
    assertFalse(reader.nextBoolean());
    reader.nextNull();
    reader.endArray();
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testLongBoundaryValues() throws IOException {
    String json = "[" + Long.MIN_VALUE + ", " + Long.MAX_VALUE + ", 0, -0]";
    JsonReader reader = new JsonReader(new StringReader(json));

    reader.beginArray();
    assertEquals(Long.MIN_VALUE, reader.nextLong());
    assertEquals(Long.MAX_VALUE, reader.nextLong());
    assertEquals(0L, reader.nextLong());
    assertEquals(0L, reader.nextLong());
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testIntBoundaryValues() throws IOException {
    String json = "[" + Integer.MIN_VALUE + ", " + Integer.MAX_VALUE + ", \"123\", '456']";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);

    reader.beginArray();
    assertEquals(Integer.MIN_VALUE, reader.nextInt());
    assertEquals(Integer.MAX_VALUE, reader.nextInt());
    assertEquals(123, reader.nextInt());
    assertEquals(456, reader.nextInt());
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testNumbersExceedingLongCapacity() throws IOException {
    String bigNum = "9223372036854775808"; // Long.MAX_VALUE + 1
    JsonReader reader = new JsonReader(new StringReader(bigNum));

    assertEquals(JsonToken.NUMBER, reader.peek());
    assertEquals(9.223372036854776E18, reader.nextDouble(), 1e10);
  }

  @Test(timeout = 4000)
  public void testStackExpansionBeyondInitialCapacity() throws IOException {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 40; i++) {
      sb.append('[');
    }
    sb.append("\"deep\"");
    for (int i = 0; i < 40; i++) {
      sb.append(']');
    }

    JsonReader reader = new JsonReader(new StringReader(sb.toString()));
    for (int i = 0; i < 40; i++) {
      reader.beginArray();
    }
    assertEquals("deep", reader.nextString());
    for (int i = 0; i < 40; i++) {
      reader.endArray();
    }
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test(timeout = 4000)
  public void testBufferFillingAcrossLargeLiteral() throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append("\"");
    for (int i = 0; i < 2048; i++) {
      sb.append('x');
    }
    sb.append("\"");

    JsonReader reader = new JsonReader(new StringReader(sb.toString()));
    String str = reader.nextString();
    assertEquals(2048, str.length());
  }

  @Test(timeout = 4000)
  public void testByteOrderMarkHandlingAtStart() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\ufeff{\"key\": 42}"));
    reader.beginObject();
    assertEquals("key", reader.nextName());
    assertEquals(42, reader.nextInt());
    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testEscapeSequences() throws IOException {
    String json = "\"\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0041\"";
    JsonReader reader = new JsonReader(new StringReader(json));
    assertEquals("\"\\/\b\f\n\r\tA", reader.nextString());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testNullReaderThrows() {
    new JsonReader(null);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBeginArrayWhenInObjectThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\":1}"));
    reader.beginArray();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testEndArrayWhenInObjectThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"a\":1}"));
    reader.beginObject();
    reader.endArray();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBeginObjectWhenInArrayThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1,2]"));
    reader.beginObject();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testEndObjectWhenInArrayThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1,2]"));
    reader.beginArray();
    reader.endObject();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNextNameInArrayThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1,2]"));
    reader.beginArray();
    reader.nextName();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNextBooleanOnNumberThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("123"));
    reader.nextBoolean();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNextNullOnStringThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\"text\""));
    reader.nextNull();
  }

  @Test(expected = NumberFormatException.class, timeout = 4000)
  public void testNextIntPrecisionLossThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("123.45"));
    reader.nextInt();
  }

  @Test(expected = NumberFormatException.class, timeout = 4000)
  public void testNextIntOverflowThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("9223372036854775807"));
    reader.nextInt();
  }

  @Test(expected = NumberFormatException.class, timeout = 4000)
  public void testNextLongPrecisionLossThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("123.45"));
    reader.nextLong();
  }

  @Test(expected = MalformedJsonException.class, timeout = 4000)
  public void testStrictForbidsNaN() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("NaN"));
    reader.nextDouble();
  }

  @Test(expected = MalformedJsonException.class, timeout = 4000)
  public void testStrictForbidsInfinity() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("Infinity"));
    reader.nextDouble();
  }

  @Test(expected = MalformedJsonException.class, timeout = 4000)
  public void testUnterminatedStringThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\"unterminated"));
    reader.nextString();
  }

  @Test(expected = NumberFormatException.class, timeout = 4000)
  public void testMalformedUnicodeEscapeThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\"\\u000G\""));
    reader.nextString();
  }

  @Test(expected = MalformedJsonException.class, timeout = 4000)
  public void testUnterminatedCommentThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("/* comment without end"));
    reader.setLenient(true);
    reader.peek();
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Comments, Prefixes & Syntax Flexibility
  // =========================================================================

  @Test(timeout = 4000)
  public void testNonExecutePrefixHandlingInLenient() throws IOException {
    String json = ")]}'\n{\"key\":\"value\"}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);

    reader.beginObject();
    assertEquals("key", reader.nextName());
    assertEquals("value", reader.nextString());
    reader.endObject();
  }

  @Test(expected = MalformedJsonException.class, timeout = 4000)
  public void testNonExecutePrefixThrowsInStrict() throws IOException {
    String json = ")]}'\n{\"key\":\"value\"}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginObject();
  }

  @Test(timeout = 4000)
  public void testLenientComments() throws IOException {
    String json = "// start comment\n"
        + "{\n"
        + "  # hash comment\n"
        + "  \"a\": /* inline comment */ 1\n"
        + "}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);

    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(1, reader.nextInt());
    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testLenientSeparatorsAndUnquotedTokens() throws IOException {
    String json = "{key = 'single' ; num => 100 , arr: [1; 2; , 4,] ;}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);

    reader.beginObject();
    assertEquals("key", reader.nextName());
    assertEquals("single", reader.nextString());
    assertEquals("num", reader.nextName());
    assertEquals(100, reader.nextInt());
    assertEquals("arr", reader.nextName());

    reader.beginArray();
    assertEquals(1, reader.nextInt());
    assertEquals(2, reader.nextInt());
    reader.nextNull(); // empty element ", ," parsed as null
    assertEquals(4, reader.nextInt());
    reader.endArray();

    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testCloseReaderLifecycle() throws IOException {
    Reader stringReader = new StringReader("[1, 2]");
    JsonReader reader = new JsonReader(stringReader);

    reader.beginArray();
    assertEquals(1, reader.nextInt());
    reader.close();

    try {
      reader.peek();
      fail("peek after close should throw IllegalStateException");
    } catch (IllegalStateException expected) {
      assertEquals("JsonReader is closed", expected.getMessage());
    }

    try {
      reader.hasNext();
      fail("hasNext after close should throw IllegalStateException");
    } catch (IllegalStateException expected) {
      assertEquals("JsonReader is closed", expected.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testToStringContract() {
    JsonReader reader = new JsonReader(new StringReader("[1]"));
    String str = reader.toString();
    assertNotNull(str);
    assertTrue(str.contains("JsonReader"));
    assertTrue(str.contains("line 1"));
    assertTrue(str.contains("column 1"));
  }

  @Test(expected = EOFException.class, timeout = 4000)
  public void testEmptyDocumentThrowsEOF() throws IOException {
    JsonReader reader = new JsonReader(new StringReader(""));
    reader.peek();
  }

  @Test(timeout = 4000)
  public void testLenientMultipleTopLevelValues() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("1 2 3"));
    reader.setLenient(true);

    assertTrue(reader.isLenient());
    assertEquals(1, reader.nextInt());
    assertEquals(2, reader.nextInt());
    assertEquals(3, reader.nextInt());
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }
}