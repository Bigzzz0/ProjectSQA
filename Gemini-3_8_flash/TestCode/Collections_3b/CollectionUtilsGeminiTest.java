/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.collections;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Targeted Defect:
 * - CollectionUtils.removeAll(Collection, Collection) bug where ListUtils.retainAll is incorrectly
 *   invoked instead of ListUtils.removeAll, causing elements intended for removal to be retained.
 *   Targeted in testRemoveAllDefectRevealed() expecting cardinality reduction from 3 to 1.
 *
 * Branch & Coverage Matrix:
 * - union, intersection, disjunction, subtract:
 *   * Overlapping elements, non-overlapping elements, duplicates (cardinalities), empty inputs.
 * - containsAny:
 *   * coll1.size() < coll2.size() vs coll1.size() >= coll2.size(); true/false outcomes.
 * - getCardinalityMap, cardinality:
 *   * Target element null vs non-null; Collection vs Set vs Bag; elements present vs absent.
 * - isSubCollection, isProperSubCollection, isEqualCollection:
 *   * Equal sizes vs different sizes; identical cardinalities vs mismatched frequencies.
 *   * Strict sub-collections vs non-sub collections; proper subsets vs identical sets.
 * - find, forAllDo, filter, transform:
 *   * Null collection / predicate / closure / transformer safety checks.
 *   * List (in-place listIterator.set) vs other Collections (clear and addAll).
 *   * Match found vs not found; filtering with removals.
 * - countMatches, exists, select, selectRejected:
 *   * Null collections and predicates; full matches, partial matches, zero matches.
 * - collect (Collection / Iterator variants):
 *   * Null input, null transformer, destination collection mutation.
 * - addIgnoreNull, addAll (Iterator, Enumeration, Object[]):
 *   * Null checks, empty iterations, multi-element additions.
 * - index(Object, int / Object):
 *   * Map with exact key match vs index iteration on keys.
 *   * List, Object[], Enumeration, Iterator, Collection, and fallback for unsupported/negative idx.
 * - get(Object, int):
 *   * Negative index (IndexOutOfBoundsException).
 *   * Map.Entry extraction, List indexing, Object[] indexing, primitive array via reflection.
 *   * Iterator / Enumeration advance and exhaustion (IndexOutOfBoundsException).
 *   * Null object and unsupported type (IllegalArgumentException).
 * - size, sizeIsEmpty:
 *   * Map, Collection, Object[], Iterator, Enumeration, primitive array, null, unsupported object.
 * - isEmpty, isNotEmpty:
 *   * Null safe checks on empty/non-empty collections.
 * - reverseArray:
 *   * Null, empty, odd length, even length array reversals.
 * - isFull, maxSize:
 *   * Null check (NullPointerException).
 *   * Directly implementing BoundedCollection vs unmodifiable decorated vs standard Collection.
 * - retainAll, removeAll:
 *   * Retention and removal validation against ListUtils delegates.
 * - Decorator helpers:
 *   * synchronizedCollection, unmodifiableCollection, predicatedCollection, typedCollection,
 *     transformedCollection.
 * ----------------------------------------------------------------------------------------------------
 */
public class CollectionUtilsGeminiTest {

    // Helper implementation of BoundedCollection for deterministic testing
    private static class TestBoundedCollection extends ArrayList implements BoundedCollection {
        private final int max;

        public TestBoundedCollection(int max) {
            this.max = max;
        }

        public boolean isFull() {
            return size() >= max;
        }

        public int maxSize() {
            return max;
        }
    }

    // Helper implementation of Bag for cardinality testing
    private static class TestBag extends ArrayList implements Bag {
        public int getCount(Object object) {
            int count = 0;
            for (Iterator it = iterator(); it.hasNext();) {
                Object item = it.next();
                if (object == null ? item == null : object.equals(item)) {
                    count++;
                }
            }
            return count;
        }

        public boolean add(Object object, int nCopies) {
            for (int i = 0; i < nCopies; i++) {
                add(object);
            }
            return nCopies > 0;
        }

        public boolean remove(Object object, int nCopies) {
            boolean modified = false;
            for (int i = 0; i < nCopies; i++) {
                modified |= remove(object);
            }
            return modified;
        }

        public Set uniqueSet() {
            return new HashSet(this);
        }
    }

    // =========================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Known Bug)
    // =========================================================================

    /**
     * Targets the defect where CollectionUtils.removeAll delegates to ListUtils.retainAll
     * instead of ListUtils.removeAll.
     */
    @Test(timeout = 4000)
    public void testRemoveAllDefectRevealed() {
        List listA = new ArrayList();
        listA.add("A");
        listA.add("B");
        listA.add("C");

        List listB = new ArrayList();
        listB.add("A");
        listB.add("C");

        // When removing {"A", "C"} from {"A", "B", "C"}, exactly 1 element ("B") must remain.
        Collection result = CollectionUtils.removeAll(listA, listB);

        assertEquals("Expected size 1 after removing 2 of 3 elements", 1, result.size());
        assertTrue("Result should contain remaining element B", result.contains("B"));
        assertFalse("Result should not contain removed element A", result.contains("A"));
        assertFalse("Result should not contain removed element C", result.contains("C"));
    }

    // =========================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & SET OPERATIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testUnion() {
        List a = Arrays.asList("1", "2", "2", "3");
        List b = Arrays.asList("2", "3", "3", "4");

        Collection result = CollectionUtils.union(a, b);
        Map cardMap = CollectionUtils.getCardinalityMap(result);

        assertEquals(new Integer(1), cardMap.get("1"));
        assertEquals(new Integer(2), cardMap.get("2"));
        assertEquals(new Integer(2), cardMap.get("3"));
        assertEquals(new Integer(1), cardMap.get("4"));
        assertEquals(6, result.size());
    }

    @Test(timeout = 4000)
    public void testIntersection() {
        List a = Arrays.asList("1", "2", "2", "3");
        List b = Arrays.asList("2", "2", "2", "3", "4");

        Collection result = CollectionUtils.intersection(a, b);
        Map cardMap = CollectionUtils.getCardinalityMap(result);

        assertNull(cardMap.get("1"));
        assertEquals(new Integer(2), cardMap.get("2"));
        assertEquals(new Integer(1), cardMap.get("3"));
        assertNull(cardMap.get("4"));
        assertEquals(3, result.size());
    }

    @Test(timeout = 4000)
    public void testDisjunction() {
        List a = Arrays.asList("1", "2", "2", "3");
        List b = Arrays.asList("2", "3", "3", "4");

        Collection result = CollectionUtils.disjunction(a, b);
        Map cardMap = CollectionUtils.getCardinalityMap(result);

        assertEquals(new Integer(1), cardMap.get("1"));
        assertEquals(new Integer(1), cardMap.get("2"));
        assertEquals(new Integer(1), cardMap.get("3"));
        assertEquals(new Integer(1), cardMap.get("4"));
        assertEquals(4, result.size());
    }

    @Test(timeout = 4000)
    public void testSubtract() {
        List a = new ArrayList(Arrays.asList("A", "A", "A", "B", "C"));
        List b = Arrays.asList("A", "C", "D");

        Collection result = CollectionUtils.subtract(a, b);
        assertEquals(3, result.size());
        assertEquals(2, CollectionUtils.cardinality("A", result));
        assertEquals(1, CollectionUtils.cardinality("B", result));
        assertEquals(0, CollectionUtils.cardinality("C", result));
    }

    @Test(timeout = 4000)
    public void testContainsAny() {
        List a = Arrays.asList("A", "B");
        List b = Arrays.asList("B", "C", "D");
        List c = Arrays.asList("X", "Y");

        // coll1.size() < coll2.size() -> true
        assertTrue(CollectionUtils.containsAny(a, b));
        // coll1.size() >= coll2.size() -> true
        assertTrue(CollectionUtils.containsAny(b, a));
        // coll1.size() < coll2.size() -> false
        assertFalse(CollectionUtils.containsAny(a, c));
        // coll1.size() >= coll2.size() -> false
        assertFalse(CollectionUtils.containsAny(c, a));
    }

    @Test(timeout = 4000)
    public void testSubCollectionAndProperSubCollection() {
        List parent = Arrays.asList("A", "A", "B", "C");
        List sub = Arrays.asList("A", "B");
        List identical = Arrays.asList("A", "A", "B", "C");
        List overCount = Arrays.asList("A", "A", "A");

        assertTrue(CollectionUtils.isSubCollection(sub, parent));
        assertTrue(CollectionUtils.isProperSubCollection(sub, parent));

        assertTrue(CollectionUtils.isSubCollection(identical, parent));
        assertFalse(CollectionUtils.isProperSubCollection(identical, parent));

        assertFalse(CollectionUtils.isSubCollection(overCount, parent));
        assertFalse(CollectionUtils.isProperSubCollection(overCount, parent));
    }

    @Test(timeout = 4000)
    public void testIsEqualCollection() {
        List a = Arrays.asList("1", "2", "3");
        List b = Arrays.asList("3", "1", "2");
        List c = Arrays.asList("1", "2", "4");
        List d = Arrays.asList("1", "2");
        List e = Arrays.asList("1", "2", "2");

        assertTrue(CollectionUtils.isEqualCollection(a, b));
        assertFalse(CollectionUtils.isEqualCollection(a, c));
        assertFalse(CollectionUtils.isEqualCollection(a, d));
        assertFalse(CollectionUtils.isEqualCollection(a, e));
    }

    @Test(timeout = 4000)
    public void testCardinalityVariants() {
        // Set test
        Set set = new HashSet(Arrays.asList("X", "Y"));
        assertEquals(1, CollectionUtils.cardinality("X", set));
        assertEquals(0, CollectionUtils.cardinality("Z", set));

        // Bag test
        TestBag bag = new TestBag();
        bag.add("B", 3);
        assertEquals(3, CollectionUtils.cardinality("B", bag));
        assertEquals(0, CollectionUtils.cardinality("Z", bag));

        // List with nulls and non-nulls
        List list = new ArrayList();
        list.add("O");
        list.add(null);
        list.add("O");
        list.add(null);
        assertEquals(2, CollectionUtils.cardinality(null, list));
        assertEquals(2, CollectionUtils.cardinality("O", list));
        assertEquals(0, CollectionUtils.cardinality("Missing", list));
    }

    // =========================================================================
    // PARTITION B: FUNCTIONAL OPERATIONS (FIND, FILTER, TRANSFORM, SELECT, COLLECT)
    // =========================================================================

    @Test(timeout = 4000)
    public void testFind() {
        List list = Arrays.asList("apple", "banana", "cherry");
        Predicate startsWithB = new Predicate() {
            public boolean evaluate(Object object) {
                return object != null && ((String) object).startsWith("b");
            }
        };

        assertEquals("banana", CollectionUtils.find(list, startsWithB));
        assertNull(CollectionUtils.find(list, new Predicate() {
            public boolean evaluate(Object object) {
                return "zebra".equals(object);
            }
        }));
        assertNull(CollectionUtils.find(null, startsWithB));
        assertNull(CollectionUtils.find(list, null));
    }

    @Test(timeout = 4000)
    public void testForAllDo() {
        List list = new ArrayList(Arrays.asList("a", "b", "c"));
        final List executed = new ArrayList();
        Closure closure = new Closure() {
            public void execute(Object input) {
                executed.add(input);
            }
        };

        CollectionUtils.forAllDo(list, closure);
        assertEquals(Arrays.asList("a", "b", "c"), executed);

        // Null safety
        CollectionUtils.forAllDo(null, closure);
        CollectionUtils.forAllDo(list, null);
    }

    @Test(timeout = 4000)
    public void testFilter() {
        List list = new ArrayList(Arrays.asList("1", "22", "333", "4444"));
        Predicate lengthGt2 = new Predicate() {
            public boolean evaluate(Object object) {
                return ((String) object).length() > 2;
            }
        };

        CollectionUtils.filter(list, lengthGt2);
        assertEquals(2, list.size());
        assertEquals("333", list.get(0));
        assertEquals("4444", list.get(1));

        // Null safe
        CollectionUtils.filter(null, lengthGt2);
        CollectionUtils.filter(list, null);
        assertEquals(2, list.size());
    }

    @Test(timeout = 4000)
    public void testTransformListAndCollection() {
        Transformer toUpper = new Transformer() {
            public Object transform(Object input) {
                return ((String) input).toUpperCase();
            }
        };

        // List branch (in place listIterator.set)
        List list = new ArrayList(Arrays.asList("x", "y"));
        CollectionUtils.transform(list, toUpper);
        assertEquals("X", list.get(0));
        assertEquals("Y", list.get(1));

        // General Collection branch (clear and addAll)
        Set set = new HashSet(Arrays.asList("a", "b"));
        CollectionUtils.transform(set, toUpper);
        assertTrue(set.contains("A"));
        assertTrue(set.contains("B"));

        // Null safe
        CollectionUtils.transform(null, toUpper);
        CollectionUtils.transform(list, null);
    }

    @Test(timeout = 4000)
    public void testCountMatchesAndExists() {
        List list = Arrays.asList(1, 2, 3, 4, 5);
        Predicate isEven = new Predicate() {
            public boolean evaluate(Object object) {
                return ((Integer) object).intValue() % 2 == 0;
            }
        };

        assertEquals(2, CollectionUtils.countMatches(list, isEven));
        assertEquals(0, CollectionUtils.countMatches(null, isEven));
        assertEquals(0, CollectionUtils.countMatches(list, null));

        assertTrue(CollectionUtils.exists(list, isEven));
        assertFalse(CollectionUtils.exists(Arrays.asList(1, 3, 5), isEven));
        assertFalse(CollectionUtils.exists(null, isEven));
        assertFalse(CollectionUtils.exists(list, null));
    }

    @Test(timeout = 4000)
    public void testSelectAndSelectRejected() {
        List list = Arrays.asList(10, 15, 20, 25);
        Predicate greaterThan18 = new Predicate() {
            public boolean evaluate(Object object) {
                return ((Integer) object).intValue() > 18;
            }
        };

        Collection selected = CollectionUtils.select(list, greaterThan18);
        assertEquals(Arrays.asList(20, 25), selected);

        Collection rejected = CollectionUtils.selectRejected(list, greaterThan18);
        assertEquals(Arrays.asList(10, 15), rejected);

        List outSel = new ArrayList();
        CollectionUtils.select(null, greaterThan18, outSel);
        assertTrue(outSel.isEmpty());
        CollectionUtils.select(list, null, outSel);
        assertTrue(outSel.isEmpty());

        List outRej = new ArrayList();
        CollectionUtils.selectRejected(null, greaterThan18, outRej);
        assertTrue(outRej.isEmpty());
        CollectionUtils.selectRejected(list, null, outRej);
        assertTrue(outRej.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCollectVariants() {
        Transformer timesTwo = new Transformer() {
            public Object transform(Object input) {
                return new Integer(((Integer) input).intValue() * 2);
            }
        };

        List numbers = Arrays.asList(1, 2, 3);
        Collection resultColl = CollectionUtils.collect(numbers, timesTwo);
        assertEquals(Arrays.asList(2, 4, 6), resultColl);

        Collection resultIter = CollectionUtils.collect(numbers.iterator(), timesTwo);
        assertEquals(Arrays.asList(2, 4, 6), resultIter);

        List out = new ArrayList();
        CollectionUtils.collect(numbers, timesTwo, out);
        assertEquals(Arrays.asList(2, 4, 6), out);

        Collection nullCollResult = CollectionUtils.collect((Collection) null, timesTwo, new ArrayList());
        assertTrue(nullCollResult.isEmpty());

        Collection nullIterResult = CollectionUtils.collect((Iterator) null, timesTwo, new ArrayList());
        assertTrue(nullIterResult.isEmpty());

        Collection emptyFromNullTransformer = CollectionUtils.collect(numbers, (Transformer) null);
        assertTrue(emptyFromNullTransformer.isEmpty());
    }

    // =========================================================================
    // PARTITION D: COLLECTION ADDITIONS, EMPTY CHECKS & REVERSE
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddIgnoreNull() {
        List list = new ArrayList();
        assertFalse(CollectionUtils.addIgnoreNull(list, null));
        assertEquals(0, list.size());

        assertTrue(CollectionUtils.addIgnoreNull(list, "Element"));
        assertEquals(1, list.size());
    }

    @Test(timeout = 4000)
    public void testAddAllVariants() {
        List target = new ArrayList();

        CollectionUtils.addAll(target, Arrays.asList("1", "2").iterator());
        assertEquals(2, target.size());

        Vector v = new Vector();
        v.add("3");
        v.add("4");
        CollectionUtils.addAll(target, v.elements());
        assertEquals(4, target.size());

        CollectionUtils.addAll(target, new Object[]{"5", "6"});
        assertEquals(6, target.size());
        assertEquals("6", target.get(5));
    }

    @Test(timeout = 4000)
    public void testReverseArray() {
        Object[] even = new Object[]{"A", "B", "C", "D"};
        CollectionUtils.reverseArray(even);
        assertArrayEquals(new Object[]{"D", "C", "B", "A"}, even);

        Object[] odd = new Object[]{"1", "2", "3"};
        CollectionUtils.reverseArray(odd);
        assertArrayEquals(new Object[]{"3", "2", "1"}, odd);

        Object[] empty = new Object[0];
        CollectionUtils.reverseArray(empty);
        assertEquals(0, empty.length);

        Object[] single = new Object[]{"One"};
        CollectionUtils.reverseArray(single);
        assertEquals("One", single[0]);
    }

    @Test(timeout = 4000)
    public void testIsEmptyAndIsNotEmpty() {
        assertTrue(CollectionUtils.isEmpty(null));
        assertTrue(CollectionUtils.isEmpty(Collections.EMPTY_LIST));
        assertFalse(CollectionUtils.isEmpty(Collections.singletonList("X")));

        assertFalse(CollectionUtils.isNotEmpty(null));
        assertFalse(CollectionUtils.isNotEmpty(Collections.EMPTY_LIST));
        assertTrue(CollectionUtils.isNotEmpty(Collections.singletonList("X")));
    }

    // =========================================================================
    // PARTITION E: GET, SIZE, SIZE_IS_EMPTY, INDEX OPERATIONS
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetSuccessPaths() {
        // Map
        Map map = new HashMap();
        map.put("key", "val");
        Object mapEntry = CollectionUtils.get(map, 0);
        assertTrue(mapEntry instanceof Map.Entry);
        assertEquals("key", ((Map.Entry) mapEntry).getKey());

        // List
        List list = Arrays.asList("L0", "L1");
        assertEquals("L1", CollectionUtils.get(list, 1));

        // Object array
        Object[] arr = new Object[]{"A0", "A1"};
        assertEquals("A0", CollectionUtils.get(arr, 0));

        // Iterator
        assertEquals("I1", CollectionUtils.get(Arrays.asList("I0", "I1").iterator(), 1));

        // Collection (general)
        Set set = new HashSet(Collections.singleton("S0"));
        assertEquals("S0", CollectionUtils.get(set, 0));

        // Enumeration
        Vector v = new Vector();
        v.add("E0");
        assertEquals("E0", CollectionUtils.get(v.elements(), 0));

        // Primitive array (reflection Array.get)
        int[] primitives = new int[]{100, 200, 300};
        assertEquals(200, CollectionUtils.get(primitives, 1));
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetNegativeIndex() {
        CollectionUtils.get(new ArrayList(), -1);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetIteratorExhausted() {
        CollectionUtils.get(Collections.singletonList("A").iterator(), 5);
    }

    @Test(expected = IndexOutOfBoundsException.class, timeout = 4000)
    public void testGetEnumerationExhausted() {
        Vector v = new Vector();
        v.add("A");
        CollectionUtils.get(v.elements(), 3);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetNullObject() {
        CollectionUtils.get(null, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetUnsupportedObject() {
        CollectionUtils.get(new Object(), 0);
    }

    @Test(timeout = 4000)
    public void testSize() {
        Map map = new HashMap();
        map.put("1", "A");
        assertEquals(1, CollectionUtils.size(map));

        assertEquals(2, CollectionUtils.size(Arrays.asList("A", "B")));
        assertEquals(3, CollectionUtils.size(new Object[]{"A", "B", "C"}));
        assertEquals(2, CollectionUtils.size(Arrays.asList("X", "Y").iterator()));

        Vector v = new Vector();
        v.add("1");
        assertEquals(1, CollectionUtils.size(v.elements()));

        int[] prims = new int[]{1, 2, 3, 4};
        assertEquals(4, CollectionUtils.size(prims));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSizeNull() {
        CollectionUtils.size(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSizeUnsupported() {
        CollectionUtils.size(new Object());
    }

    @Test(timeout = 4000)
    public void testSizeIsEmpty() {
        assertTrue(CollectionUtils.sizeIsEmpty(new ArrayList()));
        assertFalse(CollectionUtils.sizeIsEmpty(Collections.singletonList("A")));

        assertTrue(CollectionUtils.sizeIsEmpty(new HashMap()));
        assertFalse(CollectionUtils.sizeIsEmpty(Collections.singletonMap("K", "V")));

        assertTrue(CollectionUtils.sizeIsEmpty(new Object[0]));
        assertFalse(CollectionUtils.sizeIsEmpty(new Object[]{"X"}));

        assertTrue(CollectionUtils.sizeIsEmpty(Collections.emptyList().iterator()));
        assertFalse(CollectionUtils.sizeIsEmpty(Collections.singletonList("Z").iterator()));

        assertTrue(CollectionUtils.sizeIsEmpty(new Vector().elements()));
        Vector v = new Vector();
        v.add("Y");
        assertFalse(CollectionUtils.sizeIsEmpty(v.elements()));

        assertTrue(CollectionUtils.sizeIsEmpty(new int[0]));
        assertFalse(CollectionUtils.sizeIsEmpty(new int[]{1}));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSizeIsEmptyNull() {
        CollectionUtils.sizeIsEmpty(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSizeIsEmptyUnsupported() {
        CollectionUtils.sizeIsEmpty(new Object());
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testIndexDeprecatedMethods() {
        // Map with direct key match
        Map mapWithIntKey = new HashMap();
        mapWithIntKey.put(new Integer(2), "Two");
        assertEquals("Two", CollectionUtils.index(mapWithIntKey, 2));

        // Map with keySet iteration
        Map mapWithoutIntKey = new HashMap();
        mapWithoutIntKey.put("Key0", "Val0");
        Object indexedKey = CollectionUtils.index(mapWithoutIntKey, 0);
        assertEquals("Key0", indexedKey);

        // List & Object array
        List list = Arrays.asList("A", "B", "C");
        assertEquals("B", CollectionUtils.index(list, 1));
        assertEquals("C", CollectionUtils.index(new Object[]{"A", "B", "C"}, 2));

        // Enumeration & Iterator & Collection
        Vector v = new Vector();
        v.add("E0");
        v.add("E1");
        assertEquals("E1", CollectionUtils.index(v.elements(), 1));
        assertEquals("I0", CollectionUtils.index(Arrays.asList("I0").iterator(), 0));
        assertEquals("C0", CollectionUtils.index(new HashSet(Collections.singleton("C0")), 0));

        // Non integer index or negative index
        assertEquals("Fallback", CollectionUtils.index("Fallback", "notAnInt"));
        assertEquals(list, CollectionUtils.index(list, -1));

        // Index overflow returns Iterator/Enumeration
        Object overflow = CollectionUtils.index(Arrays.asList("A").iterator(), 5);
        assertTrue(overflow instanceof Iterator);
    }

    // =========================================================================
    // PARTITION F: BOUNDED COLLECTION, DECORATORS & CONTRACT INTEGRITY
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsFullAndMaxSize() {
        TestBoundedCollection bounded = new TestBoundedCollection(2);
        assertEquals(2, CollectionUtils.maxSize(bounded));
        assertFalse(CollectionUtils.isFull(bounded));

        bounded.add("item1");
        bounded.add("item2");
        assertTrue(CollectionUtils.isFull(bounded));

        // Normal unbounded collection
        List unbounded = new ArrayList();
        assertEquals(-1, CollectionUtils.maxSize(unbounded));
        assertFalse(CollectionUtils.isFull(unbounded));
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testIsFullNull() {
        CollectionUtils.isFull(null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testMaxSizeNull() {
        CollectionUtils.maxSize(null);
    }

    @Test(timeout = 4000)
    public void testRetainAll() {
        List a = new ArrayList(Arrays.asList("A", "B", "C", "D"));
        List b = Arrays.asList("B", "D", "E");

        Collection retained = CollectionUtils.retainAll(a, b);
        assertEquals(2, retained.size());
        assertTrue(retained.contains("B"));
        assertTrue(retained.contains("D"));
    }

    @Test(timeout = 4000)
    public void testDecoratorsCreation() {
        List raw = new ArrayList();
        raw.add("test");

        Collection synch = CollectionUtils.synchronizedCollection(raw);
        assertNotNull(synch);

        Collection unmod = CollectionUtils.unmodifiableCollection(raw);
        assertNotNull(unmod);

        Predicate notNullPred = new Predicate() {
            public boolean evaluate(Object object) {
                return object != null;
            }
        };
        Collection pred = CollectionUtils.predicatedCollection(raw, notNullPred);
        assertNotNull(pred);

        Collection typed = CollectionUtils.typedCollection(raw, String.class);
        assertNotNull(typed);

        Transformer noop = new Transformer() {
            public Object transform(Object input) {
                return input;
            }
        };
        Collection trans = CollectionUtils.transformedCollection(raw, noop);
        assertNotNull(trans);
    }

    @Test(timeout = 4000)
    public void testConstructorAndConstants() {
        CollectionUtils utils = new CollectionUtils();
        assertNotNull(utils);
        assertNotNull(CollectionUtils.EMPTY_COLLECTION);
        assertTrue(CollectionUtils.EMPTY_COLLECTION.isEmpty());
    }
}