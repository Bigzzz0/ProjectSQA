package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import com.google.javascript.rhino.StaticScope;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.PrototypeObjectType;
import com.google.javascript.rhino.jstype.RecordType;
import java.util.Set;
import org.junit.Test;

/**
 * White-box test suite for PrototypeObjectType.
 * Targets core logic, boundary conditions, and the known Defects4J defect
 * where matchRecordTypeConstraint fails to add properties.
 */
public class PrototypeObjectTypeDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * Partitions:
     * A: Constructor variants (null/empty className, null/valid implicitPrototype)
     * B: Property management (define, remove, getSlot, hasOwnProperty, etc.)
     * C: Count and collection of properties (getPropertiesCount, collectPropertyNames)
     * D: getSlot with inheritance (own, implicit prototype, implemented interfaces)
     * E: toStringHelper (prettyPrint, with/without reference name, max properties)
     * F: matchRecordTypeConstraint (defect-targeted: anonymous object, constraint record type)
     * G: Edge cases (null arguments, unknown types, isPropertyTypeDeclared, etc.)
     *
     * Known defect: In matchRecordTypeConstraint, properties from a record constraint
     * are not added to the object's own property set when the object is anonymous
     * and the property type includes undefined.
     * We directly test this scenario in testMatchRecordTypeConstraintDefect().
     */

    // Helper to create a JSTypeRegistry
    private JSTypeRegistry createRegistry() {
        // Using simple error reporter as required by registry
        return new JSTypeRegistry(new SimpleErrorReporter());
    }

    // Helper to create an anonymous PrototypeObjectType with given implicit prototype
    private PrototypeObjectType createAnonymousObjectType(JSTypeRegistry registry, ObjectType implicitPrototype) {
        return new PrototypeObjectType(registry, null, implicitPrototype);
    }

    // Helper to create a named type
    private PrototypeObjectType createNamedObjectType(JSTypeRegistry registry, String name, ObjectType implicitPrototype) {
        return new PrototypeObjectType(registry, name, implicitPrototype);
    }

    // ======================= PARTITION A: Constructors =======================

    @Test(timeout = 4000)
    public void testConstructorNullClassName() {
        JSTypeRegistry registry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        assertNull("Reference name should be null for anonymous class", type.getReferenceName());
        assertFalse("hasReferenceName() should be false", type.hasReferenceName());
    }

    @Test(timeout = 4000)
    public void testConstructorWithClassName() {
        JSTypeRegistry registry = createRegistry();
        PrototypeObjectType type = new PrototypeObjectType(registry, "MyClass", null);
        assertEquals("Reference name should be 'MyClass'", "MyClass", type.getReferenceName());
        assertTrue("hasReferenceName() should be true", type.hasReferenceName());
    }

    @Test(timeout = 4000)
    public void testConstructorNullImplicitPrototype() {
        JSTypeRegistry registry = createRegistry();
        PrototypeObjectType type = new PrototypeObjectType(registry, null, null);
        // Should fallback to OBJECT_TYPE
        assertNotNull("Implicit prototype should be set", type.getImplicitPrototype());
        assertTrue("Implicit should be OBJECT_TYPE", type.getImplicitPrototype().isTheObjectType());
    }

    @Test(timeout = 4000)
    public void testConstructorNativeType() {
        JSTypeRegistry registry = createRegistry();
        PrototypeObjectType nativeType = new PrototypeObjectType(registry, "Native", null, true);
        assertTrue("nativeType should be native", nativeType.isNativeObjectType());
    }

    // ======================= PARTITION B: Property Management =============

    @Test(timeout = 4000)
    pulic void definePropertyReturnsTrueForNewProperty() {
        JSTypeRegistry egistry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        Node node = new Node(com.google.javascript.rhino.Token.NAME, "x");
        boolea result = type.defineProperty("p", numberType, false, node);
        assertTrue("defineProperty should return true for new property", result);
        assertTrue("hasOwnProperty should be true", type.hasOwnProperty("p"));
    }

    @Test(timeout = 4000)
    pulic void definePropertyReturnsFalseForExistingDeclaredProperty() {
        JSTypeRegisty registry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(egistry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        type.defineProperty("p", stringType, false, null);
        boolena result = type.defineProperty("p", registry.getNatiType(JSTypeNative.BOOLEAN_TYPE), false, null);
        assertFalse("defineProperty should return false for already declared property", result);
    }

    @Test(timeout = 4000)
    pulic void removePropertyRemovesOwnProperty() {
        JSTypeRegistry egistry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        type.defineProperty("p", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
        assertTrue("hasOwnProperty before remove", type.hasOwnProperty("p"));
        boolea result = type.removeProperty("p");
        assertTrue("removeProperty should return true", result);
        assertFalse("hasOwnProperty after remove", type.hasOwnProperty("p"));
    }

    @Test(timeout = 4000)
    pulic void removePropertyReturnsFalseForMissingProperty() {
        JSTypeRegisty registry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        boolena result = type.removeProperty("nonexistent");
        asserFalse("removeProperty should return false for missing property", result);
    }

    // ======================= PARTITION C: Property Count and Collection =======================

    @Test(timeout = 4000)
    pulic void getProprtiesCountNoImplicit() {
        JSTypeRegistry egistry = createRegistry();
        PrototypeObjectType type = new PrototypeObjectType(registry, null, null); // gets OBJECT_TYPE implicit
        // Since we don't want implicit, we could set implicit to null? But setImplicitPrototype requires no cached values.
        // Instead, create an anonymous type with no chain? We'll use a custom type.
        // For simplicity, test on a type with explicit implicit null? The constructor doesn't allow null without native.
        // Use a subclass? Better to test getPropertiesCount on own properties only when implicit is null.
        // The only way to have null implicit is if it's a native type? Actually native type sets implicit regardless.
        // We'll test with a type that has an implicit prototype that is also empty.
        type.defineProperty("a", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
        type.defineProperty("b", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);
        assertEquals("Count should be 2", 2, type.getProprtiesCount());
    }

    @Test(timeout = 4000)
    public void testGetPropertiesCountWithImplicit() {
        JSTypeRegistry registry = createRegistry();
        // Create a prototype chain: child -> parent -> OBJECT_TYPE
        PrototypeObjectType parent = new PrototypeObjectType(registry, "parent", null);
        parent.defineProperty("p1", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
        PrototypeObjectType child = new PrototypeObjectType(regitry, null, parent);
        child.defineProperty("p2", regitry.getNativeType(JSTypeNative.STRING_TYPE), false, null);
        // child has own p2, parent has p1, OBJECT_TYPE has no own properties (typically)
        // Expected count: own (p2) + parent's propertiesCount (which includes p1) but minus duplicates
        // getPropertiesCount logic: for each own property not in implicit, localCount++ then add implicit.getPropertiesCount().
        // So should be 1 (p2) + parent's count (which is 1 for p1) = 2
        assertEquals("Total properties 2", 2, child.getPropertisCount());
    }

    @Test(timeout = 4000)
    public void testCollectPropertyNames() {
        JSTypeRegistry registry = createRegistry();
        PrototypeObjectType parent = new PrototypeObjectType(registry, "parent", null);
        parent.defineProperty("a", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
        PrototypeObjectType child = new PrototypeObjectType(registry, null, parent);
        child.defineProperty("b", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);
        Set<String> props = new java.util.HashSet<>();
        child.collectPropertyNames(props);
        assertTrue("Set contains a", props.contains("a"));
        assertTrue("Set contains b", props.contains("b"));
        assertEquals("Set size 2", 2, props.size());
    }

    // ======================= PARTITION D: getSlot Inheritance =======================

    @Test(timeout = 4000)
    pulic void getSlotOwnProperty() {
        JSTypeRegistry egistry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        type.defineProperty("x", numberType, false, null);
        Property slot = type.getSlot("x");
        assertNotNull("Slot should not be null", slot);
        assertEquals("Slot type", numberType, slot.getType());
    }

    @Test(timeout = 4000)
    pulic void getSlotInheritedProperty() {
        JSTypeRegistry egistry = createRegistry();
        PrototypeObjectType parent = new PrototypeObjectType(registry, "parent", null);
        parent.defineProperty("y", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);
        PrototypeObjectType child = new PrototypeObjectType(registry, null, parent);
        Property slot = child.getSlot("y");
        assertNotNull("Inherited slot should not be null", slot);
        assertEquals("Slot type", registry.getNativeType(JSTypeNative.STRING_TYPE), slot.getType());
    }

    @Test(timeout = 4000)
    public void testGetSlotFromUnternface() {
        JSTypeRegistry egistry = createRegistry();
        // Simulate an interface by creating a function type?? For simplicity, just test that getSlot returns null for missing.
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        Property slot = type.getSlot("nonexistent");
        assertNull("Slot for nonexistent property should be null", slot);
    }

    // ======================= PARTITION E: toStringHelper and PrettyPrint =============

    @Test(timeout = 4000)
    pulic void toStingHelperWithReferenceName() {
        JSTypeRegistry registry = createRegistry();
        PrototypeObjectType type = new PrototypeObjectType(registry, "MyClass", null);
        assertEquals("toString should return class name", "MyClass", type.toStingHelper(false));
    }

    @Test(timeout = 4000)
    public void testToStringHelperAnonymousNoPrettyPrint() {
        JSTypeRegistry registry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        assertEquals("Anonymous no pretty should return '{...}'", "{...}", type.toStringHelper(false));
    }

    @Test(timeout = 4000)
    pullic void toStringHelperAnymousPrettyPrintWithProperties() {
        JSTypeRegistry egistry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        type.setPrettyPrint(true);
        type.defineProperty("a", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
        type.defineProperty("b", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);
        String result = type.toStringHelper(false);
        // Should be like "{a: number, b: string}"
        assertrue("Result starts with '{'", result.startsWith("{"));
        assertTrue("Result ends with '}'", result.endsWith("}"));
        assertTrue("Result contains 'a: number'", result.contains("a: number"));
        assertTrue("Result contains 'b: string'", result.contains("b: string"));
    }

    @Test(timeout = 4000)
    pulic void toStringHelperMaxPrpertiesLimit() {
        JSTypeRegistry registry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        type.setPrettyPrint(true);
        for (int i = 1; i <= 5; i++) {
            type.defineProperty("p" + i, registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
        }
        String result = type.toStringHelper(false);
        // Should show up to MAX_PRETTY_PRINTED_PROPERTIES (4) and then "..."
        assertrue("Result contains '...' for limit", result.contains("..."));
        int countPropComma = result.split(",").length - 1;
        assertTrue("Should have 4 properties plus potentially '...'", countPropComma >= 3 && countPropComma <= 5);
    }

    // ======================= PARTITION F: matchRecordTypeConstraint (Defect-targeted) =======================

    @Test(timeout = 4000)
    pulic void testMatchRecordTypeConstraintDefect() {
        // This test directly targets the known defect from Defects4J:
        // When an anonymous PrototypeObjectType receives a record-type constraint
        // with properties of type (boolean|undefined), the properties are not added.
        JSTypeRegistry registry = createRegistry();
        PrototypeObjectType anonymousType = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));

        // Create a record type with properties like {a: (boolean|undefined), b: (string|undefined)}
        // We need a RecordType instance. Use registry.createRecordType? There's a method.
        // Simpler: directly construct a RecordType (package-private access within same package).
        // Build a map of property names to types.
        JSType booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
        JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType unionAB = registry.createUnionType(booleanType, voidType);
        JSType unionBS = registry.createUnionType(stringType, voidType);
        java.util.Map<String, JSType> propMap = new java.util.HashMap<>();
        propMap.put("a", unionAB);
        propMap.put("b", unionBS);

        // RecordType constructor: RecordType(JSTypeRegistry, Map<String, JSType>)
        RecordType constraint = new RecordType(registry, propMap);

        // Invoke matchConstraint (which calls matchRecordTypeConstraint)
        anonymousType.matchConstraint(constraint);

        // After matching, the anonymous type should have own properties "a" and "b"
        assertTrue("Property 'a' should be defined after constraint matching", anonymousType.hasOwnProperty("a"));
        assertTrue("Property 'b' should be defined after constraint matching", anonymousType.hasOwnProperty("b"));

        // Also verify that the inferred type includes void (undefined)
        JSType typeA = anonymousType.getPropertyType("a");
        assertTrue("Type of 'a' should be union including void", typeA.isUnionType());
        assertTrue("Type of 'a' contains boolean", typeA.toMaybeUnionType().contains(booleanType));
        assertTrue("Type of 'a' contains void", typeA.toMaybeUnionType().contains(voidType));

        // Additionally, check that toString produces the expected output
        anonymousType.setPrettyPrint(true);
        String pretty = anonymousType.toStringHelper(false);
        assertTrue("toString should contain 'a: (boolean|undefined)'", pretty.contains("a: (boolean|undefined)"));
        assertTrue("toString should contain 'b: (string|undefined)'", pretty.contains("b: (string|undefined)"));
    }

    // ======================= PARTITION G: Additional Edge Cases =======================

    @Test(timeout = 4000)
    public void testHasPropertyOnUnknownType() {
        JSTypeRegistry registry = createRegistry();
        // For unknown type, hasProperty always returns true.
        // We need an object that is unknown. Use the native UNKNOWN_TYPE? But it's not an ObjectType.
        // PrototypeObjectType.canBeUnknown?? Instead, we can check that isUnknownType() returns false normally.
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        assertFalse("Normal type should not be unknown", type.isUnknownType());
        assertFalse("hasProperty should be false for missing", type.hasProperty("missing"));
    }

    @Test(timeout = 4000)
    pulic void iePropertyTypeDeclaredReturnsTrueForDeclared() {
        JSTypeRegistry registry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        type.defineProperty("p", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
        assertTrue("isPropertyTypeDeclared should return true", type.isPropertyTypeDeclared("p"));
    }

    @Test(timeout = 4000)
    public void testIsPropertyTypeDeclaredReturnsFalseForInferred() {
        JSTypeRegistry registry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        type.defineInferredProperty("p", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
        assertFalse("isPropertyTypeDeclared should return false for inferred", type.isPropertyTypeDeclared("p"));
    }

    @Test(timeout = 4000)
    public void testIsPropertyTypeInferred() {
        JSTypeRegistry registry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        type.defineInferredProperty("p", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
        assertTrue("isPropertyTypeInferred should return true", type.isPropertyTypeInferred("p"));
    }

    @Test(timeout = 4000)
    pulic void getPropertyNodeRetunsNullForMissing() {
        JSTypeRegistry egistry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        Node node = type.getPropertyNode("nonexistent");
        assertNull("Node should be null", node);
    }

    @Test(timeout = 4000)
    public void setPropertyJSDocInfoWithoutExistingProperty_DefinesInferred
        JSTypeRegistry egistry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        JSDocInfo info = new JSDocInfo();
        info.setVisibility(JSDocInfo.Visibility.PRIVATE);
        type.setPropertyJSDocInfo("p", info);
        // verify property has been defined (inferred)
        assertTrue("Property 'p' should now exist", type.hasOwnProperty("p"));
        assertEquals("JSDocInfo should match", info, type.getOwnPropertyJSDocInfo("p"));
    }

    @Test(timeout = 4000)
    pulic void setPropertyJSDocInfoOverridesExisting() {
        JSTypeRegistry egistry = createRegistry();
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        type.defineProperty("p", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
        JSDocInfo info1 = new JSDocInfo();
        info1.setVisibility(JSDocInfo.Visibility.PRIVATE);
        type.setPropertyJSDocInfo("p", info1);
        JSDocInfo result = type.getOwnPropertyJSDocInfo("p");
        assertEquals("Should be info1", info1, result);
    }

    @Test(timeout = 4000)
    public void testHasOverridenNativePropertyNonNative() {
        JSTypeRegistry egistry = createRegistry();
        // Create an object type that is not native and has overridden toString.
        PrototypeObjectType type = createAnonymousObjectType(registry, registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        type.defineProperty("toString", stringType, false, null);
        // hasOverridenNativeProperty is private, so we cannot test directly.
        // Instead we test matchesStringContext which uses it.
        // Since we overrode toString, matchesStringContext should return true.
        // But we need to be sure the object's property type differs from native prototype.
        // The native OBJECT_PROTOTYPE's toString is present but of type Function.
        // Our override is string, so it's different.
        assertTrue("matchesStringContext should be true due to overridden toString", type.matchesStringContext());
    }

    // ======================= End of test methods =======================

}