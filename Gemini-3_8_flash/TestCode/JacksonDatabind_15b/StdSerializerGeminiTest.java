/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.ser.std.StdSerializer
 * 
 * Targeted Decision Branches & Boundaries:
 * 1. Constructors:
 *    - StdSerializer(Class<T>)
 *    - StdSerializer(JavaType) (verifying type.getRawClass())
 *    - StdSerializer(Class<?>, boolean) (dummy overload)
 * 2. Schema Generation:
 *    - getSchema(SerializerProvider, Type): returns ObjectNode with "type" = "string"
 *    - getSchema(SerializerProvider, Type, boolean isOptional):
 *      * isOptional == false -> schema has "required": true
 *      * isOptional == true -> schema does NOT have "required" property
 *    - createSchemaNode(String)
 *    - createSchemaNode(String, boolean isOptional):
 *      * isOptional == false -> schema has "required": true
 *      * isOptional == true -> schema does NOT have "required" property
 * 3. Visitor:
 *    - acceptJsonFormatVisitor: calls visitor.expectAnyFormat(typeHint)
 * 4. Exception Handling (wrapAndThrow for String and int index):
 *    - InvocationTargetException unwrapping chain until non-ITE or cause == null
 *    - Error instance: rethrown directly as Error
 *    - provider == null (wrap == true):
 *      * IOException (plain): rethrown directly without wrapping
 *      * JsonMappingException: wrapped with path
 *      * Checked exception (e.g. Exception): wrapped into JsonMappingException with path
 *    - provider != null, WRAP_EXCEPTIONS disabled:
 *      * IOException: rethrown directly
 *      * RuntimeException: rethrown directly without wrapping
 *      * Checked Exception: wrapped into JsonMappingException
 * 5. Helpers:
 *    - isDefaultSerializer: annotated with @JacksonStdImpl vs custom class
 *    - findPropertyFilter:
 *      * FilterProvider is null -> throws JsonMappingException
 *      * FilterProvider exists -> queries findPropertyFilter(filterId, valueToFilter)
 * 6. Defect-Targeted Zone (databind#731 / testIssue731):
 *    - findConvertingContentSerializer with a content converter producing java.lang.Object (delegateType == Object.class)
 *    - Ground truth defect: findConvertingContentSerializer fails to skip calling findValueSerializer
 *      when delegateType is java.lang.Object, triggering FAIL_ON_EMPTY_BEANS JsonMappingException.
 */

package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;

public class StdSerializerGeminiTest {

    // Concrete test implementation of StdSerializer
    private static class ConcreteStdSerializer<T> extends StdSerializer<T> {
        private static final long serialVersionUID = 1L;

        public ConcreteStdSerializer(Class<T> t) {
            super(t);
        }

        public ConcreteStdSerializer(JavaType type) {
            super(type);
        }

        public ConcreteStdSerializer(Class<?> t, boolean dummy) {
            super(t, dummy);
        }

        @Override
        public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(String.valueOf(value));
        }

        // Public exposures of protected helper methods
        @Override
        public ObjectNode createObjectNode() {
            return super.createObjectNode();
        }

        @Override
        public ObjectNode createSchemaNode(String type) {
            return super.createSchemaNode(type);
        }

        @Override
        public ObjectNode createSchemaNode(String type, boolean isOptional) {
            return super.createSchemaNode(type, isOptional);
        }

        @Override
        public boolean isDefaultSerializer(JsonSerializer<?> serializer) {
            return super.isDefaultSerializer(serializer);
        }

        @Override
        public JsonSerializer<?> findConvertingContentSerializer(SerializerProvider provider,
                BeanProperty prop, JsonSerializer<?> existingSerializer) throws JsonMappingException {
            return super.findConvertingContentSerializer(provider, prop, existingSerializer);
        }

        @Override
        public PropertyFilter findPropertyFilter(SerializerProvider provider,
                Object filterId, Object valueToFilter) throws JsonMappingException {
            return super.findPropertyFilter(provider, filterId, valueToFilter);
        }
    }

    @JacksonStdImpl
    private static class StdAnnotatedSerializer extends StdSerializer<String> {
        private static final long serialVersionUID = 1L;
        public StdAnnotatedSerializer() {
            super(String.class);
        }
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider provider) {}
    }

    /*
     * -------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * -------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testConstructorsAndHandledType() {
        ConcreteStdSerializer<String> ser1 = new ConcreteStdSerializer<>(String.class);
        assertEquals(String.class, ser1.handledType());

        JavaType intType = TypeFactory.defaultInstance().constructType(Integer.class);
        ConcreteStdSerializer<Integer> ser2 = new ConcreteStdSerializer<>(intType);
        assertEquals(Integer.class, ser2.handledType());

        ConcreteStdSerializer<Double> ser3 = new ConcreteStdSerializer<>(Double.class, true);
        assertEquals(Double.class, ser3.handledType());
    }

    @Test(timeout = 4000)
    public void testSchemaGenerationBasic() throws Exception {
        ConcreteStdSerializer<String> ser = new ConcreteStdSerializer<>(String.class);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        // 2-arg getSchema defaults to "string"
        JsonNode node = ser.getSchema(provider, String.class);
        assertNotNull(node);
        assertTrue(node.isObject());
        assertEquals("string", node.get("type").asText());
        assertNull(node.get("required"));

        // 3-arg getSchema with isOptional == false
        JsonNode requiredNode = ser.getSchema(provider, String.class, false);
        assertEquals("string", requiredNode.get("type").asText());
        assertNotNull(requiredNode.get("required"));
        assertTrue(requiredNode.get("required").asBoolean());

        // 3-arg getSchema with isOptional == true
        JsonNode optionalNode = ser.getSchema(provider, String.class, true);
        assertEquals("string", optionalNode.get("type").asText());
        assertNull(optionalNode.get("required"));
    }

    @Test(timeout = 4000)
    public void testCreateSchemaNodeVariants() {
        ConcreteStdSerializer<String> ser = new ConcreteStdSerializer<>(String.class);

        ObjectNode node1 = ser.createSchemaNode("integer");
        assertEquals("integer", node1.get("type").asText());
        assertNull(node1.get("required"));

        ObjectNode node2 = ser.createSchemaNode("boolean", false);
        assertEquals("boolean", node2.get("type").asText());
        assertNotNull(node2.get("required"));
        assertTrue(node2.get("required").asBoolean());

        ObjectNode node3 = ser.createSchemaNode("number", true);
        assertEquals("number", node3.get("type").asText());
        assertNull(node3.get("required"));
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorCallsExpectAnyFormat() throws Exception {
        ConcreteStdSerializer<String> ser = new ConcreteStdSerializer<>(String.class);
        final boolean[] visited = new boolean[1];

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor expectAnyFormat(JavaType type) {
                visited[0] = true;
                return null;
            }
        };

        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        ser.acceptJsonFormatVisitor(visitor, stringType);
        assertTrue("acceptJsonFormatVisitor should invoke expectAnyFormat", visited[0]);
    }

    @Test(timeout = 4000)
    public void testIsDefaultSerializer() {
        ConcreteStdSerializer<String> customSer = new ConcreteStdSerializer<>(String.class);
        StdAnnotatedSerializer stdSer = new StdAnnotatedSerializer();

        assertFalse(customSer.isDefaultSerializer(customSer));
        assertTrue(customSer.isDefaultSerializer(stdSer));
        assertFalse(customSer.isDefaultSerializer(null));
    }

    /*
     * -------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * -------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testWrapAndThrowWithInvocationTargetExceptionChain() {
        ConcreteStdSerializer<String> ser = new ConcreteStdSerializer<>(String.class);

        NullPointerException root = new NullPointerException("deep null");
        InvocationTargetException ite1 = new InvocationTargetException(root);
        InvocationTargetException ite2 = new InvocationTargetException(ite1);

        try {
            ser.wrapAndThrow(null, ite2, "targetBean", "fieldA");
            fail("Should have thrown JsonMappingException");
        } catch (IOException e) {
            assertTrue(e instanceof JsonMappingException);
            JsonMappingException jme = (JsonMappingException) e;
            assertSame(root, jme.getCause());
            assertTrue(jme.getPath().size() > 0);
            assertEquals("fieldA", jme.getPath().get(0).getFieldName());
        }

        try {
            ser.wrapAndThrow(null, ite2, "targetBean", 5);
            fail("Should have thrown JsonMappingException");
        } catch (IOException e) {
            assertTrue(e instanceof JsonMappingException);
            JsonMappingException jme = (JsonMappingException) e;
            assertSame(root, jme.getCause());
            assertTrue(jme.getPath().size() > 0);
            assertEquals(5, jme.getPath().get(0).getIndex());
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrowErrorDirectRethrow() throws IOException {
        ConcreteStdSerializer<String> ser = new ConcreteStdSerializer<>(String.class);
        OutOfMemoryError error = new OutOfMemoryError("OOM test");

        try {
            ser.wrapAndThrow(null, error, "bean", "field");
            fail("Should have rethrown OutOfMemoryError");
        } catch (Error e) {
            assertSame(error, e);
        }

        try {
            ser.wrapAndThrow(null, error, "bean", 0);
            fail("Should have rethrown OutOfMemoryError");
        } catch (Error e) {
            assertSame(error, e);
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrowPlainIOExceptionRethrownDirectly() {
        ConcreteStdSerializer<String> ser = new ConcreteStdSerializer<>(String.class);
        IOException plainIOE = new IOException("plain disk error");

        try {
            ser.wrapAndThrow(null, plainIOE, "bean", "field");
            fail("Should have rethrown IOException directly");
        } catch (IOException e) {
            assertSame(plainIOE, e);
            assertFalse(e instanceof JsonMappingException);
        }

        try {
            ser.wrapAndThrow(null, plainIOE, "bean", 2);
            fail("Should have rethrown IOException directly");
        } catch (IOException e) {
            assertSame(plainIOE, e);
            assertFalse(e instanceof JsonMappingException);
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrowWrappingDisabled() {
        ConcreteStdSerializer<String> ser = new ConcreteStdSerializer<>(String.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRAP_EXCEPTIONS);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        // 1. RuntimeException should be rethrown as-is when WRAP_EXCEPTIONS is false
        IllegalArgumentException iae = new IllegalArgumentException("arg error");
        try {
            ser.wrapAndThrow(provider, iae, "bean", "field");
            fail("Should have thrown IllegalArgumentException");
        } catch (Exception e) {
            assertSame(iae, e);
        }

        try {
            ser.wrapAndThrow(provider, iae, "bean", 1);
            fail("Should have thrown IllegalArgumentException");
        } catch (Exception e) {
            assertSame(iae, e);
        }

        // 2. IOException should also be rethrown as-is
        IOException ioe = new IOException("io error");
        try {
            ser.wrapAndThrow(provider, ioe, "bean", "field");
            fail("Should have thrown IOException");
        } catch (IOException e) {
            assertSame(ioe, e);
        }

        try {
            ser.wrapAndThrow(provider, ioe, "bean", 1);
            fail("Should have thrown IOException");
        } catch (IOException e) {
            assertSame(ioe, e);
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrowWrappingEnabledWithCheckedException() {
        ConcreteStdSerializer<String> ser = new ConcreteStdSerializer<>(String.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRAP_EXCEPTIONS);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        Exception checked = new Exception("checked failure");
        try {
            ser.wrapAndThrow(provider, checked, "bean", "prop");
            fail("Should wrap into JsonMappingException");
        } catch (IOException e) {
            assertTrue(e instanceof JsonMappingException);
            assertSame(checked, e.getCause());
        }
    }

    /*
     * -------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone (databind#731)
     * -------------------------------------------------------------------
     */

    public static class Issue731Bean {
        @JsonSerialize(contentConverter = ObjectReturningConverter.class)
        public List<String> values = Arrays.asList("hello");
    }

    public static class ObjectReturningConverter extends StdConverter<String, Object> {
        @Override
        public Object convert(String value) {
            return value;
        }
    }

    /**
     * Targets Jackson Issue #731:
     * When a content converter's delegateType is java.lang.Object (nominally Object),
     * StdSerializer.findConvertingContentSerializer must skip eagerly finding a value serializer
     * (since Object.class has no properties and would fail with FAIL_ON_EMPTY_BEANS).
     * If the defect is present, serializing Issue731Bean fails with:
     * "No serializer found for class java.lang.Object and no properties discovered to create BeanSerializer"
     */
    @Test(timeout = 4000)
    public void testDefectIssue731ConvertingContentSerializerToObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        assertTrue("FAIL_ON_EMPTY_BEANS must be enabled to detect Issue #731",
                mapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS));

        Issue731Bean bean = new Issue731Bean();
        String json = mapper.writeValueAsString(bean);
        assertEquals("{\"values\":[\"hello\"]}", json);
    }

    /*
     * -------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * -------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testFindPropertyFilterMissingFilterProviderThrows() {
        ConcreteStdSerializer<String> ser = new ConcreteStdSerializer<>(String.class);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        try {
            ser.findPropertyFilter(provider, "testFilter", "value");
            fail("Expected JsonMappingException due to missing FilterProvider");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Can not resolve PropertyFilter with id 'testFilter'"));
        }
    }

    @Test(timeout = 4000)
    public void testFindPropertyFilterWithConfiguredProvider() throws Exception {
        ConcreteStdSerializer<String> ser = new ConcreteStdSerializer<>(String.class);
        ObjectMapper mapper = new ObjectMapper();

        final PropertyFilter dummyFilter = new PropertyFilter() {
            @Override
            public void serializeAsField(Object pojo, JsonGenerator jgen, SerializerProvider prov, PropertyWriter writer) {}
            @Override
            public void serializeAsElement(Object elementValue, JsonGenerator jgen, SerializerProvider prov, PropertyWriter writer) {}
            @Override
            public void depositSchemaProperty(PropertyWriter writer, ObjectNode propertiesNode, SerializerProvider provider) {}
            @Override
            public void depositSchemaProperty(PropertyWriter writer, com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor objectVisitor, SerializerProvider provider) {}
        };

        FilterProvider filterProvider = new FilterProvider() {
            @Override
            public PropertyFilter findPropertyFilter(Object filterId, Object valueToFilter) {
                if ("knownFilter".equals(filterId)) {
                    return dummyFilter;
                }
                return null;
            }
        };

        mapper.setFilters(filterProvider);
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        PropertyFilter filter = ser.findPropertyFilter(provider, "knownFilter", "anyValue");
        assertSame(dummyFilter, filter);

        PropertyFilter unknownFilter = ser.findPropertyFilter(provider, "unknownFilter", "anyValue");
        assertNull(unknownFilter);
    }

    @Test(timeout = 4000)
    public void testFindConvertingContentSerializerNoAnnotationReturnsExisting() throws Exception {
        ConcreteStdSerializer<String> ser = new ConcreteStdSerializer<>(String.class);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        // When prop is null, findConvertingContentSerializer should return existing serializer directly
        JsonSerializer<?> existing = new ConcreteStdSerializer<>(Integer.class);
        JsonSerializer<?> result = ser.findConvertingContentSerializer(provider, null, existing);
        assertSame(existing, result);
    }

    /*
     * -------------------------------------------------------------------
     * Partition E: Object Lifecycle & Contract Integrity
     * -------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testHandledTypeWithNullClass() {
        ConcreteStdSerializer<Void> ser = new ConcreteStdSerializer<>((Class<Void>) null);
        assertNull(ser.handledType());
    }

    @Test(timeout = 4000)
    public void testCreateObjectNodeProducesEmptyObjectNode() {
        ConcreteStdSerializer<String> ser = new ConcreteStdSerializer<>(String.class);
        ObjectNode node = ser.createObjectNode();
        assertNotNull(node);
        assertEquals(0, node.size());
    }
}