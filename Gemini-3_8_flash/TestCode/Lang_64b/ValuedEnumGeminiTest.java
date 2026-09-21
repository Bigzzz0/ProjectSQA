package org.apache.commons.lang.enums;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.lang.enums.ValuedEnum
 *
 * Decision / Condition Coverage Targets:
 * 1. getEnum(Class enumClass, int value):
 *    - Branch: enumClass == null -> throws IllegalArgumentException
 *    - Branch: enumClass != null -> iterates through Enum.getEnumList(enumClass)
 *    - Branch: iteration finds matching value (enumeration.getValue() == value) -> returns enumeration
 *    - Branch: iteration completes without finding matching value -> returns null
 *    - Branch: items in enumClass are not ValuedEnum instances -> throws ClassCastException
 * 2. getValue():
 *    - Returns exact primitive int iValue (positive, zero, negative, MIN_VALUE, MAX_VALUE)
 * 3. compareTo(Object other):
 *    - Branch: other is null -> throws NullPointerException
 *    - Branch: other is not an Enum / ValuedEnum -> throws ClassCastException
 *    - Branch: other is of different ValuedEnum subclass -> MUST throw ClassCastException
 *      [Defects4J Ground Truth Defect: ValuedEnumTest::testCompareTo_otherEnumType]
 *    - Branch: other is of identical ValuedEnum subclass -> returns numeric difference
 * 4. toString():
 *    - Branch: iToString == null -> formats "ShortClassName[name=value]", caches in iToString
 *    - Branch: iToString != null -> returns cached string reference immediately
 * 5. Lifecycle / Serialization:
 *    - Serialization round-trip maintains strict singleton reference identity via readResolve
 */
public class ValuedEnumGeminiTest {

    // Concrete test fixtures subclassing ValuedEnum
    private static final class TestValuedEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;

        public static final TestValuedEnum ITEM_1 = new TestValuedEnum("Item 1", 1);
        public static final TestValuedEnum ITEM_2 = new TestValuedEnum("Item 2", 2);
        public static final TestValuedEnum ITEM_3 = new TestValuedEnum("Item 3", 3);
        public static final TestValuedEnum ZERO = new TestValuedEnum("Zero", 0);
        public static final TestValuedEnum NEGATIVE = new TestValuedEnum("Negative", -100);
        public static final TestValuedEnum MIN = new TestValuedEnum("Min", Integer.MIN_VALUE);
        public static final TestValuedEnum MAX = new TestValuedEnum("Max", Integer.MAX_VALUE);

        private TestValuedEnum(String name, int value) {
            super(name, value);
        }

        public static TestValuedEnum getEnum(int value) {
            return (TestValuedEnum) getEnum(TestValuedEnum.class, value);
        }
    }

    private static final class AnotherValuedEnum extends ValuedEnum {
        private static final long serialVersionUID = 1L;

        public static final AnotherValuedEnum OTHER_1 = new AnotherValuedEnum("Item 1", 1);
        public static final AnotherValuedEnum OTHER_2 = new AnotherValuedEnum("Item 2", 2);

        private AnotherValuedEnum(String name, int value) {
            super(name, value);
        }
    }

    private static final class NonValuedStandardEnum extends Enum {
        private static final long serialVersionUID = 1L;

        public static final NonValuedStandardEnum NON_VALUED_ITEM = new NonValuedStandardEnum("NonValued");

        private NonValuedStandardEnum(String name) {
            super(name);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetValue_returnsConfiguredValue() {
        assertEquals("Exact int value should match constructor argument", 1, TestValuedEnum.ITEM_1.getValue());
        assertEquals("Exact int value should match constructor argument", 2, TestValuedEnum.ITEM_2.getValue());
        assertEquals("Exact int value should match constructor argument", 3, TestValuedEnum.ITEM_3.getValue());
    }

    @Test(timeout = 4000)
    public void testGetEnum_successfulLookup() {
        Enum result1 = ValuedEnum.getEnum(TestValuedEnum.class, 1);
        assertNotNull("Lookup should find enum with value 1", result1);
        assertSame("Lookup should return the singleton instance", TestValuedEnum.ITEM_1, result1);
        assertEquals("Found enum name must match", "Item 1", result1.getName());

        Enum result2 = ValuedEnum.getEnum(TestValuedEnum.class, 2);
        assertSame("Lookup should return the singleton instance", TestValuedEnum.ITEM_2, result2);

        TestValuedEnum helperResult = TestValuedEnum.getEnum(3);
        assertSame("Subclass convenience lookup should work", TestValuedEnum.ITEM_3, helperResult);
    }

    @Test(timeout = 4000)
    public void testGetEnum_nonExistentValueReturnsNull() {
        Enum result = ValuedEnum.getEnum(TestValuedEnum.class, 99999);
        assertNull("Lookup for non-existent value must return null", result);
    }

    @Test(timeout = 4000)
    public void testToString_formattingAndCaching() {
        TestValuedEnum item = TestValuedEnum.ITEM_1;
        String firstCall = item.toString();

        assertNotNull("toString result must not be null", firstCall);
        assertTrue("toString should contain class name", firstCall.contains("TestValuedEnum"));
        assertTrue("toString should contain item name and value", firstCall.contains("Item 1=1"));
        assertTrue("toString format should be ClassName[name=value]", firstCall.endsWith("[Item 1=1]"));

        String secondCall = item.toString();
        assertSame("Second call should return cached iToString instance", firstCall, secondCall);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testBva_zeroAndNegativeValues() {
        assertSame("Lookup for 0 should succeed", TestValuedEnum.ZERO, ValuedEnum.getEnum(TestValuedEnum.class, 0));
        assertEquals("Zero value must be 0", 0, TestValuedEnum.ZERO.getValue());

        assertSame("Lookup for negative should succeed", TestValuedEnum.NEGATIVE, ValuedEnum.getEnum(TestValuedEnum.class, -100));
        assertEquals("Negative value must be -100", -100, TestValuedEnum.NEGATIVE.getValue());
    }

    @Test(timeout = 4000)
    public void testBva_minAndMaxIntegerValues() {
        assertSame("Lookup for Integer.MIN_VALUE should succeed", TestValuedEnum.MIN, ValuedEnum.getEnum(TestValuedEnum.class, Integer.MIN_VALUE));
        assertEquals("Min value must equal Integer.MIN_VALUE", Integer.MIN_VALUE, TestValuedEnum.MIN.getValue());

        assertSame("Lookup for Integer.MAX_VALUE should succeed", TestValuedEnum.MAX, ValuedEnum.getEnum(TestValuedEnum.class, Integer.MAX_VALUE));
        assertEquals("Max value must equal Integer.MAX_VALUE", Integer.MAX_VALUE, TestValuedEnum.MAX.getValue());
    }

    @Test(timeout = 4000)
    public void testCompareTo_sameEnumTypeOrdering() {
        assertTrue("Equal items should compare to 0", TestValuedEnum.ITEM_1.compareTo(TestValuedEnum.ITEM_1) == 0);
        assertTrue("Smaller item compared to larger should return negative", TestValuedEnum.ITEM_1.compareTo(TestValuedEnum.ITEM_2) < 0);
        assertTrue("Larger item compared to smaller should return positive", TestValuedEnum.ITEM_2.compareTo(TestValuedEnum.ITEM_1) > 0);
        assertEquals("Difference between 1 and 2 must be -1", -1, TestValuedEnum.ITEM_1.compareTo(TestValuedEnum.ITEM_2));
        assertEquals("Difference between 3 and 1 must be 2", 2, TestValuedEnum.ITEM_3.compareTo(TestValuedEnum.ITEM_1));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets Defects4J known fault:
     * ValuedEnumTest::testCompareTo_otherEnumType
     *
     * ValuedEnum.compareTo(Object) must verify that the other object is of the same
     * Enum subclass. If passed an instance of a different ValuedEnum subclass,
     * it must throw ClassCastException rather than subtracting values across disparate enums.
     */
    @Test(timeout = 4000)
    public void testCompareTo_otherEnumType() {
        try {
            TestValuedEnum.ITEM_1.compareTo(AnotherValuedEnum.OTHER_1);
            fail("Expected ClassCastException when comparing different ValuedEnum subclasses");
        } catch (ClassCastException ex) {
            // Success: comparing disparate enum types must throw ClassCastException
            assertTrue("ClassCastException thrown as expected", true);
        }
    }

    @Test(timeout = 4000)
    public void testCompareTo_otherEnumTypeDifferentValues() {
        try {
            TestValuedEnum.ITEM_1.compareTo(AnotherValuedEnum.OTHER_2);
            fail("Expected ClassCastException when comparing different ValuedEnum subclasses with different values");
        } catch (ClassCastException ex) {
            assertTrue("ClassCastException thrown as expected", true);
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetEnum_nullClassThrowsIllegalArgumentException() {
        ValuedEnum.getEnum(null, 1);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testCompareTo_nullArgumentThrowsNullPointerException() {
        TestValuedEnum.ITEM_1.compareTo(null);
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testCompareTo_nonEnumArgumentThrowsClassCastException() {
        TestValuedEnum.ITEM_1.compareTo("A String is not an Enum");
    }

    @Test(timeout = 4000, expected = ClassCastException.class)
    public void testGetEnum_nonValuedEnumClassThrowsClassCastException() {
        // Trigger class loading for the NonValuedStandardEnum
        assertNotNull(NonValuedStandardEnum.NON_VALUED_ITEM);
        // Passing a standard non-ValuedEnum class causes the loop cast to fail
        ValuedEnum.getEnum(NonValuedStandardEnum.class, 1);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Reflection, & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerialization_preservesSingletonIdentity() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(TestValuedEnum.ITEM_1);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertNotNull("Deserialized object must not be null", deserialized);
        assertSame("Deserialized ValuedEnum must maintain singleton identity via readResolve",
                TestValuedEnum.ITEM_1, deserialized);
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode_contractIntegrity() {
        assertTrue("Reflexive equality must hold", TestValuedEnum.ITEM_1.equals(TestValuedEnum.ITEM_1));
        assertFalse("Different values within same enum class must not be equal", TestValuedEnum.ITEM_1.equals(TestValuedEnum.ITEM_2));
        assertFalse("Different enum classes with same value must not be equal", TestValuedEnum.ITEM_1.equals(AnotherValuedEnum.OTHER_1));
        assertFalse("Equality to null must return false", TestValuedEnum.ITEM_1.equals(null));
        assertFalse("Equality to non-Enum type must return false", TestValuedEnum.ITEM_1.equals("Item 1"));

        assertEquals("Equal objects must have equal hashCodes",
                TestValuedEnum.ITEM_1.hashCode(), TestValuedEnum.ITEM_1.hashCode());
    }

    @Test(timeout = 4000)
    public void testGetEnumListAndIterator_integration() {
        List list = Enum.getEnumList(TestValuedEnum.class);
        assertNotNull("Enum list must not be null", list);
        assertTrue("Enum list must contain items", list.size() >= 7);
        assertTrue("Enum list must contain ITEM_1", list.contains(TestValuedEnum.ITEM_1));

        Iterator it = Enum.iterator(TestValuedEnum.class);
        assertNotNull("Iterator must not be null", it);
        assertTrue("Iterator must have next item", it.hasNext());
        Object firstIterated = it.next();
        assertTrue("Iterated elements must be instances of ValuedEnum", firstIterated instanceof ValuedEnum);
    }
}