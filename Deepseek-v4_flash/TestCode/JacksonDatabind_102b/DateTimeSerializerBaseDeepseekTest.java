package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.*;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Branch & Defect Analysis Matrix:
 * =================================
 * Partition A: Core Functional Logic & State Transitions
 *   - _asTimestamp() with various combinations of _useTimestamp, _customFormat, serializers.
 *   - isEmpty() always returns false.
 *   - getSchema() delegates to _asTimestamp().
 *   - acceptJsonFormatVisitor() delegates to _acceptJsonFormatVisitor().
 *   - _acceptJsonFormatVisitor() calls visitIntFormat or visitStringFormat based on asNumber.
 * Partition B: Boundary Value Analysis & Extremes
 *   - createContextual() with null property → returns this.
 *   - createContextual() with null format → returns this.
 *   - createContextual() with numeric shape → withFormat(Boolean.TRUE, null).
 *   - createContextual() with pattern, locale, timezone → withFormat(Boolean.FALSE, SimpleDateFormat).
 *   - createContextual() with no pattern but hasLocale/hasTZ/asString → various branches.
 *   - _asTimestamp() with null serializers → IllegalArgumentException.
 *   - _serializeAsString() with null customFormat → delegates to provider.
 *   - _serializeAsString() with non-null customFormat → uses _reusedCustomFormat pool.
 * Partition C: Defect-Targeted Branch Zone
 *   - Defect: createContextual() returns early for null property, skipping config overrides (root value).
 *     Test: Provide config override for Date.class with pattern, call createContextual(provider, null).
 *     Expected (fixed): returned serializer has _customFormat set.
 *     Actual (defective): returned serializer unchanged (_customFormat null).
 * Partition D: Exception & Defensive Guard Paths
 *   - createContextual() with non-SimpleDateFormat df0 → reportBadDefinition.
 * Partition E: Object Lifecycle & Contract Integrity
 *   - (Not applicable for this abstract base)
 */
public class DateTimeSerializerBaseDeepseekTest {

    // -----------------------------------------------------------------------
    // Helper concrete subclass for testing abstract methods
    // -----------------------------------------------------------------------
    @SuppressWarnings("serial")
    private static class TestDateSerializer extends DateTimeSerializerBase<Date> {

        public TestDateSerializer(Class<Date> type, Boolean useTimestamp, DateFormat customFormat) {
            super(type, useTimestamp, customFormat);
        }

        @Override
        public DateTimeSerializerBase<Date> withFormat(Boolean timestamp, DateFormat customFormat) {
            return new TestDateSerializer(handledType(), timestamp, customFormat);
        }

        @Override
        protected long _timestamp(Date value) {
            return value.getTime();
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            // For testing, delegate to _serializeAsString if we have a custom format; else default.
            if (_customFormat != null) {
                _serializeAsString(value, gen, serializers);
            } else {
                serializers.defaultSerializeDateValue(value, gen);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAsTimestampWithExplicitTrue() {
        TestDateSerializer ser = new TestDateSerializer(Date.class, Boolean.TRUE, null);
        assertTrue(ser._asTimestamp(null)); // null provider not reached because _useTimestamp != null
    }

    @Test(timeout = 4000)
    public void testAsTimestampWithExplicitFalse() {
        TestDateSerializer ser = new TestDateSerializer(Date.class, Boolean.FALSE, null);
        assertFalse(ser._asTimestamp(null));
    }

    @Test(timeout = 4000)
    public void testAsTimestampWithCustomFormat() {
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, new SimpleDateFormat("yyyy-MM-dd"));
        assertFalse(ser._asTimestamp(null)); // custom format forces false
    }

    @Test(timeout = 4000)
    public void testAsTimestampWithCustomFormatAndExplicitTrue() {
        TestDateSerializer ser = new TestDateSerializer(Date.class, Boolean.TRUE, new SimpleDateFormat("yyyy-MM-dd"));
        assertTrue(ser._asTimestamp(null)); // explicit true overrides custom format
    }

    @Test(timeout = 4000)
    public void testAsTimestampDefaultsToSerializersFeature() {
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, null);
        // When serializers is null, should throw IllegalArgumentException
        try {
            ser._asTimestamp(null);
            fail("Expected IllegalArgumentException for null serializers");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAsTimestampWithSerializersFeatureEnabled() {
        // We can't easily mock SerializerProvider, but we can test via ObjectMapper
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, null);
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider provider = (DefaultSerializerProvider) mapper.getSerializerProvider();
        // By default, WRITE_DATES_AS_TIMESTAMPS is enabled
        assertTrue(ser._asTimestamp(provider));
    }

    @Test(timeout = 4000)
    public void testAsTimestampWithSerializersFeatureDisabled() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        DefaultSerializerProvider provider = (DefaultSerializerProvider) mapper.getSerializerProvider();
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, null);
        assertFalse(ser._asTimestamp(provider));
    }

    @Test(timeout = 4000)
    public void testIsEmptyAlwaysFalse() {
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, null);
        assertFalse(ser.isEmpty(null, new Date()));
        assertFalse(ser.isEmpty(null, null)); // even null value is not considered empty
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes (createContextual)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCreateContextualNullPropertyReturnsThis() {
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, null);
        TestDateSerializer result = (TestDateSerializer) ser.createContextual(null, null);
        assertSame("Null property should return the same instance", ser, result);
    }

    @Test(timeout = 4000)
    public void testCreateContextualNullFormat() {
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, null);
        // Property that returns null from findFormatOverrides (e.g., no annotation, no config override)
        // For simplicity, we use a property with no format info.
        BeanProperty prop = createPropertyWithFormat(null);
        TestDateSerializer result = (TestDateSerializer) ser.createContextual(null, prop);
        assertSame("Null format should return this", ser, result);
    }

    @Test(timeout = 4000)
    public void testCreateContextualNumericShape() {
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, null);
        JsonFormat.Value format = JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER);
        BeanProperty prop = createPropertyWithFormat(format);
        TestDateSerializer result = (TestDateSerializer) ser.createContextual(null, prop);
        assertNotNull(result);
        assertEquals(Boolean.TRUE, result._useTimestamp);
        assertNull(result._customFormat);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithPattern() {
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, null);
        JsonFormat.Value format = JsonFormat.Value.forPattern("yyyy-MM-dd");
        // Provide locale and timezone via serializers? We'll use default.
        BeanProperty prop = createPropertyWithFormat(format);
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider provider = (DefaultSerializerProvider) mapper.getSerializerProvider();
        TestDateSerializer result = (TestDateSerializer) ser.createContextual(provider, prop);
        assertNotNull(result);
        assertFalse(result._useTimestamp);
        assertNotNull(result._customFormat);
        assertTrue(result._customFormat instanceof SimpleDateFormat);
        SimpleDateFormat sdf = (SimpleDateFormat) result._customFormat;
        assertEquals("yyyy-MM-dd", sdf.toPattern());
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithPatternAndLocale() {
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, null);
        JsonFormat.Value format = JsonFormat.Value.forPattern("dd/MM/yy");
        // Locale and timezone from format
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider provider = (DefaultSerializerProvider) mapper.getSerializerProvider();
        BeanProperty prop = createPropertyWithFormat(format);
        TestDateSerializer result = (TestDateSerializer) ser.createContextual(provider, prop);
        // Should have created SimpleDateFormat with default locale from provider
        assertNotNull(result._customFormat);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithNoPatternButAsString() {
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, null);
        JsonFormat.Value format = JsonFormat.Value.forShape(JsonFormat.Shape.STRING);
        BeanProperty prop = createPropertyWithFormat(format);
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider provider = (DefaultSerializerProvider) mapper.getSerializerProvider();
        // By default, config date format is SimpleDateFormat
        TestDateSerializer result = (TestDateSerializer) ser.createContextual(provider, prop);
        assertNotNull(result);
        assertFalse(result._useTimestamp);
        assertNotNull(result._customFormat);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithStdDateFormatCustomizations() {
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, null);
        JsonFormat.Value format = JsonFormat.Value.forPattern("yyyy/MM/dd").withLocale(Locale.GERMANY).withTimeZone(TimeZone.getTimeZone("CET"));
        BeanProperty prop = createPropertyWithFormat(format);
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider provider = (DefaultSerializerProvider) mapper.getSerializerProvider();
        TestDateSerializer result = (TestDateSerializer) ser.createContextual(provider, prop);
        assertNotNull(result);
        // Should have SimpleDateFormat with pattern and custom locale/timezone
        SimpleDateFormat sdf = (SimpleDateFormat) result._customFormat;
        assertEquals("yyyy/MM/dd", sdf.toPattern());
        assertEquals(Locale.GERMANY, sdf.getDateFormatSymbols().getLocale());
        assertEquals(TimeZone.getTimeZone("CET"), sdf.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithNonStdDateFormat() throws Exception {
        // This tests the branch where df0 is not SimpleDateFormat -> expected exception
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, null);
        JsonFormat.Value format = JsonFormat.Value.forShape(JsonFormat.Shape.STRING).withLocale(Locale.FRANCE);
        BeanProperty prop = createPropertyWithFormat(format);
        ObjectMapper mapper = new ObjectMapper();
        // Set a custom DateFormat that is not SimpleDateFormat
        mapper.setDateFormat(new DateFormat() {
            private static final long serialVersionUID = 1L;
            @Override
            public StringBuffer format(Date date, StringBuffer toAppendTo, java.text.FieldPosition fieldPosition) {
                return null;
            }
            @Override
            public Date parse(String source, java.text.ParsePosition pos) {
                return null;
            }
            @Override
            public Object clone() {
                return this;
            }
        });
        DefaultSerializerProvider provider = (DefaultSerializerProvider) mapper.getSerializerProvider();
        try {
            ser.createContextual(provider, prop);
            fail("Expected JsonMappingException for non-SimpleDateFormat");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Root value config override)
    // -----------------------------------------------------------------------
    @Test(timeout = 4000)
    public void testRootValueConfigOverrideShouldApplyFormat() throws Exception {
        // This test directly targets the known defect:
        // When property is null (root value), the method returns early,
        // ignoring config overrides that should be applied.
        ObjectMapper mapper = new ObjectMapper();
        // Set a config override for Date.class to format as string with pattern "yyyy-MM-dd"
        mapper.configOverride(Date.class).setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd"));
        DefaultSerializerProvider provider = (DefaultSerializerProvider) mapper.getSerializerProvider();
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, null);
        // Call createContextual with null property (root value)
        DateTimeSerializerBase<?> contextual = ser.createContextual(provider, null);

        // In a fixed version, the returned serializer should have a custom format.
        // In the defective version (early return for null property), it is the same instance
        // with no custom format.
        assertNotNull("Config override should have created custom format", contextual._customFormat);
        assertFalse("useTimestamp should be false for pattern", contextual._useTimestamp);

        // Additionally, serializing a date should produce a formatted string.
        // We can test by creating a JsonGenerator stub that records the output.
        StringBuilder sb = new StringBuilder();
        JsonGenerator gen = new JsonGenerator() {
            @Override
            public void writeString(String text) throws IOException {
                sb.append(text);
            }
            // Required abstract methods – stub them as no-ops or minimal
            @Override public void flush() throws IOException {}
            @Override public void close() throws IOException {}
            @Override public JsonStreamContext getOutputContext() { return null; }
            @Override public void writeStartArray() throws IOException {}
            @Override public void writeEndArray() throws IOException {}
            @Override public void writeStartObject() throws IOException {}
            @Override public void writeEndObject() throws IOException {}
            @Override public void writeFieldName(String name) throws IOException {}
            @Override public void writeFieldName(SerializableString name) throws IOException {}
            @Override public void writeString(char[] text, int offset, int len) throws IOException {}
            @Override public void writeRawUTF8String(byte[] text, int offset, int length) throws IOException {}
            @Override public void writeUTF8String(byte[] text, int offset, int length) throws IOException {}
            @Override public void writeRaw(String text) throws IOException {}
            @Override public void writeRaw(String text, int offset, int len) throws IOException {}
            @Override public void writeRaw(char[] text, int offset, int len) throws IOException {}
            @Override public void writeRaw(char c) throws IOException {}
            @Override public void writeRawValue(String text) throws IOException {}
            @Override public void writeRawValue(String text, int offset, int len) throws IOException {}
            @Override public void writeRawValue(char[] text, int offset, int len) throws IOException {}
            @Override public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException {}
            @Override public int writeBinary(Base64Variant b64variant, InputStream data, int dataBytes) throws IOException { return 0; }
            @Override public void writeNumber(short v) throws IOException {}
            @Override public void writeNumber(int v) throws IOException {}
            @Override public void writeNumber(long v) throws IOException {}
            @Override public void writeNumber(float v) throws IOException {}
            @Override public void writeNumber(double v) throws IOException {}
            @Override public void writeNumber(String encodedValue) throws IOException {}
            @Override public void writeBoolean(boolean state) throws IOException {}
            @Override public void writeNull() throws IOException {}
            @Override public void writeObject(Object pojo) throws IOException {}
            @Override public void writeTree(TreeNode rootNode) throws IOException {}
            @Override public JsonGenerator setCodec(ObjectMapper mapper) { return this; }
            @Override public ObjectMapper getCodec() { return null; }
            @Override public boolean isClosed() { return false; }
            @Override public JsonGenerator disable(Feature f) { return this; }
            @Override public JsonGenerator enable(Feature f) { return this; }
            @Override public boolean isEnabled(Feature f) { return false; }
            @Override public int getFeatureMask() { return 0; }
            @Override public JsonGenerator setFeatureMask(int mask) { return this; }
            @Override public JsonGenerator useDefaultPrettyPrinter() { return this; }
            @Override public JsonGenerator setPrettyPrinter(PrettyPrinter pp) { return this; }
            @Override public PrettyPrinter getPrettyPrinter() { return null; }
            @Override public JsonGenerator setHighestEscapedChar(int charCode) { return this; }
            @Override public int getHighestEscapedChar() { return 0; }
            @Override public CharacterEscapes getCharacterEscapes() { return null; }
            @Override public JsonGenerator setCharacterEscapes(CharacterEscapes esc) { return this; }
            @Override public JsonGenerator setRootValueSeparator(SerializableString sep) { return this; }
            @Override public Object getCurrentValue() { return null; }
            @Override public void setCurrentValue(Object v) {}
            @Override public Version version() { return null; }
            @Override public Object getOutputBuffered() { return null; }
            @Override public int getOutputSize() { return 0; }
            @Override public boolean canWriteObjectId() { return false; }
            @Override public boolean canWriteTypeId() { return false; }
            @Override public boolean canOmitFields() { return false; }
            @Override public void writeObjectId(Object id) throws IOException {}
            @Override public void writeObjectRef(Object id) throws IOException {}
            @Override public void writeTypeId(Object id) throws IOException {}
            @Override public void writeEmbeddedObject(Object object) throws IOException {}
        };
        // Use the contextual serializer to serialize a specific date
        Date date = new Date(324547200000L); // 1980-04-14T00:00:00Z
        contextual.serialize(date, gen, provider);
        String output = sb.toString();
        // Expected formatted string "1980-04-14"
        assertEquals("Serialized string should match pattern", "1980-04-14", output);
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAsTimestampWithNullSerializersAndNoExplicitFlag() {
        TestDateSerializer ser = new TestDateSerializer(Date.class, null, null);
        ser._asTimestamp(null);
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity (not applicable)
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    /**
     * Creates a simple BeanProperty stub that returns the given JsonFormat.Value.
     */
    private BeanProperty createPropertyWithFormat(final JsonFormat.Value format) {
        return new BeanProperty() {
            @Override
            public String getName() { return "test"; }
            @Override
            public JavaType getType() { return null; }
            @Override
            public JavaType getMember() { return null; }
            @Override
            public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> acls) { return null; }
            @Override
            public <A extends java.lang.annotation.Annotation> A getContextAnnotation(Class<A> acls) { return null; }
            @Override
            public JsonFormat.Value findPropertyFormat(SerializationConfig config, Class<?> type) {
                return format;
            }
            @Override
            public boolean isRequired() { return false; }
            @Override
            public <A extends java.lang.annotation.Annotation> A getAnnotation(MapperConfig<?> config, Class<A> acls) { return null; }
            @Override
            public AnnotationIntrospector.ReferenceProperty findReferenceType() { return null; }
            @Override
            public boolean isTypeId() { return false; }
            @Override
            public PropertyMetadata getMetadata() { return null; }
            @Override
            public PropertyName getFullName() { return new PropertyName(getName()); }
            @Override
            public PropertyName getWrapperName() { return null; }
            @Override
            public ObjectIdInfo getObjectIdInfo() { return null; }
            @Override
            public String getPropertyName() { return getName(); }
        };
    }
}