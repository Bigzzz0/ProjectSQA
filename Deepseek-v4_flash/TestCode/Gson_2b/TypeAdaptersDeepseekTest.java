package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Test;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.BitSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * Target class: TypeAdapters
 * Key decision branches:
 * - Null handling in all read/write methods
 * - BitSet: token type switch (NUMBER, BOOLEAN, STRING, default)
 * - Boolean: NULL, STRING, boolean
 * - NUMBER: NULL, NUMBER, default
 * - CHARACTER: NULL, length != 1
 * - STRING: NULL, BOOLEAN, string
 * - BIG_DECIMAL/BIG_INTEGER: NULL, NumberFormatException
 * - URL: NULL, "null" string, normal
 * - URI: NULL, "null" string, URISyntaxException
 * - CALENDAR: NULL, object field parsing, unrecognized name
 * - LOCALE: StringTokenizer with 0-3 tokens
 * - JSON_ELEMENT: switch on peek (STRING, NUMBER, BOOLEAN, NULL, BEGIN_ARRAY, BEGIN_OBJECT, default)
 * - EnumTypeAdapter: SerializedName and alternates
 * - Factories: type checks (rawType==, isAssignableFrom, isEnum, etc.)
 * 
 * Known defect: ClassCastException when deserializing a JSON primitive into JsonObject.
 *   Expected correct behavior: throw JsonSyntaxException instead of ClassCastException.
 *   Test: deserialize number into JsonObject.class, expect JsonSyntaxException.
 */
public class TypeAdaptersDeepseekTest {

    // ---------- Partition A: Core Functional Logic & State Transitions ----------

    @Test(timeout = 4000)
    public void testClassAdapterNull() throws Exception {
        // CLASS only supports null
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.CLASS.write(out, null);
        out.close();
        assertEquals("null", sw.toString());

        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.CLASS.read(in));
    }

    @Test(timeout = 4000)
    public void testClassAdapterNonNullFails() throws Exception {
        // writing a non-null class should throw
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        try {
            TypeAdapters.CLASS.write(out, String.class);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}
        // reading non-null should throw
        JsonReader in = new JsonReader(new StringReader("\"java.lang.String\""));
        try {
            TypeAdapters.CLASS.read(in);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}
    }

    @Test(timeout = 4000)
    public void testBitSetRoundtrip() throws Exception {
        BitSet original = new BitSet();
        original.set(0);
        original.set(5);
        original.set(63);

        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.BIT_SET.write(out, original);
        out.close();
        assertEquals("[1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1]", sw.toString());

        JsonReader in = new JsonReader(new StringReader(sw.toString()));
        BitSet result = TypeAdapters.BIT_SET.read(in);
        assertEquals(original, result);
    }

    @Test(timeout = 4000)
    public void testBitSetNull() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.BIT_SET.write(out, null);
        out.close();
        assertEquals("null", sw.toString());

        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BIT_SET.read(in));
    }

    @Test(timeout = 4000)
    public void testBitSetBooleanValues() throws Exception {
        // read boolean array: true/false
        JsonReader in = new JsonReader(new StringReader("[true, false, true]"));
        BitSet bitset = TypeAdapters.BIT_SET.read(in);
        assertTrue(bitset.get(0));
        assertFalse(bitset.get(1));
        assertTrue(bitset.get(2));
    }

    @Test(timeout = 4000)
    public void testBitSetStringValues() throws Exception {
        // string values "1"/"0"
        JsonReader in = new JsonReader(new StringReader("[\"1\",\"0\",\"1\"]"));
        BitSet bitset = TypeAdapters.BIT_SET.read(in);
        assertTrue(bitset.get(0));
        assertFalse(bitset.get(1));
        assertTrue(bitset.get(2));
    }

    @Test(timeout = 4000)
    public void testBitSetStringInvalidThrows() throws Exception {
        JsonReader in = new JsonReader(new StringReader("[\"abc\"]"));
        try {
            TypeAdapters.BIT_SET.read(in);
            fail("Expected JsonSyntaxException");
        } catch (JsonSyntaxException expected) {}
    }

    @Test(timeout = 4000)
    public void testBitSetInvalidTokenThrows() throws Exception {
        JsonReader in = new JsonReader(new StringReader("[null]"));
        try {
            TypeAdapters.BIT_SET.read(in);
            fail("Expected JsonSyntaxException");
        } catch (JsonSyntaxException expected) {}
    }

    @Test(timeout = 4000)
    public void testBooleanRoundtrip() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.BOOLEAN.write(out, true);
        out.close();
        assertEquals("true", sw.toString());

        JsonReader in = new JsonReader(new StringReader("true"));
        assertTrue(TypeAdapters.BOOLEAN.read(in));
    }

    @Test(timeout = 4000)
    public void testBooleanStringSupport() throws Exception {
        JsonReader in = new JsonReader(new StringReader("\"true\""));
        assertTrue(TypeAdapters.BOOLEAN.read(in));
    }

    @Test(timeout = 4000)
    public void testBooleanNull() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.BOOLEAN.write(out, null);
        out.close();
        assertEquals("null", sw.toString());

        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BOOLEAN.read(in));
    }

    @Test(timeout = 4000)
    public void testBooleanAsString() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.BOOLEAN_AS_STRING.write(out, false);
        out.close();
        assertEquals("\"false\"", sw.toString());

        JsonReader in = new JsonReader(new StringReader("\"false\""));
        assertFalse(TypeAdapters.BOOLEAN_AS_STRING.read(in));
    }

    @Test(timeout = 4000)
    public void testByte() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.BYTE.write(out, 42);
        out.close();
        assertEquals("42", sw.toString());

        JsonReader in = new JsonReader(new StringReader("42"));
        assertEquals(Byte.valueOf((byte)42), TypeAdapters.BYTE.read(in));
    }

    @Test(timeout = 4000)
    public void testByteNull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BYTE.read(in));
    }

    @Test(timeout = 4000)
    public void testShort() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.SHORT.write(out, 1000);
        out.close();
        assertEquals("1000", sw.toString());

        JsonReader in = new JsonReader(new StringReader("1000"));
        assertEquals(Short.valueOf((short)1000), TypeAdapters.SHORT.read(in));
    }

    @Test(timeout = 4000)
    public void testInteger() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.INTEGER.write(out, 123456);
        out.close();
        assertEquals("123456", sw.toString());

        JsonReader in = new JsonReader(new StringReader("123456"));
        assertEquals(Integer.valueOf(123456), TypeAdapters.INTEGER.read(in));
    }

    @Test(timeout = 4000)
    public void testLong() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.LONG.write(out, 9876543210L);
        out.close();
        assertEquals("9876543210", sw.toString());

        JsonReader in = new JsonReader(new StringReader("9876543210"));
        assertEquals(Long.valueOf(9876543210L), TypeAdapters.LONG.read(in));
    }

    @Test(timeout = 4000)
    public void testFloat() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.FLOAT.write(out, 3.14f);
        out.close();
        assertEquals("3.14", sw.toString());

        JsonReader in = new JsonReader(new StringReader("3.14"));
        assertEquals(Float.valueOf(3.14f), TypeAdapters.FLOAT.read(in));
    }

    @Test(timeout = 4000)
    public void testDouble() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.DOUBLE.write(out, 2.71828);
        out.close();
        assertEquals("2.71828", sw.toString());

        JsonReader in = new JsonReader(new StringReader("2.71828"));
        assertEquals(Double.valueOf(2.71828), TypeAdapters.DOUBLE.read(in));
    }

    @Test(timeout = 4000)
    public void testNumber() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.NUMBER.write(out, 123);
        out.close();
        assertEquals("123", sw.toString());

        JsonReader in = new JsonReader(new StringReader("123"));
        assertEquals(new com.google.gson.internal.LazilyParsedNumber("123"), TypeAdapters.NUMBER.read(in));
    }

    @Test(timeout = 4000)
    public void testNumberNull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.NUMBER.read(in));
    }

    @Test(timeout = 4000)
    public void testNumberInvalidToken() throws Exception {
        JsonReader in = new JsonReader(new StringReader("true"));
        try {
            TypeAdapters.NUMBER.read(in);
            fail("Expected JsonSyntaxException");
        } catch (JsonSyntaxException expected) {}
    }

    @Test(timeout = 4000)
    public void testCharacter() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.CHARACTER.write(out, 'A');
        out.close();
        assertEquals("\"A\"", sw.toString());

        JsonReader in = new JsonReader(new StringReader("\"A\""));
        assertEquals(Character.valueOf('A'), TypeAdapters.CHARACTER.read(in));
    }

    @Test(timeout = 4000)
    public void testCharacterNull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.CHARACTER.read(in));
    }

    @Test(timeout = 4000)
    public void testCharacterLengthMismatch() throws Exception {
        JsonReader in = new JsonReader(new StringReader("\"AB\""));
        try {
            TypeAdapters.CHARACTER.read(in);
            fail("Expected JsonSyntaxException");
        } catch (JsonSyntaxException expected) {}
    }

    @Test(timeout = 4000)
    public void testString() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.STRING.write(out, "hello");
        out.close();
        assertEquals("\"hello\"", sw.toString());

        JsonReader in = new JsonReader(new StringReader("\"hello\""));
        assertEquals("hello", TypeAdapters.STRING.read(in));
    }

    @Test(timeout = 4000)
    public void testStringNull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.STRING.read(in));
    }

    @Test(timeout = 4000)
    public void testStringCoercesBoolean() throws Exception {
        JsonReader in = new JsonReader(new StringReader("true"));
        assertEquals("true", TypeAdapters.STRING.read(in));
    }

    @Test(timeout = 4000)
    public void testBigDecimal() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.BIG_DECIMAL.write(out, new BigDecimal("123.456"));
        out.close();
        assertEquals("123.456", sw.toString());

        JsonReader in = new JsonReader(new StringReader("123.456"));
        assertEquals(new BigDecimal("123.456"), TypeAdapters.BIG_DECIMAL.read(in));
    }

    @Test(timeout = 4000)
    public void testBigDecimalNull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BIG_DECIMAL.read(in));
    }

    @Test(timeout = 4000)
    public void testBigDecimalInvalid() throws Exception {
        JsonReader in = new JsonReader(new StringReader("\"abc\""));
        try {
            TypeAdapters.BIG_DECIMAL.read(in);
            fail("Expected JsonSyntaxException");
        } catch (JsonSyntaxException expected) {}
    }

    @Test(timeout = 4000)
    public void testBigInteger() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.BIG_INTEGER.write(out, new BigInteger("987654321"));
        out.close();
        assertEquals("987654321", sw.toString());

        JsonReader in = new JsonReader(new StringReader("987654321"));
        assertEquals(new BigInteger("987654321"), TypeAdapters.BIG_INTEGER.read(in));
    }

    @Test(timeout = 4000)
    public void testBigIntegerNull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BIG_INTEGER.read(in));
    }

    @Test(timeout = 4000)
    public void testStringBuilder() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.STRING_BUILDER.write(out, new StringBuilder("test"));
        out.close();
        assertEquals("\"test\"", sw.toString());

        JsonReader in = new JsonReader(new StringReader("\"test\""));
        assertEquals("test", TypeAdapters.STRING_BUILDER.read(in).toString());
    }

    @Test(timeout = 4000)
    public void testStringBuilderNull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.STRING_BUILDER.read(in));
    }

    @Test(timeout = 4000)
    public void testStringBuffer() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.STRING_BUFFER.write(out, new StringBuffer("buffer"));
        out.close();
        assertEquals("\"buffer\"", sw.toString());

        JsonReader in = new JsonReader(new StringReader("\"buffer\""));
        assertEquals("buffer", TypeAdapters.STRING_BUFFER.read(in).toString());
    }

    @Test(timeout = 4000)
    public void testURL() throws Exception {
        URL url = new URL("http://example.com");
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.URL.write(out, url);
        out.close();
        assertEquals("\"http://example.com\"", sw.toString());

        JsonReader in = new JsonReader(new StringReader("\"http://example.com\""));
        assertEquals(url, TypeAdapters.URL.read(in));
    }

    @Test(timeout = 4000)
    public void testURLNull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.URL.read(in));
    }

    @Test(timeout = 4000)
    public void testURLStringNull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("\"null\""));
        assertNull(TypeAdapters.URL.read(in));
    }

    @Test(timeout = 4000)
    public void testURI() throws Exception {
        URI uri = new URI("http://example.com");
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.URI.write(out, uri);
        out.close();
        assertEquals("\"http://example.com\"", sw.toString());

        JsonReader in = new JsonReader(new StringReader("\"http://example.com\""));
        assertEquals(uri, TypeAdapters.URI.read(in));
    }

    @Test(timeout = 4000)
    public void testURINull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.URI.read(in));
    }

    @Test(timeout = 4000)
    public void testURIStringNull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("\"null\""));
        assertNull(TypeAdapters.URI.read(in));
    }

    @Test(timeout = 4000)
    public void testURIInvalidSyntax() throws Exception {
        JsonReader in = new JsonReader(new StringReader("\"http://[invalid\""));
        try {
            TypeAdapters.URI.read(in);
            fail("Expected JsonIOException");
        } catch (com.google.gson.JsonIOException expected) {}
    }

    @Test(timeout = 4000)
    public void testInetAddress() throws Exception {
        InetAddress addr = InetAddress.getByName("8.8.8.8");
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.INET_ADDRESS.write(out, addr);
        out.close();
        assertEquals("\"8.8.8.8\"", sw.toString());

        JsonReader in = new JsonReader(new StringReader("\"8.8.8.8\""));
        assertEquals(addr, TypeAdapters.INET_ADDRESS.read(in));
    }

    @Test(timeout = 4000)
    public void testInetAddressNull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.INET_ADDRESS.read(in));
    }

    @Test(timeout = 4000)
    public void testUUID() throws Exception {
        UUID uuid = UUID.randomUUID();
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.UUID.write(out, uuid);
        out.close();
        assertEquals("\"" + uuid.toString() + "\"", sw.toString());

        JsonReader in = new JsonReader(new StringReader("\"" + uuid.toString() + "\""));
        assertEquals(uuid, TypeAdapters.UUID.read(in));
    }

    @Test(timeout = 4000)
    public void testUUIDNull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.UUID.read(in));
    }

    @Test(timeout = 4000)
    public void testCalendarRoundtrip() throws Exception {
        Calendar cal = new GregorianCalendar(2021, 11, 25, 10, 30, 45);
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.CALENDAR.write(out, cal);
        out.close();
        String json = sw.toString();
        assertTrue(json.contains("\"year\":2021"));
        assertTrue(json.contains("\"month\":11"));

        JsonReader in = new JsonReader(new StringReader(json));
        Calendar result = TypeAdapters.CALENDAR.read(in);
        assertEquals(2021, result.get(Calendar.YEAR));
        assertEquals(11, result.get(Calendar.MONTH));
        assertEquals(25, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, result.get(Calendar.MINUTE));
        assertEquals(45, result.get(Calendar.SECOND));
    }

    @Test(timeout = 4000)
    public void testCalendarNull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.CALENDAR.read(in));
    }

    @Test(timeout = 4000)
    public void testLocale() throws Exception {
        Locale loc = new Locale("en", "US", "WIN");
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.LOCALE.write(out, loc);
        out.close();
        assertEquals("\"en_US_WIN\"", sw.toString());

        JsonReader in = new JsonReader(new StringReader("\"en_US_WIN\""));
        assertEquals(loc, TypeAdapters.LOCALE.read(in));
    }

    @Test(timeout = 4000)
    public void testLocaleNull() throws Exception {
        JsonReader in = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.LOCALE.read(in));
    }

    @Test(timeout = 4000)
    public void testLocaleLanguageOnly() throws Exception {
        JsonReader in = new JsonReader(new StringReader("\"fr\""));
        Locale loc = TypeAdapters.LOCALE.read(in);
        assertEquals("fr", loc.getLanguage());
        assertNull(loc.getCountry());
        assertNull(loc.getVariant());
    }

    @Test(timeout = 4000)
    public void testLocaleLanguageCountry() throws Exception {
        JsonReader in = new JsonReader(new StringReader("\"de_DE\""));
        Locale loc = TypeAdapters.LOCALE.read(in);
        assertEquals("de", loc.getLanguage());
        assertEquals("DE", loc.getCountry());
        assertNull(loc.getVariant());
    }

    // ---------- Partition B: Boundary Value Analysis & Null Handling ----------

    @Test(timeout = 4000)
    public void testNullValuesForAllNumeric() throws Exception {
        assertNull(TypeAdapters.BYTE.read(new JsonReader(new StringReader("null"))));
        assertNull(TypeAdapters.SHORT.read(new JsonReader(new StringReader("null"))));
        assertNull(TypeAdapters.INTEGER.read(new JsonReader(new StringReader("null"))));
        assertNull(TypeAdapters.LONG.read(new JsonReader(new StringReader("null"))));
        assertNull(TypeAdapters.FLOAT.read(new JsonReader(new StringReader("null"))));
        assertNull(TypeAdapters.DOUBLE.read(new JsonReader(new StringReader("null"))));
    }

    @Test(timeout = 4000)
    public void testBitSetEmptyArray() throws Exception {
        JsonReader in = new JsonReader(new StringReader("[]"));
        BitSet bitset = TypeAdapters.BIT_SET.read(in);
        assertTrue(bitset.isEmpty());
    }

    // ---------- Partition C: Defect-Targeted Branch Zone ----------

    @Test(timeout = 4000)
    public void testJsonElementTypeMismatch_NumberToObject() {
        // The bug: deserializing a JSON number into JsonObject.class throws ClassCastException
        // Correct behavior: throw JsonSyntaxException (type mismatch)
        Gson gson = new Gson();
        try {
            gson.fromJson("123", JsonObject.class);
            fail("Expected JsonSyntaxException but no exception thrown");
        } catch (JsonSyntaxException e) {
            // expected correct behavior
        } catch (ClassCastException e) {
            // buggy version throws this, test will fail
            fail("Bug: ClassCastException thrown instead of JsonSyntaxException");
        }
    }

    @Test(timeout = 4000)
    public void testJsonElementTypeMismatch_StringToObject() {
        Gson gson = new Gson();
        try {
            gson.fromJson("\"hello\"", JsonObject.class);
            fail("Expected JsonSyntaxException but no exception thrown");
        } catch (JsonSyntaxException e) {
            // expected
        } catch (ClassCastException e) {
            fail("Bug: ClassCastException thrown instead of JsonSyntaxException");
        }
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------

    @Test(timeout = 4000)
    public void testBitSetWriteNull() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.BIT_SET.write(out, null);
        out.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testCalendarWriteNull() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.CALENDAR.write(out, null);
        out.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testJsonSyntaxExceptionFromNumberFormat() throws Exception {
        JsonReader in = new JsonReader(new StringReader("\"notanumber\""));
        try {
            TypeAdapters.INTEGER.read(in);
            fail("Expected JsonSyntaxException");
        } catch (JsonSyntaxException expected) {}
    }

    @Test(timeout = 4000)
    public void testJsonElementReadInvalidToken() throws Exception {
        // END_DOCUMENT triggers default -> IllegalArgumentException
        JsonReader in = new JsonReader(new StringReader(""));
        try {
            TypeAdapters.JSON_ELEMENT.read(in);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {}
    }

    @Test(timeout = 4000)
    public void testJsonElementRoundtrip() throws Exception {
        // Write a JsonObject and read it back
        JsonObject original = new JsonObject();
        original.addProperty("key", "value");
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(out, original);
        out.close();
        String json = sw.toString();

        JsonReader in = new JsonReader(new StringReader(json));
        JsonElement result = TypeAdapters.JSON_ELEMENT.read(in);
        assertEquals(original, result);
    }

    @Test(timeout = 4000)
    public void testJsonElementWriteNull() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(out, null);
        out.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testJsonElementWriteJsonNull() throws Exception {
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(out, com.google.gson.JsonNull.INSTANCE);
        out.close();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testJsonElementWriteArray() throws Exception {
        JsonArray array = new JsonArray();
        array.add(1);
        array.add("two");
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(out, array);
        out.close();
        assertEquals("[1,\"two\"]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testJsonElementWritePrimitiveNumber() throws Exception {
        JsonPrimitive prim = new JsonPrimitive(42);
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(out, prim);
        out.close();
        assertEquals("42", sw.toString());
    }

    @Test(timeout = 4000)
    public void testJsonElementWritePrimitiveString() throws Exception {
        JsonPrimitive prim = new JsonPrimitive("test");
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(out, prim);
        out.close();
        assertEquals("\"test\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testJsonElementWritePrimitiveBoolean() throws Exception {
        JsonPrimitive prim = new JsonPrimitive(false);
        StringWriter sw = new StringWriter();
        JsonWriter out = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(out, prim);
        out.close();
        assertEquals("false", sw.toString());
    }

    @Test(timeout = 4000)
    public void testEnumFactoryWithSerializedName() throws Exception {
        // Use a simple enum with SerializedName
        // We'll use Gson to ensure factory works, but we can also test the adapter directly via reflection
        Gson gson = new Gson();
        MyEnum e = gson.fromJson("\"custom_name\"", MyEnum.class);
        assertEquals(MyEnum.VALUE, e);
    }

    enum MyEnum {
        @com.google.gson.annotations.SerializedName("custom_name")
        VALUE,
        OTHER
    }

    @Test(timeout = 4000)
    public void testEnumFactoryWithAlternate() throws Exception {
        Gson gson = new Gson();
        MyEnumWithAlternates e = gson.fromJson("\"alt_name\"", MyEnumWithAlternates.class);
        assertEquals(MyEnumWithAlternates.FOO, e);
    }

    enum MyEnumWithAlternates {
        @com.google.gson.annotations.SerializedName(value = "primary", alternate = {"alt_name"})
        FOO
    }

    // ---------- Partition E: Object Lifecycle & Contract Integrity ----------
    // Factories are tested implicitly; no public constructors.

    @Test(timeout = 4000)
    public void testPrivateConstructorThrows() throws Exception {
        try {
            java.lang.reflect.Constructor<TypeAdapters> c = TypeAdapters.class.getDeclaredConstructor();
            c.setAccessible(true);
            c.newInstance();
            fail("Expected UnsupportedOperationException");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue(e.getCause() instanceof UnsupportedOperationException);
        }
    }
}