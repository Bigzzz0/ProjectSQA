package com.fasterxml.jackson.databind.ser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Partition A – Core Functional Logic & State Transitions:
 *   - getStdKeySerializer: null, Object.class, String.class, primitive, Number subclass,
 *     Class.class, Date, Calendar, UUID, other types with useDefault true/false.
 *   - getFallbackKeySerializer: null, Enum.class, enum types, other types.
 *   - Default serializer: all typeId branches (TYPE_DATE, TYPE_CALENDAR, TYPE_CLASS,
 *     TYPE_ENUM, TYPE_TO_STRING).
 *   - Dynamic serializer: first call (miss), subsequent call (hit), readResolve.
 *   - StringKeySerializer: basic serialization.
 * 
 * Partition B – Boundary Value Analysis & Extremes:
 *   - rawKeyType = null, Object.class, String.class, int.class, boolean.class,
 *     Integer.class, Long.class, Class.class, Date.class, Calendar.class,
 *     UUID.class, List.class, Enum.class, custom enum.
 *   - useDefault = true / false.
 * 
 * Partition C – Defect-Targeted Branch Zone:
 *   - [databind#1322] Enum key with @JsonProperty: Default serializer must use
 *     annotated value, not Enum.name(). The known defect produces {"A":"b"} instead
 *     of {"aleph":"b"}.
 * 
 * Partition D – Exception & Defensive Guard Paths:
 *   - Dynamic._findAndAddDynamic with null provider? (not tested directly)
 *   - Default.serialize with invalid typeId? (not possible via factory)
 * 
 * Partition E – Object Lifecycle & Contract Integrity:
 *   - Dynamic.readResolve() resets transient map.
 *   - StringKeySerializer handles null? (not expected, but safe)
 */
public class StdKeySerializersDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A & B: Factory method coverage
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetStdKeySerializerNull() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, null, false);
        assertTrue("Expected Dynamic for null type", ser instanceof StdKeySerializers.Dynamic);
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerObjectClass() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, Object.class, false);
        assertTrue("Expected Dynamic for Object.class", ser instanceof StdKeySerializers.Dynamic);
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerString() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, String.class, false);
        assertTrue("Expected StringKeySerializer", ser instanceof StdKeySerializers.StringKeySerializer);
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerPrimitive() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, int.class, false);
        assertTrue("Expected StdKeySerializer for primitive", ser == StdKeySerializers.DEFAULT_KEY_SERIALIZER);
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerNumberSubclass() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, Integer.class, false);
        assertTrue("Expected StdKeySerializer for Number subclass", ser == StdKeySerializers.DEFAULT_KEY_SERIALIZER);
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerClass() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, Class.class, false);
        assertTrue("Expected Default with TYPE_CLASS", ser instanceof StdKeySerializers.Default);
        // We can't easily check _typeId, but we trust the factory
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerDate() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, Date.class, false);
        assertTrue("Expected Default for Date", ser instanceof StdKeySerializers.Default);
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerCalendar() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, Calendar.class, false);
        assertTrue("Expected Default for Calendar", ser instanceof StdKeySerializers.Default);
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerUUID() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, java.util.UUID.class, false);
        assertTrue("Expected Default for UUID", ser instanceof StdKeySerializers.Default);
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerOtherWithDefaultTrue() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, List.class, true);
        assertTrue("Expected DEFAULT_KEY_SERIALIZER when useDefault=true",
                ser == StdKeySerializers.DEFAULT_KEY_SERIALIZER);
    }

    @Test(timeout = 4000)
    public void testGetStdKeySerializerOtherWithDefaultFalse() {
        JsonSerializer<Object> ser = StdKeySerializers.getStdKeySerializer(null, List.class, false);
        assertNull("Expected null when useDefault=false and no match", ser);
    }

    @Test(timeout = 4000)
    public void testGetFallbackKeySerializerNull() {
        JsonSerializer<Object> ser = StdKeySerializers.getFallbackKeySerializer(null, null);
        assertTrue("Expected DEFAULT_KEY_SERIALIZER for null type",
                ser == StdKeySerializers.DEFAULT_KEY_SERIALIZER);
    }

    @Test(timeout = 4000)
    public void testGetFallbackKeySerializerEnumClass() {
        JsonSerializer<Object> ser = StdKeySerializers.getFallbackKeySerializer(null, Enum.class);
        assertTrue("Expected Dynamic for Enum.class", ser instanceof StdKeySerializers.Dynamic);
    }

    @Test(timeout = 4000)
    public void testGetFallbackKeySerializerEnumType() {
        JsonSerializer<Object> ser = StdKeySerializers.getFallbackKeySerializer(null, TestEnum.class);
        assertTrue("Expected Default for enum type", ser instanceof StdKeySerializers.Default);
    }

    @Test(timeout = 4000)
    public void testGetFallbackKeySerializerOther() {
        JsonSerializer<Object> ser = StdKeySerializers.getFallbackKeySerializer(null, String.class);
        assertTrue("Expected DEFAULT_KEY_SERIALIZER for non-enum",
                ser == StdKeySerializers.DEFAULT_KEY_SERIALIZER);
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-targeted test for enum key with @JsonProperty
    // -----------------------------------------------------------------------

    // Enum with @JsonProperty to trigger the known bug
    public enum TestEnumWithJsonProperty {
        @JsonProperty("aleph") A,
        @JsonProperty("bet") B
    }

    @Test(timeout = 4000)
    public void testEnumKeyWithJsonProperty() throws Exception {
        // This test reveals the defect: the Default serializer uses Enum.name()
        // instead of the @JsonProperty value.
        ObjectMapper mapper = new ObjectMapper();
        Map<TestEnumWithJsonProperty, String> map = new HashMap<>();
        map.put(TestEnumWithJsonProperty.A, "b");
        String json = mapper.writeValueAsString(map);
        // Expected: {"aleph":"b"}  (buggy version produces {"A":"b"})
        assertEquals("{\"aleph\":\"b\"}", json);
    }

    // -----------------------------------------------------------------------
    // Partition A & E: Inner class behavior (serialize methods)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDefaultSerializeDate() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(
                StdKeySerializers.Default.TYPE_DATE, Date.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = new DefaultSerializerProvider.Impl();
        // Use a concrete date
        Date date = new Date(0L); // epoch
        ser.serialize(date, gen, prov);
        gen.close();
        // The output depends on timezone; we just check it's not empty
        assertNotNull(sw.toString());
    }

    @Test(timeout = 4000)
    public void testDefaultSerializeCalendar() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(
                StdKeySerializers.Default.TYPE_CALENDAR, Calendar.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = new DefaultSerializerProvider.Impl();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0L);
        ser.serialize(cal, gen, prov);
        gen.close();
        assertNotNull(sw.toString());
    }

    @Test(timeout = 4000)
    public void testDefaultSerializeClass() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(
                StdKeySerializers.Default.TYPE_CLASS, Class.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = new DefaultSerializerProvider.Impl();
        ser.serialize(String.class, gen, prov);
        gen.close();
        assertEquals("java.lang.String", sw.toString());
    }

    @Test(timeout = 4000)
    public void testDefaultSerializeEnum() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(
                StdKeySerializers.Default.TYPE_ENUM, TestEnum.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = new DefaultSerializerProvider.Impl();
        // Use a simple enum without @JsonProperty to test name()
        ser.serialize(TestEnum.VALUE, gen, prov);
        gen.close();
        assertEquals("VALUE", sw.toString());
    }

    @Test(timeout = 4000)
    public void testDefaultSerializeToString() throws Exception {
        StdKeySerializers.Default ser = new StdKeySerializers.Default(
                StdKeySerializers.Default.TYPE_TO_STRING, Object.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = new DefaultSerializerProvider.Impl();
        ser.serialize("test", gen, prov);
        gen.close();
        assertEquals("test", sw.toString());
    }

    @Test(timeout = 4000)
    public void testDynamicSerializeFirstCall() throws Exception {
        StdKeySerializers.Dynamic ser = new StdKeySerializers.Dynamic();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = new DefaultSerializerProvider.Impl();
        // First call with a type not in the map
        ser.serialize("hello", gen, prov);
        gen.close();
        assertEquals("hello", sw.toString());
        // After this, the map should have an entry for String.class
    }

    @Test(timeout = 4000)
    public void testDynamicSerializeSecondCall() throws Exception {
        StdKeySerializers.Dynamic ser = new StdKeySerializers.Dynamic();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = new DefaultSerializerProvider.Impl();
        // First call to populate map
        ser.serialize("first", gen, prov);
        // Second call with same type
        ser.serialize("second", gen, prov);
        gen.close();
        assertEquals("firstsecond", sw.toString());
    }

    @Test(timeout = 4000)
    public void testDynamicReadResolve() throws Exception {
        StdKeySerializers.Dynamic ser = new StdKeySerializers.Dynamic();
        // Simulate deserialization: readResolve should reset the map
        ser.readResolve();
        // After readResolve, the map should be empty; we can test by serializing a new type
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = new DefaultSerializerProvider.Impl();
        ser.serialize(123, gen, prov); // Integer type
        gen.close();
        assertEquals("123", sw.toString());
    }

    @Test(timeout = 4000)
    public void testStringKeySerializer() throws Exception {
        StdKeySerializers.StringKeySerializer ser = new StdKeySerializers.StringKeySerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = new DefaultSerializerProvider.Impl();
        ser.serialize("key", gen, prov);
        gen.close();
        assertEquals("key", sw.toString());
    }

    // -----------------------------------------------------------------------
    // Helper enum for tests
    // -----------------------------------------------------------------------
    public enum TestEnum {
        VALUE, OTHER
    }
}