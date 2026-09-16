package com.google.gson.stream;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

public class JsonWriterDeepseekTest {

    /* [Branch & Defect Analysis Matrix] */
    // Target defect: testBoxedBooleans -> NullPointerException when writing boxed Boolean values
    // via value(Number) or value(Object) path with null handling
    // Branches targeted:
    // - value(Number) with null -> should write "null" literal, not NPE
    // - value(Boolean) with null -> should write "null" literal, not NPE
    // - serializeNulls true/false combinations
    // - lenient mode top-level values
    // - HTML safe escaping
    // - Stack growth (push beyond initial 32)
    // - Deferred name handling
    // - Boundary: empty string, special chars, control chars, unicode
    // - Exception paths: NaN, Infinity, dangling name, nesting problems

    @Test(timeout = 4000)
    public void testBoxedBooleansNull() throws IOException {
        // Defect: writing null boxed Boolean should produce "null" not NPE
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Boolean) null);
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testBoxedBooleansTrueFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Boolean.TRUE);
        writer.value(Boolean.FALSE);
        writer.endArray();
        assertEquals("[true,false]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testBoxedBooleansInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("flag").value(Boolean.TRUE);
        writer.name("nullFlag").value((Boolean) null);
        writer.endObject();
        assertEquals("{\"flag\":true,\"nullFlag\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testBoxedBooleansSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("nullFlag").value((Boolean) null);
        writer.name("trueFlag").value(Boolean.TRUE);
        writer.endObject();
        assertEquals("{\"trueFlag\":true}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testBoxedBooleansSerializeNullsTrue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(true);
        writer.beginObject();
        writer.name("nullFlag").value((Boolean) null);
        writer.endObject();
        assertEquals("{\"nullFlag\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNullValueInArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.nullValue();
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNullValueInObjectSerializeNulls() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("a").nullValue();
        writer.endObject();
        assertEquals("{\"a\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNullValueInObjectSkipNulls() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("a").nullValue();
        writer.endObject();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNullString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((String) null);
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNullNumber() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Number) null);
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNullNumberInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("num").value((Number) null);
        writer.endObject();
        assertEquals("{\"num\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNumberInteger() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(42);
        writer.endArray();
        assertEquals("[42]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNumberLong() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(1234567890123L);
        writer.endArray();
        assertEquals("[1234567890123]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNumberDouble() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(3.14);
        writer.endArray();
        assertEquals("[3.14]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNumberFloat() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(1.5f);
        writer.endArray();
        assertEquals("[1.5]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNumberBigInteger() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(new java.math.BigInteger("123456789012345678901234567890"));
        writer.endArray();
        assertEquals("[123456789012345678901234567890]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNumberBigDecimal() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(new java.math.BigDecimal("123.456"));
        writer.endArray();
        assertEquals("[123.456]", stringWriter.toString());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testValueNaN() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Double.NaN);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testValueInfinity() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Double.POSITIVE_INFINITY);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testValueNegativeInfinity() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Double.NEGATIVE_INFINITY);
    }

    @Test(timeout = 4000)
    public void testValueNaNLenient() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(Double.NaN);
        writer.endArray();
        assertEquals("[NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueInfinityLenient() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(Double.POSITIVE_INFINITY);
        writer.endArray();
        assertEquals("[Infinity]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNegativeInfinityLenient() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(Double.NEGATIVE_INFINITY);
        writer.endArray();
        assertEquals("[-Infinity]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNumberNaNLenient() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(Double.valueOf(Double.NaN));
        writer.endArray();
        assertEquals("[NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNumberInfinityLenient() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
        writer.endArray();
        assertEquals("[Infinity]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNumberNegativeInfinityLenient() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(Double.valueOf(Double.NEGATIVE_INFINITY));
        writer.endArray();
        assertEquals("[-Infinity]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNumberNaNStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.value(Double.valueOf(Double.NaN));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testValueNumberInfinityStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.value(Double.valueOf(Double.POSITIVE_INFINITY));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testValueNumberNegativeInfinityStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.value(Double.valueOf(Double.NEGATIVE_INFINITY));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testStringEscaping() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("hello\"world\\test\nnewline\t tab");
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStringControlChars() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("control\u0000char\u001f");
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStringUnicodeLineSeparator() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("line\u2028separator\u2029");
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStringHtmlSafe() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value("<tag>&amp;=equal");
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStringHtmlSafeFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value("<tag>&amp;=equal");
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStringEmpty() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("");
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStringUnicode() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("héllo wörld");
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStringBackspaceAndFormFeed() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("back\bspace\fform");
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStringCarriageReturn() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("carriage\rreturn");
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStringSlash() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("slash/backslash\\");
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testJsonValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.jsonValue("{\"key\":\"value\"}");
        writer.endArray();
        assertEquals("[{\"key\":\"value\"}]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testJsonValueNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.jsonValue(null);
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testJsonValueInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("data").jsonValue("[1,2,3]");
        writer.endObject();
        assertEquals("{\"data\":[1,2,3]}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testBeginEndArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        assertEquals("[]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testBeginEndObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.endObject();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNestedArrays() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.beginArray();
        writer.value(1);
        writer.endArray();
        writer.beginArray();
        writer.value(2);
        writer.endArray();
        writer.endArray();
        assertEquals("[[1],[2]]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNestedObjects() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("a").beginObject();
        writer.name("b").value(1);
        writer.endObject();
        writer.endObject();
        assertEquals("{\"a\":{\"b\":1}}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testMixedNesting() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.beginObject();
        writer.name("key").value("value");
        writer.endObject();
        writer.endArray();
        assertEquals("[{\"key\":\"value\"}]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testMultipleTopLevelValuesStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        try {
            writer.beginArray();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testMultipleTopLevelValuesLenient() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        writer.beginArray();
        writer.endArray();
        assertEquals("[][]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testTopLevelValueStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        try {
            writer.value(1);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testTopLevelValueLenient() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(1);
        assertEquals("1", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testTopLevelStringLenient() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value("hello");
        assertEquals("\"hello\"", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testTopLevelBooleanLenient() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(true);
        assertEquals("true", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testTopLevelNullLenient() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.nullValue();
        assertEquals("null", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testDeferredName() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("a");
        writer.name("b").value(1);
        writer.endObject();
        assertEquals("{\"b\":1}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testDeferredNameNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("a");
        writer.nullValue();
        writer.endObject();
        assertEquals("{\"a\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testDeferredNameSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("a");
        writer.nullValue();
        writer.endObject();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNameNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        try {
            writer.name(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testNameOutsideObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.name("a");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testDanglingNameEndObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("a");
        try {
            writer.endObject();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testDanglingNameEndArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.name("a");
        try {
            writer.endArray();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testNestingProblem() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.endObject();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testCloseIncompleteDocument() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.close();
            fail("Expected IOException");
        } catch (IOException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testCloseCompleteDocument() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        writer.close();
        assertEquals("[]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testCloseClosedWriter() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        writer.close();
        try {
            writer.beginArray();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testFlush() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(1);
        writer.flush();
        writer.endArray();
        assertEquals("[1]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testSetIndentEmpty() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("");
        writer.beginArray();
        writer.value(1);
        writer.endArray();
        assertEquals("[1]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testSetIndentSpaces() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");
        writer.beginArray();
        writer.value(1);
        writer.value(2);
        writer.endArray();
        assertEquals("[\n  1,\n  2\n]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testSetIndentObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");
        writer.beginObject();
        writer.name("a").value(1);
        writer.name("b").value(2);
        writer.endObject();
        assertEquals("{\n  \"a\": 1,\n  \"b\": 2\n}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testSetIndentNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent(null);
        writer.beginArray();
        writer.value(1);
        writer.endArray();
        assertEquals("[1]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testLenientDefault() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        assertFalse(writer.isLenient());
    }

    @Test(timeout = 4000)
    public void testLenientSet() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        assertTrue(writer.isLenient());
    }

    @Test(timeout = 4000)
    public void testHtmlSafeDefault() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        assertFalse(writer.isHtmlSafe());
    }

    @Test(timeout = 4000)
    public void testHtmlSafeSet() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        assertTrue(writer.isHtmlSafe());
    }

    @Test(timeout = 4000)
    public void testSerializeNullsDefault() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        assertTrue(writer.getSerializeNulls());
    }

    @Test(timeout = 4000)
    public void testSerializeNullsSetFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        assertFalse(writer.getSerializeNulls());
    }

    @Test(timeout = 4000)
    public void testSerializeNullsSetTrue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(true);
        assertTrue(writer.getSerializeNulls());
    }

    @Test(timeout = 4000)
    public void testConstructorNullWriter() {
        try {
            new JsonWriter(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testStackGrowth() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        for (int i = 0; i < 100; i++) {
            writer.beginArray();
        }
        for (int i = 0; i < 100; i++) {
            writer.endArray();
        }
        assertEquals("[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[[]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueBooleanTrue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(true);
        writer.endArray();
        assertEquals("[true]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueBooleanFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(false);
        writer.endArray();
        assertEquals("[false]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueLongMin() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Long.MIN_VALUE);
        writer.endArray();
        assertEquals("[-9223372036854775808]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueLongMax() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Long.MAX_VALUE);
        writer.endArray();
        assertEquals("[9223372036854775807]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueDoubleMin() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Double.MIN_VALUE);
        writer.endArray();
        assertEquals("[4.9E-324]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueDoubleMax() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Double.MAX_VALUE);
        writer.endArray();
        assertEquals("[1.7976931348623157E308]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueDoubleZero() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(0.0);
        writer.endArray();
        assertEquals("[0.0]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueDoubleNegativeZero() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(-0.0);
        writer.endArray();
        assertEquals("[-0.0]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueDoubleOne() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(1.0);
        writer.endArray();
        assertEquals("[1.0]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueDoubleNegativeOne() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(-1.0);
        writer.endArray();
        assertEquals("[-1.0]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueDoubleScientific() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(1.0E10);
        writer.endArray();
        assertEquals("[1.0E10]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueDoubleNegativeScientific() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(-1.0E-10);
        writer.endArray();
        assertEquals("[-1.0E-10]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueFloatMin() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Float.MIN_VALUE);
        writer.endArray();
        assertEquals("[1.4E-45]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueFloatMax() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(Float.MAX_VALUE);
        writer.endArray();
        assertEquals("[3.4028235E38]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueFloatZero() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(0.0f);
        writer.endArray();
        assertEquals("[0.0]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueFloatNegativeZero() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(-0.0f);
        writer.endArray();
        assertEquals("[-0.0]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueFloatOne() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(1.0f);
        writer.endArray();
        assertEquals("[1.0]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueFloatNegativeOne() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(-1.0f);
        writer.endArray();
        assertEquals("[-1.0]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueFloatScientific() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(1.0E10f);
        writer.endArray();
        assertEquals("[1.0E10]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueFloatNegativeScientific() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(-1.0E-10f);
        writer.endArray();
        assertEquals("[-1.0E-10]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueShort() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((short) 42);
        writer.endArray();
        assertEquals("[42]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueByte() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((byte) 42);
        writer.endArray();
        assertEquals("[42]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueCharacter() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value('a');
        writer.endArray();
        assertEquals("[\"a\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueCharacterSpecial() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value('\n');
        writer.endArray();
        assertEquals("[\"\\n\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueCharacterUnicode() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value('\u2028');
        writer.endArray();
        assertEquals("[\"\\u2028\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueCharacterHtmlSafe() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value('<');
        writer.endArray();
        assertEquals("[\"\\u003c\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueCharacterHtmlSafeFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value('<');
        writer.endArray();
        assertEquals("[\"<\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueCharacterNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Character) null);
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueCharacterNullInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("char").value((Character) null);
        writer.endObject();
        assertEquals("{\"char\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueCharacterNullSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("char").value((Character) null);
        writer.endObject();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueBooleanNullInArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Boolean) null);
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueBooleanNullInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("bool").value((Boolean) null);
        writer.endObject();
        assertEquals("{\"bool\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueBooleanNullSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("bool").value((Boolean) null);
        writer.endObject();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNumberNullInArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Number) null);
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNumberNullInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("num").value((Number) null);
        writer.endObject();
        assertEquals("{\"num\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueNumberNullSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("num").value((Number) null);
        writer.endObject();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueStringNullInArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((String) null);
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueStringNullInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("str").value((String) null);
        writer.endObject();
        assertEquals("{\"str\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueStringNullSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("str").value((String) null);
        writer.endObject();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) null);
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) "hello");
        writer.endArray();
        assertEquals("[\"hello\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectNumber() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) 42);
        writer.endArray();
        assertEquals("[42]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectBoolean() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) true);
        writer.endArray();
        assertEquals("[true]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCharacter() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) 'a');
        writer.endArray();
        assertEquals("[\"a\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectNullInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("obj").value((Object) null);
        writer.endObject();
        assertEquals("{\"obj\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectNullSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("obj").value((Object) null);
        writer.endObject();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectBooleanNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) (Boolean) null);
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectNumberNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) (Number) null);
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectStringNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) (String) null);
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCharacterNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) (Character) null);
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectBooleanNullInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("obj").value((Object) (Boolean) null);
        writer.endObject();
        assertEquals("{\"obj\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectBooleanNullSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("obj").value((Object) (Boolean) null);
        writer.endObject();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectNumberNullInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("obj").value((Object) (Number) null);
        writer.endObject();
        assertEquals("{\"obj\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectNumberNullSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("obj").value((Object) (Number) null);
        writer.endObject();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectStringNullInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("obj").value((Object) (String) null);
        writer.endObject();
        assertEquals("{\"obj\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectStringNullSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("obj").value((Object) (String) null);
        writer.endObject();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCharacterNullInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("obj").value((Object) (Character) null);
        writer.endObject();
        assertEquals("{\"obj\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCharacterNullSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("obj").value((Object) (Character) null);
        writer.endObject();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectBooleanTrue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) Boolean.TRUE);
        writer.endArray();
        assertEquals("[true]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectBooleanFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) Boolean.FALSE);
        writer.endArray();
        assertEquals("[false]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectInteger() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) Integer.valueOf(42));
        writer.endArray();
        assertEquals("[42]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectLong() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) Long.valueOf(42L));
        writer.endArray();
        assertEquals("[42]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectDouble() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) Double.valueOf(3.14));
        writer.endArray();
        assertEquals("[3.14]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectFloat() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) Float.valueOf(1.5f));
        writer.endArray();
        assertEquals("[1.5]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectShort() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) Short.valueOf((short) 42));
        writer.endArray();
        assertEquals("[42]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectByte() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) Byte.valueOf((byte) 42));
        writer.endArray();
        assertEquals("[42]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectBigInteger() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new java.math.BigInteger("123456789012345678901234567890"));
        writer.endArray();
        assertEquals("[123456789012345678901234567890]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectBigDecimal() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new java.math.BigDecimal("123.456"));
        writer.endArray();
        assertEquals("[123.456]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectNaN() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value((Object) Double.valueOf(Double.NaN));
        writer.endArray();
        assertEquals("[NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectInfinity() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value((Object) Double.valueOf(Double.POSITIVE_INFINITY));
        writer.endArray();
        assertEquals("[Infinity]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectNegativeInfinity() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value((Object) Double.valueOf(Double.NEGATIVE_INFINITY));
        writer.endArray();
        assertEquals("[-Infinity]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectNaNStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.value((Object) Double.valueOf(Double.NaN));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testValueObjectInfinityStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.value((Object) Double.valueOf(Double.POSITIVE_INFINITY));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testValueObjectNegativeInfinityStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.value((Object) Double.valueOf(Double.NEGATIVE_INFINITY));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testValueObjectFloatNaN() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value((Object) Float.valueOf(Float.NaN));
        writer.endArray();
        assertEquals("[NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectFloatInfinity() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value((Object) Float.valueOf(Float.POSITIVE_INFINITY));
        writer.endArray();
        assertEquals("[Infinity]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectFloatNegativeInfinity() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value((Object) Float.valueOf(Float.NEGATIVE_INFINITY));
        writer.endArray();
        assertEquals("[-Infinity]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectFloatNaNStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.value((Object) Float.valueOf(Float.NaN));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testValueObjectFloatInfinityStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.value((Object) Float.valueOf(Float.POSITIVE_INFINITY));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testValueObjectFloatNegativeInfinityStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.value((Object) Float.valueOf(Float.NEGATIVE_INFINITY));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomNumber() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Number() {
            @Override
            public int intValue() { return 42; }
            @Override
            public long longValue() { return 42L; }
            @Override
            public float floatValue() { return 42.0f; }
            @Override
            public double doubleValue() { return 42.0; }
            @Override
            public String toString() { return "custom"; }
        });
        writer.endArray();
        assertEquals("[custom]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomNumberNaN() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value((Object) new Number() {
            @Override
            public int intValue() { return 0; }
            @Override
            public long longValue() { return 0L; }
            @Override
            public float floatValue() { return 0.0f; }
            @Override
            public double doubleValue() { return 0.0; }
            @Override
            public String toString() { return "NaN"; }
        });
        writer.endArray();
        assertEquals("[NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomNumberInfinity() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value((Object) new Number() {
            @Override
            public int intValue() { return 0; }
            @Override
            public long longValue() { return 0L; }
            @Override
            public float floatValue() { return 0.0f; }
            @Override
            public double doubleValue() { return 0.0; }
            @Override
            public String toString() { return "Infinity"; }
        });
        writer.endArray();
        assertEquals("[Infinity]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomNumberNegativeInfinity() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value((Object) new Number() {
            @Override
            public int intValue() { return 0; }
            @Override
            public long longValue() { return 0L; }
            @Override
            public float floatValue() { return 0.0f; }
            @Override
            public double doubleValue() { return 0.0; }
            @Override
            public String toString() { return "-Infinity"; }
        });
        writer.endArray();
        assertEquals("[-Infinity]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomNumberNaNStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.value((Object) new Number() {
                @Override
                public int intValue() { return 0; }
                @Override
                public long longValue() { return 0L; }
                @Override
                public float floatValue() { return 0.0f; }
                @Override
                public double doubleValue() { return 0.0; }
                @Override
                public String toString() { return "NaN"; }
            });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomNumberInfinityStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.value((Object) new Number() {
                @Override
                public int intValue() { return 0; }
                @Override
                public long longValue() { return 0L; }
                @Override
                public float floatValue() { return 0.0f; }
                @Override
                public double doubleValue() { return 0.0; }
                @Override
                public String toString() { return "Infinity"; }
            });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomNumberNegativeInfinityStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.value((Object) new Number() {
                @Override
                public int intValue() { return 0; }
                @Override
                public long longValue() { return 0L; }
                @Override
                public float floatValue() { return 0.0f; }
                @Override
                public double doubleValue() { return 0.0; }
                @Override
                public String toString() { return "-Infinity"; }
            });
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "custom"; }
        });
        writer.endArray();
        assertEquals("[\"custom\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("obj").value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endObject();
        assertEquals("{\"obj\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("obj").value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endObject();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialChars() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlChars() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparator() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafe() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicode() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeed() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturn() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlash() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Integer.valueOf(42).toString(); }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return null; }
        });
        writer.endArray();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return ""; }
        });
        writer.endArray();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSpecialCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "hello\"world\\test\nnewline\t tab"; }
        });
        writer.endArray();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\\t tab\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectControlCharsObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "control\u0000char\u001f"; }
        });
        writer.endArray();
        assertEquals("[\"control\\u0000char\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeLineSeparatorObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "line\u2028separator\u2029"; }
        });
        writer.endArray();
        assertEquals("[\"line\\u2028separator\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dequal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectHtmlSafeFalseObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(false);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "<tag>&amp;=equal"; }
        });
        writer.endArray();
        assertEquals("[\"<tag>&amp;=equal\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectUnicodeObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "héllo wörld"; }
        });
        writer.endArray();
        assertEquals("[\"héllo wörld\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBackspaceAndFormFeedObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "back\bspace\fform"; }
        });
        writer.endArray();
        assertEquals("[\"back\\bspace\\fform\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectCarriageReturnObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "carriage\rreturn"; }
        });
        writer.endArray();
        assertEquals("[\"carriage\\rreturn\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectSlashObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "slash/backslash\\"; }
        });
        writer.endArray();
        assertEquals("[\"slash/backslash\\\\\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "true"; }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "42"; }
        });
        writer.endArray();
        assertEquals("[\"42\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNullStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "null"; }
        });
        writer.endArray();
        assertEquals("[\"null\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[]"; }
        });
        writer.endArray();
        assertEquals("[\"[]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectEmptyObjectStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{}"; }
        });
        writer.endArray();
        assertEquals("[\"{}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectJsonStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "{\"key\":\"value\"}"; }
        });
        writer.endArray();
        assertEquals("[\"{\\\"key\\\":\\\"value\\\"}\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectArrayStringObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return "[1,2,3]"; }
        });
        writer.endArray();
        assertEquals("[\"[1,2,3]\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectBooleanObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Object) new Object() {
            @Override
            public String toString() { return Boolean.TRUE.toString(); }
        });
        writer.endArray();
        assertEquals("[\"true\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueObjectCustomObjectNumberObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObjectObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);