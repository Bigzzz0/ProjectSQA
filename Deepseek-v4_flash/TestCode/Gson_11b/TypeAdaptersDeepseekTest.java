package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LazilyParsedNumber;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TypeAdapters.java (all static adapters and factories)
 * 
 * Key branches covered:
 * - Null handling in every adapter (read/write)
 * - Token type switching (NUMBER, BOOLEAN, STRING, NULL, BEGIN_ARRAY, BEGIN_OBJECT)
 * - Exception paths: UnsupportedOperationException, JsonSyntaxException, JsonIOException, IllegalArgumentException
 * - Boundary values: empty arrays, single element arrays, max/min integers, invalid strings
 * - Enum with SerializedName and alternates
 * - Factory creation logic (raw type matching, hierarchy)
 * 
 * Defect-targeted branch (Defects4J #testNumberAsStringDeserialization):
 * - NUMBER adapter: when JSON token is STRING, it should parse as number but currently throws JsonSyntaxException.
 *   Test expects successful parsing of string "123" into a LazilyParsedNumber.
 */
public class TypeAdaptersDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testClassAdapterNull() throws IOException {
        // write null
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.CLASS.write(jw, null);
        jw.close();
        assertEquals("null", sw.toString());

        // read null
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.CLASS.read(jr));
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testClassAdapterWriteNonNull() throws IOException {
        JsonWriter jw = new JsonWriter(new StringWriter());
        TypeAdapters.CLASS.write(jw, String.class);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testClassAdapterReadNonNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"java.lang.String\""));
        TypeAdapters.CLASS.read(jr);
    }

    @Test(timeout = 4000)
    public void testBitSetReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BIT_SET.read(jr));
    }

    @Test(timeout = 4000)
    public void testBitSetReadNumbers() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("[1,0,1]"));
        BitSet bs = TypeAdapters.BIT_SET.read(jr);
        assertTrue(bs.get(0));
        assertFalse(bs.get(1));
        assertTrue(bs.get(2));
    }

    @Test(timeout = 4000)
    public void testBitSetReadBooleans() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("[true,false,true]"));
        BitSet bs = TypeAdapters.BIT_SET.read(jr);
        assertTrue(bs.get(0));
        assertFalse(bs.get(1));
        assertTrue(bs.get(2));
    }

    @Test(timeout = 4000)
    public void testBitSetReadStrings() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("[\"1\",\"0\",\"1\"]"));
        BitSet bs = TypeAdapters.BIT_SET.read(jr);
        assertTrue(bs.get(0));
        assertFalse(bs.get(1));
        assertTrue(bs.get(2));
    }

    @Test(timeout = 4000, expected = JsonSyntaxException.class)
    public void testBitSetReadInvalidString() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("[\"abc\"]"));
        TypeAdapters.BIT_SET.read(jr);
    }

    @Test(timeout = 4000, expected = JsonSyntaxException.class)
    public void testBitSetReadInvalidToken() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("[{}]"));
        TypeAdapters.BIT_SET.read(jr);
    }

    @Test(timeout = 4000)
    public void testBitSetWriteNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BIT_SET.write(jw, null);
        jw.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testBitSetWrite() throws IOException {
        BitSet bs = new BitSet();
        bs.set(0);
        bs.set(2);
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BIT_SET.write(jw, bs);
        jw.close();
        assertEquals("[1,0,1]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testBooleanReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BOOLEAN.read(jr));
    }

    @Test(timeout = 4000)
    public void testBooleanReadString() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"true\""));
        assertTrue(TypeAdapters.BOOLEAN.read(jr));
    }

    @Test(timeout = 4000)
    public void testBooleanReadBoolean() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("false"));
        assertFalse(TypeAdapters.BOOLEAN.read(jr));
    }

    @Test(timeout = 4000)
    public void testBooleanWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BOOLEAN.write(jw, true);
        jw.close();
        assertEquals("true", sw.toString());
    }

    @Test(timeout = 4000)
    public void testBooleanAsStringReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BOOLEAN_AS_STRING.read(jr));
    }

    @Test(timeout = 4000)
    public void testBooleanAsStringReadString() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"true\""));
        assertTrue(TypeAdapters.BOOLEAN_AS_STRING.read(jr));
    }

    @Test(timeout = 4000)
    public void testBooleanAsStringWriteNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BOOLEAN_AS_STRING.write(jw, null);
        jw.close();
        assertEquals("\"null\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testBooleanAsStringWriteNonNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BOOLEAN_AS_STRING.write(jw, false);
        jw.close();
        assertEquals("\"false\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testByteReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BYTE.read(jr));
    }

    @Test(timeout = 4000)
    public void testByteReadValid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("42"));
        assertEquals((byte) 42, TypeAdapters.BYTE.read(jr));
    }

    @Test(timeout = 4000, expected = JsonSyntaxException.class)
    public void testByteReadInvalid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("9999999999"));
        TypeAdapters.BYTE.read(jr);
    }

    @Test(timeout = 4000)
    public void testByteWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BYTE.write(jw, (byte) 7);
        jw.close();
        assertEquals("7", sw.toString());
    }

    @Test(timeout = 4000)
    public void testShortReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.SHORT.read(jr));
    }

    @Test(timeout = 4000)
    public void testShortReadValid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("1000"));
        assertEquals((short) 1000, TypeAdapters.SHORT.read(jr));
    }

    @Test(timeout = 4000, expected = JsonSyntaxException.class)
    public void testShortReadInvalid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("9999999999"));
        TypeAdapters.SHORT.read(jr);
    }

    @Test(timeout = 4000)
    public void testShortWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.SHORT.write(jw, (short) -1);
        jw.close();
        assertEquals("-1", sw.toString());
    }

    @Test(timeout = 4000)
    public void testIntegerReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.INTEGER.read(jr));
    }

    @Test(timeout = 4000)
    public void testIntegerReadValid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("123456"));
        assertEquals(123456, TypeAdapters.INTEGER.read(jr));
    }

    @Test(timeout = 4000, expected = JsonSyntaxException.class)
    public void testIntegerReadInvalid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("notanumber"));
        TypeAdapters.INTEGER.read(jr);
    }

    @Test(timeout = 4000)
    public void testIntegerWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.INTEGER.write(jw, 0);
        jw.close();
        assertEquals("0", sw.toString());
    }

    @Test(timeout = 4000)
    public void testAtomicIntegerRead() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("42"));
        assertEquals(42, TypeAdapters.ATOMIC_INTEGER.read(jr).get());
    }

    @Test(timeout = 4000, expected = JsonSyntaxException.class)
    public void testAtomicIntegerReadInvalid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("abc"));
        TypeAdapters.ATOMIC_INTEGER.read(jr);
    }

    @Test(timeout = 4000)
    public void testAtomicIntegerWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.ATOMIC_INTEGER.write(jw, new AtomicInteger(99));
        jw.close();
        assertEquals("99", sw.toString());
    }

    @Test(timeout = 4000)
    public void testAtomicBooleanRead() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("true"));
        assertTrue(TypeAdapters.ATOMIC_BOOLEAN.read(jr).get());
    }

    @Test(timeout = 4000)
    public void testAtomicBooleanWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.ATOMIC_BOOLEAN.write(jw, new AtomicBoolean(false));
        jw.close();
        assertEquals("false", sw.toString());
    }

    @Test(timeout = 4000)
    public void testAtomicIntegerArrayRead() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("[1,2,3]"));
        AtomicIntegerArray aia = TypeAdapters.ATOMIC_INTEGER_ARRAY.read(jr);
        assertEquals(3, aia.length());
        assertEquals(1, aia.get(0));
        assertEquals(2, aia.get(1));
        assertEquals(3, aia.get(2));
    }

    @Test(timeout = 4000, expected = JsonSyntaxException.class)
    public void testAtomicIntegerArrayReadInvalid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("[1,abc]"));
        TypeAdapters.ATOMIC_INTEGER_ARRAY.read(jr);
    }

    @Test(timeout = 4000)
    public void testAtomicIntegerArrayWrite() throws IOException {
        AtomicIntegerArray aia = new AtomicIntegerArray(new int[]{10, 20});
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.ATOMIC_INTEGER_ARRAY.write(jw, aia);
        jw.close();
        assertEquals("[10,20]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testLongReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.LONG.read(jr));
    }

    @Test(timeout = 4000)
    public void testLongReadValid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("9223372036854775807"));
        assertEquals(Long.MAX_VALUE, TypeAdapters.LONG.read(jr));
    }

    @Test(timeout = 4000, expected = JsonSyntaxException.class)
    public void testLongReadInvalid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("abc"));
        TypeAdapters.LONG.read(jr);
    }

    @Test(timeout = 4000)
    public void testLongWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.LONG.write(jw, Long.MIN_VALUE);
        jw.close();
        assertEquals("-9223372036854775808", sw.toString());
    }

    @Test(timeout = 4000)
    public void testFloatReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.FLOAT.read(jr));
    }

    @Test(timeout = 4000)
    public void testFloatRead() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("3.14"));
        assertEquals(3.14f, TypeAdapters.FLOAT.read(jr), 1e-6f);
    }

    @Test(timeout = 4000)
    public void testFloatWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.FLOAT.write(jw, 2.5f);
        jw.close();
        assertEquals("2.5", sw.toString());
    }

    @Test(timeout = 4000)
    public void testDoubleReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.DOUBLE.read(jr));
    }

    @Test(timeout = 4000)
    public void testDoubleRead() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("1.7976931348623157E308"));
        assertEquals(Double.MAX_VALUE, TypeAdapters.DOUBLE.read(jr), 1e-300);
    }

    @Test(timeout = 4000)
    public void testDoubleWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.DOUBLE.write(jw, -0.0);
        jw.close();
        assertEquals("-0.0", sw.toString());
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testNumberReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.NUMBER.read(jr));
    }

    @Test(timeout = 4000)
    public void testNumberReadNumber() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("42"));
        Number n = TypeAdapters.NUMBER.read(jr);
        assertTrue(n instanceof LazilyParsedNumber);
        assertEquals("42", n.toString());
    }

    // Defect-targeted test: read string as number
    @Test(timeout = 4000)
    public void testNumberAsStringDeserialization() throws IOException {
        // This test reveals the defect: NUMBER adapter should parse string "123" as a number
        // but currently throws JsonSyntaxException.
        JsonReader jr = new JsonReader(new StringReader("\"123\""));
        Number n = TypeAdapters.NUMBER.read(jr);
        assertNotNull(n);
        assertTrue("Expected LazilyParsedNumber but got " + n.getClass(), n instanceof LazilyParsedNumber);
        assertEquals("123", n.toString());
    }

    @Test(timeout = 4000, expected = JsonSyntaxException.class)
    public void testNumberReadInvalidToken() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("true"));
        TypeAdapters.NUMBER.read(jr);
    }

    @Test(timeout = 4000)
    public void testNumberWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.NUMBER.write(jw, 123);
        jw.close();
        assertEquals("123", sw.toString());
    }

    @Test(timeout = 4000)
    public void testCharacterReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.CHARACTER.read(jr));
    }

    @Test(timeout = 4000)
    public void testCharacterReadValid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"a\""));
        assertEquals(Character.valueOf('a'), TypeAdapters.CHARACTER.read(jr));
    }

    @Test(timeout = 4000, expected = JsonSyntaxException.class)
    public void testCharacterReadInvalidLength() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"ab\""));
        TypeAdapters.CHARACTER.read(jr);
    }

    @Test(timeout = 4000)
    public void testCharacterWriteNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.CHARACTER.write(jw, null);
        jw.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testCharacterWriteNonNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.CHARACTER.write(jw, 'Z');
        jw.close();
        assertEquals("\"Z\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testStringReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.STRING.read(jr));
    }

    @Test(timeout = 4000)
    public void testStringReadBoolean() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("true"));
        assertEquals("true", TypeAdapters.STRING.read(jr));
    }

    @Test(timeout = 4000)
    public void testStringReadString() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"hello\""));
        assertEquals("hello", TypeAdapters.STRING.read(jr));
    }

    @Test(timeout = 4000)
    public void testStringWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.STRING.write(jw, "test");
        jw.close();
        assertEquals("\"test\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testBigDecimalReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BIG_DECIMAL.read(jr));
    }

    @Test(timeout = 4000)
    public void testBigDecimalReadValid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"3.14159\""));
        assertEquals(new BigDecimal("3.14159"), TypeAdapters.BIG_DECIMAL.read(jr));
    }

    @Test(timeout = 4000, expected = JsonSyntaxException.class)
    public void testBigDecimalReadInvalid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"abc\""));
        TypeAdapters.BIG_DECIMAL.read(jr);
    }

    @Test(timeout = 4000)
    public void testBigDecimalWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BIG_DECIMAL.write(jw, BigDecimal.TEN);
        jw.close();
        assertEquals("10", sw.toString());
    }

    @Test(timeout = 4000)
    public void testBigIntegerReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BIG_INTEGER.read(jr));
    }

    @Test(timeout = 4000)
    public void testBigIntegerReadValid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"12345678901234567890\""));
        assertEquals(new BigInteger("12345678901234567890"), TypeAdapters.BIG_INTEGER.read(jr));
    }

    @Test(timeout = 4000, expected = JsonSyntaxException.class)
    public void testBigIntegerReadInvalid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"notanumber\""));
        TypeAdapters.BIG_INTEGER.read(jr);
    }

    @Test(timeout = 4000)
    public void testBigIntegerWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BIG_INTEGER.write(jw, BigInteger.ONE);
        jw.close();
        assertEquals("1", sw.toString());
    }

    @Test(timeout = 4000)
    public void testStringBuilderReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.STRING_BUILDER.read(jr));
    }

    @Test(timeout = 4000)
    public void testStringBuilderRead() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"abc\""));
        assertEquals(new StringBuilder("abc").toString(), TypeAdapters.STRING_BUILDER.read(jr).toString());
    }

    @Test(timeout = 4000)
    public void testStringBuilderWriteNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.STRING_BUILDER.write(jw, null);
        jw.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testStringBuilderWriteNonNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.STRING_BUILDER.write(jw, new StringBuilder("xyz"));
        jw.close();
        assertEquals("\"xyz\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testStringBufferReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.STRING_BUFFER.read(jr));
    }

    @Test(timeout = 4000)
    public void testStringBufferRead() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"def\""));
        assertEquals(new StringBuffer("def").toString(), TypeAdapters.STRING_BUFFER.read(jr).toString());
    }

    @Test(timeout = 4000)
    public void testStringBufferWriteNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.STRING_BUFFER.write(jw, null);
        jw.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testStringBufferWriteNonNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.STRING_BUFFER.write(jw, new StringBuffer("ghi"));
        jw.close();
        assertEquals("\"ghi\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testURLReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.URL.read(jr));
    }

    @Test(timeout = 4000)
    public void testURLReadNullString() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"null\""));
        assertNull(TypeAdapters.URL.read(jr));
    }

    @Test(timeout = 4000)
    public void testURLReadValid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"http://example.com\""));
        assertEquals(new URL("http://example.com"), TypeAdapters.URL.read(jr));
    }

    @Test(timeout = 4000)
    public void testURLWriteNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.URL.write(jw, null);
        jw.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testURLWriteNonNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.URL.write(jw, new URL("https://google.com"));
        jw.close();
        assertEquals("\"https://google.com\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testURIReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.URI.read(jr));
    }

    @Test(timeout = 4000)
    public void testURIReadNullString() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"null\""));
        assertNull(TypeAdapters.URI.read(jr));
    }

    @Test(timeout = 4000)
    public void testURIReadValid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"http://example.com/path\""));
        assertEquals(new URI("http://example.com/path"), TypeAdapters.URI.read(jr));
    }

    @Test(timeout = 4000, expected = JsonIOException.class)
    public void testURIReadInvalid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"invalid uri\""));
        TypeAdapters.URI.read(jr);
    }

    @Test(timeout = 4000)
    public void testURIWriteNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.URI.write(jw, null);
        jw.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testURIWriteNonNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.URI.write(jw, new URI("mailto:test@example.com"));
        jw.close();
        assertEquals("\"mailto:test@example.com\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testInetAddressReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.INET_ADDRESS.read(jr));
    }

    @Test(timeout = 4000)
    public void testInetAddressRead() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"127.0.0.1\""));
        assertEquals(InetAddress.getByName("127.0.0.1"), TypeAdapters.INET_ADDRESS.read(jr));
    }

    @Test(timeout = 4000)
    public void testInetAddressWriteNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.INET_ADDRESS.write(jw, null);
        jw.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testInetAddressWriteNonNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.INET_ADDRESS.write(jw, InetAddress.getLocalHost());
        jw.close();
        assertNotNull(sw.toString());
    }

    @Test(timeout = 4000)
    public void testUUIDReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.UUID.read(jr));
    }

    @Test(timeout = 4000)
    public void testUUIDReadValid() throws IOException {
        UUID uuid = UUID.randomUUID();
        JsonReader jr = new JsonReader(new StringReader("\"" + uuid.toString() + "\""));
        assertEquals(uuid, TypeAdapters.UUID.read(jr));
    }

    @Test(timeout = 4000)
    public void testUUIDWriteNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.UUID.write(jw, null);
        jw.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testUUIDWriteNonNull() throws IOException {
        UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.UUID.write(jw, uuid);
        jw.close();
        assertEquals("\"123e4567-e89b-12d3-a456-426614174000\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testCurrencyRead() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"USD\""));
        assertEquals(Currency.getInstance("USD"), TypeAdapters.CURRENCY.read(jr));
    }

    @Test(timeout = 4000)
    public void testCurrencyWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.CURRENCY.write(jw, Currency.getInstance("EUR"));
        jw.close();
        assertEquals("\"EUR\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testTimestampFactory() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<Timestamp> adapter = gson.getAdapter(Timestamp.class);
        // write
        Timestamp ts = new Timestamp(1234567890123L);
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        adapter.write(jw, ts);
        jw.close();
        // read back
        JsonReader jr = new JsonReader(new StringReader(sw.toString()));
        Timestamp result = adapter.read(jr);
        assertEquals(ts, result);
    }

    @Test(timeout = 4000)
    public void testTimestampFactoryNull() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<Timestamp> adapter = gson.getAdapter(Timestamp.class);
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(adapter.read(jr));
    }

    @Test(timeout = 4000)
    public void testCalendarReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.CALENDAR.read(jr));
    }

    @Test(timeout = 4000)
    public void testCalendarRead() throws IOException {
        String json = "{\"year\":2020,\"month\":0,\"dayOfMonth\":15,\"hourOfDay\":10,\"minute\":30,\"second\":0}";
        JsonReader jr = new JsonReader(new StringReader(json));
        Calendar cal = TypeAdapters.CALENDAR.read(jr);
        assertEquals(2020, cal.get(Calendar.YEAR));
        assertEquals(0, cal.get(Calendar.MONTH));
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
    }

    @Test(timeout = 4000)
    public void testCalendarWriteNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.CALENDAR.write(jw, null);
        jw.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testCalendarWrite() throws IOException {
        Calendar cal = new GregorianCalendar(2021, 11, 25, 8, 15, 45);
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.CALENDAR.write(jw, cal);
        jw.close();
        String expected = "{\"year\":2021,\"month\":11,\"dayOfMonth\":25,\"hourOfDay\":8,\"minute\":15,\"second\":45}";
        assertEquals(expected, sw.toString());
    }

    @Test(timeout = 4000)
    public void testLocaleReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.LOCALE.read(jr));
    }

    @Test(timeout = 4000)
    public void testLocaleReadLanguage() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"en\""));
        assertEquals(new Locale("en"), TypeAdapters.LOCALE.read(jr));
    }

    @Test(timeout = 4000)
    public void testLocaleReadLanguageCountry() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"en_US\""));
        assertEquals(new Locale("en", "US"), TypeAdapters.LOCALE.read(jr));
    }

    @Test(timeout = 4000)
    public void testLocaleReadLanguageCountryVariant() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"th_TH_TH_#u-nu-thai\""));
        // Note: StringTokenizer splits on "_", so variant becomes "TH" and then "#u-nu-thai" is extra? Actually the tokenizer will produce: "th", "TH", "TH", "#u-nu-thai". The code only takes first three tokens. So variant will be "TH". This is a known quirk.
        Locale loc = TypeAdapters.LOCALE.read(jr);
        assertEquals("th", loc.getLanguage());
        assertEquals("TH", loc.getCountry());
        assertEquals("TH", loc.getVariant());
    }

    @Test(timeout = 4000)
    public void testLocaleWriteNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.LOCALE.write(jw, null);
        jw.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testLocaleWriteNonNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.LOCALE.write(jw, Locale.CANADA_FRENCH);
        jw.close();
        assertEquals("\"fr_CA\"", sw.toString());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    // Already covered by testNumberAsStringDeserialization above.

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testJsonElementReadInvalidToken() throws IOException {
        // END_DOCUMENT is not handled
        JsonReader jr = new JsonReader(new StringReader(""));
        // This will cause END_DOCUMENT
        TypeAdapters.JSON_ELEMENT.read(jr);
    }

    @Test(timeout = 4000)
    public void testJsonElementReadString() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"hello\""));
        JsonElement elem = TypeAdapters.JSON_ELEMENT.read(jr);
        assertTrue(elem.isJsonPrimitive());
        assertEquals("hello", elem.getAsString());
    }

    @Test(timeout = 4000)
    public void testJsonElementReadNumber() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("42"));
        JsonElement elem = TypeAdapters.JSON_ELEMENT.read(jr);
        assertTrue(elem.isJsonPrimitive());
        assertEquals(42, elem.getAsInt());
    }

    @Test(timeout = 4000)
    public void testJsonElementReadBoolean() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("false"));
        JsonElement elem = TypeAdapters.JSON_ELEMENT.read(jr);
        assertTrue(elem.isJsonPrimitive());
        assertFalse(elem.getAsBoolean());
    }

    @Test(timeout = 4000)
    public void testJsonElementReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        JsonElement elem = TypeAdapters.JSON_ELEMENT.read(jr);
        assertTrue(elem.isJsonNull());
    }

    @Test(timeout = 4000)
    public void testJsonElementReadArray() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("[1, \"two\", true]"));
        JsonElement elem = TypeAdapters.JSON_ELEMENT.read(jr);
        assertTrue(elem.isJsonArray());
        JsonArray arr = elem.getAsJsonArray();
        assertEquals(3, arr.size());
        assertEquals(1, arr.get(0).getAsInt());
        assertEquals("two", arr.get(1).getAsString());
        assertTrue(arr.get(2).getAsBoolean());
    }

    @Test(timeout = 4000)
    public void testJsonElementReadObject() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("{\"key\":\"value\",\"num\":123}"));
        JsonElement elem = TypeAdapters.JSON_ELEMENT.read(jr);
        assertTrue(elem.isJsonObject());
        JsonObject obj = elem.getAsJsonObject();
        assertEquals("value", obj.get("key").getAsString());
        assertEquals(123, obj.get("num").getAsInt());
    }

    @Test(timeout = 4000)
    public void testJsonElementWriteNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, null);
        jw.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testJsonElementWriteJsonNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, JsonNull.INSTANCE);
        jw.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testJsonElementWritePrimitiveNumber() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, new JsonPrimitive(3.14));
        jw.close();
        assertEquals("3.14", sw.toString());
    }

    @Test(timeout = 4000)
    public void testJsonElementWritePrimitiveBoolean() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, new JsonPrimitive(true));
        jw.close();
        assertEquals("true", sw.toString());
    }

    @Test(timeout = 4000)
    public void testJsonElementWritePrimitiveString() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, new JsonPrimitive("test"));
        jw.close();
        assertEquals("\"test\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testJsonElementWriteArray() throws IOException {
        JsonArray arr = new JsonArray();
        arr.add(1);
        arr.add("two");
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, arr);
        jw.close();
        assertEquals("[1,\"two\"]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testJsonElementWriteObject() throws IOException {
        JsonObject obj = new JsonObject();
        obj.addProperty("a", 1);
        obj.addProperty("b", "c");
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, obj);
        jw.close();
        assertEquals("{\"a\":1,\"b\":\"c\"}", sw.toString());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testJsonElementWriteUnknownType() throws IOException {
        JsonElement unknown = new JsonElement() {
            @Override
            public JsonElement deepCopy() { return this; }
        };
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, unknown);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEnumFactory() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<TestEnum> adapter = gson.getAdapter(TestEnum.class);
        // write
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        adapter.write(jw, TestEnum.VALUE1);
        jw.close();
        assertEquals("\"custom1\"", sw.toString());
        // read
        JsonReader jr = new JsonReader(new StringReader("\"custom1\""));
        assertEquals(TestEnum.VALUE1, adapter.read(jr));
        // read alternate
        jr = new JsonReader(new StringReader("\"alt1\""));
        assertEquals(TestEnum.VALUE1, adapter.read(jr));
        // read null
        jr = new JsonReader(new StringReader("null"));
        assertNull(adapter.read(jr));
    }

    @Test(timeout = 4000)
    public void testEnumFactoryWriteNull() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<TestEnum> adapter = gson.getAdapter(TestEnum.class);
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        adapter.write(jw, null);
        jw.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testFactoryNewFactoryClass() {
        TypeAdapterFactory factory = TypeAdapters.newFactory(String.class, TypeAdapters.STRING);
        assertNotNull(factory.create(new Gson(), TypeToken.get(String.class)));
        assertNull(factory.create(new Gson(), TypeToken.get(Integer.class)));
    }

    @Test(timeout = 4000)
    public void testFactoryNewFactoryUnboxedBoxed() {
        TypeAdapterFactory factory = TypeAdapters.newFactory(int.class, Integer.class, TypeAdapters.INTEGER);
        assertNotNull(factory.create(new Gson(), TypeToken.get(int.class)));
        assertNotNull(factory.create(new Gson(), TypeToken.get(Integer.class)));
        assertNull(factory.create(new Gson(), TypeToken.get(long.class)));
    }

    @Test(timeout = 4000)
    public void testFactoryNewFactoryForMultipleTypes() {
        TypeAdapterFactory factory = TypeAdapters.newFactoryForMultipleTypes(Calendar.class, GregorianCalendar.class, TypeAdapters.CALENDAR);
        assertNotNull(factory.create(new Gson(), TypeToken.get(Calendar.class)));
        assertNotNull(factory.create(new Gson(), TypeToken.get(GregorianCalendar.class)));
        assertNull(factory.create(new Gson(), TypeToken.get(Date.class)));
    }

    @Test(timeout = 4000)
    public void testFactoryNewTypeHierarchyFactory() {
        TypeAdapterFactory factory = TypeAdapters.newTypeHierarchyFactory(JsonElement.class, TypeAdapters.JSON_ELEMENT);
        assertNotNull(factory.create(new Gson(), TypeToken.get(JsonObject.class)));
        assertNotNull(factory.create(new Gson(), TypeToken.get(JsonArray.class)));
        assertNull(factory.create(new Gson(), TypeToken.get(String.class)));
    }

    @Test(timeout = 4000)
    public void testFactoryNewTypeHierarchyFactoryTypeCheck() throws IOException {
        // Test that the hierarchy adapter checks the deserialized type
        TypeAdapterFactory factory = TypeAdapters.newTypeHierarchyFactory(Number.class, TypeAdapters.NUMBER);
        TypeAdapter<Number> adapter = factory.create(new Gson(), TypeToken.get(Integer.class));
        // Read a number that is not an Integer (e.g., a double)
        JsonReader jr = new JsonReader(new StringReader("3.14"));
        try {
            adapter.read(jr);
            fail("Expected JsonSyntaxException because 3.14 is not an Integer");
        } catch (JsonSyntaxException e) {
            // expected
        }
    }

    // Helper enum for testing EnumTypeAdapter
    private enum TestEnum {
        @SerializedName(value = "custom1", alternate = {"alt1", "alt2"})
        VALUE1,
        @SerializedName("custom2")
        VALUE2
    }
}