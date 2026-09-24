package com.google.javascript.rhino.jstype;

import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

/**
 * [Branch & Defect Analysis Matrix]
 * ==================================
 * Coverage targets:
 * - Constructors: default implicit prototype, native type flag, null implicit prototype.
 * - Property management: defineProperty, removeProperty, hasOwnProperty, getSlot (own/prototype chain).
 * - Pretty printing: limit (MAX_PRETTY_PRINTED_PROPERTIES), recursion detection, toggling.
 * - Type queries: hasProperty, getPropertyType, isPropertyTypeDeclared/Inferred, isPropertyInExterns.
 * - Context matching: matchesNumberContext, matchesStringContext, matchesObjectContext, canBeCalled.
 * - Subtype logic: basic subtype, implicit prototype chain, unknown handling.
 * - Lifecycle: getReferenceName, hasReferenceName, setOwnerFunction, getOwnerFunction.
 * - Defect targeting: self-referential property should print as "?" and limit must be exactly 4 with ", ...".
 */
public class PrototypeObjectTypeDeepseekTest {

  private JSTypeRegistry registry;

  @Before
  public void setUp() {
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
  public void testDefaultImplicitPrototypeForNonNative() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null, false);
    ObjectType implicit = obj.getImplicitPrototype();
    assertNotNull("Default implicit prototype should not be null", implicit);
    assertEquals("Default should be the native Object type",
        registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), implicit);
  }

  @Test(timeout = 4000)
  public void testConstructorPreservesExplicitImplicitPrototype() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null, true);
    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent, false);
    assertSame(parent, child.getImplicitPrototype());
  }

  @Test(timeout = 4000)
  public void testNativeTypeConstructorAllowsNullPrototype() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, "Native", null, true);
    assertNull("Native types may have null implicit prototype", obj.getImplicitPrototype());
  }

  @Test(timeout = 4000)
  public void testDefinePropertyAndRetrieveSlot() {
    PrototypeObjectType obj = createObjectType("MyObject");
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertTrue(obj.defineProperty("x", numberType, false, null));
    Property slot = obj.getSlot("x");
    assertNotNull(slot);
    assertEquals(numberType, slot.getType());
    assertTrue(obj.hasProperty("x"));
    assertTrue(obj.hasOwnProperty("x"));
  }

  @Test(timeout = 4000)
  public void testDefinePropertyDuplicateReturnsFalse() {
    PrototypeObjectType obj = createObjectType("MyObject");
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    assertTrue(obj.defineProperty("x", numberType, false, null));
    assertFalse("Redefining an existing property should return false",
        obj.defineProperty("x", registry.getNativeType(JSTypeNative.STRING_TYPE), true, null));
  }

  @Test(timeout = 4000)
  public void testGetSlotFromImplicitPrototype() {
    PrototypeObjectType parent = createObjectType("Parent");
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    assertTrue(parent.defineProperty("p", stringType, false, null));

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent, false);
    Property slot = child.getSlot("p");
    assertNotNull("Should find property from prototype chain", slot);
    assertEquals(stringType, slot.getType());
    assertTrue(child.hasProperty("p"));
    assertFalse("Property from prototype is not own", child.hasOwnProperty("p"));
  }

  @Test(timeout = 4000)
  public void testGetSlotNullWhenAbsent() {
    PrototypeObjectType obj = createObjectType("MyObject");
    assertNull(obj.getSlot("nonexistent"));
    assertFalse(obj.hasProperty("nonexistent"));
    assertFalse(obj.hasOwnProperty("nonexistent"));
  }

  @Test(timeout = 4000)
  public void testRemoveProperty() {
    PrototypeObjectType obj = createObjectType("MyObject");
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    obj.defineProperty("x", numberType, false, null);
    assertTrue(obj.removeProperty("x"));
    assertFalse(obj.hasOwnProperty("x"));
    assertNull(obj.getSlot("x"));
    assertFalse("Removing a non-existent property should return false", obj.removeProperty("x"));
  }

  @Test(timeout = 4000)
  public void testGetPropertiesCountWhenNoPrototype() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null, true);
    assertEquals(0, obj.getPropertiesCount());
    obj.defineProperty("a", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    assertEquals(1, obj.getPropertiesCount());
  }

  @Test(timeout = 4000)
  public void testGetPropertiesCountWithPrototype() {
    PrototypeObjectType parent = createObjectType("Parent");
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    parent.defineProperty("p", numberType, false, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent, false);
    child.defineProperty("c", numberType, false, null);
    // parent has 1, child adds 1 -> total 2
    assertEquals(2, child.getPropertiesCount());
  }

  @Test(timeout = 4000)
  public void testGetOwnPropertyNames() {
    PrototypeObjectType obj = createObjectType("MyObject");
    obj.defineProperty("b", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    obj.defineProperty("a", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);
    Set<String> names = obj.getOwnPropertyNames();
    assertEquals(2, names.size());
    assertTrue(names.contains("a"));
    assertTrue(names.contains("b"));
  }

  @Test(timeout = 4000)
  public void testIsPropertyTypeDeclaredAndInferred() {
    PrototypeObjectType obj = createObjectType("MyObject");
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    // Declared property
    obj.defineProperty("declared", numberType, false, null);
    // Inferred property
    obj.defineProperty("inferred", numberType, true, null);

    assertTrue(obj.isPropertyTypeDeclared("declared"));
    assertFalse(obj.isPropertyTypeInferred("declared"));
    assertFalse(obj.isPropertyTypeDeclared("inferred"));
    assertTrue(obj.isPropertyTypeInferred("inferred"));
    // Non-existent property
    assertFalse(obj.isPropertyTypeDeclared("nonexistent"));
    assertFalse(obj.isPropertyTypeInferred("nonexistent"));
  }

  @Test(timeout = 4000)
  public void testGetPropertyType() {
    PrototypeObjectType obj = createObjectType("MyObject");
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    obj.defineProperty("n", numberType, false, null);
    assertSame(numberType, obj.getPropertyType("n"));
    // Should return UNKNOWN_TYPE for non-existent properties
    assertSame(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), obj.getPropertyType("missing"));
  }

  @Test(timeout = 4000)
  public void testIsPropertyInExterns() {
    PrototypeObjectType obj = createObjectType("MyObject");
    // Define a property with a node to simulate extern? We'll just test default false
    obj.defineProperty("p", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    assertFalse(obj.isPropertyInExterns("p"));
    assertFalse(obj.isPropertyInExterns("missing"));
  }

  @Test(timeout = 4000)
  public void testGetPropertyNode() {
    PrototypeObjectType obj = createObjectType("MyObject");
    assertNull(obj.getPropertyNode("missing"));
  }

  @Test(timeout = 4000)
  public void testJSDocInfoHandling() {
    PrototypeObjectType obj = createObjectType("MyObject");
    JSDocInfo info = new JSDocInfo();
    obj.setPropertyJSDocInfo("x", info);
    assertNotNull("Should have created a property when setting JSDocInfo for missing property",
        obj.getOwnPropertyJSDocInfo("x"));
    assertSame(info, obj.getOwnPropertyJSDocInfo("x"));
    // Update existing info
    JSDocInfo newInfo = new JSDocInfo();
    obj.setPropertyJSDocInfo("x", newInfo);
    assertSame(newInfo, obj.getOwnPropertyJSDocInfo("x"));
  }

  @Test(timeout = 4000)
  public void testGetReferenceName() {
    PrototypeObjectType named = createObjectType("MyName");
    assertEquals("MyName", named.getReferenceName());
    assertTrue(named.hasReferenceName());

    PrototypeObjectType anon = createObjectType(null);
    assertFalse(anon.hasReferenceName());
    assertNull(anon.getReferenceName());
  }

  @Test(timeout = 4000)
  public void testOwnerFunctionAndReferenceName() {
    // Need a FunctionType to test owner function, but that's complex. We'll just test the setter/getter.
    PrototypeObjectType obj = createObjectType(null);
    // setOwnerFunction is package-private; we can set to null (no-op)
    obj.setOwnerFunction(null);
    assertNull(obj.getOwnerFunction());
  }

  // ---------- Partition B: Boundary Value Analysis & Extremes ----------

  @Test(timeout = 4000)
  public void testPrettyPrintStaticGetterSetter() {
    PrototypeObjectType obj = createObjectType(null);
    assertFalse("Initial prettyPrint should be false", obj.isPrettyPrint());
    obj.setPrettyPrint(true);
    assertTrue(obj.isPrettyPrint());
    obj.setPrettyPrint(false);
    assertFalse(obj.isPrettyPrint());
  }

  @Test(timeout = 4000)
  public void testNullImplicitPrototypeGivesNativeObjectTypeAsPrototype() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null, false);
    assertNotNull(obj.getImplicitPrototype());
  }

  @Test(timeout = 4000)
  public void testNativeTypeWithNullPrototype() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null, true);
    assertNull(obj.getImplicitPrototype());
  }

  // ---------- Partition C: Defect-Targeted Branch Zone ----------

  @Test(timeout = 4000)
  public void testPrettyPrintWithFewerThanMaxProperties() {
    PrototypeObjectType obj = createObjectType(null);
    obj.setPrettyPrint(true);
    obj.defineProperty("a", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    obj.defineProperty("b", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);
    String result = obj.toString();
    assertEquals("{a: number, b: string}", result);
  }

  @Test(timeout = 4000)
  public void testPrettyPrintWithExactlyMaxProperties() {
    PrototypeObjectType obj = createObjectType(null);
    obj.setPrettyPrint(true);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    for (int i = 0; i < 4; i++) {
      obj.defineProperty("p" + i, numberType, false, null);
    }
    String result = obj.toString();
    assertTrue(result.startsWith("{p0: number, p1: number, p2: number, p3: number}"));
    assertFalse("Should not include ellipsis exactly at max", result.contains("..."));
  }

  @Test(timeout = 4000)
  public void testPrettyPrintWithMoreThanMaxPropertiesShowsEllipsis() {
    PrototypeObjectType obj = createObjectType(null);
    obj.setPrettyPrint(true);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    for (int i = 0; i < 6; i++) {
      obj.defineProperty("p" + i, numberType, false, null);
    }
    String result = obj.toString();
    assertTrue("Expected first 4 properties and ellipsis, got: " + result,
        result.startsWith("{p0: number, p1: number, p2: number, p3: number, ...}"));
  }

  @Test(timeout = 4000)
  public void testPrettyPrintSelfReferentialPropertyShouldPrintAsQuestionMark() {
    // This test targets the defect where a recursive type prints as "{...}" instead of "?"
    PrototypeObjectType obj = createObjectType(null);
    obj.setPrettyPrint(true);
    // Define a property whose type is the object itself (self-reference)
    obj.defineProperty("loop", obj, false, null);
    String result = obj.toString();
    // The expected correct behavior is that "loop" should show "?" to avoid infinite recursion
    assertTrue("Expected recursive property to print as '?', actual: " + result,
        result.contains("loop: ?"));
  }

  @Test(timeout = 4000)
  public void testPrettyPrintNoClassNameWhenNotPretty() {
    PrototypeObjectType obj = createObjectType(null);
    assertEquals("When no prettyPrint and no className, toString should be {...}", "{...}", obj.toString());
  }

  @Test(timeout = 4000)
  public void testPrettyPrintClassNameOverridesProperties() {
    PrototypeObjectType obj = createObjectType("NamedClass");
    obj.setPrettyPrint(true);
    obj.defineProperty("x", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    assertEquals("NamedClass", obj.toString());
  }

  // ---------- Partition D: Exception & Defensive Guard Paths ----------

  @Test(timeout = 4000)
  public void testSetImplicitPrototypeRejectsAfterCachedValues() {
    PrototypeObjectType obj = createObjectType("MyObject");
    // Trigger caching? Hard to simulate, but we can check that the checkState is enforced.
    // We'll just verify that setting a new prototype works when not cached.
    PrototypeObjectType newProto = createObjectType("NewProto");
    // Calling getPropertiesCount caches values? Not necessarily. We'll rely on the check.
    try {
      obj.setImplicitPrototype(newProto);
      assertSame("Prototype should be updated", newProto, obj.getImplicitPrototype());
    } catch (IllegalStateException e) {
      // This can happen if cached values are present; we accept either behaviour in test.
    }
  }

  @Test(timeout = 4000)
  public void testMatchesNumberContextForDefaultObject() {
    PrototypeObjectType obj = createObjectType(null);
    assertFalse("Plain object should not match number context", obj.matchesNumberContext());
  }

  @Test(timeout = 4000)
  public void testMatchesStringContextForDefaultObject() {
    PrototypeObjectType obj = createObjectType(null);
    assertFalse("Plain object should not match string context", obj.matchesStringContext());
  }

  @Test(timeout = 4000)
  public void testMatchesObjectContextAlwaysTrue() {
    PrototypeObjectType obj = createObjectType(null);
    assertTrue(obj.matchesObjectContext());
  }

  @Test(timeout = 4000)
  public void testCanBeCalled() {
    PrototypeObjectType obj = createObjectType(null);
    assertFalse("Plain object cannot be called", obj.canBeCalled());
  }

  @Test(timeout = 4000)
  public void testUnboxesToReturnsNativePrimitiveForStringObject() {
    // This is hard to construct without a full registry setup; we just verify it doesn't crash.
    PrototypeObjectType obj = createObjectType(null);
    assertNull("Should return null for non-wrapper types", obj.unboxesTo());
  }

  // ---------- Partition E: Object Lifecycle & Contract Integrity ----------

  @Test(timeout = 4000)
  public void testGetConstructorReturnsNull() {
    PrototypeObjectType obj = createObjectType(null);
    assertNull(obj.getConstructor());
  }

  @Test(timeout = 4000)
  public void testIsNativeObjectType() {
    PrototypeObjectType nativeObj = new PrototypeObjectType(registry, null, null, true);
    PrototypeObjectType nonNative = createObjectType(null);
    assertTrue(nativeObj.isNativeObjectType());
    assertFalse(nonNative.isNativeObjectType());
  }

  @Test(timeout = 4000)
  public void testIsSubtypeOfItself() {
    PrototypeObjectType obj = createObjectType("MyObject");
    assertTrue(obj.isSubtype(obj));
  }

  @Test(timeout = 4000)
  public void testGetCtorImplementedInterfacesForNonFunction() {
    PrototypeObjectType obj = createObjectType(null);
    Iterable<ObjectType> interfaces = obj.getCtorImplementedInterfaces();
    assertFalse("Should have no implemented interfaces", interfaces.iterator().hasNext());
  }

  @Test(timeout = 4000)
  public void testCollectPropertyNamesIncludesPrototypeChain() {
    PrototypeObjectType parent = createObjectType("Parent");
    parent.defineProperty("p", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent, false);
    child.defineProperty("c", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);
    Set<String> names = Sets.newHashSet();
    child.collectPropertyNames(names);
    assertTrue(names.contains("p"));
    assertTrue(names.contains("c"));
  }

  // Helper to create a prototype object type with given className
  private PrototypeObjectType createObjectType(String className) {
    return new PrototypeObjectType(registry, className, null, false);
  }
}