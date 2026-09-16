package org.joda.time.field;

import org.junit.Test;
import static org.junit.Assert.*;

import org.joda.time.DurationField;
import org.joda.time.DurationFieldType;

public class UnsupportedDurationFieldDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: UnsupportedDurationField - a singleton cache-based immutable placeholder.
     * 
     * Branches covered:
     * 1. getInstance(DurationFieldType):
     *    - cCache == null (first call) -> create cache, field = null
     *    - cCache != null -> field = cCache.get(type)
     *    - field == null -> create new instance and put in cache
     *    - field != null -> return cached instance
     * 2. equals(Object):
     *    - this == obj -> true
     *    - obj instanceof UnsupportedDurationField -> compare names
     *      - other.getName() == null -> return (getName() == null)
     *      - other.getName() != null -> return other.getName().equals(getName())
     *    - obj not instance -> false
     * 3. hashCode(): getName().hashCode() (null name would NPE - but type always has name)
     * 4. toString(): "UnsupportedDurationField[" + getName() + ']'
     * 5. readResolve(): returns getInstance(iType) - singleton preservation
     * 6. All calculation methods throw UnsupportedOperationException
     * 7. Simple accessors: getType(), getName(), isSupported()=false, isPrecise()=true,
     *    getUnitMillis()=0, compareTo()=0
     * 
     * Defect targeting:
     * The known defect is in TestPartial_Basics::testWith_baseAndArgHaveNoRange
     * -> IllegalArgumentException: "Types array must not contain duplicate: era and year"
     * This defect is triggered when a Partial.with() is called with a field that
     * already exists in the partial, causing duplicate types. UnsupportedDurationField
     * is used as a placeholder for unsupported fields in such scenarios. The test
     * below verifies that UnsupportedDurationField instances for different types
     * (era and year) are distinct and correctly identified, and that the singleton
     * cache does not incorrectly conflate them, which could lead to the duplicate
     * type detection failure.
     * 
     * Boundary values:
     * - null DurationFieldType (not allowed by API but tested for defensive behavior)
     * - DurationFieldType.era() and DurationFieldType.years() - distinct types
     * - Same type called twice - should return same instance (singleton)
     * - equals with null, different type, same name, different name
     * - hashCode consistency with equals
     * - toString format
     * - All unsupported operation methods with various argument values
     */
    
    // ------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ------------------------------------------------------------------------
    
    @Test(timeout = 4000)
    public void testGetInstance_createsAndCaches() {
        DurationFieldType type = DurationFieldType.years();
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(type);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(type);
        
        assertNotNull("First instance should not be null", field1);
        assertSame("Cached instance should be returned for same type", field1, field2);
        assertEquals("Type should match", type, field1.getType());
        assertEquals("Name should match type name", type.getName(), field1.getName());
    }
    
    @Test(timeout = 4000)
    public void testGetInstance_differentTypes() {
        UnsupportedDurationField era = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField year = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        assertNotSame("Different types should return different instances", era, year);
        assertFalse("Different types should not be equal", era.equals(year));
        assertEquals("Era name", "era", era.getName());
        assertEquals("Year name", "years", year.getName());
    }
    
    @Test(timeout = 4000)
    public void testGetType() {
        DurationFieldType type = DurationFieldType.months();
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(type);
        assertSame("getType should return the exact type", type, field.getType());
    }
    
    @Test(timeout = 4000)
    public void testGetName() {
        DurationFieldType type = DurationFieldType.weeks();
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(type);
        assertEquals("getName should return type name", type.getName(), field.getName());
    }
    
    @Test(timeout = 4000)
    public void testIsSupported() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.days());
        assertFalse("isSupported should always return false", field.isSupported());
    }
    
    @Test(timeout = 4000)
    public void testIsPrecise() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.halfdays());
        assertTrue("isPrecise should always return true", field.isPrecise());
    }
    
    @Test(timeout = 4000)
    public void testGetUnitMillis() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.centuries());
        assertEquals("getUnitMillis should always return 0", 0L, field.getUnitMillis());
    }
    
    @Test(timeout = 4000)
    public void testCompareTo() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.months());
        assertEquals("compareTo should always return 0", 0, field1.compareTo(field2));
        assertEquals("compareTo with itself should return 0", 0, field1.compareTo(field1));
    }
    
    // ------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ------------------------------------------------------------------------
    
    @Test(timeout = 4000)
    public void testGetInstance_nullType() {
        try {
            UnsupportedDurationField.getInstance(null);
            fail("Expected NullPointerException or similar for null type");
        } catch (NullPointerException e) {
            // Expected - null type not allowed
        } catch (RuntimeException e) {
            // Accept any runtime exception as defensive behavior
        }
    }
    
    @Test(timeout = 4000)
    public void testGetInstance_sameTypeMultipleTimes() {
        DurationFieldType type = DurationFieldType.years();
        UnsupportedDurationField first = UnsupportedDurationField.getInstance(type);
        UnsupportedDurationField second = UnsupportedDurationField.getInstance(type);
        UnsupportedDurationField third = UnsupportedDurationField.getInstance(type);
        
        assertSame("All instances for same type should be identical", first, second);
        assertSame("All instances for same type should be identical", first, third);
    }
    
    @Test(timeout = 4000)
    public void testGetInstance_manyTypes() {
        // Exercise cache with many types to ensure no cross-contamination
        DurationFieldType[] types = {
            DurationFieldType.eras(), DurationFieldType.centuries(), DurationFieldType.years(),
            DurationFieldType.months(), DurationFieldType.weeks(), DurationFieldType.days(),
            DurationFieldType.halfdays(), DurationFieldType.hours(), DurationFieldType.minutes(),
            DurationFieldType.seconds(), DurationFieldType.millis()
        };
        
        UnsupportedDurationField[] fields = new UnsupportedDurationField[types.length];
        for (int i = 0; i < types.length; i++) {
            fields[i] = UnsupportedDurationField.getInstance(types[i]);
        }
        
        // Verify all distinct
        for (int i = 0; i < fields.length; i++) {
            for (int j = i + 1; j < fields.length; j++) {
                assertNotSame("Fields for different types should not be same", fields[i], fields[j]);
                assertFalse("Fields for different types should not be equal", fields[i].equals(fields[j]));
            }
        }
        
        // Verify each has correct name
        for (int i = 0; i < types.length; i++) {
            assertEquals("Name mismatch for type " + i, types[i].getName(), fields[i].getName());
        }
    }
    
    @Test(timeout = 4000)
    public void testEquals_null() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        assertFalse("equals(null) should be false", field.equals(null));
    }
    
    @Test(timeout = 4000)
    public void testEquals_sameObject() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        assertTrue("equals with itself should be true", field.equals(field));
    }
    
    @Test(timeout = 4000)
    public void testEquals_differentClass() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        Object other = new Object();
        assertFalse("equals with different class should be false", field.equals(other));
    }
    
    @Test(timeout = 4000)
    public void testEquals_sameNameDifferentInstance() {
        DurationFieldType type = DurationFieldType.years();
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(type);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(type);
        assertTrue("Same type instances should be equal", field1.equals(field2));
    }
    
    @Test(timeout = 4000)
    public void testEquals_differentName() {
        UnsupportedDurationField era = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField year = UnsupportedDurationField.getInstance(DurationFieldType.years());
        assertFalse("Different type instances should not be equal", era.equals(year));
    }
    
    @Test(timeout = 4000)
    public void testHashCode_consistency() {
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        assertEquals("Equal objects must have equal hashCodes", field1.hashCode(), field2.hashCode());
        assertEquals("hashCode should be stable", field1.hashCode(), field1.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testHashCode_differentTypes() {
        UnsupportedDurationField era = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField year = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        // Different names should generally produce different hashCodes (not guaranteed but expected)
        assertNotEquals("Different names should have different hashCodes", era.hashCode(), year.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testToString() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        assertEquals("toString format", "UnsupportedDurationField[years]", field.toString());
    }
    
    @Test(timeout = 4000)
    public void testToString_era() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.era());
        assertEquals("toString format for era", "UnsupportedDurationField[era]", field.toString());
    }
    
    // ------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone
    // ------------------------------------------------------------------------
    
    /**
     * Defect-targeted test: The known defect is that Partial.with() fails with
     * "Types array must not contain duplicate: era and year" when trying to add
     * a field that already exists. UnsupportedDurationField is used as a placeholder
     * for unsupported fields. This test verifies that era and year are correctly
     * distinguished as different types, ensuring that the duplicate detection
     * logic in Partial can correctly identify when a field is being added twice.
     * If UnsupportedDurationField incorrectly conflated era and year (e.g., by
     * returning the same instance or making them equal), the duplicate detection
     * would fail and the IllegalArgumentException would not be thrown when expected.
     */
    @Test(timeout = 4000)
    public void testDefect_eraAndYearAreDistinct() {
        // Get unsupported fields for era and year
        UnsupportedDurationField eraField = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField yearField = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        // They must be different instances
        assertNotSame("Era and year must be different instances", eraField, yearField);
        
        // They must not be equal
        assertFalse("Era and year must not be equal", eraField.equals(yearField));
        
        // Their types must be different
        assertNotEquals("Era and year types must be different", 
            DurationFieldType.era(), DurationFieldType.years());
        
        // Their names must be different
        assertNotEquals("Era and year names must be different", 
            DurationFieldType.era().getName(), DurationFieldType.years().getName());
        
        // Verify the cache returns consistent results
        UnsupportedDurationField eraField2 = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField yearField2 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        assertSame("Era cache should return same instance", eraField, eraField2);
        assertSame("Year cache should return same instance", yearField, yearField2);
        assertNotSame("Era and year should never be same instance", eraField2, yearField2);
        
        // This simulates the scenario in the defect: if a Partial has both era and year,
        // they must be recognized as distinct types. The bug would cause them to be
        // treated as duplicates incorrectly, or fail to detect actual duplicates.
        // By verifying distinctness, we ensure the duplicate detection can work.
        assertTrue("Era and year must be distinct for duplicate detection to work", 
            !eraField.equals(yearField) && !yearField.equals(eraField));
    }
    
    @Test(timeout = 4000)
    public void testDefect_duplicateTypeDetection() {
        // Simulate the scenario from TestPartial_Basics::testWith_baseAndArgHaveNoRange
        // where a Partial with era and year causes duplicate type error.
        // The UnsupportedDurationField must correctly identify era and year as distinct.
        
        UnsupportedDurationField era = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField year = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        // If the defect caused era and year to be conflated, this assertion would fail
        assertFalse("Era and year must not be equal - this is critical for duplicate detection", 
            era.equals(year));
        
        // Also verify that the same type is always equal to itself
        UnsupportedDurationField eraAgain = UnsupportedDurationField.getInstance(DurationFieldType.era());
        assertTrue("Same type must be equal to itself", era.equals(eraAgain));
        
        // And different types are never equal
        assertFalse("Different types must never be equal", era.equals(year));
    }
    
    // ------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ------------------------------------------------------------------------
    
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetValue_long_throws() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getValue(1000L);
    }
    
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetValueAsLong_throws() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getValueAsLong(1000L);
    }
    
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetValue_long_long_throws() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getValue(1000L, 2000L);
    }
    
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetValueAsLong_long_long_throws() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getValueAsLong(1000L, 2000L);
    }
    
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetMillis_int_throws() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getMillis(5);
    }
    
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetMillis_long_throws() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getMillis(5L);
    }
    
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetMillis_int_long_throws() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getMillis(5, 1000L);
    }
    
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetMillis_long_long_throws() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getMillis(5L, 1000L);
    }
    
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testAdd_long_int_throws() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.add(1000L, 5);
    }
    
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testAdd_long_long_throws() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.add(1000L, 5L);
    }
    
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetDifference_throws() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getDifference(1000L, 2000L);
    }
    
    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testGetDifferenceAsLong_throws() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        field.getDifferenceAsLong(1000L, 2000L);
    }
    
    @Test(timeout = 4000)
    public void testUnsupportedException_message() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        try {
            field.getValue(0L);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            String message = e.getMessage();
            assertNotNull("Exception message should not be null", message);
            assertTrue("Exception message should contain type name", message.contains("years"));
            assertTrue("Exception message should indicate unsupported", message.contains("unsupported"));
        }
    }
    
    @Test(timeout = 4000)
    public void testUnsupportedException_message_era() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.era());
        try {
            field.getMillis(1);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            String message = e.getMessage();
            assertNotNull("Exception message should not be null", message);
            assertTrue("Exception message should contain type name", message.contains("era"));
            assertTrue("Exception message should indicate unsupported", message.contains("unsupported"));
        }
    }
    
    // ------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // ------------------------------------------------------------------------
    
    @Test(timeout = 4000)
    public void testReadResolve_singleton() throws Exception {
        // Access readResolve via reflection to verify singleton behavior
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        java.lang.reflect.Method method = UnsupportedDurationField.class.getDeclaredMethod("readResolve");
        method.setAccessible(true);
        Object resolved = method.invoke(field);
        
        assertNotNull("readResolve should return non-null", resolved);
        assertTrue("readResolve should return UnsupportedDurationField", 
            resolved instanceof UnsupportedDurationField);
        assertSame("readResolve should return the singleton instance", field, resolved);
    }
    
    @Test(timeout = 4000)
    public void testSerialization_roundTrip() throws Exception {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        // Serialize
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(field);
        oos.close();
        
        // Deserialize
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();
        
        assertNotNull("Deserialized object should not be null", deserialized);
        assertTrue("Deserialized object should be UnsupportedDurationField", 
            deserialized instanceof UnsupportedDurationField);
        
        UnsupportedDurationField deserializedField = (UnsupportedDurationField) deserialized;
        
        // readResolve should ensure singleton
        assertSame("Deserialization should resolve to singleton", field, deserializedField);
        assertEquals("Type should be preserved", DurationFieldType.years(), deserializedField.getType());
        assertEquals("Name should be preserved", "years", deserializedField.getName());
    }
    
    @Test(timeout = 4000)
    public void testSerialization_era() throws Exception {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.era());
        
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(field);
        oos.close();
        
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();
        
        UnsupportedDurationField deserializedField = (UnsupportedDurationField) deserialized;
        assertSame("Era deserialization should resolve to singleton", field, deserializedField);
        assertEquals("Era type should be preserved", DurationFieldType.era(), deserializedField.getType());
    }
    
    @Test(timeout = 4000)
    public void testEquals_afterDeserialization() throws Exception {
        UnsupportedDurationField original = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        UnsupportedDurationField deserialized = (UnsupportedDurationField) ois.readObject();
        ois.close();
        
        assertTrue("Original and deserialized should be equal", original.equals(deserialized));
        assertEquals("HashCodes should match", original.hashCode(), deserialized.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testCompareTo_withNull() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        // compareTo should return 0 even with null (though not typical usage)
        assertEquals("compareTo(null) should return 0", 0, field.compareTo(null));
    }
    
    @Test(timeout = 4000)
    public void testGetInstance_afterCacheInitialized() {
        // First call initializes cache
        UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        // Subsequent calls should work correctly
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.months());
        assertNotNull("Field should not be null", field);
        assertEquals("Type should be months", DurationFieldType.months(), field.getType());
    }
    
    @Test(timeout = 4000)
    public void testMultipleTypes_noInterference() {
        // Get several fields and verify no cross-interference
        UnsupportedDurationField era = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField year = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField month = UnsupportedDurationField.getInstance(DurationFieldType.months());
        UnsupportedDurationField week = UnsupportedDurationField.getInstance(DurationFieldType.weeks());
        UnsupportedDurationField day = UnsupportedDurationField.getInstance(DurationFieldType.days());
        
        assertEquals("era", era.getName());
        assertEquals("years", year.getName());
        assertEquals("months", month.getName());
        assertEquals("weeks", week.getName());
        assertEquals("days", day.getName());
        
        assertFalse(era.equals(year));
        assertFalse(year.equals(month));
        assertFalse(month.equals(week));
        assertFalse(week.equals(day));
        assertFalse(era.equals(day));
    }
    
    @Test(timeout = 4000)
    public void testGetInstance_standardTypes() {
        // Test all standard DurationFieldType values
        DurationFieldType[] standardTypes = {
            DurationFieldType.eras(),
            DurationFieldType.centuries(),
            DurationFieldType.years(),
            DurationFieldType.months(),
            DurationFieldType.weeks(),
            DurationFieldType.days(),
            DurationFieldType.halfdays(),
            DurationFieldType.hours(),
            DurationFieldType.minutes(),
            DurationFieldType.seconds(),
            DurationFieldType.millis()
        };
        
        for (DurationFieldType type : standardTypes) {
            UnsupportedDurationField field = UnsupportedDurationField.getInstance(type);
            assertNotNull("Field should not be null for " + type.getName(), field);
            assertEquals("Type should match for " + type.getName(), type, field.getType());
            assertEquals("Name should match for " + type.getName(), type.getName(), field.getName());
            assertFalse("isSupported should be false for " + type.getName(), field.isSupported());
            assertTrue("isPrecise should be true for " + type.getName(), field.isPrecise());
            assertEquals("getUnitMillis should be 0 for " + type.getName(), 0L, field.getUnitMillis());
        }
    }
    
    @Test(timeout = 4000)
    public void testGetInstance_customType() {
        // Create a custom DurationFieldType
        DurationFieldType customType = new DurationFieldType("custom") {
            @Override
            public DurationField getField(org.joda.time.Chronology chronology) {
                return null;
            }
            
            @Override
            public DurationField getField(org.joda.time.chrono.BaseChronology chronology) {
                return null;
            }
        };
        
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(customType);
        assertNotNull("Field should not be null for custom type", field);
        assertEquals("Type should match", customType, field.getType());
        assertEquals("Name should match", "custom", field.getName());
    }
    
    @Test(timeout = 4000)
    public void testEquals_customType() {
        DurationFieldType customType1 = new DurationFieldType("custom1") {
            @Override
            public DurationField getField(org.joda.time.Chronology chronology) {
                return null;
            }
            
            @Override
            public DurationField getField(org.joda.time.chrono.BaseChronology chronology) {
                return null;
            }
        };
        
        DurationFieldType customType2 = new DurationFieldType("custom2") {
            @Override
            public DurationField getField(org.joda.time.Chronology chronology) {
                return null;
            }
            
            @Override
            public DurationField getField(org.joda.time.chrono.BaseChronology chronology) {
                return null;
            }
        };
        
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(customType1);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(customType2);
        
        assertFalse("Different custom types should not be equal", field1.equals(field2));
        assertTrue("Same custom type should be equal", field1.equals(UnsupportedDurationField.getInstance(customType1)));
    }
    
    @Test(timeout = 4000)
    public void testHashCode_customType() {
        DurationFieldType customType = new DurationFieldType("customHash") {
            @Override
            public DurationField getField(org.joda.time.Chronology chronology) {
                return null;
            }
            
            @Override
            public DurationField getField(org.joda.time.chrono.BaseChronology chronology) {
                return null;
            }
        };
        
        UnsupportedDurationField field1 = UnsupportedDurationField.getInstance(customType);
        UnsupportedDurationField field2 = UnsupportedDurationField.getInstance(customType);
        
        assertEquals("HashCodes should match for same type", field1.hashCode(), field2.hashCode());
        assertEquals("HashCode should match name hash", customType.getName().hashCode(), field1.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testToString_customType() {
        DurationFieldType customType = new DurationFieldType("customToString") {
            @Override
            public DurationField getField(org.joda.time.Chronology chronology) {
                return null;
            }
            
            @Override
            public DurationField getField(org.joda.time.chrono.BaseChronology chronology) {
                return null;
            }
        };
        
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(customType);
        assertEquals("toString should include custom name", 
            "UnsupportedDurationField[customToString]", field.toString());
    }
    
    @Test(timeout = 4000)
    public void testAllUnsupportedMethods_throw() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        // Verify all calculation methods throw UnsupportedOperationException
        try {
            field.getValue(0L);
            fail("getValue(long) should throw");
        } catch (UnsupportedOperationException e) { /* expected */ }
        
        try {
            field.getValueAsLong(0L);
            fail("getValueAsLong(long) should throw");
        } catch (UnsupportedOperationException e) { /* expected */ }
        
        try {
            field.getValue(0L, 0L);
            fail("getValue(long, long) should throw");
        } catch (UnsupportedOperationException e) { /* expected */ }
        
        try {
            field.getValueAsLong(0L, 0L);
            fail("getValueAsLong(long, long) should throw");
        } catch (UnsupportedOperationException e) { /* expected */ }
        
        try {
            field.getMillis(0);
            fail("getMillis(int) should throw");
        } catch (UnsupportedOperationException e) { /* expected */ }
        
        try {
            field.getMillis(0L);
            fail("getMillis(long) should throw");
        } catch (UnsupportedOperationException e) { /* expected */ }
        
        try {
            field.getMillis(0, 0L);
            fail("getMillis(int, long) should throw");
        } catch (UnsupportedOperationException e) { /* expected */ }
        
        try {
            field.getMillis(0L, 0L);
            fail("getMillis(long, long) should throw");
        } catch (UnsupportedOperationException e) { /* expected */ }
        
        try {
            field.add(0L, 0);
            fail("add(long, int) should throw");
        } catch (UnsupportedOperationException e) { /* expected */ }
        
        try {
            field.add(0L, 0L);
            fail("add(long, long) should throw");
        } catch (UnsupportedOperationException e) { /* expected */ }
        
        try {
            field.getDifference(0L, 0L);
            fail("getDifference(long, long) should throw");
        } catch (UnsupportedOperationException e) { /* expected */ }
        
        try {
            field.getDifferenceAsLong(0L, 0L);
            fail("getDifferenceAsLong(long, long) should throw");
        } catch (UnsupportedOperationException e) { /* expected */ }
    }
    
    @Test(timeout = 4000)
    public void testUnsupportedException_containsTypeName() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.months());
        try {
            field.getValue(0L);
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
            assertTrue("Message should contain 'months'", e.getMessage().contains("months"));
            assertTrue("Message should contain 'unsupported'", e.getMessage().contains("unsupported"));
        }
    }
    
    @Test(timeout = 4000)
    public void testUnsupportedException_containsTypeToString() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.days());
        try {
            field.getMillis(1);
            fail("Expected exception");
        } catch (UnsupportedOperationException e) {
            String typeString = DurationFieldType.days().toString();
            assertTrue("Message should contain type toString", e.getMessage().contains(typeString));
        }
    }
    
    @Test(timeout = 4000)
    public void testGetInstance_returnsSameForSameType() {
        DurationFieldType type = DurationFieldType.weeks();
        UnsupportedDurationField f1 = UnsupportedDurationField.getInstance(type);
        UnsupportedDurationField f2 = UnsupportedDurationField.getInstance(type);
        UnsupportedDurationField f3 = UnsupportedDurationField.getInstance(type);
        
        assertSame(f1, f2);
        assertSame(f2, f3);
        assertSame(f1, f3);
    }
    
    @Test(timeout = 4000)
    public void testGetInstance_differentTypesNotSame() {
        UnsupportedDurationField f1 = UnsupportedDurationField.getInstance(DurationFieldType.hours());
        UnsupportedDurationField f2 = UnsupportedDurationField.getInstance(DurationFieldType.minutes());
        UnsupportedDurationField f3 = UnsupportedDurationField.getInstance(DurationFieldType.seconds());
        
        assertNotSame(f1, f2);
        assertNotSame(f2, f3);
        assertNotSame(f1, f3);
    }
    
    @Test(timeout = 4000)
    public void testEquals_symmetric() {
        UnsupportedDurationField era1 = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField era2 = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField year = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        assertTrue(era1.equals(era2));
        assertTrue(era2.equals(era1));
        assertFalse(era1.equals(year));
        assertFalse(year.equals(era1));
    }
    
    @Test(timeout = 4000)
    public void testEquals_transitive() {
        UnsupportedDurationField f1 = UnsupportedDurationField.getInstance(DurationFieldType.months());
        UnsupportedDurationField f2 = UnsupportedDurationField.getInstance(DurationFieldType.months());
        UnsupportedDurationField f3 = UnsupportedDurationField.getInstance(DurationFieldType.months());
        
        assertTrue(f1.equals(f2));
        assertTrue(f2.equals(f3));
        assertTrue(f1.equals(f3));
    }
    
    @Test(timeout = 4000)
    public void testHashCode_equalsContract() {
        UnsupportedDurationField f1 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        UnsupportedDurationField f2 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testToString_containsName() {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.centuries());
        String str = field.toString();
        assertTrue("toString should contain name", str.contains("centuries"));
        assertTrue("toString should start with UnsupportedDurationField[", 
            str.startsWith("UnsupportedDurationField["));
        assertTrue("toString should end with ]", str.endsWith("]"));
    }
    
    @Test(timeout = 4000)
    public void testGetInstance_afterReadResolve() throws Exception {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        java.lang.reflect.Method method = UnsupportedDurationField.class.getDeclaredMethod("readResolve");
        method.setAccessible(true);
        Object resolved = method.invoke(field);
        
        UnsupportedDurationField resolvedField = (UnsupportedDurationField) resolved;
        UnsupportedDurationField again = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        assertSame("readResolve should return same as getInstance", resolvedField, again);
    }
    
    @Test(timeout = 4000)
    public void testCompareTo_alwaysZero() {
        UnsupportedDurationField f1 = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField f2 = UnsupportedDurationField.getInstance(DurationFieldType.millis());
        UnsupportedDurationField f3 = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        assertEquals(0, f1.compareTo(f2));
        assertEquals(0, f2.compareTo(f3));
        assertEquals(0, f3.compareTo(f1));
        assertEquals(0, f1.compareTo(f1));
    }
    
    @Test(timeout = 4000)
    public void testGetUnitMillis_alwaysZero() {
        UnsupportedDurationField era = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField millis = UnsupportedDurationField.getInstance(DurationFieldType.millis());
        UnsupportedDurationField years = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        assertEquals(0L, era.getUnitMillis());
        assertEquals(0L, millis.getUnitMillis());
        assertEquals(0L, years.getUnitMillis());
    }
    
    @Test(timeout = 4000)
    public void testIsPrecise_alwaysTrue() {
        UnsupportedDurationField era = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField millis = UnsupportedDurationField.getInstance(DurationFieldType.millis());
        
        assertTrue(era.isPrecise());
        assertTrue(millis.isPrecise());
    }
    
    @Test(timeout = 4000)
    public void testIsSupported_alwaysFalse() {
        UnsupportedDurationField era = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField millis = UnsupportedDurationField.getInstance(DurationFieldType.millis());
        
        assertFalse(era.isSupported());
        assertFalse(millis.isSupported());
    }
    
    @Test(timeout = 4000)
    public void testGetName_neverNull() {
        DurationFieldType[] types = {
            DurationFieldType.eras(), DurationFieldType.centuries(), DurationFieldType.years(),
            DurationFieldType.months(), DurationFieldType.weeks(), DurationFieldType.days(),
            DurationFieldType.halfdays(), DurationFieldType.hours(), DurationFieldType.minutes(),
            DurationFieldType.seconds(), DurationFieldType.millis()
        };
        
        for (DurationFieldType type : types) {
            UnsupportedDurationField field = UnsupportedDurationField.getInstance(type);
            assertNotNull("Name should not be null for " + type, field.getName());
        }
    }
    
    @Test(timeout = 4000)
    public void testGetType_neverNull() {
        DurationFieldType[] types = {
            DurationFieldType.eras(), DurationFieldType.centuries(), DurationFieldType.years(),
            DurationFieldType.months(), DurationFieldType.weeks(), DurationFieldType.days(),
            DurationFieldType.halfdays(), DurationFieldType.hours(), DurationFieldType.minutes(),
            DurationFieldType.seconds(), DurationFieldType.millis()
        };
        
        for (DurationFieldType type : types) {
            UnsupportedDurationField field = UnsupportedDurationField.getInstance(type);
            assertNotNull("Type should not be null for " + type, field.getType());
            assertEquals("Type should match", type, field.getType());
        }
    }
    
    @Test(timeout = 4000)
    public void testSerialization_roundTripMultipleTypes() throws Exception {
        DurationFieldType[] types = {
            DurationFieldType.era(), DurationFieldType.years(), DurationFieldType.months()
        };
        
        for (DurationFieldType type : types) {
            UnsupportedDurationField original = UnsupportedDurationField.getInstance(type);
            
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();
            
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
            UnsupportedDurationField deserialized = (UnsupportedDurationField) ois.readObject();
            ois.close();
            
            assertSame("Should resolve to singleton for " + type, original, deserialized);
            assertEquals("Type should match for " + type, type, deserialized.getType());
        }
    }
    
    @Test(timeout = 4000)
    public void testReadResolve_returnsSingleton() throws Exception {
        UnsupportedDurationField field = UnsupportedDurationField.getInstance(DurationFieldType.days());
        
        java.lang.reflect.Method method = UnsupportedDurationField.class.getDeclaredMethod("readResolve");
        method.setAccessible(true);
        Object resolved = method.invoke(field);
        
        assertSame("readResolve should return the singleton", field, resolved);
    }
    
    @Test(timeout = 4000)
    public void testDefect_eraAndYearNotEqualInCache() {
        // This test directly targets the defect scenario
        // The bug causes "Types array must not contain duplicate: era and year"
        // This happens when Partial.with() is called with a field that already exists.
        // UnsupportedDurationField must correctly distinguish era from year.
        
        UnsupportedDurationField era = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField year = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        // Critical: era and year must be different instances
        assertNotSame("Era and year must be different instances", era, year);
        
        // Critical: era and year must not be equal
        assertFalse("Era and year must not be equal", era.equals(year));
        
        // Critical: their names must be different
        assertNotEquals("Era and year names must differ", era.getName(), year.getName());
        
        // Verify the cache maintains distinct entries
        UnsupportedDurationField eraAgain = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField yearAgain = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        assertSame("Era cache must return same instance", era, eraAgain);
        assertSame("Year cache must return same instance", year, yearAgain);
        assertNotSame("Era and year must remain distinct", eraAgain, yearAgain);
        
        // This ensures that when Partial checks for duplicate types,
        // era and year are correctly identified as different.
        // If they were incorrectly equal, the duplicate detection would fail
        // and the IllegalArgumentException would not be thrown when expected.
    }
    
    @Test(timeout = 4000)
    public void testDefect_duplicateDetectionWithEraAndYear() {
        // Simulate the scenario from the defect report
        // TestPartial_Basics::testWith_baseAndArgHaveNoRange
        // The partial has both era and year fields, and when with() is called,
        // it should throw IllegalArgumentException about duplicate types.
        // UnsupportedDurationField must correctly identify era and year as distinct.
        
        UnsupportedDurationField eraField = UnsupportedDurationField.getInstance(DurationFieldType.era());
        UnsupportedDurationField yearField = UnsupportedDurationField.getInstance(DurationFieldType.years());
        
        // If the defect caused era and year to be conflated, this would fail
        assertFalse("Era and year must be distinct for duplicate detection", 
            eraField.equals(yearField));
        
        // Verify that the types themselves are distinct
        assertNotEquals("DurationFieldType.era() and DurationFieldType.years() must differ",
            DurationFieldType.era(), DurationFieldType.years());
        
        // Verify that the names are distinct
        assertNotEquals("Era and year names must differ",
            DurationFieldType.era().getName(), DurationFieldType.years().getName());
        
        // The duplicate detection in Partial relies on these being distinct.
        // If they were equal, the detection would incorrectly flag duplicates
        // or fail to detect them, causing the IllegalArgumentException.
    }
}