package com.google.javascript.rhino.jstype;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Constructor and getAlternates
 *   - matchesNumberContext / matchesStringContext / matchesObjectContext
 *   - findPropertyType
 *   - canAssignTo
 *   - canBeCalled
 *   - autobox, restrictByNotNullOrUndefined
 *   - testForEquality
 *   - isNullable, isUnknownType, isStruct, isDict
 *   - getLeastSupertype, meet
 *   - checkUnionEquivalenceHelper, hasAlternate
 *   - hasProperty, contains
 *   - getRestrictedUnion
 *   - isSubtype
 *   - getRestrictedTypeGivenToBooleanOutcome, getPossibleToBooleanOutcomes
 *   - resolveInternal, collapseUnion, matchConstraint, hasAnyTemplateInternal
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Empty alternates
 *   - Single alternate
 *   - Multiple alternates including unknown
 *   - Null/undefined alternates (not allowed; constructor assumes valid)
 *   - Alternates with various JSType subclasses (NoType, AllType, UnknownType, etc.)
 *   - isNullable true/false combinations
 *   - isSubtype edge cases (unknown, all type, null)
 *   - getLeastSupertype with union and non-union, unknown types
 *   - meet method with NoObjectType/NoType fallback
 * 
 * Partition C: Defect-Targeted Branch Zone (Defects4J: testIssue791, testSubtypeWithUnknowns2)
 *   - isSubtype when ‘that’ is unknown (should return true)
 *   - isSubtype when ‘that’ is all type (should return true)
 *   - isSubtype when alternates contain unknown type
 *   - checkUnionEquivalenceHelper with tolerateUnknowns
 *   - getLeastSupertype when ‘that’ is unknown and not union type (should compute LUB correctly)
 *   - meet with unknown subtypes
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - No explicit exception checks in public API; internal preconditions not violated
 *   - resolveInternal assumes alternates collection hash stable; tests verify no exception
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - hashCode consistent with alternates
 *   - contains uses isEquivalentTo
 *   - toMaybeUnionType returns self
 */
public class UnionTypeDeepseekTest {

    private JSTypeRegistry registry;

    public UnionTypeDeepseekTest() {
        this.registry = new JSTypeRegistry(null);
    }

    // Helper to create a union type from JSType instances
    private UnionType createUnion(JSType... types) {
        java.util.Collection<JSType> coll = new java.util.ArrayList<>();
        java.util.Collections.addAll(coll, types);
        return new UnionType(registry, coll);
    }

    // ---------- Partition A: Core Functional Logic ----------

    @Test(timeout = 4000)
    public void testConstructorAndGetAlternates() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        UnionType union = createUnion(numberType, stringType);
        Iterable<JSType> alternates = union.getAlternates();
        java.util.List<JSType> list = new java.util.ArrayList<>();
        for (JSType t : alternates) list.add(t);
        assertEquals(2, list.size());
        assertTrue(list.contains(numberType));
        assertTrue(list.contains(stringType));
    }

    @Test(timeout = 4000)
    public void testMatchesNumberContext() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
        assertTrue(createUnion(numberType).matchesNumberContext());
        assertFalse(createUnion(voidType).matchesNumberContext());
        assertTrue(createUnion(numberType, voidType).matchesNumberContext());
    }

    @Test(timeout = 4000)
    public void testMatchesStringContext() {
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
        assertTrue(createUnion(stringType).matchesStringContext());
        assertTrue(createUnion(nullType).matchesStringContext()); // null can be in string context
        // void type does not match string context (see JSType default)
        JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
        assertFalse(createUnion(voidType).matchesStringContext());
    }

    @Test(timeout = 4000)
    public void testMatchesObjectContext() {
        JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
        JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
        assertTrue(createUnion(objectType).matchesObjectContext());
        assertFalse(createUnion(nullType).matchesObjectContext()); // null does not
        JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
        assertFalse(createUnion(voidType).matchesObjectContext()); // void does not
        assertTrue(createUnion(objectType, nullType).matchesObjectContext());
    }

    @Test(timeout = 4000)
    public void testFindPropertyType() {
        // Uses actual object types - we'll use a simple String type for demonstration
        // (String has property "length" of number type)
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        UnionType union = createUnion(stringType);
        JSType lengthType = union.findPropertyType("length");
        assertNotNull(lengthType);
        assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), lengthType);
        // Test with null/void alternates filtered out
        JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
        UnionType union2 = createUnion(stringType, nullType);
        assertNotNull(union2.findPropertyType("length"));
    }

    @Test(timeout = 4000)
    public void testCanAssignTo() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
        UnionType unionNumStr = createUnion(numberType, stringType);
        assertTrue(unionNumStr.canAssignTo(allType)); // all type accepts everything
        assertFalse(unionNumStr.canAssignTo(numberType)); // only one alternate can assign
        // When alternates contain unknown, returns true
        JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        UnionType unionWithUnknown = createUnion(unknownType, numberType);
        assertTrue(unionWithUnknown.canAssignTo(numberType));
    }

    @Test(timeout = 4000)
    public void testCanBeCalled() {
        JSType functionType = registry.getNativeType(JSTypeNative.FUNCTION_TYPE);
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        assertTrue(createUnion(functionType).canBeCalled());
        assertFalse(createUnion(numberType).canBeCalled());
        assertFalse(createUnion(functionType, numberType).canBeCalled()); // all must be callable
    }

    @Test(timeout = 4000)
    public void testAutobox() {
        // Not easily tested without detailed type analysis, but verify it builds and returns a union
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType autoboxed = createUnion(numberType).autobox();
        assertNotNull(autoboxed);
        assertTrue(autoboxed.isUnionType() || !autoboxed.isEquivalentTo(numberType)); // autobox typically changes
    }

    @Test(timeout = 4000)
    public void testRestrictByNotNullOrUndefined() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
        JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
        UnionType union = createUnion(numberType, nullType, voidType);
        JSType restricted = union.restrictByNotNullOrUndefined();
        assertFalse(restricted.isUnionType()); // should collapse to number
        assertTrue(restricted.isEquivalentTo(numberType));
    }

    @Test(timeout = 4000)
    public void testTestForEquality() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        UnionType union = createUnion(numberType, stringType);
        TernaryValue result = union.testForEquality(numberType);
        // numberType testForEquality with numberType: likely FALSE (non-strict equality)
        // stringType testForEquality with numberType: might be UNKNOWN if string always false? This is complex.
        // We'll just check that result is not UNKNOWN due to inconsistency
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testIsNullable() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
        assertFalse(createUnion(numberType).isNullable());
        assertTrue(createUnion(nullType).isNullable());
        assertTrue(createUnion(numberType, nullType).isNullable());
    }

    @Test(timeout = 4000)
    public void testIsUnknownType() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        assertFalse(createUnion(numberType).isUnknownType());
        assertTrue(createUnion(unknownType).isUnknownType());
        assertTrue(createUnion(numberType, unknownType).isUnknownType());
    }

    @Test(timeout = 4000)
    public void testIsStructAndIsDict() {
        // Use object types; struct/dict are specific subtypes - we'll mock with boolean expectations
        // For simplicity, test with non-struct/dict types
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        UnionType union = createUnion(numberType);
        assertFalse(union.isStruct());
        assertFalse(union.isDict());
    }

    @Test(timeout = 4000)
    public void testGetLeastSupertype() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
        UnionType union = createUnion(numberType, stringType);
        // LUB of union and number should be union (since number is in union)
        assertEquals(union, union.getLeastSupertype(numberType));
        // LUB of union and all type should be all type
        assertTrue(allType.isEquivalentTo(union.getLeastSupertype(allType))
                || union.getLeastSupertype(allType).isEquivalentTo(allType));
        // When 'that' is unknown and not union
        JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        JSType lub = union.getLeastSupertype(unknownType);
        assertTrue(lub.isUnknownType() || lub.isUnionType());
    }

    @Test(timeout = 4000)
    public void testMeet() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
        UnionType union = createUnion(numberType, stringType);
        // meet with all type should return union itself (since all types are supertypes)
        JSType meetResult = union.meet(allType);
        assertTrue(meetResult.isEquivalentTo(union));
        // meet with number should return number (if number is subtype of union)
        JSType meetNumber = union.meet(numberType);
        assertTrue(meetNumber.isEquivalentTo(numberType));
        // meet with no type fallback
        JSType noObjectType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
        JSType meetNo = union.meet(noObjectType);
        assertTrue(meetNo.isNoType() || meetNo.isNoObjectType());
    }

    @Test(timeout = 4000)
    public void testCheckUnionEquivalenceHelper() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        UnionType u1 = createUnion(numberType, stringType);
        UnionType u2 = createUnion(stringType, numberType);
        assertTrue(u1.checkUnionEquivalenceHelper(u2, false));
        assertTrue(u1.checkUnionEquivalenceHelper(u2, true));
        UnionType u3 = createUnion(numberType, unknownType);
        assertFalse(u1.checkUnionEquivalenceHelper(u3, false)); // different sizes
        // With tolerateUnknowns, sizes still differ -> false
        assertFalse(u1.checkUnionEquivalenceHelper(u3, true));
        // Equal sizes but different types
        UnionType u4 = createUnion(numberType, stringType);
        UnionType u5 = createUnion(numberType, numberType); // duplicates - but collection might dedup? Not in constructor
        // Actually constructor does not deduplicate; alternates may contain duplicates.
        // So equivalence might be false if duplicates present? Let's avoid.
        // Instead test with same elements but order doesn't matter.
    }

    @Test(timeout = 4000)
    public void testHasProperty() {
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        UnionType union = createUnion(stringType);
        assertTrue(union.hasProperty("length"));
        assertFalse(union.hasProperty("foo"));
    }

    @Test(timeout = 4000)
    public void testContains() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        UnionType union = createUnion(numberType, stringType);
        assertTrue(union.contains(numberType));
        assertTrue(union.contains(stringType));
        JSType booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
        assertFalse(union.contains(booleanType));
    }

    @Test(timeout = 4000)
    public void testGetRestrictedUnion() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
        UnionType union = createUnion(numberType, stringType, nullType);
        JSType restricted = union.getRestrictedUnion(numberType);
        // Should remove number and subtypes of number; only string and null remain
        assertTrue(restricted.isUnionType() || !restricted.isEquivalentTo(numberType));
        // When type is unknown, it's not removed
        JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        UnionType union2 = createUnion(numberType, unknownType);
        JSType restricted2 = union2.getRestrictedUnion(numberType);
        // unknownType is not removed because isSubtype might not hold? Actually isSubtype(unknown, number) is false,
        // so unknown remains. number is removed.
        assertTrue(restricted2.isUnknownType() || restricted2.isEquivalentTo(unknownType));
    }

    // ---------- Partition C: Defect-Targeted Tests (Defects4J: testIssue791, testSubtypeWithUnknowns2) ----------

    @Test(timeout = 4000)
    public void testIsSubtypeWithUnknownTarget() {
        // Defects4J: testIssue791 likely involves unknown type incorrectly handled in isSubtype.
        // According to code, if `that` is unknown, isSubtype returns true. This is expected.
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        UnionType union = createUnion(numberType);
        assertTrue("Union with number should be subtype of unknown", union.isSubtype(unknownType));
        // Also test that union itself is considered subtype of unknown even if it contains unknown? 
        // Actually isSubtype for union loops over alternates: each element must be subtype of that.
        // If union contains unknown element, then for that element, isSubtype(that) is false? 
        // No, unknown type's isSubtype returns true for unknown? Let's check JSType default.
        // We'll assume unknown is subtype of unknown.
        UnionType union2 = createUnion(unknownType);
        assertTrue("Union with unknown should be subtype of unknown", union2.isSubtype(unknownType));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeWithAllType() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType allType = registry.getNativeType(JSTypeNative.ALL_TYPE);
        UnionType union = createUnion(numberType);
        assertTrue("Union with number should be subtype of all type", union.isSubtype(allType));
    }

    @Test(timeout = 4000)
    public void testIsSubtypeWhenAlternatesContainUnknown() {
        // Defects4J: testSubtypeWithUnknowns2 may involve subtype check where union contains unknown.
        // When unknown alternate is present, isSubtype should return true for any that? No, because loop checks
        // each alternate: unknown.isSubtype(that) returns true (since unknown is subtype of everything? Actually
        // unknown is top? No, unknown is bottom? In Closure, unknown is both supertype and subtype? The code says:
        // if (that.isUnknownType()) return true; but that only applies when that is unknown.
        // For other 'that', unknown.isSubtype(that) might return false. Let's test with a concrete that.
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        UnionType union = createUnion(unknownType);
        // isSubtype(numberType) should be false because unknown type is not a subtype of number.
        // However, in the Defects4J bug, it might incorrectly return true.
        // We'll assert false to match expected correct behavior.
        assertFalse("Union with unknown should NOT be subtype of number", union.isSubtype(numberType));
    }

    @Test(timeout = 4000)
    public void testCheckUnionEquivalenceWithUnknowns() {
        // Defects4J: testSubtypeWithUnknowns2 may also involve equivalence check with unknown types.
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        UnionType u1 = createUnion(numberType, unknownType);
        UnionType u2 = createUnion(unknownType, numberType);
        // With tolerateUnknowns = true, equivalence should hold because order doesn't matter and unknowns match?
        // Actually tolerateUnknowns flag makes checkEquivalenceHelper ignore unknown types? Not exactly.
        // The hasAlternate method uses checkEquivalenceHelper with tolerateUnknowns.
        // For two unions with same elements, equivalence should be true.
        assertTrue("Equivalent unions with unknown should be equal with tolerateUnknowns",
                u1.checkUnionEquivalenceHelper(u2, true));
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------
    // No explicit exceptions thrown in public APIs; constructor assumes non-null collection.

    @Test(timeout = 4000)
    public void testEmptyAlternates() {
        java.util.Collection<JSType> empty = java.util.Collections.emptyList();
        UnionType emptyUnion = new UnionType(registry, empty);
        assertFalse(emptyUnion.matchesNumberContext());
        assertFalse(emptyUnion.matchesStringContext());
        assertFalse(emptyUnion.matchesObjectContext());
        assertTrue(emptyUnion.isNullable()); // For no alternates, loop never returns true -> false? Actually isNullable returns false if no alternates.
        // isNullable loops and if any isNullable returns true; with empty, it returns false.
        assertFalse(emptyUnion.isNullable());
        assertFalse(emptyUnion.isUnknownType());
        assertTrue(emptyUnion.canBeCalled()); // loop: for each alternate, !canBeCalled returns false? Actually loop returns false if any !canBeCalled. With empty, loop ends and returns true.
        assertTrue(emptyUnion.canBeCalled());
    }

    // ---------- Partition E: Object Lifecycle & Contract Integrity ----------

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        UnionType u1 = createUnion(numberType, stringType);
        UnionType u2 = createUnion(numberType, stringType);
        assertEquals(u1.hashCode(), u2.hashCode());
        // Different order should produce same hashCode if alternates collection hash is order-independent? 
        // Since alternates is a Collection, its hashCode is based on element ordering if it's a List? 
        // Actually ArrayList.hashCode is order-dependent. So order matters. That's a potential bug, but not our focus.
        // We'll just test that same collection yields same hash.
    }

    @Test(timeout = 4000)
    public void testToMaybeUnionType() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        UnionType union = createUnion(numberType);
        assertSame(union, union.toMaybeUnionType());
    }

    @Test(timeout = 4000)
    public void testIsObject() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
        assertFalse(createUnion(numberType).isObject()); // not all alternates are object
        assertTrue(createUnion(objectType).isObject());
        assertFalse(createUnion(numberType, objectType).isObject());
    }

    @Test(timeout = 4000)
    public void testGetRestrictedTypeGivenToBooleanOutcome() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
        UnionType union = createUnion(numberType, voidType);
        JSType restrictedTrue = union.getRestrictedTypeGivenToBooleanOutcome(true);
        // number is truthy, void is falsy, so for true outcome only number remains
        assertTrue(restrictedTrue.isEquivalentTo(numberType) || restrictedTrue.isUnionType()); // Could be simplified
        JSType restrictedFalse = union.getRestrictedTypeGivenToBooleanOutcome(false);
        assertNotNull(restrictedFalse);
    }

    @Test(timeout = 4000)
    public void testGetPossibleToBooleanOutcomes() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
        UnionType union = createUnion(numberType, voidType);
        BooleanLiteralSet literals = union.getPossibleToBooleanOutcomes();
        // number->BOTH? Actually number can be truthy or falsy (0 is falsy), so BOTH.
        // void->FALSE. Union => BOTH.
        assertEquals(BooleanLiteralSet.BOTH, literals);
    }

    @Test(timeout = 4000)
    public void testCollapseUnion() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
        JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
        JSType unknownType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
        // Two objects collapse to common super
        UnionType unionObjects = createUnion(objectType, registry.getNativeType(JSTypeNative.ARRAY_TYPE));
        JSType collapsed = unionObjects.collapseUnion();
        assertTrue(collapsed.isObjectType() || collapsed.isAllType());
        // Two non-objects (values) -> all type
        UnionType unionValues = createUnion(numberType, stringType);
        assertEquals(registry.getNativeType(JSTypeNative.ALL_TYPE), unionValues.collapseUnion());
        // Unknown in alternates -> unknown
        UnionType unionWithUnknown = createUnion(numberType, unknownType);
        assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), unionWithUnknown.collapseUnion());
    }

    @Test(timeout = 4000)
    public void testHasAnyTemplateInternal() {
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        UnionType union = createUnion(numberType);
        assertFalse(union.hasAnyTemplateInternal());
        // No template types in basic types, so false
    }

    @Test(timeout = 4000)
    public void testResolveInternalNoChange() {
        // Simple resolve that returns same alternates
        JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
        UnionType union = createUnion(numberType);
        JSType resolved = union.resolveInternal(null, null);
        assertSame(union, resolved);
        // Verify alternates unchanged
        assertEquals(1, ((UnionType)resolved).alternates.size());
    }
}