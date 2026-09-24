package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.collect.Lists;
import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.FunctionBuilder;
import com.google.javascript.rhino.jstype.FunctionParamBuilder;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: com.google.javascript.jscomp.FunctionTypeBuilder
 * TARGET DEFECT (Defects4J Closure):
 *   - TypeCheckTest::testIssue368 / TypeCheckTest::testMethodInference6 / LooseTypeCheckTest::testMethodInference6
 *   - Symptom: When inferring parameter types from an overridden function where the overriding function literal
 *     declares fewer parameters than the overridden base method, FunctionTypeBuilder fails to copy/clone
 *     the remaining parameters from oldParams (marked by the dangling comment: "// Clone any remaining params
 *     that aren't in the function literal.").
 *   - Fault Condition: Subclass methods overriding a superclass/interface method with fewer explicit formal parameters
 *     lose the trailing parameter types, causing downstream type-checking mismatches or dropped parameters.
 *
 * COVERAGE PATHS & PARTITIONS TARGETED:
 *   Partition A: Core Functional Logic & State Transitions
 *     - inferFromOverriddenFunction with literal overriding and full parameter mapping
 *     - inferFromOverriddenFunction with null paramsParent (non-literal target)
 *     - inferReturnType with explicit JSDoc return type
 *     - inferThisType with valid ObjectType @this annotation and fallback
 *     - buildAndRegister for constructor, interface, and standard functions
 *     - isFunctionTypeDeclaration covering all 5 conditional predicates
 *   Partition B: Boundary Value Analysis (BVA) & Extremes
 *     - Empty string and null function names
 *     - Null JSDocInfo across all infer* methods
 *     - Function literal with more parameters than overridden function (unknown type inference)
 *     - VarArgs parameter in overridden function split into individual subclass parameters
 *     - UnknownFunctionContents & AstFunctionContents lifecycle transitions
 *   Partition C: Defect-Targeted Branch Zone
 *     - Overriding function literal with FEWER parameters than the overridden function (Direct Defect Trigger)
 *   Partition D: Exception & Defensive Guard Paths
 *     - Null errorRoot in constructor (asserts NullPointerException via Preconditions.checkNotNull)
 *     - buildAndRegister called without inferring parameter types (asserts IllegalStateException)
 *     - Template type errors (TEMPLATE_TYPE_EXPECTED on return type / missing param, TEMPLATE_TYPE_DUPLICATED)
 *     - Parameter ordering warnings (OPTIONAL_ARG_AT_END, VAR_ARGS_MUST_BE_LAST)
 *     - JSDoc parameter mismatch warnings (INEXISTANT_PARAM)
 *     - Inheritance error warnings (EXTENDS_WITHOUT_TYPEDEF, IMPLEMENTS_WITHOUT_CONSTRUCTOR, EXTENDS_NON_OBJECT, BAD_IMPLEMENTED_TYPE)
 *     - Constructor redefinition warning (TYPE_REDEFINITION)
 *   Partition E: Object Lifecycle & Scope Integration
 *     - Namespaced functions (dot-notation) and scope hierarchy resolution in getScopeDeclaredIn
 */
public class FunctionTypeBuilderGeminiTest {

  private Compiler compiler;
  private JSTypeRegistry registry;
  private Scope scope;
  private Node errorRoot;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    compiler.initOptions(options);
    registry = compiler.getTypeRegistry();
    errorRoot = IR.script();
    scope = Scope.createGlobalScope(errorRoot);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Known Defect Reproduction)
  // =========================================================================

  /**
   * Targets the defect underlying TypeCheckTest#testIssue368 and testMethodInference6.
   * When an overriding function literal has fewer parameters than the overridden method,
   * all remaining trailing parameters from the base method MUST be cloned into the new type.
   * In the defective version, the builder omits copying remaining parameters.
   */
  @Test(timeout = 4000)
  public void testDefect_inferFromOverriddenFunction_clonesRemainingParameters() {
    // 1. Create base function type with two parameters: (number, string)
    FunctionParamBuilder baseParamBuilder = new FunctionParamBuilder(registry);
    baseParamBuilder.addRequiredParams(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    baseParamBuilder.addRequiredParams(registry.getNativeType(JSTypeNative.STRING_TYPE));
    Node baseParamsNode = baseParamBuilder.build();

    FunctionType overriddenType = new FunctionBuilder(registry)
        .withParamsNode(baseParamsNode)
        .withReturnType(registry.getNativeType(JSTypeNative.VOID_TYPE))
        .build();

    // 2. Overriding function literal only explicitly specifies 1 parameter: function(x)
    Node literalParamsParent = IR.paramList(IR.name("x"));

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "Bar.prototype.add", compiler, errorRoot, "test.js", scope);
    builder.inferFromOverriddenFunction(overriddenType, literalParamsParent);

    FunctionType resultType = builder.buildAndRegister();
    Node resultParams = resultType.getParametersNode();

    assertNotNull("Result parameters node must not be null", resultParams);
    // On the defective version, resultParams only has 1 parameter (x) because remaining
    // params were not cloned. On the correct version, remaining params are preserved (2).
    assertEquals("Overriding function should preserve all parameters from overridden function",
        2, resultParams.getChildCount());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testInferFromOverriddenFunction_nullParamsParent_copiesDirectly() {
    FunctionParamBuilder paramBuilder = new FunctionParamBuilder(registry);
    paramBuilder.addRequiredParams(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    Node originalParams = paramBuilder.build();

    FunctionType overriddenType = new FunctionBuilder(registry)
        .withParamsNode(originalParams)
        .withReturnType(registry.getNativeType(JSTypeNative.STRING_TYPE))
        .build();

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "foo", compiler, errorRoot, "test.js", scope);
    builder.inferFromOverriddenFunction(overriddenType, null);

    FunctionType built = builder.buildAndRegister();
    assertEquals(registry.getNativeType(JSTypeNative.STRING_TYPE), built.getReturnType());
    assertSame(originalParams, built.getParametersNode());
  }

  @Test(timeout = 4000)
  public void testInferReturnType_fromJSDoc() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordReturnType(new JSTypeExpression(IR.name("number"), "test.js"));
    JSDocInfo info = docBuilder.build(IR.empty());

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "f", compiler, errorRoot, "test.js", scope);
    builder.inferReturnType(info);
    builder.inferParameterTypes(IR.paramList(), null);

    FunctionType fn = builder.buildAndRegister();
    assertEquals(registry.getNativeType(JSTypeNative.NUMBER_TYPE), fn.getReturnType());
    assertFalse(fn.isReturnTypeInferred());
  }

  @Test(timeout = 4000)
  public void testInferThisType_validObjectAndFallback() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordThisType(new JSTypeExpression(IR.name("Object"), "test.js"));
    JSDocInfo info = docBuilder.build(IR.empty());

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "f", compiler, errorRoot, "test.js", scope);
    builder.inferThisType(info);
    builder.inferParameterTypes(IR.paramList(), null);

    FunctionType fn = builder.buildAndRegister();
    assertTrue(fn.getTypeOfThis().isSubtype(registry.getNativeType(JSTypeNative.OBJECT_TYPE)));
  }

  @Test(timeout = 4000)
  public void testInferThisType_fallbackToContextTypeWhenDocMissing() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "f", compiler, errorRoot, "test.js", scope);
    ObjectType defaultThis = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
    builder.inferThisType(null, defaultThis);
    builder.inferParameterTypes(IR.paramList(), null);

    FunctionType fn = builder.buildAndRegister();
    assertEquals(defaultThis, fn.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testBuildAndRegister_constructorDeclaration() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    JSDocInfo info = docBuilder.build(IR.empty());

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "MyClass", compiler, errorRoot, "test.js", scope);
    builder.inferInheritance(info);
    builder.inferParameterTypes(IR.paramList(), null);

    FunctionType ctor = builder.buildAndRegister();
    assertTrue(ctor.isConstructor());
    assertNotNull(registry.getType("MyClass"));
  }

  @Test(timeout = 4000)
  public void testBuildAndRegister_interfaceDeclaration() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordInterface();
    JSDocInfo info = docBuilder.build(IR.empty());

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "MyInterface", compiler, errorRoot, "test.js", scope);
    builder.inferInheritance(info);
    builder.inferParameterTypes(IR.paramList(), null);

    FunctionType iface = builder.buildAndRegister();
    assertTrue(iface.isInterface());
    assertNotNull(registry.getType("MyInterface"));
  }

  @Test(timeout = 4000)
  public void testIsFunctionTypeDeclaration_allPredicates() {
    // 1. parameterCount > 0
    JSDocInfoBuilder b1 = new JSDocInfoBuilder(true);
    b1.recordParameter("x", new JSTypeExpression(IR.name("number"), "test.js"));
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b1.build(IR.empty())));

    // 2. hasReturnType
    JSDocInfoBuilder b2 = new JSDocInfoBuilder(true);
    b2.recordReturnType(new JSTypeExpression(IR.name("boolean"), "test.js"));
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b2.build(IR.empty())));

    // 3. hasThisType
    JSDocInfoBuilder b3 = new JSDocInfoBuilder(true);
    b3.recordThisType(new JSTypeExpression(IR.name("Object"), "test.js"));
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b3.build(IR.empty())));

    // 4. isConstructor
    JSDocInfoBuilder b4 = new JSDocInfoBuilder(true);
    b4.recordConstructor();
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b4.build(IR.empty())));

    // 5. isInterface
    JSDocInfoBuilder b5 = new JSDocInfoBuilder(true);
    b5.recordInterface();
    assertTrue(FunctionTypeBuilder.isFunctionTypeDeclaration(b5.build(IR.empty())));

    // 6. none of the above
    JSDocInfoBuilder b6 = new JSDocInfoBuilder(true);
    b6.recordDeprecationReason("deprecated function");
    assertFalse(FunctionTypeBuilder.isFunctionTypeDeclaration(b6.build(IR.empty())));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testBoundary_nullAndEmptyFunctionName() {
    FunctionTypeBuilder builderNull = new FunctionTypeBuilder(
        null, compiler, errorRoot, "test.js", scope);
    builderNull.inferParameterTypes(IR.paramList(), null);
    FunctionType fnNull = builderNull.buildAndRegister();
    assertNotNull(fnNull);

    FunctionTypeBuilder builderEmpty = new FunctionTypeBuilder(
        "", compiler, errorRoot, "test.js", scope);
    builderEmpty.inferParameterTypes(IR.paramList(), null);
    FunctionType fnEmpty = builderEmpty.buildAndRegister();
    assertNotNull(fnEmpty);
  }

  @Test(timeout = 4000)
  public void testBoundary_nullJSDocHandlingAcrossAllInferenceMethods() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "fn", compiler, errorRoot, "test.js", scope);
    builder.inferReturnType(null)
           .inferInheritance(null)
           .inferThisType(null)
           .inferThisType(null, null)
           .inferTemplateTypeName(null)
           .inferParameterTypes(IR.paramList(), null);

    FunctionType fn = builder.buildAndRegister();
    assertNotNull(fn);
    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testBoundary_inferFromOverriddenFunction_subclassHasMoreParameters() {
    // Base method has 1 parameter (number)
    FunctionParamBuilder baseParamBuilder = new FunctionParamBuilder(registry);
    baseParamBuilder.addRequiredParams(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType overriddenType = new FunctionBuilder(registry)
        .withParamsNode(baseParamBuilder.build())
        .build();

    // Overriding method has 2 parameters: (a, opt_b)
    Node literalParams = IR.paramList(IR.name("a"), IR.name("opt_b"));

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "subMethod", compiler, errorRoot, "test.js", scope);
    builder.inferFromOverriddenFunction(overriddenType, literalParams);
    FunctionType result = builder.buildAndRegister();

    assertEquals(2, result.getParametersNode().getChildCount());
    Node secondParam = result.getParametersNode().getFirstChild().getNext();
    assertTrue(secondParam.isOptionalArg());
  }

  @Test(timeout = 4000)
  public void testBoundary_inferFromOverriddenFunction_varArgsExpandedToIndividual() {
    // Base method has 1 var_args parameter (...number)
    FunctionParamBuilder baseParamBuilder = new FunctionParamBuilder(registry);
    baseParamBuilder.addVarArgs(registry.getNativeType(JSTypeNative.NUMBER_TYPE));
    FunctionType overriddenType = new FunctionBuilder(registry)
        .withParamsNode(baseParamBuilder.build())
        .build();

    // Overriding literal provides two individual arguments (a, b)
    Node literalParams = IR.paramList(IR.name("a"), IR.name("b"));

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "expandVarArgs", compiler, errorRoot, "test.js", scope);
    builder.inferFromOverriddenFunction(overriddenType, literalParams);
    FunctionType result = builder.buildAndRegister();

    Node firstParam = result.getParametersNode().getFirstChild();
    assertFalse(firstParam.isVarArgs());
    assertTrue(firstParam.isOptionalArg());
  }

  @Test(timeout = 4000)
  public void testBoundary_inferParameterTypes_fromDocAloneWhenArgsParentNull() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordParameter("docParam", new JSTypeExpression(IR.name("string"), "test.js"));
    JSDocInfo info = docBuilder.build(IR.empty());

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "docOnly", compiler, errorRoot, "test.js", scope);
    builder.inferParameterTypes(null, info);

    FunctionType fn = builder.buildAndRegister();
    assertEquals(1, fn.getParametersNode().getChildCount());
    assertEquals("docParam", fn.getParametersNode().getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testBoundary_inferParameterTypes_nullArgsAndNullDocDoesNothing() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "nullTest", compiler, errorRoot, "test.js", scope);
    builder.inferParameterTypes(null, null);

    // parametersNode remains null, which triggers the defensive exception in buildAndRegister
    try {
      builder.buildAndRegister();
      fail("Expected IllegalStateException due to missing parameters");
    } catch (IllegalStateException expected) {
      assertTrue(expected.getMessage().contains("params"));
    }
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testException_nullErrorRootThrowsNpe() {
    new FunctionTypeBuilder("fail", compiler, null, "test.js", scope);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testException_buildWithoutParametersThrows() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "noParams", compiler, errorRoot, "test.js", scope);
    builder.buildAndRegister();
  }

  @Test(timeout = 4000)
  public void testWarning_optionalParamBeforeRequired() {
    // opt_a is optional by convention, but b is required
    Node params = IR.paramList(IR.name("opt_a"), IR.name("b"));

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "badParams", compiler, errorRoot, "test.js", scope);
    builder.inferParameterTypes(params, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.OPTIONAL_ARG_AT_END.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testWarning_varArgsNotLast() {
    // var_args followed by another parameter
    Node params = IR.paramList(IR.name("var_args"), IR.name("opt_extra"));

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "badVarArgs", compiler, errorRoot, "test.js", scope);
    builder.inferParameterTypes(params, null);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.VAR_ARGS_MUST_BE_LAST.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testWarning_inexistantParamInDoc() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordParameter("nonExistent", new JSTypeExpression(IR.name("number"), "test.js"));
    JSDocInfo info = docBuilder.build(IR.empty());

    Node params = IR.paramList(IR.name("actual"));

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "mismatchedDoc", compiler, errorRoot, "test.js", scope);
    builder.inferParameterTypes(params, info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.INEXISTANT_PARAM.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testWarning_extendsWithoutTypedef() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordBaseType(new JSTypeExpression(IR.name("Object"), "test.js"));
    JSDocInfo info = docBuilder.build(IR.empty());

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "plainFn", compiler, errorRoot, "test.js", scope);
    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.EXTENDS_WITHOUT_TYPEDEF.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testWarning_implementsWithoutConstructor() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordImplementedInterface(new JSTypeExpression(IR.name("Object"), "test.js"));
    JSDocInfo info = docBuilder.build(IR.empty());

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "plainFn", compiler, errorRoot, "test.js", scope);
    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.IMPLEMENTS_WITHOUT_CONSTRUCTOR.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testWarning_extendsNonObject() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    docBuilder.recordBaseType(new JSTypeExpression(IR.name("number"), "test.js"));
    JSDocInfo info = docBuilder.build(IR.empty());

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "BadCtor", compiler, errorRoot, "test.js", scope);
    builder.inferInheritance(info);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.EXTENDS_NON_OBJECT.key,
        compiler.getWarnings()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testError_badImplementedType() {
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    docBuilder.recordImplementedInterface(new JSTypeExpression(IR.name("number"), "test.js"));
    JSDocInfo info = docBuilder.build(IR.empty());

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "BadImplCtor", compiler, errorRoot, "test.js", scope);
    builder.inferInheritance(info);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(TypeCheck.BAD_IMPLEMENTED_TYPE.key,
        compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testError_templateTypeDuplicatedAndExpected() {
    // 1. Template type duplicated in parameter list
    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordTemplateTypeName("T");
    docBuilder.recordParameter("a", new JSTypeExpression(IR.name("T"), "test.js"));
    docBuilder.recordParameter("b", new JSTypeExpression(IR.name("T"), "test.js"));
    JSDocInfo info = docBuilder.build(IR.empty());

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "templateFn", compiler, errorRoot, "test.js", scope);
    builder.inferTemplateTypeName(info);
    builder.inferParameterTypes(IR.paramList(IR.name("a"), IR.name("b")), info);

    assertEquals(1, compiler.getErrorCount());
    assertEquals(FunctionTypeBuilder.TEMPLATE_TYPE_DUPLICATED.key,
        compiler.getErrors()[0].getType().key);

    // 2. Template type expected on return type alone
    Compiler compiler2 = new Compiler();
    compiler2.initOptions(new CompilerOptions());
    FunctionTypeBuilder builder2 = new FunctionTypeBuilder(
        "templateRetOnly", compiler2, errorRoot, "test.js", scope);

    JSDocInfoBuilder docBuilder2 = new JSDocInfoBuilder(true);
    docBuilder2.recordTemplateTypeName("U");
    docBuilder2.recordReturnType(new JSTypeExpression(IR.name("U"), "test.js"));
    JSDocInfo info2 = docBuilder2.build(IR.empty());

    builder2.inferTemplateTypeName(info2);
    builder2.inferReturnType(info2);

    assertEquals(1, compiler2.getErrorCount());
    assertEquals(FunctionTypeBuilder.TEMPLATE_TYPE_EXPECTED.key,
        compiler2.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testWarning_typeRedefinitionOnNativeFunction() {
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "Function", compiler, errorRoot, "test.js", scope);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordConstructor();
    builder.inferInheritance(docBuilder.build(IR.empty()));
    builder.inferParameterTypes(IR.paramList(IR.name("customArg")), null);

    FunctionType fnType = builder.buildAndRegister();
    assertNotNull(fnType);
    assertEquals(1, compiler.getWarningCount());
    assertEquals(FunctionTypeBuilder.TYPE_REDEFINITION.key,
        compiler.getWarnings()[0].getType().key);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Scope Integration
  // =========================================================================

  @Test(timeout = 4000)
  public void testContents_unknownFunctionContents() {
    FunctionTypeBuilder.FunctionContents unknown =
        FunctionTypeBuilder.UnknownFunctionContents.get();
    assertNull(unknown.getSourceNode());
    assertTrue(unknown.mayBeFromExterns());
    assertTrue(unknown.mayHaveNonEmptyReturns());
    assertFalse(unknown.getEscapedVarNames().iterator().hasNext());

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "testContents", compiler, errorRoot, "test.js", scope);
    builder.setContents(null); // Should not overwrite default
    builder.setContents(unknown);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType fn = builder.buildAndRegister();
    assertNotNull(fn);
  }

  @Test(timeout = 4000)
  public void testContents_astFunctionContents() {
    Node fnNode = IR.function(IR.name("f"), IR.paramList(), IR.block());
    FunctionTypeBuilder.AstFunctionContents astContents =
        new FunctionTypeBuilder.AstFunctionContents(fnNode);

    assertSame(fnNode, astContents.getSourceNode());
    assertFalse(astContents.mayHaveNonEmptyReturns());
    astContents.recordNonEmptyReturn();
    assertTrue(astContents.mayHaveNonEmptyReturns());

    assertFalse(astContents.getEscapedVarNames().iterator().hasNext());
    astContents.recordEscapedVarName("escapedVar1");
    astContents.recordEscapedVarName("escapedVar2");
    List<String> escaped = Lists.newArrayList(astContents.getEscapedVarNames());
    assertEquals(2, escaped.size());
    assertTrue(escaped.contains("escapedVar1"));
    assertTrue(escaped.contains("escapedVar2"));

    // Verify buildAndRegister infers VOID when mayHaveNonEmptyReturns is false
    FunctionTypeBuilder.AstFunctionContents emptyReturnsContents =
        new FunctionTypeBuilder.AstFunctionContents(fnNode);
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "voidInferred", compiler, errorRoot, "test.js", scope);
    builder.setContents(emptyReturnsContents);
    builder.inferParameterTypes(IR.paramList(), null);
    FunctionType fn = builder.buildAndRegister();
    assertEquals(registry.getNativeType(JSTypeNative.VOID_TYPE), fn.getReturnType());
    assertTrue(fn.isReturnTypeInferred());
  }

  @Test(timeout = 4000)
  public void testScopeDeclaredIn_namespacedFunction() {
    scope.declare("MyNamespace", IR.name("MyNamespace"), null, null);

    JSDocInfoBuilder docBuilder = new JSDocInfoBuilder(true);
    docBuilder.recordInterface();
    JSDocInfo info = docBuilder.build(IR.empty());

    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "MyNamespace.MySubInterface", compiler, errorRoot, "test.js", scope);
    builder.inferInheritance(info);
    builder.inferParameterTypes(IR.paramList(), null);

    FunctionType iface = builder.buildAndRegister();
    assertTrue(iface.isInterface());
    assertNotNull(registry.getType("MyNamespace.MySubInterface"));
  }
}