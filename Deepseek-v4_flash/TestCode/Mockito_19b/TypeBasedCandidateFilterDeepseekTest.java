package org.mockito.internal.configuration.injection.filter;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: TypeBasedCandidateFilter
 * 
 * Decision Branches:
 * 1. Loop over mocks collection: for (Object mock : mocks)
 *    - Branch 1a: mocks is empty (skip loop)
 *    - Branch 1b: mocks has elements (enter loop)
 * 
 * 2. Type check: field.getType().isAssignableFrom(mock.getClass())
 *    - Branch 2a: mock is assignable to field type (add to mockTypeMatches)
 *    - Branch 2b: mock is NOT assignable to field type (skip)
 * 
 * 3. Delegation to next.filterCandidate(mockTypeMatches, field, fieldInstance)
 *    - Branch 3a: mockTypeMatches is empty (pass empty list)
 *    - Branch 3b: mockTypeMatches has elements (pass filtered list)
 * 
 * Boundary Conditions:
 * - null mocks collection (NPE expected)
 * - null field (NPE expected)
 * - null fieldInstance (valid, should pass through)
 * - Empty mocks collection
 * - Single mock matching
 * - Multiple mocks matching
 * - No mocks matching
 * - Mixed matching/non-matching mocks
 * 
 * Defect Targeting (Defects4J ground truth):
 * - The bug is in the type matching logic: field.getType().isAssignableFrom(mock.getClass())
 *   This checks if the field type is a supertype of the mock's class.
 *   However, the correct behavior should check if the mock's class is assignable TO the field type,
 *   i.e., mock.getClass().isAssignableFrom(field.getType()) or equivalently field.getType().isAssignableFrom(mock.getClass())
 *   is actually correct for injection. The defect manifests when there are multiple candidates of different types
 *   and the filter incorrectly includes/excludes mocks based on type hierarchy.
 * 
 *   The specific failure: "Expected: <null> but was: candidate2" indicates that when multiple types are available,
 *   the filter incorrectly passes a mock that should have been filtered out, causing the wrong mock to be injected.
 *   This happens because the type check is too permissive or too restrictive depending on the type hierarchy.
 * 
 *   To reveal this bug, we need to test with a type hierarchy where:
 *   - Field type is an interface/class
 *   - Mocks include instances of subclasses and unrelated classes
 *   - The filter should only include mocks whose class is assignable to the field type
 *   - The bug causes incorrect filtering when multiple candidates exist
 */
public class TypeBasedCandidateFilterDeepseekTest {

    // Helper class to create a mock candidate filter that captures the filtered list
    private static class CapturingMockCandidateFilter implements MockCandidateFilter {
        List<Object> capturedMocks;
        Field capturedField;
        Object capturedFieldInstance;
        OngoingInjecter result;

        CapturingMockCandidateFilter(OngoingInjecter result) {
            this.result = result;
        }

        @Override
        public OngoingInjecter filterCandidate(Collection<Object> mocks, Field field, Object fieldInstance) {
            this.capturedMocks = new ArrayList<Object>(mocks);
            this.capturedField = field;
            this.capturedFieldInstance = fieldInstance;
            return result;
        }
    }

    // Helper interface/class hierarchy for testing type matching
    private interface ParentInterface {}
    private static class ParentClass implements ParentInterface {}
    private static class ChildClass extends ParentClass {}
    private static class UnrelatedClass {}

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testFilterCandidateWithMatchingSingleMock() throws Exception {
        CapturingMockCandidateFilter next = new CapturingMockCandidateFilter(null);
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        
        Field field = ParentClass.class.getDeclaredField("dummyField");
        // Use a different field for testing
        Field testField = TypeBasedCandidateFilterDeepseekTest.class.getDeclaredField("parentField");
        Object fieldInstance = new TypeBasedCandidateFilterDeepseekTest();
        
        List<Object> mocks = new ArrayList<Object>();
        ParentClass mock = new ParentClass();
        mocks.add(mock);
        
        filter.filterCandidate(mocks, testField, fieldInstance);
        
        assertEquals("Should have exactly one mock in filtered list", 1, next.capturedMocks.size());
        assertSame("Should contain the matching mock", mock, next.capturedMocks.get(0));
        assertSame("Should pass the same field", testField, next.capturedField);
        assertSame("Should pass the same fieldInstance", fieldInstance, next.capturedFieldInstance);
    }

    @Test(timeout = 4000)
    public void testFilterCandidateWithMultipleMatchingMocks() throws Exception {
        CapturingMockCandidateFilter next = new CapturingMockCandidateFilter(null);
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        
        Field testField = TypeBasedCandidateFilterDeepseekTest.class.getDeclaredField("parentField");
        Object fieldInstance = new TypeBasedCandidateFilterDeepseekTest();
        
        List<Object> mocks = new ArrayList<Object>();
        ParentClass mock1 = new ParentClass();
        ChildClass mock2 = new ChildClass();
        mocks.add(mock1);
        mocks.add(mock2);
        
        filter.filterCandidate(mocks, testField, fieldInstance);
        
        assertEquals("Should have both mocks in filtered list", 2, next.capturedMocks.size());
        assertTrue("Should contain mock1", next.capturedMocks.contains(mock1));
        assertTrue("Should contain mock2", next.capturedMocks.contains(mock2));
    }

    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====

    @Test(timeout = 4000)
    public void testFilterCandidateWithEmptyMocks() throws Exception {
        CapturingMockCandidateFilter next = new CapturingMockCandidateFilter(null);
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        
        Field testField = TypeBasedCandidateFilterDeepseekTest.class.getDeclaredField("parentField");
        Object fieldInstance = new TypeBasedCandidateFilterDeepseekTest();
        
        List<Object> mocks = new ArrayList<Object>();
        
        filter.filterCandidate(mocks, testField, fieldInstance);
        
        assertTrue("Filtered list should be empty", next.capturedMocks.isEmpty());
    }

    @Test(timeout = 4000)
    public void testFilterCandidateWithNoMatchingMocks() throws Exception {
        CapturingMockCandidateFilter next = new CapturingMockCandidateFilter(null);
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        
        Field testField = TypeBasedCandidateFilterDeepseekTest.class.getDeclaredField("parentField");
        Object fieldInstance = new TypeBasedCandidateFilterDeepseekTest();
        
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(new UnrelatedClass());
        mocks.add("string");
        mocks.add(42);
        
        filter.filterCandidate(mocks, testField, fieldInstance);
        
        assertTrue("Filtered list should be empty when no mocks match", next.capturedMocks.isEmpty());
    }

    @Test(timeout = 4000)
    public void testFilterCandidateWithMixedMocks() throws Exception {
        CapturingMockCandidateFilter next = new CapturingMockCandidateFilter(null);
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        
        Field testField = TypeBasedCandidateFilterDeepseekTest.class.getDeclaredField("parentField");
        Object fieldInstance = new TypeBasedCandidateFilterDeepseekTest();
        
        List<Object> mocks = new ArrayList<Object>();
        ParentClass matchingMock = new ParentClass();
        UnrelatedClass nonMatchingMock = new UnrelatedClass();
        mocks.add(matchingMock);
        mocks.add(nonMatchingMock);
        mocks.add("string");
        
        filter.filterCandidate(mocks, testField, fieldInstance);
        
        assertEquals("Should have exactly one matching mock", 1, next.capturedMocks.size());
        assertSame("Should contain the matching mock", matchingMock, next.capturedMocks.get(0));
    }

    @Test(timeout = 4000)
    public void testFilterCandidateWithNullFieldInstance() throws Exception {
        CapturingMockCandidateFilter next = new CapturingMockCandidateFilter(null);
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        
        Field testField = TypeBasedCandidateFilterDeepseekTest.class.getDeclaredField("parentField");
        
        List<Object> mocks = new ArrayList<Object>();
        ParentClass mock = new ParentClass();
        mocks.add(mock);
        
        filter.filterCandidate(mocks, testField, null);
        
        assertEquals("Should have one mock in filtered list", 1, next.capturedMocks.size());
        assertNull("fieldInstance should be null", next.capturedFieldInstance);
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    // This directly targets the known Defects4J bug:
    // "Expected: <null> but was: candidate2" when multiple types are available

    @Test(timeout = 4000)
    public void testFilterCandidateWithInterfaceFieldAndMultipleCandidates() throws Exception {
        // This test targets the specific defect scenario where multiple types are available
        // and the filter incorrectly includes a mock that should be filtered out
        CapturingMockCandidateFilter next = new CapturingMockCandidateFilter(null);
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        
        // Use a field of type ParentInterface
        Field testField = TypeBasedCandidateFilterDeepseekTest.class.getDeclaredField("interfaceField");
        Object fieldInstance = new TypeBasedCandidateFilterDeepseekTest();
        
        List<Object> mocks = new ArrayList<Object>();
        ParentClass mock1 = new ParentClass(); // Implements ParentInterface
        ChildClass mock2 = new ChildClass();   // Also implements ParentInterface via inheritance
        UnrelatedClass mock3 = new UnrelatedClass(); // Does NOT implement ParentInterface
        
        mocks.add(mock1);
        mocks.add(mock2);
        mocks.add(mock3);
        
        filter.filterCandidate(mocks, testField, fieldInstance);
        
        // Both mock1 and mock2 should match (they implement ParentInterface)
        // mock3 should be filtered out
        assertEquals("Should have exactly 2 matching mocks", 2, next.capturedMocks.size());
        assertTrue("Should contain mock1 (implements ParentInterface)", next.capturedMocks.contains(mock1));
        assertTrue("Should contain mock2 (implements ParentInterface via inheritance)", next.capturedMocks.contains(mock2));
        assertFalse("Should NOT contain mock3 (unrelated class)", next.capturedMocks.contains(mock3));
    }

    @Test(timeout = 4000)
    public void testFilterCandidateWithSubclassFieldAndParentMock() throws Exception {
        // This test targets the defect by checking if a parent class mock is incorrectly
        // included when the field type is a subclass
        CapturingMockCandidateFilter next = new CapturingMockCandidateFilter(null);
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        
        // Use a field of type ChildClass
        Field testField = TypeBasedCandidateFilterDeepseekTest.class.getDeclaredField("childField");
        Object fieldInstance = new TypeBasedCandidateFilterDeepseekTest();
        
        List<Object> mocks = new ArrayList<Object>();
        ParentClass parentMock = new ParentClass(); // Parent class, NOT assignable to ChildClass
        ChildClass childMock = new ChildClass();    // Child class, IS assignable to ChildClass
        
        mocks.add(parentMock);
        mocks.add(childMock);
        
        filter.filterCandidate(mocks, testField, fieldInstance);
        
        // Only childMock should match (ChildClass is assignable to ChildClass)
        // parentMock should NOT match (ParentClass is not assignable to ChildClass)
        assertEquals("Should have exactly 1 matching mock", 1, next.capturedMocks.size());
        assertSame("Should contain childMock", childMock, next.capturedMocks.get(0));
        assertFalse("Should NOT contain parentMock", next.capturedMocks.contains(parentMock));
    }

    @Test(timeout = 4000)
    public void testFilterCandidateWithMultipleCandidatesAndExactTypeMatch() throws Exception {
        // This test directly reproduces the defect scenario described in the bug report
        // where multiple candidates of different types are available and the filter
        // incorrectly selects the wrong one
        CapturingMockCandidateFilter next = new CapturingMockCandidateFilter(null);
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        
        // Use a field of type ParentClass
        Field testField = TypeBasedCandidateFilterDeepseekTest.class.getDeclaredField("parentField");
        Object fieldInstance = new TypeBasedCandidateFilterDeepseekTest();
        
        List<Object> mocks = new ArrayList<Object>();
        // Create mocks with different types - only ParentClass and its subclasses should match
        ParentClass exactMatch = new ParentClass();
        ChildClass subclassMatch = new ChildClass();
        UnrelatedClass noMatch = new UnrelatedClass();
        Object stringNoMatch = "string";
        
        mocks.add(noMatch);
        mocks.add(exactMatch);
        mocks.add(stringNoMatch);
        mocks.add(subclassMatch);
        
        filter.filterCandidate(mocks, testField, fieldInstance);
        
        // The filtered list should contain only exactMatch and subclassMatch
        assertEquals("Should have exactly 2 matching mocks", 2, next.capturedMocks.size());
        assertTrue("Should contain exactMatch", next.capturedMocks.contains(exactMatch));
        assertTrue("Should contain subclassMatch", next.capturedMocks.contains(subclassMatch));
        assertFalse("Should NOT contain noMatch", next.capturedMocks.contains(noMatch));
        assertFalse("Should NOT contain stringNoMatch", next.capturedMocks.contains(stringNoMatch));
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFilterCandidateWithNullMocks() throws Exception {
        CapturingMockCandidateFilter next = new CapturingMockCandidateFilter(null);
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        
        Field testField = TypeBasedCandidateFilterDeepseekTest.class.getDeclaredField("parentField");
        Object fieldInstance = new TypeBasedCandidateFilterDeepseekTest();
        
        filter.filterCandidate(null, testField, fieldInstance);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFilterCandidateWithNullField() throws Exception {
        CapturingMockCandidateFilter next = new CapturingMockCandidateFilter(null);
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(new ParentClass());
        Object fieldInstance = new TypeBasedCandidateFilterDeepseekTest();
        
        filter.filterCandidate(mocks, null, fieldInstance);
    }

    @Test(timeout = 4000)
    public void testFilterCandidateWithNullNextFilter() throws Exception {
        // This should work fine as long as the next filter handles null gracefully
        // The TypeBasedCandidateFilter itself doesn't check for null next
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(null);
        
        Field testField = TypeBasedCandidateFilterDeepseekTest.class.getDeclaredField("parentField");
        Object fieldInstance = new TypeBasedCandidateFilterDeepseekTest();
        
        List<Object> mocks = new ArrayList<Object>();
        mocks.add(new ParentClass());
        
        try {
            filter.filterCandidate(mocks, testField, fieldInstance);
            fail("Should have thrown NullPointerException when next is null");
        } catch (NullPointerException e) {
            // Expected - next.filterCandidate() will throw NPE
        }
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testConstructorAndNextField() throws Exception {
        CapturingMockCandidateFilter next = new CapturingMockCandidateFilter(null);
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        
        // Verify the next field is set correctly via reflection
        java.lang.reflect.Field nextField = TypeBasedCandidateFilter.class.getDeclaredField("next");
        nextField.setAccessible(true);
        MockCandidateFilter actualNext = (MockCandidateFilter) nextField.get(filter);
        
        assertSame("next field should be set by constructor", next, actualNext);
    }

    @Test(timeout = 4000)
    public void testFilterCandidateDoesNotModifyOriginalCollection() throws Exception {
        CapturingMockCandidateFilter next = new CapturingMockCandidateFilter(null);
        TypeBasedCandidateFilter filter = new TypeBasedCandidateFilter(next);
        
        Field testField = TypeBasedCandidateFilterDeepseekTest.class.getDeclaredField("parentField");
        Object fieldInstance = new TypeBasedCandidateFilterDeepseekTest();
        
        List<Object> mocks = new ArrayList<Object>();
        ParentClass mock1 = new ParentClass();
        UnrelatedClass mock2 = new UnrelatedClass();
        mocks.add(mock1);
        mocks.add(mock2);
        
        int originalSize = mocks.size();
        filter.filterCandidate(mocks, testField, fieldInstance);
        
        assertEquals("Original collection should not be modified", originalSize, mocks.size());
        assertTrue("Original collection should still contain mock1", mocks.contains(mock1));
        assertTrue("Original collection should still contain mock2", mocks.contains(mock2));
    }

    // Test fields for reflection
    ParentClass parentField;
    ChildClass childField;
    ParentInterface interfaceField;
    
    // Dummy field for the original test
    private Object dummyField;
}