package org.mockito.internal.configuration.injection;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.exceptions.base.MockitoException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Method Under Test: OngoingInjecter filterCandidate(Collection<Object>, Field, Object)
 *
 * Decision / Condition Matrix:
 * 1. mocks.size() == 1 [TRUE]
 *    - Extracts matchingMock via mocks.iterator().next()
 *    - Returns OngoingInjecter implementation:
 *      * thenInject() -> try: FieldSetter.set(matchingMock) -> return true;
 *      * thenInject() -> catch (Exception): wrap in MockitoException and throw.
 * 2. mocks.size() == 1 [FALSE]
 *    - mocks.size() == 0 (empty collection) -> returns OngoingInjecter { thenInject() -> return false; }
 *    - mocks.size() > 1 (multiple candidates) -> returns OngoingInjecter { thenInject() -> return false; }
 * 3. mocks == null -> Throws NullPointerException immediately upon mocks.size().
 *
 * Defects4J Ground Truth Defect:
 * - InjectMocksShouldTryPropertySettersFirstBeforeFieldAccessTest::shouldInjectUsingPropertySetterIfAvailable
 *   The specification and Javadoc of FinalMockCandidateFilter state that it will try first the
 *   property setter then if not possible try field access. The defective implementation directly
 *   uses FieldSetter, completely bypassing JavaBean property setter methods when available.
 * ----------------------------------------------------------------------------------------------------
 */
public class FinalMockCandidateFilterGeminiTest {

    // Target test class fixtures
    static class TargetWithPropertySetter {
        private String value;
        private boolean setterCalled = false;

        public void setValue(String value) {
            this.value = value;
            this.setterCalled = true;
        }

        public String getValue() {
            return value;
        }

        public boolean isSetterCalled() {
            return setterCalled;
        }
    }

    static class TargetObjectFields {
        private String stringField = "initial";
        private int primitiveIntField = 42;
        private Object objectField = new Object();

        public String getStringField() {
            return stringField;
        }

        public int getPrimitiveIntField() {
            return primitiveIntField;
        }

        public Object getObjectField() {
            return objectField;
        }
    }

    /* -------------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * ------------------------------------------------------------------------- */

    @Test(timeout = 4000)
    public void testFilterCandidate_singleCandidate_injectsSuccessfully() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetObjectFields target = new TargetObjectFields();
        Field field = TargetObjectFields.class.getDeclaredField("stringField");
        String mockCandidate = "mockValue";

        OngoingInjecter injecter = filter.filterCandidate(
                Collections.singletonList((Object) mockCandidate),
                field,
                target
        );

        assertNotNull("OngoingInjecter should never be null", injecter);
        boolean injected = injecter.thenInject();
        assertTrue("thenInject should return true when single candidate is injected", injected);
        assertEquals("Field value must be updated to the candidate mock", "mockValue", target.getStringField());
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_emptyCandidateCollection_returnsFalseInjecter() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetObjectFields target = new TargetObjectFields();
        Field field = TargetObjectFields.class.getDeclaredField("stringField");

        OngoingInjecter injecter = filter.filterCandidate(
                Collections.emptyList(),
                field,
                target
        );

        assertNotNull("OngoingInjecter should never be null", injecter);
        boolean injected = injecter.thenInject();
        assertFalse("thenInject should return false when mocks collection is empty", injected);
        assertEquals("Field value must remain untouched", "initial", target.getStringField());
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_multipleCandidates_returnsFalseInjecter() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetObjectFields target = new TargetObjectFields();
        Field field = TargetObjectFields.class.getDeclaredField("stringField");
        List<Object> mocks = Arrays.asList("mockOne", "mockTwo");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);

        assertNotNull("OngoingInjecter should never be null", injecter);
        boolean injected = injecter.thenInject();
        assertFalse("thenInject should return false when multiple candidates match", injected);
        assertEquals("Field value must remain untouched", "initial", target.getStringField());
    }

    /* -------------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * ------------------------------------------------------------------------- */

    @Test(timeout = 4000)
    public void testFilterCandidate_singleNullCandidate_injectsNullSuccessfully() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetObjectFields target = new TargetObjectFields();
        Field field = TargetObjectFields.class.getDeclaredField("stringField");

        OngoingInjecter injecter = filter.filterCandidate(
                Collections.singletonList((Object) null),
                field,
                target
        );

        assertNotNull("OngoingInjecter should not be null", injecter);
        boolean injected = injecter.thenInject();
        assertTrue("Injecting null into an Object field should succeed", injected);
        assertNull("Field value should now be null", target.getStringField());
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_threeCandidatesBoundary_returnsFalseInjecter() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetObjectFields target = new TargetObjectFields();
        Field field = TargetObjectFields.class.getDeclaredField("stringField");
        List<Object> mocks = Arrays.asList("mock1", "mock2", "mock3");

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);

        assertNotNull("Injecter should not be null", injecter);
        boolean injected = injecter.thenInject();
        assertFalse("thenInject should return false for 3 candidates", injected);
        assertEquals("Field value must not change", "initial", target.getStringField());
    }

    /* -------------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone
     * Defects4J Target: InjectMocks should try property setters first before field access
     * ------------------------------------------------------------------------- */

    @Test(timeout = 4000)
    public void testDefect_shouldInjectUsingPropertySetterIfAvailable() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetWithPropertySetter target = new TargetWithPropertySetter();
        Field field = TargetWithPropertySetter.class.getDeclaredField("value");
        String mockCandidate = "propertyValue";

        OngoingInjecter injecter = filter.filterCandidate(
                Collections.singletonList((Object) mockCandidate),
                field,
                target
        );

        assertNotNull("OngoingInjecter must be created", injecter);
        boolean injected = injecter.thenInject();
        assertTrue("Injection should succeed", injected);
        assertEquals("Value must be injected", "propertyValue", target.getValue());
        assertTrue(
                "Property setter must be invoked before/instead of direct field access",
                target.isSetterCalled()
        );
    }

    /* -------------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * ------------------------------------------------------------------------- */

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testFilterCandidate_nullMocksCollection_throwsNullPointerException() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetObjectFields target = new TargetObjectFields();
        Field field = TargetObjectFields.class.getDeclaredField("stringField");

        filter.filterCandidate(null, field, target);
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_typeMismatch_throwsMockitoException() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetObjectFields target = new TargetObjectFields();
        Field field = TargetObjectFields.class.getDeclaredField("stringField");
        Integer incompatibleMock = 12345;

        OngoingInjecter injecter = filter.filterCandidate(
                Collections.singletonList((Object) incompatibleMock),
                field,
                target
        );

        try {
            injecter.thenInject();
            fail("Expected MockitoException due to incompatible type assignment");
        } catch (MockitoException expected) {
            assertTrue(
                    "Exception message should mention problem injecting dependency in field",
                    expected.getMessage().contains("Problems injecting dependency in stringField")
            );
        }
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_nullIntoPrimitiveField_throwsMockitoException() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetObjectFields target = new TargetObjectFields();
        Field field = TargetObjectFields.class.getDeclaredField("primitiveIntField");

        OngoingInjecter injecter = filter.filterCandidate(
                Collections.singletonList((Object) null),
                field,
                target
        );

        try {
            injecter.thenInject();
            fail("Expected MockitoException when assigning null to a primitive field");
        } catch (MockitoException expected) {
            assertTrue(
                    "Exception message should mention field name primitiveIntField",
                    expected.getMessage().contains("primitiveIntField")
            );
        }
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_wrongFieldInstanceTarget_throwsMockitoException() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Object wrongInstance = new Object();
        Field field = TargetObjectFields.class.getDeclaredField("stringField");

        OngoingInjecter injecter = filter.filterCandidate(
                Collections.singletonList((Object) "sampleValue"),
                field,
                wrongInstance
        );

        try {
            injecter.thenInject();
            fail("Expected MockitoException when target instance does not have the field");
        } catch (MockitoException expected) {
            assertTrue(
                    "Exception message should report problem injecting dependency",
                    expected.getMessage().contains("Problems injecting dependency in stringField")
            );
        }
    }

    /* -------------------------------------------------------------------------
     * Partition E: Object Lifecycle & Contract Integrity
     * ------------------------------------------------------------------------- */

    @Test(timeout = 4000)
    public void testFilterCandidate_repeatedThenInjectCalls_idempotentBehavior() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetObjectFields target = new TargetObjectFields();
        Field field = TargetObjectFields.class.getDeclaredField("stringField");
        String mockCandidate = "reusableMock";

        OngoingInjecter injecter = filter.filterCandidate(
                Collections.singletonList((Object) mockCandidate),
                field,
                target
        );

        assertTrue("First injection should succeed", injecter.thenInject());
        assertEquals("reusableMock", target.getStringField());

        assertTrue("Second injection should also succeed", injecter.thenInject());
        assertEquals("reusableMock", target.getStringField());
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_repeatedThenInjectCallsOnNoopInjecter() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetObjectFields target = new TargetObjectFields();
        Field field = TargetObjectFields.class.getDeclaredField("stringField");

        OngoingInjecter injecter = filter.filterCandidate(
                Collections.emptyList(),
                field,
                target
        );

        assertFalse("First call must return false", injecter.thenInject());
        assertFalse("Second call must consistently return false", injecter.thenInject());
    }
}