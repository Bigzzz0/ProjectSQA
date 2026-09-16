package org.joda.time.field;

import org.joda.time.*;
import org.joda.time.chrono.*;
import org.joda.time.field.UnsupportedDurationField;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Class Under Test: org.joda.time.field.UnsupportedDurationField
 * ---------------------------------------------------------------------------------------------------------
 * Target / Method          | Decision / Branch Condition               | Test Objective / Fault Targeted
 * ---------------------------------------------------------------------------------------------------------
 * getInstance()            | cCache == null vs != null                 | Verify initialization & caching
 *                          | field == null vs cached                   | Verify singleton identity across calls
 * getType() / getName()    | Normal state                              | Verify returns correct DurationFieldType
 * isSupported()            | Invariant                                 | Must strictly return false
 * isPrecise()              | Invariant                                 | Must strictly return true
 * getUnitMillis()          | Invariant                                 | Must strictly return 0L
 * getValue*() variants     | Always throws UOE                         | 4 methods: getValue(l), getValueAsLong(l),
 *                          |                                           | getValue(l,l), getValueAsLong(l,l)
 * getMillis*() variants    | Always throws UOE                         | 4 methods: getMillis(i), getMillis(l),
 *                          |                                           | getMillis(i,l), getMillis(l,l)
 * add*() variants          | Always throws UOE                         | 2 methods: add(l,i), add(l,l)
 * getDifference*() vars    | Always throws UOE                         | 2 methods: getDifference, getDifferenceAsLong
 * compareTo()              | durationField.isSupported() == true       | Must return 0 or negative relative to supported
 *                          |                                           | (Exposes anti-symmetry defect Time-1)
 *                          | durationField.isSupported() == false      | Compares two unsupported fields -> 0
 * equals()                 | this == obj                               | Reflexive equality
 *                          | !(obj instanceof UnsupportedDurationField)| Return false for null / foreign type
 *                          | other.getName() == null (both null)       | Custom type with null name equality
 *                          | other.getName() != null (same/diff)       | Equality based on field name
 * hashCode()               | Normal state                              | Must match getName().hashCode()
 * toString()               | Normal state                              | Format: UnsupportedDurationField[name]
 * readResolve()            | Deserialization lifecycle                 | Ensure deserialized equals cached instance
 * ---------------------------------------------------------------------------------------------------------
 */
public class UnsupportedDurationFieldGeminiTest {

    // =========================================================================
    // Partition A: Factory & Invariant Properties
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryAndSingletonCaching() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        UnsupportedDurationField field3 = UnsupportedDurationField.getInstance(DurationFieldType.centuries());

        assertNotNull("Instance must not be null", field1);
        assertSame("Factory must return cached singleton instance", field1, field2);
        assertNotSame("Distinct types must return distinct instances", field1, field3);
    }

    @Test(timeout = 4000)
    public void testBasicPropertiesAndInvariants() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());

        assertEquals("getType() should return requested DurationFieldType", DurationFieldType.eras(), field.getType());
        assertEquals("getName() should match DurationFieldType name", "eras", field.getName());
        assertFalse("isSupported() must always be false", field.isSupported());
        assertTrue("isPrecise() must always be true", field.isPrecise());
        assertEquals("getUnitMillis() must always be 0", 0L, field.getUnitMillis());
    }

    // =========================================================================
    // Partition B: Unsupported Operation Exceptions (All 12 Calculation Methods)
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetValueLongThrowsUnsupported() {
        UnsupportedDurationField.getInstance(DurationFieldType.eras()).getValue(1000L);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetValueAsLongLongThrowsUnsupported() {
        UnsupportedDurationField.getInstance(DurationFieldType.eras()).getValueAsLong(1000L);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetValueLongLongThrowsUnsupported() {
        UnsupportedDurationField.getInstance(DurationFieldType.eras()).getValue(1000L, 500L);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetValueAsLongLongLongThrowsUnsupported() {
        UnsupportedDurationField.getInstance(DurationFieldType.eras()).getValueAsLong(1000L, 500L);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetMillisIntThrowsUnsupported() {
        UnsupportedDurationField.getInstance(DurationFieldType.eras()).getMillis(10);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetMillisLongThrowsUnsupported() {
        UnsupportedDurationField.getInstance(DurationFieldType.eras()).getMillis(10L);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetMillisIntLongThrowsUnsupported() {
        UnsupportedDurationField.getInstance(DurationFieldType.eras()).getMillis(10, 500L);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetMillisLongLongThrowsUnsupported() {
        UnsupportedDurationField.getInstance(DurationFieldType.eras()).getMillis(10L, 500L);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testAddLongIntThrowsUnsupported() {
        UnsupportedDurationField.getInstance(DurationFieldType.eras()).add(1000L, 5);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testAddLongLongThrowsUnsupported() {
        UnsupportedDurationField.getInstance(DurationFieldType.eras()).add(1000L, 5L);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetDifferenceThrowsUnsupported() {
        UnsupportedDurationField.getInstance(DurationFieldType.eras()).getDifference(1000L, 500L);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetDifferenceAsLongThrowsUnsupported() {
        UnsupportedDurationField.getInstance(DurationFieldType.eras()).getDifferenceAsLong(1000L, 500L);
    }

    @Test(timeout = 4000)
    public void testExceptionMessageContainsType() {
        try {
            UnsupportedDurationField.getInstance(DurationFieldType.eras()).getValue(100L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue("Exception message should reference field type",
                    e.getMessage() != null && e.getMessage().contains("eras"));
        }
    }

    // =========================================================================
    // Partition C: Comparable Contract & Defect Analysis (Time-1)
    // =========================================================================

    @Test(timeout = 4000)
    public void testCompareToBothUnsupported() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.centuries());

        assertEquals("Comparing two unsupported fields should return 0", 0, field1.compareTo(field2));
        assertEquals("Comparing two unsupported fields should be symmetric", 0, field2.compareTo(field1));
    }

    @Test(timeout = 4000)
    public void testCompareToContractWithSupportedDurationField_Time1() {
        DurationField unsupported = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        DurationField supported = ISOChronology.getInstanceUTC().days();

        int c1 = unsupported.compareTo(supported);
        int c2 = supported.compareTo(unsupported);

        // In standard Comparable contract: sgn(x.compareTo(y)) == -sgn(y.compareTo(x))
        // Both c1 and c2 cannot simultaneously be greater than 0.
        assertFalse("Comparable contract broken: both cannot be > 0 (Time-1 defect)", c1 > 0 && c2 > 0);
    }

    // =========================================================================
    // Partition D: Object Identity, Equals, HashCode, Serialization & String
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        UnsupportedDurationField eras1 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        UnsupportedDurationField eras2 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        UnsupportedDurationField centuries = UnsupportedDurationField.getInstance(DurationFieldType.centuries());

        // Reflexive
        assertTrue("Self-equality check", eras1.equals(eras1));

        // Symmetric
        assertTrue("Symmetric equality", eras1.equals(eras2) && eras2.equals(eras1));
        assertEquals("Equal objects must have identical hashCode", eras1.hashCode(), eras2.hashCode());
        assertEquals("HashCode must match name.hashCode()", eras1.getName().hashCode(), eras1.hashCode());

        // Inequality with distinct type
        assertFalse("Different field types must not be equal", eras1.equals(centuries));

        // Incompatible types and null
        assertFalse("equals(null) must return false", eras1.equals(null));
        assertFalse("equals(non-DurationField) must return false", eras1.equals("eras"));
        assertFalse("equals(supportedDurationField) must return false",
                eras1.equals(ISOChronology.getInstanceUTC().days()));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullFieldNames() {
        DurationFieldType nullNameType1 = new DurationFieldType(null) {
            private static final long serialVersionUID = 1L;
            @Override
            public DurationField getField(Chronology chronology) {
                return UnsupportedDurationField.getInstance(this);
            }
        };
        DurationFieldType nullNameType2 = new DurationFieldType(null) {
            private static final long serialVersionUID = 2L;
            @Override
            public DurationField getField(Chronology chronology) {
                return UnsupportedDurationField.getInstance(this);
            }
        };

        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(nullNameType1);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(nullNameType2);
        UnsupportedDurationField fieldWithRealName = UnsupportedDurationField.getInstance(DurationFieldType.eras());

        assertTrue("Both having null names should be equal", field1.equals(field2));
        assertFalse("Null name vs real name should not be equal", field1.equals(fieldWithRealName));
        assertFalse("Real name vs null name should not be equal", fieldWithRealName.equals(field1));
    }

    @Test(timeout = 4000)
    public void testToStringFormat() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.months());
        assertEquals("ToString format should match UnsupportedDurationField[months]",
                "UnsupportedDurationField[months]", field.toString());
    }

    @Test(timeout = 4000)
    public void testSerializationReadResolveSingleton() throws Exception {
        UnsupportedDurationField original = UnsupportedDurationField.getInstance(DurationFieldType.years());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertSame("readResolve() must restore the cached singleton instance", original, deserialized);
    }
}