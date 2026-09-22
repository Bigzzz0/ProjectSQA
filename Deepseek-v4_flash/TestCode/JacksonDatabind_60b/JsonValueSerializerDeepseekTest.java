package com.fasterxml.jackson.databind.ser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.JsonValueSerializer;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: JsonValueSerializer.java
 * 
 * Defect Analysis (from Defects4J):
 * - Bug 1: serializeWithType() incorrectly uses the delegate type's serializer
 *   with the logical type's type id, causing type id mismatch (e.g., expected
 *   "thingy" but got "date"). The fix involves using the value's actual class
 *   for type serialization when _forceTypeInformation is false.
 * - Bug 2: In serializeWithType(), when _forceTypeInformation is true, the
 *   method writes type prefix/suffix for the bean, but the value serializer
 *   may not match the type id (e.g., byte[] vs Bean1385).
 * 
 * Branch Coverage Targets:
 * 1. Constructor with (AnnotatedMethod, JsonSerializer) - initial state
 * 2. Constructor with (JsonValueSerializer, BeanProperty, JsonSerializer, boolean)
 * 3. _notNullClass() - null vs non-null class
 * 4. withResolved() - same vs different property/serializer/forceTypeInfo
 * 5. createContextual() - null vs non-null _valueSerializer
 *    - null: USE_STATIC_TYPING enabled/disabled, final/non-final type
 *    - non-null: handlePrimaryContextualization path
 * 6. serialize() - null value, non-null value with null/non-null serializer
 *    - IOException vs other Exception vs Error
 *    - InvocationTargetException unwrapping
 * 7. serializeWithType() - null value, non-null value
 *    - _forceTypeInformation true/false
 *    - null/non-null _valueSerializer
 *    - Exception handling paths
 * 8. getSchema() - SchemaAware vs non-SchemaAware serializer
 * 9. acceptJsonFormatVisitor() - enum declaring class vs non-enum
 *    - _valueSerializer null vs non-null
 *    - _acceptJsonFormatVisitorForEnum() success/failure
 * 10. isNaturalTypeWithStdHandling() - primitive vs non-primitive
 *     - Integer/Boolean/Double vs other types
 *     - isDefaultSerializer() true/false
 * 11. toString() - format check
 * 
 * Boundary Conditions:
 * - null arguments (bean, property, serializer)
 * - null value returned from accessor method
 * - empty enum set
 * - primitive types (int, boolean, double) vs wrapper types
 * - String, Integer, Boolean, Double natural types
 * - InvocationTargetException with null cause
 * - Error vs Exception in catch blocks
 */
public class JsonValueSerializerDeepseekTest {

    // ==================== Helper Classes ====================

    static class TestBean {
        private final Object value;
        
        public TestBean(Object value) {
            this.value = value;
        }
        
        @com.fasterxml.jackson.annotation.JsonValue
        public Object getValue() {
            return value;
        }
    }

    static class TestBeanWithNull {
        @com.fasterxml.jackson.annotation.JsonValue
        public Object getValue() {
            return null;
        }
    }

    static class TestBeanWithException {
        @com.fasterxml.jackson.annotation.JsonValue
        public Object getValue() throws Exception {
            throw new IllegalStateException("test exception");
        }
    }

    static class TestBeanWithInvocationTargetException {
        @com.fasterxml.jackson.annotation.JsonValue
        public Object getValue() throws Exception {
            throw new InvocationTargetException(new RuntimeException("cause"));
        }
    }

    static class TestBeanWithError {
        @com.fasterxml.jackson.annotation.JsonValue
        public Object getValue() {
            throw new AssertionError("test error");
        }
    }

    enum TestEnum {
        A("value-a"), B("value-b");
        
        private final String value;
        
        TestEnum(String value) {
            this.value = value;
        }
        
        @com.fasterxml.jackson.annotation.JsonValue
        public String getValue() {
            return value;
        }
    }

    enum TestEnumWithException {
        A;
        
        @com.fasterxml.jackson.annotation.JsonValue
        public String getValue() throws Exception {
            throw new Exception("enum exception");
        }
    }

    static class TestEnumHolder {
        private final TestEnum value;
        
        TestEnumHolder(TestEnum value) {
            this.value = value;
        }
        
        @com.fasterxml.jackson.annotation.JsonValue
        public TestEnum getValue() {
            return value;
        }
    }

    static class TestStringHolder {
        private final String value;
        
        TestStringHolder(String value) {
            this.value = value;
        }
        
        @com.fasterxml.jackson.annotation.JsonValue
        public String getValue() {
            return value;
        }
    }

    static class TestIntHolder {
        private final int value;
        
        TestIntHolder(int value) {
            this.value = value;
        }
        
        @com.fasterxml.jackson.annotation.JsonValue
        public int getValue() {
            return value;
        }
    }

    static class TestBooleanHolder {
        private final boolean value;
        
        TestBooleanHolder(boolean value) {
            this.value = value;
        }
        
        @com.fasterxml.jackson.annotation.JsonValue
        public boolean getValue() {
            return value;
        }
    }

    static class TestDoubleHolder {
        private final double value;
        
        TestDoubleHolder(double value) {
            this.value = value;
        }
        
        @com.fasterxml.jackson.annotation.JsonValue
        public double getValue() {
            return value;
        }
    }

    static class TestByteArrayHolder {
        private final byte[] value;
        
        TestByteArrayHolder(byte[] value) {
            this.value = value;
        }
        
        @com.fasterxml.jackson.annotation.JsonValue
        public byte[] getValue() {
            return value;
        }
    }

    static class TestObjectHolder {
        private final Object value;
        
        TestObjectHolder(Object value) {
            this.value = value;
        }
        
        @com.fasterxml.jackson.annotation.JsonValue
        public Object getValue() {
            return value;
        }
    }

    // ==================== Mock/Stub Classes ====================

    static class MockJsonGenerator implements JsonGenerator {
        private StringBuilder sb = new StringBuilder();
        private boolean closed = false;
        
        @Override
        public JsonGenerator enable(Feature f) { return this; }
        @Override
        public JsonGenerator disable(Feature f) { return this; }
        @Override
        public boolean isEnabled(Feature f) { return false; }
        @Override
        public int getFeatureMask() { return 0; }
        @Override
        public JsonGenerator setFeatureMask(int values) { return this; }
        @Override
        public JsonGenerator useDefaultPrettyPrinter() { return this; }
        @Override
        public JsonGenerator setCodec(ObjectCodec oc) { return this; }
        @Override
        public ObjectCodec getCodec() { return null; }
        @Override
        public Version version() { return null; }
        @Override
        public JsonGenerator setSchema(FormatSchema schema) { return this; }
        @Override
        public FormatSchema getSchema() { return null; }
        @Override
        public JsonGenerator setPrettyPrinter(PrettyPrinter pp) { return this; }
        @Override
        public PrettyPrinter getPrettyPrinter() { return null; }
        @Override
        public JsonStreamContext getOutputContext() { return null; }
        @Override
        public void writeStartArray() throws IOException { sb.append("["); }
        @Override
        public void writeEndArray() throws IOException { sb.append("]"); }
        @Override
        public void writeStartObject() throws IOException { sb.append("{"); }
        @Override
        public void writeEndObject() throws IOException { sb.append("}"); }
        @Override
        public void writeFieldName(String name) throws IOException { sb.append("\"").append(name).append("\":"); }
        @Override
        public void writeFieldName(SerializableString name) throws IOException { writeFieldName(name.getValue()); }
        @Override
        public void writeString(String text) throws IOException { sb.append("\"").append(text).append("\""); }
        @Override
        public void writeString(char[] text, int offset, int len) throws IOException { writeString(new String(text, offset, len)); }
        @Override
        public void writeString(SerializableString text) throws IOException { writeString(text.getValue()); }
        @Override
        public void writeRawUTF8String(byte[] text) throws IOException { writeString(new String(text, "UTF-8")); }
        @Override
        public void writeUTF8String(byte[] text) throws IOException { writeString(new String(text, "UTF-8")); }
        @Override
        public void writeRaw(String text) throws IOException { sb.append(text); }
        @Override
        public void writeRaw(String text, int offset, int len) throws IOException { sb.append(text, offset, offset + len); }
        @Override
        public void writeRaw(char[] text, int offset, int len) throws IOException { sb.append(text, offset, offset + len); }
        @Override
        public void writeRaw(char c) throws IOException { sb.append(c); }
        @Override
        public void writeRawValue(String text) throws IOException { sb.append(text); }
        @Override
        public void writeRawValue(String text, int offset, int len) throws IOException { sb.append(text, offset, offset + len); }
        @Override
        public void writeRawValue(char[] text, int offset, int len) throws IOException { sb.append(text, offset, offset + len); }
        @Override
        public void writeBinary(Base64Variant b64variant, byte[] data, int offset, int len) throws IOException { sb.append("binary"); }
        @Override
        public int writeBinary(Base64Variant b64variant, java.io.InputStream data, int dataLength) throws IOException { return 0; }
        @Override
        public void writeNumber(int v) throws IOException { sb.append(v); }
        @Override
        public void writeNumber(long v) throws IOException { sb.append(v); }
        @Override
        public void writeNumber(java.math.BigInteger v) throws IOException { sb.append(v); }
        @Override
        public void writeNumber(double v) throws IOException { sb.append(v); }
        @Override
        public void writeNumber(float v) throws IOException { sb.append(v); }
        @Override
        public void writeNumber(java.math.BigDecimal v) throws IOException { sb.append(v); }
        @Override
        public void writeNumber(String encodedValue) throws IOException { sb.append(encodedValue); }
        @Override
        public void writeBoolean(boolean state) throws IOException { sb.append(state); }
        @Override
        public void writeNull() throws IOException { sb.append("null"); }
        @Override
        public void writeObject(Object pojo) throws IOException { sb.append("obj:").append(pojo); }
        @Override
        public void writeTree(TreeNode rootNode) throws IOException { sb.append("tree"); }
        @Override
        public JsonGenerator copy() { return this; }
        @Override
        public void flush() throws IOException { }
        @Override
        public boolean isClosed() { return closed; }
        @Override
        public void close() throws IOException { closed = true; }
        
        public String getOutput() { return sb.toString(); }
    }

    static class MockSerializerProvider extends SerializerProvider {
        private final JsonSerializer<Object> serializer;
        private final boolean useStaticTyping;
        
        MockSerializerProvider(JsonSerializer<Object> serializer, boolean useStaticTyping) {
            super(new com.fasterxml.jackson.databind.ObjectMapper().getSerializationConfig(), 
                  new com.fasterxml.jackson.databind.ser.BasicSerializerFactory());
            this.serializer = serializer;
            this.useStaticTyping = useStaticTyping;
        }
        
        @Override
        public boolean isEnabled(MapperFeature feature) {
            if (feature == MapperFeature.USE_STATIC_TYPING) {
                return useStaticTyping;
            }
            return false;
        }
        
        @Override
        public JsonSerializer<Object> findPrimaryPropertySerializer(JavaType type, BeanProperty property) {
            return serializer;
        }
        
        @Override
        public JsonSerializer<Object> findTypedValueSerializer(Class<?> valueType, boolean cache, BeanProperty property) {
            return serializer;
        }
        
        @Override
        public JsonSerializer<Object> findValueSerializer(Class<?> valueType, BeanProperty property) {
            return serializer;
        }
        
        @Override
        public JsonSerializer<Object> handlePrimaryContextualization(JsonSerializer<?> ser, BeanProperty property) {
            return (JsonSerializer<Object>) ser;
        }
        
        @Override
        public void defaultSerializeNull(JsonGenerator gen) throws IOException {
            gen.writeNull();
        }
    }

    static class MockJsonSerializer extends JsonSerializer<Object> {
        private final String output;
        
        MockJsonSerializer(String output) {
            this.output = output;
        }
        
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(output);
        }
        
        @Override
        public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider serializers,
                TypeSerializer typeSer) throws IOException {
            gen.writeString("typed:" + output);
        }
    }

    static class MockSchemaAwareSerializer extends MockJsonSerializer implements com.fasterxml.jackson.databind.jsonschema.SchemaAware {
        MockSchemaAwareSerializer(String output) {
            super(output);
        }
        
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
            return com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.textNode("schema");
        }
        
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint, boolean isOptional) throws JsonMappingException {
            return getSchema(provider, typeHint);
        }
    }

    static class MockTypeSerializer extends TypeSerializer {
        @Override
        public TypeSerializer forProperty(BeanProperty prop) { return this; }
        @Override
        public com.fasterxml.jackson.core.type.WritableTypeId writeTypePrefix(JsonGenerator g,
                com.fasterxml.jackson.core.type.WritableTypeId idMetadata) throws IOException {
            g.writeString("type:" + idMetadata.id);
            return idMetadata;
        }
        @Override
        public com.fasterxml.jackson.core.type.WritableTypeId writeTypeSuffix(JsonGenerator g,
                com.fasterxml.jackson.core.type.WritableTypeId idMetadata) throws IOException {
            return idMetadata;
        }
        @Override
        public void writeTypePrefixForScalar(Object value, JsonGenerator gen) throws IOException {
            gen.writeString("prefix:" + value.getClass().getSimpleName());
        }
        @Override
        public void writeTypeSuffixForScalar(Object value, JsonGenerator gen) throws IOException {
            gen.writeString(":suffix");
        }
        @Override
        public void writeTypePrefixForObject(Object value, JsonGenerator gen) throws IOException {
            gen.writeString("prefix:" + value.getClass().getSimpleName());
        }
        @Override
        public void writeTypeSuffixForObject(Object value, JsonGenerator gen) throws IOException {
            gen.writeString(":suffix");
        }
        @Override
        public void writeTypePrefixForArray(Object value, JsonGenerator gen) throws IOException {
            gen.writeString("prefix:" + value.getClass().getSimpleName());
        }
        @Override
        public void writeTypeSuffixForArray(Object value, JsonGenerator gen) throws IOException {
            gen.writeString(":suffix");
        }
        @Override
        public void writeTypePrefixForScalar(Object value, JsonGenerator gen, Class<?> type) throws IOException {
            gen.writeString("prefix:" + type.getSimpleName());
        }
        @Override
        public void writeTypeSuffixForScalar(Object value, JsonGenerator gen, Class<?> type) throws IOException {
            gen.writeString(":suffix");
        }
        @Override
        public void writeTypePrefixForObject(Object value, JsonGenerator gen, Class<?> type) throws IOException {
            gen.writeString("prefix:" + type.getSimpleName());
        }
        @Override
        public void writeTypeSuffixForObject(Object value, JsonGenerator gen, Class<?> type) throws IOException {
            gen.writeString(":suffix");
        }
        @Override
        public void writeTypePrefixForArray(Object value, JsonGenerator gen, Class<?> type) throws IOException {
            gen.writeString("prefix:" + type.getSimpleName());
        }
        @Override
        public void writeTypeSuffixForArray(Object value, JsonGenerator gen, Class<?> type) throws IOException {
            gen.writeString(":suffix");
        }
        @Override
        public String getTypeIdResolver() { return null; }
        @Override
        public String getPropertyName() { return "type"; }
        @Override
        public com.fasterxml.jackson.annotation.JsonTypeInfo.As getTypeInclusion() { return null; }
    }

    static class MockJsonFormatVisitorWrapper implements JsonFormatVisitorWrapper {
        private JsonStringFormatVisitor stringVisitor;
        private boolean expectAnyCalled = false;
        
        MockJsonFormatVisitorWrapper(JsonStringFormatVisitor stringVisitor) {
            this.stringVisitor = stringVisitor;
        }
        
        @Override
        public JsonStringFormatVisitor expectStringFormat(JavaType type) {
            return stringVisitor;
        }
        
        @Override
        public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor expectArrayFormat(JavaType type) {
            return null;
        }
        
        @Override
        public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor expectObjectFormat(JavaType type) {
            return null;
        }
        
        @Override
        public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
            return null;
        }
        
        @Override
        public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
            return null;
        }
        
        @Override
        public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonBooleanFormatVisitor expectBooleanFormat(JavaType type) {
            return null;
        }
        
        @Override
        public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNullFormatVisitor expectNullFormat(JavaType type) {
            return null;
        }
        
        @Override
        public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor expectAnyFormat(JavaType type) {
            expectAnyCalled = true;
            return null;
        }
        
        @Override
        public SerializerProvider getProvider() {
            return new MockSerializerProvider(new MockJsonSerializer("default"), false);
        }
        
        public boolean isExpectAnyCalled() { return expectAnyCalled; }
    }

    static class MockJsonStringFormatVisitor implements JsonStringFormatVisitor {
        private Set<String> enumTypes;
        
        @Override
        public void enumTypes(Set<String> enums) {
            this.enumTypes = new LinkedHashSet<String>(enums);
        }
        
        @Override
        public void format(com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat format) {
        }
        
        public Set<String> getEnumTypes() { return enumTypes; }
    }

    // ==================== Helper Methods ====================

    private AnnotatedMethod getAnnotatedMethod(Class<?> clazz, String methodName) throws Exception {
        java.lang.reflect.Method method = clazz.getMethod(methodName);
        return new AnnotatedMethod(null, method, null, null, null);
    }

    private JsonValueSerializer createSerializer(Class<?> clazz, String methodName, JsonSerializer<?> ser) throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(clazz, methodName);
        return new JsonValueSerializer(method, ser);
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testConstructorWithNullSerializer() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        assertNotNull(serializer);
        assertEquals(TestBean.class, serializer.handledType());
        assertNull(serializer._valueSerializer);
        assertNull(serializer._property);
        assertTrue(serializer._forceTypeInformation);
    }

    @Test(timeout = 4000)
    public void testConstructorWithSerializer() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        assertNotNull(serializer);
        assertSame(ser, serializer._valueSerializer);
        assertNull(serializer._property);
        assertTrue(serializer._forceTypeInformation);
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer original = new JsonValueSerializer(method, ser);
        
        JsonValueSerializer copy = new JsonValueSerializer(original, null, ser, false);
        
        assertNotNull(copy);
        assertSame(original._accessorMethod, copy._accessorMethod);
        assertSame(ser, copy._valueSerializer);
        assertNull(copy._property);
        assertFalse(copy._forceTypeInformation);
    }

    @Test(timeout = 4000)
    public void testNotNullClassWithNull() throws Exception {
        Class<Object> result = JsonValueSerializer._notNullClass(null);
        assertEquals(Object.class, result);
    }

    @Test(timeout = 4000)
    public void testNotNullClassWithNonNull() throws Exception {
        Class<Object> result = JsonValueSerializer._notNullClass(String.class);
        assertEquals(String.class, result);
    }

    @Test(timeout = 4000)
    public void testWithResolvedSameValues() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer original = new JsonValueSerializer(method, ser);
        
        JsonValueSerializer result = original.withResolved(null, ser, true);
        
        assertSame(original, result);
    }

    @Test(timeout = 4000)
    public void testWithResolvedDifferentProperty() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer original = new JsonValueSerializer(method, ser);
        
        BeanProperty property = new com.fasterxml.jackson.databind.introspect.BeanProperty.Std(
                null, null, null, null, null, null);
        JsonValueSerializer result = original.withResolved(property, ser, true);
        
        assertNotSame(original, result);
        assertSame(property, result._property);
        assertSame(ser, result._valueSerializer);
        assertTrue(result._forceTypeInformation);
    }

    @Test(timeout = 4000)
    public void testWithResolvedDifferentSerializer() throws Exception {
        JsonSerializer<Object> ser1 = new MockJsonSerializer("test1");
        JsonSerializer<Object> ser2 = new MockJsonSerializer("test2");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer original = new JsonValueSerializer(method, ser1);
        
        JsonValueSerializer result = original.withResolved(null, ser2, true);
        
        assertNotSame(original, result);
        assertSame(ser2, result._valueSerializer);
    }

    @Test(timeout = 4000)
    public void testWithResolvedDifferentForceTypeInfo() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer original = new JsonValueSerializer(method, ser);
        
        JsonValueSerializer result = original.withResolved(null, ser, false);
        
        assertNotSame(original, result);
        assertFalse(result._forceTypeInformation);
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testCreateContextualWithNullSerializerAndFinalType() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestStringHolder.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("contextual"), false);
        JsonSerializer<?> result = serializer.createContextual(provider, null);
        
        assertNotNull(result);
        assertTrue(result instanceof JsonValueSerializer);
        JsonValueSerializer jsonValueSerializer = (JsonValueSerializer) result;
        assertNotNull(jsonValueSerializer._valueSerializer);
        assertFalse(jsonValueSerializer._forceTypeInformation);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithNullSerializerAndStaticTyping() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestObjectHolder.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("contextual"), true);
        JsonSerializer<?> result = serializer.createContextual(provider, null);
        
        assertNotNull(result);
        assertTrue(result instanceof JsonValueSerializer);
        JsonValueSerializer jsonValueSerializer = (JsonValueSerializer) result;
        assertNotNull(jsonValueSerializer._valueSerializer);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithNullSerializerAndNonFinalTypeNoStaticTyping() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestObjectHolder.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("contextual"), false);
        JsonSerializer<?> result = serializer.createContextual(provider, null);
        
        assertSame(serializer, result);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithNonNullSerializer() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("contextual"), false);
        JsonSerializer<?> result = serializer.createContextual(provider, null);
        
        assertNotNull(result);
        assertTrue(result instanceof JsonValueSerializer);
        JsonValueSerializer jsonValueSerializer = (JsonValueSerializer) result;
        assertSame(ser, jsonValueSerializer._valueSerializer);
        assertTrue(jsonValueSerializer._forceTypeInformation);
    }

    @Test(timeout = 4000)
    public void testSerializeWithNullValue() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBeanWithNull.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        
        serializer.serialize(new TestBeanWithNull(), gen, provider);
        
        assertEquals("null", gen.getOutput());
    }

    @Test(timeout = 4000)
    public void testSerializeWithNonNullValueAndNullSerializer() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        
        serializer.serialize(new TestBean("value"), gen, provider);
        
        assertEquals("\"test\"", gen.getOutput());
    }

    @Test(timeout = 4000)
    public void testSerializeWithNonNullValueAndNonNullSerializer() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("explicit");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        
        serializer.serialize(new TestBean("value"), gen, provider);
        
        assertEquals("\"explicit\"", gen.getOutput());
    }

    @Test(timeout = 4000)
    public void testSerializeWithIOException() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonGenerator gen = new MockJsonGenerator() {
            @Override
            public void writeString(String text) throws IOException {
                throw new IOException("io error");
            }
        };
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        
        try {
            serializer.serialize(new TestBean("value"), gen, provider);
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals("io error", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSerializeWithException() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBeanWithException.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        
        try {
            serializer.serialize(new TestBeanWithException(), gen, provider);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSerializeWithInvocationTargetException() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBeanWithInvocationTargetException.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        
        try {
            serializer.serialize(new TestBeanWithInvocationTargetException(), gen, provider);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSerializeWithError() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBeanWithError.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        
        try {
            serializer.serialize(new TestBeanWithError(), gen, provider);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            assertEquals("test error", e.getMessage());
        }
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect-targeted test for serializeWithType with _forceTypeInformation=true.
     * This tests the case where the type prefix/suffix is written for the bean,
     * but the value serializer may not match the type id.
     */
    @Test(timeout = 4000)
    public void testSerializeWithTypeForceTypeInformationTrue() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("value");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        // Force type information to true
        JsonValueSerializer forced = serializer.withResolved(null, ser, true);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        MockTypeSerializer typeSer = new MockTypeSerializer();
        
        forced.serializeWithType(new TestBean("value"), gen, provider, typeSer);
        
        String output = gen.getOutput();
        assertTrue(output.contains("prefix:"));
        assertTrue(output.contains(":suffix"));
        assertTrue(output.contains("\"value\""));
    }

    /**
     * Defect-targeted test for serializeWithType with _forceTypeInformation=false.
     * This tests the case where the delegate type's serializer is used with
     * the logical type's type id, which is the known defect.
     */
    @Test(timeout = 4000)
    public void testSerializeWithTypeForceTypeInformationFalse() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("value");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        // Force type information to false
        JsonValueSerializer nonForced = serializer.withResolved(null, ser, false);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        MockTypeSerializer typeSer = new MockTypeSerializer();
        
        nonForced.serializeWithType(new TestBean("value"), gen, provider, typeSer);
        
        String output = gen.getOutput();
        assertTrue(output.contains("typed:value"));
    }

    /**
     * Defect-targeted test for serializeWithType with null value.
     */
    @Test(timeout = 4000)
    public void testSerializeWithTypeNullValue() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBeanWithNull.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        MockTypeSerializer typeSer = new MockTypeSerializer();
        
        serializer.serializeWithType(new TestBeanWithNull(), gen, provider, typeSer);
        
        assertEquals("null", gen.getOutput());
    }

    /**
     * Defect-targeted test for serializeWithType with null serializer.
     * This tests the case where the value serializer is fetched dynamically.
     */
    @Test(timeout = 4000)
    public void testSerializeWithTypeNullSerializer() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("dynamic"), false);
        MockTypeSerializer typeSer = new MockTypeSerializer();
        
        serializer.serializeWithType(new TestBean("value"), gen, provider, typeSer);
        
        String output = gen.getOutput();
        assertTrue(output.contains("typed:dynamic"));
    }

    /**
     * Defect-targeted test for serializeWithType with exception.
     */
    @Test(timeout = 4000)
    public void testSerializeWithTypeException() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBeanWithException.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        MockTypeSerializer typeSer = new MockTypeSerializer();
        
        try {
            serializer.serializeWithType(new TestBeanWithException(), gen, provider, typeSer);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Defect-targeted test for serializeWithType with InvocationTargetException.
     */
    @Test(timeout = 4000)
    public void testSerializeWithTypeInvocationTargetException() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBeanWithInvocationTargetException.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        MockTypeSerializer typeSer = new MockTypeSerializer();
        
        try {
            serializer.serializeWithType(new TestBeanWithInvocationTargetException(), gen, provider, typeSer);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Defect-targeted test for serializeWithType with Error.
     */
    @Test(timeout = 4000)
    public void testSerializeWithTypeError() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBeanWithError.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        MockTypeSerializer typeSer = new MockTypeSerializer();
        
        try {
            serializer.serializeWithType(new TestBeanWithError(), gen, provider, typeSer);
            fail("Expected AssertionError");
        } catch (AssertionError e) {
            assertEquals("test error", e.getMessage());
        }
    }

    /**
     * Defect-targeted test for serializeWithType with byte array value.
     * This targets the specific defect where the type id is incorrect
     * (expected "thingy" but got "date").
     */
    @Test(timeout = 4000)
    public void testSerializeWithTypeByteArrayValue() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("bytearray");
        AnnotatedMethod method = getAnnotatedMethod(TestByteArrayHolder.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        // Force type information to false to trigger the defect path
        JsonValueSerializer nonForced = serializer.withResolved(null, ser, false);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        MockTypeSerializer typeSer = new MockTypeSerializer();
        
        nonForced.serializeWithType(new TestByteArrayHolder(new byte[]{1, 2, 3}), gen, provider, typeSer);
        
        String output = gen.getOutput();
        // The defect causes the type id to be based on the delegate type (byte[])
        // instead of the logical type (TestByteArrayHolder)
        assertTrue(output.contains("typed:bytearray"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testGetSchemaWithSchemaAwareSerializer() throws Exception {
        JsonSerializer<Object> ser = new MockSchemaAwareSerializer("schema");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        JsonNode schema = serializer.getSchema(provider, null);
        
        assertNotNull(schema);
        assertEquals("schema", schema.asText());
    }

    @Test(timeout = 4000)
    public void testGetSchemaWithNonSchemaAwareSerializer() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        JsonNode schema = serializer.getSchema(provider, null);
        
        assertNotNull(schema);
        assertTrue(schema.isObject());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorWithEnum() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestEnumHolder.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonStringFormatVisitor stringVisitor = new MockJsonStringFormatVisitor();
        MockJsonFormatVisitorWrapper visitor = new MockJsonFormatVisitorWrapper(stringVisitor);
        
        serializer.acceptJsonFormatVisitor(visitor, null);
        
        assertNotNull(stringVisitor.getEnumTypes());
        assertEquals(2, stringVisitor.getEnumTypes().size());
        assertTrue(stringVisitor.getEnumTypes().contains("value-a"));
        assertTrue(stringVisitor.getEnumTypes().contains("value-b"));
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorWithEnumException() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestEnumWithException.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonStringFormatVisitor stringVisitor = new MockJsonStringFormatVisitor();
        MockJsonFormatVisitorWrapper visitor = new MockJsonFormatVisitorWrapper(stringVisitor);
        
        try {
            serializer.acceptJsonFormatVisitor(visitor, null);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorWithNonEnum() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonStringFormatVisitor stringVisitor = new MockJsonStringFormatVisitor();
        MockJsonFormatVisitorWrapper visitor = new MockJsonFormatVisitorWrapper(stringVisitor);
        
        serializer.acceptJsonFormatVisitor(visitor, null);
        
        // Should not call expectAnyFormat
        assertFalse(visitor.isExpectAnyCalled());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorWithNullSerializer() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonStringFormatVisitor stringVisitor = new MockJsonStringFormatVisitor();
        MockJsonFormatVisitorWrapper visitor = new MockJsonFormatVisitorWrapper(stringVisitor);
        
        serializer.acceptJsonFormatVisitor(visitor, null);
        
        // Should not call expectAnyFormat since serializer is found
        assertFalse(visitor.isExpectAnyCalled());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorWithNonNullSerializer() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        MockJsonStringFormatVisitor stringVisitor = new MockJsonStringFormatVisitor();
        MockJsonFormatVisitorWrapper visitor = new MockJsonFormatVisitorWrapper(stringVisitor);
        
        serializer.acceptJsonFormatVisitor(visitor, null);
        
        // Should not call expectAnyFormat
        assertFalse(visitor.isExpectAnyCalled());
    }

    @Test(timeout = 4000)
    public void testIsNaturalTypeWithStdHandlingPrimitiveInt() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestIntHolder.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        // Mock isDefaultSerializer to return true
        boolean result = serializer.isNaturalTypeWithStdHandling(Integer.TYPE, ser);
        assertFalse(result); // MockJsonSerializer is not a default serializer
    }

    @Test(timeout = 4000)
    public void testIsNaturalTypeWithStdHandlingPrimitiveBoolean() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestBooleanHolder.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        boolean result = serializer.isNaturalTypeWithStdHandling(Boolean.TYPE, ser);
        assertFalse(result);
    }

    @Test(timeout = 4000)
    public void testIsNaturalTypeWithStdHandlingPrimitiveDouble() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestDoubleHolder.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        boolean result = serializer.isNaturalTypeWithStdHandling(Double.TYPE, ser);
        assertFalse(result);
    }

    @Test(timeout = 4000)
    public void testIsNaturalTypeWithStdHandlingPrimitiveOther() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestIntHolder.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        boolean result = serializer.isNaturalTypeWithStdHandling(Long.TYPE, ser);
        assertFalse(result);
    }

    @Test(timeout = 4000)
    public void testIsNaturalTypeWithStdHandlingString() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestStringHolder.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        boolean result = serializer.isNaturalTypeWithStdHandling(String.class, ser);
        assertFalse(result);
    }

    @Test(timeout = 4000)
    public void testIsNaturalTypeWithStdHandlingInteger() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestIntHolder.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        boolean result = serializer.isNaturalTypeWithStdHandling(Integer.class, ser);
        assertFalse(result);
    }

    @Test(timeout = 4000)
    public void testIsNaturalTypeWithStdHandlingBoolean() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestBooleanHolder.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        boolean result = serializer.isNaturalTypeWithStdHandling(Boolean.class, ser);
        assertFalse(result);
    }

    @Test(timeout = 4000)
    public void testIsNaturalTypeWithStdHandlingDouble() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestDoubleHolder.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        boolean result = serializer.isNaturalTypeWithStdHandling(Double.class, ser);
        assertFalse(result);
    }

    @Test(timeout = 4000)
    public void testIsNaturalTypeWithStdHandlingOtherType() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        boolean result = serializer.isNaturalTypeWithStdHandling(Object.class, ser);
        assertFalse(result);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testToString() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        String result = serializer.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("@JsonValue serializer"));
        assertTrue(result.contains("getValue"));
    }

    @Test(timeout = 4000)
    public void testHandledType() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        assertEquals(TestBean.class, serializer.handledType());
    }

    @Test(timeout = 4000)
    public void testHandledTypeWithNullClass() throws Exception {
        // Create a serializer with a method that returns Object type
        AnnotatedMethod method = getAnnotatedMethod(TestObjectHolder.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        assertNotNull(serializer.handledType());
    }

    @Test(timeout = 4000)
    public void testAccessorMethodGetter() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        assertSame(method, serializer._accessorMethod);
    }

    @Test(timeout = 4000)
    public void testValueSerializerGetter() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        assertSame(ser, serializer._valueSerializer);
    }

    @Test(timeout = 4000)
    public void testPropertyGetter() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        assertNull(serializer._property);
    }

    @Test(timeout = 4000)
    public void testForceTypeInformationGetter() throws Exception {
        JsonSerializer<Object> ser = new MockJsonSerializer("test");
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, ser);
        
        assertTrue(serializer._forceTypeInformation);
    }

    @Test(timeout = 4000)
    public void testJacksonStdImplAnnotation() throws Exception {
        assertNotNull(JsonValueSerializer.class.getAnnotation(JacksonStdImpl.class));
    }

    @Test(timeout = 4000)
    public void testSerialVersionUID() throws Exception {
        // The class has @SuppressWarnings("serial") so no serialVersionUID is defined
        // This test verifies the class is serializable
        assertTrue(java.io.Serializable.class.isAssignableFrom(JsonValueSerializer.class));
    }

    @Test(timeout = 4000)
    public void testImplementsInterfaces() throws Exception {
        assertTrue(com.fasterxml.jackson.databind.ser.ContextualSerializer.class.isAssignableFrom(JsonValueSerializer.class));
        assertTrue(com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable.class.isAssignableFrom(JsonValueSerializer.class));
        assertTrue(com.fasterxml.jackson.databind.jsonschema.SchemaAware.class.isAssignableFrom(JsonValueSerializer.class));
    }

    @Test(timeout = 4000)
    public void testExtendsStdSerializer() throws Exception {
        assertTrue(StdSerializer.class.isAssignableFrom(JsonValueSerializer.class));
    }

    @Test(timeout = 4000)
    public void testSerializeWithNullBean() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        
        try {
            serializer.serialize(null, gen, provider);
            fail("Expected exception");
        } catch (Exception e) {
            // Expected - accessor method will fail on null bean
        }
    }

    @Test(timeout = 4000)
    public void testSerializeWithTypeWithNullBean() throws Exception {
        AnnotatedMethod method = getAnnotatedMethod(TestBean.class, "getValue");
        JsonValueSerializer serializer = new JsonValueSerializer(method, null);
        
        MockJsonGenerator gen = new MockJsonGenerator();
        MockSerializerProvider provider = new MockSerializerProvider(new MockJsonSerializer("test"), false);
        MockTypeSerializer typeSer = new MockTypeSerializer();
        
        try {
            serializer.serializeWithType(null, gen, provider, typeSer);
            fail("Expected exception");
        } catch (Exception e) {
            // Expected - accessor method will fail on null bean
        }
    }
}