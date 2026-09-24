package com.fasterxml.jackson.databind.introspect;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;

import java.util.Collections;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: com.fasterxml.jackson.databind.introspect.ObjectIdInfo
 *
 * Decision / Branch Matrix:
 * 1. Constructor (5-arg):
 *    - resolver == null -> defaults to SimpleObjectIdResolver.class (Branch TRUE)
 *    - resolver != null -> retains specified resolver class (Branch FALSE)
 * 2. withAlwaysAsId(boolean state):
 *    - _alwaysAsId == state -> return this (Branch TRUE, identity preserved)
 *    - _alwaysAsId != state -> return new ObjectIdInfo(...) (Branch FALSE, new state copied)
 * 3. toString():
 *    - _scope == null -> appends "null"
 *    - _scope != null -> appends _scope.getName()
 *    - _generator == null -> appends "null"
 *    - _generator != null -> appends _generator.getName()
 * 4. Deprecated & Convenience Constructors:
 *    - (PropertyName, Class, Class, Class): delegates with alwaysAsId=false
 *    - (PropertyName, Class, Class): delegates with alwaysAsId=false, resolver=SimpleObjectIdResolver
 *    - (String, Class, Class): converts String to PropertyName, alwaysAsId=false, resolver=SimpleObjectIdResolver
 *    - (PropertyName, Class, Class, boolean): defaults resolver to SimpleObjectIdResolver
 *
 * Defect Matrix (Defects4J Issue #1607 / AlwaysAsReferenceFirstTest):
 * - Target: Class-level @JsonIdentityReference(alwaysAsId=true) failed to serialize
 *   collection elements as scalar IDs, outputting full POJOs instead of scalar IDs.
 * - Test testIssue1607AlwaysAsIdOnClassInCollection asserts that class-level alwaysAsId=true
 *   serializes list elements directly to IDs (e.g. [1]) rather than full objects ([{"id":1,...}]).
 */
public class ObjectIdInfoGeminiTest {

    private static abstract class CustomResolver implements ObjectIdResolver {}

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFullConstructorWithExplicitResolver() {
        PropertyName propName = new PropertyName("customId");
        ObjectIdInfo info = new ObjectIdInfo(
                propName,
                String.class,
                ObjectIdGenerators.PropertyGenerator.class,
                true,
                CustomResolver.class
        );

        assertEquals(propName, info.getPropertyName());
        assertEquals(String.class, info.getScope());
        assertEquals(ObjectIdGenerators.PropertyGenerator.class, info.getGeneratorType());
        assertEquals(CustomResolver.class, info.getResolverType());
        assertTrue(info.getAlwaysAsId());
    }

    @Test(timeout = 4000)
    public void testWithAlwaysAsIdTransition() {
        PropertyName propName = new PropertyName("id");
        ObjectIdInfo initial = new ObjectIdInfo(
                propName,
                Object.class,
                ObjectIdGenerators.IntSequenceGenerator.class,
                false,
                SimpleObjectIdResolver.class
        );

        assertFalse(initial.getAlwaysAsId());

        // Branch: _alwaysAsId == state -> returns same instance
        ObjectIdInfo same = initial.withAlwaysAsId(false);
        assertSame(initial, same);

        // Branch: _alwaysAsId != state -> returns new instance with state updated
        ObjectIdInfo toggled = initial.withAlwaysAsId(true);
        assertNotSame(initial, toggled);
        assertTrue(toggled.getAlwaysAsId());
        assertEquals(initial.getPropertyName(), toggled.getPropertyName());
        assertEquals(initial.getScope(), toggled.getScope());
        assertEquals(initial.getGeneratorType(), toggled.getGeneratorType());
        assertEquals(initial.getResolverType(), toggled.getResolverType());

        // Toggled back to original state
        ObjectIdInfo toggledBack = toggled.withAlwaysAsId(false);
        assertNotSame(toggled, toggledBack);
        assertFalse(toggledBack.getAlwaysAsId());

        // Same state idempotent return
        assertSame(toggled, toggled.withAlwaysAsId(true));
    }

    @Test(timeout = 4000)
    public void testFourArgConstructorWithResolver() {
        PropertyName propName = new PropertyName("item");
        ObjectIdInfo info = new ObjectIdInfo(
                propName,
                Long.class,
                ObjectIdGenerators.UUIDGenerator.class,
                CustomResolver.class
        );

        assertEquals(propName, info.getPropertyName());
        assertEquals(Long.class, info.getScope());
        assertEquals(ObjectIdGenerators.UUIDGenerator.class, info.getGeneratorType());
        assertEquals(CustomResolver.class, info.getResolverType());
        assertFalse(info.getAlwaysAsId());
    }

    @Test(timeout = 4000)
    public void testDeprecatedThreeArgPropertyNameConstructor() {
        PropertyName propName = new PropertyName("legacyProp");
        ObjectIdInfo info = new ObjectIdInfo(
                propName,
                Integer.class,
                ObjectIdGenerators.IntSequenceGenerator.class
        );

        assertEquals(propName, info.getPropertyName());
        assertEquals(Integer.class, info.getScope());
        assertEquals(ObjectIdGenerators.IntSequenceGenerator.class, info.getGeneratorType());
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
        assertFalse(info.getAlwaysAsId());
    }

    @Test(timeout = 4000)
    public void testDeprecatedThreeArgStringConstructor() {
        ObjectIdInfo info = new ObjectIdInfo(
                "strProp",
                Double.class,
                ObjectIdGenerators.PropertyGenerator.class
        );

        assertNotNull(info.getPropertyName());
        assertEquals("strProp", info.getPropertyName().getSimpleName());
        assertEquals(Double.class, info.getScope());
        assertEquals(ObjectIdGenerators.PropertyGenerator.class, info.getGeneratorType());
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
        assertFalse(info.getAlwaysAsId());
    }

    @Test(timeout = 4000)
    public void testProtectedFourArgConstructor() {
        PropertyName propName = new PropertyName("protectedProp");
        ObjectIdInfo info = new ObjectIdInfo(
                propName,
                Void.class,
                ObjectIdGenerators.None.class,
                true
        );

        assertEquals(propName, info.getPropertyName());
        assertEquals(Void.class, info.getScope());
        assertEquals(ObjectIdGenerators.None.class, info.getGeneratorType());
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
        assertTrue(info.getAlwaysAsId());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullResolverDefaultsToSimpleObjectIdResolver() {
        // Resolver == null branch in protected 5-arg constructor
        ObjectIdInfo info = new ObjectIdInfo(
                new PropertyName("test"),
                Object.class,
                ObjectIdGenerators.IntSequenceGenerator.class,
                false,
                null
        );

        assertNotNull(info.getResolverType());
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
    }

    @Test(timeout = 4000)
    public void testFourArgConstructorNullResolverDefaults() {
        // 4-arg public constructor passing null resolver
        ObjectIdInfo info = new ObjectIdInfo(
                new PropertyName("test"),
                Object.class,
                ObjectIdGenerators.IntSequenceGenerator.class,
                null
        );

        assertNotNull(info.getResolverType());
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
    }

    @Test(timeout = 4000)
    public void testAllNullArguments() {
        ObjectIdInfo info = new ObjectIdInfo(
                (PropertyName) null,
                null,
                null,
                false,
                null
        );

        assertNull(info.getPropertyName());
        assertNull(info.getScope());
        assertNull(info.getGeneratorType());
        assertEquals(SimpleObjectIdResolver.class, info.getResolverType());
        assertFalse(info.getAlwaysAsId());
    }

    @Test(timeout = 4000)
    public void testEmptyPropertyNameString() {
        ObjectIdInfo info = new ObjectIdInfo(
                "",
                Object.class,
                ObjectIdGenerators.IntSequenceGenerator.class
        );

        assertNotNull(info.getPropertyName());
        assertEquals("", info.getPropertyName().getSimpleName());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Issue 1607)
    // =========================================================================

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    static class AlwaysClass1607 {
        public int id;
        public int value;

        public AlwaysClass1607() {
            this(1, 13);
        }

        public AlwaysClass1607(int id, int value) {
            this.id = id;
            this.value = value;
        }
    }

    static class Issue1607Bean {
        public List<AlwaysClass1607> alwaysClass = Collections.singletonList(new AlwaysClass1607(1, 13));
        @JsonIdentityReference(alwaysAsId = true)
        public AlwaysClass1607 alwaysProp = new AlwaysClass1607(2, 13);
    }

    /**
     * Targets Defects4J Issue 1607:
     * When @JsonIdentityReference(alwaysAsId=true) is placed on a Class, elements within
     * collections/arrays must be serialized as scalar IDs (e.g. [1]) rather than expanded POJOs ([{"id":1,...}]).
     */
    @Test(timeout = 4000)
    public void testIssue1607AlwaysAsIdOnClassInCollection() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Issue1607Bean bean = new Issue1607Bean();
        String json = mapper.writeValueAsString(bean);

        assertEquals("{\"alwaysClass\":[1],\"alwaysProp\":2}", json);
    }

    // =========================================================================
    // Partition D: Object Lifecycle, String Representation & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testToStringWithNonNullScopeAndGenerator() {
        PropertyName propName = new PropertyName("orderId");
        ObjectIdInfo info = new ObjectIdInfo(
                propName,
                String.class,
                ObjectIdGenerators.PropertyGenerator.class,
                true,
                SimpleObjectIdResolver.class
        );

        String str = info.toString();
        assertNotNull(str);
        assertTrue(str.startsWith("ObjectIdInfo:"));
        assertTrue(str.contains("propName=" + propName));
        assertTrue(str.contains("scope=" + String.class.getName()));
        assertTrue(str.contains("generatorType=" + ObjectIdGenerators.PropertyGenerator.class.getName()));
        assertTrue(str.contains("alwaysAsId=true"));
    }

    @Test(timeout = 4000)
    public void testToStringWithNullScopeAndNullGenerator() {
        ObjectIdInfo info = new ObjectIdInfo(
                null,
                null,
                null,
                false,
                null
        );

        String str = info.toString();
        assertNotNull(str);
        assertTrue(str.contains("propName=null"));
        assertTrue(str.contains("scope=null"));
        assertTrue(str.contains("generatorType=null"));
        assertTrue(str.contains("alwaysAsId=false"));
    }

    @Test(timeout = 4000)
    public void testToStringWithNullScopeOnly() {
        ObjectIdInfo info = new ObjectIdInfo(
                new PropertyName("field"),
                null,
                ObjectIdGenerators.IntSequenceGenerator.class,
                false
        );

        String str = info.toString();
        assertTrue(str.contains("scope=null"));
        assertTrue(str.contains("generatorType=" + ObjectIdGenerators.IntSequenceGenerator.class.getName()));
    }

    @Test(timeout = 4000)
    public void testToStringWithNullGeneratorOnly() {
        ObjectIdInfo info = new ObjectIdInfo(
                new PropertyName("field"),
                Integer.class,
                null,
                false
        );

        String str = info.toString();
        assertTrue(str.contains("scope=" + Integer.class.getName()));
        assertTrue(str.contains("generatorType=null"));
    }
}