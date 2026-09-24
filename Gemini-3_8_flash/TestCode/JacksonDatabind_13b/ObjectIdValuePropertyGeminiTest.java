package com.fasterxml.jackson.databind.deser.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.impl.ObjectIdValueProperty
 *
 * 1. Constructor Branches:
 *    - (ObjectIdReader, PropertyMetadata): populates super with reader fields (propertyName, idType, metadata, deserializer).
 *    - (ObjectIdValueProperty, JsonDeserializer<?>): copy constructor updating deserializer.
 *    - (ObjectIdValueProperty, PropertyName): copy constructor updating name.
 *    - (ObjectIdValueProperty, String): deprecated constructor wrapping string in PropertyName.
 *
 * 2. Method Branches:
 *    - withName(PropertyName): returns new ObjectIdValueProperty with new name.
 *    - withValueDeserializer(JsonDeserializer<?>): returns new ObjectIdValueProperty with new deserializer.
 *    - getAnnotation(Class<A>): always returns null.
 *    - getMember(): always returns null.
 *    - deserializeAndSet(JsonParser, DeserializationContext, Object): delegates to deserializeSetAndReturn.
 *    - deserializeSetAndReturn(JsonParser, DeserializationContext, Object):
 *         a) deserializes id: _valueDeserializer.deserialize(jp, ctxt)
 *         b) Defect Target (databind#742 / TestObjectIdDeserialization::testNullObjectId):
 *            When id is null, ctxt.findObjectId(null, ...) returns null, causing roid.bindItem(instance)
 *            to throw NullPointerException unless handled.
 *         c) non-null id: finds roid, calls roid.bindItem(instance).
 *         d) idProp != null: delegates to idProp.setAndReturn(instance, id).
 *         e) idProp == null: returns instance directly.
 *    - set(Object, Object): delegates to setAndReturn.
 *    - setAndReturn(Object, Object):
 *         a) idProp == null: throws UnsupportedOperationException.
 *         b) idProp != null: delegates to idProp.setAndReturn(instance, value).
 */
public class ObjectIdValuePropertyGeminiTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface TestAnnotation {
    }

    static class DummyPOJO {
        public Object id;
        public String name;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    static class IdentifiablePOJO {
        public String id;
        public String name;
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndBasicGetters() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdGenerator<?> generator = new ObjectIdGenerators.IntSequenceGenerator();
        JsonDeserializer<Object> deser = new ObjectMapper().deserializationConfig().findRootValueDeserializer(type);

        ObjectIdReader reader = ObjectIdReader.construct(
                type,
                new PropertyName("id"),
                generator,
                deser,
                null,
                new SimpleObjectIdResolver()
        );

        PropertyMetadata metadata = PropertyMetadata.STD_REQUIRED;
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, metadata);

        assertEquals("id", prop.getName());
        assertEquals(type, prop.getType());
        assertEquals(metadata, prop.getMetadata());
        assertNull(prop.getAnnotation(TestAnnotation.class));
        assertNull(prop.getMember());
    }

    @Test(timeout = 4000)
    public void testWithNameAndWithValueDeserializer() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdGenerator<?> generator = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectMapper mapper = new ObjectMapper();
        JsonDeserializer<Object> deser1 = mapper.deserializationConfig().findRootValueDeserializer(type);
        JsonDeserializer<Object> deser2 = mapper.deserializationConfig().findRootValueDeserializer(
                TypeFactory.defaultInstance().constructType(Integer.class)
        );

        ObjectIdReader reader = ObjectIdReader.construct(
                type,
                new PropertyName("oldId"),
                generator,
                deser1,
                null,
                new SimpleObjectIdResolver()
        );

        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        // Test withName(PropertyName)
        PropertyName newName = new PropertyName("newId");
        ObjectIdValueProperty propWithName = prop.withName(newName);
        assertNotNull(propWithName);
        assertNotSame(prop, propWithName);
        assertEquals("newId", propWithName.getName());

        // Test withName(String) deprecated constructor via reflection or subclass usage
        ObjectIdValueProperty propWithString = new ObjectIdValueProperty(prop, "stringName");
        assertEquals("stringName", propWithString.getName());

        // Test withValueDeserializer
        ObjectIdValueProperty propWithDeser = prop.withValueDeserializer(deser2);
        assertNotNull(propWithDeser);
        assertNotSame(prop, propWithDeser);
        assertSame(deser2, propWithDeser.getValueDeserializer());
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetWithIdPropNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdGenerator<?> generator = new ObjectIdGenerators.IntSequenceGenerator();
        JsonDeserializer<Object> deser = mapper.deserializationConfig().findRootValueDeserializer(type);

        ObjectIdReader reader = ObjectIdReader.construct(
                type,
                new PropertyName("id"),
                generator,
                deser,
                null, // idProperty is null
                new SimpleObjectIdResolver()
        );

        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        DummyPOJO pojo = new DummyPOJO();

        JsonParser parser = mapper.getFactory().createParser("\"id-value-123\"");
        parser.nextToken(); // Move to VALUE_STRING
        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) {
            ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) ctxt)
                    .createInstance(mapper.getDeserializationConfig(), parser, mapper.getInjectableValues());
        }

        Object result = prop.deserializeSetAndReturn(parser, ctxt, pojo);
        assertSame(pojo, result);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetAndReturnWithValidIdProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType pojoType = mapper.constructType(DummyPOJO.class);
        SettableBeanProperty idProp = null;

        com.fasterxml.jackson.databind.BeanDescription beanDesc =
                mapper.getDeserializationConfig().introspect(pojoType);
        for (com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition def : beanDesc.findProperties()) {
            if ("id".equals(def.getName())) {
                idProp = new MethodProperty(
                        def,
                        def.getPrimaryType(),
                        def.getWrapperName(),
                        mapper.getDeserializationConfig().findRootValueDeserializer(def.getPrimaryType()),
                        beanDesc.getClassAnnotations(),
                        def.getSetter() != null ? def.getSetter() : def.getField()
                );
                break;
            }
        }

        if (idProp == null) {
            // Fallback: simple SettableBeanProperty mock through anonymous class
            idProp = new SettableBeanProperty(
                    new PropertyName("id"),
                    TypeFactory.defaultInstance().constructType(Object.class),
                    PropertyMetadata.STD_OPTIONAL,
                    mapper.deserializationConfig().findRootValueDeserializer(TypeFactory.defaultInstance().constructType(Object.class))
            ) {
                private static final long serialVersionUID = 1L;

                @Override
                public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
                    return this;
                }

                @Override
                public SettableBeanProperty withName(PropertyName newName) {
                    return this;
                }

                @Override
                public <A extends Annotation> A getAnnotation(Class<A> acls) {
                    return null;
                }

                @Override
                public com.fasterxml.jackson.databind.introspect.AnnotatedMember getMember() {
                    return null;
                }

                @Override
                public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) {
                }

                @Override
                public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) {
                    return instance;
                }

                @Override
                public void set(Object instance, Object value) {
                    ((DummyPOJO) instance).id = value;
                }

                @Override
                public Object setAndReturn(Object instance, Object value) {
                    ((DummyPOJO) instance).id = value;
                    return "custom-return";
                }
            };
        }

        ObjectIdGenerator<?> generator = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectIdReader reader = ObjectIdReader.construct(
                TypeFactory.defaultInstance().constructType(Object.class),
                new PropertyName("id"),
                generator,
                mapper.deserializationConfig().findRootValueDeserializer(TypeFactory.defaultInstance().constructType(Object.class)),
                idProp,
                new SimpleObjectIdResolver()
        );

        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        DummyPOJO target = new DummyPOJO();

        // Testing set(instance, value)
        prop.set(target, "val-1");
        assertEquals("val-1", target.id);

        // Testing setAndReturn(instance, value)
        Object ret = prop.setAndReturn(target, "val-2");
        assertNotNull(ret);
        assertEquals("val-2", target.id);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Fault)
    // =========================================================================

    /**
     * Targets Defects4J bug where deserializing an object with a null object ID
     * causes a NullPointerException in ctxt.findObjectId(...) or roid.bindItem(...)
     * instead of gracefully returning null / handling missing ID.
     */
    @Test(timeout = 4000)
    public void testDefectNullObjectIdDirectDeserializeSetAndReturn() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType idType = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdGenerator<?> generator = new ObjectIdGenerators.PropertyGenerator(IdentifiablePOJO.class) {
            private static final long serialVersionUID = 1L;

            @Override
            public ObjectIdGenerator<Object> forScope(Class<?> scope) {
                return this;
            }

            @Override
            public ObjectIdGenerator<Object> newForSerialization(Object context) {
                return this;
            }

            @Override
            public com.fasterxml.jackson.annotation.ObjectIdGenerator.IdKey key(Object key) {
                return new IdKey(getClass(), null, key);
            }

            @Override
            public Object generateId(Object forPojo) {
                return null;
            }
        };

        ObjectIdReader reader = ObjectIdReader.construct(
                idType,
                new PropertyName("id"),
                generator,
                mapper.deserializationConfig().findRootValueDeserializer(idType),
                null,
                new SimpleObjectIdResolver()
        );

        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken(); // Move to VALUE_NULL

        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) {
            ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) ctxt)
                    .createInstance(mapper.getDeserializationConfig(), parser, mapper.getInjectableValues());
        }

        IdentifiablePOJO pojo = new IdentifiablePOJO();
        // On defective version: throws NullPointerException because id is null,
        // roid returned is null, and roid.bindItem(pojo) throws NPE.
        // On fixed version: gracefully returns null.
        Object returned = prop.deserializeSetAndReturn(parser, ctxt, pojo);
        assertNull("When object id is null, deserializeSetAndReturn must return null without NPE", returned);
    }

    @Test(timeout = 4000)
    public void testDefectNullObjectIdViaObjectMapper() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":null, \"name\":\"test\"}";

        // When Jackson handles null ID for an identifiable object, it should not throw NPE
        IdentifiablePOJO result = mapper.readValue(json, IdentifiablePOJO.class);
        assertNotNull(result);
        assertNull(result.id);
        assertEquals("test", result.name);
    }

    @Test(timeout = 4000)
    public void testDefectDeserializeAndSetDelegationWithNullId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType idType = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdGenerator<?> generator = new ObjectIdGenerators.IntSequenceGenerator();

        ObjectIdReader reader = ObjectIdReader.construct(
                idType,
                new PropertyName("id"),
                generator,
                mapper.deserializationConfig().findRootValueDeserializer(idType),
                null,
                new SimpleObjectIdResolver()
        );

        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        if (ctxt instanceof com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) {
            ctxt = ((com.fasterxml.jackson.databind.deser.DefaultDeserializationContext) ctxt)
                    .createInstance(mapper.getDeserializationConfig(), parser, mapper.getInjectableValues());
        }

        IdentifiablePOJO pojo = new IdentifiablePOJO();
        // Should not throw NullPointerException
        prop.deserializeAndSet(parser, ctxt, pojo);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testSetThrowsWhenIdPropertyIsNull() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdGenerator<?> generator = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectMapper mapper = new ObjectMapper();
        JsonDeserializer<Object> deser = mapper.deserializationConfig().findRootValueDeserializer(type);

        ObjectIdReader reader = ObjectIdReader.construct(
                type,
                new PropertyName("id"),
                generator,
                deser,
                null, // idProperty is null
                new SimpleObjectIdResolver()
        );

        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        prop.set(new DummyPOJO(), "someValue");
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testSetAndReturnThrowsWhenIdPropertyIsNull() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdGenerator<?> generator = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectMapper mapper = new ObjectMapper();
        JsonDeserializer<Object> deser = mapper.deserializationConfig().findRootValueDeserializer(type);

        ObjectIdReader reader = ObjectIdReader.construct(
                type,
                new PropertyName("id"),
                generator,
                deser,
                null, // idProperty is null
                new SimpleObjectIdResolver()
        );

        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        prop.setAndReturn(new DummyPOJO(), "someValue");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testImmutableCopyWithNamePreservesReader() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdGenerator<?> generator = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectMapper mapper = new ObjectMapper();
        JsonDeserializer<Object> deser = mapper.deserializationConfig().findRootValueDeserializer(type);

        ObjectIdReader reader = ObjectIdReader.construct(
                type,
                new PropertyName("origId"),
                generator,
                deser,
                null,
                new SimpleObjectIdResolver()
        );

        ObjectIdValueProperty original = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        ObjectIdValueProperty modified = original.withName(new PropertyName("newId"));

        assertEquals("origId", original.getName());
        assertEquals("newId", modified.getName());
        assertEquals(original.getType(), modified.getType());
        assertSame(original.getValueDeserializer(), modified.getValueDeserializer());
    }

    @Test(timeout = 4000)
    public void testImmutableCopyWithDeserializerPreservesReader() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdGenerator<?> generator = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectMapper mapper = new ObjectMapper();
        JsonDeserializer<Object> deser1 = mapper.deserializationConfig().findRootValueDeserializer(type);
        JsonDeserializer<Object> deser2 = mapper.deserializationConfig().findRootValueDeserializer(
                TypeFactory.defaultInstance().constructType(Long.class)
        );

        ObjectIdReader reader = ObjectIdReader.construct(
                type,
                new PropertyName("id"),
                generator,
                deser1,
                null,
                new SimpleObjectIdResolver()
        );

        ObjectIdValueProperty original = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        ObjectIdValueProperty modified = original.withValueDeserializer(deser2);

        assertSame(deser1, original.getValueDeserializer());
        assertSame(deser2, modified.getValueDeserializer());
        assertEquals(original.getName(), modified.getName());
    }
}