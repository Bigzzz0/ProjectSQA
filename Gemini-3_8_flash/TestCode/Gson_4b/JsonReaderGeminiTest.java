/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.google.gson.stream.JsonReader
 *
 * 1. DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth):
 *    - Bug: Top-level primitive values (RFC 7159 compliance) in strict mode (lenient = false).
 *    - Defect Manifestation: Calling peek()/nextString()/nextBoolean()/nextDouble()/nextInt()/nextLong()/skipValue()
 *      on top-level literals causes checkLenient() invocation when stackSize == 1 in doPeek(), throwing:
 *      MalformedJsonException: "Use JsonReader.setLenient(true) to accept malformed JSON at line 1 column 1 path $"
 *    - Targeted Tests:
 *      - testTopLevelValueTypes_String()
 *      - testTopLevelValueTypes_Boolean()
 *      - testTopLevelValueTypes_Number()
 *      - testTopLevelValueTypeWithSkipValue()
 *
 * 2. CORE FUNCTIONAL LOGIC & STATE TRANSITIONS (Partition A):
 *    - Object traversal: beginObject, nextName, endObject, multiple keys, empty objects.
 *    - Array traversal: beginArray, next*, endArray, nested arrays, empty arrays.
 *    - Nested mixed documents: objects within arrays, arrays within objects.
 *    - Token peeking: peek() and hasNext() across every JsonToken variant.
 *    - Path tracking: getPath() verification ($[0], $.field, etc.) across push/pop transitions.
 *
 * 3. BOUNDARY VALUE ANALYSIS (BVA) & NUMERIC PARSING (Partition B):
 *    - Number machine: digits, signs (+, -), exponents (e, E, e+, e-, E+), decimals.
 *    - Long boundaries: Long.MIN_VALUE, Long.MAX_VALUE, overflow falling back to double/PEEKED_NUMBER.
 *    - Int boundaries: Integer.MIN_VALUE, Integer.MAX_VALUE, lossy casts throwing NumberFormatException.
 *    - Double parsing: standard, scientific, strict mode NaN/Infinity rejection vs lenient acceptance.
 *    - Buffer boundaries (buffer size 1024): spanning literal across fillBuffer boundaries.
 *
 * 4. EXTENDED SYNTAX, LENIENT BRANCHES, & COMMENTS (Partition C & D):
 *    - Non-execute prefix ")]}'\n" in lenient mode.
 *    - Unquoted strings and names, single-quoted strings and names.
 *    - Lenient separators: semicolon ';' instead of ',', '=' and '=>' name/value separators.
 *    - Comments: "//", "#", and "/* ... */" multi-line comments.
 *    - Escaped characters: \uXXXX, \n, \r, \t, \b, \f, \', \", \\, unescaped/unterminated sequences.
 *    - JsonReaderInternalAccess.INSTANCE.promoteNameToValue verification.
 *    - Defensive and exception paths: premature EOF, closed reader access, unmatched brackets/braces.
 */

package com.google.gson.stream;

import com.google.gson.internal.JsonReaderInternalAccess;
import org.junit.Test;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.*;

public class JsonReaderGeminiTest {

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testTopLevelValueTypes_String() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("\"hello world\""));
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals("hello world", reader.nextString());
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test(timeout = 4000)
  public void testTopLevelValueTypes_Boolean() throws IOException {
    JsonReader readerTrue = new JsonReader(new StringReader("true"));
    assertEquals(JsonToken.BOOLEAN, readerTrue.peek());
    assertTrue(readerTrue.nextBoolean());

    JsonReader readerFalse = new JsonReader(new StringReader("false"));
    assertEquals(JsonToken.BOOLEAN, readerFalse.peek());
    assertFalse(readerFalse.nextBoolean());
  }

  @Test(timeout = 4000)
  public void testTopLevelValueTypes_Number() throws IOException {
    JsonReader readerInt = new JsonReader(new StringReader("12345"));
    assertEquals(JsonToken.NUMBER, readerInt.peek());
    assertEquals(12345, readerInt.nextInt());

    JsonReader readerDouble = new JsonReader(new StringReader("12.34"));
    assertEquals(JsonToken.NUMBER, readerDouble.peek());
    assertEquals(12.34, readerDouble.nextDouble(), 0.0001);
  }

  @Test(timeout = 4000)
  public void testTopLevelValueTypes_Null() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("null"));
    assertEquals(JsonToken.NULL, reader.peek());
    reader.nextNull();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test(timeout = 4000)
  public void testTopLevelValueTypeWithSkipValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("12345"));
    reader.skipValue();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyArrayAndObject() throws IOException {
    JsonReader readerArray = new JsonReader(new StringReader("[]"));
    readerArray.beginArray();
    assertFalse(readerArray.hasNext());
    readerArray.endArray();
    assertEquals(JsonToken.END_DOCUMENT, readerArray.peek());

    JsonReader readerObject = new JsonReader(new StringReader("{}"));
    readerObject.beginObject();
    assertFalse(readerObject.hasNext());
    readerObject.endObject();
    assertEquals(JsonToken.END_DOCUMENT, readerObject.peek());
  }

  @Test(timeout = 4000)
  public void testComplexDocumentTraversalAndJsonPath() throws IOException {
    String json = "{\"users\": [{\"id\": 101, \"name\": \"Alice\", \"active\": true, \"extra\": null}]}";
    JsonReader reader = new JsonReader(new StringReader(json));

    assertEquals("$", reader.getPath());
    reader.beginObject();
    assertEquals("$.", reader.getPath());
    assertTrue(reader.hasNext());
    assertEquals("users", reader.nextName());
    assertEquals("$.users", reader.getPath());

    reader.beginArray();
    assertEquals("$.users[0]", reader.getPath());

    reader.beginObject();
    assertEquals("$.users[0].", reader.getPath());

    assertEquals("id", reader.nextName());
    assertEquals("$.users[0].id", reader.getPath());
    assertEquals(101, reader.nextInt());

    assertEquals("name", reader.nextName());
    assertEquals("Alice", reader.nextString());

    assertEquals("active", reader.nextName());
    assertTrue(reader.nextBoolean());

    assertEquals("extra", reader.nextName());
    reader.nextNull();

    reader.endObject();
    assertEquals("$.users[0]", reader.getPath());
    reader.endArray();
    assertEquals("$.users", reader.getPath());
    reader.endObject();
    assertEquals("$", reader.getPath());
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test(timeout = 4000)
  public void testSkipValueNestedStructures() throws IOException {
    String json = "{\"skipObj\": {\"a\": [1, 2, 3], \"b\": {\"c\": 4}}, \"keep\": 42}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginObject();
    assertEquals("skipObj", reader.nextName());
    reader.skipValue();
    assertEquals("keep", reader.nextName());
    assertEquals(42, reader.nextInt());
    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testSkipValuePrimitives() throws IOException {
    String json = "[\"string\", 123, 45.67, true, false, null, 'single']";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginArray();
    for (int i = 0; i < 7; i++) {
      reader.skipValue();
    }
    reader.endArray();
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Numbers
  // =========================================================================

  @Test(timeout = 4000)
  public void testLongBoundaries() throws IOException {
    String json = "[" + Long.MAX_VALUE + ", " + Long.MIN_VALUE + ", 0, -0]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    assertEquals(Long.MAX_VALUE, reader.nextLong());
    assertEquals(Long.MIN_VALUE, reader.nextLong());
    assertEquals(0L, reader.nextLong());
    assertEquals(0L, reader.nextLong());
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testIntBoundariesAndLossyExceptions() throws IOException {
    String json = "[" + Integer.MAX_VALUE + ", " + Integer.MIN_VALUE + ", " + (Integer.MAX_VALUE + 1L) + "]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    assertEquals(Integer.MAX_VALUE, reader.nextInt());
    assertEquals(Integer.MIN_VALUE, reader.nextInt());

    try {
      reader.nextInt();
      fail("Expected NumberFormatException for int overflow");
    } catch (NumberFormatException expected) {
      assertTrue(expected.getMessage().contains("Expected an int but was"));
    }
    // Now consume the value as long
    assertEquals(Integer.MAX_VALUE + 1L, reader.nextLong());
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testScientificAndFractionalDoubles() throws IOException {
    String json = "[1.25e2, -1.25E-2, 0.5, -0.5, 1e5, 1E+3]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    assertEquals(125.0, reader.nextDouble(), 0.0001);
    assertEquals(-0.0125, reader.nextDouble(), 0.000001);
    assertEquals(0.5, reader.nextDouble(), 0.0001);
    assertEquals(-0.5, reader.nextDouble(), 0.0001);
    assertEquals(100000.0, reader.nextDouble(), 0.0001);
    assertEquals(1000.0, reader.nextDouble(), 0.0001);
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testDoubleParsingFromStringAndStrictGuards() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"123.45\", \"NaN\", \"Infinity\"]"));
    reader.beginArray();
    assertEquals(123.45, reader.nextDouble(), 0.0001);

    try {
      reader.nextDouble();
      fail("NaN forbidden in strict mode");
    } catch (MalformedJsonException expected) {
      assertTrue(expected.getMessage().contains("JSON forbids NaN and infinities"));
    }

    reader.setLenient(true);
    assertTrue(Double.isNaN(reader.nextDouble()));
    assertTrue(Double.isInfinite(reader.nextDouble()));
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testLargeBufferSpanForLongStringAndNumbers() throws IOException {
    // Generate a long string larger than 1024 characters buffer
    StringBuilder sb = new StringBuilder("[\"");
    for (int i = 0; i < 1500; i++) {
      sb.append('a');
    }
    sb.append("\", 9223372036854775807]");
    JsonReader reader = new JsonReader(new StringReader(sb.toString()));
    reader.beginArray();
    String longStr = reader.nextString();
    assertEquals(1500, longStr.length());
    assertEquals(Long.MAX_VALUE, reader.nextLong());
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testVeryLongUnquotedLiteralSpanningBuffer() throws IOException {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 1100; i++) {
      sb.append('x');
    }
    JsonReader reader = new JsonReader(new StringReader(sb.toString()));
    reader.setLenient(true);
    assertEquals(sb.toString(), reader.nextString());
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  // =========================================================================
  // Partition D: Lenient Modes, Comments, Separators & Escape Sequences
  // =========================================================================

  @Test(timeout = 4000)
  public void testNonExecutePrefixInLenient() throws IOException {
    String json = ")]}'\n{\"key\":\"value\"}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("key", reader.nextName());
    assertEquals("value", reader.nextString());
    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testCommentsLenient() throws IOException {
    String json = "// comment line 1\n"
        + "/* comment line 2 \n multiline */\n"
        + "# hash comment\n"
        + "{\"a\": 1 /* comment inside */, \"b\": // eol comment\n 2}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("a", reader.nextName());
    assertEquals(1, reader.nextInt());
    assertEquals("b", reader.nextName());
    assertEquals(2, reader.nextInt());
    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testUnquotedAndSingleQuotedNamesAndValues() throws IOException {
    String json = "{name: 'Bob', 'age': 30, address: unquoted_val}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("name", reader.nextName());
    assertEquals("Bob", reader.nextString());
    assertEquals("age", reader.nextName());
    assertEquals(30, reader.nextInt());
    assertEquals("address", reader.nextName());
    assertEquals("unquoted_val", reader.nextString());
    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testAlternativeSeparatorsLenient() throws IOException {
    String json = "{key = 'val'; key2 => 'val2'}; [1; 2;]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginObject();
    assertEquals("key", reader.nextName());
    assertEquals("val", reader.nextString());
    assertEquals("key2", reader.nextName());
    assertEquals("val2", reader.nextString());
    reader.endObject();

    reader.beginArray();
    assertEquals(1, reader.nextInt());
    assertEquals(2, reader.nextInt());
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testEscapeSequences() throws IOException {
    String json = "[\"\\\"\", \"\\\\\", \"\\/\", \"\\b\", \"\\f\", \"\\n\", \"\\r\", \"\\t\", \"\\u0041\\u000a\"]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    assertEquals("\"", reader.nextString());
    assertEquals("\\", reader.nextString());
    assertEquals("/", reader.nextString());
    assertEquals("\b", reader.nextString());
    assertEquals("\f", reader.nextString());
    assertEquals("\n", reader.nextString());
    assertEquals("\r", reader.nextString());
    assertEquals("\t", reader.nextString());
    assertEquals("A\n", reader.nextString());
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testUtf8BomHandling() throws IOException {
    String json = "\ufeff{\"bom\": true}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginObject();
    assertEquals("bom", reader.nextName());
    assertTrue(reader.nextBoolean());
    reader.endObject();
  }

  // =========================================================================
  // Partition E: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testNullReaderThrows() {
    new JsonReader(null);
  }

  @Test(timeout = 4000)
  public void testCloseReaderAndOperationsAfterClose() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[]"));
    reader.close();
    try {
      reader.peek();
      fail("Expected IllegalStateException on closed reader");
    } catch (IllegalStateException expected) {
      assertEquals("JsonReader is closed", expected.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testMismatchedStructureExceptions() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[]"));
    try {
      reader.beginObject();
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
      assertTrue(expected.getMessage().contains("Expected BEGIN_OBJECT but was BEGIN_ARRAY"));
    }

    try {
      reader.endArray();
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
      assertTrue(expected.getMessage().contains("Expected END_ARRAY but was BEGIN_ARRAY"));
    }
  }

  @Test(timeout = 4000)
  public void testMalformedNumberFormatExceptions() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"not_a_number\"]"));
    reader.beginArray();
    try {
      reader.nextInt();
      fail("Expected NumberFormatException");
    } catch (NumberFormatException expected) {
      assertTrue(expected.getMessage().contains("Expected an int but was not_a_number"));
    }
  }

  @Test(timeout = 4000)
  public void testPrematureEOFException() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"unclosed\": "));
    reader.beginObject();
    assertEquals("unclosed", reader.nextName());
    try {
      reader.peek();
      fail("Expected EOFException");
    } catch (EOFException expected) {
      assertTrue(expected.getMessage().contains("End of input"));
    }
  }

  @Test(timeout = 4000)
  public void testUnterminatedCommentException() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("/* unclosed comment "));
    reader.setLenient(true);
    try {
      reader.peek();
      fail("Expected MalformedJsonException");
    } catch (MalformedJsonException expected) {
      assertTrue(expected.getMessage().contains("Unterminated comment"));
    }
  }

  @Test(timeout = 4000)
  public void testUnterminatedEscapeSequence() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"\\u12\"]"));
    reader.beginArray();
    try {
      reader.nextString();
      fail("Expected MalformedJsonException for unterminated escape");
    } catch (MalformedJsonException expected) {
      assertTrue(expected.getMessage().contains("Unterminated escape sequence"));
    }
  }

  @Test(timeout = 4000)
  public void testInternalAccessPromoteNameToValue() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"key\":\"val\"}"));
    reader.beginObject();
    assertEquals(JsonToken.NAME, reader.peek());
    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals("key", reader.nextString());
    assertEquals("val", reader.nextString());
    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testToStringAndLenientGetterSetter() {
    JsonReader reader = new JsonReader(new StringReader("{}"));
    assertFalse(reader.isLenient());
    reader.setLenient(true);
    assertTrue(reader.isLenient());
    assertTrue(reader.toString().contains("JsonReader at line 1 column 1"));
  }
}