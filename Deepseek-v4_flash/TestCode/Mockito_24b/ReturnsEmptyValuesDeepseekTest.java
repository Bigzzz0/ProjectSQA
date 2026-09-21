package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * White-box test suite for ReturnsEmptyValues.
 * Targets complete branch coverage and reveals the known compareTo defect.
 */
/*
 * [Branch & Defect Analysis Matrix]
 * Branch coverage targets:
 *   answer():
 *     1) isToString true -> name.isDefault() true/false
 *     2) isCompareToMethod true -> return 1 (defective: should be 0 for same reference)
 *     3) fallback to returnValueFor
 *   returnValueFor():
 *     4) Primitives.isPrimitiveOrWrapper true
 *     5) else-if chain for 14 collection types
 *     6) default null return
 * Known defect: compareTo returns 1 unconditionally; should return 0 when mock compared to itself.
 * Tests:
 *   - should_return_zero_when_compareTo_on_same_mock (fails on defective version)
 *   - should_return_one_when_compareTo_on_different_mocks (passes on defective version)
 *   - toString with default/non-default name
 *   - all returnValueFor branches
 *   - primitive/wrapper boundaries
 */
public class ReturnsEmptyValuesDeepseekTest {

    // ---------- Helper stubs ----------

    private static class SimpleInvocation implements InvocationOnMock {
        private final Method method;
        private final Object mock;
        private final Object[] arguments;

        SimpleInvocation(Method method, Object mock, Object[] arguments) {
            this.method = method;
            this.mock = mock;
            this.arguments = arguments;
        }

        @Override
        public Object getMock() {
            return mock;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Object[] getArguments() {
            return arguments;
        }

        @Override
        public void setArguments(Object... arguments) {
            throw new UnsupportedOperationException();
        }
    }

    private static class TestableObjectMethodsGuru extends ObjectMethodsGuru {
        private boolean toStringReturn = false;
        private boolean compareToReturn = false;

        void setToString(boolean val) {
            this.toStringReturn = val;
        }

        void setCompareTo(boolean val) {
            this.compareToReturn = val;
        }

        @Override
        public boolean isToString(Method method) {
            return toStringReturn;
        }

        @Override
        public boolean isCompareToMethod(Method method) {
            return compareToReturn;
        }
    }

    private static class TestableMockUtil extends MockUtil {
        private MockName mockName;

        void setMockName(MockName name) {
            this.mockName = name;
        }

        @Override
        public MockName getMockName(Object mock) {
            return mockName;
        }
    }

    private static MockName createMockName(String name, boolean isDefault) {
        return new MockName(name, isDefault);
    }

    // ---------- Partition A: Core Functional Logic ----------

    @Test(timeout = 4000)
    public void shouldReturnDescriptionForToStringWithDefaultName() throws Exception {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        // Override dependencies with custom stubs
        TestableObjectMethodsGuru guru = new TestableObjectMethodsGuru();
        guru.setToString(true);
        TestableMockUtil mockUtil = new TestableMockUtil();
        MockName name = createMockName("testMock", true);
        // Need a simple mock object for hashCode
        Object mock = new Object() {
            @Override
            public int hashCode() {
                return 12345;
            }
        };
        mockUtil.setMockName(name);
        answer.methodsGuru = guru;
        answer.mockUtil = mockUtil;

        // Simulate toString invocation
        Method toStringMethod = Object.class.getMethod("toString");
        InvocationOnMock invocation = new SimpleInvocation(toStringMethod, mock, new Object[0]);

        Object result = answer.answer(invocation);
        // Expected: "Mock for " + type.getSimpleName() + ", hashCode: " + hashCode
        // We used Object as mock type, simple name is "Object"
        assertEquals("Mock for Object, hashCode: 12345", result);
    }

    @Test(timeout = 4000)
    public void shouldReturnNameForToStringWithCustomName() throws Exception {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        TestableObjectMethodsGuru guru = new TestableObjectMethodsGuru();
        guru.setToString(true);
        TestableMockUtil mockUtil = new TestableMockUtil();
        MockName name = createMockName("customName", false);
        mockUtil.setMockName(name);
        answer.methodsGuru = guru;
        answer.mockUtil = mockUtil;

        Method toStringMethod = Object.class.getMethod("toString");
        Object mock = new Object();
        InvocationOnMock invocation = new SimpleInvocation(toStringMethod, mock, new Object[0]);

        Object result = answer.answer(invocation);
        assertEquals("customName", result);
    }

    // ---------- Partition B: Boundary Value Analysis (compareTo) ----------

    @Test(timeout = 4000)
    public void shouldReturnZeroWhenCompareToOnSameMock() throws Exception {
        // This test exposes the defect: compareTo should return 0 for same reference,
        // but current code returns 1.
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        TestableObjectMethodsGuru guru = new TestableObjectMethodsGuru();
        guru.setCompareTo(true);
        answer.methodsGuru = guru;

        Object mock = new Object();
        // Simulate invocation of compareTo with the mock itself as argument
        Method compareToMethod = Comparable.class.getMethod("compareTo", Object.class);
        InvocationOnMock invocation = new SimpleInvocation(compareToMethod, mock, new Object[]{mock});

        Object result = answer.answer(invocation);
        // Expected correct behavior: 0. Current defective implementation returns 1.
        assertEquals("compareTo on same mock should return 0", 0, result);
    }

    @Test(timeout = 4000)
    public void shouldReturnOneWhenCompareToOnDifferentMocks() throws Exception {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        TestableObjectMethodsGuru guru = new TestableObjectMethodsGuru();
        guru.setCompareTo(true);
        answer.methodsGuru = guru;

        Object mock1 = new Object();
        Object mock2 = new Object();
        Method compareToMethod = Comparable.class.getMethod("compareTo", Object.class);
        InvocationOnMock invocation = new SimpleInvocation(compareToMethod, mock1, new Object[]{mock2});

        Object result = answer.answer(invocation);
        assertEquals("compareTo on different mocks should return non-zero (1)", 1, result);
    }

    // ---------- Partition C: Defect-Targeted Branch Zone ----------
    // Already covered by compareTo tests above. Additional test for completeness.

    @Test(timeout = 4000)
    public void shouldReturnOneForCompareToEvenIfArgumentsNull() throws Exception {
        // Edge case: null argument should not affect return (still returns 1, bug)
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        TestableObjectMethodsGuru guru = new TestableObjectMethodsGuru();
        guru.setCompareTo(true);
        answer.methodsGuru = guru;

        Object mock = new Object();
        Method compareToMethod = Comparable.class.getMethod("compareTo", Object.class);
        InvocationOnMock invocation = new SimpleInvocation(compareToMethod, mock, new Object[]{null});

        Object result = answer.answer(invocation);
        assertEquals(1, result);
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------
    // No expected exceptions in the current code; we test null return for unknown types.

    @Test(timeout = 4000)
    public void shouldReturnNullForUnknownGenericType() {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        Object result = answer.returnValueFor(Queue.class); // Queue not in the list
        assertNull(result);
    }

    // ---------- Partition E: Object Lifecycle & Contract Integrity ----------
    // Serialization test: ReturnsEmptyValues implements Serializable

    @Test(timeout = 4000)
    public void shouldBeSerializable() {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        assertTrue("ReturnsEmptyValues must implement Serializable",
                answer instanceof Serializable);
    }

    @Test(timeout = 4000)
    public void shouldBeAbleToSerializeBasicInstance() throws Exception {
        // Simple serialization round-trip (no custom dependencies set)
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        // Replace ObjectMethodsGuru and MockUtil with serializable mocks? Not required; only answer is tested here.
        // Actually, returnValueFor does not use them.
        // We can test that the class is serializable by serializing a plain instance.
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(answer);
        oos.close();
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        ReturnsEmptyValues deserialized = (ReturnsEmptyValues) ois.readObject();
        assertNotNull(deserialized);
        // After deserialization, check that returnValueFor still works
        assertEquals(new LinkedList<>(), deserialized.returnValueFor(Collection.class));
    }

    // ---------- Utility: exhaustive returnValueFor tests ----------

    @Test(timeout = 4000)
    public void shouldReturnPrimitiveDefaultForPrimitiveOrWrapper() {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        assertEquals(false, answer.returnValueFor(boolean.class));
        assertEquals((byte)0, answer.returnValueFor(byte.class));
        assertEquals((short)0, answer.returnValueFor(short.class));
        assertEquals(0, answer.returnValueFor(int.class));
        assertEquals(0L, answer.returnValueFor(long.class));
        assertEquals(0.0f, answer.returnValueFor(float.class), 0.0f);
        assertEquals(0.0, answer.returnValueFor(double.class), 0.0);
        assertEquals('\0', answer.returnValueFor(char.class));
        // Wrappers
        assertEquals(false, answer.returnValueFor(Boolean.class));
        assertEquals((byte)0, answer.returnValueFor(Byte.class));
        assertEquals((short)0, answer.returnValueFor(Short.class));
        assertEquals(0, answer.returnValueFor(Integer.class));
        assertEquals(0L, answer.returnValueFor(Long.class));
        assertEquals(0.0f, answer.returnValueFor(Float.class), 0.0f);
        assertEquals(0.0, answer.returnValueFor(Double.class), 0.0);
        assertEquals('\0', answer.returnValueFor(Character.class));
    }

    @Test(timeout = 4000)
    public void shouldReturnEmptyLinkedListForCollection() {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        Object result = answer.returnValueFor(Collection.class);
        assertTrue(result instanceof LinkedList);
        assertTrue(((Collection) result).isEmpty());
    }

    @Test(timeout = 4000)
    public void shouldReturnEmptyHashSetForSet() {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        assertTrue(answer.returnValueFor(Set.class) instanceof HashSet);
        assertTrue(answer.returnValueFor(HashSet.class) instanceof HashSet);
        assertTrue(((Set) answer.returnValueFor(Set.class)).isEmpty());
    }

    @Test(timeout = 4000)
    public void shouldReturnEmptyTreeSetForSortedSet() {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        assertTrue(answer.returnValueFor(SortedSet.class) instanceof TreeSet);
        assertTrue(answer.returnValueFor(TreeSet.class) instanceof TreeSet);
        assertTrue(((SortedSet) answer.returnValueFor(SortedSet.class)).isEmpty());
    }

    @Test(timeout = 4000)
    public void shouldReturnEmptyLinkedHashSetForLinkedHashSet() {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        assertTrue(answer.returnValueFor(LinkedHashSet.class) instanceof LinkedHashSet);
        assertTrue(((LinkedHashSet) answer.returnValueFor(LinkedHashSet.class)).isEmpty());
    }

    @Test(timeout = 4000)
    public void shouldReturnEmptyLinkedListForListAndLinkedList() {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        assertTrue(answer.returnValueFor(List.class) instanceof LinkedList);
        assertTrue(answer.returnValueFor(LinkedList.class) instanceof LinkedList);
        assertTrue(((List) answer.returnValueFor(List.class)).isEmpty());
    }

    @Test(timeout = 4000)
    public void shouldReturnEmptyArrayListForArrayList() {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        assertTrue(answer.returnValueFor(ArrayList.class) instanceof ArrayList);
        assertTrue(((ArrayList) answer.returnValueFor(ArrayList.class)).isEmpty());
    }

    @Test(timeout = 4000)
    public void shouldReturnEmptyHashMapForMap() {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        assertTrue(answer.returnValueFor(Map.class) instanceof HashMap);
        assertTrue(answer.returnValueFor(HashMap.class) instanceof HashMap);
        assertTrue(((Map) answer.returnValueFor(Map.class)).isEmpty());
    }

    @Test(timeout = 4000)
    public void shouldReturnEmptyTreeMapForSortedMapAndTreeMap() {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        assertTrue(answer.returnValueFor(SortedMap.class) instanceof TreeMap);
        assertTrue(answer.returnValueFor(TreeMap.class) instanceof TreeMap);
        assertTrue(((SortedMap) answer.returnValueFor(SortedMap.class)).isEmpty());
    }

    @Test(timeout = 4000)
    public void shouldReturnEmptyLinkedHashMapForLinkedHashMap() {
        ReturnsEmptyValues answer = new ReturnsEmptyValues();
        assertTrue(answer.returnValueFor(LinkedHashMap.class) instanceof LinkedHashMap);
        assertTrue(((LinkedHashMap) answer.returnValueFor(LinkedHashMap.class)).isEmpty());
    }
}