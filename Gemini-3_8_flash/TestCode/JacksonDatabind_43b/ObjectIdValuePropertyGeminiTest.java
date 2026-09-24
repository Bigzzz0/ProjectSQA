package com.fasterxml.jackson.databind.deser.impl;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: com.fasterxml.jackson.databind.deser.impl.ObjectIdValueProperty
 * Extends     : SettableBeanProperty
 * 
 * Branches & Conditions Targeted:
 * 1. Constructor initializations:
 *    - ObjectIdValueProperty(ObjectIdReader, PropertyMetadata)
 *    - ObjectIdValueProperty(ObjectIdValueProperty, JsonDeserializer) -> via withValueDeserializer()
 *    - ObjectIdValueProperty(ObjectIdValueProperty, PropertyName)     -> via withName()
 * 2. Annotation & Introspection:
 *    - getAnnotation(Class<A>) -> returns null unconditionally.
 *    - getMember()             -> returns null unconditionally.
 * 3. Mutation operations (set / setAndReturn):
 *    - Branch: _objectIdReader.idProperty == null -> throws UnsupportedOperationException.
 *    - Branch: _objectIdReader.idProperty != null -> delegates to idProperty.setAndReturn(instance, value).
 *    - set(instance, value) -> calls setAndReturn(instance, value).
 * 4. Deserialization operations (deserializeAndSet / deserializeSetAndReturn):
 *    - deserializeAndSet(...) -> delegates to deserializeSetAndReturn(...).
 *    - Branch: id == null -> returns null immediately (no binding, no idProp update).
 *    - Branch: id != null, idProperty == null -> binds instance to ReadableObjectId, returns instance.
 *    - Branch: id != null, idProperty != null -> binds instance to ReadableObjectId, delegates to
 *              idProperty.setAndReturn(instance, id).
 * 5. Ground Truth Defect Zone (Defects4J / databind#742):
 *    - TestObjectIdSerialization::testNullStringPropertyId
 *    - VALUE_NULL token handling where String/Object IDs deserialized directly from token stream
 *      can trigger JsonMappingException: "Can not deserialize instance of java.lang.String out of VALUE_NULL token"
 *      or corrupt bean instantiation.
 */

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.Test;

import java.io.IOException;
import java.lang.annotation.Annotation;

import static org.junit.Assert.*;

public class ObjectIdValuePropertyGeminiTest {

    // =========================================================================
    // Test Stub Helpers
    // =========================================================================

    private static class DummySettableProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;

        Object assignedInstance;
        Object assignedValue;
        Object returnOverride;

        public DummySettableProperty(PropertyName name, JavaType type) {
            super(name, type, PropertyMetadata.STD_OPTIONAL, null);
        }

        protected DummySettableProperty(DummySettableProperty src) {
            super(src);
            this.assignedInstance = src.assignedInstance;
            this.assignedValue = src.assignedValue;
            this.returnOverride = src.returnOverride;
        }

        protected DummySettableProperty(DummySettableProperty src, JsonDeserializer<?> deser) {
            super(src, deser);
            this.assignedInstance = src.assignedInstance;
            this.assignedValue = src.assignedValue;
            this.returnOverride = src.returnOverride;
        }

        protected DummySettableProperty(DummySettableProperty src, PropertyName newName) {
            super(src, newName);
            this.assignedInstance = src.assignedInstance;
            this.assignedValue = src.assignedValue;
            this.returnOverride = src.returnOverride;
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return new DummySettableProperty(this, newName);
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return new DummySettableProperty(this, deser);
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) {
            return null;
        }

        @Override
        public AnnotatedMember getMember() {
            return null;
        }

        @Override
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            set(instance, deserialize(p, ctxt));
        }

        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            set(instance, deserialize(p, ctxt));
            return returnOverride != null ? returnOverride : instance;
        }

        @Override
        public void set(Object instance, Object value) throws IOException {
            this.assignedInstance = instance;
            this.assignedValue = value;
        }

        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            set(instance, value);
            return returnOverride != null ? returnOverride : instance;
        }
    }

    private static class FixedValueDeserializer extends JsonDeserializer<Object> {
        private final Object fixedValue;

        public FixedValueDeserializer(Object fixedValue) {
            this.fixedValue = fixedValue;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            return fixedValue;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class NullStringPropertyEntity {
        public String id;
        public String name;

        public NullStringPropertyEntity() {}
        public NullStringPropertyEntity(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class NullIntegerPropertyEntity {
        public Integer id;
        public String value;

        public NullIntegerPropertyEntity() {}
        public NullIntegerEntity(Integer id, String value) {
            this.id = id;
            this.value = value;
        }
    }

    private DeserializationContext createDeserializationContext(ObjectMapper mapper, JsonParser parser) {
        return ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), parser, mapper.getInjectableValues());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructionAndPropertyAccessors() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        PropertyName propName = new PropertyName("oidProp");
        ObjectIdGenerator<?> generator = new ObjectIdGenerators.IntSequenceGenerator();
        JsonDeserializer<Object> deser = new FixedValueDeserializer("testId");
        ObjectIdResolver resolver = new SimpleObjectIdResolver();

        ObjectIdReader reader = ObjectIdReader.construct(type, propName, generator, deser, null, resolver);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        assertEquals("oidProp", prop.getName());
        assertEquals(type, prop.getType());
        assertEquals(PropertyMetadata.STD_REQUIRED, prop.getMetadata());
        assertSame(deser, prop.getValueDeserializer());
        assertSame(reader, prop._objectIdReader);
        assertNull(prop.getAnnotation(Deprecated.class));
        assertNull(prop.getMember());
    }

    @Test(timeout = 4000)
    public void testWithName() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdReader reader = ObjectIdReader.construct(type, new PropertyName("oldName"),
                new ObjectIdGenerators.IntSequenceGenerator(), null, null, new SimpleObjectIdResolver());

        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        ObjectIdValueProperty renamed = prop.withName(new PropertyName("newName"));

        assertNotSame(prop, renamed);
        assertEquals("newName", renamed.getName());
        assertEquals("oldName", prop.getName());
        assertSame(prop._objectIdReader, renamed._objectIdReader);
    }

    @Test(timeout = 4000)
    public void testWithValueDeserializer() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdReader reader = ObjectIdReader.construct(type, new PropertyName("id"),
                new ObjectIdGenerators.IntSequenceGenerator(), null, null, new SimpleObjectIdResolver());

        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        JsonDeserializer<Object> newDeser = new FixedValueDeserializer("val");
        ObjectIdValueProperty updated = prop.withValueDeserializer(newDeser);

        assertNotSame(prop, updated);
        assertSame(newDeser, updated.getValueDeserializer());
        assertNull(prop.getValueDeserializer());
        assertSame(prop._objectIdReader, updated._objectIdReader);
    }

    @Test(timeout = 4000)
    public void testSetAndReturnWithIdPropertyPresent() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummySettableProperty dummyProp = new DummySettableProperty(new PropertyName("id"), type);
        dummyProp.returnOverride = "MODIFIED_INSTANCE";

        ObjectIdReader reader = ObjectIdReader.construct(type, new PropertyName("id"),
                new ObjectIdGenerators.IntSequenceGenerator(), null, dummyProp, new SimpleObjectIdResolver());
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        Object target = new Object();
        Object result = prop.setAndReturn(target, "myId");

        assertSame("MODIFIED_INSTANCE", result);
        assertSame(target, dummyProp.assignedInstance);
        assertEquals("myId", dummyProp.assignedValue);
    }

    @Test(timeout = 4000)
    public void testSetDelegatesToSetAndReturn() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummySettableProperty dummyProp = new DummySettableProperty(new PropertyName("id"), type);

        ObjectIdReader reader = ObjectIdReader.construct(type, new PropertyName("id"),
                new ObjectIdGenerators.IntSequenceGenerator(), null, dummyProp, new SimpleObjectIdResolver());
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        Object target = new Object();
        prop.set(target, "directSetId");

        assertSame(target, dummyProp.assignedInstance);
        assertEquals("directSetId", dummyProp.assignedValue);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWhenDeserializedIdIsNull() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonDeserializer<Object> nullDeser = new FixedValueDeserializer(null);
        ObjectIdReader reader = ObjectIdReader.construct(type, new PropertyName("id"),
                new ObjectIdGenerators.IntSequenceGenerator(), nullDeser, null, new SimpleObjectIdResolver());

        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        Object target = new Object();

        Object result = prop.deserializeSetAndReturn(parser, ctxt, target);
        assertNull("When deserialized id is null, deserializeSetAndReturn must return null", result);
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetWhenDeserializedIdIsNull() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonDeserializer<Object> nullDeser = new FixedValueDeserializer(null);
        DummySettableProperty dummyProp = new DummySettableProperty(new PropertyName("id"), type);

        ObjectIdReader reader = ObjectIdReader.construct(type, new PropertyName("id"),
                new ObjectIdGenerators.IntSequenceGenerator(), nullDeser, dummyProp, new SimpleObjectIdResolver());

        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        Object target = new Object();

        prop.deserializeAndSet(parser, ctxt, target);
        assertNull(dummyProp.assignedInstance);
        assertNull(dummyProp.assignedValue);
    }

    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWithNonNullIdAndNoIdProperty() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        JsonDeserializer<Object> deser = new FixedValueDeserializer("bound-id-100");
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.StringIdGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();

        ObjectIdReader reader = ObjectIdReader.construct(type, new PropertyName("id"), gen, deser, null, resolver);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        Object target = new Object();
        Object result = prop.deserializeSetAndReturn(parser, ctxt, target);

        assertSame("Without idProperty, deserializeSetAndReturn must return the target instance", target, result);

        ReadableObjectId roid = ctxt.findObjectId("bound-id-100", gen, resolver);
        assertNotNull(roid);
        assertSame(target, roid.resolve());
    }

    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWithNonNullIdAndIdPropertyPresent() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummySettableProperty idProp = new DummySettableProperty(new PropertyName("id"), type);
        Object updatedInstance = new Object();
        idProp.returnOverride = updatedInstance;

        JsonDeserializer<Object> deser = new FixedValueDeserializer("bound-id-200");
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.StringIdGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();

        ObjectIdReader reader = ObjectIdReader.construct(type, new PropertyName("id"), gen, deser, idProp, resolver);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        Object target = new Object();
        Object result = prop.deserializeSetAndReturn(parser, ctxt, target);

        assertSame(updatedInstance, result);
        assertSame(target, idProp.assignedInstance);
        assertEquals("bound-id-200", idProp.assignedValue);

        ReadableObjectId roid = ctxt.findObjectId("bound-id-200", gen, resolver);
        assertNotNull(roid);
        assertSame(target, roid.resolve());
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetWithNonNullId() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("{}");
        DeserializationContext ctxt = createDeserializationContext(mapper, parser);

        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        DummySettableProperty idProp = new DummySettableProperty(new PropertyName("id"), type);

        JsonDeserializer<Object> deser = new FixedValueDeserializer("bound-id-300");
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.StringIdGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();

        ObjectIdReader reader = ObjectIdReader.construct(type, new PropertyName("id"), gen, deser, idProp, resolver);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        Object target = new Object();
        prop.deserializeAndSet(parser, ctxt, target);

        assertSame(target, idProp.assignedInstance);
        assertEquals("bound-id-300", idProp.assignedValue);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectNullStringPropertyId() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":null,\"name\":\"Bob\"}";

        NullStringPropertyEntity result = mapper.readValue(json, NullStringPropertyEntity.class);
        assertNotNull("Deserialization of POJO with null String ObjectId must not return null", result);
        assertNull("ObjectId property should be null", result.id);
        assertEquals("Bob", result.name);
    }

    @Test(timeout = 4000)
    public void testDefectNullIntegerPropertyId() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":null,\"value\":\"testVal\"}";

        NullIntegerPropertyEntity result = mapper.readValue(json, NullIntegerPropertyEntity.class);
        assertNotNull("Deserialization of POJO with null Integer ObjectId must not return null", result);
        assertNull("ObjectId property should be null", result.id);
        assertEquals("testVal", result.value);
    }

    @Test(timeout = 4000)
    public void testDefectDirectTokenHandlingOnValueNull() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("{\"id\":null}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        DeserializationContext ctxt = createDeserializationContext(mapper, parser);
        JavaType stringType = mapper.getTypeFactory().constructType(String.class);
        JsonDeserializer<Object> stringDeser = ctxt.findRootValueDeserializer(stringType);

        DummySettableProperty idProp = new DummySettableProperty(new PropertyName("id"), stringType);
        ObjectIdReader reader = ObjectIdReader.construct(
                stringType,
                new PropertyName("id"),
                new ObjectIdGenerators.PropertyGenerator(NullStringPropertyEntity.class) {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public Object generateId(Object forPojo) { return null; }
                    @Override
                    public ObjectIdGenerator<Object> forScope(Class<?> scope) { return this; }
                    @Override
                    public ObjectIdGenerator<Object> newForSerialization(Object context) { return this; }
                    @Override
                    public IdKey key(Object key) { return new IdKey(getClass(), getClass(), key); }
                },
                stringDeser,
                idProp,
                new SimpleObjectIdResolver()
        );

        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        NullStringPropertyEntity instance = new NullStringPropertyEntity();

        Object res = prop.deserializeSetAndReturn(parser, ctxt, instance);
        assertNull("A null deserialized token should return null from deserializeSetAndReturn", res);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetThrowsUnsupportedOperationExceptionWhenIdPropertyIsNull() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdReader reader = ObjectIdReader.construct(type, new PropertyName("id"),
                new ObjectIdGenerators.IntSequenceGenerator(), null, null, new SimpleObjectIdResolver());
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        try {
            prop.set(new Object(), "testValue");
            fail("Expected UnsupportedOperationException when idProperty is null");
        } catch (UnsupportedOperationException e) {
            assertTrue("Exception message must describe missing SettableBeanProperty",
                    e.getMessage().contains("Should not call set() on ObjectIdProperty that has no SettableBeanProperty"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSetAndReturnThrowsUnsupportedOperationExceptionWhenIdPropertyIsNull() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        ObjectIdReader reader = ObjectIdReader.construct(type, new PropertyName("id"),
                new ObjectIdGenerators.IntSequenceGenerator(), null, null, new SimpleObjectIdResolver());
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        try {
            prop.setAndReturn(new Object(), "testValue");
            fail("Expected UnsupportedOperationException when idProperty is null");
        } catch (UnsupportedOperationException e) {
            assertTrue("Exception message must describe missing SettableBeanProperty",
                    e.getMessage().contains("Should not call set() on ObjectIdProperty that has no SettableBeanProperty"));
        } catch (IOException e) {
            fail("Unexpected IOException: " + e.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testChainModificationsWithNameAndDeserializer() {
        JavaType type = TypeFactory.defaultInstance().constructType(Integer.class);
        ObjectIdReader reader = ObjectIdReader.construct(type, new PropertyName("initial"),
                new ObjectIdGenerators.IntSequenceGenerator(), null, null, new SimpleObjectIdResolver());

        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);
        JsonDeserializer<Object> deser1 = new FixedValueDeserializer(123);
        JsonDeserializer<Object> deser2 = new FixedValueDeserializer(456);

        ObjectIdValueProperty step1 = prop.withName(new PropertyName("step1"));
        ObjectIdValueProperty step2 = step1.withValueDeserializer(deser1);
        ObjectIdValueProperty step3 = step2.withName(new PropertyName("step3"));
        ObjectIdValueProperty step4 = step3.withValueDeserializer(deser2);

        assertEquals("initial", prop.getName());
        assertNull(prop.getValueDeserializer());

        assertEquals("step1", step1.getName());
        assertNull(step1.getValueDeserializer());

        assertEquals("step1", step2.getName());
        assertSame(deser1, step2.getValueDeserializer());

        assertEquals("step3", step3.getName());
        assertSame(deser1, step3.getValueDeserializer());

        assertEquals("step3", step4.getName());
        assertSame(deser2, step4.getValueDeserializer());

        assertSame(prop._objectIdReader, step4._objectIdReader);
    }
}