package com.fasterxml.jackson.databind.deser.impl;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.impl.ObjectIdValueProperty
 *
 * Branches & Logic Targeted:
 * 1. Constructor:
 *    - super(objectIdReader.propertyName, objectIdReader.getIdType(), metadata, objectIdReader.getDeserializer())
 *    - copy-constructors: (src, deser, nva) and (src, newName)
 * 2. withName(PropertyName newName):
 *    - Returns new ObjectIdValueProperty instance with updated PropertyName.
 * 3. withValueDeserializer(JsonDeserializer<?> deser):
 *    - Branch A (_valueDeserializer == deser): returns this (identity preservation).
 *    - Branch B (_valueDeserializer != deser): MUST return new ObjectIdValueProperty with updated deser.
 *      [DEFECT ZONE - databind#2303 / JDKAtomicTypesDeserTest]: If _valueDeserializer == _nullProvider,
 *      replacing _valueDeserializer must keep NullValueProvider in sync with the new deserializer.
 * 4. withNullProvider(NullValueProvider nva):
 *    - Returns new ObjectIdValueProperty instance with updated NullValueProvider.
 * 5. getAnnotation(Class<A>) & getMember():
 *    - Always returns null (virtual property contract).
 * 6. deserializeAndSet(JsonParser, DeserializationContext, Object):
 *    - Delegates directly to deserializeSetAndReturn.
 * 7. deserializeSetAndReturn(JsonParser, DeserializationContext, Object):
 *    - Branch A (p.hasToken(JsonToken.VALUE_NULL)): returns null immediately.
 *    - Branch B (!p.hasToken(JsonToken.VALUE_NULL)):
 *        * Deserializes id using _valueDeserializer.
 *        * Finds/registers ReadableObjectId and calls roid.bindItem(instance).
 *        * Sub-branch B1 (idProp != null): delegates to idProp.setAndReturn(instance, id).
 *        * Sub-branch B2 (idProp == null): returns instance directly.
 * 8. set(Object, Object):
 *    - Delegates directly to setAndReturn.
 * 9. setAndReturn(Object, Object):
 *    - Branch A (_objectIdReader.idProperty == null): throws UnsupportedOperationException.
 *    - Branch B (_objectIdReader.idProperty != null): delegates to idProp.setAndReturn(instance, value).
 */

import java.io.IOException;
import java.lang.annotation.Annotation;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ObjectIdValuePropertyGeminiTest {

    // Concrete test stub for SettableBeanProperty when testing idProperty forwarding
    private static class StubSettableProperty extends SettableBeanProperty {
        private static final long serialVersionUID = 1L;
        Object lastInstance;
        Object lastValue;

        StubSettableProperty(PropertyName name, JavaType type) {
            super(name, type, PropertyMetadata.STD_OPTIONAL, null);
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
            return null;
        }

        @Override
        public AnnotatedMember getMember() {
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
            this.lastInstance = instance;
            this.lastValue = value;
        }

        @Override
        public Object setAndReturn(Object instance, Object value) {
            this.lastInstance = instance;
            this.lastValue = value;
            return "setAndReturn:" + value;
        }
    }

    private static class DummyDeserializer extends JsonDeserializer<Object> {
        private final Object _valueToReturn;

        DummyDeserializer(Object valueToReturn) {
            this._valueToReturn = valueToReturn;
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            return _valueToReturn;
        }
    }

    private ObjectIdReader createReader(SettableBeanProperty idProp, JsonDeserializer<?> deser) {
        JavaType idType = TypeFactory.defaultInstance().constructType(String.class);
        return ObjectIdReader.construct(
                idType,
                PropertyName.construct("idProp"),
                new ObjectIdGenerators.IntSequenceGenerator(),
                deser,
                idProp,
                new SimpleObjectIdResolver()
        );
    }

    private DefaultDeserializationContext createCtxt(ObjectMapper mapper, JsonParser p) {
        DefaultDeserializationContext orig = (DefaultDeserializationContext) mapper.getDeserializationContext();
        return orig.createInstance(mapper.getDeserializationConfig(), p, mapper.getInjectableValues());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicPropertiesAndAccessors() {
        DummyDeserializer deser = new DummyDeserializer("id123");
        ObjectIdReader reader = createReader(null, deser);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        assertEquals("idProp", prop.getName());
        assertEquals(PropertyName.construct("idProp"), prop.getFullName());
        assertNull(prop.getAnnotation(Override.class));
        assertNull(prop.getMember());
        assertSame(deser, prop.getValueDeserializer());
        assertTrue(prop.isRequired());
    }

    @Test(timeout = 4000)
    public void testWithName() {
        DummyDeserializer deser = new DummyDeserializer("id123");
        ObjectIdReader reader = createReader(null, deser);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        PropertyName newName = PropertyName.construct("renamedId");
        SettableBeanProperty renamed = prop.withName(newName);

        assertNotNull(renamed);
        assertTrue(renamed instanceof ObjectIdValueProperty);
        assertEquals("renamedId", renamed.getName());
        assertSame(deser, renamed.getValueDeserializer());
    }

    @Test(timeout = 4000)
    public void testWithValueDeserializerSameInstanceReturnsThis() {
        DummyDeserializer deser = new DummyDeserializer("id123");
        ObjectIdReader reader = createReader(null, deser);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        SettableBeanProperty same = prop.withValueDeserializer(deser);
        assertSame(prop, same);
    }

    @Test(timeout = 4000)
    public void testWithNullProvider() {
        DummyDeserializer deser = new DummyDeserializer("id123");
        ObjectIdReader reader = createReader(null, deser);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        NullValueProvider customNva = new NullValueProvider() {
            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return "customNull";
            }
        };

        SettableBeanProperty withNva = prop.withNullProvider(customNva);
        assertNotNull(withNva);
        assertSame(customNva, withNva.getNullValueProvider());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWhenTokenIsNull() throws IOException {
        DummyDeserializer deser = new DummyDeserializer("id123");
        ObjectIdReader reader = createReader(null, deser);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken(); // Move to VALUE_NULL
        assertEquals(JsonToken.VALUE_NULL, parser.currentToken());

        DefaultDeserializationContext ctxt = createCtxt(mapper, parser);
        Object target = new Object();
        Object result = prop.deserializeSetAndReturn(parser, ctxt, target);

        assertNull("Should return null on VALUE_NULL token per databind#742", result);
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetDelegatesToDeserializeSetAndReturn() throws IOException {
        DummyDeserializer deser = new DummyDeserializer("id123");
        ObjectIdReader reader = createReader(null, deser);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("null");
        parser.nextToken();

        DefaultDeserializationContext ctxt = createCtxt(mapper, parser);
        Object target = new Object();
        // deserializeAndSet returns void, verifies no exceptions on null token
        prop.deserializeAndSet(parser, ctxt, target);
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWithoutIdProperty() throws IOException {
        DummyDeserializer deser = new DummyDeserializer("generated-id");
        ObjectIdReader reader = createReader(null, deser);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("\"someInput\"");
        parser.nextToken();

        DefaultDeserializationContext ctxt = createCtxt(mapper, parser);
        Object target = new Object();
        Object result = prop.deserializeSetAndReturn(parser, ctxt, target);

        assertSame("When idProperty is null, result should be the instance itself", target, result);
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnWithIdProperty() throws IOException {
        DummyDeserializer deser = new DummyDeserializer("generated-id");
        JavaType idType = TypeFactory.defaultInstance().constructType(String.class);
        StubSettableProperty idProp = new StubSettableProperty(PropertyName.construct("idProp"), idType);

        ObjectIdReader reader = createReader(idProp, deser);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("\"idPayload\"");
        parser.nextToken();

        DefaultDeserializationContext ctxt = createCtxt(mapper, parser);
        Object target = new Object();
        Object result = prop.deserializeSetAndReturn(parser, ctxt, target);

        assertEquals("setAndReturn:generated-id", result);
        assertSame(target, idProp.lastInstance);
        assertEquals("generated-id", idProp.lastValue);
        parser.close();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind#2303 / Defects4J)
    // =========================================================================

    /**
     * Target Defect: [databind#2303]
     * When ObjectIdValueProperty is constructed, its _nullProvider and _valueDeserializer
     * both point to the same JsonDeserializer. When withValueDeserializer(newDeser) is called,
     * the new instance MUST keep _nullProvider in sync with the new deserializer if they were identical.
     */
    @Test(timeout = 4000)
    public void testDefectKeepDeserializerAndNullProviderInSync() {
        DummyDeserializer initialDeser = new DummyDeserializer("init");
        ObjectIdReader reader = createReader(null, initialDeser);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        // Precondition check: initially, _nullProvider and _valueDeserializer are identical
        assertSame(prop.getValueDeserializer(), prop.getNullValueProvider());

        DummyDeserializer newDeser = new DummyDeserializer("new");
        SettableBeanProperty modified = prop.withValueDeserializer(newDeser);

        assertSame("Value deserializer must be updated", newDeser, modified.getValueDeserializer());
        assertSame("Null provider must be kept in sync with new value deserializer as per [databind#2303]",
                newDeser, modified.getNullValueProvider());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testSetWithoutIdPropertyThrowsException() throws IOException {
        ObjectIdReader reader = createReader(null, new DummyDeserializer("val"));
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        prop.set(new Object(), "anyId");
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testSetAndReturnWithoutIdPropertyThrowsException() throws IOException {
        ObjectIdReader reader = createReader(null, new DummyDeserializer("val"));
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);
        prop.setAndReturn(new Object(), "anyId");
    }

    @Test(timeout = 4000)
    public void testSetWithIdPropertyDelegatesProperly() throws IOException {
        JavaType idType = TypeFactory.defaultInstance().constructType(String.class);
        StubSettableProperty idProp = new StubSettableProperty(PropertyName.construct("idProp"), idType);

        ObjectIdReader reader = createReader(idProp, new DummyDeserializer("val"));
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        Object target = new Object();
        prop.set(target, "directValue");

        assertSame(target, idProp.lastInstance);
        assertEquals("directValue", idProp.lastValue);
    }

    @Test(timeout = 4000)
    public void testSetAndReturnWithIdPropertyDelegatesProperly() throws IOException {
        JavaType idType = TypeFactory.defaultInstance().constructType(String.class);
        StubSettableProperty idProp = new StubSettableProperty(PropertyName.construct("idProp"), idType);

        ObjectIdReader reader = createReader(idProp, new DummyDeserializer("val"));
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_OPTIONAL);

        Object target = new Object();
        Object ret = prop.setAndReturn(target, "returnValue");

        assertEquals("setAndReturn:returnValue", ret);
        assertSame(target, idProp.lastInstance);
        assertEquals("returnValue", idProp.lastValue);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testChainedTransformationsPreserveState() {
        DummyDeserializer deser1 = new DummyDeserializer("v1");
        DummyDeserializer deser2 = new DummyDeserializer("v2");
        ObjectIdReader reader = createReader(null, deser1);
        ObjectIdValueProperty prop = new ObjectIdValueProperty(reader, PropertyMetadata.STD_REQUIRED);

        SettableBeanProperty p2 = prop.withName(PropertyName.construct("newName"));
        SettableBeanProperty p3 = p2.withValueDeserializer(deser2);

        assertEquals("newName", p3.getName());
        assertSame(deser2, p3.getValueDeserializer());
        assertTrue(p3.isRequired());
    }
}