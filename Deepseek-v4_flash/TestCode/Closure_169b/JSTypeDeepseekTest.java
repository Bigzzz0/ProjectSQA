package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.jstype.JSType.TypePair;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Targeted decision branches and boundary conditions:
 * - isSubtype(): Unknown type, All type, equality, union alternates, ProxyObjectType delegation
 * - checkEquivalenceHelper(): same object, unknown types (with/without tolerateUnknowns), 
 *   nominal type mismatch, ProxyObjectType unboxing
 * - getGreatestSubtype(): function types, unknown types, subtype relations, union/record/enum meets, 
 *   NoResolvedType filtering
 * - getLeastSupertype(): union delegation, equivalence shortcut, union creation
 * - testForEqualityHelper(): all/unknown/noResolved, empty types, function types, enum/union delegation
 * - isEmptyType(): combinations of NoType, NoObjectType, NoResolvedType, LEAST_FUNCTION_TYPE
 * - isString()/isNumber(): subtyping against STRING_VALUE_OR_OBJECT_TYPE and NUMBER_VALUE_OR_OBJECT_TYPE
 * - resolve/clearResolved: state transitions and edge cases
 * - filterNoResolvedType: union filtering logic
 * 
 * Defect-specific targeting (testIssue791 & testSubtypeWithUnknowns2):
 * - Subtyping when one type is unknown or contains unknown components
 * - Equivalence between unknown and nominal types (tolerateUnknowns = false)
 * - Proper handling of NoResolvedType in subtyping/meet operations
 * - Interaction of unknown types with proxy types (e.g., NamedType)
 */
public class JSTypeDeepseekTest {

  private static JSTypeRegistry registry;

  @BeforeClass
  public static void setUp() {
    ErrorReporter reporter = new ErrorReporter() {
      @Override
      public void warning(String message, String sourceName, int line, int lineOffset) {}
      @Override
      public void error(String message, String sourceName, int line, int lineOffset) {}
    };
    registry = new JSTypeRegistry(reporter);
  }

  // ---------- Partition A: Core Functional Logic & State Transitions ----------

  @Test(timeout = 4000)
  public void testIsNoType() {
    assertTrue(registry.getNativeType(JSTypeNative.NO_TYPE).isNoType());
    assertFalse(registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE).isNoType());
    assertFalse(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE).isNoType());
  }

  @Test(timeout = 4000)
  public void testIsNoResolvedType() {
    assertTrue(registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE).isNoResolvedType());
    assertFalse(registry.getNativeType(JSTypeNative.NO_TYPE).isNoResolvedType());
  }

  @Test(timeout = 4000)
  public void testIsNoObjectType() {
    assertTrue(registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE).isNoObjectType());
    assertFalse(registry.getNativeType(JSTypeNative.NO_TYPE).isNoObjectType());
  }

  @Test(timeout = 4000)
  public void testIsEmptyType() {
    JSType noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    JSType noObjType = registry.getNativeType(JSTypeNative.NO_OBJECT_TYPE);
    JSType noResolved = registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);
    JSType leastFunc = registry.getNativeType(JSTypeNative.LEAST_FUNCTION_TYPE);
    JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);

    assertTrue(noType.isEmptyType());
    assertTrue(noObjType.isEmptyType());
    assertTrue(noResolved.isEmptyType());
    assertTrue(leastFunc.isEmptyType());
    assertFalse(unknown.isEmptyType());
  }

  @Test(timeout = 4000)
  public void testIsStringAndIsNumber() {
    JSType stringVal = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberVal = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringObj = registry.getNativeType(JSTypeNative.STRING_OBJECT_TYPE);
    JSType numberObj = registry.getNativeType(JSTypeNative.NUMBER_OBJECT_TYPE);
    JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);

    assertTrue(stringVal.isString());
    assertTrue(stringObj.isString());
    assertTrue(numberVal.isNumber());
    assertTrue(numberObj.isNumber());
    assertFalse(stringVal.isNumber());
    assertFalse(numberVal.isString());
    assertTrue(unknown.isString());  // unknown is subtype of everything
    assertTrue(unknown.isNumber());
  }

  @Test(timeout = 4000)
  public void testIsFunctionTypeAndToMaybeFunctionType() {
    JSType fn = registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE);
    JSType obj = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType noType = registry.getNativeType(JSTypeNative.NO_TYPE);

    assertTrue(fn.isFunctionType());
    assertNotNull(fn.toMaybeFunctionType());
    assertFalse(obj.isFunctionType());
    assertNull(obj.toMaybeFunctionType());
    assertFalse(noType.isFunctionType());
    assertNull(noType.toMaybeFunctionType());
  }

  @Test(timeout = 4000)
  public void testIsUnionTypeAndToMaybeUnionType() {
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    assertTrue(union.isUnionType());
    assertNotNull(union.toMaybeUnionType());
    assertFalse(number.isUnionType());
    assertNull(number.toMaybeUnionType());
  }

  @Test(timeout = 4000)
  public void testIsObjectAndToObjectType() {
    JSType obj = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);

    assertTrue(obj.isObject());
    assertNotNull(obj.toObjectType());
    assertFalse(number.isObject());
    assertNull(number.toObjectType());
    assertFalse(nullType.isObject());
    assertNull(nullType.toObjectType());
  }

  // ---------- Partition B: Boundary Value Analysis & Extremes ----------

  @Test(timeout = 4000)
  public void testIsSubtypeWithUnknownAndAll() {
    JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    JSType all = registry.getNativeType(JSTypeNative.ALL_TYPE);
    JSType noType = registry.getNativeType(JSTypeNative.NO_TYPE);
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    // unknown is subtype of everything
    assertTrue(unknown.isSubtype(all));
    assertTrue(unknown.isSubtype(number));
    assertTrue(unknown.isSubtype(noType));  // NoType is bottom, but unknown is subtype of everything
    // all is supertype of everything
    assertTrue(number.isSubtype(all));
    assertTrue(noType.isSubtype(all));
    // NoType is subtype of everything
    assertTrue(noType.isSubtype(number));
    assertTrue(noType.isSubtype(unknown));
  }

  @Test(timeout = 4000)
  public void testIsSubtypeWithUnion() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType string = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType union = registry.createUnionType(number, string);

    // element is subtype of union
    assertTrue(number.isSubtype(union));
    assertTrue(string.isSubtype(union));
    // union is not subtype of element
    assertFalse(union.isSubtype(number));
    assertFalse(union.isSubtype(string));
  }

  @Test(timeout = 4000)
  public void testIsEquivalentToBasics() {
    JSType number1 = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType number2 = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType string = registry.getNativeType(JSTypeNative.STRING_TYPE);

    // same type from registry should be identical object
    assertTrue(number1.isEquivalentTo(number2));
    assertFalse(number1.isEquivalentTo(string));
    assertFalse(number1.isEquivalentTo(null));
  }

  @Test(timeout = 4000)
  public void testIsEquivalentToUnknownAndNominal() {
    JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    JSType unknown2 = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    // two unknown types are equivalent
    assertTrue(unknown.isEquivalentTo(unknown2));
    // unknown is not equivalent to number (unless nominal type check)
    assertFalse(unknown.isEquivalentTo(number));
  }

  @Test(timeout = 4000)
  public void testCheckEquivalenceHelperTolerateUnknowns() {
    JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    // tolerateUnknowns=true: unknown is invariant with everyone
    assertTrue(unknown.checkEquivalenceHelper(number, true));
    // tolerateUnknowns=false: unknown not equivalent to number
    assertFalse(unknown.checkEquivalenceHelper(number, false));
  }

  // ---------- Partition C: Defect-Targeted Branch Zone ----------

  @Test(timeout = 4000)
  public void testIsSubtypeWithNoResolvedType() {
    // Defect: testIssue791 and testSubtypeWithUnknowns2
    // NoResolvedType should behave similarly to unknown in some contexts
    JSType noResolved = registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);
    JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    // NoResolvedType is subtype of unknown (since unknown is supertype of all)
    assertTrue(noResolved.isSubtype(unknown));
    // NoResolvedType is subtype of number? Actually NoResolvedType is bottom-ish, so it should be subtype of number
    assertTrue(noResolved.isSubtype(number));
    // But unknown is not subtype of NoResolvedType (NoResolvedType is not unknown)
    assertFalse(unknown.isSubtype(noResolved));
  }

  @Test(timeout = 4000)
  public void testGreatestSubtypeWithUnknown() {
    JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType all = registry.getNativeType(JSTypeNative.ALL_TYPE);

    // getGreatestSubtype(unknown, number) should be unknown (unless equal)
    JSType result = number.getGreatestSubtype(unknown);
    assertTrue(result.isUnknownType());

    // getGreatestSubtype(unknown, unknown) should be unknown
    result = unknown.getGreatestSubtype(unknown);
    assertTrue(result.isUnknownType());
  }

  @Test(timeout = 4000)
  public void testLeastSupertypeWithUnknown() {
    JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType all = registry.getNativeType(JSTypeNative.ALL_TYPE);

    // getLeastSupertype(number, unknown) should be unknown
    JSType result = number.getLeastSupertype(unknown);
    assertTrue(result.isUnknownType());

    // getLeastSupertype(all, unknown) should be all
    result = all.getLeastSupertype(unknown);
    assertTrue(result.isAllType());
  }

  @Test(timeout = 4000)
  public void testFilterNoResolvedType() {
    // Package-private method accessible because same package
    JSType noResolved = registry.getNativeType(JSTypeNative.NO_RESOLVED_TYPE);
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType union = registry.createUnionType(noResolved, number);

    JSType filtered = JSType.filterNoResolvedType(union);
    // The filtered union should not contain NoResolvedType
    assertFalse(filtered.isNoResolvedType());
    assertTrue(filtered.isUnionType());
    // The filtered type should be just number
    assertTrue(filtered.isEquivalentTo(number));
  }

  // ---------- Partition D: Exception & Defensive Guard Paths ----------

  @Test(timeout = 4000)
  public void testResolveAndClearResolved() {
    JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    // Initially not resolved? Actually the type may be resolved already. But we can test clearResolved
    unknown.clearResolved();
    assertFalse(unknown.isResolved());
    // Resolve again (needs ErrorReporter and scope, but we can use a simple scope)
    ErrorReporter reporter = new ErrorReporter() {
      @Override public void warning(String msg, String src, int line, int offset) {}
      @Override public void error(String msg, String src, int line, int offset) {}
    };
    StaticScope<JSType> scope = new StaticScope<JSType>() {
      // Minimal implementation for test
      @Override public StaticScope<JSType> getParentScope() { return null; }
      @Override public JSType getSlot(String name) { return null; }
    };
    JSType resolved = unknown.resolve(reporter, scope);
    assertTrue(resolved.isResolved());
  }

  @Test(timeout = 4000)
  public void testForceResolve() {
    JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    ErrorReporter reporter = new ErrorReporter() {
      @Override public void warning(String msg, String src, int line, int offset) {}
      @Override public void error(String msg, String src, int line, int offset) {}
    };
    StaticScope<JSType> scope = new StaticScope<JSType>() {
      @Override public StaticScope<JSType> getParentScope() { return null; }
      @Override public JSType getSlot(String name) { return null; }
    };
    JSType result = unknown.forceResolve(reporter, scope);
    assertNotNull(result);
  }

  @Test(timeout = 4000)
  public void testMatchConstraintDoesNothing() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType string = registry.getNativeType(JSTypeNative.STRING_TYPE);
    // Should not throw
    number.matchConstraint(string);
  }

  // ---------- Partition E: Object Lifecycle & Contract Integrity ----------

  @Test(timeout = 4000)
  public void testEqualsAndHashCode() {
    JSType number1 = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType number2 = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType string = registry.getNativeType(JSTypeNative.STRING_TYPE);

    // equals uses isEquivalentTo
    assertTrue(number1.equals(number2));
    assertFalse(number1.equals(string));
    assertFalse(number1.equals(null));

    // hashCode is identity based
    assertEquals(System.identityHashCode(number1), number1.hashCode());
  }

  @Test(timeout = 4000)
  public void testToStringAndToAnnotationString() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));

    assertNotNull(number.toString());
    assertNotNull(number.toAnnotationString());
    assertNotNull(union.toString());
    assertNotNull(union.toAnnotationString());
  }

  @Test(timeout = 4000)
  public void testGetJSDocInfoReturnsNull() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertNull(number.getJSDocInfo());
  }

  @Test(timeout = 4000)
  public void testGetDisplayNameAndHasDisplayName() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertNull(number.getDisplayName());
    assertFalse(number.hasDisplayName());
  }

  @Test(timeout = 4000)
  public void testHasPropertyReturnsFalse() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertFalse(number.hasProperty("foo"));
  }

  @Test(timeout = 4000)
  public void testCanBeCalledReturnsFalse() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertFalse(number.canBeCalled());
  }

  @Test(timeout = 4000)
  public void testIsNullable() {
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    assertTrue(nullType.isNullable());
    assertFalse(voidType.isNullable()); // void is not subtype of null
    assertFalse(number.isNullable());
  }

  @Test(timeout = 4000)
  public void testAutoboxesToAndUnboxesTo() {
    JSType numberVal = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType numberObj = registry.getNativeType(JSTypeNative.NUMBER_OBJECT_TYPE);
    JSType stringVal = registry.getNativeType(JSTypeNative.STRING_TYPE);

    assertEquals(numberObj, numberVal.autoboxesTo());
    assertEquals(numberVal, numberObj.unboxesTo());
    assertNull(stringVal.unboxesTo()); // string object unboxes to string, but string value does not unbox
    assertNotNull(stringVal.autoboxesTo());
  }

  @Test(timeout = 4000)
  public void testRestrictByNotNullOrUndefined() {
    JSType nullable = registry.createUnionType(
        registry.getNativeType(JSTypeNative.NULL_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    JSType restricted = nullable.restrictByNotNullOrUndefined();
    assertTrue(restricted.isEquivalentTo(registry.getNativeType(JSTypeNative.NUMBER_TYPE)));
  }

  @Test(timeout = 4000)
  public void testGetPossibleToBooleanOutcomes() {
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    JSType booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);

    assertEquals(BooleanLiteralSet.FALSE, nullType.getPossibleToBooleanOutcomes());
    assertEquals(BooleanLiteralSet.FALSE, voidType.getPossibleToBooleanOutcomes());
    assertEquals(BooleanLiteralSet.BOTH, booleanType.getPossibleToBooleanOutcomes());
  }

  @Test(timeout = 4000)
  public void testGetRestrictedTypeGivenToBooleanOutcome() {
    JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    // Unknown with outcome true returns CHECKED_UNKNOWN
    JSType restricted = unknown.getRestrictedTypeGivenToBooleanOutcome(true);
    assertTrue(restricted.isCheckedUnknownType());

    // Number with outcome false should return NO_TYPE because number can be both true/false? Actually number can be both, so it returns itself
    restricted = number.getRestrictedTypeGivenToBooleanOutcome(false);
    assertTrue(restricted.isEquivalentTo(number));

    // Null with outcome false should return null (since null only false)
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    restricted = nullType.getRestrictedTypeGivenToBooleanOutcome(true);
    assertTrue(restricted.isNoType());
  }

  @Test(timeout = 4000)
  public void testTestForEquality() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType string = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);

    // null and void are equal (TRUE)
    assertEquals(TernaryValue.TRUE, nullType.testForEquality(voidType));
    // number and string are unknown
    assertEquals(TernaryValue.UNKNOWN, number.testForEquality(string));
    // null and number are unknown (null can be converted to object?)
    assertEquals(TernaryValue.UNKNOWN, nullType.testForEquality(number));
  }

  @Test(timeout = 4000)
  public void testCanTestForEqualityWith() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType string = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);

    assertTrue(number.canTestForEqualityWith(string));  // UNKNOWN
    assertTrue(nullType.canTestForEqualityWith(voidType)); // TRUE
  }

  @Test(timeout = 4000)
  public void testCanAssignTo() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType all = registry.getNativeType(JSTypeNative.ALL_TYPE);
    JSType string = registry.getNativeType(JSTypeNative.STRING_TYPE);

    assertTrue(number.canAssignTo(all));
    assertFalse(number.canAssignTo(string));
  }

  @Test(timeout = 4000)
  public void testFindPropertyType() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType object = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    // number autoboxes to NumberObject, which has properties
    JSType propType = number.findPropertyType("toString");
    assertNotNull(propType); // function type
    // null type has no properties
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    assertNull(nullType.findPropertyType("foo"));
  }

  @Test(timeout = 4000)
  public void testDereference() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType object = number.dereference();
    assertTrue(object.isObjectType());
    assertTrue(object.isEquivalentTo(registry.getNativeType(JSTypeNative.NUMBER_OBJECT_TYPE)));
  }

  @Test(timeout = 4000)
  public void testGetTypesUnderEquality() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);

    // null and void: equality yields TRUE -> restricted pair (null, void)
    TypePair pair = nullType.getTypesUnderEquality(voidType);
    assertTrue(pair.typeA.isEquivalentTo(nullType));
    assertTrue(pair.typeB.isEquivalentTo(voidType));

    // number and null: equality yields UNKNOWN -> restricted pair (number, null)
    pair = number.getTypesUnderEquality(nullType);
    assertTrue(pair.typeA.isEquivalentTo(number));
    assertTrue(pair.typeB.isEquivalentTo(nullType));
  }

  @Test(timeout = 4000)
  public void testGetTypesUnderInequality() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);

    // null and null: equality TRUE -> inequality yields NO_TYPE
    TypePair pair = nullType.getTypesUnderInequality(nullType);
    assertTrue(pair.typeA.isNoType());
    assertTrue(pair.typeB.isNoType());

    // number and null: inequality UNKNOWN -> unrestricted
    pair = number.getTypesUnderInequality(nullType);
    assertTrue(pair.typeA.isEquivalentTo(number));
    assertTrue(pair.typeB.isEquivalentTo(nullType));
  }

  @Test(timeout = 4000)
  public void testGetTypesUnderShallowEquality() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType string = registry.getNativeType(JSTypeNative.STRING_TYPE);

    // common type is NO_TYPE because they are unrelated
    TypePair pair = number.getTypesUnderShallowEquality(string);
    assertTrue(pair.typeA.isNoType());
    assertTrue(pair.typeB.isNoType());

    // number with number -> common is number
    pair = number.getTypesUnderShallowEquality(number);
    assertTrue(pair.typeA.isEquivalentTo(number));
    assertTrue(pair.typeB.isEquivalentTo(number));
  }

  @Test(timeout = 4000)
  public void testGetTypesUnderShallowInequality() {
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);

    // null and null -> inequality true -> both null
    TypePair pair = nullType.getTypesUnderShallowInequality(nullType);
    assertNull(pair.typeA);
    assertNull(pair.typeB);

    // null and void -> inequality false (they are not shallow equal) -> return (null, void)
    pair = nullType.getTypesUnderShallowInequality(voidType);
    assertTrue(pair.typeA.isEquivalentTo(nullType));
    assertTrue(pair.typeB.isEquivalentTo(voidType));
  }

  @Test(timeout = 4000)
  public void testVisit() {
    // Abstract method, but we can test on a concrete type
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    Visitor<String> visitor = new Visitor<String>() {
      @Override public String caseNoType(NoType type) { return "NoType"; }
      @Override public String caseNoResolvedType(NoResolvedType type) { return "NoResolved"; }
      @Override public String caseNoObjectType(NoObjectType type) { return "NoObject"; }
      @Override public String caseAllType(AllType type) { return "All"; }
      @Override public String caseBooleanType(BooleanType type) { return "Boolean"; }
      @Override public String caseEnumElementType(EnumElementType type) { return "EnumElement"; }
      @Override public String caseEnumType(EnumType type) { return "Enum"; }
      @Override public String caseFunctionType(FunctionType type) { return "Function"; }
      @Override public String caseNullType(NullType type) { return "Null"; }
      @Override public String caseNumberType(NumberType type) { return "Number"; }
      @Override public String caseObjectType(ObjectType type) { return "Object"; }
      @Override public String caseStringType(StringType type) { return "String"; }
      @Override public String caseUnionType(UnionType type) { return "Union"; }
      @Override public String caseUnknownType(UnknownType type) { return "Unknown"; }
      @Override public String caseVoidType(VoidType type) { return "Void"; }
      @Override public String caseParameterizedType(ParameterizedType type) { return "Parameterized"; }
      @Override public String caseTemplateType(TemplateType type) { return "Template"; }
      @Override public String caseRecordType(RecordType type) { return "Record"; }
      @Override public String caseProxyObjectType(ProxyObjectType type) { return "Proxy"; }
    };
    assertEquals("Number", number.visit(visitor));
  }

  @Test(timeout = 4000)
  public void testStaticIsEquivalent() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType string = registry.getNativeType(JSTypeNative.STRING_TYPE);

    assertTrue(JSType.isEquivalent(number, number));
    assertFalse(JSType.isEquivalent(number, string));
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(number, null));
  }

  @Test(timeout = 4000)
  public void testHasAnyTemplate() {
    JSType number = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertFalse(number.hasAnyTemplate());
  }
}