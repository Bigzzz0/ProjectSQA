package com.fasterxml.jackson.databind.ser.std;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Method: getStdKeySerializer(SerializationConfig, Class<?>, boolean)
 * - Branch 1: rawKeyType == null || rawKeyType == Object.class -> StdKeySerializers.Dynamic
 * - Branch 2: rawKeyType == String.class -> DEFAULT_STRING_SERIALIZER (StringKeySerializer)
 * - Branch 3: rawKeyType.isPrimitive() || Number.class.isAssignableFrom(rawKeyType) -> DEFAULT_KEY_SERIALIZER
 * - Branch 4: rawKeyType == Class.class -> Default(TYPE_CLASS)
 * - Branch 5: Date.class.isAssignableFrom(rawKeyType) -> Default(TYPE_DATE)
 * - Branch 6: Calendar.class.isAssignableFrom(rawKeyType) -> Default(TYPE_CALENDAR)
 * - Branch 7: rawKeyType == UUID.class -> Default(TYPE_TO_STRING)
 * - Branch 8: Fallback -> useDefault ? DEFAULT_KEY_SERIALIZER : null
 *
 * Method: getFallbackKeySerializer(SerializationConfig, Class<?>)
 * - Branch 1: rawKeyType != null && rawKeyType == Enum.class -> Dynamic
 * - Branch 2: rawKeyType != null && rawKeyType.isEnum() -> Default(TYPE_ENUM) [Note: Jackson defect databind#1322]
 * - Branch 3: rawKeyType == null or other -> DEFAULT_KEY_SERIALIZER
 *
 * Inner Classes & Serialization:
 * - Default: switch on _typeId:
 *     - TYPE_DATE: provider.defaultSerializeDateKey((Date) value, g)
 *     - TYPE_CALENDAR: provider.defaultSerializeDateKey(((Calendar) value).getTimeInMillis(), g)
 *     - TYPE_CLASS: g.writeFieldName(((Class<?>) value).getName())
 *     - TYPE_ENUM: WRITE_ENUMS_USING_TO_STRING check, value.toString() vs name()
 *     - TYPE_TO_STRING & default: g.writeFieldName(value.toString())
 * - Dynamic: PropertySerializerMap caching, lookup, _findAndAddDynamic, and readResolve() deserialization.
 * - StringKeySerializer: serialize String key via g.writeFieldName((String) value).
 *
 * Defect Zone (Defects4J / Jackson databind#1322):
 * - TestEnumSerialization::testEnumsWithJsonPropertyAsKey expects `@JsonProperty` annotations on Enum keys
 *   to be respected (e.g. Enum entry A annotated with @JsonProperty("aleph") serialized as "aleph" instead of "A").
 *   StdKeySerializers.Default with TYPE_ENUM only falls back to value.toString() or value.name() and ignores
 *   per-property EnumValues / @JsonProperty annotations.
 */
public class StdKeySerializersGeminiTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory jsonFactory = new JsonFactory();

    // Enum fixture targeting databind#1322
    public enum AnnotatedEnum {
        @JsonProperty("aleph")
        A,
        @JsonProperty("beth")
        B
    }

    public enum PlainEnum {
        FIRST,
        SECOND;

        @Override
        public String toString() {
            return "toString:" + name();
        }
    }

    /*
     * ----------------------------------------------------------------------
     * Partition A: Core Functional Logic & Standard Dispatch
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testGetStdKeySerializerForString() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, String.class, false);
        assertNotNull(ser);
        assertTrue(ser instanceof StdKeySerializers.StringKeySerializer);
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerForPrimitivesAndNumbers() {
        JsonSerializer<Object> serIntPrim = StdKeySerializers.getStdKeySerializer(null, Integer.TYPE, false);
        assertNotNull(serIntPrim);
        assertTrue(serIntPrim instanceof StdKeySerializer);

        JsonSerializer<Object> serLongPrim = StdKeySerializers.getStdKeySerializer(null, Long.TYPE, false);
        assertNotNull(serLongPrim);
        assertTrue(serLongPrim instanceof StdKeySerializer);

        JsonSerializer<Object> serInteger = StdKeySerializers.getStdKeySerializer(null, Integer.class, false);
        assertNotNull(serInteger);
        assertTrue(serInteger instanceof StdKeySerializer);

        JsonSerializer<Object> serDouble = StdKeySerializers.getStdKeySerializer(null, Double.class, false);
        assertNotNull(serDouble);
        assertTrue(serDouble instanceof StdKeySerializer);
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerForClass() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, Class.class, false);
        assertNotNull(ser);
        assertTrue(ser instanceof StdKeySerializers.Default);
        assertEquals(Class.class, ser.handledType());
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerForDateAndSubclasses() {
        JsonSerializer<Object> serDate = StdKeySerializers.getStdKeySerializer(null, Date.class, false);
        assertNotNull(serDate);
        assertTrue(serDate instanceof StdKeySerializers.Default);

        JsonSerializer<Object> serSqlDate = StdKeySerializers.getStdKeySerializer(null, java.sql.Date.class, false);
        assertNotNull(serSqlDate);
        assertTrue(serSqlDate instanceof StdKeySerializers.Default);
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerForCalendar() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, Calendar.class, false);
        assertNotNull(ser);
        assertTrue(ser instanceof StdKeySerializers.Default);
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerForUUID() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, UUID.class, false);
        assertNotNull(ser);
        assertTrue(ser instanceof StdKeySerializers.Default);
    }

    @Test(timeout = 4000)
    public void testGetFallbackKeySerializerEnumTypes() {
        // Raw Enum.class -> Dynamic
        JsonSerializer<Object> serEnumClass = StdKeySerializers.getFallbackKeySerializer(null, Enum.class);
        assertNotNull(serEnumClass);
        assertTrue(serEnumClass instanceof StdKeySerializers.Dynamic);

        // Concrete Enum class -> Default (with TYPE_ENUM)
        JsonSerializer<Object> serConcreteEnum = StdKeySerializers.getFallbackKeySerializer(null, PlainEnum.class);
        assertNotNull(serConcreteEnum);
        assertTrue(serConcreteEnum instanceof StdKeySerializers.Default);
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testGetDefaultDeprecated() {
        JsonSerializer<Object> ser = StdKeySerializers.getDefault();
        assertNotNull(ser);
        assertTrue(ser instanceof StdKeySerializer);
    }

    /*
     * ----------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testGetStdKeySerializerNullAndObject() {
        JsonSerializer<Object> serNull = StdKeySerializers.getStdKeySerializer(null, null, false);
        assertNotNull(serNull);
        assertTrue(serNull instanceof StdKeySerializers.Dynamic);

        JsonSerializer<Object> serObj = StdKeySerializers.getStdKeySerializer(null, Object.class, false);
        assertNotNull(serObj);
        assertTrue(serObj instanceof StdKeySerializers.Dynamic);
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerUnknownType() {
        // Unknown type with useDefault = false -> null
        JsonSerializer<Object> serNoDefault = StdKeySerializers.getStdKeySerializer(null, Object[].class, false);
        assertNull(serNoDefault);

        // Unknown type with useDefault = true -> DEFAULT_KEY_SERIALIZER
        JsonSerializer<Object> serWithDefault = StdKeySerializers.getStdKeySerializer(null, Object[].class, true);
        assertNotNull(serWithDefault);
        assertTrue(serWithDefault instanceof StdKeySerializer);
    }

    @Test(timeout = 4000)
    public void testGetFallbackKeySerializerNullAndNonEnum() {
        JsonSerializer<Object> serNull = StdKeySerializers.getFallbackKeySerializer(null, null);
        assertNotNull(serNull);
        assertTrue(serNull instanceof StdKeySerializer);

        JsonSerializer<Object> serOther = StdKeySerializers.getFallbackKeySerializer(null, Object.class);
        assertNotNull(serOther);
        assertTrue(serOther instanceof StdKeySerializer);
    }

    /*
     * ----------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
     * ----------------------------------------------------------------------
     */

    /**
     * Targets Jackson databind#1322 / TestEnumSerialization::testEnumsWithJsonPropertyAsKey.
     * When an enum constant has a @JsonProperty annotation, it MUST be serialized
     * using the annotation name ("aleph"), NOT enum.name() ("A") or toString().
     */
    @Test(timeout = 4000)
    public void testEnumsWithJsonPropertyAsKey() throws Exception {
        Map<AnnotatedEnum, String> map = new HashMap<AnnotatedEnum, String>();
        map.put(AnnotatedEnum.A, "b");
        String json = mapper.writeValueAsString(map);
        assertEquals("{\"aleph\":\"b\"}", json);
    }

    @Test(timeout = 4000)
    public void testEnumSerializationWithToStringFeature() throws Exception {
        ObjectMapper mapperToString = new ObjectMapper();
        mapperToString.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

        Map<PlainEnum, Integer> map = Collections.singletonMap(PlainEnum.FIRST, 1);
        String json = mapperToString.writeValueAsString(map);
        assertEquals("{\"toString:FIRST\":1}", json);
    }

    @Test(timeout = 4000)
    public void testEnumSerializationWithoutToStringFeature() throws Exception {
        ObjectMapper mapperDefault = new ObjectMapper();
        mapperDefault.disable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);

        Map<PlainEnum, Integer> map = Collections.singletonMap(PlainEnum.SECOND, 2);
        String json = mapperDefault.writeValueAsString(map);
        assertEquals("{\"SECOND\":2}", json);
    }

    /*
     * ----------------------------------------------------------------------
     * Partition D: Inner Serializers Execution (Default, Dynamic, StringKey)
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testDefaultSerializerDateAndCalendar() throws Exception {
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        // 1. TYPE_DATE
        StdKeySerializers.Default dateSer = new StdKeySerializers.Default(StdKeySerializers.Default.TYPE_DATE, Date.class);
        Date testDate = new Date(1500000000000L);
        StringWriter sw1 = new StringWriter();
        JsonGenerator g1 = jsonFactory.createGenerator(sw1);
        g1.writeStartObject();
        dateSer.serialize(testDate, g1, prov);
        g1.writeString("val");
        g1.writeEndObject();
        g1.close();
        assertTrue(sw1.toString().contains("1500000000000") || sw1.toString().contains("2017"));

        // 2. TYPE_CALENDAR
        StdKeySerializers.Default calSer = new StdKeySerializers.Default(StdKeySerializers.Default.TYPE_CALENDAR, Calendar.class);
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(1500000000000L);
        StringWriter sw2 = new StringWriter();
        JsonGenerator g2 = jsonFactory.createGenerator(sw2);
        g2.writeStartObject();
        calSer.serialize(cal, g2, prov);
        g2.writeString("val");
        g2.writeEndObject();
        g2.close();
        assertTrue(sw2.toString().contains("1500000000000") || sw2.toString().contains("2017"));
    }

    @Test(timeout = 4000)
    public void testDefaultSerializerClassAndToString() throws Exception {
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        // 1. TYPE_CLASS
        StdKeySerializers.Default classSer = new StdKeySerializers.Default(StdKeySerializers.Default.TYPE_CLASS, Class.class);
        StringWriter sw1 = new StringWriter();
        JsonGenerator g1 = jsonFactory.createGenerator(sw1);
        g1.writeStartObject();
        classSer.serialize(String.class, g1, prov);
        g1.writeBoolean(true);
        g1.writeEndObject();
        g1.close();
        assertEquals("{\"java.lang.String\":true}", sw1.toString());

        // 2. TYPE_TO_STRING
        StdKeySerializers.Default uuidSer = new StdKeySerializers.Default(StdKeySerializers.Default.TYPE_TO_STRING, UUID.class);
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        StringWriter sw2 = new StringWriter();
        JsonGenerator g2 = jsonFactory.createGenerator(sw2);
        g2.writeStartObject();
        uuidSer.serialize(uuid, g2, prov);
        g2.writeNumber(42);
        g2.writeEndObject();
        g2.close();
        assertEquals("{\"00000000-0000-0000-0000-000000000001\":42}", sw2.toString());

        // 3. Fallback / default case branch in Default.serialize
        StdKeySerializers.Default otherSer = new StdKeySerializers.Default(999, Object.class);
        StringWriter sw3 = new StringWriter();
        JsonGenerator g3 = jsonFactory.createGenerator(sw3);
        g3.writeStartObject();
        otherSer.serialize("customKey", g3, prov);
        g3.writeNull();
        g3.writeEndObject();
        g3.close();
        assertEquals("{\"customKey\":null}", sw3.toString());
    }

    @Test(timeout = 4000)
    public void testStringKeySerializerDirect() throws Exception {
        StdKeySerializers.StringKeySerializer ser = new StdKeySerializers.StringKeySerializer();
        assertEquals(String.class, ser.handledType());

        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        g.writeStartObject();
        ser.serialize("keyName", g, mapper.getSerializerProviderInstance());
        g.writeNumber(100);
        g.writeEndObject();
        g.close();
        assertEquals("{\"keyName\":100}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testDynamicKeySerializerDirectAndCacheHit() throws Exception {
        StdKeySerializers.Dynamic dynamic = new StdKeySerializers.Dynamic();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        g.writeStartObject();

        // First call: cache miss, triggers _findAndAddDynamic
        dynamic.serialize("strKey", g, prov);
        g.writeNumber(1);

        // Second call with same class: cache hit
        dynamic.serialize("strKey2", g, prov);
        g.writeNumber(2);

        // Third call with another class: cache miss
        dynamic.serialize(12345, g, prov);
        g.writeNumber(3);

        g.writeEndObject();
        g.close();

        assertEquals("{\"strKey\":1,\"strKey2\":2,\"12345\":3}", sw.toString());
    }

    /*
     * ----------------------------------------------------------------------
     * Partition E: Object Lifecycle & Contract Integrity
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testDynamicSerializerJavaSerializationReadResolve() throws Exception {
        StdKeySerializers.Dynamic dynamic = new StdKeySerializers.Dynamic();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(dynamic);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof StdKeySerializers.Dynamic);

        // Ensure deserialized instance has re-initialized _dynamicSerializers and functions properly
        StdKeySerializers.Dynamic resolvedDynamic = (StdKeySerializers.Dynamic) deserialized;
        StringWriter sw = new StringWriter();
        JsonGenerator g = jsonFactory.createGenerator(sw);
        g.writeStartObject();
        resolvedDynamic.serialize("postDeserializationKey", g, mapper.getSerializerProviderInstance());
        g.writeString("test");
        g.writeEndObject();
        g.close();

        assertEquals("{\"postDeserializationKey\":\"test\"}", sw.toString());
    }
}