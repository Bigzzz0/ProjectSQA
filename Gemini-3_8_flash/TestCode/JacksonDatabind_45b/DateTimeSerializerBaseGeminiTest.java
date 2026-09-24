package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.*;

/*
 [Branch & Defect Analysis Matrix]
 ------------------------------------------------------------------------------------------------------------------
 Target: DateTimeSerializerBase & Subclass Contract
 1. createContextual():
    - property == null -> returns this
    - property != null, findFormat() == null -> returns this
    - format.getShape().isNumeric() (e.g., NUMBER, NUMBER_INT, NUMBER_FLOAT) -> withFormat(Boolean.TRUE, null)
    - format.getShape() == STRING with hasPattern(), hasLocale(), getTimeZone() (both present and null fallback)
    - DEFECT Branch (Jackson-databind DateSerializationTest#testDateDefaultShape):
      When @JsonFormat has pattern/locale/timezone but shape is ANY (default), format.getShape() == STRING is false,
      causing the serializer to ignore the pattern and serialize as timestamp or default format!
 2. isEmpty():
    - isEmpty(T) & isEmpty(SerializerProvider, T):
      - value == null -> true
      - _timestamp(value) == 0L -> true
      - _timestamp(value) != 0L -> false
 3. _asTimestamp():
    - _useTimestamp != null -> returns _useTimestamp.booleanValue()
    - _useTimestamp == null, _customFormat == null, serializers != null -> serializers.isEnabled(WRITE_DATES_AS_TIMESTAMPS)
    - _useTimestamp == null, _customFormat == null, serializers == null -> IllegalArgumentException
    - _useTimestamp == null, _customFormat != null -> false
 4. getSchema():
    - _asTimestamp(serializers) == true -> "number"
    - _asTimestamp(serializers) == false -> "string"
 5. acceptJsonFormatVisitor():
    - asNumber == true -> visitIntFormat(visitor, LONG, UTC_MILLISEC)
    - asNumber == false -> visitStringFormat(visitor, DATE_TIME)
 ------------------------------------------------------------------------------------------------------------------
*/
public class DateTimeSerializerBaseGeminiTest {

    // Concrete test implementation of DateTimeSerializerBase
    private static class StubDateTimeSerializer extends DateTimeSerializerBase<Date> {
        private static final long serialVersionUID = 1L;

        public StubDateTimeSerializer() {
            this(null, null);
        }

        public StubDateTimeSerializer(Boolean useTimestamp, DateFormat customFormat) {
            super(Date.class, useTimestamp, customFormat);
        }

        @Override
        public DateTimeSerializerBase<Date> withFormat(Boolean timestamp, DateFormat customFormat) {
            return new StubDateTimeSerializer(timestamp, customFormat);
        }

        @Override
        protected long _timestamp(Date value) {
            return (value == null) ? 0L : value.getTime();
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

        public Boolean getUseTimestamp() {
            return _useTimestamp;
        }

        public DateFormat getCustomFormat() {
            return _customFormat;
        }
    }

    // Helper Beans for serialization testing
    static class DateDefaultShapeBean {
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
        public Date date;

        public DateDefaultShapeBean(Date date) {
            this.date = date;
        }
    }

    static class DateExplicitStringBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", locale = "fr", timezone = "GMT+1")
        public Date date;

        public DateExplicitStringBean(Date date) {
            this.date = date;
        }
    }

    static class DateNumericShapeBean {
        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public Date date;

        public DateNumericShapeBean(Date date) {
            this.date = date;
        }
    }

    static class DateIsoFallbackBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public Date date;

        public DateIsoFallbackBean(Date date) {
            this.date = date;
        }
    }

    static class DateNoFormatBean {
        public Date date;

        public DateNoFormatBean(Date date) {
            this.date = date;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsEmptyWithNullAndEpochAndValidDate() {
        StubDateTimeSerializer ser = new StubDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        // Deprecated isEmpty(T)
        assertTrue(ser.isEmpty((Date) null));
        assertTrue(ser.isEmpty(new Date(0L)));
        assertFalse(ser.isEmpty(new Date(123456789L)));
        assertFalse(ser.isEmpty(new Date(-123456789L)));

        // Contextual isEmpty(SerializerProvider, T)
        assertTrue(ser.isEmpty(prov, null));
        assertTrue(ser.isEmpty(prov, new Date(0L)));
        assertFalse(ser.isEmpty(prov, new Date(123456789L)));
        assertFalse(ser.isEmpty(prov, new Date(-123456789L)));
    }

    @Test(timeout = 4000)
    public void testAsTimestampResolutionWithUseTimestampFlag() {
        StubDateTimeSerializer serTrue = new StubDateTimeSerializer(Boolean.TRUE, null);
        StubDateTimeSerializer serFalse = new StubDateTimeSerializer(Boolean.FALSE, null);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        assertTrue(serTrue._asTimestamp(prov));
        assertTrue(serTrue._asTimestamp(null));

        assertFalse(serFalse._asTimestamp(prov));
        assertFalse(serFalse._asTimestamp(null));
    }

    @Test(timeout = 4000)
    public void testAsTimestampResolutionWithCustomFormat() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        StubDateTimeSerializer ser = new StubDateTimeSerializer(null, df);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        assertFalse(ser._asTimestamp(prov));
        assertFalse(ser._asTimestamp(null));
    }

    @Test(timeout = 4000)
    public void testAsTimestampResolutionWithProviderSetting() {
        StubDateTimeSerializer ser = new StubDateTimeSerializer(null, null);
        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider provTimestamps = mapper.getSerializerProviderInstance();
        assertTrue(ser._asTimestamp(provTimestamps));

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SerializerProvider provStrings = mapper.getSerializerProviderInstance();
        assertFalse(ser._asTimestamp(provStrings));
    }

    @Test(timeout = 4000)
    public void testGetSchemaReturnsNumberWhenTimestamp() {
        StubDateTimeSerializer ser = new StubDateTimeSerializer(Boolean.TRUE, null);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonNode schema = ser.getSchema(prov, Date.class);
        assertNotNull(schema);
        assertEquals("number", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testGetSchemaReturnsStringWhenNotTimestamp() {
        StubDateTimeSerializer ser = new StubDateTimeSerializer(Boolean.FALSE, null);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonNode schema = ser.getSchema(prov, Date.class);
        assertNotNull(schema);
        assertEquals("string", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorBranches() throws Exception {
        JavaType type = TypeFactory.defaultInstance().constructType(Date.class);
        ObjectMapper mapper = new ObjectMapper();

        // 1. Visitor as Number
        final boolean[] intVisited = new boolean[1];
        JsonFormatVisitorWrapper intVisitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType typeHint) {
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        assertEquals(JsonParser.NumberType.LONG, type);
                    }

                    @Override
                    public void format(JsonValueFormat format) {
                        assertEquals(JsonValueFormat.UTC_MILLISEC, format);
                        intVisited[0] = true;
                    }
                };
            }
        };

        StubDateTimeSerializer serTimestamp = new StubDateTimeSerializer(Boolean.TRUE, null);
        serTimestamp.acceptJsonFormatVisitor(intVisitor, type);
        assertTrue("Integer visitor format should have been invoked", intVisited[0]);

        // 2. Visitor as String
        final boolean[] stringVisited = new boolean[1];
        JsonFormatVisitorWrapper stringVisitor = new JsonFormatVisitorWrapper.Base(mapper.getSerializerProviderInstance()) {
            @Override
            public JsonStringFormatVisitor expectStringFormat(JavaType typeHint) {
                return new JsonStringFormatVisitor.Base() {
                    @Override
                    public void format(JsonValueFormat format) {
                        assertEquals(JsonValueFormat.DATE_TIME, format);
                        stringVisited[0] = true;
                    }
                };
            }
        };

        StubDateTimeSerializer serString = new StubDateTimeSerializer(Boolean.FALSE, null);
        serString.acceptJsonFormatVisitor(stringVisitor, type);
        assertTrue("String visitor format should have been invoked", stringVisited[0]);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateContextualWithNullPropertyReturnsThis() throws Exception {
        StubDateTimeSerializer ser = new StubDateTimeSerializer();
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonSerializer<?> contextual = ser.createContextual(prov, null);
        assertSame(ser, contextual);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithPropertyLackingFormatAnnotation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType javaType = mapper.constructType(DateNoFormatBean.class);
        BeanProperty prop = new BeanProperty.Std(
                mapper.getPropertyNamingStrategy(),
                javaType,
                null,
                null,
                null,
                false
        );

        StubDateTimeSerializer ser = new StubDateTimeSerializer();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonSerializer<?> contextual = ser.createContextual(prov, prop);
        assertSame(ser, contextual);
    }

    @Test(timeout = 4000)
    public void testCreateContextualNumericShape() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType javaType = mapper.constructType(DateNumericShapeBean.class);
        AnnotatedMember member = mapper.getSerializationConfig()
                .introspect(javaType)
                .findProperties()
                .get(0)
                .getField();

        BeanProperty prop = new BeanProperty.Std(
                mapper.getPropertyNamingStrategy(),
                javaType.containedType(0),
                null,
                null,
                member,
                false
        );

        StubDateTimeSerializer ser = new StubDateTimeSerializer();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonSerializer<?> contextual = ser.createContextual(prov, prop);
        assertNotSame(ser, contextual);
        assertTrue(contextual instanceof StubDateTimeSerializer);
        StubDateTimeSerializer stubContextual = (StubDateTimeSerializer) contextual;
        assertEquals(Boolean.TRUE, stubContextual.getUseTimestamp());
        assertNull(stubContextual.getCustomFormat());
    }

    @Test(timeout = 4000)
    public void testCreateContextualStringShapeWithAllFormatAttributes() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType javaType = mapper.constructType(DateExplicitStringBean.class);
        AnnotatedMember member = mapper.getSerializationConfig()
                .introspect(javaType)
                .findProperties()
                .get(0)
                .getField();

        BeanProperty prop = new BeanProperty.Std(
                mapper.getPropertyNamingStrategy(),
                javaType.containedType(0),
                null,
                null,
                member,
                false
        );

        StubDateTimeSerializer ser = new StubDateTimeSerializer();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonSerializer<?> contextual = ser.createContextual(prov, prop);
        assertNotSame(ser, contextual);
        assertTrue(contextual instanceof StubDateTimeSerializer);
        StubDateTimeSerializer stubContextual = (StubDateTimeSerializer) contextual;

        assertEquals(Boolean.FALSE, stubContextual.getUseTimestamp());
        assertNotNull(stubContextual.getCustomFormat());
        assertTrue(stubContextual.getCustomFormat() instanceof SimpleDateFormat);
        SimpleDateFormat sdf = (SimpleDateFormat) stubContextual.getCustomFormat();
        assertEquals("yyyy/MM/dd", sdf.toPattern());
        assertEquals(TimeZone.getTimeZone("GMT+1"), sdf.getTimeZone());
    }

    @Test(timeout = 4000)
    public void testCreateContextualStringShapeWithDefaultsFallback() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType javaType = mapper.constructType(DateIsoFallbackBean.class);
        AnnotatedMember member = mapper.getSerializationConfig()
                .introspect(javaType)
                .findProperties()
                .get(0)
                .getField();

        BeanProperty prop = new BeanProperty.Std(
                mapper.getPropertyNamingStrategy(),
                javaType.containedType(0),
                null,
                null,
                member,
                false
        );

        StubDateTimeSerializer ser = new StubDateTimeSerializer();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonSerializer<?> contextual = ser.createContextual(prov, prop);
        assertNotSame(ser, contextual);
        assertTrue(contextual instanceof StubDateTimeSerializer);
        StubDateTimeSerializer stubContextual = (StubDateTimeSerializer) contextual;

        assertEquals(Boolean.FALSE, stubContextual.getUseTimestamp());
        assertNotNull(stubContextual.getCustomFormat());
        assertTrue(stubContextual.getCustomFormat() instanceof SimpleDateFormat);
        SimpleDateFormat sdf = (SimpleDateFormat) stubContextual.getCustomFormat();
        assertEquals("yyyy-MM-dd'T'HH:mm:ss.SSSZ", sdf.toPattern());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Defect)
    // =========================================================================

    /**
     * Targets known defect: DateSerializationTest::testDateDefaultShape
     * When pattern is specified without explicit shape = Shape.STRING,
     * DateTimeSerializerBase must treat it as STRING format rather than falling
     * back to timestamp 0.
     */
    @Test(timeout = 4000)
    public void testDateDefaultShapeWithPatternSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Date epoch = new Date(0L);
        DateDefaultShapeBean bean = new DateDefaultShapeBean(epoch);

        String json = mapper.writeValueAsString(bean);
        // Faulty version produces: {"date":0}
        // Correct version produces: {"date":"1970-01-01"}
        assertEquals("{\"date\":\"1970-01-01\"}", json);
    }

    @Test(timeout = 4000)
    public void testContextualResolutionWithPatternOnly() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType javaType = mapper.constructType(DateDefaultShapeBean.class);
        AnnotatedMember member = mapper.getSerializationConfig()
                .introspect(javaType)
                .findProperties()
                .get(0)
                .getField();

        BeanProperty prop = new BeanProperty.Std(
                mapper.getPropertyNamingStrategy(),
                javaType.containedType(0),
                null,
                null,
                member,
                false
        );

        StubDateTimeSerializer ser = new StubDateTimeSerializer();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonSerializer<?> contextual = ser.createContextual(prov, prop);

        // When pattern is present, contextual serializer should NOT be the default timestamp serializer
        assertNotSame("Contextual serializer must create a new formatted instance when pattern is specified", ser, contextual);
        StubDateTimeSerializer stubContextual = (StubDateTimeSerializer) contextual;
        assertEquals(Boolean.FALSE, stubContextual.getUseTimestamp());
        assertNotNull(stubContextual.getCustomFormat());
        assertEquals("yyyy-MM-dd", ((SimpleDateFormat) stubContextual.getCustomFormat()).toPattern());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAsTimestampThrowsOnNullSerializerProviderWhenFormatIsNull() {
        StubDateTimeSerializer ser = new StubDateTimeSerializer(null, null);
        ser._asTimestamp(null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCustomFormatCloneSafetyAndSerialization() throws Exception {
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        StubDateTimeSerializer ser = new StubDateTimeSerializer(false, df);

        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        StringWriter writer = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(writer);

        ser.serialize(new Date(0L), gen, prov);
        gen.flush();
        assertEquals("\"1970/01/01\"", writer.toString());
    }

    @Test(timeout = 4000)
    public void testWithFormatContract() {
        StubDateTimeSerializer ser = new StubDateTimeSerializer();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        DateTimeSerializerBase<Date> modified = ser.withFormat(Boolean.TRUE, df);
        assertNotSame(ser, modified);
        assertTrue(modified instanceof StubDateTimeSerializer);
        StubDateTimeSerializer stubModified = (StubDateTimeSerializer) modified;
        assertEquals(Boolean.TRUE, stubModified.getUseTimestamp());
        assertEquals(df, stubModified.getCustomFormat());
    }
}