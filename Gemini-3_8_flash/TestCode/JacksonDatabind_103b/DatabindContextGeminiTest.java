package com.fasterxml.jackson.databind;

import java.io.Closeable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------------
 * Method Under Test           | Branch / Condition                          | Test Method
 * ---------------------------------------------------------------------------------------------------------------
 * constructType               | type == null                                | testConstructType_Null()
 * constructType               | type != null                                | testConstructType_NonNull()
 * constructSpecializedType    | rawClass == subclass (optimization branch)  | testConstructSpecializedType_SameClass()
 * constructSpecializedType    | rawClass != subclass (specialization path)  | testConstructSpecializedType_SubClass()
 * resolveSubType              | subClass contains '<' & valid subtype       | testResolveSubType_WithGenerics_Success()
 * resolveSubType              | subClass contains '<' & invalid subtype     | testResolveSubType_WithGenerics_NotSubType()
 * resolveSubType              | subClass without '<' & valid subtype        | testResolveSubType_Simple_Success()
 * resolveSubType              | subClass without '<' & invalid subtype      | testResolveSubType_Simple_NotSubType()
 * resolveSubType              | ClassNotFoundException                      | testResolveSubType_ClassNotFound_ReturnsNull()
 * resolveSubType              | Generic Exception in findClass              | testResolveSubType_FindClassException_ThrowsJsonMappingException()
 * converterInstance           | converterDef == null                        | testConverterInstance_Null()
 * converterInstance           | converterDef is Converter instance          | testConverterInstance_AlreadyInstance()
 * converterInstance           | converterDef not Class or Converter         | testConverterInstance_InvalidType_ThrowsIllegalState()
 * converterInstance           | converterDef == Converter.None.class        | testConverterInstance_NoneClass_ReturnsNull()
 * converterInstance           | converterDef is bogus class (Void.class)    | testConverterInstance_BogusClass_ReturnsNull()
 * converterInstance           | converterDef not assignable to Converter    | testConverterInstance_NonConverterClass_ThrowsIllegalState()
 * converterInstance           | valid Class, HandlerInstantiator is null    | testConverterInstance_ViaReflection()
 * converterInstance           | valid Class, HandlerInstantiator provides   | testConverterInstance_ViaHandlerInstantiator()
 * converterInstance           | HandlerInstantiator returns null fallback   | testConverterInstance_ViaHandlerInstantiator_Fallback()
 * objectIdGeneratorInstance   | HandlerInstantiator provides generator      | testObjectIdGeneratorInstance_ViaHandlerInstantiator()
 * objectIdGeneratorInstance   | HandlerInstantiator null / returns null     | testObjectIdGeneratorInstance_ViaReflection()
 * objectIdResolverInstance    | HandlerInstantiator provides resolver       | testObjectIdResolverInstance_ViaHandlerInstantiator()
 * objectIdResolverInstance    | HandlerInstantiator null / returns null     | testObjectIdResolverInstance_ViaReflection()
 * reportBadDefinition(Class)  | delegates to reportBadDefinition(JavaType)  | testReportBadDefinition_ClassDelegation()
 * _format                     | msgArgs.length == 0 vs msgArgs.length > 0   | testHelper_Format()
 * _truncate                   | desc == null, len <= 500, len > 500         | testHelper_Truncate()
 * _quotedString               | desc == null vs desc != null                | testHelper_QuotedString()
 * _colonConcat                | extra == null vs extra != null              | testHelper_ColonConcat()
 * _desc                       | desc == null vs desc != null                | testHelper_Desc()
 * Defects4J Ground Truth      | BasicExceptionTest::testLocationAddition    | testLocationAddition_SingleLocationMarkerDefect()
 * ---------------------------------------------------------------------------------------------------------------
 */
public class DatabindContextGeminiTest {

    // Concrete test implementation of abstract DatabindContext
    static class ConcreteDatabindContext extends DatabindContext {
        private final MapperConfig<?> _config;
        private final TypeFactory _typeFactory;
        private final Map<Object, Object> _attributes = new HashMap<>();

        ConcreteDatabindContext(MapperConfig<?> config, TypeFactory tf) {
            _config = config;
            _typeFactory = (tf != null) ? tf : TypeFactory.defaultInstance();
        }

        @Override
        public MapperConfig<?> getConfig() {
            return _config;
        }

        @Override
        public AnnotationIntrospector getAnnotationIntrospector() {
            return _config.getAnnotationIntrospector();
        }

        @Override
        public boolean isEnabled(MapperFeature feature) {
            return _config.isEnabled(feature);
        }

        @Override
        public boolean canOverrideAccessModifiers() {
            return _config.canOverrideAccessModifiers();
        }

        @Override
        public Class<?> getActiveView() {
            return _config.getActiveView();
        }

        @Override
        public Locale getLocale() {
            return _config.getLocale();
        }

        @Override
        public TimeZone getTimeZone() {
            return _config.getTimeZone();
        }

        @Override
        public JsonFormat.Value getDefaultPropertyFormat(Class<?> baseType) {
            return _config.getDefaultPropertyFormat(baseType);
        }

        @Override
        public Object getAttribute(Object key) {
            return _attributes.get(key);
        }

        @Override
        public DatabindContext setAttribute(Object key, Object value) {
            _attributes.put(key, value);
            return this;
        }

        @Override
        protected JsonMappingException invalidTypeIdException(JavaType baseType, String typeId, String extraDesc) {
            return new JsonMappingException((Closeable) null, "Invalid type id: " + typeId + " for " + baseType + "; " + extraDesc);
        }

        @Override
        public TypeFactory getTypeFactory() {
            return _typeFactory;
        }

        @Override
        public <T> T reportBadDefinition(JavaType type, String msg) throws JsonMappingException {
            throw new JsonMappingException((Closeable) null, "Bad definition for " + type + ": " + msg);
        }

        // Expose protected helper methods for direct white-box assertion
        public String formatHelper(String msg, Object... msgArgs) {
            return _format(msg, msgArgs);
        }

        public String truncateHelper(String desc) {
            return _truncate(desc);
        }

        public String quotedStringHelper(String desc) {
            return _quotedString(desc);
        }

        public String colonConcatHelper(String msgBase, String extra) {
            return _colonConcat(msgBase, extra);
        }

        public String descHelper(String desc) {
            return _desc(desc);
        }
    }

    // Dummy Converter implementation for reflection & instantiator tests
    public static class StringToIntegerConverter extends StdConverter<String, Integer> {
        @Override
        public Integer convert(String value) {
            return Integer.valueOf(value);
        }
    }

    // Custom HandlerInstantiator for testing handler hook paths
    static class TestHandlerInstantiator extends HandlerInstantiator {
        private final ObjectIdGenerator<?> _gen;
        private final ObjectIdResolver _resolver;
        private final Converter<?, ?> _converter;

        TestHandlerInstantiator(ObjectIdGenerator<?> gen, ObjectIdResolver resolver, Converter<?, ?> converter) {
            _gen = gen;
            _resolver = resolver;
            _converter = converter;
        }

        @Override
        public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) {
            return null;
        }

        @Override
        public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
            return null;
        }

        @Override
        public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
            return null;
        }

        @Override
        public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) {
            return null;
        }

        @Override
        public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
            return null;
        }

        @Override
        public ValueInstantiator valueInstantiatorInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
            return null;
        }

        @Override
        public ObjectIdGenerator<?> objectIdGeneratorInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
            return _gen;
        }

        @Override
        public ObjectIdResolver resolverIdGeneratorInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
            return _resolver;
        }

        @Override
        public Converter<?, ?> converterInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
            return _converter;
        }
    }

    // Defect reproduction payload model
    public enum ABC { A, B, C }
    public static class MapWrapper {
        public Map<ABC, Integer> map;
    }

    private ConcreteDatabindContext createDefaultContext() {
        ObjectMapper mapper = new ObjectMapper();
        return new ConcreteDatabindContext(mapper.getSerializationConfig(), TypeFactory.defaultInstance());
    }

    /*
     * =========================================================================
     * Partition A: Core Functional Logic & State Transitions
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testConstructType_NonNull() {
        ConcreteDatabindContext context = createDefaultContext();
        JavaType javaType = context.constructType(String.class);
        assertNotNull(javaType);
        assertEquals(String.class, javaType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedType_SameClass() {
        ConcreteDatabindContext context = createDefaultContext();
        JavaType baseType = context.constructType(String.class);
        JavaType specialized = context.constructSpecializedType(baseType, String.class);
        assertSame("Should return exact same instance if raw class matches", baseType, specialized);
    }

    @Test(timeout = 4000)
    public void testConstructSpecializedType_SubClass() {
        ConcreteDatabindContext context = createDefaultContext();
        JavaType baseType = context.constructType(List.class);
        JavaType specialized = context.constructSpecializedType(baseType, ArrayList.class);
        assertNotSame(baseType, specialized);
        assertEquals(ArrayList.class, specialized.getRawClass());
    }

    @Test(timeout = 4000)
    public void testResolveSubType_WithGenerics_Success() throws Exception {
        ConcreteDatabindContext context = createDefaultContext();
        JavaType baseType = context.constructType(List.class);
        JavaType resolved = context.resolveSubType(baseType, "java.util.ArrayList<java.lang.String>");
        assertNotNull(resolved);
        assertEquals(ArrayList.class, resolved.getRawClass());
        assertEquals(1, resolved.containedTypeCount());
        assertEquals(String.class, resolved.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testResolveSubType_Simple_Success() throws Exception {
        ConcreteDatabindContext context = createDefaultContext();
        JavaType baseType = context.constructType(List.class);
        JavaType resolved = context.resolveSubType(baseType, "java.util.ArrayList");
        assertNotNull(resolved);
        assertEquals(ArrayList.class, resolved.getRawClass());
    }

    @Test(timeout = 4000)
    public void testAttributeStateStorageAndChaining() {
        ConcreteDatabindContext context = createDefaultContext();
        assertNull(context.getAttribute("key1"));

        DatabindContext chained = context.setAttribute("key1", "value1");
        assertSame(context, chained);
        assertEquals("value1", context.getAttribute("key1"));

        context.setAttribute("key1", "value2");
        assertEquals("value2", context.getAttribute("key1"));
    }

    /*
     * =========================================================================
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testConstructType_Null() {
        ConcreteDatabindContext context = createDefaultContext();
        assertNull(context.constructType((Type) null));
    }

    @Test(timeout = 4000)
    public void testResolveSubType_ClassNotFound_ReturnsNull() throws Exception {
        ConcreteDatabindContext context = createDefaultContext();
        JavaType baseType = context.constructType(Object.class);
        JavaType resolved = context.resolveSubType(baseType, "com.nonexistent.NoSuchClass12345");
        assertNull("ClassNotFoundException should result in null return value", resolved);
    }

    @Test(timeout = 4000)
    public void testHelper_Truncate() {
        ConcreteDatabindContext context = createDefaultContext();
        assertEquals("", context.truncateHelper(null));
        assertEquals("", context.truncateHelper(""));

        // Exactly 500 characters: boundary threshold
        StringBuilder sb500 = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            sb500.append('a');
        }
        String str500 = sb500.toString();
        assertEquals(str500, context.truncateHelper(str500));

        // 501 characters: exceeds boundary, triggers truncation
        String str501 = str500 + "b";
        String truncated501 = context.truncateHelper(str501);
        assertEquals(500 + 5 + 500, truncated501.length());
        assertTrue(truncated501.startsWith(str500));
        assertTrue(truncated501.contains("]...["));
        assertTrue(truncated501.endsWith(str501.substring(1)));
    }

    @Test(timeout = 4000)
    public void testHelper_Format() {
        ConcreteDatabindContext context = createDefaultContext();
        assertEquals("Static Message", context.formatHelper("Static Message"));
        assertEquals("Value: 42, Text: hello", context.formatHelper("Value: %d, Text: %s", 42, "hello"));
    }

    @Test(timeout = 4000)
    public void testHelper_QuotedString() {
        ConcreteDatabindContext context = createDefaultContext();
        assertEquals("[N/A]", context.quotedStringHelper(null));
        assertEquals("\"test\"", context.quotedStringHelper("test"));
    }

    @Test(timeout = 4000)
    public void testHelper_ColonConcat() {
        ConcreteDatabindContext context = createDefaultContext();
        assertEquals("BaseMessage", context.colonConcatHelper("BaseMessage", null));
        assertEquals("BaseMessage: ExtraDetail", context.colonConcatHelper("BaseMessage", "ExtraDetail"));
    }

    @Test(timeout = 4000)
    public void testHelper_Desc() {
        ConcreteDatabindContext context = createDefaultContext();
        assertEquals("[N/A]", context.descHelper(null));
        assertEquals("description", context.descHelper("description"));
    }

    /*
     * =========================================================================
     * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testLocationAddition_SingleLocationMarkerDefect() {
        // Targets known defect where nested exceptions accumulate multiple duplicate 'at [' location markers
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.readValue("{\"map\":{\"value\": 3}}", MapWrapper.class);
            fail("Expected JsonMappingException due to invalid enum key");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertNotNull("Exception message should not be null", msg);
            int count = 0;
            int idx = 0;
            while ((idx = msg.indexOf(" at [", idx)) != -1) {
                count++;
                idx += 5;
            }
            assertEquals("Should only get one 'at [' marker, got " + count + ", source: " + msg, 1, count);
        } catch (Exception e) {
            fail("Expected JsonMappingException but got: " + e.getClass().getName());
        }
    }

    @Test(timeout = 4000)
    public void testResolveSubType_FindClassException_ThrowsJsonMappingException() {
        TypeFactory faultyTypeFactory = new TypeFactory(null) {
            private static final long serialVersionUID = 1L;

            @Override
            public Class<?> findClass(String className) throws ClassNotFoundException {
                if ("throw-runtime".equals(className)) {
                    throw new IllegalArgumentException("Forced simulated lookup failure");
                }
                return super.findClass(className);
            }
        };

        ObjectMapper mapper = new ObjectMapper();
        ConcreteDatabindContext context = new ConcreteDatabindContext(mapper.getSerializationConfig(), faultyTypeFactory);
        JavaType baseType = context.constructType(Object.class);

        try {
            context.resolveSubType(baseType, "throw-runtime");
            fail("Expected JsonMappingException wrapping the IllegalArgumentException");
        } catch (JsonMappingException e) {
            assertTrue("Message should indicate problem details",
                    e.getMessage().contains("problem: (java.lang.IllegalArgumentException) Forced simulated lookup failure"));
        }
    }

    /*
     * =========================================================================
     * Partition D: Exception & Defensive Guard Paths
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testResolveSubType_WithGenerics_NotSubType() {
        ConcreteDatabindContext context = createDefaultContext();
        JavaType baseType = context.constructType(List.class);
        try {
            context.resolveSubType(baseType, "java.util.HashMap<java.lang.String,java.lang.String>");
            fail("Expected JsonMappingException for incompatible subtype");
        } catch (JsonMappingException e) {
            assertTrue("Message should declare 'Not a subtype'", e.getMessage().contains("Not a subtype"));
        }
    }

    @Test(timeout = 4000)
    public void testResolveSubType_Simple_NotSubType() {
        ConcreteDatabindContext context = createDefaultContext();
        JavaType baseType = context.constructType(List.class);
        try {
            context.resolveSubType(baseType, "java.util.HashMap");
            fail("Expected JsonMappingException for incompatible subtype");
        } catch (JsonMappingException e) {
            assertTrue("Message should declare 'Not a subtype'", e.getMessage().contains("Not a subtype"));
        }
    }

    @Test(timeout = 4000)
    public void testConverterInstance_Null() throws Exception {
        ConcreteDatabindContext context = createDefaultContext();
        assertNull(context.converterInstance(null, null));
    }

    @Test(timeout = 4000)
    public void testConverterInstance_AlreadyInstance() throws Exception {
        ConcreteDatabindContext context = createDefaultContext();
        Converter<Object, Object> existing = new StringToIntegerConverter();
        Converter<Object, Object> returned = context.converterInstance(null, existing);
        assertSame(existing, returned);
    }

    @Test(timeout = 4000)
    public void testConverterInstance_InvalidType_ThrowsIllegalState() throws Exception {
        ConcreteDatabindContext context = createDefaultContext();
        try {
            context.converterInstance(null, 12345);
            fail("Expected IllegalStateException for non-class, non-converter definition");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("AnnotationIntrospector returned Converter definition of type java.lang.Integer"));
        }
    }

    @Test(timeout = 4000)
    public void testConverterInstance_NoneClass_ReturnsNull() throws Exception {
        ConcreteDatabindContext context = createDefaultContext();
        assertNull(context.converterInstance(null, Converter.None.class));
    }

    @Test(timeout = 4000)
    public void testConverterInstance_BogusClass_ReturnsNull() throws Exception {
        ConcreteDatabindContext context = createDefaultContext();
        assertNull(context.converterInstance(null, Void.class));
    }

    @Test(timeout = 4000)
    public void testConverterInstance_NonConverterClass_ThrowsIllegalState() throws Exception {
        ConcreteDatabindContext context = createDefaultContext();
        try {
            context.converterInstance(null, String.class);
            fail("Expected IllegalStateException for class not extending Converter");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("AnnotationIntrospector returned Class java.lang.String; expected Class<Converter>"));
        }
    }

    @Test(timeout = 4000)
    public void testReportBadDefinition_ClassDelegation() {
        ConcreteDatabindContext context = createDefaultContext();
        try {
            context.reportBadDefinition(String.class, "Custom problem message");
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Bad definition for [simple type, class java.lang.String]: Custom problem message"));
        }
    }

    /*
     * =========================================================================
     * Partition E: Object Lifecycle & HandlerInstantiator Integration
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testConverterInstance_ViaReflection() throws Exception {
        ConcreteDatabindContext context = createDefaultContext();
        Converter<Object, Object> conv = context.converterInstance(null, StringToIntegerConverter.class);
        assertNotNull(conv);
        assertTrue(conv instanceof StringToIntegerConverter);
    }

    @Test(timeout = 4000)
    public void testConverterInstance_ViaHandlerInstantiator() throws Exception {
        Converter<?, ?> customConv = new StringToIntegerConverter();
        TestHandlerInstantiator hi = new TestHandlerInstantiator(null, null, customConv);
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig().with(hi);
        ConcreteDatabindContext context = new ConcreteDatabindContext(config, TypeFactory.defaultInstance());

        Converter<Object, Object> conv = context.converterInstance(null, StringToIntegerConverter.class);
        assertSame(customConv, conv);
    }

    @Test(timeout = 4000)
    public void testConverterInstance_ViaHandlerInstantiator_Fallback() throws Exception {
        TestHandlerInstantiator hi = new TestHandlerInstantiator(null, null, null);
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig().with(hi);
        ConcreteDatabindContext context = new ConcreteDatabindContext(config, TypeFactory.defaultInstance());

        Converter<Object, Object> conv = context.converterInstance(null, StringToIntegerConverter.class);
        assertNotNull("Should fallback to reflection when HandlerInstantiator returns null", conv);
        assertTrue(conv instanceof StringToIntegerConverter);
    }

    @Test(timeout = 4000)
    public void testObjectIdGeneratorInstance_ViaReflection() throws Exception {
        ConcreteDatabindContext context = createDefaultContext();
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class,
                ObjectIdGenerators.IntSequenceGenerator.class, SimpleObjectIdResolver.class);

        ObjectIdGenerator<?> gen = context.objectIdGeneratorInstance(null, info);
        assertNotNull(gen);
        assertTrue(gen instanceof ObjectIdGenerators.IntSequenceGenerator);
        assertEquals(Object.class, gen.getScope());
    }

    @Test(timeout = 4000)
    public void testObjectIdGeneratorInstance_ViaHandlerInstantiator() throws Exception {
        ObjectIdGenerator<?> customGen = new ObjectIdGenerators.IntSequenceGenerator();
        TestHandlerInstantiator hi = new TestHandlerInstantiator(customGen, null, null);
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig().with(hi);
        ConcreteDatabindContext context = new ConcreteDatabindContext(config, TypeFactory.defaultInstance());

        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), String.class,
                ObjectIdGenerators.IntSequenceGenerator.class, SimpleObjectIdResolver.class);

        ObjectIdGenerator<?> gen = context.objectIdGeneratorInstance(null, info);
        assertNotNull(gen);
        assertEquals(String.class, gen.getScope());
    }

    @Test(timeout = 4000)
    public void testObjectIdResolverInstance_ViaReflection() {
        ConcreteDatabindContext context = createDefaultContext();
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class,
                ObjectIdGenerators.IntSequenceGenerator.class, SimpleObjectIdResolver.class);

        ObjectIdResolver resolver = context.objectIdResolverInstance(null, info);
        assertNotNull(resolver);
        assertTrue(resolver instanceof SimpleObjectIdResolver);
    }

    @Test(timeout = 4000)
    public void testObjectIdResolverInstance_ViaHandlerInstantiator() {
        ObjectIdResolver customResolver = new SimpleObjectIdResolver();
        TestHandlerInstantiator hi = new TestHandlerInstantiator(null, customResolver, null);
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig().with(hi);
        ConcreteDatabindContext context = new ConcreteDatabindContext(config, TypeFactory.defaultInstance());

        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class,
                ObjectIdGenerators.IntSequenceGenerator.class, SimpleObjectIdResolver.class);

        ObjectIdResolver resolver = context.objectIdResolverInstance(null, info);
        assertSame(customResolver, resolver);
    }
}