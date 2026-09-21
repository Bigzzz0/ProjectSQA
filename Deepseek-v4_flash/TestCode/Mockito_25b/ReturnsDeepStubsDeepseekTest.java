package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Test suite for ReturnsDeepStubs, targeting high line/branch coverage and
 * the known Defects4J bug related to deep stubbing with generics (multiple bounds, bounded wildcards).
 *
 * [Branch & Defect Analysis Matrix]
 * - Branch 1: isTypeMockable(rawType) == true -> proceed to getMock()
 *   - Sub-branch: get mock from stubbed invocations (matching) -> return stubbed answer
 *   - Sub-branch: no matching stubbed invocation -> recordDeepStubMock() -> return newly created deep stub
 * - Branch 2: isTypeMockable(rawType) == false -> return delegate.returnValueFor(rawType)
 * - Defect: When method return type involves type variables with multiple bounds or bounded wildcards,
 *   recordDeepStubMock() uses raw type (erasure) instead of the resolved generic type,
 *   causing ClassCastException when the caller expects the bound type (e.g., Cloneable, Number).
 */
public class ReturnsDeepStubsDeepseekTest {

    // Helper interfaces for generic testing
    interface SimpleInterface {
        List<String> getList();
    }

    interface NonMockableReturn {
        String getString();
    }

    interface MultiBoundInterface<K extends Comparable<K> & Cloneable> {
        K getKey();
    }

    interface BoundedWildcardInterface {
        Set<? extends Number> getSet();
    }

    interface ChainedInterface {
        Map<String, Set<Integer>> getMap();
    }

    interface VoidReturn {
        void doSomething();
    }

    @Test(timeout = 4000)
    public void testDeepStubSimpleReturnType() {
        // Arrange
        SimpleInterface mock = Mockito.mock(SimpleInterface.class, new ReturnsDeepStubs());

        // Act
        List<String> result = mock.getList();

        // Assert
        assertNotNull("Deep stub should not be null", result);
        assertTrue("Deep stub should be a mock of List", result instanceof List);
    }

    @Test(timeout = 4000)
    public void testDeepStubNonMockableReturnType() {
        // Arrange
        NonMockableReturn mock = Mockito.mock(NonMockableReturn.class, new ReturnsDeepStubs());

        // Act
        String result = mock.getString();

        // Assert: String is not mockable (final) -> should return default value (null)
        assertNull("Non-mockable type should return null", result);
    }

    @Test(timeout = 4000)
    public void testDeepStubStubbedInvocationMatch() {
        // Arrange
        SimpleInterface mock = Mockito.mock(SimpleInterface.class, new ReturnsDeepStubs());
        List<String> expected = Arrays.asList("a", "b");
        Mockito.when(mock.getList()).thenReturn(expected);

        // Act
        List<String> result = mock.getList();

        // Assert: should return the stubbed value, not a deep stub
        assertSame("Should return the previously stubbed value", expected, result);
    }

    @Test(timeout = 4000)
    public void testDeepStubMultipleBounds() {
        // Arrange
        MultiBoundInterface<Comparable<Object>> mock = Mockito.mock(
            (Class<MultiBoundInterface<Comparable<Object>>>) (Class<?>) MultiBoundInterface.class,
            new ReturnsDeepStubs()
        );

        // Act
        Object result = mock.getKey();

        // Assert: the deep stub should be castable to Cloneable (one of the bounds)
        // This triggers the known ClassCastException on buggy versions.
        try {
            Cloneable cloneable = (Cloneable) result;
            // If we reach here, the test passed (expected behavior)
        } catch (ClassCastException e) {
            fail("Deep stub should be castable to Cloneable, but got ClassCastException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeepStubBoundedWildcard() {
        // Arrange
        BoundedWildcardInterface mock = Mockito.mock(
            BoundedWildcardInterface.class,
            new ReturnsDeepStubs()
        );

        // Act
        Object result = mock.getSet();

        // Assert: the deep stub should be castable to Set<? extends Number>, i.e., Set<Number>
        // This triggers the known ClassCastException on buggy versions.
        try {
            Set<? extends Number> numberSet = (Set<? extends Number>) result;
        } catch (ClassCastException e) {
            fail("Deep stub should be castable to Set<? extends Number>, but got ClassCastException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeepStubVoidReturnType() {
        // Arrange
        VoidReturn mock = Mockito.mock(VoidReturn.class, new ReturnsDeepStubs());

        // Act
        Object result = mock.doSomething();

        // Assert: void methods return null from delegate
        assertNull("Void method should return null", result);
    }

    @Test(timeout = 4000)
    public void testDeepStubChainedCalls() {
        // Arrange
        ChainedInterface mock = Mockito.mock(ChainedInterface.class, new ReturnsDeepStubs());

        // Act
        Map<String, Set<Integer>> map = mock.getMap();

        // Assert: first-level deep stub
        assertNotNull("Map deep stub should not be null", map);
        assertTrue("Should return a mock of Map", map instanceof Map);

        // Now chain a call: map.get("key") should return a Set<Integer> deep stub
        Set<Integer> set = map.get("key");
        assertNotNull("Set deep stub should not be null", set);
        assertTrue("Should return a mock of Set", set instanceof Set);

        // Chain further: set.iterator() -> Iterator<Integer> deep stub
        Iterator<Integer> iterator = set.iterator();
        assertNotNull("Iterator deep stub should not be null", iterator);
        assertTrue("Should return a mock of Iterator", iterator instanceof Iterator);
    }

    @Test(timeout = 4000)
    public void testAnswerWithNullInvocation() {
        // This test exercises the delegate path when isTypeMockable fails for a non-object type.
        // We cannot directly call answer(null) because it would NPE.
        // Instead, we create a mock of an interface returning a primitive-like type (e.g., int or char).
        // Since primitive wrappers are not mockable in standard Mockito, we can test with int.

        // Arrange
        interface PrimitiveReturn {
            int getInt();
        }
        PrimitiveReturn mock = Mockito.mock(PrimitiveReturn.class, new ReturnsDeepStubs());

        // Act
        int result = mock.getInt();

        // Assert: For primitive int, delegate.returnValueFor(int.class) returns 0.
        assertEquals("Primitive int should return default value 0", 0, result);
    }
}