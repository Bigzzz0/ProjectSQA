package com.google.gson.internal;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Target Class: com.google.gson.internal.UnsafeAllocator
 * Target Methods: create(), newInstance(Class<T>)
 *
 * Known Defects:
 * - UnsafeAllocatorInstantiationTest::testInterfaceInstantiation
 * - UnsafeAllocatorInstantiationTest::testAbstractClassInstantiation
 *
 * Defect Description:
 * Attempting to allocate an interface or abstract class via UnsafeAllocator must throw an
 * UnsupportedOperationException. In the defective implementation, assertInstantiable() validation
 * is omitted, causing sun.misc.Unsafe.allocateInstance() to be executed, which throws an
 * InvocationTargetException wrapping java.lang.InstantiationException instead of the contractually
 * specified UnsupportedOperationException.
 *
 * Branch & Equivalence Partitions:
 * - Partition A: Core Functional Logic (Normal instantiation without constructor execution)
 * - Partition B: Boundary Value Analysis (Null class, primitives, array types)
 * - Partition C: Defect-Targeted Zone (Interfaces and abstract classes -> UnsupportedOperationException)
 * - Partition D: Fallback / Subclass Integrity (Direct UnsafeAllocator contract compliance)
 * - Partition E: Object Hierarchy & Field Initialization Verification
 * -------------------------------------------------------------------------------------------------------
 */
public class UnsafeAllocatorGeminiTest {

  // Sample interface to trigger interface instantiation defect
  private interface EmptyInterface {
    void execute();
  }

  // Sample abstract class to trigger abstract class instantiation defect
  private static abstract class AbstractBaseClass {
    abstract int calculate();
  }

  // Sample standard class with fields
  private static class RegularClass {
    final int number = 100;
    final String text = "initial";
  }

  // Sample class whose constructor throws an exception if invoked
  private static class ThrowingConstructorClass {
    public ThrowingConstructorClass() {
      throw new AssertionError("Constructor execution was NOT bypassed!");
    }
  }

  // Sample class with only a private constructor
  private static class PrivateConstructorClass {
    private final String state;

    private PrivateConstructorClass() {
      this.state = "constructed";
    }

    public String getState() {
      return state;
    }
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCreateReturnsNonNullAllocator() {
    UnsafeAllocator allocator = UnsafeAllocator.create();
    assertNotNull("UnsafeAllocator.create() must return a non-null instance", allocator);
  }

  @Test(timeout = 4000)
  public void testInstantiateStandardClassBypassesConstructor() throws Exception {
    UnsafeAllocator allocator = UnsafeAllocator.create();
    RegularClass instance = allocator.newInstance(RegularClass.class);

    assertNotNull("Allocated instance must not be null", instance);
    assertTrue("Allocated instance must be of type RegularClass", instance instanceof RegularClass);
    // Because constructors are bypassed, initializers in bytecode (<init>) do not run
    assertEquals("Primitive field must default to 0 without constructor", 0, instance.number);
    assertNull("Reference field must default to null without constructor", instance.text);
  }

  @Test(timeout = 4000)
  public void testInstantiateClassWithThrowingConstructor() throws Exception {
    UnsafeAllocator allocator = UnsafeAllocator.create();
    // Must succeed without throwing the AssertionError from the constructor
    ThrowingConstructorClass instance = allocator.newInstance(ThrowingConstructorClass.class);
    assertNotNull("Allocated instance must not be null", instance);
  }

  @Test(timeout = 4000)
  public void testInstantiateClassWithPrivateConstructor() throws Exception {
    UnsafeAllocator allocator = UnsafeAllocator.create();
    PrivateConstructorClass instance = allocator.newInstance(PrivateConstructorClass.class);

    assertNotNull("Allocated instance must not be null", instance);
    assertNull("Constructor initialization was bypassed, state must be null", instance.getState());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testNewInstanceWithNullClassThrowsException() {
    UnsafeAllocator allocator = UnsafeAllocator.create();
    try {
      allocator.newInstance(null);
      fail("Expected exception when passing null class to newInstance");
    } catch (Exception expected) {
      assertNotNull("Expected exception instance", expected);
    }
  }

  @Test(timeout = 4000)
  public void testNewInstanceWithPrimitiveTypeThrowsException() {
    UnsafeAllocator allocator = UnsafeAllocator.create();
    try {
      allocator.newInstance(int.class);
      fail("Expected exception when attempting to allocate primitive type");
    } catch (Exception expected) {
      assertNotNull("Expected exception instance", expected);
    }
  }

  @Test(timeout = 4000)
  public void testNewInstanceWithArrayTypeThrowsException() {
    UnsafeAllocator allocator = UnsafeAllocator.create();
    try {
      allocator.newInstance(String[].class);
      fail("Expected exception when attempting to allocate array class");
    } catch (Exception expected) {
      assertNotNull("Expected exception instance", expected);
    }
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testInterfaceInstantiationThrowsUnsupportedOperationException() {
    UnsafeAllocator allocator = UnsafeAllocator.create();
    try {
      allocator.newInstance(EmptyInterface.class);
      fail("Expected UnsupportedOperationException when instantiating interface");
    } catch (UnsupportedOperationException expected) {
      // Correct expected behavior
      assertNotNull(expected);
    } catch (Exception actual) {
      fail("Expected UnsupportedOperationException but caught: " + actual.getClass().getName());
    }
  }

  @Test(timeout = 4000)
  public void testAbstractClassInstantiationThrowsUnsupportedOperationException() {
    UnsafeAllocator allocator = UnsafeAllocator.create();
    try {
      allocator.newInstance(AbstractBaseClass.class);
      fail("Expected UnsupportedOperationException when instantiating abstract class");
    } catch (UnsupportedOperationException expected) {
      // Correct expected behavior
      assertNotNull(expected);
    } catch (Exception actual) {
      fail("Expected UnsupportedOperationException but caught: " + actual.getClass().getName());
    }
  }

  // =========================================================================
  // Partition D: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testSubclassContractIntegrity() throws Exception {
    UnsafeAllocator customAllocator = new UnsafeAllocator() {
      @Override
      public <T> T newInstance(Class<T> c) {
        return null;
      }
    };

    assertNull("Custom UnsafeAllocator implementation can return null", customAllocator.newInstance(String.class));
  }
}