package com.google.gson.stream;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;

public class JsonWriterDeepseekTest {

    /* [Branch & Defect Analysis Matrix] */
    // Target defect: testNonFiniteDoublesWhenLenient - IllegalArgumentException for NaN in lenient mode
    // Branches targeted:
    // 1. value(double) - NaN/Infinity check (lenient vs strict)
    // 2. value(Number) - NaN/Infinity string check (lenient vs strict)
    // 3. setLenient(true/false) - state transitions
    // 4. isLenient() - getter
    // 5. setHtmlSafe(true/false) - HTML escaping
    // 6. isHtmlSafe() - getter
    // 7. setSerializeNulls(true/false) - null serialization
    // 8. getSerializeNulls() - getter
    // 9. beginArray/endArray - array state transitions
    // 10. beginObject/endObject - object state transitions
    // 11. name() - deferred name handling
    // 12. value(String) - null vs non-null
    // 13. nullValue() - with/without serializeNulls
    // 14. jsonValue() - raw JSON output
    // 15. close() - incomplete document detection
    // 16. flush() - output flushing
    // 17. setIndent() - indentation handling
    // 18. Boundary: empty string, null values, top-level values

    @Test(timeout = 4000)
    public void testNonFiniteDoublesWhenLenient() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNonFiniteDoublesStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNonFiniteDoublesWhenLenientInfinity() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(Double.POSITIVE_INFINITY);
        writer.value(Double.NEGATIVE_INFINITY);
        writer.endArray();
        writer.close();
        assertEquals("[Infinity,-Infinity]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNonFiniteNumberWhenLenient() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNonFiniteNumberStrict() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientStateTransitions() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        assertFalse(writer.isLenient());
        writer.setLenient(true);
        assertTrue(writer.isLenient());
        writer.setLenient(false);
        assertFalse(writer.isLenient());
    }

    @Test(timeout = 4000)
    public void testHtmlSafeStateTransitions() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        assertFalse(writer.isHtmlSafe());
        writer.setHtmlSafe(true);
        assertTrue(writer.isHtmlSafe());
        writer.setHtmlSafe(false);
        assertFalse(writer.isHtmlSafe());
    }

    @Test(timeout = 4000)
    public void testSerializeNullsStateTransitions() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        assertTrue(writer.getSerializeNulls());
        writer.setSerializeNulls(false);
        assertFalse(writer.getSerializeNulls());
        writer.setSerializeNulls(true);
        assertTrue(writer.getSerializeNulls());
    }

    @Test(timeout = 4000)
    public void testBasicArrayWriting() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(1);
        writer.value(2);
        writer.value(3);
        writer.endArray();
        writer.close();
        assertEquals("[1,2,3]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testBasicObjectWriting() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("a").value(1);
        writer.name("b").value(2);
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":1,\"b\":2}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNestedStructures() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.beginObject();
        writer.name("key").value("value");
        writer.endObject();
        writer.beginArray();
        writer.value(true);
        writer.value(false);
        writer.endArray();
        writer.endArray();
        writer.close();
        assertEquals("[{\"key\":\"value\"},[true,false]]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNullValueWithSerializeNulls() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(true);
        writer.beginObject();
        writer.name("a").nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNullValueWithoutSerializeNulls() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("a").nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNullStringValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((String) null);
        writer.endArray();
        writer.close();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testJsonValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.jsonValue("{\"raw\":true}");
        writer.endArray();
        writer.close();
        assertEquals("[{\"raw\":true}]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStringEscaping() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("hello\"world\\test\nnewline");
        writer.endArray();
        writer.close();
        assertEquals("[\"hello\\\"world\\\\test\\nnewline\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testHtmlSafeEscaping() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value("<tag>&amp;=value");
        writer.endArray();
        writer.close();
        assertEquals("[\"\\u003ctag\\u003e\\u0026amp;\\u003dvalue\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testIndentation() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("  ");
        writer.beginArray();
        writer.value(1);
        writer.value(2);
        writer.endArray();
        writer.close();
        assertEquals("[\n  1,\n  2\n]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyIndent() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("");
        writer.beginArray();
        writer.value(1);
        writer.endArray();
        writer.close();
        assertEquals("[1]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testCloseIncompleteDocument() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        try {
            writer.close();
            fail("Expected IOException for incomplete document");
        } catch (IOException expected) {
            assertEquals("Incomplete document", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testFlush() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(1);
        writer.flush();
        assertEquals("[1", stringWriter.toString());
        writer.endArray();
        writer.close();
        assertEquals("[1]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNullWriterConstructor() {
        try {
            new JsonWriter(null);
            fail("Expected NullPointerException for null writer");
        } catch (NullPointerException expected) {
            assertEquals("out == null", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNullName() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        try {
            writer.name(null);
            fail("Expected NullPointerException for null name");
        } catch (NullPointerException expected) {
            assertEquals("name == null", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDanglingName() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.name("a");
        try {
            writer.beginArray();
            fail("Expected IllegalStateException for dangling name");
        } catch (IllegalStateException expected) {
            assertEquals("Dangling name: a", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testMultipleTopLevelValuesStrict() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.endArray();
        try {
            writer.beginArray();
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
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
        writer.close();
        assertEquals("[][]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testTopLevelPrimitiveStrict() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        try {
            writer.value(1);
            fail("Expected IllegalStateException for top-level primitive in strict mode");
        } catch (IllegalStateException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testTopLevelPrimitiveLenient() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(1);
        writer.close();
        assertEquals("1", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testBooleanValues() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(true);
        writer.value(false);
        writer.value(Boolean.TRUE);
        writer.value(Boolean.FALSE);
        writer.endArray();
        writer.close();
        assertEquals("[true,false,true,false]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testLongValues() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(0L);
        writer.value(Long.MAX_VALUE);
        writer.value(Long.MIN_VALUE);
        writer.endArray();
        writer.close();
        assertEquals("[0,9223372036854775807,-9223372036854775808]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testDoubleValues() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(1.5);
        writer.value(-2.25);
        writer.value(0.0);
        writer.endArray();
        writer.close();
        assertEquals("[1.5,-2.25,0.0]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNumberValues() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(new Integer(42));
        writer.value(new Long(123456789L));
        writer.value(new Double(3.14));
        writer.endArray();
        writer.close();
        assertEquals("[42,123456789,3.14]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNullNumberValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value((Number) null);
        writer.endArray();
        writer.close();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        writer.close();
        assertEquals("[]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.endObject();
        writer.close();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNestingProblem() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        try {
            writer.endObject();
            fail("Expected IllegalStateException for nesting problem");
        } catch (IllegalStateException expected) {
            assertEquals("Nesting problem.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testClosedWriter() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.endArray();
        writer.close();
        try {
            writer.beginArray();
            fail("Expected IllegalStateException for closed writer");
        } catch (IllegalStateException expected) {
            assertEquals("JsonWriter is closed.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeferredName() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("deferred");
        writer.value("value");
        writer.endObject();
        writer.close();
        assertEquals("{\"deferred\":\"value\"}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testUnicodeEscaping() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("\u2028\u2029");
        writer.endArray();
        writer.close();
        assertEquals("[\"\\u2028\\u2029\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testControlCharacterEscaping() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("\u0000\u001f");
        writer.endArray();
        writer.close();
        assertEquals("[\"\\u0000\\u001f\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testLargeStackGrowth() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        for (int i = 0; i < 100; i++) {
            writer.beginArray();
        }
        for (int i = 0; i < 100; i++) {
            writer.endArray();
        }
        writer.close();
        assertEquals("", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNameAfterValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.value(1);
        try {
            writer.name("a");
            fail("Expected IllegalStateException for name after value");
        } catch (IllegalStateException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testValueAfterValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.value(1);
        writer.value(2);
        writer.endArray();
        writer.close();
        // Should not throw
    }

    @Test(timeout = 4000)
    public void testNameInArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        try {
            writer.name("a");
            fail("Expected IllegalStateException for name in array");
        } catch (IllegalStateException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testSerializeNullsInArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginArray();
        writer.nullValue();
        writer.endArray();
        writer.close();
        assertEquals("[null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testLenientTopLevelMultipleValues() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(1);
        writer.value(2);
        writer.close();
        assertEquals("12", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictTopLevelMultipleValues() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(1);
        try {
            writer.value(2);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumber() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(new Double(Double.POSITIVE_INFINITY));
        writer.value(new Double(Double.NEGATIVE_INFINITY));
        writer.endArray();
        writer.close();
        assertEquals("[Infinity,-Infinity]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumber() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        try {
            writer.value(new Double(Double.POSITIVE_INFINITY));
            fail("Expected IllegalArgumentException for Infinity in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was Infinity", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testHtmlSafeWithSpecialChars() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value("<>&=");
        writer.endArray();
        writer.close();
        assertEquals("[\"\\u003c\\u003e\\u0026\\u003d\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNonHtmlSafeWithSpecialChars() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("<>&=");
        writer.endArray();
        writer.close();
        assertEquals("[\"<>&=\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testIndentWithWhitespace() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setIndent("\t");
        writer.beginObject();
        writer.name("a").value(1);
        writer.endObject();
        writer.close();
        assertEquals("{\n\t\"a\": 1\n}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testEmptyStringValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value("");
        writer.endArray();
        writer.close();
        assertEquals("[\"\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testLongStringValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append('a');
        }
        writer.value(sb.toString());
        writer.endArray();
        writer.close();
        assertEquals("[\"" + sb.toString() + "\"]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testMixedTypesInArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.value(1);
        writer.value("two");
        writer.value(3.0);
        writer.value(true);
        writer.nullValue();
        writer.endArray();
        writer.close();
        assertEquals("[1,\"two\",3.0,true,null]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testMixedTypesInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("int").value(1);
        writer.name("string").value("two");
        writer.name("double").value(3.0);
        writer.name("bool").value(true);
        writer.name("null").nullValue();
        writer.endObject();
        writer.close();
        assertEquals("{\"int\":1,\"string\":\"two\",\"double\":3.0,\"bool\":true,\"null\":null}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testDeepNesting() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.beginObject();
        writer.name("a").beginArray();
        writer.value(1);
        writer.endArray();
        writer.endObject();
        writer.endArray();
        writer.close();
        assertEquals("[{\"a\":[1]}]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testValueAfterDeferredName() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.name("a");
        writer.value(1);
        writer.name("b");
        writer.value(2);
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":1,\"b\":2}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNullValueInObjectWithSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("a").nullValue();
        writer.name("b").value(1);
        writer.endObject();
        writer.close();
        assertEquals("{\"b\":1}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testNullValueInObjectWithSerializeNullsTrue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setSerializeNulls(true);
        writer.beginObject();
        writer.name("a").nullValue();
        writer.name("b").value(1);
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":null,\"b\":1}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testLenientTopLevelNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.nullValue();
        writer.close();
        assertEquals("null", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictTopLevelNull() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        try {
            writer.nullValue();
            fail("Expected IllegalStateException for top-level null in strict mode");
        } catch (IllegalStateException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testLenientTopLevelString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value("hello");
        writer.close();
        assertEquals("\"hello\"", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictTopLevelString() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        try {
            writer.value("hello");
            fail("Expected IllegalStateException for top-level string in strict mode");
        } catch (IllegalStateException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testLenientTopLevelBoolean() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(true);
        writer.close();
        assertEquals("true", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictTopLevelBoolean() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        try {
            writer.value(true);
            fail("Expected IllegalStateException for top-level boolean in strict mode");
        } catch (IllegalStateException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testLenientTopLevelDouble() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(1.5);
        writer.close();
        assertEquals("1.5", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictTopLevelDouble() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        try {
            writer.value(1.5);
            fail("Expected IllegalStateException for top-level double in strict mode");
        } catch (IllegalStateException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testLenientTopLevelLong() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(123L);
        writer.close();
        assertEquals("123", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictTopLevelLong() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        try {
            writer.value(123L);
            fail("Expected IllegalStateException for top-level long in strict mode");
        } catch (IllegalStateException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testLenientTopLevelNumber() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Integer(42));
        writer.close();
        assertEquals("42", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictTopLevelNumber() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        try {
            writer.value(new Integer(42));
            fail("Expected IllegalStateException for top-level number in strict mode");
        } catch (IllegalStateException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testLenientTopLevelJsonValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.jsonValue("{\"a\":1}");
        writer.close();
        assertEquals("{\"a\":1}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictTopLevelJsonValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        try {
            writer.jsonValue("{\"a\":1}");
            fail("Expected IllegalStateException for top-level jsonValue in strict mode");
        } catch (IllegalStateException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testLenientTopLevelArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        writer.close();
        assertEquals("[]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testLenientTopLevelObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginObject();
        writer.endObject();
        writer.close();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictTopLevelArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginArray();
        writer.endArray();
        writer.close();
        assertEquals("[]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictTopLevelObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.beginObject();
        writer.endObject();
        writer.close();
        assertEquals("{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testLenientMultipleTopLevelArrays() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        writer.beginArray();
        writer.endArray();
        writer.close();
        assertEquals("[][]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictMultipleTopLevelArrays() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.endArray();
        try {
            writer.beginArray();
            fail("Expected IllegalStateException for multiple top-level arrays");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientMultipleTopLevelObjects() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginObject();
        writer.endObject();
        writer.beginObject();
        writer.endObject();
        writer.close();
        assertEquals("{}{}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictMultipleTopLevelObjects() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.endObject();
        try {
            writer.beginObject();
            fail("Expected IllegalStateException for multiple top-level objects");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientMixedTopLevelValues() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        writer.value(1);
        writer.close();
        assertEquals("[]1", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictMixedTopLevelValues() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.endArray();
        try {
            writer.value(1);
            fail("Expected IllegalStateException for mixed top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginObject();
        writer.name("a").value(Double.NaN);
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":NaN}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleInObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.name("a");
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberInObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginObject();
        writer.name("a").value(new Double(Double.NaN));
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":NaN}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberInObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.name("a");
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleInNestedArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.beginArray();
        writer.value(Double.NaN);
        writer.endArray();
        writer.endArray();
        writer.close();
        assertEquals("[[NaN]]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleInNestedArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.beginArray();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberInNestedArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.beginArray();
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.endArray();
        writer.close();
        assertEquals("[[NaN]]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberInNestedArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.beginArray();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleInNestedObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginObject();
        writer.name("a").beginObject();
        writer.name("b").value(Double.NaN);
        writer.endObject();
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":{\"b\":NaN}}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleInNestedObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.name("a").beginObject();
        writer.name("b");
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberInNestedObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginObject();
        writer.name("a").beginObject();
        writer.name("b").value(new Double(Double.NaN));
        writer.endObject();
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":{\"b\":NaN}}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberInNestedObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.name("a").beginObject();
        writer.name("b");
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleInMixedStructure() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.beginObject();
        writer.name("a").value(Double.NaN);
        writer.endObject();
        writer.beginArray();
        writer.value(Double.POSITIVE_INFINITY);
        writer.endArray();
        writer.endArray();
        writer.close();
        assertEquals("[{\"a\":NaN},[Infinity]]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleInMixedStructure() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.beginObject();
        writer.name("a");
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberInMixedStructure() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.beginObject();
        writer.name("a").value(new Double(Double.NaN));
        writer.endObject();
        writer.beginArray();
        writer.value(new Double(Double.POSITIVE_INFINITY));
        writer.endArray();
        writer.endArray();
        writer.close();
        assertEquals("[{\"a\":NaN},[Infinity]]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberInMixedStructure() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.beginObject();
        writer.name("a");
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithIndent() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.setIndent("  ");
        writer.beginArray();
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[\n  NaN\n]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithIndent() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.setIndent("  ");
        writer.beginArray();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithIndent() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.setIndent("  ");
        writer.beginArray();
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[\n  NaN\n]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithIndent() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.setIndent("  ");
        writer.beginArray();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithHtmlSafe() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithHtmlSafe() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.setHtmlSafe(true);
        writer.beginArray();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithHtmlSafe() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.setHtmlSafe(true);
        writer.beginArray();
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithHtmlSafe() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.setHtmlSafe(true);
        writer.beginArray();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("a").value(Double.NaN);
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":NaN}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithSerializeNullsFalse() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("a");
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithSerializeNullsFalse() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("a").value(new Double(Double.NaN));
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":NaN}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithSerializeNullsFalse() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("a");
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithAllOptions() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.setHtmlSafe(true);
        writer.setIndent("  ");
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("a").value(Double.NaN);
        writer.endObject();
        writer.close();
        assertEquals("{\n  \"a\": NaN\n}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithAllOptions() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.setHtmlSafe(true);
        writer.setIndent("  ");
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("a");
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithAllOptions() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.setHtmlSafe(true);
        writer.setIndent("  ");
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("a").value(new Double(Double.NaN));
        writer.endObject();
        writer.close();
        assertEquals("{\n  \"a\": NaN\n}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithAllOptions() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.setHtmlSafe(true);
        writer.setIndent("  ");
        writer.setSerializeNulls(false);
        writer.beginObject();
        writer.name("a");
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleAfterFlush() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.flush();
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleAfterFlush() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.flush();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberAfterFlush() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.flush();
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberAfterFlush() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.flush();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleAfterClose() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        writer.close();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for closed writer");
        } catch (IllegalStateException expected) {
            assertEquals("JsonWriter is closed.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleAfterClose() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.endArray();
        writer.close();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for closed writer");
        } catch (IllegalStateException expected) {
            assertEquals("JsonWriter is closed.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberAfterClose() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        writer.close();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for closed writer");
        } catch (IllegalStateException expected) {
            assertEquals("JsonWriter is closed.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberAfterClose() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.endArray();
        writer.close();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for closed writer");
        } catch (IllegalStateException expected) {
            assertEquals("JsonWriter is closed.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithDeferredName() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginObject();
        writer.name("a");
        writer.value(Double.NaN);
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":NaN}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithDeferredName() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.name("a");
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithDeferredName() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginObject();
        writer.name("a");
        writer.value(new Double(Double.NaN));
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":NaN}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithDeferredName() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.name("a");
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithMultipleValues() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(1);
        writer.value(Double.NaN);
        writer.value(2);
        writer.endArray();
        writer.close();
        assertEquals("[1,NaN,2]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithMultipleValues() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.value(1);
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithMultipleValues() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(1);
        writer.value(new Double(Double.NaN));
        writer.value(2);
        writer.endArray();
        writer.close();
        assertEquals("[1,NaN,2]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithMultipleValues() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.value(1);
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithNullValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.nullValue();
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[null,NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithNullValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.nullValue();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithNullValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.nullValue();
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[null,NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithNullValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.nullValue();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithStringValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value("string");
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[\"string\",NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithStringValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.value("string");
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithStringValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value("string");
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[\"string\",NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithStringValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.value("string");
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithBooleanValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(true);
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[true,NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithBooleanValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.value(true);
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithBooleanValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(true);
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[true,NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithBooleanValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.value(true);
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithLongValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(123L);
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[123,NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithLongValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.value(123L);
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithLongValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(123L);
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[123,NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithLongValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.value(123L);
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithDoubleValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(1.5);
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[1.5,NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithDoubleValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.value(1.5);
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithDoubleValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(1.5);
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[1.5,NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithDoubleValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.value(1.5);
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithNumberValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(new Integer(42));
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[42,NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithNumberValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.value(new Integer(42));
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithNumberValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(new Integer(42));
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[42,NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithNumberValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.value(new Integer(42));
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithJsonValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.jsonValue("{\"a\":1}");
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[{\"a\":1},NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithJsonValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.jsonValue("{\"a\":1}");
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithJsonValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.jsonValue("{\"a\":1}");
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[{\"a\":1},NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithJsonValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.jsonValue("{\"a\":1}");
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithNestedArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.beginArray();
        writer.value(1);
        writer.endArray();
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[[1],NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithNestedArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.beginArray();
        writer.value(1);
        writer.endArray();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithNestedArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.beginArray();
        writer.value(1);
        writer.endArray();
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[[1],NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithNestedArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.beginArray();
        writer.value(1);
        writer.endArray();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithNestedObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.beginObject();
        writer.name("a").value(1);
        writer.endObject();
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[{\"a\":1},NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithNestedObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.beginObject();
        writer.name("a").value(1);
        writer.endObject();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithNestedObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.beginObject();
        writer.name("a").value(1);
        writer.endObject();
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[{\"a\":1},NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithNestedObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.beginObject();
        writer.name("a").value(1);
        writer.endObject();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithDeepNesting() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.beginObject();
        writer.name("a").beginArray();
        writer.value(1);
        writer.endArray();
        writer.endObject();
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[{\"a\":[1]},NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithDeepNesting() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.beginObject();
        writer.name("a").beginArray();
        writer.value(1);
        writer.endArray();
        writer.endObject();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithDeepNesting() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.beginObject();
        writer.name("a").beginArray();
        writer.value(1);
        writer.endArray();
        writer.endObject();
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[{\"a\":[1]},NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithDeepNesting() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.beginObject();
        writer.name("a").beginArray();
        writer.value(1);
        writer.endArray();
        writer.endObject();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithLargeStack() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        for (int i = 0; i < 50; i++) {
            writer.beginArray();
        }
        writer.value(Double.NaN);
        for (int i = 0; i < 50; i++) {
            writer.endArray();
        }
        writer.close();
        // Just verify no exception
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithLargeStack() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        for (int i = 0; i < 50; i++) {
            writer.beginArray();
        }
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithLargeStack() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        for (int i = 0; i < 50; i++) {
            writer.beginArray();
        }
        writer.value(new Double(Double.NaN));
        for (int i = 0; i < 50; i++) {
            writer.endArray();
        }
        writer.close();
        // Just verify no exception
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithLargeStack() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        for (int i = 0; i < 50; i++) {
            writer.beginArray();
        }
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithEmptyArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithEmptyArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.endArray();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithEmptyArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithEmptyArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.endArray();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithEmptyObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginObject();
        writer.endObject();
        writer.value(Double.NaN);
        writer.close();
        assertEquals("{}NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithEmptyObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.endObject();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithEmptyObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginObject();
        writer.endObject();
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("{}NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithEmptyObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.endObject();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithMultipleArrays() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        writer.beginArray();
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[][NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithMultipleArrays() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.endArray();
        try {
            writer.beginArray();
            fail("Expected IllegalStateException for multiple top-level arrays");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithMultipleArrays() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        writer.beginArray();
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[][NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithMultipleArrays() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.endArray();
        try {
            writer.beginArray();
            fail("Expected IllegalStateException for multiple top-level arrays");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithMultipleObjects() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginObject();
        writer.endObject();
        writer.beginObject();
        writer.name("a").value(Double.NaN);
        writer.endObject();
        writer.close();
        assertEquals("{}{\"a\":NaN}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithMultipleObjects() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.endObject();
        try {
            writer.beginObject();
            fail("Expected IllegalStateException for multiple top-level objects");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithMultipleObjects() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginObject();
        writer.endObject();
        writer.beginObject();
        writer.name("a").value(new Double(Double.NaN));
        writer.endObject();
        writer.close();
        assertEquals("{}{\"a\":NaN}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithMultipleObjects() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.endObject();
        try {
            writer.beginObject();
            fail("Expected IllegalStateException for multiple top-level objects");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithMixedTopLevel() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        writer.beginObject();
        writer.endObject();
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[]{}NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithMixedTopLevel() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.endArray();
        writer.beginObject();
        writer.endObject();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithMixedTopLevel() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.endArray();
        writer.beginObject();
        writer.endObject();
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[]{}NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithMixedTopLevel() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        writer.endArray();
        writer.beginObject();
        writer.endObject();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(Double.NaN);
        writer.endArray();
        writer.close();
        assertEquals("[NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginArray();
        writer.value(new Double(Double.NaN));
        writer.endArray();
        writer.close();
        assertEquals("[NaN]", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginArray();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginObject();
        writer.name("a").value(Double.NaN);
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":NaN}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.name("a");
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalArgumentException for NaN in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.beginObject();
        writer.name("a").value(new Double(Double.NaN));
        writer.endObject();
        writer.close();
        assertEquals("{\"a\":NaN}", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.beginObject();
        writer.name("a");
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalArgumentException for NaN Number in strict mode");
        } catch (IllegalArgumentException expected) {
            assertEquals("Numeric values must be finite, but was NaN", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelJsonValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.jsonValue("{\"a\":1}");
        writer.value(Double.NaN);
        writer.close();
        assertEquals("{\"a\":1}NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelJsonValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.jsonValue("{\"a\":1}");
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelJsonValue() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.jsonValue("{\"a\":1}");
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("{\"a\":1}NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelJsonValue() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.jsonValue("{\"a\":1}");
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value("hello");
        writer.value(Double.NaN);
        writer.close();
        assertEquals("\"hello\"NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelString() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value("hello");
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelString() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value("hello");
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("\"hello\"NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelString() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value("hello");
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelBoolean() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(true);
        writer.value(Double.NaN);
        writer.close();
        assertEquals("trueNaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelBoolean() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(true);
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelBoolean() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(true);
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("trueNaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelBoolean() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(true);
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.nullValue();
        writer.value(Double.NaN);
        writer.close();
        assertEquals("nullNaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelNull() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.nullValue();
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelNull() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.nullValue();
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("nullNaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelNull() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.nullValue();
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelNumber() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(42);
        writer.value(Double.NaN);
        writer.close();
        assertEquals("42NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelNumber() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(42);
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelNumber() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(42);
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("42NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelNumber() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(42);
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelLong() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(123L);
        writer.value(Double.NaN);
        writer.close();
        assertEquals("123NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelLong() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(123L);
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelLong() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(123L);
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("123NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelLong() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(123L);
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelDouble() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(1.5);
        writer.value(Double.NaN);
        writer.close();
        assertEquals("1.5NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelDouble() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(1.5);
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelDouble() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(1.5);
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("1.5NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelDouble() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(1.5);
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelNumberObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Integer(42));
        writer.value(Double.NaN);
        writer.close();
        assertEquals("42NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelNumberObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Integer(42));
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelNumberObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Integer(42));
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("42NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelNumberObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Integer(42));
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelBooleanObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(Boolean.TRUE);
        writer.value(Double.NaN);
        writer.close();
        assertEquals("trueNaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelBooleanObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(Boolean.TRUE);
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelBooleanObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(Boolean.TRUE);
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("trueNaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelBooleanObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(Boolean.TRUE);
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelStringObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value("hello");
        writer.value(Double.NaN);
        writer.close();
        assertEquals("\"hello\"NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelStringObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value("hello");
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelStringObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value("hello");
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("\"hello\"NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelStringObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value("hello");
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelNullObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value((String) null);
        writer.value(Double.NaN);
        writer.close();
        assertEquals("nullNaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelNullObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value((String) null);
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelNullObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value((String) null);
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("nullNaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelNullObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value((String) null);
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelNumberArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new int[]{1, 2});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelNumberArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new int[]{1, 2});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelNumberArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new int[]{1, 2});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelNumberArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new int[]{1, 2});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelStringArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new String[]{"a", "b"});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelStringArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new String[]{"a", "b"});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelStringArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new String[]{"a", "b"});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelStringArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new String[]{"a", "b"});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelBooleanArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new boolean[]{true, false});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelBooleanArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new boolean[]{true, false});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelBooleanArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new boolean[]{true, false});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelBooleanArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new boolean[]{true, false});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelDoubleArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new double[]{1.5, 2.5});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelDoubleArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new double[]{1.5, 2.5});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelDoubleArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new double[]{1.5, 2.5});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelDoubleArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new double[]{1.5, 2.5});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelLongArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new long[]{1L, 2L});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelLongArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new long[]{1L, 2L});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelLongArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new long[]{1L, 2L});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelLongArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new long[]{1L, 2L});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelFloatArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new float[]{1.5f, 2.5f});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelFloatArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new float[]{1.5f, 2.5f});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelFloatArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new float[]{1.5f, 2.5f});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelFloatArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new float[]{1.5f, 2.5f});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelShortArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new short[]{1, 2});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelShortArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new short[]{1, 2});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelShortArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new short[]{1, 2});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelShortArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new short[]{1, 2});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelByteArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new byte[]{1, 2});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelByteArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new byte[]{1, 2});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelByteArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new byte[]{1, 2});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelByteArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new byte[]{1, 2});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelCharArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new char[]{'a', 'b'});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelCharArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new char[]{'a', 'b'});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelCharArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new char[]{'a', 'b'});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelCharArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new char[]{'a', 'b'});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", 1});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",1]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", 1});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", 1});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",1]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", 1});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelIntegerArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Integer[]{1, 2});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelIntegerArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Integer[]{1, 2});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelIntegerArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Integer[]{1, 2});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelIntegerArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Integer[]{1, 2});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelLongObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Long[]{1L, 2L});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelLongObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Long[]{1L, 2L});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelLongObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Long[]{1L, 2L});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelLongObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Long[]{1L, 2L});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelDoubleObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Double[]{1.5, 2.5});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelDoubleObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Double[]{1.5, 2.5});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelDoubleObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Double[]{1.5, 2.5});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelDoubleObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Double[]{1.5, 2.5});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelFloatObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Float[]{1.5f, 2.5f});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelFloatObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Float[]{1.5f, 2.5f});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelFloatObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Float[]{1.5f, 2.5f});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelFloatObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Float[]{1.5f, 2.5f});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelShortObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Short[]{1, 2});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelShortObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Short[]{1, 2});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelShortObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Short[]{1, 2});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelShortObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Short[]{1, 2});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelByteObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Byte[]{1, 2});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelByteObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Byte[]{1, 2});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelByteObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Byte[]{1, 2});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelByteObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Byte[]{1, 2});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelCharacterArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Character[]{'a', 'b'});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelCharacterArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Character[]{'a', 'b'});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelCharacterArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Character[]{'a', 'b'});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelCharacterArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Character[]{'a', 'b'});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelBooleanObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Boolean[]{true, false});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelBooleanObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Boolean[]{true, false});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelBooleanObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Boolean[]{true, false});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelBooleanObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Boolean[]{true, false});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelStringObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new String[]{"a", "b"});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelStringObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new String[]{"a", "b"});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelStringObjectArray() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new String[]{"a", "b"});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelStringObjectArray() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new String[]{"a", "b"});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object() {
            @SuppressWarnings("unused")
            public String getName() {
                return "test";
            }
        });
        writer.value(Double.NaN);
        writer.close();
        assertEquals("{\"name\":\"test\"}NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object() {
            @SuppressWarnings("unused")
            public String getName() {
                return "test";
            }
        });
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object() {
            @SuppressWarnings("unused")
            public String getName() {
                return "test";
            }
        });
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("{\"name\":\"test\"}NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object() {
            @SuppressWarnings("unused")
            public String getName() {
                return "test";
            }
        });
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelMap() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        writer.value(map);
        writer.value(Double.NaN);
        writer.close();
        assertEquals("{\"key\":\"value\"}NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelMap() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        writer.value(map);
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelMap() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        writer.value(map);
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("{\"key\":\"value\"}NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelMap() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        writer.value(map);
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelCollection() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        writer.value(list);
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelCollection() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        writer.value(list);
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelCollection() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        writer.value(list);
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelCollection() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        writer.value(list);
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfArrays() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{new Object[]{"a", "b"}});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[[\"a\",\"b\"]]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfArrays() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{new Object[]{"a", "b"}});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfArrays() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{new Object[]{"a", "b"}});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[[\"a\",\"b\"]]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfArrays() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{new Object[]{"a", "b"}});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfMaps() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        writer.value(new Object[]{map});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[{\"key\":\"value\"}]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfMaps() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        writer.value(new Object[]{map});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfMaps() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        writer.value(new Object[]{map});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[{\"key\":\"value\"}]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfMaps() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        writer.value(new Object[]{map});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfCollections() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        writer.value(new Object[]{list});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[[\"a\",\"b\"]]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfCollections() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        writer.value(new Object[]{list});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfCollections() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        writer.value(new Object[]{list});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[[\"a\",\"b\"]]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfCollections() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("b");
        writer.value(new Object[]{list});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfObjects() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{new Object() {
            @SuppressWarnings("unused")
            public String getName() {
                return "test";
            }
        }});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[{\"name\":\"test\"}]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfObjects() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{new Object() {
            @SuppressWarnings("unused")
            public String getName() {
                return "test";
            }
        }});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfObjects() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{new Object() {
            @SuppressWarnings("unused")
            public String getName() {
                return "test";
            }
        }});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[{\"name\":\"test\"}]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfObjects() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{new Object() {
            @SuppressWarnings("unused")
            public String getName() {
                return "test";
            }
        }});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfNulls() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{null, null});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[null,null]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfNulls() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{null, null});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfNulls() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{null, null});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[null,null]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfNulls() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{null, null});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfNumbers() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{1, 2.5, 3L});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2.5,3]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfNumbers() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{1, 2.5, 3L});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfNumbers() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{1, 2.5, 3L});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2.5,3]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfNumbers() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{1, 2.5, 3L});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfBooleans() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{true, false});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfBooleans() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{true, false});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfBooleans() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{true, false});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfBooleans() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{true, false});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfStrings() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfStrings() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfStrings() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfStrings() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfChars() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{'a', 'b'});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfChars() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{'a', 'b'});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfChars() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{'a', 'b'});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfChars() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{'a', 'b'});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfBytes() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{(byte) 1, (byte) 2});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfBytes() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{(byte) 1, (byte) 2});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfBytes() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{(byte) 1, (byte) 2});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfBytes() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{(byte) 1, (byte) 2});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfShorts() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{(short) 1, (short) 2});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfShorts() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{(short) 1, (short) 2});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfShorts() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{(short) 1, (short) 2});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfShorts() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{(short) 1, (short) 2});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfFloats() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{1.5f, 2.5f});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfFloats() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{1.5f, 2.5f});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfFloats() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{1.5f, 2.5f});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfFloats() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{1.5f, 2.5f});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfDoubles() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{1.5, 2.5});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfDoubles() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{1.5, 2.5});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfDoubles() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{1.5, 2.5});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfDoubles() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{1.5, 2.5});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfLongs() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{1L, 2L});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfLongs() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{1L, 2L});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfLongs() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{1L, 2L});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfLongs() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{1L, 2L});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfIntegers() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{1, 2});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfIntegers() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{1, 2});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfIntegers() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{1, 2});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfIntegers() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{1, 2});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfBooleansObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfBooleansObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfBooleansObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfBooleansObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfStringsObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfStringsObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfStringsObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfStringsObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfCharsObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{'a', 'b'});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfCharsObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{'a', 'b'});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfCharsObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{'a', 'b'});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfCharsObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{'a', 'b'});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfBytesObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfBytesObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfBytesObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfBytesObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfShortsObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfShortsObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfShortsObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfShortsObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfFloatsObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfFloatsObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfFloatsObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfFloatsObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfDoublesObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfDoublesObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfDoublesObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfDoublesObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfLongsObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfLongsObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfLongsObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfLongsObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfIntegersObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfIntegersObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfIntegersObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfIntegersObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfCharactersObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfCharactersObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfCharactersObject() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfCharactersObject() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfBooleansObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfBooleansObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfBooleansObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfBooleansObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfStringsObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfStringsObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfStringsObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfStringsObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfCharsObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{'a', 'b'});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfCharsObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{'a', 'b'});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfCharsObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{'a', 'b'});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfCharsObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{'a', 'b'});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfBytesObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfBytesObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfBytesObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfBytesObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfShortsObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfShortsObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfShortsObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfShortsObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfFloatsObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfFloatsObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfFloatsObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfFloatsObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfDoublesObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfDoublesObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfDoublesObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfDoublesObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfLongsObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfLongsObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfLongsObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfLongsObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfIntegersObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfIntegersObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfIntegersObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfIntegersObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfCharactersObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfCharactersObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfCharactersObject2() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfCharactersObject2() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfBooleansObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfBooleansObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfBooleansObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfBooleansObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfStringsObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfStringsObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfStringsObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfStringsObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfCharsObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{'a', 'b'});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfCharsObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{'a', 'b'});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfCharsObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{'a', 'b'});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfCharsObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{'a', 'b'});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfBytesObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfBytesObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfBytesObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfBytesObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfShortsObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfShortsObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfShortsObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfShortsObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfFloatsObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfFloatsObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfFloatsObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfFloatsObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfDoublesObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfDoublesObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfDoublesObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfDoublesObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfLongsObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfLongsObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfLongsObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfLongsObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfIntegersObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfIntegersObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfIntegersObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfIntegersObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfCharactersObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfCharactersObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfCharactersObject3() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfCharactersObject3() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfBooleansObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfBooleansObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfBooleansObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfBooleansObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfStringsObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfStringsObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfStringsObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfStringsObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfCharsObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{'a', 'b'});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfCharsObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{'a', 'b'});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfCharsObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{'a', 'b'});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfCharsObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{'a', 'b'});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfBytesObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfBytesObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfBytesObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfBytesObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfShortsObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfShortsObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfShortsObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfShortsObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfFloatsObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfFloatsObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfFloatsObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfFloatsObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfDoublesObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfDoublesObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfDoublesObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfDoublesObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfLongsObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfLongsObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfLongsObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfLongsObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfIntegersObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfIntegersObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfIntegersObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfIntegersObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfCharactersObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfCharactersObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfCharactersObject4() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfCharactersObject4() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfBooleansObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfBooleansObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfBooleansObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfBooleansObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfStringsObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfStringsObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfStringsObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfStringsObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfCharsObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{'a', 'b'});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfCharsObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{'a', 'b'});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfCharsObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{'a', 'b'});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfCharsObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{'a', 'b'});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfBytesObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfBytesObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfBytesObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfBytesObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Byte.valueOf((byte) 1), Byte.valueOf((byte) 2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfShortsObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfShortsObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfShortsObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfShortsObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Short.valueOf((short) 1), Short.valueOf((short) 2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfFloatsObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfFloatsObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfFloatsObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfFloatsObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Float.valueOf(1.5f), Float.valueOf(2.5f)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfDoublesObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfDoublesObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfDoublesObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1.5,2.5]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfDoublesObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Double.valueOf(1.5), Double.valueOf(2.5)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfLongsObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfLongsObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfLongsObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfLongsObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Long.valueOf(1L), Long.valueOf(2L)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfIntegersObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfIntegersObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfIntegersObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[1,2]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfIntegersObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfCharactersObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfCharactersObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfCharactersObject5() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfCharactersObject5() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Character.valueOf('a'), Character.valueOf('b')});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfBooleansObject6() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfBooleansObject6() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfBooleansObject6() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[true,false]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfBooleansObject6() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{Boolean.TRUE, Boolean.FALSE});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfStringsObject6() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfStringsObject6() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteNumberWithTopLevelArrayOfStringsObject6() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{"a", "b"});
        writer.value(new Double(Double.NaN));
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteNumberWithTopLevelArrayOfStringsObject6() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{"a", "b"});
        try {
            writer.value(new Double(Double.NaN));
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testLenientNonFiniteDoubleWithTopLevelArrayOfCharsObject6() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter);
        writer.setLenient(true);
        writer.value(new Object[]{'a', 'b'});
        writer.value(Double.NaN);
        writer.close();
        assertEquals("[\"a\",\"b\"]NaN", stringWriter.toString());
    }

    @Test(timeout = 4000)
    public void testStrictNonFiniteDoubleWithTopLevelArrayOfCharsObject6() throws IOException {
        JsonWriter writer = new JsonWriter(new StringWriter());
        writer.value(new Object[]{'a', 'b'});
        try {
            writer.value(Double.NaN);
            fail("Expected IllegalStateException for multiple top-level values");
        } catch (IllegalStateException expected) {
            assertEquals("JSON must have only one top-level value.", expected.getMessage());
        }
    }

    @Test(timeout =