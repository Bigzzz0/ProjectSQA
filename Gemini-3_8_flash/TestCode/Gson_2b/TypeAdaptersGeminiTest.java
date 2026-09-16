/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: com.google.gson.internal.bind.TypeAdapters
 *
 * 1. Defect Targeting (Ground Truth - Defects4J / Gson):
 *    - DefaultTypeAdaptersTest::testJsonElementTypeMismatch
 *    - Root Cause: newTypeHierarchyFactory(JsonElement.class, JSON_ELEMENT) creates an adapter for any
 *      subtype of JsonElement without a runtime instance check against the requested subtype (e.g.,
 *      requesting JsonObject when JSON contains a primitive string). On defective code, this throws a
 *      ClassCastException when attempting to cast the returned JsonPrimitive to JsonObject instead of
 *      raising a proper JsonSyntaxException.
 *
 * 2. Decision Branches & Condition Coverage:
 *    - CLASS: null read/write vs. non-null write/read (UnsupportedOperationException branch).
 *    - BIT_SET: null check, empty bitset, tokens (NUMBER 0/1, BOOLEAN true/false, STRING "0"/"1",
 *      malformed STRING -> JsonSyntaxException, invalid token type -> JsonSyntaxException).
 *    - BOOLEAN & BOOLEAN_AS_STRING: null checks, STRING tokens (GSON 1.7 compatibility), boolean tokens,
 *      write as boolean literal vs. string representation.
 *    - BYTE, SHORT, INTEGER, LONG: null checks, valid parsing, NumberFormatException -> JsonSyntaxException.
 *    - FLOAT, DOUBLE, NUMBER: null checks, LazilyParsedNumber path, unexpected token -> JsonSyntaxException.
 *    - CHARACTER: null checks, length == 1 vs. length != 1 (JsonSyntaxException), writing null / char.
 *    - STRING: null check, boolean-to-string coercion branch, string literal branch.
 *    - BIG_DECIMAL & BIG_INTEGER: null checks, valid string parsing, NumberFormatException -> JsonSyntaxException.
 *    - STRING_BUILDER & STRING_BUFFER: null read/write, normal read/write.
 *    - URL & URI: null tokens, "null" string literal branch, valid URL/URI, URISyntaxException -> JsonIOException.
 *    - INET_ADDRESS: null read/write, valid address resolution, host address serialization.
 *    - UUID: null read/write, valid UUID parsing and serialization.
 *    - TIMESTAMP_FACTORY: non-Timestamp rawType check (returns null), Timestamp read/write delegation.
 *    - CALENDAR & GREGORIAN_CALENDAR: null checks, field matching (YEAR, MONTH, DAY_OF_MONTH, HOUR_OF_DAY,
 *      MINUTE, SECOND), out-of-order/partial fields, write object structure.
 *    - LOCALE: null check, 1-segment ("en"), 2-segment ("en_US"), 3-segment ("en_US_WIN") localization branches.
 *    - JSON_ELEMENT: read (STRING, NUMBER, BOOLEAN, NULL, BEGIN_ARRAY, BEGIN_OBJECT, default -> IllegalArgumentException),
 *      write (null/JsonNull, primitive number/boolean/string, array, object, custom anonymous subclass -> IllegalArgumentException).
 *    - ENUM_FACTORY & EnumTypeAdapter: null read/write, normal enum, @SerializedName value, @SerializedName alternates,
 *      anonymous enum constants with method overrides (class is not enum, rawType.isEnum() false -> superclass branch).
 *    - Factory Helpers: toString() invocations, type matching and mismatching on all factory variations.
 */

package com.google.gson.internal.bind;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.sql.Timestamp;
import java.util.BitSet;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;

import static org.junit.Assert.*;

public class TypeAdaptersGeminiTest {

  private final Gson gson = new Gson();

  // -------------------------------------------------------------------------
  // Helper Methods for I/O
  // -------------------------------------------------------------------------
  private static JsonReader reader(String json) {
    return new JsonReader(new StringReader(json));
  }

  private static String write(TypeAdapter adapter, Object value) throws IOException {
    StringWriter sw = new StringWriter();
    JsonWriter jw = new JsonWriter(sw);
    adapter.write(jw, value);
    return sw.toString();
  }

  // -------------------------------------------------------------------------
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // -------------------------------------------------------------------------

  /**
   * Targets com.google.gson.functional.DefaultTypeAdaptersTest::testJsonElementTypeMismatch
   * When deserializing a primitive string into JsonObject.class, JsonSyntaxException should be thrown.
   * In the defective version, ClassCastException: Cannot cast JsonPrimitive to JsonObject occurs.
   */
  @Test(timeout = 4000)
  public void testJsonElementTypeMismatch_PrimitiveExpectedObject() {
    try {
      gson.fromJson("\"abc\"", JsonObject.class);
      fail("Expected JsonSyntaxException when attempting to deserialize JsonPrimitive as JsonObject");
    } catch (JsonSyntaxException expected) {
      // Expected behavior on fixed version
    }
  }

  @Test(timeout = 4000)
  public void testJsonElementTypeMismatch_PrimitiveExpectedArray() {
    try {
      gson.fromJson("123", JsonArray.class);
      fail("Expected JsonSyntaxException when attempting to deserialize JsonPrimitive as JsonArray");
    } catch (JsonSyntaxException expected) {
      // Expected behavior on fixed version
    }
  }

  // -------------------------------------------------------------------------
  // Partition A: Core Functional Logic & State Transitions
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testPrivateConstructor() throws Exception {
    Constructor<TypeAdapters> constructor = TypeAdapters.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
      fail("Expected UnsupportedOperationException");
    } catch (InvocationTargetException e) {
      assertTrue(e.getCause() instanceof UnsupportedOperationException);
    }
  }

  @Test(timeout = 4000)
  public void testClassAdapter() throws IOException {
    // Null handling
    assertEquals("null", write(TypeAdapters.CLASS, null));
    assertNull(TypeAdapters.CLASS.read(reader("null")));

    // Unsupported write
    try {
      write(TypeAdapters.CLASS, String.class);
      fail();
    } catch (UnsupportedOperationException expected) {
      assertTrue(expected.getMessage().contains("java.lang.Class"));
    }

    // Unsupported read
    try {
      TypeAdapters.CLASS.read(reader("\"java.lang.String\""));
      fail();
    } catch (UnsupportedOperationException expected) {
      assertTrue(expected.getMessage().contains("java.lang.Class"));
    }
  }

  @Test(timeout = 4000)
  public void testBitSetAdapter() throws IOException {
    assertNull(TypeAdapters.BIT_SET.read(reader("null")));
    assertEquals("null", write(TypeAdapters.BIT_SET, null));

    BitSet empty = new BitSet();
    assertEquals("[]", write(TypeAdapters.BIT_SET, empty));
    BitSet readEmpty = TypeAdapters.BIT_SET.read(reader("[]"));
    assertTrue(readEmpty.isEmpty());

    BitSet bits = new BitSet();
    bits.set(0);
    bits.set(2);
    bits.set(3); // [1, 0, 1, 1]
    assertEquals("[1,0,1,1]", write(TypeAdapters.BIT_SET, bits));

    // Read mixed tokens: number (0, 1), boolean (true, false), string ("0", "1")
    BitSet readBits = TypeAdapters.BIT_SET.read(reader("[1, 0, true, false, \"1\", \"0\"]"));
    assertTrue(readBits.get(0));
    assertFalse(readBits.get(1));
    assertTrue(readBits.get(2));
    assertFalse(readBits.get(3));
    assertTrue(readBits.get(4));
    assertFalse(readBits.get(5));
    assertEquals(5, readBits.length());
  }

  @Test(timeout = 4000)
  public void testBitSetInvalidTokens() throws IOException {
    try {
      TypeAdapters.BIT_SET.read(reader("[\"not_a_number\"]"));
      fail();
    } catch (JsonSyntaxException expected) {
      assertTrue(expected.getMessage().contains("bitset number value"));
    }

    try {
      TypeAdapters.BIT_SET.read(reader("[{}]"));
      fail();
    } catch (JsonSyntaxException expected) {
      assertTrue(expected.getMessage().contains("Invalid bitset value type"));
    }
  }

  @Test(timeout = 4000)
  public void testBooleanAdapter() throws IOException {
    assertNull(TypeAdapters.BOOLEAN.read(reader("null")));
    assertEquals("null", write(TypeAdapters.BOOLEAN, null));

    assertTrue(TypeAdapters.BOOLEAN.read(reader("true")));
    assertFalse(TypeAdapters.BOOLEAN.read(reader("false")));
    assertTrue(TypeAdapters.BOOLEAN.read(reader("\"true\"")));
    assertFalse(TypeAdapters.BOOLEAN.read(reader("\"not_true\"")));

    assertEquals("true", write(TypeAdapters.BOOLEAN, true));
    assertEquals("false", write(TypeAdapters.BOOLEAN, false));
  }

  @Test(timeout = 4000)
  public void testBooleanAsStringAdapter() throws IOException {
    assertNull(TypeAdapters.BOOLEAN_AS_STRING.read(reader("null")));
    assertEquals("\"null\"", write(TypeAdapters.BOOLEAN_AS_STRING, null));

    assertTrue(TypeAdapters.BOOLEAN_AS_STRING.read(reader("\"true\"")));
    assertFalse(TypeAdapters.BOOLEAN_AS_STRING.read(reader("\"false\"")));
    assertFalse(TypeAdapters.BOOLEAN_AS_STRING.read(reader("\"random\"")));

    assertEquals("\"true\"", write(TypeAdapters.BOOLEAN_AS_STRING, true));
    assertEquals("\"false\"", write(TypeAdapters.BOOLEAN_AS_STRING, false));
  }

  @Test(timeout = 4000)
  public void testByteAdapter() throws IOException {
    assertNull(TypeAdapters.BYTE.read(reader("null")));
    assertEquals((byte) 42, TypeAdapters.BYTE.read(reader("42")).byteValue());
    assertEquals((byte) -12, TypeAdapters.BYTE.read(reader("-12")).byteValue());
    assertEquals("50", write(TypeAdapters.BYTE, (byte) 50));

    try {
      TypeAdapters.BYTE.read(reader("\"invalid\""));
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test(timeout = 4000)
  public void testShortAdapter() throws IOException {
    assertNull(TypeAdapters.SHORT.read(reader("null")));
    assertEquals((short) 1024, TypeAdapters.SHORT.read(reader("1024")).shortValue());
    assertEquals((short) -300, TypeAdapters.SHORT.read(reader("-300")).shortValue());
    assertEquals("100", write(TypeAdapters.SHORT, (short) 100));

    try {
      TypeAdapters.SHORT.read(reader("\"invalid\""));
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test(timeout = 4000)
  public void testIntegerAdapter() throws IOException {
    assertNull(TypeAdapters.INTEGER.read(reader("null")));
    assertEquals(123456, TypeAdapters.INTEGER.read(reader("123456")).intValue());
    assertEquals(-654321, TypeAdapters.INTEGER.read(reader("-654321")).intValue());
    assertEquals("999", write(TypeAdapters.INTEGER, 999));

    try {
      TypeAdapters.INTEGER.read(reader("\"invalid\""));
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test(timeout = 4000)
  public void testLongAdapter() throws IOException {
    assertNull(TypeAdapters.LONG.read(reader("null")));
    assertEquals(9876543210123L, TypeAdapters.LONG.read(reader("9876543210123")).longValue());
    assertEquals("12345", write(TypeAdapters.LONG, 12345L));

    try {
      TypeAdapters.LONG.read(reader("\"invalid\""));
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test(timeout = 4000)
  public void testFloatAdapter() throws IOException {
    assertNull(TypeAdapters.FLOAT.read(reader("null")));
    assertEquals(3.14f, TypeAdapters.FLOAT.read(reader("3.14")).floatValue(), 0.0001f);
    assertEquals("3.14", write(TypeAdapters.FLOAT, 3.14f));
  }

  @Test(timeout = 4000)
  public void testDoubleAdapter() throws IOException {
    assertNull(TypeAdapters.DOUBLE.read(reader("null")));
    assertEquals(2.71828, TypeAdapters.DOUBLE.read(reader("2.71828")).doubleValue(), 0.000001);
    assertEquals("2.71828", write(TypeAdapters.DOUBLE, 2.71828));
  }

  @Test(timeout = 4000)
  public void testNumberAdapter() throws IOException {
    assertNull(TypeAdapters.NUMBER.read(reader("null")));
    Number num = TypeAdapters.NUMBER.read(reader("123.456"));
    assertEquals(123.456, num.doubleValue(), 0.001);
    assertEquals("789", write(TypeAdapters.NUMBER, 789));

    try {
      TypeAdapters.NUMBER.read(reader("\"not_a_number\""));
      fail();
    } catch (JsonSyntaxException expected) {
      assertTrue(expected.getMessage().contains("Expecting number"));
    }
  }

  @Test(timeout = 4000)
  public void testCharacterAdapter() throws IOException {
    assertNull(TypeAdapters.CHARACTER.read(reader("null")));
    assertEquals("null", write(TypeAdapters.CHARACTER, null));

    assertEquals(Character.valueOf('A'), TypeAdapters.CHARACTER.read(reader("\"A\"")));
    assertEquals("\"Z\"", write(TypeAdapters.CHARACTER, 'Z'));

    try {
      TypeAdapters.CHARACTER.read(reader("\"multi\""));
      fail();
    } catch (JsonSyntaxException expected) {
      assertTrue(expected.getMessage().contains("Expecting character"));
    }
  }

  @Test(timeout = 4000)
  public void testStringAdapter() throws IOException {
    assertNull(TypeAdapters.STRING.read(reader("null")));
    assertEquals("hello", TypeAdapters.STRING.read(reader("\"hello\"")));
    assertEquals("true", TypeAdapters.STRING.read(reader("true")));
    assertEquals("false", TypeAdapters.STRING.read(reader("false")));
    assertEquals("\"world\"", write(TypeAdapters.STRING, "world"));
  }

  @Test(timeout = 4000)
  public void testBigDecimalAdapter() throws IOException {
    assertNull(TypeAdapters.BIG_DECIMAL.read(reader("null")));
    BigDecimal bd = new BigDecimal("123456789.987654321");
    assertEquals(bd, TypeAdapters.BIG_DECIMAL.read(reader("\"123456789.987654321\"")));
    assertEquals("123456789.987654321", write(TypeAdapters.BIG_DECIMAL, bd));

    try {
      TypeAdapters.BIG_DECIMAL.read(reader("\"invalid\""));
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test(timeout = 4000)
  public void testBigIntegerAdapter() throws IOException {
    assertNull(TypeAdapters.BIG_INTEGER.read(reader("null")));
    BigInteger bi = new BigInteger("999999999999999999999999");
    assertEquals(bi, TypeAdapters.BIG_INTEGER.read(reader("\"999999999999999999999999\"")));
    assertEquals("999999999999999999999999", write(TypeAdapters.BIG_INTEGER, bi));

    try {
      TypeAdapters.BIG_INTEGER.read(reader("\"invalid\""));
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

  @Test(timeout = 4000)
  public void testStringBuilderAdapter() throws IOException {
    assertNull(TypeAdapters.STRING_BUILDER.read(reader("null")));
    assertEquals("null", write(TypeAdapters.STRING_BUILDER, null));

    StringBuilder sb = TypeAdapters.STRING_BUILDER.read(reader("\"builder_test\""));
    assertEquals("builder_test", sb.toString());
    assertEquals("\"sample\"", write(TypeAdapters.STRING_BUILDER, new StringBuilder("sample")));
  }

  @Test(timeout = 4000)
  public void testStringBufferAdapter() throws IOException {
    assertNull(TypeAdapters.STRING_BUFFER.read(reader("null")));
    assertEquals("null", write(TypeAdapters.STRING_BUFFER, null));

    StringBuffer sb = TypeAdapters.STRING_BUFFER.read(reader("\"buffer_test\""));
    assertEquals("buffer_test", sb.toString());
    assertEquals("\"sample_buf\"", write(TypeAdapters.STRING_BUFFER, new StringBuffer("sample_buf")));
  }

  @Test(timeout = 4000)
  public void testUrlAdapter() throws IOException {
    assertNull(TypeAdapters.URL.read(reader("null")));
    assertNull(TypeAdapters.URL.read(reader("\"null\"")));
    assertEquals("null", write(TypeAdapters.URL, null));

    URL url = new URL("http://example.com");
    assertEquals(url, TypeAdapters.URL.read(reader("\"http://example.com\"")));
    assertEquals("\"http://example.com\"", write(TypeAdapters.URL, url));
  }

  @Test(timeout = 4000)
  public void testUriAdapter() throws IOException {
    assertNull(TypeAdapters.URI.read(reader("null")));
    assertNull(TypeAdapters.URI.read(reader("\"null\"")));
    assertEquals("null", write(TypeAdapters.URI, null));

    URI uri = URI.create("http://example.com/path?arg=val");
    assertEquals(uri, TypeAdapters.URI.read(reader("\"http://example.com/path?arg=val\"")));
    assertEquals("\"http://example.com/path?arg=val\"", write(TypeAdapters.URI, uri));

    try {
      TypeAdapters.URI.read(reader("\"http://invalid URI with spaces\""));
      fail();
    } catch (JsonIOException expected) {
    }
  }

  @Test(timeout = 4000)
  public void testInetAddressAdapter() throws IOException {
    assertNull(TypeAdapters.INET_ADDRESS.read(reader("null")));
    assertEquals("null", write(TypeAdapters.INET_ADDRESS, null));

    InetAddress addr = InetAddress.getByName("127.0.0.1");
    InetAddress readAddr = TypeAdapters.INET_ADDRESS.read(reader("\"127.0.0.1\""));
    assertEquals(addr.getHostAddress(), readAddr.getHostAddress());
    assertEquals("\"127.0.0.1\"", write(TypeAdapters.INET_ADDRESS, addr));
  }

  @Test(timeout = 4000)
  public void testUuidAdapter() throws IOException {
    assertNull(TypeAdapters.UUID.read(reader("null")));
    assertEquals("null", write(TypeAdapters.UUID, null));

    UUID uuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    assertEquals(uuid, TypeAdapters.UUID.read(reader("\"123e4567-e89b-12d3-a456-426614174000\"")));
    assertEquals("\"123e4567-e89b-12d3-a456-426614174000\"", write(TypeAdapters.UUID, uuid));
  }

  @Test(timeout = 4000)
  public void testTimestampFactory() throws IOException {
    assertNull(TypeAdapters.TIMESTAMP_FACTORY.create(gson, TypeToken.get(String.class)));

    TypeAdapter<Timestamp> adapter = TypeAdapters.TIMESTAMP_FACTORY.create(gson, TypeToken.get(Timestamp.class));
    assertNotNull(adapter);

    Timestamp now = new Timestamp(1000000L);
    String json = write(adapter, now);
    Timestamp read = adapter.read(reader(json));
    assertEquals(now.getTime(), read.getTime());

    assertNull(adapter.read(reader("null")));
  }

  @Test(timeout = 4000)
  public void testCalendarAdapter() throws IOException {
    assertNull(TypeAdapters.CALENDAR.read(reader("null")));
    assertEquals("null", write(TypeAdapters.CALENDAR, null));

    Calendar cal = new GregorianCalendar(2023, 10, 24, 15, 30, 45);
    String json = write(TypeAdapters.CALENDAR, cal);
    assertTrue(json.contains("\"year\":2023"));
    assertTrue(json.contains("\"month\":10"));
    assertTrue(json.contains("\"dayOfMonth\":24"));
    assertTrue(json.contains("\"hourOfDay\":15"));
    assertTrue(json.contains("\"minute\":30"));
    assertTrue(json.contains("\"second\":45"));

    Calendar readCal = TypeAdapters.CALENDAR.read(reader(
        "{\"year\":2022,\"month\":5,\"dayOfMonth\":12,\"hourOfDay\":10,\"minute\":20,\"second\":30,\"ignored\":99}"));
    assertEquals(2022, readCal.get(Calendar.YEAR));
    assertEquals(5, readCal.get(Calendar.MONTH));
    assertEquals(12, readCal.get(Calendar.DAY_OF_MONTH));
    assertEquals(10, readCal.get(Calendar.HOUR_OF_DAY));
    assertEquals(20, readCal.get(Calendar.MINUTE));
    assertEquals(30, readCal.get(Calendar.SECOND));
  }

  @Test(timeout = 4000)
  public void testLocaleAdapter() throws IOException {
    assertNull(TypeAdapters.LOCALE.read(reader("null")));
    assertEquals("null", write(TypeAdapters.LOCALE, null));

    Locale en = TypeAdapters.LOCALE.read(reader("\"en\""));
    assertEquals(new Locale("en"), en);

    Locale enUS = TypeAdapters.LOCALE.read(reader("\"en_US\""));
    assertEquals(new Locale("en", "US"), enUS);

    Locale enUSWin = TypeAdapters.LOCALE.read(reader("\"en_US_WIN\""));
    assertEquals(new Locale("en", "US", "WIN"), enUSWin);

    assertEquals("\"en_US\"", write(TypeAdapters.LOCALE, Locale.US));
  }

  @Test(timeout = 4000)
  public void testJsonElementAdapter_ReadWrite() throws IOException {
    // Null element
    JsonElement nullElem = TypeAdapters.JSON_ELEMENT.read(reader("null"));
    assertTrue(nullElem.isJsonNull());
    assertEquals("null", write(TypeAdapters.JSON_ELEMENT, JsonNull.INSTANCE));
    assertEquals("null", write(TypeAdapters.JSON_ELEMENT, null));

    // String primitive
    JsonElement strElem = TypeAdapters.JSON_ELEMENT.read(reader("\"hello\""));
    assertTrue(strElem.isJsonPrimitive());
    assertEquals("hello", strElem.getAsString());
    assertEquals("\"hello\"", write(TypeAdapters.JSON_ELEMENT, strElem));

    // Number primitive
    JsonElement numElem = TypeAdapters.JSON_ELEMENT.read(reader("123"));
    assertTrue(numElem.isJsonPrimitive());
    assertEquals(123, numElem.getAsInt());
    assertEquals("123", write(TypeAdapters.JSON_ELEMENT, numElem));

    // Boolean primitive
    JsonElement boolElem = TypeAdapters.JSON_ELEMENT.read(reader("true"));
    assertTrue(boolElem.isJsonPrimitive());
    assertTrue(boolElem.getAsBoolean());
    assertEquals("true", write(TypeAdapters.JSON_ELEMENT, boolElem));

    // Array
    JsonElement arrElem = TypeAdapters.JSON_ELEMENT.read(reader("[1,\"a\",false,null]"));
    assertTrue(arrElem.isJsonArray());
    assertEquals(4, arrElem.getAsJsonArray().size());
    assertEquals("[1,\"a\",false,null]", write(TypeAdapters.JSON_ELEMENT, arrElem));

    // Object
    JsonElement objElem = TypeAdapters.JSON_ELEMENT.read(reader("{\"key\":\"value\"}"));
    assertTrue(objElem.isJsonObject());
    assertEquals("value", objElem.getAsJsonObject().get("key").getAsString());
    assertEquals("{\"key\":\"value\"}", write(TypeAdapters.JSON_ELEMENT, objElem));
  }

  @Test(timeout = 4000)
  public void testJsonElementAdapter_UnsupportedWrite() throws IOException {
    JsonElement customElement = new JsonElement() {
      @Override
      public JsonElement deepCopy() {
        return this;
      }
    };
    try {
      write(TypeAdapters.JSON_ELEMENT, customElement);
      fail();
    } catch (IllegalArgumentException expected) {
      assertTrue(expected.getMessage().contains("Couldn't write"));
    }
  }

  // -------------------------------------------------------------------------
  // Partition B & E: Enum Type Adapter & Edge Cases
  // -------------------------------------------------------------------------

  private enum SampleEnum {
    @SerializedName(value = "FIRST", alternate = {"first_alt", "1st"})
    ONE,
    TWO {
      @Override
      public String toString() {
        return "TWO_SUBCLASS";
      }
    }
  }

  @Test(timeout = 4000)
  public void testEnumAdapter() throws IOException {
    TypeAdapter<SampleEnum> adapter = gson.getAdapter(SampleEnum.class);
    assertNotNull(adapter);

    // Serialization
    assertEquals("\"FIRST\"", write(adapter, SampleEnum.ONE));
    assertEquals("\"TWO\"", write(adapter, SampleEnum.TWO));
    assertEquals("null", write(adapter, null));

    // Deserialization with primary and alternate names
    assertNull(adapter.read(reader("null")));
    assertEquals(SampleEnum.ONE, adapter.read(reader("\"FIRST\"")));
    assertEquals(SampleEnum.ONE, adapter.read(reader("\"first_alt\"")));
    assertEquals(SampleEnum.ONE, adapter.read(reader("\"1st\"")));
    assertEquals(SampleEnum.TWO, adapter.read(reader("\"TWO\"")));

    // Anonymous subclass check (TWO overrides toString, creating an anonymous enum subclass)
    TypeAdapter<SampleEnum> subAdapter = gson.getAdapter(TypeToken.get(SampleEnum.TWO.getClass()));
    assertNotNull(subAdapter);
    assertEquals("\"TWO\"", write(subAdapter, SampleEnum.TWO));

    // ENUM_FACTORY create with non-enum class
    assertNull(TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(String.class)));
    // rawType == Enum.class check
    assertNull(TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(Enum.class)));
  }

  // -------------------------------------------------------------------------
  // Partition D: Factory Generators & ToString Assertions
  // -------------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testFactoryTypeToken() {
    TypeToken<String> token = TypeToken.get(String.class);
    TypeAdapterFactory factory = TypeAdapters.newFactory(token, TypeAdapters.STRING);

    assertNotNull(factory.create(gson, token));
    assertNull(factory.create(gson, TypeToken.get(Integer.class)));
  }

  @Test(timeout = 4000)
  public void testFactorySingleClass() {
    TypeAdapterFactory factory = TypeAdapters.newFactory(String.class, TypeAdapters.STRING);

    assertNotNull(factory.create(gson, TypeToken.get(String.class)));
    assertNull(factory.create(gson, TypeToken.get(Integer.class)));
    assertEquals("Factory[type=java.lang.String,adapter=" + TypeAdapters.STRING + "]", factory.toString());
  }

  @Test(timeout = 4000)
  public void testFactoryUnboxedAndBoxed() {
    TypeAdapterFactory factory = TypeAdapters.newFactory(int.class, Integer.class, TypeAdapters.INTEGER);

    assertNotNull(factory.create(gson, TypeToken.get(int.class)));
    assertNotNull(factory.create(gson, TypeToken.get(Integer.class)));
    assertNull(factory.create(gson, TypeToken.get(Long.class)));
    assertEquals("Factory[type=java.lang.Integer+int,adapter=" + TypeAdapters.INTEGER + "]", factory.toString());
  }

  @Test(timeout = 4000)
  public void testFactoryMultipleTypes() {
    TypeAdapterFactory factory = TypeAdapters.newFactoryForMultipleTypes(
        Calendar.class, GregorianCalendar.class, TypeAdapters.CALENDAR);

    assertNotNull(factory.create(gson, TypeToken.get(Calendar.class)));
    assertNotNull(factory.create(gson, TypeToken.get(GregorianCalendar.class)));
    assertNull(factory.create(gson, TypeToken.get(Date.class)));
    assertEquals("Factory[type=java.util.Calendar+java.util.GregorianCalendar,adapter="
        + TypeAdapters.CALENDAR + "]", factory.toString());
  }

  @Test(timeout = 4000)
  public void testTypeHierarchyFactory() {
    TypeAdapterFactory factory = TypeAdapters.newTypeHierarchyFactory(
        Number.class, TypeAdapters.NUMBER);

    assertNotNull(factory.create(gson, TypeToken.get(Number.class)));
    assertNotNull(factory.create(gson, TypeToken.get(Integer.class)));
    assertNull(factory.create(gson, TypeToken.get(String.class)));
    assertEquals("Factory[typeHierarchy=java.lang.Number,adapter=" + TypeAdapters.NUMBER + "]", factory.toString());
  }
}