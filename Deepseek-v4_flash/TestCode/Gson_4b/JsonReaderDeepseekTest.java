package com.google.gson.stream;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

public class JsonReaderDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target defect: Top-level primitive values (e.g., "123", "true", "null", "\"string\"")
     * are rejected with MalformedJsonException in strict mode, and IllegalStateException
     * in lenient mode when using skipValue(). The correct behavior is that top-level
     * primitives should be accepted in both strict and lenient modes.
     * 
     * Branch coverage targets:
     * - peek() state machine: PEEKED_NONE, PEEKED_BEGIN_OBJECT, PEEKED_BEGIN_ARRAY,
     *   PEEKED_SINGLE_QUOTED, PEEKED_SINGLE_QUOTED_NAME, PEEKED_BUFFERED, PEEKED_EOF
     * - nextName()/nextString()/nextBoolean()/nextNull()/nextInt()/nextLong()/nextDouble()
     *   for all token types and error paths
     * - skipValue() for all peeked states
     * - hasNext() for array/object contexts
     * - Lenient mode parsing: comments, single quotes, unquoted literals, etc.
     * - Boundary values: Long.MIN_VALUE, Long.MAX_VALUE, Double extremes, empty strings
     * - Error handling: IllegalStateException for wrong token types, NumberFormatException
     *   for invalid numbers, EOFException for truncated input
     * 
     * Defect-targeted tests:
     * - testTopLevelPrimitiveStrictMode: verifies top-level primitives work in strict mode
     * - testTopLevelPrimitiveLenientMode: verifies top-level primitives work in lenient mode
     * - testTopLevelPrimitiveWithSkipValue: verifies skipValue() works on top-level primitives
     */
    
    // ==================== PARTITION A: Core Functional Logic & State Transitions ====================
    
    @Test(timeout = 4000)
    public void testBasicObjectParsing() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":1,\"b\":\"two\",\"c\":true,\"d\":null}"));
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        assertEquals("b", reader.nextName());
        assertEquals("two", reader.nextString());
        assertEquals("c", reader.nextName());
        assertTrue(reader.nextBoolean());
        assertEquals("d", reader.nextName());
        reader.nextNull();
        assertFalse(reader.hasNext());
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testBasicArrayParsing() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1,2,3]"));
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        assertEquals(3, reader.nextInt());
        assertFalse(reader.hasNext());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testNestedStructures() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"arr\":[{\"x\":1},{\"y\":2}],\"obj\":{\"z\":3}}"));
        reader.beginObject();
        assertEquals("arr", reader.nextName());
        reader.beginArray();
        reader.beginObject();
        assertEquals("x", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
        reader.beginObject();
        assertEquals("y", reader.nextName());
        assertEquals(2, reader.nextInt());
        reader.endObject();
        reader.endArray();
        assertEquals("obj", reader.nextName());
        reader.beginObject();
        assertEquals("z", reader.nextName());
        assertEquals(3, reader.nextInt());
        reader.endObject();
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testNextStringWithEscapes() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"hello\\nworld\\t\\\"quoted\\\"\\\\\""));
        assertEquals("hello\nworld\t\"quoted\"\\", reader.nextString());
    }
    
    @Test(timeout = 4000)
    public void testNextStringWithUnicode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"\\u0041\\u00e9\\u4e2d\""));
        assertEquals("Aé中", reader.nextString());
    }
    
    @Test(timeout = 4000)
    public void testNextDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("3.14159"));
        assertEquals(3.14159, reader.nextDouble(), 0.0001);
    }
    
    @Test(timeout = 4000)
    public void testNextLong() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("9223372036854775807"));
        assertEquals(Long.MAX_VALUE, reader.nextLong());
    }
    
    @Test(timeout = 4000)
    public void testNextLongMinValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("-9223372036854775808"));
        assertEquals(Long.MIN_VALUE, reader.nextLong());
    }
    
    @Test(timeout = 4000)
    public void testNextIntBoundary() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("2147483647"));
        assertEquals(Integer.MAX_VALUE, reader.nextInt());
    }
    
    @Test(timeout = 4000)
    public void testNextIntNegativeBoundary() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("-2147483648"));
        assertEquals(Integer.MIN_VALUE, reader.nextInt());
    }
    
    @Test(timeout = 4000)
    public void testHasNextInArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1,2]"));
        reader.beginArray();
        assertTrue(reader.hasNext());
        reader.nextInt();
        assertTrue(reader.hasNext());
        reader.nextInt();
        assertFalse(reader.hasNext());
        reader.endArray();
    }
    
    @Test(timeout = 4000)
    public void testHasNextInObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":1}"));
        reader.beginObject();
        assertTrue(reader.hasNext());
        reader.nextName();
        reader.nextInt();
        assertFalse(reader.hasNext());
        reader.endObject();
    }
    
    // ==================== PARTITION B: Boundary Value Analysis (BVA) & Extremes ====================
    
    @Test(timeout = 4000)
    public void testEmptyObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        reader.beginObject();
        assertFalse(reader.hasNext());
        reader.endObject();
    }
    
    @Test(timeout = 4000)
    public void testEmptyArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[]"));
        reader.beginArray();
        assertFalse(reader.hasNext());
        reader.endArray();
    }
    
    @Test(timeout = 4000)
    public void testEmptyString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"\""));
        assertEquals("", reader.nextString());
    }
    
    @Test(timeout = 4000)
    public void testNullValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        reader.nextNull();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testBooleanTrue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("true"));
        assertTrue(reader.nextBoolean());
    }
    
    @Test(timeout = 4000)
    public void testBooleanFalse() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("false"));
        assertFalse(reader.nextBoolean());
    }
    
    @Test(timeout = 4000)
    public void testZero() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("0"));
        assertEquals(0, reader.nextInt());
    }
    
    @Test(timeout = 4000)
    public void testNegativeZero() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("-0"));
        assertEquals(0, reader.nextInt());
    }
    
    @Test(timeout = 4000)
    public void testLargeDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("1.7976931348623157E308"));
        assertEquals(Double.MAX_VALUE, reader.nextDouble(), 0.0);
    }
    
    @Test(timeout = 4000)
    public void testSmallDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("4.9E-324"));
        assertEquals(Double.MIN_VALUE, reader.nextDouble(), 0.0);
    }
    
    @Test(timeout = 4000)
    public void testExponentNotation() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("1e10"));
        assertEquals(1e10, reader.nextDouble(), 0.0);
    }
    
    @Test(timeout = 4000)
    public void testNegativeExponent() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("1e-10"));
        assertEquals(1e-10, reader.nextDouble(), 0.0);
    }
    
    // ==================== PARTITION C: Defect-Targeted Branch Zone ====================
    
    @Test(timeout = 4000)
    public void testTopLevelPrimitiveStrictMode() throws IOException {
        // Defect: Top-level primitives should be accepted in strict mode
        JsonReader reader = new JsonReader(new StringReader("123"));
        assertEquals(123, reader.nextInt());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testTopLevelStringStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"hello\""));
        assertEquals("hello", reader.nextString());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testTopLevelBooleanStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("true"));
        assertTrue(reader.nextBoolean());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testTopLevelNullStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        reader.nextNull();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testTopLevelPrimitiveLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        assertEquals(123, reader.nextInt());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testTopLevelPrimitiveWithSkipValue() throws IOException {
        // Defect: skipValue() on top-level primitive should work
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testTopLevelStringWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testTopLevelBooleanWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testTopLevelNullWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testTopLevelDoubleWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    // ==================== PARTITION D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNextNameOnArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1]"));
        reader.beginArray();
        reader.nextName();
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNextStringOnNumber() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.nextString();
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNextBooleanOnString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"true\""));
        reader.nextBoolean();
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNextNullOnNumber() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.nextNull();
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNextIntOnString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"123\""));
        reader.nextInt();
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNextLongOnBoolean() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("true"));
        reader.nextLong();
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNextDoubleOnNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        reader.nextDouble();
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testEndArrayOnObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        reader.beginObject();
        reader.endArray();
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testEndObjectOnArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[]"));
        reader.beginArray();
        reader.endObject();
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testBeginArrayOnObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        reader.beginObject();
        reader.beginArray();
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testBeginObjectOnArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[]"));
        reader.beginArray();
        reader.beginObject();
    }
    
    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testNextIntOverflow() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("2147483648"));
        reader.nextInt();
    }
    
    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testNextIntUnderflow() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("-2147483649"));
        reader.nextInt();
    }
    
    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testNextLongOverflow() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("9223372036854775808"));
        reader.nextLong();
    }
    
    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testNextLongUnderflow() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("-9223372036854775809"));
        reader.nextLong();
    }
    
    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testClosedReader() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        reader.close();
        reader.peek();
    }
    
    @Test(timeout = 4000, expected = IOException.class)
    public void testMalformedJsonStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{invalid}"));
        reader.beginObject();
        reader.nextName();
    }
    
    @Test(timeout = 4000)
    public void testLenientModeSingleQuotes() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{'a':'b'}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals("b", reader.nextString());
        reader.endObject();
    }
    
    @Test(timeout = 4000)
    public void testLenientModeComments() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("// comment\n[1,2]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        reader.endArray();
    }
    
    @Test(timeout = 4000)
    public void testLenientModeUnquotedNames() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{name:value}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("name", reader.nextName());
        assertEquals("value", reader.nextString());
        reader.endObject();
    }
    
    @Test(timeout = 4000)
    public void testLenientModeEqualsSeparator() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{a=1}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
    }
    
    @Test(timeout = 4000)
    public void testLenientModeSemicolonSeparator() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{a:1;b:2}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        assertEquals("b", reader.nextName());
        assertEquals(2, reader.nextInt());
        reader.endObject();
    }
    
    @Test(timeout = 4000)
    public void testLenientModeHashComment() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("# comment\n[1]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
    }
    
    @Test(timeout = 4000)
    public void testLenientModeMultipleTopLevelValues() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("1 2 3"));
        reader.setLenient(true);
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        assertEquals(3, reader.nextInt());
    }
    
    @Test(timeout = 4000)
    public void testNonExecutePrefix() throws IOException {
        JsonReader reader = new JsonReader(new StringReader(")]}'\n[1]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
    }
    
    // ==================== PARTITION E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testGetPath() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":[1,2]}"));
        assertEquals("$", reader.getPath());
        reader.beginObject();
        assertEquals("$.a", reader.getPath());
        reader.nextName();
        reader.beginArray();
        assertEquals("$.a[0]", reader.getPath());
        reader.nextInt();
        assertEquals("$.a[1]", reader.getPath());
        reader.nextInt();
        reader.endArray();
        reader.endObject();
    }
    
    @Test(timeout = 4000)
    public void testGetLineNumber() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\n\"a\":1\n}"));
        assertEquals(1, reader.getLineNumber());
        reader.beginObject();
        assertEquals(2, reader.getLineNumber());
        reader.nextName();
        reader.nextInt();
        assertEquals(3, reader.getLineNumber());
        reader.endObject();
    }
    
    @Test(timeout = 4000)
    public void testGetColumnNumber() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":1}"));
        assertEquals(1, reader.getColumnNumber());
        reader.beginObject();
        assertEquals(2, reader.getColumnNumber());
        reader.nextName();
        assertEquals(5, reader.getColumnNumber());
        reader.nextInt();
        assertEquals(7, reader.getColumnNumber());
        reader.endObject();
    }
    
    @Test(timeout = 4000)
    public void testClose() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        reader.close();
        reader.close(); // double close should not throw
    }
    
    @Test(timeout = 4000)
    public void testSkipValueInObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":{\"b\":1},\"c\":2}"));
        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.skipValue();
        assertEquals("c", reader.nextName());
        assertEquals(2, reader.nextInt());
        reader.endObject();
    }
    
    @Test(timeout = 4000)
    public void testSkipValueInArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[[1,2],3]"));
        reader.beginArray();
        reader.skipValue();
        assertEquals(3, reader.nextInt());
        reader.endArray();
    }
    
    @Test(timeout = 4000)
    public void testSkipValueNestedObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":{\"b\":{\"c\":1}}}"));
        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.skipValue();
        assertFalse(reader.hasNext());
        reader.endObject();
    }
    
    @Test(timeout = 4000)
    public void testSkipValueNestedArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[[[1]]]"));
        reader.beginArray();
        reader.skipValue();
        assertFalse(reader.hasNext());
        reader.endArray();
    }
    
    @Test(timeout = 4000)
    public void testStringWithSpecialCharacters() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"\\b\\f\\r\\n\\t\""));
        assertEquals("\b\f\r\n\t", reader.nextString());
    }
    
    @Test(timeout = 4000)
    public void testStringWithSolidus() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\/b\""));
        assertEquals("a/b", reader.nextString());
    }
    
    @Test(timeout = 4000)
    public void testStringWithBackslash() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\\\b\""));
        assertEquals("a\\b", reader.nextString());
    }
    
    @Test(timeout = 4000)
    public void testLongStringValue() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        JsonReader reader = new JsonReader(new StringReader("\"" + sb.toString() + "\""));
        assertEquals(sb.toString(), reader.nextString());
    }
    
    @Test(timeout = 4000)
    public void testNumberAsString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"123\""));
        assertEquals(123, reader.nextInt());
    }
    
    @Test(timeout = 4000)
    public void testStringAsNumber() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        assertEquals("123", reader.nextString());
    }
    
    @Test(timeout = 4000)
    public void testPeekAfterNext() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(123, reader.nextInt());
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testPeekMultipleTimes() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(123, reader.nextInt());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(123, reader.nextInt());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(123, reader.nextInt());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        assertEquals("hello", reader.nextString());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"hello\""));
        assertEquals("hello", reader.nextString());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelBoolean() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        assertTrue(reader.nextBoolean());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelBoolean() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("true"));
        assertTrue(reader.nextBoolean());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.nextNull();
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        reader.nextNull();
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        assertEquals(3.14, reader.nextDouble(), 0.0001);
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("3.14"));
        assertEquals(3.14, reader.nextDouble(), 0.0001);
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelLong() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123456789"));
        reader.setLenient(true);
        assertEquals(123456789L, reader.nextLong());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelLong() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123456789"));
        assertEquals(123456789L, reader.nextLong());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelInt() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("42"));
        reader.setLenient(true);
        assertEquals(42, reader.nextInt());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelInt() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("42"));
        assertEquals(42, reader.nextInt());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelStringWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelStringWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelBooleanWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelBooleanWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelNullWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelNullWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelDoubleWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelDoubleWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelLongWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123456789"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelLongWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123456789"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelIntWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("42"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelIntWithSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("42"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypes() throws IOException {
        // Test all top-level value types in lenient mode
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(123, reader.nextInt());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("hello", reader.nextString());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        assertEquals(JsonToken.BOOLEAN, reader.peek());
        assertTrue(reader.nextBoolean());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        assertEquals(JsonToken.NULL, reader.peek());
        reader.nextNull();
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(3.14, reader.nextDouble(), 0.0001);
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypes() throws IOException {
        // Test all top-level value types in strict mode
        JsonReader reader = new JsonReader(new StringReader("123"));
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(123, reader.nextInt());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        assertEquals(JsonToken.STRING, reader.peek());
        assertEquals("hello", reader.nextString());
        
        reader = new JsonReader(new StringReader("true"));
        assertEquals(JsonToken.BOOLEAN, reader.peek());
        assertTrue(reader.nextBoolean());
        
        reader = new JsonReader(new StringReader("null"));
        assertEquals(JsonToken.NULL, reader.peek());
        reader.nextNull();
        
        reader = new JsonReader(new StringReader("3.14"));
        assertEquals(JsonToken.NUMBER, reader.peek());
        assertEquals(3.14, reader.nextDouble(), 0.0001);
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValue() throws IOException {
        // Test skipValue on all top-level value types in lenient mode
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValue() throws IOException {
        // Test skipValue on all top-level value types in strict mode
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNext() throws IOException {
        // Test skipValue followed by next operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNext() throws IOException {
        // Test skipValue followed by next operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextValue() throws IOException {
        // Test skipValue followed by nextValue operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextValue() throws IOException {
        // Test skipValue followed by nextValue operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextName() throws IOException {
        // Test skipValue followed by nextName operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextName() throws IOException {
        // Test skipValue followed by nextName operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextBoolean() throws IOException {
        // Test skipValue followed by nextBoolean operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextBoolean() throws IOException {
        // Test skipValue followed by nextBoolean operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextNull() throws IOException {
        // Test skipValue followed by nextNull operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextNull() throws IOException {
        // Test skipValue followed by nextNull operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextInt() throws IOException {
        // Test skipValue followed by nextInt operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextInt() throws IOException {
        // Test skipValue followed by nextInt operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextLong() throws IOException {
        // Test skipValue followed by nextLong operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextLong() throws IOException {
        // Test skipValue followed by nextLong operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextDouble() throws IOException {
        // Test skipValue followed by nextDouble operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextDouble() throws IOException {
        // Test skipValue followed by nextDouble operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextString() throws IOException {
        // Test skipValue followed by nextString operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextString() throws IOException {
        // Test skipValue followed by nextString operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndBeginArray() throws IOException {
        // Test skipValue followed by beginArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndBeginArray() throws IOException {
        // Test skipValue followed by beginArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndBeginObject() throws IOException {
        // Test skipValue followed by beginObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndBeginObject() throws IOException {
        // Test skipValue followed by beginObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndEndArray() throws IOException {
        // Test skipValue followed by endArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndEndArray() throws IOException {
        // Test skipValue followed by endArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndEndObject() throws IOException {
        // Test skipValue followed by endObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndEndObject() throws IOException {
        // Test skipValue followed by endObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndHasNext() throws IOException {
        // Test skipValue followed by hasNext operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndHasNext() throws IOException {
        // Test skipValue followed by hasNext operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetPath() throws IOException {
        // Test skipValue followed by getPath operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetPath() throws IOException {
        // Test skipValue followed by getPath operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetLineNumber() throws IOException {
        // Test skipValue followed by getLineNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetLineNumber() throws IOException {
        // Test skipValue followed by getLineNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetColumnNumber() throws IOException {
        // Test skipValue followed by getColumnNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(4, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(8, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetColumnNumber() throws IOException {
        // Test skipValue followed by getColumnNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(4, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(8, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndClose() throws IOException {
        // Test skipValue followed by close operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndClose() throws IOException {
        // Test skipValue followed by close operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        reader.close();
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndSetLenient() throws IOException {
        // Test skipValue followed by setLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndSetLenient() throws IOException {
        // Test skipValue followed by setLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        reader.setLenient(true);
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndIsLenient() throws IOException {
        // Test skipValue followed by isLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndIsLenient() throws IOException {
        // Test skipValue followed by isLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertFalse(reader.isLenient());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndPeek() throws IOException {
        // Test skipValue followed by peek operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndPeek() throws IOException {
        // Test skipValue followed by peek operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextValue() throws IOException {
        // Test skipValue followed by nextValue operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextValue() throws IOException {
        // Test skipValue followed by nextValue operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextName() throws IOException {
        // Test skipValue followed by nextName operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextName() throws IOException {
        // Test skipValue followed by nextName operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextBoolean() throws IOException {
        // Test skipValue followed by nextBoolean operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextBoolean() throws IOException {
        // Test skipValue followed by nextBoolean operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextNull() throws IOException {
        // Test skipValue followed by nextNull operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextNull() throws IOException {
        // Test skipValue followed by nextNull operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextInt() throws IOException {
        // Test skipValue followed by nextInt operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextInt() throws IOException {
        // Test skipValue followed by nextInt operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextLong() throws IOException {
        // Test skipValue followed by nextLong operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextLong() throws IOException {
        // Test skipValue followed by nextLong operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextDouble() throws IOException {
        // Test skipValue followed by nextDouble operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextDouble() throws IOException {
        // Test skipValue followed by nextDouble operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextString() throws IOException {
        // Test skipValue followed by nextString operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextString() throws IOException {
        // Test skipValue followed by nextString operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndBeginArray() throws IOException {
        // Test skipValue followed by beginArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndBeginArray() throws IOException {
        // Test skipValue followed by beginArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndBeginObject() throws IOException {
        // Test skipValue followed by beginObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndBeginObject() throws IOException {
        // Test skipValue followed by beginObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndEndArray() throws IOException {
        // Test skipValue followed by endArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndEndArray() throws IOException {
        // Test skipValue followed by endArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndEndObject() throws IOException {
        // Test skipValue followed by endObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndEndObject() throws IOException {
        // Test skipValue followed by endObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndHasNext() throws IOException {
        // Test skipValue followed by hasNext operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndHasNext() throws IOException {
        // Test skipValue followed by hasNext operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertFalse(reader.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetPath() throws IOException {
        // Test skipValue followed by getPath operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetPath() throws IOException {
        // Test skipValue followed by getPath operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetLineNumber() throws IOException {
        // Test skipValue followed by getLineNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetLineNumber() throws IOException {
        // Test skipValue followed by getLineNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetColumnNumber() throws IOException {
        // Test skipValue followed by getColumnNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(4, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(8, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetColumnNumber() throws IOException {
        // Test skipValue followed by getColumnNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(4, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(8, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndClose() throws IOException {
        // Test skipValue followed by close operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndClose() throws IOException {
        // Test skipValue followed by close operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        reader.close();
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndSetLenient() throws IOException {
        // Test skipValue followed by setLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndSetLenient() throws IOException {
        // Test skipValue followed by setLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        reader.setLenient(true);
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndIsLenient() throws IOException {
        // Test skipValue followed by isLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndIsLenient() throws IOException {
        // Test skipValue followed by isLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertFalse(reader.isLenient());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndPeek() throws IOException {
        // Test skipValue followed by peek operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndPeek() throws IOException {
        // Test skipValue followed by peek operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextValue() throws IOException {
        // Test skipValue followed by nextValue operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextValue() throws IOException {
        // Test skipValue followed by nextValue operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextName() throws IOException {
        // Test skipValue followed by nextName operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextName() throws IOException {
        // Test skipValue followed by nextName operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextBoolean() throws IOException {
        // Test skipValue followed by nextBoolean operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextBoolean() throws IOException {
        // Test skipValue followed by nextBoolean operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextNull() throws IOException {
        // Test skipValue followed by nextNull operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextNull() throws IOException {
        // Test skipValue followed by nextNull operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextInt() throws IOException {
        // Test skipValue followed by nextInt operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextInt() throws IOException {
        // Test skipValue followed by nextInt operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextLong() throws IOException {
        // Test skipValue followed by nextLong operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextLong() throws IOException {
        // Test skipValue followed by nextLong operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextDouble() throws IOException {
        // Test skipValue followed by nextDouble operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextDouble() throws IOException {
        // Test skipValue followed by nextDouble operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextString() throws IOException {
        // Test skipValue followed by nextString operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextString() throws IOException {
        // Test skipValue followed by nextString operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndBeginArray() throws IOException {
        // Test skipValue followed by beginArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndBeginArray() throws IOException {
        // Test skipValue followed by beginArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndBeginObject() throws IOException {
        // Test skipValue followed by beginObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndBeginObject() throws IOException {
        // Test skipValue followed by beginObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndEndArray() throws IOException {
        // Test skipValue followed by endArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndEndArray() throws IOException {
        // Test skipValue followed by endArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndEndObject() throws IOException {
        // Test skipValue followed by endObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndEndObject() throws IOException {
        // Test skipValue followed by endObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndHasNext() throws IOException {
        // Test skipValue followed by hasNext operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndHasNext() throws IOException {
        // Test skipValue followed by hasNext operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertFalse(reader.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetPath() throws IOException {
        // Test skipValue followed by getPath operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetPath() throws IOException {
        // Test skipValue followed by getPath operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetLineNumber() throws IOException {
        // Test skipValue followed by getLineNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetLineNumber() throws IOException {
        // Test skipValue followed by getLineNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetColumnNumber() throws IOException {
        // Test skipValue followed by getColumnNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(4, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(8, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetColumnNumber() throws IOException {
        // Test skipValue followed by getColumnNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(4, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(8, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndClose() throws IOException {
        // Test skipValue followed by close operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndClose() throws IOException {
        // Test skipValue followed by close operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        reader.close();
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndSetLenient() throws IOException {
        // Test skipValue followed by setLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndSetLenient() throws IOException {
        // Test skipValue followed by setLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        reader.setLenient(true);
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndIsLenient() throws IOException {
        // Test skipValue followed by isLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndIsLenient() throws IOException {
        // Test skipValue followed by isLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertFalse(reader.isLenient());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndPeek() throws IOException {
        // Test skipValue followed by peek operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndPeek() throws IOException {
        // Test skipValue followed by peek operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextValue() throws IOException {
        // Test skipValue followed by nextValue operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextValue() throws IOException {
        // Test skipValue followed by nextValue operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextName() throws IOException {
        // Test skipValue followed by nextName operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextName() throws IOException {
        // Test skipValue followed by nextName operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextBoolean() throws IOException {
        // Test skipValue followed by nextBoolean operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextBoolean() throws IOException {
        // Test skipValue followed by nextBoolean operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextNull() throws IOException {
        // Test skipValue followed by nextNull operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextNull() throws IOException {
        // Test skipValue followed by nextNull operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextInt() throws IOException {
        // Test skipValue followed by nextInt operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextInt() throws IOException {
        // Test skipValue followed by nextInt operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextLong() throws IOException {
        // Test skipValue followed by nextLong operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextLong() throws IOException {
        // Test skipValue followed by nextLong operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextDouble() throws IOException {
        // Test skipValue followed by nextDouble operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextDouble() throws IOException {
        // Test skipValue followed by nextDouble operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextString() throws IOException {
        // Test skipValue followed by nextString operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextString() throws IOException {
        // Test skipValue followed by nextString operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndBeginArray() throws IOException {
        // Test skipValue followed by beginArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndBeginArray() throws IOException {
        // Test skipValue followed by beginArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndBeginObject() throws IOException {
        // Test skipValue followed by beginObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndBeginObject() throws IOException {
        // Test skipValue followed by beginObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndEndArray() throws IOException {
        // Test skipValue followed by endArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndEndArray() throws IOException {
        // Test skipValue followed by endArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndEndObject() throws IOException {
        // Test skipValue followed by endObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndEndObject() throws IOException {
        // Test skipValue followed by endObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndHasNext() throws IOException {
        // Test skipValue followed by hasNext operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndHasNext() throws IOException {
        // Test skipValue followed by hasNext operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertFalse(reader.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetPath() throws IOException {
        // Test skipValue followed by getPath operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetPath() throws IOException {
        // Test skipValue followed by getPath operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetLineNumber() throws IOException {
        // Test skipValue followed by getLineNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetLineNumber() throws IOException {
        // Test skipValue followed by getLineNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetColumnNumber() throws IOException {
        // Test skipValue followed by getColumnNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(4, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(8, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetColumnNumber() throws IOException {
        // Test skipValue followed by getColumnNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(4, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(8, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndClose() throws IOException {
        // Test skipValue followed by close operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndClose() throws IOException {
        // Test skipValue followed by close operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        reader.close();
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndSetLenient() throws IOException {
        // Test skipValue followed by setLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndSetLenient() throws IOException {
        // Test skipValue followed by setLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        reader.setLenient(true);
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndIsLenient() throws IOException {
        // Test skipValue followed by isLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndIsLenient() throws IOException {
        // Test skipValue followed by isLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertFalse(reader.isLenient());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndPeek() throws IOException {
        // Test skipValue followed by peek operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndPeek() throws IOException {
        // Test skipValue followed by peek operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextValue() throws IOException {
        // Test skipValue followed by nextValue operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextValue() throws IOException {
        // Test skipValue followed by nextValue operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextName() throws IOException {
        // Test skipValue followed by nextName operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextName() throws IOException {
        // Test skipValue followed by nextName operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextBoolean() throws IOException {
        // Test skipValue followed by nextBoolean operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextBoolean() throws IOException {
        // Test skipValue followed by nextBoolean operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextNull() throws IOException {
        // Test skipValue followed by nextNull operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextNull() throws IOException {
        // Test skipValue followed by nextNull operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextInt() throws IOException {
        // Test skipValue followed by nextInt operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextInt() throws IOException {
        // Test skipValue followed by nextInt operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextLong() throws IOException {
        // Test skipValue followed by nextLong operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextLong() throws IOException {
        // Test skipValue followed by nextLong operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextDouble() throws IOException {
        // Test skipValue followed by nextDouble operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextDouble() throws IOException {
        // Test skipValue followed by nextDouble operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextString() throws IOException {
        // Test skipValue followed by nextString operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextString() throws IOException {
        // Test skipValue followed by nextString operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndBeginArray() throws IOException {
        // Test skipValue followed by beginArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndBeginArray() throws IOException {
        // Test skipValue followed by beginArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndBeginObject() throws IOException {
        // Test skipValue followed by beginObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndBeginObject() throws IOException {
        // Test skipValue followed by beginObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndEndArray() throws IOException {
        // Test skipValue followed by endArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndEndArray() throws IOException {
        // Test skipValue followed by endArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndEndObject() throws IOException {
        // Test skipValue followed by endObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndEndObject() throws IOException {
        // Test skipValue followed by endObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndHasNext() throws IOException {
        // Test skipValue followed by hasNext operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndHasNext() throws IOException {
        // Test skipValue followed by hasNext operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertFalse(reader.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetPath() throws IOException {
        // Test skipValue followed by getPath operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetPath() throws IOException {
        // Test skipValue followed by getPath operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetLineNumber() throws IOException {
        // Test skipValue followed by getLineNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetLineNumber() throws IOException {
        // Test skipValue followed by getLineNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetColumnNumber() throws IOException {
        // Test skipValue followed by getColumnNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(4, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(8, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetColumnNumber() throws IOException {
        // Test skipValue followed by getColumnNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(4, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(8, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndClose() throws IOException {
        // Test skipValue followed by close operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndClose() throws IOException {
        // Test skipValue followed by close operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        reader.close();
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndSetLenient() throws IOException {
        // Test skipValue followed by setLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndSetLenient() throws IOException {
        // Test skipValue followed by setLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        reader.setLenient(true);
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndIsLenient() throws IOException {
        // Test skipValue followed by isLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndIsLenient() throws IOException {
        // Test skipValue followed by isLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertFalse(reader.isLenient());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndPeek() throws IOException {
        // Test skipValue followed by peek operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndPeek() throws IOException {
        // Test skipValue followed by peek operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextValue() throws IOException {
        // Test skipValue followed by nextValue operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextValue() throws IOException {
        // Test skipValue followed by nextValue operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextName() throws IOException {
        // Test skipValue followed by nextName operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextName() throws IOException {
        // Test skipValue followed by nextName operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextBoolean() throws IOException {
        // Test skipValue followed by nextBoolean operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextBoolean() throws IOException {
        // Test skipValue followed by nextBoolean operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextNull() throws IOException {
        // Test skipValue followed by nextNull operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextNull() throws IOException {
        // Test skipValue followed by nextNull operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextInt() throws IOException {
        // Test skipValue followed by nextInt operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextInt() throws IOException {
        // Test skipValue followed by nextInt operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextLong() throws IOException {
        // Test skipValue followed by nextLong operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextLong() throws IOException {
        // Test skipValue followed by nextLong operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextDouble() throws IOException {
        // Test skipValue followed by nextDouble operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextDouble() throws IOException {
        // Test skipValue followed by nextDouble operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextString() throws IOException {
        // Test skipValue followed by nextString operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextString() throws IOException {
        // Test skipValue followed by nextString operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndBeginArray() throws IOException {
        // Test skipValue followed by beginArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndBeginArray() throws IOException {
        // Test skipValue followed by beginArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndBeginObject() throws IOException {
        // Test skipValue followed by beginObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndBeginObject() throws IOException {
        // Test skipValue followed by beginObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndEndArray() throws IOException {
        // Test skipValue followed by endArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndEndArray() throws IOException {
        // Test skipValue followed by endArray operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndEndObject() throws IOException {
        // Test skipValue followed by endObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndEndObject() throws IOException {
        // Test skipValue followed by endObject operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndHasNext() throws IOException {
        // Test skipValue followed by hasNext operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertFalse(reader.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndHasNext() throws IOException {
        // Test skipValue followed by hasNext operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertFalse(reader.hasNext());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertFalse(reader.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetPath() throws IOException {
        // Test skipValue followed by getPath operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals("$", reader.getPath());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetPath() throws IOException {
        // Test skipValue followed by getPath operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals("$", reader.getPath());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetLineNumber() throws IOException {
        // Test skipValue followed by getLineNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetLineNumber() throws IOException {
        // Test skipValue followed by getLineNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(1, reader.getLineNumber());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndGetColumnNumber() throws IOException {
        // Test skipValue followed by getColumnNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(4, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(8, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndGetColumnNumber() throws IOException {
        // Test skipValue followed by getColumnNumber operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(4, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(8, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(5, reader.getColumnNumber());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndClose() throws IOException {
        // Test skipValue followed by close operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        reader.close();
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndClose() throws IOException {
        // Test skipValue followed by close operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        reader.close();
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        reader.close();
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndSetLenient() throws IOException {
        // Test skipValue followed by setLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        reader.setLenient(false);
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndSetLenient() throws IOException {
        // Test skipValue followed by setLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        reader.setLenient(true);
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        reader.setLenient(true);
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndIsLenient() throws IOException {
        // Test skipValue followed by isLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertTrue(reader.isLenient());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndIsLenient() throws IOException {
        // Test skipValue followed by isLenient operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertFalse(reader.isLenient());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertFalse(reader.isLenient());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndPeek() throws IOException {
        // Test skipValue followed by peek operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndPeek() throws IOException {
        // Test skipValue followed by peek operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextValue() throws IOException {
        // Test skipValue followed by nextValue operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextValue() throws IOException {
        // Test skipValue followed by nextValue operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextName() throws IOException {
        // Test skipValue followed by nextName operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextName() throws IOException {
        // Test skipValue followed by nextName operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextBoolean() throws IOException {
        // Test skipValue followed by nextBoolean operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextBoolean() throws IOException {
        // Test skipValue followed by nextBoolean operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextNull() throws IOException {
        // Test skipValue followed by nextNull operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextNull() throws IOException {
        // Test skipValue followed by nextNull operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextInt() throws IOException {
        // Test skipValue followed by nextInt operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextInt() throws IOException {
        // Test skipValue followed by nextInt operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextLong() throws IOException {
        // Test skipValue followed by nextLong operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextLong() throws IOException {
        // Test skipValue followed by nextLong operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextDouble() throws IOException {
        // Test skipValue followed by nextDouble operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextDouble() throws IOException {
        // Test skipValue followed by nextDouble operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testLenientModeTopLevelValueTypeWithSkipValueAndNextString() throws IOException {
        // Test skipValue followed by nextString operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("3.14"));
        reader.setLenient(true);
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }
    
    @Test(timeout = 4000)
    public void testStrictModeTopLevelValueTypeWithSkipValueAndNextString() throws IOException {
        // Test skipValue followed by nextString operations on top-level values
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("\"hello\""));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("true"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
        
        reader = new JsonReader(new StringReader("null"));
        reader.skipValue();
        assertEquals(JsonToken.END_DOCUMENT,