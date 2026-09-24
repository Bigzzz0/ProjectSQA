package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets com.fasterxml.jackson.databind.deser.CreatorProperty:
 * 
 * 1. Defect [databind#2303 / JDKAtomicTypesDeserTest::testNullWithinNested]:
 *    - In `withValueDeserializer(JsonDeserializer<?> deser)`:
 *      When `_valueDeserializer == _nullProvider`, changing `_valueDeserializer` must keep
 *      the `_nullProvider` in-sync so that contextualized null value providers (e.g. for
 *      AtomicReference, Optional, etc.) correctly provide non-null container defaults when null is encountered.
 * 
 * 2. Method Decision & Branch Coverage:
 *    - `withName(PropertyName)`: copy-constructor state propagation.
 *    - `withValueDeserializer(JsonDeserializer<?> deser)`:
 *      * Branch: `_valueDeserializer == deser` (returns `this`)
 *      * Branch: `_valueDeserializer != deser` (creates new CreatorProperty with updated deser and synced null provider)
 *    - `withNullProvider(NullValueProvider nva)`: updates null provider.
 *    - `fixAccess(DeserializationConfig)`:
 *      * Branch: `_fallbackSetter != null` -> delegates to fallbackSetter.fixAccess
 *      * Branch: `_fallbackSetter == null` -> NOP
 *    - `setFallbackSetter(SettableBeanProperty)`: mutator and subsequent delegation.
 *    - `markAsIgnorable()` / `isIgnorable()`: flag transition (false -> true).
 *    - `findInjectableValue(DeserializationContext, Object)`:
 *      * Branch: `_injectableValueId == null` -> reports bad definition
 *      * Branch: `_injectableValueId != null` -> delegates to context.findInjectableValue
 *    - `inject(DeserializationContext, Object)`: locates injectable and calls `set(beanInstance, val)`.
 *    - `getAnnotation(Class<A>)`:
 *      * Branch: `_annotated == null` -> returns null
 *      * Branch: `_annotated != null` -> delegates to annotated.getAnnotation
 *    - `getMember()`: returns `_annotated`.
 *    - `getCreatorIndex()`: returns `_creatorIndex`.
 *    - `getInjectableValueId()`: returns `_injectableValueId`.
 *    - `toString()`: formatting check.
 *    - Mutator methods (`deserializeAndSet`, `deserializeSetAndReturn`, `set`, `setAndReturn`):
 *      * Branch: `_fallbackSetter == null` -> `_verifySetter()` throws `InvalidDefinitionException` (or reports bad def)
 *      * Branch: `_fallbackSetter != null` -> delegates to fallbackSetter
 */
public class CreatorPropertyGeminiTest {

    @Retention(RetentionPolicy.RUNTIME)
    private @interface MarkerAnnotation {
        String value() default "";
    }

    private static class SampleBean {
        String name;
        int age;

        public void setName(String n) { this.name = n; }
        public String getName() { return name; }
    }

    private static class AtomicContainer {
        public final AtomicReference<String> ref;

        @JsonCreator
        public AtomicContainer(@JsonProperty("ref") AtomicReference<String> ref) {
            this.ref = ref;
        }
    }

    private static class NestedAtomicContainer {
        public final AtomicContainer nested;

        @JsonCreator
        public NestedAtomicContainer(@JsonProperty("nested") AtomicContainer nested) {
            this.nested = nested;
        }
    }

    private CreatorProperty createBasicProperty(String name, JavaType type, int index, Object injectId) {
        PropertyName propName = new PropertyName(name);
        return new CreatorProperty(
                propName,
                type,
                null,
                null,
                null,
                null,
                index,
                injectId,
                PropertyMetadata.STD_REQUIRED_OR_OPTIONAL
        );
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructionAndBasicGetters() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("propA", type, 2, "injectKey1");

        assertEquals("propA", prop.getName());
        assertEquals(type, prop.getType());
        assertEquals(2, prop.getCreatorIndex());
        assertEquals("injectKey1", prop.getInjectableValueId());
        assertNull(prop.getMember());
        assertFalse(prop.isIgnorable());

        String str = prop.toString();
        assertTrue(str.contains("propA"));
        assertTrue(str.contains("injectKey1"));
    }

    @Test(timeout = 4000)
    public void testWithNameStatePropagation() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("propOld", type, 1, "id123");
        prop.markAsIgnorable();

        SettableBeanProperty renamed = prop.withName(new PropertyName("propNew"));
        assertTrue(renamed instanceof CreatorProperty);
        CreatorProperty cpRenamed = (CreatorProperty) renamed;

        assertEquals("propNew", cpRenamed.getName());
        assertEquals(1, cpRenamed.getCreatorIndex());
        assertEquals("id123", cpRenamed.getInjectableValueId());
        assertTrue(cpRenamed.isIgnorable());
    }

    @Test(timeout = 4000)
    public void testMarkAsIgnorable() {
        JavaType type = TypeFactory.defaultInstance().constructType(Integer.class);
        CreatorProperty prop = createBasicProperty("item", type, 0, null);

        assertFalse(prop.isIgnorable());
        prop.markAsIgnorable();
        assertTrue(prop.isIgnorable());
    }

    @Test(timeout = 4000)
    public void testWithValueDeserializerSameInstance() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("item", type, 0, null);
        JsonDeserializer<Object> deser = new NullifyingDeserializer();
        SettableBeanProperty propWithDeser = prop.withValueDeserializer(deser);

        assertSame(propWithDeser, propWithDeser.withValueDeserializer(deser));
    }

    @Test(timeout = 4000)
    public void testWithNullProvider() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("item", type, 0, null);
        NullValueProvider nva = new NullValueProvider() {
            @Override
            public Object getNullValue(DeserializationContext ctxt) {
                return "customNull";
            }
        };

        SettableBeanProperty updated = prop.withNullProvider(nva);
        assertTrue(updated instanceof CreatorProperty);
        assertSame(nva, updated.getNullValueProvider());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & AnnotatedParameter Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetAnnotationWhenAnnotatedIsNull() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("noAnnotated", type, -1, null);

        assertNull(prop.getMember());
        assertNull(prop.getAnnotation(MarkerAnnotation.class));
    }

    @Test(timeout = 4000)
    public void testGetAnnotationWhenAnnotatedIsPresent() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        AnnotationMap map = new AnnotationMap();
        MarkerAnnotation marker = new MarkerAnnotation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return MarkerAnnotation.class;
            }
            @Override
            public String value() {
                return "testVal";
            }
        };
        map.add(marker);
        AnnotatedParameter param = new AnnotatedParameter(null, type, null, map, 0);

        CreatorProperty prop = new CreatorProperty(
                new PropertyName("annotatedProp"),
                type,
                null,
                null,
                null,
                param,
                0,
                null,
                PropertyMetadata.STD_OPTIONAL
        );

        assertSame(param, prop.getMember());
        MarkerAnnotation found = prop.getAnnotation(MarkerAnnotation.class);
        assertNotNull(found);
        assertEquals("testVal", found.value());
    }

    @Test(timeout = 4000)
    public void testCreatorIndexBoundaries() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty minProp = createBasicProperty("min", type, Integer.MIN_VALUE, null);
        assertEquals(Integer.MIN_VALUE, minProp.getCreatorIndex());

        CreatorProperty maxProp = createBasicProperty("max", type, Integer.MAX_VALUE, null);
        assertEquals(Integer.MAX_VALUE, maxProp.getCreatorIndex());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind#2303 / testNullWithinNested)
    // =========================================================================

    @Test(timeout = 4000)
    public void testWithValueDeserializerKeepsNullProviderInSync() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("syncProp", type, 0, null);

        class SyncTestDeserializer extends JsonDeserializer<Object> {
            private final String val;
            SyncTestDeserializer(String val) { this.val = val; }
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) { return val; }
            @Override
            public Object getNullValue(DeserializationContext ctxt) { return val; }
        }

        SyncTestDeserializer deser1 = new SyncTestDeserializer("first");
        SettableBeanProperty prop1 = prop.withValueDeserializer(deser1);
        assertSame("Initial deserializer should act as null provider", deser1, prop1.getNullValueProvider());

        SyncTestDeserializer deser2 = new SyncTestDeserializer("second");
        SettableBeanProperty prop2 = prop1.withValueDeserializer(deser2);

        // Ground-truth defect target:
        // When _valueDeserializer == _nullProvider, withValueDeserializer(deser) MUST keep them in sync
        assertSame("New deserializer must update _nullProvider when VD == NVP", deser2, prop2.getNullValueProvider());
    }

    @Test(timeout = 4000)
    public void testNullWithinNestedAtomicTypes() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        // Jackson Issue 2303: Deserializing nested container with creator and null reference
        String json = "{\"nested\":{\"ref\":null}}";
        NestedAtomicContainer result = mapper.readValue(json, NestedAtomicContainer.class);

        assertNotNull("Nested container should be created", result);
        assertNotNull("Inner container should be created", result.nested);
        assertNotNull("AtomicReference must not be null when null is provided in JSON", result.nested.ref);
        assertNull("Inner AtomicReference value should be null", result.nested.ref.get());
    }

    // =========================================================================
    // Partition D: Fallback Setter & Exception / Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testFallbackSetterMissingThrowsOnSet() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("missingSetter", type, 0, null);
        SampleBean bean = new SampleBean();

        try {
            prop.set(bean, "value");
            fail("Expected InvalidDefinitionException due to missing fallback setter");
        } catch (InvalidDefinitionException e) {
            assertTrue(e.getMessage().contains("No fallback setter/field defined"));
            assertTrue(e.getMessage().contains("missingSetter"));
        }
    }

    @Test(timeout = 4000)
    public void testFallbackSetterMissingThrowsOnSetAndReturn() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("missingSetter", type, 0, null);
        SampleBean bean = new SampleBean();

        try {
            prop.setAndReturn(bean, "value");
            fail("Expected InvalidDefinitionException due to missing fallback setter");
        } catch (InvalidDefinitionException e) {
            assertTrue(e.getMessage().contains("No fallback setter/field defined"));
        }
    }

    @Test(timeout = 4000)
    public void testFallbackSetterMissingThrowsOnDeserializeAndSet() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("missingSetter", type, 0, null);
        SampleBean bean = new SampleBean();

        try {
            prop.deserializeAndSet(null, null, bean);
            fail("Expected InvalidDefinitionException due to missing fallback setter");
        } catch (InvalidDefinitionException e) {
            assertTrue(e.getMessage().contains("No fallback setter/field defined"));
        }
    }

    @Test(timeout = 4000)
    public void testFallbackSetterMissingThrowsOnDeserializeSetAndReturn() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("missingSetter", type, 0, null);
        SampleBean bean = new SampleBean();

        try {
            prop.deserializeSetAndReturn(null, null, bean);
            fail("Expected InvalidDefinitionException due to missing fallback setter");
        } catch (InvalidDefinitionException e) {
            assertTrue(e.getMessage().contains("No fallback setter/field defined"));
        }
    }

    @Test(timeout = 4000)
    public void testDelegationToFallbackSetter() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("name", type, 0, null);

        final boolean[] flag = new boolean[2];
        SettableBeanProperty mockFallback = new SettableBeanProperty(
                new PropertyName("name"), type, PropertyMetadata.STD_REQUIRED, new NullifyingDeserializer()) {
            @Override
            public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) { return this; }
            @Override
            public SettableBeanProperty withName(PropertyName newName) { return this; }
            @Override
            public SettableBeanProperty withNullProvider(NullValueProvider nva) { return this; }
            @Override
            public <A extends Annotation> A getAnnotation(Class<A> acls) { return null; }
            @Override
            public AnnotatedMember getMember() { return null; }
            @Override
            public void set(Object instance, Object value) {
                ((SampleBean) instance).setName((String) value);
            }
            @Override
            public Object setAndReturn(Object instance, Object value) {
                ((SampleBean) instance).setName((String) value);
                return instance;
            }
            @Override
            public void fixAccess(DeserializationConfig config) {
                flag[0] = true;
            }
        };

        prop.setFallbackSetter(mockFallback);
        SampleBean bean = new SampleBean();

        // Test set
        prop.set(bean, "Alice");
        assertEquals("Alice", bean.getName());

        // Test setAndReturn
        Object returned = prop.setAndReturn(bean, "Bob");
        assertSame(bean, returned);
        assertEquals("Bob", bean.getName());

        // Test fixAccess
        prop.fixAccess(null);
        assertTrue(flag[0]);
    }

    @Test(timeout = 4000)
    public void testFixAccessWithNullFallbackSetterIsNoOp() {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("test", type, 0, null);
        // Should execute cleanly without throwing NPE
        prop.fixAccess(null);
    }

    // =========================================================================
    // Partition E: Injection Mechanics
    // =========================================================================

    @Test(timeout = 4000)
    public void testFindInjectableValueMissingThrows() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("noInject", type, 0, null);
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();

        try {
            prop.findInjectableValue(ctxt, new SampleBean());
            fail("Expected JsonMappingException because injectableValueId is null");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("has no injectable value id configured"));
            assertTrue(e.getMessage().contains("noInject"));
        }
    }

    @Test(timeout = 4000)
    public void testFindInjectableValueAndInjectSuccessful() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("name", type, 0, "sampleInjectId");

        final boolean[] fallbackSetCalled = new boolean[1];
        SettableBeanProperty fallback = new SettableBeanProperty(
                new PropertyName("name"), type, PropertyMetadata.STD_REQUIRED, new NullifyingDeserializer()) {
            @Override public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) { return this; }
            @Override public SettableBeanProperty withName(PropertyName newName) { return this; }
            @Override public SettableBeanProperty withNullProvider(NullValueProvider nva) { return this; }
            @Override public <A extends Annotation> A getAnnotation(Class<A> acls) { return null; }
            @Override public AnnotatedMember getMember() { return null; }
            @Override
            public void set(Object instance, Object value) {
                fallbackSetCalled[0] = true;
                ((SampleBean) instance).setName((String) value);
            }
            @Override
            public Object setAndReturn(Object instance, Object value) {
                set(instance, value);
                return instance;
            }
        };
        prop.setFallbackSetter(fallback);

        InjectableValues injectValues = new InjectableValues.Std().addValue("sampleInjectId", "InjectedName");
        ObjectMapper mapper = new ObjectMapper();
        DeserializationContext ctxt = mapper.getDeserializationContext();
        ctxt = mapper.createDeserializationContext(mapper.getFactory().createParser("{}"));
        ctxt = ctxt.assignAndReturnRootValueDeserializer(new NullifyingDeserializer());

        // Use ObjectMapper reading with InjectableValues to trigger injection
        SampleBean bean = new SampleBean();
        Object injected = mapper.reader(injectValues).forType(Object.class).createDeserializationContext(
                mapper.getFactory().createParser("{}")
        );
        assertTrue(injected instanceof DeserializationContext);
        DeserializationContext activeCtxt = (DeserializationContext) injected;

        Object foundValue = prop.findInjectableValue(activeCtxt, bean);
        assertEquals("InjectedName", foundValue);

        prop.inject(activeCtxt, bean);
        assertTrue(fallbackSetCalled[0]);
        assertEquals("InjectedName", bean.getName());
    }

    @Test(timeout = 4000)
    public void testDeserializeAndSetWithFallbackSetter() throws IOException {
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        CreatorProperty prop = createBasicProperty("name", type, 0, null);

        SettableBeanProperty fallback = new SettableBeanProperty(
                new PropertyName("name"), type, PropertyMetadata.STD_REQUIRED, new NullifyingDeserializer()) {
            @Override public SettableBeanProperty withValueDeserializer(JsonDeserializer<?> deser) { return this; }
            @Override public SettableBeanProperty withName(PropertyName newName) { return this; }
            @Override public SettableBeanProperty withNullProvider(NullValueProvider nva) { return this; }
            @Override public <A extends Annotation> A getAnnotation(Class<A> acls) { return null; }
            @Override public AnnotatedMember getMember() { return null; }
            @Override
            public void set(Object instance, Object value) {
                ((SampleBean) instance).setName((String) value);
            }
            @Override
            public Object setAndReturn(Object instance, Object value) {
                set(instance, value);
                return instance;
            }
        };
        prop.setFallbackSetter(fallback);

        JsonDeserializer<Object> valDeser = new JsonDeserializer<Object>() {
            @Override
            public Object deserialize(JsonParser p, DeserializationContext ctxt) {
                return "deserializedValue";
            }
        };
        prop = (CreatorProperty) prop.withValueDeserializer(valDeser);

        ObjectMapper mapper = new ObjectMapper();
        JsonParser parser = mapper.getFactory().createParser("\"someJson\"");
        parser.nextToken(); // consume to start token
        DeserializationContext ctxt = mapper.getDeserializationContext();

        SampleBean target = new SampleBean();
        prop.deserializeAndSet(parser, ctxt, target);
        assertEquals("deserializedValue", target.getName());

        JsonParser parser2 = mapper.getFactory().createParser("\"anotherJson\"");
        parser2.nextToken();
        Object ret = prop.deserializeSetAndReturn(parser2, ctxt, target);
        assertSame(target, ret);
        assertEquals("deserializedValue", target.getName());
    }
}