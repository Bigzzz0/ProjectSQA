package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospector;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.Converter;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: StdSerializer (abstract) – all non-abstract methods.
 * 
 * Branches covered:
 * - wrapAndThrow(SerializerProvider, Throwable, Object, String):
 *   - while loop (InvocationTargetException unwrapping)
 *   - if (t instanceof Error)
 *   - boolean wrap = (provider==null || provider.isEnabled(WRAP_EXCEPTIONS))
 *   - if (t instanceof IOException) { if (!wrap || !(t instanceof JsonMappingException)) }
 *   - else if (!wrap) { if (t instanceof RuntimeException) }
 *   - throw JsonMappingException.wrapWithPath(...)
 * - wrapAndThrow(SerializerProvider, Throwable, Object, int): same branches
 * - isDefaultSerializer: delegates to ClassUtil.isJacksonStdImpl
 * - findConvertingContentSerializer:
 *   - if (intr != null && prop != null)
 *   - if (m != null)
 *   - Object convDef = intr.findSerializationContentConverter(m)
 *   - if (convDef != null)
 *   - Converter<Object,Object> conv = provider.converterInstance(...)
 *   - JavaType delegateType = conv.getOutputType(...)
 *   - if (existingSerializer == null) -> findValueSerializer
 *   - return new StdDelegatingSerializer(...)
 *   - return existingSerializer (fallback)
 * - findPropertyFilter:
 *   - if (filters == null) throw JsonMappingException
 *   - return filters.findPropertyFilter(...)
 * - getSchema(SerializerProvider, Type) -> createSchemaNode("string")
 * - getSchema(SerializerProvider, Type, boolean) -> cast, put "required"
 * - createObjectNode -> JsonNodeFactory.instance.objectNode()
 * - createSchemaNode(String) -> put "type"
 * - createSchemaNode(String, boolean) -> put "required" if !isOptional
 * - acceptJsonFormatVisitor -> visitor.expectAnyFormat(typeHint)
 * 
 * Defect target (Issue #731 / #357):
 *   findConvertingContentSerializer must not cause infinite loop when converter
 *   returns the same type as input. Test ensures no exception and returns
 *   StdDelegatingSerializer.
 */
public class StdSerializerDeepseekTest {

    // Concrete subclass for testing
    private static class TestStdSerializer extends StdSerializer<String> {
        private static final long serialVersionUID = 1L;

        protected TestStdSerializer() {
            super(String.class);
        }

        @Override
        public void serialize(String value, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonGenerationException {
            // no-op for testing
        }
    }

    private final TestStdSerializer serializer = new TestStdSerializer();

    // ----------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testHandledType() {
        assertEquals(String.class, serializer.handledType());
    }

    @Test(timeout = 4000)
    public void testGetSchemaDefault() throws JsonMappingException {
        JsonNode schema = serializer.getSchema(null, (Type) null);
        assertTrue(schema instanceof ObjectNode);
        assertEquals("string", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testGetSchemaWithOptional() throws JsonMappingException {
        // isOptional = true -> no "required" field
        JsonNode schema = serializer.getSchema(null, (Type) null, true);
        assertTrue(schema instanceof ObjectNode);
        assertEquals("string", schema.get("type").asText());
        assertNull(schema.get("required"));

        // isOptional = false -> "required" = true
        schema = serializer.getSchema(null, (Type) null, false);
        assertTrue(schema instanceof ObjectNode);
        assertEquals("string", schema.get("type").asText());
        assertTrue(schema.get("required").asBoolean());
    }

    @Test(timeout = 4000)
    public void testCreateObjectNode() {
        ObjectNode node = serializer.createObjectNode();
        assertNotNull(node);
        assertTrue(node.isObject());
    }

    @Test(timeout = 4000)
    public void testCreateSchemaNode() {
        ObjectNode node = serializer.createSchemaNode("integer");
        assertEquals("integer", node.get("type").asText());
        assertNull(node.get("required"));
    }

    @Test(timeout = 4000)
    public void testCreateSchemaNodeWithOptional() {
        // isOptional = true -> no "required"
        ObjectNode node = serializer.createSchemaNode("integer", true);
        assertEquals("integer", node.get("type").asText());
        assertNull(node.get("required"));

        // isOptional = false -> "required" = true
        node = serializer.createSchemaNode("integer", false);
        assertEquals("integer", node.get("type").asText());
        assertTrue(node.get("required").asBoolean());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitor() throws JsonMappingException {
        // Use a simple visitor that records call
        final boolean[] visited = {false};
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonAnyFormatVisitor expectAnyFormat(JavaType type) throws JsonMappingException {
                visited[0] = true;
                return null;
            }
        };
        serializer.acceptJsonFormatVisitor(visitor, null);
        assertTrue(visited[0]);
    }

    // ----------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testIsDefaultSerializer() {
        // Test with a serializer annotated with @JacksonStdImpl
        JsonSerializer<?> stdImpl = new StdSerializer<String>(String.class) {
            private static final long serialVersionUID = 1L;
            @Override
            public void serialize(String value, JsonGenerator gen, SerializerProvider prov) {}
        };
        // Not annotated, so false
        assertFalse(serializer.isDefaultSerializer(stdImpl));

        // Annotated class
        @JacksonStdImpl
        class AnnotatedSerializer extends JsonSerializer<String> {
            @Override
            public void serialize(String value, JsonGenerator gen, SerializerProvider prov) {}
        }
        assertTrue(serializer.isDefaultSerializer(new AnnotatedSerializer()));
    }

    // ----------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Issue #731)
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testFindConvertingContentSerializer_DefectTarget() throws JsonMappingException {
        // Simulate a converter that returns the same type (potential infinite loop)
        Converter<Object, Object> identityConverter = new Converter<Object, Object>() {
            @Override
            public Object convert(Object value) {
                return value;
            }

            @Override
            public JavaType getOutputType(TypeFactory typeFactory) {
                // Return a type that is the same as input (String)
                return typeFactory.constructType(String.class);
            }
        };

        // Create a minimal SerializerProvider stub
        SerializerProvider provider = new SerializerProvider(null, null, null) {
            private static final long serialVersionUID = 1L;

            @Override
            public AnnotationIntrospector getAnnotationIntrospector() {
                return new AnnotationIntrospector() {
                    @Override
                    public Object findSerializationContentConverter(AnnotatedMember member) {
                        return "converterId"; // non-null to trigger converter path
                    }
                    // other methods not needed
                };
            }

            @Override
            public Converter<Object, Object> converterInstance(AnnotatedMember member, Object converterDef) {
                return identityConverter;
            }

            @Override
            public JavaType getTypeFactory() {
                return TypeFactory.defaultInstance();
            }

            @Override
            public JsonSerializer<Object> findValueSerializer(JavaType type) {
                // Return a dummy serializer
                return new StdSerializer<Object>(Object.class) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public void serialize(Object value, JsonGenerator gen, SerializerProvider prov) {}
                };
            }
        };

        // Create a minimal BeanProperty stub
        BeanProperty prop = new BeanProperty.Std(null, null, null, null, null, false, null);

        // This call should not throw or hang; it should return a StdDelegatingSerializer
        JsonSerializer<?> result = serializer.findConvertingContentSerializer(provider, prop, null);
        assertNotNull(result);
        assertTrue("Expected StdDelegatingSerializer", result instanceof StdDelegatingSerializer);
    }

    // ----------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testWrapAndThrow_InvocationTargetUnwrapping() throws IOException {
        // Create a chain: InvocationTargetException -> RuntimeException
        RuntimeException cause = new RuntimeException("inner");
        InvocationTargetException ite = new InvocationTargetException(cause);
        try {
            serializer.wrapAndThrow(null, ite, new Object(), "field");
            fail("Expected IOException");
        } catch (IOException e) {
            // Should be a JsonMappingException wrapping the RuntimeException
            assertTrue(e instanceof JsonMappingException);
            assertTrue(e.getCause() instanceof RuntimeException);
            assertEquals("inner", e.getCause().getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrow_Error() throws IOException {
        Error error = new StackOverflowError("test");
        try {
            serializer.wrapAndThrow(null, error, new Object(), "field");
            fail("Expected Error");
        } catch (Error e) {
            assertEquals("test", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrow_IOException_NoWrap() throws IOException {
        // provider with WRAP_EXCEPTIONS disabled
        SerializerProvider provider = new SerializerProvider(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isEnabled(SerializationFeature feature) {
                return false; // disable wrapping
            }
        };
        IOException ioException = new IOException("plain");
        try {
            serializer.wrapAndThrow(provider, ioException, new Object(), "field");
            fail("Expected IOException");
        } catch (IOException e) {
            assertEquals("plain", e.getMessage());
            assertFalse(e instanceof JsonMappingException);
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrow_IOException_Wrap() throws IOException {
        // provider with WRAP_EXCEPTIONS enabled (default)
        SerializerProvider provider = new SerializerProvider(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isEnabled(SerializationFeature feature) {
                return true;
            }
        };
        IOException ioException = new IOException("plain");
        try {
            serializer.wrapAndThrow(provider, ioException, new Object(), "field");
            fail("Expected IOException");
        } catch (IOException e) {
            // Should be wrapped into JsonMappingException
            assertTrue(e instanceof JsonMappingException);
            assertEquals("plain", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrow_RuntimeException_NoWrap() throws IOException {
        SerializerProvider provider = new SerializerProvider(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isEnabled(SerializationFeature feature) {
                return false;
            }
        };
        RuntimeException re = new RuntimeException("unchecked");
        try {
            serializer.wrapAndThrow(provider, re, new Object(), "field");
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            assertEquals("unchecked", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrow_RuntimeException_Wrap() throws IOException {
        SerializerProvider provider = new SerializerProvider(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isEnabled(SerializationFeature feature) {
                return true;
            }
        };
        RuntimeException re = new RuntimeException("unchecked");
        try {
            serializer.wrapAndThrow(provider, re, new Object(), "field");
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e instanceof JsonMappingException);
            assertTrue(e.getCause() instanceof RuntimeException);
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrow_OtherException() throws IOException {
        // Throwable that is not Error, IOException, RuntimeException
        Throwable other = new Exception("other");
        try {
            serializer.wrapAndThrow(null, other, new Object(), "field");
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e instanceof JsonMappingException);
            assertTrue(e.getCause() instanceof Exception);
        }
    }

    // Index version of wrapAndThrow
    @Test(timeout = 4000)
    public void testWrapAndThrow_WithIndex_InvocationTarget() throws IOException {
        InvocationTargetException ite = new InvocationTargetException(new RuntimeException("inner"));
        try {
            serializer.wrapAndThrow(null, ite, new Object(), 0);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e instanceof JsonMappingException);
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrow_WithIndex_Error() throws IOException {
        Error error = new StackOverflowError();
        try {
            serializer.wrapAndThrow(null, error, new Object(), 0);
            fail("Expected Error");
        } catch (Error e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrow_WithIndex_IOException_NoWrap() throws IOException {
        SerializerProvider provider = new SerializerProvider(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isEnabled(SerializationFeature feature) {
                return false;
            }
        };
        IOException io = new IOException("plain");
        try {
            serializer.wrapAndThrow(provider, io, new Object(), 0);
            fail("Expected IOException");
        } catch (IOException e) {
            assertFalse(e instanceof JsonMappingException);
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrow_WithIndex_IOException_Wrap() throws IOException {
        SerializerProvider provider = new SerializerProvider(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isEnabled(SerializationFeature feature) {
                return true;
            }
        };
        IOException io = new IOException("plain");
        try {
            serializer.wrapAndThrow(provider, io, new Object(), 0);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e instanceof JsonMappingException);
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrow_WithIndex_RuntimeException_NoWrap() throws IOException {
        SerializerProvider provider = new SerializerProvider(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isEnabled(SerializationFeature feature) {
                return false;
            }
        };
        RuntimeException re = new RuntimeException("unchecked");
        try {
            serializer.wrapAndThrow(provider, re, new Object(), 0);
            fail("Expected RuntimeException");
        } catch (RuntimeException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testWrapAndThrow_WithIndex_RuntimeException_Wrap() throws IOException {
        SerializerProvider provider = new SerializerProvider(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isEnabled(SerializationFeature feature) {
                return true;
            }
        };
        RuntimeException re = new RuntimeException("unchecked");
        try {
            serializer.wrapAndThrow(provider, re, new Object(), 0);
            fail("Expected IOException");
        } catch (IOException e) {
            assertTrue(e instanceof JsonMappingException);
        }
    }

    // ----------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testFindPropertyFilter_NullProvider() throws JsonMappingException {
        // provider.getFilterProvider() returns null -> should throw
        SerializerProvider provider = new SerializerProvider(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public FilterProvider getFilterProvider() {
                return null;
            }
        };
        try {
            serializer.findPropertyFilter(provider, "id", new Object());
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("No FilterProvider configured"));
        }
    }

    @Test(timeout = 4000)
    public void testFindPropertyFilter_WithFilter() throws JsonMappingException {
        final PropertyFilter dummyFilter = new PropertyFilter() {
            @Override
            public void serializeAsField(Object pojo, JsonGenerator gen, SerializerProvider prov,
                    PropertyWriter writer) throws Exception {}
            @Override
            public void serializeAsElement(Object elementValue, JsonGenerator gen, SerializerProvider prov,
                    PropertyWriter writer) throws Exception {}
        };
        FilterProvider filterProvider = new FilterProvider() {
            @Override
            public PropertyFilter findPropertyFilter(Object filterId, Object valueToFilter) {
                return dummyFilter;
            }
        };
        SerializerProvider provider = new SerializerProvider(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public FilterProvider getFilterProvider() {
                return filterProvider;
            }
        };
        PropertyFilter result = serializer.findPropertyFilter(provider, "id", new Object());
        assertSame(dummyFilter, result);
    }

    // Additional tests for findConvertingContentSerializer edge cases
    @Test(timeout = 4000)
    public void testFindConvertingContentSerializer_NullIntr() throws JsonMappingException {
        SerializerProvider provider = new SerializerProvider(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public AnnotationIntrospector getAnnotationIntrospector() {
                return null;
            }
        };
        BeanProperty prop = new BeanProperty.Std(null, null, null, null, null, false, null);
        JsonSerializer<?> result = serializer.findConvertingContentSerializer(provider, prop, null);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testFindConvertingContentSerializer_NullProp() throws JsonMappingException {
        SerializerProvider provider = new SerializerProvider(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public AnnotationIntrospector getAnnotationIntrospector() {
                return new AnnotationIntrospector() {
                    @Override
                    public Object findSerializationContentConverter(AnnotatedMember member) {
                        return null;
                    }
                };
            }
        };
        JsonSerializer<?> result = serializer.findConvertingContentSerializer(provider, null, null);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testFindConvertingContentSerializer_NoConverter() throws JsonMappingException {
        SerializerProvider provider = new SerializerProvider(null, null, null) {
            private static final long serialVersionUID = 1L;
            @Override
            public AnnotationIntrospector getAnnotationIntrospector() {
                return new AnnotationIntrospector() {
                    @Override
                    public Object findSerializationContentConverter(AnnotatedMember member) {
                        return null; // no converter
                    }
                };
            }
        };
        BeanProperty prop = new BeanProperty.Std(null, null, null, null, null, false, null);
        JsonSerializer<?> existing = new StdSerializer<String>(String.class) {
            private static final long serialVersionUID = 1L;
            @Override
            public void serialize(String value, JsonGenerator gen, SerializerProvider prov) {}
        };
        JsonSerializer<?> result = serializer.findConvertingContentSerializer(provider, prop, existing);
        assertSame(existing, result);
    }
}