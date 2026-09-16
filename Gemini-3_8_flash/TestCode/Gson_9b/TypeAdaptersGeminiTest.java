/* [Branch & Defect Analysis Matrix]
 * Target: com.google.gson.internal.bind.TypeAdapters
 *
 * Decision / Condition Coverage Targets:
 * 1. Constructor: TypeAdapters() private invocation via reflection throws UnsupportedOperationException.
 * 2. CLASS Adapter: null read/write vs non-null read/write (UnsupportedOperationException).
 * 3. BIT_SET Adapter:
 *    - read: null token, empty array, token NUMBER (0 vs !=0), token BOOLEAN (true/false),
 *            token STRING (valid integer vs NumberFormatException -> JsonSyntaxException),
 *            default token -> JsonSyntaxException.
 *    - write: null vs BitSet (empty, single bit, multiple bits).
 * 4. BOOLEAN & BOOLEAN_AS_STRING Adapters:
 *    - BOOLEAN read: null, JsonToken.STRING (Boolean.parseBoolean), JsonToken.BOOLEAN.
 *    - BOOLEAN write: null handling (defect-targeted: boxed boolean null vs true/false), value(Boolean).
 *    - BOOLEAN_AS_STRING read: null vs string; write: null vs boolean.toString().
 * 5. Numerical Adapters (BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, NUMBER, BIG_DECIMAL, BIG_INTEGER):
 *    - read: null token vs valid value vs invalid token/format -> JsonSyntaxException.
 *    - write: null vs values.
 *    - LazilyParsedNumber generation in NUMBER adapter.
 * 6. ATOMIC_* Adapters (ATOMIC_INTEGER, ATOMIC_BOOLEAN, ATOMIC_INTEGER_ARRAY):
 *    - null-safe behavior, successful read/write, arrays with nested values & errors.
 * 7. CHARACTER & STRING Adapters:
 *    - CHARACTER: null, single char, multichar string -> JsonSyntaxException.
 *    - STRING: null, boolean coercion (backwards compat), string.
 * 8. STRING_BUILDER, STRING_BUFFER, URL, URI, INET_ADDRESS, UUID, CURRENCY:
 *    - URL: null token, literal "null", valid URL, MalformedURLException.
 *    - URI: null token, literal "null", valid URI, URISyntaxException -> JsonIOException.
 *    - INET_ADDRESS: null, hostname/IP resolution, write host address.
 *    - CURRENCY: valid ISO currency codes, nullSafe.
 * 9. TIMESTAMP_FACTORY & CALENDAR:
 *    - TIMESTAMP_FACTORY: non-Timestamp TypeToken returns null; Timestamp read/write delegation to Date.
 *    - CALENDAR: null token, field mapping (year, month, dayOfMonth, hourOfDay, minute, second), unknown fields.
 * 10. LOCALE Adapter:
 *     - tokens: language, language_country, language_country_variant, null token.
 * 11. JSON_ELEMENT Adapter:
 *     - read: STRING, NUMBER, BOOLEAN, NULL, BEGIN_ARRAY, BEGIN_OBJECT, default -> IllegalArgumentException.
 *     - write: null/JsonNull, JsonPrimitive (number, boolean, string), JsonArray, JsonObject, custom subclass -> IAE.
 * 12. ENUM_FACTORY & EnumTypeAdapter:
 *     - non-enum rejection, Enum.class rejection, enum with @SerializedName & alternate names.
 *     - anonymous enum subclasses (constant-specific class body).
 * 13. Factory Helper Methods:
 *     - newFactory(TypeToken, TypeAdapter), newFactory(Class, TypeAdapter),
 *       newFactory(unboxed, boxed, TypeAdapter), newFactoryForMultipleTypes(base, sub, TypeAdapter),
 *       newTypeHierarchyFactory(Class, TypeAdapter) including type-check assertion branch in read().
 *     - Factory toString() representations.
 */

package com.google.gson.internal.bind;

import static org.junit.Assert.*;

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
import java.util.Currency;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.junit.Test;

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
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class TypeAdaptersGeminiTest {

  private static JsonReader reader(String json) {
    return new JsonReader(new StringReader(json));
  }

  private static String toJson(TypeAdapter<Object> adapter, Object value) throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.setLenient(true);
    adapter.write(writer, value);
    return out.toString();
  }

  private enum SampleEnum {
    @SerializedName(value = "FIRST", alternate = {"1st", "uno"})
    FIRST,
    SECOND {
      @Override
      public String toString() {
        return "custom_second";
      }
    }
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBitSetAdapterNormal() throws Exception {
    BitSet bitSet = new BitSet();
    bitSet.set(0);
    bitSet.set(2);
    String json = TypeAdapters.BIT_SET.toJson(bitSet);
    assertEquals("[1,0,1]", json);

    BitSet parsed = TypeAdapters.BIT_SET.fromJson("[1,0,1]");
    assertTrue(parsed.get(0));
    assertFalse(parsed.get(1));
    assertTrue(parsed.get(2));
    assertEquals(3, parsed.length());

    // Reading mixed numeric and boolean values in BitSet
    BitSet parsedMixed = TypeAdapters.BIT_SET.fromJson("[true, 0, \"1\", false]");
    assertTrue(parsedMixed.get(0));
    assertFalse(parsedMixed.get(1));
    assertTrue(parsedMixed.get(2));
    assertFalse(parsedMixed.get(3));
  }

  @Test(timeout = 4000)
  public void testBooleanAndBooleanAsStringAdapters() throws Exception {
    // BOOLEAN adapter
    assertEquals("true", TypeAdapters.BOOLEAN.toJson(Boolean.TRUE));
    assertEquals("false", TypeAdapters.BOOLEAN.toJson(Boolean.FALSE));
    assertEquals(Boolean.TRUE, TypeAdapters.BOOLEAN.fromJson("true"));
    assertEquals(Boolean.FALSE, TypeAdapters.BOOLEAN.fromJson("false"));
    assertEquals(Boolean.TRUE, TypeAdapters.BOOLEAN.fromJson("\"true\""));
    assertEquals(Boolean.FALSE, TypeAdapters.BOOLEAN.fromJson("\"false\""));

    // BOOLEAN_AS_STRING adapter
    assertEquals("\"true\"", TypeAdapters.BOOLEAN_AS_STRING.toJson(Boolean.TRUE));
    assertEquals("\"false\"", TypeAdapters.BOOLEAN_AS_STRING.toJson(Boolean.FALSE));
    assertEquals(Boolean.TRUE, TypeAdapters.BOOLEAN_AS_STRING.fromJson("\"true\""));
    assertEquals(Boolean.FALSE, TypeAdapters.BOOLEAN_AS_STRING.fromJson("\"false\""));
  }

  @Test(timeout = 4000)
  public void testNumericalAdapters() throws Exception {
    assertEquals(Byte.valueOf((byte) 42), TypeAdapters.BYTE.fromJson("42"));
    assertEquals("42", TypeAdapters.BYTE.toJson((byte) 42));

    assertEquals(Short.valueOf((short) 1234), TypeAdapters.SHORT.fromJson("1234"));
    assertEquals("1234", TypeAdapters.SHORT.toJson((short) 1234));

    assertEquals(Integer.valueOf(123456), TypeAdapters.INTEGER.fromJson("123456"));
    assertEquals("123456", TypeAdapters.INTEGER.toJson(123456));

    assertEquals(Long.valueOf(123456789012L), TypeAdapters.LONG.fromJson("123456789012"));
    assertEquals("123456789012", TypeAdapters.LONG.toJson(123456789012L));

    assertEquals(Float.valueOf(3.14f), TypeAdapters.FLOAT.fromJson("3.14"));
    assertEquals("3.14", TypeAdapters.FLOAT.toJson(3.14f));

    assertEquals(Double.valueOf(2.71828), TypeAdapters.DOUBLE.fromJson("2.71828"));
    assertEquals("2.71828", TypeAdapters.DOUBLE.toJson(2.71828));

    Number number = TypeAdapters.NUMBER.fromJson("99999999999999999999.99");
    assertEquals("99999999999999999999.99", number.toString());
    assertEquals("99", TypeAdapters.NUMBER.toJson(99));

    BigDecimal bigDecimal = new BigDecimal("123456789.987654321");
    assertEquals(bigDecimal, TypeAdapters.BIG_DECIMAL.fromJson("123456789.987654321"));
    assertEquals("123456789.987654321", TypeAdapters.BIG_DECIMAL.toJson(bigDecimal));

    BigInteger bigInteger = new BigInteger("98765432109876543210");
    assertEquals(bigInteger, TypeAdapters.BIG_INTEGER.fromJson("98765432109876543210"));
    assertEquals("98765432109876543210", TypeAdapters.BIG_INTEGER.toJson(bigInteger));
  }

  @Test(timeout = 4000)
  public void testAtomicAdapters() throws Exception {
    AtomicInteger atomicInt = TypeAdapters.ATOMIC_INTEGER.fromJson("55");
    assertNotNull(atomicInt);
    assertEquals(55, atomicInt.get());
    assertEquals("55", TypeAdapters.ATOMIC_INTEGER.toJson(new AtomicInteger(55)));

    AtomicBoolean atomicBool = TypeAdapters.ATOMIC_BOOLEAN.fromJson("true");
    assertNotNull(atomicBool);
    assertTrue(atomicBool.get());
    assertEquals("true", TypeAdapters.ATOMIC_BOOLEAN.toJson(new AtomicBoolean(true)));

    AtomicIntegerArray atomicArray = TypeAdapters.ATOMIC_INTEGER_ARRAY.fromJson("[1, 2, 3]");
    assertNotNull(atomicArray);
    assertEquals(3, atomicArray.length());
    assertEquals(1, atomicArray.get(0));
    assertEquals(2, atomicArray.get(1));
    assertEquals(3, atomicArray.get(2));
    assertEquals("[1,2,3]", TypeAdapters.ATOMIC_INTEGER_ARRAY.toJson(atomicArray));
  }

  @Test(timeout = 4000)
  public void testCharacterAndStringAdapters() throws Exception {
    assertEquals(Character.valueOf('a'), TypeAdapters.CHARACTER.fromJson("\"a\""));
    assertEquals("\"z\"", TypeAdapters.CHARACTER.toJson('z'));

    assertEquals("hello", TypeAdapters.STRING.fromJson("\"hello\""));
    assertEquals("true", TypeAdapters.STRING.fromJson("true"));
    assertEquals("\"world\"", TypeAdapters.STRING.toJson("world"));

    StringBuilder sb = TypeAdapters.STRING_BUILDER.fromJson("\"testBuilder\"");
    assertNotNull(sb);
    assertEquals("testBuilder", sb.toString());
    assertEquals("\"abc\"", TypeAdapters.STRING_BUILDER.toJson(new StringBuilder("abc")));

    StringBuffer sbuf = TypeAdapters.STRING_BUFFER.fromJson("\"testBuffer\"");
    assertNotNull(sbuf);
    assertEquals("testBuffer", sbuf.toString());
    assertEquals("\"xyz\"", TypeAdapters.STRING_BUFFER.toJson(new StringBuffer("xyz")));
  }

  @Test(timeout = 4000)
  public void testNetworkAndIdentifierAdapters() throws Exception {
    URL url = TypeAdapters.URL.fromJson("\"http://google.com\"");
    assertNotNull(url);
    assertEquals("http://google.com", url.toExternalForm());
    assertEquals("\"http://google.com\"", TypeAdapters.URL.toJson(url));

    URI uri = TypeAdapters.URI.fromJson("\"http://google.com/path?q=1\"");
    assertNotNull(uri);
    assertEquals("http://google.com/path?q=1", uri.toString());
    assertEquals("\"http://google.com/path?q=1\"", TypeAdapters.URI.toJson(uri));

    InetAddress address = TypeAdapters.INET_ADDRESS.fromJson("\"127.0.0.1\"");
    assertNotNull(address);
    assertEquals("127.0.0.1", address.getHostAddress());
    assertEquals("\"127.0.0.1\"", TypeAdapters.INET_ADDRESS.toJson(address));

    UUID uuid = UUID.randomUUID();
    UUID parsedUuid = TypeAdapters.UUID.fromJson("\"" + uuid.toString() + "\"");
    assertEquals(uuid, parsedUuid);
    assertEquals("\"" + uuid.toString() + "\"", TypeAdapters.UUID.toJson(uuid));

    Currency currency = TypeAdapters.CURRENCY.fromJson("\"USD\"");
    assertNotNull(currency);
    assertEquals("USD", currency.getCurrencyCode());
    assertEquals("\"USD\"", TypeAdapters.CURRENCY.toJson(currency));
  }

  @Test(timeout = 4000)
  public void testCalendarAdapter() throws Exception {
    Calendar cal = new GregorianCalendar(2023, Calendar.MARCH, 15, 10, 20, 30);
    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
    String json = TypeAdapters.CALENDAR.toJson(cal);
    assertTrue(json.contains("\"year\":2023"));
    assertTrue(json.contains("\"month\":2"));
    assertTrue(json.contains("\"dayOfMonth\":15"));
    assertTrue(json.contains("\"hourOfDay\":10"));
    assertTrue(json.contains("\"minute\":20"));
    assertTrue(json.contains("\"second\":30"));

    Calendar readCal = TypeAdapters.CALENDAR.fromJson(
        "{\"year\":2021,\"month\":11,\"dayOfMonth\":25,\"hourOfDay\":18,\"minute\":45,\"second\":50}"
    );
    assertEquals(2021, readCal.get(Calendar.YEAR));
    assertEquals(11, readCal.get(Calendar.MONTH));
    assertEquals(25, readCal.get(Calendar.DAY_OF_MONTH));
    assertEquals(18, readCal.get(Calendar.HOUR_OF_DAY));
    assertEquals(45, readCal.get(Calendar.MINUTE));
    assertEquals(50, readCal.get(Calendar.SECOND));
  }

  @Test(timeout = 4000)
  public void testLocaleAdapter() throws Exception {
    Locale l1 = TypeAdapters.LOCALE.fromJson("\"en\"");
    assertEquals(new Locale("en"), l1);

    Locale l2 = TypeAdapters.LOCALE.fromJson("\"en_US\"");
    assertEquals(new Locale("en", "US"), l2);

    Locale l3 = TypeAdapters.LOCALE.fromJson("\"en_US_POSIX\"");
    assertEquals(new Locale("en", "US", "POSIX"), l3);

    assertEquals("\"en_US\"", TypeAdapters.LOCALE.toJson(Locale.US));
  }

  @Test(timeout = 4000)
  public void testJsonElementAdapter() throws Exception {
    // Primitives
    assertEquals(new JsonPrimitive("test"), TypeAdapters.JSON_ELEMENT.fromJson("\"test\""));
    assertEquals(new JsonPrimitive(123), TypeAdapters.JSON_ELEMENT.fromJson("123"));
    assertEquals(new JsonPrimitive(true), TypeAdapters.JSON_ELEMENT.fromJson("true"));
    assertEquals(JsonNull.INSTANCE, TypeAdapters.JSON_ELEMENT.fromJson("null"));

    // Array & Object
    JsonElement arrayElement = TypeAdapters.JSON_ELEMENT.fromJson("[1,\"a\",false,null]");
    assertTrue(arrayElement.isJsonArray());
    JsonArray array = arrayElement.getAsJsonArray();
    assertEquals(4, array.size());

    JsonElement objElement = TypeAdapters.JSON_ELEMENT.fromJson("{\"k\":\"v\",\"num\":10}");
    assertTrue(objElement.isJsonObject());
    JsonObject obj = objElement.getAsJsonObject();
    assertEquals("v", obj.get("k").getAsString());
    assertEquals(10, obj.get("num").getAsInt());

    // Write back
    assertEquals("[1,\"a\",false,null]", TypeAdapters.JSON_ELEMENT.toJson(array));
    assertEquals("{\"k\":\"v\",\"num\":10}", TypeAdapters.JSON_ELEMENT.toJson(obj));
  }

  @Test(timeout = 4000)
  public void testEnumAdapter() throws Exception {
    TypeAdapter<SampleEnum> adapter = new Gson().getAdapter(SampleEnum.class);
    assertEquals("\"FIRST\"", adapter.toJson(SampleEnum.FIRST));
    assertEquals(SampleEnum.FIRST, adapter.fromJson("\"FIRST\""));
    assertEquals(SampleEnum.FIRST, adapter.fromJson("\"1st\""));
    assertEquals(SampleEnum.FIRST, adapter.fromJson("\"uno\""));

    // Enum with subclass (anonymous constant-specific class body)
    assertEquals("\"SECOND\"", adapter.toJson(SampleEnum.SECOND));
    assertEquals(SampleEnum.SECOND, adapter.fromJson("\"SECOND\""));
  }

  @Test(timeout = 4000)
  public void testTimestampFactory() throws Exception {
    Gson gson = new Gson();
    TypeAdapter<Timestamp> adapter = TypeAdapters.TIMESTAMP_FACTORY.create(gson, TypeToken.get(Timestamp.class));
    assertNotNull(adapter);

    long time = 1672531199000L;
    Timestamp ts = new Timestamp(time);
    String json = adapter.toJson(ts);
    Timestamp readTs = adapter.fromJson(json);
    assertEquals(ts.getTime(), readTs.getTime());

    // Non-Timestamp type returns null factory adapter
    assertNull(TypeAdapters.TIMESTAMP_FACTORY.create(gson, TypeToken.get(String.class)));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testNullHandlingAcrossAdapters() throws Exception {
    assertEquals("null", TypeAdapters.BIT_SET.toJson(null));
    assertNull(TypeAdapters.BIT_SET.fromJson("null"));

    assertEquals("null", TypeAdapters.BOOLEAN.toJson(null));
    assertNull(TypeAdapters.BOOLEAN.fromJson("null"));

    assertEquals("\"null\"", TypeAdapters.BOOLEAN_AS_STRING.toJson(null));
    assertNull(TypeAdapters.BOOLEAN_AS_STRING.fromJson("null"));

    assertEquals("null", TypeAdapters.BYTE.toJson(null));
    assertNull(TypeAdapters.BYTE.fromJson("null"));

    assertEquals("null", TypeAdapters.SHORT.toJson(null));
    assertNull(TypeAdapters.SHORT.fromJson("null"));

    assertEquals("null", TypeAdapters.INTEGER.toJson(null));
    assertNull(TypeAdapters.INTEGER.fromJson("null"));

    assertEquals("null", TypeAdapters.ATOMIC_INTEGER.toJson(null));
    assertNull(TypeAdapters.ATOMIC_INTEGER.fromJson("null"));

    assertEquals("null", TypeAdapters.ATOMIC_BOOLEAN.toJson(null));
    assertNull(TypeAdapters.ATOMIC_BOOLEAN.fromJson("null"));

    assertEquals("null", TypeAdapters.ATOMIC_INTEGER_ARRAY.toJson(null));
    assertNull(TypeAdapters.ATOMIC_INTEGER_ARRAY.fromJson("null"));

    assertEquals("null", TypeAdapters.LONG.toJson(null));
    assertNull(TypeAdapters.LONG.fromJson("null"));

    assertEquals("null", TypeAdapters.FLOAT.toJson(null));
    assertNull(TypeAdapters.FLOAT.fromJson("null"));

    assertEquals("null", TypeAdapters.DOUBLE.toJson(null));
    assertNull(TypeAdapters.DOUBLE.fromJson("null"));

    assertEquals("null", TypeAdapters.NUMBER.toJson(null));
    assertNull(TypeAdapters.NUMBER.fromJson("null"));

    assertEquals("null", TypeAdapters.CHARACTER.toJson(null));
    assertNull(TypeAdapters.CHARACTER.fromJson("null"));

    assertEquals("null", TypeAdapters.STRING.toJson(null));
    assertNull(TypeAdapters.STRING.fromJson("null"));

    assertEquals("null", TypeAdapters.BIG_DECIMAL.toJson(null));
    assertNull(TypeAdapters.BIG_DECIMAL.fromJson("null"));

    assertEquals("null", TypeAdapters.BIG_INTEGER.toJson(null));
    assertNull(TypeAdapters.BIG_INTEGER.fromJson("null"));

    assertEquals("null", TypeAdapters.STRING_BUILDER.toJson(null));
    assertNull(TypeAdapters.STRING_BUILDER.fromJson("null"));

    assertEquals("null", TypeAdapters.STRING_BUFFER.toJson(null));
    assertNull(TypeAdapters.STRING_BUFFER.fromJson("null"));

    assertEquals("null", TypeAdapters.URL.toJson(null));
    assertNull(TypeAdapters.URL.fromJson("null"));
    assertNull(TypeAdapters.URL.fromJson("\"null\""));

    assertEquals("null", TypeAdapters.URI.toJson(null));
    assertNull(TypeAdapters.URI.fromJson("null"));
    assertNull(TypeAdapters.URI.fromJson("\"null\""));

    assertEquals("null", TypeAdapters.INET_ADDRESS.toJson(null));
    assertNull(TypeAdapters.INET_ADDRESS.fromJson("null"));

    assertEquals("null", TypeAdapters.UUID.toJson(null));
    assertNull(TypeAdapters.UUID.fromJson("null"));

    assertEquals("null", TypeAdapters.CURRENCY.toJson(null));
    assertNull(TypeAdapters.CURRENCY.fromJson("null"));

    assertEquals("null", TypeAdapters.CALENDAR.toJson(null));
    assertNull(TypeAdapters.CALENDAR.fromJson("null"));

    assertEquals("null", TypeAdapters.LOCALE.toJson(null));
    assertNull(TypeAdapters.LOCALE.fromJson("null"));

    assertEquals("null", TypeAdapters.JSON_ELEMENT.toJson(null));
    assertEquals(JsonNull.INSTANCE, TypeAdapters.JSON_ELEMENT.fromJson("null"));
  }

  @Test(timeout = 4000)
  public void testEmptyAndBoundaryBitSet() throws Exception {
    BitSet empty = new BitSet();
    assertEquals("[]", TypeAdapters.BIT_SET.toJson(empty));
    BitSet readEmpty = TypeAdapters.BIT_SET.fromJson("[]");
    assertEquals(0, readEmpty.length());
  }

  @Test(timeout = 4000)
  public void testEmptyAtomicIntegerArray() throws Exception {
    AtomicIntegerArray empty = new AtomicIntegerArray(0);
    assertEquals("[]", TypeAdapters.ATOMIC_INTEGER_ARRAY.toJson(empty));
    AtomicIntegerArray readEmpty = TypeAdapters.ATOMIC_INTEGER_ARRAY.fromJson("[]");
    assertEquals(0, readEmpty.length());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (JsonWriter boxed booleans defect)
  // =========================================================================

  @Test(timeout = 4000)
  public void testBoxedBooleans() throws Exception {
    // Tests JsonWriter directly and via TypeAdapters.BOOLEAN
    // Target defect: JsonWriterTest::testBoxedBooleans NullPointerException when handling boxed Booleans
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    writer.beginArray();
    writer.value((Boolean) true);
    writer.value((Boolean) false);
    writer.value((Boolean) null);
    writer.endArray();
    assertEquals("[true,false,null]", out.toString());

    // Also assert through the TypeAdapter write path
    StringWriter outAdapter = new StringWriter();
    JsonWriter adapterWriter = new JsonWriter(outAdapter);
    adapterWriter.beginArray();
    TypeAdapters.BOOLEAN.write(adapterWriter, Boolean.TRUE);
    TypeAdapters.BOOLEAN.write(adapterWriter, Boolean.FALSE);
    TypeAdapters.BOOLEAN.write(adapterWriter, null);
    adapterWriter.endArray();
    assertEquals("[true,false,null]", outAdapter.toString());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testPrivateConstructorInvocationThrows() throws Throwable {
    Constructor<TypeAdapters> constructor = TypeAdapters.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    try {
      constructor.newInstance();
      fail("Expected UnsupportedOperationException");
    } catch (InvocationTargetException e) {
      assertTrue(e.getCause() instanceof UnsupportedOperationException);
    }
  }

  @Test(expected = UnsupportedOperationException.class, timeout = 4000)
  public void testClassAdapterWriteThrows() throws Exception {
    TypeAdapters.CLASS.toJson(String.class);
  }

  @Test(timeout = 4000)
  public void testClassAdapterReadNullAndNonNull() throws Exception {
    assertNull(TypeAdapters.CLASS.fromJson("null"));
    try {
      TypeAdapters.CLASS.fromJson("\"java.lang.String\"");
      fail("Expected UnsupportedOperationException");
    } catch (UnsupportedOperationException expected) {
      // Success
    }
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testBitSetInvalidStringValue() throws Exception {
    TypeAdapters.BIT_SET.fromJson("[\"invalid\"]");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testBitSetInvalidTokenType() throws Exception {
    TypeAdapters.BIT_SET.fromJson("[{}]");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testByteInvalidTokenThrows() throws Exception {
    TypeAdapters.BYTE.fromJson("\"not_a_number\"");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testShortInvalidTokenThrows() throws Exception {
    TypeAdapters.SHORT.fromJson("\"not_a_short\"");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testIntegerInvalidTokenThrows() throws Exception {
    TypeAdapters.INTEGER.fromJson("\"not_an_int\"");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testLongInvalidTokenThrows() throws Exception {
    TypeAdapters.LONG.fromJson("\"not_a_long\"");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testAtomicIntegerInvalidTokenThrows() throws Exception {
    TypeAdapters.ATOMIC_INTEGER.fromJson("\"not_an_int\"");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testAtomicIntegerArrayInvalidTokenThrows() throws Exception {
    TypeAdapters.ATOMIC_INTEGER_ARRAY.fromJson("[1, \"not_an_int\"]");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testNumberAdapterUnexpectedTokenThrows() throws Exception {
    TypeAdapters.NUMBER.fromJson("true");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testCharacterMultiCharStringThrows() throws Exception {
    TypeAdapters.CHARACTER.fromJson("\"multi\"");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testBigDecimalInvalidFormatThrows() throws Exception {
    TypeAdapters.BIG_DECIMAL.fromJson("\"bad_decimal\"");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testBigIntegerInvalidFormatThrows() throws Exception {
    TypeAdapters.BIG_INTEGER.fromJson("\"bad_integer\"");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testUrlMalformedThrows() throws Exception {
    TypeAdapters.URL.fromJson("\"bad_protocol://foo\"");
  }

  @Test(expected = JsonIOException.class, timeout = 4000)
  public void testUriMalformedThrows() throws Exception {
    TypeAdapters.URI.fromJson("\"http://bad uri with spaces\"");
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testJsonElementWriteUnsupportedSubclass() throws Exception {
    JsonElement customElement = new JsonElement() {
      @Override
      public JsonElement deepCopy() {
        return this;
      }
    };
    TypeAdapters.JSON_ELEMENT.toJson(customElement);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testJsonElementReadEndDocumentThrows() throws Exception {
    JsonReader jsonReader = reader("");
    TypeAdapters.JSON_ELEMENT.read(jsonReader);
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Factories & Factory Contracts
  // =========================================================================

  @Test(timeout = 4000)
  public void testNewFactoryTypeToken() {
    TypeToken<String> token = TypeToken.get(String.class);
    TypeAdapterFactory factory = TypeAdapters.newFactory(token, TypeAdapters.STRING);
    Gson gson = new Gson();

    assertNotNull(factory.create(gson, token));
    assertNull(factory.create(gson, TypeToken.get(Integer.class)));
  }

  @Test(timeout = 4000)
  public void testNewFactoryClass() {
    TypeAdapterFactory factory = TypeAdapters.newFactory(String.class, TypeAdapters.STRING);
    Gson gson = new Gson();

    assertNotNull(factory.create(gson, TypeToken.get(String.class)));
    assertNull(factory.create(gson, TypeToken.get(Integer.class)));
    assertTrue(factory.toString().contains("Factory[type=java.lang.String"));
  }

  @Test(timeout = 4000)
  public void testNewFactoryUnboxedBoxed() {
    TypeAdapterFactory factory = TypeAdapters.newFactory(int.class, Integer.class, TypeAdapters.INTEGER);
    Gson gson = new Gson();

    assertNotNull(factory.create(gson, TypeToken.get(int.class)));
    assertNotNull(factory.create(gson, TypeToken.get(Integer.class)));
    assertNull(factory.create(gson, TypeToken.get(Long.class)));
    assertTrue(factory.toString().contains("Factory[type=java.lang.Integer+int"));
  }

  @Test(timeout = 4000)
  public void testNewFactoryForMultipleTypes() {
    TypeAdapterFactory factory = TypeAdapters.newFactoryForMultipleTypes(
        Calendar.class, GregorianCalendar.class, TypeAdapters.CALENDAR
    );
    Gson gson = new Gson();

    assertNotNull(factory.create(gson, TypeToken.get(Calendar.class)));
    assertNotNull(factory.create(gson, TypeToken.get(GregorianCalendar.class)));
    assertNull(factory.create(gson, TypeToken.get(String.class)));
    assertTrue(factory.toString().contains("Factory[type=java.util.Calendar+java.util.GregorianCalendar"));
  }

  @Test(timeout = 4000)
  public void testNewTypeHierarchyFactory() throws Exception {
    TypeAdapterFactory factory = TypeAdapters.newTypeHierarchyFactory(Number.class, TypeAdapters.NUMBER);
    Gson gson = new Gson();

    TypeAdapter<Long> longAdapter = factory.create(gson, TypeToken.get(Long.class));
    assertNotNull(longAdapter);
    assertNull(factory.create(gson, TypeToken.get(String.class)));
    assertTrue(factory.toString().contains("Factory[typeHierarchy=java.lang.Number"));

    // Subtype instance check validation in read()
    try {
      // Reading "123" with longAdapter yields LazilyParsedNumber which is not an instance of Long
      longAdapter.fromJson("123");
      fail("Expected JsonSyntaxException because LazilyParsedNumber is not a Long");
    } catch (JsonSyntaxException expected) {
      assertTrue(expected.getMessage().contains("Expected a java.lang.Long but was"));
    }
  }

  @Test(timeout = 4000)
  public void testEnumFactoryEdgeCases() {
    Gson gson = new Gson();
    // Non-enum classes must be ignored
    assertNull(TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(String.class)));
    // Raw Enum.class must be ignored
    assertNull(TypeAdapters.ENUM_FACTORY.create(gson, TypeToken.get(Enum.class)));
  }

  @Test(timeout = 4000)
  public void testStaticFactoryFields() {
    assertNotNull(TypeAdapters.CLASS_FACTORY);
    assertNotNull(TypeAdapters.BIT_SET_FACTORY);
    assertNotNull(TypeAdapters.BOOLEAN_FACTORY);
    assertNotNull(TypeAdapters.BYTE_FACTORY);
    assertNotNull(TypeAdapters.SHORT_FACTORY);
    assertNotNull(TypeAdapters.INTEGER_FACTORY);
    assertNotNull(TypeAdapters.ATOMIC_INTEGER_FACTORY);
    assertNotNull(TypeAdapters.ATOMIC_BOOLEAN_FACTORY);
    assertNotNull(TypeAdapters.ATOMIC_INTEGER_ARRAY_FACTORY);
    assertNotNull(TypeAdapters.NUMBER_FACTORY);
    assertNotNull(TypeAdapters.CHARACTER_FACTORY);
    assertNotNull(TypeAdapters.STRING_FACTORY);
    assertNotNull(TypeAdapters.STRING_BUILDER_FACTORY);
    assertNotNull(TypeAdapters.STRING_BUFFER_FACTORY);
    assertNotNull(TypeAdapters.URL_FACTORY);
    assertNotNull(TypeAdapters.URI_FACTORY);
    assertNotNull(TypeAdapters.INET_ADDRESS_FACTORY);
    assertNotNull(TypeAdapters.UUID_FACTORY);
    assertNotNull(TypeAdapters.CURRENCY_FACTORY);
    assertNotNull(TypeAdapters.CALENDAR_FACTORY);
    assertNotNull(TypeAdapters.LOCALE_FACTORY);
    assertNotNull(TypeAdapters.JSON_ELEMENT_FACTORY);
    assertNotNull(TypeAdapters.ENUM_FACTORY);
  }
}