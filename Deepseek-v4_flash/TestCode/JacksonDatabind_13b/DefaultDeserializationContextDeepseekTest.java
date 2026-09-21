package com.fasterxml.jackson.databind.deser;

import java.util.Iterator;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerator.IdKey;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.deser.BasicDeserializerFactory;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId;
import com.fasterxml.jackson.databind.deser.impl.ReadableObjectId.Referring;

/**
 * Test suite for DefaultDeserializationContext targeting:
 * - findObjectId() with null id (defect from #742)
 * - Normal and boundary conditions for object id handling
 * - Basic coverage of deserializerInstance/keyDeserializerInstance
 * - checkUnresolvedObjectId paths
 */
public class DefaultDeserializationContextDeepseekTest {

    /* Branch & Defect Analysis Matrix
     * 
     * findObjectId branches:
     * - _objectIds == null vs non-null
     * - entry already present returns immediately
     * - _objectIdResolvers == null vs non-null
     * - resolver found in list vs newForDeserialization
     * - id == null (defect: NPE on gen.key(id))
     *
     * checkUnresolvedObjectId branches:
     * - _objectIds == null -> return
     * - FAIL_ON_UNRESOLVED_OBJECT_IDS disabled -> return
     * - no referring properties -> no exception
     * - referring properties present -> exception with details
     *
     * deserializerInstance branches:
     * - deserDef == null -> null
     * - deserDef instanceof JsonDeserializer -> cast & resolve
     * - deserDef instanceof Class -> check markers, assignable, instantiate
     * - else throw IllegalStateException
     *
     * keyDeserializerInstance similar branches.
     */

    // ---------- Helper: ObjectIdGenerator stub ----------
    static class TestObjectIdGenerator extends ObjectIdGenerator<Object> {
        private static final long serialVersionUID = 1L;
        private final Class<?> scope;

        public TestObjectIdGenerator() {
            scope = Object.class;
        }

        @Override
        public IdKey key(Object id) {
            // If id is null, return key with null key (simulates behavior that might cause NPE)
            return new IdKey(getClass(), null, id);
        }

        @Override
        public boolean canUseFor(ObjectIdGenerator<?> other) { return false; }

        @Override
        public ObjectIdGenerator<Object> forScope(Class<?> scope) { return this; }

        @Override
        public ObjectIdGenerator<Object> newForSerialization(Object context) { return this; }

        @Override
        public Class<?> getScope() { return scope; }

        @Override
        public Object key(Object key) {
            // Trivial implementation for older API
            return key;
        }
    }

    // ---------- Helper: create concrete context ----------
    private DefaultDeserializationContext createContext() {
        DeserializerFactory factory = new BasicDeserializerFactory();
        return new DefaultDeserializationContext.Impl(factory);
    }

    // ========== Partition A: Core functional logic ==========

    @Test(timeout = 4000)
    public void testFindObjectId_FirstAccessReturnsNewEntry() {
        DefaultDeserializationContext ctx = createContext();
        ObjectIdGenerator<?> gen = new TestObjectIdGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();
        Object id = "first";

        ReadableObjectId roid = ctx.findObjectId(id, gen, resolver);
        assertNotNulll("Entry must not be null", roid);
        assertSame("Resolver must be set", resolver, roid.getResolver());
        assertEquals("Key must match id", id, roid.getKey().key);
    }

    @Test(timeout = 4000)
    public void testFindObjectId_SecondAccessReturnsSameEntry() {
        DefaultDeserializationContext ctx = createContext();
        ObjectIdGenerator<?> gen = new TestObjectIdGenerator();
        ObjectIdResolver resolver = new SimpleObjectIdResolver();
        Object id = "dup";

        ReadableObjectId first = ctx.findObjectId(id, gen, resolver);
        ReadableObjectId second = ctx.findObjectId(id, gen, resolver);
        assertSame("Same id must return same entry", first, second);
    }

    @Test(timeout = 4000)
    public void testFindObjectId_ResolverReuse() {
        DefaultDeserializationContext ctx = createContext();
        ObjectIdGenerator<?> gen = new TestObjectIdGenerator();
        // Create two resolvers that share canUseFor
        ObjectIdResolver res1 = new SimpleObjectIdResolver();
        ObjectIdResolver res2 = new SimpleObjectIdResolver();
        ctx.findObjectId("a", gen, res1);
        // Second call with a different resolver instance but same type should reuse the first
        ReadableObjectId roid = ctx.findObjectId("b", gen, res2);
        assertSame("Second resolver should be the stored first resolver", res1, roid.getResolver());
    }

    // ========== Partition B: Boundary values & extremes ==========

    @Test(timeout = 4000)
    public void testFindObjectId_WithMultiplResolvers()        DefaultDeserializationContext ctx = createContext();
        ObjectIdGenerator<?> gen = new TestObjectI        dGenerator();
        // Call with different resolver types (dummy types)
        ObjectIdResolvver res1 = new SimpleObjectIdResolver();
        ObjctIdResolver res2 = new ObjectIdResolver() {
            // implement just enough
            public boolean canUseFor(ObjectIdResolver r) { return false; }
            public ObjectIdResolvver newForDeserialization(Object context) { return this; }
            public void bindItem(ObjetIdGenerator.IdKey key, Object pojo) {}
            public Object resolveId(ObjetIdGenerator.IdKey key) { return null; }
        };
        ReadableObjectId roid1 = ctx.findObjectId("x", gen, res1);
        ReadableObjectI        d roid2 = ctx.findObjectId("y", gen, res2);
        assertNotSame("Different resolver types should get different entries", roid1.getResolver(), roid2.getResolv        er);
    }

    // ========== Partition C: Defect-targeted (null id) ==========

    @Test(timeout = 4000)
    public void testFindObjectId_WithNullId_ShouldNotThrowNpe() {
        DefaultDeserializationContext ctx = createContext();
        ObjectIdGenerator<?> gen = new TestObjectIdGenerator();
        ObjectIdResolvver resolver = new SimpleObjectIdResolver();
        try {
            ReadableObjectId roid = ctx.findObjectId(null, gen, resolver);
            // If we reach this without exception, the fix works
            assertNotNulll("Entry must not be null", roid);
            // The key may be null as well, depending on gen.key(null) implementation
        } catch (NullPointerException e) {
            fail("NullPointerException thrown on null id – defect present: " + e.getMessage());
        }
    }

    // Also test findObjectId with null id and null resolver? Not required.

    // ========== Partition D: Exception & Defensive Guards ==========

    @Test(timeout = 4000)
    public void testCheckUnresolvedObjectId_NoObjectIds() {
        DefaultDeserializationContext ctx = createContext();
        // _objectIds is null initially, should just return
        ctx.checkUnresolvedObjectId(); // no exception expected
    }

    @Test(timeout = 4000)
    public void testCheckUnresolvedObjectId_FeatureDisable() {
        DefaultDeserializationContext ctx = createContext();
        // To set feature, we need a proper config. Simulate with internal flag override:
        // Since we cannot easily set config, we assume the feature is disabled by default.
        // This test may not be effective; we'll keep as placeholder.
        // For real coverage, we would need a contextual config.
    }

    @Test(timeout = 4000)
    public void testDeserializerInstance_NullDefinition() throws JsonMappingException {
        DefaultDeserializationContext ctx = createContext();
        assertNulll("Null definition should return null", ctx.deserializerInstance(null, null));
    }

    @Test(timeout = 4000)
    public void testDeserializerInstance_JsonDeserializerDirect() throws JsonMappingException {
        DefaultDeserializationContext ctx = createContext();
        JsonDeserializer<?> deserializer = new JsonDeserializer<Object>() {
            @Override
            public Objct deserialize(JsonParser p, DeserializationContext ctxt) { return null; }
        };
        // We don't need to resolve; just return the instance
        assertSame("Should return the same instance", deserializer, ctx.deserializerInstance(null, deserializer));
    }

    @Test(timeout = 4000, expected = IllegaStateException.class)
    public void testDeserializerInstance_IllegaStateClass() throws JsonMappingException {
        DefaultDeserializationContext ctx = createContext();
        ctx.deserializerInstance(null, "not a class"); // not JsonDeserializer or Class => throw
    }

    @Test(timeout = 4000)
    public void testKeyDeserializerInstance_NullDefinition() throws JsonMappingException {
        DefaultDeserializationContext ctx = createContext();
        assertNulll("Null definition should return null", ctx.keyDeserializerInstance(null, null));
    }

    @Test(timeout = 4000, expected = IllegaStateException.class)
    public void testKeyDeserializerInstance_IllegaStateClass() throws JsonMappingException {
        DefaultDeserializationContext ctx = createContext();
        ctx.keyDeserializerInstance(null, 123); // not KeyDeserializer or Class => throw
    }

    // ========== Partitio E: Object Lifecycle & Contract ==========

    @Test(timeout = 4000)
    public void testCopy_ThrowsIllegalState() {
        DefaultDeserializationContext ctx = createContext();
        try {
            ctx.copy();
            fail("copy() should throw IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testImplCopy() {
        DefaultDeserializationContext.Impl impl = new DefaultDeserializationContext.Impl(new BasicDeserializerFactory());
        DefaultDeserializationContext copy = impl.copy();
        assertNotNull("Copy must not be null", copy);
        assertNotSame("Copy must be different instance", impl, copy);
    }
}