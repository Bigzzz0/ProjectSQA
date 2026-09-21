package org.mockito.internal.configuration;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.*;

import org.mockito.internal.configuration.injection.*;
import org.mockito.internal.util.reflection.FieldInitializer;
import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.base.MockitoException;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: DefaultInjectionEngine.injectMocksOnFields
 * 
 * Decision branches covered:
 * - Loop over injectMocksFields (Set<Field>)
 * - try-catch for MockitoException from FieldInitializer
 * - While loop over class hierarchy (fieldClass != Object.class)
 * - For loop over orderedInstanceFieldsFrom (sorted by supertypesLast comparator)
 * - mockCandidateFilter.filterCandidate(...).thenInject() chain
 * 
 * Boundary conditions:
 * - Empty injectMocksFields set
 * - Empty mocks set
 * - Null testClassInstance (should throw NPE)
 * - Field type without default constructor (MockitoException path)
 * - Multiple mocks of same type
 * - Mocks of exact type vs supertype (defect-targeted)
 * - Fields in superclass and subclass (ordering)
 * 
 * Defect-targeted scenario (from Defects4J):
 * - Injection by type should first look for exact type then ancestor.
 * - When a field of type SuperType has mocks for both SuperType and SubType,
 *   the exact type mock (SuperType) must be injected, not the subtype.
 * - The bug causes the subtype mock to be injected instead.
 */
public class DefaultInjectionEngineDeepseekTest {

    // --- Helper types for injection scenarios ---
    static class SuperType {
        // marker
    }
    static class SubType extends SuperType {
        // marker
    }

    static class InjectedTarget {
        SuperType superField;
        SubType subField;
    }

    static class TestClassWithInjectMocks {
        InjectedTarget injected;
    }

    static class NoDefaultConstructor {
        @SuppressWarnings("unused")
        private final int x;
        public NoDefaultConstructor(int x) { this.x = x; }
    }

    static class TestClassWithNoDefaultCtor {
        NoDefaultConstructor field;
    }

    // --- Test instances ---
    private final DefaultInjectionEngine engine = new DefaultInjectionEngine();

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testBasicInjectionWithExactTypeMatch() throws Exception {
        // Setup: test class with @InjectMocks field, mocks for exact type
        TestClassWithInjectMocks testInstance = new TestClassWithInjectMocks();
        Field injectField = TestClassWithInjectMocks.class.getDeclaredField("injected");
        injectField.setAccessible(true);
        injectField.set(testInstance, null); // ensure null for initialization

        Set<Field> injectFields = new HashSet<>();
        injectFields.add(injectField);

        // Create mocks: one for SuperType (exact match for superField)
        SuperType superMock = new SuperType() {};
        Set<Object> mocks = new HashSet<>();
        mocks.add(superMock);

        engine.injectMocksOnFields(injectFields, mocks, testInstance);

        InjectedTarget injectedInstance = (InjectedTarget) injectField.get(testInstance);
        assertNotNull("Injected field should be initialized", injectedInstance);
        assertSame("superField should be injected with exact type mock",
                superMock, injectedInstance.superField);
        assertNull("subField should remain null (no mock for SubType)",
                injectedInstance.subField);
    }

    @Test(timeout = 4000)
    public void testInjectionWithMultipleFieldsInHierarchy() throws Exception {
        // Use a class with fields in superclass and subclass to test ordering
        static class Base {
            SuperType baseField;
        }
        static class Derived extends Base {
            SubType derivedField;
        }
        static class TestClassHierarchy {
            Derived injected;
        }

        TestClassHierarchy testInstance = new TestClassHierarchy();
        Field injectField = TestClassHierarchy.class.getDeclaredField("injected");
        injectField.setAccessible(true);
        injectField.set(testInstance, null);

        Set<Field> injectFields = new HashSet<>();
        injectFields.add(injectField);

        SuperType superMock = new SuperType() {};
        SubType subMock = new SubType() {};
        Set<Object> mocks = new HashSet<>();
        mocks.add(superMock);
        mocks.add(subMock);

        engine.injectMocksOnFields(injectFields, mocks, testInstance);

        Derived injectedInstance = (Derived) injectField.get(testInstance);
        assertNotNull(injectedInstance);
        assertSame("baseField should get SuperType mock", superMock, injectedInstance.baseField);
        assertSame("derivedField should get SubType mock", subMock, injectedInstance.derivedField);
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testEmptyInjectMocksFields() throws Exception {
        Set<Field> emptyFields = new HashSet<>();
        Set<Object> mocks = new HashSet<>();
        mocks.add(new Object());
        // Should not throw
        engine.injectMocksOnFields(emptyFields, mocks, new Object());
    }

    @Test(timeout = 4000)
    public void testEmptyMocksSet() throws Exception {
        TestClassWithInjectMocks testInstance = new TestClassWithInjectMocks();
        Field injectField = TestClassWithInjectMocks.class.getDeclaredField("injected");
        injectField.setAccessible(true);
        injectField.set(testInstance, null);

        Set<Field> injectFields = new HashSet<>();
        injectFields.add(injectField);

        Set<Object> emptyMocks = new HashSet<>();
        engine.injectMocksOnFields(injectFields, emptyMocks, testInstance);

        InjectedTarget injectedInstance = (InjectedTarget) injectField.get(testInstance);
        assertNotNull(injectedInstance);
        assertNull("superField should remain null (no mocks)", injectedInstance.superField);
        assertNull("subField should remain null", injectedInstance.subField);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testNullTestClassInstance() {
        Set<Field> fields = new HashSet<>();
        Set<Object> mocks = new HashSet<>();
        mocks.add(new Object());
        engine.injectMocksOnFields(fields, mocks, null);
    }

    // ==================== Partition C: Defect-Targeted Branch ====================

    @Test(timeout = 4000)
    public void testExactTypeShouldBePreferredOverSupertype() throws Exception {
        // This test directly targets the known defect:
        // When a field of type SuperType has mocks for both SuperType and SubType,
        // the exact type mock (SuperType) must be injected.
        // The bug causes the SubType mock to be injected instead.
        TestClassWithInjectMocks testInstance = new TestClassWithInjectMocks();
        Field injectField = TestClassWithInjectMocks.class.getDeclaredField("injected");
        injectField.setAccessible(true);
        injectField.set(testInstance, null);

        Set<Field> injectFields = new HashSet<>();
        injectFields.add(injectField);

        SuperType superMock = new SuperType() {};
        SubType subMock = new SubType() {};
        Set<Object> mocks = new HashSet<>();
        mocks.add(superMock);
        mocks.add(subMock);

        engine.injectMocksOnFields(injectFields, mocks, testInstance);

        InjectedTarget injectedInstance = (InjectedTarget) injectField.get(testInstance);
        assertNotNull(injectedInstance);
        // The exact type for superField is SuperType, so superMock must be injected
        assertSame("Exact type mock (SuperType) should be injected into superField",
                superMock, injectedInstance.superField);
        // subField should get SubType mock (exact match)
        assertSame("SubType mock should be injected into subField",
                subMock, injectedInstance.subField);
    }

    @Test(timeout = 4000)
    public void testSubtypeMockNotInjectedIntoSupertypeFieldWhenExactMatchExists() throws Exception {
        // Variation: only one mock of SubType, no SuperType mock.
        // The field type is SuperType, so SubType is assignable but not exact.
        // The engine should still inject the SubType mock because it's the only candidate.
        // This tests that the filter does not reject assignable types.
        TestClassWithInjectMocks testInstance = new TestClassWithInjectMocks();
        Field injectField = TestClassWithInjectMocks.class.getDeclaredField("injected");
        injectField.setAccessible(true);
        injectField.set(testInstance, null);

        Set<Field> injectFields = new HashSet<>();
        injectFields.add(injectField);

        SubType subMock = new SubType() {};
        Set<Object> mocks = new HashSet<>();
        mocks.add(subMock);

        engine.injectMocksOnFields(injectFields, mocks, testInstance);

        InjectedTarget injectedInstance = (InjectedTarget) injectField.get(testInstance);
        assertNotNull(injectedInstance);
        // Only candidate is SubType, so it should be injected into superField
        assertSame("SubType mock should be injected into superField (only candidate)",
                subMock, injectedInstance.superField);
        assertSame("SubType mock should also be injected into subField",
                subMock, injectedInstance.subField);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testFieldInitializerThrowsException() throws Exception {
        // Field type without default constructor should cause MockitoException
        TestClassWithNoDefaultCtor testInstance = new TestClassWithNoDefaultCtor();
        Field injectField = TestClassWithNoDefaultCtor.class.getDeclaredField("field");
        injectField.setAccessible(true);
        injectField.set(testInstance, null);

        Set<Field> injectFields = new HashSet<>();
        injectFields.add(injectField);

        Set<Object> mocks = new HashSet<>();
        mocks.add(new Object());

        // This should throw MockitoException (caught and re-thrown by Reporter)
        engine.injectMocksOnFields(injectFields, mocks, testInstance);
    }

    @Test(timeout = 4000)
    public void testMultipleInjectMocksFields() throws Exception {
        // Test that mocks are injected only once (each mock used at most once)
        static class MultiTarget {
            SuperType field1;
            SuperType field2;
        }
        static class TestMulti {
            MultiTarget target1;
            MultiTarget target2;
        }

        TestMulti testInstance = new TestMulti();
        Field field1 = TestMulti.class.getDeclaredField("target1");
        field1.setAccessible(true);
        field1.set(testInstance, null);
        Field field2 = TestMulti.class.getDeclaredField("target2");
        field2.setAccessible(true);
        field2.set(testInstance, null);

        Set<Field> injectFields = new HashSet<>();
        injectFields.add(field1);
        injectFields.add(field2);

        SuperType mock = new SuperType() {};
        Set<Object> mocks = new HashSet<>();
        mocks.add(mock); // only one mock

        engine.injectMocksOnFields(injectFields, mocks, testInstance);

        MultiTarget target1 = (MultiTarget) field1.get(testInstance);
        MultiTarget target2 = (MultiTarget) field2.get(testInstance);
        assertNotNull(target1);
        assertNotNull(target2);
        // The single mock should be injected into one of the fields (first processed)
        // The other field should remain null because mock is removed after injection
        boolean oneInjected = (target1.field1 == mock) ^ (target2.field1 == mock);
        assertTrue("Mock should be injected into exactly one field", oneInjected);
        assertNull("The other field should have null", 
                (target1.field1 == mock) ? target2.field1 : target1.field1);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    // Not applicable for this engine; no equals/hashCode/clone/serialization.
    // However, we can test that the engine does not modify the input sets.

    @Test(timeout = 4000)
    public void testInputSetsAreNotModified() throws Exception {
        TestClassWithInjectMocks testInstance = new TestClassWithInjectMocks();
        Field injectField = TestClassWithInjectMocks.class.getDeclaredField("injected");
        injectField.setAccessible(true);
        injectField.set(testInstance, null);

        Set<Field> originalFields = new HashSet<>();
        originalFields.add(injectField);
        Set<Field> fieldsCopy = new HashSet<>(originalFields);

        Set<Object> originalMocks = new HashSet<>();
        SuperType mock = new SuperType() {};
        originalMocks.add(mock);
        Set<Object> mocksCopy = new HashSet<>(originalMocks);

        engine.injectMocksOnFields(originalFields, originalMocks, testInstance);

        assertEquals("Fields set should not be modified", fieldsCopy, originalFields);
        assertEquals("Mocks set should not be modified", mocksCopy, originalMocks);
    }
}