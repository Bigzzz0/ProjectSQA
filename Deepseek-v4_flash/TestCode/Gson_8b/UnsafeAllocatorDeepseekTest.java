package com.google.gson.internal;

import org.junit.Test;
import java.lang.reflect.InvocationTargetException;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: UnsafeAllocator.create() and newInstance(Class<T>)
 * 
 * Decision branches in create():
 *   - Branch 1: sun.misc.Unsafe available → returns Unsafe-based allocator
 *   - Branch 2: dalvikvm post-gingerbread (ObjectStreamClass) → returns that allocator
 *   - Branch 3: dalvikvm pre-gingerbread (ObjectInputStream) → returns that allocator
 *   - Branch 4: fallback → returns allocator that throws UnsupportedOperationException
 * 
 * Decision branches in newInstance() for each allocator:
 *   - Normal: concrete class → returns instance
 *   - Interface/abstract class → throws InvocationTargetException (from Unsafe) or UnsupportedOperationException (fallback)
 *   - Null class → throws NullPointerException
 *   - Primitive class → throws IllegalArgumentException or InvocationTargetException
 * 
 * Known defect: When allocating an interface or abstract class, the fallback allocator
 * throws UnsupportedOperationException instead of InvocationTargetException.
 * This test suite targets that defect with dedicated tests.
 */
public class UnsafeAllocatorDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testCreateReturnsNonNull() {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        assertNotNull("create() should return a non-null allocator", allocator);
    }

    @Test(timeout = 4000)
    public void testNewInstanceConcreteClass() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        Object instance = allocator.newInstance(Object.class);
        assertNotNull("newInstance(Object.class) should return non-null", instance);
        assertTrue("Returned object should be instance of Object", instance instanceof Object);
        assertEquals("Returned object's class should be Object", Object.class, instance.getClass());
    }

    @Test(timeout = 4000)
    public void testNewInstanceMultipleCalls() throws Exception {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        Object obj1 = allocator.newInstance(Object.class);
        Object obj2 = allocator.newInstance(Object.class);
        assertNotNull(obj1);
        assertNotNull(obj2);
        assertNotSame("Each call should create a new instance", obj1, obj2);
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testNewInstanceNullClass() {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        try {
            allocator.newInstance(null);
            fail("newInstance(null) should throw an exception");
        } catch (Exception e) {
            // Expected: NullPointerException (or any exception indicating null argument)
            assertTrue("Exception should be NullPointerException or similar",
                e instanceof NullPointerException || e instanceof IllegalArgumentException);
        }
    }

    @Test(timeout = 4000)
    public void testNewInstancePrimitiveClass() {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        try {
            allocator.newInstance(int.class);
            fail("newInstance(int.class) should throw an exception");
        } catch (Exception e) {
            // Primitive classes cannot be allocated; expect InvocationTargetException or IllegalArgumentException
            assertTrue("Exception should be InvocationTargetException or IllegalArgumentException",
                e instanceof InvocationTargetException || e instanceof IllegalArgumentException);
        }
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testInterfaceInstantiation() {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        try {
            allocator.newInstance(Runnable.class);
            fail("newInstance(Runnable.class) should throw InvocationTargetException");
        } catch (InvocationTargetException e) {
            // Expected: InvocationTargetException wrapping InstantiationException
            assertNotNull("InvocationTargetException should have a cause", e.getCause());
        } catch (Exception e) {
            // If fallback is used, it throws UnsupportedOperationException (bug)
            fail("Expected InvocationTargetException but got " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testAbstractClassInstantiation() {
        UnsafeAllocator allocator = UnsafeAllocator.create();
        try {
            allocator.newInstance(java.util.AbstractList.class);
            fail("newInstance(AbstractList.class) should throw InvocationTargetException");
        } catch (InvocationTargetException e) {
            assertNotNull("InvocationTargetException should have a cause", e.getCause());
        } catch (Exception e) {
            fail("Expected InvocationTargetException but got " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testNewInstanceWithSecurityManager() {
        // This test is a placeholder; in a real environment with a security manager,
        // reflection may be blocked and the fallback allocator is used.
        // We simply verify that create() still returns a non-null allocator.
        UnsafeAllocator allocator = UnsafeAllocator.create();
        assertNotNull(allocator);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testAllocatorIdentity() {
        UnsafeAllocator allocator1 = UnsafeAllocator.create();
        UnsafeAllocator allocator2 = UnsafeAllocator.create();
        // create() may return the same instance or different; we just check they are not null
        assertNotNull(allocator1);
        assertNotNull(allocator2);
    }
}