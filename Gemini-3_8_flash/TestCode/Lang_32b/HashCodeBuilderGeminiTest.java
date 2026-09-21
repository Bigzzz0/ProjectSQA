package org.apache.commons.lang3.builder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.lang3.builder.HashCodeBuilder
 * Target Defects4J Fault: testReflectionObjectCycle failing with:
 *                         junit.framework.AssertionFailedError: Expected: <null> but was: []
 *
 * Decision / Condition Branch Coverage:
 * 1. Constructor Validation:
 *    - initial == 0 (throws IAE)
 *    - initial % 2 == 0 (even, throws IAE)
 *    - initial negative odd, positive odd (valid)
 *    - multiplier == 0 (throws IAE)
 *    - multiplier % 2 == 0 (even, throws IAE)
 *    - multiplier negative odd, positive odd (valid)
 * 2. Primitive & Array Appends (null array vs empty array vs non-empty array):
 *    - boolean, boolean[]
 *    - byte, byte[]
 *    - char, char[]
 *    - double, double[]
 *    - float, float[]
 *    - int, int[]
 *    - long, long[] (bit shift: value ^ (value >> 32))
 *    - short, short[]
 * 3. Object & Multidimensional / Primitive Reflection Dispatch in append(Object):
 *    - object == null
 *    - object != null, not an array
 *    - object is long[], int[], short[], char[], byte[], double[], float[], boolean[]
 *    - object is Object[] (including nested Object[][] and String[])
 * 4. Reflection Append & Hierarchy Traversal:
 *    - reflectionHashCode(null) -> throws IAE
 *    - static fields exclusion
 *    - inner class synthetic fields with '$' exclusion
 *    - excludeFields matching vs non-matching
 *    - testTransients = true vs false
 *    - reflectUpToClass boundary (null vs explicit superclass vs Object.class)
 *    - Circular object references (cycle detection via IDKey registry)
 * 5. Registry State & Lifecycle (Target Bug Zone):
 *    - register(obj), unregister(obj), isRegistered(obj)
 *    - Post-reflection cleanup check: getRegistry() should be null/empty
 * 6. appendSuper and Object Contract Integrity:
 *    - appendSuper(super.hashCode())
 *    - hashCode() returns toHashCode()
 * ---------------------------------------------------------------------------------------------------------
 */
public class HashCodeBuilderGeminiTest {

    // Helper test classes for reflection and hierarchy testing
    static class ReflectionTestCycleA {
        ReflectionTestCycleB b;
    }

    static class ReflectionTestCycleB {
        ReflectionTestCycleA a;
    }

    static class SuperClass {
        int superField = 10;
        transient int superTransient = 20;
    }

    static class SubClass extends SuperClass {
        int subField = 30;
        transient int subTransient = 40;
        static int staticField = 50;
        String fieldWithExclude = "excludeMe";
    }

    static class EmptyClass {
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    /**
     * Targets Defects4J bug: When reflectionHashCode handles cyclical references or finishes,
     * the registry state must be cleanly removed so that getRegistry() is null.
     */
    @Test(timeout = 4000)
    public void testReflectionObjectCycle() {
        ReflectionTestCycleA a = new ReflectionTestCycleA();
        ReflectionTestCycleB b = new ReflectionTestCycleB();
        a.b = b;
        b.a = a;

        int hashA = HashCodeBuilder.reflectionHashCode(a);
        assertTrue(hashA != 0);

        // Ground-truth defect expectation: Registry should be cleared and null after completion
        assertNull("Registry must be null after reflectionHashCode completes", HashCodeBuilder.getRegistry());
    }

    @Test(timeout = 4000)
    public void testReflectionCycleDirectSelfReference() {
        ReflectionTestCycleA a = new ReflectionTestCycleA();
        ReflectionTestCycleB b = new ReflectionTestCycleB();
        a.b = b;
        b.a = a;

        int hash1 = HashCodeBuilder.reflectionHashCode(a, false);
        int hash2 = HashCodeBuilder.reflectionHashCode(a, false);
        assertEquals(hash1, hash2);

        assertNull("Registry must be null after circular reference calculation", HashCodeBuilder.getRegistry());
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDefaultConstructorAndPrimitives() {
        HashCodeBuilder builder = new HashCodeBuilder();
        assertEquals(17, builder.toHashCode());

        builder.append(true);
        // 17 * 37 + 0 = 629
        assertEquals(629, builder.toHashCode());

        builder.append(false);
        // 629 * 37 + 1 = 23274
        assertEquals(23274, builder.toHashCode());

        builder.append((byte) 5);
        // 23274 * 37 + 5 = 861143
        assertEquals(861143, builder.toHashCode());

        builder.append('A');
        // 861143 * 37 + 65 = 31862356
        assertEquals(31862356, builder.toHashCode());

        builder.append((short) 2);
        // 31862356 * 37 + 2 = 1178907174
        assertEquals(1178907174, builder.toHashCode());

        builder.append(100);
        int expectedAfterInt = 1178907174 * 37 + 100;
        assertEquals(expectedAfterInt, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendLongAndFloatingPoint() {
        HashCodeBuilder builder = new HashCodeBuilder(19, 41);

        long lVal = 0x123456789ABCDEFL;
        int longBits = (int) (lVal ^ (lVal >> 32));
        builder.append(lVal);
        assertEquals(19 * 41 + longBits, builder.toHashCode());

        builder = new HashCodeBuilder(17, 37);
        float fVal = 3.14f;
        int floatBits = Float.floatToIntBits(fVal);
        builder.append(fVal);
        assertEquals(17 * 37 + floatBits, builder.toHashCode());

        builder = new HashCodeBuilder(17, 37);
        double dVal = 2.71828;
        long doubleBits = Double.doubleToLongBits(dVal);
        int expectedDoubleHash = (int) (doubleBits ^ (doubleBits >> 32));
        builder.append(dVal);
        assertEquals(17 * 37 + expectedDoubleHash, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendSuper() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        builder.appendSuper(42);
        assertEquals(17 * 37 + 42, builder.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendObjectNonArray() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37);
        String testStr = "Defects4J";
        builder.append(testStr);
        assertEquals(17 * 37 + testStr.hashCode(), builder.toHashCode());
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Arrays (Multi-dimensional)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAppendNullArrays() {
        assertEquals(new HashCodeBuilder(17, 37).append((boolean[]) null).toHashCode(), 17 * 37);
        assertEquals(new HashCodeBuilder(17, 37).append((byte[]) null).toHashCode(), 17 * 37);
        assertEquals(new HashCodeBuilder(17, 37).append((char[]) null).toHashCode(), 17 * 37);
        assertEquals(new HashCodeBuilder(17, 37).append((double[]) null).toHashCode(), 17 * 37);
        assertEquals(new HashCodeBuilder(17, 37).append((float[]) null).toHashCode(), 17 * 37);
        assertEquals(new HashCodeBuilder(17, 37).append((int[]) null).toHashCode(), 17 * 37);
        assertEquals(new HashCodeBuilder(17, 37).append((long[]) null).toHashCode(), 17 * 37);
        assertEquals(new HashCodeBuilder(17, 37).append((short[]) null).toHashCode(), 17 * 37);
        assertEquals(new HashCodeBuilder(17, 37).append((Object[]) null).toHashCode(), 17 * 37);
        assertEquals(new HashCodeBuilder(17, 37).append((Object) null).toHashCode(), 17 * 37);
    }

    @Test(timeout = 4000)
    public void testAppendEmptyArrays() {
        assertEquals(17, new HashCodeBuilder(17, 37).append(new boolean[0]).toHashCode());
        assertEquals(17, new HashCodeBuilder(17, 37).append(new byte[0]).toHashCode());
        assertEquals(17, new HashCodeBuilder(17, 37).append(new char[0]).toHashCode());
        assertEquals(17, new HashCodeBuilder(17, 37).append(new double[0]).toHashCode());
        assertEquals(17, new HashCodeBuilder(17, 37).append(new float[0]).toHashCode());
        assertEquals(17, new HashCodeBuilder(17, 37).append(new int[0]).toHashCode());
        assertEquals(17, new HashCodeBuilder(17, 37).append(new long[0]).toHashCode());
        assertEquals(17, new HashCodeBuilder(17, 37).append(new short[0]).toHashCode());
        assertEquals(17, new HashCodeBuilder(17, 37).append(new Object[0]).toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendPopulatedPrimitiveArrays() {
        HashCodeBuilder b1 = new HashCodeBuilder().append(new boolean[]{true, false});
        HashCodeBuilder b2 = new HashCodeBuilder().append(true).append(false);
        assertEquals(b2.toHashCode(), b1.toHashCode());

        HashCodeBuilder by1 = new HashCodeBuilder().append(new byte[]{1, 2});
        HashCodeBuilder by2 = new HashCodeBuilder().append((byte) 1).append((byte) 2);
        assertEquals(by2.toHashCode(), by1.toHashCode());

        HashCodeBuilder c1 = new HashCodeBuilder().append(new char[]{'x', 'y'});
        HashCodeBuilder c2 = new HashCodeBuilder().append('x').append('y');
        assertEquals(c2.toHashCode(), c1.toHashCode());

        HashCodeBuilder d1 = new HashCodeBuilder().append(new double[]{1.5, 2.5});
        HashCodeBuilder d2 = new HashCodeBuilder().append(1.5).append(2.5);
        assertEquals(d2.toHashCode(), d1.toHashCode());

        HashCodeBuilder f1 = new HashCodeBuilder().append(new float[]{1.1f, 2.2f});
        HashCodeBuilder f2 = new HashCodeBuilder().append(1.1f).append(2.2f);
        assertEquals(f2.toHashCode(), f1.toHashCode());

        HashCodeBuilder i1 = new HashCodeBuilder().append(new int[]{10, 20});
        HashCodeBuilder i2 = new HashCodeBuilder().append(10).append(20);
        assertEquals(i2.toHashCode(), i1.toHashCode());

        HashCodeBuilder l1 = new HashCodeBuilder().append(new long[]{100L, 200L});
        HashCodeBuilder l2 = new HashCodeBuilder().append(100L).append(200L);
        assertEquals(l2.toHashCode(), l1.toHashCode());

        HashCodeBuilder s1 = new HashCodeBuilder().append(new short[]{3, 4});
        HashCodeBuilder s2 = new HashCodeBuilder().append((short) 3).append((short) 4);
        assertEquals(s2.toHashCode(), s1.toHashCode());
    }

    @Test(timeout = 4000)
    public void testAppendObjectSwitchBranchesForArrays() {
        // Test append(Object) dynamically dispatching to specific array handlers
        assertEquals(new HashCodeBuilder().append(new long[]{1L, 2L}).toHashCode(),
                new HashCodeBuilder().append((Object) new long[]{1L, 2L}).toHashCode());

        assertEquals(new HashCodeBuilder().append(new int[]{1, 2}).toHashCode(),
                new HashCodeBuilder().append((Object) new int[]{1, 2}).toHashCode());

        assertEquals(new HashCodeBuilder().append(new short[]{1, 2}).toHashCode(),
                new HashCodeBuilder().append((Object) new short[]{1, 2}).toHashCode());

        assertEquals(new HashCodeBuilder().append(new char[]{'a', 'b'}).toHashCode(),
                new HashCodeBuilder().append((Object) new char[]{'a', 'b'}).toHashCode());

        assertEquals(new HashCodeBuilder().append(new byte[]{1, 2}).toHashCode(),
                new HashCodeBuilder().append((Object) new byte[]{1, 2}).toHashCode());

        assertEquals(new HashCodeBuilder().append(new double[]{1.0, 2.0}).toHashCode(),
                new HashCodeBuilder().append((Object) new double[]{1.0, 2.0}).toHashCode());

        assertEquals(new HashCodeBuilder().append(new float[]{1.0f, 2.0f}).toHashCode(),
                new HashCodeBuilder().append((Object) new float[]{1.0f, 2.0f}).toHashCode());

        assertEquals(new HashCodeBuilder().append(new boolean[]{true, false}).toHashCode(),
                new HashCodeBuilder().append((Object) new boolean[]{true, false}).toHashCode());

        // Multi-dimensional Object array
        String[][] matrix = new String[][]{{"a", "b"}, {"c", null}};
        HashCodeBuilder b1 = new HashCodeBuilder().append((Object) matrix);
        HashCodeBuilder b2 = new HashCodeBuilder().append(matrix);
        assertEquals(b1.toHashCode(), b2.toHashCode());
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorZeroInitial() {
        new HashCodeBuilder(0, 37);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorEvenInitial() {
        new HashCodeBuilder(2, 37);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNegativeEvenInitial() {
        new HashCodeBuilder(-4, 37);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorZeroMultiplier() {
        new HashCodeBuilder(17, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorEvenMultiplier() {
        new HashCodeBuilder(17, 4);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNegativeEvenMultiplier() {
        new HashCodeBuilder(17, -6);
    }

    @Test(timeout = 4000)
    public void testConstructorNegativeOddValuesValid() {
        HashCodeBuilder builder = new HashCodeBuilder(-3, -5);
        assertEquals(-3, builder.toHashCode());
        builder.append(1);
        assertEquals((-3) * (-5) + 1, builder.toHashCode());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReflectionHashCodeNullObject() {
        HashCodeBuilder.reflectionHashCode(null);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testReflectionHashCodeNullObjectWithOptions() {
        HashCodeBuilder.reflectionHashCode(17, 37, null, true, null, new String[0]);
    }

    // -------------------------------------------------------------------------
    // Partition E: Reflection Coverage (Transients, Exclusions, Hierarchy)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testReflectionHashCodeExclusionsAndTransients() {
        SubClass obj = new SubClass();

        // 1. Without transients, without exclusions
        int hashDefault = HashCodeBuilder.reflectionHashCode(obj);
        int expectedDefault = new HashCodeBuilder(17, 37)
                .append(obj.subField)
                .append(obj.fieldWithExclude)
                .append(obj.superField)
                .toHashCode();
        assertEquals(expectedDefault, hashDefault);

        // 2. With transients
        int hashWithTransients = HashCodeBuilder.reflectionHashCode(obj, true);
        int expectedWithTrans = new HashCodeBuilder(17, 37)
                .append(obj.subField)
                .append(obj.subTransient)
                .append(obj.fieldWithExclude)
                .append(obj.superField)
                .append(obj.superTransient)
                .toHashCode();
        assertEquals(expectedWithTrans, hashWithTransients);

        // 3. With excluded fields array
        int hashWithExcludeArray = HashCodeBuilder.reflectionHashCode(obj, new String[]{"fieldWithExclude"});
        int expectedWithExclude = new HashCodeBuilder(17, 37)
                .append(obj.subField)
                .append(obj.superField)
                .toHashCode();
        assertEquals(expectedWithExclude, hashWithExcludeArray);

        // 4. With excluded fields collection
        Collection<String> excludes = new ArrayList<String>();
        excludes.add("fieldWithExclude");
        int hashWithExcludeCol = HashCodeBuilder.reflectionHashCode(obj, excludes);
        assertEquals(hashWithExcludeArray, hashWithExcludeCol);

        // 5. With reflectUpToClass stopping at SubClass (excluding SuperClass)
        int hashSubClassOnly = HashCodeBuilder.reflectionHashCode(17, 37, obj, false, SubClass.class);
        int expectedSubOnly = new HashCodeBuilder(17, 37)
                .append(obj.subField)
                .append(obj.fieldWithExclude)
                .toHashCode();
        assertEquals(expectedSubOnly, hashSubClassOnly);
    }

    @Test(timeout = 4000)
    public void testReflectionHashCodeWithOddNumbersVariations() {
        SubClass obj = new SubClass();
        int hash1 = HashCodeBuilder.reflectionHashCode(19, 41, obj);
        int expected = new HashCodeBuilder(19, 41)
                .append(obj.subField)
                .append(obj.fieldWithExclude)
                .append(obj.superField)
                .toHashCode();
        assertEquals(expected, hash1);

        int hash2 = HashCodeBuilder.reflectionHashCode(19, 41, obj, true);
        int expected2 = new HashCodeBuilder(19, 41)
                .append(obj.subField)
                .append(obj.subTransient)
                .append(obj.fieldWithExclude)
                .append(obj.superField)
                .append(obj.superTransient)
                .toHashCode();
        assertEquals(expected2, hash2);
    }

    @Test(timeout = 4000)
    public void testReflectionEmptyClass() {
        EmptyClass obj = new EmptyClass();
        int hash = HashCodeBuilder.reflectionHashCode(obj);
        assertEquals(17, hash);
    }

    // -------------------------------------------------------------------------
    // Partition F: Registry State & Object Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testRegistryRegisterUnregister() {
        Object item = new Object();
        assertFalse(HashCodeBuilder.isRegistered(item));

        HashCodeBuilder.register(item);
        assertTrue(HashCodeBuilder.isRegistered(item));

        Set<IDKey> registry = HashCodeBuilder.getRegistry();
        assertNotNull(registry);
        assertTrue(registry.contains(new IDKey(item)));

        HashCodeBuilder.unregister(item);
        assertFalse(HashCodeBuilder.isRegistered(item));
    }

    @Test(timeout = 4000)
    public void testHashCodeContract() {
        HashCodeBuilder builder = new HashCodeBuilder(17, 37).append("sample").append(123);
        assertEquals(builder.toHashCode(), builder.hashCode());
    }
}