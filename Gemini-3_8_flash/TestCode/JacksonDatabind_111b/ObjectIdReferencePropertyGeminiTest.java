package com.fasterxml.jackson.databind.deser.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.UnresolvedForwardReference;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.impl.ObjectIdReferenceProperty
 *
 * Decision / Branch Matrix:
 * 1. withValueDeserializer(JsonDeserializer<?> deser)
 *    - Branch: _valueDeserializer == deser -> returns `this`
 *    - Branch: _valueDeserializer != deser -> returns new ObjectIdReferenceProperty
 *    - Known Defect Check ([databind#2303] / testNullWithinNested):
 *      When _valueDeserializer == _nullProvider, withValueDeserializer must keep NullValueProvider
 *      in sync with the new deserializer (or handle null value resolution properly).
 * 2. withNullProvider(NullValueProvider nva)
 *    - Creates new ObjectIdReferenceProperty with updated NullValueProvider.
 * 3. withName(PropertyName newName)
 *    - Creates new ObjectIdReferenceProperty with updated PropertyName.
 * 4. fixAccess(DeserializationConfig config)
 *    - Branch: _forward != null -> calls _forward.fixAccess(config)
 *    - Branch: _forward == null (defensive check if possible)
 * 5. getAnnotation(Class<A> acls), getMember(), getCreatorIndex()
 *    - Delegates directly to _forward.
 * 6. deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance)
 *    - Delegates to deserializeSetAndReturn(p, ctxt, instance).
 * 7. deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance)
 *    - Normal path: calls deserialize(p, ctxt) and setAndReturn(instance, value)
 *    - Exception path (UnresolvedForwardReference):
 *      - Branch: usingIdentityInfo == true (_objectIdInfo != null) -> appends Referring to ROID, returns null
 *      - Branch: usingIdentityInfo == true (_valueDeserializer.getObjectIdReader() != null) -> appends Referring, returns null
 *      - Branch: usingIdentityInfo == false (_objectIdInfo == null && reader == null) -> throws JsonMappingException
 * 8. set(Object instance, Object value) & setAndReturn(Object instance, Object value)
 *    - Delegates to _forward.set / _forward.setAndReturn.
 * 9. PropertyReferring (inner class)
 *    - handleResolvedForwardReference(Object id, Object value):
 *      - Branch: hasId(id) == false -> throws IllegalArgumentException
 *      - Branch: hasId(id) == true -> calls _parent.set(_pojo, value)
 */
public class ObjectIdReferencePropertyGeminiTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface DummyAnnotation {
        String value() default "test";
    }

    private static class MockForwardProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        private Object lastAssignedInstance;
        private Object lastAssignedValue;
        private boolean fixAccessCalled = false;
        private int creatorIndex = 42;

        public MockForwardProperty(PropertyName name, JavaType type) {
            super(name, type, PropertyMetadata.STD_REQUIRED, (JsonDeserializer<Object>) null);
        }

        public MockForwardProperty(MockForwardProperty src) {
            super(src);
            this.lastAssignedInstance = src.lastAssignedInstance;
            this.lastAssignedValue = src.lastAssignedValue;
            this.fixAccessCalled = src.fixAccessCalled;
            this.creatorIndex = src.creatorIndex;
        }

        @Override
        public SettableBeanProperty withName(PropertyName newName) {
            return this;
        }

        @Override
        public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) {
            return this;
        }

        @Override
        public SettableBeanProperty withNullProvider(NullValueProvider nva) {
            return this;
        }

        @Override
        public <A extends Annotation> A getAnnotation(Class<A> acls) {
            if (acls == DummyAnnotation.class) {
                @DummyAnnotation("mockAnnotation")
                class DummyHolder {}
                return acls.cast(DummyHolder.class.getAnnotation(DummyAnnotation.class));
            }
            return null;
        }

        @Override
        public AnnotatedMember getMember() {
            return null;
        }

        @Override
        public int getCreatorIndex() {
            return creatorIndex;
        }

        @Override
        public void fixAccess(DeserializationConfig config) {
            this.fixAccessCalled = true;
        }

        @Override
        public void deserializeAndSet(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            set(instance, deserialize(p, ctxt));
        }

        @Override
        public Object deserializeSetAndReturn(JsonParser p, DeserializationContext ctxt, Object instance) throws IOException {
            return setAndReturn(instance, deserialize(p, ctxt));
        }

        @Override
        public void set(Object instance, Object value) throws IOException {
            this.lastAssignedInstance = instance;
            this.lastAssignedValue = value;
        }

        @Override
        public Object setAndReturn(Object instance, Object value) throws IOException {
            set(instance, value);
            return instance;
        }
    }

    // Helper POJO for end-to-end forward reference and defect testing
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    public static class Node {
        public int id;
        public Node next;
        public AtomicReference<Node> atomicNext;

        public Node() {}
        public Node(int id) { this.id = id; }
    }

    public static class ContainerWithAtomic {
        public AtomicReference<String> value;
    }

    /* -------------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * ------------------------------------------------------------------------- */

    @Test(timeout = 4000)
    public void testConstructionAndForwardingGetters() {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class);
        MockForwardProperty forward = new MockForwardProperty(new PropertyName("propA"), type);
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class, ObjectIdGenerators.PropertyGenerator.class, null);

        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, info);

        assertEquals("propA", prop.getName());
        assertEquals(42, prop.getCreatorIndex());
        assertNull(prop.getMember());
        assertNotNull(prop.getAnnotation(DummyAnnotation.class));
        assertNull(prop.getAnnotation(Deprecated.class));

        // Test fixAccess
        assertFalse(forward.fixAccessCalled);
        prop.fixAccess(null);
        assertTrue(forward.fixAccessCalled);
    }

    @Test(timeout = 4000)
    public void testWithName() {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class);
        MockForwardProperty forward = new MockForwardProperty(new PropertyName("originalName"), type);
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class, ObjectIdGenerators.PropertyGenerator.class, null);

        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, info);
        SettableBeanProperty renamed = prop.withName(new PropertyName("newName"));

        assertTrue(renamed instanceof ObjectIdReferenceProperty);
        assertEquals("newName", renamed.getName());
        assertEquals("originalName", prop.getName()); // Immutability check
    }

    @Test(timeout = 4000)
    public void testWithValueDeserializerSameInstanceReturnsThis() {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class);
        MockForwardProperty forward = new MockForwardProperty(new PropertyName("prop"), type);
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class, ObjectIdGenerators.PropertyGenerator.class, null);

        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, info);
        // Current deserializer is null
        SettableBeanProperty same = prop.withValueDeserializer(null);
        assertSame(prop, same);
    }

    @Test(timeout = 4000)
    public void testWithValueDeserializerNewInstance() {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class);
        MockForwardProperty forward = new MockForwardProperty(new PropertyName("prop"), type);
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class, ObjectIdGenerators.PropertyGenerator.class, null);

        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, info);
        JsonDeserializer<Object> newDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "deserializedValue";
            }
        };

        SettableBeanProperty modified = prop.withValueDeserializer(newDeser);
        assertNotSame(prop, modified);
        assertSame(newDeser, modified.getValueDeserializer());
    }

    @Test(timeout = 4000)
    public void testWithNullProvider() {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class);
        MockForwardProperty forward = new MockForwardProperty(new PropertyName("prop"), type);
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class, ObjectIdGenerators.PropertyGenerator.class, null);

        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, info);
        NullValueProvider nva = (ctxt) -> "nullReplacement";

        SettableBeanProperty modified = prop.withNullProvider(nva);
        assertNotSame(prop, modified);
        assertSame(prop.getValueDeserializer(), modified.getValueDeserializer());
        assertEquals("nullReplacement", modified.getNullValueProvider().getNullValue(null));
    }

    @Test(timeout = 4000)
    public void testDirectSetAndSetAndReturn() throws IOException {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class);
        MockForwardProperty forward = new MockForwardProperty(new PropertyName("prop"), type);
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class, ObjectIdGenerators.PropertyGenerator.class, null);

        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, info);
        Object target = new Object();

        prop.set(target, "directValue");
        assertSame(target, forward.lastAssignedInstance);
        assertEquals("directValue", forward.lastAssignedValue);

        Object returned = prop.setAndReturn(target, "returnedValue");
        assertSame(target, returned);
        assertEquals("returnedValue", forward.lastAssignedValue);
    }

    /* -------------------------------------------------------------------------
     * Partition B: Forward Reference Resolution & PropertyReferring Inner Class
     * ------------------------------------------------------------------------- */

    @Test(timeout = 4000)
    public void testPropertyReferringSuccessfulResolution() throws IOException {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(Node.class);
        MockForwardProperty forward = new MockForwardProperty(new PropertyName("nodeProp"), type);
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Node.class, ObjectIdGenerators.PropertyGenerator.class, null);

        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, info);
        Node targetPojo = new Node(100);

        JsonLocation loc = new JsonLocation("source", 0, 1, 1);
        UnresolvedForwardReference ufr = new UnresolvedForwardReference(null, "Unresolved test ref", loc, new ReadableObjectId("key-1"));
        ufr.getRoid().appendReferring(new ObjectIdReferenceProperty.PropertyReferring(prop, ufr, Node.class, targetPojo));

        // Directly invoke handleResolvedForwardReference on PropertyReferring
        ObjectIdReferenceProperty.PropertyReferring referring =
                new ObjectIdReferenceProperty.PropertyReferring(prop, ufr, Node.class, targetPojo);

        Node resolvedObj = new Node(200);
        referring.handleResolvedForwardReference("key-1", resolvedObj);

        assertSame(targetPojo, forward.lastAssignedInstance);
        assertSame(resolvedObj, forward.lastAssignedValue);
    }

    @Test(timeout = 4000)
    public void testPropertyReferringWithWrongIdThrowsException() throws IOException {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(Node.class);
        MockForwardProperty forward = new MockForwardProperty(new PropertyName("nodeProp"), type);
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Node.class, ObjectIdGenerators.PropertyGenerator.class, null);

        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, info);
        Node targetPojo = new Node(1);

        JsonLocation loc = new JsonLocation("source", 0, 1, 1);
        UnresolvedForwardReference ufr = new UnresolvedForwardReference(null, "Unresolved", loc, new ReadableObjectId("correct-id"));

        ObjectIdReferenceProperty.PropertyReferring referring =
                new ObjectIdReferenceProperty.PropertyReferring(prop, ufr, Node.class, targetPojo);

        try {
            referring.handleResolvedForwardReference("wrong-id", new Node(2));
            fail("Expected IllegalArgumentException for mismatched ID");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Trying to resolve a forward reference with id [wrong-id]"));
        }
    }

    /* -------------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone & Forward Reference Exception Handling
     * ------------------------------------------------------------------------- */

    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnUnresolvedForwardReferenceWithObjectIdInfo() throws Exception {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(Node.class);
        MockForwardProperty forward = new MockForwardProperty(new PropertyName("nodeProp"), type);
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Node.class, ObjectIdGenerators.PropertyGenerator.class, null);

        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, info);

        JsonLocation loc = new JsonLocation("source", 0, 1, 1);
        final ReadableObjectId roid = new ReadableObjectId("ref-123");
        final UnresolvedForwardReference expectedUfr = new UnresolvedForwardReference(null, "Unresolved", loc, roid);

        JsonDeserializer<Object> throwingDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                throw expectedUfr;
            }
        };

        SettableBeanProperty configuredProp = prop.withValueDeserializer(throwingDeser);

        Node instance = new Node(1);
        Object result = configuredProp.deserializeSetAndReturn(null, null, instance);

        // When unresolved forward reference is caught and objectIdInfo != null, returns null and registers referring
        assertNull(result);
        assertTrue(roid.hasReferringProperties());
    }

    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnUnresolvedForwardReferenceWithoutIdentityInfoThrows() throws Exception {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class);
        MockForwardProperty forward = new MockForwardProperty(new PropertyName("strProp"), type);
        // Null objectIdInfo and deserializer without ObjectIdReader
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);

        JsonLocation loc = new JsonLocation("source", 0, 1, 1);
        final ReadableObjectId roid = new ReadableObjectId("ref-456");
        final UnresolvedForwardReference expectedUfr = new UnresolvedForwardReference(null, "Unresolved", loc, roid);

        JsonDeserializer<Object> throwingDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                throw expectedUfr;
            }
            @Override
            public ObjectIdReader getObjectIdReader() {
                return null;
            }
        };

        SettableBeanProperty configuredProp = prop.withValueDeserializer(throwingDeser);

        try {
            configuredProp.deserializeSetAndReturn(null, null, new Object());
            fail("Expected JsonMappingException when forward reference has no identity info");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Unresolved forward reference but no identity info"));
            assertSame(expectedUfr, e.getCause());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetDelegatesToDeserializeSetAndReturn() throws Exception {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class);
        MockForwardProperty forward = new MockForwardProperty(new PropertyName("strProp"), type);
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, null);

        JsonDeserializer<Object> deser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "hello";
            }
        };

        SettableBeanProperty configuredProp = prop.withValueDeserializer(deser);
        Object instance = new Object();
        configuredProp.deserializeAndSet(null, null, instance);

        assertSame(instance, forward.lastAssignedInstance);
        assertEquals("hello", forward.lastAssignedValue);
    }

    /**
     * Defect Target: Tests null deserialization within nested atomic structures
     * related to [databind#2303] and JDKAtomicTypesDeserTest::testNullWithinNested.
     */
    @Test(timeout = 4000)
    public void testNullWithinNestedAtomicReference() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Deserializing a JSON containing an explicit null into an AtomicReference field
        String json = "{\"value\":null}";
        ContainerWithAtomic container = mapper.readValue(json, ContainerWithAtomic.class);
        assertNotNull("Container should not be null", container);
        // Container's atomic reference should either be null or an AtomicReference holding null
        if (container.value != null) {
            assertNull("Inner value of AtomicReference should be null", container.value.get());
        }
    }

    /**
     * Defect Target: Verify forward references combined with nested object identities.
     */
    @Test(timeout = 4000)
    public void testForwardReferenceResolutionCycle() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"id\":1,\"next\":2} {\"id\":2,\"next\":1}";

        com.fasterxml.jackson.databind.MappingIterator<Node> it =
                mapper.readerFor(Node.class).readValues(json);

        assertTrue(it.hasNext());
        Node first = it.next();
        assertNotNull(first);
        assertEquals(1, first.id);

        assertTrue(it.hasNext());
        Node second = it.next();
        assertNotNull(second);
        assertEquals(2, second.id);

        // Assert cyclic reference successfully stitched by PropertyReferring
        assertSame(first, second.next);
        assertSame(second, first.next);
    }

    /* -------------------------------------------------------------------------
     * Partition D: Boundary Conditions & Null Safety
     * ------------------------------------------------------------------------- */

    @Test(timeout = 4000)
    public void testFixAccessWithNullForwardDoesNotThrow() {
        // Construct with null forward property
        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty((SettableBeanProperty) null, null);
        try {
            prop.fixAccess(null);
        } catch (NullPointerException e) {
            fail("fixAccess should safely guard against null _forward");
        }
    }

    @Test(timeout = 4000)
    public void testNullValueProviderPreservationOnDeserializerChange() {
        JavaType type = com.fasterxml.jackson.databind.type.TypeFactory.defaultInstance().constructType(String.class);
        MockForwardProperty forward = new MockForwardProperty(new PropertyName("prop"), type);
        ObjectIdInfo info = new ObjectIdInfo(new PropertyName("id"), Object.class, ObjectIdGenerators.PropertyGenerator.class, null);

        ObjectIdReferenceProperty prop = new ObjectIdReferenceProperty(forward, info);
        NullValueProvider customNva = (ctxt) -> "customNull";

        SettableBeanProperty withNva = prop.withNullProvider(customNva);
        assertSame(customNva, withNva.getNullValueProvider());

        JsonDeserializer<Object> newDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "val";
            }
        };

        SettableBeanProperty withDeser = withNva.withValueDeserializer(newDeser);
        // Null provider should be preserved
        assertNotNull(withDeser.getNullValueProvider());
    }
}