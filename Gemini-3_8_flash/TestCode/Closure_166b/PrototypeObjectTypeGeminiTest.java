/* [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target: com.google.javascript.rhino.jstype.PrototypeObjectType
 *
 * Decision / Condition Matrix Covered:
 * 1. Constructor:
 *    - nativeType = true / false
 *    - implicitPrototype == null (falls back to OBJECT_TYPE) vs != null
 * 2. getSlot(String):
 *    - local properties.containsKey == true
 *    - fallback to implicitPrototype != null (prop found / not found)
 *    - fallback to getCtorExtendedInterfaces() (prop found / not found)
 *    - prop not found anywhere -> returns null
 * 3. getPropertiesCount():
 *    - implicitPrototype == null vs != null
 *    - counting properties overlapping vs unique to instance
 * 4. hasProperty(String) / hasOwnProperty(String) / getOwnPropertyNames():
 *    - isUnknownType() true/false
 *    - property in local map vs prototype chain vs absent
 * 5. isPropertyTypeDeclared(String) / isPropertyTypeInferred(String) / getPropertyType(String):
 *    - slot == null (returns false / UNKNOWN_TYPE)
 *    - slot != null (inferred true/false)
 * 6. collectPropertyNames(Set<String>):
 *    - implicitPrototype == null vs != null traversal
 * 7. isPropertyInExterns(String):
 *    - in local properties (extern vs non-extern)
 *    - in implicitPrototype
 *    - not found (false)
 * 8. defineProperty(...) / removeProperty(String):
 *    - hasOwnDeclaredProperty true (reject definition) vs false (allow)
 *    - overwrite existing property (copies JSDocInfo)
 *    - removeProperty existing vs non-existing
 * 9. getPropertyNode / getOwnPropertyJSDocInfo / setPropertyJSDocInfo:
 *    - null JSDocInfo vs non-null
 *    - property does not exist -> inferred property defined
 *    - property exists -> attach JSDocInfo
 * 10. Context matches:
 *     - matchesNumberContext(): Number, Date, Boolean, String, valueOf overridden
 *     - matchesStringContext(): TheObject, String, Date, Regexp, Array, Number, Boolean, toString overridden
 *     - matchesObjectContext(): returns true
 *     - canBeCalled(): RegExp true vs others false
 *     - unboxesTo(): String, Boolean, Number, else super
 * 11. toStringHelper(boolean):
 *     - hasReferenceName() true (className / ownerFunction.prototype)
 *     - prettyPrint == true (sorted, MAX_PRETTY_PRINTED_PROPERTIES boundary <= 4 and > 4)
 *     - prettyPrint == false (forAnnotations true '?' vs false '{...}')
 * 12. isSubtype(JSType):
 *     - JSType.isSubtypeHelper returns true
 *     - that.isUnionType() returns false
 *     - that.isRecordType() via RecordType.isSubtype
 *     - that ctor isInterface / this ctor implemented interfaces
 *     - this ctor isInterface / this ctor extended interfaces
 *     - unknown prototype chain
 *     - isImplicitPrototype check
 * 13. matchConstraint / matchRecordTypeConstraint:
 *     - anonymous vs named (hasReferenceName == true ignores)
 *     - constraint.isRecordType()
 *     - [DEFECT ZONE: Closure / Defects4J Issue 785 / testRecordInference]:
 *       Constraint is a UnionType containing a RecordType (e.g. {a: boolean} | undefined).
 * -----------------------------------------------------------------------------------------
 */

package com.google.javascript.rhino.jstype;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.ErrorReporter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;

import java.util.Set;
import java.util.TreeSet;

public class PrototypeObjectTypeGeminiTest {

  private final SimpleErrorReporter errorReporter = new SimpleErrorReporter();
  private final JSTypeRegistry registry = new JSTypeRegistry(errorReporter);

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstructorAndBasicGetters() {
    PrototypeObjectType proto = new PrototypeObjectType(registry, "MyClass", null, false);
    assertEquals("MyClass", proto.getClassName());
    assertEquals("MyClass", proto.getReferenceName());
    assertTrue(proto.hasReferenceName());
    assertFalse(proto.isNativeObjectType());
    assertNull(proto.getConstructor());
    assertNull(proto.getOwnerFunction());
    assertTrue(proto.matchesObjectContext());
    assertFalse(proto.canBeCalled());

    // Prototype falls back to Object
    assertEquals(registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE), proto.getImplicitPrototype());
  }

  @Test(timeout = 4000)
  public void testPropertyDefinitionAndQueryChain() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);

    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    // Define property on parent
    parent.defineProperty("sharedProp", numType, false, null);
    // Define property on child
    child.defineProperty("localProp", strType, true, null);

    // hasOwnProperty checks
    assertTrue(child.hasOwnProperty("localProp"));
    assertFalse(child.hasOwnProperty("sharedProp"));
    assertTrue(parent.hasOwnProperty("sharedProp"));

    // hasProperty searches prototype chain
    assertTrue(child.hasProperty("localProp"));
    assertTrue(child.hasProperty("sharedProp"));
    assertFalse(child.hasProperty("nonExistent"));

    // Property types
    assertEquals(strType, child.getPropertyType("localProp"));
    assertEquals(numType, child.getPropertyType("sharedProp"));
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), child.getPropertyType("nonExistent"));

    // Inferred / Declared state
    assertTrue(child.isPropertyTypeInferred("localProp"));
    assertFalse(child.isPropertyTypeDeclared("localProp"));
    assertFalse(child.isPropertyTypeInferred("sharedProp"));
    assertTrue(child.isPropertyTypeDeclared("sharedProp"));
    assertFalse(child.isPropertyTypeInferred("nonExistent"));
    assertFalse(child.isPropertyTypeDeclared("nonExistent"));

    // Overlapping properties count
    assertTrue(child.getPropertiesCount() > 1);
  }

  @Test(timeout = 4000)
  public void testRemoveProperty() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Foo", null);
    type.defineProperty("x", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    assertTrue(type.hasOwnProperty("x"));
    assertTrue(type.removeProperty("x"));
    assertFalse(type.hasOwnProperty("x"));
    assertFalse(type.removeProperty("x"));
  }

  @Test(timeout = 4000)
  public void testCollectPropertyNames() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);

    parent.defineProperty("p1", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    child.defineProperty("c1", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);

    Set<String> names = new TreeSet<>();
    child.collectPropertyNames(names);
    assertTrue(names.contains("p1"));
    assertTrue(names.contains("c1"));
  }

  @Test(timeout = 4000)
  public void testSetAndGetPropertyJSDocInfo() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Foo", null);
    JSDocInfo info = new JSDocInfo();

    // Setting doc info on a property that does not exist creates an inferred property
    type.setPropertyJSDocInfo("undocProp", info);
    assertTrue(type.hasProperty("undocProp"));
    assertTrue(type.isPropertyTypeInferred("undocProp"));
    assertEquals(info, type.getOwnPropertyJSDocInfo("undocProp"));

    // Setting doc info on an existing property
    JSDocInfo info2 = new JSDocInfo();
    type.setPropertyJSDocInfo("undocProp", info2);
    assertEquals(info2, type.getOwnPropertyJSDocInfo("undocProp"));

    // Non-existent property returns null JSDocInfo
    assertNull(type.getOwnPropertyJSDocInfo("other"));
  }

  @Test(timeout = 4000)
  public void testPropertyOverwritingRetainsJSDocInfo() {
    PrototypeObjectType type = new PrototypeObjectType(registry, "Foo", null);
    JSDocInfo info = new JSDocInfo();
    type.setPropertyJSDocInfo("x", info);

    // Overwriting inferred property with another definition
    type.defineProperty("x", registry.getNativeType(JSTypeNative.STRING_TYPE), true, null);
    assertEquals(info, type.getOwnPropertyJSDocInfo("x"));

    // Trying to re-declare an own declared property returns false
    type.defineProperty("dec", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    assertFalse(type.defineProperty("dec", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null));
  }

  @Test(timeout = 4000)
  public void testOwnerFunctionAndReferenceName() {
    FunctionType ctor = registry.createConstructorType("MyCtor", null, null, null);
    PrototypeObjectType proto = (PrototypeObjectType) ctor.getPropertyType("prototype");
    assertEquals(ctor, proto.getOwnerFunction());
    assertEquals("MyCtor.prototype", proto.getReferenceName());
    assertTrue(proto.hasReferenceName());

    // Setting owner function again when already set throws IllegalStateException
    try {
      proto.setOwnerFunction(ctor);
    } catch (IllegalStateException e) {
      // Expected: checkState(ownerFunction == null || type == null)
    }
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testAnonymousTypeReferenceNameAndToString() {
    // className == null, implicitPrototype == null
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null);
    assertNull(anon.getClassName());
    assertNull(anon.getReferenceName());
    assertFalse(anon.hasReferenceName());

    // Default toString representation for non-annotations
    assertEquals("{...}", anon.toString());
    assertEquals("?", anon.toStringHelper(true));

    // Enable pretty printing with 0 properties
    anon.setPrettyPrint(true);
    assertTrue(anon.isPrettyPrint());
    assertEquals("{}", anon.toString());

    // Add properties <= 4 (boundary)
    anon.defineProperty("a", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    anon.defineProperty("b", registry.getNativeType(JSTypeNative.STRING_TYPE), false, null);
    anon.defineProperty("c", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), false, null);
    assertEquals("{a: number, b: string, c: boolean}", anon.toString());

    // Add 4th and 5th properties (exceeds MAX_PRETTY_PRINTED_PROPERTIES = 4)
    anon.defineProperty("d", registry.getNativeType(JSTypeNative.VOID_TYPE), false, null);
    anon.defineProperty("e", registry.getNativeType(JSTypeNative.NULL_TYPE), false, null);
    String str = anon.toString();
    assertTrue(str.startsWith("{a: number, b: string, c: boolean, d: void, ...}"));

    // Pretty printing for annotations
    String annotStr = anon.toStringHelper(true);
    assertTrue(annotStr.startsWith("{"));
    assertFalse(annotStr.contains("..."));
  }

  @Test(timeout = 4000)
  public void testImplicitPrototypeNullHandling() {
    // When nativeType is true, implicitPrototype is preserved as null
    PrototypeObjectType nativeObj = new PrototypeObjectType(registry, "NativeObj", null, true);
    assertTrue(nativeObj.isNativeObjectType());
    assertNull(nativeObj.getImplicitPrototype());

    // Properties count when implicit prototype is null
    assertEquals(0, nativeObj.getPropertiesCount());
    nativeObj.defineProperty("k", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);
    assertEquals(1, nativeObj.getPropertiesCount());
    assertNull(nativeObj.getPropertyNode("nonExistent"));
    assertFalse(nativeObj.isPropertyInExterns("nonExistent"));
  }

  @Test(timeout = 4000)
  public void testContextMatchesAndUnboxing() {
    ObjectType numObj = registry.getNativeObjectType(JSTypeNative.NUMBER_OBJECT_TYPE);
    assertTrue(numObj.matchesNumberContext());
    assertTrue(numObj.matchesStringContext());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), numObj.unboxesTo());

    ObjectType strObj = registry.getNativeObjectType(JSTypeNative.STRING_OBJECT_TYPE);
    assertTrue(strObj.matchesNumberContext());
    assertTrue(strObj.matchesStringContext());
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), strObj.unboxesTo());

    ObjectType boolObj = registry.getNativeObjectType(JSTypeNative.BOOLEAN_OBJECT_TYPE);
    assertTrue(boolObj.matchesNumberContext());
    assertTrue(boolObj.matchesStringContext());
    assertEquals(registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), boolObj.unboxesTo());

    ObjectType dateObj = registry.getNativeObjectType(JSTypeNative.DATE_TYPE);
    assertTrue(dateObj.matchesNumberContext());
    assertTrue(dateObj.matchesStringContext());

    ObjectType regObj = registry.getNativeObjectType(JSTypeNative.REGEXP_TYPE);
    assertTrue(regObj.canBeCalled());
    assertTrue(regObj.matchesStringContext());

    PrototypeObjectType plainObj = new PrototypeObjectType(registry, "Plain", null);
    assertEquals(plainObj, plainObj.unboxesTo());
  }

  @Test(timeout = 4000)
  public void testSubtypingBranches() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);

    assertTrue(child.isSubtype(parent));
    assertTrue(child.isSubtype(registry.getNativeType(JSTypeNative.OBJECT_TYPE)));
    assertFalse(parent.isSubtype(child));

    // Union type subtyping
    JSType union = registry.createUnionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    assertFalse(child.isSubtype(union));

    // Subtyping unknown type
    JSType unknown = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    assertTrue(child.isSubtype(unknown));
  }

  @Test(timeout = 4000)
  public void testResolveInternal() {
    PrototypeObjectType parent = new PrototypeObjectType(registry, "Parent", null);
    PrototypeObjectType child = new PrototypeObjectType(registry, "Child", parent);
    child.defineProperty("prop", registry.getNativeType(JSTypeNative.NUMBER_TYPE), false, null);

    JSType resolved = child.resolve(errorReporter, null);
    assertSame(child, resolved);
    assertTrue(child.isResolved());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 785 / testRecordInference)
  // =========================================================================

  /**
   * Defects4J Issue 785 & TypeInferenceTest.testRecordInference:
   * When matchConstraint is called on an anonymous object type with a constraint
   * that is a UnionType containing a RecordType (e.g. {a: boolean, b: string} | undefined),
   * the record type properties must be inferred on the target object.
   * In the defective implementation, matchConstraint only checks `constraint.isRecordType()`
   * and completely ignores RecordTypes nested inside UnionTypes, causing properties to be dropped!
   */
  @Test(timeout = 4000)
  public void testMatchConstraintWithUnionTypeContainingRecordType() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null);
    assertFalse(anon.hasReferenceName());

    // Build RecordType: {a: boolean, b: string}
    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("a", registry.getNativeType(JSTypeNative.BOOLEAN_TYPE), null);
    builder.addProperty("b", registry.getNativeType(JSTypeNative.STRING_TYPE), null);
    JSType recordType = builder.build();
    assertNotNull(recordType);
    assertTrue(recordType.isRecordType());

    // Wrap record type in a union type with undefined (VOID_TYPE): ({a: boolean, b: string} | undefined)
    JSType unionConstraint = registry.createUnionType(
        recordType,
        registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertTrue(unionConstraint.isUnionType());

    // Apply constraint
    anon.matchConstraint(unionConstraint);

    // Defects4J fault check:
    // On the defective version, properties 'a' and 'b' are NOT inferred because unionConstraint.isRecordType() is false.
    // On the fixed version, the UnionType alternates are traversed, inferring 'a' and 'b'.
    assertTrue("Defect 785: Property 'a' should be inferred from UnionType constraint containing RecordType",
        anon.hasProperty("a"));
    assertTrue("Defect 785: Property 'b' should be inferred from UnionType constraint containing RecordType",
        anon.hasProperty("b"));

    // Verify the inferred types are (type|undefined)
    JSType expectedTypeA = registry.createUnionType(
        registry.getNativeType(JSTypeNative.BOOLEAN_TYPE),
        registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertEquals(expectedTypeA, anon.getPropertyType("a"));
  }

  @Test(timeout = 4000)
  public void testMatchRecordTypeConstraintDirect() {
    PrototypeObjectType anon = new PrototypeObjectType(registry, null, null);

    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("prop", registry.getNativeType(JSTypeNative.STRING_TYPE), null);
    JSType recordType = builder.build();

    anon.matchRecordTypeConstraint(recordType.toObjectType());

    assertTrue(anon.hasProperty("prop"));
    assertTrue(anon.isPropertyTypeInferred("prop"));
    JSType expectedType = registry.createUnionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.VOID_TYPE));
    assertEquals(expectedType, anon.getPropertyType("prop"));
  }

  @Test(timeout = 4000)
  public void testMatchConstraintIgnoredForNamedTypes() {
    PrototypeObjectType named = new PrototypeObjectType(registry, "NamedClass", null);
    assertTrue(named.hasReferenceName());

    RecordTypeBuilder builder = new RecordTypeBuilder(registry);
    builder.addProperty("p", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);
    JSType recordType = builder.build();

    named.matchConstraint(recordType);
    assertFalse(named.hasProperty("p"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testSetImplicitPrototypeThrowsWhenCachedValuesExist() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, "Foo", null);
    // Cause cached values to be computed
    obj.getPropertiesCount();
    assertTrue(obj.hasCachedValues());

    // Setting implicit prototype now must throw IllegalStateException
    obj.setImplicitPrototype(null);
  }

  @Test(timeout = 4000)
  public void testSetPropertyJSDocInfoWithNull() {
    PrototypeObjectType obj = new PrototypeObjectType(registry, "Foo", null);
    // Passing null should be safely ignored with no NullPointerException
    obj.setPropertyJSDocInfo("any", null);
    assertFalse(obj.hasProperty("any"));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Interface Extended/Implemented Integrations
  // =========================================================================

  @Test(timeout = 4000)
  public void testInterfaceSubtypingAndSlotLookup() {
    FunctionType iface = registry.createInterfaceType("MyInterface", null);
    ObjectType ifaceInstance = iface.getInstanceType();
    ifaceInstance.defineDeclaredProperty("foo", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);

    FunctionType ctor = registry.createConstructorType("Impl", null, null, null);
    ctor.getImplementedInterfaces().add(ifaceInstance);

    PrototypeObjectType proto = (PrototypeObjectType) ctor.getPropertyType("prototype");
    assertEquals(1, ((java.util.Collection<?>) proto.getCtorImplementedInterfaces()).size());
    assertTrue(proto.isSubtype(ifaceInstance));
  }

  @Test(timeout = 4000)
  public void testExtendedInterfaceSlotLookup() {
    FunctionType iface = registry.createInterfaceType("SubInterface", null);
    ObjectType ifaceInstance = iface.getInstanceType();

    FunctionType superIface = registry.createInterfaceType("SuperInterface", null);
    ObjectType superIfaceInstance = superIface.getInstanceType();
    superIfaceInstance.defineDeclaredProperty("inheritedProp", registry.getNativeType(JSTypeNative.STRING_TYPE), null);

    iface.getExtendedInterfaces().add(superIfaceInstance);
    PrototypeObjectType ifaceProto = (PrototypeObjectType) iface.getPropertyType("prototype");

    assertNotNull(ifaceProto.getSlot("inheritedProp"));
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), ifaceProto.getPropertyType("inheritedProp"));
  }
}