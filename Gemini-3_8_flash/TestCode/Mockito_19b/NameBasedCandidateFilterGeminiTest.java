/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mockito.internal.configuration.injection.filter.NameBasedCandidateFilter
 *
 * Decision / Branch Matrix:
 * 1. Branch: `mocks.size() > 1` (TRUE)
 *    - Condition: `field.getName().equals(mockUtil.getMockName(mock).toString())` is TRUE
 *      -> Matches added to `mockNameMatches`, forwarded to `next.filterCandidate(...)`.
 *    - Condition: `field.getName().equals(mockUtil.getMockName(mock).toString())` is FALSE
 *      -> Non-matching mocks excluded. If none match, empty list forwarded.
 *    - Condition: Multiple mocks have matching names
 *      -> All matching mocks collected and forwarded.
 * 2. Branch: `mocks.size() > 1` (FALSE: size == 1 or size == 0)
 *    - Condition: `mocks.size() == 1`, mock name matches field name
 *      -> Original collection forwarded directly to `next.filterCandidate(...)`.
 *    - Condition: `mocks.size() == 1`, mock name does NOT match field name
 *      -> Original collection forwarded directly without name validation.
 *    - Condition: `mocks.isEmpty()`
 *      -> Empty collection forwarded directly to `next.filterCandidate(...)`.
 * 3. Defect-Targeted Ground Truth (Defects4J Mockito-28):
 *    - Test: `shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable`
 *      Verifies disambiguation among multiple mock candidates of matching type,
 *      ensuring only candidate matching target field name ("candidate2") is retained.
 * 4. Defensive & Exceptional Paths:
 *    - `mocks.size() > 1` with non-mock element -> throws NotAMockException.
 *    - `mocks.size() > 1` with null element -> throws NotAMockException.
 *    - `mocks == null` -> throws NullPointerException.
 *    - `field == null` when `mocks.size() > 1` -> throws NullPointerException.
 *    - `next == null` -> throws NullPointerException upon filter dispatch.
 */
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

public class NameBasedCandidateFilterGeminiTest {

    // Target object hosting test fields for reflection
    private static class SampleTarget {
        private Comparable<?> candidate1;
        private Comparable<?> candidate2;
        private Comparable<?> duplicateMockName;
        private Object otherField;
    }

    // Deterministic test double capturing parameters forwarded to next filter
    private static class RecordingCandidateFilter implements MockCandidateFilter {
        private Collection<Object> capturedMocks;
        private Field capturedField;
        private Object capturedFieldInstance;
        private final OngoingInjecter injecterToReturn;

        public RecordingCandidateFilter() {
            this.injecterToReturn = new OngoingInjecter() {
                @Override
                public Object thenInject() {
                    return "injected_successfully";
                }
            };
        }

        public RecordingCandidateFilter(OngoingInjecter injecterToReturn) {
            this.injecterToReturn = injecterToReturn;
        }

        @Override
        public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object fieldInstance) {
            this.capturedMocks = mocks;
            this.capturedField = field;
            this.capturedFieldInstance = fieldInstance;
            return injecterToReturn;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFilterCandidateWithMultipleMocksOneMatching() throws Exception {
        RecordingCandidateFilter next = new RecordingCandidateFilter();
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);

        Field field = SampleTarget.class.getDeclaredField("candidate1");
        SampleTarget instance = new SampleTarget();

        Object mock1 = org.mockito.Mockito.mock(Comparable.class, "candidate1");
        Object mock2 = org.mockito.Mockito.mock(Comparable.class, "candidate2");
        List<Object> mocks = Arrays.asList(mock1, mock2);

        OngoingInjecter result = filter.filterCandidate(mocks, field, instance);

        assertNotNull("Filter must return an OngoingInjecter", result);
        assertEquals("Injected result contract integrity", "injected_successfully", result.thenInject());
        assertSame("Field instance must be forwarded unchanged", instance, next.capturedFieldInstance);
        assertSame("Field metadata must be forwarded unchanged", field, next.capturedField);
        assertNotNull("Captured mocks must not be null", next.capturedMocks);
        assertEquals("Only matching mock should be forwarded", 1, next.capturedMocks.size());
        assertTrue("Matching mock1 must be present", next.capturedMocks.contains(mock1));
        assertFalse("Non-matching mock2 must be excluded", next.capturedMocks.contains(mock2));
    }

    @Test(timeout = 4000)
    public void testFilterCandidateWithMultipleMocksNoneMatching() throws Exception {
        RecordingCandidateFilter next = new RecordingCandidateFilter();
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);

        Field field = SampleTarget.class.getDeclaredField("otherField");
        SampleTarget instance = new SampleTarget();

        Object mock1 = org.mockito.Mockito.mock(Comparable.class, "candidate1");
        Object mock2 = org.mockito.Mockito.mock(Comparable.class, "candidate2");
        List<Object> mocks = Arrays.asList(mock1, mock2);

        OngoingInjecter result = filter.filterCandidate(mocks, field, instance);

        assertNotNull(result);
        assertNotNull(next.capturedMocks);
        assertTrue("When no mock matches field name, empty collection must be forwarded", next.capturedMocks.isEmpty());
    }

    @Test(timeout = 4000)
    public void testFilterCandidateWithMultipleMocksAllMatchingDuplicateNames() throws Exception {
        RecordingCandidateFilter next = new RecordingCandidateFilter();
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);

        Field field = SampleTarget.class.getDeclaredField("duplicateMockName");
        SampleTarget instance = new SampleTarget();

        Object mock1 = org.mockito.Mockito.mock(Comparable.class, "duplicateMockName");
        Object mock2 = org.mockito.Mockito.mock(Comparable.class, "duplicateMockName");
        List<Object> mocks = Arrays.asList(mock1, mock2);

        OngoingInjecter result = filter.filterCandidate(mocks, field, instance);

        assertNotNull(result);
        assertEquals("Both mocks sharing the target name must be retained", 2, next.capturedMocks.size());
        assertTrue(next.capturedMocks.contains(mock1));
        assertTrue(next.capturedMocks.contains(mock2));
    }

    @Test(timeout = 4000)
    public void testFilterCandidateReturnNullInjecterPassThrough() throws Exception {
        RecordingCandidateFilter next = new RecordingCandidateFilter(null);
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);

        Field field = SampleTarget.class.getDeclaredField("candidate1");
        SampleTarget instance = new SampleTarget();

        Object mock1 = org.mockito.Mockito.mock(Comparable.class, "candidate1");
        Object mock2 = org.mockito.Mockito.mock(Comparable.class, "candidate2");

        OngoingInjecter result = filter.filterCandidate(Arrays.asList(mock1, mock2), field, instance);
        assertNull("If next returns null OngoingInjecter, filterCandidate must return null", result);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFilterCandidateWithEmptyCollection() throws Exception {
        RecordingCandidateFilter next = new RecordingCandidateFilter();
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);

        Field field = SampleTarget.class.getDeclaredField("candidate1");
        SampleTarget instance = new SampleTarget();
        List<Object> mocks = Collections.emptyList();

        OngoingInjecter result = filter.filterCandidate(mocks, field, instance);

        assertNotNull(result);
        assertSame("Empty collection should be passed through directly", mocks, next.capturedMocks);
        assertTrue(next.capturedMocks.isEmpty());
    }

    @Test(timeout = 4000)
    public void testFilterCandidateWithSingleMockMatchingName() throws Exception {
        RecordingCandidateFilter next = new RecordingCandidateFilter();
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);

        Field field = SampleTarget.class.getDeclaredField("candidate1");
        SampleTarget instance = new SampleTarget();
        Object mock1 = org.mockito.Mockito.mock(Comparable.class, "candidate1");
        List<Object> mocks = Collections.singletonList(mock1);

        OngoingInjecter result = filter.filterCandidate(mocks, field, instance);

        assertNotNull(result);
        assertSame("Single mock collection must be forwarded directly without re-filtering", mocks, next.capturedMocks);
        assertEquals(1, next.capturedMocks.size());
        assertTrue(next.capturedMocks.contains(mock1));
    }

    @Test(timeout = 4000)
    public void testFilterCandidateWithSingleMockNonMatchingName() throws Exception {
        RecordingCandidateFilter next = new RecordingCandidateFilter();
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);

        Field field = SampleTarget.class.getDeclaredField("candidate1");
        SampleTarget instance = new SampleTarget();
        Object mockOther = org.mockito.Mockito.mock(Comparable.class, "completelyDifferentName");
        List<Object> mocks = Collections.singletonList(mockOther);

        OngoingInjecter result = filter.filterCandidate(mocks, field, instance);

        assertNotNull(result);
        assertSame("When size is 1, collection must be forwarded without name match checks", mocks, next.capturedMocks);
        assertEquals(1, next.capturedMocks.size());
        assertTrue(next.capturedMocks.contains(mockOther));
    }

    @Test(timeout = 4000)
    public void testFilterCandidateWithSetCollectionPolymorphism() throws Exception {
        RecordingCandidateFilter next = new RecordingCandidateFilter();
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);

        Field field = SampleTarget.class.getDeclaredField("candidate2");
        SampleTarget instance = new SampleTarget();

        Object mock1 = org.mockito.Mockito.mock(Comparable.class, "candidate1");
        Object mock2 = org.mockito.Mockito.mock(Comparable.class, "candidate2");
        Set<Object> mockSet = new HashSet<Object>(Arrays.asList(mock1, mock2));

        OngoingInjecter result = filter.filterCandidate(mockSet, field, instance);

        assertNotNull(result);
        assertEquals(1, next.capturedMocks.size());
        assertTrue(next.capturedMocks.contains(mock2));
        assertFalse(next.capturedMocks.contains(mock1));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets defects documented in MockInjectionUsingSetterOrPropertyTest:
     * shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable
     *
     * When multiple candidate mocks of matching types exist (candidate1, candidate2),
     * NameBasedCandidateFilter must disambiguate candidate2 by exact name match.
     */
    @Test(timeout = 4000)
    public void shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable() throws Exception {
        RecordingCandidateFilter next = new RecordingCandidateFilter();
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);

        Field candidate2Field = SampleTarget.class.getDeclaredField("candidate2");
        SampleTarget instance = new SampleTarget();

        Object mock1 = org.mockito.Mockito.mock(Comparable.class, "candidate1");
        Object mock2 = org.mockito.Mockito.mock(Comparable.class, "candidate2");
        List<Object> multipleTypeMocks = Arrays.asList(mock1, mock2);

        OngoingInjecter injecter = filter.filterCandidate(multipleTypeMocks, candidate2Field, instance);

        assertNotNull("Injecter must not be null", injecter);
        assertNotNull("Captured mocks must not be null", next.capturedMocks);
        assertEquals("Defect verification: only candidate2 should be filtered in for candidate2 field",
                1, next.capturedMocks.size());
        assertSame("The retained mock must strictly match field name 'candidate2'",
                mock2, next.capturedMocks.iterator().next());
    }

    @Test(timeout = 4000)
    public void shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable_reverseFieldSelection() throws Exception {
        RecordingCandidateFilter next = new RecordingCandidateFilter();
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);

        Field candidate1Field = SampleTarget.class.getDeclaredField("candidate1");
        SampleTarget instance = new SampleTarget();

        Object mock1 = org.mockito.Mockito.mock(Comparable.class, "candidate1");
        Object mock2 = org.mockito.Mockito.mock(Comparable.class, "candidate2");
        List<Object> multipleTypeMocks = Arrays.asList(mock1, mock2);

        OngoingInjecter injecter = filter.filterCandidate(multipleTypeMocks, candidate1Field, instance);

        assertNotNull("Injecter must not be null", injecter);
        assertEquals("Defect verification: candidate1 must be filtered in for candidate1 field",
                1, next.capturedMocks.size());
        assertSame("The retained mock must strictly match field name 'candidate1'",
                mock1, next.capturedMocks.iterator().next());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testFilterCandidateThrowsExceptionWhenElementIsNotAMock() throws Exception {
        RecordingCandidateFilter next = new RecordingCandidateFilter();
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);

        Field field = SampleTarget.class.getDeclaredField("candidate1");
        SampleTarget instance = new SampleTarget();

        List<Object> mocks = Arrays.asList("not_a_mock_string", new Object());
        try {
            filter.filterCandidate(mocks, field, instance);
            fail("Expected Mockito exception when querying mock name from non-mock instance");
        } catch (RuntimeException expected) {
            assertTrue("Exception should indicate NotAMockException",
                    expected.getClass().getName().contains("NotAMockException"));
        }
    }

    @Test(timeout = 4000)
    public void testFilterCandidateThrowsExceptionWhenElementIsNull() throws Exception {
        RecordingCandidateFilter next = new RecordingCandidateFilter();
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);

        Field field = SampleTarget.class.getDeclaredField("candidate1");
        SampleTarget instance = new SampleTarget();

        List<Object> mocksWithNull = Arrays.asList(null, org.mockito.Mockito.mock(Comparable.class, "candidate1"));
        try {
            filter.filterCandidate(mocksWithNull, field, instance);
            fail("Expected Mockito exception when querying mock name for null reference");
        } catch (RuntimeException expected) {
            assertTrue("Expected NotAMockException or NullPointerException for null mock",
                    expected.getClass().getName().contains("NotAMockException") || (expected instanceof NullPointerException));
        }
    }

    @Test(timeout = 4000)
    public void testFilterCandidateThrowsNullPointerExceptionOnNullMocks() throws Exception {
        RecordingCandidateFilter next = new RecordingCandidateFilter();
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);

        Field field = SampleTarget.class.getDeclaredField("candidate1");
        SampleTarget instance = new SampleTarget();

        try {
            filter.filterCandidate(null, field, instance);
            fail("Expected NullPointerException when mocks collection is null");
        } catch (NullPointerException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testFilterCandidateThrowsNullPointerExceptionOnNullField() throws Exception {
        RecordingCandidateFilter next = new RecordingCandidateFilter();
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(next);

        Object mock1 = org.mockito.Mockito.mock(Comparable.class, "candidate1");
        Object mock2 = org.mockito.Mockito.mock(Comparable.class, "candidate2");
        List<Object> mocks = Arrays.asList(mock1, mock2);

        try {
            filter.filterCandidate(mocks, null, new SampleTarget());
            fail("Expected NullPointerException when field is null and mocks.size() > 1");
        } catch (NullPointerException expected) {
            // Success: field.getName() on null throws NPE
        }
    }

    @Test(timeout = 4000)
    public void testFilterCandidateThrowsNullPointerExceptionOnNullNextFilter() throws Exception {
        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(null);
        Field field = SampleTarget.class.getDeclaredField("candidate1");
        List<Object> mocks = Collections.emptyList();

        try {
            filter.filterCandidate(mocks, field, new SampleTarget());
            fail("Expected NullPointerException when delegate next filter is null");
        } catch (NullPointerException expected) {
            // Success
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorInitialization() {
        MockCandidateFilter dummyNext = new MockCandidateFilter() {
            @Override
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object fieldInstance) {
                return null;
            }
        };

        NameBasedCandidateFilter filter = new NameBasedCandidateFilter(dummyNext);
        assertNotNull("Filter instance must be successfully constructed", filter);
    }
}