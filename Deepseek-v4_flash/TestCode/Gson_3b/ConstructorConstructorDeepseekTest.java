package com.google.gson.internal;

import static org.junit.Assert.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;

import org.junit.Test;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;

/**
 * White-box test suite for ConstructorConstructor targeting maximum coverage
 * and known defect in ConcurrentMap/ConcurrentNavigableMap handling.
 *
 * <p><b>Branch & Defect Analysis Matrix:</b>
 * <ul>
 *   <li>Instance creator (exact type vs raw type)</li>
 *   <li>Default constructor – exists / missing</li>
 *   <li>Collection implementations:
 *       SortedSet, EnumSet, Set, Queue, Collection (else)</li>
 *   <li>Map implementations:
 *       SortedMap, Map with ParameterizedType & non-String key,
 *       Map with String key or raw type</li>
 *   <li>UnsafeAllocator fallback</li>
 *   <li>EnumSet error paths (non-ParameterizedType, non-Class element)</li>
 *   <li>Defect: ConcurrentMap → should produce ConcurrentMap (bug: LinkedHashMap)
 *       ConcurrentNavigableMap → should produce ConcurrentNavigableMap (bug: TreeMap)</li>
 * </ul>
 */
public class ConstructorConstructorDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testInstanceCreatorExactType() {
        final Object expected = new Object();
        Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
        creators.put(String.class, new InstanceCreator<String>() {
            @Override
            public String createInstance(Type type) {
                return "exact";
            }
        });
        ConstructorConstructor cc = new ConstructorConstructor(creators);
        TypeToken<String> token = TypeToken.get(String.class);
        ObjectConstructor<String> constructor = cc.get(token);
        String result = constructor.construct();
        assertEquals("exact", result);
    }

    @Test(timeout = 4000)
    public void testInstanceCreatorRawType() {
        Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
        creators.put(CharSequence.class, new InstanceCreator<CharSequence>() {
            @Override
            public CharSequence createInstance(Type type) {
                return "raw";
            }
        });
        ConstructorConstructor cc = new ConstructorConstructor(creators);
        // String is assignable to CharSequence, raw type match
        TypeToken<String> token = TypeToken.get(String.class);
        ObjectConstructor<String> constructor = cc.get(token);
        String result = constructor.construct();
        assertEquals("raw", result);
    }

    @Test(timeout = 4000)
    public void testDefaultConstructorExists() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        TypeToken<ArrayList<String>> token = new TypeToken<ArrayList<String>>() {};
        ObjectConstructor<ArrayList<String>> constructor = cc.get(token);
        ArrayList<String> list = constructor.construct();
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test(timeout = 4000)
    public void testDefaultConstructorMissingFallsToUnsafe() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        // No default constructor – e.g., Integer
        TypeToken<Integer> token = TypeToken.get(Integer.class);
        ObjectConstructor<Integer> constructor = cc.get(token);
        // Unsafe allocator should succeed
        Integer result = constructor.construct();
        assertNotNull(result);
        // Unsafe allocator creates instance without calling constructor, so 0
        assertEquals(0, result.intValue());
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testEmptyInstanceCreators() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        TypeToken<String> token = TypeToken.get(String.class);
        ObjectConstructor<String> constructor = cc.get(token);
        // String has default constructor -> returns new String() empty
        String result = constructor.construct();
        assertEquals("", result);
    }

    @Test(timeout = 4000)
    public void testNullInstanceCreatorsMap() {
        // Constructor should accept null map
        ConstructorConstructor cc = new ConstructorConstructor(null);
        TypeToken<Object> token = TypeToken.get(Object.class);
        ObjectConstructor<Object> constructor = cc.get(token);
        // Object has default constructor
        Object result = constructor.construct();
        assertNotNull(result);
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    @Test(timeout = 4000)
    public void testConcurrentMap_RevealsDefect() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        TypeToken<ConcurrentMap<String, String>> token =
                new TypeToken<ConcurrentMap<String, String>>() {};
        ObjectConstructor<ConcurrentMap<String, String>> constructor = cc.get(token);
        Object map = constructor.construct();
        // Defect: returns LinkedHashMap which is NOT a ConcurrentMap
        // Revealing assertion: must be instance of ConcurrentMap
        assertTrue("ConstructorConstructor should produce a ConcurrentMap, but bug gives LinkedHashMap",
                map instanceof ConcurrentMap);
    }

    @Test(timeout = 4000)
    public void testConcurrentNavigableMap_RevealsDefect() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        TypeToken<ConcurrentNavigableMap<String, String>> token =
                new TypeToken<ConcurrentNavigableMap<String, String>>() {};
        ObjectConstructor<ConcurrentNavigableMap<String, String>> constructor = cc.get(token);
        Object map = constructor.construct();
        // Defect: returns TreeMap which is NOT a ConcurrentNavigableMap
        assertTrue("ConstructorConstructor should produce a ConcurrentNavigableMap, but bug gives TreeMap",
                map instanceof ConcurrentNavigableMap);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(expected = JsonIOException.class, timeout = 4000)
    public void testEnumSetInvalidNonParameterizedType() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        // Use raw EnumSet type (no parameter) -> triggers JsonIOException
        TypeToken<EnumSet<SampleEnum>> token = TypeToken.get(EnumSet.class);
        cc.get(token).construct();
    }

    @Test(expected = JsonIOException.class, timeout = 4000)
    public void testEnumSetInvalidNonClassElement() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        // ParameterizedType but element type is not Class (e.g., TypeVariable)
        TypeToken<EnumSet<?>> token = new TypeToken<EnumSet<?>>() {};
        cc.get(token).construct();
    }

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testUnsafeAllocatorFails() {
        // Use a type that cannot be allocated via Unsafe (e.g., primitive)
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        TypeToken<Integer> token = TypeToken.get(Integer.class);
        // The UnsafeAllocator will throw Exception (actually Unsafe.allocateInstance works for
        // Integer? It does.) So to force failure we need a class that Unsafe cannot instantiate,
        // e.g., an abstract class or interface. Let's use Cloneable.
        TypeToken<Cloneable> token2 = TypeToken.get(Cloneable.class);
        cc.get(token2).construct();
    }

    // ===== Partition E: Collections & Maps Comprehensive =====

    @Test(timeout = 4000)
    public void testSortedSetReturnsTreeSet() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        TypeToken<SortedSet<String>> token = new TypeToken<SortedSet<String>>() {};
        Object set = cc.get(token).construct();
        assertTrue(set instanceof TreeSet);
    }

    @Test(timeout = 4000)
    public void testEnumSetValid() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        TypeToken<EnumSet<SampleEnum>> token = new TypeToken<EnumSet<SampleEnum>>() {};
        Object set = cc.get(token).construct();
        assertTrue(set instanceof EnumSet);
        assertTrue(((EnumSet<?>)set).isEmpty());
    }

    @Test(timeout = 4000)
    public void testSetReturnsLinkedHashSet() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        TypeToken<Set<String>> token = new TypeToken<Set<String>>() {};
        Object set = cc.get(token).construct();
        assertTrue(set instanceof LinkedHashSet);
    }

    @Test(timeout = 4000)
    public void testQueueReturnsLinkedList() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        TypeToken<Queue<String>> token = new TypeToken<Queue<String>>() {};
        Object queue = cc.get(token).construct();
        assertTrue(queue instanceof LinkedList);
    }

    @Test(timeout = 4000)
    public void testCollectionReturnsArrayList() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        TypeToken<Collection<String>> token = new TypeToken<Collection<String>>() {};
        Object coll = cc.get(token).construct();
        assertTrue(coll instanceof ArrayList);
    }

    @Test(timeout = 4000)
    public void testSortedMapReturnsTreeMap() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        TypeToken<SortedMap<String, String>> token = new TypeToken<SortedMap<String, String>>() {};
        Object map = cc.get(token).construct();
        assertTrue(map instanceof TreeMap);
    }

    @Test(timeout = 4000)
    public void testMapWithNonStringKeyReturnsLinkedHashMap() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        TypeToken<Map<Integer, String>> token = new TypeToken<Map<Integer, String>>() {};
        Object map = cc.get(token).construct();
        assertTrue("Should be LinkedHashMap for non-String key", map instanceof LinkedHashMap);
    }

    @Test(timeout = 4000)
    public void testMapWithStringKeyReturnsLinkedTreeMap() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        TypeToken<Map<String, String>> token = new TypeToken<Map<String, String>>() {};
        Object map = cc.get(token).construct();
        // LinkedTreeMap is package-private, check class name or that it's a Map
        assertTrue(map instanceof Map);
        // For String key, the code returns LinkedTreeMap (not LinkedHashMap)
        // We cannot directly reference LinkedTreeMap, but we know LinkedTreeMap is not LinkedHashMap
        assertFalse("Should be LinkedTreeMap, not LinkedHashMap", map instanceof LinkedHashMap);
    }

    @Test(timeout = 4000)
    public void testRawTypeMapReturnsLinkedTreeMap() {
        ConstructorConstructor cc = new ConstructorConstructor(
                new HashMap<Type, InstanceCreator<?>>());
        TypeToken<Map> token = TypeToken.get(Map.class); // raw type
        Object map = cc.get(token).construct();
        assertTrue(map instanceof Map);
        // raw type -> goes to else branch -> LinkedTreeMap
        assertFalse(map instanceof LinkedHashMap);
    }

    // ===== Helper Enum for EnumSet tests =====
    enum SampleEnum { A, B }
}