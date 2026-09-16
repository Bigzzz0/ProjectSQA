package com.google.gson;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: com.google.gson.DefaultDateTypeAdapter
 * Defect Target (Defects4J):
 *   - testUnexpectedToken: Defective version throws JsonParseException instead of JsonSyntaxException
 *                          when in.peek() != JsonToken.STRING (e.g., object token '{').
 *   - testNullValue: Defective version checks in.peek() != JsonToken.STRING unconditionally, throwing
 *                    JsonParseException on JsonToken.NULL instead of consuming null and returning null.
 *
 * Decision / Branch Coverage Map:
 *   1. Constructors:
 *      - dateType validation branch: (dateType != Date.class && dateType != java.sql.Date.class
 *                                    && dateType != Timestamp.class) -> true/false
 *      - Constructor variants: (dateType), (dateType, pattern), (dateType, style),
 *                              (dateStyle, timeStyle), (dateType, dateStyle, timeStyle),
 *                              (dateType, enUsFormat, localFormat).
 *   2. write(JsonWriter, Date):
 *      - Branch value == null -> out.nullValue()
 *      - Branch value != null -> format date via enUsFormat in synchronized(localFormat)
 *   3. read(JsonReader):
 *      - Branch in.peek() == JsonToken.NULL (Bug Target: expected null return)
 *      - Branch in.peek() != JsonToken.STRING (Bug Target: expected JsonSyntaxException)
 *      - Branch deserializeToDate: localFormat succeeds, localFormat fails -> enUsFormat succeeds,
 *                                  enUsFormat fails -> ISO8601Utils succeeds, all fail -> JsonSyntaxException
 *      - Branch dateType == Date.class -> returns java.util.Date
 *      - Branch dateType == Timestamp.class -> returns java.sql.Timestamp
 *      - Branch dateType == java.sql.Date.class -> returns java.sql.Date
 *   4. toString():
 *      - String representation matching "DefaultDateTypeAdapter(<FormatClassName>)"
 * ====================================================================================================
 */
public class DefaultDateTypeAdapterGeminiTest {

  // ==================================================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testDateSerializationAndDeserializationWithDefaultConstructor() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    Date originalDate = new Date(1577836800000L); // 2020-01-01 00:00:00 UTC (second-aligned)
    String json = adapter.toJson(originalDate);

    Date deserialized = adapter.fromJson(json);
    assertNotNull(deserialized);
    assertEquals(Date.class, deserialized.getClass());
    assertEquals(originalDate.getTime() / 1000, deserialized.getTime() / 1000);
  }

  @Test(timeout = 4000)
  public void testTimestampSerializationAndDeserialization() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class, "yyyy-MM-dd HH:mm:ss");
    Timestamp original = new Timestamp(1577836800000L);
    String json = adapter.toJson(original);

    Date deserialized = adapter.fromJson(json);
    assertNotNull(deserialized);
    assertEquals(Timestamp.class, deserialized.getClass());
    assertEquals(original.getTime(), deserialized.getTime());
  }

  @Test(timeout = 4000)
  public void testSqlDateSerializationAndDeserialization() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class, "yyyy-MM-dd");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    Date parsedDate = sdf.parse("2021-05-15");
    java.sql.Date original = new java.sql.Date(parsedDate.getTime());
    String json = adapter.toJson(original);

    Date deserialized = adapter.fromJson(json);
    assertNotNull(deserialized);
    assertEquals(java.sql.Date.class, deserialized.getClass());
    assertEquals(original.toString(), deserialized.toString());
  }

  @Test(timeout = 4000)
  public void testConstructorWithDateStyle() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, DateFormat.SHORT);
    Date original = new Date(1577836800000L);
    String json = adapter.toJson(original);
    Date deserialized = adapter.fromJson(json);
    assertNotNull(deserialized);
    assertEquals(Date.class, deserialized.getClass());
  }

  @Test(timeout = 4000)
  public void testConstructorWithDateAndStyles() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(DateFormat.SHORT, DateFormat.SHORT);
    Date original = new Date(1577836800000L);
    String json = adapter.toJson(original);
    Date deserialized = adapter.fromJson(json);
    assertNotNull(deserialized);
    assertEquals(Date.class, deserialized.getClass());
  }

  @Test(timeout = 4000)
  public void testConstructorWithTypeAndStyles() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class, DateFormat.MEDIUM, DateFormat.SHORT);
    Timestamp original = new Timestamp(1577836800000L);
    String json = adapter.toJson(original);
    Date deserialized = adapter.fromJson(json);
    assertNotNull(deserialized);
    assertEquals(Timestamp.class, deserialized.getClass());
  }

  @Test(timeout = 4000)
  public void testIso8601ParsingFallback() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    // Standard ISO8601 format that fails standard SimpleDateFormat but succeeds in ISO8601Utils
    String iso8601String = "\"2020-01-01T12:34:56.789Z\"";
    Date deserialized = adapter.fromJson(iso8601String);
    assertNotNull(deserialized);
    assertEquals(Date.class, deserialized.getClass());
  }

  @Test(timeout = 4000)
  public void testEnUsFormatFallback() throws Exception {
    Locale originalLocale = Locale.getDefault();
    try {
      Locale.setDefault(Locale.FRANCE);
      DateFormat enUs = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US);
      DateFormat local = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.FRANCE);
      DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, enUs, local);

      Date now = new Date(1577836800000L);
      String enUsFormatted = enUs.format(now);
      Date parsed = adapter.fromJson("\"" + enUsFormatted + "\"");
      assertNotNull(parsed);
      assertEquals(now.getTime() / 1000, parsed.getTime() / 1000);
    } finally {
      Locale.setDefault(originalLocale);
    }
  }

  // ==================================================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testWriteNullValue() throws IOException {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    StringWriter out = new StringWriter();
    JsonWriter writer = new JsonWriter(out);
    adapter.write(writer, null);
    assertEquals("null", out.toString());
  }

  @Test(timeout = 4000)
  public void testEpochZeroDate() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd HH:mm:ss");
    Date epoch = new Date(0L);
    String json = adapter.toJson(epoch);
    Date deserialized = adapter.fromJson(json);
    assertNotNull(deserialized);
    assertEquals(0L, deserialized.getTime());
  }

  // ==================================================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testNullValue() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    // Defect: Defective implementation fails with JsonParseException: The date should be a string value
    // Fixed implementation properly inspects JsonToken.NULL, invokes in.nextNull(), and returns null
    Date result = adapter.fromJson("null");
    assertNull(result);
  }

  @Test(timeout = 4000)
  public void testNullValueTimestamp() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Timestamp.class);
    Date result = adapter.fromJson("null");
    assertNull(result);
  }

  @Test(timeout = 4000)
  public void testNullValueSqlDate() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(java.sql.Date.class);
    Date result = adapter.fromJson("null");
    assertNull(result);
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testUnexpectedToken() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    // Defect: Defective implementation throws JsonParseException directly rather than JsonSyntaxException
    adapter.fromJson("{}");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testUnexpectedBooleanToken() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    adapter.fromJson("true");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testUnexpectedNumberToken() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    adapter.fromJson("12345");
  }

  // ==================================================================================================
  // Partition D: Exception & Defensive Guard Paths
  // ==================================================================================================

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testUnsupportedDateSubclassThrowsIllegalArgumentException() {
    // java.sql.Time is an unhandled subclass of java.util.Date
    new DefaultDateTypeAdapter(java.sql.Time.class);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testCustomDateSubclassThrowsIllegalArgumentException() {
    class CustomDate extends Date {}
    new DefaultDateTypeAdapter(CustomDate.class);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testNullDateTypeThrowsIllegalArgumentException() {
    new DefaultDateTypeAdapter(null);
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testInvalidDateFormatStringThrowsJsonSyntaxException() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    adapter.fromJson("\"invalid-date-not-parseable\"");
  }

  @Test(expected = JsonSyntaxException.class, timeout = 4000)
  public void testUnparseableGarbageStringThrowsJsonSyntaxException() throws Exception {
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class);
    adapter.fromJson("\"foo bar baz 99999\"");
  }

  // ==================================================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testToStringRepresentation() {
    DefaultDateTypeAdapter adapterPattern = new DefaultDateTypeAdapter(Date.class, "yyyy-MM-dd");
    String str = adapterPattern.toString();
    assertEquals("DefaultDateTypeAdapter(SimpleDateFormat)", str);
  }

  @Test(timeout = 4000)
  public void testCustomDateFormatInstanceToString() {
    DateFormat df = DateFormat.getDateInstance(DateFormat.FULL, Locale.US);
    DefaultDateTypeAdapter adapter = new DefaultDateTypeAdapter(Date.class, df, df);
    String str = adapter.toString();
    assertEquals("DefaultDateTypeAdapter(" + df.getClass().getSimpleName() + ")", str);
  }
}