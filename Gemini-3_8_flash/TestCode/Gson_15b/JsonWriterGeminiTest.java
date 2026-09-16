/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.gson.stream.JsonWriter
 * -----------------------------------------------------------------------------------------
 * Branch / Condition Coverage:
 * 1. Constructor: out == null -> NullPointerException; valid Writer -> stack initialized to EMPTY_DOCUMENT.
 * 2. setIndent: indent.isEmpty() (compact format, separator ":") vs non-empty (indented, separator ": ").
 * 3. Configuration toggles: lenient, htmlSafe, serializeNulls getter/setter verification.
 * 4. beginArray / endArray: EMPTY_ARRAY vs NONEMPTY_ARRAY; invalid context -> ISE("Nesting problem.");
 *    dangling name -> ISE("Dangling name: ..."); stack size decrease; newline emission.
 * 5. beginObject / endObject: EMPTY_OBJECT vs NONEMPTY_OBJECT; commas emitted between fields.
 * 6. push / stack resize: pushing > 32 scopes to force stack array growth (doubling capacity).
 * 7. peek / replaceTop: peek when stackSize == 0 -> ISE("JsonWriter is closed.").
 * 8. name: null name -> NPE; deferredName != null -> ISE; closed writer -> ISE.
 * 9. writeDeferredName: correctly deferred and emitted before value/array/object/nullValue.
 * 10. value(String): null -> nullValue(); non-null string encoding.
 * 11. jsonValue: null -> nullValue(); raw string appended verbatim without quotes.
 * 12. nullValue: deferredName != null with serializeNulls=true vs serializeNulls=false (skip property).
 * 13. value(boolean) and value(Boolean): primitive true/false, boxed true/false/null.
 * 14. value(double): finite double vs NaN / -Infinity / Infinity.
 * 15. value(long): normal, min, max values.
 * 16. value(Number): null -> nullValue(); finite numbers; NaN/Infinities when lenient=false (IAE) vs lenient=true.
 * 17. flush: open writer flushes underlying writer; stackSize == 0 -> ISE.
 * 18. close: size > 1 -> IOException("Incomplete document"); size == 1 && top != NONEMPTY_DOCUMENT -> IOException;
 *     valid completed document closes underlying writer; stackSize set to 0.
 * 19. string escaping:
 *     - standard control chars (0x00 - 0x1f), quotes (\"), backslashes (\\), \t, \b, \n, \r, \f
 *     - JavaScript newline chars \u2028, \u2029
 *     - htmlSafe replacements: '<', '>', '&', '=', '\''
 * 20. beforeName: context == NONEMPTY_OBJECT (emits comma) vs EMPTY_OBJECT vs illegal contexts (ISE).
 * 21. beforeValue: NONEMPTY_DOCUMENT with lenient=false (ISE) vs lenient=true; EMPTY_DOCUMENT, EMPTY_ARRAY,
 *     NONEMPTY_ARRAY, DANGLING_NAME, and default illegal context (ISE).
 * -----------------------------------------------------------------------------------------
 * Defect Zone (Defects4J Known Bug):
 * - com.google.gson.stream.JsonWriterTest::testNonFiniteDoublesWhenLenient
 *   In value(double), Double.isNaN(value) || Double.isInfinite(value) throws IllegalArgumentException
 *   even if lenient == true. In value(Number), lenient allows NaN and Infinities, but value(double)
 *   does not check `lenient`.
 */

package com.google.gson.stream;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class JsonWriterGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyObjectAndArray() throws IOException {
    StringWriter stringWriter = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginObject();
    jsonWriter.endObject();
    jsonWriter.close();
    assertEquals("{}", stringWriter.toString());

    stringWriter = new StringWriter();
    jsonWriter = new JsonWriter(stringWriter);
    jsonWriter.beginArray();
    jsonWriter.endArray();
    jsonWriter.close();
    assertEquals("[]", stringWriter.toString());
  }

  @Test(timeout = 4000)
  public void testCompleteObjectWithValues() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    writer.name("str").value("hello");
    writer.name("boolTrue").value(true);
    writer.name("boolFalse").value(false);
    writer.name("boxedBool").value(Boolean.TRUE);
    writer.name("boxedBoolNull").value((Boolean) null);
    writer.name("long").value(123456789L);
    writer.name("double").value(123.456);
    writer.name("number").value(new BigDecimal("98765.4321"));
    writer.name("nullVal").nullValue();
    writer.name("rawJson").jsonValue("{\"raw\":1}");
    writer.endObject();
    writer.close();

    String expected = "{\"str\":\"hello\",\"boolTrue\":true,\"boolFalse\":false,\"boxedBool\":true,"
        + "\"boxedBoolNull\":null,\"long\":123456789,\"double\":123.456,\"number\":98765.4321,"
        + "\"nullVal\":null,\"rawJson\":{\"raw\":1}}";
    assertEquals(expected, out.toString());
  }

  @Test(timeout = 4000)
  public void testNestedArraysAndObjectsWithIndentation() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setIndent("  ");
    writer.beginArray();
    writer.beginObject();
    writer.name("k").value("v");
    writer.endObject();
    writer.beginArray();
    writer.value(1);
    writer.value(2);
    writer.endArray();
    writer.endArray();
    writer.close();

    String expected = "[\n"
        + "  {\n"
        + "    \"k\": \"v\"\n"
        + "  },\n"
        + "  [\n"
        + "    1,\n"
        + "    2\n"
        + "  ]\n"
        + "]";
    assertEquals(expected, out.toString());
  }

  @Test(timeout = 4000)
  public void testSetIndentResetToEmpty() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setIndent("   ");
    writer.setIndent(""); // Should reset indent to null and separator to ":"
    writer.beginObject();
    writer.name("a").value("b");
    writer.endObject();
    writer.close();
    assertEquals("{\"a\":\"b\"}", out.toString());
  }

  @Test(timeout = 4000)
  public void testSerializeNullsFalseSkipsObjectProperty() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    assertTrue(writer.getSerializeNulls());
    writer.setSerializeNulls(false);
    assertFalse(writer.getSerializeNulls());

    writer.beginObject();
    writer.name("kept").value("present");
    writer.name("skipped").nullValue();
    writer.name("skippedString").value((String) null);
    writer.name("skippedNumber").value((Number) null);
    writer.name("skippedBoolean").value((Boolean) null);
    writer.name("skippedJsonValue").jsonValue(null);
    writer.endObject();
    writer.close();

    assertEquals("{\"kept\":\"present\"}", out.toString());
  }

  @Test(timeout = 4000)
  public void testSerializeNullsFalseDoesNotAffectArrayElements() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setSerializeNulls(false);
    writer.beginArray();
    writer.nullValue();
    writer.value((String) null);
    writer.value((Number) null);
    writer.value((Boolean) null);
    writer.jsonValue(null);
    writer.endArray();
    writer.close();

    assertEquals("[null,null,null,null,null]", out.toString());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testStackExpansionBeyondInitialCapacity() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    // Initial stack capacity is 32. Nest 35 arrays to trigger resize.
    int depth = 35;
    for (int i = 0; i < depth; i++) {
      writer.beginArray();
    }
    writer.value("deep");
    for (int i = 0; i < depth; i++) {
      writer.endArray();
    }
    writer.close();

    StringBuilder expected = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      expected.append("[");
    }
    expected.append("\"deep\"");
    for (int i = 0; i < depth; i++) {
      expected.append("]");
    }
    assertEquals(expected.toString(), out.toString());
  }

  @Test(timeout = 4000)
  public void testNumberBoundaries() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.value(Long.MIN_VALUE);
    writer.value(Long.MAX_VALUE);
    writer.value(Double.MIN_VALUE);
    writer.value(Double.MAX_VALUE);
    writer.value(new BigInteger("-999999999999999999999999999999"));
    writer.endArray();
    writer.close();

    String expected = "[" + Long.MIN_VALUE + "," + Long.MAX_VALUE + ","
        + Double.MIN_VALUE + "," + Double.MAX_VALUE + ",-999999999999999999999999999999]";
    assertEquals(expected, out.toString());
  }

  @Test(timeout = 4000)
  public void testStringEscapesStandard() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    assertFalse(writer.isHtmlSafe());
    writer.beginArray();
    // Escape quote, backslash, control chars, tabs, newlines
    writer.value("Quote: \", Backslash: \\, Tab: \t, Backspace: \b, Newline: \n, CR: \r, FF: \f");
    // Control characters < 0x20
    writer.value("\u0000\u0007\u001f");
    // JS newlines \u2028 and \u2029
    writer.value("JS newline: \u2028 and paragraph: \u2029");
    writer.endArray();
    writer.close();

    String expected = "[\"Quote: \\\", Backslash: \\\\, Tab: \\t, Backspace: \\b, Newline: \\n, CR: \\r, FF: \\f\","
        + "\"\\u0000\\u0007\\u001f\","
        + "\"JS newline: \\u2028 and paragraph: \\u2029\"]";
    assertEquals(expected, out.toString());
  }

  @Test(timeout = 4000)
  public void testStringEscapesHtmlSafe() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setHtmlSafe(true);
    assertTrue(writer.isHtmlSafe());
    writer.beginArray();
    writer.value("<tag> & 'foo' = \"bar\"");
    writer.endArray();
    writer.close();

    String expected = "[\"\\u003ctag\\u003e \\u0026 \\u0027foo\\u0027 \\u003d \\\"bar\\\"\"]";
    assertEquals(expected, out.toString());
  }

  @Test(timeout = 4000)
  public void testEmptyStringsAndCleanStrings() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.value("");
    writer.value("plainAsciiWithoutAnyEscapes");
    writer.endArray();
    writer.close();

    assertEquals("[\"\",\"plainAsciiWithoutAnyEscapes\"]", out.toString());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
  // =========================================================================

  /**
   * Targets the defect where JsonWriter.value(double) throws IllegalArgumentException
   * even when setLenient(true) is enabled.
   */
  @Test(timeout = 4000)
  public void testNonFiniteDoublesWhenLenient() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setLenient(true);
    assertTrue(writer.isLenient());

    writer.beginArray();
    writer.value(Double.NaN);
    writer.value(Double.NEGATIVE_INFINITY);
    writer.value(Double.POSITIVE_INFINITY);
    writer.endArray();
    writer.close();

    assertEquals("[NaN,-Infinity,Infinity]", out.toString());
  }

  @Test(timeout = 4000)
  public void testNonFiniteNumbersWhenLenient() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setLenient(true);

    writer.beginArray();
    writer.value(Double.valueOf(Double.NaN));
    writer.value(Double.valueOf(Double.NEGATIVE_INFINITY));
    writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
    writer.endArray();
    writer.close();

    assertEquals("[NaN,-Infinity,Infinity]", out.toString());
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testNonFiniteDoubleThrowsWhenStrictNaN() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    assertFalse(writer.isLenient());
    writer.value(Double.NaN);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testNonFiniteDoubleThrowsWhenStrictPositiveInfinity() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.value(Double.POSITIVE_INFINITY);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testNonFiniteDoubleThrowsWhenStrictNegativeInfinity() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.value(Double.NEGATIVE_INFINITY);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testNonFiniteNumberThrowsWhenStrictNaN() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.value(Double.valueOf(Double.NaN));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testNonFiniteNumberThrowsWhenStrictInfinity() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testNonFiniteNumberThrowsWhenStrictNegativeInfinity() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.value(Double.valueOf(Double.NEGATIVE_INFINITY));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructorNullWriterThrows() {
    new JsonWriter(null);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testNullNameThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    writer.name(null);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testDuplicateNameThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    writer.name("a");
    writer.name("b");
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNameWithoutObjectThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.name("a");
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNameAtDocumentRootThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.name("a");
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testMultipleTopLevelValuesWhenStrictThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray().endArray();
    writer.beginArray(); // Illegal second top-level value
  }

  @Test(timeout = 4000)
  public void testMultipleTopLevelValuesWhenLenientAllowed() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setLenient(true);
    writer.value("first");
    writer.value("second");
    writer.beginArray().endArray();
    writer.close();
    assertEquals("\"first\"\"second\"[]", out.toString());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testMismatchedEndObjectThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.endObject();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testMismatchedEndArrayThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    writer.endArray();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testDanglingNameAtEndObjectThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    writer.name("dangling");
    writer.endObject();
  }

  @Test(expected = IOException.class, timeout = 4000)
  public void testCloseUnclosedArrayThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.close();
  }

  @Test(expected = IOException.class, timeout = 4000)
  public void testCloseUnclosedObjectThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginObject();
    writer.close();
  }

  @Test(expected = IOException.class, timeout = 4000)
  public void testCloseEmptyDocumentThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.close();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testOperationsOnClosedWriterThrow() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray().endArray();
    writer.close();

    writer.name("test");
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testFlushOnClosedWriterThrows() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray().endArray();
    writer.close();

    writer.flush();
  }

  @Test(timeout = 4000)
  public void testFlushOnOpenWriterSucceeds() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.value(42);
    writer.flush();
    assertEquals("[42", out.toString());
    writer.endArray();
    writer.close();
    assertEquals("[42]", out.toString());
  }
}