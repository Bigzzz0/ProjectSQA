package com.google.javascript.rhino.jstype;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * TARGET CLASS: PrototypeObjectType
 *
 * DEFECT TARGETED:
 * - Issue 700 / TypeCheckTest::testIssue700:
 *   matchConstraint(ObjectType constraintObj) is intended ONLY for anonymous types.
 *   In the defective implementation, matchConstraint does not check hasReferenceName(),
 *   causing named PrototypeObjectTypes and Function prototype types to have inferred
 *   properties defined on them when evaluated against record type constraints.
 *
 * BRANCH COVERAGE TARGETS:
 * 1. Constructor initializations:
 *    - nativeType = true (implicitPrototype preserved even if null)
 *    - nativeType = false, implicitPrototype != null
 *    - nativeType = false, implicitPrototype == null (defaults to OBJECT_TYPE)
 * 2. getSlot(String):
 *    - property in local properties map
 *    - property in implicitPrototype chain
 *    - property in ctorExtendedInterfaces
 *    - property missing from all
 * 3. getPropertiesCount():
 *    - implicitPrototype == null
 *    - implicitPrototype != null, with and without shadowed properties
 * 4. hasProperty(String) / hasOwnProperty(String):
 *    - isUnknownType() true vs false
 *    - slot exists vs missing
 *    - local property vs prototype property
 * 5. collectPropertyNames(Set<String>):
 *    - implicitPrototype == null vs non-null recursion
 * 6. isPropertyTypeDeclared(String) / isPropertyTypeInferred(String):
 *    - slot null vs inferred vs declared
 * 7. getPropertyType(String):
 *    - slot null (returns UNKNOWN_TYPE) vs slot present
 * 8. isPropertyInExterns(String):
 *    - local property present (from externs vs not)
 *    - implicitPrototype != null delegation
 *    - property missing everywhere
 * 9. defineProperty(String, JSType, boolean, Node):
 *    - hasOwnDeclaredProperty == true (returns false)
 *    - oldProp != null (preserves existing JSDocInfo)
 *    - fresh property insertion
 * 10. removeProperty(String):
 *     - property present (true) vs absent (false)
 * 11. getPropertyNode(String):
 *     - local property present vs implicitPrototype delegation vs absent
 * 12. getOwnPropertyJSDocInfo(String) / setPropertyJSDocInfo(String, JSDocInfo):
 *     - info == null vs non-null
 *     - property absent (defines inferred property first) vs already present
 * 13. matchesNumberContext() & matchesStringContext():
 *     - primitive object types: Number, Date, Boolean, String, Object, RegExp, Array
 *     - hasOverridenNativeProperty("valueOf") / hasOverridenNativeProperty("toString")
 *     - isNativeObjectType() == true vs false
 * 14. unboxesTo():
 *     - StringObject, BooleanObject, NumberObject vs default (null)
 * 15. toStringHelper(boolean):
 *     - hasReferenceName() == true (className vs ownerFunction.prototype)
 *     - prettyPrint == true (<= MAX_PRETTY_PRINTED_PROPERTIES vs > 4 with/without annotations)
 *     - prettyPrint == false (forAnnotations true '?' vs false '{...}')
 * 16. isSubtype(JSType):
 *     - isSubtypeHelper true
 *     - that.isUnionType() (returns false)
 *     - that.isRecordType() (RecordType.isSubtype delegation)
 *     - that constructor isInterface (ctorImplementedInterfaces check)
 *     - implicitPrototypeChainIsUnknown() (returns true)
 *     - standard isImplicitPrototype check
 * 17. setOwnerFunction(FunctionType) / getOwnerFunction():
 *     - null to instance, instance to null, invalid reassignment (IllegalStateException)
 * 18. matchConstraint(ObjectType):
 *     - non-record type (noop)
 *     - anonymous type with unmapped property (unions with VOID_TYPE)
 *     - anonymous type with existing property (direct assignment)
 *     - anonymous type with declared property (skipped)
 *     - named type / function prototype (TARGET DEFECT: must NOT mutate)
 */
public class PrototypeObjectTypeGeminiTest {

  private JSTypeRegistry registry;
  private SimpleErrorReporter errorReporter;

  @Before
  public void setUp() {
    errorReporter = new SimpleErrorReporter();
    registry = new JSTypeRegistry(errorReporter);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 700 / TypeCheckTest)
  // =========================================================================

  /**
   * Targets Defects4J known issue (Issue 700 / TypeCheckTest::testIssue700):
   * matchConstraint is intended only for anonymous types.
   * When matchConstraint is invoked on a named PrototypeObjectType,
   * it must not define/infer properties on that named type.
   */
  @Test(timeout = 4000)
  public void testIssue700_matchConstraintOnNamedTypeDoesNotMutate() {
    PrototypeObjectType namedType = new PrototypeObjectType(registry, "NamedClass", null);
    assertTrue(namedType.hasReferenceName());

    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("propA", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    RecordType recordType = (RecordType) builder.build();

    namedType.matchConstraint(recordType);

    assertFalse("Issue 700: Non-anonymous types should not match constraints",
        namedType.hasProperty("propA"));
    assertFalse(namedType.hasOwnProperty("propA"));
  }

  /**
   * Targets Defects4J known issue (Issue 700):
   * Function prototype types also have a reference name (e.g., 'Foo.prototype')
   * and must not have constraints matched onto them.
   */
  @Test(timeout = 4000)
  public void testIssue700_matchConstraintOnFunctionPrototypeDoesNotMutate() {
    FunctionType ctor = registry.createConstructorType("Foo", null, null, null);
    ObjectType fnProto = ctor.getPrototype();
    assertTrue(fnProto.hasReferenceName());

    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("propB", registry.getNativeType(JSTypeNative.STRING_TYPE), null);
    RecordType recordType = (RecordType) builder.build();

    fnProto.matchConstraint(recordType);

    assertFalse("Issue 700: Function prototype types must not match constraints",
        fnProto.hasProperty("propB"));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstructorsAndImplicitPrototypeFallbacks() {
    // 1. Non-native with null implicitPrototype defaults to OBJECT_TYPE
    PrototypeObjectType obj1 = new PrototypeObjectType(registry, "Obj1", null);
    assertEquals(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), obj1.getImplicitPrototype());
    assertFalse(obj1.isNativeObjectType());
    assertEquals("Obj1", obj1.getReferenceName());

    // 2. Non-native with explicit implicitPrototype
    ObjectType customProto = new PrototypeObjectType(registry, "CustomProto", null);
    PrototypeObjectType obj2 = new PrototypeObjectType(registry, null, customProto, false);
    assertEquals(customProto, obj2.getImplicitPrototype());
    assertFalse(obj2.isNativeObjectType());
    assertNull(obj2.getReferenceName());

    // 3. Native type with null implicitPrototype stays null
    PrototypeObjectType nativeObj = new PrototypeObjectType(registry, "NativeObj", null, true);
    assertNull(nativeObj.getImplicitPrototype());
    assertTrue(nativeObj.isNativeObjectType());
  }

  @Test(timeout = 4000)
  public void testPropertyDefinitionAndInspection() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null);

    // Inferred property definition
    assertTrue(obj.defineInferredProperty("inferredProp", registry.getNativeType(JSTypeNative.STRING_TYPE), null));
    assertTrue(obj.hasProperty("inferredProp"));
    assertTrue(obj.hasOwnProperty("inferredProp"));
    assertTrue(obj.isPropertyTypeInferred("inferredProp"));
    assertFalse(obj.isPropertyTypeDeclared("inferredProp"));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), obj.getPropertyType("inferredProp"));

    // Declared property definition
    Node propNode = Node.newString("declaredProp");
    assertTrue(obj.defineProperty("declaredProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, propNode));
    assertTrue(obj.hasProperty("declaredProp"));
    assertTrue(obj.isPropertyTypeDeclared("declaredProp"));
    assertFalse(obj.isPropertyTypeInferred("declaredProp"));
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), obj.getPropertyType("declaredProp"));
    assertEquals(propNode, obj.getPropertyNode("declaredProp"));

    // Declaring an already declared property returns false
    assertFalse(obj.defineProperty("declaredProp", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), false, null));

    // Non-existent property
    assertFalse(obj.hasProperty("nonExistent"));
    assertFalse(obj.hasOwnProperty("nonExistent"));
    assertFalse(obj.isPropertyTypeDeclared("nonExistent"));
    assertFalse(obj.isPropertyTypeInferred("nonExistent"));
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), obj.getPropertyType("nonExistent"));
    assertNull(obj.getPropertyNode("nonExistent"));
  }

  @Test(timeout = 4000)
  public void testPropertyRemoval() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null);
    obj.defineInferredProperty("tempProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    assertTrue(obj.hasOwnProperty("tempProp"));

    assertTrue(obj.removeProperty("tempProp"));
    assertFalse(obj.hasOwnProperty("tempProp"));
    assertFalse(obj.removeProperty("tempProp"));
  }

  @Test(timeout = 4000)
  public void testPrototypeChainSlotLookupAndCounts() {
    PrototypeObjectType protoParent = new PrototypeObjectType(registry, "Parent", null, true);
    protoParent.defineInferredProperty("parentProp", registry.getNativeType(JSTypeNative.STRING_TYPE), null);
    protoParent.defineInferredProperty("sharedProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);

    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", protoParent);
    child.defineInferredProperty("childProp", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), null);
    child.defineInferredProperty("sharedProp", registry.getNativeType(JSTypeNative.STRING_TYPE), null);

    // Slot lookups
    assertNotNull(child.getSlot("childProp"));
    assertNotNull(child.getSlot("parentProp"));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), child.getSlot("sharedProp").getType());
    assertNull(child.getSlot("missingProp"));

    // hasProperty & hasOwnProperty
    assertTrue(child.hasProperty("parentProp"));
    assertFalse(child.hasOwnProperty("parentProp"));
    assertTrue(child.hasOwnProperty("childProp"));

    // Property count: parent has 2, child has 1 unique ('childProp') -> total = 3
    assertEquals(3, child.getPropertiesCount());

    // collectPropertyNames
    Set<String> collected = Sets.newHashSet();
    child.collectPropertyNames(collected);
    assertTrue(collected.contains("parentProp"));
    assertTrue(collected.contains("childProp"));
    assertTrue(collected.contains("sharedProp"));
  }

  @Test(timeout = 4000)
  public void testGetPropertiesCountWithNullImplicitPrototype() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, "Standalone", null, true);
    obj.defineInferredProperty("p1", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    obj.defineInferredProperty("p2", registry.getNativeType(JSTypeNative.STRING_TYPE), null);

    assertEquals(2, obj.getPropertiesCount());
    assertEquals(2, obj.getOwnPropertyNames().size());
  }

  @Test(timeout = 4000)
  public void testJSDocInfoLifecycleAndPreservation() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null);

    // Setting JSDoc on unmapped property implicitly defines it
    JSDocInfo info1 = new JSDocInfo();
    obj.setPropertyJSDocInfo("docProp", info1);
    assertTrue(obj.hasOwnProperty("docProp"));
    assertEquals(info1, obj.getOwnPropertyJSDocInfo("docProp"));

    // Overwriting inferred property preserves previously attached JSDocInfo
    obj.defineInferredProperty("docProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    assertEquals(info1, obj.getOwnPropertyJSDocInfo("docProp"));

    // Setting null JSDocInfo does not crash or erase
    obj.setPropertyJSDocInfo("docProp", null);
    assertEquals(info1, obj.getOwnPropertyJSDocInfo("docProp"));

    assertNull(obj.getOwnPropertyJSDocInfo("nonExistent"));
  }

  @Test(timeout = 4000)
  public void testExtendedInterfacesSlotLookup() {
    // Create super-interface ISuper with slot 'superMethod'
    FunctionType iSuperCtor = registry.createInterfaceType("ISuper", null);
    ObjectType iSuperInstance = iSuperCtor.getInstanceType();
    iSuperInstance.defineInferredProperty("superMethod", registry.getNativeType(JSTypeNative.STRING_TYPE), null);

    // Create sub-interface ISub extending ISuper
    FunctionType iSubCtor = registry.createInterfaceType("ISub", null);
    iSubCtor.setExtendedInterfaces(ImmutableList.of(iSuperInstance));

    ObjectType iSubProto = iSubCtor.getPrototype();
    assertNotNull("Slot should be found via extended interfaces chain", iSubProto.getSlot("superMethod"));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), iSubProto.getPropertyType("superMethod"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testToStringHelperRepresentations() {
    // 1. With class name
    PrototypeObjectType named = new PrototypeObjectType(registry, "Person", null);
    assertEquals("Person", named.toString());

    // 2. Anonymous without pretty-print
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null, true);
    assertFalse(anon.isPrettyPrint());
    assertEquals("{...}", anon.toStringHelper(false));
    assertEquals("?", anon.toStringHelper(true));

    // 3. Anonymous with pretty-print (empty)
    anon.setPrettyPrint(true);
    assertTrue(anon.isPrettyPrint());
    assertEquals("{}", anon.toStringHelper(false));

    // 4. Anonymous with pretty-print <= MAX_PRETTY_PRINTED_PROPERTIES (4)
    anon.defineInferredProperty("b", registry.getNativeType(JSTypeNative.STRING_TYPE), null);
    anon.defineInferredProperty("a", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    assertEquals("{a: number, b: string}", anon.toStringHelper(false));

    // 5. Anonymous with pretty-print > 4 properties (overflow truncation)
    anon.defineInferredProperty("c", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), null);
    anon.defineInferredProperty("d", registry.getNativeType(JSTypeNative.VOID_TYPE), null);
    anon.defineInferredProperty("e", registry.getNativeType(JSTypeNative.ALL_TYPE), null);

    assertEquals("{a: number, b: string, c: boolean, d: void, ...}", anon.toStringHelper(false));
    assertEquals("{a: number, b: string, c: boolean, d: void, e: *}", anon.toStringHelper(true));
  }

  @Test(timeout = 4000)
  public void testFunctionOwnerReferenceName() {
    FunctionType fn = registry.createConstructorType("Widget", null, null, null);
    ObjectType proto = fn.getPrototype();
    assertEquals("Widget.prototype", proto.getReferenceName());
    assertTrue(proto.hasReferenceName());
    assertEquals(fn, proto.getOwnerFunction());
  }

  @Test(timeout = 4000)
  public void testContextMatchingPredicates() {
    PrototypeObjectType custom = new PrototypeObjectType(registry, "Custom", null);
    assertTrue(custom.matchesObjectContext());
    assertFalse(custom.canBeCalled());

    // RegExp can be called
    ObjectType regexpType = registry.getNativeObjectType(JSTypeNative.REGEXP_TYPE);
    assertTrue(regexpType.canBeCalled());

    // Primitive object types number & string context matches
    ObjectType numObj = registry.getNativeObjectType(JSTypeNative.NUMBER_OBJECT_TYPE);
    ObjectType dateObj = registry.getNativeObjectType(JSTypeNative.DATE_TYPE);
    ObjectType boolObj = registry.getNativeObjectType(JSTypeNative.BOOLEAN_OBJECT_TYPE);
    ObjectType strObj = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);
    ObjectType theObj = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    ObjectType arrayObj = registry.getNativeObjectType(JSTypeNative.ARRAY_TYPE);

    assertTrue(numObj.matchesNumberContext());
    assertTrue(dateObj.matchesNumberContext());
    assertTrue(boolObj.matchesNumberContext());
    assertTrue(strObj.matchesNumberContext());

    assertTrue(theObj.matchesStringContext());
    assertTrue(strObj.matchesStringContext());
    assertTrue(dateObj.matchesStringContext());
    assertTrue(regexpType.matchesStringContext());
    assertTrue(arrayObj.matchesStringContext());
    assertTrue(numObj.matchesStringContext());
    assertTrue(boolObj.matchesStringContext());

    // Overridden native properties
    assertFalse(custom.matchesNumberContext());
    assertFalse(custom.matchesStringContext());

    custom.defineInferredProperty("valueOf", registry.getNativeType(JSTypeNative.STRING_TYPE), null);
    assertTrue(custom.matchesNumberContext());

    custom.defineInferredProperty("toString", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    assertTrue(custom.matchesStringContext());
  }

  @Test(timeout = 4000)
  public void testUnboxingTypes() {
    ObjectType strObj = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);
    ObjectType boolObj = registry.getNativeObjectType(JSTypeNative.BOOLEAN_OBJECT_TYPE);
    ObjectType numObj = registry.getNativeObjectType(JSTypeNative.NUMBER_OBJECT_TYPE);
    PrototypeObjectType regular = new PrototypeObjectType(registry, null, null);

    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), strObj.unboxesTo());
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), boolObj.unboxesTo());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), numObj.unboxesTo());
    assertNull(regular.unboxesTo());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testSetOwnerFunctionCannotBeReassigned() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, null, null);
    FunctionType fn1 = registry.createFunctionType(registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType fn2 = registry.createFunctionType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    proto.setOwnerFunction(fn1);
    // Reassignment to another non-null function must throw IllegalStateException
    proto.setOwnerFunction(fn2);
  }

  @Test(timeout = 4000)
  public void testSetOwnerFunctionCanBeResetToNull() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, null, null);
    FunctionType fn = registry.createFunctionType(registry.getNativeType(JSTypeNative.VOID_TYPE));

    proto.setOwnerFunction(fn);
    assertEquals(fn, proto.getOwnerFunction());

    // Resetting to null is allowed by preconditions
    proto.setOwnerFunction(null);
    assertNull(proto.getOwnerFunction());
  }

  @Test(timeout = 4000)
  public void testGetConstructorAlwaysReturnsNull() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, "Sample", null);
    assertNull(proto.getConstructor());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Subtyping Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testSubtypingUnionAndRecordTypes() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, null, null);
    obj.defineInferredProperty("x", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);

    // 1. Same instance is subtype
    assertTrue(obj.isSubtype(obj));

    // 2. Union types return false in isSubtype decomposition
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    assertFalse(obj.isSubtype(union));

    // 3. Record type matching
    RecordTypeBuilder matchBuilder = new RecordTypeBuilder(registry);
    matchBuilder.addProperty("x", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    RecordType matchingRecord = (RecordType) matchBuilder.build();
    assertTrue(obj.isSubtype(matchingRecord));

    RecordTypeBuilder unmatchBuilder = new RecordTypeBuilder(registry);
    unmatchBuilder.addProperty("y", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    RecordType unmatchingRecord = (RecordType) unmatchBuilder.build();
    assertFalse(obj.isSubtype(unmatchingRecord));
  }

  @Test(timeout = 4000)
  public void testSubtypeWithUnknownPrototypeChain() {
    ObjectType unknown = (ObjectType) registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    PrototypeObjectType protoWithUnknown = new PrototypeObjectType(registry, null, unknown);

    // If implicit prototype chain contains UNKNOWN_TYPE, isSubtype returns true defensively
    assertTrue(protoWithUnknown.isSubtype(registry.getNativeObjectType(JSTypeNative.DATE_TYPE)));
  }

  @Test(timeout = 4000)
  public void testSubtypeWithImplementedInterface() {
    FunctionType ifaceCtor = registry.createInterfaceType("IAction", null);
    ObjectType ifaceInstance = ifaceCtor.getInstanceType();

    FunctionType implCtor = registry.createConstructorType("ActionImpl", null, null, null);
    implCtor.setImplementedInterfaces(ImmutableList.of(ifaceInstance));

    ObjectType implProto = implCtor.getPrototype();
    assertTrue(implProto.isSubtype(ifaceInstance));
  }

  @Test(timeout = 4000)
  public void testResolveInternalCyclesAndProperties() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, "ResolveMe", null);
    obj.defineInferredProperty("prop", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);

    JSType resolved = obj.resolveInternal(errorReporter, null);
    assertSame(obj, resolved);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), obj.getPropertyType("prop"));
  }

  @Test(timeout = 4000)
  public void testMatchConstraintOnAnonymousType() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null, true);
    assertFalse(anon.hasReferenceName());

    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("newProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    builder.addProperty("existingProp", registry.getNativeType(JSTypeNative.STRING_TYPE), null);
    RecordType record = (RecordType) builder.build();

    // Existing inferred property
    anon.defineInferredProperty("existingProp", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), null);

    anon.matchConstraint(record);

    // 1. newProp wasn't present, so inferred type is union with VOID_TYPE
    assertTrue(anon.hasProperty("newProp"));
    JSType expectedNewType = registry.getNativeType(JSTypeNative.VOID_TYPE)
        .getLeastSupertype(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    assertEquals(expectedNewType, anon.getPropertyType("newProp"));

    // 2. existingProp was present, inferred type is replaced by constraint's type directly
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), anon.getPropertyType("existingProp"));

    // 3. Calling matchConstraint with non-record type is a no-op
    anon.matchConstraint(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE));
  }
}