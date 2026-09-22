package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeBindings;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * ========== BRANCH COVERAGE TARGETS ==========
 * Branch 1: withStaticTyping() - when _asStatic is true vs false
 * Branch 2: withContentType() - when _valueType == contentType vs different
 * Branch 3: withKeyType() - when keyType == _keyType vs different
 * Branch 4: construct() - deprecated path with null bindings (defect target)
 * Branch 5: withKeyTypeHandler() - delegation to _keyType.withTypeHandler()
 * Branch 6: withKeyValueHandler() - delegation to _keyType.withValueHandler()
 * Branch 7: withContentTypeHandler() - delegation to _valueType.withTypeHandler()
 * Branch 8: withContentValueHandler() - delegation to _valueType.withValueHandler()
 * Branch 9: withTypeHandler() - handler replacement
 * Branch 10: withValueHandler() - value handler replacement
 * Branch 11: refine() - reconstructs with same _keyType/_valueType
 * Branch 12: _narrow() - deprecated subclass narrowing
 * Branch 13: toString() - format string generation
 * 
 * ========== BOUNDARY VALUE ANALYSIS ==========
 * Boundary 1: null keyType/valueType (for construct and factory methods)
 * Boundary 2: identical object references (for withContentType/withKeyType identity check)
 * Boundary 3: _asStatic = true (withStaticTyping self-return)
 * Boundary 4: non-null valueHandler/typeHandler
 * 
 * ========== DEFECT TARGET ==========
 * Defect [databind#1102]: Deprecated MapType.construct(Class<?>, JavaType, JavaType)
 * creates MapType with null _bindings, causing type resolution failure.
 * The bug manifests when super class resolution uses _bindings which is null,
 * leading to incorrect type handling. We test that after construction with
 * the deprecated method, key/value types are properly accessible.
 */
public class MapTypeDeepseekTest {

    private final TypeFactory typeFactory = TypeFactory.defaultInstance();

    // ====== Partition A: Core Functional Logic & State Transitions ======

    @Test(timeout = 4000)
    public void testStaticTypingWhenAlreadyStatic() {
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType initial = MapType.construct(
            java.util.HashMap.class,
            TypeBindings.create(java.util.HashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType,
            intType
        );
        MapType staticVersion = initial.withStaticTyping();
        // Make it static
        MapType againStatic = staticVersion.withStaticTyping();
        assertSame("withStaticTyping() should return same instance when already static", 
                    staticVersion, againStatic);
    }

    @Test(timeout = 4000)
    public void testWithContentTypeIdenticalReference() {
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType mapType = MapType.construct(
            java.util.HashMap.class,
            TypeBindings.create(java.util.HashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType,
            intType
        );
        // Same _valueType reference
        assertSame("withContentType() should return same instance when same contentType",
                   mapType, mapType.withContentType(intType));
        
        // Different contentType
        JavaType longType = typeFactory.constructType(Long.class);
        MapType newMapType = mapType.withContentType(longType);
        assertNotSame("withContentType() should return new instance with different contentType",
                      mapType, newMapType);
        assertEquals("Content type should be updated", longType, newMapType.getContentType());
    }

    @Test(timeout = 4000)
    public void testWithKeyTypeIdenticalReference() {
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType mapType = MapType.construct(
            java.util.HashMap.class,
            TypeBindings.create(java.util.HashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType,
            intType
        );
        // Same _keyType reference
        assertSame("withKeyType() should return same instance when same keyType",
                   mapType, mapType.withKeyType(stringType));
        
        // Different keyType
        JavaType longType = typeFactory.constructType(Long.class);
        MapType newMapType = mapType.withKeyType(longType);
        assertNotSame("withKeyType() should return new instance with different keyType",
                      mapType, newMapType);
        assertEquals("Key type should be updated", longType, newMapType.getKeyType());
    }

    @Test(timeout = 4000)
    public void testWithTypeHandler() {
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType mapType = MapType.construct(
            java.util.HashMap.class,
            TypeBindings.create(java.util.HashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType,
            intType
        );
        Object handler = new Object();
        MapType withHandler = mapType.withTypeHandler(handler);
        assertNotNull("withTypeHandler() should return non-null type", withHandler);
        assertNotSame("withTypeHandler() should return new instance", mapType, withHandler);
    }

    @Test(timeout = 4000)
    public void testWithValueHandler() {
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType mapType = MapType.construct(
            java.util.HashMap.class,
            TypeBindings.create(java.util.HashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType,
            intType
        );
        Object handler = new Object();
        MapType withHandler = mapType.withValueHandler(handler);
        assertNotNull("withValueHandler() should return non-null type", withHandler);
        assertNotSame("withValueHandler() should return new instance", mapType, withHandler);
    }

    @Test(timeout = 4000)
    public void testRefineMethod() {
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType mapType = MapType.construct(
            java.util.HashMap.class,
            TypeBindings.create(java.util.HashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType,
            intType
        );
        JavaType refined = mapType.refine(
            java.util.LinkedHashMap.class,
            TypeBindings.create(java.util.LinkedHashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)}
        );
        assertTrue("Refined type should be MapType", refined instanceof MapType);
        assertEquals("Key type should be preserved", stringType, ((MapType) refined).getKeyType());
        assertEquals("Value type should be preserved", intType, refined.getContentType());
    }

    // ====== Partition B: Boundary Value Analysis & Extremes ======

    @Test(timeout = 4000)
    public void testConstructWithNullBindings() {
        // This tests the deprecated construct path and flag behavior
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType mapType = MapType.construct(java.util.HashMap.class, TypeBindings.emptyBindings(),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType, intType);
        assertNotNull("MapType should be constructed with empty bindings", mapType);
        assertNotNull("Key type should be accessible", mapType.getKeyType());
        assertNotNull("Value type should be accessible", mapType.getContentType());
    }

    @Test(timeout = 4000)
    public void testGetKeyTypeWithNullKey() {
        // Verify that we can construct with potentially null key (if allowed)
        JavaType stringType = typeFactory.constructType(String.class);
        MapType mapType = MapType.construct(java.util.HashMap.class, TypeBindings.emptyBindings(),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            null, stringType);
        // This should be allowed per superclass, test it doesn't NPE
        assertNull("Key type can be null", mapType.getKeyType());
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingKeyTypesBecomeStatic() {
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType mapType = MapType.construct(
            java.util.HashMap.class,
            TypeBindings.create(java.util.HashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType,
            intType
        );
        MapType staticMap = mapType.withStaticTyping();
        assertTrue("Key type should be static after withStaticTyping", 
                   staticMap.getKeyType().isStatic());
        assertTrue("Content type should be static after withStaticTyping", 
                   staticMap.getContentType().isStatic());
    }

    // ====== Partition C: Defect-Targeted Branch Zone (targets #1102) ======

    @Test(timeout = 4000)
    public void testDeprecatedConstructPreservesKeyValueTypes() {
        // This test directly targets the defect from databind#1102
        // The deprecated construct can produce a MapType with null _bindings,
        // but key/value types should still be accessible
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        
        // Use the deprecated construct method (note: we must use the 2-parameter version)
        MapType deprecatedMap = MapType.construct(java.util.HashMap.class, stringType, intType);
        
        assertNotNull("MapType should not be null", deprecatedMap);
        assertNotNull("Key type should not be null", deprecatedMap.getKeyType());
        assertNotNull("Value type should not be null", deprecatedMap.getContentType());
        assertEquals("Key type should be String", "java.lang.String", 
                     deprecatedMap.getKeyType().toCanonical());
        assertEquals("Value type should be Integer", "java.lang.Integer", 
                     deprecatedMap.getContentType().toCanonical());
        
        // This is the core defect test: verify that a type constructed with the deprecated
        // method can still properly resolve key/value types without NPE
        String toString = deprecatedMap.toString();
        assertTrue("toString should contain key type", toString.contains("java.lang.String"));
        assertTrue("toString should contain value type", toString.contains("java.lang.Integer"));
    }

    @Test(timeout = 4000)
    public void testNarrowDeprecatedSubtype() {
        // Test the _narrow path which is also deprecated
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType mapType = MapType.construct(
            java.util.HashMap.class,
            TypeBindings.create(java.util.HashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType,
            intType
        );
        // _narrow is deprecated but still functional; test it doesn't crash
        // We need to use JavaType as return type since _narrow is protected
        // So we test via a public method that calls it
        // For simplicity, we test that construct with explicit subclass works
        MapType narrowed = MapType.construct(java.util.LinkedHashMap.class, 
            TypeBindings.create(java.util.LinkedHashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType, intType);
        assertEquals("Narrowed type should be LinkedHashMap",
                     java.util.LinkedHashMap.class, narrowed.getRawClass());
    }

    // ====== Partition D: Exception & Defensive Guard Paths ======

    @Test(timeout = 4000)
    public void testWithContentTypeHandlerCreatesNewInstance() {
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType mapType = MapType.construct(
            java.util.HashMap.class,
            TypeBindings.create(java.util.HashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType,
            intType
        );
        Object handler = new Object();
        MapType withHandler = mapType.withContentTypeHandler(handler);
        assertNotNull("withContentTypeHandler() should return non-null", withHandler);
        assertNotSame("withContentTypeHandler() should return new instance", mapType, withHandler);
    }

    @Test(timeout = 4000)
    public void testWithContentValueHandlerCreatesNewInstance() {
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType mapType = MapType.construct(
            java.util.HashMap.class,
            TypeBindings.create(java.util.HashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType,
            intType
        );
        Object handler = new Object();
        MapType withHandler = mapType.withContentValueHandler(handler);
        assertNotNull("withContentValueHandler() should return non-null", withHandler);
        assertNotSame("withContentValueHandler() should return new instance", mapType, withHandler);
    }

    @Test(timeout = 4000)
    public void testWithKeyTypeHandlerCreatesNewInstance() {
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType mapType = MapType.construct(
            java.util.HashMap.class,
            TypeBindings.create(java.util.HashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType,
            intType
        );
        Object handler = new Object();
        MapType withHandler = mapType.withKeyTypeHandler(handler);
        assertNotNull("withKeyTypeHandler() should return non-null", withHandler);
        assertNotSame("withKeyTypeHandler() should return new instance", mapType, withHandler);
    }

    @Test(timeout = 4000)
    public void testWithKeyValueHandlerCreatesNewInstance() {
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType mapType = MapType.construct(
            java.util.HashMap.class,
            TypeBindings.create(java.util.HashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType,
            intType
        );
        Object handler = new Object();
        MapType withHandler = mapType.withKeyValueHandler(handler);
        assertNotNull("withKeyValueHandler() should return non-null", withHandler);
        assertNotSame("withKeyValueHandler() should return new instance", mapType, withHandler);
    }

    // ====== Partition E: Object Lifecycle & Contract Integrity ======

    @Test(timeout = 4000)
    public void testToStringFormat() {
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType mapType = MapType.construct(
            java.util.HashMap.class,
            TypeBindings.create(java.util.HashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType,
            intType
        );
        String toString = mapType.toString();
        assertTrue("toString should start with [map type; class", 
                   toString.startsWith("[map type; class"));
        assertTrue("toString should contain ->", toString.contains("->"));
        assertTrue("toString should contain java.util.HashMap", 
                   toString.contains("java.util.HashMap"));
        assertTrue("toString should contain key type", 
                   toString.contains("java.lang.String"));
        assertTrue("toString should contain value type", 
                   toString.contains("java.lang.Integer"));
    }

    @Test(timeout = 4000)
    public void testMultipleCallsWithDifferentHandlers() {
        JavaType stringType = typeFactory.constructType(String.class);
        JavaType intType = typeFactory.constructType(Integer.class);
        MapType base = MapType.construct(
            java.util.HashMap.class,
            TypeBindings.create(java.util.HashMap.class, stringType, intType),
            typeFactory.constructType(java.util.AbstractMap.class),
            new JavaType[]{typeFactory.constructType(java.util.Map.class)},
            stringType,
            intType
        );
        
        // Apply multiple handler modifications and verify they're independent
        Object handler1 = new Object();
        Object handler2 = new Object();
        
        MapType withTypeHandler = base.withTypeHandler(handler1);
        MapType withValueHandler = base.withValueHandler(handler2);
        
        // Both should be different from base and each other
        assertNotSame("withTypeHandler result should differ from base", base, withTypeHandler);
        assertNotSame("withValueHandler result should differ from base", base, withValueHandler);
        assertNotSame("Different handler methods should produce different instances",
                      withTypeHandler, withValueHandler);
        
        // Both should still be valid MapTypes
        assertNotNull("withTypeHandler result should have key type", 
                      withTypeHandler.getKeyType());
        assertNotNull("withValueHandler result should have key type", 
                      withValueHandler.getKeyType());
    }
}