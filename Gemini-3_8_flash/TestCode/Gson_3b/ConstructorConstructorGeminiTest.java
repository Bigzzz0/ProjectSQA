/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.gson.internal.ConstructorConstructor
 * 
 * Branch Coverage Targets:
 * 1. InstanceCreator Match:
 *    - typeCreator != null (exact Type match)
 *    - rawTypeCreator != null (fallback raw Class match)
 * 2. Default Constructor Path:
 *    - Default public no-arg constructor
 *    - Private no-arg constructor (accessible flag toggled)
 *    - InstantiationException handling (abstract class with constructor)
 *    - InvocationTargetException handling (constructor throwing exception)
 *    - NoSuchMethodException branch (no default constructor available)
 * 3. Default Implementation Path:
 *    - Collection hierarchy:
 *      * SortedSet -> TreeSet
 *      * EnumSet: ParameterizedType with Class arg -> EnumSet.noneOf
 *      * EnumSet: ParameterizedType with non-Class arg (wildcard) -> JsonIOException
 *      * EnumSet: Non-ParameterizedType (raw EnumSet) -> JsonIOException
 *      * Set -> LinkedHashSet
 *      * Queue -> LinkedList
 *      * List/Collection -> ArrayList
 *    - Map hierarchy:
 *      * SortedMap -> TreeMap
 *      * ParameterizedType with non-String key -> LinkedHashMap
 *      * ParameterizedType with String key / Raw Map -> LinkedTreeMap
 * 4. Unsafe Allocator Fallback:
 *    - Normal unsafe instantiation (classes without no-arg constructors)
 *    - Unsafe instantiation failure / interface invocation
 * 5. toString():
 *    - Returns string representation of instanceCreators map
 * 
 * Defects4J Ground Truth Regression Targets:
 * - ConcurrentMap instantiation: Returns LinkedTreeMap/LinkedHashMap which fails ClassCastException
 * - ConcurrentNavigableMap instantiation: Returns TreeMap which fails ClassCastException
 */

package com.google.gson.internal;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.Assert.*;

public class ConstructorConstructorGeminiTest {

  private enum TestEnum {
    ALPHA, BETA
  }

  private static class ClassWithPrivateConstructor {
    private final String value;
    private ClassWithPrivateConstructor() {
      this.value = "private_success";
    }
  }

  private static abstract class AbstractClassWithConstructor {
    public AbstractClassWithConstructor() {}
  }

  private static class ThrowingConstructorClass {
    public ThrowingConstructorClass() {
      throw new IllegalStateException("Intentional constructor failure");
    }
  }

  private static class ClassWithoutNoArgConstructor {
    private final int value;
    public ClassWithoutNoArgConstructor(int value) {
      this.value = value;
    }
  }

  private interface UnsupportedInterface {
    void execute();
  }

  /* =========================================================================
   * Partition A: Core Functional Logic & State Transitions
   * ========================================================================= */

  @Test(timeout = 4000)
  public void testExactTypeInstanceCreator() {
    Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
    final String expectedValue = "custom_instance";
    TypeToken<String> token = TypeToken.get(String.class);
    creators.put(token.getType(), new InstanceCreator<String>() {
      @Override
      public String createInstance(Type type) {
        return expectedValue;
      }
    });

    ConstructorConstructor cc = new ConstructorConstructor(creators);
    ObjectConstructor<String> constructor = cc.get(token);
    assertNotNull(constructor);
    assertEquals(expectedValue, constructor.construct());
  }

  @Test(timeout = 4000)
  public void testRawTypeInstanceCreatorFallback() {
    Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
    creators.put(List.class, new InstanceCreator<List<?>>() {
      @Override
      public List<?> createInstance(Type type) {
        List<Object> list = new ArrayList<Object>();
        list.add("raw_creator");
        return list;
      }
    });

    ConstructorConstructor cc = new ConstructorConstructor(creators);
    TypeToken<List<String>> token = new TypeToken<List<String>>() {};
    ObjectConstructor<List<String>> constructor = cc.get(token);
    assertNotNull(constructor);

    List<String> constructed = constructor.construct();
    assertEquals(1, constructed.size());
    assertEquals("raw_creator", constructed.get(0));
  }

  @Test(timeout = 4000)
  public void testDefaultPublicConstructor() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<ArrayList> token = TypeToken.get(ArrayList.class);
    ObjectConstructor<ArrayList> constructor = cc.get(token);
    assertNotNull(constructor);

    ArrayList constructed = constructor.construct();
    assertNotNull(constructed);
    assertTrue(constructed.isEmpty());
  }

  @Test(timeout = 4000)
  public void testDefaultPrivateConstructor() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<ClassWithPrivateConstructor> token = TypeToken.get(ClassWithPrivateConstructor.class);
    ObjectConstructor<ClassWithPrivateConstructor> constructor = cc.get(token);
    assertNotNull(constructor);

    ClassWithPrivateConstructor constructed = constructor.construct();
    assertNotNull(constructed);
    assertEquals("private_success", constructed.value);
  }

  @Test(timeout = 4000)
  public void testDefaultImplementationCollections() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());

    // SortedSet -> TreeSet
    ObjectConstructor<SortedSet<String>> sortedSetCons = cc.get(new TypeToken<SortedSet<String>>() {});
    SortedSet<String> sortedSet = sortedSetCons.construct();
    assertTrue(sortedSet instanceof TreeSet);

    // Set -> LinkedHashSet
    ObjectConstructor<Set<String>> setCons = cc.get(new TypeToken<Set<String>>() {});
    Set<String> set = setCons.construct();
    assertTrue(set instanceof LinkedHashSet);

    // Queue -> LinkedList
    ObjectConstructor<Queue<String>> queueCons = cc.get(new TypeToken<Queue<String>>() {});
    Queue<String> queue = queueCons.construct();
    assertTrue(queue instanceof LinkedList);

    // Collection / List -> ArrayList
    ObjectConstructor<Collection<String>> collectionCons = cc.get(new TypeToken<Collection<String>>() {});
    Collection<String> collection = collectionCons.construct();
    assertTrue(collection instanceof ArrayList);

    ObjectConstructor<List<String>> listCons = cc.get(new TypeToken<List<String>>() {});
    List<String> list = listCons.construct();
    assertTrue(list instanceof ArrayList);
  }

  @Test(timeout = 4000)
  public void testDefaultImplementationMaps() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());

    // SortedMap -> TreeMap
    ObjectConstructor<SortedMap<String, Object>> sortedMapCons =
        cc.get(new TypeToken<SortedMap<String, Object>>() {});
    SortedMap<String, Object> sortedMap = sortedMapCons.construct();
    assertTrue(sortedMap instanceof TreeMap);

    // Map with non-String key -> LinkedHashMap
    ObjectConstructor<Map<Integer, Object>> nonStringKeyMapCons =
        cc.get(new TypeToken<Map<Integer, Object>>() {});
    Map<Integer, Object> nonStringKeyMap = nonStringKeyMapCons.construct();
    assertTrue(nonStringKeyMap instanceof LinkedHashMap);

    // Map with String key -> LinkedTreeMap
    ObjectConstructor<Map<String, Object>> stringKeyMapCons =
        cc.get(new TypeToken<Map<String, Object>>() {});
    Map<String, Object> stringKeyMap = stringKeyMapCons.construct();
    assertTrue(stringKeyMap instanceof LinkedTreeMap);

    // Raw Map -> LinkedTreeMap
    ObjectConstructor<Map> rawMapCons = cc.get(TypeToken.get(Map.class));
    Map rawMap = rawMapCons.construct();
    assertTrue(rawMap instanceof LinkedTreeMap);
  }

  @Test(timeout = 4000)
  public void testValidEnumSetCreation() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<EnumSet<TestEnum>> token = new TypeToken<EnumSet<TestEnum>>() {};
    ObjectConstructor<EnumSet<TestEnum>> constructor = cc.get(token);
    assertNotNull(constructor);

    EnumSet<TestEnum> set = constructor.construct();
    assertNotNull(set);
    assertTrue(set.isEmpty());
    set.add(TestEnum.ALPHA);
    assertTrue(set.contains(TestEnum.ALPHA));
  }

  @Test(timeout = 4000)
  public void testUnsafeAllocatorFallback() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<ClassWithoutNoArgConstructor> token = TypeToken.get(ClassWithoutNoArgConstructor.class);
    ObjectConstructor<ClassWithoutNoArgConstructor> constructor = cc.get(token);
    assertNotNull(constructor);

    ClassWithoutNoArgConstructor instance = constructor.construct();
    assertNotNull(instance);
    assertEquals(0, instance.value);
  }

  @Test(timeout = 4000)
  public void testToStringRepresentation() {
    Map<Type, InstanceCreator<?>> creators = new HashMap<Type, InstanceCreator<?>>();
    ConstructorConstructor cc = new ConstructorConstructor(creators);
    assertEquals(creators.toString(), cc.toString());
  }

  /* =========================================================================
   * Partition B: Boundary Value Analysis (BVA) & Extremes
   * ========================================================================= */

  @Test(timeout = 4000)
  public void testRawEnumSetThrowsJsonIOException() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<EnumSet> token = TypeToken.get(EnumSet.class);
    ObjectConstructor<EnumSet> constructor = cc.get(token);
    assertNotNull(constructor);

    try {
      constructor.construct();
      fail("Expected JsonIOException for raw EnumSet type");
    } catch (JsonIOException expected) {
      assertTrue(expected.getMessage().contains("Invalid EnumSet type"));
    }
  }

  @Test(timeout = 4000)
  public void testWildcardEnumSetThrowsJsonIOException() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<EnumSet<? extends TestEnum>> token = new TypeToken<EnumSet<? extends TestEnum>>() {};
    ObjectConstructor<EnumSet<? extends TestEnum>> constructor = cc.get(token);
    assertNotNull(constructor);

    try {
      constructor.construct();
      fail("Expected JsonIOException for EnumSet with wildcard type parameter");
    } catch (JsonIOException expected) {
      assertTrue(expected.getMessage().contains("Invalid EnumSet type"));
    }
  }

  /* =========================================================================
   * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
   * ========================================================================= */

  /**
   * Targets defects where ConcurrentMap is treated as a general Map and returns
   * a LinkedTreeMap / LinkedHashMap which cannot be cast to ConcurrentMap.
   */
  @Test(timeout = 4000)
  public void testDefectConcurrentMapConstruction() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<ConcurrentMap<String, String>> token = new TypeToken<ConcurrentMap<String, String>>() {};
    ObjectConstructor<ConcurrentMap<String, String>> constructor = cc.get(token);
    assertNotNull("ConstructorConstructor must return an ObjectConstructor for ConcurrentMap", constructor);

    ConcurrentMap<String, String> map = constructor.construct();
    assertNotNull("Constructed ConcurrentMap must not be null", map);
    assertTrue("Constructed instance must implement ConcurrentMap", map instanceof ConcurrentMap);
  }

  /**
   * Targets defects where ConcurrentNavigableMap is treated as SortedMap and returns
   * a TreeMap which cannot be cast to ConcurrentNavigableMap.
   */
  @Test(timeout = 4000)
  public void testDefectConcurrentNavigableMapConstruction() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<ConcurrentNavigableMap<String, String>> token =
        new TypeToken<ConcurrentNavigableMap<String, String>>() {};
    ObjectConstructor<ConcurrentNavigableMap<String, String>> constructor = cc.get(token);
    assertNotNull("ConstructorConstructor must return an ObjectConstructor for ConcurrentNavigableMap", constructor);

    ConcurrentNavigableMap<String, String> map = constructor.construct();
    assertNotNull("Constructed ConcurrentNavigableMap must not be null", map);
    assertTrue("Constructed instance must implement ConcurrentNavigableMap",
        map instanceof ConcurrentNavigableMap);
  }

  /* =========================================================================
   * Partition D: Exception & Defensive Guard Paths
   * ========================================================================= */

  @Test(timeout = 4000)
  public void testInstantiationExceptionOnAbstractClass() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<AbstractClassWithConstructor> token = TypeToken.get(AbstractClassWithConstructor.class);
    ObjectConstructor<AbstractClassWithConstructor> constructor = cc.get(token);
    assertNotNull(constructor);

    try {
      constructor.construct();
      fail("Expected RuntimeException wrapping InstantiationException");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("Failed to invoke"));
      assertTrue(expected.getCause() instanceof InstantiationException);
    }
  }

  @Test(timeout = 4000)
  public void testInvocationTargetExceptionOnThrowingConstructor() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<ThrowingConstructorClass> token = TypeToken.get(ThrowingConstructorClass.class);
    ObjectConstructor<ThrowingConstructorClass> constructor = cc.get(token);
    assertNotNull(constructor);

    try {
      constructor.construct();
      fail("Expected RuntimeException wrapping TargetException");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("Failed to invoke"));
      assertTrue(expected.getCause() instanceof IllegalStateException);
      assertEquals("Intentional constructor failure", expected.getCause().getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testUnsafeAllocationFailureOnInterface() {
    ConstructorConstructor cc = new ConstructorConstructor(Collections.<Type, InstanceCreator<?>>emptyMap());
    TypeToken<UnsupportedInterface> token = TypeToken.get(UnsupportedInterface.class);
    ObjectConstructor<UnsupportedInterface> constructor = cc.get(token);
    assertNotNull(constructor);

    try {
      constructor.construct();
      fail("Expected RuntimeException when unsafe allocating an unsupported interface");
    } catch (RuntimeException expected) {
      assertTrue(expected.getMessage().contains("Unable to invoke no-args constructor"));
    }
  }
}