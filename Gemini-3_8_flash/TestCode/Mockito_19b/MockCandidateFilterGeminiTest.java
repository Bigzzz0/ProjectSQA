package org.mockito.internal.configuration.injection.filter;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: MockCandidateFilter (& implementations: TypeBasedCandidateFilter, FinalMockCandidateFilter,
 *               NameBasedCandidateFilter)
 * Defect Reference: Defects4J Mockito-28
 * Failing Test Ground Truth:
 *   org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable
 *   --> junit.framework.AssertionFailedError: Expected: <null> but was: candidate2
 *
 * Core Defect Mechanism:
 *   When multiple mocks of distinct types are available, TypeBasedCandidateFilter narrows the candidate
 *   collection down to a single type-matching mock. If the target field's name does NOT match this mock's
 *   name, NameBasedCandidateFilter historically evaluated `if (mocks.size() > 1)`. Because the filtered
 *   collection had size == 1, it bypassed name verification, delegating directly to FinalMockCandidateFilter
 *   which forcefully injected the mock into the wrong field. The correct behavior demands that when name
 *   matching is required under multiple available candidate types, unmatched fields remain null.
 *
 * Branch & Boundary Coverage Matrix:
 * 1. TypeBasedCandidateFilter:
 *    - mocks is empty (boundary) -> next filter receives empty collection
 *    - mocks contains assignable type -> added to filtered collection
 *    - mocks contains unassignable type -> excluded from filtered collection
 *    - mocks contains null element -> throws NullPointerException (defensive exception path)
 * 2. FinalMockCandidateFilter:
 *    - mocks is empty -> returns OngoingInjecter producing null, no field modification
 *    - mocks is non-empty -> returns OngoingInjecter with matchingMock:
 *        a) Bean property setter exists and succeeds -> setter invoked
 *        b) Bean property setter absent -> direct field reflection assignment
 *        c) Bean property setter throws RuntimeException -> caught, fallback to field reflection
 * 3. NameBasedCandidateFilter:
 *    - mocks.size() == 0 or 1 -> delegates directly to next filter
 * 4. Ground-Truth Defect Injection Target:
 *    - Filter pipeline with candidate2 (matching type, wrong name) and otherCandidate (different type).
 *      Target field "candidate1" must NOT receive candidate2 and must remain null.
 * ----------------------------------------------------------------------------------------------------
 */
public class MockCandidateFilterGeminiTest {

    // Helper method to create Mockito mocks by name via reflection (avoiding direct Mockito imports)
    private Object createMock(Class<?> type, String name) {
        try {
            Class<?> mockitoClass = Class.forName("org.mockito.Mockito");
            Method mockMethod = mockitoClass.getMethod("mock", Class.class, String.class);
            return mockMethod.invoke(null, type, name);
        } catch (Exception e) {
            throw new RuntimeException("Failed to reflectively create mock for testing defect", e);
        }
    }

    // Target test class with varied field types and setter configurations
    public static class SampleTarget {
        public CharSequence candidate1;
        public CharSequence candidate2;
        public String directField;
        private Double propertyField;
        public boolean setterInvoked = false;
        private String failingPropertyField;

        public void setPropertyField(Double propertyField) {
            this.propertyField = propertyField;
            this.setterInvoked = true;
        }

        public Double getPropertyField() {
            return propertyField;
        }

        public void setFailingPropertyField(String val) {
            throw new RuntimeException("Simulated setter failure");
        }

        public String getFailingPropertyField() {
            return failingPropertyField;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTypeBasedFilterPassesMatchingTypesToNext() throws Exception {
        final List<Collection<Object>> capturedMocks = new ArrayList<Collection<Object>>();
        MockCandidateFilter recordingNext = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                capturedMocks.add(new ArrayList<Object>(mocks));
                return new OngoingInjecter() {
                    public Object thenInject() {
                        return null;
                    }
                };
            }
        };

        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(recordingNext);
        Field field = SampleTarget.class.getField("directField"); // type String

        String matchingMock = "matchedString";
        Integer unmatchingMock = 42;
        List<Object> mocks = Arrays.asList(matchingMock, unmatchingMock);

        SampleTarget target = new SampleTarget();
        OngoingInjecter injecter = filter.filterCandidate(mocks, field, target);

        assertNotNull("Injecter should not be null", injecter);
        assertEquals("Next filter should be called exactly once", 1, capturedMocks.size());
        Collection<Object> passed = capturedMocks.get(0);
        assertEquals("Only one mock should match the String type", 1, passed.size());
        assertTrue("Matching mock should be present", passed.contains(matchingMock));
        assertFalse("Unmatching mock should be filtered out", passed.contains(unmatchingMock));
    }

    @Test(timeout = 4000)
    public void testFinalMockFilterInjectsViaSetterWhenAvailable() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Field field = SampleTarget.class.getDeclaredField("propertyField");
        SampleTarget target = new SampleTarget();

        Double mockValue = 3.14159;
        OngoingInjecter injecter = filter.filterCandidate(Collections.singletonList((Object) mockValue), field, target);

        Object injected = injecter.thenInject();
        assertEquals("Injected value should match candidate", mockValue, injected);
        assertTrue("Setter method should have been invoked", target.setterInvoked);
        assertEquals("Field value should match setter invocation", mockValue, target.getPropertyField());
    }

    @Test(timeout = 4000)
    public void testFinalMockFilterInjectsViaDirectFieldWhenNoSetter() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Field field = SampleTarget.class.getField("directField");
        SampleTarget target = new SampleTarget();

        String mockValue = "directAssignmentValue";
        OngoingInjecter injecter = filter.filterCandidate(Collections.singletonList((Object) mockValue), field, target);

        Object injected = injecter.thenInject();
        assertEquals("Injected value should match candidate", mockValue, injected);
        assertEquals("Direct field should be populated", mockValue, target.directField);
    }

    @Test(timeout = 4000)
    public void testFinalMockFilterFallbackToFieldWhenSetterThrows() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Field field = SampleTarget.class.getDeclaredField("failingPropertyField");
        SampleTarget target = new SampleTarget();

        String mockValue = "fallbackValue";
        OngoingInjecter injecter = filter.filterCandidate(Collections.singletonList((Object) mockValue), field, target);

        Object injected = injecter.thenInject();
        assertEquals("Injected value should match candidate despite setter failure", mockValue, injected);
        assertEquals("Field should be set directly via FieldSetter fallback", mockValue, target.getFailingPropertyField());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testTypeBasedFilterWithEmptyMocks() throws Exception {
        final List<Collection<Object>> capturedMocks = new ArrayList<Collection<Object>>();
        MockCandidateFilter recordingNext = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                capturedMocks.add(new ArrayList<Object>(mocks));
                return new OngoingInjecter() {
                    public Object thenInject() {
                        return null;
                    }
                };
            }
        };

        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(recordingNext);
        Field field = SampleTarget.class.getField("directField");
        SampleTarget target = new SampleTarget();

        filter.filterCandidate(Collections.emptyList(), field, target);

        assertEquals(1, capturedMocks.size());
        assertTrue("Captured mocks should be empty", capturedMocks.get(0).isEmpty());
    }

    @Test(timeout = 4000)
    public void testFinalMockFilterWithEmptyMocksReturnsNull() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Field field = SampleTarget.class.getField("directField");
        SampleTarget target = new SampleTarget();

        OngoingInjecter injecter = filter.filterCandidate(Collections.emptyList(), field, target);
        assertNotNull("Injecter should never be null", injecter);
        Object injected = injecter.thenInject();
        assertNull("thenInject() on empty mocks must return null", injected);
        assertNull("Target field should remain null", target.directField);
    }

    @Test(timeout = 4000)
    public void testNameBasedFilterWithEmptyMocksDelegatesDirectly() throws Exception {
        final boolean[] nextInvoked = new boolean[]{false};
        MockCandidateFilter next = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                nextInvoked[0] = true;
                assertTrue("Mocks should be empty", mocks.isEmpty());
                return new OngoingInjecter() {
                    public Object thenInject() {
                        return null;
                    }
                };
            }
        };

        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);
        Field field = SampleTarget.class.getField("directField");
        SampleTarget target = new SampleTarget();

        filter.filterCandidate(Collections.emptyList(), field, target);
        assertTrue("Next filter should be invoked when mocks collection is empty", nextInvoked[0]);
    }

    @Test(timeout = 4000)
    public void testNameBasedFilterWithSingleMockDelegatesDirectly() throws Exception {
        final boolean[] nextInvoked = new boolean[]{false};
        final Object singleMock = "singleMockInstance";
        MockCandidateFilter next = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                nextInvoked[0] = true;
                assertEquals(1, mocks.size());
                assertSame(singleMock, mocks.iterator().next());
                return new OngoingInjecter() {
                    public Object thenInject() {
                        return singleMock;
                    }
                };
            }
        };

        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);
        Field field = SampleTarget.class.getField("directField");
        SampleTarget target = new SampleTarget();

        OngoingInjecter injecter = filter.filterCandidate(Collections.singletonList(singleMock), field, target);
        assertTrue("Next filter should be invoked for single mock", nextInvoked[0]);
        assertEquals(singleMock, injecter.thenInject());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Mockito-28 Ground Truth)
    // =========================================================================

    /**
     * Dedicated test targeting:
     * org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable
     * Expected failure on defective version: Expected: <null> but was: candidate2
     */
    @Test(timeout = 4000)
    public void testShouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable() throws Exception {
        MockCandidateFilter filterPipeline = new TypeBasedCandidateFilter(
                new NameBasedCandidateFilter(
                        new FinalMockCandidateFilter()
                )
        );

        Object candidate2Mock = createMock(CharSequence.class, "candidate2");
        Object otherTypeMock = createMock(Number.class, "otherCandidate");

        List<Object> availableMocks = Arrays.asList(candidate2Mock, otherTypeMock);

        SampleTarget target = new SampleTarget();
        Field fieldCandidate1 = SampleTarget.class.getField("candidate1");

        OngoingInjecter injecter = filterPipeline.filterCandidate(availableMocks, fieldCandidate1, target);
        injecter.thenInject();

        // On the defective version, candidate2 is wrongly injected because NameBasedCandidateFilter
        // only filtered by name if mocks.size() > 1, but TypeBasedCandidateFilter reduced size to 1!
        assertNull("Expected: <null> but was: candidate2", target.candidate1);
    }

    @Test(timeout = 4000)
    public void testFieldWithMatchingNameWhenMultipleTypesAvailable() throws Exception {
        MockCandidateFilter filterPipeline = new TypeBasedCandidateFilter(
                new NameBasedCandidateFilter(
                        new FinalMockCandidateFilter()
                )
        );

        Object candidate2Mock = createMock(CharSequence.class, "candidate2");
        Object otherTypeMock = createMock(Number.class, "otherCandidate");

        List<Object> availableMocks = Arrays.asList(candidate2Mock, otherTypeMock);

        SampleTarget target = new SampleTarget();
        Field fieldCandidate2 = SampleTarget.class.getField("candidate2");

        OngoingInjecter injecter = filterPipeline.filterCandidate(availableMocks, fieldCandidate2, target);
        Object injected = injecter.thenInject();

        assertSame("Candidate2 mock should be injected when name matches", candidate2Mock, injected);
        assertSame("Field candidate2 should be populated with candidate2Mock", candidate2Mock, target.candidate2);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testTypeBasedFilterThrowsOnNullMockInCollection() throws Exception {
        MockCandidateFilter dummyNext = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                return null;
            }
        };

        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(dummyNext);
        Field field = SampleTarget.class.getField("directField");
        SampleTarget target = new SampleTarget();

        List<Object> mocksWithNull = Collections.singletonList(null);
        filter.filterCandidate(mocksWithNull, field, target);
    }

    @Test(timeout = 4000)
    public void testCustomMockCandidateFilterInterfaceContract() throws Exception {
        final boolean[] executed = new boolean[]{false};
        MockCandidateFilter customFilter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field fieldToBeInjected, Object fieldInstance) {
                executed[0] = true;
                return new OngoingInjecter() {
                    public Object thenInject() {
                        return "customResult";
                    }
                };
            }
        };

        Field field = SampleTarget.class.getField("directField");
        SampleTarget target = new SampleTarget();

        OngoingInjecter injecter = customFilter.filterCandidate(Collections.emptyList(), field, target);
        assertNotNull(injecter);
        assertTrue("filterCandidate implementation should execute", executed[0]);
        assertEquals("customResult", injecter.thenInject());
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testOngoingInjecterIdempotencyAndReturnContract() throws Exception {
        FinalMockCandidateFilter filter = new FinalMockCandidateFilter();
        Field field = SampleTarget.class.getField("directField");
        SampleTarget target = new SampleTarget();

        String mockValue = "repeatableMock";
        OngoingInjecter injecter = filter.filterCandidate(Collections.singletonList((Object) mockValue), field, target);

        Object firstCall = injecter.thenInject();
        Object secondCall = injecter.thenInject();

        assertSame("thenInject() must consistently return the same mock instance", mockValue, firstCall);
        assertSame("thenInject() repeated invocation must be idempotent", firstCall, secondCall);
        assertEquals("Target field must retain mock value", mockValue, target.directField);
    }
}