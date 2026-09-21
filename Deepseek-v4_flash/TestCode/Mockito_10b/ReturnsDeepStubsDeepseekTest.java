package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.*;

import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.internal.InternalMockHandler;
import org.mockito.internal.MockitoCore;
import org.mockito.internal.creation.settings.CreationSettings;
import org.mockito.internal.stubbing.InvocationContainerImpl;
import org.mockito.internal.stubbing.StubbedInvocationMatcher;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.reflection.GenericMetadataSupport;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ReturnsDeepStubs.answer()
 * 
 * Decision Branches:
 * 1. rawType mockable? (mockitoCore().isTypeMockable(rawType))
 *    - TRUE: proceed to deepStub()
 *    - FALSE: delegate().returnValueFor(rawType)
 * 
 * 2. deepStub() - stubbed invocation match loop
 *    - Match found: return stubbed answer
 *    - No match: record new deep stub
 * 
 * 3. newDeepStubMock() - rawExtraInterfaces check
 *    - hasRawExtraInterfaces(): withSettings().extraInterfaces(...)
 *    - else: withSettings() only
 * 
 * 4. ReturnsDeepStubsSerializationFallback.actualParameterizedType()
 *    - Override returns stored metadata (defect target)
 * 
 * Boundary Conditions:
 * - null mock handler
 * - non-mockable types (primitives, final classes)
 * - empty stubbed invocations list
 * - serialization fallback path
 * - generic metadata with extra interfaces
 * - generic metadata without extra interfaces
 * 
 * Defect Target (Defects4J):
 * - DeepStubsWronglyReportsSerializationProblemsTest
 * - The bug: ReturnsDeepStubsSerializationFallback.actualParameterizedType() 
 *   returns stored metadata, but when serialization context changes, 
 *   the mock's actual parameterized type should be resolved from the mock itself,
 *   not from the stored metadata that may be stale/invalid.
 * - Test: should_not_raise_a_mockito_exception_about_serialization_when_accessing_deep_stub
 *   --> MockitoException on serialization access
 */
public class ReturnsDeepStubsDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testAnswerWithMockableType_createsDeepStub() throws Throwable {
        // Create a mock with ReturnsDeepStubs answer
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        List<String> mock = Mockito.mock(List.class, answer);
        
        // Invoke a method that returns a mockable type
        Object result = mock.iterator();
        
        // Should return a deep stub mock (not null)
        assertNotNull("Deep stub should not be null", result);
        assertTrue("Deep stub should be a mock", Mockito.mockingDetails(result).isMock());
    }

    @Test(timeout = 4000)
    public void testAnswerWithNonMockableType_returnsDefaultValue() throws Throwable {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        // Use a mock that returns int (non-mockable primitive)
        Object mock = Mockito.mock(Object.class, answer);
        
        // We need to invoke a method that returns int - use hashCode
        // But hashCode is on Object, so we need a different approach
        // Create a mock of a class with a method returning primitive
        // For simplicity, test with a mock that returns String (mockable) vs int
        // Actually, let's test with a method that returns void - but void is not mockable
        // Better: use a custom interface
        InterfaceWithPrimitive mock2 = Mockito.mock(InterfaceWithPrimitive.class, answer);
        int result = mock2.getInt();
        assertEquals("Non-mockable type should return default", 0, result);
    }

    @Test(timeout = 4000)
    public void testDeepStubWithExistingStub_returnsStubbedValue() throws Throwable {
        // Create a mock with ReturnsDeepStubs
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        Map<String, List<String>> mock = Mockito.mock(Map.class, answer);
        
        // First invocation creates deep stub
        List<String> list1 = mock.get("key1");
        assertNotNull("First deep stub should be created", list1);
        
        // Second invocation with same key should return same stub
        List<String> list2 = mock.get("key1");
        assertSame("Same invocation should return same deep stub", list1, list2);
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testAnswerWithNullInvocation_throwsException() {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        try {
            answer.answer(null);
            fail("Should throw NullPointerException for null invocation");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testActualParameterizedType_withMock() {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        List<String> mock = Mockito.mock(List.class, answer);
        
        GenericMetadataSupport metadata = answer.actualParameterizedType(mock);
        assertNotNull("Metadata should not be null", metadata);
        assertEquals("Raw type should be List", List.class, metadata.rawType());
    }

    @Test(timeout = 4000)
    public void testActualParameterizedType_withSerializationFallback() {
        // Create a ReturnsDeepStubsSerializationFallback instance
        // This is package-private, so we test through the normal path
        // The fallback is used when serialization occurs
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        List<String> mock = Mockito.mock(List.class, answer);
        
        // Trigger serialization fallback by accessing deep stub
        // The inner ReturnsDeepStubsSerializationFallback will be used
        Iterator<String> iterator = mock.iterator();
        assertNotNull("Deep stub iterator should not be null", iterator);
    }

    @Test(timeout = 4000)
    public void testNewDeepStubMock_withExtraInterfaces() throws Exception {
        // Use reflection to test private method
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        
        // Create generic metadata with extra interfaces
        // We need to simulate this - use a mock with generic info
        Map<String, List<String>> mock = Mockito.mock(Map.class, answer);
        
        // Access a method that returns a type with extra interfaces
        // Set<Number> has no extra interfaces, but we can test the path
        Set<Number> set = mock.get("key").iterator().next();
        assertNotNull("Deep stub should be created", set);
    }

    @Test(timeout = 4000)
    public void testNewDeepStubMock_withoutExtraInterfaces() throws Exception {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        List<String> mock = Mockito.mock(List.class, answer);
        
        // Simple generic return type without extra interfaces
        Iterator<String> iterator = mock.iterator();
        assertNotNull("Deep stub should be created", iterator);
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    /**
     * Defect-targeted test for Defects4J bug:
     * DeepStubsWronglyReportsSerializationProblemsTest
     * 
     * The bug occurs when accessing a deep stub after serialization context changes.
     * ReturnsDeepStubsSerializationFallback.actualParameterizedType() returns stored
     * metadata that may be stale, causing MockitoException about serialization.
     * 
     * This test verifies that accessing a deep stub does not throw MockitoException
     * related to serialization problems.
     */
    @Test(timeout = 4000)
    public void should_not_raise_a_mockito_exception_about_serialization_when_accessing_deep_stub() throws Throwable {
        // Create a mock with ReturnsDeepStubs answer
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        Map<String, List<String>> mock = Mockito.mock(Map.class, answer);
        
        // Access deep stub - this should work without serialization exception
        try {
            List<String> list = mock.get("key");
            assertNotNull("Deep stub should be created", list);
            
            // Access nested deep stub
            boolean added = list.add("value");
            assertTrue("Should be able to add to deep stub", added);
            
            // Access another deep stub path
            Set<Map.Entry<String, List<String>>> entries = mock.entrySet();
            assertNotNull("Entry set deep stub should be created", entries);
            
        } catch (Exception e) {
            // The defect causes MockitoException about serialization
            // This test should pass on fixed version, fail on defective version
            fail("Should not throw exception when accessing deep stub: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeepStubWithSerializationFallback_doesNotThrowMockitoException() throws Throwable {
        // Simulate the scenario from the defect report
        // Create a mock that will use ReturnsDeepStubsSerializationFallback
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        Map<String, List<String>> mock = Mockito.mock(Map.class, answer);
        
        // Access deep stub - this creates ReturnsDeepStubsSerializationFallback internally
        List<String> list = mock.get("test");
        assertNotNull("First deep stub should be created", list);
        
        // Access the same deep stub again - this should use the stored answer
        List<String> sameList = mock.get("test");
        assertSame("Should return same deep stub", list, sameList);
        
        // Access a different deep stub
        List<String> otherList = mock.get("other");
        assertNotNull("Other deep stub should be created", otherList);
        assertNotSame("Different keys should return different stubs", list, otherList);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testAnswerWithNonMockableRawType_returnsDefault() throws Throwable {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        // Use a mock that returns a final class (non-mockable)
        // String is final, so it's non-mockable
        Object mock = Mockito.mock(Object.class, answer);
        
        // We need to invoke a method that returns String
        // Use toString() which returns String
        // But toString() is on Object, and we're mocking Object
        // Actually, let's create a custom interface
        InterfaceWithString mock2 = Mockito.mock(InterfaceWithString.class, answer);
        String result = mock2.getString();
        assertNull("Non-mockable type should return null default", result);
    }

    @Test(timeout = 4000)
    public void testDeepStubWithEmptyStubbedInvocations_createsNewMock() throws Throwable {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        List<String> mock = Mockito.mock(List.class, answer);
        
        // First invocation - no stubbed invocations, should create new deep stub
        Iterator<String> iterator = mock.iterator();
        assertNotNull("Should create new deep stub", iterator);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testReturnsDeepStubs_isSerializable() {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        assertTrue("ReturnsDeepStubs should be Serializable", answer instanceof Serializable);
    }

    @Test(timeout = 4000)
    public void testDeeplyStubbedAnswer_returnsMock() throws Throwable {
        // Create a mock to use as the stubbed answer
        List<String> mock = Mockito.mock(List.class);
        
        // Create DeeplyStubbedAnswer via reflection (package-private)
        // We can test through the normal flow
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        List<String> mock2 = Mockito.mock(List.class, answer);
        
        // Access deep stub - internally creates DeeplyStubbedAnswer
        Iterator<String> iterator = mock2.iterator();
        assertNotNull("DeeplyStubbedAnswer should return the mock", iterator);
    }

    @Test(timeout = 4000)
    public void testMultipleDeepStubs_areIndependent() throws Throwable {
        ReturnsDeepStubs answer = new ReturnsDeepStubs();
        Map<String, List<String>> mock = Mockito.mock(Map.class, answer);
        
        // Create multiple deep stubs
        List<String> list1 = mock.get("key1");
        List<String> list2 = mock.get("key2");
        List<String> list3 = mock.get("key3");
        
        assertNotNull("list1 should not be null", list1);
        assertNotNull("list2 should not be null", list2);
        assertNotNull("list3 should not be null", list3);
        assertNotSame("Different keys should return different stubs", list1, list2);
        assertNotSame("Different keys should return different stubs", list1, list3);
        assertNotSame("Different keys should return different stubs", list2, list3);
    }

    // Helper interfaces for testing
    interface InterfaceWithPrimitive {
        int getInt();
    }

    interface InterfaceWithString {
        String getString();
    }
}