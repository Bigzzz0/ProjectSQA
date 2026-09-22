package com.fasterxml.jackson.databind.ser.std;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.IOException;
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
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import com.fasterxml.jackson.databind.util.StdDateFormat;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: DateTimeSerializerBase
 * Known Defect: When shape=STRING without pattern, the format pattern used is
 *   StdDateFormat.DATE_FORMAT_STR_ISO8601 which produces timezone offset without
 *   colon (e.g., "+0100") instead of the expected ISO 8601 format with colon
 *   (e.g., "+01:00"). This test suite covers all branches and specifically targets
 *   the defect via testFormatWithoutPattern().
 *
 * Branches covered in createContextual:
 *   - property == null                    -> return this
 *   - format == null                      -> return this
 *   - shape.isNumeric()                   -> withFormat(Boolean.TRUE, null)
 *   - shape == STRING, hasPattern,
 *     hasLocale, hasTimeZone              -> withFormat(Boolean.FALSE, df)
 *   - otherwise                           -> return this
 *
 * Branches in _asTimestamp:
 *   - _useTimestamp != null               -> return booleanValue
 *   - _useTimestamp == null && _customFormat == null
 *       -> if serializers != null -> use feature; else throw IllegalArgumentException
 *   - _useTimestamp == null && _customFormat != null -> false
 *
 * isEmpty: value == null || _timestamp(value)==0L
 * getSchema: delegates to _asTimestamp
 * acceptJsonFormatVisitor: delegates to _acceptJsonFormatVisitor, branches on asNumber
 */
public class DateTimeSerializerBaseDeepseekTest {

    // ---------- Stub subclass for testing abstract base ----------
    private static class StubSerializer extends DateTimeSerializerBase<Date> {
        public StubSerializer(Boolean useTimestamp, DateFormat customFormat) {
            super(Date.class, useTimestamp, customFormat);
        }

        @Override
        public DateTimeSerializerBase<Date> withFormat(Boolean timestamp, DateFormat customFormat) {
            return new StubSerializer(timestamp, customFormat);
        }

        @Override
        protected long _timestamp(Date value) {
            return value.getTime();
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            // Minimal serialization for testing: just write timestamp
            gen.writeNumber(_timestamp(value));
        }
    }

    // ========== Partition A: Core Functional Logic ==========
    @Test(timeout = 4000)
    public void testCreateContextualWithNullProperty() throws Exception {
        StubSerializer base = new StubSerializer(null, null);
        JsonSerializer<?> result = base.createContextual(null, null);
        assertSame("Should return this when property is null", base, result);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithNumericShape() throws Exception {
        // Use real ObjectMapper to get a mock Provider and Property
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();
        // Create a minimal BeanProperty that returns a numeric shape
        BeanProperty prop = new BeanProperty() {
            @Override public String getName() { return "date"; }
            @Override public PropertyName getFullName() { return new PropertyName("date"); }
            @Override public JavaType getType() { return prov.constructType(Date.class); }
            @Override public PropertyName getWrapperName() { return null; }
            @Override public boolean isRequired() { return false; }
            @Override public boolean isIgnored() { return false; }
            @Override public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> acls) { return null; }
            @Override public <A extends java.lang.annotation.Annotation> A getContextAnnotation(Class<A> acls) { return null; }
            @Override public PropertyMetadata getMetadata() { return PropertyMetadata.STD_REQUIRED; }
            @Override public JavaType getPrimaryType() { return getType(); }
            @Override public boolean isVirtual() { return false; }
            @Override public com.fasterxml.jackson.databind.util.Annotations getMemberAnnotations() { return null; }
            @Override
            public JsonFormat.Value findFormatOverrides(AnnotationIntrospector intr) {
                return JsonFormat.Value.forShape(JsonFormat.Shape.NUMBER);
            }

        };

        StubSerializer base = new StubSerializer(null, null);
        JsonSerializer<?> result = base.createContextual(prov, prop);
        assertTrue("Should return a serializer with _useTimestamp=true", result instanceof DateTimeSerializerBase);
        DateTimeSerializerBase<?> dts = (DateTimeSerializerBase<?>) result;
        assertTrue("_useTimestamp should be Boolean.TRUE", dts._useTimestamp.booleanValue());
        assertNull("_customFormat should be null", dts._customFormat);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithShapeStringNoPattern() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();
        BeanProperty prop = new BeanProperty() {
            @Override public String getName() { return "date"; }
            @Override public PropertyName getFullName() { return new PropertyName("date"); }
            @Override public JavaType getType() { return prov.constructType(Date.class); }
            @Override public PropertyName getWrapperName() { return null; }
            @Override public boolean isRequired() { return false; }
            @Override public boolean isIgnored() { return false; }
            @Override public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> acls) { return null; }
            @Override public <A extends java.lang.annotation.Annotation> A getContextAnnotation(Class<A> acls) { return null; }
            @Override public PropertyMetadata getMetadata() { return PropertyMetadata.STD_REQUIRED; }
            @Override public JavaType getPrimaryType() { return getType(); }
            @Override public boolean isVirtual() { return false; }
            @Override public com.fasterxml.jackson.databind.util.Annotations getMemberAnnotations() { return null; }
            @Override
            public JsonFormat.Value findFormatOverrides(AnnotationIntrospector intr) {
                return JsonFormat.Value.forShape(JsonFormat.Shape.STRING);
            }
        };

        StubSerializer base = new StubSerializer(null, null);
        JsonSerializer<?> result = base.createContextual(prov, prop);
        assertTrue("Should return a serializer with _useTimestamp=false", result instanceof DateTimeSerializerBase);
        DateTimeSerializerBase<?> dts = (DateTimeSerializerBase<?>) result;
        assertFalse("_useTimestamp should be Boolean.FALSE", dts._useTimestamp.booleanValue());
        assertNotNull("_customFormat should be non-null", dts._customFormat);
        assertTrue("_customFormat should be SimpleDateFormat", dts._customFormat instanceof SimpleDateFormat);
        SimpleDateFormat df = (SimpleDateFormat) dts._customFormat;
        assertEquals("Pattern should be StdDateFormat.DATE_FORMAT_STR_ISO8601",
                StdDateFormat.DATE_FORMAT_STR_ISO8601, df.toPattern());
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithShapeStringAndPattern() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();
        BeanProperty prop = new BeanProperty() {
            @Override public String getName() { return "date"; }
            @Override public PropertyName getFullName() { return new PropertyName("date"); }
            @Override public JavaType getType() { return prov.constructType(Date.class); }
            @Override public PropertyName getWrapperName() { return null; }
            @Override public boolean isRequired() { return false; }
            @Override public boolean isIgnored() { return false; }
            @Override public <A extends java.lang.annotation.Annotation> A getAnnotation(Class<A> acls) { return null; }
            @Override public <A extends java.lang.annotation.Annotation> A getContextAnnotation(Class<A> acls) { return null; }
            @Override public PropertyMetadata getMetadata() { return PropertyMetadata.STD_REQUIRED; }
            @Override public JavaType getPrimaryType() { return getType(); }
            @Override public boolean isVirtual() { return false; }
            @Override public com.fasterxml.jackson.databind.util.Annotations getMemberAnnotations() { return null; }
            @Override
            public JsonFormat.Value findFormatOverrides(AnnotationIntrospector intr) {
                return JsonFormat.Value.forPattern("dd/MM/yyyy");
            }
        };

        StubSerializer base = new StubSerializer(null, null);
        JsonSerializer<?> result = base.createContextual(prov, prop);
        DateTimeSerializerBase<?> dts = (DateTimeSerializerBase<?>) result;
        assertFalse("_useTimestamp should be false", dts._useTimestamp.booleanValue());
        SimpleDateFormat df = (SimpleDateFormat) dts._customFormat;
        assertEquals("Pattern should be dd/MM/yyyy", "dd/MM/yyyy", df.toPattern());
    }

    // ========== Partition B: Boundary Values ==========
    @Test(timeout = 4000)
    public void testAsTimestampWhenUseTimestampTrue() {
        StubSerializer ser = new StubSerializer(Boolean.TRUE, null);
        assertTrue("_asTimestamp should return true when _useTimestamp is true",
                ser._asTimestamp(null));
    }

    @Test(timeout = 4000)
    public void testAsTimestampWhenUseTimestampFalse() {
        StubSerializer ser = new StubSerializer(Boolean.FALSE, null);
        assertFalse("_asTimestamp should return false when _useTimestamp is false",
                ser._asTimestamp(null));
    }

    @Test(timeout = 4000)
    public void testAsTimestampWhenUseTimestampNullAndCustomFormatNotNull() {
        StubSerializer ser = new StubSerializer(null, new SimpleDateFormat("yyyy-MM-dd"));
        assertFalse("_asTimestamp should return false when _customFormat is not null and _useTimestamp is null",
                ser._asTimestamp(null));
    }

    @Test(timeout = 4000)
    public void testAsTimestampWhenUseTimestampNullAndCustomFormatNullAndFeatureEnabled() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider prov = mapper.getSerializerProvider();
        StubSerializer ser = new StubSerializer(null, null);
        assertTrue("_asTimestamp should return true when feature is enabled",
                ser._asTimestamp(prov));
    }

    @Test(timeout = 4000)
    public void testAsTimestampWhenUseTimestampNullAndCustomFormatNullAndFeatureDisabled() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider prov = mapper.getSerializerProvider();
        StubSerializer ser = new StubSerializer(null, null);
        assertFalse("_asTimestamp should return false when feature is disabled",
                ser._asTimestamp(prov));
    }

    @Test(timeout = 4000)
    public void testIsEmptyWithNullValue() {
        StubSerializer ser = new StubSerializer(null, null);
        assertTrue("isEmpty should return true for null value", ser.isEmpty((Date) null));
    }

    @Test(timeout = 4000)
    public void testIsEmptyWithTimestampZero() {
        StubSerializer ser = new StubSerializer(null, null);
        Date epoch = new Date(0);
        assertTrue("isEmpty should return true for timestamp 0", ser.isEmpty(epoch));
    }

    @Test(timeout = 4000)
    public void testIsEmptyWithNonZeroTimestamp() {
        StubSerializer ser = new StubSerializer(null, null);
        Date now = new Date(123456789L);
        assertFalse("isEmpty should return false for non-zero timestamp", ser.isEmpty(now));
    }

    // ========== Partition C: Defect-Targeted Branch ==========
    /**
     * Directly targets the known defect: when shape=STRING without pattern,
     * the serialized timezone offset must contain colon (ISO 8601 extended format).
     * This test reproduces the failing scenario from Defects4J.
     */
    @Test(timeout = 4000)
    public void testFormatWithoutPattern() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Use a timezone that produces non-zero offset (Europe/Paris = GMT+1 in winter)
        mapper.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        // Create a bean with a Date field annotated as STRING shape, no pattern
        DateBean bean = new DateBean();
        bean.date = new Date(0); // 1970-01-01 00:00:00 UTC -> 1970-01-01 01:00:00 in Paris

        // Expected output: {"date":"1970-01-01T01:00:00.000+01:00"}
        String result = mapper.writeValueAsString(bean);
        assertTrue("Result should contain timezone offset with colon, but was: " + result,
                result.contains("+01:00") || result.contains("+02:00")); // DST considerations
        // More precise assertion: the offset should be exactly "+01:00" for winter
        assertTrue("Expected offset with colon (+01:00) but got: " + result,
                result.contains("1970-01-01T01:00:00.000+01:00"));
    }

    // Helper bean for the defect test
    static class DateBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Date date;
    }

    // ========== Partition D: Exception Paths ==========
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAsTimestampWithNullProvider() {
        StubSerializer ser = new StubSerializer(null, null);
        // _useTimestamp is null, _customFormat is null, provider is null -> should throw
        ser._asTimestamp(null);
    }

    // ========== Partition E: Object Lifecycle (partial) ==========
    @Test(timeout = 4000)
    public void testWithFormatReturnsNewInstance() {
        StubSerializer original = new StubSerializer(null, null);
        DateFormat custom = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeSerializerBase<Date> result = original.withFormat(Boolean.TRUE, custom);
        assertNotNull("withFormat should return non-null", result);
        assertNotSame("Should return a different instance", original, result);
        assertTrue("New instance's _useTimestamp should be TRUE", result._useTimestamp.booleanValue());
        assertSame("New instance's _customFormat should be the passed format", custom, result._customFormat);
    }

    @Test(timeout = 4000)
    public void testGetSchemaReturnsNumberWhenAsTimestampTrue() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        StubSerializer ser = new StubSerializer(null, null);
        JsonNode schema = ser.getSchema(mapper.getSerializerProvider(), Date.class);
        assertEquals("getSchema should return 'number' type when asTimestamp is true",
                "number", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testGetSchemaReturnsStringWhenAsTimestampFalse() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        StubSerializer ser = new StubSerializer(null, null);
        JsonNode schema = ser.getSchema(mapper.getSerializerProvider(), Date.class);
        assertEquals("getSchema should return 'string' type when asTimestamp is false",
                "string", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorWhenAsNumber() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider prov = mapper.getSerializerProvider();
        StubSerializer ser = new StubSerializer(null, null);
        // Use a visitor that records the expected calls
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(prov) {
            @Override
            public JsonIntegerFormatVisitor expectIntFormat(JavaType type, JsonParser.NumberType numberType,
                                                            JsonValueFormat format) {
                assertEquals("NumberType should be LONG", JsonParser.NumberType.LONG, numberType);
                assertEquals("Format should be UTC_MILLISEC", JsonValueFormat.UTC_MILLISEC, format);
                return null;
            }
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type, JsonValueFormat format) {
                fail("Should not be called when asNumber is true");
                return null;
            }
        };
        ser.acceptJsonFormatVisitor(visitor, prov.constructType(Date.class));
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorWhenAsString() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider prov = mapper.getSerializerProvider();
        StubSerializer ser = new StubSerializer(null, null);
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base(prov) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType type, JsonValueFormat format) {
                assertEquals("Format should be DATE_TIME", JsonValueFormat.DATE_TIME, format);
                return null;
            }
            @Override
            public JsonIntegerFormatVisitor expectIntFormat(JavaType type, JsonParser.NumberType numberType,
                                                            JsonValueFormat format) {
                fail("Should not be called when asNumber is false");
                return null;
            }
        };
        ser.acceptJsonFormatVisitor(visitor, prov.constructType(Date.class));
    }
}