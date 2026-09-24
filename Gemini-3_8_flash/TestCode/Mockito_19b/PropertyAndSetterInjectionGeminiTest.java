package org.mockito.internal.configuration.injection;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: org.mockito.internal.configuration.injection.PropertyAndSetterInjection
 *
 * DECISION BRANCHES & COVERAGE TARGETS:
 * 1. processInjection():
 *    - Field instantiation via initializeInjectMocksField (uninitialized field vs already initialized)
 *    - Class hierarchy loop: (fieldClass != Object.class) -> single-class vs deep hierarchy (subclass -> superclass)
 *    - Injection occurrence accumulator (injectionOccurred |= ...)
 * 2. initializeInjectMocksField():
 *    - Success path (instantiation without errors or pre-existing instance)
 *    - Failure path: MockitoException where cause is InvocationTargetException -> Reporter.fieldInitialisationThrewException
 *    - Failure path: MockitoException general cannot initialize -> Reporter.cannotInitializeForInjectMocksAnnotation
 * 3. injectMockCandidates() & injectMockCandidatesOnFields():
 *    - Pass 1 injection & Pass 2 injection
 *    - Filtered candidate injected != null -> mocks.remove(injected), it.remove()
 *    - Filtered candidate injected == null -> no removal, no injection
 * 4. orderedInstanceFieldsFrom():
 *    - notFinalOrStatic Filter: Static fields excluded
 *    - notFinalOrStatic Filter: Final fields excluded
 *    - Instance non-final fields retained and sorted via SuperTypesLastSorter
 *
 * DEFECT TARGET (Ground Truth: MockInjectionUsingSetterOrPropertyTest::shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable):
 * - Symptom: junit.framework.AssertionFailedError: Expected: <null> but was: candidate2
 * - Root Cause: In processInjection(), newMockSafeHashSet(mockCandidates) is re-instantiated on each
 *   iteration of the while(fieldClass != Object.class) loop. As a consequence, mock candidates that have
 *   already been injected into a child class are not removed from the mock candidate pool when processing
 *   the superclass. When the superclass contains a field of the same type/name, the consumed candidate
 *   is improperly injected a second time into the superclass field rather than remaining null.
 */

import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PropertyAndSetterInjectionGeminiTest {

    // -------------------------------------------------------------------------
    // Test Models & Fixtures
    // -------------------------------------------------------------------------

    static class SuperBeanWithCandidate {
        private String candidate2;

        public String getCandidate2() {
            return candidate2;
        }
    }

    static class SubBeanWithCandidate extends SuperBeanWithCandidate {
        private String candidate2;

        public String getSubCandidate2() {
            return candidate2;
        }
    }

    static class DefectFixtureOwner {
        private SubBeanWithCandidate bean;
    }

    static class SetterInjectionBean {
        private String name;
        private boolean setterInvoked = false;

        public void setName(String name) {
            this.name = name;
            this.setterInvoked = true;
        }

        public String getName() {
            return name;
        }

        public boolean isSetterInvoked() {
            return setterInvoked;
        }
    }

    static class SetterInjectionOwner {
        private SetterInjectionBean bean = new SetterInjectionBean();
    }

    static class FieldDirectInjectionBean {
        private String directValue;

        public String getDirectValue() {
            return directValue;
        }
    }

    static class FieldDirectInjectionOwner {
        private FieldDirectInjectionBean bean = new FieldDirectInjectionBean();
    }

    static class FilterModifiersBean {
        public static String staticField = "static_init";
        public final String finalField = "final_init";
        private String normalField;

        public String getNormalField() {
            return normalField;
        }
    }

    static class FilterModifiersOwner {
        private FilterModifiersBean bean = new FilterModifiersBean();
    }

    static class GrandParentLevel {
        private Double grandParentVal;

        public Double getGrandParentVal() {
            return grandParentVal;
        }
    }

    static class ParentLevel extends GrandParentLevel {
        private Integer parentVal;

        public Integer getParentVal() {
            return parentVal;
        }
    }

    static class ChildLevel extends ParentLevel {
        private String childVal;

        public String getChildVal() {
            return childVal;
        }
    }

    static class MultiLevelHierarchyOwner {
        private ChildLevel bean = new ChildLevel();
    }

    static class NoDefaultConstructorBean {
        public NoDefaultConstructorBean(String requiredArg) {
        }
    }

    static class CannotInitializeOwner {
        private NoDefaultConstructorBean bean;
    }

    static class ExplodingConstructorBean {
        public ExplodingConstructorBean() {
            throw new IllegalStateException("Simulated constructor failure");
        }
    }

    static class ExplodingConstructorOwner {
        private ExplodingConstructorBean bean;
    }

    static class EmptyBean {
    }

    static class EmptyBeanOwner {
        private EmptyBean bean = new EmptyBean();
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    /**
     * Target Defect: shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable
     * Asserts that once a mock candidate is injected into a subclass field, it is consumed
     * and NOT re-injected into a superclass field in the same hierarchy.
     */
    @Test(timeout = 4000)
    public void shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable() throws Exception {
        PropertyAndSetterInjection injection = new PropertyAndSetterInjection();
        DefectFixtureOwner owner = new DefectFixtureOwner();
        Field beanField = DefectFixtureOwner.class.getDeclaredField("bean");

        Set<Object> mocks = new HashSet<Object>();
        String candidate2 = "candidate2";
        mocks.add(candidate2);

        boolean injected = injection.processInjection(beanField, owner, mocks);

        assertTrue("Injection should have succeeded on the instance hierarchy", injected);
        assertNotNull("The bean field should have been initialized by strategy", owner.bean);
        assertEquals("Subclass field should receive candidate2", "candidate2", owner.bean.getSubCandidate2());

        // Under defective code, candidate2 is not removed across class iterations,
        // causing owner.bean.getCandidate2() to return "candidate2" instead of null.
        assertNull("Superclass field must remain null; candidate2 should not be injected twice", owner.bean.getCandidate2());
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSetterInjectionTakesPrecedenceOverFieldInjection() throws Exception {
        PropertyAndSetterInjection injection = new PropertyAndSetterInjection();
        SetterInjectionOwner owner = new SetterInjectionOwner();
        Field beanField = SetterInjectionOwner.class.getDeclaredField("bean");

        Set<Object> mocks = new HashSet<Object>();
        mocks.add("expectedValue");

        boolean injected = injection.processInjection(beanField, owner, mocks);

        assertTrue("Injection should report true", injected);
        assertTrue("Setter must be invoked when a valid property setter exists", owner.bean.isSetterInvoked());
        assertEquals("Property value should match the injected mock", "expectedValue", owner.bean.getName());
    }

    @Test(timeout = 4000)
    public void testDirectFieldInjectionWhenNoSetterAvailable() throws Exception {
        PropertyAndSetterInjection injection = new PropertyAndSetterInjection();
        FieldDirectInjectionOwner owner = new FieldDirectInjectionOwner();
        Field beanField = FieldDirectInjectionOwner.class.getDeclaredField("bean");

        Set<Object> mocks = new HashSet<Object>();
        mocks.add("directMockValue");

        boolean injected = injection.processInjection(beanField, owner, mocks);

        assertTrue("Direct field injection should succeed", injected);
        assertEquals("Value must be set directly into the field", "directMockValue", owner.bean.getDirectValue());
    }

    @Test(timeout = 4000)
    public void testHierarchicalMultiLevelInjection() throws Exception {
        PropertyAndSetterInjection injection = new PropertyAndSetterInjection();
        MultiLevelHierarchyOwner owner = new MultiLevelHierarchyOwner();
        Field beanField = MultiLevelHierarchyOwner.class.getDeclaredField("bean");

        Set<Object> mocks = new HashSet<Object>();
        mocks.add("childString");
        mocks.add(Integer.valueOf(42));
        mocks.add(Double.valueOf(3.1415));

        boolean injected = injection.processInjection(beanField, owner, mocks);

        assertTrue("Injection should occur across the multi-level hierarchy", injected);
        assertEquals("Child level field should be injected", "childString", owner.bean.getChildVal());
        assertEquals("Parent level field should be injected", Integer.valueOf(42), owner.bean.getParentVal());
        assertEquals("GrandParent level field should be injected", Double.valueOf(3.1415), owner.bean.getGrandParentVal());
    }

    @Test(timeout = 4000)
    public void testFieldInstantiationOnUninitializedInjectMocksField() throws Exception {
        PropertyAndSetterInjection injection = new PropertyAndSetterInjection();
        DefectFixtureOwner owner = new DefectFixtureOwner();
        assertNull("Field is initially null", owner.bean);

        Field beanField = DefectFixtureOwner.class.getDeclaredField("bean");
        boolean injected = injection.processInjection(beanField, owner, Collections.emptySet());

        assertFalse("No mocks injected", injected);
        assertNotNull("Target instance should have been initialized via default constructor", owner.bean);
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyMockCandidatesSetProducesNoInjection() throws Exception {
        PropertyAndSetterInjection injection = new PropertyAndSetterInjection();
        FieldDirectInjectionOwner owner = new FieldDirectInjectionOwner();
        Field beanField = FieldDirectInjectionOwner.class.getDeclaredField("bean");

        boolean injected = injection.processInjection(beanField, owner, Collections.emptySet());

        assertFalse("Injection should return false for empty mock candidates", injected);
        assertNull("Field should remain unassigned", owner.bean.getDirectValue());
    }

    @Test(timeout = 4000)
    public void testNoCompatibleMockTypeLeavesFieldsNull() throws Exception {
        PropertyAndSetterInjection injection = new PropertyAndSetterInjection();
        FieldDirectInjectionOwner owner = new FieldDirectInjectionOwner();
        Field beanField = FieldDirectInjectionOwner.class.getDeclaredField("bean");

        Set<Object> incompatibleMocks = new HashSet<Object>();
        incompatibleMocks.add(Integer.valueOf(999));

        boolean injected = injection.processInjection(beanField, owner, incompatibleMocks);

        assertFalse("No matching type should result in false", injected);
        assertNull("String field should not receive Integer mock", owner.bean.getDirectValue());
    }

    @Test(timeout = 4000)
    public void testStaticAndFinalFieldsAreIgnored() throws Exception {
        PropertyAndSetterInjection injection = new PropertyAndSetterInjection();
        FilterModifiersOwner owner = new FilterModifiersOwner();
        Field beanField = FilterModifiersOwner.class.getDeclaredField("bean");

        Set<Object> mocks = new HashSet<Object>();
        mocks.add("newInjectedValue");

        boolean injected = injection.processInjection(beanField, owner, mocks);

        assertTrue("Normal field should be injected", injected);
        assertEquals("Static field must remain unmodified", "static_init", FilterModifiersBean.staticField);
        assertEquals("Final field must remain unmodified", "final_init", owner.bean.finalField);
        assertEquals("Normal field must be populated", "newInjectedValue", owner.bean.getNormalField());
    }

    @Test(timeout = 4000)
    public void testClassWithNoDeclaredFields() throws Exception {
        PropertyAndSetterInjection injection = new PropertyAndSetterInjection();
        EmptyBeanOwner owner = new EmptyBeanOwner();
        Field beanField = EmptyBeanOwner.class.getDeclaredField("bean");

        Set<Object> mocks = new HashSet<Object>();
        mocks.add("unusedCandidate");

        boolean injected = injection.processInjection(beanField, owner, mocks);

        assertFalse("Injection on class with zero fields should return false", injected);
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCannotInitializeFieldWithoutDefaultConstructorThrowsMockitoException() throws Exception {
        PropertyAndSetterInjection injection = new PropertyAndSetterInjection();
        CannotInitializeOwner owner = new CannotInitializeOwner();
        Field beanField = CannotInitializeOwner.class.getDeclaredField("bean");

        try {
            injection.processInjection(beanField, owner, Collections.emptySet());
            fail("Expected MockitoException because field type lacks default constructor");
        } catch (MockitoException expected) {
            assertNotNull("Exception must contain informative message", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testConstructorThrowingExceptionTriggersFieldInitialisationThrewException() throws Exception {
        PropertyAndSetterInjection injection = new PropertyAndSetterInjection();
        ExplodingConstructorOwner owner = new ExplodingConstructorOwner();
        Field beanField = ExplodingConstructorOwner.class.getDeclaredField("bean");

        try {
            injection.processInjection(beanField, owner, Collections.emptySet());
            fail("Expected MockitoException wrapping InvocationTargetException real cause");
        } catch (MockitoException expected) {
            assertNotNull("Exception must not be null", expected.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Strategy Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMockInjectionStrategyChaining() {
        PropertyAndSetterInjection injection = new PropertyAndSetterInjection();
        MockInjectionStrategy fallbackStrategy = MockInjectionStrategy.nop();

        MockInjectionStrategy chainedStrategy = injection.thenTry(fallbackStrategy);

        assertNotNull("Strategy chaining with thenTry must return non-null composite strategy", chainedStrategy);
    }
}