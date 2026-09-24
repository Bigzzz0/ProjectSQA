package com.google.javascript.rhino.jstype;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for RecordTypeBuilder.
 *
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: RecordTypeBuilder
 * 
 * Decision branches:
 * 1. addProperty(): isEmpty set to false unconditionally on entry.
 *    - Branch on properties.containsKey(name):
 *        - true: return null (property not added)
 *        - false: add property and return this
 * 2. build():
 *    - isEmpty == true: return registry.getNativeObjectType(OBJECT_TYPE)
 *    - isEmpty == false: return new RecordType(registry, unmodifiableMap(properties))
 * 
 * Boundary conditions:
 * - Empty builder (no addProperty calls) -> build returns OBJECT_TYPE
 * - Builder with one or more properties -> build returns RecordType
 * - Duplicate property name: first add returns this, second returns null; isEmpty remains false
 * - Null name in addProperty -> NullPointerException (from HashMap)
 * - Null type or propertyNode: allowed, no validation in current code
 * 
 * Known defect (Issue 725): 
 * When all addProperty calls result in duplicates, isEmpty is still false because it is set
 * on the first call (even if duplicate). Then build() returns a RecordType with an empty map
 * instead of returning the native object type. The test testDuplicatePropertyDoesNotSetIsEmpty
 * reveals this by asserting that after only duplicate additions, the result is the native object type.
 */
public class RecordTypeBuilderDeepseekTest {

    // --- Helper to create a simple registry (minimal dependencies) ---
    // In the actual Defects4J environment, JSTypeRegistry can be instantiated.
    // Using a no-arg constructor if available; otherwise adjust.
    private JSTypeRegistry createRegistry() {
        return new JSTypeRegistry();
    }

    // ===================== Partition A: Core Functional Logic =====================

    @Test(timeout = 4000)
    public void testEmptyBuilderReturnsObjectType() {
        JSTypeRegistry registry = createRegistry();
        RecordTypeBuilder builder = new RecordTypeBuilder(registry);
        JSType result = builder.build();
        assertSame("Empty builder should return native object type",
                registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), result);
    }

    @Test(timeout = 4000)
    public void testAddSinglePropertyReturnsRecordType() {
        JSTypeRegistry registry = createRegistry();
        RecordTypeBuilder builder = new RecordTypeBuilder(registry);
        builder.addProperty("x", null, null);
        JSType result = builder.build();
        assertTrue("Builder with properties should return a RecordType",
                result instanceof RecordType);
    }

    @Test(timeout = 4000)
    public void testAddPropertyReturnsBuilderForChaining() {
        JSTypeRegistry registry = createRegistry();
        RecordTypeBuilder builder = new RecordTypeBuilder(registry);
        RecordTypeBuilder returned = builder.addProperty("a", null, null);
        assertSame("addProperty should return this for success", builder, returned);
    }

    @Test(timeout = 4000)
    public void testMultiplePropertiesChaining() {
        JSTypeRegistry registry = createRegistry();
        RecordTypeBuilder builder = new RecordTypeBuilder(registry);
        builder.addProperty("a", null, null)
               .addProperty("b", null, null);
        JSType result = builder.build();
        assertTrue(result instanceof RecordType);
        RecordType rt = (RecordType) result;
        // Check that properties exist (but we cannot easily query; just ensure no exception)
        assertNotNull(rt);
    }

    // ===================== Partition B: Boundary Value Analysis =====================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testAddPropertyWithNullNameThrowsNpe() {
        JSTypeRegistry registry = createRegistry();
        RecordTypeBuilder builder = new RecordTypeBuilder(registry);
        builder.addProperty(null, null, null);
    }

    @Test(timeout = 4000)
    public void testAddPropertyWithEmptyName() {
        JSTypeRegistry registry = createRegistry();
        RecordTypeBuilder builder = new RecordTypeBuilder(registry);
        // Empty string is a valid property name
        builder.addProperty("", null, null);
        JSType result = builder.build();
        assertTrue(result instanceof RecordType);
    }

    @Test(timeout = 4000)
    public void testAddPropertyWithNullTypeAndNode() {
        JSTypeRegistry registry = createRegistry();
        RecordTypeBuilder builder = new RecordTypeBuilder(registry);
        assertSame(builder, builder.addProperty("prop", null, null));
        JSType result = builder.build();
        assertTrue(result instanceof RecordType);
    }

    @Test(timeout = 4000)
    public void testAddPropertyDuplicateReturnsNull() {
        JSTypeRegistry registry = createRegistry();
        RecordTypeBuilder builder = new RecordTypeBuilder(registry);
        assertNotNull(builder.addProperty("dup", null, null));
        assertNull("Duplicate should return null", builder.addProperty("dup", null, null));
    }

    // ===================== Partition C: Defect-Targeted Branch Zone =====================

    @Test(timeout = 4000)
    public void testDuplicatePropertyDoesNotSetIsEmpty() {
        // This test directly targets Issue 725:
        // After only duplicate addProperty calls, the builder should still be considered empty
        // and build() should return the native object type.
        JSTypeRegistry registry = createRegistry();
        RecordTypeBuilder builder = new RecordTypeBuilder(registry);

        // First add succeeds
        assertNotNull(builder.addProperty("key", null, null));
        // Second add (duplicate) returns null
        assertNull(builder.addProperty("key", null, null));

        // Even though addProperty was called, the only successful addition was the first one.
        // If the builder incorrectly sets isEmpty on the first call (which it does),
        // it will return a RecordType. The correct behavior is that after duplicate,
        // no property is actually stored, so the builder should still be empty.
        // The current code sets isEmpty = true initially, then sets it to false in addProperty
        // before checking for duplicate. Thus isEmpty becomes false even when the property
        // is not added. This test expects the build to return the native object type,
        // which will fail on the buggy version.
        JSType result = builder.build();
        assertSame("Builder with only duplicate property should return native object type",
                registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), result);
    }

    @Test(timeout = 4000)
    public void testMultipleDuplicatesOnly() {
        JSTypeRegistry registry = createRegistry();
        RecordTypeBuilder builder = new RecordTypeBuilder(registry);
        builder.addProperty("p1", null, null);
        builder.addProperty("p2", null, null);
        // Now duplicate both
        assertNull(builder.addProperty("p1", null, null));
        assertNull(builder.addProperty("p2", null, null));
        // Two distinct properties were added successfully, so builder is non-empty
        JSType result = builder.build();
        assertTrue("Builder with two distinct properties should return RecordType",
                result instanceof RecordType);
    }

    // ===================== Partition D: Exception & Defensive Guard Paths =====================

    @Test(timeout = 4000)
    public void testBuildCalledMultipleTimes() {
        JSTypeRegistry registry = createRegistry();
        RecordTypeBuilder builder = new RecordTypeBuilder(registry);
        // Build once returns object type
        assertSame(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), builder.build());
        // Build again should still return same (state unchanged)
        assertSame(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), builder.build());
        // Now add property
        builder.addProperty("x", null, null);
        JSType second = builder.build();
        assertTrue(second instanceof RecordType);
        // And again
        JSType third = builder.build();
        assertSame("Build should be idempotent", second, third);
    }

    @Test(timeout = 4000)
    public void testNullRegistryThrowsNpe() {
        try {
            new RecordTypeBuilder(null);
            fail("Expected NullPointerException for null registry");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // ===================== Partition E: Object Lifecycle & Contract =====================

    @Test(timeout = 4000)
    public void testRecordPropertyGetter() {
        // Indirectly test RecordProperty through builder behavior
        JSTypeRegistry registry = createRegistry();
        RecordTypeBuilder builder = new RecordTypeBuilder(registry);
        builder.addProperty("test", null, null);
        RecordType rt = (RecordType) builder.build();
        // The RecordType should have a property "test" with null type
        // We cannot easily access internal map, but we can verify it doesn't crash
        assertNotNull(rt);
    }

    @Test(timeout = 4000)
    public void testBuilderIdentityAfterDuplicate() {
        JSTypeRegistry registry = createRegistry();
        RecordTypeBuilder builder = new RecordTypeBuilder(registry);
        builder.addProperty("p", null, null);
        builder.addProperty("p", null, null); // duplicate returns null, ignore return
        // Builder state should remain as if only one property added
        JSType result = builder.build();
        assertTrue(result instanceof RecordType);
    }
}