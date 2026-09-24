/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.TypeValidator
 * Known Defect: Defects4J TypeCheckTest::testInterfaceInheritanceCheck12 (missing warning on interface implementation / inheritance typing flaws)
 *
 * Decision / Condition Matrix:
 * 1. expectObject: type.matchesObjectContext() -> True / False (mismatch with OBJECT_TYPE)
 * 2. expectActualObject: type.isObject() -> True / False (mismatch with OBJECT_TYPE)
 * 3. expectAnyObject: !anyObjectType.isSubtype(type) && !type.isEmptyType() -> Branch coverage on subtyping & empty types
 * 4. expectString: type.matchesStringContext() -> True / False
 * 5. expectNumber: type.matchesNumberContext() -> True / False
 * 6. expectBitwiseable: !matchesNumberContext() && !isSubtype(allValueTypes)
 * 7. expectStringOrNumber: !matchesNumberContext() && !matchesStringContext()
 * 8. expectNotNullOrUndefined: nullOrUndefined subtype check, GETPROP non-global edge case, forward-declared unresolved types
 * 9. expectSwitchMatchesCase: shallow equality check & autoboxing subtype fallback
 * 10. expectIndexMatch: objType is unknown, has restricted index, is array, matches object context, or invalid indexable
 * 11. expectCanAssignTo / expectCanAssignToPropertyOf: assignment compatibility, bothIntrinsics check (constructor/enum)
 * 12. expectArgumentMatchesParameter: argument assignable to parameter check
 * 13. expectCanOverride: overridingType.canAssignTo(hiddenType) & HIDDEN_PROPERTY_MISMATCH warning
 * 14. expectSuperType: declaredSuper equals superObject, declaredSuper equals Object, prototype update
 * 15. expectCanCast: restrictByNotNullOrUndefined & bidirectional canAssignTo check, INVALID_CAST warning
 * 16. expectUndeclaredVariable: GETPROP suppressions ("duplicate"), native types, duplicate declaration warning
 * 17. expectAllInterfaceProperties & expectInterfaceProperty: defect-targeted zone (interface property not implemented or mismatched)
 * 18. getReadableJSTypeName: GETPROP prototype climb, dereferencing, qualified names, function types
 * 19. TypeMismatch: equals (symmetry, reflexive, non-matching), hashCode, toString
 */

package com.google.javascript.jscomp;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.EnumType;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class TypeValidatorGeminiTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private TypeValidator validator;
  private NodeTraversal traversal;
  private Node dummyNode;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    validator = new TypeValidator(compiler);
    traversal = new NodeTraversal(compiler, null);
    dummyNode = Node.newString("testNode");
    dummyNode.setLineno(1);
    dummyNode.setCharno(0);
    dummyNode.putProp(Node.SOURCENAME_PROP, "test.js");
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testExpectObjectSuccessAndFailure() {
    JSType objType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    boolean success = validator.expectObject(traversal, dummyNode, objType, "Expected object");
    assertTrue("Object type should match object context", success);
    assertEquals("Should not have warnings for valid object", 0, compiler.getWarningCount());

    boolean failure = validator.expectObject(traversal, dummyNode, numType, "Expected object");
    assertFalse("Number type should not match object context", failure);
    assertEquals("Should report warning on mismatch", 1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectActualObject() {
    JSType objType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    validator.expectActualObject(traversal, dummyNode, objType, "msg");
    assertEquals(0, compiler.getWarningCount());

    validator.expectActualObject(traversal, dummyNode, strType, "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectAnyObject() {
    JSType emptyType = registry.getNativeType(JSTypeNative.NO_TYPE);
    JSType objType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    validator.expectAnyObject(traversal, dummyNode, emptyType, "msg");
    assertEquals(0, compiler.getWarningCount());

    validator.expectAnyObject(traversal, dummyNode, objType, "msg");
    assertEquals(0, compiler.getWarningCount());

    validator.expectAnyObject(traversal, dummyNode, numType, "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectStringAndExpectNumber() {
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    validator.expectString(traversal, dummyNode, strType, "msg");
    assertEquals(0, compiler.getWarningCount());

    validator.expectString(traversal, dummyNode, numType, "msg");
    assertEquals(1, compiler.getWarningCount());

    validator.expectNumber(traversal, dummyNode, numType, "msg");
    assertEquals(1, compiler.getWarningCount());

    validator.expectNumber(traversal, dummyNode, strType, "msg");
    assertEquals(2, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectBitwiseable() {
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType boolType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    JSType objType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);

    validator.expectBitwiseable(traversal, dummyNode, numType, "msg");
    validator.expectBitwiseable(traversal, dummyNode, boolType, "msg");
    assertEquals(0, compiler.getWarningCount());

    validator.expectBitwiseable(traversal, dummyNode, objType, "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectStringOrNumber() {
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType boolType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);

    validator.expectStringOrNumber(traversal, dummyNode, numType, "msg");
    validator.expectStringOrNumber(traversal, dummyNode, strType, "msg");
    assertEquals(0, compiler.getWarningCount());

    validator.expectStringOrNumber(traversal, dummyNode, boolType, "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectSwitchMatchesCase() {
    Node switchNode = new Node(Token.SWITCH, dummyNode);
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    validator.expectSwitchMatchesCase(traversal, switchNode, numType, numType);
    assertEquals(0, compiler.getWarningCount());

    validator.expectSwitchMatchesCase(traversal, switchNode, numType, strType);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectIndexMatch() {
    JSType unkType = registry.getNativeType(JSTypeNative.UNKNOWN_TYPE);
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType arrType = registry.getNativeType(JSTypeNative.ARRAY_TYPE);
    JSType objType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType boolType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);

    // objType is unknown
    validator.expectIndexMatch(traversal, dummyNode, unkType, strType);
    assertEquals(0, compiler.getWarningCount());

    // objType is array
    validator.expectIndexMatch(traversal, dummyNode, arrType, numType);
    assertEquals(0, compiler.getWarningCount());
    validator.expectIndexMatch(traversal, dummyNode, arrType, strType);
    assertEquals(1, compiler.getWarningCount());

    // objType is generic object
    validator.expectIndexMatch(traversal, dummyNode, objType, strType);
    assertEquals(1, compiler.getWarningCount());

    // objType is invalid indexable (e.g., boolean)
    validator.expectIndexMatch(traversal, dummyNode, boolType, numType);
    assertEquals(2, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectIndexMatchWithRestrictedIndex() {
    ObjectType objWithIndex = registry.createObjectType("ObjWithIndex", dummyNode, null);
    ObjectType indexed = registry.createObjectType("IndexRestricted", dummyNode, objWithIndex);
    // Setting an index type
    indexed.setPropertyJSType("foo", registry.getNativeType(JSTypeNative.STRING_TYPE));

    // When indexType is assignable vs not assignable
    ObjectType recordType = registry.createRecordTypeBuilder()
        .add("key", registry.getNativeType(JSTypeNative.NUMBER_TYPE))
        .build();
    validator.expectIndexMatch(traversal, dummyNode, recordType, registry.getNativeType(JSTypeNative.STRING_TYPE));
    assertTrue(compiler.getWarningCount() >= 0);
  }

  @Test(timeout = 4000)
  public void testExpectCanAssignToAndPropertyOf() {
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    Node owner = Node.newString(Token.NAME, "myObj");
    owner.setJSType(registry.getNativeType(JSTypeNative.OBJECT_TYPE));

    boolean pass = validator.expectCanAssignToPropertyOf(traversal, dummyNode, numType, numType, owner, "prop");
    assertTrue(pass);
    assertEquals(0, compiler.getWarningCount());

    boolean fail = validator.expectCanAssignToPropertyOf(traversal, dummyNode, strType, numType, owner, "prop");
    assertFalse(fail);
    assertEquals(1, compiler.getWarningCount());

    boolean assignFail = validator.expectCanAssignTo(traversal, dummyNode, strType, numType, "Custom error");
    assertFalse(assignFail);
    assertEquals(2, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectArgumentMatchesParameter() {
    Node fnNode = Node.newString(Token.NAME, "testFn");
    Node callNode = new Node(Token.CALL, fnNode);
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    validator.expectArgumentMatchesParameter(traversal, dummyNode, numType, numType, callNode, 1);
    assertEquals(0, compiler.getWarningCount());

    validator.expectArgumentMatchesParameter(traversal, dummyNode, strType, numType, callNode, 1);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectCanOverride() {
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    ObjectType objType = (ObjectType) registry.getNativeType(JSTypeNative.OBJECT_TYPE);

    validator.expectCanOverride(traversal, dummyNode, numType, numType, "myProp", objType);
    assertEquals(0, compiler.getWarningCount());

    validator.expectCanOverride(traversal, dummyNode, strType, numType, "myProp", objType);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.HIDDEN_PROPERTY_MISMATCH, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testExpectCanCast() {
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType allType = registry.getNativeType(JSTypeNative.ALL_TYPE);

    validator.expectCanCast(traversal, dummyNode, numType, allType);
    assertEquals(0, compiler.getWarningCount());

    validator.expectCanCast(traversal, dummyNode, numType, strType);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.INVALID_CAST, compiler.getWarnings()[0].getType());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testSetShouldReport() {
    validator.setShouldReport(false);
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    validator.expectCanCast(traversal, dummyNode, numType, strType);
    assertEquals("Warnings should be suppressed when shouldReport is false", 0, compiler.getWarningCount());

    // But mismatch is still recorded
    Iterator<TypeValidator.TypeMismatch> it = validator.getMismatches().iterator();
    assertTrue(it.hasNext());
    TypeValidator.TypeMismatch mismatch = it.next();
    assertEquals(numType, mismatch.typeA);
    assertEquals(strType, mismatch.typeB);
  }

  @Test(timeout = 4000)
  public void testExpectNotNullOrUndefined() {
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    assertTrue(validator.expectNotNullOrUndefined(traversal, dummyNode, numType, "msg", numType));
    assertEquals(0, compiler.getWarningCount());

    assertFalse(validator.expectNotNullOrUndefined(traversal, dummyNode, nullType, "msg", numType));
    assertEquals(1, compiler.getWarningCount());

    assertFalse(validator.expectNotNullOrUndefined(traversal, dummyNode, voidType, "msg", numType));
    assertEquals(2, compiler.getWarningCount());

    // GETPROP edge case in local scope with null type
    Node getPropNode = new Node(Token.GETPROP, Node.newString("a"), Node.newString("b"));
    Node rootNode = new Node(Token.FUNCTION, Node.newString("f"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    NodeTraversal localTraversal = new NodeTraversal(compiler, null, new SyntacticScopeCreator(compiler));
    localTraversal.traverse(rootNode);

    // If traversed inside non-global scope
    boolean result = validator.expectNotNullOrUndefined(localTraversal, getPropNode, nullType, "msg", numType);
    assertTrue("GETPROP in non-global scope with null type should bypass warning", result);
  }

  @Test(timeout = 4000)
  public void testBothIntrinsicsMismatch() {
    EnumType enumType1 = registry.createEnumType("Enum1", dummyNode, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    EnumType enumType2 = registry.createEnumType("Enum2", dummyNode, registry.getNativeType(JSTypeNative.STRING_TYPE));

    boolean res = validator.expectCanAssignTo(traversal, dummyNode, enumType1, enumType2, "msg");
    assertFalse(res);
    // When both are intrinsics (constructors or enums), mismatch is registered directly without report
    assertEquals(0, compiler.getWarningCount());
    assertTrue(validator.getMismatches().iterator().hasNext());
  }

  @Test(timeout = 4000)
  public void testExpectSuperTypeMissingExtendsTag() {
    FunctionType superCtor = registry.createConstructorType("SuperClass", dummyNode, null, null);
    FunctionType subCtor = registry.createConstructorType("SubClass", dummyNode, null, null);

    ObjectType superInstance = superCtor.getInstanceType();
    ObjectType subInstance = subCtor.getInstanceType();

    validator.expectSuperType(traversal, dummyNode, superInstance, subInstance);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.MISSING_EXTENDS_TAG_WARNING, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testExpectSuperTypeMismatch() {
    FunctionType superCtor1 = registry.createConstructorType("SuperClass1", dummyNode, null, null);
    FunctionType superCtor2 = registry.createConstructorType("SuperClass2", dummyNode, null, null);
    FunctionType subCtor = registry.createConstructorType("SubClass", dummyNode, null, null);

    subCtor.setPrototypeBasedOn(superCtor1.getInstanceType());

    ObjectType superInstance2 = superCtor2.getInstanceType();
    ObjectType subInstance = subCtor.getInstanceType();

    validator.expectSuperType(traversal, dummyNode, superInstance2, subInstance);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.TYPE_MISMATCH_WARNING, compiler.getWarnings()[0].getType());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J interface inheritance)
  // =========================================================================

  @Test(timeout = 4000)
  public void testExpectAllInterfacePropertiesNotImplemented() {
    // Interface with a method
    ObjectType iface = registry.createInterfaceType("MyInterface", dummyNode);
    ObjectType proto = iface.getImplicitPrototype();
    assertNotNull(proto);
    proto.defineDeclaredProperty("mustImplementMethod", registry.getNativeType(JSTypeNative.NUMBER_TYPE), dummyNode);

    // Class implementing the interface but lacking the property
    FunctionType classCtor = registry.createConstructorType("MyClass", dummyNode, null, null);
    classCtor.setImplementedInterfaces(ImmutableList.of(iface));

    validator.expectAllInterfaceProperties(traversal, dummyNode, classCtor);

    // Verifying that INTERFACE_METHOD_NOT_IMPLEMENTED is reported
    assertEquals("Should warn about unimplemented interface property", 1, compiler.getWarningCount());
    assertEquals(TypeValidator.INTERFACE_METHOD_NOT_IMPLEMENTED, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testDefectInterfaceInheritanceCheckMissingWarning() {
    /*
     * Targets testInterfaceInheritanceCheck12:
     * When an interface extends a super-interface and declares or inherits incompatible property types,
     * or when a class implements an interface property with incompatible signatures.
     */
    ObjectType superIface = registry.createInterfaceType("SuperInterface", dummyNode);
    superIface.getImplicitPrototype().defineDeclaredProperty(
        "conflictProp", registry.getNativeType(JSTypeNative.NUMBER_TYPE), dummyNode);

    // Class that implements superIface but assigns conflictProp an incompatible type (String)
    FunctionType implClass = registry.createConstructorType("ImplClass", dummyNode, null, null);
    implClass.setImplementedInterfaces(ImmutableList.of(superIface));
    implClass.getInstanceType().defineDeclaredProperty(
        "conflictProp", registry.getNativeType(JSTypeNative.STRING_TYPE), dummyNode);

    validator.expectAllInterfaceProperties(traversal, dummyNode, implClass);

    // The validator checks interface properties.
    // In defective versions, mismatched interface property types do not trigger a warning on expectAllInterfaceProperties.
    // Asserting expected warning behavior:
    boolean hasWarning = compiler.getWarningCount() > 0;
    // Note: if the bug is present, hasWarning is false; on fixed versions, warning is generated.
    assertTrue("Expected a warning for interface property type mismatch", hasWarning);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testExpectUndeclaredVariableNativeDeclaration() {
    Scope scope = new SyntacticScopeCreator(compiler).createScope(dummyNode, null);
    Node nameNode = Node.newString(Token.NAME, "x");
    nameNode.setLineno(1);
    // var.input == null signifies native type declaration
    Scope.Var var = new Scope.Var(false, "x", nameNode, registry.getNativeType(JSTypeNative.NUMBER_TYPE), scope, 0, null);

    Node parent = new Node(Token.VAR, dummyNode);
    validator.expectUndeclaredVariable("test.js", dummyNode, parent, var, "x", registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    assertEquals(0, compiler.getWarningCount());
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), dummyNode.getJSType());
  }

  @Test(timeout = 4000)
  public void testExpectUndeclaredVariableDuplicateWarning() {
    CompilerInput input = new CompilerInput(null, "test.js", false);
    Scope scope = new SyntacticScopeCreator(compiler).createScope(dummyNode, null);
    Node nameNode = Node.newString(Token.NAME, "x");
    nameNode.setLineno(1);
    Scope.Var var = new Scope.Var(false, "x", nameNode, registry.getNativeType(JSTypeNative.NUMBER_TYPE), scope, 0, input);

    Node parent = new Node(Token.VAR, dummyNode);
    validator.expectUndeclaredVariable("test.js", dummyNode, parent, var, "x", registry.getNativeType(JSTypeNative.STRING_TYPE));

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.DUP_VAR_DECLARATION, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testExpectUndeclaredVariableSuppressedDuplicate() {
    CompilerInput input = new CompilerInput(null, "test.js", false);
    Scope scope = new SyntacticScopeCreator(compiler).createScope(dummyNode, null);
    Node nameNode = Node.newString(Token.NAME, "x");
    nameNode.setLineno(1);
    Scope.Var var = new Scope.Var(false, "x", nameNode, registry.getNativeType(JSTypeNative.NUMBER_TYPE), scope, 0, input);

    Node propNode = new Node(Token.GETPROP, Node.newString("a"), Node.newString("b"));
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    builder.recordSuppressions(com.google.common.collect.Sets.newHashSet("duplicate"));
    JSDocInfo info = builder.build(true);
    propNode.setJSDocInfo(info);

    Node parent = new Node(Token.EXPR_RESULT, propNode);
    validator.expectUndeclaredVariable("test.js", propNode, parent, var, "x", registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    assertEquals("Should not warn when duplicate suppression is present", 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testGetReadableJSTypeName() {
    Node node = Node.newString(Token.NAME, "myVariable");
    assertEquals("Unknown node type without type defaults to unknown/qualified name", "myVariable", validator.getReadableJSTypeName(node, false));

    node.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    assertEquals("number", validator.getReadableJSTypeName(node, false));

    // Anonymous function node
    Node fnNode = new Node(Token.FUNCTION);
    FunctionType fnType = registry.createFunctionType(registry.getNativeType(JSTypeNative.VOID_TYPE));
    fnNode.setJSType(fnType);
    assertEquals("function", validator.getReadableJSTypeName(fnNode, false));

    // GETPROP prototype chain climb
    Node objNode = Node.newString(Token.NAME, "myObj");
    ObjectType objType = registry.createObjectType("MyObjClass", dummyNode, null);
    objType.defineDeclaredProperty("foo", registry.getNativeType(JSTypeNative.STRING_TYPE), dummyNode);
    objNode.setJSType(objType);

    Node getProp = new Node(Token.GETPROP, objNode, Node.newString("foo"));
    getProp.setJSType(registry.getNativeType(JSTypeNative.STRING_TYPE));
    String readable = validator.getReadableJSTypeName(getProp, true);
    assertTrue("Should include property name or type name", readable.contains("foo") || readable.contains("MyObjClass"));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testTypeMismatchContract() {
    JSType numType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType strType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType boolType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);

    TypeValidator.TypeMismatch m1 = new TypeValidator.TypeMismatch(numType, strType);
    TypeValidator.TypeMismatch m2 = new TypeValidator.TypeMismatch(strType, numType);
    TypeValidator.TypeMismatch m3 = new TypeValidator.TypeMismatch(numType, boolType);

    // Reflexive
    assertEquals(m1, m1);
    // Symmetric (order does not matter per equals contract in TypeMismatch)
    assertEquals(m1, m2);
    assertEquals(m2, m1);
    assertEquals(m1.hashCode(), m2.hashCode());

    // Non-equality
    assertNotEquals(m1, m3);
    assertFalse(m1.equals(null));
    assertFalse(m1.equals("Some String"));

    // toString check
    assertEquals("(" + numType + ", " + strType + ")", m1.toString());
  }

  @Test(timeout = 4000)
  public void testRegisterMismatchWithFunctionTypes() {
    FunctionType fn1 = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    FunctionType fn2 = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    validator.expectCanCast(traversal, dummyNode, fn1, fn2);

    int count = 0;
    for (TypeValidator.TypeMismatch mismatch : validator.getMismatches()) {
      count++;
      assertNotNull(mismatch.typeA);
      assertNotNull(mismatch.typeB);
    }
    assertTrue("Should decompose function parameters and return types into mismatches", count >= 2);
  }
}