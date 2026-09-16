package org.apache.commons.collections;

import org.junit.Test;
import static org.junit.Assert.*;

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
import java.util.Set;
import java.util.Vector;

public class CollectionUtilsDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target defect: removeAll(Collection, Collection) returns wrong cardinality.
     * Expected behavior: For each element e in collection, if e is in remove, 
     *   the returned collection should NOT contain e (cardinality zero).
     * Defective behavior: The returned collection contains e with cardinality 1 
     *   (i.e., only removes one occurrence instead of all occurrences).
     * 
     * Key branches to cover:
     * - removeAll: null collection, null remove, empty collection, empty remove,
     *   collection with duplicates, remove with duplicates, disjoint collections,
     *   collection containing all elements of remove, remove containing all elements.
     * - union: null inputs, empty inputs, overlapping sets, duplicate handling.
     * - intersection: null inputs, empty inputs, overlapping sets, duplicates.
     * - disjunction: null inputs, empty inputs, overlapping sets, duplicates.
     * - subtract: null inputs, empty inputs, overlapping sets, duplicates.
     * - containsAny: null inputs, empty inputs, overlapping, disjoint.
     * - getCardinalityMap: null input, empty collection, duplicates.
     * - isSubCollection: null inputs, empty collections, proper subset, equal.
     * - isProperSubCollection: null inputs, empty collections, proper subset, equal.
     * - isEqualCollection: null inputs, empty collections, same/different cardinality.
     * - find: null collection, null predicate, found/not found.
     * - forAllDo: null collection, null closure, normal execution.
     * - filter: null collection, null predicate, filtering with/without matches.
     * - transform: null collection, null transformer, list vs non-list.
     * - exists: null collection, null predicate, found/not found.
     * - select: null collection, null predicate, selection with/without matches.
     * - selectRejected: null collection, null predicate, rejection with/without matches.
     * - collect: null collection/iterator, null transformer, normal execution.
     * - addIgnoreNull: null collection, null object, non-null object.
     * - addAll: null collection, null iterator/enumeration/array, normal execution.
     * - index: negative index, map, list, array, iterator, enumeration, collection, null.
     * - size: null, collection, array, iterator, enumeration, map.
     * - sizeIsEmpty: null, empty/non-empty collection, array, iterator, enumeration, map.
     * - isEmpty/isNotEmpty: null, empty, non-empty.
     * - reverseArray: null array, empty array, odd/even length.
     * - isFull: null collection, empty collection, non-full collection.
     * - maxSize: null collection, empty collection, non-full collection.
     * - retainAll: null collection, null retain, empty collection, empty retain,
     *   duplicates, disjoint.
     * - synchronizedCollection: null collection, normal collection.
     * - unmodifiableCollection: null collection, normal collection.
     * - predicatedCollection: null collection, null predicate, normal.
     * - typedCollection: null collection, null type, normal.
     * - transformedCollection: null collection, null transformer, normal.
     */

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testUnionBasic() {
        Collection a = new ArrayList(Arrays.asList(1, 2, 3));
        Collection b = new ArrayList(Arrays.asList(3, 4, 5));
        Collection result = CollectionUtils.union(a, b);
        assertEquals(5, result.size());
        assertTrue(result.containsAll(Arrays.asList(1, 2, 3, 4, 5)));
    }

    @Test(timeout = 4000)
    public void testUnionWithDuplicates() {
        Collection a = new ArrayList(Arrays.asList(1, 1, 2));
        Collection b = new ArrayList(Arrays.asList(1, 2, 2, 3));
        Collection result = CollectionUtils.union(a, b);
        // Union should preserve max cardinality: 1->2, 2->2, 3->1 => total 5
        assertEquals(5, result.size());
        assertEquals(2, CollectionUtils.getCardinalityMap(result).get(1));
        assertEquals(2, CollectionUtils.getCardinalityMap(result).get(2));
        assertEquals(1, CollectionUtils.getCardinalityMap(result).get(3));
    }

    @Test(timeout = 4000)
    public void testIntersectionBasic() {
        Collection a = new ArrayList(Arrays.asList(1, 2, 3));
        Collection b = new ArrayList(Arrays.asList(2, 3, 4));
        Collection result = CollectionUtils.intersection(a, b);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList(2, 3)));
    }

    @Test(timeout = 4000)
    public void testIntersectionWithDuplicates() {
        Collection a = new ArrayList(Arrays.asList(1, 2, 2, 3));
        Collection b = new ArrayList(Arrays.asList(2, 2, 2, 4));
        Collection result = CollectionUtils.intersection(a, b);
        // Intersection should preserve min cardinality: 2->2
        assertEquals(2, result.size());
        assertEquals(2, CollectionUtils.getCardinalityMap(result).get(2));
    }

    @Test(timeout = 4000)
    public void testDisjunctionBasic() {
        Collection a = new ArrayList(Arrays.asList(1, 2, 3));
        Collection b = new ArrayList(Arrays.asList(3, 4, 5));
        Collection result = CollectionUtils.disjunction(a, b);
        assertEquals(4, result.size());
        assertTrue(result.containsAll(Arrays.asList(1, 2, 4, 5)));
    }

    @Test(timeout = 4000)
    public void testDisjunctionWithDuplicates() {
        Collection a = new ArrayList(Arrays.asList(1, 1, 2));
        Collection b = new ArrayList(Arrays.asList(1, 2, 2, 3));
        Collection result = CollectionUtils.disjunction(a, b);
        // Disjunction: |card(a)-card(b)| for each element: 1->0, 2->0, 3->1 => total 1
        assertEquals(1, result.size());
        assertTrue(result.contains(3));
    }

    @Test(timeout = 4000)
    public void testSubtractBasic() {
        Collection a = new ArrayList(Arrays.asList(1, 2, 3, 4));
        Collection b = new ArrayList(Arrays.asList(2, 4));
        Collection result = CollectionUtils.subtract(a, b);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList(1, 3)));
    }

    @Test(timeout = 4000)
    public void testSubtractWithDuplicates() {
        Collection a = new ArrayList(Arrays.asList(1, 2, 2, 3, 3, 3));
        Collection b = new ArrayList(Arrays.asList(2, 3));
        Collection result = CollectionUtils.subtract(a, b);
        // Subtract should remove all occurrences of elements in b
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList(1, 3, 3)));
    }

    @Test(timeout = 4000)
    public void testContainsAnyTrue() {
        Collection a = new ArrayList(Arrays.asList(1, 2, 3));
        Collection b = new ArrayList(Arrays.asList(3, 4));
        assertTrue(CollectionUtils.containsAny(a, b));
    }

    @Test(timeout = 4000)
    public void testContainsAnyFalse() {
        Collection a = new ArrayList(Arrays.asList(1, 2));
        Collection b = new ArrayList(Arrays.asList(3, 4));
        assertFalse(CollectionUtils.containsAny(a, b));
    }

    @Test(timeout = 4000)
    public void testGetCardinalityMap() {
        Collection coll = new ArrayList(Arrays.asList("a", "b", "a", "c", "b", "a"));
        Map map = CollectionUtils.getCardinalityMap(coll);
        assertEquals(3, map.size());
        assertEquals(3, map.get("a"));
        assertEquals(2, map.get("b"));
        assertEquals(1, map.get("c"));
    }

    @Test(timeout = 4000)
    public void testIsSubCollectionTrue() {
        Collection a = new ArrayList(Arrays.asList(1, 2, 2));
        Collection b = new ArrayList(Arrays.asList(1, 2, 2, 3));
        assertTrue(CollectionUtils.isSubCollection(a, b));
    }

    @Test(timeout = 4000)
    public void testIsSubCollectionFalse() {
        Collection a = new ArrayList(Arrays.asList(1, 2, 2, 2));
        Collection b = new ArrayList(Arrays.asList(1, 2, 2, 3));
        assertFalse(CollectionUtils.isSubCollection(a, b));
    }

    @Test(timeout = 4000)
    public void testIsProperSubCollectionTrue() {
        Collection a = new ArrayList(Arrays.asList(1, 2));
        Collection b = new ArrayList(Arrays.asList(1, 2, 3));
        assertTrue(CollectionUtils.isProperSubCollection(a, b));
    }

    @Test(timeout = 4000)
    public void testIsProperSubCollectionFalseEqual() {
        Collection a = new ArrayList(Arrays.asList(1, 2, 3));
        Collection b = new ArrayList(Arrays.asList(1, 2, 3));
        assertFalse(CollectionUtils.isProperSubCollection(a, b));
    }

    @Test(timeout = 4000)
    public void testIsEqualCollectionTrue() {
        Collection a = new ArrayList(Arrays.asList(1, 2, 2, 3));
        Collection b = new ArrayList(Arrays.asList(3, 2, 1, 2));
        assertTrue(CollectionUtils.isEqualCollection(a, b));
    }

    @Test(timeout = 4000)
    public void testIsEqualCollectionFalse() {
        Collection a = new ArrayList(Arrays.asList(1, 2, 2));
        Collection b = new ArrayList(Arrays.asList(1, 2, 3));
        assertFalse(CollectionUtils.isEqualCollection(a, b));
    }

    @Test(timeout = 4000)
    public void testFindFound() {
        Collection coll = new ArrayList(Arrays.asList(1, 2, 3, 4));
        Object result = CollectionUtils.find(coll, new Predicate() {
            public boolean evaluate(Object obj) {
                return ((Integer) obj) % 2 == 0;
            }
        });
        assertEquals(2, result);
    }

    @Test(timeout = 4000)
    public void testFindNotFound() {
        Collection coll = new ArrayList(Arrays.asList(1, 3, 5));
        Object result = CollectionUtils.find(coll, new Predicate() {
            public boolean evaluate(Object obj) {
                return ((Integer) obj) % 2 == 0;
            }
        });
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testForAllDo() {
        final List<Integer> results = new ArrayList<Integer>();
        Collection coll = new ArrayList(Arrays.asList(1, 2, 3));
        CollectionUtils.forAllDo(coll, new Closure() {
            public void execute(Object input) {
                results.add((Integer) input * 2);
            }
        });
        assertEquals(Arrays.asList(2, 4, 6), results);
    }

    @Test(timeout = 4000)
    public void testFilter() {
        Collection coll = new ArrayList(Arrays.asList(1, 2, 3, 4, 5));
        CollectionUtils.filter(coll, new Predicate() {
            public boolean evaluate(Object obj) {
                return ((Integer) obj) % 2 == 0;
            }
        });
        assertEquals(Arrays.asList(2, 4), coll);
    }

    @Test(timeout = 4000)
    public void testTransformList() {
        List coll = new ArrayList(Arrays.asList(1, 2, 3));
        CollectionUtils.transform(coll, new Transformer() {
            public Object transform(Object input) {
                return (Integer) input * 10;
            }
        });
        assertEquals(Arrays.asList(10, 20, 30), coll);
    }

    @Test(timeout = 4000)
    public void testTransformNonList() {
        Collection coll = new HashSet(Arrays.asList(1, 2, 3));
        CollectionUtils.transform(coll, new Transformer() {
            public Object transform(Object input) {
                return (Integer) input * 10;
            }
        });
        assertEquals(new HashSet(Arrays.asList(10, 20, 30)), coll);
    }

    @Test(timeout = 4000)
    public void testExistsTrue() {
        Collection coll = new ArrayList(Arrays.asList(1, 2, 3));
        assertTrue(CollectionUtils.exists(coll, new Predicate() {
            public boolean evaluate(Object obj) {
                return (Integer) obj == 2;
            }
        }));
    }

    @Test(timeout = 4000)
    public void testExistsFalse() {
        Collection coll = new ArrayList(Arrays.asList(1, 3));
        assertFalse(CollectionUtils.exists(coll, new Predicate() {
            public boolean evaluate(Object obj) {
                return (Integer) obj == 2;
            }
        }));
    }

    @Test(timeout = 4000)
    public void testSelect() {
        Collection input = new ArrayList(Arrays.asList(1, 2, 3, 4));
        Collection result = CollectionUtils.select(input, new Predicate() {
            public boolean evaluate(Object obj) {
                return (Integer) obj % 2 == 0;
            }
        });
        assertEquals(Arrays.asList(2, 4), result);
    }

    @Test(timeout = 4000)
    public void testSelectRejected() {
        Collection input = new ArrayList(Arrays.asList(1, 2, 3, 4));
        Collection result = CollectionUtils.selectRejected(input, new Predicate() {
            public boolean evaluate(Object obj) {
                return (Integer) obj % 2 == 0;
            }
        });
        assertEquals(Arrays.asList(1, 3), result);
    }

    @Test(timeout = 4000)
    public void testCollectCollection() {
        Collection input = new ArrayList(Arrays.asList(1, 2, 3));
        Collection result = CollectionUtils.collect(input, new Transformer() {
            public Object transform(Object input) {
                return (Integer) input * 2;
            }
        });
        assertEquals(Arrays.asList(2, 4, 6), result);
    }

    @Test(timeout = 4000)
    public void testCollectIterator() {
        Iterator it = Arrays.asList(1, 2, 3).iterator();
        Collection result = CollectionUtils.collect(it, new Transformer() {
            public Object transform(Object input) {
                return (Integer) input * 2;
            }
        });
        assertEquals(Arrays.asList(2, 4, 6), result);
    }

    @Test(timeout = 4000)
    public void testAddIgnoreNull() {
        Collection coll = new ArrayList();
        assertTrue(CollectionUtils.addIgnoreNull(coll, "value"));
        assertFalse(CollectionUtils.addIgnoreNull(coll, null));
        assertEquals(1, coll.size());
    }

    @Test(timeout = 4000)
    public void testAddAllIterator() {
        Collection coll = new ArrayList();
        CollectionUtils.addAll(coll, Arrays.asList(1, 2, 3).iterator());
        assertEquals(Arrays.asList(1, 2, 3), coll);
    }

    @Test(timeout = 4000)
    public void testAddAllEnumeration() {
        Collection coll = new ArrayList();
        Vector v = new Vector(Arrays.asList(1, 2, 3));
        CollectionUtils.addAll(coll, v.elements());
        assertEquals(Arrays.asList(1, 2, 3), coll);
    }

    @Test(timeout = 4000)
    public void testAddAllArray() {
        Collection coll = new ArrayList();
        CollectionUtils.addAll(coll, new Object[] {1, 2, 3});
        assertEquals(Arrays.asList(1, 2, 3), coll);
    }

    @Test(timeout = 4000)
    public void testIndexList() {
        List list = Arrays.asList("a", "b", "c");
        assertEquals("b", CollectionUtils.index(list, 1));
    }

    @Test(timeout = 4000)
    public void testIndexArray() {
        String[] array = {"a", "b", "c"};
        assertEquals("c", CollectionUtils.index(array, 2));
    }

    @Test(timeout = 4000)
    public void testIndexMap() {
        Map map = new HashMap();
        map.put("key1", "value1");
        map.put("key2", "value2");
        Object entry = CollectionUtils.index(map, 0);
        assertTrue(entry instanceof Map.Entry);
    }

    @Test(timeout = 4000)
    public void testIndexIterator() {
        Iterator it = Arrays.asList("a", "b", "c").iterator();
        assertEquals("b", CollectionUtils.index(it, 1));
    }

    @Test(timeout = 4000)
    public void testIndexEnumeration() {
        Vector v = new Vector(Arrays.asList("a", "b", "c"));
        assertEquals("c", CollectionUtils.index(v.elements(), 2));
    }

    @Test(timeout = 4000)
    public void testIndexCollection() {
        Collection coll = new ArrayList(Arrays.asList("a", "b", "c"));
        assertEquals("a", CollectionUtils.index(coll, 0));
    }

    @Test(timeout = 4000)
    public void testIndexNull() {
        assertNull(CollectionUtils.index(null, 0));
    }

    @Test(timeout = 4000)
    public void testSizeCollection() {
        Collection coll = new ArrayList(Arrays.asList(1, 2, 3));
        assertEquals(3, CollectionUtils.size(coll));
    }

    @Test(timeout = 4000)
    public void testSizeArray() {
        String[] array = {"a", "b"};
        assertEquals(2, CollectionUtils.size(array));
    }

    @Test(timeout = 4000)
    public void testSizeIterator() {
        Iterator it = Arrays.asList(1, 2, 3).iterator();
        assertEquals(3, CollectionUtils.size(it));
    }

    @Test(timeout = 4000)
    public void testSizeEnumeration() {
        Vector v = new Vector(Arrays.asList(1, 2));
        assertEquals(2, CollectionUtils.size(v.elements()));
    }

    @Test(timeout = 4000)
    public void testSizeMap() {
        Map map = new HashMap();
        map.put("a", 1);
        map.put("b", 2);
        assertEquals(2, CollectionUtils.size(map));
    }

    @Test(timeout = 4000)
    public void testSizeNull() {
        assertEquals(0, CollectionUtils.size(null));
    }

    @Test(timeout = 4000)
    public void testSizeIsEmptyCollection() {
        assertTrue(CollectionUtils.sizeIsEmpty(new ArrayList()));
        assertFalse(CollectionUtils.sizeIsEmpty(new ArrayList(Arrays.asList(1))));
    }

    @Test(timeout = 4000)
    public void testSizeIsEmptyArray() {
        assertTrue(CollectionUtils.sizeIsEmpty(new Object[0]));
        assertFalse(CollectionUtils.sizeIsEmpty(new Object[] {1}));
    }

    @Test(timeout = 4000)
    public void testSizeIsEmptyIterator() {
        assertTrue(CollectionUtils.sizeIsEmpty(Collections.emptyList().iterator()));
        assertFalse(CollectionUtils.sizeIsEmpty(Arrays.asList(1).iterator()));
    }

    @Test(timeout = 4000)
    public void testSizeIsEmptyEnumeration() {
        assertTrue(CollectionUtils.sizeIsEmpty(new Vector().elements()));
        assertFalse(CollectionUtils.sizeIsEmpty(new Vector(Arrays.asList(1)).elements()));
    }

    @Test(timeout = 4000)
    public void testSizeIsEmptyMap() {
        assertTrue(CollectionUtils.sizeIsEmpty(new HashMap()));
        Map map = new HashMap();
        map.put("a", 1);
        assertFalse(CollectionUtils.sizeIsEmpty(map));
    }

    @Test(timeout = 4000)
    public void testSizeIsEmptyNull() {
        assertTrue(CollectionUtils.sizeIsEmpty(null));
    }

    @Test(timeout = 4000)
    public void testIsEmpty() {
        assertTrue(CollectionUtils.isEmpty(null));
        assertTrue(CollectionUtils.isEmpty(new ArrayList()));
        assertFalse(CollectionUtils.isEmpty(new ArrayList(Arrays.asList(1))));
    }

    @Test(timeout = 4000)
    public void testIsNotEmpty() {
        assertFalse(CollectionUtils.isNotEmpty(null));
        assertFalse(CollectionUtils.isNotEmpty(new ArrayList()));
        assertTrue(CollectionUtils.isNotEmpty(new ArrayList(Arrays.asList(1))));
    }

    @Test(timeout = 4000)
    public void testReverseArray() {
        Integer[] array = {1, 2, 3, 4, 5};
        CollectionUtils.reverseArray(array);
        assertArrayEquals(new Integer[] {5, 4, 3, 2, 1}, array);
    }

    @Test(timeout = 4000)
    public void testReverseArrayEvenLength() {
        Integer[] array = {1, 2, 3, 4};
        CollectionUtils.reverseArray(array);
        assertArrayEquals(new Integer[] {4, 3, 2, 1}, array);
    }

    @Test(timeout = 4000)
    public void testIsFull() {
        Collection coll = new ArrayList();
        assertFalse(CollectionUtils.isFull(coll));
    }

    @Test(timeout = 4000)
    public void testMaxSize() {
        Collection coll = new ArrayList();
        assertEquals(-1, CollectionUtils.maxSize(coll));
    }

    @Test(timeout = 4000)
    public void testRetainAll() {
        Collection collection = new ArrayList(Arrays.asList(1, 2, 2, 3, 3, 3));
        Collection retain = new ArrayList(Arrays.asList(2, 3));
        Collection result = CollectionUtils.retainAll(collection, retain);
        assertEquals(5, result.size());
        assertEquals(2, CollectionUtils.getCardinalityMap(result).get(2));
        assertEquals(3, CollectionUtils.getCardinalityMap(result).get(3));
    }

    @Test(timeout = 4000)
    public void testSynchronizedCollection() {
        Collection coll = new ArrayList(Arrays.asList(1, 2));
        Collection result = CollectionUtils.synchronizedCollection(coll);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testUnmodifiableCollection() {
        Collection coll = new ArrayList(Arrays.asList(1, 2));
        Collection result = CollectionUtils.unmodifiableCollection(coll);
        assertNotNull(result);
        assertEquals(2, result.size());
        try {
            result.add(3);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPredicatedCollection() {
        Collection coll = new ArrayList();
        Collection result = CollectionUtils.predicatedCollection(coll, new Predicate() {
            public boolean evaluate(Object obj) {
                return obj instanceof Integer;
            }
        });
        result.add(1);
        try {
            result.add("string");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTypedCollection() {
        Collection coll = new ArrayList();
        Collection result = CollectionUtils.typedCollection(coll, Integer.class);
        result.add(1);
        try {
            result.add("string");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTransformedCollection() {
        Collection coll = new ArrayList();
        Collection result = CollectionUtils.transformedCollection(coll, new Transformer() {
            public Object transform(Object input) {
                return (Integer) input * 2;
            }
        });
        result.add(1);
        assertEquals(2, result.iterator().next());
    }

    // ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ====================

    @Test(timeout = 4000)
    public void testUnionNullInputs() {
        try {
            CollectionUtils.union(null, new ArrayList());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            CollectionUtils.union(new ArrayList(), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testUnionEmptyCollections() {
        Collection result = CollectionUtils.union(new ArrayList(), new ArrayList());
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testIntersectionNullInputs() {
        try {
            CollectionUtils.intersection(null, new ArrayList());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            CollectionUtils.intersection(new ArrayList(), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIntersectionEmptyCollections() {
        Collection result = CollectionUtils.intersection(new ArrayList(), new ArrayList());
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testDisjunctionNullInputs() {
        try {
            CollectionUtils.disjunction(null, new ArrayList());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            CollectionUtils.disjunction(new ArrayList(), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testDisjunctionEmptyCollections() {
        Collection result = CollectionUtils.disjunction(new ArrayList(), new ArrayList());
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testSubtractNullInputs() {
        try {
            CollectionUtils.subtract(null, new ArrayList());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            CollectionUtils.subtract(new ArrayList(), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSubtractEmptyCollections() {
        Collection result = CollectionUtils.subtract(new ArrayList(), new ArrayList());
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testContainsAnyNullInputs() {
        try {
            CollectionUtils.containsAny(null, new ArrayList());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            CollectionUtils.containsAny(new ArrayList(), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testContainsAnyEmptyCollections() {
        assertFalse(CollectionUtils.containsAny(new ArrayList(), new ArrayList()));
        assertFalse(CollectionUtils.containsAny(new ArrayList(Arrays.asList(1)), new ArrayList()));
        assertFalse(CollectionUtils.containsAny(new ArrayList(), new ArrayList(Arrays.asList(1))));
    }

    @Test(timeout = 4000)
    public void testGetCardinalityMapNull() {
        try {
            CollectionUtils.getCardinalityMap(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetCardinalityMapEmpty() {
        Map map = CollectionUtils.getCardinalityMap(new ArrayList());
        assertTrue(map.isEmpty());
    }

    @Test(timeout = 4000)
    public void testIsSubCollectionNullInputs() {
        try {
            CollectionUtils.isSubCollection(null, new ArrayList());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            CollectionUtils.isSubCollection(new ArrayList(), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIsSubCollectionEmpty() {
        assertTrue(CollectionUtils.isSubCollection(new ArrayList(), new ArrayList()));
        assertTrue(CollectionUtils.isSubCollection(new ArrayList(), new ArrayList(Arrays.asList(1))));
        assertFalse(CollectionUtils.isSubCollection(new ArrayList(Arrays.asList(1)), new ArrayList()));
    }

    @Test(timeout = 4000)
    public void testIsProperSubCollectionNullInputs() {
        try {
            CollectionUtils.isProperSubCollection(null, new ArrayList());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            CollectionUtils.isProperSubCollection(new ArrayList(), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIsProperSubCollectionEmpty() {
        assertFalse(CollectionUtils.isProperSubCollection(new ArrayList(), new ArrayList()));
        assertTrue(CollectionUtils.isProperSubCollection(new ArrayList(), new ArrayList(Arrays.asList(1))));
        assertFalse(CollectionUtils.isProperSubCollection(new ArrayList(Arrays.asList(1)), new ArrayList()));
    }

    @Test(timeout = 4000)
    public void testIsEqualCollectionNullInputs() {
        try {
            CollectionUtils.isEqualCollection(null, new ArrayList());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
        try {
            CollectionUtils.isEqualCollection(new ArrayList(), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIsEqualCollectionEmpty() {
        assertTrue(CollectionUtils.isEqualCollection(new ArrayList(), new ArrayList()));
        assertFalse(CollectionUtils.isEqualCollection(new ArrayList(), new ArrayList(Arrays.asList(1))));
    }

    @Test(timeout = 4000)
    public void testFindNullCollection() {
        assertNull(CollectionUtils.find(null, new Predicate() {
            public boolean evaluate(Object obj) {
                return true;
            }
        }));
    }

    @Test(timeout = 4000)
    public void testFindNullPredicate() {
        Collection coll = new ArrayList(Arrays.asList(1, 2));
        assertNull(CollectionUtils.find(coll, null));
    }

    @Test(timeout = 4000)
    public void testForAllDoNullCollection() {
        try {
            CollectionUtils.forAllDo(null, new Closure() {
                public void execute(Object input) {
                }
            });
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testForAllDoNullClosure() {
        Collection coll = new ArrayList(Arrays.asList(1));
        try {
            CollectionUtils.forAllDo(coll, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFilterNullCollection() {
        try {
            CollectionUtils.filter(null, new Predicate() {
                public boolean evaluate(Object obj) {
                    return true;
                }
            });
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testFilterNullPredicate() {
        Collection coll = new ArrayList(Arrays.asList(1, 2));
        CollectionUtils.filter(coll, null);
        assertEquals(0, coll.size());
    }

    @Test(timeout = 4000)
    public void testTransformNullCollection() {
        try {
            CollectionUtils.transform(null, new Transformer() {
                public Object transform(Object input) {
                    return input;
                }
            });
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTransformNullTransformer() {
        Collection coll = new ArrayList(Arrays.asList(1, 2));
        CollectionUtils.transform(coll, null);
        assertEquals(2, coll.size());
    }

    @Test(timeout = 4000)
    public void testExistsNullCollection() {
        assertFalse(CollectionUtils.exists(null, new Predicate() {
            public boolean evaluate(Object obj) {
                return true;
            }
        }));
    }

    @Test(timeout = 4000)
    public void testExistsNullPredicate() {
        Collection coll = new ArrayList(Arrays.asList(1, 2));
        assertFalse(CollectionUtils.exists(coll, null));
    }

    @Test(timeout = 4000)
    public void testSelectNullCollection() {
        Collection result = CollectionUtils.select(null, new Predicate() {
            public boolean evaluate(Object obj) {
                return true;
            }
        });
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testSelectNullPredicate() {
        Collection input = new ArrayList(Arrays.asList(1, 2));
        Collection result = CollectionUtils.select(input, null);
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testSelectRejectedNullCollection() {
        Collection result = CollectionUtils.selectRejected(null, new Predicate() {
            public boolean evaluate(Object obj) {
                return true;
            }
        });
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testSelectRejectedNullPredicate() {
        Collection input = new ArrayList(Arrays.asList(1, 2));
        Collection result = CollectionUtils.selectRejected(input, null);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testCollectNullCollection() {
        Collection result = CollectionUtils.collect((Collection) null, new Transformer() {
            public Object transform(Object input) {
                return input;
            }
        });
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCollectNullTransformer() {
        Collection input = new ArrayList(Arrays.asList(1, 2));
        Collection result = CollectionUtils.collect(input, (Transformer) null);
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testCollectNullIterator() {
        Collection result = CollectionUtils.collect((Iterator) null, new Transformer() {
            public Object transform(Object input) {
                return input;
            }
        });
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testAddIgnoreNullNullCollection() {
        try {
            CollectionUtils.addIgnoreNull(null, "value");
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddAllNullCollection() {
        try {
            CollectionUtils.addAll(null, Arrays.asList(1).iterator());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddAllNullIterator() {
        Collection coll = new ArrayList();
        try {
            CollectionUtils.addAll(coll, (Iterator) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddAllNullEnumeration() {
        Collection coll = new ArrayList();
        try {
            CollectionUtils.addAll(coll, (Enumeration) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testAddAllNullArray() {
        Collection coll = new ArrayList();
        try {
            CollectionUtils.addAll(coll, (Object[]) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIndexNegative() {
        try {
            CollectionUtils.index(new ArrayList(Arrays.asList(1)), -1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIndexOutOfBoundsList() {
        try {
            CollectionUtils.index(new ArrayList(Arrays.asList(1)), 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIndexInvalidType() {
        try {
            CollectionUtils.index(new Object(), 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSizeInvalidType() {
        try {
            CollectionUtils.size(new Object());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSizeIsEmptyInvalidType() {
        try {
            CollectionUtils.sizeIsEmpty(new Object());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReverseArrayNull() {
        try {
            CollectionUtils.reverseArray(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReverseArrayEmpty() {
        Object[] array = new Object[0];
        CollectionUtils.reverseArray(array);
        assertEquals(0, array.length);
    }

    @Test(timeout = 4000)
    public void testIsFullNull() {
        try {
            CollectionUtils.isFull(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMaxSizeNull() {
        try {
            CollectionUtils.maxSize(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRetainAllNullCollection() {
        try {
            CollectionUtils.retainAll(null, new ArrayList());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRetainAllNullRetain() {
        try {
            CollectionUtils.retainAll(new ArrayList(), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRetainAllEmpty() {
        Collection result = CollectionUtils.retainAll(new ArrayList(), new ArrayList());
        assertTrue(result.isEmpty());
        result = CollectionUtils.retainAll(new ArrayList(Arrays.asList(1, 2)), new ArrayList());
        assertTrue(result.isEmpty());
        result = CollectionUtils.retainAll(new ArrayList(), new ArrayList(Arrays.asList(1)));
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testSynchronizedCollectionNull() {
        try {
            CollectionUtils.synchronizedCollection(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testUnmodifiableCollectionNull() {
        try {
            CollectionUtils.unmodifiableCollection(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPredicatedCollectionNullCollection() {
        try {
            CollectionUtils.predicatedCollection(null, new Predicate() {
                public boolean evaluate(Object obj) {
                    return true;
                }
            });
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testPredicatedCollectionNullPredicate() {
        try {
            CollectionUtils.predicatedCollection(new ArrayList(), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTypedCollectionNullCollection() {
        try {
            CollectionUtils.typedCollection(null, Integer.class);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTypedCollectionNullType() {
        try {
            CollectionUtils.typedCollection(new ArrayList(), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTransformedCollectionNullCollection() {
        try {
            CollectionUtils.transformedCollection(null, new Transformer() {
                public Object transform(Object input) {
                    return input;
                }
            });
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTransformedCollectionNullTransformer() {
        try {
            CollectionUtils.transformedCollection(new ArrayList(), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect: removeAll(Collection, Collection) should remove ALL occurrences of
     * elements in the remove collection. The defective version only removes one
     * occurrence, resulting in cardinality 1 instead of 0.
     */
    @Test(timeout = 4000)
    public void testRemoveAllDefect() {
        Collection collection = new ArrayList(Arrays.asList(1, 1, 2, 2, 3));
        Collection remove = new ArrayList(Arrays.asList(1, 2));
        Collection result = CollectionUtils.removeAll(collection, remove);
        
        // Expected: all 1s and 2s removed, only 3 remains
        assertEquals("All occurrences of 1 and 2 should be removed", 1, result.size());
        assertTrue("Result should contain only 3", result.contains(3));
        assertFalse("Result should not contain 1", result.contains(1));
        assertFalse("Result should not contain 2", result.contains(2));
    }

    @Test(timeout = 4000)
    public void testRemoveAllAllElementsRemoved() {
        Collection collection = new ArrayList(Arrays.asList(1, 1, 1));
        Collection remove = new ArrayList(Arrays.asList(1));
        Collection result = CollectionUtils.removeAll(collection, remove);
        assertTrue("All elements should be removed", result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testRemoveAllNoOverlap() {
        Collection collection = new ArrayList(Arrays.asList(1, 2, 3));
        Collection remove = new ArrayList(Arrays.asList(4, 5));
        Collection result = CollectionUtils.removeAll(collection, remove);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList(1, 2, 3)));
    }

    @Test(timeout = 4000)
    public void testRemoveAllWithDuplicatesInRemove() {
        Collection collection = new ArrayList(Arrays.asList(1, 2, 3));
        Collection remove = new ArrayList(Arrays.asList(1, 1, 2, 2));
        Collection result = CollectionUtils.removeAll(collection, remove);
        assertEquals(1, result.size());
        assertTrue(result.contains(3));
    }

    @Test(timeout = 4000)
    public void testRemoveAllEmptyCollection() {
        Collection result = CollectionUtils.removeAll(new ArrayList(), new ArrayList(Arrays.asList(1)));
        assertTrue(result.isEmpty());
    }

    @Test(timeout = 4000)
    public void testRemoveAllEmptyRemove() {
        Collection collection = new ArrayList(Arrays.asList(1, 2));
        Collection result = CollectionUtils.removeAll(collection, new ArrayList());
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList(1, 2)));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testRemoveAllNullCollection() {
        try {
            CollectionUtils.removeAll(null, new ArrayList());
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRemoveAllNullRemove() {
        try {
            CollectionUtils.removeAll(new ArrayList(), null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIndexOutOfBoundsArray() {
        try {
            CollectionUtils.index(new Object[] {1, 2}, 2);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIndexOutOfBoundsIterator() {
        try {
            CollectionUtils.index(Arrays.asList(1).iterator(), 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIndexOutOfBoundsEnumeration() {
        try {
            CollectionUtils.index(new Vector(Arrays.asList(1)).elements(), 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIndexOutOfBoundsCollection() {
        try {
            CollectionUtils.index(new ArrayList(Arrays.asList(1)), 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIndexOutOfBoundsMap() {
        Map map = new HashMap();
        map.put("a", 1);
        try {
            CollectionUtils.index(map, 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEMPTY_COLLECTION() {
        assertNotNull(CollectionUtils.EMPTY_COLLECTION);
        assertTrue(CollectionUtils.EMPTY_COLLECTION.isEmpty());
        try {
            CollectionUtils.EMPTY_COLLECTION.add("element");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructor() {
        // Constructor is public, should not throw
        new CollectionUtils();
    }

    @Test(timeout = 4000)
    public void testRetainAllResultIsNewCollection() {
        Collection collection = new ArrayList(Arrays.asList(1, 2, 3));
        Collection retain = new ArrayList(Arrays.asList(2));
        Collection result = CollectionUtils.retainAll(collection, retain);
        assertNotSame("Result should be a new collection", collection, result);
        assertEquals(1, result.size());
        assertTrue(result.contains(2));
    }

    @Test(timeout = 4000)
    public void testRemoveAllResultIsNewCollection() {
        Collection collection = new ArrayList(Arrays.asList(1, 2, 3));
        Collection remove = new ArrayList(Arrays.asList(2));
        Collection result = CollectionUtils.removeAll(collection, remove);
        assertNotSame("Result should be a new collection", collection, result);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList(1, 3)));
    }

    @Test(timeout = 4000)
    public void testUnionResultIsNewCollection() {
        Collection a = new ArrayList(Arrays.asList(1));
        Collection b = new ArrayList(Arrays.asList(2));
        Collection result = CollectionUtils.union(a, b);
        assertNotSame(a, result);
        assertNotSame(b, result);
        assertEquals(2, result.size());
    }

    @Test(timeout = 4000)
    public void testIntersectionResultIsNewCollection() {
        Collection a = new ArrayList(Arrays.asList(1, 2));
        Collection b = new ArrayList(Arrays.asList(2, 3));
        Collection result = CollectionUtils.intersection(a, b);
        assertNotSame(a, result);
        assertNotSame(b, result);
        assertEquals(1, result.size());
        assertTrue(result.contains(2));
    }

    @Test(timeout = 4000)
    public void testDisjunctionResultIsNewCollection() {
        Collection a = new ArrayList(Arrays.asList(1, 2));
        Collection b = new ArrayList(Arrays.asList(2, 3));
        Collection result = CollectionUtils.disjunction(a, b);
        assertNotSame(a, result);
        assertNotSame(b, result);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList(1, 3)));
    }

    @Test(timeout = 4000)
    public void testSubtractResultIsNewCollection() {
        Collection a = new ArrayList(Arrays.asList(1, 2, 3));
        Collection b = new ArrayList(Arrays.asList(2));
        Collection result = CollectionUtils.subtract(a, b);
        assertNotSame(a, result);
        assertNotSame(b, result);
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList(1, 3)));
    }

    @Test(timeout = 4000)
    public void testSelectResultIsNewCollection() {
        Collection input = new ArrayList(Arrays.asList(1, 2, 3));
        Collection result = CollectionUtils.select(input, new Predicate() {
            public boolean evaluate(Object obj) {
                return true;
            }
        });
        assertNotSame(input, result);
        assertEquals(3, result.size());
    }

    @Test(timeout = 4000)
    public void testSelectRejectedResultIsNewCollection() {
        Collection input = new ArrayList(Arrays.asList(1, 2, 3));
        Collection result = CollectionUtils.selectRejected(input, new Predicate() {
            public boolean evaluate(Object obj) {
                return false;
            }
        });
        assertNotSame(input, result);
        assertEquals(3, result.size());
    }

    @Test(timeout = 4000)
    public void testCollectResultIsNewCollection() {
        Collection input = new ArrayList(Arrays.asList(1, 2, 3));
        Collection result = CollectionUtils.collect(input, new Transformer() {
            public Object transform(Object input) {
                return input;
            }
        });
        assertNotSame(input, result);
        assertEquals(3, result.size());
    }

    @Test(timeout = 4000)
    public void testIsEqualCollectionWithSets() {
        Set a = new HashSet(Arrays.asList(1, 2, 3));
        Set b = new HashSet(Arrays.asList(3, 2, 1));
        assertTrue(CollectionUtils.isEqualCollection(a, b));
    }

    @Test(timeout = 4000)
    public void testIsEqualCollectionWithDifferentTypes() {
        Collection a = new ArrayList(Arrays.asList(1, 2, 3));
        Collection b = new HashSet(Arrays.asList(1, 2, 3));
        assertTrue(CollectionUtils.isEqualCollection(a, b));
    }

    @Test(timeout = 4000)
    public void testGetCardinalityMapWithNullElements() {
        Collection coll = new ArrayList();
        coll.add(null);
        coll.add(null);
        coll.add("a");
        Map map = CollectionUtils.getCardinalityMap(coll);
        assertEquals(2, map.size());
        assertEquals(2, map.get(null));
        assertEquals(1, map.get("a"));
    }

    @Test(timeout = 4000)
    public void testContainsAnyWithNullElements() {
        Collection a = new ArrayList();
        a.add(null);
        Collection b = new ArrayList();
        b.add(null);
        assertTrue(CollectionUtils.containsAny(a, b));
    }

    @Test(timeout = 4000)
    public void testIsSubCollectionWithNullElements() {
        Collection a = new ArrayList();
        a.add(null);
        a.add(null);
        Collection b = new ArrayList();
        b.add(null);
        b.add(null);
        b.add("x");
        assertTrue(CollectionUtils.isSubCollection(a, b));
    }

    @Test(timeout = 4000)
    public void testIsProperSubCollectionWithNullElements() {
        Collection a = new ArrayList();
        a.add(null);
        Collection b = new ArrayList();
        b.add(null);
        b.add(null);
        assertTrue(CollectionUtils.isProperSubCollection(a, b));
    }

    @Test(timeout = 4000)
    public void testIsEqualCollectionWithNullElements() {
        Collection a = new ArrayList();
        a.add(null);
        a.add(null);
        Collection b = new ArrayList();
        b.add(null);
        b.add(null);
        assertTrue(CollectionUtils.isEqualCollection(a, b));
    }

    @Test(timeout = 4000)
    public void testFindWithNullElements() {
        Collection coll = new ArrayList();
        coll.add(null);
        coll.add("a");
        Object result = CollectionUtils.find(coll, new Predicate() {
            public boolean evaluate(Object obj) {
                return obj == null;
            }
        });
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testExistsWithNullElements() {
        Collection coll = new ArrayList();
        coll.add(null);
        assertTrue(CollectionUtils.exists(coll, new Predicate() {
            public boolean evaluate(Object obj) {
                return obj == null;
            }
        }));
    }

    @Test(timeout = 4000)
    public void testSelectWithNullElements() {
        Collection input = new ArrayList();
        input.add(null);
        input.add("a");
        Collection result = CollectionUtils.select(input, new Predicate() {
            public boolean evaluate(Object obj) {
                return obj == null;
            }
        });
        assertEquals(1, result.size());
        assertTrue(result.contains(null));
    }

    @Test(timeout = 4000)
    public void testSelectRejectedWithNullElements() {
        Collection input = new ArrayList();
        input.add(null);
        input.add("a");
        Collection result = CollectionUtils.selectRejected(input, new Predicate() {
            public boolean evaluate(Object obj) {
                return obj == null;
            }
        });
        assertEquals(1, result.size());
        assertTrue(result.contains("a"));
    }

    @Test(timeout = 4000)
    public void testCollectWithNullElements() {
        Collection input = new ArrayList();
        input.add(null);
        input.add("a");
        Collection result = CollectionUtils.collect(input, new Transformer() {
            public Object transform(Object input) {
                return input;
            }
        });
        assertEquals(2, result.size());
        assertTrue(result.contains(null));
        assertTrue(result.contains("a"));
    }

    @Test(timeout = 4000)
    public void testAddIgnoreNullWithNullCollection() {
        try {
            CollectionUtils.addIgnoreNull(null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testRetainAllWithNullElements() {
        Collection collection = new ArrayList();
        collection.add(null);
        collection.add(null);
        collection.add("a");
        Collection retain = new ArrayList();
        retain.add(null);
        Collection result = CollectionUtils.retainAll(collection, retain);
        assertEquals(2, result.size());
        assertTrue(result.contains(null));
        assertFalse(result.contains("a"));
    }

    @Test(timeout = 4000)
    public void testRemoveAllWithNullElements() {
        Collection collection = new ArrayList();
        collection.add(null);
        collection.add(null);
        collection.add("a");
        Collection remove = new ArrayList();
        remove.add(null);
        Collection result = CollectionUtils.removeAll(collection, remove);
        assertEquals(1, result.size());
        assertTrue(result.contains("a"));
        assertFalse(result.contains(null));
    }
}