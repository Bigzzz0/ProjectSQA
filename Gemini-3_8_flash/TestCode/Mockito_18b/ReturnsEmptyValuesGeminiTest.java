/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.mockito.internal.stubbing.defaultanswers.ReturnsEmptyValues
 * -----------------------------------------------------------------------------------------
 * Branch / Condition                                         | Targeted Test Case(s)
 * -----------------------------------------------------------------------------------------
 * isToString(method) == true                                 | testAnswerToStringWithNonMockThrowsException
 * isCompareToMethod(method) == true, same reference (==)     | testAnswerCompareToSameInstanceReturnsZero
 * isCompareToMethod(method) == true, different reference (!=)| testAnswerCompareToDifferentInstanceReturnsOne
 * Normal method dispatch via answer(InvocationOnMock)        | testAnswerReturnsDefaultCollectionForMethod, testAnswerReturnsNullForUnsupportedMethod
 * Primitives & Wrapper types (all 8 pairs + void)            | testPrimitiveAndWrapperReturnValues
 * Collection types (Collection, List, LinkedList, ArrayList) | testCollectionAndListReturnValues
 * Set types (Set, HashSet, SortedSet, TreeSet, LinkedHashSet)| testSetReturnValues
 * Map types (Map, HashMap, SortedMap, TreeMap, LinkedHashMap)| testMapReturnValues
 * Unsupported / non-collection types (null, Object, String)  | testUnsupportedTypesReturnNull
 * DEFECT TARGET: Iterable.class support                      | should_return_empty_iterable
 * Return value mutability (non-read-only collection verify)  | testReturnedCollectionsAreMutable
 * Serializable interface contract integrity                  | testSerializationIntegrity
 * -----------------------------------------------------------------------------------------
 */

package org.mockito.internal.stubbing.defaultanswers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
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

import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.invocation.InvocationOnMock;

public class ReturnsEmptyValuesGeminiTest {

    // --- Helper Interface & Invocations for White-Box Testing without Mockito.mock() ---

    private interface DummyComparable extends Comparable<DummyComparable> {
        @Override
        int compareTo(DummyComparable other);
    }

    private interface DummyService {
        List<String> getList();
        String getString();
        int getPrimitiveInt();
    }

    private static class SimpleInvocation implements InvocationOnMock {
        private final Object mock;
        private final Method method;
        private final Object[] args;

        public SimpleInvocation(Object mock, Method method, Object[] args) {
            this.mock = mock;
            this.method = method;
            this.args = args != null ? args : new Object[0];
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
            return args;
        }

        @Override
        public Object callRealMethod() throws Throwable {
            return null;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Collections
    // =========================================================================

    @Test(timeout = 4000)
    public void testCollectionAndListReturnValues() {
        ReturnsEmptyValues values = new ReturnsEmptyValues();

        Object col = values.returnValueFor(Collection.class);
        assertNotNull(col);
        assertTrue(col instanceof LinkedList);
        assertTrue(((Collection<?>) col).isEmpty());

        Object list = values.returnValueFor(List.class);
        assertNotNull(list);
        assertTrue(list instanceof LinkedList);
        assertTrue(((List<?>) list).isEmpty());

        Object linkedList = values.returnValueFor(LinkedList.class);
        assertNotNull(linkedList);
        assertTrue(linkedList instanceof LinkedList);
        assertTrue(((LinkedList<?>) linkedList).isEmpty());

        Object arrayList = values.returnValueFor(ArrayList.class);
        assertNotNull(arrayList);
        assertTrue(arrayList instanceof ArrayList);
        assertTrue(((ArrayList<?>) arrayList).isEmpty());
    }

    @Test(timeout = 4000)
    public void testSetReturnValues() {
        ReturnsEmptyValues values = new ReturnsEmptyValues();

        Object set = values.returnValueFor(Set.class);
        assertNotNull(set);
        assertTrue(set instanceof HashSet);
        assertTrue(((Set<?>) set).isEmpty());

        Object hashSet = values.returnValueFor(HashSet.class);
        assertNotNull(hashSet);
        assertTrue(hashSet instanceof HashSet);
        assertTrue(((HashSet<?>) hashSet).isEmpty());

        Object sortedSet = values.returnValueFor(SortedSet.class);
        assertNotNull(sortedSet);
        assertTrue(sortedSet instanceof TreeSet);
        assertTrue(((SortedSet<?>) sortedSet).isEmpty());

        Object treeSet = values.returnValueFor(TreeSet.class);
        assertNotNull(treeSet);
        assertTrue(treeSet instanceof TreeSet);
        assertTrue(((TreeSet<?>) treeSet).isEmpty());

        Object linkedHashSet = values.returnValueFor(LinkedHashSet.class);
        assertNotNull(linkedHashSet);
        assertTrue(linkedHashSet instanceof LinkedHashSet);
        assertTrue(((LinkedHashSet<?>) linkedHashSet).isEmpty());
    }

    @Test(timeout = 4000)
    public void testMapReturnValues() {
        ReturnsEmptyValues values = new ReturnsEmptyValues();

        Object map = values.returnValueFor(Map.class);
        assertNotNull(map);
        assertTrue(map instanceof HashMap);
        assertTrue(((Map<?, ?>) map).isEmpty());

        Object hashMap = values.returnValueFor(HashMap.class);
        assertNotNull(hashMap);
        assertTrue(hashMap instanceof HashMap);
        assertTrue(((HashMap<?, ?>) hashMap).isEmpty());

        Object sortedMap = values.returnValueFor(SortedMap.class);
        assertNotNull(sortedMap);
        assertTrue(sortedMap instanceof TreeMap);
        assertTrue(((SortedMap<?, ?>) sortedMap).isEmpty());

        Object treeMap = values.returnValueFor(TreeMap.class);
        assertNotNull(treeMap);
        assertTrue(treeMap instanceof TreeMap);
        assertTrue(((TreeMap<?, ?>) treeMap).isEmpty());

        Object linkedHashMap = values.returnValueFor(LinkedHashMap.class);
        assertNotNull(linkedHashMap);
        assertTrue(linkedHashMap instanceof LinkedHashMap);
        assertTrue(((LinkedHashMap<?, ?>) linkedHashMap).isEmpty());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testReturnedCollectionsAreMutable() {
        ReturnsEmptyValues values = new ReturnsEmptyValues();

        List<Object> list = (List<Object>) values.returnValueFor(List.class);
        list.add("item");
        assertEquals(1, list.size());

        Set<Object> set = (Set<Object>) values.returnValueFor(Set.class);
        set.add("item");
        assertEquals(1, set.size());

        Map<Object, Object> map = (Map<Object, Object>) values.returnValueFor(Map.class);
        map.put("key", "val");
        assertEquals(1, map.size());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Primitives
    // =========================================================================

    @Test(timeout = 4000)
    public void testPrimitiveAndWrapperReturnValues() {
        ReturnsEmptyValues values = new ReturnsEmptyValues();

        assertEquals(false, values.returnValueFor(boolean.class));
        assertEquals(Boolean.FALSE, values.returnValueFor(Boolean.class));

        assertEquals((char) 0, values.returnValueFor(char.class));
        assertEquals(Character.valueOf((char) 0), values.returnValueFor(Character.class));

        assertEquals((byte) 0, values.returnValueFor(byte.class));
        assertEquals(Byte.valueOf((byte) 0), values.returnValueFor(Byte.class));

        assertEquals((short) 0, values.returnValueFor(short.class));
        assertEquals(Short.valueOf((short) 0), values.returnValueFor(Short.class));

        assertEquals(0, values.returnValueFor(int.class));
        assertEquals(Integer.valueOf(0), values.returnValueFor(Integer.class));

        assertEquals(0L, values.returnValueFor(long.class));
        assertEquals(Long.valueOf(0L), values.returnValueFor(Long.class));

        assertEquals(0.0F, values.returnValueFor(float.class));
        assertEquals(Float.valueOf(0.0F), values.returnValueFor(Float.class));

        assertEquals(0.0D, values.returnValueFor(double.class));
        assertEquals(Double.valueOf(0.0D), values.returnValueFor(Double.class));
    }

    @Test(timeout = 4000)
    public void testUnsupportedTypesReturnNull() {
        ReturnsEmptyValues values = new ReturnsEmptyValues();

        assertNull(values.returnValueFor(null));
        assertNull(values.returnValueFor(String.class));
        assertNull(values.returnValueFor(Object.class));
        assertNull(values.returnValueFor(DummyService.class));
        assertNull(values.returnValueFor(void.class));
        assertNull(values.returnValueFor(Void.class));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Defects4J Ground Truth Target:
     * ReturnsEmptyValuesTest::should_return_empty_iterable -> NullPointerException
     * Bug: ReturnsEmptyValues does not support Iterable.class and returns null,
     * causing NullPointerException when iterating over the returned value.
     */
    @Test(timeout = 4000)
    public void should_return_empty_iterable() {
        ReturnsEmptyValues values = new ReturnsEmptyValues();
        Iterable<?> iterable = (Iterable<?>) values.returnValueFor(Iterable.class);
        assertNotNull("returnValueFor(Iterable.class) should not return null", iterable);
        assertFalse("Iterable should be empty", iterable.iterator().hasNext());
    }

    // =========================================================================
    // Partition D: Invocation Dispatch & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testAnswerCompareToSameInstanceReturnsZero() throws Exception {
        ReturnsEmptyValues values = new ReturnsEmptyValues();
        Method compareToMethod = DummyComparable.class.getMethod("compareTo", DummyComparable.class);
        Object dummyMock = new Object();

        InvocationOnMock invocation = new SimpleInvocation(dummyMock, compareToMethod, new Object[]{dummyMock});
        Object result = values.answer(invocation);

        assertEquals(0, result);
    }

    @Test(timeout = 4000)
    public void testAnswerCompareToDifferentInstanceReturnsOne() throws Exception {
        ReturnsEmptyValues values = new ReturnsEmptyValues();
        Method compareToMethod = DummyComparable.class.getMethod("compareTo", DummyComparable.class);
        Object dummyMock = new Object();
        Object otherInstance = new Object();

        InvocationOnMock invocation = new SimpleInvocation(dummyMock, compareToMethod, new Object[]{otherInstance});
        Object result = values.answer(invocation);

        assertEquals(1, result);
    }

    @Test(timeout = 4000)
    public void testAnswerReturnsDefaultCollectionForMethod() throws Exception {
        ReturnsEmptyValues values = new ReturnsEmptyValues();
        Method getListMethod = DummyService.class.getMethod("getList");
        InvocationOnMock invocation = new SimpleInvocation(new Object(), getListMethod, new Object[0]);

        Object result = values.answer(invocation);
        assertNotNull(result);
        assertTrue(result instanceof LinkedList);
        assertTrue(((List<?>) result).isEmpty());
    }

    @Test(timeout = 4000)
    public void testAnswerReturnsNullForUnsupportedMethod() throws Exception {
        ReturnsEmptyValues values = new ReturnsEmptyValues();
        Method getStringMethod = DummyService.class.getMethod("getString");
        InvocationOnMock invocation = new SimpleInvocation(new Object(), getStringMethod, new Object[0]);

        Object result = values.answer(invocation);
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testAnswerReturnsPrimitiveForMethod() throws Exception {
        ReturnsEmptyValues values = new ReturnsEmptyValues();
        Method getIntMethod = DummyService.class.getMethod("getPrimitiveInt");
        InvocationOnMock invocation = new SimpleInvocation(new Object(), getIntMethod, new Object[0]);

        Object result = values.answer(invocation);
        assertEquals(0, result);
    }

    @Test(timeout = 4000)
    public void testAnswerToStringWithNonMockThrowsException() throws Exception {
        ReturnsEmptyValues values = new ReturnsEmptyValues();
        Method toStringMethod = Object.class.getMethod("toString");
        InvocationOnMock invocation = new SimpleInvocation(new Object(), toStringMethod, new Object[0]);

        try {
            values.answer(invocation);
            fail("Expected exception when invoking toString on a non-mock");
        } catch (RuntimeException expected) {
            // Expected NotAMockException or RuntimeException from MockUtil
            assertNotNull(expected);
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        ReturnsEmptyValues values = new ReturnsEmptyValues();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(values);
        oos.flush();
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertTrue(deserialized instanceof ReturnsEmptyValues);
        ReturnsEmptyValues restored = (ReturnsEmptyValues) deserialized;
        assertEquals(0, restored.returnValueFor(int.class));
        assertTrue(((List<?>) restored.returnValueFor(List.class)).isEmpty());
    }
}