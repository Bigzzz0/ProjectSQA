package org.apache.commons.collections4;

import java.util.*;
import org.apache.commons.collections4.functors.TruePredicate;
import org.apache.commons.collections4.functors.EqualPredicate;
import org.apache.commons.collections4.iterators.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.collections4.IteratorUtils
 *
 * Partition A: Empty, Singleton & Array Iterators
 *  - emptyIterator, emptyListIterator, emptyOrderedIterator, emptyMapIterator, emptyOrderedMapIterator
 *  - singletonIterator, singletonListIterator (boundary: null element vs non-null element)
 *  - arrayIterator (varargs, object, with start, with start & end)
 *  - arrayListIterator (varargs, object, with start, with start & end)
 *  - Boundary conditions: primitive arrays (int[], double[]), 0-length arrays, index bounds
 *
 * Partition B: Composition & Transformation Iterators
 *  - boundedIterator (offset, max, negative boundaries)
 *  - unmodifiableIterator, unmodifiableListIterator, unmodifiableMapIterator
 *  - chainedIterator (2 iters, varargs, collection)
 *  - objectGraphIterator (traversing graph with Transformer)
 *  - transformedIterator (Transformer null checks, functional path)
 *  - filteredIterator & filteredListIterator (Predicate null checks, filter logic)
 *  - loopingIterator & loopingListIterator (Collection/List null checks, cycle behavior)
 *  - nodeListIterator (NodeList, Node null checks and traversal via dynamic proxies)
 *  - peekingIterator, pushbackIterator, skippingIterator, zippingIterator
 *
 * Partition C: Defect-Targeted Branch Zone (Collections-25 / COLLECTIONS-566)
 *  - collatedIterator(null, it1, it2): CollatingIterator with null comparator must sort by natural order.
 *    Defect: Fails with NPE "You must invoke setComparator() to set a comparator first" upon traversal.
 *  - collatedIterator(null, array) and collatedIterator(null, collection) variants.
 *
 * Partition D: Collection Views & Conversion Utilities
 *  - asIterator (Enumeration, Enumeration + removeCollection)
 *  - asEnumeration, asIterable, asMultipleUseIterable, toListIterator
 *  - toArray(iterator), toArray(iterator, Class)
 *  - toList(iterator), toList(iterator, estimatedSize)
 *  - getIterator(Object): Exhaustive branching for null, Iterator, Iterable, Object[], Enumeration,
 *    Map, NodeList, Node, Dictionary, primitive array, reflection iterator(), fallback singleton.
 *
 * Partition E: Functional Helpers, String Representation & Exception Paths
 *  - apply, find, matchesAny, matchesAll (null iterators, non-null iterators, early returns)
 *  - isEmpty, contains, size, get (with valid index, out-of-bounds, negative)
 *  - toString (1-arg, 2-arg, 5-arg with prefixes, delimiters, suffixes, null iterators, empty iterators)
 * -------------------------------------------------------------------------------------------------------------
 */
public class IteratorUtilsGeminiTest {

    // =========================================================================
    // Partition A: Empty, Singleton & Array Iterators
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyIterators() {
        assertNotNull(IteratorUtils.EMPTY_ITERATOR);
        assertNotNull(IteratorUtils.EMPTY_LIST_ITERATOR);
        assertNotNull(IteratorUtils.EMPTY_ORDERED_ITERATOR);
        assertNotNull(IteratorUtils.EMPTY_MAP_ITERATOR);
        assertNotNull(IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR);

        ResettableIterator<Object> emptyIt = IteratorUtils.emptyIterator();
        assertFalse(emptyIt.hasNext());

        ResettableListIterator<Object> emptyListIt = IteratorUtils.emptyListIterator();
        assertFalse(emptyListIt.hasNext());
        assertFalse(emptyListIt.hasPrevious());

        OrderedIterator<Object> emptyOrdIt = IteratorUtils.emptyOrderedIterator();
        assertFalse(emptyOrdIt.hasNext());
        assertFalse(emptyOrdIt.hasPrevious());

        MapIterator<Object, Object> emptyMapIt = IteratorUtils.emptyMapIterator();
        assertFalse(emptyMapIt.hasNext());

        OrderedMapIterator<Object, Object> emptyOrdMapIt = IteratorUtils.emptyOrderedMapIterator();
        assertFalse(emptyOrdMapIt.hasNext());
        assertFalse(emptyOrdMapIt.hasPrevious());
    }

    @Test(timeout = 4000)
    public void testSingletonIterators() {
        ResettableIterator<String> sIt = IteratorUtils.singletonIterator("test");
        assertTrue(sIt.hasNext());
        assertEquals("test", sIt.next());
        assertFalse(sIt.hasNext());
        sIt.reset();
        assertTrue(sIt.hasNext());
        assertEquals("test", sIt.next());

        ListIterator<Integer> slIt = IteratorUtils.singletonListIterator(42);
        assertTrue(slIt.hasNext());
        assertEquals(Integer.valueOf(42), slIt.next());
        assertFalse(slIt.hasNext());
        assertTrue(slIt.hasPrevious());
        assertEquals(Integer.valueOf(42), slIt.previous());
    }

    @Test(timeout = 4000)
    public void testArrayIteratorObject() {
        String[] arr = new String[] {"A", "B", "C"};
        ResettableIterator<String> it = IteratorUtils.arrayIterator(arr);
        assertTrue(it.hasNext());
        assertEquals("A", it.next());
        assertEquals("B", it.next());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());

        it = IteratorUtils.arrayIterator(arr, 1);
        assertEquals("B", it.next());
        assertEquals("C", it.next());
        assertFalse(it.hasNext());

        it = IteratorUtils.arrayIterator(arr, 1, 2);
        assertEquals("B", it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testArrayIteratorPrimitive() {
        int[] primitiveArr = new int[] {10, 20, 30};
        ResettableIterator<Integer> it = IteratorUtils.arrayIterator((Object) primitiveArr);
        assertEquals(Integer.valueOf(10), it.next());
        assertEquals(Integer.valueOf(20), it.next());
        assertEquals(Integer.valueOf(30), it.next());
        assertFalse(it.hasNext());

        it = IteratorUtils.arrayIterator((Object) primitiveArr, 1);
        assertEquals(Integer.valueOf(20), it.next());
        assertEquals(Integer.valueOf(30), it.next());

        it = IteratorUtils.arrayIterator((Object) primitiveArr, 0, 1);
        assertEquals(Integer.valueOf(10), it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testArrayListIteratorObject() {
        String[] arr = new String[] {"X", "Y", "Z"};
        ResettableListIterator<String> it = IteratorUtils.arrayListIterator(arr);
        assertTrue(it.hasNext());
        assertEquals("X", it.next());
        assertEquals("Y", it.next());
        assertEquals("Z", it.next());
        assertFalse(it.hasNext());

        it = IteratorUtils.arrayListIterator(arr, 1);
        assertEquals("Y", it.next());

        it = IteratorUtils.arrayListIterator(arr, 1, 2);
        assertEquals("Y", it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testArrayListIteratorPrimitive() {
        long[] primArr = new long[] {100L, 200L, 300L};
        ResettableListIterator<Long> it = IteratorUtils.arrayListIterator((Object) primArr);
        assertEquals(Long.valueOf(100L), it.next());
        assertEquals(Long.valueOf(200L), it.next());
        assertEquals(Long.valueOf(300L), it.next());

        it = IteratorUtils.arrayListIterator((Object) primArr, 1);
        assertEquals(Long.valueOf(200L), it.next());

        it = IteratorUtils.arrayListIterator((Object) primArr, 1, 2);
        assertEquals(Long.valueOf(200L), it.next());
        assertFalse(it.hasNext());
    }

    // =========================================================================
    // Partition B: Composition & Transformation Iterators
    // =========================================================================

    @Test(timeout = 4000)
    public void testBoundedIterator() {
        List<String> list = Arrays.asList("a", "b", "c", "d", "e");
        BoundedIterator<String> bounded = IteratorUtils.boundedIterator(list.iterator(), 2);
        assertEquals(Arrays.asList("a", "b"), IteratorUtils.toList(bounded));

        bounded = IteratorUtils.boundedIterator(list.iterator(), 1, 2);
        assertEquals(Arrays.asList("b", "c"), IteratorUtils.toList(bounded));
    }

    @Test(timeout = 4000)
    public void testUnmodifiableWrappers() {
        List<String> list = new ArrayList<String>(Arrays.asList("one", "two"));
        Iterator<String> unmodifiable = IteratorUtils.unmodifiableIterator(list.iterator());
        assertEquals("one", unmodifiable.next());
        try {
            unmodifiable.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}

        ListIterator<String> unmodifiableList = IteratorUtils.unmodifiableListIterator(list.listIterator());
        assertEquals("one", unmodifiableList.next());
        try {
            unmodifiableList.remove();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}

        MapIterator<Object, Object> emptyMapIt = IteratorUtils.emptyMapIterator();
        MapIterator<Object, Object> unmodifiableMap = IteratorUtils.unmodifiableMapIterator(emptyMapIt);
        assertFalse(unmodifiableMap.hasNext());
    }

    @Test(timeout = 4000)
    public void testChainedIteratorVariants() {
        Iterator<String> it1 = Arrays.asList("1", "2").iterator();
        Iterator<String> it2 = Arrays.asList("3", "4").iterator();
        Iterator<String> chainedPair = IteratorUtils.chainedIterator(it1, it2);
        assertEquals(Arrays.asList("1", "2", "3", "4"), IteratorUtils.toList(chainedPair));

        @SuppressWarnings("unchecked")
        Iterator<String> chainedVarargs = IteratorUtils.chainedIterator(
                Arrays.asList("a").iterator(),
                Arrays.asList("b").iterator()
        );
        assertEquals(Arrays.asList("a", "b"), IteratorUtils.toList(chainedVarargs));

        List<Iterator<? extends String>> itList = new ArrayList<Iterator<? extends String>>();
        itList.add(Arrays.asList("alpha").iterator());
        itList.add(Arrays.asList("beta").iterator());
        Iterator<String> chainedColl = IteratorUtils.chainedIterator(itList);
        assertEquals(Arrays.asList("alpha", "beta"), IteratorUtils.toList(chainedColl));
    }

    @Test(timeout = 4000)
    public void testObjectGraphIterator() {
        List<String> branch = Arrays.asList("leaf1", "leaf2");
        Transformer<Object, Object> transformer = new Transformer<Object, Object>() {
            public Object transform(Object input) {
                if (input instanceof List) {
                    return ((List<?>) input).iterator();
                }
                return input;
            }
        };

        Iterator<Object> ogi = IteratorUtils.objectGraphIterator(branch, transformer);
        assertTrue(ogi.hasNext());
        assertEquals("leaf1", ogi.next());
        assertEquals("leaf2", ogi.next());
        assertFalse(ogi.hasNext());

        Iterator<Object> emptyOgi = IteratorUtils.objectGraphIterator(null, null);
        assertFalse(emptyOgi.hasNext());
    }

    @Test(timeout = 4000)
    public void testTransformedIterator() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        Transformer<Integer, String> stringTransformer = new Transformer<Integer, String>() {
            public String transform(Integer input) {
                return "Val:" + input;
            }
        };
        Iterator<String> transformed = IteratorUtils.transformedIterator(list.iterator(), stringTransformer);
        assertEquals(Arrays.asList("Val:1", "Val:2", "Val:3"), IteratorUtils.toList(transformed));
    }

    @Test(timeout = 4000)
    public void testFilteredIterators() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        Predicate<Integer> evenPredicate = new Predicate<Integer>() {
            public boolean evaluate(Integer object) {
                return object % 2 == 0;
            }
        };
        Iterator<Integer> filtered = IteratorUtils.filteredIterator(list.iterator(), evenPredicate);
        assertEquals(Arrays.asList(2, 4), IteratorUtils.toList(filtered));

        ListIterator<Integer> filteredList = IteratorUtils.filteredListIterator(list.listIterator(), evenPredicate);
        assertEquals(Arrays.asList(2, 4), IteratorUtils.toList(filteredList));
    }

    @Test(timeout = 4000)
    public void testLoopingIterators() {
        List<String> list = Arrays.asList("A", "B");
        ResettableIterator<String> loopIt = IteratorUtils.loopingIterator(list);
        assertEquals("A", loopIt.next());
        assertEquals("B", loopIt.next());
        assertEquals("A", loopIt.next());

        ResettableListIterator<String> loopListIt = IteratorUtils.loopingListIterator(list);
        assertEquals("A", loopListIt.next());
        assertEquals("B", loopListIt.next());
        assertEquals("A", loopListIt.next());
        assertEquals("B", loopListIt.previous());
    }

    @Test(timeout = 4000)
    public void testNodeListIteratorViaProxy() {
        final List<Node> nodes = new ArrayList<Node>();
        InvocationHandler nodeHandler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                return null;
            }
        };
        Node node1 = (Node) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Node.class}, nodeHandler);
        Node node2 = (Node) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Node.class}, nodeHandler);
        nodes.add(node1);
        nodes.add(node2);

        InvocationHandler nodeListHandler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("getLength".equals(method.getName())) {
                    return nodes.size();
                }
                if ("item".equals(method.getName())) {
                    return nodes.get(((Integer) args[0]).intValue());
                }
                return null;
            }
        };
        final NodeList mockNodeList = (NodeList) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] {NodeList.class}, nodeListHandler);

        NodeListIterator it = IteratorUtils.nodeListIterator(mockNodeList);
        assertTrue(it.hasNext());
        assertSame(node1, it.next());
        assertSame(node2, it.next());
        assertFalse(it.hasNext());

        InvocationHandler parentNodeHandler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                if ("getChildNodes".equals(method.getName())) {
                    return mockNodeList;
                }
                return null;
            }
        };
        Node parentNode = (Node) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[] {Node.class}, parentNodeHandler);

        NodeListIterator itParent = IteratorUtils.nodeListIterator(parentNode);
        assertTrue(itParent.hasNext());
        assertSame(node1, itParent.next());
        assertSame(node2, itParent.next());
    }

    @Test(timeout = 4000)
    public void testPeekingAndPushbackAndSkippingIterators() {
        List<String> list = Arrays.asList("a", "b", "c");
        PeekingIterator<String> peekIt = (PeekingIterator<String>) IteratorUtils.peekingIterator(list.iterator());
        assertEquals("a", peekIt.peek());
        assertEquals("a", peekIt.next());
        assertEquals("b", peekIt.peek());

        PushbackIterator<String> pushIt = (PushbackIterator<String>) IteratorUtils.pushbackIterator(list.iterator());
        assertEquals("a", pushIt.next());
        pushIt.pushback("z");
        assertEquals("z", pushIt.next());
        assertEquals("b", pushIt.next());

        SkippingIterator<String> skipIt = IteratorUtils.skippingIterator(list.iterator(), 2);
        assertEquals(Collections.singletonList("c"), IteratorUtils.toList(skipIt));
    }

    @Test(timeout = 4000)
    public void testZippingIteratorVariants() {
        Iterator<Integer> it1 = Arrays.asList(1, 4).iterator();
        Iterator<Integer> it2 = Arrays.asList(2, 5).iterator();
        Iterator<Integer> it3 = Arrays.asList(3, 6).iterator();

        ZippingIterator<Integer> zip2 = IteratorUtils.zippingIterator(it1, it2);
        assertEquals(Arrays.asList(1, 2, 4, 5), IteratorUtils.toList(zip2));

        it1 = Arrays.asList(1, 4).iterator();
        it2 = Arrays.asList(2, 5).iterator();
        ZippingIterator<Integer> zip3 = IteratorUtils.zippingIterator(it1, it2, it3);
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6), IteratorUtils.toList(zip3));

        @SuppressWarnings("unchecked")
        ZippingIterator<Integer> zipArr = IteratorUtils.zippingIterator(
                Arrays.asList(10).iterator(),
                Arrays.asList(20).iterator()
        );
        assertEquals(Arrays.asList(10, 20), IteratorUtils.toList(zipArr));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Collections-25 / COLLECTIONS-566)
    // =========================================================================

    /**
     * Targets defect COLLECTIONS-566:
     * When collatedIterator is called with a null comparator, it must default to
     * natural order rather than throwing NullPointerException when next() is called.
     */
    @Test(timeout = 4000)
    public void testCollatedIterator_NullComparator_NaturalOrder_COLLECTIONS566() {
        Iterator<Integer> it1 = Arrays.asList(1, 3, 5).iterator();
        Iterator<Integer> it2 = Arrays.asList(2, 4, 6).iterator();

        // 1. Two iterators variant
        Iterator<Integer> collated = IteratorUtils.collatedIterator(null, it1, it2);
        List<Integer> expected = Arrays.asList(1, 2, 3, 4, 5, 6);
        List<Integer> actual = IteratorUtils.toList(collated);
        assertEquals(expected, actual);

        // 2. Array variant
        it1 = Arrays.asList(10, 30).iterator();
        it2 = Arrays.asList(20, 40).iterator();
        @SuppressWarnings("unchecked")
        Iterator<Integer> collatedArr = IteratorUtils.collatedIterator(null, (Iterator<Integer>[]) new Iterator<?>[] {it1, it2});
        assertEquals(Arrays.asList(10, 20, 30, 40), IteratorUtils.toList(collatedArr));

        // 3. Collection variant
        it1 = Arrays.asList(100, 300).iterator();
        it2 = Arrays.asList(200, 400).iterator();
        List<Iterator<? extends Integer>> list = new ArrayList<Iterator<? extends Integer>>();
        list.add(it1);
        list.add(it2);
        Iterator<Integer> collatedColl = IteratorUtils.collatedIterator(null, list);
        assertEquals(Arrays.asList(100, 200, 300, 400), IteratorUtils.toList(collatedColl));
    }

    @Test(timeout = 4000)
    public void testCollatedIteratorWithExplicitComparator() {
        Comparator<Integer> reverseComp = new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                return o2.compareTo(o1);
            }
        };
        Iterator<Integer> it1 = Arrays.asList(5, 3, 1).iterator();
        Iterator<Integer> it2 = Arrays.asList(6, 4, 2).iterator();
        Iterator<Integer> collated = IteratorUtils.collatedIterator(reverseComp, it1, it2);
        assertEquals(Arrays.asList(6, 5, 4, 3, 2, 1), IteratorUtils.toList(collated));
    }

    // =========================================================================
    // Partition D: Collection Views & Conversion Utilities
    // =========================================================================

    @Test(timeout = 4000)
    public void testAsIteratorAndAsEnumeration() {
        Vector<String> vector = new Vector<String>(Arrays.asList("alpha", "beta"));
        Enumeration<String> en = vector.elements();
        Iterator<String> it = IteratorUtils.asIterator(en);
        assertTrue(it.hasNext());
        assertEquals("alpha", it.next());
        assertEquals("beta", it.next());
        assertFalse(it.hasNext());

        final List<String> removed = new ArrayList<String>();
        Enumeration<String> en2 = new Vector<String>(Arrays.asList("x", "y")).elements();
        Iterator<String> itRem = IteratorUtils.asIterator(en2, removed);
        assertEquals("x", itRem.next());
        itRem.remove();
        assertEquals(Collections.singletonList("x"), removed);

        Enumeration<String> backToEn = IteratorUtils.asEnumeration(Arrays.asList("1", "2").iterator());
        assertTrue(backToEn.hasMoreElements());
        assertEquals("1", backToEn.nextElement());
        assertEquals("2", backToEn.nextElement());
        assertFalse(backToEn.hasMoreElements());
    }

    @Test(timeout = 4000)
    public void testAsIterable() {
        List<String> list = Arrays.asList("foo", "bar");
        Iterable<String> iterable = IteratorUtils.asIterable(list.iterator());
        List<String> result = new ArrayList<String>();
        for (String s : iterable) {
            result.add(s);
        }
        assertEquals(list, result);

        Iterable<String> multiUse = IteratorUtils.asMultipleUseIterable(list.iterator());
        List<String> r1 = new ArrayList<String>();
        for (String s : multiUse) {
            r1.add(s);
        }
        assertEquals(list, r1);
        List<String> r2 = new ArrayList<String>();
        for (String s : multiUse) {
            r2.add(s);
        }
        assertEquals(list, r2);
    }

    @Test(timeout = 4000)
    public void testToListIterator() {
        Iterator<String> it = Arrays.asList("a", "b", "c").iterator();
        ListIterator<String> listIt = IteratorUtils.toListIterator(it);
        assertEquals("a", listIt.next());
        assertEquals("b", listIt.next());
        assertEquals("b", listIt.previous());
        assertEquals("b", listIt.next());
        assertEquals("c", listIt.next());
    }

    @Test(timeout = 4000)
    public void testToArray() {
        List<String> list = Arrays.asList("apple", "banana");
        Object[] objArr = IteratorUtils.toArray(list.iterator());
        assertArrayEquals(new Object[] {"apple", "banana"}, objArr);

        String[] strArr = IteratorUtils.toArray(list.iterator(), String.class);
        assertArrayEquals(new String[] {"apple", "banana"}, strArr);
    }

    @Test(timeout = 4000)
    public void testToList() {
        List<String> list = Arrays.asList("first", "second");
        List<String> result = IteratorUtils.toList(list.iterator());
        assertEquals(list, result);

        List<String> resultWithCap = IteratorUtils.toList(list.iterator(), 5);
        assertEquals(list, resultWithCap);
    }

    // Class with a public iterator() method for reflection test in getIterator
    public static class CustomIterableLike {
        public Iterator<String> iterator() {
            return Arrays.asList("custom1", "custom2").iterator();
        }
    }

    @Test(timeout = 4000)
    public void testGetIteratorExhaustive() {
        // null -> emptyIterator
        assertFalse(IteratorUtils.getIterator(null).hasNext());

        // Iterator -> returned directly
        Iterator<String> it = Arrays.asList("a").iterator();
        assertSame(it, IteratorUtils.getIterator(it));

        // Iterable -> iterator from iterable
        List<String> list = Arrays.asList("b");
        assertEquals("b", IteratorUtils.getIterator(list).next());

        // Object[] -> ObjectArrayIterator
        Object[] objArr = new Object[] {"c"};
        assertEquals("c", IteratorUtils.getIterator(objArr).next());

        // Enumeration -> EnumerationIterator
        Vector<String> v = new Vector<String>();
        v.add("d");
        assertEquals("d", IteratorUtils.getIterator(v.elements()).next());

        // Map -> values().iterator()
        Map<String, String> map = new HashMap<String, String>();
        map.put("k", "v");
        assertEquals("v", IteratorUtils.getIterator(map).next());

        // Dictionary -> elements() iterator
        Hashtable<String, String> dict = new Hashtable<String, String>();
        dict.put("dk", "dv");
        assertEquals("dv", IteratorUtils.getIterator((Dictionary<?, ?>) dict).next());

        // Primitive array -> ArrayIterator
        int[] prim = new int[] {99};
        assertEquals(Integer.valueOf(99), IteratorUtils.getIterator(prim).next());

        // Custom object with iterator() method via reflection
        assertEquals("custom1", IteratorUtils.getIterator(new CustomIterableLike()).next());

        // Fallback object -> singletonIterator
        Object plain = new Object();
        Iterator<?> plainIt = IteratorUtils.getIterator(plain);
        assertTrue(plainIt.hasNext());
        assertSame(plain, plainIt.next());
        assertFalse(plainIt.hasNext());
    }

    // =========================================================================
    // Partition E: Functional Helpers & String Representation
    // =========================================================================

    @Test(timeout = 4000)
    public void testApply() {
        final List<String> collector = new ArrayList<String>();
        Closure<String> closure = new Closure<String>() {
            public void execute(String input) {
                collector.add(input);
            }
        };

        IteratorUtils.apply(Arrays.asList("1", "2").iterator(), closure);
        assertEquals(Arrays.asList("1", "2"), collector);

        // Null iterator should be handled gracefully (no-op)
        IteratorUtils.apply(null, closure);
    }

    @Test(timeout = 4000)
    public void testFind() {
        List<Integer> numbers = Arrays.asList(1, 3, 4, 7);
        Predicate<Integer> evenPredicate = new Predicate<Integer>() {
            public boolean evaluate(Integer object) {
                return object % 2 == 0;
            }
        };
        assertEquals(Integer.valueOf(4), IteratorUtils.find(numbers.iterator(), evenPredicate));
        assertNull(IteratorUtils.find(Arrays.asList(1, 3).iterator(), evenPredicate));
        assertNull(IteratorUtils.find(null, evenPredicate));
    }

    @Test(timeout = 4000)
    public void testMatchesAnyAndAll() {
        List<Integer> numbers = Arrays.asList(1, 2, 3);
        Predicate<Integer> evenPredicate = new Predicate<Integer>() {
            public boolean evaluate(Integer object) {
                return object % 2 == 0;
            }
        };
        assertTrue(IteratorUtils.matchesAny(numbers.iterator(), evenPredicate));
        assertFalse(IteratorUtils.matchesAny(Arrays.asList(1, 3).iterator(), evenPredicate));
        assertFalse(IteratorUtils.matchesAny(null, evenPredicate));

        assertFalse(IteratorUtils.matchesAll(numbers.iterator(), evenPredicate));
        assertTrue(IteratorUtils.matchesAll(Arrays.asList(2, 4).iterator(), evenPredicate));
        assertTrue(IteratorUtils.matchesAll(null, evenPredicate));
    }

    @Test(timeout = 4000)
    public void testIsEmptyAndContainsAndSize() {
        assertTrue(IteratorUtils.isEmpty(null));
        assertTrue(IteratorUtils.isEmpty(Collections.emptyList().iterator()));
        assertFalse(IteratorUtils.isEmpty(Arrays.asList("test").iterator()));

        assertTrue(IteratorUtils.contains(Arrays.asList("a", "b").iterator(), "b"));
        assertFalse(IteratorUtils.contains(Arrays.asList("a", "b").iterator(), "c"));
        assertFalse(IteratorUtils.contains(null, "a"));

        assertEquals(0, IteratorUtils.size(null));
        assertEquals(0, IteratorUtils.size(Collections.emptyList().iterator()));
        assertEquals(3, IteratorUtils.size(Arrays.asList(1, 2, 3).iterator()));
    }

    @Test(timeout = 4000)
    public void testGet() {
        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("a", IteratorUtils.get(list.iterator(), 0));
        assertEquals("b", IteratorUtils.get(list.iterator(), 1));
        assertEquals("c", IteratorUtils.get(list.iterator(), 2));
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetOutOfBoundsHigh() {
        IteratorUtils.get(Arrays.asList("a").iterator(), 5);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetOutOfBoundsNegative() {
        IteratorUtils.get(Arrays.asList("a").iterator(), -1);
    }

    @Test(timeout = 4000)
    public void testToString() {
        List<Integer> list = Arrays.asList(1, 2, 3);
        assertEquals("[1, 2, 3]", IteratorUtils.toString(list.iterator()));
        assertEquals("[]", IteratorUtils.toString(Collections.emptyList().iterator()));
        assertEquals("[]", IteratorUtils.toString(null));

        Transformer<Integer, String> hexTransformer = new Transformer<Integer, String>() {
            public String transform(Integer input) {
                return "0x" + Integer.toHexString(input);
            }
        };
        assertEquals("[0x1, 0x2, 0x3]", IteratorUtils.toString(list.iterator(), hexTransformer));

        assertEquals("{0x1; 0x2; 0x3}", IteratorUtils.toString(
                list.iterator(), hexTransformer, "; ", "{", "}"));
        assertEquals("{}", IteratorUtils.toString(
                Collections.<Integer>emptyList().iterator(), hexTransformer, "; ", "{", "}"));
    }

    // =========================================================================
    // Partition F: Exception & Guard Paths
    // =========================================================================

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testTransformedIteratorNullIterator() {
        IteratorUtils.transformedIterator(null, new Transformer<Object, Object>() {
            public Object transform(Object input) { return input; }
        });
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testTransformedIteratorNullTransformer() {
        IteratorUtils.transformedIterator(Collections.emptyList().iterator(), null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFilteredIteratorNullIterator() {
        IteratorUtils.filteredIterator(null, TruePredicate.truePredicate());
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFilteredIteratorNullPredicate() {
        IteratorUtils.filteredIterator(Collections.emptyList().iterator(), null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFilteredListIteratorNullIterator() {
        IteratorUtils.filteredListIterator(null, TruePredicate.truePredicate());
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFilteredListIteratorNullPredicate() {
        IteratorUtils.filteredListIterator(new ArrayList<Object>().listIterator(), null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testLoopingIteratorNullCollection() {
        IteratorUtils.loopingIterator(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testLoopingListIteratorNullList() {
        IteratorUtils.loopingListIterator(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNodeListIteratorNullNodeList() {
        IteratorUtils.nodeListIterator((NodeList) null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testNodeListIteratorNullNode() {
        IteratorUtils.nodeListIterator((Node) null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testPeekingIteratorNull() {
        IteratorUtils.peekingIterator(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testPushbackIteratorNull() {
        IteratorUtils.pushbackIterator(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSkippingIteratorNegativeOffset() {
        IteratorUtils.skippingIterator(Collections.emptyList().iterator(), -1);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testAsIteratorNullEnumeration() {
        IteratorUtils.asIterator(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testAsIteratorNullCollection() {
        IteratorUtils.asIterator(new Vector<String>().elements(), null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testAsEnumerationNull() {
        IteratorUtils.asEnumeration(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testAsIterableNull() {
        IteratorUtils.asIterable(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testAsMultipleUseIterableNull() {
        IteratorUtils.asMultipleUseIterable(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToListIteratorNull() {
        IteratorUtils.toListIterator(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToArrayNull() {
        IteratorUtils.toArray(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToArrayNullClass() {
        IteratorUtils.toArray(Collections.emptyList().iterator(), null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToListNull() {
        IteratorUtils.toList(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testToListZeroSize() {
        IteratorUtils.toList(Collections.emptyList().iterator(), 0);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testApplyNullClosure() {
        IteratorUtils.apply(Collections.emptyList().iterator(), null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testFindNullPredicate() {
        IteratorUtils.find(Collections.emptyList().iterator(), null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testMatchesAnyNullPredicate() {
        IteratorUtils.matchesAny(Collections.emptyList().iterator(), null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testMatchesAllNullPredicate() {
        IteratorUtils.matchesAll(Collections.emptyList().iterator(), null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToStringNullTransformer() {
        IteratorUtils.toString(Collections.emptyList().iterator(), null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToStringNullDelimiter() {
        IteratorUtils.toString(Collections.emptyList().iterator(),
                TransformerUtils.<Object>stringValueTransformer(), null, "[", "]");
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToStringNullPrefix() {
        IteratorUtils.toString(Collections.emptyList().iterator(),
                TransformerUtils.<Object>stringValueTransformer(), ", ", null, "]");
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testToStringNullSuffix() {
        IteratorUtils.toString(Collections.emptyList().iterator(),
                TransformerUtils.<Object>stringValueTransformer(), ", ", "[", null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArrayIteratorNonArray() {
        IteratorUtils.arrayIterator("not-an-array");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testArrayListIteratorNonArray() {
        IteratorUtils.arrayListIterator("not-an-array");
    }
}