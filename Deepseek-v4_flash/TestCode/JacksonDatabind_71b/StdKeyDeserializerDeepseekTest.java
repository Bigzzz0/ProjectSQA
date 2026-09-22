package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class StdKeyDeserializerDeepseekTest {
    /*
     * [Branch & Defect Analysis Matrix]
     *
     * Target class: StdKeyDeserializer (standalone + inner classes)
     *
     * Branches covered:
     * - forType(Class):
     *   - raw == String.class / Object.class => StringKD
     *   - raw == UUID, Integer, Long, Date, Calendar, Boolean, Byte, Character, Short, Float, Double, URI, URL, Class
     *   - raw == Locale => special (with FromStringDeserializer)
     *   - raw == Currency => special (with FromStringDeserializer)
     *   - else => return null
     *   - Defect: raw == CharSequence.class – currently returns null, should return a deserializer (StringKD expected)
     *
     * - deserializeKey(String, DeserializationContext):
     *   - key == null => return null
     *   - key != null => call _parse; on exception => weirdKeyException
     *   - if _parse returns null and _keyClass.isEnum() && READ_UNKNOWN_ENUM_VALUES_AS_NULL => return null
     *   - else => weirdKeyException
     *
     * - _parse(String key, DeserializationContext ctxt) => switch on _kind:
     *   - TYPE_BOOLEAN: "true"/"false" else exception
     *   - TYPE_BYTE: parse int, range check (MIN_VALUE..255)
     *   - TYPE_SHORT: parse int, range check (MIN_VALUE..MAX_VALUE)
     *   - TYPE_CHAR: length==1 => Character, else exception
     *   - TYPE_INT: _parseInt
     *   - TYPE_LONG: _parseLong
     *   - TYPE_FLOAT: _parseDouble cast to float
     *   - TYPE_DOUBLE: _parseDouble
     *   - TYPE_LOCALE / TYPE_CURRENCY: use _deser._deserialize
     *   - TYPE_DATE: ctxt.parseDate
     *   - TYPE_CALENDAR: ctxt.parseDate + constructCalendar
     *   - TYPE_UUID: UUID.fromString
     *   - TYPE_URI: URI.create
     *   - TYPE_URL: new URL
     *   - TYPE_CLASS: ctxt.findClass
     *   - default: return null
     *
     * Defect-target: when a Map key type is CharSequence, forType returns null,
     * so deserialization fails. Fixed version should return a StringKD for CharSequence.
     */

    @Test(timeout = 4000)
    public void testCharSequenceKeyMap() throws IOException {
        // This test triggers the known defect: forType(CharSequence.class) returns null.
        // On a fixed version, deserialization succeeds.
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        java.util.Map<?, ?> result = mapper.readValue(json,
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, CharSequence.class, String.class));
        assertNotNull("Map with CharSequence keys should be deserialized", result);
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    @Test(timeout = 4000)
    public void testForTypeReturnsNonNullForCharSequence() {
        // Explicitly test that forType(CharSequence.class) returns non-null on fixed version.
        StdKeyDeserializer deser = StdKeyDeserializer.forType(CharSequence.class);
        assertNotNull("StdKeyDeserializer for CharSequence should not be null", deser);
        assertTrue("Should be instance of StringKD", deser instanceof StdKeyDeserializer.StringKD);
    }

    @Test(timeout = 4000)
    public void testForTypeString() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(String.class);
        assertNotNull(deser);
        assertTrue(deser instanceof StdKeyDeserializer.StringKD);
    }

    @Test(timeout = 4000)
    public void testForTypeObject() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Object.class);
        assertNotNull(deser);
        assertTrue(deser instanceof StdKeyDeserializer.StringKD);
    }

    @Test(timeout = 4000)
    public void testForTypeUUID() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(UUID.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_UUID, deser._kind);
        assertEquals(UUID.class, deser._keyClass);
    }

    @Test(timeout = 4000)
    public void testForTypeInteger() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Integer.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_INT, deser._kind);
    }

    @Test(timeout = 4000)
    public void testForTypeLong() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Long.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_LONG, deser._kind);
    }

    @Test(timeout = 4000)
    public void testForTypeDate() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Date.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_DATE, deser._kind);
    }

    @Test(timeout = 4000)
    public void testForTypeCalendar() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Calendar.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_CALENDAR, deser._kind);
    }

    @Test(timeout = 4000)
    public void testForTypeBoolean() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Boolean.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_BOOLEAN, deser._kind);
    }

    @Test(timeout = 4000)
    public void testForTypeByte() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Byte.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_BYTE, deser._kind);
    }

    @Test(timeout = 4000)
    public void testForTypeCharacter() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Character.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_CHAR, deser._kind);
    }

    @Test(timeout = 4000)
    public void testForTypeShort() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Short.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_SHORT, deser._kind);
    }

    @Test(timeout = 4000)
    public void testForTypeFloat() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Float.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_FLOAT, deser._kind);
    }

    @Test(timeout = 4000)
    public void testForTypeDouble() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Double.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_DOUBLE, deser._kind);
    }

    @Test(timeout = 4000)
    public void testForTypeURI() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(URI.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_URI, deser._kind);
    }

    @Test(timeout = 4000)
    public void testForTypeURL() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(URL.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_URL, deser._kind);
    }

    @Test(timeout = 4000)
    public void testForTypeClass() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Class.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_CLASS, deser._kind);
    }

    @Test(timeout = 4000)
    public void testForTypeLocale() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Locale.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_LOCALE, deser._kind);
        assertNotNull(deser._deser);
    }

    @Test(timeout = 4000)
    public void testForTypeCurrency() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Currency.class);
        assertNotNull(deser);
        assertEquals(StdKeyDeserializer.TYPE_CURRENCY, deser._kind);
        assertNotNull(deser._deser);
    }

    @Test(timeout = 4000)
    public void testForTypeUnknownReturnsNull() {
        // Classes not explicitly handled should return null
        StdKeyDeserializer deser = StdKeyDeserializer.forType(StringBuilder.class);
        assertNull(deser);
        deser = StdKeyDeserializer.forType(AbstractStringBuilder.class);
        assertNull(deser);
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyNullReturnsNull() throws IOException {
        // StringKD's deserializeKey returns key directly (for null returns null)
        StdKeyDeserializer deser = StdKeyDeserializer.forType(String.class);
        Object result = deser.deserializeKey(null, null);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyBooleanTrue() throws IOException {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Boolean.class);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        // We need a context; using mapper's context after a simple deserialization
        // For Boolean, we can test via the static _parse directly using a known key.
        // Since deserializeKey requires ctxt, we create one using an internal approach:
        // Actually, we can use a helper method to get a context.
        // For simplicity, we test the _parse method via reflecting? Instead, rely on integration test.
        // But we can use the mapper to parse a Boolean key in a map.
        java.util.Map<Boolean, String> result = mapper.readValue("{\"true\":\"yes\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Boolean.class, String.class));
        assertNotNull(result);
        assertTrue(result.containsKey(true));
        assertEquals("yes", result.get(true));
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyBooleanFalse() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map<Boolean, String> result = mapper.readValue("{\"false\":\"no\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Boolean.class, String.class));
        assertNotNull(result);
        assertTrue(result.containsKey(false));
        assertEquals("no", result.get(false));
    }

    @Test(timeout = 4000, expected = Exception.class)
    public void testDeserializeKeyBooleanInvalid() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map<Boolean, String> result = mapper.readValue("{\"notBoolean\":\"value\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Boolean.class, String.class));
        // Should throw an exception before reaching here
        fail("Should have thrown an exception");
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyByte() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map<Byte, String> result = mapper.readValue("{\"10\":\"ten\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Byte.class, String.class));
        assertNotNull(result);
        assertTrue(result.containsKey((byte)10));
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyShort() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map<Short, String> result = mapper.readValue("{\"200\":\"twohundred\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Short.class, String.class));
        assertNotNull(result);
        assertTrue(result.containsKey((short)200));
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyChar() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map<Character, String> result = mapper.readValue("{\"A\":\"letterA\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Character.class, String.class));
        assertNotNull(result);
        assertTrue(result.containsKey('A'));
        assertEquals("letterA", result.get('A'));
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyInt() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map<Integer, String> result = mapper.readValue("{\"42\":\"answer\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Integer.class, String.class));
        assertNotNull(result);
        assertTrue(result.containsKey(42));
        assertEquals("answer", result.get(42));
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyLong() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map<Long, String> result = mapper.readValue("{\"2147483648\":\"big\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Long.class, String.class));
        assertNotNull(result);
        assertTrue(result.containsKey(2147483648L));
        assertEquals("big", result.get(2147483648L));
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyFloat() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map<Float, String> result = mapper.readValue("{\"3.14\":\"pi\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Float.class, String.class));
        assertNotNull(result);
        Float expected = 3.14f;
        assertTrue(result.containsKey(expected));
        assertEquals("pi", result.get(expected));
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyDouble() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map<Double, String> result = mapper.readValue("{\"2.71828\":\"euler\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Double.class, String.class));
        assertNotNull(result);
        Double expected = 2.71828;
        assertTrue(result.containsKey(expected));
        assertEquals("euler", result.get(expected));
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyURI() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map<URI, String> result = mapper.readValue("{\"http://example.com\":\"home\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, URI.class, String.class));
        assertNotNull(result);
        assertTrue(result.containsKey(URI.create("http://example.com")));
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyURL() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map<URL, String> result = mapper.readValue("{\"http://jackson.com\":\"site\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, URL.class, String.class));
        assertNotNull(result);
        assertTrue(result.containsKey(new URL("http://jackson.com")));
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyUUID() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String uuid = "12345678-1234-1234-1234-123456789abc";
        java.util.Map<UUID, String> result = mapper.readValue("{\"" + uuid + "\":\"uuidVal\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, UUID.class, String.class));
        assertNotNull(result);
        assertTrue(result.containsKey(UUID.fromString(uuid)));
        assertEquals("uuidVal", result.get(UUID.fromString(uuid)));
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyLocale() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map<Locale, String> result = mapper.readValue("{\"en_US\":\"english\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Locale.class, String.class));
        assertNotNull(result);
        assertTrue(result.containsKey(Locale.US));
        assertEquals("english", result.get(Locale.US));
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyCurrency() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map<Currency, String> result = mapper.readValue("{\"USD\":\"dollar\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Currency.class, String.class));
        assertNotNull(result);
        assertTrue(result.containsKey(Currency.getInstance("USD")));
        assertEquals("dollar", result.get(Currency.getInstance("USD")));
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyDate() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String dateStr = "2018-01-01";
        java.util.Map<Date, String> result = mapper.readValue("{\"" + dateStr + "\":\"start\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Date.class, String.class));
        assertNotNull(result);
        // The exact date parsing depends on mapper config; we just check non-null
        assertFalse(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyCalendar() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String dateStr = "2019-06-15";
        java.util.Map<Calendar, String> result = mapper.readValue("{\"" + dateStr + "\":\"midyear\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Calendar.class, String.class));
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testDeserializeKeyClass() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        java.util.Map<Class<?>, String> result = mapper.readValue("{\"java.lang.String\":\"stringClass\"}",
                mapper.getTypeFactory().constructMapType(java.util.HashMap.class, Class.class, String.class));
        assertNotNull(result);
        assertTrue(result.containsKey(String.class));
        assertEquals("stringClass", result.get(String.class));
    }

    @Test(timeout = 4000)
    public void testGetKeyClass() {
        StdKeyDeserializer deser = StdKeyDeserializer.forType(Integer.class);
        assertEquals(Integer.class, deser.getKeyClass());
        deser = StdKeyDeserializer.forType(String.class);
        assertEquals(String.class, deser.getKeyClass());
    }
}