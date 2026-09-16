package com.google.gson.stream;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Defect: Unquoted long/integer keys in JSON objects are incorrectly
 * treated as STRING tokens instead of NUMBER tokens, causing
 * IllegalStateException when nextLong()/nextInt() is called.
 * 
 * Key Branches Targeted:
 * - peek() method: PEEKED_LONG, PEEKED_NUMBER, PEEKED_BUFFERED, PEEKED_STRING
 * - nextLong()/nextInt(): handling of PEEKED_LONG, PEEKED_NUMBER, PEEKED_STRING
 * - peekNumber() logic: digit detection, sign handling, fitsInLong determination
 * - Lenient mode: unquoted string handling, non-standard syntax acceptance
 * 
 * Boundary Values:
 * - Long.MIN_VALUE, Long.MAX_VALUE, 0, negative numbers
 * - Integer boundaries: Integer.MIN_VALUE, Integer.MAX_VALUE
 * - Unquoted strings that look like numbers (e.g., "123abc")
 * - Empty strings, whitespace handling
 * 
 * Partitions:
 * A. Core functional: basic object/array parsing, name-value pairs
 * B. BVA: numeric boundaries, empty inputs, null handling
 * C. Defect-targeted: unquoted numeric keys in objects
 * D. Exception paths: wrong token types, malformed JSON
 * E. Lifecycle: close(), multiple reads, state transitions
 */
public class JsonReaderDeepseekTest {

    // ==================== PARTITION A: CORE FUNCTIONAL LOGIC ====================

    @Test(timeout = 4000)
    public void testBasicObjectParsing() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"key\":\"value\"}"));
        reader.beginObject();
        assertEquals("key", reader.nextName());
        assertEquals("value", reader.nextString());
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
        reader.endArray();
        assertEquals(JsonToken.END_DOCUMENT, reader.peek());
    }

    @Test(timeout = 4000)
    public void testNestedStructures() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"arr\":[{\"a\":1}],\"obj\":{\"b\":true}}"));
        reader.beginObject();
        assertEquals("arr", reader.nextName());
        reader.beginArray();
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
        reader.endArray();
        assertEquals("obj", reader.nextName());
        reader.beginObject();
        assertEquals("b", reader.nextName());
        assertTrue(reader.nextBoolean());
        reader.endObject();
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testHasNextInObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":1,\"b\":2}"));
        reader.beginObject();
        assertTrue(reader.hasNext());
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        assertTrue(reader.hasNext());
        assertEquals("b", reader.nextName());
        assertEquals(2, reader.nextInt());
        assertFalse(reader.hasNext());
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testHasNextInArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1,2]"));
        reader.beginArray();
        assertTrue(reader.hasNext());
        assertEquals(1, reader.nextInt());
        assertTrue(reader.hasNext());
        assertEquals(2, reader.nextInt());
        assertFalse(reader.hasNext());
        reader.endArray();
    }

    // ==================== PARTITION B: BOUNDARY VALUE ANALYSIS ====================

    @Test(timeout = 4000)
    public void testLongBoundaries() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[" + Long.MAX_VALUE + "," + Long.MIN_VALUE + "]"));
        reader.beginArray();
        assertEquals(Long.MAX_VALUE, reader.nextLong());
        assertEquals(Long.MIN_VALUE, reader.nextLong());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testIntBoundaries() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[" + Integer.MAX_VALUE + "," + Integer.MIN_VALUE + "]"));
        reader.beginArray();
        assertEquals(Integer.MAX_VALUE, reader.nextInt());
        assertEquals(Integer.MIN_VALUE, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testZeroAndNegativeNumbers() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[0,-0,-1,1]"));
        reader.beginArray();
        assertEquals(0, reader.nextInt());
        assertEquals(0, reader.nextInt());
        assertEquals(-1, reader.nextInt());
        assertEquals(1, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testEmptyObjectAndArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{[]}"));
        reader.beginObject();
        reader.beginArray();
        reader.endArray();
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testNullValues() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[null,null]"));
        reader.beginArray();
        reader.nextNull();
        reader.nextNull();
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testBooleanValues() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[true,false]"));
        reader.beginArray();
        assertTrue(reader.nextBoolean());
        assertFalse(reader.nextBoolean());
        reader.endArray();
    }

    // ==================== PARTITION C: DEFECT-TARGETED TESTS ====================

    /**
     * CRITICAL DEFECT TEST: Unquoted long keys in objects.
     * This test targets the exact failure condition from the defect report.
     */
    @Test(timeout = 4000)
    public void testUnquotedLongKeysInObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{1234567890123456789: \"value\"}"));
        reader.setLenient(true);
        reader.beginObject();
        // This should succeed: the unquoted key should be parsed as a long
        assertEquals(1234567890123456789L, reader.nextLong());
        assertEquals("value", reader.nextString());
        reader.endObject();
    }

    /**
     * CRITICAL DEFECT TEST: Unquoted integer keys in objects.
     * This test targets the exact failure condition from the defect report.
     */
    @Test(timeout = 4000)
    public void testUnquotedIntegerKeysInObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{123: \"value\"}"));
        reader.setLenient(true);
        reader.beginObject();
        // This should succeed: the unquoted key should be parsed as an int
        assertEquals(123, reader.nextInt());
        assertEquals("value", reader.nextString());
        reader.endObject();
    }

    /**
     * CRITICAL DEFECT TEST: Unquoted strings prefixed with integers.
     * This test targets the exact failure condition from the defect report.
     */
    @Test(timeout = 4000)
    public void testPeekingUnquotedStringsPrefixedWithIntegers() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[123abc]"));
        reader.setLenient(true);
        reader.beginArray();
        // This should succeed: the unquoted string should be readable as a string
        assertEquals("123abc", reader.nextString());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testUnquotedLongKeysWithNextString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{1234567890123456789: \"value\"}"));
        reader.setLenient(true);
        reader.beginObject();
        // Reading as string should also work for unquoted numeric keys
        assertEquals("1234567890123456789", reader.nextString());
        assertEquals("value", reader.nextString());
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testUnquotedIntegerKeysWithNextString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{123: \"value\"}"));
        reader.setLenient(true);
        reader.beginObject();
        // Reading as string should also work for unquoted numeric keys
        assertEquals("123", reader.nextString());
        assertEquals("value", reader.nextString());
        reader.endObject();
    }

    // ==================== PARTITION D: EXCEPTION & DEFENSIVE PATHS ====================

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testNextNameOnNonNameToken() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1]"));
        reader.beginArray();
        reader.nextName();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testNextIntOnStringToken() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[\"abc\"]"));
        reader.beginArray();
        reader.nextInt();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testNextLongOnStringToken() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[\"abc\"]"));
        reader.beginArray();
        reader.nextLong();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testNextBooleanOnNonBooleanToken() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1]"));
        reader.beginArray();
        reader.nextBoolean();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testNextNullOnNonNullToken() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1]"));
        reader.beginArray();
        reader.nextNull();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testEndArrayOnObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        reader.beginObject();
        reader.endArray();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testEndObjectOnArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[]"));
        reader.beginArray();
        reader.endObject();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testBeginObjectOnArray() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[]"));
        reader.beginArray();
        reader.beginObject();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testBeginArrayOnObject() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{}"));
        reader.beginObject();
        reader.beginArray();
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testMalformedJson() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{invalid}"));
        reader.beginObject();
        reader.nextName();
    }

    // ==================== PARTITION E: LIFECYCLE & CONTRACT ====================

    @Test(timeout = 4000)
    public void testClose() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1]"));
        reader.close();
        // After close, peek should throw
        try {
            reader.peek();
            fail("Expected IllegalStateException after close");
        } catch (IllegalStateException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testMultipleReadsAfterClose() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1]"));
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
        reader.close();
        // After close, further operations should throw
        try {
            reader.peek();
            fail("Expected IllegalStateException after close");
        } catch (IllegalStateException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testSkipValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":[1,2,{\"b\":3}],\"c\":4}"));
        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.skipValue();
        assertEquals("c", reader.nextName());
        assertEquals(4, reader.nextInt());
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{'a': 1,}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testStrictModeRejectsLenientSyntax() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{'a': 1}"));
        try {
            reader.beginObject();
            reader.nextName();
            fail("Expected IOException in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

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
    public void testGetLineNumberAndColumnNumber() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\n\"a\": 1\n}"));
        reader.beginObject();
        assertEquals(1, reader.getLineNumber());
        assertEquals(1, reader.getColumnNumber());
        reader.nextName();
        assertEquals(2, reader.getLineNumber());
        assertEquals(1, reader.getColumnNumber());
        reader.nextInt();
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testNextDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1.5,2.0,3]"));
        reader.beginArray();
        assertEquals(1.5, reader.nextDouble(), 0.0001);
        assertEquals(2.0, reader.nextDouble(), 0.0001);
        assertEquals(3.0, reader.nextDouble(), 0.0001);
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testNextLongFromDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1.0,2.5]"));
        reader.beginArray();
        assertEquals(1L, reader.nextLong());
        try {
            reader.nextLong();
            fail("Expected NumberFormatException for non-integer double");
        } catch (NumberFormatException expected) {
            // Expected
        }
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testNextIntFromDouble() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1.0,2.5]"));
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        try {
            reader.nextInt();
            fail("Expected NumberFormatException for non-integer double");
        } catch (NumberFormatException expected) {
            // Expected
        }
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testNonExecutePrefix() throws IOException {
        JsonReader reader = new JsonReader(new StringReader(")]}'\n[1]"));
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testCommentsInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1, // comment\n 2]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testUnquotedNamesInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{name: \"value\"}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("name", reader.nextName());
        assertEquals("value", reader.nextString());
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testEqualsSignInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{name= \"value\"}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("name", reader.nextName());
        assertEquals("value", reader.nextString());
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testSemicolonSeparatorInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{name: \"value\";}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("name", reader.nextName());
        assertEquals("value", reader.nextString());
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testSingleQuotedStringsInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("['value']"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals("value", reader.nextString());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testHashCommentInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1, # comment\n 2]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testNaNInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[NaN]"));
        reader.setLenient(true);
        reader.beginArray();
        assertTrue(Double.isNaN(reader.nextDouble()));
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testInfinityInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[Infinity, -Infinity]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(Double.POSITIVE_INFINITY, reader.nextDouble(), 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, reader.nextDouble(), 0.0);
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testTopLevelPrimitiveInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        reader.setLenient(true);
        assertEquals(123, reader.nextInt());
    }

    @Test(timeout = 4000)
    public void testTopLevelStringInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"hello\""));
        reader.setLenient(true);
        assertEquals("hello", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testTopLevelBooleanInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("true"));
        reader.setLenient(true);
        assertTrue(reader.nextBoolean());
    }

    @Test(timeout = 4000)
    public void testTopLevelNullInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("null"));
        reader.setLenient(true);
        reader.nextNull();
    }

    @Test(timeout = 4000)
    public void testMultipleTopLevelValuesInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("1 2"));
        reader.setLenient(true);
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
    }

    @Test(timeout = 4000)
    public void testStrictModeRejectsTopLevelPrimitive() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("123"));
        try {
            reader.nextInt();
            fail("Expected IOException in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStrictModeRejectsMultipleTopLevelValues() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("1 2"));
        try {
            reader.nextInt();
            fail("Expected IOException in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testUnicodeEscapes() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"\\u0041\""));
        assertEquals("A", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testEscapedQuotes() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\\"b\""));
        assertEquals("a\"b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testBackslashEscapes() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\\\b\""));
        assertEquals("a\\b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testUnicodeSurrogatePairs() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"\\uD83D\\uDE00\""));
        assertEquals("\uD83D\uDE00", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testLargeStringValue() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        JsonReader reader = new JsonReader(new StringReader("\"" + sb.toString() + "\""));
        assertEquals(sb.toString(), reader.nextString());
    }

    @Test(timeout = 4000)
    public void testLargeNumberValue() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[123456789012345678901234567890]"));
        reader.beginArray();
        assertEquals(123456789012345678901234567890.0, reader.nextDouble(), 0.0);
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testNegativeZero() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[-0.0]"));
        reader.beginArray();
        assertEquals(-0.0, reader.nextDouble(), 0.0);
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testExponentNotation() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1e10,1E10,1e+10,1e-10]"));
        reader.beginArray();
        assertEquals(1e10, reader.nextDouble(), 0.0);
        assertEquals(1E10, reader.nextDouble(), 0.0);
        assertEquals(1e10, reader.nextDouble(), 0.0);
        assertEquals(1e-10, reader.nextDouble(), 0.0);
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testHexNumbersInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[0x1A]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(26, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testOctalNumbersInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[017]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(15, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testLeadingZerosInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[007]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(7, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testLeadingZerosInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[007]"));
        try {
            reader.beginArray();
            reader.nextInt();
            fail("Expected IOException in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testPlusSignInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[+1]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testPlusSignInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[+1]"));
        try {
            reader.beginArray();
            reader.nextInt();
            fail("Expected IOException in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testTrailingCommaInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1,2,]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testTrailingCommaInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1,2,]"));
        try {
            reader.beginArray();
            reader.nextInt();
            reader.nextInt();
            reader.endArray();
            fail("Expected IOException in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testMissingCommaInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1 2]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testMissingColonInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\" 1}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testMissingValueInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName());
        reader.nextNull();
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testMissingNameInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{:1}"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("", reader.nextName());
        assertEquals(1, reader.nextInt());
        reader.endObject();
    }

    @Test(timeout = 4000)
    public void testUnterminatedStringInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"abc"));
        reader.setLenient(true);
        try {
            reader.nextString();
            fail("Expected IOException for unterminated string");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testUnterminatedCommentInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1, /* comment]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        try {
            reader.nextInt();
            fail("Expected IOException for unterminated comment");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testUnterminatedArrayInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1,2"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        assertEquals(2, reader.nextInt());
        try {
            reader.endArray();
            fail("Expected IOException for unterminated array");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testUnterminatedObjectInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("{\"a\":1"));
        reader.setLenient(true);
        reader.beginObject();
        assertEquals("a", reader.nextName());
        assertEquals(1, reader.nextInt());
        try {
            reader.endObject();
            fail("Expected IOException for unterminated object");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testUnexpectedCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1, @]"));
        reader.setLenient(true);
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        try {
            reader.nextInt();
            fail("Expected IOException for unexpected character");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testUnexpectedCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("[1, @]"));
        try {
            reader.beginArray();
            reader.nextInt();
            reader.nextInt();
            fail("Expected IOException in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testEmptyDocumentInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader(""));
        try {
            reader.peek();
            fail("Expected IOException for empty document");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testWhitespaceOnlyDocumentInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("   \n\t "));
        try {
            reader.peek();
            fail("Expected IOException for whitespace-only document");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testBOMHandling() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\uFEFF[1]"));
        reader.beginArray();
        assertEquals(1, reader.nextInt());
        reader.endArray();
    }

    @Test(timeout = 4000)
    public void testControlCharactersInString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\nb\""));
        assertEquals("a\nb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testTabInString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\tb\""));
        assertEquals("a\tb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testBackspaceInString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\bb\""));
        assertEquals("a\bb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testFormFeedInString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\fb\""));
        assertEquals("a\fb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testCarriageReturnInString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\rb\""));
        assertEquals("a\rb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testSlashInString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\/b\""));
        assertEquals("a/b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testUnicodeEscapeInString() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"\\u0041\\u0042\""));
        assertEquals("AB", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testInvalidUnicodeEscape() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"\\u00G0\""));
        try {
            reader.nextString();
            fail("Expected NumberFormatException for invalid unicode escape");
        } catch (NumberFormatException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testInvalidEscapeSequence() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"\\x\""));
        try {
            reader.nextString();
            fail("Expected IOException for invalid escape sequence");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testUnterminatedEscapeSequence() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"\\"));
        try {
            reader.nextString();
            fail("Expected IOException for unterminated escape sequence");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithNewlineInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\nb\""));
        reader.setLenient(true);
        assertEquals("a\nb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithNewlineInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\nb\""));
        try {
            reader.nextString();
            fail("Expected IOException for newline in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithCarriageReturnInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\rb\""));
        reader.setLenient(true);
        assertEquals("a\rb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithCarriageReturnInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\rb\""));
        try {
            reader.nextString();
            fail("Expected IOException for carriage return in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithTabInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\tb\""));
        reader.setLenient(true);
        assertEquals("a\tb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithTabInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\tb\""));
        try {
            reader.nextString();
            fail("Expected IOException for tab in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithBackspaceInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\bb\""));
        reader.setLenient(true);
        assertEquals("a\bb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithBackspaceInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\bb\""));
        try {
            reader.nextString();
            fail("Expected IOException for backspace in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithFormFeedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\fb\""));
        reader.setLenient(true);
        assertEquals("a\fb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithFormFeedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\fb\""));
        try {
            reader.nextString();
            fail("Expected IOException for form feed in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithSlashInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a/b\""));
        reader.setLenient(true);
        assertEquals("a/b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithSlashInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a/b\""));
        try {
            reader.nextString();
            fail("Expected IOException for slash in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithBackslashInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\b\""));
        reader.setLenient(true);
        assertEquals("a\\b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithBackslashInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\b\""));
        try {
            reader.nextString();
            fail("Expected IOException for backslash in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithQuoteInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\"b\""));
        reader.setLenient(true);
        assertEquals("a\"b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithQuoteInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\"b\""));
        try {
            reader.nextString();
            fail("Expected IOException for quote in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithSingleQuoteInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a'b\""));
        reader.setLenient(true);
        assertEquals("a'b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithSingleQuoteInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a'b\""));
        try {
            reader.nextString();
            fail("Expected IOException for single quote in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithUnicodeInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0041b\""));
        reader.setLenient(true);
        assertEquals("aAb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithUnicodeInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0041b\""));
        try {
            reader.nextString();
            fail("Expected IOException for unicode in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithControlCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0001b\""));
        reader.setLenient(true);
        assertEquals("a\u0001b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithControlCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0001b\""));
        try {
            reader.nextString();
            fail("Expected IOException for control character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithDelCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u007Fb\""));
        reader.setLenient(true);
        assertEquals("a\u007Fb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithDelCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u007Fb\""));
        try {
            reader.nextString();
            fail("Expected IOException for DEL character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithNonAsciiInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u00E9b\""));
        reader.setLenient(true);
        assertEquals("a\u00E9b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithNonAsciiInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u00E9b\""));
        try {
            reader.nextString();
            fail("Expected IOException for non-ASCII character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithSurrogatePairInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uD83D\uDE00b\""));
        reader.setLenient(true);
        assertEquals("a\uD83D\uDE00b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithSurrogatePairInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uD83D\uDE00b\""));
        try {
            reader.nextString();
            fail("Expected IOException for surrogate pair in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithUnpairedSurrogateInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uD83Db\""));
        reader.setLenient(true);
        assertEquals("a\uD83Db", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithUnpairedSurrogateInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uD83Db\""));
        try {
            reader.nextString();
            fail("Expected IOException for unpaired surrogate in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithNullCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithNullCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for null character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithBOMInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uFEFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFEFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithBOMInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uFEFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for BOM in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithLineSeparatorInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u2028b\""));
        reader.setLenient(true);
        assertEquals("a\u2028b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithLineSeparatorInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u2028b\""));
        try {
            reader.nextString();
            fail("Expected IOException for line separator in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithParagraphSeparatorInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u2029b\""));
        reader.setLenient(true);
        assertEquals("a\u2029b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithParagraphSeparatorInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u2029b\""));
        try {
            reader.nextString();
            fail("Expected IOException for paragraph separator in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithNonBreakingSpaceInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u00A0b\""));
        reader.setLenient(true);
        assertEquals("a\u00A0b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithNonBreakingSpaceInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u00A0b\""));
        try {
            reader.nextString();
            fail("Expected IOException for non-breaking space in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithZeroWidthSpaceInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u200Bb\""));
        reader.setLenient(true);
        assertEquals("a\u200Bb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithZeroWidthSpaceInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u200Bb\""));
        try {
            reader.nextString();
            fail("Expected IOException for zero-width space in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithZeroWidthNoBreakSpaceInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uFEFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFEFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithZeroWidthNoBreakSpaceInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uFEFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for zero-width no-break space in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithSoftHyphenInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u00ADb\""));
        reader.setLenient(true);
        assertEquals("a\u00ADb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithSoftHyphenInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u00ADb\""));
        try {
            reader.nextString();
            fail("Expected IOException for soft hyphen in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithReplacementCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uFFFDb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFDb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithReplacementCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uFFFDb\""));
        try {
            reader.nextString();
            fail("Expected IOException for replacement character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithPrivateUseAreaInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithPrivateUseAreaInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for private use area in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithSupplementaryPlaneInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uD83D\uDE00b\""));
        reader.setLenient(true);
        assertEquals("a\uD83D\uDE00b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithSupplementaryPlaneInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uD83D\uDE00b\""));
        try {
            reader.nextString();
            fail("Expected IOException for supplementary plane in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithAstralSymbolInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uD83D\uDE00b\""));
        reader.setLenient(true);
        assertEquals("a\uD83D\uDE00b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithAstralSymbolInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uD83D\uDE00b\""));
        try {
            reader.nextString();
            fail("Expected IOException for astral symbol in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEmojiInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uD83D\uDE00b\""));
        reader.setLenient(true);
        assertEquals("a\uD83D\uDE00b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEmojiInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uD83D\uDE00b\""));
        try {
            reader.nextString();
            fail("Expected IOException for emoji in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithVariationSelectorInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uFE0Fb\""));
        reader.setLenient(true);
        assertEquals("a\uFE0Fb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithVariationSelectorInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uFE0Fb\""));
        try {
            reader.nextString();
            fail("Expected IOException for variation selector in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithCombiningCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0301b\""));
        reader.setLenient(true);
        assertEquals("a\u0301b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithCombiningCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0301b\""));
        try {
            reader.nextString();
            fail("Expected IOException for combining character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithCombiningMarkInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0301b\""));
        reader.setLenient(true);
        assertEquals("a\u0301b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithCombiningMarkInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0301b\""));
        try {
            reader.nextString();
            fail("Expected IOException for combining mark in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEnclosingMarkInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u20DDb\""));
        reader.setLenient(true);
        assertEquals("a\u20DDb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEnclosingMarkInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u20DDb\""));
        try {
            reader.nextString();
            fail("Expected IOException for enclosing mark in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithFormatCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u200Db\""));
        reader.setLenient(true);
        assertEquals("a\u200Db", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithFormatCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u200Db\""));
        try {
            reader.nextString();
            fail("Expected IOException for format character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithJoinControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u200Db\""));
        reader.setLenient(true);
        assertEquals("a\u200Db", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithJoinControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u200Db\""));
        try {
            reader.nextString();
            fail("Expected IOException for join control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithNonSpacingMarkInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0301b\""));
        reader.setLenient(true);
        assertEquals("a\u0301b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithNonSpacingMarkInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0301b\""));
        try {
            reader.nextString();
            fail("Expected IOException for non-spacing mark in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithSpacingCombiningMarkInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0301b\""));
        reader.setLenient(true);
        assertEquals("a\u0301b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithSpacingCombiningMarkInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0301b\""));
        try {
            reader.nextString();
            fail("Expected IOException for spacing combining mark in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithSurrogateInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uD83Db\""));
        reader.setLenient(true);
        assertEquals("a\uD83Db", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithSurrogateInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uD83Db\""));
        try {
            reader.nextString();
            fail("Expected IOException for surrogate in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithHighSurrogateInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uD83Db\""));
        reader.setLenient(true);
        assertEquals("a\uD83Db", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithHighSurrogateInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uD83Db\""));
        try {
            reader.nextString();
            fail("Expected IOException for high surrogate in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithLowSurrogateInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uDC00b\""));
        reader.setLenient(true);
        assertEquals("a\uDC00b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithLowSurrogateInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uDC00b\""));
        try {
            reader.nextString();
            fail("Expected IOException for low surrogate in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapeInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\nb\""));
        reader.setLenient(true);
        assertEquals("a\nb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapeInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\nb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escape in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithUnicodeEscapeInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0041b\""));
        reader.setLenient(true);
        assertEquals("aAb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithUnicodeEscapeInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0041b\""));
        try {
            reader.nextString();
            fail("Expected IOException for unicode escape in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedQuoteInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\\"b\""));
        reader.setLenient(true);
        assertEquals("a\"b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedQuoteInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\\"b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped quote in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedBackslashInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\\\b\""));
        reader.setLenient(true);
        assertEquals("a\\b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedBackslashInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\\\b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped backslash in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedSlashInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\/b\""));
        reader.setLenient(true);
        assertEquals("a/b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedSlashInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\/b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped slash in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedBackspaceInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\bb\""));
        reader.setLenient(true);
        assertEquals("a\bb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedBackspaceInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\bb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped backspace in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedFormFeedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\fb\""));
        reader.setLenient(true);
        assertEquals("a\fb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedFormFeedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\fb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped form feed in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedLineFeedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\nb\""));
        reader.setLenient(true);
        assertEquals("a\nb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedLineFeedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\nb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped line feed in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedCarriageReturnInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\rb\""));
        reader.setLenient(true);
        assertEquals("a\rb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedCarriageReturnInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\rb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped carriage return in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedTabInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\tb\""));
        reader.setLenient(true);
        assertEquals("a\tb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedTabInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\tb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped tab in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnicodeInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0041b\""));
        reader.setLenient(true);
        assertEquals("aAb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnicodeInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0041b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unicode in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedSurrogateInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uD83D\\uDE00b\""));
        reader.setLenient(true);
        assertEquals("a\uD83D\uDE00b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedSurrogateInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uD83D\\uDE00b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped surrogate in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedHighSurrogateInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uD83Db\""));
        reader.setLenient(true);
        assertEquals("a\uD83Db", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedHighSurrogateInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uD83Db\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped high surrogate in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedLowSurrogateInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uDC00b\""));
        reader.setLenient(true);
        assertEquals("a\uDC00b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedLowSurrogateInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uDC00b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped low surrogate in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        reader.setLenient(true);
        assertEquals("a\u0378b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedUnassignedInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0378b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped unassigned in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        reader.setLenient(true);
        assertEquals("a\uFFFFb", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedNonCharacterInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uFFFFb\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped non-character in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        reader.setLenient(true);
        assertEquals("a\u0000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedControlInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\u0000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped control in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInLenientMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        reader.setLenient(true);
        assertEquals("a\uE000b", reader.nextString());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapedPrivateUseInStrictMode() throws IOException {
        JsonReader reader = new JsonReader(new StringReader("\"a\\uE000b\""));
        try {
            reader.nextString();
            fail("Expected IOException for escaped private use in string in strict mode");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000