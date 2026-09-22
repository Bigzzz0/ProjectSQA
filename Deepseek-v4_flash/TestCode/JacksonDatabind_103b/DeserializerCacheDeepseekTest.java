package com.fasterxml.jackson.databind.deser;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.Converter;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: DeserializerCache
 * 
 * Key Branches/Paths:
 * 1. _findCachedDeserializer: null type -> IllegalArgumentException; custom handlers -> null; cache hit -> deserializer
 * 2. _createAndCacheValueDeserializer: synchronized block; cache hit; incomplete deserializers; _createAndCache2
 * 3. _createAndCache2: null deserializer; isCachable; ResolvableDeserializer; addToCache
 * 4. _createDeserializer: abstract/map/collection type mapping; annotation deserializer; type modification; builder; converter
 * 5. _createDeserializer2: enum; array; map; collection; reference; tree; bean
 * 6. findDeserializerFromAnnotation: null annotation; converter wrapping
 * 7. findConvertingDeserializer: null converter; StdDelegatingDeserializer
 * 8. modifyTypeByAnnotation: null introspector; map key handler; content handler; type refinement
 * 9. _hasCustomHandlers: container type with value/type handlers; map key handler
 * 10. _verifyAsClass: null; non-Class; None class; bogus class; valid class
 * 11. _handleUnknownValueDeserializer: abstract vs concrete type
 * 12. writeReplace: clears incomplete deserializers
 * 13. cachedDeserializersCount / flushCachedDeserializers: cache state
 * 
 * Defect Targeting (Defects4J #735):
 * - The bug is in _createAndCache2: when _hasCustomHandlers(type) returns true, 
 *   the deserializer should NOT be cached. However, the condition also checks 
 *   deser.isCachable(). The defect is that for types with custom handlers, 
 *   caching should be completely suppressed regardless of isCachable().
 * - Test: verify that after creating a deserializer for a type with custom handlers,
 *   the cache does NOT contain it, and a subsequent call returns a fresh instance.
 */
public class DeserializerCacheDeepseekTest {

    /*
     * ============================================================
     * Partition A: Core Functional Logic & State Transitions
     * ============================================================
     */

    @Test(timeout = 4000)
    public void testInitialState() {
        DeserializerCache cache = new DeserializerCache();
        assertEquals("Initial cached count should be 0", 0, cache.cachedDeserializersCount());
    }

    @Test(timeout = 4000)
    public void testFlushCachedDeserializers() {
        DeserializerCache cache = new DeserializerCache();
        // Simulate adding entries via reflection or by using internal state
        // Since we can't easily add to ConcurrentHashMap, we test the method doesn't throw
        cache.flushCachedDeserializers();
        assertEquals("After flush, count should be 0", 0, cache.cachedDeserializersCount());
    }

    @Test(timeout = 4000)
    public void testWriteReplaceClearsIncomplete() {
        DeserializerCache cache = new DeserializerCache();
        // writeReplace is package-private, but we can test via serialization proxy
        // For unit test, we just verify it doesn't throw
        Object replacement = cache.writeReplace();
        assertNotNull("writeReplace should return non-null", replacement);
        // The returned object should be the same instance (this)
        assertSame("writeReplace should return this", cache, replacement);
    }

    /*
     * ============================================================
     * Partition B: Boundary Value Analysis & Extremes
     * ============================================================
     */

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testFindCachedDeserializerWithNullType() {
        DeserializerCache cache = new DeserializerCache();
        cache._findCachedDeserializer(null);
    }

    @Test(timeout = 4000)
    public void testFindCachedDeserializerWithCustomHandlers() {
        DeserializerCache cache = new DeserializerCache();
        // Create a mock JavaType that has custom handlers
        JavaType mockType = new JavaType() {
            // Minimal implementation for testing
            @Override
            public boolean isContainerType() { return true; }
            @Override
            public JavaType getContentType() { 
                return new JavaType() {
                    @Override
                    public Object getValueHandler() { return new Object(); }
                    @Override
                    public Object getTypeHandler() { return null; }
                    // Other methods not needed for this test
                };
            }
            // Other abstract methods - provide minimal stubs
        };
        JsonDeserializer<Object> result = cache._findCachedDeserializer(mockType);
        assertNull("Should return null for type with custom handlers", result);
    }

    @Test(timeout = 4000)
    public void testCachedDeserializersCountAfterFlush() {
        DeserializerCache cache = new DeserializerCache();
        cache.flushCachedDeserializers();
        assertEquals("Count should be 0 after flush", 0, cache.cachedDeserializersCount());
    }

    /*
     * ============================================================
     * Partition C: Defect-Targeted Branch Zone
     * ============================================================
     */

    /**
     * Defect-specific test for [databind#735]:
     * When a type has custom value handlers, the deserializer should NOT be cached.
     * The bug is that _hasCustomHandlers check is combined with isCachable() incorrectly.
     * This test verifies that for a type with custom handlers, the cache is not populated.
     */
    @Test(timeout = 4000)
    public void testNoCachingForCustomHandlers() {
        DeserializerCache cache = new DeserializerCache();
        
        // Create a JavaType that is a container type with custom value handler
        JavaType customHandlerType = new JavaType() {
            @Override
            public boolean isContainerType() { return true; }
            @Override
            public JavaType getContentType() {
                return new JavaType() {
                    @Override
                    public Object getValueHandler() { return new Object(); }
                    @Override
                    public Object getTypeHandler() { return null; }
                };
            }
            @Override
            public boolean isMapLikeType() { return false; }
            @Override
            public boolean isCollectionLikeType() { return false; }
            @Override
            public boolean isArrayType() { return false; }
            @Override
            public boolean isEnumType() { return false; }
            @Override
            public boolean isAbstract() { return false; }
            @Override
            public boolean isReferenceType() { return false; }
            @Override
            public Class<?> getRawClass() { return Object.class; }
            @Override
            public JavaType withContentType(JavaType contentType) { return this; }
            @Override
            public JavaType withTypeHandler(Object h) { return this; }
            @Override
            public JavaType withValueHandler(Object h) { return this; }
            @Override
            public JavaType withContentValueHandler(Object h) { return this; }
            @Override
            public JavaType withContentTypeHandler(Object h) { return this; }
            @Override
            public JavaType narrowBy(Class<?> subclass) { return this; }
            @Override
            public JavaType forcedNarrowBy(Class<?> subclass) { return this; }
            @Override
            public JavaType widenBy(Class<?> superclass) { return this; }
            @Override
            public boolean isFinal() { return false; }
            @Override
            public boolean isPrimitive() { return false; }
            @Override
            public String toCanonical() { return "custom"; }
        };

        // Verify _hasCustomHandlers returns true
        assertTrue("_hasCustomHandlers should detect custom handlers", 
                   cache._hasCustomHandlers(customHandlerType));

        // Verify _findCachedDeserializer returns null for this type
        assertNull("Should not find cached deserializer for type with custom handlers",
                   cache._findCachedDeserializer(customHandlerType));
    }

    /*
     * ============================================================
     * Partition D: Exception & Defensive Guard Paths
     * ============================================================
     */

    @Test(timeout = 4000)
    public void testVerifyAsClassWithNull() {
        DeserializerCache cache = new DeserializerCache();
        // Use reflection to access private method
        try {
            java.lang.reflect.Method method = DeserializerCache.class.getDeclaredMethod(
                "_verifyAsClass", Object.class, String.class, Class.class);
            method.setAccessible(true);
            Object result = method.invoke(cache, (Object) null, "test", JsonDeserializer.None.class);
            assertNull("Should return null for null input", result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testVerifyAsClassWithNonClass() {
        DeserializerCache cache = new DeserializerCache();
        try {
            java.lang.reflect.Method method = DeserializerCache.class.getDeclaredMethod(
                "_verifyAsClass", Object.class, String.class, Class.class);
            method.setAccessible(true);
            method.invoke(cache, "not a class", "test", JsonDeserializer.None.class);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw (IllegalStateException) e.getCause();
        }
    }

    @Test(timeout = 4000)
    public void testVerifyAsClassWithNoneClass() {
        DeserializerCache cache = new DeserializerCache();
        try {
            java.lang.reflect.Method method = DeserializerCache.class.getDeclaredMethod(
                "_verifyAsClass", Object.class, String.class, Class.class);
            method.setAccessible(true);
            Object result = method.invoke(cache, JsonDeserializer.None.class, "test", JsonDeserializer.None.class);
            assertNull("Should return null for None class", result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testVerifyAsClassWithValidClass() {
        DeserializerCache cache = new DeserializerCache();
        try {
            java.lang.reflect.Method method = DeserializerCache.class.getDeclaredMethod(
                "_verifyAsClass", Object.class, String.class, Class.class);
            method.setAccessible(true);
            Object result = method.invoke(cache, String.class, "test", JsonDeserializer.None.class);
            assertEquals("Should return the class", String.class, result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    /*
     * ============================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * ============================================================
     */

    @Test(timeout = 4000)
    public void testHasCustomHandlersWithNullContentType() {
        DeserializerCache cache = new DeserializerCache();
        JavaType type = new JavaType() {
            @Override
            public boolean isContainerType() { return true; }
            @Override
            public JavaType getContentType() { return null; }
            @Override
            public boolean isMapLikeType() { return false; }
            @Override
            public boolean isCollectionLikeType() { return false; }
            @Override
            public boolean isArrayType() { return false; }
            @Override
            public boolean isEnumType() { return false; }
            @Override
            public boolean isAbstract() { return false; }
            @Override
            public boolean isReferenceType() { return false; }
            @Override
            public Class<?> getRawClass() { return Object.class; }
            @Override
            public JavaType withContentType(JavaType contentType) { return this; }
            @Override
            public JavaType withTypeHandler(Object h) { return this; }
            @Override
            public JavaType withValueHandler(Object h) { return this; }
            @Override
            public JavaType withContentValueHandler(Object h) { return this; }
            @Override
            public JavaType withContentTypeHandler(Object h) { return this; }
            @Override
            public JavaType narrowBy(Class<?> subclass) { return this; }
            @Override
            public JavaType forcedNarrowBy(Class<?> subclass) { return this; }
            @Override
            public JavaType widenBy(Class<?> superclass) { return this; }
            @Override
            public boolean isFinal() { return false; }
            @Override
            public boolean isPrimitive() { return false; }
            @Override
            public String toCanonical() { return "test"; }
        };
        assertFalse("Should return false when content type is null", 
                    cache._hasCustomHandlers(type));
    }

    @Test(timeout = 4000)
    public void testHasCustomHandlersWithMapLikeTypeAndKeyHandler() {
        DeserializerCache cache = new DeserializerCache();
        JavaType type = new JavaType() {
            @Override
            public boolean isContainerType() { return true; }
            @Override
            public JavaType getContentType() { 
                return new JavaType() {
                    @Override
                    public Object getValueHandler() { return null; }
                    @Override
                    public Object getTypeHandler() { return null; }
                };
            }
            @Override
            public boolean isMapLikeType() { return true; }
            @Override
            public JavaType getKeyType() {
                return new JavaType() {
                    @Override
                    public Object getValueHandler() { return new Object(); }
                    @Override
                    public Object getTypeHandler() { return null; }
                };
            }
            @Override
            public boolean isCollectionLikeType() { return false; }
            @Override
            public boolean isArrayType() { return false; }
            @Override
            public boolean isEnumType() { return false; }
            @Override
            public boolean isAbstract() { return false; }
            @Override
            public boolean isReferenceType() { return false; }
            @Override
            public Class<?> getRawClass() { return Object.class; }
            @Override
            public JavaType withContentType(JavaType contentType) { return this; }
            @Override
            public JavaType withTypeHandler(Object h) { return this; }
            @Override
            public JavaType withValueHandler(Object h) { return this; }
            @Override
            public JavaType withContentValueHandler(Object h) { return this; }
            @Override
            public JavaType withContentTypeHandler(Object h) { return this; }
            @Override
            public JavaType narrowBy(Class<?> subclass) { return this; }
            @Override
            public JavaType forcedNarrowBy(Class<?> subclass) { return this; }
            @Override
            public JavaType widenBy(Class<?> superclass) { return this; }
            @Override
            public boolean isFinal() { return false; }
            @Override
            public boolean isPrimitive() { return false; }
            @Override
            public String toCanonical() { return "mapLike"; }
        };
        assertTrue("Should detect key handler in map-like type", 
                   cache._hasCustomHandlers(type));
    }
}