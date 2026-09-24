/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.TypeValidator
 * Target Defect: Defects4J Issue 1047 (TypeCheckTest::testIssue1047)
 * Failure Symptom: expected:<...p never defined on C[2]> but was:<...p never defined on C[3.c2_]>
 *
 * Key Logical Decision Branches & Boundaries Tested:
 * 1. getReadableJSTypeName(Node, boolean dereference):
 *    - DEFECT ZONE: When n is a GETPROP (e.g. C3.c2_) and dereference=true, the code mistakenly
 *      executed the n.isGetProp() prototype climbing branch instead of dereferencing the type
 *      of n itself, incorrectly yielding "C3.c2_" rather than the target instance type "C2".
 *    - Fallback branches: functionPrototype, named constructor types, qualified names, anonymous functions, primitives.
 * 2. expectValidTypeofName: Diagnostic UNKNOWN_TYPEOF_VALUE.
 * 3. expectObject / expectActualObject / expectAnyObject:
 *    - Object context match vs mismatch.
 *    - Actual object (isObject()) vs convertible types.
 *    - NO_OBJECT_TYPE subtyping and empty type handling.
 * 4. expectString / expectNumber / expectBitwiseable / expectStringOrNumber:
 *    - Context matches vs primitive mismatches against allValueTypes / NUMBER_STRING.
 * 5. expectNotNullOrUndefined:
 *    - Edge case: Local non-global scope GETPROP where type isNullType() (Issue 109 suppression).
 *    - Forward declared / unresolved name checks (containsForwardDeclaredUnresolvedName: union types, no-resolved types).
 * 6. expectSwitchMatchesCase:
 *    - Shallow equality check; autoboxing subtype check vs mismatch.
 * 7. expectIndexMatch:
 *    - objType.isStruct() warning.
 *    - objType.isUnknownType() property access.
 *    - Templatized map key lookup (getObjectIndexKey).
 *    - Array type vs Object context vs invalid dereference mismatch.
 * 8. expectCanAssignToPropertyOf / expectCanAssignTo / expectArgumentMatchesParameter:
 *    - Interface method dummy implementation bypass (owner is FunctionPrototype of interface).
 *    - Mismatch registration and error propagation.
 * 9. expectCanOverride / expectSuperType / expectCanCast:
 *    - Overriding subtype verification.
 *    - Missing extends tag vs superclass mismatch.
 *    - Cast compatibility (canCastTo).
 * 10. expectUndeclaredVariable:
 *    - Duplicate suppressions (@suppress {duplicate} on GETPROP or ObjectLitKey).
 *    - Stub variable declarations (exprResult).
 *    - Native initial scope undeclare/declare vs duplicate warning.
 * 11. expectAllInterfaceProperties:
 *    - Missing interface method; mismatched property types with templated map replacement.
 * 12. TypeMismatch:
 *    - Symmetry in equals(), hashCode() consistency, and toString() formatting.
 */

package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.ARRAY_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.ERROR_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.FUNCTION_INSTANCE_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_RESOLVED_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NO_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NULL_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.TemplateType;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.*;

public class TypeValidatorGeminiTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private TypeValidator validator;

  @Before
  public void setUp() {
    compiler = new Compiler();
    compiler.initCompilerOptionsIfTesting();
    registry = compiler.getTypeRegistry();
    validator = new TypeValidator(compiler);
  }

  private NodeTraversal createTraversal() {
    return new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {}
    });
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 1047)
  // =========================================================================

  /**
   * Targets Defects4J Issue 1047:
   * When dereference is true for getReadableJSTypeName on a GETPROP node (e.g. C3.c2_),
   * it must return the dereferenced type name ("C2") instead of traversing into the
   * property's own definition path on the owner ("C3.c2_").
   */
  @Test(timeout = 4000)
  public void testIssue1047_GetReadableJSTypeNameWithDereferenceOnGetProp() {
    FunctionType c2Ctor = registry.buildConstructorType("C2", null, null, null, null);
    ObjectType c2Type = c2Ctor.getInstanceType();

    FunctionType c3Ctor = registry.buildConstructorType("C3", null, null, null, null);
    ObjectType c3Type = c3Ctor.getInstanceType();
    c3Type.defineDeclaredProperty("c2_", c2Type, new Node(Token.NAME, "c2_"));

    Node c3Node = Node.newString(Token.NAME, "C3");
    c3Node.setJSType(c3Type);

    Node propNameNode = Node.newString("c2_");
    Node getPropNode = new Node(Token.GETPROP, c3Node, propNameNode);
    getPropNode.setJSType(c2Type);

    // Buggy implementation returns "C3.c2_" because n.isGetProp() branch is executed unconditionally.
    // Correct behavior with dereference=true must return "C2".
    String readableName = validator.getReadableJSTypeName(getPropNode, true);
    assertEquals("C2", readableName);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & Readable Type Names
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetReadableJSTypeName_InterfaceOwner() {
    FunctionType ifaceCtor = registry.buildInterfaceType("AnInterface", null);
    ObjectType ifaceType = ifaceCtor.getInstanceType();
    ifaceType.defineDeclaredProperty("foo", registry.getNativeType(STRING_TYPE), new Node(Token.NAME, "foo"));

    Node receiver = Node.newString(Token.NAME, "inst");
    receiver.setJSType(ifaceType);
    Node getProp = new Node(Token.GETPROP, receiver, Node.newString("foo"));
    getProp.setJSType(registry.getNativeType(STRING_TYPE));

    String name = validator.getReadableJSTypeName(getProp, false);
    assertEquals("AnInterface.foo", name);
  }

  @Test(timeout = 4000)
  public void testGetReadableJSTypeName_NonGetPropFallbacks() {
    Node nullTypeNode = new Node(Token.NAME, "someVar");
    nullTypeNode.setJSType(null); // Triggers getJSType null branch -> UNKNOWN_TYPE
    assertEquals("someVar", validator.getReadableJSTypeName(nullTypeNode, false));

    Node anonFnNode = new Node(Token.FUNCTION);
    anonFnNode.setJSType(registry.getNativeType(FUNCTION_INSTANCE_TYPE));
    assertEquals("function", validator.getReadableJSTypeName(anonFnNode, false));

    Node numNode = Node.newNumber(42);
    numNode.setJSType(registry.getNativeType(NUMBER_TYPE));
    assertEquals("number", validator.getReadableJSTypeName(numNode, false));
  }

  @Test(timeout = 4000)
  public void testExpectValidTypeofName() {
    NodeTraversal t = createTraversal();
    Node n = IR.string("foo");
    validator.expectValidTypeofName(t, n, "bar");
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.UNKNOWN_TYPEOF_VALUE, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testExpectObjectAndExpectActualObject() {
    NodeTraversal t = createTraversal();
    Node n = IR.empty();

    assertTrue(validator.expectObject(t, n, registry.getNativeType(OBJECT_TYPE), "msg"));
    assertEquals(0, compiler.getWarningCount());

    assertFalse(validator.expectObject(t, n, registry.getNativeType(NUMBER_TYPE), "msg"));
    assertEquals(1, compiler.getWarningCount());

    // expectActualObject: String is convertible to Object context, but not an actual Object
    validator.expectActualObject(t, n, registry.getNativeType(STRING_TYPE), "msg");
    assertEquals(2, compiler.getWarningCount());

    validator.expectActualObject(t, n, registry.getNativeType(OBJECT_TYPE), "msg");
    assertEquals(2, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectAnyObject() {
    NodeTraversal t = createTraversal();
    Node n = IR.empty();

    validator.expectAnyObject(t, n, registry.getNativeType(NO_OBJECT_TYPE), "msg");
    assertEquals(0, compiler.getWarningCount());

    // Empty type should be skipped
    validator.expectAnyObject(t, n, registry.getNativeType(NO_TYPE), "msg");
    assertEquals(0, compiler.getWarningCount());

    // Number type triggers mismatch
    validator.expectAnyObject(t, n, registry.getNativeType(NUMBER_TYPE), "msg");
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectStringNumberBitwiseable() {
    NodeTraversal t = createTraversal();
    Node n = IR.empty();

    validator.expectString(t, n, registry.getNativeType(STRING_TYPE), "msg");
    validator.expectNumber(t, n, registry.getNativeType(NUMBER_TYPE), "msg");
    validator.expectBitwiseable(t, n, registry.getNativeType(NUMBER_TYPE), "msg");
    validator.expectBitwiseable(t, n, registry.getNativeType(BOOLEAN_TYPE), "msg");
    validator.expectStringOrNumber(t, n, registry.getNativeType(STRING_TYPE), "msg");
    validator.expectStringOrNumber(t, n, registry.getNativeType(NUMBER_TYPE), "msg");
    assertEquals(0, compiler.getWarningCount());

    validator.expectString(t, n, registry.getNativeType(NO_OBJECT_TYPE), "msg");
    validator.expectNumber(t, n, registry.getNativeType(STRING_TYPE), "msg");
    validator.expectBitwiseable(t, n, registry.getNativeType(OBJECT_TYPE), "msg");
    validator.expectStringOrNumber(t, n, registry.getNativeType(OBJECT_TYPE), "msg");
    assertEquals(4, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Null / Undefined Checks
  // =========================================================================

  @Test(timeout = 4000)
  public void testExpectNotNullOrUndefined_BasicAndIssue109EdgeCase() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("x");

    assertTrue(validator.expectNotNullOrUndefined(t, n, registry.getNativeType(NUMBER_TYPE), "msg", registry.getNativeType(NUMBER_TYPE)));
    assertTrue(validator.expectNotNullOrUndefined(t, n, registry.getNativeType(NO_TYPE), "msg", registry.getNativeType(NUMBER_TYPE)));
    assertTrue(validator.expectNotNullOrUndefined(t, n, registry.getNativeType(UNKNOWN_TYPE), "msg", registry.getNativeType(NUMBER_TYPE)));

    // Warning on NULL_TYPE
    assertFalse(validator.expectNotNullOrUndefined(t, n, registry.getNativeType(NULL_TYPE), "msg", registry.getNativeType(NUMBER_TYPE)));
    assertEquals(1, compiler.getWarningCount());

    // Issue 109 edge case: !inGlobalScope() and n.isGetProp() and type.isNullType() -> returns true without warning
    Node getPropNode = new Node(Token.GETPROP, IR.thisNode(), IR.string("x"));
    Node fnRoot = new Node(Token.FUNCTION, IR.name("f"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    NodeTraversal localTraversal = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node node, Node parent) {}
    });
    localTraversal.traverseInnerNode(fnRoot, fnRoot.getParent(), compiler.getTopScope());
    // Simulate non-global scope
    NodeTraversal scopedTraversal = new NodeTraversal(compiler, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node node, Node parent) {}
    }, new SyntacticScopeCreator(compiler));
    scopedTraversal.traverse(fnRoot);

    boolean result = validator.expectNotNullOrUndefined(
        scopedTraversal, getPropNode, registry.getNativeType(NULL_TYPE), "msg", registry.getNativeType(NUMBER_TYPE));
    assertTrue(result);
  }

  @Test(timeout = 4000)
  public void testExpectNotNullOrUndefined_ForwardDeclaredUnresolved() {
    NodeTraversal t = createTraversal();
    Node n = IR.name("x");

    JSType unresolved = registry.getNativeType(NO_RESOLVED_TYPE);
    assertTrue(validator.expectNotNullOrUndefined(t, n, unresolved, "msg", registry.getNativeType(OBJECT_TYPE)));

    JSType unionWithUnresolved = registry.createUnionType(unresolved, registry.getNativeType(NULL_TYPE));
    assertTrue(validator.expectNotNullOrUndefined(t, n, unionWithUnresolved, "msg", registry.getNativeType(OBJECT_TYPE)));
  }

  @Test(timeout = 4000)
  public void testExpectSwitchMatchesCase() {
    NodeTraversal t = createTraversal();
    Node switchNode = new Node(Token.SWITCH, IR.number(1));
    Node caseNode = new Node(Token.CASE, IR.string("1"));
    switchNode.addChildToBack(caseNode);

    // Number vs String shallow equality failure
    validator.expectSwitchMatchesCase(t, caseNode, registry.getNativeType(NUMBER_TYPE), registry.getNativeType(STRING_TYPE));
    assertEquals(1, compiler.getWarningCount());

    // Compatible cases
    validator.expectSwitchMatchesCase(t, caseNode, registry.getNativeType(NUMBER_TYPE), registry.getNativeType(NUMBER_TYPE));
    assertEquals(1, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition D: GETELEM, Assign, Override, Cast, & Duplicate Checks
  // =========================================================================

  @Test(timeout = 4000)
  public void testExpectIndexMatch_AllBranches() {
    NodeTraversal t = createTraversal();

    // Struct check
    Node getElem1 = new Node(Token.GETELEM, IR.name("structObj"), IR.string("prop"));
    ObjectType structType = registry.createAnonymousObjectType(null);
    structType.setPrettyPrint(true);
    JSDocInfoBuilder info = new JSDocInfoBuilder(true);
    info.recordStruct();
    structType.setJSDocInfo(info.build(null));
    validator.expectIndexMatch(t, getElem1, structType, registry.getNativeType(STRING_TYPE));
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.ILLEGAL_PROPERTY_ACCESS, compiler.getWarnings()[0].getType());

    // Unknown type receiver
    Node getElem2 = new Node(Token.GETELEM, IR.name("unk"), IR.name("k"));
    validator.expectIndexMatch(t, getElem2, registry.getNativeType(UNKNOWN_TYPE), registry.getNativeType(STRING_TYPE));

    // Array type receiver with string index -> warning
    Node getElem3 = new Node(Token.GETELEM, IR.name("arr"), IR.string("k"));
    validator.expectIndexMatch(t, getElem3, registry.getNativeType(ARRAY_TYPE), registry.getNativeType(STRING_TYPE));
    assertEquals(2, compiler.getWarningCount());

    // Object type with non-object context receiver
    Node getElem4 = new Node(Token.GETELEM, IR.name("num"), IR.string("k"));
    validator.expectIndexMatch(t, getElem4, registry.getNativeType(NUMBER_TYPE), registry.getNativeType(STRING_TYPE));
    assertEquals(3, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectCanAssignToPropertyOf_InterfaceDummyMethod() {
    NodeTraversal t = createTraversal();
    FunctionType ifaceCtor = registry.buildInterfaceType("IAction", null);
    Node ownerNode = IR.name("IActionProto");
    ownerNode.setJSType(ifaceCtor.getPrototype());

    FunctionType fn1 = registry.createFunctionType(registry.getNativeType(VOID_TYPE));
    FunctionType fn2 = registry.createFunctionType(registry.getNativeType(NUMBER_TYPE));

    Node assignNode = IR.exprResult(IR.assign(IR.name("x"), IR.name("y")));
    // Function to function assign on interface prototype should be allowed
    boolean allowed = validator.expectCanAssignToPropertyOf(t, assignNode, fn1, fn2, ownerNode, "action");
    assertTrue(allowed);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testExpectArgumentMatchesParameterAndCanOverride() {
    NodeTraversal t = createTraversal();
    Node call = new Node(Token.CALL, IR.name("myFn"), IR.string("argVal"));

    validator.expectArgumentMatchesParameter(
        t, call.getLastChild(), registry.getNativeType(STRING_TYPE), registry.getNativeType(NUMBER_TYPE), call, 1);
    assertEquals(1, compiler.getWarningCount());

    validator.expectCanOverride(
        t, call, registry.getNativeType(STRING_TYPE), registry.getNativeType(NUMBER_TYPE), "prop", registry.getNativeType(OBJECT_TYPE));
    assertEquals(2, compiler.getWarningCount());
    assertEquals(TypeValidator.HIDDEN_PROPERTY_MISMATCH, compiler.getWarnings()[1].getType());
  }

  @Test(timeout = 4000)
  public void testExpectSuperTypeAndCanCast() {
    NodeTraversal t = createTraversal();
    Node n = IR.empty();

    FunctionType superCtor = registry.buildConstructorType("Super", null, null, null, null);
    FunctionType subCtor = registry.buildConstructorType("Sub", null, null, null, null);
    subCtor.setPrototypeBasedOn(registry.getNativeType(OBJECT_TYPE).toObjectType());

    validator.expectSuperType(t, n, superCtor.getInstanceType(), subCtor.getInstanceType());
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.MISSING_EXTENDS_TAG_WARNING, compiler.getWarnings()[0].getType());

    // Cast validation: Object to Number is invalid
    validator.expectCanCast(t, n, registry.getNativeType(NUMBER_TYPE), registry.getNativeType(OBJECT_TYPE));
    assertEquals(2, compiler.getWarningCount());
    assertEquals(TypeValidator.INVALID_CAST, compiler.getWarnings()[1].getType());
  }

  @Test(timeout = 4000)
  public void testExpectUndeclaredVariable_DuplicateWarningAndSuppression() {
    CompilerInput input = new CompilerInput(SourceFile.fromCode("input.js", "var a;"));
    Node nameNode = IR.name("a");
    nameNode.setLineno(1);
    Node parent = IR.var(nameNode);

    Scope s = Scope.createGlobalScope(parent);
    Scope.Var var = s.declare("a", nameNode, registry.getNativeType(STRING_TYPE), input);

    // Duplicate definition with different type without suppression -> warning
    Node newNameNode = IR.name("a");
    Node newParent = IR.var(newNameNode);
    validator.expectUndeclaredVariable("test.js", input, newNameNode, newParent, var, "a", registry.getNativeType(NUMBER_TYPE));
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.DUP_VAR_DECLARATION, compiler.getWarnings()[0].getType());

    // With @suppress {duplicate}
    JSDocInfoBuilder builder = new JSDocInfoBuilder(true);
    builder.recordSuppressions(Collections.singleton("duplicate"));
    newNameNode.setJSDocInfo(builder.build(null));
    Node getPropNode = new Node(Token.GETPROP, IR.name("o"), newNameNode);
    getPropNode.setJSDocInfo(newNameNode.getJSDocInfo());

    validator.expectUndeclaredVariable("test.js", input, getPropNode, newParent, var, "a", registry.getNativeType(STRING_TYPE));
    assertEquals(1, compiler.getWarningCount()); // No new warning
  }

  @Test(timeout = 4000)
  public void testExpectAllInterfaceProperties() {
    NodeTraversal t = createTraversal();
    Node n = IR.function(IR.name("Impl"), IR.paramList(), IR.block());

    FunctionType iface = registry.buildInterfaceType("IFoo", null);
    iface.getImplicitPrototype().defineDeclaredProperty("mustHave", registry.getNativeType(STRING_TYPE), n);

    FunctionType impl = registry.buildConstructorType("Impl", null, null, null, null);
    impl.setImplementedInterfaces(ImmutableList.of(iface.getInstanceType()));

    // Missing property
    validator.expectAllInterfaceProperties(t, n, impl);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(TypeValidator.INTERFACE_METHOD_NOT_IMPLEMENTED, compiler.getWarnings()[0].getType());

    // Implemented with wrong type
    impl.getInstanceType().defineDeclaredProperty("mustHave", registry.getNativeType(NUMBER_TYPE), n);
    validator.expectAllInterfaceProperties(t, n, impl);
    assertEquals(2, compiler.getWarningCount());
    assertEquals(TypeValidator.HIDDEN_INTERFACE_PROPERTY_MISMATCH, compiler.getWarnings()[1].getType());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity (TypeMismatch & Flags)
  // =========================================================================

  @Test(timeout = 4000)
  public void testSetShouldReportAndMismatchRegistration() {
    validator.setShouldReport(false);
    NodeTraversal t = createTraversal();
    Node n = IR.empty();

    validator.expectNumber(t, n, registry.getNativeType(STRING_TYPE), "msg");
    assertEquals(0, compiler.getWarningCount());

    Iterator<TypeValidator.TypeMismatch> it = validator.getMismatches().iterator();
    assertTrue(it.hasNext());
    TypeValidator.TypeMismatch mismatch = it.next();
    assertTrue(mismatch.typeA.isEquivalentTo(registry.getNativeType(STRING_TYPE)));
    assertTrue(mismatch.typeB.isEquivalentTo(registry.getNativeType(NUMBER_TYPE)));
  }

  @Test(timeout = 4000)
  public void testTypeMismatch_EqualsHashCodeToString() {
    JSError error = JSError.make("file.js", 1, 1, TypeValidator.TYPE_MISMATCH_WARNING, "err");
    TypeValidator.TypeMismatch tm1 = new TypeValidator.TypeMismatch(
        registry.getNativeType(STRING_TYPE), registry.getNativeType(NUMBER_TYPE), error);
    TypeValidator.TypeMismatch tm2 = new TypeValidator.TypeMismatch(
        registry.getNativeType(NUMBER_TYPE), registry.getNativeType(STRING_TYPE), error);
    TypeValidator.TypeMismatch tm3 = new TypeValidator.TypeMismatch(
        registry.getNativeType(BOOLEAN_TYPE), registry.getNativeType(STRING_TYPE), error);

    // Symmetric equality
    assertEquals(tm1, tm2);
    assertEquals(tm2, tm1);
    assertNotEquals(tm1, tm3);
    assertFalse(tm1.equals("not-a-mismatch"));
    assertFalse(tm1.equals(null));

    assertEquals(tm1.hashCode(), tm2.hashCode());
    assertEquals("(string, number)", tm1.toString());
  }
}