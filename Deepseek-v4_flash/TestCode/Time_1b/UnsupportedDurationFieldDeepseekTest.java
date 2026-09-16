package org.joda.time.field;

import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Deep test suite for {@link UnsupportedDurationField} targeting maximum line/branch coverage
 * and the known compareTo contract violation (Defects4J Time-1).
 */
public class UnsupportedDurationFieldDeepseekTest {

    // -----------------------------------------------------------------------
    // Singleton factory
    // -----------------------------------------------------------------------

    /**
     * @target getInstance(DurationFieldType)
     * @scenario Same type requested twice – must return the cached instance.
     * @defectRisk Singleton cache not used (could break identity assumption).
     */
    @Test(timeout = 4000)
    public void testGetInstance_returnsCachedInstance() {
        DurationFieldType type = DurationFieldType.eras();
        UnsupportedDurationField f1 = UnsupportedDurationField.getInstance(type);
        UnsupportedDurationField f2 = UnsupportedDurationField.getInstance(type);
        assertSame("Cached instance must be returned", f1, f2);
    }

    /**
     * @target getInstance(DurationFieldType)
     * @scenario Different types requested – must return different instances.
     * @defectRisk Cache returned same object for different types.
     */
    @Test(timeout = 4000)
    public void testGetInstance_differentTypes() {
        UnsupportedDurationField eras = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        UnsupportedDurationField years = UnsupportedDurationField.getInstance(DurationFieldType.years());
        assertNotSame("Different types must yield different instances", eras, years);
    }

    // -----------------------------------------------------------------------
    // Simple accessors
    // -----------------------------------------------------------------------

    /**
     * @target getType()
     * @scenario Verify returned type matches the one used at construction.
     * @defectRisk getType() returns null or wrong type.
     */
    @Test(timeout = 4000)
    public void testGetType() {
        DurationFieldType type = DurationFieldType.weeks();
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(type);
        assertSame("getType must return the field's type", type, field.getType());
    }

    /**
     * @target getName()
     * @scenario Verify name matches the type's name.
     * @defectRisk getName() returns null or wrong string.
     */
    @Test(timeout = 4000)
    public void testGetName() {
        DurationFieldType type = DurationFieldType.months();
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(type);
        assertEquals("getName must equal type.getName()", type.getName(), field.getName());
    }

    /**
     * @target isSupported()
     * @scenario Always returns false.
     * @defectRisk Returns true (indicating support).
     */
    @Test(timeout = 4000)
    public void testIsSupported() {
        assertFalse("isSupported must be false",
                UnsupportedDurationField.getInstance(DurationFieldType.eras()).isSupported());
    }

    /**
     * @target isPrecise()
     * @scenario Always returns true.
     * @defectRisk Returns false (misleading precision claim).
     */
    @Test(timeout = 4000)
    public void testIsPrecise() {
        assertTrue("isPrecise must be true",
                UnsupportedDurationField.getInstance(DurationFieldType.halfdays()).isPrecise());
    }

    /**
     * @target getUnitMillis()
     * @scenario Always returns 0.
     * @defectRisk Returns non-zero value (incorrect unit).
     */
    @Test(timeout = 4000)
    public void testGetUnitMillis() {
        assertEquals("getUnitMillis must be 0", 0L,
                UnsupportedDurationField.getInstance(DurationFieldType.minutes()).getUnitMillis());
    }

    // -----------------------------------------------------------------------
    // Unsupported operations that must throw UnsupportedOperationException
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetValue_long() {
        UnsupportedDurationField.getInstance(DurationFieldType.centuries()).getValue(1000L);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetValueAsLong_long() {
        UnsupportedDurationField.getInstance(DurationFieldType.centuries()).getValueAsLong(1000L);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetValue_long_long() {
        UnsupportedDurationField.getInstance(DurationFieldType.centuries()).getValue(1000L, 50000L);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetValueAsLong_long_long() {
        UnsupportedDurationField.getInstance(DurationFieldType.centuries()).getValueAsLong(1000L, 50000L);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetMillis_int() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).getMillis(5);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetMillis_long() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).getMillis(5L);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetMillis_int_long() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).getMillis(5, 1000L);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetMillis_long_long() {
        UnsupportedDurationField.getInstance(DurationFieldType.millis()).getMillis(5L, 1000L);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testAdd_long_int() {
        UnsupportedDurationField.getInstance(DurationFieldType.seconds()).add(1000L, 10);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testAdd_long_long() {
        UnsupportedDurationField.getInstance(DurationFieldType.seconds()).add(1000L, 10L);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetDifference_long_long() {
        UnsupportedDurationField.getInstance(DurationFieldType.days()).getDifference(2000L, 1000L);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testGetDifferenceAsLong_long_long() {
        UnsupportedDurationField.getInstance(DurationFieldType.days()).getDifferenceAsLong(2000L, 1000L);
    }

    // -----------------------------------------------------------------------
    // compareTo
    // -----------------------------------------------------------------------

    /**
     * @target compareTo(DurationField)
     * @scenario Compared to a supported field – must return positive (1).
     * @defectRisk Returns zero or negative for supported fields.
     */
    @Test(timeout = 4000)
    public void testCompareTo_withSupportedField() {
        UnsupportedDurationField unsupported = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        DurationField supported = ISOChronology.getInstanceUTC().days();
        int result = unsupported.compareTo(supported);
        assertTrue("compareTo must be >0 when compared to supported field", result > 0);
    }

    /**
     * @target compareTo(DurationField)
     * @scenario Compared to another unsupported field – must return 0.
     * @defectRisk Non-zero result for unsupported (breaks identity assumption).
     */
    @Test(timeout = 4000)
    public void testCompareTo_withUnsupportedField() {
        UnsupportedDurationField f1 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField f2 = UnsupportedDurationField.getInstance(DurationFieldType.months());
        assertEquals("compareTo must be 0 for two unsupported fields", 0, f1.compareTo(f2));
    }

    /**
     * @target compareTo(DurationField) (contract defect: Time-1)
     * @scenario Comparable anti-symmetry: both directions must not return positive.
     * @defectRisk unsupported.compareTo(supported) > 0 and supported.compareTo(unsupported) > 0
     *             violates the Comparable contract.
     */
    @Test(timeout = 4000)
    public void testCompareTo_contractAntiSymmetry() {
        DurationField unsupported = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        DurationField supported = ISOChronology.getInstanceUTC().days();
        int c1 = unsupported.compareTo(supported);
        int c2 = supported.compareTo(unsupported);
        assertFalse("Comparable contract broken: both comparisons cannot be > 0", c1 > 0 && c2 > 0);
    }

    // -----------------------------------------------------------------------
    // equals
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEquals_sameInstance() {
        UnsupportedDurationField f = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        assertTrue("equals must be true for same instance", f.equals(f));
    }

    @Test(timeout = 4000)
    public void testEquals_differentType() {
        UnsupportedDurationField f1 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField f2 = UnsupportedDurationField.getInstance(DurationFieldType.months());
        assertFalse("Different types must not be equal", f1.equals(f2));
    }

    @Test(timeout = 4000)
    public void testEquals_null() {
        UnsupportedDurationField f = UnsupportedDurationField.getInstance(DurationFieldType.weeks());
        assertFalse("Equals to null must be false", f.equals(null));
    }

    @Test(timeout = 4000)
    public void testEquals_nonUnsupportedDurationField() {
        UnsupportedDurationField unsupported = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        DurationField supported = ISOChronology.getInstanceUTC().days();
        assertFalse("Equals to a supported DurationField must be false", unsupported.equals(supported));
    }

    @Test(timeout = 4000)
    public void testEquals_sameType() {
        // Two references to the same type are identical due to cache
        UnsupportedDurationField f1 = UnsupportedDurationField.getInstance(DurationFieldType.centuries());
        UnsupportedDurationField f2 = UnsupportedDurationField.getInstance(DurationFieldType.centuries());
        assertTrue("Equals for same type must be true (cached instance)", f1.equals(f2));
    }

    // -----------------------------------------------------------------------
    // hashCode
    // -----------------------------------------------------------------------

    /**
     * @target hashCode()
     * @scenario Consistent with equals: equal objects have equal hash codes.
     * @defectRisk Hash code not computed from the name (breaks contract).
     */
    @Test(timeout = 4000)
    public void testHashCode_consistency() {
        UnsupportedDurationField f1 = UnsupportedDurationField.getInstance(DurationFieldType.millis());
        UnsupportedDurationField f2 = UnsupportedDurationField.getInstance(DurationFieldType.millis());
        assertEquals("Equal objects must have equal hash codes", f1.hashCode(), f2.hashCode());
    }

    /**
     * @target hashCode()
     * @scenario Different types produce different hash codes.
     * @defectRisk All unsupported fields return same hash code.
     */
    @Test(timeout = 4000)
    public void testHashCode_different() {
        UnsupportedDurationField f1 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        UnsupportedDurationField f2 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        assertNotEquals("Different types should have different hash codes (low probability of collision)",
                f1.hashCode(), f2.hashCode());
    }

    // -----------------------------------------------------------------------
    // toString
    // -----------------------------------------------------------------------

    /**
     * @target toString()
     * @scenario Verify format "UnsupportedDurationField[name]".
     * @defectRisk Incorrect format or missing information.
     */
    @Test(timeout = 4000)
    public void testToString() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.halfdays());
        String expected = "UnsupportedDurationField[" + field.getName() + "]";
        assertEquals("toString must match expected format", expected, field.toString());
    }
}