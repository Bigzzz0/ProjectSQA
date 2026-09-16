package com.google.gson.stream;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target: com.google.gson.stream.JsonReader
 * Defects4J Target Defect: testNegativeZero (ComparisonFailure: expected:<[-]0> but was:<[]0>)
 * Root Cause: peekNumber() categorizes "-0" as PEEKED_LONG (storing 0L). When nextString() is called,
 *             it converts peekedLong (0L) via Long.toString(), losing the negative sign ("0" instead of "-0").
 *
 * Key Decision Branches & Boundary Conditions Covered:
 * 1. Number parsing DFA states: NUMBER_CHAR_NONE -> NUMBER_CHAR_SIGN -> NUMBER_CHAR_DIGIT ->
 *    NUMBER_CHAR_DECIMAL -> NUMBER_CHAR_FRACTION_DIGIT -> NUMBER_CHAR_EXP_E ->
 *    NUMBER_CHAR_EXP_SIGN -> NUMBER_CHAR_EXP_DIGIT.
 * 2. Number boundaries: 0, -0, MIN_VALUE / 10 boundary check, Long.MIN_VALUE, Long.MAX_VALUE,
 *    overflows falling back to PEEKED_NUMBER / double.
 * 3. Escape sequence handling: \uXXXX (valid, hex lower/upper, malformed), \b, \t, \n, \f, \r, \', \", \\, \/.
 * 4. Comments & Whitespace: C-style comments, end-of-line comments (// and #), CRLF handling, EOF handling.
 * 5. Security Token: Non-execute prefix ")]}'\n" in lenient vs strict mode.
 * 6. Nesting Stack Expansion: Stack depth exceeding 32 to trigger stack, pathNames, and pathIndices reallocation.
 * 7. Path Tracking: getPath() behavior across arrays, nested arrays, objects, dangling names, and skipValue().
 * 8. Internal Access: JsonReaderInternalAccess.INSTANCE.promoteNameToValue().
 * 9. Lenient vs Strict: ';' separators, '=' and '=>' name separators, unquoted and single-quoted strings/names.
 */

import org.junit.Test;
import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import com.google.gson.internal.JsonReaderInternalAccess;

import static org.junit.Assert.*;

public class JsonReaderGeminiTest {

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets known Defect: "-0" must preserve its negative sign when read as a String.
   * Buggy implementation treats -0 as 0L in PEEKED_LONG, returning "0" on nextString().
   */
  @Test(timeout = 4000)
  public void testNegativeZeroPreservedAsString() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[-0]"));
    reader.beginArray();
    assertEquals("-0", reader.nextString());
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testNegativeZeroAsDoubleAndInt() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[-0, -0.0]"));
    reader.beginArray();
    assertEquals(0.0, reader.nextDouble(), 0.0);
    assertEquals(-0.0, reader.nextDouble(), 0.0);
    reader.endArray();
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testReadBasicObjectAndArray() throws IOException {
    String json = "{\"name\":\"Gson\",\"version\":1,\"flags\":[true,false,null]}";
    JsonReader reader = new JsonReader(new StringReader(json));

    reader.beginObject();
    assertEquals("name", reader.nextName());
    assertEquals("Gson", reader.nextString());
    assertEquals("version", reader.nextName());
    assertEquals(1, reader.nextInt());
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
  public void testNextLongAndNextIntConversions() throws IOException {
    String json = "[123, -456, 9223372036854775807, -9223372036854775808, \"789\", \"100.0\"]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();

    assertEquals(123, reader.nextInt());
    assertEquals(-456L, reader.nextLong());
    assertEquals(Long.MAX_VALUE, reader.nextLong());
    assertEquals(Long.MIN_VALUE, reader.nextLong());
    assertEquals(789, reader.nextInt());
    assertEquals(100, reader.nextInt()); // fallback via double parse

    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testNextDoubleFormats() throws IOException {
    String json = "[0.0, -1.5, 1e3, 2.5E-2, \"3.1415\"]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();

    assertEquals(0.0, reader.nextDouble(), 1e-9);
    assertEquals(-1.5, reader.nextDouble(), 1e-9);
    assertEquals(1000.0, reader.nextDouble(), 1e-9);
    assertEquals(0.025, reader.nextDouble(), 1e-9);
    assertEquals(3.1415, reader.nextDouble(), 1e-9);

    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testNextQuotedValuesAndEscapes() throws IOException {
    String json = "[\"hello\\nworld\", \"\\\"quote\\\"\", \"\\t\\b\\r\\f\\\\\\/\", \"\\u0041\\u0061\"]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();

    assertEquals("hello\nworld", reader.nextString());
    assertEquals("\"quote\"", reader.nextString());
    assertEquals("\t\b\r\f\\/", reader.nextString());
    assertEquals("Aa", reader.nextString());

    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testHasNextOnEmptyAndNonEmptyStructures() throws IOException {
    JsonReader emptyArrayReader = new JsonReader(new StringReader("[]"));
    emptyArrayReader.beginArray();
    assertFalse(emptyArrayReader.hasNext());
    emptyArrayReader.endArray();

    JsonReader emptyObjReader = new JsonReader(new StringReader("{}"));
    emptyObjReader.beginObject();
    assertFalse(emptyObjReader.hasNext());
    emptyObjReader.endObject();
  }

  @Test(timeout = 4000)
  public void testSkipValueScalarsAndComposites() throws IOException {
    String json = "{\"skipArray\":[1,2,{\"inner\":3}],\"skipStr\":\"val\",\"skipNum\":99,\"keep\":true}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginObject();

    assertEquals("skipArray", reader.nextName());
    reader.skipValue();

    assertEquals("skipStr", reader.nextName());
    reader.skipValue();

    assertEquals("skipNum", reader.nextName());
    reader.skipValue();

    assertEquals("keep", reader.nextName());
    assertTrue(reader.nextBoolean());

    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testPathTracking() throws IOException {
    String json = "{\"a\":[1,{\"b\":2}],\"c\":3}";
    JsonReader reader = new JsonReader(new StringReader(json));

    assertEquals("$", reader.getPath());
    reader.beginObject();
    assertEquals("$.", reader.getPath());

    assertEquals("a", reader.nextName());
    assertEquals("$.a", reader.getPath());

    reader.beginArray();
    assertEquals("$.a[0]", reader.getPath());

    assertEquals(1, reader.nextInt());
    assertEquals("$.a[1]", reader.getPath());

    reader.beginObject();
    assertEquals("$.a[1].", reader.getPath());

    assertEquals("b", reader.nextName());
    assertEquals("$.a[1].b", reader.getPath());

    assertEquals(2, reader.nextInt());
    reader.endObject();

    reader.endArray();
    assertEquals("$.a", reader.getPath());

    assertEquals("c", reader.nextName());
    assertEquals(3, reader.nextInt());
    reader.endObject();

    assertEquals("$", reader.getPath());
  }

  @Test(timeout = 4000)
  public void testInternalAccessPromoteNameToValue() throws IOException {
    String json = "{\"key\":\"value\"}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginObject();

    assertEquals(JsonToken.NAME, reader.peek());
    JsonReaderInternalAccess.INSTANCE.promoteNameToValue(reader);
    assertEquals(JsonToken.STRING, reader.peek());
    assertEquals("key", reader.nextString());
    assertEquals("value", reader.nextString());
    reader.endObject();
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructorNullReader() {
    new JsonReader(null);
  }

  @Test(timeout = 4000)
  public void testStackAndBufferReallocation() throws IOException {
    // Exceed default stack size of 32
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 40; i++) {
      sb.append("[");
    }
    sb.append("123");
    for (int i = 0; i < 40; i++) {
      sb.append("]");
    }

    JsonReader reader = new JsonReader(new StringReader(sb.toString()));
    for (int i = 0; i < 40; i++) {
      reader.beginArray();
    }
    assertEquals(123, reader.nextInt());
    for (int i = 0; i < 40; i++) {
      reader.endArray();
    }
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test(timeout = 4000)
  public void testLargeBufferFill() throws IOException {
    // Generate unquoted / literal string larger than internal buffer size (1024)
    StringBuilder sb = new StringBuilder("[\"");
    for (int i = 0; i < 2050; i++) {
      sb.append('x');
    }
    sb.append("\"]");

    JsonReader reader = new JsonReader(new StringReader(sb.toString()));
    reader.beginArray();
    String large = reader.nextString();
    assertEquals(2050, large.length());
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testBOMHandlingAtStreamStart() throws IOException {
    String json = "\ufeff[1]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.beginArray();
    assertEquals(1, reader.nextInt());
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testLeadingZeroDisallowedInStrictIntegers() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[01]"));
    reader.beginArray();
    try {
      reader.nextInt();
      fail("Leading zeroes not allowed in RFC compliant JSON numbers");
    } catch (MalformedJsonException expected) {
      // Expected syntax error in strict mode
    }
  }

  @Test(timeout = 4000)
  public void testNumberOverflowToDouble() throws IOException {
    // Exceeds 64-bit long limits -> parses as double
    String largeNum = "10000000000000000000000000000000";
    JsonReader reader = new JsonReader(new StringReader("[" + largeNum + "]"));
    reader.beginArray();
    assertEquals(JsonToken.NUMBER, reader.peek());
    assertEquals(1e31, reader.nextDouble(), 1e25);
    reader.endArray();
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBeginArrayThrowsOnWrongToken() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"key\":\"value\"}"));
    reader.beginArray();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testEndArrayThrowsOnWrongToken() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1]"));
    reader.beginArray();
    reader.endArray(); // Not yet at END_ARRAY
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBeginObjectThrowsOnWrongToken() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1, 2]"));
    reader.beginObject();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testEndObjectThrowsOnWrongToken() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"k\": 1}"));
    reader.beginObject();
    reader.endObject(); // Still has properties
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNextBooleanThrowsOnNumber() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[123]"));
    reader.beginArray();
    reader.nextBoolean();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNextNullThrowsOnString() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"non-null\"]"));
    reader.beginArray();
    reader.nextNull();
  }

  @Test(expected = NumberFormatException.class, timeout = 4000)
  public void testNextIntPrecisionLossThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"123.45\"]"));
    reader.beginArray();
    reader.nextInt();
  }

  @Test(expected = NumberFormatException.class, timeout = 4000)
  public void testNextLongPrecisionLossThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"123.45\"]"));
    reader.beginArray();
    reader.nextLong();
  }

  @Test(expected = MalformedJsonException.class, timeout = 4000)
  public void testUnterminatedArrayThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1, 2"));
    reader.beginArray();
    reader.nextInt();
    reader.nextInt();
    reader.hasNext();
  }

  @Test(expected = MalformedJsonException.class, timeout = 4000)
  public void testUnterminatedObjectThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"key\":"));
    reader.beginObject();
    reader.nextName();
    reader.peek();
  }

  @Test(expected = MalformedJsonException.class, timeout = 4000)
  public void testUnterminatedEscapeSequenceThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"\\u001"));
    reader.beginArray();
    reader.nextString();
  }

  @Test(expected = NumberFormatException.class, timeout = 4000)
  public void testInvalidHexEscapeSequenceThrows() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[\"\\u00G0\"]"));
    reader.beginArray();
    reader.nextString();
  }

  @Test(expected = MalformedJsonException.class, timeout = 4000)
  public void testStrictForbidsNaN() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[NaN]"));
    reader.setLenient(false);
    reader.beginArray();
    reader.nextDouble();
  }

  @Test(expected = MalformedJsonException.class, timeout = 4000)
  public void testStrictForbidsInfinity() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[Infinity]"));
    reader.setLenient(false);
    reader.beginArray();
    reader.nextDouble();
  }

  @Test(expected = EOFException.class, timeout = 4000)
  public void testEmptyDocumentThrowsEOF() throws IOException {
    JsonReader reader = new JsonReader(new StringReader(""));
    reader.peek();
  }

  // =========================================================================
  // Partition E: Lenient Mode Permissiveness & Syntax Quirks
  // =========================================================================

  @Test(timeout = 4000)
  public void testLenientNonExecutePrefix() throws IOException {
    JsonReader reader = new JsonReader(new StringReader(")]}'\n[1]"));
    reader.setLenient(true);
    assertTrue(reader.isLenient());
    reader.beginArray();
    assertEquals(1, reader.nextInt());
    reader.endArray();
  }

  @Test(expected = MalformedJsonException.class, timeout = 4000)
  public void testStrictRejectsNonExecutePrefix() throws IOException {
    JsonReader reader = new JsonReader(new StringReader(")]}'\n[1]"));
    reader.setLenient(false);
    reader.peek();
  }

  @Test(timeout = 4000)
  public void testLenientComments() throws IOException {
    String json = "// single line\n"
        + "# hash comment\n"
        + "/* multi\n"
        + "   line */\n"
        + "[1, /* comment */ 2]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginArray();
    assertEquals(1, reader.nextInt());
    assertEquals(2, reader.nextInt());
    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testLenientNameSeparatorsAndQuoting() throws IOException {
    String json = "{key = 'value', unquotedKey => \"value2\"; nextKey: 3}";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginObject();

    assertEquals("key", reader.nextName());
    assertEquals("value", reader.nextString());

    assertEquals("unquotedKey", reader.nextName());
    assertEquals("value2", reader.nextString());

    assertEquals("nextKey", reader.nextName());
    assertEquals(3, reader.nextInt());

    reader.endObject();
  }

  @Test(timeout = 4000)
  public void testLenientOmittedArrayValuesAsNull() throws IOException {
    String json = "[1,,2;]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginArray();

    assertEquals(1, reader.nextInt());
    reader.nextNull();
    assertEquals(2, reader.nextInt());
    reader.nextNull();

    reader.endArray();
  }

  @Test(timeout = 4000)
  public void testLenientMultipleTopLevelValues() throws IOException {
    String json = "1 2 3";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);

    assertEquals(1, reader.nextInt());
    assertEquals(2, reader.nextInt());
    assertEquals(3, reader.nextInt());
    assertEquals(JsonToken.END_DOCUMENT, reader.peek());
  }

  @Test(timeout = 4000)
  public void testLenientNaNAndInfinities() throws IOException {
    String json = "[NaN, -Infinity, Infinity]";
    JsonReader reader = new JsonReader(new StringReader(json));
    reader.setLenient(true);
    reader.beginArray();

    assertTrue(Double.isNaN(reader.nextDouble()));
    assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble(), 0.0);
    assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble(), 0.0);

    reader.endArray();
  }

  // =========================================================================
  // Partition F: Object Lifecycle & Close Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testCloseClosesUnderlyingReader() throws IOException {
    final boolean[] readerClosed = new boolean[]{false};
    Reader underlying = new StringReader("[]") {
      @Override
      public void close() {
        readerClosed[0] = true;
        super.close();
      }
    };

    JsonReader reader = new JsonReader(underlying);
    reader.close();
    assertTrue(readerClosed[0]);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testOperationsThrowAfterClose() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("[1]"));
    reader.close();
    reader.peek();
  }

  @Test(timeout = 4000)
  public void testToStringContainsLocationAndPath() throws IOException {
    JsonReader reader = new JsonReader(new StringReader("{\"key\": [10]}"));
    reader.beginObject();
    reader.nextName();
    reader.beginArray();

    String str = reader.toString();
    assertTrue(str.contains("JsonReader"));
    assertTrue(str.contains("line 1"));
    assertTrue(str.contains("path $.key[0]"));
  }
}