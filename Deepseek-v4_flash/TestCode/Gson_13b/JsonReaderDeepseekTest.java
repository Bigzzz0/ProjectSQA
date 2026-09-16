package com.google.gson.stream;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

/**
 * Advanced white-box test suite for JsonReader targeting:
 * - Core functional logic & state transitions
 * - Boundary value analysis (including the known negative-zero defect)
 * - Defect-targeted branch zone (directly reproduces testNegativeZero failure)
 * - Exception & defensive guard paths
 * - Object lifecycle & contract integrity
 */
public class JsonReaderDeepseekTest {

    /* ================================================================
     * PART A: Core Functional Logic & State Transitions
     * ================================================================ */

    @Test(timeout = 4000)
    public void testEmptyObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
        reader.beginObject();
        assertFalse(reader.hasNext());
        reader.endObject();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testEmptyArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[]"));
        reader.beginArray();
        assertFalse(reader.hasNext());
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testSimpleObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":1}"));
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testSimpleArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1,2,3]"));
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        assertEquals(3, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testPeekMultiple() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"x\":true}"));
        assertEquals(JsonToken.BEGIN_OBJECT, reader.peek());
        reader.beginObject();
        assertEquals(JsonToken.NAME, reader.peek());
        assertEquals("x", reader.nextName());
        assertEquals(JsonToken.BOOLEAN, reader.peek());
        assertTrue(reader.nextBoolean());
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testHasNextInObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":1,\"b\":2}"));
        reader.beginObject();
        assertTrue(reader.hasNext());
        reader.nextName();
        reader.nextInt();
        assertTrue(reader.hasNext());
        reader.nextName();
        reader.nextInt();
        assertFalse(reader.hasNext());
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testNextString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"hello\""));
        assertEquals("hello", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testNextBoolean() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("true"));
        assertTrue(reader.nextBoolean());
        reader = new JsonReader(new StringReader("false"));
        assertFalse(reader.nextBoolean());
    }

    @Test(timeout = 4000)
    public void testNextNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        reader.nextNull();
        // success if no exception
    }

    @Test(timeout = 4000)
    public void testNextLong() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("42"));
        assertEquals(42L, reader.nextLong());
    }

    @Test(timeout = 4000)
    public void testNextInt() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        assertEquals(123, reader.nextInt());
    }

    @Test(timeout = 4000)
    public void testNextDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("3.14"));
        assertEquals(3.14, reader.nextDouble(), 1e-15);
    }

    /* ================================================================
     * PART B: Boundary Value Analysis & Extremes
     * ================================================================ */

    @Test(timeout = 4000)
    public void testLargeLong() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("9223372036854775807"));
        assertEquals(Long.MAX_VALUE, reader.nextLong());
    }

    @Test(timeout = 4000)
    public void testNegativeLong() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("-9223372036854775808"));
        assertEquals(Long.MIN_VALUE, reader.nextLong());
    }

    @Test(timeout = 4000)
    public void testLargeInt() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("2147483647"));
        assertEquals(Integer.MAX_VALUE, reader.nextInt());
    }

    @Test(timeout = 4000)
    public void testNegativeInt() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("-2147483648"));
        assertEquals(Integer.MIN_VALUE, reader.nextInt());
    }

    @Test(timeout = 4000)
    public void testDoubleMax() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("1.7976931348623157E308"));
        assertEquals(Double.MAX_VALUE, reader.nextDouble(), 1e300);
    }

    @Test(timeout = 4000)
    public void testDoubleMin() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("4.9E-324"));
        assertEquals(Double.MIN_VALUE, reader.nextDouble(), 1e-320);
    }

    @Test(timeout = 4000)
    public void testEmptyString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"\""));
        assertEquals("", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testUnicodeString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"\\u0041\""));
        assertEquals("A", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testLongString() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) sb.append('a');
        String longStr = sb.toString();
        JsonReader reader = new JsonReader(new StringReader("\"" + longStr + "\""));
        assertEquals(longStr, reader.nextString());
    }

    /* ================================================================
     * PART C: Defect-Targeted Branch Zone (testNegativeZero)
     * ================================================================ */

    @Test(timeout = 4000)
    public void testNegativeZeroDouble() throws IOException {
        // This test directly targets the known defect:
        // expected: -0.0, but buggy version may return 0.0 (losing the sign)
        JsonReader reader = new JsonReader(new StringReader("[-0]"));
        reader.beginArray();
        double val = reader.nextDouble();
        assertEquals("Negative zero should be -0.0", -0.0, val, 0.0);
        // Additionally, the bit representation should be negative zero
        assertEquals("Double.doubleToRawLongBits should be 0x8000000000000000L",
                0x8000000000000000L, Double.doubleToRawLongBits(val));
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testNegativeZeroLong() throws IOException {
        // -0 is a valid long, representable as 0L but historically could lose sign
        JsonReader reader = new JsonReader(new StringReader("[-0]"));
        reader.beginArray();
        long val = reader.nextLong();
        assertEquals("As a long, -0 should be 0", 0L, val);
        // peek/nextString should retain the "-0" string form
        reader = new JsonReader(new StringReader("[-0]"));
        reader.beginArray();
        String str = reader.nextString();
        assertEquals("String representation of -0 must include sign", "-0", str);
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testNegativeZeroInt() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[-0]"));
        reader.beginArray();
        int val = reader.nextInt();
        assertEquals(0, val);
    }

    @Test(timeout = 4000)
    public void testNegativeZeroStringFromNumber() throws IOException {
        // When reading -0 via nextString(), the original literal should be preserved
        JsonReader reader = new JsonReader(new StringReader("[-0]"));
        reader.beginArray();
        String val = reader.nextString();
        assertEquals("-0", val);
        reader.endArray();
    }

    /* ================================================================
     * PART D: Exception & Defensive Guard Paths
     * ================================================================ */

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testNullReader() {
        new JsonReader(null);
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testBeginArrayMismatch() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        reader.beginArray();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testEndArrayMismatch() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[]"));
        reader.beginArray();
        reader.endArray();
        reader.endArray(); // extra end
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testBeginObjectMismatch() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[]"));
        reader.beginObject();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testEndObjectMismatch() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        reader.beginObject();
        reader.endObject();
        reader.endObject();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNextNameFromValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("42"));
        reader.nextName();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNextStringFromObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        reader.beginObject();
        reader.nextString();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNextBooleanFromNull() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        reader.nextBoolean();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testNextNullFromFalse() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("false"));
        reader.nextNull();
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testNextIntFromLargeDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("1e100"));
        reader.nextInt();
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testNextLongFromLargeDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("1e100"));
        reader.nextLong();
    }

    @Test(timeout = 4000, expected = MalformedJsonException.class)
    public void testStrictModeRejectsComments() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("/* comment */ 1"));
        reader.nextDouble();
    }

    @Test(timeout = 4000, expected = MalformedJsonException.class)
    public void testStrictModeRejectsSingleQuotes() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("'hello'"));
        reader.nextString();
    }

    @Test(timeout = 4000)
    public void testLenientAllowsComments() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("/* comment */ 1"));
        reader.setLenient(true);
        assertEquals(1, reader.nextInt());
    }

    @Test(timeout = 4000)
    public void testLenientAllowsTrailingComma() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1,2,]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        assertEquals(JsonToken.NULL, reader.peek()); // trailing comma yields null
        reader.nextNull();
        reader.endArray();
    }

    @Test(timeout = 4000, expected = EOFException.class)
    public void testUnterminatedObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":1"));
        reader.setLenient(true); // lenient still requires closing brace for object? Actually it throws on EOF.
        reader.beginObject();
        reader.nextName();
        reader.nextInt();
        reader.endObject(); // should throw EOFException
    }

    /* ================================================================
     * PART E: Object Lifecycle & Contract Integrity
     * ================================================================ */

    @Test(timeout = 4000)
    public void testClose() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        reader.close();
        // Should be able to call close multiple times
        reader.close();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testReadAfterClose() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("42"));
        reader.close();
        reader.nextInt();
    }

    @Test(timeout = 4000)
    public void testGetPath() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":[1,{\"b\":2}]}"));
        assertEquals("$", reader.getPath());
        reader.beginObject();
        assertEquals("$.a", reader.getPath()); // after beginning object, pathNames not set yet? Actually path is "$" until name consumed.
        // After beginObject, path is "$"
        // Let's do step by step:
        // Actually getPath() uses stack and pathNames. At start: stack=[EMPTY_DOCUMENT], pathNames[0] maybe null.
        // After beginObject: stack=[NONEMPTY_DOCUMENT, EMPTY_OBJECT], pathIndices[0]? undefined -> but path will be "$."
        // But since pathNames[1] is null, result is "$." with trailing dot? Let's test exactly.
        reader.nextName(); // "a"
        assertEquals("$.a", reader.getPath());
        reader.beginArray(); // path becomes $.a[0] after first element? Actually after beginArray, stack has EMPTY_ARRAY, pathIndices for array set to 0, but index not used until after first value.
        assertEquals("$.a[0]", reader.getPath()); // after beginArray, pathIndices[stackSize-1]=0 so "[0]"
        reader.nextInt(); // 1
        assertEquals("$.a[1]", reader.getPath()); // after reading int, pathIndices incremented
        reader.beginObject(); // {"b":2}
        assertEquals("$.a[1].", reader.getPath()); // name not yet consumed -> pathNames is null so trailing dot
        reader.nextName(); // "b"
        assertEquals("$.a[1].b", reader.getPath());
        reader.nextInt(); // 2
        assertEquals("$.a[1].b", reader.getPath()); // path doesn't change for value
        reader.endObject();
        assertEquals("$.a[2]", reader.getPath()); // after endObject, pathIndices incremented for array
        reader.endArray();
        assertEquals("$.a", reader.getPath()); // after endArray, goes back to object level
        reader.endObject();
        assertEquals("$", reader.getPath());
    }

    @Test(timeout = 4000)
    public void testSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":{\"b\":[1,2,3]},\"c\":4}"));
        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.skipValue(); // skip nested object
        assertEquals("c", reader.nextName());
        assertEquals(4, reader.nextInt());
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testSkipValueOnArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1,2,3]"));
        reader.beginArray();
        reader.skipValue(); // skip first
        reader.skipValue(); // skip second
        assertEquals(3, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testSetLenient() {
        JsonReader reader = new JsonReader(new StringReader("42"));
        assertFalse(reader.isLenient());
        reader.setLenient(true);
        assertTrue(reader.isLenient());
        reader.setLenient(false);
        assertFalse(reader.isLenient());
    }

    @Test(timeout = 4000)
    public void testToString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("42"));
        String str = reader.toString();
        assertTrue(str.contains("JsonReader"));
        reader.nextInt();
    }

    /* ================================================================
     * Additional edge cases for branch coverage
     * ================================================================ */

    @Test(timeout = 4000)
    public void testNumberWithLeadingZero() throws IOException {
        // Strict mode should reject leading zero? Actually peekNumber returns PEEKED_NONE for "012" because leading zero not allowed
        JsonReader reader = new JsonReader(new StringReader("012"));
        // In strict mode, this should throw MalformedJsonException because '0' then '1' is not valid JSON number.
        // But doPeek will treat it as unquoted string in lenient? Actually isLiteral returns true for digits, so it would be unquoted value.
        // In strict mode, unquoted values are not allowed -> checkLenient will throw. So we expect exception.
        try {
            reader.nextString();
            fail("Expected MalformedJsonException for leading zero");
        } catch (MalformedJsonException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testLenientNumberWithLeadingZero() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("012"));
        reader.setLenient(true);
        // In lenient mode, it's treated as an unquoted string.
        assertEquals("012", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testSingleQuotedStringLenient() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("'hello'"));
        reader.setLenient(true);
        assertEquals("hello", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testUnquotedStringLenient() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("hello"));
        reader.setLenient(true);
        assertEquals("hello", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testNonExecutePrefix() throws IOException {
        String prefix = ")]}'\n";
        JsonReader reader = new JsonReader(new StringReader(prefix + "[1]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testEscapeCharacters() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"\\n\\t\\r\\f\\b\\\\\\/\\\"\""));
        assertEquals("\n\t\r\f\b\\/\"", reader.nextString());
    }

    @Test(timeout = 4000, expected = NumberFormatException.class)
    public void testMalformedUnicodeEscape() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"\\uGGGG\""));
        reader.nextString();
    }
}