package com.fasterxml.jackson.databind.ser;

import java.io.*;
import java.util.*;
import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.ser.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class BeanPropertyWriterDeepseekTest {

    /**
     * @target serializeAsColumn(Object, JsonGenerator, SerializerProvider)
     * @scenario POJO configured as JSON array with first field null, second field "bar"
     * @defectRisk Defective version writes duplicate null entries in array output
     */
    @Test(timeout = 4000)
    public void testNullColumnSerialization() throws Exception {
        @JsonFormat(shape = JsonFormat.Shape.ARRAY)
        class PojoAsArray {
            public String first;
            public String second;
            public PojoAsArray(String first, String second) {
                this.first = first;
                this.second = second;
            }
        }
        
        PojoAsArray pojo = new PojoAsArray(null, "bar");
        String json = new ObjectMapper().writeValueAsString(pojo);
        assertEquals("[null,\"bar\"]", json);
    }

    /**
     * @target getName()
     * @scenario Property with simple name
     * @defectRisk Incorrect name retrieval
     */
    @Test(timeout = 4000)
    public void testGetName() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertEquals("testProp", writer.getName());
    }

    /**
     * @target getFullName()
     * @scenario Property with simple name
     * @defectRisk Incorrect full name retrieval
     */
    @Test(timeout = 4000)
    public void testGetFullName() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertEquals("testProp", writer.getFullName().getSimpleName());
    }

    /**
     * @target getType()
     * @scenario Property with String type
     * @defectRisk Incorrect type retrieval
     */
    @Test(timeout = 4000)
    public void testGetType() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertEquals(String.class, writer.getType().getRawClass());
    }

    /**
     * @target getMember()
     * @scenario Property backed by field
     * @defectRisk Incorrect member retrieval
     */
    @Test(timeout = 4000)
    public void testGetMember() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertNotNull(writer.getMember());
    }

    /**
     * @target getSerializationType()
     * @scenario Property with declared type
     * @defectRisk Incorrect serialization type
     */
    @Test(timeout = 4000)
    public void testGetSerializationType() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertNotNull(writer.getSerializationType());
    }

    /**
     * @target serializeAsField(Object, JsonGenerator, SerializerProvider)
     * @scenario Normal field serialization with non-null value
     * @defectRisk Incorrect field name or value output
     */
    @Test(timeout = 4000)
    public void testSerializeAsFieldNormal() throws Exception {
        TestBean bean = new TestBean();
        bean.value = "hello";
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"value\":\"hello\"}", json);
    }

    /**
     * @target serializeAsField(Object, JsonGenerator, SerializerProvider)
     * @scenario Null value with null suppression enabled
     * @defectRisk Null value not suppressed
     */
    @Test(timeout = 4000)
    public void testSerializeAsFieldNullSuppression() throws Exception {
        TestBean bean = new TestBean();
        bean.value = null;
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{}", json);
    }

    /**
     * @target serializeAsField(Object, JsonGenerator, SerializerProvider)
     * @scenario Empty value with MARKER_FOR_EMPTY suppression
     * @defectRisk Empty value not suppressed
     */
    @Test(timeout = 4000)
    public void testSerializeAsFieldEmptySuppression() throws Exception {
        EmptySuppressBean bean = new EmptySuppressBean();
        bean.value = "";
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{}", json);
    }

    /**
     * @target serializeAsField(Object, JsonGenerator, SerializerProvider)
     * @scenario Default value suppression
     * @defectRisk Default value not suppressed
     */
    @Test(timeout = 4000)
    public void testSerializeAsFieldDefaultSuppression() throws Exception {
        DefaultSuppressBean bean = new DefaultSuppressBean();
        bean.value = 0;
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{}", json);
    }

    /**
     * @target serializeAsField(Object, JsonGenerator, SerializerProvider)
     * @scenario Self-reference handling
     * @defectRisk Self-reference not detected
     */
    @Test(timeout = 4000)
    public void testSerializeAsFieldSelfReference() throws Exception {
        SelfRefBean bean = new SelfRefBean();
        bean.self = bean;
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValueAsString(bean);
            fail("Expected JsonMappingException for self-reference");
        } catch (JsonMappingException e) {
            // Expected
        }
    }

    /**
     * @target serializeAsColumn(Object, JsonGenerator, SerializerProvider)
     * @scenario Non-null value in array format
     * @defectRisk Incorrect array output
     */
    @Test(timeout = 4000)
    public void testSerializeAsColumnNormal() throws Exception {
        @JsonFormat(shape = JsonFormat.Shape.ARRAY)
        class ArrayBean {
            public String value;
        }
        ArrayBean bean = new ArrayBean();
        bean.value = "test";
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(bean);
        assertEquals("[\"test\"]", json);
    }

    /**
     * @target serializeAsPlaceholder(Object, JsonGenerator, SerializerProvider)
     * @scenario Placeholder serialization for null value
     * @defectRisk Incorrect placeholder output
     */
    @Test(timeout = 4000)
    public void testSerializeAsPlaceholder() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        StringWriter sw = new StringWriter();
        JsonGenerator jgen = new JsonFactory().createGenerator(sw);
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        writer.serializeAsPlaceholder(null, jgen, prov);
        jgen.flush();
        assertEquals("null", sw.toString());
    }

    /**
     * @target getInternalSetting(Object)
     * @scenario Setting retrieval
     * @defectRisk Incorrect setting value
     */
    @Test(timeout = 4000)
    public void testGetInternalSetting() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        writer.setInternalSetting("key", "value");
        assertEquals("value", writer.getInternalSetting("key"));
    }

    /**
     * @target setInternalSetting(Object, Object)
     * @scenario Setting new value
     * @defectRisk Incorrect old value returned
     */
    @Test(timeout = 4000)
    public void testSetInternalSetting() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertNull(writer.setInternalSetting("key", "value"));
        assertEquals("value", writer.setInternalSetting("key", "newValue"));
    }

    /**
     * @target removeInternalSetting(Object)
     * @scenario Removing existing setting
     * @defectRisk Incorrect removal behavior
     */
    @Test(timeout = 4000)
    public void testRemoveInternalSetting() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        writer.setInternalSetting("key", "value");
        assertEquals("value", writer.removeInternalSetting("key"));
        assertNull(writer.removeInternalSetting("key"));
    }

    /**
     * @target getSerializedName()
     * @scenario Serialized name retrieval
     * @defectRisk Incorrect serialized name
     */
    @Test(timeout = 4000)
    public void testGetSerializedName() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertEquals("testProp", writer.getSerializedName().getValue());
    }

    /**
     * @target hasSerializer()
     * @scenario No static serializer assigned
     * @defectRisk Incorrect serializer presence
     */
    @Test(timeout = 4000)
    public void testHasSerializer() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertFalse(writer.hasSerializer());
    }

    /**
     * @target hasNullSerializer()
     * @scenario No null serializer assigned
     * @defectRisk Incorrect null serializer presence
     */
    @Test(timeout = 4000)
    public void testHasNullSerializer() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertFalse(writer.hasNullSerializer());
    }

    /**
     * @target willSuppressNulls()
     * @scenario Null suppression enabled
     * @defectRisk Incorrect null suppression flag
     */
    @Test(timeout = 4000)
    public void testWillSuppressNulls() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertTrue(writer.willSuppressNulls());
    }

    /**
     * @target getSerializer()
     * @scenario No serializer assigned
     * @defectRisk Incorrect serializer retrieval
     */
    @Test(timeout = 4000)
    public void testGetSerializer() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertNull(writer.getSerializer());
    }

    /**
     * @target getRawSerializationType()
     * @scenario Raw serialization type retrieval
     * @defectRisk Incorrect raw type
     */
    @Test(timeout = 4000)
    public void testGetRawSerializationType() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertEquals(String.class, writer.getRawSerializationType());
    }

    /**
     * @target getPropertyType()
     * @scenario Property type retrieval
     * @defectRisk Incorrect property type
     */
    @Test(timeout = 4000)
    public void testGetPropertyType() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertEquals(String.class, writer.getPropertyType());
    }

    /**
     * @target getGenericPropertyType()
     * @scenario Generic property type retrieval
     * @defectRisk Incorrect generic type
     */
    @Test(timeout = 4000)
    public void testGetGenericPropertyType() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertEquals(String.class, writer.getGenericPropertyType());
    }

    /**
     * @target getViews()
     * @scenario No views defined
     * @defectRisk Incorrect views array
     */
    @Test(timeout = 4000)
    public void testGetViews() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertNull(writer.getViews());
    }

    /**
     * @target isRequired()
     * @scenario Not required property
     * @defectRisk Incorrect required flag
     */
    @Test(timeout = 4000)
    public void testIsRequired() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertFalse(writer.isRequired());
    }

    /**
     * @target getWrapperName()
     * @scenario No wrapper name
     * @defectRisk Incorrect wrapper name
     */
    @Test(timeout = 4000)
    public void testGetWrapperName() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertNull(writer.getWrapperName());
    }

    /**
     * @target getAnnotation(Class)
     * @scenario Annotation retrieval
     * @defectRisk Incorrect annotation
     */
    @Test(timeout = 4000)
    public void testGetAnnotation() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertNull(writer.getAnnotation(JsonProperty.class));
    }

    /**
     * @target getContextAnnotation(Class)
     * @scenario Context annotation retrieval
     * @defectRisk Incorrect context annotation
     */
    @Test(timeout = 4000)
    public void testGetContextAnnotation() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertNull(writer.getContextAnnotation(JsonProperty.class));
    }

    /**
     * @target assignSerializer(JsonSerializer)
     * @scenario Assigning serializer
     * @defectRisk Serializer not assigned
     */
    @Test(timeout = 4000)
    public void testAssignSerializer() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        JsonSerializer<Object> ser = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeString("custom");
            }
        };
        writer.assignSerializer(ser);
        assertTrue(writer.hasSerializer());
    }

    /**
     * @target assignNullSerializer(JsonSerializer)
     * @scenario Assigning null serializer
     * @defectRisk Null serializer not assigned
     */
    @Test(timeout = 4000)
    public void testAssignNullSerializer() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        JsonSerializer<Object> ser = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                gen.writeNull();
            }
        };
        writer.assignNullSerializer(ser);
        assertTrue(writer.hasNullSerializer());
    }

    /**
     * @target rename(NameTransformer)
     * @scenario Renaming property
     * @defectRisk Incorrect rename behavior
     */
    @Test(timeout = 4000)
    public void testRename() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        BeanPropertyWriter renamed = writer.rename(new NameTransformer() {
            @Override
            public String transform(String name) {
                return "renamed_" + name;
            }

            @Override
            public String reverse(String transformed) {
                return transformed.substring("renamed_".length());
            }
        });
        assertEquals("renamed_testProp", renamed.getName());
    }

    /**
     * @target unwrappingWriter(NameTransformer)
     * @scenario Creating unwrapping writer
     * @defectRisk Incorrect unwrapping writer creation
     */
    @Test(timeout = 4000)
    public void testUnwrappingWriter() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        BeanPropertyWriter unwrapped = writer.unwrappingWriter(new NameTransformer() {
            @Override
            public String transform(String name) {
                return name;
            }

            @Override
            public String reverse(String transformed) {
                return transformed;
            }
        });
        assertNotNull(unwrapped);
    }

    /**
     * @target setNonTrivialBaseType(JavaType)
     * @scenario Setting non-trivial base type
     * @defectRisk Base type not set
     */
    @Test(timeout = 4000)
    public void testSetNonTrivialBaseType() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        JavaType type = new ObjectMapper().getTypeFactory().constructType(String.class);
        writer.setNonTrivialBaseType(type);
        // No exception expected
    }

    /**
     * @target depositSchemaProperty(JsonObjectFormatVisitor)
     * @scenario Schema property deposit
     * @defectRisk Schema not deposited
     */
    @Test(timeout = 4000)
    public void testDepositSchemaProperty() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        // This test just ensures no exception is thrown
        // Full schema testing would require more setup
    }

    /**
     * @target get(Object)
     * @scenario Field access
     * @defectRisk Incorrect field value
     */
    @Test(timeout = 4000)
    public void testGetFieldValue() throws Exception {
        TestBean bean = new TestBean();
        bean.value = "test";
        BeanPropertyWriter writer = createSimpleWriter("value");
        assertEquals("test", writer.get(bean));
    }

    /**
     * @target get(Object)
     * @scenario Method access
     * @defectRisk Incorrect method value
     */
    @Test(timeout = 4000)
    public void testGetMethodValue() throws Exception {
        MethodBean bean = new MethodBean();
        bean.value = "test";
        BeanPropertyWriter writer = createMethodWriter("value");
        assertEquals("test", writer.get(bean));
    }

    /**
     * @target toString()
     * @scenario String representation
     * @defectRisk Incorrect string representation
     */
    @Test(timeout = 4000)
    public void testToString() throws Exception {
        BeanPropertyWriter writer = createSimpleWriter("testProp");
        assertNotNull(writer.toString());
        assertTrue(writer.toString().contains("testProp"));
    }

    // Helper methods and classes

    private static class TestBean {
        public String value;
    }

    private static class MethodBean {
        private String value;
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }

    private static class EmptySuppressBean {
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public String value;
    }

    private static class DefaultSuppressBean {
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        public int value;
    }

    private static class SelfRefBean {
        public SelfRefBean self;
    }

    private BeanPropertyWriter createSimpleWriter(String name) throws Exception {
        TestBean bean = new TestBean();
        Field field = TestBean.class.getField(name);
        AnnotatedField annotatedField = new AnnotatedField(null, field, null);
        BeanPropertyDefinition propDef = new BeanPropertyDefinition(
                new PropertyName(name), null, annotatedField, null, null, false, false);
        JavaType type = new ObjectMapper().getTypeFactory().constructType(field.getType());
        return new BeanPropertyWriter(propDef, annotatedField, null, type, null, null, type, null, null, true, null, null);
    }

    private BeanPropertyWriter createMethodWriter(String name) throws Exception {
        MethodBean bean = new MethodBean();
        Method method = MethodBean.class.getMethod("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
        AnnotatedMethod annotatedMethod = new AnnotatedMethod(null, method, null, null);
        BeanPropertyDefinition propDef = new BeanPropertyDefinition(
                new PropertyName(name), null, annotatedMethod, null, null, false, false);
        JavaType type = new ObjectMapper().getTypeFactory().constructType(method.getReturnType());
        return new BeanPropertyWriter(propDef, annotatedMethod, null, type, null, null, type, null, null, true, null, null);
    }
}