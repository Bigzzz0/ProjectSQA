package com.google.gson.stream;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target: com.google.gson.stream.JsonWriter
 *
 * 1. Defect-Targeted Branch Zone (Defects4J ground truth: testTopLevelValueTypes):
 *    - Branch: beforeValue(boolean root) -> case EMPTY_DOCUMENT:
 *      `if (!lenient && !root) throw new IllegalStateException("JSON must start with an array or an object.");`
 *    - RFC 7159 permits top-level values of any type (strings, numbers, booleans, nulls) in strict mode.
 *      The defective implementation rejects top-level primitives when lenient == false.
 *    - Target Tests: testTopLevelValueTypes_* assert valid top-level values in non-lenient mode.
 *
 * 2. Scope & Stack State Transitions:
 *    - Scope transitions: EMPTY_DOCUMENT -> NONEMPTY_DOCUMENT -> NONEMPTY_DOCUMENT (lenient fall-through)
 *    - Array handling: EMPTY_ARRAY -> NONEMPTY_ARRAY (commas, newlines) -> close
 *    - Object handling: EMPTY_OBJECT -> DANGLING_NAME -> NONEMPTY_OBJECT (commas, separators) -> close
 *    - Stack capacity expansion: nesting depth > 32 triggers stack resize (System.arraycopy).
 *
 * 3. Value Encoding & Escaping:
 *    - Control characters (0x00 - 0x1F), quotes, backslashes, tabs, newlines, form feeds, carriage returns.
 *    - JavaScript newline specials: \u2028 and \u2029.
 *    - HTML safe characters (<, >, &, =, ') active vs inactive.
 *    - Consecutive vs non-consecutive escapes (verifying substring chunking last < i and last < length).
 *    - Null serialization: serializeNulls true vs false (with and without deferred name).
 *    - Numeric boundaries: Double.NaN, Double.isInfinite, and non-lenient Number checks.
 *
 * 4. Error Paths & Defensive Guards:
 *    - Null writer constructor guard.
 *    - Null property names, duplicate names, dangling names on scope close.
 *    - Operations after close() (stackSize == 0).
 *    - Mis-matched scope closures (endArray on object, endObject on array).
 *    - Premature close() on incomplete documents.
 * ----------------------------------------------------------------------------------------------------
 */
public class JsonWriterGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyArrayAndObject() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray().endArray();
    writer.close();
    assertEquals("[]", out.toString());

    out = new StringWriter();
    writer = new JsonWriter(out);
    writer.beginObject().endObject();
    writer.close();
    assertEquals("{}", out.toString());
  }

  @Test(timeout = 4000)
  public void testComplexNestedStructure() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);

    writer.beginObject();
    writer.name("name").value("Gson");
    writer.name("version").value(2);
    writer.name("active").value(true);
    writer.name("features").beginArray();
    writer.value("streaming");
    writer.value("reflection");
    writer.endArray();
    writer.name("config").beginObject();
    writer.name("indent").value("none");
    writer.endObject();
    writer.name("nullable").nullValue();
    writer.endObject();
    writer.close();

    String expected = "{\"name\":\"Gson\",\"version\":2,\"active\":true,\"features\":[\"streaming\",\"reflection\"],\"config\":{\"indent\":\"none\"},\"nullable\":null}";
    assertEquals(expected, out.toString());
  }

  @Test(timeout = 4000)
  public void testPrettyPrintingIndentation() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setIndent("  ");

    writer.beginObject();
    writer.name("k1").value("v1");
    writer.name("list").beginArray();
    writer.value(100);
    writer.value(200);
    writer.endArray();
    writer.endObject();
    writer.close();

    String expected = "{\n  \"k1\": \"v1\",\n  \"list\": [\n    100,\n    200\n  ]\n}";
    assertEquals(expected, out.toString());
  }

  @Test(timeout = 4000)
  public void testSetIndentEmptyResetsFormatting() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setIndent("    ");
    writer.setIndent(""); // Should reset indentation to compact mode

    writer.beginArray();
    writer.value(1);
    writer.value(2);
    writer.endArray();
    writer.close();

    assertEquals("[1,2]", out.toString());
  }

  @Test(timeout = 4000)
  public void testJsonValueDirectInjection() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);

    writer.beginArray();
    writer.jsonValue("{\"raw\":true}");
    writer.jsonValue(null);
    writer.endArray();
    writer.close();

    assertEquals("[{\"raw\":true},null]", out.toString());
  }

  @Test(timeout = 4000)
  public void testJsonValueWithDeferredName() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);

    writer.beginObject();
    writer.name("data").jsonValue("[1,2,3]");
    writer.endObject();
    writer.close();

    assertEquals("{\"data\":[1,2,3]}", out.toString());
  }

  @Test(timeout = 4000)
  public void testSerializeNullsBehavior() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    assertTrue(writer.getSerializeNulls());

    writer.setSerializeNulls(false);
    assertFalse(writer.getSerializeNulls());

    writer.beginObject();
    writer.name("included").value("val");
    writer.name("skippedString").value((String) null);
    writer.name("skippedNumber").value((Number) null);
    writer.name("skippedExplicitNull").nullValue();
    writer.name("last").value(123);
    writer.endObject();
    writer.close();

    assertEquals("{\"included\":\"val\",\"last\":123}", out.toString());
  }

  @Test(timeout = 4000)
  public void testNullInArrayAlwaysEmittedEvenIfSerializeNullsFalse() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setSerializeNulls(false);

    writer.beginArray();
    writer.nullValue();
    writer.value((String) null);
    writer.value((Number) null);
    writer.endArray();
    writer.close();

    assertEquals("[null,null,null]", out.toString());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testStackExpansionBeyondInitial32Limit() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);

    int depth = 40; // Exceeds default stack size of 32
    for (int i = 0; i < depth; i++) {
      writer.beginArray();
    }
    writer.value("deep");
    for (int i = 0; i < depth; i++) {
      writer.endArray();
    }
    writer.close();

    StringBuilder expected = new StringBuilder();
    for (int i = 0; i < depth; i++) expected.append('[');
    expected.append("\"deep\"");
    for (int i = 0; i < depth; i++) expected.append(']');
    assertEquals(expected.toString(), out.toString());
  }

  @Test(timeout = 4000)
  public void testHtmlSafeEscaping() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    assertFalse(writer.isHtmlSafe());

    writer.setHtmlSafe(true);
    assertTrue(writer.isHtmlSafe());

    writer.beginArray();
    writer.value("<tag> & 'quote' = \"val\"");
    writer.endArray();
    writer.close();

    assertEquals("[\"\\u003ctag\\u003e \\u0026 \\u0027quote\\u0027 \\u003d \\\"val\\\"\"]", out.toString());
  }

  @Test(timeout = 4000)
  public void testHtmlSafeDisabledDoesNotEscapeXmlSpecialChars() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setHtmlSafe(false);

    writer.beginArray();
    writer.value("<foo>&'=");
    writer.endArray();
    writer.close();

    assertEquals("[\"<foo>&'=\"]", out.toString());
  }

  @Test(timeout = 4000)
  public void testStringEscapesAllControlAndSpecialCharacters() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);

    String input = "\u0000\u001f\"\\\t\b\n\r\f\u2028\u2029abc\u4e2d";
    writer.beginArray();
    writer.value(input);
    writer.endArray();
    writer.close();

    String expected = "[\"\\u0000\\u001f\\\"\\\\\\t\\b\\n\\r\\f\\u2028\\u2029abc\u4e2d\"]";
    assertEquals(expected, out.toString());
  }

  @Test(timeout = 4000)
  public void testStringBoundaryChunks() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);

    // Escape at start, middle, and end, plus consecutive escapes
    writer.beginArray();
    writer.value("\nplain\t\tplain\n");
    writer.value(""); // empty string boundary
    writer.value("no_escapes_here");
    writer.endArray();
    writer.close();

    assertEquals("[\"\\nplain\\t\\tplain\\n\",\"\",\"no_escapes_here\"]", out.toString());
  }

  @Test(timeout = 4000)
  public void testNumericValuesBoundaries() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);

    writer.beginArray();
    writer.value(0L);
    writer.value(Long.MIN_VALUE);
    writer.value(Long.MAX_VALUE);
    writer.value(0.0);
    writer.value(-0.0);
    writer.value(Double.MIN_VALUE);
    writer.value(Double.MAX_VALUE);
    writer.value(new BigInteger("123456789012345678901234567890"));
    writer.value(new BigDecimal("1234567890.0987654321"));
    writer.endArray();
    writer.close();

    String result = out.toString();
    assertTrue(result.contains("0"));
    assertTrue(result.contains(String.valueOf(Long.MIN_VALUE)));
    assertTrue(result.contains(String.valueOf(Long.MAX_VALUE)));
    assertTrue(result.contains("123456789012345678901234567890"));
  }

  @Test(timeout = 4000)
  public void testLenientAllowsMultipleTopLevelValues() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setLenient(true);
    assertTrue(writer.isLenient());

    writer.beginArray().endArray();
    writer.beginObject().endObject();
    writer.flush();

    assertEquals("[]{}", out.toString());
  }

  @Test(timeout = 4000)
  public void testLenientNumberSpecialValues() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setLenient(true);

    writer.beginArray();
    writer.value(Double.valueOf(Double.NaN));
    writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
    writer.value(Double.valueOf(Double.NEGATIVE_INFINITY));
    writer.endArray();
    writer.close();

    assertEquals("[NaN,Infinity,-Infinity]", out.toString());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets Defect: JsonWriter rejecting top-level primitive values in RFC 7159 compliance.
   * On defective version, calling writer.value(...) without lenient mode throws:
   * java.lang.IllegalStateException: JSON must start with an array or an object.
   */
  @Test(timeout = 4000)
  public void testTopLevelValueTypesBoolean() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.value(true);
    writer.close();
    assertEquals("true", out.toString());
  }

  @Test(timeout = 4000)
  public void testTopLevelValueTypesString() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.value("hello");
    writer.close();
    assertEquals("\"hello\"", out.toString());
  }

  @Test(timeout = 4000)
  public void testTopLevelValueTypesLong() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.value(12345L);
    writer.close();
    assertEquals("12345", out.toString());
  }

  @Test(timeout = 4000)
  public void testTopLevelValueTypesDouble() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.value(123.45);
    writer.close();
    assertEquals("123.45", out.toString());
  }

  @Test(timeout = 4000)
  public void testTopLevelValueTypesNull() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.nullValue();
    writer.close();
    assertEquals("null", out.toString());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructorNullWriter() {
    new JsonWriter(null);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testNameNullThrowsNpe() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    writer.name(null);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testDuplicateNameThrowsIllegalState() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    writer.name("a");
    writer.name("b");
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNameOutsideObjectThrowsIllegalState() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.name("a");
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testDanglingNameAtObjectCloseThrowsIllegalState() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    writer.name("dangling");
    writer.endObject();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testMismatchedArrayCloseThrowsIllegalState() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    writer.endArray();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testMismatchedObjectCloseThrowsIllegalState() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.endObject();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testUnopenedEndArrayThrowsIllegalState() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.endArray();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testMultipleTopLevelValuesInStrictModeThrowsIllegalState() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setLenient(false);
    writer.beginArray().endArray();
    writer.beginArray();
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testStrictDoubleNaNThrowsIllegalArgument() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.value(Double.NaN);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testStrictDoubleInfinityThrowsIllegalArgument() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.value(Double.POSITIVE_INFINITY);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testStrictDoubleNegativeInfinityThrowsIllegalArgument() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.value(Double.NEGATIVE_INFINITY);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testStrictNumberNaNThrowsIllegalArgument() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.value(Double.valueOf(Double.NaN));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testStrictNumberPositiveInfinityThrowsIllegalArgument() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testStrictNumberNegativeInfinityThrowsIllegalArgument() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.value(Double.valueOf(Double.NEGATIVE_INFINITY));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(expected = IOException.class, timeout = 4000)
  public void testClosePrematureIncompleteArrayThrowsIOException() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.close();
  }

  @Test(expected = IOException.class, timeout = 4000)
  public void testClosePrematureIncompleteObjectThrowsIOException() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    writer.close();
  }

  @Test(expected = IOException.class, timeout = 4000)
  public void testCloseEmptyDocumentThrowsIOException() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.close();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testFlushAfterCloseThrowsIllegalState() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray().endArray();
    writer.close();
    writer.flush();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNameAfterCloseThrowsIllegalState() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray().endArray();
    writer.close();
    writer.name("key");
  }

  @Test(timeout = 4000)
  public void testMultipleClosesAreIdempotentOnWriter() throws IOException {
    class MockWriter extends Writer {
      int closeCount = 0;
      @Override public void write(char[] cbuf, int off, int len) {}
      @Override public void flush() {}
      @Override public void close() { closeCount++; }
    }

    MockWriter mockOut = new MockWriter();
    JsonWriter writer = new JsonWriter(mockOut);
    writer.beginArray().endArray();
    writer.close();
    assertEquals(1, mockOut.closeCount);

    writer.close(); // Second close should not fail and should invoke out.close()
    assertEquals(2, mockOut.closeCount);
  }

  @Test(timeout = 4000)
  public void testFlushPassesThroughToUnderlyingWriter() throws IOException {
    class FlushCountingWriter extends StringWriter {
      int flushCount = 0;
      @Override public void flush() {
        super.flush();
        flushCount++;
      }
    }

    FlushCountingWriter out = new FlushCountingWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.flush();
    assertEquals(1, out.flushCount);
    writer.endArray();
    writer.close();
  }
}