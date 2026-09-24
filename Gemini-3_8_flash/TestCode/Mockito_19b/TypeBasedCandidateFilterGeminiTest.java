/* [Branch & Defect Analysis Matrix]
 * Target Class: org.mockito.internal.configuration.injection.filter.TypeBasedCandidateFilter
 *
 * ---------------------------------------------------------------------------------------------------------
 * Decision / Condition                     | Branch Outcome | Test Case / Method
 * ---------------------------------------------------------------------------------------------------------
 * field.getType().isAssignableFrom(mock)   | TRUE (Match)   | testFilterCandidate_MatchingTypeIncluded()
 *                                          | FALSE (No-match| testFilterCandidate_NonMatchingTypeExcluded()
 * Iteration over Collection<Object> mocks  | Empty (0 mocks)| testFilterCandidate_EmptyMocksCollection()
 *                                          | Single mock    | testFilterCandidate_SingleMatchingMock()
 *                                          | Multiple mocks | testFilterCandidate_MultipleTypesFiltering()
 * Polymorphic Subtype Matching             | Subclass match | testFilterCandidate_SubclassAssignable()
 * Interface Implementation Matching        | Interface match| testFilterCandidate_InterfaceAssignable()
 * Primitive vs Wrapper Types               | Mismatch check | testFilterCandidate_PrimitiveVsWrapperHandling()
 * Mock list passed to next filter          | Exact contents | testFilterCandidate_PropagatesFilteredListToNext()
 * Return value of next filter              | Delegated return testFilterCandidate_DelegatesOngoingInjecterResult()
 * Multiple Candidates Available (Ground    | Defect target  | testDefect_MultipleMatchingCandidatesDisambiguation()
 *   Truth: setter/property candidate clash)|                |
 * Null checks / Robustness                 | Null in mocks  | testFilterCandidate_NullMockInCollectionThrowsNPE()
 * ---------------------------------------------------------------------------------------------------------
 */

package org.mockito.internal.configuration.injection.filter;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TypeBasedCandidateFilterGeminiTest {

    // --- Helper Test Fixtures and Dummy Types ---

    interface ServiceInterface {
    }

    static class ServiceImpl implements ServiceInterface {
    }

    static class SubServiceImpl extends ServiceImpl {
    }

    static class AnotherUnrelatedType {
    }

    static class SampleTarget {
        private ServiceInterface serviceField;
        private ServiceImpl concreteField;
        private Object objectField;
        private int primitiveIntField;
        private Integer wrapperIntField;
        private String candidate1;
        private String candidate2;
    }

    /**
     * Minimal recording test-double for MockCandidateFilter.
     */
    static class RecordingMockCandidateFilter implements MockCandidateFilter {
        Collection<Object> capturedMocks;
        Field capturedField;
        Object capturedFieldInstance;
        OngoingInjecter injecterToReturn = new OngoingInjecter() {
            public boolean thenInject() {
                return true;
            }
        };

        public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object fieldInstance) {
            this.capturedMocks = mocks;
            this.capturedField = field;
            this.capturedFieldInstance = fieldInstance;
            return injecterToReturn;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Delegation
    // =========================================================================

    @Test(timeout = 4000)
    public void testFilterCandidate_SingleMatchingMock() throws Exception {
        RecordingMockCandidateFilter nextFilter = new RecordingMockCandidateFilter();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(nextFilter);

        Field field = SampleTarget.class.getDeclaredField("concreteField");
        SampleTarget instance = new SampleTarget();
        ServiceImpl service = new ServiceImpl();
        List<Object> mocks = Collections.singletonList((Object) service);

        OngoingInjecter result = filter.filterCandidate(mocks, field, instance);

        assertNotNull("OngoingInjecter should not be null", result);
        assertSame("Should return the OngoingInjecter provided by next filter", nextFilter.injecterToReturn, result);
        assertSame("Target field should be passed to next filter", field, nextFilter.capturedField);
        assertSame("Target fieldInstance should be passed to next filter", instance, nextFilter.capturedFieldInstance);
        assertEquals("Should contain exactly 1 candidate", 1, nextFilter.capturedMocks.size());
        assertTrue("Captured mocks should contain the matching instance", nextFilter.capturedMocks.contains(service));
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_MatchingTypeIncluded() throws Exception {
        RecordingMockCandidateFilter nextFilter = new RecordingMockCandidateFilter();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(nextFilter);

        Field field = SampleTarget.class.getDeclaredField("concreteField");
        SampleTarget instance = new SampleTarget();
        ServiceImpl match = new ServiceImpl();
        AnotherUnrelatedType nonMatch = new AnotherUnrelatedType();

        List<Object> mocks = Arrays.asList(match, nonMatch);
        filter.filterCandidate(mocks, field, instance);

        assertEquals("Only matching candidate should be retained", 1, nextFilter.capturedMocks.size());
        assertTrue("Matching service must be in filtered list", nextFilter.capturedMocks.contains(match));
        assertFalse("Unrelated type must not be in filtered list", nextFilter.capturedMocks.contains(nonMatch));
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_NonMatchingTypeExcluded() throws Exception {
        RecordingMockCandidateFilter nextFilter = new RecordingMockCandidateFilter();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(nextFilter);

        Field field = SampleTarget.class.getDeclaredField("concreteField");
        SampleTarget instance = new SampleTarget();
        AnotherUnrelatedType nonMatch1 = new AnotherUnrelatedType();
        AnotherUnrelatedType nonMatch2 = new AnotherUnrelatedType();

        List<Object> mocks = Arrays.asList((Object) nonMatch1, nonMatch2);
        filter.filterCandidate(mocks, field, instance);

        assertNotNull("Captured mocks list should not be null", nextFilter.capturedMocks);
        assertTrue("Filtered candidate list should be empty when no types match", nextFilter.capturedMocks.isEmpty());
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_SubclassAssignable() throws Exception {
        RecordingMockCandidateFilter nextFilter = new RecordingMockCandidateFilter();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(nextFilter);

        Field field = SampleTarget.class.getDeclaredField("concreteField");
        SampleTarget instance = new SampleTarget();
        SubServiceImpl subService = new SubServiceImpl();

        filter.filterCandidate(Collections.singletonList((Object) subService), field, instance);

        assertEquals("Subclass candidate should be matched by isAssignableFrom", 1, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(subService));
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_InterfaceAssignable() throws Exception {
        RecordingMockCandidateFilter nextFilter = new RecordingMockCandidateFilter();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(nextFilter);

        Field field = SampleTarget.class.getDeclaredField("serviceField");
        SampleTarget instance = new SampleTarget();
        ServiceImpl impl = new ServiceImpl();
        SubServiceImpl subImpl = new SubServiceImpl();
        AnotherUnrelatedType unrelated = new AnotherUnrelatedType();

        List<Object> mocks = Arrays.asList(impl, unrelated, subImpl);
        filter.filterCandidate(mocks, field, instance);

        assertEquals("Both interface implementers should be matched", 2, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(impl));
        assertTrue(nextFilter.capturedMocks.contains(subImpl));
        assertFalse(nextFilter.capturedMocks.contains(unrelated));
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_ObjectFieldMatchesAnyType() throws Exception {
        RecordingMockCandidateFilter nextFilter = new RecordingMockCandidateFilter();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(nextFilter);

        Field field = SampleTarget.class.getDeclaredField("objectField");
        SampleTarget instance = new SampleTarget();
        ServiceImpl service = new ServiceImpl();
        AnotherUnrelatedType unrelated = new AnotherUnrelatedType();
        String stringMock = "mockString";

        List<Object> mocks = Arrays.asList(service, unrelated, stringMock);
        filter.filterCandidate(mocks, field, instance);

        assertEquals("Object field matches all non-primitive objects", 3, nextFilter.capturedMocks.size());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Collection Variations
    // =========================================================================

    @Test(timeout = 4000)
    public void testFilterCandidate_EmptyMocksCollection() throws Exception {
        RecordingMockCandidateFilter nextFilter = new RecordingMockCandidateFilter();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(nextFilter);

        Field field = SampleTarget.class.getDeclaredField("concreteField");
        SampleTarget instance = new SampleTarget();

        filter.filterCandidate(Collections.emptyList(), field, instance);

        assertNotNull(nextFilter.capturedMocks);
        assertEquals(0, nextFilter.capturedMocks.size());
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_SetCollectionInput() throws Exception {
        RecordingMockCandidateFilter nextFilter = new RecordingMockCandidateFilter();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(nextFilter);

        Field field = SampleTarget.class.getDeclaredField("concreteField");
        SampleTarget instance = new SampleTarget();

        ServiceImpl service = new ServiceImpl();
        Set<Object> mockSet = new HashSet<Object>();
        mockSet.add(service);

        filter.filterCandidate(mockSet, field, instance);

        assertEquals(1, nextFilter.capturedMocks.size());
        assertTrue(nextFilter.capturedMocks.contains(service));
        assertTrue("Filter output must be an instance of List", nextFilter.capturedMocks instanceof List);
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_MultipleTypesFiltering() throws Exception {
        RecordingMockCandidateFilter nextFilter = new RecordingMockCandidateFilter();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(nextFilter);

        Field field = SampleTarget.class.getDeclaredField("serviceField");
        SampleTarget instance = new SampleTarget();

        List<Object> mocks = new ArrayList<Object>();
        mocks.add(new AnotherUnrelatedType());
        mocks.add(new ServiceImpl());
        mocks.add("StringCandidate");
        mocks.add(new SubServiceImpl());
        mocks.add(Integer.valueOf(100));

        filter.filterCandidate(mocks, field, instance);

        assertEquals(2, nextFilter.capturedMocks.size());
    }

    @Test(timeout = 4000)
    public void testFilterCandidate_PrimitiveVsWrapperHandling() throws Exception {
        RecordingMockCandidateFilter nextFilter = new RecordingMockCandidateFilter();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(nextFilter);

        Field primitiveField = SampleTarget.class.getDeclaredField("primitiveIntField");
        Field wrapperField = SampleTarget.class.getDeclaredField("wrapperIntField");
        SampleTarget instance = new SampleTarget();

        Integer boxedInt = Integer.valueOf(42);
        List<Object> mocks = Collections.singletonList((Object) boxedInt);

        // int.class.isAssignableFrom(Integer.class) is FALSE in Java reflection
        filter.filterCandidate(mocks, primitiveField, instance);
        assertEquals("Primitive class does not match boxed wrapper via isAssignableFrom", 0, nextFilter.capturedMocks.size());

        // Integer.class.isAssignableFrom(Integer.class) is TRUE
        filter.filterCandidate(mocks, wrapperField, instance);
        assertEquals("Wrapper class matches boxed wrapper via isAssignableFrom", 1, nextFilter.capturedMocks.size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Zone (Ground Truth from Defects4J)
    // =========================================================================

    /**
     * Ground Truth Defect:
     * org.mockitousage.annotation.MockInjectionUsingSetterOrPropertyTest::shouldInsertFieldWithCorrectNameWhenMultipleTypesAvailable
     * When multiple mocks with the same or compatible type are present (e.g. multiple Strings "candidate1", "candidate2"),
     * TypeBasedCandidateFilter must preserve all type matches and forward them to the downstream filter
     * (e.g. NameBasedCandidateFilter) without dropping any candidate or prematurely resolving to the wrong candidate.
     */
    @Test(timeout = 4000)
    public void testDefect_MultipleMatchingCandidatesDisambiguation() throws Exception {
        final List<Collection<Object>> forwardedMocksHistory = new ArrayList<Collection<Object>>();

        MockCandidateFilter downstreamFilter = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object fieldInstance) {
                forwardedMocksHistory.add(mocks);
                // Verify that downstream receives both candidates so that name disambiguation can occur
                assertEquals("Downstream filter must receive all matching candidates for name resolution", 2, mocks.size());
                return new OngoingInjecter() {
                    public boolean thenInject() {
                        return true;
                    }
                };
            }
        };

        TypeBasedCandidateFilter typeFilter = new TypeBasedCandidateFilter(downstreamFilter);

        Field candidate1Field = SampleTarget.class.getDeclaredField("candidate1");
        SampleTarget targetInstance = new SampleTarget();

        String candidate1Mock = "candidate1";
        String candidate2Mock = "candidate2";
        List<Object> mocks = Arrays.asList((Object) candidate1Mock, candidate2Mock);

        OngoingInjecter injecter = typeFilter.filterCandidate(mocks, candidate1Field, targetInstance);

        assertNotNull("Injecter returned from downstream filter must not be null", injecter);
        assertEquals("Downstream filter should be invoked once", 1, forwardedMocksHistory.size());

        List<Object> forwardedList = (List<Object>) forwardedMocksHistory.get(0);
        assertEquals("Both candidates must be forwarded", 2, forwardedList.size());
        assertEquals("Candidate 1 must be present in order", candidate1Mock, forwardedList.get(0));
        assertEquals("Candidate 2 must be present in order", candidate2Mock, forwardedList.get(1));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testFilterCandidate_NullMockInCollectionThrowsNPE() throws Exception {
        RecordingMockCandidateFilter nextFilter = new RecordingMockCandidateFilter();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(nextFilter);

        Field field = SampleTarget.class.getDeclaredField("concreteField");
        SampleTarget instance = new SampleTarget();

        List<Object> mocks = new ArrayList<Object>();
        mocks.add(null);

        // mock.getClass() will throw NullPointerException on null element
        filter.filterCandidate(mocks, field, instance);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testFilterCandidate_NullFieldThrowsNPE() {
        RecordingMockCandidateFilter nextFilter = new RecordingMockCandidateFilter();
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(nextFilter);

        List<Object> mocks = Collections.singletonList((Object) new ServiceImpl());
        filter.filterCandidate(mocks, null, new SampleTarget());
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testFilterCandidate_NullNextFilterThrowsNPE() throws Exception {
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(null);
        Field field = SampleTarget.class.getDeclaredField("concreteField");
        filter.filterCandidate(Collections.emptyList(), field, new SampleTarget());
    }

    // =========================================================================
    // Partition E: Object Construction & Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor_FieldAssignment() {
        MockCandidateFilter next = new MockCandidateFilter() {
            public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object fieldInstance) {
                return null;
            }
        };

        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        assertSame("Next filter reference must be assigned", next, filter.next);
    }
}