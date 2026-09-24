/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValues
 *
 * Decision / Branch Points Targeted:
 * 1. answer(InvocationOnMock):
 *    - methodsGuru.isToString(method) == true:
 *      - Sub-branch: name.isDefault() == true -> returns formatted default mock representation.
 *      - Sub-branch: name.isDefault() == false -> returns name.toString().
 *    - methodsGuru.isCompareToMethod(method) == true:
 *      - Sub-branch: mock == argument[0] (comparing mock to itself) -> Expected: 0.
 *        [KNOWN DEFECT ZONE: Issue 184 / Defects4J Bug: Defective version unconditionally returns 1,
 *         failing with expected:<0> but was:<1>].
 *      - Sub-branch: mock != argument[0] (comparing mock to different instance) -> Expected: 1 (non-zero).
 *    - Non-toString & Non-compareTo method -> falls through to invocation.getMethod().getReturnType() and returnValueFor(type).
 *
 * 2. returnValueFor(Class<?> type):
 *    - Primitives.isPrimitiveOrWrapper(type) == true:
 *      - Covers all primitive types: boolean, byte, short, char, int, long, float, double.
 *      - Covers all primitive wrappers: Boolean, Byte, Short, Character, Integer, Long, Float, Double.
 *    - Collections & Maps:
 *      - Collection.class -> returns empty LinkedList.
 *      - Set.class -> returns empty HashSet.
 *      - HashSet.class -> returns empty HashSet.
 *      - SortedSet.class -> returns empty TreeSet.
 *      - TreeSet.class -> returns empty TreeSet.
 *      - LinkedHashSet.class -> returns empty LinkedHashSet.
 *      - List.class -> returns empty LinkedList.
 *      - LinkedList.class -> returns empty LinkedList.
 *      - ArrayList.class -> returns empty ArrayList.
 *      - Map.class -> returns empty HashMap.
 *      - HashMap.class -> returns empty HashMap.
 *      - SortedMap.class -> returns empty TreeMap.
 *      - TreeMap.class -> returns empty TreeMap.
 *      - LinkedHashMap.class -> returns empty LinkedHashMap.
 *    - Fall-through:
 *      - Other classes (String, Object, array types) -> returns null.
 *      - Boundary: null input -> returns null.
 *      - Void / void -> returns null.
 *
 * 3. Contract & Lifecycle:
 *    - Serialization round-trip verification (serialVersionUID integrity).
 *    - Returned collections/maps must be mutable (non-empty list modification guard).
 */

package org.mockito.internal.stubbing.defaultanswers;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.internal.util.MockUtil;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.mock.MockCreationSettings;
import org.mockito.mock.MockName;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class ReturnsEmptyValuesGeminiTest {

    // Helper interface for testing invocations across arbitrary return types
    private interface SampleMethods {
        List<String> getList();
        String getString();
        int getPrimitiveInt();
        boolean getPrimitiveBoolean();
        Map<String, Object> getMap();
        void doVoid();
    }

    // Helper class for Comparable testing
    public static class ComparableItem implements Comparable<ComparableItem> {
        private final String id;

        public ComparableItem(String id) {
            this.id = id;
        }

        @Override
        public int compareTo(ComparableItem other) {
            return this.id.compareTo(other.id);
        }
    }

    // Helper class that mimics compareTo signature but does NOT implement Comparable
    public static class NonComparableItem {
        public int compareTo(NonComparableItem other) {
            return 0;
        }
    }

    /**
     * Constructs a deterministic, dynamic-proxy-backed InvocationOnMock
     * without relying on third-party mocking frameworks.
     */
    private InvocationOnMock createInvocation(final Object mock, final Method method, final Object[] args) {
        return (InvocationOnMock) Proxy.newProxyInstance(
                InvocationOnMock.class.getClassLoader(),
                new Class<?>[]{InvocationOnMock.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method m, Object[] mArgs) throws Throwable {
                        String name = m.getName();
                        if ("getMock".equals(name)) {
                            return mock;
                        } else if ("getMethod".equals(name)) {
                            return method;
                        } else if ("getArguments".equals(name)) {
                            return args != null ? args : new Object[0];
                        }
                        return null;
                    }
                }
        );
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testReturnValueFor_PrimitiveTypes() {
        ReturnsEmptyValues emptyValues = new ReturnsEmptyValues();

        assertEquals(false, emptyValues.returnValueFor(boolean.class));
        assertEquals((byte) 0, emptyValues.returnValueFor(byte.class));
        assertEquals((short) 0, emptyValues.returnValueFor(short.class));
        assertEquals('\u0000', emptyValues.returnValueFor(char.class));
        assertEquals(0, emptyValues.returnValueFor(int.class));
        assertEquals(0L, emptyValues.returnValueFor(long.class));
        assertEquals(0.0F, emptyValues.returnValueFor(float.class));
        assertEquals(0.0D, emptyValues.returnValueFor(double.class));
    }

    @Test(timeout = 4000)
    public void testReturnValueFor_PrimitiveWrapperTypes() {
        ReturnsEmptyValues emptyValues = new ReturnsEmptyValues();

        assertEquals(Boolean.FALSE, emptyValues.returnValueFor(Boolean.class));
        assertEquals(Byte.valueOf((byte) 0), emptyValues.returnValueFor(Byte.class));
        assertEquals(Short.valueOf((short) 0), emptyValues.returnValueFor(Short.class));
        assertEquals(Character.valueOf('\u0000'), emptyValues.returnValueFor(Character.class));
        assertEquals(Integer.valueOf(0), emptyValues.returnValueFor(Integer.class));
        assertEquals(Long.valueOf(0L), emptyValues.returnValueFor(Long.class));
        assertEquals(Float.valueOf(0.0F), emptyValues.returnValueFor(Float.class));
        assertEquals(Double.valueOf(0.0D), emptyValues.returnValueFor(Double.class));
    }

    @Test(timeout = 4000)
    public void testReturnValueFor_CollectionInterfaces() {
        ReturnsEmptyValues emptyValues = new ReturnsEmptyValues();

        Object collectionResult = emptyValues.returnValueFor(Collection.class);
        assertNotNull(collectionResult);
        assertTrue("Collection.class must return LinkedList", collectionResult instanceof LinkedList);
        assertTrue(((Collection<?>) collectionResult).isEmpty());

        Object listResult = emptyValues.returnValueFor(List.class);
        assertNotNull(listResult);
        assertTrue("List.class must return LinkedList", listResult instanceof LinkedList);
        assertTrue(((List<?>) listResult).isEmpty());

        Object setResult = emptyValues.returnValueFor(Set.class);
        assertNotNull(setResult);
        assertTrue("Set.class must return HashSet", setResult instanceof HashSet);
        assertTrue(((Set<?>) setResult).isEmpty());

        Object sortedSetResult = emptyValues.returnValueFor(SortedSet.class);
        assertNotNull(sortedSetResult);
        assertTrue("SortedSet.class must return TreeSet", sortedSetResult instanceof TreeSet);
        assertTrue(((SortedSet<?>) sortedSetResult).isEmpty());
    }

    @Test(timeout = 4000)
    public void testReturnValueFor_CollectionImplementations() {
        ReturnsEmptyValues emptyValues = new ReturnsEmptyValues();

        Object linkedListResult = emptyValues.returnValueFor(LinkedList.class);
        assertTrue(linkedListResult instanceof LinkedList);
        assertTrue(((LinkedList<?>) linkedListResult).isEmpty());

        Object arrayListResult = emptyValues.returnValueFor(ArrayList.class);
        assertTrue(arrayListResult instanceof ArrayList);
        assertTrue(((ArrayList<?>) arrayListResult).isEmpty());

        Object hashSetResult = emptyValues.returnValueFor(HashSet.class);
        assertTrue(hashSetResult instanceof HashSet);
        assertTrue(((HashSet<?>) hashSetResult).isEmpty());

        Object treeSetResult = emptyValues.returnValueFor(TreeSet.class);
        assertTrue(treeSetResult instanceof TreeSet);
        assertTrue(((TreeSet<?>) treeSetResult).isEmpty());

        Object linkedHashSetResult = emptyValues.returnValueFor(LinkedHashSet.class);
        assertTrue(linkedHashSetResult instanceof LinkedHashSet);
        assertTrue(((LinkedHashSet<?>) linkedHashSetResult).isEmpty());
    }

    @Test(timeout = 4000)
    public void testReturnValueFor_MapTypes() {
        ReturnsEmptyValues emptyValues = new ReturnsEmptyValues();

        Object mapResult = emptyValues.returnValueFor(Map.class);
        assertTrue(mapResult instanceof HashMap);
        assertTrue(((Map<?, ?>) mapResult).isEmpty());

        Object hashMapResult = emptyValues.returnValueFor(HashMap.class);
        assertTrue(hashMapResult instanceof HashMap);
        assertTrue(((HashMap<?, ?>) hashMapResult).isEmpty());

        Object sortedMapResult = emptyValues.returnValueFor(SortedMap.class);
        assertTrue(sortedMapResult instanceof TreeMap);
        assertTrue(((SortedMap<?, ?>) sortedMapResult).isEmpty());

        Object treeMapResult = emptyValues.returnValueFor(TreeMap.class);
        assertTrue(treeMapResult instanceof TreeMap);
        assertTrue(((TreeMap<?, ?>) treeMapResult).isEmpty());

        Object linkedHashMapResult = emptyValues.returnValueFor(LinkedHashMap.class);
        assertTrue(linkedHashMapResult instanceof LinkedHashMap);
        assertTrue(((LinkedHashMap<?, ?>) linkedHashMapResult).isEmpty());
    }

    @Test(timeout = 4000)
    public void testAnswer_NormalMethodInvocations() throws Exception {
        ReturnsEmptyValues emptyValues = new ReturnsEmptyValues();
        Object mock = new Object();

        Method getListMethod = SampleMethods.class.getMethod("getList");
        Object listResult = emptyValues.answer(createInvocation(mock, getListMethod, null));
        assertTrue(listResult instanceof LinkedList);
        assertTrue(((List<?>) listResult).isEmpty());

        Method getStringMethod = SampleMethods.class.getMethod("getString");
        Object stringResult = emptyValues.answer(createInvocation(mock, getStringMethod, null));
        assertNull("Non-collection, non-primitive reference type must return null", stringResult);

        Method getIntMethod = SampleMethods.class.getMethod("getPrimitiveInt");
        Object intResult = emptyValues.answer(createInvocation(mock, getIntMethod, null));
        assertEquals(0, intResult);

        Method getBoolMethod = SampleMethods.class.getMethod("getPrimitiveBoolean");
        Object boolResult = emptyValues.answer(createInvocation(mock, getBoolMethod, null));
        assertEquals(false, boolResult);

        Method getMapMethod = SampleMethods.class.getMethod("getMap");
        Object mapResult = emptyValues.answer(createInvocation(mock, getMapMethod, null));
        assertTrue(mapResult instanceof HashMap);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testReturnValueFor_NullAndVoidAndUnknownTypes() {
        ReturnsEmptyValues emptyValues = new ReturnsEmptyValues();

        assertNull("null type must return null", emptyValues.returnValueFor(null));
        assertNull("void.class must return null", emptyValues.returnValueFor(void.class));
        assertNull("Void.class must return null", emptyValues.returnValueFor(Void.class));
        assertNull("String.class must return null", emptyValues.returnValueFor(String.class));
        assertNull("Object.class must return null", emptyValues.returnValueFor(Object.class));
        assertNull("Array type must return null", emptyValues.returnValueFor(int[].class));
        assertNull("Object Array type must return null", emptyValues.returnValueFor(Object[].class));
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testReturnValueFor_CollectionsAreMutable() {
        ReturnsEmptyValues emptyValues = new ReturnsEmptyValues();

        // Verifies the contract: returned collections are new mutable instances,
        // avoiding UnsupportedOperationException when modified by the caller.
        Collection<Object> collection = (Collection<Object>) emptyValues.returnValueFor(Collection.class);
        collection.add("element");
        assertEquals(1, collection.size());

        List<Object> list = (List<Object>) emptyValues.returnValueFor(List.class);
        list.add("listElement");
        assertEquals(1, list.size());

        Set<Object> set = (Set<Object>) emptyValues.returnValueFor(Set.class);
        set.add("setElement");
        assertEquals(1, set.size());

        Map<Object, Object> map = (Map<Object, Object>) emptyValues.returnValueFor(Map.class);
        map.put("key", "value");
        assertEquals(1, map.size());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGET DEFECT TEST: Issue 184 / Defects4J Mockito Bug
     * Under the defect specification:
     * When a mock is compared to itself via Comparable#compareTo(T other),
     * it MUST return 0 because the references are identical.
     * The defective implementation returns 1 unconditionally, triggering:
     * junit.framework.AssertionFailedError: expected:<0> but was:<1>
     */
    @Test(timeout = 4000)
    public void testCompareTo_MockComparedToItself_ReturnsZero() throws Exception {
        ReturnsEmptyValues emptyValues = new ReturnsEmptyValues();
        ComparableItem mock = new ComparableItem("itemA");
        Method compareToMethod = ComparableItem.class.getMethod("compareTo", ComparableItem.class);

        InvocationOnMock invocation = createInvocation(mock, compareToMethod, new Object[]{mock});
        Object result = emptyValues.answer(invocation);

        assertNotNull("Result of compareTo should not be null", result);
        assertEquals("Mock compared to itself must return 0", 0, result);
    }

    @Test(timeout = 4000)
    public void testCompareTo_MockComparedToDifferentInstance_ReturnsNonZero() throws Exception {
        ReturnsEmptyValues emptyValues = new ReturnsEmptyValues();
        ComparableItem mock1 = new ComparableItem("item1");
        ComparableItem mock2 = new ComparableItem("item2");
        Method compareToMethod = ComparableItem.class.getMethod("compareTo", ComparableItem.class);

        InvocationOnMock invocation = createInvocation(mock1, compareToMethod, new Object[]{mock2});
        Object result = emptyValues.answer(invocation);

        assertNotNull("Result of compareTo should not be null", result);
        assertEquals("Mock compared to a different instance must return 1", 1, result);
    }

    // =========================================================================
    // Partition D: ToString Branch & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testAnswer_ToString_CustomMockName() throws Exception {
        ReturnsEmptyValues emptyValues = new ReturnsEmptyValues();

        // Inject MockUtil returning a custom (non-default) MockName
        emptyValues.mockUtil = new MockUtil() {
            @Override
            public MockName getMockName(Object mock) {
                return (MockName) Proxy.newProxyInstance(
                        MockName.class.getClassLoader(),
                        new Class<?>[]{MockName.class},
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) {
                                if ("isDefault".equals(method.getName())) {
                                    return false;
                                }
                                if ("toString".equals(method.getName())) {
                                    return "myNamedMock";
                                }
                                return null;
                            }
                        }
                );
            }
        };

        Method toStringMethod = Object.class.getMethod("toString");
        InvocationOnMock invocation = createInvocation("anyMock", toStringMethod, null);
        Object result = emptyValues.answer(invocation);

        assertEquals("myNamedMock", result);
    }

    @Test(timeout = 4000)
    @SuppressWarnings("rawtypes")
    public void testAnswer_ToString_DefaultMockName() throws Exception {
        ReturnsEmptyValues emptyValues = new ReturnsEmptyValues();
        final Object dummyMock = new Object();

        final Object mockSettingsProxy = Proxy.newProxyInstance(
                MockCreationSettings.class.getClassLoader(),
                new Class<?>[]{MockCreationSettings.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("getTypeToMock".equals(method.getName())) {
                            return CharSequence.class;
                        }
                        return null;
                    }
                }
        );

        emptyValues.mockUtil = new MockUtil() {
            @Override
            public MockName getMockName(Object mock) {
                return (MockName) Proxy.newProxyInstance(
                        MockName.class.getClassLoader(),
                        new Class<?>[]{MockName.class},
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) {
                                if ("isDefault".equals(method.getName())) {
                                    return true;
                                }
                                return null;
                            }
                        }
                );
            }

            @Override
            public MockCreationSettings getMockSettings(Object mock) {
                return (MockCreationSettings) mockSettingsProxy;
            }
        };

        Method toStringMethod = Object.class.getMethod("toString");
        InvocationOnMock invocation = createInvocation(dummyMock, toStringMethod, null);
        Object result = emptyValues.answer(invocation);

        String expected = "Mock for CharSequence, hashCode: " + dummyMock.hashCode();
        assertEquals(expected, result);
    }

    @Test(timeout = 4000)
    public void testAnswer_CompareToOnNonComparableClass_FallsThroughToPrimitiveDefault() throws Exception {
        ReturnsEmptyValues emptyValues = new ReturnsEmptyValues();
        NonComparableItem mock = new NonComparableItem();
        NonComparableItem arg = new NonComparableItem();
        Method compareToMethod = NonComparableItem.class.getMethod("compareTo", NonComparableItem.class);

        InvocationOnMock invocation = createInvocation(mock, compareToMethod, new Object[]{arg});
        Object result = emptyValues.answer(invocation);

        // Since declaring class does not implement Comparable, it is not a Comparable#compareTo method.
        // It falls through to returnValueFor(int.class), returning 0.
        assertEquals(0, result);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        ReturnsEmptyValues original = new ReturnsEmptyValues();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
            oos.flush();
        }

        byte[] serializedData = baos.toByteArray();
        assertTrue("Serialized stream must not be empty", serializedData.length > 0);

        ReturnsEmptyValues deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serializedData))) {
            deserialized = (ReturnsEmptyValues) ois.readObject();
        }

        assertNotNull("Deserialized instance must not be null", deserialized);
        // Ensure inner helper structures and answer capabilities remain functional
        assertEquals(0, deserialized.returnValueFor(int.class));
        assertTrue(deserialized.returnValueFor(List.class) instanceof LinkedList);
    }
}