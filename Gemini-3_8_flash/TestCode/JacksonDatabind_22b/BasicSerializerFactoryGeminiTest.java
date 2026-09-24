package com.fasterxml.jackson.databind.ser;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.fasterxml.jackson.databind.ser.impl.IndexedListSerializer;
import com.fasterxml.jackson.databind.ser.impl.IndexedStringListSerializer;
import com.fasterxml.jackson.databind.ser.impl.IteratorSerializer;
import com.fasterxml.jackson.databind.ser.impl.MapEntrySerializer;
import com.fasterxml.jackson.databind.ser.impl.StringArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.StringCollectionSerializer;
import com.fasterxml.jackson.databind.ser.std.*;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Class Under Test: com.fasterxml.jackson.databind.ser.BasicSerializerFactory
 * Target Branches & Decision Paths:
 * 1. Configuration & Life Cycle:
 *    - null config vs explicit config in constructor
 *    - withConfig, withAdditionalSerializers, withAdditionalKeySerializers, withSerializerModifier
 * 2. findSerializerByLookup:
 *    - Reference types (AtomicReference) -> AtomicReferenceSerializer
 *    - Concrete cached map lookup (String, StringBuffer, BigInteger, Calendar, Date, UUID, etc.)
 *    - Lazy cached map lookup (java.sql.Date, java.sql.Time, TokenBuffer)
 *    - Unregistered type -> null
 * 3. findSerializerByAnnotations:
 *    - JsonSerializable implementations -> SerializableSerializer
 *    - @JsonValue method detected -> JsonValueSerializer
 *    - Neither -> null
 * 4. findSerializerByPrimaryType:
 *    - Optional handlers (OptionalHandlerFactory)
 *    - Calendar, Date, Map.Entry (with and without contained type parameters)
 *    - ByteBuffer, InetAddress, InetSocketAddress, TimeZone, Charset
 *    - Number hierarchy: JsonFormat.Shape.STRING, OBJECT, ARRAY, and default
 *    - Enum hierarchy: JsonFormat.Shape.OBJECT vs default EnumSerializer
 * 5. findSerializerByAddonType:
 *    - Iterator, Iterable (with single type param vs unknown fallback)
 *    - CharSequence -> ToStringSerializer
 *    - Unrecognized -> null
 * 6. Container Building (Collections, Maps, Arrays):
 *    - Collections: EnumSet, RandomAccess String List, RandomAccess Other List, String Set, Generic Set
 *    - Collections with JsonFormat.Shape.OBJECT -> null
 *    - Maps: standard MapSerializer, suppression values (NON_DEFAULT -> NON_EMPTY, NON_NULL)
 *    - Arrays: String[], primitive arrays, Object[]
 *    - MapLike & CollectionLike custom serializers and modifiers
 * 7. Key Serializer Construction (createKeySerializer):
 *    - Custom key serializers registered
 *    - Default key serializer provided
 *    - StdKeySerializers lookup (primitives, string, date)
 *    - @JsonValue method on key class
 *    - Fallback default key serializer
 *    - Modifier post-processing
 * 8. Utility & Defensive Guard Paths:
 *    - _verifyAsClass: null, non-class (IllegalStateException), noneClass, bogusClass, valid class
 *    - usesStaticTyping: explicit TypeSerializer present (forces false), JsonSerialize.Typing.STATIC/DYNAMIC,
 *      MapperFeature.USE_STATIC_TYPING
 *    - modifySecondaryTypesByAnnotation: illegal key-type annotation on non-Map type (IllegalArgumentException)
 * 9. Defect-Targeted Ground Truth Zone (Defects4J):
 *    - TestJsonValue::testJsonValueWithCustomOverride:
 *      Ensures custom serializer registered on module properly overrides @JsonValue serialization.
 * -------------------------------------------------------------------------------------------------------
 */
public class BasicSerializerFactoryGeminiTest {

    // Concrete harness extending abstract BasicSerializerFactory for testing protected methods
    static class TestableBasicSerializerFactory extends BasicSerializerFactory {
        public TestableBasicSerializerFactory() {
            super(null);
        }

        public TestableBasicSerializerFactory(SerializerFactoryConfig config) {
            super(config);
        }

        @Override
        public SerializerFactory withConfig(SerializerFactoryConfig config) {
            return new TestableBasicSerializerFactory(config);
        }

        @Override
        public JsonSerializer<Object> createSerializer(SerializerProvider prov, JavaType type)
                throws JsonMappingException {
            return null;
        }

        @Override
        protected Iterable<Serializers> customSerializers() {
            return _factoryConfig.serializers();
        }

        // Public delegators for protected methods
        public JsonSerializer<?> testFindSerializerByLookup(JavaType type, SerializationConfig config,
                BeanDescription beanDesc, boolean staticTyping) {
            return findSerializerByLookup(type, config, beanDesc, staticTyping);
        }

        public JsonSerializer<?> testFindSerializerByAnnotations(SerializerProvider prov, JavaType type,
                BeanDescription beanDesc) throws JsonMappingException {
            return findSerializerByAnnotations(prov, type, beanDesc);
        }

        public JsonSerializer<?> testFindSerializerByPrimaryType(SerializerProvider prov, JavaType type,
                BeanDescription beanDesc, boolean staticTyping) throws JsonMappingException {
            return findSerializerByPrimaryType(prov, type, beanDesc, staticTyping);
        }

        public JsonSerializer<?> testFindSerializerByAddonType(SerializationConfig config, JavaType javaType,
                BeanDescription beanDesc, boolean staticTyping) throws JsonMappingException {
            return findSerializerByAddonType(config, javaType, beanDesc, staticTyping);
        }

        public JsonSerializer<?> testBuildContainerSerializer(SerializerProvider prov, JavaType type,
                BeanDescription beanDesc, boolean staticTyping) throws JsonMappingException {
            return buildContainerSerializer(prov, type, beanDesc, staticTyping);
        }

        public JsonSerializer<?> testBuildCollectionSerializer(SerializationConfig config, CollectionType type,
                BeanDescription beanDesc, boolean staticTyping, TypeSerializer elementTypeSerializer,
                JsonSerializer<Object> elementValueSerializer) throws JsonMappingException {
            return buildCollectionSerializer(config, type, beanDesc, staticTyping, elementTypeSerializer,
                    elementValueSerializer);
        }

        public JsonSerializer<?> testBuildMapSerializer(SerializationConfig config, MapType type,
                BeanDescription beanDesc, boolean staticTyping, JsonSerializer<Object> keySerializer,
                TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
                throws JsonMappingException {
            return buildMapSerializer(config, type, beanDesc, staticTyping, keySerializer, elementTypeSerializer,
                    elementValueSerializer);
        }

        public JsonSerializer<?> testBuildArraySerializer(SerializationConfig config, ArrayType type,
                BeanDescription beanDesc, boolean staticTyping, TypeSerializer elementTypeSerializer,
                JsonSerializer<Object> elementValueSerializer) throws JsonMappingException {
            return buildArraySerializer(config, type, beanDesc, staticTyping, elementTypeSerializer,
                    elementValueSerializer);
        }

        public JsonSerializer<?> testBuildIteratorSerializer(SerializationConfig config, JavaType type,
                BeanDescription beanDesc, boolean staticTyping) throws JsonMappingException {
            return buildIteratorSerializer(config, type, beanDesc, staticTyping);
        }

        public JsonSerializer<?> testBuildIterableSerializer(SerializationConfig config, JavaType type,
                BeanDescription beanDesc, boolean staticTyping) throws JsonMappingException {
            return buildIterableSerializer(config, type, beanDesc, staticTyping);
        }

        public JsonSerializer<?> testBuildMapEntrySerializer(SerializationConfig config, JavaType type,
                BeanDescription beanDesc, boolean staticTyping, JavaType keyType, JavaType valueType)
                throws JsonMappingException {
            return buildMapEntrySerializer(config, type, beanDesc, staticTyping, keyType, valueType);
        }

        public Object testFindSuppressableContentValue(SerializationConfig config, JavaType contentType,
                BeanDescription beanDesc) throws JsonMappingException {
            return findSuppressableContentValue(config, contentType, beanDesc);
        }

        public boolean testUsesStaticTyping(SerializationConfig config, BeanDescription beanDesc,
                TypeSerializer typeSer) {
            return usesStaticTyping(config, beanDesc, typeSer);
        }

        public Class<?> testVerifyAsClass(Object src, String methodName, Class<?> noneClass) {
            return _verifyAsClass(src, methodName, noneClass);
        }

        public <T extends JavaType> T testModifySecondaryTypesByAnnotation(SerializationConfig config,
                Annotated a, T type) {
            return modifySecondaryTypesByAnnotation(config, a, type);
        }
    }

    // Helper POJOs and Annotations for testing
    static class PojoSerializable implements JsonSerializable {
        @Override
        public void serialize(JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString("serialized");
        }
        @Override
        public void serializeWithType(JsonGenerator gen, SerializerProvider serializers,
                TypeSerializer typeSer) throws IOException {
            serialize(gen, serializers);
        }
    }

    static class PojoWithJsonValue {
        @JsonValue
        public String getValue() {
            return "jsonValueOutput";
        }
    }

    static class KeyWithJsonValue {
        final String name;
        public KeyWithJsonValue(String name) { this.name = name; }
        @JsonValue
        public String asKey() { return "key:" + name; }
    }

    enum TestEnum { ALPHA, BETA }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    enum EnumAsObject { FIRST, SECOND }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    static class NumberFormattedAsString extends Number {
        @Override public int intValue() { return 1; }
        @Override public long longValue() { return 1L; }
        @Override public float floatValue() { return 1.0f; }
        @Override public double doubleValue() { return 1.0; }
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static class NumberFormattedAsObject extends Number {
        @Override public int intValue() { return 2; }
        @Override public long longValue() { return 2L; }
        @Override public float floatValue() { return 2.0f; }
        @Override public double doubleValue() { return 2.0; }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    static class NumberFormattedAsArray extends Number {
        @Override public int intValue() { return 3; }
        @Override public long longValue() { return 3L; }
        @Override public float floatValue() { return 3.0f; }
        @Override public double doubleValue() { return 3.0; }
    }

    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static class CollectionAsObject extends ArrayList<String> {}

    @JsonInclude(content = JsonInclude.Include.NON_DEFAULT)
    static class InclusionNonDefaultBean {}

    @JsonInclude(content = JsonInclude.Include.NON_NULL)
    static class InclusionNonNullBean {}

    @JsonSerialize(typing = JsonSerialize.Typing.STATIC)
    static class StaticTypingBean {}

    @JsonSerialize(typing = JsonSerialize.Typing.DYNAMIC)
    static class DynamicTypingBean {}

    static class DefaultTypingBean {}

    // Ground Truth target classes for Defects4J testJsonValueWithCustomOverride
    static class ValueClassCustom {
        @JsonValue
        public String value() {
            return "value";
        }
    }

    static class CustomOverrideSerializer extends JsonSerializer<ValueClassCustom> {
        @Override
        public void serialize(ValueClassCustom value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            gen.writeNumber(42);
        }
    }

    // ===================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ===================================================================================================

    @Test(timeout = 4000)
    public void testFactoryConfigAndFluentChaining() {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory(null);
        assertNotNull(factory.getFactoryConfig());
        assertFalse(factory.getFactoryConfig().hasSerializers());
        assertFalse(factory.getFactoryConfig().hasKeySerializers());
        assertFalse(factory.getFactoryConfig().hasSerializerModifiers());

        SerializerFactory f1 = factory.withAdditionalSerializers(new SimpleSerializers());
        assertTrue(f1.getFactoryConfig().hasSerializers());

        SerializerFactory f2 = factory.withAdditionalKeySerializers(new com.fasterxml.jackson.databind.module.SimpleKeySerializers());
        assertTrue(f2.getFactoryConfig().hasKeySerializers());

        SerializerFactory f3 = factory.withSerializerModifier(new BeanSerializerModifier());
        assertTrue(f3.getFactoryConfig().hasSerializerModifiers());
    }

    @Test(timeout = 4000)
    public void testFindSerializerByLookupStandardConcreteAndLazy() {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory();
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();

        // Concrete types
        Class<?>[] concreteTypes = new Class<?>[] {
            String.class, StringBuffer.class, StringBuilder.class, Character.class,
            Boolean.TYPE, Boolean.class, BigInteger.class, BigDecimal.class,
            Calendar.class, Date.class, Timestamp.class, UUID.class
        };
        for (Class<?> cls : concreteTypes) {
            JavaType type = config.constructType(cls);
            BeanDescription beanDesc = config.introspect(type);
            JsonSerializer<?> ser = factory.testFindSerializerByLookup(type, config, beanDesc, false);
            assertNotNull("Serializer for " + cls.getName() + " should not be null", ser);
        }

        // Lazy types
        Class<?>[] lazyTypes = new Class<?>[] {
            java.sql.Date.class, Time.class, com.fasterxml.jackson.databind.util.TokenBuffer.class
        };
        for (Class<?> cls : lazyTypes) {
            JavaType type = config.constructType(cls);
            BeanDescription beanDesc = config.introspect(type);
            JsonSerializer<?> ser = factory.testFindSerializerByLookup(type, config, beanDesc, false);
            assertNotNull("Lazy serializer for " + cls.getName() + " should not be null", ser);
        }

        // AtomicReference
        JavaType refType = config.getTypeFactory().constructReferenceType(AtomicReference.class,
                config.constructType(String.class));
        BeanDescription refDesc = config.introspect(refType);
        JsonSerializer<?> refSer = factory.testFindSerializerByLookup(refType, config, refDesc, false);
        assertNotNull(refSer);
        assertTrue(refSer instanceof AtomicReferenceSerializer);

        // Unknown type
        JavaType pojoType = config.constructType(PojoSerializable.class);
        assertNull(factory.testFindSerializerByLookup(pojoType, config, config.introspect(pojoType), false));
    }

    @Test(timeout = 4000)
    public void testFindSerializerByPrimaryTypeBranches() throws Exception {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory();
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        DefaultSerializerProvider prov = ((DefaultSerializerProvider) mapper.getSerializerProvider())
                .createInstance(config, factory);

        // Calendar, Date, ByteBuffer, InetAddress, InetSocketAddress, TimeZone, Charset
        Class<?>[] primaryTypes = new Class<?>[] {
            GregorianCalendar.class, Date.class, ByteBuffer.class,
            InetAddress.class, InetSocketAddress.class, TimeZone.class, Charset.class
        };
        for (Class<?> cls : primaryTypes) {
            JavaType type = config.constructType(cls);
            BeanDescription desc = config.introspect(type);
            JsonSerializer<?> ser = factory.testFindSerializerByPrimaryType(prov, type, desc, false);
            assertNotNull("Primary serializer for " + cls.getName() + " should be resolved", ser);
        }

        // Map.Entry
        JavaType mapEntryType = config.constructType(Map.Entry.class);
        JsonSerializer<?> entrySer = factory.testFindSerializerByPrimaryType(prov, mapEntryType,
                config.introspect(mapEntryType), false);
        assertNotNull(entrySer);
        assertTrue(entrySer instanceof MapEntrySerializer);

        // Number variations
        JavaType numStr = config.constructType(NumberFormattedAsString.class);
        JsonSerializer<?> serStr = factory.testFindSerializerByPrimaryType(prov, numStr, config.introspect(numStr), false);
        assertTrue(serStr instanceof ToStringSerializer);

        JavaType numObj = config.constructType(NumberFormattedAsObject.class);
        assertNull(factory.testFindSerializerByPrimaryType(prov, numObj, config.introspect(numObj), false));

        JavaType numArr = config.constructType(NumberFormattedAsArray.class);
        assertNull(factory.testFindSerializerByPrimaryType(prov, numArr, config.introspect(numArr), false));

        JavaType plainNum = config.constructType(Long.class);
        JsonSerializer<?> serNum = factory.testFindSerializerByPrimaryType(prov, plainNum, config.introspect(plainNum), false);
        assertTrue(serNum instanceof NumberSerializer);

        // Enum variations
        JavaType plainEnum = config.constructType(TestEnum.class);
        JsonSerializer<?> enumSer = factory.testFindSerializerByPrimaryType(prov, plainEnum, config.introspect(plainEnum), false);
        assertTrue(enumSer instanceof EnumSerializer);

        JavaType objEnum = config.constructType(EnumAsObject.class);
        assertNull(factory.testFindSerializerByPrimaryType(prov, objEnum, config.introspect(objEnum), false));
    }

    @Test(timeout = 4000)
    public void testFindSerializerByAddonTypeBranches() throws Exception {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory();
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();

        // Iterator
        JavaType iterType = config.constructType(Iterator.class);
        JsonSerializer<?> iterSer = factory.testFindSerializerByAddonType(config, iterType, config.introspect(iterType), false);
        assertTrue(iterSer instanceof IteratorSerializer);

        // Iterable
        JavaType iterableType = config.constructType(Iterable.class);
        JsonSerializer<?> iterableSer = factory.testFindSerializerByAddonType(config, iterableType, config.introspect(iterableType), false);
        assertTrue(iterableSer instanceof IterableSerializer);

        // CharSequence
        JavaType charSeqType = config.constructType(CharSequence.class);
        JsonSerializer<?> charSeqSer = factory.testFindSerializerByAddonType(config, charSeqType, config.introspect(charSeqType), false);
        assertSame(ToStringSerializer.instance, charSeqSer);

        // Non-addon
        JavaType plain = config.constructType(Integer.class);
        assertNull(factory.testFindSerializerByAddonType(config, plain, config.introspect(plain), false));
    }

    @Test(timeout = 4000)
    public void testBuildCollectionSerializerVariations() throws Exception {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory();
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();

        // 1. EnumSet
        JavaType enumSetType = config.getTypeFactory().constructCollectionType(EnumSet.class, TestEnum.class);
        JsonSerializer<?> enumSetSer = factory.testBuildCollectionSerializer(config, (CollectionType) enumSetType,
                config.introspect(enumSetType), false, null, null);
        assertTrue(enumSetSer instanceof EnumSetSerializer);

        // 2. RandomAccess Indexed List of String
        JavaType listStringType = config.getTypeFactory().constructCollectionType(ArrayList.class, String.class);
        JsonSerializer<?> indexedStringSer = factory.testBuildCollectionSerializer(config, (CollectionType) listStringType,
                config.introspect(listStringType), false, null, null);
        assertSame(IndexedStringListSerializer.instance, indexedStringSer);

        // 3. RandomAccess Indexed List of Integer
        JavaType listIntType = config.getTypeFactory().constructCollectionType(ArrayList.class, Integer.class);
        JsonSerializer<?> indexedIntSer = factory.testBuildCollectionSerializer(config, (CollectionType) listIntType,
                config.introspect(listIntType), false, null, null);
        assertTrue(indexedIntSer instanceof IndexedListSerializer);

        // 4. Non-indexed Collection of String (HashSet)
        JavaType setStringType = config.getTypeFactory().constructCollectionType(HashSet.class, String.class);
        JsonSerializer<?> setStringSer = factory.testBuildCollectionSerializer(config, (CollectionType) setStringType,
                config.introspect(setStringType), false, null, null);
        assertSame(StringCollectionSerializer.instance, setStringSer);

        // 5. Non-indexed Collection of Object (HashSet)
        JavaType setIntType = config.getTypeFactory().constructCollectionType(HashSet.class, Integer.class);
        JsonSerializer<?> setIntSer = factory.testBuildCollectionSerializer(config, (CollectionType) setIntType,
                config.introspect(setIntType), false, null, null);
        assertTrue(setIntSer instanceof CollectionSerializer);

        // 6. Collection with Shape.OBJECT -> null
        JavaType collAsObjType = config.constructType(CollectionAsObject.class);
        JsonSerializer<?> objCollSer = factory.testBuildCollectionSerializer(config, (CollectionType) collAsObjType,
                config.introspect(collAsObjType), false, null, null);
        assertNull(objCollSer);
    }

    @Test(timeout = 4000)
    public void testBuildArraySerializerVariations() throws Exception {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory();
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();

        // String[]
        ArrayType strArrType = config.getTypeFactory().constructArrayType(String.class);
        JsonSerializer<?> strArrSer = factory.testBuildArraySerializer(config, strArrType,
                config.introspect(strArrType), false, null, null);
        assertSame(StringArraySerializer.instance, strArrSer);

        // int[]
        ArrayType intArrType = config.getTypeFactory().constructArrayType(int.class);
        JsonSerializer<?> intArrSer = factory.testBuildArraySerializer(config, intArrType,
                config.introspect(intArrType), false, null, null);
        assertNotNull(intArrSer);
        assertEquals("com.fasterxml.jackson.databind.ser.std.StdArraySerializers$IntArraySerializer",
                intArrSer.getClass().getName());

        // Object[]
        ArrayType objArrType = config.getTypeFactory().constructArrayType(Object.class);
        JsonSerializer<?> objArrSer = factory.testBuildArraySerializer(config, objArrType,
                config.introspect(objArrType), false, null, null);
        assertTrue(objArrSer instanceof ObjectArraySerializer);
    }

    @Test(timeout = 4000)
    public void testCreateKeySerializerBranches() throws Exception {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory();
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();

        // 1. Standard key: String
        JavaType stringType = config.constructType(String.class);
        JsonSerializer<Object> strKeySer = factory.createKeySerializer(config, stringType, null);
        assertNotNull(strKeySer);

        // 2. Default implementation passed
        JsonSerializer<Object> dummySer = new ToStringSerializer();
        JsonSerializer<Object> returnedDefault = factory.createKeySerializer(config, config.constructType(Object.class), dummySer);
        assertSame(dummySer, returnedDefault);

        // 3. Key type with @JsonValue
        JavaType keyWithJsonValueType = config.constructType(KeyWithJsonValue.class);
        JsonSerializer<Object> valKeySer = factory.createKeySerializer(config, keyWithJsonValueType, null);
        assertTrue(valKeySer instanceof JsonValueSerializer);

        // 4. Custom key serializer via config
        final JsonSerializer<Object> customKeySer = new ToStringSerializer();
        SimpleModule mod = new SimpleModule();
        mod.addKeySerializer(TestEnum.class, customKeySer);
        SerializerFactoryConfig facConfig = new SerializerFactoryConfig()
                .withAdditionalKeySerializers(new com.fasterxml.jackson.databind.module.SimpleKeySerializers() {
                    @Override
                    public JsonSerializer<?> findKeySerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
                        if (type.getRawClass() == TestEnum.class) {
                            return customKeySer;
                        }
                        return null;
                    }
                });
        TestableBasicSerializerFactory customFactory = new TestableBasicSerializerFactory(facConfig);
        JsonSerializer<Object> resolvedCustom = customFactory.createKeySerializer(config, config.constructType(TestEnum.class), null);
        assertSame(customKeySer, resolvedCustom);
    }

    @Test(timeout = 4000)
    public void testCreateTypeSerializer() {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory();
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();

        // No type info -> returns null
        JavaType plainType = config.constructType(String.class);
        assertNull(factory.createTypeSerializer(config, plainType));
    }

    // ===================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ===================================================================================================

    @Test(timeout = 4000)
    public void testVerifyAsClassBoundaries() {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory();

        // Null input
        assertNull(factory.testVerifyAsClass(null, "testMethod", Object.class));

        // noneClass matching
        assertNull(factory.testVerifyAsClass(Void.class, "testMethod", Void.class));

        // Valid Class
        assertSame(String.class, factory.testVerifyAsClass(String.class, "testMethod", Void.class));
    }

    @Test(timeout = 4000)
    public void testFindSuppressableContentValueBoundaries() throws Exception {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory();
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();

        // NON_DEFAULT maps to NON_EMPTY
        JavaType nonDefaultType = config.constructType(InclusionNonDefaultBean.class);
        BeanDescription nonDefaultDesc = config.introspect(nonDefaultType);
        Object suppNonDefault = factory.testFindSuppressableContentValue(config, config.constructType(String.class), nonDefaultDesc);
        assertEquals(JsonInclude.Include.NON_EMPTY, suppNonDefault);

        // NON_NULL maps as is
        JavaType nonNullType = config.constructType(InclusionNonNullBean.class);
        BeanDescription nonNullDesc = config.introspect(nonNullType);
        Object suppNonNull = factory.testFindSuppressableContentValue(config, config.constructType(String.class), nonNullDesc);
        assertEquals(JsonInclude.Include.NON_NULL, suppNonNull);

        // No inclusion annotation -> returns null
        JavaType plainType = config.constructType(String.class);
        assertNull(factory.testFindSuppressableContentValue(config, plainType, config.introspect(plainType)));
    }

    @Test(timeout = 4000)
    public void testUsesStaticTypingBoundaries() {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory();
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();

        // 1. Non-null TypeSerializer forces false
        TypeSerializer dummyTypeSer = new TypeSerializer() {
            @Override public TypeSerializer forProperty(BeanProperty prop) { return this; }
            @Override public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() { return null; }
            @Override public String getPropertyName() { return null; }
            @Override public com.fasterxml.jackson.databind.jsontype.TypeIdResolver getTypeIdResolver() { return null; }
            @Override public void writeTypePrefixForScalar(Object value, JsonGenerator gen) throws IOException {}
            @Override public void writeTypePrefixForObject(Object value, JsonGenerator gen) throws IOException {}
            @Override public void writeTypePrefixForArray(Object value, JsonGenerator gen) throws IOException {}
            @Override public void writeTypeSuffixForScalar(Object value, JsonGenerator gen) throws IOException {}
            @Override public void writeTypeSuffixForObject(Object value, JsonGenerator gen) throws IOException {}
            @Override public void writeTypeSuffixForArray(Object value, JsonGenerator gen) throws IOException {}
            @Override public void writeCustomTypePrefixForScalar(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override public void writeCustomTypePrefixForObject(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override public void writeCustomTypePrefixForArray(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override public void writeCustomTypeSuffixForScalar(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override public void writeCustomTypeSuffixForObject(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override public void writeCustomTypeSuffixForArray(Object value, JsonGenerator gen, String typeId) throws IOException {}
        };
        JavaType staticType = config.constructType(StaticTypingBean.class);
        assertFalse(factory.testUsesStaticTyping(config, config.introspect(staticType), dummyTypeSer));

        // 2. STATIC typing annotation -> true
        assertTrue(factory.testUsesStaticTyping(config, config.introspect(staticType), null));

        // 3. DYNAMIC typing annotation -> false
        JavaType dynamicType = config.constructType(DynamicTypingBean.class);
        assertFalse(factory.testUsesStaticTyping(config, config.introspect(dynamicType), null));

        // 4. Default typing follows config feature
        JavaType defType = config.constructType(DefaultTypingBean.class);
        assertFalse(factory.testUsesStaticTyping(config, config.introspect(defType), null));
        SerializationConfig staticConfig = config.with(MapperFeature.USE_STATIC_TYPING);
        assertTrue(factory.testUsesStaticTyping(staticConfig, staticConfig.introspect(defType), null));
    }

    @Test(timeout = 4000)
    public void testDeprecatedIteratorAndIterableBuilders() throws Exception {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory();
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();

        JavaType rawIterType = config.constructType(Iterator.class);
        JsonSerializer<?> iterSer = factory.testBuildIteratorSerializer(config, rawIterType, config.introspect(rawIterType), false);
        assertTrue(iterSer instanceof IteratorSerializer);

        JavaType rawIterableType = config.constructType(Iterable.class);
        JsonSerializer<?> iterableSer = factory.testBuildIterableSerializer(config, rawIterableType, config.introspect(rawIterableType), false);
        assertTrue(iterableSer instanceof IterableSerializer);
    }

    // ===================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // ===================================================================================================

    /**
     * Directly targets Defects4J defect:
     * com.fasterxml.jackson.databind.ser.TestJsonValue::testJsonValueWithCustomOverride
     * expected:<[42]> but was:<["value"]>
     *
     * A class annotated with @JsonValue should allow its serialization to be overridden
     * by a custom module serializer registered on ObjectMapper.
     */
    @Test(timeout = 4000)
    public void testJsonValueWithCustomOverride() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ValueClassCustom.class, new CustomOverrideSerializer());
        mapper.registerModule(module);

        String json = mapper.writeValueAsString(new ValueClassCustom());
        assertEquals("42", json);
    }

    @Test(timeout = 4000)
    public void testFindSerializerByAnnotationsJsonSerializableAndJsonValue() throws Exception {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory();
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        DefaultSerializerProvider prov = ((DefaultSerializerProvider) mapper.getSerializerProvider())
                .createInstance(config, factory);

        // JsonSerializable implementation
        JavaType serializableType = config.constructType(PojoSerializable.class);
        JsonSerializer<?> ser1 = factory.testFindSerializerByAnnotations(prov, serializableType,
                config.introspect(serializableType));
        assertSame(SerializableSerializer.instance, ser1);

        // @JsonValue annotated class
        JavaType jsonValueType = config.constructType(PojoWithJsonValue.class);
        JsonSerializer<?> ser2 = factory.testFindSerializerByAnnotations(prov, jsonValueType,
                config.introspect(jsonValueType));
        assertTrue(ser2 instanceof JsonValueSerializer);

        // No annotation
        JavaType plainType = config.constructType(String.class);
        assertNull(factory.testFindSerializerByAnnotations(prov, plainType, config.introspect(plainType)));
    }

    // ===================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ===================================================================================================

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testVerifyAsClassThrowsOnInvalidType() {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory();
        factory.testVerifyAsClass("NotAClassInstance", "targetMethod", Void.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testModifySecondaryTypesByAnnotationThrowsOnNonMapWithKeyType() {
        TestableBasicSerializerFactory factory = new TestableBasicSerializerFactory();
        ObjectMapper mapper = new ObjectMapper();
        mapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            public Class<?> findSerializationKeyType(Annotated a, JavaType t) {
                return String.class;
            }
        });
        SerializationConfig config = mapper.getSerializationConfig();

        // Pass a CollectionType (container, but NOT a MapType) -> must throw IllegalArgumentException
        JavaType listType = config.constructType(ArrayList.class);
        AnnotatedClass ac = config.introspect(listType).getClassInfo();
        factory.testModifySecondaryTypesByAnnotation(config, ac, listType);
    }

    // ===================================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ===================================================================================================

    @Test(timeout = 4000)
    public void testFactoryImmutabilityAndModifiers() throws Exception {
        TestableBasicSerializerFactory base = new TestableBasicSerializerFactory();
        final boolean[] modifierCalled = new boolean[1];

        BeanSerializerModifier modifier = new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifyKeySerializer(SerializationConfig config, JavaType valueType,
                    BeanDescription beanDesc, JsonSerializer<?> serializer) {
                modifierCalled[0] = true;
                return serializer;
            }
        };

        SerializerFactory configuredFactory = base.withSerializerModifier(modifier);
        assertNotSame(base, configuredFactory);
        assertFalse(base.getFactoryConfig().hasSerializerModifiers());
        assertTrue(configuredFactory.getFactoryConfig().hasSerializerModifiers());

        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig();
        configuredFactory.createKeySerializer(config, config.constructType(String.class), null);
        assertTrue("Serializer modifier must be executed during key serializer creation", modifierCalled[0]);
    }
}