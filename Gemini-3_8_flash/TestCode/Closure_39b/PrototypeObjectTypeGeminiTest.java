package com.google.javascript.rhino.jstype;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.rhino.jstype.PrototypeObjectType
 *
 * Branch & Condition Analysis:
 * 1. Constructor:
 *    - nativeType = true/false; implicitPrototype == null (falls back to OBJECT_TYPE) vs != null.
 * 2. getSlot(String):
 *    - Direct property hit in this.properties.
 *    - Implicit prototype lookup chain hit/miss.
 *    - Constructor extended interfaces lookup hit/miss.
 *    - Not found returns null.
 * 3. getPropertiesCount():
 *    - implicitPrototype == null vs != null.
 *    - Property shadowing (local property overrides prototype property vs new property).
 * 4. hasProperty / hasOwnProperty / getOwnPropertyNames:
 *    - isUnknownType() true vs slot != null vs slot == null.
 *    - Local properties map containment.
 * 5. isPropertyTypeDeclared / isPropertyTypeInferred / getPropertyType:
 *    - Slot null check (falls back to UNKNOWN_TYPE for getPropertyType).
 *    - Slot not inferred vs inferred.
 * 6. isPropertyInExterns:
 *    - Found in local properties: p.isFromExterns() true/false.
 *    - Not in local: searches implicitPrototype chain.
 *    - Fallback false when prototype chain exhausted.
 * 7. defineProperty / removeProperty:
 *    - hasOwnDeclaredProperty check (returns false if already declared).
 *    - Re-definition preserving old JSDocInfo.
 *    - removeProperty true if existed, false if absent.
 * 8. Node & JSDoc methods (getPropertyNode, getOwnPropertyJSDocInfo, setPropertyJSDocInfo):
 *    - Local vs implicit prototype resolution.
 *    - Setting JSDoc on unassigned property triggering defineInferredProperty.
 *    - Null docInfo handling.
 * 9. Context Matching (matchesNumberContext, matchesStringContext):
 *    - isNumberObjectType, isDateType, isBooleanObjectType, isStringObjectType, isTheObjectType, isRegexpType, isArrayType.
 *    - hasOverridenNativeProperty for "valueOf" and "toString" (nativeType bypass vs function/object prototype override).
 * 10. unboxesTo():
 *    - String, Boolean, Number object types vs default super.unboxesTo().
 * 11. toStringHelper & prettyPrint:
 *    - hasReferenceName() returns className or ownerFunction + ".prototype".
 *    - Anonymous with prettyPrint=false -> returns "{...}".
 *    - Anonymous with prettyPrint=true -> iterates prototype chain (non-native), formats properties.
 *    - DEFECT ZONE: Truncation at MAX_PRETTY_PRINTED_PROPERTIES (4) producing ", ..." instead of all properties,
 *      and cyclic/recursive record serialization producing "{...}" instead of "?".
 * 12. isSubtype(JSType):
 *    - JSType.isSubtypeHelper fast path.
 *    - Union type handling (returns false after decomposition).
 *    - Record type handling (delegates to RecordType.isSubtype).
 *    - Interface checking (ctor is interface vs implemented interfaces vs extended interfaces).
 *    - Unknown prototype chain checks (implicitPrototypeChainIsUnknown).
 *    - Prototype inheritance hierarchy check (isImplicitPrototype).
 * 13. Lifecycle, OwnerFunction & Resolve:
 *    - setOwnerFunction precondition (cannot reassign if already set).
 *    - resolveInternal updates implicitPrototypeFallback and resolves property types.
 */
public class PrototypeObjectTypeGeminiTest {

  private JSTypeRegistry registry;
  private ObjectType objectPrototype;
  private JSType numberType;
  private JSType stringType;
  private JSType booleanType;

  @Before
  public void setUp() {
    ErrorReporter reporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(reporter);
    objectPrototype = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    booleanType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstructorAndImplicitPrototypeDefault() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "CustomClass", null, false);
    assertEquals("CustomClass", type.getClassName());
    assertEquals("CustomClass", type.getReferenceName());
    assertTrue(type.hasReferenceName());
    assertFalse(type.isNativeObjectType());
    assertEquals(objectPrototype, type.getImplicitPrototype());
    assertTrue(type.matchesObjectContext());
    assertFalse(type.canBeCalled());
  }

  @Test(timeout = 4000)
  public void testConstructorWithExplicitPrototypeAndNativeFlag() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, "Parent", null, false);
    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", proto, true);
    assertTrue(child.isNativeObjectType());
    assertEquals(proto, child.getImplicitPrototype());
  }

  @Test(timeout = 4000)
  public void testPropertyDefinitionAndLookup() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null, false);
    Node node = Node.newString("propA");
    assertTrue(obj.defineProperty("propA", numberType, false, node));

    assertTrue(obj.hasOwnProperty("propA"));
    assertTrue(obj.hasProperty("propA"));
    assertEquals(numberType, obj.getPropertyType("propA"));
    assertTrue(obj.isPropertyTypeDeclared("propA"));
    assertFalse(obj.isPropertyTypeInferred("propA"));
    assertEquals(node, obj.getPropertyNode("propA"));

    // Redefining a declared property should fail
    assertFalse(obj.defineProperty("propA", stringType, false, null));
    assertEquals(numberType, obj.getPropertyType("propA"));
  }

  @Test(timeout = 4000)
  public void testPropertyInheritanceThroughPrototypeChain() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null, false);
    parent.defineProperty("parentProp", stringType, true, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent, false);
    child.defineProperty("childProp", numberType, true, null);

    assertTrue(child.hasProperty("parentProp"));
    assertFalse(child.hasOwnProperty("parentProp"));
    assertTrue(child.hasProperty("childProp"));
    assertTrue(child.hasOwnProperty("childProp"));
    assertEquals(stringType, child.getPropertyType("parentProp"));

    Set<String> allProps = new HashSet<String>();
    child.collectPropertyNames(allProps);
    assertTrue(allProps.contains("parentProp"));
    assertTrue(allProps.contains("childProp"));
  }

  @Test(timeout = 4000)
  public void testPropertyShadowingAndCount() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, null, null, false);
    parent.defineProperty("shared", stringType, true, null);
    parent.defineProperty("parentOnly", stringType, true, null);

    PrototypeObjectType child = new PrototypeObjectType(registry, null, parent, false);
    child.defineProperty("shared", numberType, true, null);
    child.defineProperty("childOnly", numberType, true, null);

    assertEquals(parent.getPropertiesCount() + 1, child.getPropertiesCount());
    assertEquals(numberType, child.getPropertyType("shared"));
  }

  @Test(timeout = 4000)
  public void testPropertyRemoval() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null, false);
    obj.defineProperty("temp", numberType, true, null);
    assertTrue(obj.hasProperty("temp"));
    assertTrue(obj.removeProperty("temp"));
    assertFalse(obj.hasProperty("temp"));
    assertFalse(obj.removeProperty("temp"));
  }

  @Test(timeout = 4000)
  public void testOwnerFunctionAndReferenceName() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, null, null, false);
    assertNull(proto.getReferenceName());
    assertFalse(proto.hasReferenceName());

    FunctionType ctor = registry.createConstructorType("MyFunction", null, null, null);
    proto.setOwnerFunction(ctor);
    assertEquals(ctor, proto.getOwnerFunction());
    assertEquals("MyFunction.prototype", proto.getReferenceName());
    assertTrue(proto.hasReferenceName());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Edge Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testPropertyQueriesOnNonExistentProperty() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null, false);
    assertNull(obj.getSlot("nonExistent"));
    assertFalse(obj.hasOwnProperty("nonExistent"));
    assertFalse(obj.isPropertyTypeDeclared("nonExistent"));
    assertFalse(obj.isPropertyTypeInferred("nonExistent"));
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), obj.getPropertyType("nonExistent"));
    assertNull(obj.getPropertyNode("nonExistent"));
    assertNull(obj.getOwnPropertyJSDocInfo("nonExistent"));
    assertFalse(obj.isPropertyInExterns("nonExistent"));
  }

  @Test(timeout = 4000)
  public void testSetPropertyJSDocInfoOnExistingAndNewProperties() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null, false);
    JSDocInfo info = new JSDocInfo();

    // Null docInfo should do nothing
    obj.setPropertyJSDocInfo("unknown", null);
    assertNull(obj.getOwnPropertyJSDocInfo("unknown"));

    // Setting docInfo on non-existent property defines it as inferred
    obj.setPropertyJSDocInfo("autoDefined", info);
    assertTrue(obj.hasOwnProperty("autoDefined"));
    assertTrue(obj.isPropertyTypeInferred("autoDefined"));
    assertEquals(info, obj.getOwnPropertyJSDocInfo("autoDefined"));

    // Setting docInfo on existing property
    JSDocInfo info2 = new JSDocInfo();
    obj.setPropertyJSDocInfo("autoDefined", info2);
    assertEquals(info2, obj.getOwnPropertyJSDocInfo("autoDefined"));
  }

  @Test(timeout = 4000)
  public void testPropertyInExternsInheritance() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null, false);
    Node externNode = Node.newString("externProp");
    externNode.putProp(Node.SOURCENAME_PROP, "externs.js");

    parent.defineProperty("externProp", numberType, true, externNode);
    // Explicitly set extern slot if available
    Property prop = parent.getSlot("externProp");
    assertNotNull(prop);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent, false);
    assertEquals(parent.isPropertyInExterns("externProp"), child.isPropertyInExterns("externProp"));
  }

  @Test(timeout = 4000)
  public void testUnboxesToSpecialTypes() {
    ObjectType strObj = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);
    assertEquals(stringType, strObj.unboxesTo());

    ObjectType boolObj = registry.getNativeObjectType(JSTypeNative.BOOLEAN_OBJECT_TYPE);
    assertEquals(booleanType, boolObj.unboxesTo());

    ObjectType numObj = registry.getNativeObjectType(JSTypeNative.NUMBER_OBJECT_TYPE);
    assertEquals(numberType, numObj.unboxesTo());

    PrototypeObjectType custom = new PrototypeObjectType(registry, "Custom", null, false);
    assertNull(custom.unboxesTo());
  }

  @Test(timeout = 4000)
  public void testContextMatchingSpecialTypes() {
    ObjectType dateType = registry.getNativeObjectType(JSTypeNative.DATE_TYPE);
    assertTrue(dateType.matchesNumberContext());
    assertTrue(dateType.matchesStringContext());

    ObjectType regexpType = registry.getNativeObjectType(JSTypeNative.REGEXP_TYPE);
    assertTrue(regexpType.matchesStringContext());
    assertTrue(regexpType.canBeCalled());

    ObjectType arrayType = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);
    assertTrue(arrayType.matchesStringContext());
  }

  @Test(timeout = 4000)
  public void testHasOverridenNativeProperty() {
    PrototypeObjectType custom = new PrototypeObjectType(registry, null, null, false);
    // Before override: valueOf and toString match native object prototype
    assertFalse(custom.matchesNumberContext());

    // Override toString with incompatible function
    custom.defineProperty("toString", numberType, true, null);
    assertTrue(custom.matchesStringContext());

    // Override valueOf
    custom.defineProperty("valueOf", stringType, true, null);
    assertTrue(custom.matchesNumberContext());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Known Regressions)
  // =========================================================================

  /**
   * Targets Defects4J known defect:
   * RecordTypeTest::testLongToString
   * ComparisonFailure: expected:<...number, a4: number, [a5: number, a6: number]}> but was:<...number, a4: number, [...]}>
   * When an anonymous object has more than MAX_PRETTY_PRINTED_PROPERTIES (4),
   * pretty printing must not prematurely truncate required properties when full serialization is expected.
   */
  @Test(timeout = 4000)
  public void testPrettyPrintTruncationBoundaryDefect() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null, false);
    obj.setPrettyPrint(true);
    assertTrue(obj.isPrettyPrint());

    obj.defineProperty("a1", numberType, true, null);
    obj.defineProperty("a2", numberType, true, null);
    obj.defineProperty("a3", numberType, true, null);
    obj.defineProperty("a4", numberType, true, null);
    obj.defineProperty("a5", numberType, true, null);
    obj.defineProperty("a6", numberType, true, null);

    String printed = obj.toStringHelper(false);
    // On the defective version, obj truncates at 4 properties with ", ...}"
    // The expected behavior in testLongToString expects all properties to be formatted.
    assertEquals("{a1: number, a2: number, a3: number, a4: number, a5: number, a6: number}", printed);
  }

  /**
   * Targets Defects4J known defect:
   * RecordTypeTest::testRecursiveRecord
   * ComparisonFailure: expected:<{loop: [?], number: number, st...> but was:<{loop: [{...}], number: number, st...>
   */
  @Test(timeout = 4000)
  public void testRecursiveRecordPrettyPrintDefect() {
    PrototypeObjectType rec = new PrototypeObjectType(registry, null, null, false);
    rec.setPrettyPrint(true);
    rec.defineProperty("loop", rec, true, null);
    rec.defineProperty("number", numberType, true, null);

    String printed = rec.toStringHelper(false);
    // The recursive self-reference should be formatted as "?" rather than "{...}"
    assertEquals("{loop: ?, number: number}", printed);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testSetOwnerFunctionTwiceThrowsException() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, null, null, false);
    FunctionType fn1 = registry.createConstructorType("Fn1", null, null, null);
    FunctionType fn2 = registry.createConstructorType("Fn2", null, null, null);

    proto.setOwnerFunction(fn1);
    try {
      proto.setOwnerFunction(fn2);
      fail("Expected IllegalStateException on resetting owner function");
    } catch (IllegalStateException expected) {
      // Success
    }
  }

  @Test(timeout = 4000)
  public void testSetOwnerFunctionToNullAllowed() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, null, null, false);
    FunctionType fn1 = registry.createConstructorType("Fn1", null, null, null);
    proto.setOwnerFunction(fn1);
    proto.setOwnerFunction(null);
    assertNull(proto.getOwnerFunction());
  }

  @Test(timeout = 4000)
  public void testSubtypeCheckWithUnionAndUnknown() {
    PrototypeObjectType obj1 = new PrototypeObjectType(registry, "TypeA", null, false);
    PrototypeObjectType obj2 = new PrototypeObjectType(registry, "TypeB", null, false);

    // Union type check should return false directly from PrototypeObjectType.isSubtype
    JSType union = registry.createUnionType(obj1, obj2);
    assertFalse(obj1.isSubtype(union));

    // Unknown type check
    JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    assertTrue(obj1.isSubtype(unknown));
  }

  @Test(timeout = 4000)
  public void testSubtypeInheritanceHierarchy() {
    PrototypeObjectType grandparent = new PrototypeObjectType(registry, "Grandparent", null, false);
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", grandparent, false);
    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent, false);

    assertTrue(child.isSubtype(parent));
    assertTrue(child.isSubtype(grandparent));
    assertFalse(parent.isSubtype(child));
  }

  // =========================================================================
  // Partition E: Lifecycle, Interface Implementation & Type Resolution
  // =========================================================================

  @Test(timeout = 4000)
  public void testCtorInterfacesOnFunctionPrototypeType() {
    FunctionType ctor = registry.createConstructorType("CustomIface", null, null, null);
    ObjectType proto = ctor.getPrototype();

    assertNotNull(proto.getCtorImplementedInterfaces());
    assertNotNull(proto.getCtorExtendedInterfaces());
    assertFalse(proto.getCtorImplementedInterfaces().iterator().hasNext());
    assertFalse(proto.getCtorExtendedInterfaces().iterator().hasNext());
  }

  @Test(timeout = 4000)
  public void testTypeResolution() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "UnresolvedParent", null, false);
    PrototypeObjectType child = new PrototypeObjectType(registry, "UnresolvedChild", parent, false);
    child.defineProperty("prop1", numberType, false, null);

    ErrorReporter reporter = new SimpleErrorReporter();
    JSType resolved = child.resolve(reporter, null);
    assertSame(child, resolved);
    assertTrue(child.isResolved());
    assertEquals(numberType, child.getPropertyType("prop1"));
  }

  @Test(timeout = 4000)
  public void testToStringAnonymousNotPretty() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null, false);
    anon.setPrettyPrint(false);
    assertEquals("{...}", anon.toStringHelper(false));
  }
}