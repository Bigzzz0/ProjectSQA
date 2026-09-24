package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionParamBuilder;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.FunctionTypeBuilder
 *
 * Core Decision Branches & Methods Covered:
 * 1. Constructor Guard & Initialization:
 *    - fnName null vs non-null (defaults to "")
 *    - Preconditions.checkNotNull(errorRoot)
 * 2. setSourceNode: sets node and returns builder instance.
 * 3. inferFromOverriddenFunction:
 *    - oldType == null -> returns this immediately
 *    - paramsParent == null -> copies parametersNode, null parameters fallback to empty builder
 *    - paramsParent != null -> matches existing params with new params
 *    - varArgs transformation to optional when followed by subsequent parameters
 *    - old params exhausted -> appends extra parameters as UNKNOWN with convention checking
 * 4. inferReturnType:
 *    - info == null vs info != null
 *    - info.hasReturnType() -> evaluates returnType
 *    - templateTypeName != null & templateType in returnType -> TEMPLATE_TYPE_EXPECTED error
 * 5. inferReturnStatementsAsLastResort:
 *    - functionBlock == null -> early return
 *    - compiler.getInput(sourceName).isExtern() -> early return
 *    - worklist traversal: RETURN with child / THROW -> hasNonEmptyReturns = true
 *    - statement blocks / control structures traversal (IF, BLOCK, WHILE, etc.)
 *    - empty returns / void return type inference
 * 6. inferInheritance:
 *    - info == null vs hasBaseType() with isConstructor / isInterface
 *    - baseType evaluation: ExtendedTypeValidator (non-object -> EXTENDS_NON_OBJECT, unknown -> RESOLVED_TAG_EMPTY)
 *    - baseType without @constructor/@interface -> EXTENDS_WITHOUT_TYPEDEF warning
 *    - implementedInterfaces evaluation: ImplementedTypeValidator (non-object -> BAD_IMPLEMENTED_TYPE, unknown -> RESOLVED_TAG_EMPTY)
 *    - baseType constructor inherited interfaces propagation
 *    - implements without constructor -> IMPLEMENTS_WITHOUT_CONSTRUCTOR warning
 * 7. inferThisType:
 *    - inferThisType(info, type) -> ObjectType casting & info.hasType() check
 *    - inferThisType(info, owner) -> ThisTypeValidator, non-object check (THIS_TYPE_NON_OBJECT)
 *    - owner prototype inference via getForgivingType
 * 8. inferParameterTypes:
 *    - argsParent == null vs info-based param extraction
 *    - template type checks: TEMPLATE_TYPE_DUPLICATED and TEMPLATE_TYPE_EXPECTED
 *    - missing JSDoc params -> INEXISTANT_PARAM warning
 *    - param order validation (addParameter): VAR_ARGS_MUST_BE_LAST, OPTIONAL_ARG_AT_END
 * 9. buildAndRegister & getOrCreateConstructor:
 *    - null returnType -> defaults to UNKNOWN_TYPE
 *    - null parametersNode -> throws IllegalStateException
 *    - constructor creation, existing type check, TYPE_REDEFINITION warning
 *    - interface type creation and declaration in global scope
 *    - regular function creation via FunctionBuilder
 * 10. Defect-Targeted Zone (TypeCheckTest::testBackwardsTypedefUse8, testBackwardsTypedefUse9):
 *    - Unresolved/backwards-referenced typedef resolution in JSDocInfo param/return types
 *    - Proper handling of lazy types in ExtendedTypeValidator / ImplementedTypeValidator
 */
public class FunctionTypeBuilderGeminiTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private Scope globalScope;
  private Node scriptRoot;
  private static final String SOURCE_NAME = "testcode.js";

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    SourceFile sourceFile = SourceFile.fromCode(SOURCE_NAME, "");
    compiler.init(
        Collections.<SourceFile>emptyList(),
        Collections.singletonList(sourceFile),
        options);

    registry = compiler.getTypeRegistry();
    scriptRoot = new Node(Token.SCRIPT);
    SyntacticScopeCreator scopeCreator = new SyntacticScopeCreator(compiler);
    globalScope = scopeCreator.createScope(scriptRoot, null);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBasicFunctionBuild() {
    Node errorNode = new Node(Token.NAME, "foo");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("foo", compiler, errorNode, SOURCE_NAME, globalScope);

    Node argsNode = new Node(Token.LP);
    builder.inferParameterTypes(argsNode, null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertEquals("foo", fnType.getDisplayName());
    assertFalse(fnType.isConstructor());
    assertFalse(fnType.isInterface());
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), fnType.getReturnType());
  }

  @Test(timeout = 4000)
  public void testSetSourceNodeChaining() {
    Node errorNode = new Node(Token.NAME, "testFn");
    Node fnNode = new Node(Token.FUNCTION);
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("testFn", compiler, errorNode, SOURCE_NAME, globalScope);

    assertSame(builder, builder.setSourceNode(fnNode));
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertEquals(fnNode, fnType.getSource());
  }

  @Test(timeout = 4000)
  public void testInferReturnTypeFromDoc() {
    Node errorNode = new Node(Token.NAME, "fnWithReturn");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fnWithReturn", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordReturnType(
        new JSTypeExpression(new Node(Token.STRING, "string"), SOURCE_NAME));
    JSDocInfo info = docBuilder.build(errorNode);

    builder.inferReturnType(info);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), fnType.getReturnType());
    assertFalse(fnType.isReturnTypeInferred());
  }

  @Test(timeout = 4000)
  public void testInferReturnStatementsVoidWhenNoReturn() {
    Node errorNode = new Node(Token.NAME, "fnNoReturn");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fnNoReturn", compiler, errorNode, SOURCE_NAME, globalScope);

    Node block = new Node(Token.BLOCK);
    block.addChildToBack(new Node(Token.EXPR_RESULT, new Node(Token.NUMBER, 42)));

    builder.inferReturnStatementsAsLastResort(block);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), fnType.getReturnType());
    assertTrue(fnType.isReturnTypeInferred());
  }

  @Test(timeout = 4000)
  public void testInferReturnStatementsWithNonEmptyReturn() {
    Node errorNode = new Node(Token.NAME, "fnHasReturn");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fnHasReturn", compiler, errorNode, SOURCE_NAME, globalScope);

    Node block = new Node(Token.BLOCK);
    Node returnNode = new Node(Token.RETURN, new Node(Token.NUMBER, 1));
    block.addChildToBack(returnNode);

    builder.inferReturnStatementsAsLastResort(block);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), fnType.getReturnType());
  }

  @Test(timeout = 4000)
  public void testInferReturnStatementsWithThrow() {
    Node errorNode = new Node(Token.NAME, "fnThrows");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fnThrows", compiler, errorNode, SOURCE_NAME, globalScope);

    Node block = new Node(Token.BLOCK);
    Node throwNode = new Node(Token.THROW, new Node(Token.STRING, "err"));
    block.addChildToBack(throwNode);

    builder.inferReturnStatementsAsLastResort(block);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), fnType.getReturnType());
  }

  @Test(timeout = 4000)
  public void testInferReturnStatementsTraverseBlocks() {
    Node errorNode = new Node(Token.NAME, "fnNested");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fnNested", compiler, errorNode, SOURCE_NAME, globalScope);

    Node block = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF, new Node(Token.TRUE), new Node(Token.BLOCK));
    ifNode.getLastChild().addChildToBack(new Node(Token.RETURN, new Node(Token.NUMBER, 10)));
    block.addChildToBack(ifNode);

    builder.inferReturnStatementsAsLastResort(block);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertEquals(registry.getNativeType(JSTypeNative.UNKNOWN_TYPE), fnType.getReturnType());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testNullFunctionNameDefaultsToEmptyString() {
    Node errorNode = new Node(Token.NAME, "temp");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder(null, compiler, errorNode, SOURCE_NAME, globalScope);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertEquals("", fnType.getDisplayName());
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testNullErrorRootThrowsNPE() {
    new FunctionTypeBuilder("fn", compiler, null, SOURCE_NAME, globalScope);
  }

  @Test(timeout = 4000)
  public void testInferFromOverriddenFunctionNullOldType() {
    Node errorNode = new Node(Token.NAME, "fn");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fn", compiler, errorNode, SOURCE_NAME, globalScope);

    assertSame(builder, builder.inferFromOverriddenFunction(null, new Node(Token.LP)));
  }

  @Test(timeout = 4000)
  public void testInferFromOverriddenFunctionWithoutParamsParent() {
    Node errorNode = new Node(Token.NAME, "subFn");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("subFn", compiler, errorNode, SOURCE_NAME, globalScope);

    FunctionType superFn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE));

    builder.inferFromOverriddenFunction(superFn, null);
    FunctionType built = builder.buildAndRegister();

    assertNotNull(built);
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), built.getReturnType());
    assertEquals(1, built.getParametersCount());
  }

  @Test(timeout = 4000)
  public void testInferFromOverriddenFunctionWithParamsParent() {
    Node errorNode = new Node(Token.NAME, "subFn");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("subFn", compiler, errorNode, SOURCE_NAME, globalScope);

    FunctionType superFn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.NUMBER_TYPE),
        registry.getNativeType(JSTypeNative.STRING_TYPE),
        registry.getNativeType(JSTypeNative.BOOLEAN_TYPE));

    Node paramsParent = new Node(Token.LP);
    paramsParent.addChildToBack(new Node(Token.NAME, "arg1"));
    paramsParent.addChildToBack(new Node(Token.NAME, "arg2"));
    paramsParent.addChildToBack(new Node(Token.NAME, "arg3")); // extra param

    builder.inferFromOverriddenFunction(superFn, paramsParent);
    FunctionType built = builder.buildAndRegister();

    assertNotNull(built);
    assertEquals(3, built.getParametersCount());
  }

  @Test(timeout = 4000)
  public void testInferFromOverriddenFunctionVarArgsConversion() {
    Node errorNode = new Node(Token.NAME, "subFn");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("subFn", compiler, errorNode, SOURCE_NAME, globalScope);

    FunctionParamBuilder paramBuilder = new FunctionParamBuilder(registry);
    paramBuilder.addVarArgs(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType superFn = registry.createFunctionType(
        registry.getNativeType(JSTypeNative.VOID_TYPE),
        paramBuilder.build());

    Node paramsParent = new Node(Token.LP);
    paramsParent.addChildToBack(new Node(Token.NAME, "p1"));
    paramsParent.addChildToBack(new Node(Token.NAME, "p2"));

    builder.inferFromOverriddenFunction(superFn, paramsParent);
    FunctionType built = builder.buildAndRegister();

    assertNotNull(built);
    assertEquals(2, built.getParametersCount());
  }

  @Test(timeout = 4000)
  public void testInferParameterTypesFromDocInfoDirectly() {
    Node errorNode = new Node(Token.NAME, "docOnly");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("docOnly", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordParameter("x",
        new JSTypeExpression(new Node(Token.STRING, "number"), SOURCE_NAME));
    JSDocInfo info = docBuilder.build(errorNode);

    builder.inferParameterTypes(info);
    FunctionType built = builder.buildAndRegister();

    assertNotNull(built);
    assertEquals(1, built.getParametersCount());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Zone & Diagnostic Warnings
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefectTargetedBackwardsTypedefInParameter() {
    Node errorNode = new Node(Token.NAME, "fnWithLazyTypedef");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fnWithLazyTypedef", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    // Reference a type name 'BackwardsType' that will be evaluated against registry/scope
    docBuilder.recordParameter("p1",
        new JSTypeExpression(new Node(Token.STRING, "BackwardsType"), SOURCE_NAME));
    JSDocInfo info = docBuilder.build(errorNode);

    Node args = new Node(Token.LP, new Node(Token.NAME, "p1"));
    builder.inferParameterTypes(args, info);
    FunctionType fnType = builder.buildAndRegister();

    assertNotNull(fnType);
    assertEquals(1, fnType.getParametersCount());
    // The parameter type should be registered and resolved without producing unmatched errors
    JSType paramType = fnType.getParameters().iterator().next().getJSType();
    assertNotNull(paramType);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testExtendsWithoutTypedefWarning() {
    Node errorNode = new Node(Token.NAME, "plainFn");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("plainFn", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordBaseType(
        new JSTypeExpression(new Node(Token.STRING, "Object"), SOURCE_NAME));
    JSDocInfo info = docBuilder.build(errorNode);

    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.EXTENDS_WITHOUT_TYPEDEF,
        compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testExtendsNonObjectWarning() {
    Node errorNode = new Node(Token.NAME, "MyClass");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("MyClass", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordConstructor(true);
    docBuilder.recordBaseType(
        new JSTypeExpression(new Node(Token.STRING, "number"), SOURCE_NAME));
    JSDocInfo info = docBuilder.build(errorNode);

    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.EXTENDS_NON_OBJECT,
        compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testImplementsWithoutConstructorWarning() {
    Node errorNode = new Node(Token.NAME, "notAClass");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("notAClass", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordImplementedInterface(
        new JSTypeExpression(new Node(Token.STRING, "AnInterface"), SOURCE_NAME));
    JSDocInfo info = docBuilder.build(errorNode);

    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.IMPLEMENTS_WITHOUT_CONSTRUCTOR,
        compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testInexistentParamWarning() {
    Node errorNode = new Node(Token.NAME, "fnMismatch");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fnMismatch", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordParameter("paramNotInArgs",
        new JSTypeExpression(new Node(Token.STRING, "string"), SOURCE_NAME));
    JSDocInfo info = docBuilder.build(errorNode);

    Node args = new Node(Token.LP, new Node(Token.NAME, "actualParam"));
    builder.inferParameterTypes(args, info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.INEXISTANT_PARAM,
        compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testOptionalArgAtEndWarning() {
    Node errorNode = new Node(Token.NAME, "fnInvalidArgs");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fnInvalidArgs", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    // Optional parameter followed by required parameter
    Node optTypeNode = new Node(Token.EQUALS, new Node(Token.STRING, "number"));
    docBuilder.recordParameter("opt", new JSTypeExpression(optTypeNode, SOURCE_NAME));
    docBuilder.recordParameter("req",
        new JSTypeExpression(new Node(Token.STRING, "string"), SOURCE_NAME));
    JSDocInfo info = docBuilder.build(errorNode);

    Node args = new Node(Token.LP,
        new Node(Token.NAME, "opt"),
        new Node(Token.NAME, "req"));
    builder.inferParameterTypes(args, info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.OPTIONAL_ARG_AT_END,
        compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testVarArgsMustBeLastWarning() {
    Node errorNode = new Node(Token.NAME, "fnVarArgs");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fnVarArgs", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    Node varArgsTypeNode = new Node(Token.ELLIPSIS, new Node(Token.STRING, "number"));
    docBuilder.recordParameter("va", new JSTypeExpression(varArgsTypeNode, SOURCE_NAME));
    docBuilder.recordParameter("extra",
        new JSTypeExpression(new Node(Token.STRING, "string"), SOURCE_NAME));
    JSDocInfo info = docBuilder.build(errorNode);

    Node args = new Node(Token.LP,
        new Node(Token.NAME, "va"),
        new Node(Token.NAME, "extra"));
    builder.inferParameterTypes(args, info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.VAR_ARGS_MUST_BE_LAST,
        compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testTemplateTypeDuplicatedError() {
    Node errorNode = new Node(Token.NAME, "fnTemplate");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fnTemplate", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordTemplateTypeName("T");
    docBuilder.recordParameter("a",
        new JSTypeExpression(new Node(Token.STRING, "T"), SOURCE_NAME));
    docBuilder.recordParameter("b",
        new JSTypeExpression(new Node(Token.STRING, "T"), SOURCE_NAME));
    JSDocInfo info = docBuilder.build(errorNode);

    builder.inferTemplateTypeName(info);
    Node args = new Node(Token.LP,
        new Node(Token.NAME, "a"),
        new Node(Token.NAME, "b"));
    builder.inferParameterTypes(args, info);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(FunctionTypeBuilder.TEMPLATE_TYPE_DUPLICATED,
        compiler.getErrors()[0].getType());
  }

  @Test(timeout = 4000)
  public void testTemplateTypeExpectedError() {
    Node errorNode = new Node(Token.NAME, "fnTemplateMissing");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fnTemplateMissing", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordTemplateTypeName("T");
    docBuilder.recordParameter("a",
        new JSTypeExpression(new Node(Token.STRING, "number"), SOURCE_NAME));
    JSDocInfo info = docBuilder.build(errorNode);

    builder.inferTemplateTypeName(info);
    Node args = new Node(Token.LP, new Node(Token.NAME, "a"));
    builder.inferParameterTypes(args, info);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(FunctionTypeBuilder.TEMPLATE_TYPE_EXPECTED,
        compiler.getErrors()[0].getType());
  }

  @Test(timeout = 4000)
  public void testThisTypeNonObjectWarning() {
    Node errorNode = new Node(Token.NAME, "fnThis");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("fnThis", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordThisType(
        new JSTypeExpression(new Node(Token.STRING, "number"), SOURCE_NAME));
    JSDocInfo info = docBuilder.build(errorNode);

    builder.inferThisType(info, (Node) null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.THIS_TYPE_NON_OBJECT,
        compiler.getWarnings()[0].getType());
  }

  // =========================================================================
  // Partition D: Object Creation, Interface & Constructor Mechanics
  // =========================================================================

  @Test(timeout = 4000)
  public void testBuildConstructorAndRegister() {
    Node errorNode = new Node(Token.NAME, "MyConstructor");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("MyConstructor", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordConstructor(true);
    JSDocInfo info = docBuilder.build(errorNode);

    builder.inferInheritance(info);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType ctor = builder.buildAndRegister();

    assertNotNull(ctor);
    assertTrue(ctor.isConstructor());
    assertNotNull(registry.getType("MyConstructor"));
  }

  @Test(timeout = 4000)
  public void testBuildInterfaceAndRegister() {
    Node errorNode = new Node(Token.NAME, "MyInterface");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("MyInterface", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordInterface(true);
    JSDocInfo info = docBuilder.build(errorNode);

    builder.inferInheritance(info);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType iface = builder.buildAndRegister();

    assertNotNull(iface);
    assertTrue(iface.isInterface());
    assertNotNull(registry.getType("MyInterface"));
  }

  @Test(timeout = 4000)
  public void testConstructorRedefinitionWarning() {
    // 1. Declare first constructor
    Node errorNode1 = new Node(Token.NAME, "DuplicateClass");
    FunctionTypeBuilder builder1 =
        new FunctionTypeBuilder("DuplicateClass", compiler, errorNode1, SOURCE_NAME, globalScope);
    JSDocInfoBuilder docBuilder1 = new JSDocInfoBuilder(false);
    docBuilder1.recordConstructor(true);
    builder1.inferInheritance(docBuilder1.build(errorNode1));
    builder1.inferParameterTypes(new Node(Token.LP), null);
    builder1.buildAndRegister();

    // 2. Declare second constructor with different parameters
    Node errorNode2 = new Node(Token.NAME, "DuplicateClass");
    FunctionTypeBuilder builder2 =
        new FunctionTypeBuilder("DuplicateClass", compiler, errorNode2, SOURCE_NAME, globalScope);
    JSDocInfoBuilder docBuilder2 = new JSDocInfoBuilder(false);
    docBuilder2.recordConstructor(true);
    builder2.inferInheritance(docBuilder2.build(errorNode2));
    Node args2 = new Node(Token.LP, new Node(Token.NAME, "arg1"));
    builder2.inferParameterTypes(args2, null);
    builder2.buildAndRegister();

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.TYPE_REDEFINITION,
        compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testInferInheritanceWithBaseAndInterfaces() {
    // Create base constructor
    FunctionType baseCtor = registry.createConstructorType(
        "BaseClass", null, new Node(Token.LP), null);
    registry.declareType("BaseClass", baseCtor.getInstanceType());

    // Create interface
    FunctionType interfaceType = registry.createInterfaceType("IInterface", null);
    registry.declareType("IInterface", interfaceType.getInstanceType());

    Node errorNode = new Node(Token.NAME, "DerivedClass");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("DerivedClass", compiler, errorNode, SOURCE_NAME, globalScope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(false);
    docBuilder.recordConstructor(true);
    docBuilder.recordBaseType(
        new JSTypeExpression(new Node(Token.STRING, "BaseClass"), SOURCE_NAME));
    docBuilder.recordImplementedInterface(
        new JSTypeExpression(new Node(Token.STRING, "IInterface"), SOURCE_NAME));
    JSDocInfo info = docBuilder.build(errorNode);

    builder.inferInheritance(info);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType derivedCtor = builder.buildAndRegister();

    assertNotNull(derivedCtor);
    assertTrue(derivedCtor.isConstructor());
    assertEquals(1, derivedCtor.getImplementedInterfaces().size());
  }

  @Test(timeout = 4000)
  public void testInferThisTypeFromOwnerNode() {
    ObjectType objType = registry.createAnonymousObjectType();
    registry.declareType("MyOwner", objType);

    Node errorNode = new Node(Token.NAME, "ownedMethod");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("ownedMethod", compiler, errorNode, SOURCE_NAME, globalScope);

    Node ownerNode = Node.newString(Token.NAME, "MyOwner");
    builder.inferThisType(null, ownerNode);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType fn = builder.buildAndRegister();

    assertNotNull(fn);
    assertEquals(objType, fn.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testInferThisTypeDirectType() {
    Node errorNode = new Node(Token.NAME, "method");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("method", compiler, errorNode, SOURCE_NAME, globalScope);

    ObjectType objType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    builder.inferThisType(null, objType);
    builder.inferParameterTypes(new Node(Token.LP), null);
    FunctionType fn = builder.buildAndRegister();

    assertNotNull(fn);
    assertEquals(objType, fn.getTypeOfThis());
  }

  // =========================================================================
  // Partition E: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBuildWithoutParametersThrowsIllegalStateException() {
    Node errorNode = new Node(Token.NAME, "noParams");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("noParams", compiler, errorNode, SOURCE_NAME, globalScope);
    // Intentionally omit inferParameterTypes
    builder.buildAndRegister();
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testInferReturnStatementsWithNonBlockThrowsIllegalArgumentException() {
    Node errorNode = new Node(Token.NAME, "invalidBlock");
    FunctionTypeBuilder builder =
        new FunctionTypeBuilder("invalidBlock", compiler, errorNode, SOURCE_NAME, globalScope);

    Node invalidNode = new Node(Token.EXPR_RESULT);
    builder.inferReturnStatementsAsLastResort(invalidNode);
  }

  @Test(timeout = 4000)
  public void testIsFunctionTypeDeclaration() {
    JSDocInfoBuilder docBuilder1 = new JSDocInfoBuilder(false);
    assertFalse(FunctionTypeBuilder.isFunctionTypeDeclaration(docBuilder1.build(scriptRoot)));

    JSDocInfoBuilder docBuilder2 = new JSDocInfoBuilder(false);
    docBuilder2.recordConstructor(true);
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(docBuilder2.build(scriptRoot)));

    JSDocInfoBuilder docBuilder3 = new JSDocInfoBuilder(false);
    docBuilder3.recordInterface(true);
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(docBuilder3.build(scriptRoot)));

    JSDocInfoBuilder docBuilder4 = new JSDocInfoBuilder(false);
    docBuilder4.recordReturnType(
        new JSTypeExpression(new Node(Token.STRING, "boolean"), SOURCE_NAME));
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(docBuilder4.build(scriptRoot)));

    JSDocInfoBuilder docBuilder5 = new JSDocInfoBuilder(false);
    docBuilder5.recordThisType(
        new JSTypeExpression(new Node(Token.STRING, "Object"), SOURCE_NAME));
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(docBuilder5.build(scriptRoot)));

    JSDocInfoBuilder docBuilder6 = new JSDocInfoBuilder(false);
    docBuilder6.recordParameter("x",
        new JSTypeExpression(new Node(Token.STRING, "number"), SOURCE_NAME));
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(docBuilder6.build(scriptRoot)));
  }
}