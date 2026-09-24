package com.google.javascript.jscomp.type;

import static com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;
import static org.junit.Assert.*;

import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.DefaultCodingConvention;
import com.google.javascript.jscomp.type.ChainableReverseAbstractInterpreter;
import com.google.javascript.jscomp.type.ClosureReverseAbstractInterpreter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.testing.Asserts;
import com.google.javascript.rhino.testing.BaseJSTypeTestCase;
import org.junit.Test;
import org.junit.Before;

/**
 * Tests for {@link ClosureReverseAbstractInterpreter}, specifically targeting
 * the known defect in the isArray restricter (case where goog.isArray returns
 * false on a union type containing array leads to incorrect null return).
 *
 * <p>Structured into partitions covering:
 * <ul>
 *   <li>A: Core functional logic (each restricter function, both outcomes)
 *   <li>B: Boundary values (null type, unknown type, primitive types)
 *   <li>C: Defect-targeted zone (isArray false on union types, isObject edge cases)
 *   <li>D: Exception paths (invalid arguments, null parameters)
 *   <li>E: Contract integrity (no state leakage, consistent results)
 * </ul>
 *
 * <p>The known defect (Defects4J ID: testGoogIsArray2) manifests when
 * goog.isArray returns false on a union type {@code Array|Object}. The
 * expected result is that the non-array part (Object) is retained, but the
 * bug returns null or the full union.
 */
public class ClosureReverseAbstractInterpreterDeepseekTest {

  private CodingConvention convention;
  private JSTypeRegistry registry;
  private ClosureReverseAbstractInterpreter interpreter;

  @Before
  public void setUp() {
    convention = new DefaultCodingConvention();
    registry = new JSTypeRegistry(convention);
    interpreter = new ClosureReverseAbstractInterpreter(convention, registry);
  }

  // =========================================================================
  // Partition A: Core Functional Logic – Each restricter, both outcomes
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsDefTrue() {
    JSType type = registry.createUnionType(NUMBER_TYPE, VOID_TYPE);
    TypeRestriction tr = new TypeRestriction(type, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isDef");
    JSType result = restricter.apply(tr);
    assertNotNull("Expected non-null result for isDef true", result);
    assertEquals("Should be number (without void)", registry.getNativeType(NUMBER_TYPE), result);
  }

  @Test(timeout = 4000)
  public void testIsDefFalse() {
    JSType type = registry.createUnionType(NUMBER_TYPE, VOID_TYPE);
    TypeRestriction tr = new TypeRestriction(type, false);
    Function<TypeRestriction, JSType> restricter = getRestricter("isDef");
    JSType result = restricter.apply(tr);
    assertNotNull("Should be void subtype", result);
    assertTrue("Should be subtype of void", registry.getNativeType(VOID_TYPE).isSubtype(result));
  }

  @Test(timeout = 4000)
  public void testIsNullTrue() {
    JSType type = registry.createUnionType(STRING_TYPE, NULL_TYPE);
    TypeRestriction tr = new TypeRestriction(type, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isNull");
    JSType result = restricter.apply(tr);
    assertNotNull("Expected non-null for isNull true", result);
    assertTrue("Result should be only null", result.equals(registry.getNativeType(NULL_TYPE)));
  }

  @Test(timeout = 4000)
  public void testIsNullFalse() {
    JSType type = registry.createUnionType(STRING_TYPE, NULL_TYPE);
    TypeRestriction tr = new TypeRestriction(type, false);
    Function<TypeRestriction, JSType> restricter = getRestricter("isNull");
    JSType result = restricter.apply(tr);
    assertNotNull("Should return type without null", result);
    assertEquals("Should be string only", registry.getNativeType(STRING_TYPE), result);
  }

  @Test(timeout = 4000)
  public void testIsDefAndNotNullTrue() {
    JSType type = registry.createUnionType(NUMBER_TYPE, NULL_TYPE, VOID_TYPE);
    TypeRestriction tr = new TypeRestriction(type, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isDefAndNotNull");
    JSType result = restricter.apply(tr);
    assertNotNull("Expected non-null", result);
    assertTrue("Result should be number only", result.equals(registry.getNativeType(NUMBER_TYPE)));
  }

  @Test(timeout = 4000)
  public void testIsDefAndNotNullFalse() {
    JSType type = registry.createUnionType(NUMBER_TYPE, NULL_TYPE, VOID_TYPE);
    TypeRestriction tr = new TypeRestriction(type, false);
    Function<TypeRestriction, JSType> restricter = getRestricter("isDefAndNotNull");
    JSType result = restricter.apply(tr);
    assertNotNull("Should be null_void subtype", result);
    assertTrue("Result should be subtype of null_void", 
        result.isSubtype(registry.getNativeType(NULL_VOID)));
  }

  @Test(timeout = 4000)
  public void testIsStringTrue() {
    JSType type = registry.createUnionType(STRING_TYPE, NUMBER_TYPE);
    TypeRestriction tr = new TypeRestriction(type, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isString");
    JSType result = restricter.apply(tr);
    assertNotNull("Expected non-null", result);
    assertEquals("Should be string", registry.getNativeType(STRING_TYPE), result);
  }

  @Test(timeout = 4000)
  public void testIsStringFalse() {
    JSType type = registry.createUnionType(STRING_TYPE, NUMBER_TYPE);
    TypeRestriction tr = new TypeRestriction(type, false);
    Function<TypeRestriction, JSType> restricter = getRestricter("isString");
    JSType result = restricter.apply(tr);
    assertNotNull("Expected non-null", result);
    assertEquals("Should be number", registry.getNativeType(NUMBER_TYPE), result);
  }

  @Test(timeout = 4000)
  public void testIsBooleanTrue() {
    JSType type = registry.createUnionType(BOOLEAN_TYPE, NUMBER_TYPE);
    TypeRestriction tr = new TypeRestriction(type, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isBoolean");
    JSType result = restricter.apply(tr);
    assertNotNull("Expected non-null", result);
    assertEquals("Should be boolean", registry.getNativeType(BOOLEAN_TYPE), result);
  }

  @Test(timeout = 4000)
  public void testIsBooleanFalse() {
    JSType type = registry.createUnionType(BOOLEAN_TYPE, NUMBER_TYPE);
    TypeRestriction tr = new TypeRestriction(type, false);
    Function<TypeRestriction, JSType> restricter = getRestricter("isBoolean");
    JSType result = restricter.apply(tr);
    assertNotNull("Expected non-null", result);
    assertEquals("Should be number", registry.getNativeType(NUMBER_TYPE), result);
  }

  @Test(timeout = 4000)
  public void testIsNumberTrue() {
    JSType type = registry.createUnionType(NUMBER_TYPE, STRING_TYPE);
    TypeRestriction tr = new TypeRestriction(type, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isNumber");
    JSType result = restricter.apply(tr);
    assertNotNull("Expected non-null", result);
    assertEquals("Should be number", registry.getNativeType(NUMBER_TYPE), result);
  }

  @Test(timeout = 4000)
  public void testIsNumberFalse() {
    JSType type = registry.createUnionType(NUMBER_TYPE, STRING_TYPE);
    TypeRestriction tr = new TypeRestriction(type, false);
    Function<TypeRestriction, JSType> restricter = getRestricter("isNumber");
    JSType result = restricter.apply(tr);
    assertNotNull("Expected non-null", result);
    assertEquals("Should be string", registry.getNativeType(STRING_TYPE), result);
  }

  @Test(timeout = 4000)
  public void testIsFunctionTrue() {
    // Create a function type using the registry.
    JSType functionType = registry.createFunctionType(registry.getNativeType(NUMBER_TYPE));
    JSType type = registry.createUnionType(functionType, STRING_TYPE);
    TypeRestriction tr = new TypeRestriction(type, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isFunction");
    JSType result = restricter.apply(tr);
    assertNotNull("Expected non-null", result);
    assertTrue("Result should be function", result.isFunctionType());
  }

  @Test(timeout = 4000)
  public void testIsFunctionFalse() {
    JSType functionType = registry.createFunctionType(registry.getNativeType(NUMBER_TYPE));
    JSType type = registry.createUnionType(functionType, STRING_TYPE);
    TypeRestriction tr = new TypeRestriction(type, false);
    Function<TypeRestriction, JSType> restricter = getRestricter("isFunction");
    JSType result = restricter.apply(tr);
    assertNotNull("Expected non-null", result);
    assertFalse("Result should not be function", result.isFunctionType());
    assertEquals("Should be string", registry.getNativeType(STRING_TYPE), result);
  }

  @Test(timeout = 4000)
  public void testIsArrayTrueObjectType() {
    // Object type that is indeed an array.
    JSType arrayType = registry.getNativeType(ARRAY_TYPE);
    JSType type = arrayType;
    TypeRestriction tr = new TypeRestriction(type, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isArray");
    JSType result = restricter.apply(tr);
    assertNotNull("Expected non-null for array type", result);
    assertTrue("Result should be array", result.isSubtype(registry.getNativeType(ARRAY_TYPE)));
  }

  @Test(timeout = 4000)
  public void testIsArrayTrueNonArrayObject() {
    // Object type that is NOT an array.
    ObjectType objType = registry.getNativeType(OBJECT_TYPE).toObjectType();
    assertNotNull("ObjectType must not be null", objType);
    TypeRestriction tr = new TypeRestriction(objType, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isArray");
    JSType result = restricter.apply(tr);
    // For a regular object, 'isArray true' should return null (impossible).
    assertNull("Should be null for non-array object", result);
  }

  @Test(timeout = 4000)
  public void testIsArrayFalseOnArrayType() {
    JSType arrayType = registry.getNativeType(ARRAY_TYPE);
    TypeRestriction tr = new TypeRestriction(arrayType, false);
    Function<TypeRestriction, JSType> restricter = getRestricter("isArray");
    JSType result = restricter.apply(tr);
    // For a pure array, 'isArray false' should return null (contradiction).
    assertNull("Should be null (contradiction)", result);
  }

  @Test(timeout = 4000)
  public void testIsArrayFalseOnNonArrayObject() {
    ObjectType objType = registry.getNativeType(OBJECT_TYPE).toObjectType();
    assertNotNull("ObjectType must not be null", objType);
    TypeRestriction tr = new TypeRestriction(objType, false);
    Function<TypeRestriction, JSType> restricter = getRestricter("isArray");
    JSType result = restricter.apply(tr);
    // For a non-array object, should return the object type unchanged.
    assertNotNull("Should not be null", result);
    assertTrue("Result should be object type", result.isSubtype(registry.getNativeType(OBJECT_TYPE)));
  }

  @Test(timeout = 4000)
  public void testIsObjectTrueOnObject() {
    JSType objectType = registry.getNativeType(OBJECT_TYPE);
    TypeRestriction tr = new TypeRestriction(objectType, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isObject");
    JSType result = restricter.apply(tr);
    assertNotNull("Expected non-null", result);
    assertTrue("Result should be object", result.isSubtype(registry.getNativeType(OBJECT_TYPE)));
  }

  @Test(timeout = 4000)
  public void testIsObjectFalseOnObject() {
    JSType objectType = registry.getNativeType(OBJECT_TYPE);
    TypeRestriction tr = new TypeRestriction(objectType, false);
    Function<TypeRestriction, JSType> restricter = getRestricter("isObject");
    JSType result = restricter.apply(tr);
    assertNull("Should be null for object when isObject false", result);
  }

  // =========================================================================
  // Partition B: Boundary Values (null type, unknown type, primitive types)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsDefWithNullType() {
    TypeRestriction tr = new TypeRestriction(null, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isDef");
    JSType result = restricter.apply(tr);
    // When type is null, getRestrictedWithoutUndefined handles it.
    assertNull("Null type with outcome true should return null", result);
  }

  @Test(timeout = 4000)
  public void testIsNullWithNullType() {
    TypeRestriction tr = new TypeRestriction(null, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isNull");
    JSType result = restricter.apply(tr);
    assertNull("Null type with outcome true should return null", result);
  }

  @Test(timeout = 4000)
  public void testIsArrayWithNullTypeTrue() {
    TypeRestriction tr = new TypeRestriction(null, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isArray");
    JSType result = restricter.apply(tr);
    // When p.type == null and outcome true, returns ARRAY_TYPE.
    assertNotNull("Should return array type", result);
    assertTrue("Should be array", result.isSubtype(registry.getNativeType(ARRAY_TYPE)));
  }

  @Test(timeout = 4000)
  public void testIsArrayWithNullTypeFalse() {
    TypeRestriction tr = new TypeRestriction(null, false);
    Function<TypeRestriction, JSType> restricter = getRestricter("isArray");
    JSType result = restricter.apply(tr);
    assertNull("Null type with false outcome should return null", result);
  }

  @Test(timeout = 4000)
  public void testIsObjectWithNullTypeTrue() {
    TypeRestriction tr = new TypeRestriction(null, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isObject");
    JSType result = restricter.apply(tr);
    assertNotNull("Should return object type", result);
    assertTrue("Result should be object", result.equals(registry.getNativeType(OBJECT_TYPE)));
  }

  @Test(timeout = 4000)
  public void testIsObjectWithNullTypeFalse() {
    TypeRestriction tr = new TypeRestriction(null, false);
    Function<TypeRestriction, JSType> restricter = getRestricter("isObject");
    JSType result = restricter.apply(tr);
    assertNull("Null type with false outcome should return null", result);
  }

  @Test(timeout = 4000)
  public void testIsStringOnUnknownType() {
    JSType unknownType = registry.getNativeType(UNKNOWN_TYPE);
    TypeRestriction tr = new TypeRestriction(unknownType, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isString");
    JSType result = restricter.apply(tr);
    // Unknown type should remain unknown or narrow appropriately.
    assertNotNull("Should not be null", result);
    // Typically remains unknown.
    assertTrue("Result should be unknown", result.isUnknownType());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Zone (The known Defects4J bug)
  // =========================================================================

  /**
   * Directly targets the known defect from testGoogIsArray2.
   * The bug: when goog.isArray returns false on a union type that includes
   * Array (e.g., {@code Array|Object}), the incorrect implementation
   * returns null instead of correctly restricting to the non-array part.
   */
  @Test(timeout = 4000)
  public void testIsArrayFalseOnUnionTypeContainingArray() {
    // Create union type: Array | Object
    JSType arrayType = registry.getNativeType(ARRAY_TYPE);
    JSType objectType = registry.getNativeType(OBJECT_TYPE);
    JSType unionType = registry.createUnionType(arrayType, objectType);

    TypeRestriction tr = new TypeRestriction(unionType, false);
    Function<TypeRestriction, JSType> restricter = getRestricter("isArray");
    JSType result = restricter.apply(tr);

    // EXPECTED: The result should be Object (non-array part) – NOT null, NOT the full union.
    // The defect returns null or full union incorrectly.
    assertNotNull("Defect: should not be null when excluding array from union", result);
    assertTrue("Result should be subtype of object", result.isSubtype(objectType));
    assertFalse("Result should NOT be an array type", result.isSubtype(arrayType));
    // The result should be exactly the object type.
    assertEquals("Should be exactly object type", objectType, result);
  }

  /**
   * Another variant: Union of Array | String, outcome false -> should yield String.
   */
  @Test(timeout = 4000)
  public void testIsArrayFalseOnUnionArrayString() {
    JSType arrayType = registry.getNativeType(ARRAY_TYPE);
    JSType stringType = registry.getNativeType(STRING_TYPE);
    JSType unionType = registry.createUnionType(arrayType, stringType);

    TypeRestriction tr = new TypeRestriction(unionType, false);
    Function<TypeRestriction, JSType> restricter = getRestricter("isArray");
    JSType result = restricter.apply(tr);

    assertNotNull("Should not be null for union without object", result);
    assertEquals("Should be string", stringType, result);
    assertFalse("Should not contain array", result.isSubtype(arrayType));
  }

  /**
   * Test that isArray true on a union containing array returns the array part.
   */
  @Test(timeout = 4000)
  public void testIsArrayTrueOnUnionContainingArray() {
    JSType arrayType = registry.getNativeType(ARRAY_TYPE);
    JSType stringType = registry.getNativeType(STRING_TYPE);
    JSType unionType = registry.createUnionType(arrayType, stringType);

    TypeRestriction tr = new TypeRestriction(unionType, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isArray");
    JSType result = restricter.apply(tr);

    assertNotNull("Should not be null", result);
    assertTrue("Result should be array", result.isSubtype(arrayType));
    assertFalse("Result should not be string", result.isSubtype(stringType));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testRestricterWithNullKey() {
    // Accessing a null key should throw NPE from ImmutableMap.
    Map<String, Function<TypeRestriction, JSType>> map = getRestrictersMap();
    map.get(null).apply(new TypeRestriction(registry.getNativeType(NUMBER_TYPE), true));
  }

  @Test(timeout = 4000)
  public void testNullTypeRestriction() {
    // Direct creation of TypeRestriction with null type should not crash.
    TypeRestriction tr = new TypeRestriction(null, true);
    assertNotNull("TypeRestriction should be created", tr);
    assertEquals("outcome should be true", true, tr.outcome);
    assertNull("type should be null", tr.type);
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testApplyWithNullFunction() {
    // Using null as function => NPE.
    interpreter.restrictParameter(null, null, null, null, true);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testEqualityOfRestricters() {
    // Ensure that the same restricter is returned each time for a given key.
    Map<String, Function<TypeRestriction, JSType>> map = getRestrictersMap();
    Function<TypeRestriction, JSType> f1 = map.get("isDef");
    Function<TypeRestriction, JSType> f2 = map.get("isDef");
    assertSame("Should be same instance", f1, f2);
  }

  @Test(timeout = 4000)
  public void testRestricterConsistency() {
    // Applying the same parameters twice should yield same result.
    JSType type = registry.createUnionType(NUMBER_TYPE, STRING_TYPE);
    TypeRestriction tr = new TypeRestriction(type, true);
    Function<TypeRestriction, JSType> restricter = getRestricter("isNumber");
    JSType result1 = restricter.apply(tr);
    JSType result2 = restricter.apply(tr);
    assertEquals("Results should be equal", result1, result2);
  }

  // =========================================================================
  // Helper methods (using reflection or internal accessors)
  // =========================================================================

  @SuppressWarnings("unchecked")
  private Map<String, Function<TypeRestriction, JSType>> getRestrictersMap() {
    try {
      java.lang.reflect.Field field = ClosureReverseAbstractInterpreter.class
          .getDeclaredField("restricters");
      field.setAccessible(true);
      return (Map<String, Function<TypeRestriction, JSType>>) field.get(interpreter);
    } catch (Exception e) {
      throw new RuntimeException("Cannot access restricters field", e);
    }
  }

  private Function<TypeRestriction, JSType> getRestricter(String name) {
    return getRestrictersMap().get(name);
  }

  // Inner class to replicate TypeRestriction for testing (since it's private).
  // We use a similar structure via a test-local class or access via reflection.
  // For simplicity, we create a trivial TestTypeRestriction with same fields.
  private static class TypeRestriction {
    private final JSType type;
    private final boolean outcome;

    private TypeRestriction(JSType type, boolean outcome) {
      this.type = type;
      this.outcome = outcome;
    }
  }
}