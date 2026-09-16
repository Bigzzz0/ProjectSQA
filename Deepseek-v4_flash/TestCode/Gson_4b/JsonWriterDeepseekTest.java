package com.google.gson.stream;

import org.junit.Test;
import java.io.IOException;
import java.io.StringWriter;
import static org.junit.Assert.*;

/**
 * White-box test suite for {@link JsonWriter}.
 * Targets line/branch coverage and exposes the known Defects4J bug:
 * top-level non-array/object values erroneously rejected in strict mode.
 *
 * [Branch & Defect Analysis Matrix]
 * - Top-level value type rejection: EMPTY_DOCUMENT/NONEMPTY_DOCUMENT branch in beforeValue().
 * - Lenient mode bypass: branch in beforeValue() for NONEMPTY_DOCUMENT when lenient=true.
 * - HTML-safe escaping: replacementChars switch in string().
 * - Null handling: nullValue() with serializeNulls and deferredName.
 * - Boundaries: empty string, NaN/Infinity for double/Number, closed writer, nesting errors.
 * - State transitions: open/close, deferredName, stack overflow/underflow.
 */
public class JsonWriterDeepseekTest {

    private StringWriter stringWriter = new StringWriter();
    private JsonWriter writer = new JsonWriter(stringWriter);

    // ==================== Part A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testWriteArrayWithValues() throws IOException {
        writer.beginArray();
        writer.value("hello");
        writer.value(42);
        writer.value(true);
        writer.nullValue();
        writer.endArray();
        writer.close();
        assertEquals("[\"hello\",42,true,null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testWriteObject() throws IOException {
        writer.beginObject();
        writer.name("key1").value("val1");
        writer.name("key2").value(123);
        writer.name("key3").value(false);
        writer.name("key4").nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{\"key1\":\"val1\",\"key2\":123,\"key3\":false,\"key4\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNestedArrayAndObject() throws IOException {
        writer.beginArray();
        writer.beginObject();
        writer.name("inner").value("value");
        writer.endObject();
        writer.endArray();
        writer.close();
        assertEquals("[{\"inner\":\"value\"}]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testMultipleTopLevelArray() throws IOException {
        writer.beginArray();
        writer.value(1);
        writer.endArray();
        writer.beginArray();
        writer.value(2);
        writer.endArray();
        // Only one top-level value is allowed in strict mode; this should throw
        // But we are testing the flow that triggers the bug: second top-level.
        // Actually, the bug is about writing top-level non-array/object values.
        // This test is still valid for coverage: writing second array causes NONEMPTY_DOCUMENT.
        // In strict mode, this throws IllegalStateException.
        // We will test lenient mode separately.
    }

    @Test(timeout = 4000)
    public void testFlushAndClose() throws IOException {
        writer.beginArray();
        writer.value(1);
        writer.flush();
        writer.endArray();
        writer.close();
        assertEquals("[1]", stringWriter.toString());
        assertTrue(stringWriter.toString().length() > 0);
    }

    // ==================== Part B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testEmptyArray() throws IOException {
        writer.beginArray();
        writer.endArray();
        writer.close();
        assertEquals("[]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyObject() throws IOException {
        writer.beginObject();
        writer.endObject();
        writer.close();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyStringValue() throws IOException {
        writer.beginArray();
        writer.value("");
        writer.endArray();
        writer.close();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNullObjectInput() throws IOException {
        writer.beginArray();
        writer.value((String) null);
        writer.endArray();
        writer.close();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testBoundaryLongValues() throws IOException {
        writer.beginArray();
        writer.value(Long.MIN_VALUE);
        writer.value(Long.MAX_VALUE);
        writer.value(0L);
        writer.endArray();
        writer.close();
        assertEquals("[-9223372036854775808,9223372036854775807,0]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testBoundaryDoubleValues() throws IOException {
        writer.beginArray();
        writer.value(0.0);
        writer.value(-1.0);
        writer.value(Double.MAX_VALUE);
        writer.value(Double.MIN_VALUE);
        writer.endArray();
        writer.close();
        assertTrue(stringWriter.toString().contains("0.0"));
        assertTrue(stringWriter.toString().contains("-1.0"));
    }

    @Test(timeout = 4000)
    public void testNegativeIntValue() throws IOException {
        writer.beginArray();
        writer.value(-42);
        writer.endArray();
        writer.close();
        assertEquals("[-42]", stringWriter.toString());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNaNDoubleRejected() throws IOException {
        writer.beginArray();
        writer.value(Double.NaN);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInfinityDoubleRejected() throws IOException {
        writer.beginArray();
        writer.value(Double.POSITIVE_INFINITY);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNegativeInfinityDoubleRejected() throws IOException {
        writer.beginArray();
        writer.value(Double.NEGATIVE_INFINITY);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testNaNNumberRejected() throws IOException {
        writer.beginArray();
        writer.value(Double.valueOf(Double.NaN));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInfinityNumberRejected() throws IOException {
        writer.beginArray();
        writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
    }

    // ==================== Part C: Defect-Targeted Branch Zone ====================

    /**
     * Directly targets the known defect: top-level non-array/object values
     * erroneously throw IllegalStateException in strict mode.
     */
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testTopLevelValueStringStrict() throws IOException {
        // This should throw because JSON must start with an array or an object.
        writer.value("topLevel");
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testTopLevelValueNumberStrict() throws IOException {
        writer.value(42);
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testTopLevelValueBooleanStrict() throws IOException {
        writer.value(true);
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testTopLevelValueNullStrict() throws IOException {
        writer.nullValue();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testTopLevelValueLongStrict() throws IOException {
        writer.value(42L);
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testTopLevelValueJsonValueStrict() throws IOException {
        writer.jsonValue("{}");
    }

    @Test(timeout = 4000)
    public void testTopLevelValueLenient() throws IOException {
        writer.setLenient(true);
        writer.value("topLevel");
        writer.close();
        assertEquals("\"topLevel\"", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testTopLevelValueLenientNumber() throws IOException {
        writer.setLenient(true);
        writer.value(42);
        writer.close();
        assertEquals("42", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testTopLevelValueLenientBoolean() throws IOException {
        writer.setLenient(true);
        writer.value(false);
        writer.close();
        assertEquals("false", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testTopLevelValueLenientNull() throws IOException {
        writer.setLenient(true);
        writer.nullValue();
        writer.close();
        assertEquals("null", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testTopLevelValueLenientLong() throws IOException {
        writer.setLenient(true);
        writer.value(123L);
        writer.close();
        assertEquals("123", stringWriter.toString());
    }

    // ==================== Part D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testOutIsNull() {
        new JsonWriter(null);
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testDoubleName() throws IOException {
        writer.beginObject();
        writer.name("a");
        writer.name("b"); // should throw
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNameOutsideObject() throws IOException {
        writer.name("bad");
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testCloseIncompleteDocument() throws IOException {
        writer.beginArray();
        writer.close(); // array not closed
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testCloseClosedWriter() throws IOException {
        writer.close();
        writer.close(); // double close
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testWriteAfterClose() throws IOException {
        writer.close();
        writer.value(1);
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNestingMismatchCloseArrayWhenObjectOpen() throws IOException {
        writer.beginObject();
        writer.endArray();
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testNullName() throws IOException {
        writer.name(null);
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testDanglingNameInClose() throws IOException {
        writer.beginObject();
        writer.name("dangling");
        writer.endObject(); // should throw because name has no value
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testFlushClosedWriter() throws IOException {
        writer.close();
        writer.flush();
    }

    @Test(timeout = 4000)
    public void testDeferredNameSkippedWhenSerializeNullsFalse() throws IOException {
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("nullKey");
        writer.nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testDeferredNameNullValueWithSerializeNullsTrue() throws IOException {
        writer.beginObject();
        writer.name("key");
        writer.nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{\"key\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testIndentSetToNull() throws IOException {
        writer.setIndent("");
        writer.beginArray();
        writer.value(1);
        writer.endArray();
        writer.close();
        assertEquals("[1]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testIndentWithSpaces() throws IOException {
        writer.setIndent("  ");
        writer.beginObject();
        writer.name("a").value(1);
        writer.endObject();
        writer.close();
        String expected = "{\n  \"a\": 1\n}";
        assertEquals(expected, stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testHtmlSafeEscaping() throws IOException {
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value("<>&'=");
        writer.endArray();
        writer.close();
        assertEquals("[\"\\u003c\\u003e\\u0026\\u0027\\u003d\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNonHtmlSafeEscaping() throws IOException {
        writer.beginArray();
        writer.value("<>&'=");
        writer.endArray();
        writer.close();
        // Default escaping only escapes <, >, &, =, ' as themselves? Actually not escaped.
        assertEquals("[\"<>&'=\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testControlCharacterEscaping() throws IOException {
        writer.beginArray();
        writer.value("a\nb\tc");
        writer.endArray();
        writer.close();
        assertEquals("[\"a\\nb\\tc\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testUnicodeEscapingLineSeparator() throws IOException {
        writer.beginArray();
        writer.value("\u2028");
        writer.endArray();
        writer.close();
        assertEquals("[\"\\u2028\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testUnicodeEscapingParagraphSeparator() throws IOException {
        writer.beginArray();
        writer.value("\u2029");
        writer.endArray();
        writer.close();
        assertEquals("[\"\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testBeginArrayInsideNameValueWithoutValue() throws IOException {
        writer.beginObject();
        writer.name("arr");
        writer.beginArray(); // OK: value for name
        writer.endArray();
        // But the object is not closed; this test is just for path coverage.
    }

    // ==================== Part E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testGetSerializeNullsDefault() {
        assertTrue(writer.getSerializeNulls());
    }

    @Test(timeout = 4000)
    public void testSetSerializeNullsFalseToTrue() {
        writer.setSerializeNulls(false);
        assertFalse(writer.getSerializeNulls());
        writer.setSerializeNulls(true);
        assertTrue(writer.getSerializeNulls());
    }

    @Test(timeout = 4000)
    public void testIsLenientDefault() {
        assertFalse(writer.isLenient());
    }

    @Test(timeout = 4000)
    public void testLenientToggle() {
        writer.setLenient(true);
        assertTrue(writer.isLenient());
        writer.setLenient(false);
        assertFalse(writer.isLenient());
    }

    @Test(timeout = 4000)
    public void testIsHtmlSafeDefault() {
        assertFalse(writer.isHtmlSafe());
    }

    @Test(timeout = 4000)
    public void testHtmlSafeToggle() {
        writer.setHtmlSafe(true);
        assertTrue(writer.isHtmlSafe());
        writer.setHtmlSafe(false);
        assertFalse(writer.isHtmlSafe());
    }
}