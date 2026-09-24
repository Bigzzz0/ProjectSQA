/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.SerializerProvider
 *
 * Decision Branches & Boundary Conditions Covered:
 * 1. Configuration & Null-Guards (BVA):
 *    - setDefaultKeySerializer(null) -> IllegalArgumentException
 *    - setNullValueSerializer(null) -> IllegalArgumentException
 *    - setNullKeySerializer(null) -> IllegalArgumentException
 *    - setAttribute / getAttribute per-call attributes propagation
 * 2. Specialized Unknown Type Handling:
 *    - getUnknownTypeSerializer(Object.class) vs (Custom.class)
 *    - isUnknownTypeSerializer with null, _unknownTypeSerializer, and FAIL_ON_EMPTY_BEANS flag
 * 3. Serializer Resolution & Caching:
 *    - findValueSerializer(JavaType == null) -> reportMappingProblem ("Null passed for `valueType`")
 *    - findValueSerializer(Class), findValueSerializer(JavaType) untyped resolution
 *    - findPrimaryPropertySerializer(Class), findPrimaryPropertySerializer(JavaType)
 *    - findTypedValueSerializer caching branches (local map hit, cache hit, new synthesis)
 *    - findKeySerializer(Class), findKeySerializer(JavaType)
 * 4. Contextualization & Resolution Lifecycle:
 *    - handlePrimaryContextualization / handleSecondaryContextualization with ContextualSerializer vs standard
 *    - _handleResolvable with ResolvableSerializer
 * 5. Default Serialization Helpers:
 *    - defaultSerializeValue, defaultSerializeField, defaultSerializeNull with std vs custom null serializer
 *    - defaultSerializeDateValue, defaultSerializeDateKey with timestamps true/false
 * 6. Root Type Compatibility Guard:
 *    - _reportIncompatibleRootType: primitive to wrapper coercion compatibility vs mismatch
 * 7. Defect-Targeted Error Reporting & Exception Chaining:
 *    - reportBadTypeDefinition (bean == null vs bean != null)
 *    - reportBadPropertyDefinition (bean/prop == null vs present)
 *    - reportBadDefinition (JavaType / Class with cause)
 *    - reportMappingProblem with underlying exception to verify exact message composition and no duplicate nested locations
 *    - invalidTypeIdException (with extraDesc null vs formatted)
 */

package com.fasterxml.jackson.databind;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.impl.UnknownSerializer;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
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

public class SerializerProviderGeminiTest {

    private static class ConcreteTestSerializerProvider extends SerializerProvider {
        public ConcreteTestSerializerProvider() {
            super();
        }

        public ConcreteTestSerializerProvider(SerializerProvider src, SerializationConfig config, SerializerFactory f) {
            super(src, config, f);
        }

        public ConcreteTestSerializerProvider(SerializerProvider src) {
            super(src);
        }

        @Override
        public WritableObjectId findObjectId(Object forPojo, ObjectIdGenerator<?> generatorType) {
            return null;
        }

        @Override
        public JsonSerializer<Object> serializerInstance(Annotated annotated, Object serDef) {
            return null;
        }

        @Override
        public Object includeFilterInstance(BeanPropertyDefinition forProperty, Class<?> filterClass) {
            return null;
        }

        @Override
        public boolean includeFilterSuppressNulls(Object filter) {
            return false;
        }

        public DateFormat testDateFormat() {
            return _dateFormat();
        }

        public void testReportIncompatibleRootType(Object val, JavaType rootType) throws IOException {
            _reportIncompatibleRootType(val, rootType);
        }

        public JsonSerializer<Object> testHandleResolvable(JsonSerializer<?> ser) throws JsonMappingException {
            return _handleResolvable(ser);
        }

        public JsonSerializer<Object> testHandleContextualResolvable(JsonSerializer<?> ser, BeanProperty prop) throws JsonMappingException {
            return _handleContextualResolvable(ser, prop);
        }
    }

    private static class DummyResolvableContextualSerializer extends JsonSerializer<Object>
            implements ResolvableSerializer, ContextualSerializer {
        boolean resolved = false;
        boolean contextualized = false;

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString("dummy");
        }

        @Override
        public void resolve(SerializerProvider provider) {
            resolved = true;
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
            contextualized = true;
            return this;
        }
    }

    /*
     * -------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * -------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testBlueprintAndActiveInstanceLifecycle() {
        ConcreteTestSerializerProvider blueprint = new ConcreteTestSerializerProvider();
        assertNull(blueprint.getConfig());
        assertNull(blueprint.getActiveView());
        assertNull(blueprint.getGenerator());

        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        ConcreteTestSerializerProvider active = new ConcreteTestSerializerProvider(blueprint, config, mapper.getSerializerFactory());

        assertSame(config, active.getConfig());
        assertNotNull(active.getTypeFactory());
        assertNotNull(active.getAnnotationIntrospector());
        assertEquals(config.canOverrideAccessModifiers(), active.canOverrideAccessModifiers());
        assertEquals(config.getLocale(), active.getLocale());
        assertEquals(config.getTimeZone(), active.getTimeZone());

        ConcreteTestSerializerProvider copyBlueprint = new ConcreteTestSerializerProvider(blueprint);
        assertNull(copyBlueprint.getConfig());
    }

    @Test(timeout = 4000)
    public void testPerCallAttributes() {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        assertNull(prov.getAttribute("non_existing_key"));
        SerializerProvider chained = prov.setAttribute("k1", "v1");
        assertSame(prov, chained);
        assertEquals("v1", prov.getAttribute("k1"));

        prov.setAttribute("k1", "v2");
        assertEquals("v2", prov.getAttribute("k1"));
    }

    @Test(timeout = 4000)
    public void testFeatureInquiries() {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        assertTrue(prov.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));
        assertTrue(prov.isEnabled(MapperFeature.USE_ANNOTATIONS));

        int mask = SerializationFeature.FAIL_ON_EMPTY_BEANS.getMask();
        assertTrue(prov.hasSerializationFeatures(mask));
        assertFalse(prov.hasSerializationFeatures(mask | SerializationFeature.INDENT_OUTPUT.getMask()));
    }

    @Test(timeout = 4000)
    public void testCustomSpecializedSerializersConfiguration() {
        ConcreteTestSerializerProvider prov = new ConcreteTestSerializerProvider();
        JsonSerializer<Object> customNull = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeRaw("null_override");
            }
        };
        JsonSerializer<Object> customKey = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeFieldName("custom_key");
            }
        };

        prov.setNullValueSerializer(customNull);
        prov.setDefaultKeySerializer(customKey);
        prov.setNullKeySerializer(customKey);

        assertSame(customNull, prov.getDefaultNullValueSerializer());
        assertSame(customKey, prov.getDefaultNullKeySerializer());
    }

    /*
     * -------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * -------------------------------------------------------------------
     */

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDefaultKeySerializerNullGuard() {
        ConcreteTestSerializerProvider prov = new ConcreteTestSerializerProvider();
        prov.setDefaultKeySerializer(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetNullValueSerializerNullGuard() {
        ConcreteTestSerializerProvider prov = new ConcreteTestSerializerProvider();
        prov.setNullValueSerializer(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetNullKeySerializerNullGuard() {
        ConcreteTestSerializerProvider prov = new ConcreteTestSerializerProvider();
        prov.setNullKeySerializer(null);
    }

    @Test(timeout = 4000)
    public void testFindValueSerializerWithNullType() {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        try {
            prov.findValueSerializer((JavaType) null, null);
            fail("Expected JsonMappingException for null JavaType");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Null passed for `valueType`"));
        }
    }

    @Test(timeout = 4000)
    public void testUnknownTypeSerializerDistinction() {
        ConcreteTestSerializerProvider prov = new ConcreteTestSerializerProvider();

        JsonSerializer<Object> objUnknown = prov.getUnknownTypeSerializer(Object.class);
        assertSame(prov._unknownTypeSerializer, objUnknown);

        JsonSerializer<Object> customUnknown = prov.getUnknownTypeSerializer(String.class);
        assertNotSame(objUnknown, customUnknown);
        assertTrue(customUnknown instanceof UnknownSerializer);

        assertTrue(prov.isUnknownTypeSerializer(null));
        assertTrue(prov.isUnknownTypeSerializer(objUnknown));
    }

    @Test(timeout = 4000)
    public void testIsUnknownTypeSerializerWithFailOnEmptyBeans() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        UnknownSerializer emptySer = new UnknownSerializer(String.class);
        assertTrue(prov.isUnknownTypeSerializer(emptySer));

        ObjectMapper mapperNoFail = new ObjectMapper();
        mapperNoFail.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        SerializerProvider provNoFail = mapperNoFail.getSerializerProviderInstance();
        assertFalse(provNoFail.isUnknownTypeSerializer(emptySer));
    }

    @Test(timeout = 4000)
    public void testPrimitiveWrapperCoercionInRootTypeCheck() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ConcreteTestSerializerProvider prov = new ConcreteTestSerializerProvider(
                new ConcreteTestSerializerProvider(),
                mapper.getSerializationConfig(),
                mapper.getSerializerFactory()
        );

        JavaType intType = mapper.constructType(int.class);
        // Valid coercion: Integer wrapper matches primitive int
        prov.testReportIncompatibleRootType(Integer.valueOf(100), intType);

        // Incompatible type: String does not match int
        try {
            prov.testReportIncompatibleRootType("not an int", intType);
            fail("Expected JsonMappingException for incompatible root type");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Incompatible types: declared root type"));
        }
    }

    /*
     * -------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone
     * -------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testDefectTargetedReportMappingProblemPreservesCauseAndLocationCleanliness() {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        IllegalArgumentException rootCause = new IllegalArgumentException("Root problem");
        try {
            prov.reportMappingProblem(rootCause, "Problem with key %s", "myKey");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException jme) {
            assertSame(rootCause, jme.getCause());
            assertTrue(jme.getMessage().contains("Problem with key myKey"));

            // Verification of exception message formatting and lack of duplicate markers
            String message = jme.getMessage();
            int countAt = 0;
            int idx = 0;
            while ((idx = message.indexOf("at [", idx)) != -1) {
                countAt++;
                idx += 4;
            }
            assertTrue("Expected at most 1 'at [' marker, found: " + countAt, countAt <= 1);
        }
    }

    @Test(timeout = 4000)
    public void testReportBadTypeDefinitionBranches() {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        try {
            prov.reportBadTypeDefinition(null, "Type definition failure with arg %d", 42);
            fail("Expected InvalidDefinitionException");
        } catch (InvalidDefinitionException ide) {
            assertTrue(ide.getMessage().contains("Invalid type definition for type N/A: Type definition failure with arg 42"));
        }

        BeanDescription beanDesc = mapper.getSerializationConfig().introspectClassAnnotations(String.class);
        try {
            prov.reportBadTypeDefinition(beanDesc, "Type issue");
            fail("Expected InvalidDefinitionException");
        } catch (InvalidDefinitionException ide) {
            assertTrue(ide.getMessage().contains("java.lang.String"));
            assertTrue(ide.getMessage().contains("Type issue"));
        }
    }

    @Test(timeout = 4000)
    public void testReportBadPropertyDefinitionBranches() {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        try {
            prov.reportBadPropertyDefinition(null, null, "Property failed: %s", "test");
            fail("Expected InvalidDefinitionException");
        } catch (InvalidDefinitionException ide) {
            assertTrue(ide.getMessage().contains("Invalid definition for property N/A (of type N/A): Property failed: test"));
        }
    }

    @Test(timeout = 4000)
    public void testReportBadDefinitionVariants() {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JavaType stringType = mapper.constructType(String.class);

        try {
            prov.reportBadDefinition(stringType, "Bad string definition");
            fail("Expected InvalidDefinitionException");
        } catch (InvalidDefinitionException ide) {
            assertEquals("Bad string definition", ide.getMessage());
        }

        Throwable cause = new RuntimeException("inner");
        try {
            prov.reportBadDefinition(stringType, "Bad definition with cause", cause);
            fail("Expected InvalidDefinitionException");
        } catch (InvalidDefinitionException ide) {
            assertSame(cause, ide.getCause());
        }

        try {
            prov.reportBadDefinition(Integer.class, "Bad raw class", cause);
            fail("Expected InvalidDefinitionException");
        } catch (InvalidDefinitionException ide) {
            assertSame(cause, ide.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testInvalidTypeIdExceptionFormatting() {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JavaType numType = mapper.constructType(Number.class);

        InvalidTypeIdException exWithDesc = (InvalidTypeIdException) prov.invalidTypeIdException(numType, "customId", "extra information");
        assertEquals("customId", exWithDesc.getTypeId());
        assertTrue(exWithDesc.getMessage().contains("Could not resolve type id 'customId' as a subtype of"));
        assertTrue(exWithDesc.getMessage().contains("extra information"));

        InvalidTypeIdException exWithoutDesc = (InvalidTypeIdException) prov.invalidTypeIdException(numType, "badId", null);
        assertFalse(exWithoutDesc.getMessage().contains(": null"));
    }

    /*
     * -------------------------------------------------------------------
     * Partition D: Contextualization & Resolvable & Custom Serializers
     * -------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testContextualAndResolvableHooks() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ConcreteTestSerializerProvider prov = new ConcreteTestSerializerProvider(
                new ConcreteTestSerializerProvider(),
                mapper.getSerializationConfig(),
                mapper.getSerializerFactory()
        );

        DummyResolvableContextualSerializer dummy = new DummyResolvableContextualSerializer();
        assertFalse(dummy.resolved);
        assertFalse(dummy.contextualized);

        JsonSerializer<Object> resolved = prov.testHandleResolvable(dummy);
        assertSame(dummy, resolved);
        assertTrue(dummy.resolved);

        JsonSerializer<?> contextual = prov.handlePrimaryContextualization(dummy, null);
        assertSame(dummy, contextual);
        assertTrue(dummy.contextualized);

        dummy.contextualized = false;
        JsonSerializer<?> secondaryContextual = prov.handleSecondaryContextualization(dummy, null);
        assertSame(dummy, secondaryContextual);
        assertTrue(dummy.contextualized);

        dummy.resolved = false;
        dummy.contextualized = false;
        JsonSerializer<Object> both = prov.testHandleContextualResolvable(dummy, null);
        assertSame(dummy, both);
        assertTrue(dummy.resolved);
        assertTrue(dummy.contextualized);

        // Null safe
        assertNull(prov.handlePrimaryContextualization(null, null));
        assertNull(prov.handleSecondaryContextualization(null, null));
        assertNull(prov.testHandleResolvable(null));
    }

    @Test(timeout = 4000)
    public void testFindValueSerializerLookups() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonSerializer<Object> serClass = prov.findValueSerializer(String.class, null);
        assertNotNull(serClass);

        JsonSerializer<Object> serClassNoContext = prov.findValueSerializer(String.class);
        assertNotNull(serClassNoContext);

        JavaType intType = mapper.constructType(Integer.class);
        JsonSerializer<Object> serType = prov.findValueSerializer(intType, null);
        assertNotNull(serType);

        JsonSerializer<Object> serTypeNoContext = prov.findValueSerializer(intType);
        assertNotNull(serTypeNoContext);

        JsonSerializer<Object> primSerType = prov.findPrimaryPropertySerializer(intType, null);
        assertNotNull(primSerType);

        JsonSerializer<Object> primSerClass = prov.findPrimaryPropertySerializer(Integer.class, null);
        assertNotNull(primSerClass);
    }

    @Test(timeout = 4000)
    public void testFindKeySerializerLookups() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonSerializer<Object> keySerClass = prov.findKeySerializer(String.class, null);
        assertNotNull(keySerClass);

        JavaType longType = mapper.constructType(Long.class);
        JsonSerializer<Object> keySerType = prov.findKeySerializer(longType, null);
        assertNotNull(keySerType);
    }

    @Test(timeout = 4000)
    public void testFindTypedValueSerializerCachingAndTypeSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonSerializer<Object> typedSer1 = prov.findTypedValueSerializer(String.class, true, null);
        assertNotNull(typedSer1);
        JsonSerializer<Object> typedSerCached = prov.findTypedValueSerializer(String.class, true, null);
        assertSame(typedSer1, typedSerCached);

        JavaType type = mapper.constructType(Double.class);
        JsonSerializer<Object> typedSer2 = prov.findTypedValueSerializer(type, true, null);
        assertNotNull(typedSer2);
        JsonSerializer<Object> typedSer2Cached = prov.findTypedValueSerializer(type, true, null);
        assertSame(typedSer2, typedSer2Cached);

        TypeSerializer typeSer = prov.findTypeSerializer(type);
        // By default Double has no polymorphic type serializer
        assertNull(typeSer);
    }

    /*
     * -------------------------------------------------------------------
     * Partition E: Default Serialization Helpers & Date Handling
     * -------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testDefaultSerializeValueAndField() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonFactory jf = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jf.createGenerator(sw);

        gen.writeStartObject();
        prov.defaultSerializeField("fieldNull", null, gen);
        prov.defaultSerializeField("fieldString", "hello", gen);
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"fieldNull\":null,\"fieldString\":\"hello\"}", sw.toString());

        sw = new StringWriter();
        gen = jf.createGenerator(sw);
        prov.defaultSerializeValue(null, gen);
        prov.defaultSerializeValue(Integer.valueOf(123), gen);
        gen.close();

        assertEquals("null123", sw.toString());
    }

    @Test(timeout = 4000)
    public void testDefaultSerializeNullWithCustomSerializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        ConcreteTestSerializerProvider prov = new ConcreteTestSerializerProvider(
                new ConcreteTestSerializerProvider(),
                config,
                mapper.getSerializerFactory()
        );

        prov.setNullValueSerializer(new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString("CUSTOM_NULL");
            }
        });

        JsonFactory jf = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jf.createGenerator(sw);

        prov.defaultSerializeNull(gen);
        prov.defaultSerializeValue(null, gen);

        gen.writeStartObject();
        prov.defaultSerializeField("customProp", null, gen);
        gen.writeEndObject();
        gen.close();

        assertEquals("\"CUSTOM_NULL\"\"CUSTOM_NULL\"{\"customProp\":\"CUSTOM_NULL\"}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testDefaultSerializeDateValueAndKey() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        long testEpoch = 1577836800000L; // 2020-01-01 00:00:00 UTC
        Date testDate = new Date(testEpoch);

        // 1. As timestamps
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        mapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, true);
        SerializerProvider provTs = mapper.getSerializerProviderInstance();

        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);

        provTs.defaultSerializeDateValue(testEpoch, gen);
        provTs.defaultSerializeDateValue(testDate, gen);

        gen.writeStartObject();
        provTs.defaultSerializeDateKey(testEpoch, gen);
        gen.writeBoolean(true);
        provTs.defaultSerializeDateKey(testDate, gen);
        gen.writeBoolean(false);
        gen.writeEndObject();
        gen.close();

        assertEquals("1577836800000 1577836800000{\"1577836800000\":true,\"1577836800000\":false}", sw.toString().replace(" ", ""));

        // 2. As formatted strings
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        mapper.setDateFormat(sdf);

        SerializerProvider provText = mapper.getSerializerProviderInstance();
        sw = new StringWriter();
        gen = new JsonFactory().createGenerator(sw);

        provText.defaultSerializeDateValue(testEpoch, gen);
        provText.defaultSerializeDateValue(testDate, gen);

        gen.writeStartObject();
        provText.defaultSerializeDateKey(testEpoch, gen);
        gen.writeNumber(1);
        provText.defaultSerializeDateKey(testDate, gen);
        gen.writeNumber(2);
        gen.writeEndObject();
        gen.close();

        assertEquals("\"2020-01-01\"\"2020-01-01\"{\"2020-01-01\":1,\"2020-01-01\":2}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testDateFormatLazyInitializationAndCloning() {
        ObjectMapper mapper = new ObjectMapper();
        ConcreteTestSerializerProvider prov = new ConcreteTestSerializerProvider(
                new ConcreteTestSerializerProvider(),
                mapper.getSerializationConfig(),
                mapper.getSerializerFactory()
        );

        DateFormat df1 = prov.testDateFormat();
        assertNotNull(df1);
        DateFormat df2 = prov.testDateFormat();
        assertSame("Should reuse instantiated _dateFormat on subsequent calls", df1, df2);
    }

    @Test(timeout = 4000)
    public void testDeprecatedMappingExceptionFactoryMethods() {
        ConcreteTestSerializerProvider prov = new ConcreteTestSerializerProvider();

        JsonMappingException e1 = prov.mappingException("Simple message with param %d", 123);
        assertNotNull(e1);
        assertTrue(e1.getMessage().contains("Simple message with param 123"));

        Exception cause = new Exception("cause_msg");
        JsonMappingException e2 = prov.mappingException(cause, "Msg with param %s", "paramVal");
        assertNotNull(e2);
        assertSame(cause, e2.getCause());
        assertTrue(e2.getMessage().contains("Msg with param paramVal"));
    }
}