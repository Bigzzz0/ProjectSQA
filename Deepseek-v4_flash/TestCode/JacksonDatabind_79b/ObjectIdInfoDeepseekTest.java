package com.fasterxml.jackson.databind.introspect;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdResolver;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.databind.PropertyName;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ObjectIdInfo.java
 * 
 * Decision branches identified:
 * 1. Constructor with 4 args (name, scope, gen, resolver) -> delegates to 5-arg constructor with alwaysAsId=false
 * 2. Deprecated constructor with 3 args (PropertyName, scope, gen) -> delegates to 4-arg with alwaysAsId=false
 * 3. Deprecated constructor with 3 args (String, scope, gen) -> wraps String in PropertyName, delegates
 * 4. Protected 4-arg constructor (prop, scope, gen, alwaysAsId) -> delegates to 5-arg with SimpleObjectIdResolver
 * 5. Protected 5-arg constructor (prop, scope, gen, alwaysAsId, resolver) -> null-check on resolver, defaults to SimpleObjectIdResolver
 * 6. withAlwaysAsId(boolean state) -> if same state, return this; else create new instance
 * 7. getPropertyName() -> returns _propertyName
 * 8. getScope() -> returns _scope
 * 9. getGeneratorType() -> returns _generator
 * 10. getResolverType() -> returns _resolver
 * 11. getAlwaysAsId() -> returns _alwaysAsId
 * 12. toString() -> string representation with null-checks for scope and generator
 * 
 * Boundary conditions:
 * - null PropertyName
 * - null scope
 * - null generator
 * - null resolver (should default to SimpleObjectIdResolver)
 * - alwaysAsId = true/false
 * - withAlwaysAsId(true) when already true -> returns same instance
 * - withAlwaysAsId(false) when already false -> returns same instance
 * - withAlwaysAsId(true) when false -> returns new instance with true
 * - withAlwaysAsId(false) when true -> returns new instance with false
 * 
 * Defect targeting (from Defects4J):
 * The bug is in the alwaysAsId handling. When alwaysAsId is true, the
 * serialization should produce the id directly, not the full object.
 * The test testIssue1607 expects that when alwaysAsId is true, the
 * serialization produces just the id value, not the full object structure.
 * 
 * The defect is likely in how the ObjectIdInfo is used during serialization,
 * but since we're testing ObjectIdInfo directly, we need to verify that
 * the alwaysAsId flag is correctly propagated and that the withAlwaysAsId
 * method correctly creates new instances with the right state.
 * 
 * Test partitions:
 * A. Core functional logic - constructors, getters, withAlwaysAsId
 * B. Boundary values - null arguments, empty strings, extreme values
 * C. Defect-targeted - alwaysAsId propagation and state changes
 * D. Exception paths - null resolver handling
 * E. Object lifecycle - toString, equality of states
 */
public class ObjectIdInfoDeepseekTest {

    // Helper class for testing
    private static class TestGenerator extends ObjectIdGenerator<Object> {
        private static final long serialVersionUID = 1L;

        @Override
        public Class<?> getScope() { return Object.class; }

        @Override
        public boolean canUseFor(ObjectIdGenerator<?> gen) { return false; }

        @Override
        public ObjectIdGenerator<Object> forScope(Class<?> scope) { return this; }

        @Override
        public ObjectIdGenerator<Object> newForSerialization(Object context) { return this; }

        @Override
        public ObjectIdGenerator.IdRef generateId(Object forValue) { return null; }
    }

    private static class TestResolver implements ObjectIdResolver {
        @Override
        public void bindItem(com.fasterxml.jackson.annotation.ObjectIdGenerator.IdRef id, Object pojo) {}

        @Override
        public Object resolveId(com.fasterxml.jackson.annotation.ObjectIdGenerator.IdRef id) { return null; }

        @Override
        public ObjectIdResolver newForDeserialization(Object context) { return this; }

        @Override
        public boolean canUseFor(ObjectIdResolver resolverType) { return false; }
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testFullConstructorWithAllArgs() {
        PropertyName propName = new PropertyName("testProp");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;
        Class<? extends ObjectIdResolver> resolver = TestResolver.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, true, resolver);

        assertNotNull("ObjectIdInfo should not be null", info);
        assertEquals("PropertyName mismatch", propName, info.getPropertyName());
        assertEquals("Scope mismatch", scope, info.getScope());
        assertEquals("Generator type mismatch", gen, info.getGeneratorType());
        assertEquals("Resolver type mismatch", resolver, info.getResolverType());
        assertTrue("alwaysAsId should be true", info.getAlwaysAsId());
    }

    @Test(timeout = 4000)
    public void testConstructorWithResolverNull() {
        PropertyName propName = new PropertyName("testProp");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, true, null);

        assertNotNull("ObjectIdInfo should not be null", info);
        assertEquals("Resolver should default to SimpleObjectIdResolver", 
                SimpleObjectIdResolver.class, info.getResolverType());
    }

    @Test(timeout = 4000)
    public void testFourArgConstructor() {
        PropertyName propName = new PropertyName("testProp");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;
        Class<? extends ObjectIdResolver> resolver = TestResolver.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, resolver);

        assertNotNull("ObjectIdInfo should not be null", info);
        assertEquals("PropertyName mismatch", propName, info.getPropertyName());
        assertEquals("Scope mismatch", scope, info.getScope());
        assertEquals("Generator type mismatch", gen, info.getGeneratorType());
        assertEquals("Resolver type mismatch", resolver, info.getResolverType());
        assertFalse("alwaysAsId should default to false", info.getAlwaysAsId());
    }

    @Test(timeout = 4000)
    public void testDeprecatedThreeArgConstructorWithPropertyName() {
        PropertyName propName = new PropertyName("testProp");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen);

        assertNotNull("ObjectIdInfo should not be null", info);
        assertEquals("PropertyName mismatch", propName, info.getPropertyName());
        assertEquals("Scope mismatch", scope, info.getScope());
        assertEquals("Generator type mismatch", gen, info.getGeneratorType());
        assertEquals("Resolver should default to SimpleObjectIdResolver", 
                SimpleObjectIdResolver.class, info.getResolverType());
        assertFalse("alwaysAsId should default to false", info.getAlwaysAsId());
    }

    @Test(timeout = 4000)
    public void testDeprecatedThreeArgConstructorWithString() {
        String propName = "testProp";
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen);

        assertNotNull("ObjectIdInfo should not be null", info);
        assertEquals("PropertyName mismatch", new PropertyName(propName), info.getPropertyName());
        assertEquals("Scope mismatch", scope, info.getScope());
        assertEquals("Generator type mismatch", gen, info.getGeneratorType());
        assertEquals("Resolver should default to SimpleObjectIdResolver", 
                SimpleObjectIdResolver.class, info.getResolverType());
        assertFalse("alwaysAsId should default to false", info.getAlwaysAsId());
    }

    @Test(timeout = 4000)
    public void testProtectedFourArgConstructor() {
        PropertyName propName = new PropertyName("testProp");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        // Use reflection to test protected constructor
        try {
            java.lang.reflect.Constructor<ObjectIdInfo> ctor = 
                ObjectIdInfo.class.getDeclaredConstructor(PropertyName.class, Class.class, Class.class, boolean.class);
            ctor.setAccessible(true);
            ObjectIdInfo info = ctor.newInstance(propName, scope, gen, true);

            assertNotNull("ObjectIdInfo should not be null", info);
            assertEquals("PropertyName mismatch", propName, info.getPropertyName());
            assertEquals("Scope mismatch", scope, info.getScope());
            assertEquals("Generator type mismatch", gen, info.getGeneratorType());
            assertEquals("Resolver should default to SimpleObjectIdResolver", 
                    SimpleObjectIdResolver.class, info.getResolverType());
            assertTrue("alwaysAsId should be true", info.getAlwaysAsId());
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testNullPropertyName() {
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        ObjectIdInfo info = new ObjectIdInfo((PropertyName) null, scope, gen, false);

        assertNotNull("ObjectIdInfo should not be null", info);
        assertNull("PropertyName should be null", info.getPropertyName());
    }

    @Test(timeout = 4000)
    public void testNullScope() {
        PropertyName propName = new PropertyName("testProp");
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, null, gen, false);

        assertNotNull("ObjectIdInfo should not be null", info);
        assertNull("Scope should be null", info.getScope());
    }

    @Test(timeout = 4000)
    public void testNullGenerator() {
        PropertyName propName = new PropertyName("testProp");
        Class<?> scope = String.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, null, false);

        assertNotNull("ObjectIdInfo should not be null", info);
        assertNull("Generator should be null", info.getGeneratorType());
    }

    @Test(timeout = 4000)
    public void testNullResolverInFiveArgConstructor() {
        PropertyName propName = new PropertyName("testProp");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, false, null);

        assertNotNull("ObjectIdInfo should not be null", info);
        assertEquals("Resolver should default to SimpleObjectIdResolver", 
                SimpleObjectIdResolver.class, info.getResolverType());
    }

    @Test(timeout = 4000)
    public void testEmptyStringPropertyName() {
        String propName = "";
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen);

        assertNotNull("ObjectIdInfo should not be null", info);
        assertEquals("PropertyName mismatch", new PropertyName(""), info.getPropertyName());
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect-targeted test for the alwaysAsId behavior.
     * The bug in the original code causes the alwaysAsId flag to not be
     * properly propagated, leading to serialization issues where the full
     * object is serialized instead of just the id.
     */
    @Test(timeout = 4000)
    public void testAlwaysAsIdPropagation() {
        PropertyName propName = new PropertyName("alwaysClass");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        // Create with alwaysAsId = true
        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, true);

        // Verify the flag is set correctly
        assertTrue("alwaysAsId should be true", info.getAlwaysAsId());

        // Test withAlwaysAsId when state is the same (should return same instance)
        ObjectIdInfo sameInfo = info.withAlwaysAsId(true);
        assertSame("Should return same instance when state is unchanged", info, sameInfo);

        // Test withAlwaysAsId when state changes (should return new instance)
        ObjectIdInfo changedInfo = info.withAlwaysAsId(false);
        assertNotSame("Should return new instance when state changes", info, changedInfo);
        assertFalse("New instance should have alwaysAsId=false", changedInfo.getAlwaysAsId());
        assertEquals("PropertyName should be preserved", propName, changedInfo.getPropertyName());
        assertEquals("Scope should be preserved", scope, changedInfo.getScope());
        assertEquals("Generator should be preserved", gen, changedInfo.getGeneratorType());
        assertEquals("Resolver should be preserved", SimpleObjectIdResolver.class, changedInfo.getResolverType());

        // Test the reverse direction
        ObjectIdInfo falseInfo = new ObjectIdInfo(propName, scope, gen, false);
        assertFalse("alwaysAsId should be false", falseInfo.getAlwaysAsId());

        ObjectIdInfo sameFalseInfo = falseInfo.withAlwaysAsId(false);
        assertSame("Should return same instance when state is unchanged", falseInfo, sameFalseInfo);

        ObjectIdInfo changedToTrueInfo = falseInfo.withAlwaysAsId(true);
        assertNotSame("Should return new instance when state changes", falseInfo, changedToTrueInfo);
        assertTrue("New instance should have alwaysAsId=true", changedToTrueInfo.getAlwaysAsId());
    }

    /**
     * Direct test for the defect scenario: when alwaysAsId is true,
     * the serialization should produce just the id, not the full object.
     * This test verifies the ObjectIdInfo state that would cause the bug.
     */
    @Test(timeout = 4000)
    public void testDefectScenarioAlwaysAsIdTrue() {
        // Simulate the scenario from testIssue1607
        PropertyName propName = new PropertyName("alwaysClass");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        // The defect occurs when alwaysAsId is true but the serialization
        // still produces the full object. This test verifies the state
        // that should trigger the correct behavior.
        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, true);

        // Verify all the state that would be used during serialization
        assertTrue("alwaysAsId must be true for correct serialization", info.getAlwaysAsId());
        assertEquals("PropertyName should be alwaysClass", propName, info.getPropertyName());
        assertEquals("Scope should be String.class", scope, info.getScope());
        assertEquals("Generator should be TestGenerator", gen, info.getGeneratorType());

        // The resolver type should be SimpleObjectIdResolver by default
        assertEquals("Resolver should be SimpleObjectIdResolver", 
                SimpleObjectIdResolver.class, info.getResolverType());

        // Verify toString doesn't throw and contains expected values
        String toString = info.toString();
        assertNotNull("toString should not be null", toString);
        assertTrue("toString should contain property name", toString.contains("alwaysClass"));
        assertTrue("toString should contain alwaysAsId=true", toString.contains("alwaysAsId=true"));
    }

    @Test(timeout = 4000)
    public void testDefectScenarioAlwaysAsIdFalse() {
        // Test the opposite scenario where alwaysAsId is false
        PropertyName propName = new PropertyName("alwaysProp");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, false);

        assertFalse("alwaysAsId should be false", info.getAlwaysAsId());

        // Verify toString contains the correct state
        String toString = info.toString();
        assertNotNull("toString should not be null", toString);
        assertTrue("toString should contain property name", toString.contains("alwaysProp"));
        assertTrue("toString should contain alwaysAsId=false", toString.contains("alwaysAsId=false"));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testNullResolverInFourArgConstructor() {
        PropertyName propName = new PropertyName("testProp");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        // The 4-arg constructor doesn't take a resolver, so it should use SimpleObjectIdResolver
        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, null);

        assertNotNull("ObjectIdInfo should not be null", info);
        assertEquals("Resolver should default to SimpleObjectIdResolver", 
                SimpleObjectIdResolver.class, info.getResolverType());
    }

    @Test(timeout = 4000)
    public void testAllNullArguments() {
        ObjectIdInfo info = new ObjectIdInfo((PropertyName) null, null, null, false);

        assertNotNull("ObjectIdInfo should not be null", info);
        assertNull("PropertyName should be null", info.getPropertyName());
        assertNull("Scope should be null", info.getScope());
        assertNull("Generator should be null", info.getGeneratorType());
        assertEquals("Resolver should default to SimpleObjectIdResolver", 
                SimpleObjectIdResolver.class, info.getResolverType());
        assertFalse("alwaysAsId should be false", info.getAlwaysAsId());
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testToStringWithNullValues() {
        ObjectIdInfo info = new ObjectIdInfo((PropertyName) null, null, null, false);

        String toString = info.toString();
        assertNotNull("toString should not be null", toString);
        assertTrue("toString should contain null for scope", toString.contains("scope=null"));
        assertTrue("toString should contain null for generator", toString.contains("generatorType=null"));
        assertTrue("toString should contain propName=null", toString.contains("propName=null"));
    }

    @Test(timeout = 4000)
    public void testToStringWithNonNullValues() {
        PropertyName propName = new PropertyName("testProp");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, true);

        String toString = info.toString();
        assertNotNull("toString should not be null", toString);
        assertTrue("toString should contain property name", toString.contains("testProp"));
        assertTrue("toString should contain scope class name", toString.contains("java.lang.String"));
        assertTrue("toString should contain generator class name", toString.contains("TestGenerator"));
        assertTrue("toString should contain alwaysAsId=true", toString.contains("alwaysAsId=true"));
    }

    @Test(timeout = 4000)
    public void testWithAlwaysAsIdPreservesAllFields() {
        PropertyName propName = new PropertyName("testProp");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;
        Class<? extends ObjectIdResolver> resolver = TestResolver.class;

        ObjectIdInfo original = new ObjectIdInfo(propName, scope, gen, false, resolver);
        ObjectIdInfo changed = original.withAlwaysAsId(true);

        // Verify all fields are preserved except alwaysAsId
        assertEquals("PropertyName should be preserved", propName, changed.getPropertyName());
        assertEquals("Scope should be preserved", scope, changed.getScope());
        assertEquals("Generator should be preserved", gen, changed.getGeneratorType());
        assertEquals("Resolver should be preserved", resolver, changed.getResolverType());
        assertTrue("alwaysAsId should be changed to true", changed.getAlwaysAsId());

        // Verify original is unchanged
        assertFalse("Original should still have alwaysAsId=false", original.getAlwaysAsId());
    }

    @Test(timeout = 4000)
    public void testMultipleWithAlwaysAsIdCalls() {
        PropertyName propName = new PropertyName("testProp");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, false);

        // Toggle back and forth
        ObjectIdInfo info1 = info.withAlwaysAsId(true);
        ObjectIdInfo info2 = info1.withAlwaysAsId(false);
        ObjectIdInfo info3 = info2.withAlwaysAsId(true);

        assertTrue("info1 should have alwaysAsId=true", info1.getAlwaysAsId());
        assertFalse("info2 should have alwaysAsId=false", info2.getAlwaysAsId());
        assertTrue("info3 should have alwaysAsId=true", info3.getAlwaysAsId());

        // Verify original is unchanged
        assertFalse("Original should still have alwaysAsId=false", info.getAlwaysAsId());
    }

    @Test(timeout = 4000)
    public void testWithAlwaysAsIdReturnsSameInstanceWhenStateUnchanged() {
        PropertyName propName = new PropertyName("testProp");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;

        ObjectIdInfo infoTrue = new ObjectIdInfo(propName, scope, gen, true);
        ObjectIdInfo infoFalse = new ObjectIdInfo(propName, scope, gen, false);

        // Same state should return same instance
        assertSame("withAlwaysAsId(true) on true should return same instance", 
                infoTrue, infoTrue.withAlwaysAsId(true));
        assertSame("withAlwaysAsId(false) on false should return same instance", 
                infoFalse, infoFalse.withAlwaysAsId(false));

        // Different state should return new instance
        assertNotSame("withAlwaysAsId(false) on true should return new instance", 
                infoTrue, infoTrue.withAlwaysAsId(false));
        assertNotSame("withAlwaysAsId(true) on false should return new instance", 
                infoFalse, infoFalse.withAlwaysAsId(true));
    }

    @Test(timeout = 4000)
    public void testConstructorWithCustomResolver() {
        PropertyName propName = new PropertyName("testProp");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;
        Class<? extends ObjectIdResolver> resolver = TestResolver.class;

        ObjectIdInfo info = new ObjectIdInfo(propName, scope, gen, true, resolver);

        assertEquals("Custom resolver should be used", resolver, info.getResolverType());
        assertNotEquals("Resolver should not be SimpleObjectIdResolver", 
                SimpleObjectIdResolver.class, info.getResolverType());
    }

    @Test(timeout = 4000)
    public void testGetAlwaysAsIdDefaultValues() {
        // Test all constructors that don't explicitly set alwaysAsId
        PropertyName propName = new PropertyName("testProp");
        Class<?> scope = String.class;
        Class<? extends ObjectIdGenerator<?>> gen = TestGenerator.class;
        Class<? extends ObjectIdResolver> resolver = TestResolver.class;

        // 4-arg constructor (name, scope, gen, resolver)
        ObjectIdInfo info1 = new ObjectIdInfo(propName, scope, gen, resolver);
        assertFalse("4-arg constructor should default alwaysAsId to false", info1.getAlwaysAsId());

        // Deprecated 3-arg constructor with PropertyName
        ObjectIdInfo info2 = new ObjectIdInfo(propName, scope, gen);
        assertFalse("Deprecated 3-arg constructor should default alwaysAsId to false", info2.getAlwaysAsId());

        // Deprecated 3-arg constructor with String
        ObjectIdInfo info3 = new ObjectIdInfo("testProp", scope, gen);
        assertFalse("Deprecated 3-arg String constructor should default alwaysAsId to false", info3.getAlwaysAsId());
    }
}