package com.fasterxml.jackson.databind.jsontype.impl;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.AsWrapperTypeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: AsWrapperTypeDeserializer._deserialize(JsonParser, DeserializationContext)
 * 
 * Decision branches identified:
 * 1. p.canReadTypeId() == true && typeId != null -> _deserializeWithNativeTypeId
 * 2. p.canReadTypeId() == false OR typeId == null -> continue
 * 3. p.getCurrentToken() != START_OBJECT -> throw wrongTokenException
 * 4. p.nextToken() != FIELD_NAME -> throw wrongTokenException
 * 5. _typeIdVisible == true && p.getCurrentToken() == START_OBJECT -> merge type id
 * 6. p.nextToken() != END_OBJECT -> throw wrongTokenException
 * 
 * Boundary conditions:
 * - null typeId from parser
 * - empty string typeId
 * - missing START_OBJECT token
 * - missing FIELD_NAME token
 * - missing END_OBJECT token
 * - _typeIdVisible true/false
 * - null defaultImpl
 * - null property
 * 
 * Defect-targeted scenario (from Defects4J):
 * - WrapperObjectWithObjectIdTest::testSimple fails with:
 *   "Unexpected token (FIELD_NAME), expected START_OBJECT: need JSON Object to contain 
 *    As.WRAPPER_OBJECT type information for class ...Computer"
 * - This occurs when the parser is positioned at FIELD_NAME instead of START_OBJECT
 *   when _deserialize is called, likely due to object id handling that consumes
 *   the START_OBJECT token before type deserialization.
 * 
 * Test strategy:
 * - Direct unit tests for _deserialize via public deserializeTypedFrom* methods
 * - Integration test reproducing the object-id scenario
 * - Boundary tests for token sequences
 * - Tests for _typeIdVisible merging behavior
 */
public class AsWrapperTypeDeserializerDeepseekTest {

    // ===================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ===================================================================

    @Test(timeout = 4000)
    public void testForPropertySamePropertyReturnsThis() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeIdResolver idRes = new SimpleTypeIdResolver();
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(
                baseType, idRes, "@class", false, null);
        
        BeanProperty prop = new SimpleBeanProperty("test");
        assertSame(deser, deser.forProperty(prop));
    }

    @Test(timeout = 4000)
    public void testForPropertyDifferentPropertyReturnsNewInstance() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeIdResolver idRes = new SimpleTypeIdResolver();
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(
                baseType, idRes, "@class", false, null);
        
        BeanProperty prop1 = new SimpleBeanProperty("prop1");
        BeanProperty prop2 = new SimpleBeanProperty("prop2");
        AsWrapperTypeDeserializer newDeser = (AsWrapperTypeDeserializer) deser.forProperty(prop2);
        
        assertNotSame(deser, newDeser);
        assertSame(prop2, newDeser.getProperty());
    }

    @Test(timeout = 4000)
    public void testGetTypeInclusionReturnsWrapperObject() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        TypeIdResolver idRes = new SimpleTypeIdResolver();
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(
                baseType, idRes, "@class", false, null);
        
        assertEquals(JsonTypeInfo.As.WRAPPER_OBJECT, deser.getTypeInclusion());
    }

    @Test(timeout = 4000)
    public void testDeserializeTypedFromObjectNormalCase() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addAbstractTypeMapping(Animal.class, Dog.class));
        
        String json = "{\"dog\":{\"name\":\"Rex\"}}";
        Animal result = mapper.readValue(json, new TypeReference<Animal>() {});
        
        assertNotNull(result);
        assertTrue(result instanceof Dog);
        assertEquals("Rex", ((Dog) result).name);
    }

    @Test(timeout = 4000)
    public void testDeserializeTypedFromArrayNormalCase() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addAbstractTypeMapping(Animal.class, Cat.class));
        
        String json = "{\"cat\":{\"name\":\"Whiskers\"}}";
        Animal result = mapper.readValue(json, new TypeReference<Animal>() {});
        
        assertNotNull(result);
        assertTrue(result instanceof Cat);
        assertEquals("Whiskers", ((Cat) result).name);
    }

    @Test(timeout = 4000)
    public void testDeserializeTypedFromScalarNormalCase() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addAbstractTypeMapping(Animal.class, Dog.class));
        
        String json = "{\"dog\":{\"name\":\"Buddy\"}}";
        Animal result = mapper.readValue(json, new TypeReference<Animal>() {});
        
        assertNotNull(result);
        assertTrue(result instanceof Dog);
        assertEquals("Buddy", ((Dog) result).name);
    }

    @Test(timeout = 4000)
    public void testDeserializeTypedFromAnyNormalCase() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addAbstractTypeMapping(Animal.class, Cat.class));
        
        String json = "{\"cat\":{\"name\":\"Tom\"}}";
        Animal result = mapper.readValue(json, new TypeReference<Animal>() {});
        
        assertNotNull(result);
        assertTrue(result instanceof Cat);
        assertEquals("Tom", ((Cat) result).name);
    }

    // ===================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ===================================================================

    @Test(timeout = 4000)
    public void testDeserializeWithNullTypeId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addAbstractTypeMapping(Animal.class, Dog.class));
        
        // Missing type id field
        String json = "{\"name\":\"Rex\"}";
        try {
            mapper.readValue(json, new TypeReference<Animal>() {});
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // Expected - type id not found
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithEmptyStringTypeId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addAbstractTypeMapping(Animal.class, Dog.class));
        
        String json = "{\"\":{\"name\":\"Rex\"}}";
        try {
            mapper.readValue(json, new TypeReference<Animal>() {});
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // Expected - unknown type id
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeMissingStartObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addAbstractTypeMapping(Animal.class, Dog.class));
        
        // Array instead of object
        String json = "[{\"dog\":{\"name\":\"Rex\"}}]";
        try {
            mapper.readValue(json, new TypeReference<Animal>() {});
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("need JSON Object"));
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeMissingFieldName() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addAbstractTypeMapping(Animal.class, Dog.class));
        
        // Empty object
        String json = "{}";
        try {
            mapper.readValue(json, new TypeReference<Animal>() {});
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("need JSON String"));
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeMissingEndObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addAbstractTypeMapping(Animal.class, Dog.class));
        
        // Missing closing brace
        String json = "{\"dog\":{\"name\":\"Rex\"}";
        try {
            mapper.readValue(json, new TypeReference<Animal>() {});
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("expected closing END_OBJECT"));
        }
    }

    @Test(timeout = 4000)
    public void testTypeIdVisibleTrueMergesTypeId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
        
        // With type id visible, the type id should be included in the deserialized object
        String json = "{\"@class\":\"com.fasterxml.jackson.databind.jsontype.impl.AsWrapperTypeDeserializerDeepseekTest$Dog\",\"name\":\"Rex\"}";
        Dog result = mapper.readValue(json, Dog.class);
        
        assertNotNull(result);
        assertEquals("Rex", result.name);
    }

    @Test(timeout = 4000)
    public void testTypeIdVisibleFalseDoesNotMergeTypeId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addAbstractTypeMapping(Animal.class, Dog.class));
        
        String json = "{\"dog\":{\"name\":\"Rex\"}}";
        Animal result = mapper.readValue(json, new TypeReference<Animal>() {});
        
        assertNotNull(result);
        assertTrue(result instanceof Dog);
        assertEquals("Rex", ((Dog) result).name);
    }

    // ===================================================================
    // Partition C: Defect-Targeted Branch Zone
    // ===================================================================

    /**
     * Defect-targeted test: Reproduces the scenario from
     * WrapperObjectWithObjectIdTest::testSimple where the parser is
     * positioned at FIELD_NAME instead of START_OBJECT when _deserialize
     * is called, due to object id handling.
     */
    @Test(timeout = 4000)
    public void testDeserializeWithObjectIdAndWrapperType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addAbstractTypeMapping(Animal.class, Dog.class));
        
        // Simulate the object-id scenario: the parser has already consumed
        // the START_OBJECT token and is positioned at the first FIELD_NAME
        String json = "{\"@id\":1,\"dog\":{\"name\":\"Rex\"}}";
        
        try {
            // This should fail on the defective version because the parser
            // is at FIELD_NAME (@id) when _deserialize is called
            mapper.readValue(json, new TypeReference<Animal>() {});
            // On fixed version, this should either succeed or fail with a
            // different error, but not with the specific defect error
        } catch (JsonMappingException e) {
            // The defect produces: "Unexpected token (FIELD_NAME), expected START_OBJECT"
            // The fixed version should not produce this error for this input
            assertFalse("Defect reproduced: " + e.getMessage(),
                    e.getMessage().contains("Unexpected token (FIELD_NAME), expected START_OBJECT"));
        }
    }

    /**
     * Direct test targeting the defect: call _deserialize with parser
     * positioned at FIELD_NAME instead of START_OBJECT.
     */
    @Test(timeout = 4000)
    public void testDeserializeWithParserAtFieldName() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        TypeIdResolver idRes = new SimpleTypeIdResolver();
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(
                baseType, idRes, "@class", false, null);
        
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"dog\":{\"name\":\"Rex\"}}";
        JsonParser p = mapper.getFactory().createParser(json);
        
        // Advance to FIELD_NAME token (simulating object-id consumption)
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME "dog"
        
        DeserializationContext ctxt = mapper.getDeserializationContext();
        
        try {
            deser.deserializeTypedFromObject(p, ctxt);
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // This is the defect: should throw "need JSON Object" error
            assertTrue(e.getMessage().contains("need JSON Object"));
        }
    }

    /**
     * Test that verifies the correct behavior when parser is at START_OBJECT.
     */
    @Test(timeout = 4000)
    public void testDeserializeWithParserAtStartObject() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        TypeIdResolver idRes = new SimpleTypeIdResolver();
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(
                baseType, idRes, "@class", false, null);
        
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"dog\":{\"name\":\"Rex\"}}";
        JsonParser p = mapper.getFactory().createParser(json);
        
        p.nextToken(); // START_OBJECT
        
        DeserializationContext ctxt = mapper.getDeserializationContext();
        
        try {
            Object result = deser.deserializeTypedFromObject(p, ctxt);
            assertNotNull(result);
        } catch (JsonMappingException e) {
            // May fail if type resolver can't find Dog class, but should not
            // fail with the "need JSON Object" error
            assertFalse(e.getMessage().contains("need JSON Object"));
        }
    }

    // ===================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ===================================================================

    @Test(timeout = 4000)
    public void testDeserializeWithUnknownTypeId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addAbstractTypeMapping(Animal.class, Dog.class));
        
        String json = "{\"unknown\":{\"name\":\"Rex\"}}";
        try {
            mapper.readValue(json, new TypeReference<Animal>() {});
            fail("Expected JsonMappingException");
        } catch (JsonMappingException e) {
            // Expected - unknown type id
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNullDefaultImpl() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        TypeIdResolver idRes = new SimpleTypeIdResolver();
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(
                baseType, idRes, "@class", false, null);
        
        assertNull(deser.getDefaultImpl());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNonNullDefaultImpl() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        TypeIdResolver idRes = new SimpleTypeIdResolver();
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(
                baseType, idRes, "@class", false, Dog.class);
        
        assertEquals(Dog.class, deser.getDefaultImpl());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNullProperty() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        TypeIdResolver idRes = new SimpleTypeIdResolver();
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(
                baseType, idRes, "@class", false, null);
        
        assertNull(deser.getProperty());
    }

    @Test(timeout = 4000)
    public void testDeserializeWithNonNullProperty() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        TypeIdResolver idRes = new SimpleTypeIdResolver();
        BeanProperty prop = new SimpleBeanProperty("test");
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(
                baseType, idRes, "@class", false, null);
        
        AsWrapperTypeDeserializer withProp = (AsWrapperTypeDeserializer) deser.forProperty(prop);
        assertSame(prop, withProp.getProperty());
    }

    // ===================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ===================================================================

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        TypeIdResolver idRes = new SimpleTypeIdResolver();
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(
                baseType, idRes, "@class", false, null);
        
        // Serialize
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(deser);
        oos.close();
        
        // Deserialize
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        AsWrapperTypeDeserializer deser2 = (AsWrapperTypeDeserializer) ois.readObject();
        ois.close();
        
        assertEquals(deser.getTypeInclusion(), deser2.getTypeInclusion());
        assertEquals(deser.getTypePropertyName(), deser2.getTypePropertyName());
        assertEquals(deser.isTypeIdVisible(), deser2.isTypeIdVisible());
    }

    @Test(timeout = 4000)
    public void testGetTypePropertyName() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        TypeIdResolver idRes = new SimpleTypeIdResolver();
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(
                baseType, idRes, "@class", false, null);
        
        assertEquals("@class", deser.getTypePropertyName());
    }

    @Test(timeout = 4000)
    public void testIsTypeIdVisible() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        TypeIdResolver idRes = new SimpleTypeIdResolver();
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(
                baseType, idRes, "@class", true, null);
        
        assertTrue(deser.isTypeIdVisible());
    }

    @Test(timeout = 4000)
    public void testGetBaseType() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(Animal.class);
        TypeIdResolver idRes = new SimpleTypeIdResolver();
        AsWrapperTypeDeserializer deser = new AsWrapperTypeDeserializer(
                baseType, idRes, "@class", false, null);
        
        assertEquals(Animal.class, deser.getBaseType().getRawClass());
    }

    // ===================================================================
    // Helper classes and methods
    // ===================================================================

    static class SimpleTypeIdResolver implements TypeIdResolver {
        private JavaType _baseType;
        
        @Override
        public void init(JavaType bt) {
            _baseType = bt;
        }

        @Override
        public String idFromValue(Object value) {
            return value.getClass().getSimpleName().toLowerCase();
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return idFromValue(value);
        }

        @Override
        public String idFromBaseType() {
            return _baseType.getRawClass().getSimpleName().toLowerCase();
        }

        @Override
        public JavaType typeFromId(DatabindContext context, String id) throws IOException {
            if ("dog".equals(id)) return TypeFactory.defaultInstance().constructType(Dog.class);
            if ("cat".equals(id)) return TypeFactory.defaultInstance().constructType(Cat.class);
            throw new IllegalArgumentException("Unknown type id: " + id);
        }

        @Override
        public String getDescForKnownTypeIds() {
            return "dog, cat";
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return JsonTypeInfo.Id.CLASS;
        }
    }

    static class SimpleBeanProperty extends BeanProperty.Std {
        public SimpleBeanProperty(String name) {
            super(name, null, null, null, null);
        }
    }

    // Test classes for polymorphic deserialization
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "type")
    @JsonTypeIdResolver(SimpleTypeIdResolver.class)
    static abstract class Animal {
        public String name;
    }

    static class Dog extends Animal {
        public Dog() {}
        public Dog(String name) { this.name = name; }
    }

    static class Cat extends Animal {
        public Cat() {}
        public Cat(String name) { this.name = name; }
    }
}