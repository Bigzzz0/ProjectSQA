package org.apache.commons.collections.map;

import static org.junit.Assert.*;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.FunctorException;
import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.iterators.EmptyIterator;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------
 * Method                 | Branch / Boundary Condition Target          | Defect / Bug Reference
 * ---------------------------------------------------------------------------------------------------
 * put(K, V)              | key not present (coll == null)              | DEFECT: result reset to false,
 *                        | coll.size() > 0 branch resets result = false| returning null instead of V
 * putAll(K, Coll)        | key not present (coll == null)              | DEFECT: result reset to false,
 *                        | coll.size() > 0 branch resets result = false| returning false instead of true
 * put(K, V)              | key already present (coll != null)          | Normal branch (returns V or null)
 * putAll(K, Coll)        | null or empty collection                    | Early return false
 * putAll(Map)            | map is MultiMap vs normal Map               | Branch instanceof MultiMap
 * removeMapping(K, V)    | key absent, value absent, last val (empty)  | Removal triggers map.remove(key)
 * containsValue(V)       | empty map, present value, absent value      | Iterates all inner collections
 * containsValue(K, V)    | key absent, value present, value absent     | Target collection contains check
 * iterator(K)            | key absent vs present                       | EmptyIterator vs ValuesIterator
 * ValuesIterator.remove  | removing until coll empty vs non-empty      | Coll empty removes key from map
 * Values collection view | iterator, size, clear                       | IteratorChain across keys
 * ReflectionFactory      | valid class vs non-instantiable class       | Wraps Exception in FunctorException
 * Constructor guard      | factory == null                             | IllegalArgumentException
 * ---------------------------------------------------------------------------------------------------
 */
public class MultiValueMapGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Triggers)
    // =========================================================================

    /**
     * Targets Defect: put(K, V) on a new key incorrectly returned null instead of the added value
     * because of `result = false;` in the `if (coll.size() > 0)` block.
     */
    @Test(timeout = 4000)
    public void testDefect_PutOnNewKeyReturnsValue() {
        MultiValueMap map = new MultiValueMap();
        Object result = map.put("keyA", "valueA");
        assertEquals("put() on a new key must return the inserted value, not null", "valueA", result);
        assertEquals(1, map.totalSize());
        assertEquals(1, map.size("keyA"));
    }

    /**
     * Targets Defect: put(K, V) with Set backing collection on a new key must return the value.
     */
    @Test(timeout = 4000)
    public void testDefect_PutWithSetReturnsValue() {
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), HashSet.class);
        Object result = map.put("key1", "val1");
        assertEquals("put() on a new key using HashSet backing must return the inserted value", "val1", result);
    }

    /**
     * Targets Defect: putAll(K, Collection) on a new key incorrectly returned false instead of true
     * because `result` was overwritten with `false`.
     */
    @Test(timeout = 4000)
    public void testDefect_PutAllKeyCollectionReturnsTrueOnNewKey() {
        MultiValueMap map = new MultiValueMap();
        Collection values = Arrays.asList("alpha", "beta");
        boolean modified = map.putAll("keyA", values);
        assertTrue("putAll(K, Collection) on an empty key must return true when entries are added", modified);
        assertEquals(2, map.totalSize());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndPutExistingKey() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        Object secondPut = map.put("k1", "v2");
        assertEquals("v2", secondPut);

        Collection coll = map.getCollection("k1");
        assertNotNull(coll);
        assertEquals(2, coll.size());
        assertTrue(coll.contains("v1"));
        assertTrue(coll.contains("v2"));
    }

    @Test(timeout = 4000)
    public void testPutDuplicateInSetBackingReturnsNull() {
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), HashSet.class);
        map.put("k1", "v1");
        Object duplicatePut = map.put("k1", "v1");
        assertNull("Adding duplicate value to a Set-backed MultiValueMap must return null", duplicatePut);
        assertEquals(1, map.size("k1"));
    }

    @Test(timeout = 4000)
    public void testPutAllKeyCollectionExistingKey() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");

        boolean modified = map.putAll("k1", Arrays.asList("v2", "v3"));
        assertTrue(modified);
        assertEquals(3, map.size("k1"));
        assertEquals(3, map.totalSize());
    }

    @Test(timeout = 4000)
    public void testRemoveMappingSuccessAndRemovalOfLastValue() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        map.put("k1", "v2");

        Object removed = map.removeMapping("k1", "v1");
        assertEquals("v1", removed);
        assertEquals(1, map.size("k1"));
        assertTrue(map.containsKey("k1"));

        Object removedLast = map.removeMapping("k1", "v2");
        assertEquals("v2", removedLast);
        assertNull("Key must be removed when last mapping is deleted", map.getCollection("k1"));
        assertFalse(map.containsKey("k1"));
        assertEquals(0, map.size("k1"));
    }

    @Test(timeout = 4000)
    public void testRemoveMappingNonExistentKeyOrValue() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");

        assertNull(map.removeMapping("kMissing", "v1"));
        assertNull(map.removeMapping("k1", "vMissing"));
        assertEquals(1, map.size("k1"));
    }

    @Test(timeout = 4000)
    public void testContainsValueGlobal() {
        MultiValueMap map = new MultiValueMap();
        assertFalse(map.containsValue("v1"));

        map.put("k1", "v1");
        map.put("k2", "v2");

        assertTrue(map.containsValue("v1"));
        assertTrue(map.containsValue("v2"));
        assertFalse(map.containsValue("v3"));
    }

    @Test(timeout = 4000)
    public void testContainsValueWithKey() {
        MultiValueMap map = new MultiValueMap();
        assertFalse(map.containsValue("k1", "v1"));

        map.put("k1", "v1");
        assertTrue(map.containsValue("k1", "v1"));
        assertFalse(map.containsValue("k1", "v2"));
        assertFalse(map.containsValue("kMissing", "v1"));
    }

    @Test(timeout = 4000)
    public void testPutAllFromStandardMap() {
        MultiValueMap map = new MultiValueMap();
        Map standardMap = new HashMap();
        standardMap.put("k1", "v1");
        standardMap.put("k2", "v2");

        map.putAll(standardMap);
        assertEquals(2, map.totalSize());
        assertTrue(map.containsValue("k1", "v1"));
        assertTrue(map.containsValue("k2", "v2"));
    }

    @Test(timeout = 4000)
    public void testPutAllFromMultiMap() {
        MultiValueMap src = new MultiValueMap();
        src.put("k1", "v1");
        src.put("k1", "v2");
        src.put("k2", "v3");

        MultiValueMap dest = new MultiValueMap();
        dest.putAll(src);

        assertEquals(3, dest.totalSize());
        assertEquals(2, dest.size("k1"));
        assertEquals(1, dest.size("k2"));
        assertTrue(dest.containsValue("k1", "v1"));
        assertTrue(dest.containsValue("k1", "v2"));
        assertTrue(dest.containsValue("k2", "v3"));
    }

    @Test(timeout = 4000)
    public void testClear() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        map.put("k2", "v2");
        assertEquals(2, map.totalSize());

        map.clear();
        assertEquals(0, map.totalSize());
        assertEquals(0, map.size());
        assertNull(map.getCollection("k1"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testPutAllEmptyOrNullCollection() {
        MultiValueMap map = new MultiValueMap();
        assertFalse(map.putAll("k1", null));
        assertFalse(map.putAll("k1", Collections.emptyList()));
        assertEquals(0, map.totalSize());
        assertFalse(map.containsKey("k1"));
    }

    @Test(timeout = 4000)
    public void testSizeBoundaries() {
        MultiValueMap map = new MultiValueMap();
        assertEquals(0, map.size("nonExistentKey"));
        assertEquals(0, map.totalSize());

        map.put("nullKey", null);
        assertEquals(1, map.size("nullKey"));
        assertEquals(1, map.totalSize());
        assertTrue(map.containsValue("nullKey", null));
        assertTrue(map.containsValue(null));
    }

    @Test(timeout = 4000)
    public void testIteratorKeyPresentAndAbsent() {
        MultiValueMap map = new MultiValueMap();
        Iterator emptyIt = map.iterator("nonExistent");
        assertSame(EmptyIterator.INSTANCE, emptyIt);
        assertFalse(emptyIt.hasNext());

        map.put("k1", "v1");
        map.put("k1", "v2");
        Iterator it = map.iterator("k1");
        assertTrue(it.hasNext());
        assertEquals("v1", it.next());
        assertTrue(it.hasNext());
        assertEquals("v2", it.next());
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000)
    public void testValuesIteratorRemoveCollapsesKey() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        map.put("k1", "v2");

        Iterator it = map.iterator("k1");
        assertEquals("v1", it.next());
        it.remove();
        assertEquals(1, map.size("k1"));
        assertTrue(map.containsKey("k1"));

        assertEquals("v2", it.next());
        it.remove();
        assertFalse(map.containsKey("k1"));
        assertNull(map.getCollection("k1"));
        assertEquals(0, map.totalSize());
    }

    @Test(timeout = 4000)
    public void testValuesCollectionView() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        map.put("k2", "v2");
        map.put("k2", "v3");

        Collection vals = map.values();
        assertNotNull(vals);
        // Repeated call returns cached view
        assertSame(vals, map.values());

        assertEquals(3, vals.size());

        Set collected = new HashSet();
        for (Iterator it = vals.iterator(); it.hasNext();) {
            collected.add(it.next());
        }
        assertEquals(3, collected.size());
        assertTrue(collected.contains("v1"));
        assertTrue(collected.contains("v2"));
        assertTrue(collected.contains("v3"));

        vals.clear();
        assertEquals(0, map.totalSize());
        assertEquals(0, map.size());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNullFactoryThrowsException() {
        new MultiValueMap(new HashMap(), null);
    }

    @Test(expected = FunctorException.class, timeout = 4000)
    public void testReflectionFactoryCannotInstantiateThrowsFunctorException() {
        // Abstract class cannot be instantiated by ReflectionFactory.create()
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), AbstractCollection.class);
        map.put("k1", "v1");
    }

    @Test(expected = NoSuchElementException.class, timeout = 4000)
    public void testEmptyIteratorNextThrowsException() {
        MultiValueMap map = new MultiValueMap();
        Iterator it = map.iterator("missing");
        it.next();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testValuesIteratorRemoveWithoutNextThrowsException() {
        MultiValueMap map = new MultiValueMap();
        map.put("k1", "v1");
        Iterator it = map.iterator("k1");
        it.remove();
    }

    // =========================================================================
    // Partition E: Decorator Factories & Custom Factory Support
    // =========================================================================

    @Test(timeout = 4000)
    public void testDecorateWithCustomFactory() {
        Factory customFactory = new Factory() {
            public Object create() {
                return new ArrayList();
            }
        };
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), customFactory);
        map.put("k1", "v1");
        assertEquals(1, map.size("k1"));
        assertTrue(map.getCollection("k1") instanceof ArrayList);
    }

    @Test(timeout = 4000)
    public void testDecorateWithMapOnlyUsesArrayList() {
        MultiValueMap map = MultiValueMap.decorate(new HashMap());
        map.put("k1", "v1");
        assertTrue(map.getCollection("k1") instanceof ArrayList);
    }

    @Test(timeout = 4000)
    public void testDecorateWithMapAndClass() {
        MultiValueMap map = MultiValueMap.decorate(new HashMap(), HashSet.class);
        map.put("k1", "v1");
        assertTrue(map.getCollection("k1") instanceof HashSet);
    }
}