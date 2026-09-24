package com.google.javascript.rhino.jstype;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SimpleErrorReporter;

/* [Branch & Defect Analysis Matrix]
 * =====================================================================================================
 * Class Under Test: com.google.javascript.rhino.jstype.FunctionBuilder
 * Target Defects4J Defect: Type inference inconsistency where function return types lose their "inferred"
 *                          flag during type copying/builder transformations, causing expected [undefined]
 *                          to degrade to [?] across TypeCheck, LooseTypeCheck, and CodePrinter passes.
 *
 * Decision / Branch Matrix:
 * 1. Default State:
 *    - Unspecified parametersNode, returnType, typeOfThis, templateTypeName, name, sourceNode.
 *    - Flags default to false: inferredReturnType=false, isConstructor=false, isNativeType=false.
 * 2. Fluent Mutators & Return Values:
 *    - Every builder method (withName, withSourceNode, withParams, withParamsNode, withReturnType,
 *      withInferredReturnType, withTypeOfThis, withTemplateName, forConstructor, forNativeType,
 *      copyFromOtherFunction) must return `this` instance for method chaining.
 * 3. Parameters Specification:
 *    - Branch: withParams(FunctionParamBuilder) invokes params.build().
 *    - Branch: withParamsNode(Node) assigns parametersNode directly.
 *    - Null / Empty handling on both branches.
 * 4. Return Type Specification:
 *    - Branch: withReturnType sets type, inferredReturnType remains false.
 *    - Branch: withInferredReturnType sets type AND sets inferredReturnType to true.
 * 5. Type Archetypes:
 *    - Branch: forConstructor sets isConstructor=true.
 *    - Branch: forNativeType sets isNativeType=true.
 * 6. Defect-Targeted Logic (copyFromOtherFunction):
 *    - Branch: otherType.isReturnTypeInferred() state preservation.
 *    - Latent Defect: copyFromOtherFunction copies name, source, parameters, returnType, typeOfThis,
 *      templateTypeName, isConstructor, and isNativeType, but omits copying inferredReturnType!
 * 7. Boundary / Defensive Guards:
 *    - Null arguments to withParams and copyFromOtherFunction must fail fast with NullPointerException.
 *    - Idempotent invocations and overwriting configurations.
 * =====================================================================================================
 */
public class FunctionBuilderGeminiTest {

  private JSTypeRegistry registry;
  private JSType numberType;
  private JSType stringType;
  private ObjectType objectType;

  @Before
  public void setUp() {
    registry = new JSTypeRegistry(new SimpleErrorReporter());
    numberType = registry.getNativeType(JSTypeNative.NUMBER_TYPE);
    stringType = registry.getNativeType(JSTypeNative.STRING_TYPE);
    objectType = registry.getNativeObjectType(JSTypeNative.OBJECT_TYPE);
  }

  // ===================================================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ===================================================================================================

  @Test(timeout = 4000)
  public void testDefaultBuildState() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    FunctionType fn = builder.build();

    assertNotNull("Built FunctionType should not be null", fn);
    assertNull("Default source node should be null", fn.getSource());
    assertNull("Default template name should be null", fn.getTemplateTypeName());
    assertFalse("Default isConstructor should be false", fn.isConstructor());
    assertFalse("Default isNativeObjectType should be false", fn.isNativeObjectType());
    assertFalse("Default isReturnTypeInferred should be false", fn.isReturnTypeInferred());
  }

  @Test(timeout = 4000)
  public void testFluentChainingIdentity() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    Node dummyNode = new Node(0);
    FunctionParamBuilder paramBuilder = new FunctionParamBuilder(registry);

    assertSame(builder, builder.withName("testFn"));
    assertSame(builder, builder.withSourceNode(dummyNode));
    assertSame(builder, builder.withParams(paramBuilder));
    assertSame(builder, builder.withParamsNode(dummyNode));
    assertSame(builder, builder.withReturnType(numberType));
    assertSame(builder, builder.withInferredReturnType(numberType));
    assertSame(builder, builder.withTypeOfThis(objectType));
    assertSame(builder, builder.withTemplateName("T"));
    assertSame(builder, builder.forConstructor());
    assertSame(builder, builder.forNativeType());
  }

  @Test(timeout = 4000)
  public void testFullConfigurationBuild() {
    Node sourceNode = new Node(0);
    Node paramsNode = new Node(0);
    String name = "CustomConstructor";
    String templateName = "ElemType";

    FunctionType fn = new FunctionBuilder(registry)
        .withName(name)
        .withSourceNode(sourceNode)
        .withParamsNode(paramsNode)
        .withReturnType(stringType)
        .withTypeOfThis(objectType)
        .withTemplateName(templateName)
        .forConstructor()
        .forNativeType()
        .build();

    assertSame(sourceNode, fn.getSource());
    assertSame(paramsNode, fn.getParametersNode());
    assertTrue("Return type must be stringType", stringType.isEquivalentTo(fn.getReturnType()));
    assertSame(objectType, fn.getTypeOfThis());
    assertEquals(templateName, fn.getTemplateTypeName());
    assertTrue("Should be configured as constructor", fn.isConstructor());
    assertTrue("Should be configured as native object type", fn.isNativeObjectType());
    assertFalse("Return type was not marked as inferred", fn.isReturnTypeInferred());
  }

  @Test(timeout = 4000)
  public void testWithParamsViaParamBuilder() {
    FunctionParamBuilder paramBuilder = new FunctionParamBuilder(registry);
    paramBuilder.addRequiredParams(numberType, stringType);

    FunctionType fn = new FunctionBuilder(registry)
        .withParams(paramBuilder)
        .build();

    Node parametersNode = fn.getParametersNode();
    assertNotNull("Parameters node built from paramBuilder must not be null", parametersNode);
    assertTrue("Parameters node should have children", parametersNode.hasChildren());
  }

  @Test(timeout = 4000)
  public void testWithInferredReturnTypeSetsFlag() {
    FunctionType fn = new FunctionBuilder(registry)
        .withInferredReturnType(numberType)
        .build();

    assertTrue("isReturnTypeInferred must be true when set via withInferredReturnType",
        fn.isReturnTypeInferred());
    assertTrue("Return type should match the specified type",
        numberType.isEquivalentTo(fn.getReturnType()));
  }

  @Test(timeout = 4000)
  public void testCopyFromOtherFunctionAllFields() {
    Node sourceNode = new Node(0);
    Node paramsNode = new Node(0);
    FunctionType original = new FunctionBuilder(registry)
        .withName("OriginalFunc")
        .withSourceNode(sourceNode)
        .withParamsNode(paramsNode)
        .withReturnType(numberType)
        .withTypeOfThis(objectType)
        .withTemplateName("T")
        .forConstructor()
        .forNativeType()
        .build();

    FunctionType copied = new FunctionBuilder(registry)
        .copyFromOtherFunction(original)
        .build();

    assertSame(original.getSource(), copied.getSource());
    assertSame(original.getParametersNode(), copied.getParametersNode());
    assertTrue(original.getReturnType().isEquivalentTo(copied.getReturnType()));
    assertSame(original.getTypeOfThis(), copied.getTypeOfThis());
    assertEquals(original.getTemplateTypeName(), copied.getTemplateTypeName());
    assertEquals(original.isConstructor(), copied.isConstructor());
    assertEquals(original.isNativeObjectType(), copied.isNativeObjectType());
  }

  // ===================================================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // ===================================================================================================

  @Test(timeout = 4000)
  public void testEmptyStringsForNameAndTemplate() {
    FunctionType fn = new FunctionBuilder(registry)
        .withName("")
        .withTemplateName("")
        .build();

    assertEquals("", fn.getTemplateTypeName());
  }

  @Test(timeout = 4000)
  public void testExplicitNullValues() {
    FunctionType fn = new FunctionBuilder(registry)
        .withName(null)
        .withSourceNode(null)
        .withParamsNode(null)
        .withReturnType(null)
        .withTypeOfThis(null)
        .withTemplateName(null)
        .build();

    assertNull(fn.getSource());
    assertNull(fn.getParametersNode());
    assertNull(fn.getTemplateTypeName());
  }

  @Test(timeout = 4000)
  public void testOverwritingReturnTypeWithInferredReturnType() {
    FunctionType fn = new FunctionBuilder(registry)
        .withReturnType(numberType)
        .withInferredReturnType(stringType)
        .build();

    assertTrue(stringType.isEquivalentTo(fn.getReturnType()));
    assertTrue("Last specified return type was inferred, flag must be true",
        fn.isReturnTypeInferred());
  }

  @Test(timeout = 4000)
  public void testOverwritingParamsNodeWinsOverParams() {
    FunctionParamBuilder paramBuilder = new FunctionParamBuilder(registry);
    paramBuilder.addRequiredParams(numberType);

    Node explicitNode = new Node(0);

    FunctionType fn = new FunctionBuilder(registry)
        .withParams(paramBuilder)
        .withParamsNode(explicitNode)
        .build();

    assertSame("withParamsNode should overwrite previous withParams",
        explicitNode, fn.getParametersNode());
  }

  @Test(timeout = 4000)
  public void testIdempotentFlagInvocations() {
    FunctionBuilder builder = new FunctionBuilder(registry)
        .forConstructor()
        .forConstructor()
        .forNativeType()
        .forNativeType();

    FunctionType fn = builder.build();
    assertTrue(fn.isConstructor());
    assertTrue(fn.isNativeObjectType());
  }

  // ===================================================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // ===================================================================================================

  /**
   * Targets the defect where copyFromOtherFunction omitted copying `inferredReturnType`.
   * When copying a function whose return type was inferred, the copied FunctionType incorrectly
   * had `isReturnTypeInferred() == false`, causing downstream compiler stages to treat inferred
   * types as explicitly declared types or vice-versa.
   */
  @Test(timeout = 4000)
  public void testDefectCopyFromOtherFunctionPreservesInferredReturnType() {
    FunctionType original = new FunctionBuilder(registry)
        .withInferredReturnType(numberType)
        .build();

    assertTrue("Precondition: original function must have an inferred return type",
        original.isReturnTypeInferred());

    FunctionType copied = new FunctionBuilder(registry)
        .copyFromOtherFunction(original)
        .build();

    assertTrue("Defect check: copied function must preserve inferred return type from original function",
        copied.isReturnTypeInferred());
  }

  /**
   * Targets the defect where copyFromOtherFunction does not reset `inferredReturnType` if the builder
   * was pre-configured with inferred return type, but the source function had an explicit return type.
   */
  @Test(timeout = 4000)
  public void testDefectCopyFromOtherFunctionResetsInferredReturnTypeWhenSourceIsNotInferred() {
    FunctionType explicitReturnFunc = new FunctionBuilder(registry)
        .withReturnType(numberType)
        .build();

    assertFalse("Precondition: source function must not have an inferred return type",
        explicitReturnFunc.isReturnTypeInferred());

    FunctionBuilder builder = new FunctionBuilder(registry)
        .withInferredReturnType(stringType);

    builder.copyFromOtherFunction(explicitReturnFunc);
    FunctionType copied = builder.build();

    assertFalse("Defect check: copied function must reset inferredReturnType to false when source is not inferred",
        copied.isReturnTypeInferred());
  }

  // ===================================================================================================
  // Partition D: Exception & Defensive Guard Paths
  // ===================================================================================================

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testWithParamsNullThrowsNullPointerException() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.withParams(null);
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testCopyFromOtherFunctionNullThrowsNullPointerException() {
    FunctionBuilder builder = new FunctionBuilder(registry);
    builder.copyFromOtherFunction(null);
  }

  // ===================================================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // ===================================================================================================

  @Test(timeout = 4000)
  public void testMultipleBuildInvocationsProduceDistinctInstances() {
    FunctionBuilder builder = new FunctionBuilder(registry)
        .withReturnType(numberType)
        .withName("Counter");

    FunctionType fn1 = builder.build();
    FunctionType fn2 = builder.build();

    assertNotNull(fn1);
    assertNotNull(fn2);
    assertNotSame("Consecutive build() calls must instantiate distinct FunctionType instances", fn1, fn2);
  }

  @Test(timeout = 4000)
  public void testMutatingBuilderAfterBuildDoesNotAffectPreviousInstance() {
    FunctionBuilder builder = new FunctionBuilder(registry)
        .withReturnType(numberType);

    FunctionType fn1 = builder.build();
    assertFalse(fn1.isConstructor());

    builder.forConstructor();
    FunctionType fn2 = builder.build();

    assertFalse("Previous instance should retain its constructor flag as false", fn1.isConstructor());
    assertTrue("New instance should reflect the updated constructor flag", fn2.isConstructor());
  }

  @Test(timeout = 4000)
  public void testOverrideCopiedFunctionProperties() {
    FunctionType baseFn = new FunctionBuilder(registry)
        .withReturnType(numberType)
        .forConstructor()
        .build();

    FunctionType modifiedFn = new FunctionBuilder(registry)
        .copyFromOtherFunction(baseFn)
        .withReturnType(stringType)
        .build();

    assertTrue("Original return type was NUMBER", numberType.isEquivalentTo(baseFn.getReturnType()));
    assertTrue("Overridden return type must be STRING", stringType.isEquivalentTo(modifiedFn.getReturnType()));
    assertTrue("Constructor flag copied from base must still be true", modifiedFn.isConstructor());
  }
}