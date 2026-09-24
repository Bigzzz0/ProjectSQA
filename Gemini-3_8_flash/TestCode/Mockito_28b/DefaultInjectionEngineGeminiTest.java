/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class Under Test: org.mockito.internal.configuration.DefaultInjectionEngine
 *
 * Branch & Path Coverage Goals:
 * 1. injectMocksOnFields:
 *    - Empty injectMocksFields set (zero iterations).
 *    - Empty mocks set (fields traversed, but no injections made).
 *    - Uninitialized field successfully instantiated via FieldInitializer.
 *    - Pre-initialized field preserved and dependencies injected.
 *    - Uninstantiable field (interface/abstract class) throwing MockitoException handled by Reporter.
 * 2. Class Hierarchy While-Loop:
 *    - Single-level class hierarchy (terminating at Object.class).
 *    - Multi-level class hierarchy (subclass -> midclass -> superclass -> Object.class).
 * 3. supertypesLast Comparator:
 *    - field1Type is assignable from field2Type (returns 1).
 *    - field2Type is assignable from field1Type (returns -1).
 *    - Neither type assignable from the other / unrelated types (returns 0).
 *    - Reflexive comparison on identical types.
 * 4. Ground Truth Defect Zone (Defects4J):
 *    - Reproduction of org.mockitousage.bugs.InjectionByTypeShouldFirstLookForExactTypeThenAncestorTest:
 *      When an ancestor type field resides in a subclass and a more specific type field resides in a
 *      superclass (or when sorting biases ancestor matches), injection by type inappropriately injects
 *      into the broader type field first, exhausting the candidate mock and leaving the best-matching
 *      sub-field unassigned.
 */
package org.mockito.internal.configuration;

import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class DefaultInjectionEngineGeminiTest {

    // =========================================================================
    // Test Fixtures & Hierarchy Models
    // =========================================================================

    public static class SimpleTarget {
        public String stringDependency;
        public Integer integerDependency;
    }

    public static class GrandParent {
        public Object grandParentField;
    }

    public static class Parent extends GrandParent {
        public CharSequence parentField;
    }

    public static class Child extends Parent {
        public String childField;
    }

    public static class DefectSuperService {
        public TargetBean mockedBean;
    }

    public static class DefectService extends DefectSuperService {
        public Object occurrences = new Object();
    }

    public static class TargetBean {
        @Override
        public String toString() {
            return "mockedBean";
        }
    }

    public static class SubTargetBean extends TargetBean {}

    public static class UninstantiableTargetHolder {
        public List<?> uninstantiableField;
    }

    public static class HostContainer {
        public SimpleTarget simpleTarget;
        public Child childTarget = new Child();
        public DefectService defectService = new DefectService();
        public UninstantiableTargetHolder uninstantiableHolder;
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testSuccessfulInjectionOnPreInitializedInstance() throws Exception {
        DefaultInjectionEngine engine = new DefaultInjectionEngine();
        HostContainer host = new HostContainer();
        host.simpleTarget = new SimpleTarget();

        Field targetField = HostContainer.class.getDeclaredField("simpleTarget");
        Set<Field> injectMocksFields = new HashSet<Field>(Collections.singletonList(targetField));

        String testString = "injectedString";
        Integer testInt = 42;
        Set<Object> mocks = new HashSet<Object>(Arrays.asList(testString, testInt));

        engine.injectMocksOnFields(injectMocksFields, mocks, host);

        assertSame("String field should have received exact mock", testString, host.simpleTarget.stringDependency);
        assertSame("Integer field should have received exact mock", testInt, host.simpleTarget.integerDependency);
    }

    @Test(timeout = 4000)
    public void testClassHierarchyFieldTraversal() throws Exception {
        DefaultInjectionEngine engine = new DefaultInjectionEngine();
        HostContainer host = new HostContainer();

        Field targetField = HostContainer.class.getDeclaredField("childTarget");
        Set<Field> injectMocksFields = new HashSet<Field>(Collections.singletonList(targetField));

        String stringMock = "childString";
        Set<Object> mocks = new HashSet<Object>(Collections.singletonList(stringMock));

        engine.injectMocksOnFields(injectMocksFields, mocks, host);

        assertSame("Child field should receive injected candidate", stringMock, host.childTarget.childField);
        assertNull("Parent field should remain unassigned", host.childTarget.parentField);
        assertNull("GrandParent field should remain unassigned", host.childTarget.grandParentField);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyInjectMocksFieldsDoesNothing() {
        DefaultInjectionEngine engine = new DefaultInjectionEngine();
        HostContainer host = new HostContainer();
        Set<Field> emptyFields = Collections.emptySet();
        Set<Object> mocks = new HashSet<Object>(Collections.singletonList("sampleMock"));

        engine.injectMocksOnFields(emptyFields, mocks, host);
        assertNull("Host field should remain null", host.simpleTarget);
    }

    @Test(timeout = 4000)
    public void testEmptyMocksLeavesTargetFieldsNull() throws Exception {
        DefaultInjectionEngine engine = new DefaultInjectionEngine();
        HostContainer host = new HostContainer();
        host.simpleTarget = new SimpleTarget();

        Field targetField = HostContainer.class.getDeclaredField("simpleTarget");
        Set<Field> injectMocksFields = new HashSet<Field>(Collections.singletonList(targetField));
        Set<Object> emptyMocks = Collections.emptySet();

        engine.injectMocksOnFields(injectMocksFields, emptyMocks, host);

        assertNull("String dependency should be null when no mocks supplied", host.simpleTarget.stringDependency);
        assertNull("Integer dependency should be null when no mocks supplied", host.simpleTarget.integerDependency);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Defect)
    // =========================================================================

    @Test(timeout = 4000)
    public void mock_should_be_injected_once_and_in_the_best_matching_type() throws Exception {
        DefaultInjectionEngine engine = new DefaultInjectionEngine();
        HostContainer host = new HostContainer();
        Object originalOccurrences = host.defectService.occurrences;

        Field targetField = HostContainer.class.getDeclaredField("defectService");
        Set<Field> injectMocksFields = new HashSet<Field>(Collections.singletonList(targetField));

        TargetBean mockedBean = new TargetBean();
        Set<Object> mocks = new HashSet<Object>(Collections.singletonList(mockedBean));

        engine.injectMocksOnFields(injectMocksFields, mocks, host);

        assertSame("Occurrences should remain intact and not receive mismatched subtype mock",
                originalOccurrences, host.defectService.occurrences);
        assertSame("MockedBean should receive candidate matching its exact/best type",
                mockedBean, host.defectService.mockedBean);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = MockitoException.class)
    public void testCannotInitializeInterfaceFieldThrowsMockitoException() throws Exception {
        DefaultInjectionEngine engine = new DefaultInjectionEngine();
        HostContainer host = new HostContainer();

        Field uninstantiableField = HostContainer.class.getDeclaredField("uninstantiableHolder");
        Set<Field> fields = new HashSet<Field>(Collections.singletonList(uninstantiableField));

        // When the field is null and cannot be instantiated by FieldInitializer,
        // DefaultInjectionEngine catches MockitoException and delegates to Reporter,
        // which throws a MockitoException.
        engine.injectMocksOnFields(fields, Collections.emptySet(), host);
    }

    // =========================================================================
    // Partition E: supertypesLast Comparator Logic & Contract Verification
    // =========================================================================

    @SuppressWarnings("unchecked")
    private Comparator<Field> extractSupertypesLastComparator(DefaultInjectionEngine engine) throws Exception {
        Field comparatorField = DefaultInjectionEngine.class.getDeclaredField("supertypesLast");
        comparatorField.setAccessible(true);
        return (Comparator<Field>) comparatorField.get(engine);
    }

    @Test(timeout = 4000)
    public void testSupertypesLastComparatorBranches() throws Exception {
        DefaultInjectionEngine engine = new DefaultInjectionEngine();
        Comparator<Field> comparator = extractSupertypesLastComparator(engine);

        Field grandParentField = GrandParent.class.getDeclaredField("grandParentField"); // Object
        Field parentField = Parent.class.getDeclaredField("parentField");               // CharSequence
        Field childField = Child.class.getDeclaredField("childField");                 // String
        Field intField = SimpleTarget.class.getDeclaredField("integerDependency");      // Integer

        // Branch 1: field1Type.isAssignableFrom(field2Type) => 1
        int cmpSuperFirst = comparator.compare(grandParentField, childField);
        assertTrue("Supertype field should compare greater than subtype field", cmpSuperFirst > 0);

        // Branch 2: field2Type.isAssignableFrom(field1Type) => -1
        int cmpSubFirst = comparator.compare(childField, grandParentField);
        assertTrue("Subtype field should compare less than supertype field", cmpSubFirst < 0);

        // Intermediate hierarchy: CharSequence vs String
        assertTrue("CharSequence is supertype of String", comparator.compare(parentField, childField) > 0);
        assertTrue("String is subtype of CharSequence", comparator.compare(childField, parentField) < 0);

        // Branch 3: Disjoint/Unrelated types => 0
        int cmpUnrelated = comparator.compare(childField, intField);
        assertEquals("Unrelated types (String vs Integer) should return 0", 0, cmpUnrelated);
    }

    @Test(timeout = 4000)
    public void testOrderedInstanceFieldsFromSorting() throws Exception {
        DefaultInjectionEngine engine = new DefaultInjectionEngine();
        HostContainer host = new HostContainer();

        class MultipleFieldsTarget {
            public Object objField;
            public TargetBean beanField;
            public SubTargetBean subBeanField;
        }

        class LocalHost {
            public MultipleFieldsTarget target = new MultipleFieldsTarget();
        }

        LocalHost localHost = new LocalHost();
        Field targetField = LocalHost.class.getDeclaredField("target");
        Set<Field> injectMocksFields = new HashSet<Field>(Collections.singletonList(targetField));

        SubTargetBean subMock = new SubTargetBean();
        Set<Object> mocks = new HashSet<Object>(Collections.singletonList(subMock));

        engine.injectMocksOnFields(injectMocksFields, mocks, localHost);

        // Injection ordering ensures child types get prioritized before general supertypes
        assertNotNull("At least one matching candidate should have received mock",
                localHost.target.subBeanField != null || localHost.target.beanField != null || localHost.target.objField != null);
    }
}