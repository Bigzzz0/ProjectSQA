package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.util.StdDateFormat;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.ser.std.DateTimeSerializerBase
 *
 * 1. Branch: createContextual(SerializerProvider, BeanProperty)
 *    - Case property == null with format overrides (Defects4J ground truth: testSqlDateConfigOverride).
 *      Prior to fix, early return `if (property == null) return this;` prevented finding type-level config overrides.
 *    - Case property == null without format overrides: returns `this`.
 *    - Case format == null: returns `this`.
 *    - Case shape.isNumeric(): returns withFormat(Boolean.TRUE, null).
 *    - Case format.hasPattern():
 *        * with locale / without locale (fallback to serializers.getLocale()).
 *        * with timezone / without timezone (fallback to serializers.getTimeZone()).
 *    - Case no pattern:
 *        * !hasLocale && !hasTZ && !asString: returns `this`.
 *        * df0 instanceof StdDateFormat: with locale, with timezone, or both.
 *        * df0 instanceof SimpleDateFormat:
 *            - hasLocale == true: re-creates SimpleDateFormat with new locale.
 *            - hasLocale == false: clones SimpleDateFormat.
 *            - changeTZ == true vs false.
 *        * df0 NOT SimpleDateFormat and NOT StdDateFormat: triggers reportBadDefinition (JsonMappingException).
 *
 * 2. Branch: _asTimestamp(SerializerProvider)
 *    - _useTimestamp != null: returns boolean value directly (TRUE / FALSE).
 *    - _useTimestamp == null && _customFormat == null:
 *        * serializers != null: returns serializers.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).
 *        * serializers == null: throws IllegalArgumentException("Null SerializerProvider passed for ...").
 *    - _useTimestamp == null && _customFormat != null: returns false.
 *
 * 3. Branch: _serializeAsString(Date, JsonGenerator, SerializerProvider)
 *    - _customFormat == null: delegates to provider.defaultSerializeDateValue.
 *    - _customFormat != null:
 *        * _reusedCustomFormat contains cached instance: successfully retrieved and reused.
 *        * _reusedCustomFormat empty: clones _customFormat, formats, then caches instance.
 *
 * 4. Branch: acceptJsonFormatVisitor / _acceptJsonFormatVisitor
 *    - asNumber == true: calls visitIntFormat with LONG, UTC_MILLISEC.
 *    - asNumber == false: calls visitStringFormat with DATE_TIME.
 *
 * 5. Branch: isEmpty(SerializerProvider, T)
 *    - Always returns false.
 *
 * 6. Branch: getSchema(SerializerProvider, Type)
 *    - returns "number" if _asTimestamp == true, "string" otherwise.
 */
public class DateTimeSerializerBaseGeminiTest {

    // Concrete subclass to exercise abstract methods of DateTimeSerializerBase
    static class ConcreteDateTimeSerializer extends DateTimeSerializerBase<Date> {
        public ConcreteDateTimeSerializer() {
            this(Date.class, null, null);
        }

        public ConcreteDateTimeSerializer(Boolean useTimestamp, DateFormat customFormat) {
            super(Date.class, useTimestamp, customFormat);
        }

        public ConcreteDateTimeSerializer(Class<Date> type, Boolean useTimestamp, DateFormat customFormat) {
            super(type, useTimestamp, customFormat);
        }

        @Override
        public DateTimeSerializerBase<Date> withFormat(Boolean timestamp, DateFormat customFormat) {
            return new ConcreteDateTimeSerializer(handledType(), timestamp, customFormat);
        }

        @Override
        protected long _timestamp(Date value) {
            return (value == null) ? 0L : value.getTime();
        }

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (_asTimestamp(serializers)) {
                gen.writeNumber(_timestamp(value));
            } else {
                _serializeAsString(value, gen, serializers);
            }
        }
    }

    // Visitor stub to verify visitor branch invocations
    static class TrackingVisitor extends JsonFormatVisitorWrapper.Base {
        boolean visitedInteger = false;
        boolean visitedString = false;
        JsonParser.NumberType numberType = null;
        JsonValueFormat valueFormat = null;

        public TrackingVisitor(SerializerProvider prov) {
            super(prov);
        }

        @Override
        public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
            visitedInteger = true;
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

    // Test POJOs for Contextual Resolution
    static class PojoNumericShape {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Date date;
        public PojoNumericShape(Date d) { this.date = d; }
    }

    static class PojoPatternWithoutLocaleTz {
        @JsonFormat(pattern = "yyyy/MM/dd")
        public Date date;
        public PojoPatternWithoutLocaleTz(Date d) { this.date = d; }
    }

    static class PojoPatternWithLocaleAndTz {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm", locale = "fr", timezone = "GMT+2")
        public Date date;
        public PojoPatternWithLocaleAndTz(Date d) { this.date = d; }
    }

    static class PojoStringShapeOnly {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Date date;
        public PojoStringShapeOnly(Date d) { this.date = d; }
    }

    static class PojoTimezoneOnly {
        @JsonFormat(timezone = "GMT+5")
        public Date date;
        public PojoTimezoneOnly(Date d) { this.date = d; }
    }

    static class PojoLocaleOnly {
        @JsonFormat(locale = "de")
        public Date date;
        public PojoLocaleOnly(Date d) { this.date = d; }
    }

    static class PojoEmptyFormat {
        @JsonFormat
        public Date date;
        public PojoEmptyFormat(Date d) { this.date = d; }
    }

    private DefaultSerializerProvider createSerializerProvider(ObjectMapper mapper) {
        DefaultSerializerProvider prov = (DefaultSerializerProvider) mapper.getSerializerProvider();
        return prov.createInstance(mapper.getSerializationConfig(), mapper.getSerializerFactory());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationAsTimestampEnabledByDefault() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Date date = new Date(123456789L);
        String json = mapper.writeValueAsString(date);
        assertEquals("123456789", json);
    }

    @Test(timeout = 4000)
    public void testSerializationAsTimestampDisabled() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(0L);
        String json = mapper.writeValueAsString(date);
        assertEquals("\"1970-01-01T00:00:00.000+0000\"", json);
    }

    @Test(timeout = 4000)
    public void testContextualNumericShape() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        PojoNumericShape pojo = new PojoNumericShape(new Date(1000L));
        String json = mapper.writeValueAsString(pojo);
        assertEquals("{\"date\":1000}", json);
    }

    @Test(timeout = 4000)
    public void testContextualPatternWithLocaleAndTz() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Date date = new Date(0L); // 1970-01-01 02:00 in GMT+2
        PojoPatternWithLocaleAndTz pojo = new PojoPatternWithLocaleAndTz(date);
        String json = mapper.writeValueAsString(pojo);
        assertEquals("{\"date\":\"1970-01-01 02:00\"}", json);
    }

    @Test(timeout = 4000)
    public void testContextualPatternWithoutLocaleTzUsesProviderDefaults() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(0L);
        PojoPatternWithoutLocaleTz pojo = new PojoPatternWithoutLocaleTz(date);
        String json = mapper.writeValueAsString(pojo);
        assertEquals("{\"date\":\"1970/01/01\"}", json);
    }

    @Test(timeout = 4000)
    public void testContextualStdDateFormatLocaleAndTzModifiers() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Jackson default format is StdDateFormat
        Date date = new Date(0L);
        PojoTimezoneOnly pojoTz = new PojoTimezoneOnly(date);
        String jsonTz = mapper.writeValueAsString(pojoTz);
        assertTrue("Output should reflect GMT+5", jsonTz.contains("+0500") || jsonTz.contains("+05:00"));

        PojoLocaleOnly pojoLoc = new PojoLocaleOnly(date);
        String jsonLoc = mapper.writeValueAsString(pojoLoc);
        assertNotNull(jsonLoc);
    }

    @Test(timeout = 4000)
    public void testContextualSimpleDateFormatCloneAndModify() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.setDateFormat(sdf);

        // Case 1: hasLocale -> creates new SimpleDateFormat
        Date date = new Date(0L);
        PojoLocaleOnly pojoLocale = new PojoLocaleOnly(date);
        String jsonLocale = mapper.writeValueAsString(pojoLocale);
        assertEquals("{\"date\":\"1970/01/01 00:00:00\"}", jsonLocale);

        // Case 2: hasTimeZone -> clones and sets new timezone
        PojoTimezoneOnly pojoTz = new PojoTimezoneOnly(date);
        String jsonTz = mapper.writeValueAsString(pojoTz);
        assertEquals("{\"date\":\"1970/01/01 05:00:00\"}", jsonTz);
    }

    @Test(timeout = 4000)
    public void testCustomFormatReuseAndCloneOnConcurrentEmpty() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(null, sdf);

        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider prov = createSerializerProvider(mapper);

        // First call populates/reuses format
        StringWriter sw1 = new StringWriter();
        JsonGenerator g1 = mapper.getFactory().createGenerator(sw1);
        ser.serialize(new Date(0L), g1, prov);
        g1.close();
        assertEquals("\"1970-01-01\"", sw1.toString());

        // Drain the reused format manually to simulate concurrent branch where f == null
        ser._reusedCustomFormat.set(null);
        StringWriter sw2 = new StringWriter();
        JsonGenerator g2 = mapper.getFactory().createGenerator(sw2);
        ser.serialize(new Date(86400000L), g2, prov);
        g2.close();
        assertEquals("\"1970-01-02\"", sw2.toString());
        assertNotNull("Reused format reference should be restored", ser._reusedCustomFormat.get());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsEmptyAlwaysReturnsFalse() {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();
        assertFalse(ser.isEmpty(null, null));
        assertFalse(ser.isEmpty(null, new Date(0L)));
        assertFalse(ser.isEmpty(null, new Date(Long.MIN_VALUE)));
        assertFalse(ser.isEmpty(null, new Date(Long.MAX_VALUE)));
    }

    @Test(timeout = 4000)
    public void testAsTimestampExplicitBooleanFlags() {
        ConcreteDateTimeSerializer trueSer = new ConcreteDateTimeSerializer(Boolean.TRUE, null);
        assertTrue(trueSer._asTimestamp(null));

        ConcreteDateTimeSerializer falseSer = new ConcreteDateTimeSerializer(Boolean.FALSE, null);
        assertFalse(falseSer._asTimestamp(null));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        ConcreteDateTimeSerializer customFormatSer = new ConcreteDateTimeSerializer(null, sdf);
        assertFalse(customFormatSer._asTimestamp(null));
    }

    @Test(timeout = 4000)
    public void testGetSchemaNodeTypes() {
        ConcreteDateTimeSerializer timestampSer = new ConcreteDateTimeSerializer(Boolean.TRUE, null);
        JsonNode numberSchema = timestampSer.getSchema(null, Date.class);
        assertEquals("number", numberSchema.get("type").asText());

        ConcreteDateTimeSerializer stringSer = new ConcreteDateTimeSerializer(Boolean.FALSE, null);
        JsonNode stringSchema = stringSer.getSchema(null, Date.class);
        assertEquals("string", stringSchema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorBranches() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider prov = createSerializerProvider(mapper);

        // Visitor with asNumber = true
        ConcreteDateTimeSerializer timestampSer = new ConcreteDateTimeSerializer(Boolean.TRUE, null);
        TrackingVisitor intVisitor = new TrackingVisitor(prov);
        timestampSer.acceptJsonFormatVisitor(intVisitor, mapper.constructType(Date.class));
        assertTrue(intVisitor.visitedInteger);
        assertEquals(JsonParser.NumberType.LONG, intVisitor.numberType);
        assertEquals(JsonValueFormat.UTC_MILLISEC, intVisitor.valueFormat);

        // Visitor with asNumber = false
        ConcreteDateTimeSerializer stringSer = new ConcreteDateTimeSerializer(Boolean.FALSE, null);
        TrackingVisitor strVisitor = new TrackingVisitor(prov);
        stringSer.acceptJsonFormatVisitor(strVisitor, mapper.constructType(Date.class));
        assertTrue(strVisitor.visitedString);
        assertEquals(JsonValueFormat.DATE_TIME, strVisitor.valueFormat);
    }

    @Test(timeout = 4000)
    public void testContextualReturnsSameWhenNoFormatOrEmptyFormat() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider prov = createSerializerProvider(mapper);
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer();

        // When BeanProperty is null without overrides
        assertSame(ser, ser.createContextual(prov, null));

        // When BeanProperty has empty format
        PojoEmptyFormat pojo = new PojoEmptyFormat(new Date(0L));
        String json = mapper.writeValueAsString(pojo);
        assertEquals("{\"date\":0}", json);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Defects4J Defect Target: SqlDateSerializationTest::testSqlDateConfigOverride
     * Root value config override must NOT be ignored when property is null.
     */
    @Test(timeout = 4000)
    public void testSqlDateConfigOverride() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.configOverride(java.sql.Date.class)
                .setFormat(JsonFormat.Value.forPattern("yyyy+MM+dd"));

        java.sql.Date date = new java.sql.Date(324547200000L); // 1980-04-14 UTC
        String json = mapper.writeValueAsString(date);
        assertEquals("\"1980+04+14\"", json);
    }

    /**
     * Root-level config override targeting DateTimeSerializerBase via java.util.Date.
     */
    @Test(timeout = 4000)
    public void testDateConfigOverrideRootValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.configOverride(Date.class)
                .setFormat(JsonFormat.Value.forPattern("yyyy/MM/dd"));

        Date date = new Date(0L);
        String json = mapper.writeValueAsString(date);
        assertEquals("\"1970/01/01\"", json);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testAsTimestampThrowsIllegalArgumentExceptionWhenProviderNull() {
        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(null, null);
        try {
            ser._asTimestamp(null);
            fail("Expected IllegalArgumentException on null SerializerProvider");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Null SerializerProvider passed for java.util.Date"));
        }
    }

    @Test(timeout = 4000)
    public void testReportBadDefinitionWhenDateFormatNotSimpleDateFormatNorStdDateFormat() {
        ObjectMapper mapper = new ObjectMapper();
        DateFormat unsupportedDateFormat = new DateFormat() {
            @Override
            public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
                return toAppendTo.append("unsupported");
            }
            @Override
            public Date parse(String source, ParsePosition pos) {
                return null;
            }
        };
        mapper.setDateFormat(unsupportedDateFormat);

        try {
            mapper.writeValueAsString(new PojoStringShapeOnly(new Date(0L)));
            fail("Expected JsonMappingException due to unsupported DateFormat class");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("not a `SimpleDateFormat`"));
        } catch (IOException unexpected) {
            fail("Unexpected IOException: " + unexpected.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithFormatContract() {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
        SimpleDateFormat sdf2 = new SimpleDateFormat("MM");

        ConcreteDateTimeSerializer base = new ConcreteDateTimeSerializer(null, sdf1);
        DateTimeSerializerBase<Date> modified = base.withFormat(Boolean.TRUE, sdf2);

        assertNotSame(base, modified);
        assertTrue(modified._useTimestamp);
        assertEquals(sdf2, modified._customFormat);
        assertNotNull(modified._reusedCustomFormat);

        DateTimeSerializerBase<Date> nullFormat = base.withFormat(Boolean.FALSE, null);
        assertNull(nullFormat._customFormat);
        assertNull(nullFormat._reusedCustomFormat);
    }

    @Test(timeout = 4000)
    public void testSerializeAsStringDelegationWhenCustomFormatNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));
        DefaultSerializerProvider prov = createSerializerProvider(mapper);

        ConcreteDateTimeSerializer ser = new ConcreteDateTimeSerializer(Boolean.FALSE, null);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);

        ser.serialize(new Date(0L), gen, prov);
        gen.close();

        assertEquals("\"1970-01-01T00:00:00.000+0000\"", sw.toString());
    }
}