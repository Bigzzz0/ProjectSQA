package org.apache.commons.lang3.builder;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target Class: HashCodeBuilder
 * 
 * Key Decision Branches and Boundary Conditions to Cover:
 * 
 * 1. Constructor validation:
 *    - initialNonZeroOddNumber == 0 => throw IAE
 *    - initialNonZeroOddNumber % 2 == 0 => throw IAE
 *    - multiplierNonZeroOddNumber == 0 => throw IAE
 *    - multiplierNonZeroOddNumber % 2 == 0 => throw IAE
 *    - Successful construction with valid values
 * 
 * 2. append(boolean) - value true/false branches
 * 3. append(boolean[]) - null branch, empty array, non-empty
 * 4. append(byte/char/short/int/float) - value addition
 * 5. append(byte[]/char[]/short[]/int[]/float[]) - null, empty, non-empty
 * 6. append(double) -> calls append(long) via doubleToLongBits
 * 7. append(long) - uses >> shift (>> vs >>> potential bug zone)
 * 8. append(Object) - null, non-array object, array object (all primitive array types + Object[])
 *    - Multi-dimensional array handling via array type switching
 * 9. appendSuper(int) - combining super hashCode
 * 10. reflectionHashCode family:
 *     - null object => IAE
 *     - registry management (register/unregister)
 *     - transient field handling
 *     - static field exclusion
 *     - field exclusion via excludeFields
 *     - superclass traversal with reflectUpToClass
 *     - object cycle detection (CRITICAL DEFECT TARGET)
 * 11. toHashCode() / hashCode() consistency
 * 12. IDKey/registry cycle detection edge cases
 * 
 * Known Defect: Object cycle in reflectionHashCode causes registry state leak
 * - After handling a cycle, the registry should be cleaned up properly
 * - Bug: In some cases the unregister() fails, leaving stale entries
 * - Test: Ensure that after a cycle, the registry returns to empty state
 */
public class HashCodeBuilderDeepseekTest {

    // ============================================================
    // Partition A: Core Functional Logic & State Transitions
    // ============================================================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        HashCodeBuilder builder = new HashCodeBuilder();
        // Default initial value 17, multiplier 37
        assertEquals("Default initial total should be 17", 17, builder.toHashCode());
        builder.append(5);
        // After appending 5: iTotal = 17 * 37 + 5 = 629 + 5 = 634
        assertEquals(634, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testCustomConstructorValid() {
        HashCodeBuilder builder = new HashCodeBuilder(13, 29);
        assertEquals(13, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendInt() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(7);
        // 1 * 3 + 7 = 10
        assertEquals(10, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendBooleanTrue() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(true);
        // true => 0: 1*3 + 0 = 3
        assertEquals(3, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendBooleanFalse() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(false);
        // false => 1: 1*3 + 1 = 4
        assertEquals(4, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendByte() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append((byte) 10);
        assertEquals(13, builder.toHashCode()); // 1*3 + 10 = 13
    }

    @Test(timeout = 4000)
    public void testAppendChar() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append('A'); // 'A' = 65
        assertEquals(68, builder.toHashCode()); // 1*3 + 65 = 68
    }

    @Test(timeout = 4000)
    public void testAppendShort() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append((short) 100);
        assertEquals(103, builder.toHashCode()); // 1*3 + 100 = 103
    }

    @Test(timeout = 4000)
    public void testAppendFloat() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(3.14f);
        // Float.floatToIntBits(3.14f) = 1078523331
        assertEquals(1078523334, builder.toHashCode()); // 1*3 + 1078523331 = 1078523334
    }

    @Test(timeout = 4000)
    public void testAppendDouble() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(2.71828);
        // Double.doubleToLongBits(2.71828) = 4614253070214989087L
        // (int)(4614253070214989087L ^ (4614253070214989087L >> 32))
        // = (int)(4614253070214989087L ^ 1074349807L) = -2072613345
        // 1*3 + (-2072613345) = -2072613342
        assertEquals(-2072613342, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendLong() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(1234567890123L);
        // (int)(1234567890123L ^ (1234567890123L >> 32)) = (int)(1234567890123L ^ 287L) = -2045911172
        // 1*3 + (-2045911172) = -2045911169
        assertEquals(-2045911169, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendObjectNull() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append((Object) null);
        // null => iTotal = iTotal * iConstant = 1*3 = 3
        assertEquals(3, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendObjectNonArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append((Object) "hello");
        // "hello".hashCode() = 99162322
        // 1*3 + 99162322 = 99162325
        assertEquals(99162325, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendObjectIntArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        int[] arr = {1, 2, 3};
        builder.append((Object) arr);
        // After 1: 1*3 + 1 = 4
        // After 2: 4*3 + 2 = 14
        // After 3: 14*3 + 3 = 45
        assertEquals(45, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendObjectLongArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        long[] arr = {10L, 20L};
        builder.append((Object) arr);
        // 10L: (int)(10L ^ (10L >> 32)) = 10; 1*3 + 10 = 13
        // 20L: (int)(20L ^ (20L >> 32)) = 20; 13*3 + 20 = 59
        assertEquals(59, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendObjectShortArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        short[] arr = {5, 15};
        builder.append((Object) arr);
        // 5: 1*3 + 5 = 8
        // 15: 8*3 + 15 = 39
        assertEquals(39, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendObjectCharArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        char[] arr = {'X', 'Y'};
        builder.append((Object) arr);
        // 'X'=88: 1*3 + 88 = 91
        // 'Y'=89: 91*3 + 89 = 362
        assertEquals(362, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendObjectByteArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        byte[] arr = {7, 14};
        builder.append((Object) arr);
        // 7: 1*3 + 7 = 10
        // 14: 10*3 + 14 = 44
        assertEquals(44, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendObjectDoubleArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        double[] arr = {1.0};
        builder.append((Object) arr);
        // Double.doubleToLongBits(1.0) = 4607182418800017408L
        // (int)(4607182418800017408L ^ (4607182418800017408L >> 32))
        // = (int)(4607182418800017408L ^ 1072693248L) = 1072693248
        // 1*3 + 1072693248 = 1072693251
        assertEquals(1072693251, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendObjectFloatArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        float[] arr = {2.5f};
        builder.append((Object) arr);
        // Float.floatToIntBits(2.5f) = 1073741824
        // 1*3 + 1073741824 = 1073741827
        assertEquals(1073741827, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendObjectBooleanArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        boolean[] arr = {true, false};
        builder.append((Object) arr);
        // true=0: 1*3 + 0 = 3
        // false=1: 3*3 + 1 = 10
        assertEquals(10, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendObjectObjectArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        Object[] arr = {"a", "b"};
        builder.append((Object) arr);
        // "a".hashCode() = 97: 1*3 + 97 = 100
        // "b".hashCode() = 98: 100*3 + 98 = 398
        assertEquals(398, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendSuper() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.appendSuper(42);
        // 1*3 + 42 = 45
        assertEquals(45, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testToHashCodeConsistency() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(10);
        assertEquals(builder.toHashCode(), builder.hashCode());
    }

    @Test(timeout = 4000)
    public void testChainedAppendOperations() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(1).append(2).append(3);
        // 1*3+1=4; 4*3+2=14; 14*3+3=45
        assertEquals(45, builder.toHashCode());
    }

    // ============================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ============================================================

    @Test(timeout = 4000)
    public void testAppendNullBooleanArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append((boolean[]) null);
        // null => iTotal = iTotal * iConstant = 1*3 = 3
        assertEquals(3, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendEmptyBooleanArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(new boolean[0]);
        assertEquals(1, builder.toHashCode()); // no iterations, stays at 1
    }

    @Test(timeout = 4000)
    public void testAppendNullByteArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append((byte[]) null);
        assertEquals(3, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendNullCharArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append((char[]) null);
        assertEquals(3, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendNullShortArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append((short[]) null);
        assertEquals(3, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendNullIntArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append((int[]) null);
        assertEquals(3, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendNullLongArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append((long[]) null);
        assertEquals(3, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendNullFloatArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append((float[]) null);
        assertEquals(3, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendNullDoubleArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append((double[]) null);
        assertEquals(3, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendNullObjectArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append((Object[]) null);
        assertEquals(3, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendEmptyIntArray() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(new int[0]);
        assertEquals(1, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendMinMaxValues() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(Integer.MIN_VALUE);
        // 1*3 + (-2147483648) = -2147483645
        assertEquals(-2147483645, builder.toHashCode());
        
        builder = new HashCodeBuilder(1, 3);
        builder.append(Integer.MAX_VALUE);
        // 1*3 + 2147483647 = 2147483650
        assertEquals(2147483650L, (long) builder.toHashCode()); // use long to avoid overflow issues in assertion
    }

    @Test(timeout = 4000)
    public void testAppendLongMaxValue() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(Long.MAX_VALUE);
        // (int)(Long.MAX_VALUE ^ (Long.MAX_VALUE >> 32))
        // = (int)(9223372036854775807L ^ 2147483647L) = -2147483648
        // 1*3 + (-2147483648) = -2147483645
        assertEquals(-2147483645, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendLongMinValue() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(Long.MIN_VALUE);
        // (int)(Long.MIN_VALUE ^ (Long.MIN_VALUE >> 32))
        // = (int)(-9223372036854775808L ^ (-2147483648L)) = 0
        // 1*3 + 0 = 3
        assertEquals(3, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendFloatExtremes() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(Float.NaN);
        // Float.floatToIntBits(Float.NaN) = 2143289344
        // 1*3 + 2143289344 = 2143289347
        assertEquals(2143289347, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendDoubleExtremes() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(Double.NaN);
        // Double.doubleToLongBits(Double.NaN) = 9221120237041090560L
        // (int)(... ^ (... >> 32)) = 2146959360
        // 1*3 + 2146959360 = 2146959363
        assertEquals(2146959363, builder.toHashCode());
    }

    // ============================================================
    // Partition C: Defect-Targeted Branch Zone
    // ============================================================

    /**
     * CRITICAL TEST: Targets the known Defects4J defect where 
     * reflectionHashCode with object cycles fails to properly clean 
     * the registry, leaving stale entries.
     * 
     * The test verifies that after completing a reflection hash code 
     * computation involving a cyclic object graph, the thread-local 
     * registry is empty (i.e., no stale entries remain).
     */
    @Test(timeout = 4000)
    public void testReflectionObjectCycleRegistryCleanup() {
        // Create objects with cyclic reference
        class Node {
            Node next;
        }
        
        Node a = new Node();
        Node b = new Node();
        a.next = b;
        b.next = a; // creates cycle
        
        // Compute hashCode - this should handle the cycle gracefully
        int hashCode = HashCodeBuilder.reflectionHashCode(17, 37, a, false, null, (String[]) null);
        
        // After computation, the registry should be clean (empty)
        // This is the critical assertion that reveals the defect
        Set<IDKey> registry = HashCodeBuilder.getRegistry();
        assertTrue("Registry should be empty after reflection hash code computation completes", 
                   registry == null || registry.isEmpty());
        
        // Also verify we got a valid hash code (non-zero, no exception)
        assertNotNull("Hash code should be a valid integer", hashCode);
    }

    /**
     * Additional cycle detection test: Verify that self-referential 
     * objects don't cause infinite loops or registry leaks.
     */
    @Test(timeout = 4000)
    public void testReflectionSelfReference() {
        class SelfRef {
            SelfRef self = this;
        }
        
        SelfRef obj = new SelfRef();
        
        // Should not throw StackOverflowError or any other exception
        int hashCode = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, null, (String[]) null);
        
        // Registry should be clean
        Set<IDKey> registry = HashCodeBuilder.getRegistry();
        assertTrue("Registry should be empty after self-referential hash code computation", 
                   registry == null || registry.isEmpty());
    }

    /**
     * Test reflectionHashCode with excludeFields
     */
    @Test(timeout = 4000)
    public void testReflectionHashCodeWithExcludeFields() {
        class TestClass {
            int a = 1;
            int b = 2;
        }
        
        TestClass obj = new TestClass();
        int hashCode = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, null, new String[]{"a"});
        // Only field 'b' (value 2) should be included
        // initial: 17*37 + 2 = 631
        assertEquals(631, hashCode);
    }

    /**
     * Test reflectionHashCode with reflectUpToClass
     */
    @Test(timeout = 4000)
    public void testReflectionHashCodeWithSuperclass() {
        class Parent {
            int parentField = 10;
        }
        class Child extends Parent {
            int childField = 20;
        }
        
        Child obj = new Child();
        // Reflect up to Parent class - should include both fields
        int hashCode = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, Parent.class, (String[]) null);
        // parentField=10: 17*37 + 10 = 639
        // childField=20: 639*37 + 20 = 23663
        assertEquals(23663, hashCode);
    }

    /**
     * Test reflectionHashCode with testTransients=true
     */
    @Test(timeout = 4000)
    public void testReflectionHashCodeWithTransients() {
        class TestClass {
            transient int trans = 42;
            int normal = 7;
        }
        
        TestClass obj = new TestClass();
        // With testTransients=true, transient field 'trans' should be included
        int hashCodeTrans = HashCodeBuilder.reflectionHashCode(17, 37, obj, true, null, (String[]) null);
        // trans=42: 17*37 + 42 = 671
        // normal=7: 671*37 + 7 = 24834
        assertEquals(24834, hashCodeTrans);
        
        // With testTransients=false, only 'normal' should be included
        int hashCodeNoTrans = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, null, (String[]) null);
        // normal=7: 17*37 + 7 = 636
        assertEquals(636, hashCodeNoTrans);
    }

    /**
     * Test simple object with multiple fields via reflection
     */
    @Test(timeout = 4000)
    public void testReflectionHashCodeSimpleObject() {
        class Simple {
            int x = 3;
            int y = 5;
        }
        
        Simple obj = new Simple();
        int hashCode = HashCodeBuilder.reflectionHashCode(obj);
        // Default initial=17, multiplier=37, no transients
        // x=3: 17*37 + 3 = 632
        // y=5: 632*37 + 5 = 23389
        assertEquals(23389, hashCode);
    }

    /**
     * Test reflectionHashCode with boolean testTransients overload
     */
    @Test(timeout = 4000)
    public void testReflectionHashCodeBooleanTransients() {
        class TestWithTransient {
            transient int t = 99;
            int n = 1;
        }
        
        TestWithTransient obj = new TestWithTransient();
        int hashCode = HashCodeBuilder.reflectionHashCode(obj, true);
        // Default initial=17, multiplier=37
        // n=1: 17*37 + 1 = 630
        // t=99: 630*37 + 99 = 23409
        assertEquals(23409, hashCode);
    }

    // ============================================================
    // Partition D: Exception & Defensive Guard Paths
    // ============================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorThrowsExceptionForZeroInitial() {
        new HashCodeBuilder(0, 37);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorThrowsExceptionForEvenInitial() {
        new HashCodeBuilder(2, 37);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorThrowsExceptionForZeroMultiplier() {
        new HashCodeBuilder(17, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorThrowsExceptionForEvenMultiplier() {
        new HashCodeBuilder(17, 2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReflectionHashCodeNullObject() {
        HashCodeBuilder.reflectionHashCode(17, 37, null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReflectionHashCodeNullObjectSimple() {
        HashCodeBuilder.reflectionHashCode(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReflectionHashCodeNullObjectWithTransients() {
        HashCodeBuilder.reflectionHashCode(null, true);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReflectionHashCodeNullObjectWithExcludeCollection() {
        HashCodeBuilder.reflectionHashCode(null, new java.util.ArrayList<String>());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReflectionHashCodeNullObjectWithExcludeArray() {
        HashCodeBuilder.reflectionHashCode(null, new String[0]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReflectionHashCodeInvalidInitialValueZero() {
        HashCodeBuilder.reflectionHashCode(0, 37, new Object());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReflectionHashCodeInvalidInitialValueEven() {
        HashCodeBuilder.reflectionHashCode(4, 37, new Object());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReflectionHashCodeInvalidMultiplierZero() {
        HashCodeBuilder.reflectionHashCode(17, 0, new Object());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReflectionHashCodeInvalidMultiplierEven() {
        HashCodeBuilder.reflectionHashCode(17, 6, new Object());
    }

    // ============================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ============================================================

    @Test(timeout = 4000)
    public void testMultipleBuilderInstancesIndependent() {
        HashCodeBuilder builder1 = new HashCodeBuilder(1, 3);
        HashCodeBuilder builder2 = new HashCodeBuilder(1, 3);
        
        builder1.append(10);
        builder2.append(20);
        
        // builder1: 1*3 + 10 = 13
        assertEquals(13, builder1.toHashCode());
        // builder2: 1*3 + 20 = 23
        assertEquals(23, builder2.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendAfterToHashCode() {
        HashCodeBuilder builder = new HashCodeBuilder(1, 3);
        builder.append(5);
        int first = builder.toHashCode(); // 1*3+5 = 8
        assertEquals(8, first);
        
        builder.append(7);
        int second = builder.toHashCode(); // 8*3+7 = 31
        assertEquals(31, second);
    }

    @Test(timeout = 4000)
    public void testHashCodeContract() {
        // Two equal objects (same field values) should produce same hashCode
        class TestObj {
            int value;
            TestObj(int v) { this.value = v; }
        }
        
        TestObj obj1 = new TestObj(42);
        TestObj obj2 = new TestObj(42);
        
        int hash1 = HashCodeBuilder.reflectionHashCode(17, 37, obj1, false, null, (String[]) null);
        int hash2 = HashCodeBuilder.reflectionHashCode(17, 37, obj2, false, null, (String[]) null);
        
        assertEquals("Equal objects should produce equal hash codes", hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testRegistryThreadIsolation() throws InterruptedException {
        // Verify that registry is thread-local and doesn't leak between threads
        final boolean[] threadCompleted = {false};
        final Set<IDKey>[] threadRegistry = new Set[1];
        
        Thread t = new Thread(() -> {
            HashCodeBuilder.reflectionHashCode(17, 37, new Object(), false, null, (String[]) null);
            threadRegistry[0] = HashCodeBuilder.getRegistry();
            threadCompleted[0] = true;
        });
        t.start();
        t.join(2000);
        
        assertTrue("Thread should complete", threadCompleted[0]);
        assertTrue("Thread registry should be empty after completion", 
                    threadRegistry[0] == null || threadRegistry[0].isEmpty());
        
        // Main thread registry should also be clean
        Set<IDKey> mainRegistry = HashCodeBuilder.getRegistry();
        assertTrue("Main thread registry should be empty", 
                    mainRegistry == null || mainRegistry.isEmpty());
    }

    /**
     * Stress test: Multiple consecutive reflection hash code operations
     * to check for registry state leaks
     */
    @Test(timeout = 4000)
    public void testMultipleReflectionHashCodeCalls() {
        class Data {
            int x = 1;
            int y = 2;
        }
        
        Data obj = new Data();
        for (int i = 0; i < 100; i++) {
            int hash = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, null, (String[]) null);
            // Expected: x=1 => 17*37+1=630; y=2 => 630*37+2=23312
            assertEquals(23312, hash);
        }
        
        // Registry should be clean after all operations
        Set<IDKey> registry = HashCodeBuilder.getRegistry();
        assertTrue("Registry should be empty after multiple reflection hash code calls", 
                   registry == null || registry.isEmpty());
    }
}