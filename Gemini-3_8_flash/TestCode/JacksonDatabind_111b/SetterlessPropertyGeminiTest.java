package com.fasterxml.jackson.databind.deser.impl;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.impl.SetterlessProperty
 *
 * Targeted Decision Branches & Conditions:
 * 1. Constructor & Copy Constructors:
 *    - (propDef, type, typeDeser, contextAnnotations, method) initialization of _annotated and _getter.
 *    - (src, deser, nva) copying and state retention.
 *    - (src, newName) copying with new PropertyName.
 * 2. withName(PropertyName):
 *    - Branch: returns a new SetterlessProperty with updated name, preserving state.
 * 3. withValueDeserializer(JsonDeserializer<?>):
 *    - Branch A: deser == _valueDeserializer -> return this (identity optimization).
 *    - Branch B: deser != _valueDeserializer -> return new SetterlessProperty.
 *    - [Defect databind#2303 / testNullWithinNested]: synchronization of NullValueProvider when
 *      _nullProvider was equal to _valueDeserializer.
 * 4. withNullProvider(NullValueProvider):
 *    - Branch: return new SetterlessProperty with new NullValueProvider.
 * 5. fixAccess(DeserializationConfig):
 *    - Branch A: OVERRIDE_PUBLIC_ACCESS_MODIFIERS enabled -> changes method accessibility.
 *    - Branch B: OVERRIDE_PUBLIC_ACCESS_MODIFIERS disabled.
 * 6. getAnnotation(Class<A>) & getMember():
 *    - Delegation to _annotated.
 * 7. deserializeAndSet(JsonParser, DeserializationContext, Object):
 *    - Branch 1: parser current token == JsonToken.VALUE_NULL -> early return, no modification.
 *    - Branch 2: _valueTypeDeserializer != null -> reportBadDefinition (unsupported typed deser).
 *    - Branch 3: getter invocation throws Exception -> catch and _throwAsIOE(p, e).
 *    - Branch 4: getter returns null (toModify == null) -> reportBadDefinition.
 *    - Branch 5: normal path -> invoke _valueDeserializer.deserialize(p, ctxt, toModify).
 * 8. deserializeSetAndReturn(JsonParser, DeserializationContext, Object):
 *    - Invokes deserializeAndSet and returns instance.
 * 9. set(Object, Object) & setAndReturn(Object, Object):
 *    - Strictly throws UnsupportedOperationException with property name.
 */

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.NullValueProvider;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.std.NullifyingDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Annotations;

public class SetterlessPropertyGeminiTest {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestMarker {
        String value() default "marker";
    }

    public static class CollectionContainer {
        private final List<String> items = new ArrayList<String>();

        @TestMarker("listMethod")
        public List<String> getItems() {
            return items;
        }
    }

    public static class MapContainer {
        private final Map<String, Object> values = new HashMap<String, Object>();

        public Map<String, Object> getValues() {
            return values;
        }
    }

    public static class NullReturningContainer {
        public List<String> getItems() {
            return null;
        }
    }

    public static class ThrowingContainer {
        public List<String> getItems() {
            throw new IllegalStateException("Simulated getter explosion");
        }
    }

    static class PrivateGetterContainer {
        private final List<String> secrets = new ArrayList<String>();

        private List<String> getSecrets() {
            return secrets;
        }
    }

    public static class NestedAtomicHolder {
        private final AtomicReference<String> ref = new AtomicReference<String>("initial");

        public AtomicReference<String> getRef() {
            return ref;
        }
    }

    private SetterlessProperty createProperty(Class<?> containerClass, String propName, TypeDeserializer typeDeser) {
        ObjectMapper mapper = new ObjectMapper();
        JavaType beanType = mapper.constructType(containerClass);
        BeanDescription beanDesc = mapper.getDeserializationConfig().introspect(beanType);
        BeanPropertyDefinition targetProp = null;
        for (BeanPropertyDefinition prop : beanDesc.findProperties()) {
            if (prop.getName().equals(propName)) {
                targetProp = prop;
                break;
            }
        }
        assertNotNull("Property '" + propName + "' not found on " + containerClass.getName(), targetProp);
        AnnotatedMethod getter = targetProp.getGetter();
        assertNotNull("Getter not found for property '" + propName + "'", getter);

        JavaType propType = getter.getType();
        Annotations annotations = getter.getAnnotationMap();
        return new SetterlessProperty(targetProp, propType, typeDeser, annotations, getter);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructionAndIntrospection() {
        SetterlessProperty prop = createProperty(CollectionContainer.class, "items", null);

        assertEquals("items", prop.getName());
        assertNotNull(prop.getMember());
        assertEquals("getItems", prop.getMember().getName());

        TestMarker marker = prop.getAnnotation(TestMarker.class);
        assertNotNull("Annotation on getter should be accessible", marker);
        assertEquals("listMethod", marker.value());

        assertNull("Missing annotation should return null", prop.getAnnotation(Deprecated.class));
    }

    @Test(timeout = 4000)
    public void testWithName() {
        SetterlessProperty original = createProperty(CollectionContainer.class, "items", null);
        PropertyName newName = new PropertyName("renamedItems");

        SettableBeanProperty renamed = original.withName(newName);
        assertNotSame(original, renamed);
        assertTrue(renamed instanceof SetterlessProperty);
        assertEquals("renamedItems", renamed.getName());
        assertEquals(original.getMember(), renamed.getMember());
    }

    @Test(timeout = 4000)
    public void testWithValueDeserializerIdentity() {
        SetterlessProperty prop = createProperty(CollectionContainer.class, "items", null);
        JsonDeserializer<?> deser = NullifyingDeserializer.instance;

        SettableBeanProperty updated = prop.withValueDeserializer(deser);
        assertNotSame(prop, updated);
        assertEquals(deser, updated.getValueDeserializer());

        SettableBeanProperty identity = updated.withValueDeserializer(deser);
        assertSame("Passing same deserializer should return this instance", updated, identity);
    }

    @Test(timeout = 4000)
    public void testWithNullProvider() {
        SetterlessProperty prop = createProperty(CollectionContainer.class, "items", null);
        NullValueProvider nva = NullifyingDeserializer.instance;

        SettableBeanProperty updated = prop.withNullProvider(nva);
        assertNotSame(prop, updated);
        assertSame(nva, updated.getNullValueProvider());
    }

    @Test(timeout = 4000)
    public void testFixAccessFeatureSwitch() {
        SetterlessProperty prop = createProperty(PrivateGetterContainer.class, "secrets", null);
        ObjectMapper mapper = new ObjectMapper();

        mapper.disable(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
        prop.fixAccess(mapper.getDeserializationConfig());

        mapper.enable(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS);
        prop.fixAccess(mapper.getDeserializationConfig());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializeAndSetNullTokenReturnsEarly() throws Exception {
        SetterlessProperty prop = createProperty(CollectionContainer.class, "items", null);
        CollectionContainer container = new CollectionContainer();
        container.getItems().add("pre-existing");

        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.createParser("null");
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());

        DeserializationContext ctxt = mapper.getDeserializationContext();
        prop.deserializeAndSet(p, ctxt, container);

        assertEquals("Pre-existing list should be unchanged on null token", 1, container.getItems().size());
        assertEquals("pre-existing", container.getItems().get(0));
        p.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeSetAndReturnReturnsInstance() throws Exception {
        SetterlessProperty prop = createProperty(CollectionContainer.class, "items", null);
        ObjectMapper mapper = new ObjectMapper();
        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, String.class);
        JsonDeserializer<Object> deser = mapper.getDeserializationContext().findRootValueDeserializer(listType);
        SettableBeanProperty propWithDeser = prop.withValueDeserializer(deser);

        CollectionContainer container = new CollectionContainer();
        JsonParser p = mapper.createParser("[\"alpha\", \"beta\"]");
        p.nextToken();

        DeserializationContext ctxt = mapper.getDeserializationContext();
        Object result = propWithDeser.deserializeSetAndReturn(p, ctxt, container);

        assertSame(container, result);
        assertEquals(2, container.getItems().size());
        assertEquals("alpha", container.getItems().get(0));
        assertEquals("beta", container.getItems().get(1));
        p.close();
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind#2303 & testNullWithinNested)
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullValueProviderInSyncWithValueDeserializer() {
        SetterlessProperty prop = createProperty(CollectionContainer.class, "items", null);
        JsonDeserializer<?> initialDeser = NullifyingDeserializer.instance;
        SettableBeanProperty prop1 = prop.withValueDeserializer(initialDeser);
        SettableBeanProperty propWithSyncedNull = prop1.withNullProvider(initialDeser);

        assertSame(initialDeser, propWithSyncedNull.getValueDeserializer());
        assertSame(initialDeser, propWithSyncedNull.getNullValueProvider());

        JsonDeserializer<?> newDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return null;
            }
        };

        SettableBeanProperty propUpdated = propWithSyncedNull.withValueDeserializer(newDeser);

        assertSame("Value deserializer must be updated to newDeser", newDeser, propUpdated.getValueDeserializer());
        assertSame("NullValueProvider must stay in sync when it matched old value deserializer (databind#2303)",
                newDeser, propUpdated.getNullValueProvider());
    }

    @Test(timeout = 4000)
    public void testNullWithinNestedSetterlessAtomicReference() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"ref\": null}";
        NestedAtomicHolder holder = mapper.readValue(json, NestedAtomicHolder.class);

        assertNotNull(holder);
        assertNotNull(holder.getRef());
        assertNull("Nested atomic reference should hold null after deserializing null", holder.getRef().get());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializeWithTypeThrowsBadDefinition() throws Exception {
        JavaType baseType = TypeFactory.defaultInstance().constructType(List.class);
        ClassNameIdResolver idRes = new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
        TypeDeserializer typeDeser = new AsPropertyTypeDeserializer(baseType, idRes, "@type", false, baseType);

        SetterlessProperty prop = createProperty(CollectionContainer.class, "items", typeDeser);
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.createParser("[\"item\"]");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            prop.deserializeAndSet(p, ctxt, new CollectionContainer());
            fail("Expected JsonMappingException due to unsupported typed deserializer with setterless property");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("no way to handle typed deser with setterless yet"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWhenGetterReturnsNullThrowsBadDefinition() throws Exception {
        SetterlessProperty prop = createProperty(NullReturningContainer.class, "items", null);
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.createParser("[\"item\"]");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            prop.deserializeAndSet(p, ctxt, new NullReturningContainer());
            fail("Expected JsonMappingException because get method returned null");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("get method returned null"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeWhenGetterThrowsPropagatesAsIOE() throws Exception {
        SetterlessProperty prop = createProperty(ThrowingContainer.class, "items", null);
        ObjectMapper mapper = new ObjectMapper();
        JsonParser p = mapper.createParser("[\"item\"]");
        p.nextToken();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            prop.deserializeAndSet(p, ctxt, new ThrowingContainer());
            fail("Expected exception when getter throws");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Simulated getter explosion"));
        } finally {
            p.close();
        }
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetThrowsUnsupportedOperationException() throws Exception {
        SetterlessProperty prop = createProperty(CollectionContainer.class, "items", null);
        prop.set(new CollectionContainer(), new ArrayList<String>());
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSetAndReturnThrowsUnsupportedOperationException() throws Exception {
        SetterlessProperty prop = createProperty(CollectionContainer.class, "items", null);
        prop.setAndReturn(new CollectionContainer(), new ArrayList<String>());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testMapContainerFullRoundTrip() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MapContainer container = mapper.readValue("{\"values\":{\"key1\":\"value1\",\"key2\":100}}", MapContainer.class);

        assertNotNull(container);
        assertNotNull(container.getValues());
        assertEquals("value1", container.getValues().get("key1"));
        assertEquals(100, container.getValues().get("key2"));
    }

    @Test(timeout = 4000)
    public void testToStringAndPropertyIndex() {
        SetterlessProperty prop = createProperty(CollectionContainer.class, "items", null);
        String desc = prop.toString();
        assertNotNull(desc);
        assertTrue(desc.contains("items"));
        assertEquals(-1, prop.getPropertyIndex());
    }
}