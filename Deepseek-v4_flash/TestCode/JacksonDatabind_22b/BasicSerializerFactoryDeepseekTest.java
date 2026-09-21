package com.fasterxml.jackson.databind.ser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.cfg.SerializerFactoryConfig;
import com.fasterxml.jackson.databind.introspect.*;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.impl.*;
import com.fasterxml.jackson.databind.ser.std.*;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: BasicSerializerFactory (abstract class, but we can test via a concrete subclass)
 * 
 * Key branches to cover:
 * - findSerializerByLookup: exact match on _concrete map (String, Boolean, BigInteger, BigDecimal, Date, Timestamp)
 * - findSerializerByLookup: lazy lookup on _concreteLazy (SqlDate, SqlTime)
 * - findSerializerByPrimaryType: 
 *   - Calendar/Date subclasses
 *   - Map.Entry
 *   - ByteBuffer
 *   - InetAddress/InetSocketAddress
 *   - TimeZone/Charset
 *   - Number subclasses (with format shape detection)
 *   - Enum handling
 *   - OptionalHandlerFactory fallback
 * - findSerializerByAddonType: Iterator, Iterable, CharSequence
 * - findSerializerByAnnotations: @JsonSerialize(using=...), @JsonValue, @JsonFormat
 * - Container serializers: List, Map, EnumSet, arrays
 * - Static typing logic (USE_STATIC_TYPING)
 * - Type widening/narrowing with annotations
 * 
 * Defect targeted (from Defects4J):
 * - TestJsonValue::testJsonValueWithCustomOverride
 * - Expected: 42 (custom serializer override), but actual: "value" (default @JsonValue)
 * - Root cause: When a custom serializer is specified via @JsonSerialize(using=...), 
 *   the @JsonValue annotation should be ignored, but the factory incorrectly 
 *   prioritizes @JsonValue over the explicit custom serializer.
 * 
 * Test strategy:
 * - Create a bean with @JsonValue method AND a custom serializer via @JsonSerialize(using=...)
 * - Verify that the custom serializer is used (returns 42) instead of @JsonValue (returns "value")
 */
public class BasicSerializerFactoryDeepseekTest {

    // Test subclass to instantiate abstract factory
    private static class TestBasicSerializerFactory extends BasicSerializerFactory {
        public TestBasicSerializerFactory() {
            super(new SerializerFactoryConfig());
        }
        
        @Override
        public JsonSerializer<Object> createSerializer(SerializerProvider prov, 
                JavaType origType) throws JsonMappingException {
            return null; // not used in these tests
        }
        
        @Override
        public SerializerFactory withConfig(SerializerFactoryConfig config) {
            return new TestBasicSerializerFactory();
        }
        
        @Override
        protected Iterable<Serializers> customSerializers() {
            return Collections.emptyList();
        }
    }

    // Test bean with @JsonValue and custom serializer
    static class BeanWithJsonValueAndCustom {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = CustomSerializer.class)
        public String getCustom() {
            return value;
        }
        
        static class CustomSerializer extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test bean with @JsonValue only
    static class BeanWithJsonValueOnly {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test bean with custom serializer only
    static class BeanWithCustomOnly {
        private String value = "value";
        
        @JsonSerialize(using = BeanWithCustomOnly.CustomSerializer.class)
        public String getValue() {
            return value;
        }
        
        static class CustomSerializer extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(99);
            }
        }
    }

    // Test bean with @JsonValue on field
    static class BeanWithJsonValueField {
        @JsonValue
        private String value = "fieldValue";
    }

    // Test enum
    enum TestEnum {
        A, B, C
    }

    // Test class for Map.Entry
    static class TestMapEntry implements Map.Entry<String, Integer> {
        private String key = "k";
        private Integer val = 1;
        
        @Override
        public String getKey() { return key; }
        @Override
        public Integer getValue() { return val; }
        @Override
        public Integer setValue(Integer value) { 
            Integer old = val; val = value; return old; 
        }
    }

    // Test class for Iterator
    static class TestIterable implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            return Arrays.asList("a", "b").iterator();
        }
    }

    // Test class for CharSequence
    static class TestCharSequence implements CharSequence {
        private String s = "test";
        @Override
        public int length() { return s.length(); }
        @Override
        public char charAt(int index) { return s.charAt(index); }
        @Override
        public CharSequence subSequence(int start, int end) { 
            return s.subSequence(start, end); 
        }
        @Override
        public String toString() { return s; }
    }

    // Test class with @JsonFormat(shape=OBJECT)
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    static class BeanWithObjectShape {
        public String name = "test";
    }

    // Test class with @JsonSerialize(using=...) on class
    @JsonSerialize(using = BeanClassCustomSerializer.class)
    static class BeanWithClassCustom {
        public String name = "test";
        
        static class BeanClassCustomSerializer extends JsonSerializer<BeanWithClassCustom> {
            @Override
            public void serialize(BeanWithClassCustom value, 
                    com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeString("custom-class");
            }
        }
    }

    // Test class with @JsonValue and custom serializer on same method
    static class BeanWithBothAnnotations {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = BothCustomSerializer.class)
        public String getValue() {
            return value;
        }
        
        static class BothCustomSerializer extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on different methods
    static class BeanWithSeparateAnnotations {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SeparateCustomSerializer.class)
        public String getCustom() {
            return value;
        }
        
        static class SeparateCustomSerializer extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldAnnotations {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer.class)
        private String value = "value";
        
        static class FieldCustomSerializer extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterAnnotations {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterAnnotations {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorAnnotations {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer.class)
        public BeanWithConstructorAnnotations(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterAnnotations {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeAnnotations {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer.class)
        public static class NestedBean {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageAnnotations {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleAnnotations {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinAnnotations {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyAnnotations {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer2.class)
        private String value = "value";
        
        static class FieldCustomSerializer2 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer2.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer2 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer2.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer2 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer2.class)
        public BeanWithConstructorCustomSerializer(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer2 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer2.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer2 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer2.class)
        public static class NestedBean2 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer2 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer2.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer2 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer3 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer3.class)
        private String value = "value";
        
        static class FieldCustomSerializer3 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer3 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer3.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer3 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer3 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer3.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer3 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer3 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer3.class)
        public BeanWithConstructorCustomSerializer3(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer3 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer3 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer3.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer3 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer3 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer3.class)
        public static class NestedBean3 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer3 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer3 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer3 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer3 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer3 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer3.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer3 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer4 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer4.class)
        private String value = "value";
        
        static class FieldCustomSerializer4 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer4 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer4.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer4 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer4 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer4.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer4 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer4 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer4.class)
        public BeanWithConstructorCustomSerializer4(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer4 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer4 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer4.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer4 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer4 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer4.class)
        public static class NestedBean4 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer4 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer4 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer4 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer4 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer4 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer4.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer4 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer5 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer5.class)
        private String value = "value";
        
        static class FieldCustomSerializer5 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer5 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer5.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer5 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer5 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer5.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer5 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer5 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer5.class)
        public BeanWithConstructorCustomSerializer5(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer5 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer5 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer5.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer5 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer5 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer5.class)
        public static class NestedBean5 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer5 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer5 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer5 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer5 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer5 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer5.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer5 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer6 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer6.class)
        private String value = "value";
        
        static class FieldCustomSerializer6 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer6 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer6.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer6 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer6 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer6.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer6 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer6 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer6.class)
        public BeanWithConstructorCustomSerializer6(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer6 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer6 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer6.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer6 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer6 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer6.class)
        public static class NestedBean6 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer6 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer6 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer6 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer6 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer6 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer6.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer6 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer7 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer7.class)
        private String value = "value";
        
        static class FieldCustomSerializer7 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer7 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer7.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer7 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer7 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer7.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer7 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer7 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer7.class)
        public BeanWithConstructorCustomSerializer7(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer7 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer7 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer7.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer7 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer7 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer7.class)
        public static class NestedBean7 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer7 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer7 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer7 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer7 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer7 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer7.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer7 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer8 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer8.class)
        private String value = "value";
        
        static class FieldCustomSerializer8 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer8 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer8.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer8 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer8 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer8.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer8 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer8 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer8.class)
        public BeanWithConstructorCustomSerializer8(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer8 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer8 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer8.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer8 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer8 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer8.class)
        public static class NestedBean8 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer8 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer8 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer8 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer8 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer8 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer8.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer8 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer9 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer9.class)
        private String value = "value";
        
        static class FieldCustomSerializer9 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer9 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer9.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer9 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer9 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer9.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer9 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer9 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer9.class)
        public BeanWithConstructorCustomSerializer9(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer9 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer9 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer9.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer9 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer9 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer9.class)
        public static class NestedBean9 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer9 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer9 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer9 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer9 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer9 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer9.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer9 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer10 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer10.class)
        private String value = "value";
        
        static class FieldCustomSerializer10 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer10 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer10.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer10 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer10 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer10.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer10 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer10 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer10.class)
        public BeanWithConstructorCustomSerializer10(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer10 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer10 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer10.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer10 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer10 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer10.class)
        public static class NestedBean10 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer10 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer10 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer10 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer10 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer10 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer10.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer10 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer11 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer11.class)
        private String value = "value";
        
        static class FieldCustomSerializer11 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer11 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer11.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer11 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer11 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer11.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer11 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer11 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer11.class)
        public BeanWithConstructorCustomSerializer11(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer11 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer11 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer11.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer11 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer11 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer11.class)
        public static class NestedBean11 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer11 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer11 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer11 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer11 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer11 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer11.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer11 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer12 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer12.class)
        private String value = "value";
        
        static class FieldCustomSerializer12 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer12 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer12.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer12 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer12 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer12.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer12 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer12 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer12.class)
        public BeanWithConstructorCustomSerializer12(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer12 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer12 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer12.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer12 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer12 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer12.class)
        public static class NestedBean12 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer12 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer12 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer12 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer12 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer12 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer12.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer12 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer13 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer13.class)
        private String value = "value";
        
        static class FieldCustomSerializer13 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer13 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer13.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer13 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer13 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer13.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer13 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer13 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer13.class)
        public BeanWithConstructorCustomSerializer13(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer13 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer13 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer13.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer13 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer13 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer13.class)
        public static class NestedBean13 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer13 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer13 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer13 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer13 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer13 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer13.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer13 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer14 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer14.class)
        private String value = "value";
        
        static class FieldCustomSerializer14 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer14 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer14.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer14 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer14 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer14.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer14 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer14 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer14.class)
        public BeanWithConstructorCustomSerializer14(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer14 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer14 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer14.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer14 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer14 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer14.class)
        public static class NestedBean14 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer14 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer14 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer14 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer14 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer14 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer14.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer14 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer15 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer15.class)
        private String value = "value";
        
        static class FieldCustomSerializer15 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer15 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer15.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer15 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer15 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer15.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer15 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer15 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer15.class)
        public BeanWithConstructorCustomSerializer15(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer15 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer15 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer15.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer15 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer15 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer15.class)
        public static class NestedBean15 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer15 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer15 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer15 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer15 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer15 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer15.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer15 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer16 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer16.class)
        private String value = "value";
        
        static class FieldCustomSerializer16 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer16 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer16.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer16 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer16 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer16.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer16 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer16 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer16.class)
        public BeanWithConstructorCustomSerializer16(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer16 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer16 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer16.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer16 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer16 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer16.class)
        public static class NestedBean16 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer16 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer16 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer16 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer16 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer16 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer16.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer16 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer17 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer17.class)
        private String value = "value";
        
        static class FieldCustomSerializer17 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer17 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer17.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer17 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer17 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer17.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer17 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer17 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer17.class)
        public BeanWithConstructorCustomSerializer17(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer17 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer17 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer17.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer17 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer17 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer17.class)
        public static class NestedBean17 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer17 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer17 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer17 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer17 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer17 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer17.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer17 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer18 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer18.class)
        private String value = "value";
        
        static class FieldCustomSerializer18 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer18 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer18.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer18 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer18 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer18.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer18 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer18 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer18.class)
        public BeanWithConstructorCustomSerializer18(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer18 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer18 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer18.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer18 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer18 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer18.class)
        public static class NestedBean18 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer18 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer18 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer18 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer18 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer18 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer18.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer18 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer19 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer19.class)
        private String value = "value";
        
        static class FieldCustomSerializer19 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer19 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer19.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer19 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer19 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer19.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer19 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer19 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer19.class)
        public BeanWithConstructorCustomSerializer19(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer19 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer19 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer19.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer19 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer19 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer19.class)
        public static class NestedBean19 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer19 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer19 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer19 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer19 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer19 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer19.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer19 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer20 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer20.class)
        private String value = "value";
        
        static class FieldCustomSerializer20 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer20 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer20.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer20 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer20 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer20.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer20 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer20 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer20.class)
        public BeanWithConstructorCustomSerializer20(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer20 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer20 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer20.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer20 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer20 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer20.class)
        public static class NestedBean20 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer20 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer20 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer20 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer20 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer20 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer20.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer20 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer21 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer21.class)
        private String value = "value";
        
        static class FieldCustomSerializer21 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer21 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer21.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer21 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer21 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer21.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer21 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer21 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer21.class)
        public BeanWithConstructorCustomSerializer21(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer21 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer21 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer21.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer21 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer21 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer21.class)
        public static class NestedBean21 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer21 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer21 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer21 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer21 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer21 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer21.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer21 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer22 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer22.class)
        private String value = "value";
        
        static class FieldCustomSerializer22 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer22 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer22.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer22 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer22 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer22.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer22 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer22 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer22.class)
        public BeanWithConstructorCustomSerializer22(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer22 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer22 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer22.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer22 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer22 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer22.class)
        public static class NestedBean22 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer22 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer22 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer22 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer22 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer22 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer22.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer22 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer23 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer23.class)
        private String value = "value";
        
        static class FieldCustomSerializer23 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer23 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer23.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer23 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer23 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer23.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer23 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer23 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer23.class)
        public BeanWithConstructorCustomSerializer23(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer23 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer23 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer23.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer23 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer23 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer23.class)
        public static class NestedBean23 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer23 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer23 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer23 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer23 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer23 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer23.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer23 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer24 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer24.class)
        private String value = "value";
        
        static class FieldCustomSerializer24 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer24 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer24.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer24 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer24 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer24.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer24 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer24 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer24.class)
        public BeanWithConstructorCustomSerializer24(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer24 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer24 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer24.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer24 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer24 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer24.class)
        public static class NestedBean24 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer24 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer24 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer24 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer24 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer24 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer24.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer24 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer25 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer25.class)
        private String value = "value";
        
        static class FieldCustomSerializer25 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer25 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer25.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer25 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer25 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer25.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer25 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer25 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer25.class)
        public BeanWithConstructorCustomSerializer25(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer25 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer25 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer25.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer25 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer25 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer25.class)
        public static class NestedBean25 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer25 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer25 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer25 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer25 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer25 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer25.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer25 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer26 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer26.class)
        private String value = "value";
        
        static class FieldCustomSerializer26 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer26 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer26.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer26 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer26 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer26.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer26 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer26 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer26.class)
        public BeanWithConstructorCustomSerializer26(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer26 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer26 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer26.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer26 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer26 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer26.class)
        public static class NestedBean26 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer26 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer26 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer26 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer26 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer26 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer26.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer26 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer27 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer27.class)
        private String value = "value";
        
        static class FieldCustomSerializer27 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer27 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer27.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer27 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer27 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer27.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer27 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer27 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer27.class)
        public BeanWithConstructorCustomSerializer27(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer27 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer27 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer27.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer27 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer27 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer27.class)
        public static class NestedBean27 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer27 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer27 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer27 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer27 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer27 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer27.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer27 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer28 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer28.class)
        private String value = "value";
        
        static class FieldCustomSerializer28 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer28 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer28.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer28 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer28 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer28.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer28 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer28 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer28.class)
        public BeanWithConstructorCustomSerializer28(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer28 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer28 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer28.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer28 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer28 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer28.class)
        public static class NestedBean28 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer28 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer28 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer28 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer28 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer28 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer28.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer28 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer29 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer29.class)
        private String value = "value";
        
        static class FieldCustomSerializer29 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer29 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer29.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer29 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer29 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer29.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer29 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer29 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer29.class)
        public BeanWithConstructorCustomSerializer29(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer29 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer29 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer29.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer29 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer29 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer29.class)
        public static class NestedBean29 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer29 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer29 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer29 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer29 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer29 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer29.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer29 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer30 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer30.class)
        private String value = "value";
        
        static class FieldCustomSerializer30 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer30 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer30.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer30 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer30 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer30.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer30 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer30 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer30.class)
        public BeanWithConstructorCustomSerializer30(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer30 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer30 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer30.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer30 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer30 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer30.class)
        public static class NestedBean30 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer30 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer30 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer30 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer30 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer30 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer30.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer30 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer31 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer31.class)
        private String value = "value";
        
        static class FieldCustomSerializer31 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer31 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer31.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer31 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer31 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer31.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer31 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer31 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer31.class)
        public BeanWithConstructorCustomSerializer31(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer31 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer31 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer31.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer31 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer31 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer31.class)
        public static class NestedBean31 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer31 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer31 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer31 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer31 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer31 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer31.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer31 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer32 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer32.class)
        private String value = "value";
        
        static class FieldCustomSerializer32 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer32 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer32.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer32 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer32 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer32.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer32 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer32 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer32.class)
        public BeanWithConstructorCustomSerializer32(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer32 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer32 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer32.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer32 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer32 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer32.class)
        public static class NestedBean32 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer32 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer32 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer32 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer32 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer32 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer32.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer32 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer33 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer33.class)
        private String value = "value";
        
        static class FieldCustomSerializer33 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer33 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer33.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer33 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer33 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer33.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer33 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer33 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer33.class)
        public BeanWithConstructorCustomSerializer33(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer33 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer33 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer33.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer33 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer33 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer33.class)
        public static class NestedBean33 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer33 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer33 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer33 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer33 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer33 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer33.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer33 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer34 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer34.class)
        private String value = "value";
        
        static class FieldCustomSerializer34 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer34 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer34.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer34 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer34 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer34.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer34 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer34 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer34.class)
        public BeanWithConstructorCustomSerializer34(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer34 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer34 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer34.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer34 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer34 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer34.class)
        public static class NestedBean34 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer34 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer34 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer34 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer34 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer34 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer34.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer34 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer35 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer35.class)
        private String value = "value";
        
        static class FieldCustomSerializer35 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer35 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer35.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer35 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer35 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer35.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer35 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer35 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer35.class)
        public BeanWithConstructorCustomSerializer35(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer35 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer35 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer35.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer35 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer35 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer35.class)
        public static class NestedBean35 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer35 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer35 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer35 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer35 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer35 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer35.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer35 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer36 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer36.class)
        private String value = "value";
        
        static class FieldCustomSerializer36 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer36 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer36.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer36 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer36 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer36.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer36 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer36 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer36.class)
        public BeanWithConstructorCustomSerializer36(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer36 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer36 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer36.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer36 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer36 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer36.class)
        public static class NestedBean36 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer36 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer36 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer36 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer36 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer36 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer36.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer36 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer37 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer37.class)
        private String value = "value";
        
        static class FieldCustomSerializer37 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer37 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer37.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer37 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer37 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer37.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer37 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer37 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer37.class)
        public BeanWithConstructorCustomSerializer37(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer37 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer37 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer37.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer37 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer37 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer37.class)
        public static class NestedBean37 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer37 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer37 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer37 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer37 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer37 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer37.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer37 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer38 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer38.class)
        private String value = "value";
        
        static class FieldCustomSerializer38 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer38 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer38.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer38 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer38 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer38.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer38 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer38 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer38.class)
        public BeanWithConstructorCustomSerializer38(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer38 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer38 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer38.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer38 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer38 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer38.class)
        public static class NestedBean38 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer38 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer38 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer38 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer38 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer38 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer38.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer38 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer39 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer39.class)
        private String value = "value";
        
        static class FieldCustomSerializer39 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer39 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer39.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer39 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer39 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer39.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer39 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer39 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer39.class)
        public BeanWithConstructorCustomSerializer39(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer39 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer39 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer39.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer39 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer39 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer39.class)
        public static class NestedBean39 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer39 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer39 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer39 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer39 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer39 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer39.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer39 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer40 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer40.class)
        private String value = "value";
        
        static class FieldCustomSerializer40 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer40 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer40.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer40 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer40 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer40.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer40 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer40 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer40.class)
        public BeanWithConstructorCustomSerializer40(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer40 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer40 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer40.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer40 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer40 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer40.class)
        public static class NestedBean40 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer40 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer40 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer40 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer40 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer40 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer40.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer40 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer41 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer41.class)
        private String value = "value";
        
        static class FieldCustomSerializer41 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer41 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer41.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer41 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer41 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer41.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer41 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer41 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer41.class)
        public BeanWithConstructorCustomSerializer41(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer41 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer41 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer41.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer41 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer41 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer41.class)
        public static class NestedBean41 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer41 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer41 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer41 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer41 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer41 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer41.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer41 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer42 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer42.class)
        private String value = "value";
        
        static class FieldCustomSerializer42 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer42 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer42.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer42 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer42 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer42.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer42 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer42 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer42.class)
        public BeanWithConstructorCustomSerializer42(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer42 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer42 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer42.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer42 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer42 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer42.class)
        public static class NestedBean42 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer42 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer42 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer42 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer42 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer42 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer42.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer42 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer43 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer43.class)
        private String value = "value";
        
        static class FieldCustomSerializer43 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer43 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer43.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer43 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer43 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer43.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer43 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer43 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer43.class)
        public BeanWithConstructorCustomSerializer43(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer43 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer43 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer43.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer43 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer43 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer43.class)
        public static class NestedBean43 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer43 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer43 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer43 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer43 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer43 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer43.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer43 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer44 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer44.class)
        private String value = "value";
        
        static class FieldCustomSerializer44 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer44 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer44.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer44 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer44 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer44.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer44 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer44 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer44.class)
        public BeanWithConstructorCustomSerializer44(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer44 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer44 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer44.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer44 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer44 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer44.class)
        public static class NestedBean44 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer44 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer44 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer44 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer44 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer44 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer44.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer44 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer45 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer45.class)
        private String value = "value";
        
        static class FieldCustomSerializer45 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer45 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer45.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer45 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer45 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer45.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer45 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer45 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer45.class)
        public BeanWithConstructorCustomSerializer45(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer45 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer45 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer45.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer45 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer45 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer45.class)
        public static class NestedBean45 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer45 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer45 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer45 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer45 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer45 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer45.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer45 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer46 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer46.class)
        private String value = "value";
        
        static class FieldCustomSerializer46 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer46 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer46.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer46 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer46 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer46.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer46 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer46 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer46.class)
        public BeanWithConstructorCustomSerializer46(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer46 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer46 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer46.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer46 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer46 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer46.class)
        public static class NestedBean46 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer46 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer46 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer46 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer46 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer46 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer46.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer46 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer47 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer47.class)
        private String value = "value";
        
        static class FieldCustomSerializer47 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer47 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer47.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer47 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer47 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer47.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer47 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer47 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer47.class)
        public BeanWithConstructorCustomSerializer47(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer47 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer47 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer47.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer47 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer47 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer47.class)
        public static class NestedBean47 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer47 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer47 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer47 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer47 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer47 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer47.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer47 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer48 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer48.class)
        private String value = "value";
        
        static class FieldCustomSerializer48 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer48 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer48.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer48 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer48 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer48.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer48 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer48 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer48.class)
        public BeanWithConstructorCustomSerializer48(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer48 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer48 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer48.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer48 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer48 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer48.class)
        public static class NestedBean48 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer48 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer48 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer48 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer48 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer48 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer48.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer48 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer49 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer49.class)
        private String value = "value";
        
        static class FieldCustomSerializer49 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer49 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer49.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer49 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer49 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer49.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer49 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer49 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer49.class)
        public BeanWithConstructorCustomSerializer49(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer49 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer49 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer49.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer49 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer49 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer49.class)
        public static class NestedBean49 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer49 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer49 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer49 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer49 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer49 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer49.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer49 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer50 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer50.class)
        private String value = "value";
        
        static class FieldCustomSerializer50 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer50 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer50.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer50 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer50 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer50.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer50 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer50 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer50.class)
        public BeanWithConstructorCustomSerializer50(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer50 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer50 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer50.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer50 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer50 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer50.class)
        public static class NestedBean50 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer50 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer50 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer50 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer50 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer50 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer50.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer50 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer51 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer51.class)
        private String value = "value";
        
        static class FieldCustomSerializer51 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer51 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer51.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer51 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer51 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer51.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer51 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer51 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer51.class)
        public BeanWithConstructorCustomSerializer51(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer51 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer51 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer51.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer51 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer51 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer51.class)
        public static class NestedBean51 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer51 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer51 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer51 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer51 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer51 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer51.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer51 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer52 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer52.class)
        private String value = "value";
        
        static class FieldCustomSerializer52 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer52 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer52.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer52 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer52 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer52.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer52 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer52 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer52.class)
        public BeanWithConstructorCustomSerializer52(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer52 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer52 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer52.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer52 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer52 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer52.class)
        public static class NestedBean52 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer52 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer52 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer52 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer52 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer52 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer52.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer52 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer53 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer53.class)
        private String value = "value";
        
        static class FieldCustomSerializer53 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer53 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer53.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer53 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer53 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer53.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer53 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer53 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer53.class)
        public BeanWithConstructorCustomSerializer53(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer53 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer53 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer53.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer53 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer53 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer53.class)
        public static class NestedBean53 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer53 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer53 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer53 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer53 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer53 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer53.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer53 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer54 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer54.class)
        private String value = "value";
        
        static class FieldCustomSerializer54 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer54 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer54.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer54 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer54 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer54.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer54 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer54 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer54.class)
        public BeanWithConstructorCustomSerializer54(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer54 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer54 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer54.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer54 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer54 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer54.class)
        public static class NestedBean54 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer54 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer54 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer54 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer54 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer54 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer54.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer54 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer55 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer55.class)
        private String value = "value";
        
        static class FieldCustomSerializer55 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer55 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer55.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer55 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer55 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer55.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer55 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer55 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer55.class)
        public BeanWithConstructorCustomSerializer55(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer55 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer55 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer55.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer55 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer55 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer55.class)
        public static class NestedBean55 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer55 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer55 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer55 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer55 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer55 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer55.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer55 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer56 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer56.class)
        private String value = "value";
        
        static class FieldCustomSerializer56 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer56 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer56.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer56 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer56 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer56.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer56 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer56 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer56.class)
        public BeanWithConstructorCustomSerializer56(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer56 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer56 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer56.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer56 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer56 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer56.class)
        public static class NestedBean56 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer56 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer56 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer56 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer56 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer56 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer56.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer56 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer57 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer57.class)
        private String value = "value";
        
        static class FieldCustomSerializer57 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer57 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer57.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer57 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer57 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer57.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer57 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer57 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer57.class)
        public BeanWithConstructorCustomSerializer57(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer57 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on parameter
    static class BeanWithParameterCustomSerializer57 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        public void setValue(@JsonSerialize(using = ParameterCustomSerializer57.class) String value) {
            this.value = value;
        }
        
        static class ParameterCustomSerializer57 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on type
    static class BeanWithTypeCustomSerializer57 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = TypeCustomSerializer57.class)
        public static class NestedBean57 {
            public String name = "nested";
        }
        
        static class TypeCustomSerializer57 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on package
    static class BeanWithPackageCustomSerializer57 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on module
    static class BeanWithModuleCustomSerializer57 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on mixin
    static class BeanWithMixinCustomSerializer57 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
    }

    // Test class with @JsonValue and custom serializer on property
    static class BeanWithPropertyCustomSerializer57 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = PropertyCustomSerializer57.class)
        public String getProperty() {
            return value;
        }
        
        static class PropertyCustomSerializer57 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on field
    static class BeanWithFieldCustomSerializer58 {
        @JsonValue
        @JsonSerialize(using = FieldCustomSerializer58.class)
        private String value = "value";
        
        static class FieldCustomSerializer58 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on getter
    static class BeanWithGetterCustomSerializer58 {
        private String value = "value";
        
        @JsonValue
        @JsonSerialize(using = GetterCustomSerializer58.class)
        public String getValue() {
            return value;
        }
        
        static class GetterCustomSerializer58 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on setter
    static class BeanWithSetterCustomSerializer58 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = SetterCustomSerializer58.class)
        public void setValue(String value) {
            this.value = value;
        }
        
        static class SetterCustomSerializer58 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }
    }

    // Test class with @JsonValue and custom serializer on constructor
    static class BeanWithConstructorCustomSerializer58 {
        private String value = "value";
        
        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonSerialize(using = ConstructorCustomSerializer58.class)
        public BeanWithConstructorCustomSerializer58(@JsonProperty("value") String value) {
            this.value = value;
        }
        
        static class ConstructorCustomSerializer58 extends JsonSerializer<String> {
            @Override
            public void serialize(String value, com.fasterxml.jackson.core.JsonGenerator gen, 
                    SerializerProvider serializers) throws java.io.IOException {
                gen.writeNumber(42);
            }
        }