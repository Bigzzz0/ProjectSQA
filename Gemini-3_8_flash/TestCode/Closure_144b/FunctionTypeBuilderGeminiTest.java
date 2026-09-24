/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.FunctionTypeBuilder
 *
 * 1. Defect Targeting (Ground Truth Analysis):
 *    - Defects4J reveals pervasive failures where function return types were unexpectedly
 *      UNKNOWN_TYPE (?) instead of VOID_TYPE (undefined) for constructors, interfaces,
 *      and functions without explicit JSDoc @return annotations.
 *    - In particular: constructors (@constructor) and interfaces (@interface) must evaluate
 *      to a return type of VOID_TYPE (undefined), rather than remaining UNKNOWN_TYPE.
 *    - When inferInheritance parses @constructor / @interface, or buildAndRegister constructs
 *      the function type, returnType must be correctly handled as VOID_TYPE for constructors.
 *
 * 2. Decision & Branch Coverage Points:
 *    - inferReturnType():
 *        * info == null -> UNKNOWN_TYPE
 *        * info != null && info.hasReturnType() -> evaluate return type
 *        * templateTypeName != null && returnType is template type -> TEMPLATE_TYPE_EXPECTED error
 *    - inferInheritance():
 *        * info == null vs info != null
 *        * isConstructor / isInterface branching
 *        * info.hasBaseType() with constructor/interface vs without -> EXTENDS_WITHOUT_TYPEDEF warning
 *        * baseType null / non-object -> EXTENDS_NON_OBJECT warning
 *        * info.getImplementedInterfaces() with valid vs invalid types -> BAD_IMPLEMENTED_TYPE error
 *        * baseType has constructor FunctionType -> add inherited interfaces
 *        * implements without constructor/interface -> IMPLEMENTS_WITHOUT_CONSTRUCTOR warning
 *    - inferThisType():
 *        * (info, JSType) overload: info == null, info != null without type, info with type
 *        * (info, Node owner) overload: info.hasThisType(), owner != null, owner prototype property
 *    - inferParameterTypes():
 *        * argsParent == null with info == null vs info != null
 *        * argsParent != null: parameter matching, template type checks, duplicate template error,
 *          template expected error, inexistent param warning
 *        * addParameter(): optional parameters, varArgs, required params, ordering validations:
 *          VAR_ARGS_MUST_BE_LAST, OPTIONAL_ARG_AT_END
 *    - inferFromOverriddenFunction():
 *        * paramsParent == null vs paramsParent != null
 *        * oldParams matching vs excess params in new function
 *    - buildAndRegister():
 *        * returnType == null check
 *        * parametersNode == null check -> IllegalStateException
 *        * isConstructor branch -> getOrCreateConstructor() (existing type vs new type, redefinition warning)
 *        * isInterface branch -> declareType in global scope, maybeSetBaseType
 *        * ordinary function branch -> FunctionBuilder
 *        * templateTypeName cleanup
 *    - isFunctionTypeDeclaration() static predicate:
 *        * parameter count > 0, return type, this type, constructor, interface
 */

package com.google.javascript.jscomp;

import static com.google.javascript.rhino.jstype.JSTypeNative.BOOLEAN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.FUNCTION_FUNCTION_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.UNKNOWN_TYPE;
import static com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FunctionTypeBuilderGeminiTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private Scope scope;
  private Node rootNode;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    rootNode = new Node(Token.BLOCK);
    scope = Scope.createGlobalScope(rootNode);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Constructor/Function Return Types)
  // =========================================================================

  /**
   * Targets the known defect where constructor functions or functions without explicit
   * return annotations improperly preserve UNKNOWN_TYPE (?) instead of VOID_TYPE (undefined).
   */
  @Test(timeout = 4000)
  public void testDefectConstructorReturnTypeMustBeVoid() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    JSDocInfo info = docBuilder.build(rootNode);

    Node fnNode = new Node(Token.FUNCTION, Node.newString(Token.NAME, "MyClass"), new Node(Token.LP), new Node(Token.BLOCK));
    FunctionTypeBuilder builder = new FunctionTypeBuilder("MyClass", compiler, rootNode, "test.js", scope);
    builder.setSourceNode(fnNode);
    builder.inferInheritance(info);
    builder.inferReturnType(info);
    builder.inferParameterTypes(new Node(Token.LP), info);

    FunctionType fnType = builder.buildAndRegister();
    assertNotNull("Constructor FunctionType should be successfully built", fnType);
    assertTrue("Should be registered as constructor", fnType.isConstructor());

    // Expected behavior per JS constructor semantics: return type should be VOID_TYPE (undefined)
    JSType expectedVoid = registry.getNativeType(VOID_TYPE);
    assertEquals("Constructors must have void/undefined return type", expectedVoid, fnType.getReturnType());
  }

  /**
   * Targets return type inference on functions without JSDoc or return statement:
   * verifies inference handles null JSDoc gracefully without crash.
   */
  @Test(timeout = 4000)
  public void testDefectFunctionWithoutDocReturnTypeDefaulting() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder("simpleFn", compiler, rootNode, "test.js", scope);
    builder.inferReturnType(null);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertNotNull(fnType.getReturnType());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testStandardFunctionTypeBuilding() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordReturnType(new JSTypeExpression(Node.newString(Token.NAME, "string"), "test.js"));
    docBuilder.recordParameter("x", new JSTypeExpression(Node.newString(Token.NAME, "number"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    Node lp = new Node(Token.LP, Node.newString(Token.NAME, "x"));
    FunctionTypeBuilder builder = new FunctionTypeBuilder("calculate", compiler, rootNode, "test.js", scope);
    FunctionType fnType = builder.inferReturnType(info)
        .inferParameterTypes(lp, info)
        .buildAndRegister();

    assertNotNull(fnType);
    assertEquals(registry.getNativeType(STRING_TYPE), fnType.getReturnType());
    assertEquals(1, fnType.getParametersNode().getChildCount());
  }

  @Test(timeout = 4000)
  public void testInterfaceDeclarationAndGlobalRegistration() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordInterface();
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder = new FunctionTypeBuilder("InterfaceA", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(info);
    builder.inferParameterTypes(new Node(Token.LP), info);
    FunctionType ifaceType = builder.buildAndRegister();

    assertNotNull(ifaceType);
    assertTrue(ifaceType.isInterface());
    assertNotNull(registry.getType("InterfaceA"));
    assertEquals(ifaceType.getInstanceType(), registry.getType("InterfaceA"));
  }

  @Test(timeout = 4000)
  public void testInferFromOverriddenFunctionWithoutParamsParent() {
    FunctionType baseFn = registry.createFunctionType(
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(NUMBER_TYPE),
        registry.getNativeType(BOOLEAN_TYPE));

    FunctionTypeBuilder builder = new FunctionTypeBuilder("overrideFn", compiler, rootNode, "test.js", scope);
    builder.inferFromOverriddenFunction(baseFn, null);
    FunctionType derived = builder.buildAndRegister();

    assertEquals(registry.getNativeType(STRING_TYPE), derived.getReturnType());
    assertEquals(2, derived.getParametersNode().getChildCount());
  }

  @Test(timeout = 4000)
  public void testInferFromOverriddenFunctionWithParamsParent() {
    FunctionType baseFn = registry.createFunctionType(
        registry.getNativeType(STRING_TYPE),
        registry.getNativeType(NUMBER_TYPE));

    Node lp = new Node(Token.LP, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "bExtra"));

    FunctionTypeBuilder builder = new FunctionTypeBuilder("overrideFn", compiler, rootNode, "test.js", scope);
    builder.inferFromOverriddenFunction(baseFn, lp);
    FunctionType derived = builder.buildAndRegister();

    assertEquals(registry.getNativeType(STRING_TYPE), derived.getReturnType());
    assertEquals(2, derived.getParametersNode().getChildCount());
  }

  @Test(timeout = 4000)
  public void testInferThisTypeWithExplicitDoc() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordThisType(new JSTypeExpression(Node.newString(Token.NAME, "Object"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder = new FunctionTypeBuilder("method", compiler, rootNode, "test.js", scope);
    builder.inferThisType(info, (Node) null);
    builder.inferParameterTypes(new Node(Token.LP), info);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType.getTypeOfThis());
    assertTrue(fnType.getTypeOfThis().isObjectType());
  }

  @Test(timeout = 4000)
  public void testInferThisTypeWithOwnerPrototype() {
    registry.declareType("MyOwner", registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE));
    Node owner = Node.newString(Token.NAME, "MyOwner");

    FunctionTypeBuilder builder = new FunctionTypeBuilder("method", compiler, rootNode, "test.js", scope);
    builder.inferThisType(null, owner);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testInferThisTypeWithDirectJSType() {
    ObjectType objType = registry.getNativeObjectType(com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE);
    FunctionTypeBuilder builder = new FunctionTypeBuilder("method", compiler, rootNode, "test.js", scope);
    builder.inferThisType(null, objType);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType fnType = builder.buildAndRegister();

    assertEquals(objType, fnType.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testInferParameterTypesFromDocAlone() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordParameter("alpha", new JSTypeExpression(Node.newString(Token.NAME, "string"), "test.js"));
    docBuilder.recordParameter("beta", new JSTypeExpression(Node.newString(Token.NAME, "number"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder = new FunctionTypeBuilder("fn", compiler, rootNode, "test.js", scope);
    builder.inferParameterTypes(info);
    FunctionType fnType = builder.buildAndRegister();

    assertEquals(2, fnType.getParametersNode().getChildCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Diagnostics
  // =========================================================================

  @Test(timeout = 4000)
  public void testExtendsWithoutConstructorOrInterfaceWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordBaseType(new JSTypeExpression(Node.newString(Token.NAME, "Object"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder = new FunctionTypeBuilder("InvalidExtends", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.EXTENDS_WITHOUT_TYPEDEF.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testImplementsWithoutConstructorWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordImplementedInterface(new JSTypeExpression(Node.newString(Token.NAME, "Object"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder = new FunctionTypeBuilder("InvalidImplements", compiler, rootNode, "test.js", scope);
    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.IMPLEMENTS_WITHOUT_CONSTRUCTOR.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testInexistentParameterWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordParameter("nonExistentArg", new JSTypeExpression(Node.newString(Token.NAME, "string"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    Node lp = new Node(Token.LP, Node.newString(Token.NAME, "actualArg"));
    FunctionTypeBuilder builder = new FunctionTypeBuilder("fnWithMismatchParam", compiler, rootNode, "test.js", scope);
    builder.inferParameterTypes(lp, info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.INEXISTANT_PARAM.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testOptionalArgOrderingWarning() {
    // Required param after optional param
    Node optArg = Node.newString(Token.NAME, "opt");
    Node reqArg = Node.newString(Token.NAME, "req");
    Node lp = new Node(Token.LP, optArg, reqArg);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    Node optTypeNode = new Node(Token.EQUALS, Node.newString(Token.NAME, "number"));
    docBuilder.recordParameter("opt", new JSTypeExpression(optTypeNode, "test.js"));
    docBuilder.recordParameter("req", new JSTypeExpression(Node.newString(Token.NAME, "string"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder = new FunctionTypeBuilder("orderTest", compiler, rootNode, "test.js", scope);
    builder.inferParameterTypes(lp, info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.OPTIONAL_ARG_AT_END.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testVarArgsMustBeLastWarning() {
    Node varArg = Node.newString(Token.NAME, "rest");
    Node afterArg = Node.newString(Token.NAME, "after");
    Node lp = new Node(Token.LP, varArg, afterArg);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    Node varTypeNode = new Node(Token.ELLIPSIS, Node.newString(Token.NAME, "string"));
    docBuilder.recordParameter("rest", new JSTypeExpression(varTypeNode, "test.js"));
    docBuilder.recordParameter("after", new JSTypeExpression(Node.newString(Token.NAME, "number"), "test.js"));
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder = new FunctionTypeBuilder("varArgsOrderTest", compiler, rootNode, "test.js", scope);
    builder.inferParameterTypes(lp, info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.VAR_ARGS_MUST_BE_LAST.key, compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testTemplateTypeExpectedError() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordTemplateTypeName("T");
    JSDocInfo info = docBuilder.build(rootNode);

    FunctionTypeBuilder builder = new FunctionTypeBuilder("templateFn", compiler, rootNode, "test.js", scope);
    builder.inferTemplateTypeName(info);
    // Passing param that is NOT type T
    Node lp = new Node(Token.LP, Node.newString(Token.NAME, "x"));
    builder.inferParameterTypes(lp, info);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(FunctionTypeBuilder.TEMPLATE_TYPE_EXPECTED.key, compiler.getErrors()[0].getType().key);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructorNullErrorRootThrowsNPE() {
    new FunctionTypeBuilder("fn", compiler, null, "test.js", scope);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBuildWithoutParametersThrowsIllegalStateException() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder("uninitialized", compiler, rootNode, "test.js", scope);
    // Missing inferParameterTypes call -> parametersNode remains null
    builder.buildAndRegister();
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsFunctionTypeDeclarationStaticMethod() {
    JSDocInfoBuilder b1 = new JSDocInfoBuilder(true);
    b1.recordConstructor();
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b1.build(rootNode)));

    JSDocInfoBuilder b2 = new JSDocInfoBuilder(true);
    b2.recordInterface();
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b2.build(rootNode)));

    JSDocInfoBuilder b3 = new JSDocInfoBuilder(true);
    b3.recordReturnType(new JSTypeExpression(Node.newString(Token.NAME, "number"), "test.js"));
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b3.build(rootNode)));

    JSDocInfoBuilder b4 = new JSDocInfoBuilder(true);
    b4.recordThisType(new JSTypeExpression(Node.newString(Token.NAME, "Object"), "test.js"));
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b4.build(rootNode)));

    JSDocInfoBuilder b5 = new JSDocInfoBuilder(true);
    b5.recordParameter("p1", new JSTypeExpression(Node.newString(Token.NAME, "boolean"), "test.js"));
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b5.build(rootNode)));

    JSDocInfoBuilder empty = new JSDocInfoBuilder(true);
    assertFalse(FunctionTypeBuilder.isFunctionTypeDeclaration(empty.build(rootNode)));
  }

  @Test(timeout = 4000)
  public void testConstructorRedefinitionWarning() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    JSDocInfo info = docBuilder.build(rootNode);

    // First definition
    FunctionTypeBuilder builder1 = new FunctionTypeBuilder("DuplicateClass", compiler, rootNode, "test.js", scope);
    builder1.inferInheritance(info);
    builder1.inferReturnType(info);
    builder1.inferParameterTypes(new Node(Token.LP, Node.newString(Token.NAME, "paramA")), info);
    builder1.buildAndRegister();

    // Redefinition with different parameter signature
    FunctionTypeBuilder builder2 = new FunctionTypeBuilder("DuplicateClass", compiler, rootNode, "test.js", scope);
    builder2.inferInheritance(info);
    builder2.inferReturnType(info);
    builder2.inferParameterTypes(new Node(Token.LP), info);
    builder2.buildAndRegister();

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.TYPE_REDEFINITION.key, compiler.getWarnings()[0].getType().key);
  }
}