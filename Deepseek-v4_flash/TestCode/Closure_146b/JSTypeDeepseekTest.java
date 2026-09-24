package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import com.google.javascript.rhino.ErrorReporter;

public class JSTypeDeepseekTest {
  private JSTypeRegistry registry;

  @Before
  public void setUp() {
    ErrorReporter reporter = new ErrorReporter() {
      @Override public void error(String message, String sourceName, int line, int lineOffset) {}
      @Override public void warning(String message, String sourceName, int line, int lineOffset) {}
    };
    registry = new JSTypeRegistry(reporter);
  }

  private JSType type(JSTypeNative nativeType) {
    return registry.getNativeType(nativeType);
  }

  @Test
  public void testVoidTypeIsVoidType() {
    JSType voidType = type(JSTypeNative.VOID_TYPE);
    assertTrue(voidType.isVoidType());
    assertFalse(voidType.isNullType());
  }

  @Test
  public void testNullTypeIsNullType() {
    JSType nullType = type(JSTypeNative.NULL_TYPE);
    assertTrue(nullType.isNullType());
    assertFalse(nullType.isVoidType());
  }

  @Test
  public void testNumberTypeIsNumberType() {
    JSType numberType = type(JSTypeNative.NUMBER_TYPE);
    assertTrue(numberType.isNumberType());
    assertFalse(numberType.isStringType());
  }

  @Test
  public void testUnknownTypeIsUnknownType() {
    JSType unknownType = type(JSTypeNative.UNKNOWN_TYPE);
    assertTrue(unknownType.isUnknownType());
  }

  @Test
  public void testNoTypeIsNoType() {
    JSType noType = type(JSTypeNative.NO_TYPE);
    assertTrue(noType.isNoType());
  }

  @Test
  public void testObjectTypeIsObjectType() {
    JSType objectType = type(JSTypeNative.OBJECT_TYPE);
    assertTrue(objectType.isObjectType());
    assertFalse(objectType.isNumberType());
  }

  @Test
  public void testArrayTypeIsArrayType() {
    JSType arrayType = type(JSTypeNative.ARRAY_TYPE);
    assertTrue(arrayType.isArrayType());
  }

  @Test
  public void testFunctionTypeIsFunctionType() {
    JSType functionType = type(JSTypeNative.FUNCTION_TYPE);
    assertTrue(functionType.isFunctionType());
  }

  @Test
  public void testGetTypesUnderInequalityForVoidTypes() {
    JSType voidType = type(JSTypeNative.VOID_TYPE);
    JSType.TypePair pair = voidType.getTypesUnderInequality(voidType);
    assertNull("Inequality of void with itself should yield no types", pair.typeA);
    assertNull(pair.typeB);
  }

  @Test
  public void testGetTypesUnderInequalityForNullTypes() {
    JSType nullType = type(JSTypeNative.NULL_TYPE);
    JSType.TypePair pair = nullType.getTypesUnderInequality(nullType);
    assertNull(pair.typeA);
    assertNull(pair.typeB);
  }

  @Test
  public void testGetTypesUnderEqualityForVoidTypes() {
    JSType voidType = type(JSTypeNative.VOID_TYPE);
    JSType.TypePair pair = voidType.getTypesUnderEquality(voidType);
    assertSame(voidType, pair.typeA);
    assertSame(voidType, pair.typeB);
  }

  @Test
  public void testGetTypesUnderShallowEqualityForVoidTypes() {
    JSType voidType = type(JSTypeNative.VOID_TYPE);
    JSType.TypePair pair = voidType.getTypesUnderShallowEquality(voidType);
    assertSame(voidType, pair.typeA);
    assertSame(voidType, pair.typeB);
  }

  @Test
  public void testGetTypesUnderShallowInequalityForVoidTypes() {
    JSType voidType = type(JSTypeNative.VOID_TYPE);
    JSType.TypePair pair = voidType.getTypesUnderShallowInequality(voidType);
    assertNull(pair.typeA);
    assertNull(pair.typeB);
  }

  @Test
  public void testTestForEqualityNullVsVoid() {
    JSType nullType = type(JSTypeNative.NULL_TYPE);
    JSType voidType = type(JSTypeNative.VOID_TYPE);
    assertEquals(TernaryValue.TRUE, nullType.testForEquality(voidType));
  }

  @Test
  public void testTestForEqualityNumberVsString() {
    JSType numberType = type(JSTypeNative.NUMBER_TYPE);
    JSType stringType = type(JSTypeNative.STRING_TYPE);
    assertEquals(TernaryValue.UNKNOWN, numberType.testForEquality(stringType));
  }

  @Test
  public void testCanTestForEqualityWithNumberString() {
    JSType numberType = type(JSTypeNative.NUMBER_TYPE);
    JSType stringType = type(JSTypeNative.STRING_TYPE);
    assertTrue(numberType.canTestForEqualityWith(stringType));
  }

  @Test
  public void testCanTestForEqualityWithNullVoid() {
    JSType nullType = type(JSTypeNative.NULL_TYPE);
    JSType voidType = type(JSTypeNative.VOID_TYPE);
    assertFalse(nullType.canTestForEqualityWith(voidType));
  }

  @Test
  public void testGetLeastSupertypeNumberNumber() {
    JSType numberType = type(JSTypeNative.NUMBER_TYPE);
    assertSame(numberType, numberType.getLeastSupertype(numberType));
  }

  @Test
  public void testGetGreatestSubtypeNumberString() {
    JSType numberType = type(JSTypeNative.NUMBER_TYPE);
    JSType stringType = type(JSTypeNative.STRING_TYPE);
    JSType result = numberType.getGreatestSubtype(stringType);
    assertTrue(result.isNoType());
  }

  @Test
  public void testRestrictByNotNullOrUndefinedForVoid() {
    JSType voidType = type(JSTypeNative.VOID_TYPE);
    assertTrue(voidType.restrictByNotNullOrUndefined().isNoType());
  }

  @Test
  public void testGetRestrictedTypeGivenToBooleanOutcomeForVoid() {
    JSType voidType = type(JSTypeNative.VOID_TYPE);
    assertTrue(voidType.getRestrictedTypeGivenToBooleanOutcome(true).isNoType());
    assertSame(voidType, voidType.getRestrictedTypeGivenToBooleanOutcome(false));
  }

  @Test
  public void testAutoboxesAndUnboxesNumber() {
    JSType numberType = type(JSTypeNative.NUMBER_TYPE);
    JSType boxed = numberType.autoboxesTo();
    assertNotNull(boxed);
    assertTrue(boxed.isObjectType());
    assertSame(numberType, boxed.unboxesTo());
  }

  @Test
  public void testIsEquivalentTo() {
    JSType numberType = type(JSTypeNative.NUMBER_TYPE);
    JSType numberType2 = type(JSTypeNative.NUMBER_TYPE);
    JSType stringType = type(JSTypeNative.STRING_TYPE);
    assertTrue(numberType.isEquivalentTo(numberType2));
    assertFalse(numberType.isEquivalentTo(stringType));
  }

  @Test
  public void testEqualsAndHashCode() {
    JSType numberType = type(JSTypeNative.NUMBER_TYPE);
    JSType stringType = type(JSTypeNative.STRING_TYPE);
    assertEquals(numberType, numberType);
    assertFalse(numberType.equals(stringType));
    assertNotNull(numberType.hashCode());
  }

  @Test
  public void testTypePairConstructor() {
    JSType numberType = type(JSTypeNative.NUMBER_TYPE);
    JSType stringType = type(JSTypeNative.STRING_TYPE);
    JSType.TypePair pair = new JSType.TypePair(numberType, stringType);
    assertSame(numberType, pair.typeA);
    assertSame(stringType, pair.typeB);
  }

  @Test
  public void testIsSubtype() {
    JSType numberType = type(JSTypeNative.NUMBER_TYPE);
    JSType unknownType = type(JSTypeNative.UNKNOWN_TYPE);
    JSType noType = type(JSTypeNative.NO_TYPE);
    assertTrue(numberType.isSubtype(numberType));
    assertTrue(numberType.isSubtype(unknownType));
    assertTrue(noType.isSubtype(numberType));
    assertFalse(numberType.isSubtype(noType));
  }

  @Test
  public void testCanAssignTo() {
    JSType numberType = type(JSTypeNative.NUMBER_TYPE);
    JSType stringType = type(JSTypeNative.STRING_TYPE);
    JSType noType = type(JSTypeNative.NO_TYPE);
    assertTrue(numberType.canAssignTo(numberType));
    assertFalse(numberType.canAssignTo(stringType));
    assertTrue(noType.canAssignTo(numberType));
  }
}