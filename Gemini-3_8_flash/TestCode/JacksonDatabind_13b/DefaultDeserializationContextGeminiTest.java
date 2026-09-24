package com.fasterxml.jackson.databind.deser;

import java.io.IOException;
import java.util.Iterator;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.BaseSettings;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId.Referring;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.ClassIntrospector;
import com.fasterxml.jackson.databind.introspect.SimpleMixInResolver;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.util.RootNameLookup;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.deser.DefaultDeserializationContext
 *
 * 1. DEFECT-TARGETED BRANCH:
 *    - findObjectId(id, gen, resolverType):
 *      Jackson Databind Issue #742 / Defects4J: when `id == null`, findObjectId must return null
 *      instead of constructing an IdKey(null) or failing with NullPointerException/IllegalArgumentException.
 *
 * 2. OBJECT ID RESOLUTION & MANAGEMENT BRANCHES:
 *    - _objectIds map lazy creation: first call (_objectIds == null) vs subsequent call (_objectIds != null).
 *    - Cached ReadableObjectId lookup: key exists -> return cached entry.
 *    - _objectIdResolvers list lazy creation: null -> allocate ArrayList(8).
 *    - Resolver matching branch: canUseFor(resolverType) == true vs no matching resolver.
 *    - New resolver instantiation: resolverType.newForDeserialization(this).
 *    - Deprecated findObjectId(id, gen) delegation to findObjectId(id, gen, new SimpleObjectIdResolver()).
 *
 * 3. UNRESOLVED OBJECT ID INTEGRITY (checkUnresolvedObjectId):
 *    - Early exit 1: _objectIds == null.
 *    - Early exit 2: FAIL_ON_UNRESOLVED_OBJECT_IDS is disabled.
 *    - Active check: FAIL_ON_UNRESOLVED_OBJECT_IDS enabled with no referring properties.
 *    - Active check: FAIL_ON_UNRESOLVED_OBJECT_IDS enabled with referring properties -> constructs
 *      and throws UnresolvedForwardReference populated with keys, bean types, and locations.
 *
 * 4. FACTORY METHODS: deserializerInstance & keyDeserializerInstance:
 *    - deserDef is null -> returns null.
 *    - deserDef is already an instance of JsonDeserializer / KeyDeserializer.
 *    - deserDef is ResolvableDeserializer -> triggers resolve(this).
 *    - deserDef is not Class<?> -> throws IllegalStateException.
 *    - deserDef is None.class or bogus class (void.class) -> returns null.
 *    - deserDef Class is not assignable to target type -> throws IllegalStateException.
 *    - HandlerInstantiator is null vs non-null returning null vs non-null returning instance.
 *    - Fallback instantiation via ClassUtil.createInstance.
 *
 * 5. SUBCLASS CONTRACT & LIFECYCLE:
 *    - Base DefaultDeserializationContext.copy() throws IllegalStateException if not overridden.
 *    - Impl.copy() handles exact Impl class vs sub-subclasses.
 *    - Impl.createInstance and Impl.with factory methods.
 */
public class DefaultDeserializationContextGeminiTest {

    // =========================================================================
    // Test Doubles & Helpers
    // =========================================================================

    private static class CustomResolvableDeserializer extends JsonDeserializer<Object> implements ResolvableDeserializer {
        boolean resolved = false;

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) {
            return null;
        }

        @Override
        public void resolve(DeserializationContext ctxt) {
            resolved = true;
        }
    }

    private static class CustomResolvableKeyDeserializer extends KeyDeserializer implements ResolvableDeserializer {
        boolean resolved = false;

        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return key;
        }

        @Override
        public void resolve(DeserializationContext ctxt) {
            resolved = true;
        }
    }

    private static class CustomPlainDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) {
            return "plain";
        }
    }

    private static class CustomPlainKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            return "key:" + key;
        }
    }

    private static class CustomObjectIdResolver implements ObjectIdResolver {
        private final int id;

        public CustomObjectIdResolver(int id) {
            this.id = id;
        }

        @Override
        public void bindItem(ObjectIdGenerator.IdKey id, Object pojo) {}

        @Override
        public Object resolveId(ObjectIdGenerator.IdKey id) {
            return null;
        }

        @Override
        public ObjectIdResolver newForDeserialization(Object context) {
            return new CustomObjectIdResolver(this.id);
        }

        @Override
        public boolean canUseFor(ObjectIdResolver resolverType) {
            return resolverType instanceof CustomObjectIdResolver
                    && ((CustomObjectIdResolver) resolverType).id == this.id;
        }
    }

    private static class DummyReferring extends Referring {
        public DummyReferring() {
            super(null, (Class<?>) String.class);
        }

        @Override
        public void setReferring(Referring next) {}

        @Override
        public Referring next() {
            return null;
        }

        @Override
        public void handleResolvedForwardReference(Object id, Object value) {}

        @Override
        public JsonLocation getLocation() {
            return new JsonLocation("src", 10, 1, 1);
        }
    }

    private static class CustomHandlerInstantiator extends HandlerInstantiator {
        private final JsonDeserializer<?> deser;
        private final KeyDeserializer keyDeser;

        public CustomHandlerInstantiator(JsonDeserializer<?> deser, KeyDeserializer keyDeser) {
            this.deser = deser;
            this.keyDeser = keyDeser;
        }

        @Override
        public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) {
            return deser;
        }

        @Override
        public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
            return keyDeser;
        }

        @Override
        public com.fasterxml.jackson.databind.JsonSerializer<?> serializerInstance(com.fasterxml.jackson.databind.SerializationConfig config, Annotated annotated, Class<?> serClass) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder<?> typeResolverBuilderInstance(com.fasterxml.jackson.databind.cfg.MapperConfig<?> config, Annotated annotated, Class<?> builderClass) {
            return null;
        }

        @Override
        public com.fasterxml.jackson.databind.jsontype.TypeIdResolver typeIdResolverInstance(com.fasterxml.jackson.databind.cfg.MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
            return null;
        }
    }

    private DefaultDeserializationContext.Impl createEmptyContext() {
        return new DefaultDeserializationContext.Impl(BeanDeserializerFactory.instance);
    }

    private DefaultDeserializationContext.Impl createConfiguredContext(HandlerInstantiator hi, boolean failOnUnresolved) {
        BaseSettings base = new BaseSettings(
                (ClassIntrospector) null,
                (AnnotationIntrospector) null,
                (PropertyNamingStrategy) null,
                null, // TypeFactory
                null, // TypeResolverBuilder
                null, // DateFormat
                hi,   // HandlerInstantiator
                null, // Locale
                null, // TimeZone
                null  // Base64Variant
        );
        DeserializationConfig config = new DeserializationConfig(
                base,
                (SubtypeResolver) null,
                (SimpleMixInResolver) null,
                (RootNameLookup) null
        );
        if (!failOnUnresolved) {
            config = config.without(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
        } else {
            config = config.with(DeserializationFeature.FAIL_ON_UNRESOLVED_OBJECT_IDS);
        }

        DefaultDeserializationContext.Impl blueprint = createEmptyContext();
        return (DefaultDeserializationContext.Impl) blueprint.createInstance(config, null, null);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Issue #742 / Null ObjectId)
    // =========================================================================

    /**
     * TARGETED FAULT DETECTION:
     * When id is null, findObjectId must return null as per databind#742,
     * consistent with how missing id works, preventing NullPointerException downstream.
     */
    @Test(timeout = 4000)
    public void testFindObjectIdWithNullIdReturnsNull() {
        DefaultDeserializationContext ctxt = createEmptyContext();
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();

        ReadableObjectId roid = ctxt.findObjectId(null, gen, resolver);
        assertNull("findObjectId(null, ...) must return null as per databind#742", roid);
    }

    @Test(timeout = 4000)
    public void testDeprecatedFindObjectIdWithNullIdReturnsNull() {
        DefaultDeserializationContext ctxt = createEmptyContext();
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();

        ReadableObjectId roid = ctxt.findObjectId(null, gen);
        assertNull("findObjectId(null, gen) must return null as per databind#742", roid);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions (ObjectId)
    // =========================================================================

    @Test(timeout = 4000)
    public void testFindObjectIdCreatesAndCachesEntry() {
        DefaultDeserializationContext ctxt = createEmptyContext();
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();

        ReadableObjectId roid1 = ctxt.findObjectId("id123", gen, resolver);
        assertNotNull(roid1);
        assertEquals("id123", roid1.getKey().key);

        // Second call with same id & generator scope should return the exact same instance
        ReadableObjectId roid2 = ctxt.findObjectId("id123", gen, resolver);
        assertSame(roid1, roid2);

        // Different ID should return a new instance
        ReadableObjectId roid3 = ctxt.findObjectId("id456", gen, resolver);
        assertNotNull(roid3);
        assertNotSame(roid1, roid3);
    }

    @Test(timeout = 4000)
    public void testFindObjectIdResolverReuseAndDifferentResolvers() {
        DefaultDeserializationContext ctxt = createEmptyContext();
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();

        CustomObjectIdResolver resType1 = new CustomObjectIdResolver(1);
        CustomObjectIdResolver resType2 = new CustomObjectIdResolver(2);

        ReadableObjectId roid1 = ctxt.findObjectId("item1", gen, resType1);
        assertNotNull(roid1);

        // Another item using matching resolver type 1 should reuse the pooled resolver
        ReadableObjectId roid2 = ctxt.findObjectId("item2", gen, new CustomObjectIdResolver(1));
        assertNotNull(roid2);

        // Item using resolver type 2 should instantiate a second resolver in _objectIdResolvers
        ReadableObjectId roid3 = ctxt.findObjectId("item3", gen, resType2);
        assertNotNull(roid3);
    }

    @Test(timeout = 4000)
    public void testDeprecatedFindObjectIdWithNonNullId() {
        DefaultDeserializationContext ctxt = createEmptyContext();
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();

        ReadableObjectId roid = ctxt.findObjectId("legacyId", gen);
        assertNotNull(roid);
        assertEquals("legacyId", roid.getKey().key);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Unresolved Object IDs
    // =========================================================================

    @Test(timeout = 4000)
    public void testCheckUnresolvedObjectIdWhenMapIsNull() throws UnresolvedForwardReference {
        DefaultDeserializationContext ctxt = createEmptyContext();
        // _objectIds is null initially; must return without error
        ctxt.checkUnresolvedObjectId();
    }

    @Test(timeout = 4000)
    public void testCheckUnresolvedObjectIdFeatureDisabled() throws UnresolvedForwardReference {
        DefaultDeserializationContext.Impl ctxt = createConfiguredContext(null, false);
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ReadableObjectId roid = ctxt.findObjectId("unresolvedId", gen);
        roid.appendReferring(new DummyReferring());

        assertTrue(roid.hasReferringProperties());
        // FAIL_ON_UNRESOLVED_OBJECT_IDS is disabled, should not throw
        ctxt.checkUnresolvedObjectId();
    }

    @Test(timeout = 4000)
    public void testCheckUnresolvedObjectIdNoReferringProperties() throws UnresolvedForwardReference {
        DefaultDeserializationContext.Impl ctxt = createConfiguredContext(null, true);
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ctxt.findObjectId("resolvedId", gen);

        // Entry exists, but hasReferringProperties() is false; must not throw
        ctxt.checkUnresolvedObjectId();
    }

    @Test(timeout = 4000)
    public void testCheckUnresolvedObjectIdThrowsWhenUnresolvedRefsExist() {
        DefaultDeserializationContext.Impl ctxt = createConfiguredContext(null, true);
        ObjectIdGenerator<?> gen = new ObjectIdGenerators.IntSequenceGenerator();
        ReadableObjectId roid = ctxt.findObjectId("danglingId", gen);
        roid.appendReferring(new DummyReferring());

        try {
            ctxt.checkUnresolvedObjectId();
            fail("Expected UnresolvedForwardReference to be thrown");
        } catch (UnresolvedForwardReference ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("Unresolved forward references for:"));
            assertEquals("danglingId", ex.getUnresolvedId());
        }
    }

    // =========================================================================
    // Partition D: Factory Methods (deserializerInstance & keyDeserializerInstance)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDeserializerInstanceNull() throws JsonMappingException {
        DefaultDeserializationContext ctxt = createEmptyContext();
        assertNull(ctxt.deserializerInstance(null, null));
    }

    @Test(timeout = 4000)
    public void testDeserializerInstanceDirectInstance() throws JsonMappingException {
        DefaultDeserializationContext ctxt = createEmptyContext();
        CustomResolvableDeserializer deser = new CustomResolvableDeserializer();
        assertFalse(deser.resolved);

        JsonDeserializer<Object> result = ctxt.deserializerInstance(null, deser);
        assertSame(deser, result);
        assertTrue("ResolvableDeserializer must be resolved", deser.resolved);
    }

    @Test(timeout = 4000)
    public void testDeserializerInstanceNoneClass() throws JsonMappingException {
        DefaultDeserializationContext ctxt = createEmptyContext();
        assertNull(ctxt.deserializerInstance(null, JsonDeserializer.None.class));
        assertNull(ctxt.deserializerInstance(null, com.fasterxml.jackson.databind.annotation.NoClass.class));
    }

    @Test(timeout = 4000)
    public void testDeserializerInstanceInvalidTypeThrowsIllegalState() throws JsonMappingException {
        DefaultDeserializationContext ctxt = createEmptyContext();
        try {
            ctxt.deserializerInstance(null, 12345);
            fail("Expected IllegalStateException for non-Class / non-Deserializer");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("AnnotationIntrospector returned deserializer definition of type"));
        }
    }

    @Test(timeout = 4000)
    public void testDeserializerInstanceUnassignableClassThrowsIllegalState() throws JsonMappingException {
        DefaultDeserializationContext ctxt = createEmptyContext();
        try {
            ctxt.deserializerInstance(null, String.class);
            fail("Expected IllegalStateException for Class not assignable to JsonDeserializer");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("expected Class<JsonDeserializer>"));
        }
    }

    @Test(timeout = 4000)
    public void testDeserializerInstanceViaHandlerInstantiator() throws JsonMappingException {
        CustomPlainDeserializer customDeser = new CustomPlainDeserializer();
        CustomHandlerInstantiator hi = new CustomHandlerInstantiator(customDeser, null);
        DefaultDeserializationContext.Impl ctxt = createConfiguredContext(hi, true);

        JsonDeserializer<Object> res = ctxt.deserializerInstance(null, CustomPlainDeserializer.class);
        assertSame(customDeser, res);
    }

    @Test(timeout = 4000)
    public void testDeserializerInstanceViaDefaultReflection() throws JsonMappingException {
        DefaultDeserializationContext.Impl ctxt = createConfiguredContext(null, true);

        JsonDeserializer<Object> res = ctxt.deserializerInstance(null, CustomPlainDeserializer.class);
        assertNotNull(res);
        assertTrue(res instanceof CustomPlainDeserializer);
    }

    @Test(timeout = 4000)
    public void testKeyDeserializerInstanceNull() throws JsonMappingException {
        DefaultDeserializationContext ctxt = createEmptyContext();
        assertNull(ctxt.keyDeserializerInstance(null, null));
    }

    @Test(timeout = 4000)
    public void testKeyDeserializerInstanceDirectInstance() throws JsonMappingException {
        DefaultDeserializationContext ctxt = createEmptyContext();
        CustomResolvableKeyDeserializer deser = new CustomResolvableKeyDeserializer();
        assertFalse(deser.resolved);

        KeyDeserializer result = ctxt.keyDeserializerInstance(null, deser);
        assertSame(deser, result);
        assertTrue("ResolvableDeserializer must be resolved", deser.resolved);
    }

    @Test(timeout = 4000)
    public void testKeyDeserializerInstanceNoneClass() throws JsonMappingException {
        DefaultDeserializationContext ctxt = createEmptyContext();
        assertNull(ctxt.keyDeserializerInstance(null, KeyDeserializer.None.class));
        assertNull(ctxt.keyDeserializerInstance(null, com.fasterxml.jackson.databind.annotation.NoClass.class));
    }

    @Test(timeout = 4000)
    public void testKeyDeserializerInstanceInvalidTypeThrowsIllegalState() throws JsonMappingException {
        DefaultDeserializationContext ctxt = createEmptyContext();
        try {
            ctxt.keyDeserializerInstance(null, new StringBuilder("invalid"));
            fail("Expected IllegalStateException for non-Class / non-KeyDeserializer");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("AnnotationIntrospector returned key deserializer definition of type"));
        }
    }

    @Test(timeout = 4000)
    public void testKeyDeserializerInstanceUnassignableClassThrowsIllegalState() throws JsonMappingException {
        DefaultDeserializationContext ctxt = createEmptyContext();
        try {
            ctxt.keyDeserializerInstance(null, Integer.class);
            fail("Expected IllegalStateException for Class not assignable to KeyDeserializer");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("expected Class<KeyDeserializer>"));
        }
    }

    @Test(timeout = 4000)
    public void testKeyDeserializerInstanceViaHandlerInstantiator() throws JsonMappingException {
        CustomPlainKeyDeserializer customKeyDeser = new CustomPlainKeyDeserializer();
        CustomHandlerInstantiator hi = new CustomHandlerInstantiator(null, customKeyDeser);
        DefaultDeserializationContext.Impl ctxt = createConfiguredContext(hi, true);

        KeyDeserializer res = ctxt.keyDeserializerInstance(null, CustomPlainKeyDeserializer.class);
        assertSame(customKeyDeser, res);
    }

    @Test(timeout = 4000)
    public void testKeyDeserializerInstanceViaDefaultReflection() throws JsonMappingException {
        DefaultDeserializationContext.Impl ctxt = createConfiguredContext(null, true);

        KeyDeserializer res = ctxt.keyDeserializerInstance(null, CustomPlainKeyDeserializer.class);
        assertNotNull(res);
        assertTrue(res instanceof CustomPlainKeyDeserializer);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Implementation Hierarchy
    // =========================================================================

    @Test(timeout = 4000)
    public void testBaseCopyThrowsIllegalStateExceptionWhenNotOverridden() {
        DefaultDeserializationContext sub = new DefaultDeserializationContext.Impl(BeanDeserializerFactory.instance) {
            private static final long serialVersionUID = 1L;
            // Subclassing Impl so getClass() != Impl.class, triggering super.copy()
        };

        try {
            sub.copy();
            fail("Expected IllegalStateException when copy() is not overridden");
        } catch (IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("DefaultDeserializationContext sub-class not overriding copy()"));
        }
    }

    @Test(timeout = 4000)
    public void testImplCopyCreatesDistinctInstance() {
        DefaultDeserializationContext.Impl original = createEmptyContext();
        DefaultDeserializationContext copy = original.copy();

        assertNotNull(copy);
        assertNotSame(original, copy);
        assertEquals(DefaultDeserializationContext.Impl.class, copy.getClass());
    }

    @Test(timeout = 4000)
    public void testImplWithDifferentFactory() {
        DefaultDeserializationContext.Impl original = createEmptyContext();
        DeserializerFactory customFactory = BeanDeserializerFactory.instance;

        DefaultDeserializationContext withFactory = original.with(customFactory);
        assertNotNull(withFactory);
        assertNotSame(original, withFactory);
        assertSame(customFactory, withFactory.getFactory());
    }

    @Test(timeout = 4000)
    public void testImplCreateInstance() {
        DefaultDeserializationContext.Impl blueprint = createEmptyContext();
        DefaultDeserializationContext instance = blueprint.createInstance(null, null, null);

        assertNotNull(instance);
        assertNotSame(blueprint, instance);
    }
}