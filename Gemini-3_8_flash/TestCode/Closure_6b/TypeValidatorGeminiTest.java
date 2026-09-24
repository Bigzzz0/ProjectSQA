package com.google.javascript.jscomp;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: TypeValidator
 *
 * Key Decision Branches & Boundary Conditions Targeted:
 * 1. expectObject / expectActualObject / expectAnyObject:
 *    - Valid ObjectType vs primitive types (Number, String, Boolean).
 *    - Context matching (matchesObjectContext) vs strict isObject().
 *    - NO_OBJECT_TYPE subtyping and empty types.
 * 2. expectNotNullOrUndefined:
 *    - null type, void (undefined) type, union types.
 *    - Issue 109 edge case: n.isGetProp() in non-global scope when type isNullType().
 *    - containsForwardDeclaredUnresolvedName: union types containing unresolved names.
 * 3. expectIndexMatch:
 *    - Precondition check: n.isGetElem() enforcement.
 *    - Struct types (ILLEGAL_PROPERTY_ACCESS).
 *    - UnknownType index target -> expectStringOrNumber.
 *    - Dereferenced types with custom IndexType, ArrayType, ObjectType, and non-dereferenceable types.
 * 4. expectSwitchMatchesCase:
 *    - Shallow equality testable vs autoboxing matches vs mismatched types.
 * 5. expectCanAssignTo & expectCanAssignToPropertyOf:
 *    - Compatible assignments vs incompatible assignments.
 *    - Interface dummy implementations on function prototypes.
 *    - [KNOWN DEFECT Issue 635 / testIssue635b / testTypeRedefinition]:
 *      Constructor and EnumType assignment suppression vs mismatch reporting.
 * 6. expectSuperType:
 *    - Missing extends tag warning (extends Object implicit).
 *    - Incompatible superclass declaration mismatch warning.
 *    - Constructor prototype update when not cached.
 * 7. expectCanCast:
 *    - Valid upcast / downcast vs invalid cross-type cast.
 * 8. expectUndeclaredVariable:
 *    - Native var redeclaration (input == null).
 *    - Duplicate declaration suppression with @suppress {duplicate} or ExprResult parent.
 *    - Duplicate declaration warning (DUP_VAR_DECLARATION) with mismatched types.
 * 9. expectAllInterfaceProperties & expectInterfaceProperty:
 *    - Unimplemented interface property (INTERFACE_METHOD_NOT_IMPLEMENTED).
 *    - Mismatched interface property signature (HIDDEN_INTERFACE_PROPERTY_MISMATCH).
 * 10. getReadableJSTypeName:
 *    - Qualified names, GETPROP traversal through prototype hierarchies, interfaces, functions.
 * 11. TypeMismatch equality & contract integrity:
 *    - Symmetric equals, hashCode, and toString representation.
 */

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.IR;
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

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    validator = new TypeValidator(compiler);
    traversal = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    });
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testExpectObjectSuccessAndFailure() {
    Node node = IR.name("testNode");
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    assertTrue(validator.expectObject(traversal, node, objectType, "expected object"));
    assertEquals(0, compiler.getWarningCount());

    assertFalse(validator.expectObject(traversal, node, numberType, "expected object"));
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.TYPE_MISMATCH_WARNING.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testExpectActualObject() {
    Node node = IR.name("actualObj");
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    validator.expectActualObject(traversal, node, objectType, "need actual object");
    assertEquals(0, compiler.getWarningCount());

    validator.expectActualObject(traversal, node, stringType, "need actual object");
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectAnyObject() {
    Node node = IR.name("anyObj");
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);
    JSType emptyType = registry.getNativeType(JSTypeNative.NO_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    validator.expectAnyObject(traversal, node, objectType, "any obj");
    validator.expectAnyObject(traversal, node, emptyType, "empty is allowed");
    assertEquals(0, compiler.getWarningCount());

    validator.expectAnyObject(traversal, node, numberType, "number not allowed");
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectStringAndExpectNumber() {
    Node node = IR.name("primitiveNode");
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);

    validator.expectString(traversal, node, stringType, "need str");
    assertEquals(0, compiler.getWarningCount());
    validator.expectString(traversal, node, objectType, "need str");
    assertEquals(1, compiler.getWarningCount());

    validator.expectNumber(traversal, node, numberType, "need num");
    assertEquals(1, compiler.getWarningCount());
    validator.expectNumber(traversal, node, stringType, "need num");
    assertEquals(2, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectBitwiseableAndStringOrNumber() {
    Node node = IR.name("bitNode");
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType boolType = registry.getNativeType(JSTypeNative.BOOLEAN_TYPE);
    JSType objectType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);

    validator.expectBitwiseable(traversal, node, numberType, "bitwise");
    validator.expectBitwiseable(traversal, node, boolType, "bitwise");
    assertEquals(0, compiler.getWarningCount());

    validator.expectBitwiseable(traversal, node, objectType, "bitwise obj");
    assertEquals(1, compiler.getWarningCount());

    validator.expectStringOrNumber(traversal, node, numberType, "str or num");
    assertEquals(1, compiler.getWarningCount());

    validator.expectStringOrNumber(traversal, node, objectType, "str or num obj");
    assertEquals(2, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectNotNullOrUndefined() {
    Node node = IR.name("notNullNode");
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    JSType voidType = registry.getNativeType(JSTypeNative.VOID_TYPE);

    assertTrue(validator.expectNotNullOrUndefined(traversal, node, stringType, "msg", stringType));
    assertEquals(0, compiler.getWarningCount());

    assertFalse(validator.expectNotNullOrUndefined(traversal, node, nullType, "null error", stringType));
    assertEquals(1, compiler.getWarningCount());

    assertFalse(validator.expectNotNullOrUndefined(traversal, node, voidType, "void error", stringType));
    assertEquals(2, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectNotNullOrUndefinedIssue109EdgeCase() {
    // In a non-global scope with a GETPROP node and null type, should return true without reporting
    Node getPropNode = IR.getprop(IR.thisNode(), IR.string("x"));
    getPropNode.setJSType(registry.getNativeType(JSTypeNative.NULL_TYPE));

    Node root = IR.root();
    Node script = IR.script();
    Node function = IR.function(IR.name("fn"), IR.paramList(), IR.block());
    root.addChildToBack(script);
    script.addChildToBack(function);
    function.getLastChild().addChildToBack(getPropNode);

    NodeTraversal nonGlobalTraversal = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    });
    // Traverse into function to ensure not in global scope
    nonGlobalTraversal.traverseInnerNode(function.getLastChild(), function, null);

    boolean result = validator.expectNotNullOrUndefined(
        nonGlobalTraversal, getPropNode, registry.getNativeType(JSTypeNative.NULL_TYPE),
        "msg", registry.getNativeType(JSTypeNative.OBJECT_TYPE));
    assertTrue(result);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectSwitchMatchesCase() {
    Node switchNode = IR.switchNode(IR.name("cond"));
    Node caseNode = IR.caseNode(IR.string("hello"), IR.block());
    switchNode.addChildToBack(caseNode);

    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    validator.expectSwitchMatchesCase(traversal, caseNode, stringType, stringType);
    assertEquals(0, compiler.getWarningCount());

    validator.expectSwitchMatchesCase(traversal, caseNode, numberType, stringType);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectIndexMatchOnVariousTypes() {
    Node getElemNode = IR.getelem(IR.name("arr"), IR.number(0));

    // 1. objType is UnknownType
    validator.expectIndexMatch(traversal, getElemNode,
        registry.getNativeType(JSTypeNative.UNKNOWN_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    assertEquals(0, compiler.getWarningCount());

    // 2. objType is ArrayType -> expects number index
    validator.expectIndexMatch(traversal, getElemNode,
        registry.getNativeType(JSTypeNative.ARRAY_TYPE),
        registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    assertEquals(0, compiler.getWarningCount());

    // Array accessed with non-number
    validator.expectIndexMatch(traversal, getElemNode,
        registry.getNativeType(JSTypeNative.ARRAY_TYPE),
        registry.getNativeType(JSTypeNative.OBJECT_TYPE));
    assertEquals(1, compiler.getWarningCount());

    // 3. objType is Struct -> ILLEGAL_PROPERTY_ACCESS
    ObjectType structType = registry.createRecordTypeBuilder().build();
    Node structAccess = IR.getelem(IR.name("structObj"), IR.string("prop"));
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordStruct();
    structType.setJSDocInfo(docBuilder.build(null));

    validator.expectIndexMatch(traversal, structAccess, structType,
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    assertEquals(2, compiler.getWarningCount());

    // 4. Primitive number accessed with GETELEM
    validator.expectIndexMatch(traversal, getElemNode,
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));
    assertEquals(3, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectCanAssignToPropertyOfInterfaceMethod() {
    FunctionType ifaceCtor = registry.createInterfaceType("MyInterface", null);
    ObjectType ifaceProto = ifaceCtor.getPropertyType("prototype").toObjectType();

    Node ownerNode = IR.name("ifaceProto");
    ownerNode.setJSType(ifaceProto);

    FunctionType fn1 = registry.createFunctionType(registry.getNativeType(JSTypeNative.VOID_TYPE));
    FunctionType fn2 = registry.createFunctionType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    Node assignNode = IR.assign(IR.name("dummy"), IR.name("dummyRhs"));
    // Interface methods should not report mismatch
    boolean matched = validator.expectCanAssignToPropertyOf(
        traversal, assignNode, fn2, fn1, ownerNode, "testMethod");
    assertTrue(matched);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectArgumentMatchesParameter() {
    Node callNode = IR.call(IR.name("targetFn"), IR.name("arg1"));
    Node argNode = callNode.getLastChild();

    validator.expectArgumentMatchesParameter(
        traversal, argNode,
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        callNode, 1);

    assertEquals(1, compiler.getWarningCount());
    assertTrue(compiler.getWarnings()[0].getDescription().contains("actual parameter 1"));
  }

  @Test(timeout = 4000)
  public void testExpectCanOverride() {
    Node node = IR.name("prop");
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSType ownerType = registry.getNativeType(JSTypeNative.OBJECT_TYPE);

    validator.expectCanOverride(traversal, node, numberType, numberType, "foo", ownerType);
    assertEquals(0, compiler.getWarningCount());

    validator.expectCanOverride(traversal, node, stringType, numberType, "foo", ownerType);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.HIDDEN_PROPERTY_MISMATCH.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testExpectCanCast() {
    Node node = IR.name("castNode");
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    // Number and String cannot be cast to each other
    validator.expectCanCast(traversal, node, numberType, stringType);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.INVALID_CAST.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testExpectValidTypeofName() {
    Node node = IR.name("varName");
    validator.expectValidTypeofName(traversal, node, "unknown_ident");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.UNKNOWN_TYPEOF_VALUE.key, compiler.getWarnings()[0].getType().key);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testExpectIndexMatchThrowsOnNonGetElem() {
    Node nameNode = IR.name("notGetElem");
    try {
      validator.expectIndexMatch(traversal, nameNode,
          registry.getNativeType(JSTypeNative.OBJECT_TYPE),
          registry.getNativeType(JSTypeNative.STRING_TYPE));
      fail("Expected IllegalStateException for non-GETELEM node");
    } catch (IllegalStateException expected) {
      // Preconditions verified
    }
  }

  @Test(timeout = 4000)
  public void testSetShouldReportSuppression() {
    validator.setShouldReport(false);
    Node node = IR.name("node");
    validator.expectObject(traversal, node, registry.getNativeType(JSTypeNative.NUMBER_TYPE), "no report");
    // Even though it failed, report was disabled
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectSuperTypeScenarios() {
    Node node = IR.name("subCtor");

    FunctionType superCtor = registry.createConstructorType(
        "SuperClass", null, null, null);
    FunctionType subCtor = registry.createConstructorType(
        "SubClass", null, null, null);

    // SubClass prototype extends Object by default
    subCtor.getPrototype().setImplicitPrototype(registry.getNativeType(JSTypeNative.OBJECT_TYPE).toObjectType());

    // Expecting SuperClass as super type, but declaredSuper is Object -> MISSING_EXTENDS_TAG_WARNING
    validator.expectSuperType(traversal, node, superCtor.getInstanceType(), subCtor.getInstanceType());
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.MISSING_EXTENDS_TAG_WARNING.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testExpectAllInterfacePropertiesMissingAndMismatched() {
    Node node = IR.name("ClassImplementingIface");

    FunctionType iface = registry.createInterfaceType("IAction", null);
    iface.getPrototype().defineDeclaredProperty("act",
        registry.getNativeType(JSTypeNative.NUMBER_TYPE), node);

    FunctionType implCtor = registry.createConstructorType("ActionImpl", null, null, null);
    implCtor.setImplementedInterfaces(ImmutableList.of(iface.getInstanceType()));

    // 1. Property "act" not implemented at all
    validator.expectAllInterfaceProperties(traversal, node, implCtor);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.INTERFACE_METHOD_NOT_IMPLEMENTED.key, compiler.getWarnings()[0].getType().key);

    // 2. Property "act" implemented with incompatible type
    implCtor.getInstanceType().defineDeclaredProperty("act",
        registry.getNativeType(JSTypeNative.STRING_TYPE), node);

    compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    validator = new TypeValidator(compiler);
    traversal = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    });

    validator.expectAllInterfaceProperties(traversal, node, implCtor);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.HIDDEN_INTERFACE_PROPERTY_MISMATCH.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testExpectUndeclaredVariableNativeRedeclaration() {
    Scope globalScope = Scope.createGlobalScope(IR.script());
    Node nameNode = IR.name("Math");
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);

    // native var has input == null
    Scope.Var nativeVar = globalScope.declare("Math", nameNode, numberType, null, false);

    Node varNode = IR.var(nameNode);
    Node assignVal = IR.number(123);
    nameNode.addChildToBack(assignVal);

    CompilerInput input = new CompilerInput(SourceFile.fromCode("test.js", "var Math = 123;"));
    Scope.Var redeclared = validator.expectUndeclaredVariable(
        "test.js", input, nameNode, varNode, nativeVar, "Math", numberType);

    assertNotNull(redeclared);
    assertEquals(0, compiler.getWarningCount());
    assertSame(numberType, nameNode.getJSType());
  }

  @Test(timeout = 4000)
  public void testExpectUndeclaredVariableDuplicateWarning() {
    CompilerInput input = new CompilerInput(SourceFile.fromCode("test.js", "var x;"));
    Scope globalScope = Scope.createGlobalScope(IR.script());
    Node nameNode = IR.name("x");
    nameNode.setLineno(10);
    Node parentNode = IR.var(nameNode);

    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);

    Scope.Var var = globalScope.declare("x", nameNode, numberType, input, false);

    Node newNameNode = IR.name("x");
    Node newParentNode = IR.var(newNameNode);

    validator.expectUndeclaredVariable(
        "test.js", input, newNameNode, newParentNode, var, "x", stringType);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.DUP_VAR_DECLARATION.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testExpectUndeclaredVariableSuppression() {
    CompilerInput input = new CompilerInput(SourceFile.fromCode("test.js", "/** @suppress {duplicate} */ var x;"));
    Scope globalScope = Scope.createGlobalScope(IR.script());
    Node nameNode = IR.name("x");
    Node parentNode = IR.exprResult(nameNode);

    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    Scope.Var var = globalScope.declare("x", nameNode, numberType, input, false);

    Node getPropNode = IR.getprop(IR.name("a"), IR.string("b"));
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordSuppressions(com.google.common.collect.ImmutableSet.of("duplicate"));
    getPropNode.setJSDocInfo(docBuilder.build(null));

    validator.expectUndeclaredVariable(
        "test.js", input, getPropNode, IR.exprResult(getPropNode), var, "a.b", numberType);

    // Suppressed! No warnings should be issued.
    assertEquals(0, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 635 / Defects4J)
  // =========================================================================

  /**
   * Targets the defect where constructor vs enum type mismatches in
   * expectCanAssignTo or expectCanAssignToPropertyOf are suppressed from
   * reporting a warning due to:
   * if ((leftType.isConstructor() || leftType.isEnumType()) && (rightType.isConstructor() || rightType.isEnumType()))
   * which registers mismatch with null error instead of calling mismatch().
   */
  @Test(timeout = 4000)
  public void testDefectConstructorAndEnumTypeMismatchMustReportWarning() {
    EnumType enumType = registry.createEnumType("MyEnum", null, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType ctorType = registry.createConstructorType("MyCtor", null, null, null);

    Node assignNode = IR.assign(IR.name("target"), IR.name("src"));

    // Attempting to assign constructor to enum or vice-versa must produce a mismatch warning.
    boolean canAssign = validator.expectCanAssignTo(
        traversal, assignNode, ctorType, enumType, "incompatible assignment");

    assertFalse("Constructor must not be assignable to Enum", canAssign);
    // In the defective version, registerMismatch(..., null) is called without reporting a warning.
    // The test asserts that a warning MUST be generated for this mismatch!
    assertEquals("A warning must be emitted when assigning constructor to enum",
        1, compiler.getWarningCount());
    assertEquals(TypeValidator.TYPE_MISMATCH_WARNING.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testDefectConstructorAndEnumPropertyAssignmentMustReportWarning() {
    EnumType enumType = registry.createEnumType("MyEnum", null, registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType ctorType = registry.createConstructorType("MyCtor", null, null, null);

    Node ownerNode = IR.name("owner");
    ownerNode.setJSType(registry.getNativeType(JSTypeNative.OBJECT_TYPE));
    Node assignNode = IR.assign(IR.getprop(ownerNode, IR.string("prop")), IR.name("rhs"));

    boolean canAssign = validator.expectCanAssignToPropertyOf(
        traversal, assignNode, ctorType, enumType, ownerNode, "prop");

    assertFalse(canAssign);
    assertEquals("A warning must be emitted when assigning constructor to enum property",
        1, compiler.getWarningCount());
    assertEquals(TypeValidator.TYPE_MISMATCH_WARNING.key, compiler.getWarnings()[0].getType().key);
  }

  // =========================================================================
  // Partition D: Readable Name & Property Traversal Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetReadableJSTypeName() {
    // 1. GETPROP traversal on an object with defined property
    FunctionType ctor = registry.createConstructorType("MyClass", null, null, null);
    ObjectType instanceType = ctor.getInstanceType();
    instanceType.defineDeclaredProperty("field", registry.getNativeType(JSTypeNative.NUMBER_TYPE), null);

    Node owner = IR.name("obj");
    owner.setJSType(instanceType);
    Node getProp = IR.getprop(owner, IR.string("field"));
    getProp.setJSType(registry.getNativeType(JSTypeNative.NUMBER_TYPE));

    String readableName = validator.getReadableJSTypeName(getProp, false);
    assertEquals("MyClass.field", readableName);

    // 2. Simple Function
    FunctionType fnType = registry.createFunctionType(registry.getNativeType(JSTypeNative.VOID_TYPE));
    Node fnNode = IR.name("myFn");
    fnNode.setJSType(fnType);
    assertEquals("function", validator.getReadableJSTypeName(fnNode, false));

    // 3. Node with null JSType defaults to unknown
    Node untypedNode = IR.name("unknownVar");
    assertEquals("?", validator.getReadableJSTypeName(untypedNode, false));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity (TypeMismatch)
  // =========================================================================

  @Test(timeout = 4000)
  public void testTypeMismatchContractAndMismatchesCollection() {
    JSType numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    JSType stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    JSError error = JSError.make("test.js", 1, 1, TypeValidator.TYPE_MISMATCH_WARNING, "detail");

    TypeValidator.TypeMismatch mismatch1 = new TypeValidator.TypeMismatch(numberType, stringType, error);
    TypeValidator.TypeMismatch mismatch2 = new TypeValidator.TypeMismatch(stringType, numberType, error);
    TypeValidator.TypeMismatch mismatch3 = new TypeValidator.TypeMismatch(numberType, numberType, null);

    // Symmetry in equals
    assertEquals(mismatch1, mismatch2);
    assertEquals(mismatch2, mismatch1);
    assertNotEquals(mismatch1, mismatch3);
    assertNotEquals(mismatch1, null);
    assertNotEquals(mismatch1, "not-a-mismatch");

    // hashCode consistency
    assertEquals(mismatch1.hashCode(), new TypeValidator.TypeMismatch(numberType, stringType, null).hashCode());

    // toString check
    assertEquals("(" + numberType + ", " + stringType + ")", mismatch1.toString());

    // Verify mismatches recorded in TypeValidator
    Node node = IR.name("x");
    validator.expectCanAssignTo(traversal, node, stringType, numberType, "err");

    Iterable<TypeValidator.TypeMismatch> mismatches = validator.getMismatches();
    Iterator<TypeValidator.TypeMismatch> it = mismatches.iterator();
    assertTrue(it.hasNext());
    TypeValidator.TypeMismatch recorded = it.next();
    assertTrue(recorded.typeA.isEquivalentTo(stringType) || recorded.typeA.isEquivalentTo(numberType));
  }
}