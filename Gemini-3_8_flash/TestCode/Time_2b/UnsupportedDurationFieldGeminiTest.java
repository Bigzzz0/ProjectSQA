package org.joda.time.field;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import org.joda.time.Chronology;
import org.joda.time.DateTimeFieldType;
import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;
import org.joda.time.Partial;
import org.joda.time.chrono.ISOChronology;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.joda.time.field.UnsupportedDurationField
 *
 * Decision / Branch Coverage Targets:
 * 1. getInstance(DurationFieldType):
 *    - cCache == null (fresh initialization via reflection)
 *    - cCache != null, field == null (first access for specific DurationFieldType)
 *    - cCache != null, field != null (cached retrieval, singleton identity check)
 * 2. Accessors & Metadata:
 *    - getType() -> returns underlying DurationFieldType
 *    - getName() -> returns DurationFieldType.getName()
 *    - isSupported() -> false always
 *    - isPrecise() -> true always
 *    - getUnitMillis() -> 0L always
 * 3. Calculation Operations (UnsupportedOperationException guards):
 *    - getValue(long), getValueAsLong(long)
 *    - getValue(long, long), getValueAsLong(long, long)
 *    - getMillis(int), getMillis(long)
 *    - getMillis(int, long), getMillis(long, long)
 *    - add(long, int), add(long, long)
 *    - getDifference(long, long), getDifferenceAsLong(long, long)
 * 4. compareTo(DurationField):
 *    - Returns 0 for identical, other UnsupportedDurationField, supported DurationField, or null
 * 5. equals(Object):
 *    - this == obj -> true
 *    - !(obj instanceof UnsupportedDurationField) -> false (null, wrong types, other DurationFields)
 *    - other.getName() == null && getName() == null -> true
 *    - other.getName() == null && getName() != null -> false
 *    - other.getName() != null && getName() == null -> false
 *    - other.getName() != null && names equal -> true
 *    - other.getName() != null && names not equal -> false
 * 6. Object Lifecycle & Integrity:
 *    - hashCode() contract -> matches getName().hashCode()
 *    - toString() -> "UnsupportedDurationField[" + getName() + "]"
 *    - Serialization & readResolve() -> deserialized instance must resolve to the cached singleton instance
 * 7. Defect-Targeted Branch:
 *    - Defects4J TestPartial_Basics::testWith_baseAndArgHaveNoRange
 *      Trigger Partial.with(...) when fields have UnsupportedDurationField as range (e.g. Era and Year).
 */
public class UnsupportedDurationFieldGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetInstanceAndBasicAccessors() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.days());
        assertNotNull("Instance should not be null", field);
        assertSame("DurationFieldType must match input", DurationFieldType.days(), field.getType());
        assertEquals("Name must match DurationFieldType name", "days", field.getName());
        assertFalse("isSupported must return false", field.isSupported());
        assertTrue("isPrecise must return true", field.isPrecise());
        assertEquals("getUnitMillis must return 0", 0L, field.getUnitMillis());
    }

    @Test(timeout = 4000)
    public void testGetInstanceCachingSingletonBehavior() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        assertSame("Subsequent getInstance calls must return the identical cached instance", field1, field2);

        UnsupportedDurationField field3 = UnsupportedDurationField.getInstance(DurationFieldType.minutes());
        assertNotSame("Distinct DurationFieldTypes must produce distinct instances", field1, field3);
    }

    @Test(timeout = 4000)
    public void testCompareToContract() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.months());
        DurationField supportedField = ISOChronology.getInstanceUTC().days();

        assertEquals("Self comparison must return 0", 0, field1.compareTo(field1));
        assertEquals("Comparison against another unsupported field must return 0", 0, field1.compareTo(field2));
        assertEquals("Comparison against supported field must return 0", 0, field1.compareTo(supportedField));
        assertEquals("Comparison against null must return 0", 0, field1.compareTo(null));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCustomFieldTypeWithNullName() {
        DurationFieldType nullNameType1 = new DurationFieldType(null) {
            private static final long serialVersionUID = 1L;
            @Override
            public DurationField getField(Chronology chronology) {
                return null;
            }
        };

        DurationFieldType nullNameType2 = new DurationFieldType(null) {
            private static final long serialVersionUID = 1L;
            @Override
            public DurationField getField(Chronology chronology) {
                return null;
            }
        };

        UnsupportedDurationField fieldNull1 = UnsupportedDurationField.getInstance(nullNameType1);
        UnsupportedDurationField fieldNull2 = UnsupportedDurationField.getInstance(nullNameType2);
        UnsupportedDurationField fieldStandard = UnsupportedDurationField.getInstance(DurationFieldType.seconds());

        assertNull("Name should be null", fieldNull1.getName());
        assertEquals("toString should format null name gracefully", "UnsupportedDurationField[null]", fieldNull1.toString());

        // equals branches with null names
        assertTrue("Equal when both have null names", fieldNull1.equals(fieldNull2));
        assertFalse("Not equal when other has null name but this has non-null name", fieldStandard.equals(fieldNull1));
        assertFalse("Not equal when other has non-null name but this has null name", fieldNull1.equals(fieldStandard));
    }

    @Test(timeout = 4000)
    public void testNullDurationFieldTypeHandling() {
        UnsupportedDurationField nullTypeField1 = UnsupportedDurationField.getInstance(null);
        UnsupportedDurationField nullTypeField2 = UnsupportedDurationField.getInstance(null);

        assertSame("Caching should handle null type key deterministically", nullTypeField1, nullTypeField2);
        assertNull("getType should return null", nullTypeField1.getType());

        try {
            nullTypeField1.getName();
            fail("Expected NullPointerException when iType is null");
        } catch (NullPointerException expected) {
            // Success
        }

        try {
            nullTypeField1.getValue(100L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            assertTrue("Exception message should reflect null type", ex.getMessage().contains("null field is unsupported"));
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Target Defect: org.joda.time.TestPartial_Basics::testWith_baseAndArgHaveNoRange
     * Failure symptom: java.lang.IllegalArgumentException: Types array must not contain duplicate: era and year
     * Underlying issue relates to fields where rangeDurationField is UnsupportedDurationField / null.
     */
    @Test(timeout = 4000)
    public void testDefectPartialWithBaseAndArgHaveNoRange() {
        Partial test = new Partial(DateTimeFieldType.year(), 2005);
        Partial result = test.with(DateTimeFieldType.era(), 1);

        assertEquals("Partial should contain 2 fields after with()", 2, result.size());
        assertEquals("Era field must be at index 0", 0, result.indexOf(DateTimeFieldType.era()));
        assertEquals("Year field must be at index 1", 1, result.indexOf(DateTimeFieldType.year()));
        assertEquals("Era value should match inserted value", 1, result.getValue(0));
        assertEquals("Year value should be preserved", 2005, result.getValue(1));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testAllCalculationMethodsThrowUnsupportedOperationException() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.weeks());
        String expectedMessage = "weeks field is unsupported";

        try {
            field.getValue(12345L);
            fail("getValue(long) should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals(expectedMessage, e.getMessage());
        }

        try {
            field.getValueAsLong(12345L);
            fail("getValueAsLong(long) should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals(expectedMessage, e.getMessage());
        }

        try {
            field.getValue(12345L, 67890L);
            fail("getValue(long, long) should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals(expectedMessage, e.getMessage());
        }

        try {
            field.getValueAsLong(12345L, 67890L);
            fail("getValueAsLong(long, long) should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals(expectedMessage, e.getMessage());
        }

        try {
            field.getMillis(10);
            fail("getMillis(int) should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals(expectedMessage, e.getMessage());
        }

        try {
            field.getMillis(10L);
            fail("getMillis(long) should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals(expectedMessage, e.getMessage());
        }

        try {
            field.getMillis(10, 67890L);
            fail("getMillis(int, long) should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals(expectedMessage, e.getMessage());
        }

        try {
            field.getMillis(10L, 67890L);
            fail("getMillis(long, long) should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals(expectedMessage, e.getMessage());
        }

        try {
            field.add(67890L, 10);
            fail("add(long, int) should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals(expectedMessage, e.getMessage());
        }

        try {
            field.add(67890L, 10L);
            fail("add(long, long) should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals(expectedMessage, e.getMessage());
        }

        try {
            field.getDifference(1000L, 500L);
            fail("getDifference(long, long) should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals(expectedMessage, e.getMessage());
        }

        try {
            field.getDifferenceAsLong(1000L, 500L);
            fail("getDifferenceAsLong(long, long) should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCalculationMethodsWithExtremeValues() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.millis());

        try {
            field.getValue(Long.MIN_VALUE, Long.MAX_VALUE);
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("millis field is unsupported", e.getMessage());
        }

        try {
            field.add(Long.MAX_VALUE, Long.MIN_VALUE);
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("millis field is unsupported", e.getMessage());
        }

        try {
            field.getDifference(Long.MIN_VALUE, Long.MAX_VALUE);
            fail("Should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertEquals("millis field is unsupported", e.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        UnsupportedDurationField eras1 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        UnsupportedDurationField eras2 = UnsupportedDurationField.getInstance(DurationFieldType.eras());
        UnsupportedDurationField centuries = UnsupportedDurationField.getInstance(DurationFieldType.centuries());

        // Reflexive
        assertTrue("Reflexive equals check", eras1.equals(eras1));

        // Symmetric
        assertTrue("Symmetric equals check", eras1.equals(eras2) && eras2.equals(eras1));

        // False branches
        assertFalse("Different types must not be equal", eras1.equals(centuries));
        assertFalse("Comparison against null must be false", eras1.equals(null));
        assertFalse("Comparison against non-DurationField must be false", eras1.equals("StringObject"));
        assertFalse("Comparison against supported DurationField must be false", eras1.equals(ISOChronology.getInstanceUTC().eras()));

        // Hashcode contract
        assertEquals("Equal objects must have equal hashCodes", eras1.hashCode(), eras2.hashCode());
        assertEquals("hashCode should match name hashCode", "eras".hashCode(), eras1.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.halfdays());
        assertEquals("UnsupportedDurationField[halfdays]", field.toString());
    }

    @Test(timeout = 4000)
    public void testSerializationAndReadResolveSingletonContract() throws Exception {
        UnsupportedDurationField original = UnsupportedDurationField.getInstance(DurationFieldType.centuries());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertTrue("Deserialized object must be UnsupportedDurationField", deserialized instanceof UnsupportedDurationField);
        assertSame("Deserialization via readResolve must return the cached singleton instance", original, deserialized);
    }

    @Test(timeout = 4000)
    public void testCacheInitializationBranchViaReflection() throws Exception {
        Field cacheField = UnsupportedDurationField.class.getDeclaredField("cCache");
        cacheField.setAccessible(true);
        Object originalCache = cacheField.get(null);

        try {
            // Force cache to null to hit the (cCache == null) branch
            cacheField.set(null, null);

            UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.eras());
            assertNotNull("Field retrieved after cache reset must not be null", field);
            assertSame("Retrieved instance should be cached immediately", field, UnsupportedDurationField.getInstance(DurationFieldType.eras()));
        } finally {
            // Restore original cache state
            cacheField.set(null, originalCache);
        }
    }
}