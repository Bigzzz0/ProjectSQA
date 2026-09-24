package org.mockito.internal.configuration.injection.filter;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: FinalMockCandidateFilter (implements MockCandidateFilter)
 * Method: OngoingInjecter filterCandidate(final Collection<Object> mocks, final Field field, final Object fieldInstance)
 *
 * Decision / Branch Matrix:
 * 1. Branch [mocks.size() == 1]:
 *    - TRUE:
 *      * matchingMock extracted via mocks.iterator().next()
 *      * thenInject() -> Try block:
 *        - Condition: !new BeanPropertySetter(fieldInstance, field).set(matchingMock)
 *          * TRUE (setter doesn't exist / fails matching): fallback to new FieldSetter(...)
 *          * FALSE (property setter exists & succeeds): setter called, FieldSetter bypassed
 *        - Exception Guard: catch (RuntimeException e) -> calls Reporter.cannotInjectDependency(...)
 *        - Exit State: returns matchingMock
 *    - FALSE:
 *      * Empty collection (mocks.size() == 0) -> returns OngoingInjecter with thenInject() returning null
 *      * Multiple mocks (mocks.size() > 1) -> returns OngoingInjecter with thenInject() returning null
 *
 * Defects4J Ground Truth Defect:
 * - Failure: shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable
 * - Symptom: AssertionFailedError: Expected: <null> but was: candidate2
 * - Analysis: Injection filter chain must never inject or return a candidate if resolution did not
 *   narrow candidates down to exactly 1 matching mock. When mocks.size() > 1, thenInject() MUST
 *   strictly return null and leave target field completely untouched.
 * ====================================================================================================
 */
public class FinalMockCandidateFilterGeminiTest {

    // --- Test Fixture Inner Classes ---

    static class TargetWithPropertySetter {
        private String injectedString;
        private boolean setterCalled = false;

        public void setInjectedString(String injectedString) {
            this.injectedString = injectedString;
            this.setterCalled = true;
        }

        public String getInjectedString() {
            return injectedString;
        }

        public boolean isSetterCalled() {
            return setterCalled;
        }
    }

    static class TargetWithoutPropertySetter {
        private String directField;

        public String getDirectField() {
            return directField;
        }
    }

    static class TargetWithThrowingSetter {
        private String failingField;

        public void setFailingField(String failingField) {
            throw new IllegalStateException("Simulated setter failure");
        }

        public String getFailingField() {
            return failingField;
        }
    }

    static class TargetWithTypeMismatch {
        private Integer integerField;

        public Integer getIntegerField() {
            return integerField;
        }
    }

    static class TargetWithPrimitiveSetter {
        private int count;
        private boolean setterCalled = false;

        public void setCount(int count) {
            this.count = count;
            this.setterCalled = true;
        }

        public int getCount() {
            return count;
        }

        public boolean isSetterCalled() {
            return setterCalled;
        }
    }

    // ================================================================================================
    // Partition A: Core Functional Logic & State Transitions (Setter vs FieldSetter)
    // ================================================================================================

    @Test(timeout = 4000)
    public void testSuccessfulInjectionViaBeanPropertySetter() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetWithPropertySetter target = new TargetWithPropertySetter();
        Field field = TargetWithPropertySetter.class.getDeclaredField("injectedString");
        String mockCandidate = "mockViaSetter";

        OngoingInjecter injecter = filter.filterCandidate(
                Collections.singletonList((Object) mockCandidate), field, target);

        assertNotNull("Injecter must not be null", injecter);
        Object result = injecter.thenInject();

        assertSame("thenInject() must return the matching mock", mockCandidate, result);
        assertTrue("Setter must have been invoked", target.isSetterCalled());
        assertEquals("Target field should reflect injected value", "mockViaSetter", target.getInjectedString());
    }

    @Test(timeout = 4000)
    public void testSuccessfulInjectionViaFieldSetterWhenNoPropertySetter() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetWithoutPropertySetter target = new TargetWithoutPropertySetter();
        Field field = TargetWithoutPropertySetter.class.getDeclaredField("directField");
        String mockCandidate = "mockDirectField";

        OngoingInjecter injecter = filter.filterCandidate(
                Collections.singletonList((Object) mockCandidate), field, target);

        assertNotNull("Injecter must not be null", injecter);
        Object result = injecter.thenInject();

        assertSame("thenInject() must return the matching mock", mockCandidate, result);
        assertEquals("Target field should be directly injected", "mockDirectField", target.getDirectField());
    }

    @Test(timeout = 4000)
    public void testInjectionWithNullMockCandidate() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetWithPropertySetter target = new TargetWithPropertySetter();
        target.setInjectedString("preExistingValue");
        target.setterCalled = false;
        Field field = TargetWithPropertySetter.class.getDeclaredField("injectedString");

        List<Object> mocks = new ArrayList<>();
        mocks.add(null);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);
        assertNotNull(injecter);

        Object result = injecter.thenInject();
        assertNull("Matching mock is null, so thenInject should return null", result);
        assertTrue("Setter should be invoked with null", target.isSetterCalled());
        assertNull("Field value should now be null", target.getInjectedString());
    }

    // ================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================

    @Test(timeout = 4000)
    public void testEmptyCollectionReturnsNullAndDoesNotInject() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetWithoutPropertySetter target = new TargetWithoutPropertySetter();
        Field field = TargetWithoutPropertySetter.class.getDeclaredField("directField");

        OngoingInjecter injecter = filter.filterCandidate(Collections.emptyList(), field, target);

        assertNotNull("Injecter must not be null for empty collection", injecter);
        Object result = injecter.thenInject();

        assertNull("thenInject() must return null when mocks collection is empty", result);
        assertNull("Field must remain null when mocks collection is empty", target.getDirectField());
    }

    @Test(timeout = 4000)
    public void testSingleMockInHashSetCollection() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetWithoutPropertySetter target = new TargetWithoutPropertySetter();
        Field field = TargetWithoutPropertySetter.class.getDeclaredField("directField");
        String mockCandidate = "setMock";
        Set<Object> mockSet = new HashSet<>();
        mockSet.add(mockCandidate);

        OngoingInjecter injecter = filter.filterCandidate(mockSet, field, target);
        Object result = injecter.thenInject();

        assertSame("Candidate in HashSet must be correctly injected", mockCandidate, result);
        assertEquals("setMock", target.getDirectField());
    }

    // ================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // ================================================================================================

    @Test(timeout = 4000)
    public void testMultipleCandidatesReturnsNullAndPreventsInjectionDefectTarget() throws Exception {
        // Targets known defect where non-unique mock candidate could improperly leak/inject.
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetWithoutPropertySetter target = new TargetWithoutPropertySetter();
        Field field = TargetWithoutPropertySetter.class.getDeclaredField("directField");

        List<Object> multipleMocks = Arrays.asList((Object) "candidate1", "candidate2");
        OngoingInjecter injecter = filter.filterCandidate(multipleMocks, field, target);

        assertNotNull("Injecter must not be null for multiple candidates", injecter);
        Object result = injecter.thenInject();

        // Must strictly return null, NOT candidate2 or candidate1
        assertNull("thenInject() must evaluate to null when multiple candidates are present", result);
        assertNull("Target field must never be mutated when multiple candidates are present", target.getDirectField());
    }

    @Test(timeout = 4000)
    public void testMultipleCandidatesWithSetterBypassesSetterInvocation() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetWithPropertySetter target = new TargetWithPropertySetter();
        Field field = TargetWithPropertySetter.class.getDeclaredField("injectedString");

        List<Object> candidates = Arrays.asList((Object) "mockA", "mockB", "mockC");
        OngoingInjecter injecter = filter.filterCandidate(candidates, field, target);

        Object result = injecter.thenInject();

        assertNull("Result must be null when size > 1", result);
        assertFalse("Setter must not be invoked when candidates size > 1", target.isSetterCalled());
        assertNull("Field must not be set", target.getInjectedString());
    }

    // ================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================================================

    @Test(timeout = 4000)
    public void testExceptionHandlingWhenSetterThrowsException() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetWithThrowingSetter target = new TargetWithThrowingSetter();
        Field field = TargetWithThrowingSetter.class.getDeclaredField("failingField");
        String mockCandidate = "anyValue";

        OngoingInjecter injecter = filter.filterCandidate(
                Collections.singletonList((Object) mockCandidate), field, target);

        try {
            injecter.thenInject();
            fail("Expected MockitoException from Reporter.cannotInjectDependency");
        } catch (RuntimeException e) {
            assertTrue("Expected MockitoException to be reported, caught: " + e.getClass().getName(),
                    e.getClass().getName().contains("MockitoException"));
        }
    }

    @Test(timeout = 4000)
    public void testExceptionHandlingWhenFieldTypeIsIncompatible() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetWithTypeMismatch target = new TargetWithTypeMismatch();
        Field field = TargetWithTypeMismatch.class.getDeclaredField("integerField");
        String incompatibleCandidate = "notAnInteger";

        OngoingInjecter injecter = filter.filterCandidate(
                Collections.singletonList((Object) incompatibleCandidate), field, target);

        try {
            injecter.thenInject();
            fail("Expected MockitoException due to reflection type mismatch");
        } catch (RuntimeException e) {
            assertTrue("Expected MockitoException to be reported, caught: " + e.getClass().getName(),
                    e.getClass().getName().contains("MockitoException"));
        }
    }

    @Test(timeout = 4000)
    public void testExceptionHandlingWhenPrimitiveSetterReceivesNull() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetWithPrimitiveSetter target = new TargetWithPrimitiveSetter();
        Field field = TargetWithPrimitiveSetter.class.getDeclaredField("count");

        List<Object> mocks = new ArrayList<>();
        mocks.add(null);

        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);

        try {
            injecter.thenInject();
            fail("Expected MockitoException when setting primitive with null");
        } catch (RuntimeException e) {
            assertTrue("Expected MockitoException to be reported, caught: " + e.getClass().getName(),
                    e.getClass().getName().contains("MockitoException"));
        }
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFilterCandidateThrowsNpeOnNullMocksCollection() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetWithoutPropertySetter target = new TargetWithoutPropertySetter();
        Field field = TargetWithoutPropertySetter.class.getDeclaredField("directField");

        filter.filterCandidate(null, field, target);
    }

    // ================================================================================================
    // Partition E: Object Lifecycle, Repeatability & Contract Integrity
    // ================================================================================================

    @Test(timeout = 4000)
    public void testMultipleSequentialCallsToThenInjectForSingleMock() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetWithoutPropertySetter target = new TargetWithoutPropertySetter();
        Field field = TargetWithoutPropertySetter.class.getDeclaredField("directField");
        String mockCandidate = "repeatedMock";

        OngoingInjecter injecter = filter.filterCandidate(
                Collections.singletonList((Object) mockCandidate), field, target);

        Object result1 = injecter.thenInject();
        Object result2 = injecter.thenInject();

        assertSame(mockCandidate, result1);
        assertSame(mockCandidate, result2);
        assertEquals("repeatedMock", target.getDirectField());
    }

    @Test(timeout = 4000)
    public void testMultipleSequentialCallsToThenInjectForNoMocks() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        TargetWithoutPropertySetter target = new TargetWithoutPropertySetter();
        Field field = TargetWithoutPropertySetter.class.getDeclaredField("directField");

        OngoingInjecter injecter = filter.filterCandidate(Collections.emptyList(), field, target);

        Object result1 = injecter.thenInject();
        Object result2 = injecter.thenInject();

        assertNull(result1);
        assertNull(result2);
        assertNull(target.getDirectField());
    }

    @Test(timeout = 4000)
    public void testMockCandidateFilterInterfaceContract() {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        assertTrue("Must implement MockCandidateFilter contract", filter instanceof MockCandidateFilter);
    }
}