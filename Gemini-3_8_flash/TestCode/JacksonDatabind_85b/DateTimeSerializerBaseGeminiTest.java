package com.fasterxml.jackson.databind.ser.std;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase
 * Target Defect: DateSerializationTest::testFormatWithoutPattern (databind#1648)
 * Failure Symptom: ComparisonFailure: expected:<{"date":"1970-01-01[X01:00:]00"}>
 *                                  but was:<{"date":"1970-01-01[T01:00:00.000+01]00"}>
 * ----------------------------------------------------------------------------------------------------
 * Decision Points & Branches Covered:
 * 1. createContextual:
 *    - Branch: property == null -> returns this
 *    - Branch: findFormatOverrides(...) == null -> returns this
 *    - Branch: shape.isNumeric() -> withFormat(Boolean.TRUE, null)
 *    - Branch: shape == STRING || hasPattern || hasLocale || hasTimeZone
 *        - format.hasPattern() == true vs false (StdDateFormat.DATE_FORMAT_STR_ISO8601 defect branch)
 *        - format.hasLocale() == true vs false (serializers.getLocale())
 *        - format.getTimeZone() == null vs non-null (serializers.getTimeZone())
 *    - Branch: default non-matching shape/format -> returns this
 * 2. _asTimestamp:
 *    - Branch: _useTimestamp != null (true / false)
 *    - Branch: _customFormat == null && serializers != null -> check WRITE_DATES_AS_TIMESTAMPS
 *    - Branch: _customFormat == null && serializers == null -> throws IllegalArgumentException
 *    - Branch: _customFormat != null -> false
 * 3. isEmpty(value) and isEmpty(serializers, value):
 *    - Branch: value == null -> true
 *    - Branch: _timestamp(value) == 0L -> true
 *    - Branch: _timestamp(value) != 0L -> false
 * 4. getSchema:
 *    - Branch: _asTimestamp() == true ("number") vs false ("string")
 * 5. acceptJsonFormatVisitor / _acceptJsonFormatVisitor:
 *    - Branch: asNumber == true -> visitIntFormat (LONG, UTC_MILLISEC)
 *    - Branch: asNumber == false -> visitStringFormat (DATE_TIME)
 *    - Branch: visitor == null -> defensive null check in helper
 * ====================================================================================================
 */

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

public class DateTimeSerializerBaseGeminiTest {

    // Concrete test implementation of abstract DateTimeSerializerBase
    static class ConcreteDateTimeSerializer extends DateTimeSerializerBase<Date> {
        private static final long serialVersionUID = 1L;

        public ConcreteDateTimeSerializer() {
            super(Date.class, null, null);
        }

        public ConcreteDateTimeSerializer(Boolean useTimestamp, DateFormat customFormat) {
            super(Date.class, useTimestamp, customFormat);
        }

        @Override
        public DateTimeSerializerBase<Date> withFormat(Boolean timestamp, DateFormat customFormat) {
            return new ConcreteDateTimeSerializer(timestamp, customFormat);
        }

        @Override
        protected long _timestamp(Date value) {
            return value == null ? 0L : value.getTime();
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (_asTimestamp(serializers)) {
                gen.writeNumber(_timestamp(value));
            } else if (_customFormat != null) {
                synchronized (_customFormat) {
                    gen.writeString(_customFormat.format(value));
                }
            } else {
                serializers.defaultSerializeDateValue(value, gen);
            }
        }

        public boolean exposedAsTimestamp(SerializerProvider serializers) {
            return _asTimestamp(serializers);
        }

        public void exposedAcceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint, boolean asNumber)
                throws Exception {
            _acceptJsonFormatVisitor(visitor, typeHint, asNumber);
        }

        public Boolean getUseTimestamp() {
            return _useTimestamp;
        }

        public DateFormat getCustomFormat() {
            return _customFormat;
        }
    }

    // Tracking visitor to assert acceptJsonFormatVisitor traversal paths
    static class TrackingFormatVisitor extends JsonFormatVisitorWrapper.Base {
        boolean visitedInt = false;
        boolean visitedString = false;
        JsonParser.NumberType numberType;
        JsonValueFormat valueFormat;

        public TrackingFormatVisitor(SerializerProvider provider) {
            super(provider);
        }

        @Override
        public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
            visitedInt = true;
            return new JsonIntegerFormatVisitor.Base() {
                @Override
                public void numberType(JsonParser.NumberType type) {
                    numberType = type;
                }

                @Override
                public void format(JsonValueFormat format) {
                    valueFormat = format;
                }
            };
        }

        @Override
        public JsonStringFormatVisitor expectStringFormat(JavaType type) {
            visitedString = true;
            return new JsonStringFormatVisitor.Base() {
                @Override
                public void format(JsonValueFormat format) {
                    valueFormat = format;
                }
            };
        }
    }

    // POJOs for format inspection and serialization tests
    public static class DateWrapperString {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Date date;
        public DateWrapperString() {}
        public DateWrapperString(Date date) { this.date = date; }
    }

    public static class DateWrapperNumeric {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Date date;
        public DateWrapperNumeric() {}
        public DateWrapperNumeric(Date date) { this.date = date; }
    }

    public static class DateWrapperPattern {
        @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
        public Date date;
        public DateWrapperPattern() {}
        public DateWrapperPattern(Date date) { this.date = date; }
    }

    public static class DateWrapperLocaleAndTz {
        @JsonFormat(shape = JsonFormat.Shape.STRING, locale = "fr", timezone = "GMT+2")
        public Date date;
        public DateWrapperLocaleAndTz() {}
        public DateWrapperLocaleAndTz(Date date) { this.date = date; }
    }

    public static class DateWrapperTzOnly {
        @JsonFormat(timezone = "UTC")
        public Date date;
        public DateWrapperTzOnly() {}
        public DateWrapperTzOnly(Date date) { this.date = date; }
    }

    public static class DateWrapperPlain {
        public Date date;
        public DateWrapperPlain() {}
        public DateWrapperPlain(Date date) { this.date = date; }
    }

    /*
     * --------------------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * --------------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testAsTimestampResolution() {
        ConcreteDateTimeSerializer serTrue = new ConcreteDateTimeSerializer(Boolean.TRUE, null);
        assertTrue("Explicit useTimestamp=TRUE must resolve as timestamp", serTrue.exposedAsTimestamp(null));

        ConcreteDateTimeSerializer serFalse = new ConcreteDateTimeSerializer(Boolean.FALSE, null);
        assertFalse("Explicit useTimestamp=FALSE must not resolve as timestamp", serFalse.exposedAsTimestamp(null));

        ConcreteDateTimeSerializer serCustomFormat = new ConcreteDateTimeSerializer(null, new SimpleDateFormat("yyyy-MM-dd"));
        assertFalse("When custom format is specified, asTimestamp must be false", serCustomFormat.exposedAsTimestamp(null));

        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider provider = ((DefaultSerializerProvider) mapper.getSerializerProvider())
                .createInstance(mapper.getSerializationConfig(), mapper.getSerializerFactory());

        ConcreteDateTimeSerializer serDefault = new ConcreteDateTimeSerializer(null, null);
        assertTrue("Default config has WRITE_DATES_AS_TIMESTAMPS enabled", serDefault.exposedAsTimestamp(provider));

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        DefaultSerializerProvider providerNoTs = ((DefaultSerializerProvider) mapper.getSerializerProvider())
                .createInstance(mapper.getSerializationConfig(), mapper.getSerializerFactory());
        assertFalse("Disabled WRITE_DATES_AS_TIMESTAMPS must yield false", serDefault.exposedAsTimestamp(providerNoTs));
    }

    @Test(timeout = 4000)
    public void testGetSchemaBranches() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider provider = ((DefaultSerializerProvider) mapper.getSerializerProvider())
                .createInstance(mapper.getSerializationConfig(), mapper.getSerializerFactory());

        ConcreteDateTimeSerializer serNumeric = new ConcreteDateTimeSerializer(Boolean.TRUE, null);
        JsonNode numberSchema = serNumeric.getSchema(provider, null);
        assertEquals("number", numberSchema.get("type").asText());
        assertTrue(numberSchema.get("required").asBoolean());

        ConcreteDateTimeSerializer serString = new ConcreteDateTimeSerializer(Boolean.FALSE, null);
        JsonNode stringSchema = serString.getSchema(provider, null);
        assertEquals("string", stringSchema.get("type").asText());
        assertTrue(stringSchema.get("required").asBoolean());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorBranches() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider provider = ((DefaultSerializerProvider) mapper.getSerializerProvider())
                .createInstance(mapper.getSerializationConfig(), mapper.getSerializerFactory());

        ConcreteDateTimeSerializer serTimestamp = new ConcreteDateTimeSerializer(Boolean.TRUE, null);
        TrackingFormatVisitor visitorInt = new TrackingFormatVisitor(provider);
        serTimestamp.acceptJsonFormatVisitor(visitorInt, null);
        assertTrue(visitorInt.visitedInt);
        assertFalse(visitorInt.visitedString);
        assertEquals(JsonParser.NumberType.LONG, visitorInt.numberType);
        assertEquals(JsonValueFormat.UTC_MILLISEC, visitorInt.valueFormat);

        ConcreteDateTimeSerializer serText = new ConcreteDateTimeSerializer(Boolean.FALSE, null);
        TrackingFormatVisitor visitorStr = new TrackingFormatVisitor(provider);
        serText.acceptJsonFormatVisitor(visitorStr, null);
        assertFalse(visitorStr.visitedInt);
        assertTrue(visitorStr.visitedString);
        assertEquals(JsonValueFormat.DATE_TIME, visitorStr.valueFormat);
    }

    @Test(timeout = 4000)
    public void testActualSerializationExecution() throws IOException {
        ConcreteDateTimeSerializer serNumeric = new ConcreteDateTimeSerializer(Boolean.TRUE, null);
        StringWriter sw1 = new StringWriter();
        JsonGenerator gen1 = new JsonFactory().createGenerator(sw1);
        serNumeric.serialize(new Date(12345L), gen1, null);
        gen1.close();
        assertEquals("12345", sw1.toString());

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        ConcreteDateTimeSerializer serCustom = new ConcreteDateTimeSerializer(null, df);
        StringWriter sw2 = new StringWriter();
        JsonGenerator gen2 = new JsonFactory().createGenerator(sw2);
        serCustom.serialize(new Date(0L), gen2, null);
        gen2.close();
        assertEquals("\"1970/01/01\"", sw2.toString());
    }

    /*
     * --------------------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * --------------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testIsEmptyBoundaries() {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();

        // Deprecated isEmpty(T)
        assertTrue("null value must be empty", ser.isEmpty((Date) null));
        assertTrue("Epoch zero date (timestamp 0) must be empty", ser.isEmpty(new Date(0L)));
        assertFalse("Positive timestamp must not be empty", ser.isEmpty(new Date(1L)));
        assertFalse("Negative timestamp must not be empty", ser.isEmpty(new Date(-1L)));
        assertFalse("Max timestamp boundary must not be empty", ser.isEmpty(new Date(Long.MAX_VALUE)));

        // Contextual isEmpty(SerializerProvider, T)
        assertTrue("Contextual null value must be empty", ser.isEmpty(null, null));
        assertTrue("Contextual Epoch zero date must be empty", ser.isEmpty(null, new Date(0L)));
        assertFalse("Contextual positive timestamp must not be empty", ser.isEmpty(null, new Date(1L)));
        assertFalse("Contextual negative timestamp must not be empty", ser.isEmpty(null, new Date(-1L)));
        assertFalse("Contextual Max timestamp boundary must not be empty", ser.isEmpty(null, new Date(Long.MAX_VALUE)));
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorNullVisitorHelper() throws Exception {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();
        // Direct call to helper method must be resilient when visitor is null
        ser.exposedAcceptJsonFormatVisitor(null, null, true);
        ser.exposedAcceptJsonFormatVisitor(null, null, false);
    }

    /*
     * --------------------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone (databind#1648)
     * --------------------------------------------------------------------------------
     */

    /**
     * Targets DateSerializationTest::testFormatWithoutPattern.
     * When ObjectMapper has a custom DateFormat configured and a property specifies
     * Shape.STRING without a pattern, DateTimeSerializerBase must not clobber the
     * custom DateFormat's pattern with StdDateFormat.DATE_FORMAT_STR_ISO8601.
     */
    @Test(timeout = 4000)
    public void testFormatWithoutPattern() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'X'HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        mapper.setDateFormat(df);

        DateWrapperString wrapper = new DateWrapperString(new Date(0L));
        String json = mapper.writeValueAsString(wrapper);

        assertEquals("{\"date\":\"1970-01-01X01:00:00\"}", json);
    }

    /*
     * --------------------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * --------------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testAsTimestampThrowsOnNullSerializerProvider() {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(null, null);
        try {
            ser.exposedAsTimestamp(null);
            fail("Expected IllegalArgumentException when SerializerProvider is null and no explicit config exists");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Null SerializerProvider passed for java.util.Date"));
        }
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorThrowsNpeWhenVisitorNull() throws Exception {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();
        try {
            ser.acceptJsonFormatVisitor(null, null);
            fail("Expected NullPointerException when visitor is null because visitor.getProvider() is dereferenced");
        } catch (NullPointerException expected) {
            assertNotNull(expected);
        }
    }

    /*
     * --------------------------------------------------------------------------------
     * Partition E: Contextual Lifecycle & Configuration Branches
     * --------------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testCreateContextualWithNullPropertyReturnsSame() throws Exception {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider provider = ((DefaultSerializerProvider) mapper.getSerializerProvider())
                .createInstance(mapper.getSerializationConfig(), mapper.getSerializerFactory());

        JsonSerializer<?> contextual = ser.createContextual(provider, null);
        assertSame("Null property must return unmodified serializer instance", ser, contextual);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithEmptyPropertyReturnsSame() throws Exception {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider provider = ((DefaultSerializerProvider) mapper.getSerializerProvider())
                .createInstance(mapper.getSerializationConfig(), mapper.getSerializerFactory());

        BeanProperty.Bogus bogus = new BeanProperty.Bogus();
        JsonSerializer<?> contextual = ser.createContextual(provider, bogus);
        assertSame("Property without format overrides must return same serializer instance", ser, contextual);
    }

    @Test(timeout = 4000)
    public void testSerializationWithNumericShapeOverride() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        DateWrapperNumeric wrapper = new DateWrapperNumeric(new Date(9999L));
        String json = mapper.writeValueAsString(wrapper);
        assertEquals("{\"date\":9999}", json);
    }

    @Test(timeout = 4000)
    public void testSerializationWithPatternAnnotation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));

        DateWrapperPattern wrapper = new DateWrapperPattern(new Date(0L));
        String json = mapper.writeValueAsString(wrapper);
        assertEquals("{\"date\":\"1970/01/01 00:00:00\"}", json);
    }

    @Test(timeout = 4000)
    public void testSerializationWithLocaleAndTimeZone() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DateWrapperLocaleAndTz wrapper = new DateWrapperLocaleAndTz(new Date(0L));
        String json = mapper.writeValueAsString(wrapper);

        // Epoch 0 in GMT+2 is 02:00:00
        assertTrue("Must contain formatted time with GMT+2 offset",
                json.contains("1970-01-01T02:00:00.000+0200") || json.contains("1970-01-01T02:00:00.000+02:00"));
    }

    @Test(timeout = 4000)
    public void testSerializationWithTimeZoneOnly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        DateWrapperTzOnly wrapper = new DateWrapperTzOnly(new Date(0L));
        String json = mapper.writeValueAsString(wrapper);

        // Explicit UTC on property overrides GMT+8 from mapper
        assertTrue("Must format in UTC (00:00:00)",
                json.contains("1970-01-01T00:00:00.000+0000") || json.contains("1970-01-01T00:00:00.000Z")
                        || json.contains("1970-01-01T00:00:00.000+00:00"));
    }

    @Test(timeout = 4000)
    public void testWithFormatImmutability() {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();
        SimpleDateFormat df = new SimpleDateFormat("yyyy");

        DateTimeSerializerBase<Date> modified = ser.withFormat(Boolean.TRUE, df);
        assertNotSame("withFormat must return a new instance", ser, modified);

        ConcreteDateTimeSerializer concreteModified = (ConcreteDateTimeSerializer) modified;
        assertEquals(Boolean.TRUE, concreteModified.getUseTimestamp());
        assertSame(df, concreteModified.getCustomFormat());
        assertNull(ser.getUseTimestamp());
        assertNull(ser.getCustomFormat());
    }
}