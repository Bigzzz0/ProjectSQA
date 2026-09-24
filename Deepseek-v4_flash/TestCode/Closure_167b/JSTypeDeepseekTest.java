package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import org.junit.Before;
import org.junit.Test;

public class JSTypeDeepseekTest {
  /* [Branch & Defect Analysis Matrix]
   *
   * Defect target 1: getRestrictedTypeGivenToBooleanOutcome must preserve
   * NO_RESOLVED_TYPE. The buggy collapse to NO_TYPE causes the known
   * JSTypeTest.testRestrictedTypeGivenToBoolean failure.
   *
   * Defect target 2: testForEqualityHelper must not return null for ordinary
   * comparable types; a null return propagates to canTestForEqualityWith and
   * getTypesUnderEquality/Inequality, producing NPEs.
   *
   * Additional branches: empty types, unknown types, subtype lattice,
   * union supertype creation, greatest subtype, equality switches,
   * shallow inequality, context matching, conversion defaults.
   */
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new ErrorReporter() {
      @Override
      public void warning(String message, String sourceName, int line,
          int lineOffset) {
      }

      @Override
      public void error(String message, String sourceName, int line,
          int lineOffset) {
      }
    });
  }

  private JSType type(JSTypeNative nativeType) {
    return registry.getNativeType(nativeType);
  }

  @Test(timeout = 4000)
  public void testNativeTypePredicates() {
    JSType noType = type(JSTypeNative.NO_TYPE);
    JSType noObjectType = type(JSTypeNative.NO_OBJECT_TYPE);
    JSType noResolvedType = type(JSTypeNative.NO_RESOLVED_TYPE);
    JSType numberType = type(JSTypeNative.NUMBER_TYPE);

    assertTrue(noType.isNoType());
    assertTrue(noObjectType.isNoObjectType());
    assertTrue(noResolvedType.isNoResolvedType());
    assertTrue(noType.isEmptyType());
    assertTrue(noObjectType.isEmptyType());
    assertTrue(noResolvedType.isEmptyType());
    assertFalse(numberType.isEmptyType());
  }

  @Test(timeout = 4000)
  public void testRestrictedTypeGivenToBooleanOutcomeKnownTypes() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);
    JSType nullType = type(JSTypeNative.NULL_TYPE);
    JSType voidType = type(JSTypeNative.VOID_TYPE);
    JSType unknown = type(JSTypeNative.UNKNOWN_TYPE);
    JSType all = type(JSTypeNative.ALL_TYPE);
    JSType noType = type(JSTypeNative.NO_TYPE);

    assertSame(number, number.getRestrictedTypeGivenToBooleanOutcome(true));
    assertSame(number, number.getRestrictedTypeGivenToBooleanOutcome(false));
    assertSame(nullType, nullType.getRestrictedTypeGivenToBooleanOutcome(false));
    assertTrue(nullType.getRestrictedTypeGivenToBooleanOutcome(true).isNoType());
    assertSame(voidType, voidType.getRestrictedTypeGivenToBooleanOutcome(false));
    assertTrue(voidType.getRestrictedTypeGivenToBooleanOutcome(true).isNoType());
    assertSame(unknown, unknown.getRestrictedTypeGivenToBooleanOutcome(true));
    assertSame(unknown, unknown.getRestrictedTypeGivenToBooleanOutcome(false));
    assertSame(all, all.getRestrictedTypeGivenToBooleanOutcome(true));
    assertSame(all, all.getRestrictedTypeGivenToBooleanOutcome(false));
    assertSame(noType, noType.getRestrictedTypeGivenToBooleanOutcome(true));
    assertSame(noType, noType.getRestrictedTypeGivenToBooleanOutcome(false));
  }

  @Test(timeout = 4000)
  public void testRestrictedTypeGivenToBooleanOutcomeKeepsUnresolvedType() {
    JSType noResolved = type(JSTypeNative.NO_RESOLVED_TYPE);
    assertTrue(noResolved.isNoResolvedType());

    JSType trueRestricted = noResolved.getRestrictedTypeGivenToBooleanOutcome(true);
    assertFalse("Unresolved type must not collapse to bottom on true outcome",
        trueRestricted.isNoType());
    assertSame("Unresolved type should remain unresolved for true outcome",
        noResolved, trueRestricted);

    JSType falseRestricted = noResolved.getRestrictedTypeGivenToBooleanOutcome(false);
    assertFalse("Unresolved type must not collapse to bottom on false outcome",
        falseRestricted.isNoType());
    assertSame("Unresolved type should remain unresolved for false outcome",
        noResolved, falseRestricted);
  }

  @Test(timeout = 4000)
  public void testTestForEqualityDoesNotReturnNull() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);
    JSType string = type(JSTypeNative.STRING_TYPE);
    assertNotNull(number.testForEquality(string));
    assertNotNull(string.testForEquality(number));
    assertNotNull(number.testForEquality(number));
  }

  @Test(timeout = 4000)
  public void testTestForEqualityTernaryValues() {
    JSType nullType = type(JSTypeNative.NULL_TYPE);
    JSType voidType = type(JSTypeNative.VOID_TYPE);
    JSType number = type(JSTypeNative.NUMBER_TYPE);
    JSType unknown = type(JSTypeNative.UNKNOWN_TYPE);

    assertSame(TernaryValue.TRUE, nullType.testForEquality(voidType));
    assertSame(TernaryValue.FALSE, nullType.testForEquality(number));
    assertSame(TernaryValue.UNKNOWN, unknown.testForEquality(number));
  }

  @Test(timeout = 4000)
  public void testCanTestForEqualityWith() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);
    JSType string = type(JSTypeNative.STRING_TYPE);
    JSType nullType = type(JSTypeNative.NULL_TYPE);
    JSType voidType = type(JSTypeNative.VOID_TYPE);

    assertTrue(number.canTestForEqualityWith(string));
    assertTrue(number.canTestForEqualityWith(number));
    assertFalse(nullType.canTestForEqualityWith(voidType));
    assertFalse(nullType.canTestForEqualityWith(number));
  }

  @Test(timeout = 4000)
  public void testGetTypesUnderEquality() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);
    JSType string = type(JSTypeNative.STRING_TYPE);
    JSType nullType = type(JSTypeNative.NULL_TYPE);
    JSType voidType = type(JSTypeNative.VOID_TYPE);

    JSType.TypePair p1 = number.getTypesUnderEquality(string);
    assertSame(number, p1.typeA);
    assertSame(string, p1.typeB);

    JSType.TypePair p2 = nullType.getTypesUnderEquality(voidType);
    assertSame(nullType, p2.typeA);
    assertSame(voidType, p2.typeB);

    JSType.TypePair p3 = nullType.getTypesUnderEquality(number);
    assertNull(p3.typeA);
    assertNull(p3.typeB);
  }

  @Test(timeout = 4000)
  public void testGetTypesUnderInequality() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);
    JSType string = type(JSTypeNative.STRING_TYPE);
    JSType nullType = type(JSTypeNative.NULL_TYPE);
    JSType voidType = type(JSTypeNative.VOID_TYPE);

    JSType.TypePair p1 = number.getTypesUnderInequality(string);
    assertSame(number, p1.typeA);
    assertSame(string, p1.typeB);

    JSType.TypePair p2 = nullType.getTypesUnderInequality(voidType);
    assertTrue(p2.typeA.isNoType());
    assertTrue(p2.typeB.isNoType());

    JSType.TypePair p3 = nullType.getTypesUnderInequality(number);
    assertSame(nullType, p3.typeA);
    assertSame(number, p3.typeB);
  }

  @Test(timeout = 4000)
  public void testGetTypesUnderShallowEqualityAndInequality() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);
    JSType nullType = type(JSTypeNative.NULL_TYPE);

    JSType.TypePair eq = number.getTypesUnderShallowEquality(number);
    assertSame(number, eq.typeA);
    assertSame(number, eq.typeB);

    JSType.TypePair ineq = nullType.getTypesUnderShallowInequality(nullType);
    assertNull(ineq.typeA);
    assertNull(ineq.typeB);

    JSType.TypePair ineq2 = number.getTypesUnderShallowInequality(number);
    assertSame(number, ineq2.typeA);
    assertSame(number, ineq2.typeB);
  }

  @Test(timeout = 4000)
  public void testIsSubtypeAndCanAssignTo() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);
    JSType string = type(JSTypeNative.STRING_TYPE);
    JSType unknown = type(JSTypeNative.UNKNOWN_TYPE);
    JSType all = type(JSTypeNative.ALL_TYPE);
    JSType noType = type(JSTypeNative.NO_TYPE);

    assertTrue(number.isSubtype(number));
    assertTrue(number.isSubtype(unknown));
    assertTrue(number.isSubtype(all));
    assertFalse(string.isSubtype(number));
    assertTrue(noType.isSubtype(number));

    assertTrue(number.canAssignTo(number));
    assertTrue(number.canAssignTo(unknown));
    assertFalse(string.canAssignTo(number));
    assertTrue(noType.canAssignTo(number));
  }

  @Test(timeout = 4000)
  public void testLeastAndGreatestSubtype() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);
    JSType string = type(JSTypeNative.STRING_TYPE);
    JSType unknown = type(JSTypeNative.UNKNOWN_TYPE);
    JSType noType = type(JSTypeNative.NO_TYPE);

    assertSame(number, number.getLeastSupertype(number));
    JSType union = number.getLeastSupertype(string);
    assertTrue(union.isUnionType());

    assertSame(unknown, number.getGreatestSubtype(unknown));
    assertSame(unknown, unknown.getGreatestSubtype(number));
    JSType meet = number.getGreatestSubtype(string);
    assertTrue(meet.isNoType());
    assertSame(noType, noType.getGreatestSubtype(number));
  }

  @Test(timeout = 4000)
  public void testEquivalenceAndDiffers() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);
    JSType string = type(JSTypeNative.STRING_TYPE);
    JSType unknown = type(JSTypeNative.UNKNOWN_TYPE);

    assertTrue(number.isEquivalentTo(number));
    assertFalse(number.isEquivalentTo(string));
    assertTrue(JSType.isEquivalent(number, number));
    assertTrue(JSType.isEquivalent(null, null));
    assertFalse(JSType.isEquivalent(number, null));
    assertFalse(JSType.isEquivalent(null, number));

    assertTrue(number.equals(number));
    assertFalse(number.equals(string));
    assertFalse(number.equals(null));
    assertFalse(number.equals("not a type"));
    assertEquals(number.hashCode(), number.hashCode());

    assertTrue(number.differsFrom(string));
    assertFalse(number.differsFrom(number));
    assertTrue(number.differsFrom(unknown));
    assertTrue(unknown.differsFrom(number));
    assertFalse(unknown.differsFrom(unknown));
  }

  @Test(timeout = 4000)
  public void testMatchesContexts() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);
    JSType string = type(JSTypeNative.STRING_TYPE);

    assertTrue(number.matchesNumberContext());
    assertTrue(number.matchesInt32Context());
    assertTrue(number.matchesUint32Context());
    assertTrue(string.matchesStringContext());
  }

  @Test(timeout = 4000)
  public void testConversionDefaults() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);

    assertNull(JSType.toMaybeFunctionType(null));
    assertNull(JSType.toMaybeParameterizedType(null));
    assertNull(JSType.toMaybeTemplateType(null));
    assertNull(number.toMaybeFunctionType());
    assertNull(number.toMaybeUnionType());
    assertNull(number.toMaybeParameterizedType());
    assertNull(number.toMaybeTemplateType());
    assertNull(number.toMaybeRecordType());
    assertNull(number.toMaybeEnumElementType());
    assertNull(number.toMaybeEnumType());
  }

  @Test(timeout = 4000)
  public void testFindPropertyTypeAndAutobox() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);
    assertNull(number.findPropertyType("__definitelyNotAProperty__"));
    assertNotNull(number.autoboxesTo());
    assertNotNull(number.autobox());
  }

  @Test(timeout = 4000)
  public void testDisplayNameAndStringForms() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);
    assertNotNull(number.toString());
    assertNotNull(number.toAnnotationString());
    assertNotNull(number.toDebugHashCodeString());
  }

  @Test(timeout = 4000)
  public void testHasAnyTemplateAndPredicateDefaults() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);

    assertFalse(number.hasAnyTemplate());
    assertFalse(number.isFunctionType());
    assertFalse(number.isUnionType());
    assertFalse(number.isRecordType());
    assertFalse(number.isParameterizedType());
    assertFalse(number.isTemplateType());
    assertFalse(number.isEnumElementType());
    assertFalse(number.isEnumType());
    assertFalse(number.isConstructor());
    assertFalse(number.isNominalType());
    assertFalse(number.isInterface());
    assertFalse(number.isOrdinaryFunction());
    assertFalse(number.isInstanceType());
    assertFalse(number.isArrayType());
    assertFalse(number.isBooleanObjectType());
    assertFalse(number.isBooleanValueType());
    assertFalse(number.isStringObjectType());
    assertFalse(number.isStringValueType());
    assertFalse(number.isNumberObjectType());
    assertFalse(number.isDateType());
    assertFalse(number.isRegexpType());
    assertFalse(number.isNullType());
    assertFalse(number.isVoidType());
    assertFalse(number.isAllType());
    assertFalse(number.isUnknownType());
    assertFalse(number.isCheckedUnknownType());
  }

  @Test(timeout = 4000)
  public void testTypePairPublicFields() {
    JSType number = type(JSTypeNative.NUMBER_TYPE);
    JSType string = type(JSTypeNative.STRING_TYPE);
    JSType.TypePair pair = new JSType.TypePair(number, string);
    assertSame(number, pair.typeA);
    assertSame(string, pair.typeB);
  }
}