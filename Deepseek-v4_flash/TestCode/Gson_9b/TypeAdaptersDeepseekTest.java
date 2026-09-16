package com.google.gson.internal.bind;

import org.junit.Test;
import static org.junit.Assert.*;

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
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target defect: JsonWriterTest::testBoxedBooleans - BOOLEAN adapter write path
 * must correctly serialize boxed Boolean values (true/false) as JSON booleans,
 * not as strings or nulls. The defect likely causes incorrect output for
 * Boolean.TRUE/Boolean.FALSE or null handling.
 * 
 * Branches targeted:
 * - BOOLEAN.read: NULL token -> null; "true"/"false" strings -> Boolean; 
 *   boolean primitive -> Boolean; other -> JsonSyntaxException
 * - BOOLEAN.write: null -> out.nullValue(); true -> out.value(true); 
 *   false -> out.value(false)
 * - BOOLEAN_AS_STRING.write: null -> "null"; true -> "true"; false -> "false"
 * - BYTE/SHORT/INTEGER/LONG/FLOAT/DOUBLE read/write: NULL handling, 
 *   numeric parsing, overflow, string coercion
 * - NUMBER.read: NULL, LazilyParsedNumber, double/float/long/int parsing
 * - CHARACTER.read: NULL, single char, multi-char -> JsonSyntaxException
 * - STRING.read: NULL, boolean coercion, string
 * - BIG_DECIMAL/BIG_INTEGER: NULL, parsing, write
 * - URL/URI: NULL, "null" string, valid/invalid formats
 * - UUID: NULL, valid/invalid UUID
 * - CURRENCY: NULL, valid/invalid currency code
 * - CALENDAR: read/write all fields, NULL
 * - LOCALE: read with language/country/variant combinations, NULL
 * - JSON_ELEMENT: read/write all JsonElement types (NULL, primitive, array, object)
 * - ENUM_FACTORY: enum read/write, SerializedName annotation
 * - TIMESTAMP_FACTORY: Timestamp read/write via Date adapter
 * - INET_ADDRESS: read/write, NULL
 * - ATOMIC_INTEGER/BOOLEAN/INTEGER_ARRAY: read/write, NULL
 * - BIT_SET: read/write, number parsing, invalid values
 * - newFactory/newFactoryForMultipleTypes/newTypeHierarchyFactory: type matching
 * 
 * Boundary values:
 * - null inputs for all adapters
 * - empty strings for string-based adapters
 * - 0, -1, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MAX_VALUE, Double.MAX_VALUE
 * - "true"/"false" strings for BOOLEAN
 * - single/multi-char strings for CHARACTER
 * - valid/invalid UUID, URL, URI, Currency, InetAddress
 * - Calendar with all fields set
 * - Locale with language only, language+country, language+country+variant
 * - JsonElement: JsonNull, JsonPrimitive (number/boolean/string), JsonArray, JsonObject
 * - Enum with/without SerializedName
 * - BitSet with 0, 1, mixed values
 * - AtomicIntegerArray with 0, 1, multiple elements
 */
public class TypeAdaptersDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testBooleanReadWrite() throws IOException {
        // Write true/false/null
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BOOLEAN.write(jw, Boolean.TRUE);
        jw.flush();
        assertEquals("true", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.BOOLEAN.write(jw, Boolean.FALSE);
        jw.flush();
        assertEquals("false", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.BOOLEAN.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        // Read true/false/null
        JsonReader jr = new JsonReader(new StringReader("true"));
        assertEquals(Boolean.TRUE, TypeAdapters.BOOLEAN.read(jr));

        jr = new JsonReader(new StringReader("false"));
        assertEquals(Boolean.FALSE, TypeAdapters.BOOLEAN.read(jr));

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BOOLEAN.read(jr));

        // String coercion for backwards compatibility
        jr = new JsonReader(new StringReader("\"true\""));
        assertEquals(Boolean.TRUE, TypeAdapters.BOOLEAN.read(jr));

        jr = new JsonReader(new StringReader("\"false\""));
        assertEquals(Boolean.FALSE, TypeAdapters.BOOLEAN.read(jr));
    }

    @Test(timeout = 4000)
    public void testBooleanAsStringWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BOOLEAN_AS_STRING.write(jw, Boolean.TRUE);
        jw.flush();
        assertEquals("\"true\"", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.BOOLEAN_AS_STRING.write(jw, Boolean.FALSE);
        jw.flush();
        assertEquals("\"false\"", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.BOOLEAN_AS_STRING.write(jw, null);
        jw.flush();
        assertEquals("\"null\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testByteReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BYTE.write(jw, (byte) 42);
        jw.flush();
        assertEquals("42", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.BYTE.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("42"));
        assertEquals((byte) 42, TypeAdapters.BYTE.read(jr).byteValue());

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BYTE.read(jr));

        // String coercion
        jr = new JsonReader(new StringReader("\"42\""));
        assertEquals((byte) 42, TypeAdapters.BYTE.read(jr).byteValue());
    }

    @Test(timeout = 4000)
    public void testShortReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.SHORT.write(jw, (short) 1234);
        jw.flush();
        assertEquals("1234", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.SHORT.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("1234"));
        assertEquals((short) 1234, TypeAdapters.SHORT.read(jr).shortValue());

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.SHORT.read(jr));
    }

    @Test(timeout = 4000)
    public void testIntegerReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.INTEGER.write(jw, 123456);
        jw.flush();
        assertEquals("123456", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.INTEGER.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("123456"));
        assertEquals(123456, TypeAdapters.INTEGER.read(jr).intValue());

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.INTEGER.read(jr));

        // String coercion
        jr = new JsonReader(new StringReader("\"123456\""));
        assertEquals(123456, TypeAdapters.INTEGER.read(jr).intValue());
    }

    @Test(timeout = 4000)
    public void testLongReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.LONG.write(jw, 1234567890123L);
        jw.flush();
        assertEquals("1234567890123", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.LONG.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("1234567890123"));
        assertEquals(1234567890123L, TypeAdapters.LONG.read(jr).longValue());

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.LONG.read(jr));
    }

    @Test(timeout = 4000)
    public void testFloatReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.FLOAT.write(jw, 3.14f);
        jw.flush();
        assertEquals("3.14", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.FLOAT.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("3.14"));
        assertEquals(3.14f, TypeAdapters.FLOAT.read(jr).floatValue(), 0.0001f);

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.FLOAT.read(jr));
    }

    @Test(timeout = 4000)
    public void testDoubleReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.DOUBLE.write(jw, 2.71828d);
        jw.flush();
        assertEquals("2.71828", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.DOUBLE.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("2.71828"));
        assertEquals(2.71828d, TypeAdapters.DOUBLE.read(jr).doubleValue(), 0.00001d);

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.DOUBLE.read(jr));
    }

    @Test(timeout = 4000)
    public void testNumberReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.NUMBER.write(jw, 123);
        jw.flush();
        assertEquals("123", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.NUMBER.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("123"));
        assertEquals(123, TypeAdapters.NUMBER.read(jr).intValue());

        jr = new JsonReader(new StringReader("3.14"));
        assertEquals(3.14, TypeAdapters.NUMBER.read(jr).doubleValue(), 0.0001);

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.NUMBER.read(jr));
    }

    @Test(timeout = 4000)
    public void testCharacterReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.CHARACTER.write(jw, 'A');
        jw.flush();
        assertEquals("\"A\"", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.CHARACTER.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"A\""));
        assertEquals('A', TypeAdapters.CHARACTER.read(jr).charValue());

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.CHARACTER.read(jr));
    }

    @Test(timeout = 4000)
    public void testCharacterReadInvalidLength() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"AB\""));
        try {
            TypeAdapters.CHARACTER.read(jr);
            fail("Expected JsonSyntaxException for multi-char string");
        } catch (JsonSyntaxException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testStringReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.STRING.write(jw, "hello");
        jw.flush();
        assertEquals("\"hello\"", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.STRING.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"hello\""));
        assertEquals("hello", TypeAdapters.STRING.read(jr));

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.STRING.read(jr));

        // Boolean coercion
        jr = new JsonReader(new StringReader("true"));
        assertEquals("true", TypeAdapters.STRING.read(jr));
    }

    @Test(timeout = 4000)
    public void testBigDecimalReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BIG_DECIMAL.write(jw, new BigDecimal("123.456"));
        jw.flush();
        assertEquals("123.456", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.BIG_DECIMAL.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("123.456"));
        assertEquals(new BigDecimal("123.456"), TypeAdapters.BIG_DECIMAL.read(jr));

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BIG_DECIMAL.read(jr));
    }

    @Test(timeout = 4000)
    public void testBigIntegerReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BIG_INTEGER.write(jw, new BigInteger("12345678901234567890"));
        jw.flush();
        assertEquals("12345678901234567890", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.BIG_INTEGER.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("12345678901234567890"));
        assertEquals(new BigInteger("12345678901234567890"), TypeAdapters.BIG_INTEGER.read(jr));

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BIG_INTEGER.read(jr));
    }

    @Test(timeout = 4000)
    public void testStringBuilderReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.STRING_BUILDER.write(jw, new StringBuilder("test"));
        jw.flush();
        assertEquals("\"test\"", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.STRING_BUILDER.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"test\""));
        assertEquals("test", TypeAdapters.STRING_BUILDER.read(jr).toString());

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.STRING_BUILDER.read(jr));
    }

    @Test(timeout = 4000)
    public void testStringBufferReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.STRING_BUFFER.write(jw, new StringBuffer("test"));
        jw.flush();
        assertEquals("\"test\"", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.STRING_BUFFER.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"test\""));
        assertEquals("test", TypeAdapters.STRING_BUFFER.read(jr).toString());

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.STRING_BUFFER.read(jr));
    }

    @Test(timeout = 4000)
    public void testUrlReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.URL.write(jw, new URL("http://example.com"));
        jw.flush();
        assertEquals("\"http://example.com\"", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.URL.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"http://example.com\""));
        assertEquals(new URL("http://example.com"), TypeAdapters.URL.read(jr));

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.URL.read(jr));

        // "null" string special case
        jr = new JsonReader(new StringReader("\"null\""));
        assertNull(TypeAdapters.URL.read(jr));
    }

    @Test(timeout = 4000)
    public void testUriReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.URI.write(jw, new URI("http://example.com"));
        jw.flush();
        assertEquals("\"http://example.com\"", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.URI.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"http://example.com\""));
        assertEquals(new URI("http://example.com"), TypeAdapters.URI.read(jr));

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.URI.read(jr));
    }

    @Test(timeout = 4000)
    public void testUriReadInvalid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"invalid uri\""));
        try {
            TypeAdapters.URI.read(jr);
            fail("Expected JsonSyntaxException for invalid URI");
        } catch (JsonSyntaxException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testUuidReadWrite() throws IOException {
        UUID uuid = UUID.randomUUID();
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.UUID.write(jw, uuid);
        jw.flush();
        assertEquals("\"" + uuid.toString() + "\"", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.UUID.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"" + uuid.toString() + "\""));
        assertEquals(uuid, TypeAdapters.UUID.read(jr));

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.UUID.read(jr));
    }

    @Test(timeout = 4000)
    public void testUuidReadInvalid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"invalid-uuid\""));
        try {
            TypeAdapters.UUID.read(jr);
            fail("Expected IllegalArgumentException for invalid UUID");
        } catch (IllegalArgumentException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCurrencyReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.CURRENCY.write(jw, Currency.getInstance("USD"));
        jw.flush();
        assertEquals("\"USD\"", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.CURRENCY.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"USD\""));
        assertEquals(Currency.getInstance("USD"), TypeAdapters.CURRENCY.read(jr));

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.CURRENCY.read(jr));
    }

    @Test(timeout = 4000)
    public void testCurrencyReadInvalid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"INVALID\""));
        try {
            TypeAdapters.CURRENCY.read(jr);
            fail("Expected IllegalArgumentException for invalid currency");
        } catch (IllegalArgumentException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCalendarReadWrite() throws IOException {
        Calendar cal = new GregorianCalendar(2023, Calendar.NOVEMBER, 15, 10, 30, 45);
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.CALENDAR.write(jw, cal);
        jw.flush();
        String json = sw.toString();
        assertTrue(json.contains("\"year\":2023"));
        assertTrue(json.contains("\"month\":10"));
        assertTrue(json.contains("\"dayOfMonth\":15"));
        assertTrue(json.contains("\"hourOfDay\":10"));
        assertTrue(json.contains("\"minute\":30"));
        assertTrue(json.contains("\"second\":45"));

        JsonReader jr = new JsonReader(new StringReader(json));
        Calendar result = TypeAdapters.CALENDAR.read(jr);
        assertEquals(2023, result.get(Calendar.YEAR));
        assertEquals(Calendar.NOVEMBER, result.get(Calendar.MONTH));
        assertEquals(15, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(10, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(30, result.get(Calendar.MINUTE));
        assertEquals(45, result.get(Calendar.SECOND));
    }

    @Test(timeout = 4000)
    public void testCalendarReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.CALENDAR.read(jr));
    }

    @Test(timeout = 4000)
    public void testLocaleReadWrite() throws IOException {
        // Language only
        Locale locale = new Locale("en");
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.LOCALE.write(jw, locale);
        jw.flush();
        assertEquals("\"en\"", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"en\""));
        assertEquals(locale, TypeAdapters.LOCALE.read(jr));

        // Language + Country
        locale = new Locale("en", "US");
        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.LOCALE.write(jw, locale);
        jw.flush();
        assertEquals("\"en_US\"", sw.toString());

        jr = new JsonReader(new StringReader("\"en_US\""));
        assertEquals(locale, TypeAdapters.LOCALE.read(jr));

        // Language + Country + Variant
        locale = new Locale("en", "US", "MAC");
        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.LOCALE.write(jw, locale);
        jw.flush();
        assertEquals("\"en_US_MAC\"", sw.toString());

        jr = new JsonReader(new StringReader("\"en_US_MAC\""));
        assertEquals(locale, TypeAdapters.LOCALE.read(jr));

        // Null
        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.LOCALE.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.LOCALE.read(jr));
    }

    @Test(timeout = 4000)
    public void testJsonElementReadWrite() throws IOException {
        // Null
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("null"));
        assertTrue(TypeAdapters.JSON_ELEMENT.read(jr).isJsonNull());

        // JsonNull
        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, JsonNull.INSTANCE);
        jw.flush();
        assertEquals("null", sw.toString());

        // JsonPrimitive - number
        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, new JsonPrimitive(123));
        jw.flush();
        assertEquals("123", sw.toString());

        jr = new JsonReader(new StringReader("123"));
        JsonElement elem = TypeAdapters.JSON_ELEMENT.read(jr);
        assertTrue(elem.isJsonPrimitive());
        assertEquals(123, elem.getAsInt());

        // JsonPrimitive - boolean
        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, new JsonPrimitive(true));
        jw.flush();
        assertEquals("true", sw.toString());

        jr = new JsonReader(new StringReader("true"));
        elem = TypeAdapters.JSON_ELEMENT.read(jr);
        assertTrue(elem.isJsonPrimitive());
        assertTrue(elem.getAsBoolean());

        // JsonPrimitive - string
        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, new JsonPrimitive("hello"));
        jw.flush();
        assertEquals("\"hello\"", sw.toString());

        jr = new JsonReader(new StringReader("\"hello\""));
        elem = TypeAdapters.JSON_ELEMENT.read(jr);
        assertTrue(elem.isJsonPrimitive());
        assertEquals("hello", elem.getAsString());

        // JsonArray
        JsonArray array = new JsonArray();
        array.add(1);
        array.add("two");
        array.add(true);
        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, array);
        jw.flush();
        assertEquals("[1,\"two\",true]", sw.toString());

        jr = new JsonReader(new StringReader("[1,\"two\",true]"));
        elem = TypeAdapters.JSON_ELEMENT.read(jr);
        assertTrue(elem.isJsonArray());
        assertEquals(3, elem.getAsJsonArray().size());

        // JsonObject
        JsonObject obj = new JsonObject();
        obj.addProperty("key1", "value1");
        obj.addProperty("key2", 42);
        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.JSON_ELEMENT.write(jw, obj);
        jw.flush();
        assertEquals("{\"key1\":\"value1\",\"key2\":42}", sw.toString());

        jr = new JsonReader(new StringReader("{\"key1\":\"value1\",\"key2\":42}"));
        elem = TypeAdapters.JSON_ELEMENT.read(jr);
        assertTrue(elem.isJsonObject());
        assertEquals("value1", elem.getAsJsonObject().get("key1").getAsString());
        assertEquals(42, elem.getAsJsonObject().get("key2").getAsInt());
    }

    @Test(timeout = 4000)
    public void testAtomicIntegerReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.ATOMIC_INTEGER.write(jw, new AtomicInteger(42));
        jw.flush();
        assertEquals("42", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.ATOMIC_INTEGER.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("42"));
        assertEquals(42, TypeAdapters.ATOMIC_INTEGER.read(jr).get());

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.ATOMIC_INTEGER.read(jr));
    }

    @Test(timeout = 4000)
    public void testAtomicBooleanReadWrite() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.ATOMIC_BOOLEAN.write(jw, new AtomicBoolean(true));
        jw.flush();
        assertEquals("true", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.ATOMIC_BOOLEAN.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("true"));
        assertTrue(TypeAdapters.ATOMIC_BOOLEAN.read(jr).get());

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.ATOMIC_BOOLEAN.read(jr));
    }

    @Test(timeout = 4000)
    public void testAtomicIntegerArrayReadWrite() throws IOException {
        AtomicIntegerArray array = new AtomicIntegerArray(new int[]{1, 2, 3});
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.ATOMIC_INTEGER_ARRAY.write(jw, array);
        jw.flush();
        assertEquals("[1,2,3]", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.ATOMIC_INTEGER_ARRAY.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("[1,2,3]"));
        AtomicIntegerArray result = TypeAdapters.ATOMIC_INTEGER_ARRAY.read(jr);
        assertEquals(3, result.length());
        assertEquals(1, result.get(0));
        assertEquals(2, result.get(1));
        assertEquals(3, result.get(2));

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.ATOMIC_INTEGER_ARRAY.read(jr));
    }

    @Test(timeout = 4000)
    public void testBitSetReadWrite() throws IOException {
        BitSet bitset = new BitSet();
        bitset.set(0);
        bitset.set(2);
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BIT_SET.write(jw, bitset);
        jw.flush();
        assertEquals("[true,false,true]", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.BIT_SET.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("[true,false,true]"));
        BitSet result = TypeAdapters.BIT_SET.read(jr);
        assertTrue(result.get(0));
        assertFalse(result.get(1));
        assertTrue(result.get(2));

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.BIT_SET.read(jr));
    }

    @Test(timeout = 4000)
    public void testBitSetReadInvalidValue() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("[2]"));
        try {
            TypeAdapters.BIT_SET.read(jr);
            fail("Expected JsonSyntaxException for invalid bitset value");
        } catch (JsonSyntaxException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testBitSetReadInvalidType() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("[\"true\"]"));
        try {
            TypeAdapters.BIT_SET.read(jr);
            fail("Expected JsonSyntaxException for invalid bitset type");
        } catch (JsonSyntaxException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testInetAddressReadWrite() throws IOException {
        InetAddress addr = InetAddress.getByName("127.0.0.1");
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.INET_ADDRESS.write(jw, addr);
        jw.flush();
        assertEquals("\"127.0.0.1\"", sw.toString());

        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.INET_ADDRESS.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        JsonReader jr = new JsonReader(new StringReader("\"127.0.0.1\""));
        assertEquals(addr, TypeAdapters.INET_ADDRESS.read(jr));

        jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.INET_ADDRESS.read(jr));
    }

    @Test(timeout = 4000)
    public void testTimestampFactory() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<Timestamp> adapter = TypeAdapters.TIMESTAMP_FACTORY.create(gson, TypeToken.get(Timestamp.class));
        assertNotNull(adapter);

        Timestamp ts = new Timestamp(1234567890123L);
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        adapter.write(jw, ts);
        jw.flush();
        String json = sw.toString();
        assertTrue(json.contains("1234567890123"));

        JsonReader jr = new JsonReader(new StringReader(json));
        Timestamp result = adapter.read(jr);
        assertEquals(ts.getTime(), result.getTime());

        // Null handling
        sw = new StringWriter();
        jw = new JsonWriter(sw);
        adapter.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        jr = new JsonReader(new StringReader("null"));
        assertNull(adapter.read(jr));
    }

    @Test(timeout = 4000)
    public void testTimestampFactoryNonTimestamp() {
        Gson gson = new Gson();
        TypeAdapter<String> adapter = TypeAdapters.TIMESTAMP_FACTORY.create(gson, TypeToken.get(String.class));
        assertNull(adapter);
    }

    @Test(timeout = 4000)
    public void testEnumFactory() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<TestEnum> adapter = TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(TestEnum.class));
        assertNotNull(adapter);

        // Write
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        adapter.write(jw, TestEnum.VALUE1);
        jw.flush();
        assertEquals("\"VALUE1\"", sw.toString());

        // Read
        JsonReader jr = new JsonReader(new StringReader("\"VALUE1\""));
        assertEquals(TestEnum.VALUE1, adapter.read(jr));

        // Null
        sw = new StringWriter();
        jw = new JsonWriter(sw);
        adapter.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());

        jr = new JsonReader(new StringReader("null"));
        assertNull(adapter.read(jr));
    }

    @Test(timeout = 4000)
    public void testEnumFactorySerializedName() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<TestEnumSerializedName> adapter = TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(TestEnumSerializedName.class));
        assertNotNull(adapter);

        // Write uses SerializedName
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        adapter.write(jw, TestEnumSerializedName.FIRST);
        jw.flush();
        assertEquals("\"first_value\"", sw.toString());

        // Read uses SerializedName
        JsonReader jr = new JsonReader(new StringReader("\"first_value\""));
        assertEquals(TestEnumSerializedName.FIRST, adapter.read(jr));
    }

    @Test(timeout = 4000)
    public void testEnumFactoryNonEnum() {
        Gson gson = new Gson();
        TypeAdapter<String> adapter = TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(String.class));
        assertNull(adapter);
    }

    @Test(timeout = 4000)
    public void testEnumFactoryEnumClass() {
        Gson gson = new Gson();
        TypeAdapter<Enum> adapter = TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(Enum.class));
        assertNull(adapter);
    }

    @Test(timeout = 4000)
    public void testNewFactory() {
        TypeAdapterFactory factory = TypeAdapters.newFactory(String.class, TypeAdapters.STRING);
        Gson gson = new Gson();
        TypeAdapter<String> adapter = factory.create(gson, TypeToken.get(String.class));
        assertNotNull(adapter);
        assertSame(TypeAdapters.STRING, adapter);

        // Non-matching type
        TypeAdapter<Integer> intAdapter = factory.create(gson, TypeToken.get(Integer.class));
        assertNull(intAdapter);
    }

    @Test(timeout = 4000)
    public void testNewFactoryForMultipleTypes() {
        TypeAdapterFactory factory = TypeAdapters.newFactoryForMultipleTypes(Calendar.class, GregorianCalendar.class, TypeAdapters.CALENDAR);
        Gson gson = new Gson();
        TypeAdapter<Calendar> calAdapter = factory.create(gson, TypeToken.get(Calendar.class));
        assertNotNull(calAdapter);
        assertSame(TypeAdapters.CALENDAR, calAdapter);

        TypeAdapter<GregorianCalendar> gcAdapter = factory.create(gson, TypeToken.get(GregorianCalendar.class));
        assertNotNull(gcAdapter);
        assertSame(TypeAdapters.CALENDAR, gcAdapter);

        // Non-matching type
        TypeAdapter<String> strAdapter = factory.create(gson, TypeToken.get(String.class));
        assertNull(strAdapter);
    }

    @Test(timeout = 4000)
    public void testNewTypeHierarchyFactory() {
        TypeAdapterFactory factory = TypeAdapters.newTypeHierarchyFactory(Number.class, TypeAdapters.NUMBER);
        Gson gson = new Gson();
        TypeAdapter<Number> numAdapter = factory.create(gson, TypeToken.get(Number.class));
        assertNotNull(numAdapter);

        TypeAdapter<Integer> intAdapter = factory.create(gson, TypeToken.get(Integer.class));
        assertNotNull(intAdapter);

        TypeAdapter<String> strAdapter = factory.create(gson, TypeToken.get(String.class));
        assertNull(strAdapter);
    }

    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====

    @Test(timeout = 4000)
    public void testByteBoundaryValues() throws IOException {
        // Max byte
        JsonReader jr = new JsonReader(new StringReader("127"));
        assertEquals(Byte.MAX_VALUE, TypeAdapters.BYTE.read(jr).byteValue());

        // Min byte
        jr = new JsonReader(new StringReader("-128"));
        assertEquals(Byte.MIN_VALUE, TypeAdapters.BYTE.read(jr).byteValue());

        // Overflow
        jr = new JsonReader(new StringReader("128"));
        try {
            TypeAdapters.BYTE.read(jr);
            fail("Expected NumberFormatException for byte overflow");
        } catch (NumberFormatException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testShortBoundaryValues() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("32767"));
        assertEquals(Short.MAX_VALUE, TypeAdapters.SHORT.read(jr).shortValue());

        jr = new JsonReader(new StringReader("-32768"));
        assertEquals(Short.MIN_VALUE, TypeAdapters.SHORT.read(jr).shortValue());

        jr = new JsonReader(new StringReader("32768"));
        try {
            TypeAdapters.SHORT.read(jr);
            fail("Expected NumberFormatException for short overflow");
        } catch (NumberFormatException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testIntegerBoundaryValues() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("2147483647"));
        assertEquals(Integer.MAX_VALUE, TypeAdapters.INTEGER.read(jr).intValue());

        jr = new JsonReader(new StringReader("-2147483648"));
        assertEquals(Integer.MIN_VALUE, TypeAdapters.INTEGER.read(jr).intValue());

        jr = new JsonReader(new StringReader("2147483648"));
        try {
            TypeAdapters.INTEGER.read(jr);
            fail("Expected NumberFormatException for integer overflow");
        } catch (NumberFormatException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testLongBoundaryValues() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("9223372036854775807"));
        assertEquals(Long.MAX_VALUE, TypeAdapters.LONG.read(jr).longValue());

        jr = new JsonReader(new StringReader("-9223372036854775808"));
        assertEquals(Long.MIN_VALUE, TypeAdapters.LONG.read(jr).longValue());

        jr = new JsonReader(new StringReader("9223372036854775808"));
        try {
            TypeAdapters.LONG.read(jr);
            fail("Expected NumberFormatException for long overflow");
        } catch (NumberFormatException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testFloatBoundaryValues() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("3.4028235E38"));
        assertEquals(Float.MAX_VALUE, TypeAdapters.FLOAT.read(jr).floatValue(), 0.0f);

        jr = new JsonReader(new StringReader("1.4E-45"));
        assertEquals(Float.MIN_VALUE, TypeAdapters.FLOAT.read(jr).floatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testDoubleBoundaryValues() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("1.7976931348623157E308"));
        assertEquals(Double.MAX_VALUE, TypeAdapters.DOUBLE.read(jr).doubleValue(), 0.0d);

        jr = new JsonReader(new StringReader("4.9E-324"));
        assertEquals(Double.MIN_VALUE, TypeAdapters.DOUBLE.read(jr).doubleValue(), 0.0d);
    }

    @Test(timeout = 4000)
    public void testNumberBoundaryValues() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("2147483647"));
        assertEquals(2147483647, TypeAdapters.NUMBER.read(jr).intValue());

        jr = new JsonReader(new StringReader("9223372036854775807"));
        assertEquals(9223372036854775807L, TypeAdapters.NUMBER.read(jr).longValue());

        jr = new JsonReader(new StringReader("3.14"));
        assertEquals(3.14, TypeAdapters.NUMBER.read(jr).doubleValue(), 0.0001);
    }

    @Test(timeout = 4000)
    public void testStringEmptyValue() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"\""));
        assertEquals("", TypeAdapters.STRING.read(jr));

        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.STRING.write(jw, "");
        jw.flush();
        assertEquals("\"\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testBigDecimalBoundaryValues() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("0"));
        assertEquals(new BigDecimal("0"), TypeAdapters.BIG_DECIMAL.read(jr));

        jr = new JsonReader(new StringReader("-0"));
        assertEquals(new BigDecimal("0"), TypeAdapters.BIG_DECIMAL.read(jr));

        jr = new JsonReader(new StringReader("1E+10"));
        assertEquals(new BigDecimal("1E+10"), TypeAdapters.BIG_DECIMAL.read(jr));
    }

    @Test(timeout = 4000)
    public void testBigIntegerBoundaryValues() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("0"));
        assertEquals(new BigInteger("0"), TypeAdapters.BIG_INTEGER.read(jr));

        jr = new JsonReader(new StringReader("-0"));
        assertEquals(new BigInteger("0"), TypeAdapters.BIG_INTEGER.read(jr));

        jr = new JsonReader(new StringReader("123456789012345678901234567890"));
        assertEquals(new BigInteger("123456789012345678901234567890"), TypeAdapters.BIG_INTEGER.read(jr));
    }

    @Test(timeout = 4000)
    public void testCharacterBoundaryValues() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"\\u0000\""));
        assertEquals('\u0000', TypeAdapters.CHARACTER.read(jr).charValue());

        jr = new JsonReader(new StringReader("\"\\uffff\""));
        assertEquals('\uffff', TypeAdapters.CHARACTER.read(jr).charValue());
    }

    @Test(timeout = 4000)
    public void testLocaleBoundaryValues() throws IOException {
        // Empty string
        JsonReader jr = new JsonReader(new StringReader("\"\""));
        Locale locale = TypeAdapters.LOCALE.read(jr);
        assertNotNull(locale);
        assertEquals("", locale.getLanguage());

        // Single underscore
        jr = new JsonReader(new StringReader("\"_\""));
        locale = TypeAdapters.LOCALE.read(jr);
        assertNotNull(locale);

        // Double underscore
        jr = new JsonReader(new StringReader("\"__\""));
        locale = TypeAdapters.LOCALE.read(jr);
        assertNotNull(locale);
    }

    @Test(timeout = 4000)
    public void testBitSetBoundaryValues() throws IOException {
        // Empty array
        JsonReader jr = new JsonReader(new StringReader("[]"));
        BitSet bitset = TypeAdapters.BIT_SET.read(jr);
        assertNotNull(bitset);
        assertEquals(0, bitset.length());

        // Single element
        jr = new JsonReader(new StringReader("[true]"));
        bitset = TypeAdapters.BIT_SET.read(jr);
        assertTrue(bitset.get(0));

        // All true
        jr = new JsonReader(new StringReader("[true,true,true]"));
        bitset = TypeAdapters.BIT_SET.read(jr);
        assertTrue(bitset.get(0));
        assertTrue(bitset.get(1));
        assertTrue(bitset.get(2));
    }

    @Test(timeout = 4000)
    public void testAtomicIntegerArrayBoundaryValues() throws IOException {
        // Empty array
        JsonReader jr = new JsonReader(new StringReader("[]"));
        AtomicIntegerArray array = TypeAdapters.ATOMIC_INTEGER_ARRAY.read(jr);
        assertEquals(0, array.length());

        // Single element
        jr = new JsonReader(new StringReader("[42]"));
        array = TypeAdapters.ATOMIC_INTEGER_ARRAY.read(jr);
        assertEquals(1, array.length());
        assertEquals(42, array.get(0));

        // Negative values
        jr = new JsonReader(new StringReader("[-1,-2]"));
        array = TypeAdapters.ATOMIC_INTEGER_ARRAY.read(jr);
        assertEquals(2, array.length());
        assertEquals(-1, array.get(0));
        assertEquals(-2, array.get(1));
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    @Test(timeout = 4000)
    public void testBoxedBooleansDefect() throws IOException {
        // This test directly targets the known defect in JsonWriterTest::testBoxedBooleans
        // The BOOLEAN adapter must correctly serialize boxed Boolean values
        
        // Test Boolean.TRUE
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.BOOLEAN.write(jw, Boolean.TRUE);
        jw.flush();
        assertEquals("true", sw.toString());
        
        // Test Boolean.FALSE
        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.BOOLEAN.write(jw, Boolean.FALSE);
        jw.flush();
        assertEquals("false", sw.toString());
        
        // Test null Boolean
        sw = new StringWriter();
        jw = new JsonWriter(sw);
        TypeAdapters.BOOLEAN.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());
        
        // Test round-trip
        JsonReader jr = new JsonReader(new StringReader("true"));
        assertEquals(Boolean.TRUE, TypeAdapters.BOOLEAN.read(jr));
        
        jr = new JsonReader(new StringReader("false"));
        assertEquals(Boolean.FALSE, TypeAdapters.BOOLEAN.read(jr));
        
        // Test string coercion
        jr = new JsonReader(new StringReader("\"true\""));
        assertEquals(Boolean.TRUE, TypeAdapters.BOOLEAN.read(jr));
        
        jr = new JsonReader(new StringReader("\"false\""));
        assertEquals(Boolean.FALSE, TypeAdapters.BOOLEAN.read(jr));
        
        // Test invalid value
        jr = new JsonReader(new StringReader("\"invalid\""));
        try {
            TypeAdapters.BOOLEAN.read(jr);
            fail("Expected JsonSyntaxException for invalid boolean string");
        } catch (JsonSyntaxException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testBooleanReadInvalidToken() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("123"));
        try {
            TypeAdapters.BOOLEAN.read(jr);
            fail("Expected JsonSyntaxException for non-boolean token");
        } catch (JsonSyntaxException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testBooleanReadInvalidString() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"not-a-boolean\""));
        try {
            TypeAdapters.BOOLEAN.read(jr);
            fail("Expected JsonSyntaxException for invalid boolean string");
        } catch (JsonSyntaxException expected) {
            // Expected
        }
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testClassWriteNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        TypeAdapters.CLASS.write(jw, null);
        jw.flush();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testClassWriteNonNull() throws IOException {
        StringWriter sw = new StringWriter();
        JsonWriter jw = new JsonWriter(sw);
        try {
            TypeAdapters.CLASS.write(jw, String.class);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testClassReadNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("null"));
        assertNull(TypeAdapters.CLASS.read(jr));
    }

    @Test(timeout = 4000)
    public void testClassReadNonNull() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"java.lang.String\""));
        try {
            TypeAdapters.CLASS.read(jr);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testBitSetReadNumberFormatException() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("[abc]"));
        try {
            TypeAdapters.BIT_SET.read(jr);
            fail("Expected JsonSyntaxException for invalid number");
        } catch (JsonSyntaxException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testUrlReadInvalid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"invalid url\""));
        try {
            TypeAdapters.URL.read(jr);
            fail("Expected IOException for invalid URL");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testInetAddressReadInvalid() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("\"invalid-address\""));
        try {
            TypeAdapters.INET_ADDRESS.read(jr);
            fail("Expected IOException for invalid InetAddress");
        } catch (IOException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testEnumFactoryReadInvalid() throws IOException {
        Gson gson = new Gson();
        TypeAdapter<TestEnum> adapter = TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(TestEnum.class));
        JsonReader jr = new JsonReader(new StringReader("\"INVALID\""));
        try {
            adapter.read(jr);
            fail("Expected JsonSyntaxException for invalid enum value");
        } catch (JsonSyntaxException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testJsonElementReadInvalidToken() throws IOException {
        JsonReader jr = new JsonReader(new StringReader("invalid"));
        try {
            TypeAdapters.JSON_ELEMENT.read(jr);
            fail("Expected JsonSyntaxException for invalid JSON");
        } catch (JsonSyntaxException expected) {
            // Expected
        }
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testTypeAdaptersConstructorPrivate() {
        try {
            TypeAdapters.class.getDeclaredConstructor().newInstance();
            fail("Expected IllegalAccessException for private constructor");
        } catch (Exception expected) {
            // Expected - constructor is private
        }
    }

    @Test(timeout = 4000)
    public void testFactoryConstants() {
        assertNotNull(TypeAdapters.CLASS_FACTORY);
        assertNotNull(TypeAdapters.BIT_SET_FACTORY);
        assertNotNull(TypeAdapters.NUMBER_FACTORY);
        assertNotNull(TypeAdapters.URL_FACTORY);
        assertNotNull(TypeAdapters.URI_FACTORY);
        assertNotNull(TypeAdapters.UUID_FACTORY);
        assertNotNull(TypeAdapters.CURRENCY_FACTORY);
        assertNotNull(TypeAdapters.TIMESTAMP_FACTORY);
        assertNotNull(TypeAdapters.CALENDAR_FACTORY);
        assertNotNull(TypeAdapters.LOCALE_FACTORY);
        assertNotNull(TypeAdapters.ENUM_FACTORY);
    }

    @Test(timeout = 4000)
    public void testAdapterConstants() {
        assertNotNull(TypeAdapters.CLASS);
        assertNotNull(TypeAdapters.BIT_SET);
        assertNotNull(TypeAdapters.BOOLEAN);
        assertNotNull(TypeAdapters.BOOLEAN_AS_STRING);
        assertNotNull(TypeAdapters.BYTE);
        assertNotNull(TypeAdapters.SHORT);
        assertNotNull(TypeAdapters.INTEGER);
        assertNotNull(TypeAdapters.ATOMIC_INTEGER);
        assertNotNull(TypeAdapters.ATOMIC_BOOLEAN);
        assertNotNull(TypeAdapters.ATOMIC_INTEGER_ARRAY);
        assertNotNull(TypeAdapters.LONG);
        assertNotNull(TypeAdapters.FLOAT);
        assertNotNull(TypeAdapters.DOUBLE);
        assertNotNull(TypeAdapters.NUMBER);
        assertNotNull(TypeAdapters.CHARACTER);
        assertNotNull(TypeAdapters.STRING);
        assertNotNull(TypeAdapters.BIG_DECIMAL);
        assertNotNull(TypeAdapters.BIG_INTEGER);
        assertNotNull(TypeAdapters.STRING_BUILDER);
        assertNotNull(TypeAdapters.STRING_BUFFER);
        assertNotNull(TypeAdapters.URL);
        assertNotNull(TypeAdapters.URI);
        assertNotNull(TypeAdapters.INET_ADDRESS);
        assertNotNull(TypeAdapters.UUID);
        assertNotNull(TypeAdapters.CURRENCY);
        assertNotNull(TypeAdapters.CALENDAR);
        assertNotNull(TypeAdapters.LOCALE);
        assertNotNull(TypeAdapters.JSON_ELEMENT);
    }

    // Helper enum for testing
    private enum TestEnum {
        VALUE1, VALUE2
    }

    // Helper enum with SerializedName
    private enum TestEnumSerializedName {
        @SerializedName("first_value")
        FIRST,
        @SerializedName("second_value")
        SECOND
    }
}