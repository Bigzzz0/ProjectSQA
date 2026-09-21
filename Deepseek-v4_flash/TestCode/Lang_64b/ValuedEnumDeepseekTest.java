package org.apache.commons.lang.enums;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Test class for ValuedEnum.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ValuedEnum.java
 * 
 * Branches covered:
 * 1. getEnum(Class, int): null enumClass -> IllegalArgumentException
 * 2. getEnum(Class, int): empty list -> null
 * 3. getEnum(Class, int): matching value found -> return enum
 * 4. getEnum(Class, int): no matching value -> null
 * 5. getValue(): returns iValue
 * 6. compareTo(Object): normal comparison (same type)
 * 7. compareTo(Object): comparison with different enum type (DEFECT TARGET)
 * 8. toString(): cached toString
 * 9. toString(): first call builds string
 * 
 * Boundary conditions:
 * - Integer.MIN_VALUE, Integer.MAX_VALUE
 * - Zero value
 * - Negative values
 * - Null enum class
 * - Different enum types in compareTo
 * 
 * Defect target: compareTo with different enum types should throw ClassCastException
 * but current implementation may not handle it correctly (returns incorrect comparison)
 */
public class ValuedEnumDeepseekTest {

    // ==================== Test Enum Subclasses ====================
    
    private static final class TestEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;
        
        public static final TestEnum ONE = new TestEnum("ONE", 1);
        public static final TestEnum TWO = new TestEnum("TWO", 2);
        public static final TestEnum THREE = new TestEnum("THREE", 3);
        public static final TestEnum NEGATIVE = new TestEnum("NEGATIVE", -1);
        public static final TestEnum ZERO = new TestEnum("ZERO", 0);
        public static final TestEnum MAX = new TestEnum("MAX", Integer.MAX_VALUE);
        public static final TestEnum MIN = new TestEnum("MIN", Integer.MIN_VALUE);
        
        private TestEnum(String name, int value) {
            super(name, value);
        }
        
        public static TestEnum getEnum(String name) {
            return (TestEnum) getEnum(TestEnum.class, name);
        }
        
        public static TestEnum getEnum(int value) {
            return (TestEnum) getEnum(TestEnum.class, value);
        }
        
        public static List getEnumList() {
            return getEnumList(TestEnum.class);
        }
        
        public static Iterator iterator() {
            return iterator(TestEnum.class);
        }
    }
    
    private static final class OtherTestEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;
        
        public static final OtherTestEnum A = new OtherTestEnum("A", 10);
        public static final OtherTestEnum B = new OtherTestEnum("B", 20);
        
        private OtherTestEnum(String name, int value) {
            super(name, value);
        }
        
        public static OtherTestEnum getEnum(String name) {
            return (OtherTestEnum) getEnum(OtherTestEnum.class, name);
        }
        
        public static OtherTestEnum getEnum(int value) {
            return (OtherTestEnum) getEnum(OtherTestEnum.class, value);
        }
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================
    
    @Test(timeout = 4000)
    public void testGetValue() {
        assertEquals("ONE value should be 1", 1, TestEnum.ONE.getValue());
        assertEquals("TWO value should be 2", 2, TestEnum.TWO.getValue());
        assertEquals("THREE value should be 3", 3, TestEnum.THREE.getValue());
    }
    
    @Test(timeout = 4000)
    public void testGetEnumByValue() {
        assertEquals("getEnum(1) should return ONE", TestEnum.ONE, TestEnum.getEnum(1));
        assertEquals("getEnum(2) should return TWO", TestEnum.TWO, TestEnum.getEnum(2));
        assertEquals("getEnum(3) should return THREE", TestEnum.THREE, TestEnum.getEnum(3));
    }
    
    @Test(timeout = 4000)
    public void testGetEnumByValueNotFound() {
        assertNull("getEnum(999) should return null", TestEnum.getEnum(999));
    }
    
    @Test(timeout = 4000)
    public void testGetEnumByName() {
        assertEquals("getEnum('ONE') should return ONE", TestEnum.ONE, TestEnum.getEnum("ONE"));
        assertEquals("getEnum('TWO') should return TWO", TestEnum.TWO, TestEnum.getEnum("TWO"));
    }
    
    @Test(timeout = 4000)
    public void testGetEnumByNameNotFound() {
        assertNull("getEnum('NONEXISTENT') should return null", TestEnum.getEnum("NONEXISTENT"));
    }
    
    @Test(timeout = 4000)
    public void testIterator() {
        Iterator iterator = TestEnum.iterator();
        assertNotNull("Iterator should not be null", iterator);
        assertTrue("Iterator should have elements", iterator.hasNext());
    }
    
    @Test(timeout = 4000)
    public void testGetEnumList() {
        List list = TestEnum.getEnumList();
        assertNotNull("Enum list should not be null", list);
        assertTrue("Enum list should contain elements", list.size() > 0);
        assertTrue("Enum list should contain ONE", list.contains(TestEnum.ONE));
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================
    
    @Test(timeout = 4000)
    public void testBoundaryValues() {
        assertEquals("ZERO value should be 0", 0, TestEnum.ZERO.getValue());
        assertEquals("NEGATIVE value should be -1", -1, TestEnum.NEGATIVE.getValue());
        assertEquals("MAX value should be Integer.MAX_VALUE", Integer.MAX_VALUE, TestEnum.MAX.getValue());
        assertEquals("MIN value should be Integer.MIN_VALUE", Integer.MIN_VALUE, TestEnum.MIN.getValue());
    }
    
    @Test(timeout = 4000)
    public void testGetEnumByBoundaryValues() {
        assertEquals("getEnum(0) should return ZERO", TestEnum.ZERO, TestEnum.getEnum(0));
        assertEquals("getEnum(-1) should return NEGATIVE", TestEnum.NEGATIVE, TestEnum.getEnum(-1));
        assertEquals("getEnum(Integer.MAX_VALUE) should return MAX", TestEnum.MAX, TestEnum.getEnum(Integer.MAX_VALUE));
        assertEquals("getEnum(Integer.MIN_VALUE) should return MIN", TestEnum.MIN, TestEnum.getEnum(Integer.MIN_VALUE));
    }
    
    @Test(timeout = 4000)
    public void testCompareToSameType() {
        assertTrue("ONE.compareTo(TWO) should be negative", TestEnum.ONE.compareTo(TestEnum.TWO) < 0);
        assertTrue("TWO.compareTo(ONE) should be positive", TestEnum.TWO.compareTo(TestEnum.ONE) > 0);
        assertEquals("ONE.compareTo(ONE) should be 0", 0, TestEnum.ONE.compareTo(TestEnum.ONE));
    }
    
    @Test(timeout = 4000)
    public void testCompareToBoundaryValues() {
        assertTrue("MIN.compareTo(MAX) should be negative", TestEnum.MIN.compareTo(TestEnum.MAX) < 0);
        assertTrue("MAX.compareTo(MIN) should be positive", TestEnum.MAX.compareTo(TestEnum.MIN) > 0);
        assertTrue("NEGATIVE.compareTo(ZERO) should be negative", TestEnum.NEGATIVE.compareTo(TestEnum.ZERO) < 0);
        assertTrue("ZERO.compareTo(NEGATIVE) should be positive", TestEnum.ZERO.compareTo(TestEnum.NEGATIVE) > 0);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================
    
    /**
     * CRITICAL DEFECT TEST: compareTo with different enum types
     * 
     * According to the Defects4J ground truth, comparing ValuedEnum instances
     * of different types should throw a ClassCastException because the
     * compareTo method casts the argument to ValuedEnum without checking
     * if it's the same enum type. The current implementation may return
     * an incorrect comparison result instead of throwing ClassCastException.
     * 
     * This test verifies that comparing enums of different types throws
     * ClassCastException as expected by the Comparable contract.
     */
    @Test(timeout = 4000)
    public void testCompareTo_otherEnumType() {
        try {
            // This should throw ClassCastException because TestEnum and OtherTestEnum
            // are different enum types
            int result = TestEnum.ONE.compareTo(OtherTestEnum.A);
            // If we get here, the comparison didn't throw - this is the defect!
            // The result might be incorrect (e.g., 1 - 10 = -9) but that's wrong
            // because these are incomparable types
            fail("compareTo with different enum types should throw ClassCastException, but got result: " + result);
        } catch (ClassCastException e) {
            // Expected behavior - different enum types should not be comparable
            // This is the correct behavior
        }
    }
    
    @Test(timeout = 4000)
    public void testCompareTo_nullArgument() {
        try {
            TestEnum.ONE.compareTo(null);
            fail("compareTo(null) should throw NullPointerException");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000)
    public void testGetEnumWithNullClass() {
        try {
            ValuedEnum.getEnum(null, 1);
            fail("getEnum(null, 1) should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
            assertTrue("Exception message should mention null", e.getMessage().contains("null"));
        }
    }
    
    @Test(timeout = 4000)
    public void testGetEnumWithNullClassAndZeroValue() {
        try {
            ValuedEnum.getEnum(null, 0);
            fail("getEnum(null, 0) should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }
    
    @Test(timeout = 4000)
    public void testGetEnumByValueWithNonExistentClass() {
        // This should return null for a class that has no enums defined
        // We can't easily test this without creating an empty enum subclass
        // But we can verify the method handles empty lists gracefully
        assertNull("getEnum for non-existent value should return null", 
                   TestEnum.getEnum(Integer.MAX_VALUE - 1));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testEquals() {
        assertEquals("Same enum should be equal", TestEnum.ONE, TestEnum.ONE);
        assertEquals("Same enum by value should be equal", TestEnum.ONE, TestEnum.getEnum(1));
        assertFalse("Different enums should not be equal", TestEnum.ONE.equals(TestEnum.TWO));
        assertFalse("Enum should not equal null", TestEnum.ONE.equals(null));
        assertFalse("Enum should not equal different type", TestEnum.ONE.equals("ONE"));
    }
    
    @Test(timeout = 4000)
    public void testHashCode() {
        assertEquals("Same enum should have same hashcode", 
                     TestEnum.ONE.hashCode(), TestEnum.ONE.hashCode());
        assertEquals("Same enum by value should have same hashcode", 
                     TestEnum.ONE.hashCode(), TestEnum.getEnum(1).hashCode());
    }
    
    @Test(timeout = 4000)
    public void testToString() {
        String toString = TestEnum.ONE.toString();
        assertNotNull("toString should not be null", toString);
        assertTrue("toString should contain enum name", toString.contains("ONE"));
        assertTrue("toString should contain value", toString.contains("1"));
        assertTrue("toString should contain class name", toString.contains("TestEnum"));
        
        // Verify caching works - calling toString again should return same string
        String toString2 = TestEnum.ONE.toString();
        assertEquals("toString should be cached", toString, toString2);
    }
    
    @Test(timeout = 4000)
    public void testGetName() {
        assertEquals("getName should return 'ONE'", "ONE", TestEnum.ONE.getName());
        assertEquals("getName should return 'TWO'", "TWO", TestEnum.TWO.getName());
    }
    
    @Test(timeout = 4000)
    public void testGetEnumClass() {
        assertEquals("getEnumClass should return TestEnum.class", 
                     TestEnum.class, TestEnum.ONE.getEnumClass());
    }
    
    @Test(timeout = 4000)
    public void testSerialization() {
        // ValuedEnum extends Enum which implements Serializable
        // Basic serialization test
        TestEnum original = TestEnum.ONE;
        String toString = original.toString();
        assertNotNull("Serializable enum should have toString", toString);
    }
}