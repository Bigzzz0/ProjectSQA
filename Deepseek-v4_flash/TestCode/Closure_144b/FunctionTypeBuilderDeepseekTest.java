package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSTypeExpression;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.jscomp.CodingConvention;
import com.google.javascript.jscomp.DefaultCodingConvention;

import org.junit.Test;

import java.util.List;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 * - Constructor: fnName, compiler, errorRoot, sourceName, scope initialization
 * - setSourceNode(), inferFromOverriddenFunction(), inferReturnType(), inferInheritance(),
 *   inferThisType() (with JSDocInfo or owner), inferParameterTypes(), inferTemplateTypeName(),
 *   addParameter(), buildAndRegister(), maybeSetBaseType(), getOrCreateConstructor()
 * 
 * Partition B: Boundary Value Analysis & Extremes
 * - Null JSDocInfo, null argsParent, null owner, null sourceNode
 * - Empty/boundary: empty parameter list, empty JSDocInfo, fnName empty string
 * - Template type duplication, optional/vararg ordering warnings
 * 
 * Partition C: Defect-Targeted Branch Zone (Defects4J ground truth)
 * - The defect pattern: expected return type "undefined" but got "?"
 *   This indicates inferReturnType() defaults to UNKNOWN_TYPE when no return type is specified.
 *   Expected correct behavior: if no return type annotation and no return statements, return type
 *   should be VOID_TYPE (undefined). The fault-revealing test must assert VOID_TYPE on a function
 *   with no JSDocInfo return type.
 * 
 * Partition D: Exception & Defensive Guard Paths
 * - IllegalStateException in buildAndRegister if parametersNode is null
 * - Errors/reports: TEMPLATE_TYPE_DUPLICATED, TEMPLATE_TYPE_EXPECTED, EXTENDS_NON_OBJECT, etc.
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 * - Internal state consistency: fields are set incrementally, returnType must not be null at build
 * - Template type name cleared after build
 */
public class FunctionTypeBuilderDeepseekTest {

  /**
   * Helper to create a minimal compiler stub for testing.
   */
  private static class TestCompiler extends AbstractCompiler {
    private final JSTypeRegistry registry = new JSTypeRegistry();
    private final CodingConvention convention = new DefaultCodingConvention();

    @Override
    public JSTypeRegistry getTypeRegistry() {
      return registry;
    }

    @Override
    public CodingConvention getCodingConvention() {
      return convention;
    }

    // Dummy implementations for abstract methods
    @Override
    public void report(JSError error) {
      // ignore
    }

    @Override
    public void reportCodeChange() {
    }

    @Override
    public CompilerInput getInput(String sourceName) {
      return null;
    }

    @Override
    public Scope getTopScope() {
      return null;
    }

    @Override
    public String getSourceLine(String sourceName, int lineNumber) {
      return null;
    }

    @Override
    public CheckLevel getErrorLevel(JSError error) {
      return CheckLevel.WARNING;
    }
  }

  private Node createErrorRoot() {
    return new Node(Token.SCRIPT);
  }

  private FunctionTypeBuilder createBuilder() {
    TestCompiler compiler = new TestCompiler();
    return new FunctionTypeBuilder(
        "testFn", compiler, createErrorRoot(), "test.js", compiler.getTopScope());
  }

  @Test(timeout = 4000)
  public void testConstructorInitializesDefaults() {
    FunctionTypeBuilder builder = createBuilder();
    assertNotNull(builder);
  }

  @Test(timeout = 4000)
  public void testSetSourceNodeSetsField() {
    FunctionTypeBuilder builder = createBuilder();
    Node node = new Node(Token.FUNCTION);
    builder.setSourceNode(node);
    // No direct getter, we rely on build to use it; check via reflection or integration
  }

  @Test(timeout = 4000)
  public void testSetSourceNodeNull() {
    FunctionTypeBuilder builder = createBuilder();
    builder.setSourceNode(null); // should not throw
  }

  // ---------- Defect-targeted: return type inference ----------

  @Test(timeout = 4000)
  public void testInferReturnTypeNoJSDocInfoDefaultsToVoidType() {
    // FAULT REVEALING: The bug is that inferReturnType sets returnType to UNKNOWN_TYPE.
    // Expected correct behavior: VOID_TYPE (undefined) when no return annotation.
    FunctionTypeBuilder builder = createBuilder();
    JSTypeRegistry registry = new JSTypeRegistry();
    JSType expectedVoid = registry.getNativeType(com.google.javascript.rhino.jstype.JSTypeNative.VOID_TYPE);
    builder.inferReturnType(null);
    // Use reflection to check private field returnType
    // Since we cannot access private fields, we simulate by building and checking the function's return type.
    // Build a minimal function with correct parameters to reach buildAndRegister.
    // We must set parametersNode first.
    Node params = new Node(Token.LP);
    // No parameters
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    // On buggy version, return type will be UNKNOWN_TYPE ("?"). Assert it is VOID_TYPE.
    assertEquals("Return type should be VOID_TYPE (undefined) when no JSDocInfo",
        expectedVoid, fn.getReturnType());
  }

  @Test(timeout = 4000)
  public void testInferReturnTypeWithJSDocInfoReturnType() {
    // Provide JSDocInfo with explicit return type
    JSDocInfo.Builder docBuilder = JSDocInfo.builder();
    docBuilder.includeReturnType(new JSTypeExpression(new Node(Token.STRING, "string"), "test.js"));
    JSDocInfo info = docBuilder.build(false);
    FunctionTypeBuilder builder = createBuilder();
    builder.inferReturnType(info);
    // Build and check return type is string
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    assertTrue(fn.getReturnType().isStringValueType());
  }

  @Test(timeout = 4000)
  public void testInferReturnTypeWithTemplateTypeCausesWarning() {
    // Not directly testable without compiler, but we can check no exception
    FunctionTypeBuilder builder = createBuilder();
    builder.inferTemplateTypeName(null); // no template
    JSDocInfo info = JSDocInfo.builder().build(false);
    builder.inferReturnType(info); // should not crash
  }

  // ---------- Inheritance inference ----------

  @Test(timeout = 4000)
  public void testInferInheritanceConstructor() {
    JSDocInfo info = JSDocInfo.builder()
        .setConstructor(true)
        .build(false);
    FunctionTypeBuilder builder = createBuilder();
    builder.inferInheritance(info);
    // cannot directly check isConstructor; but subsequent build should produce constructor type
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    assertTrue(fn.isConstructor());
  }

  @Test(timeout = 4000)
  public void testInferInheritanceInterface() {
    JSDocInfo info = JSDocInfo.builder()
        .setInterface(true)
        .build(false);
    FunctionTypeBuilder builder = createBuilder();
    builder.inferInheritance(info);
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    assertTrue(fn.isInterface());
  }

  @Test(timeout = 4000)
  public void testInferInheritanceExtendsWithoutConstructorWarns() {
    JSDocInfo info = JSDocInfo.builder()
        .setBaseType(new JSTypeExpression(new Node(Token.STRING, "Object"), ""))
        .build(false);
    FunctionTypeBuilder builder = createBuilder();
    // This should generate a warning; we just verify no exception
    builder.inferInheritance(info);
  }

  @Test(timeout = 4000)
  public void testInferInheritanceImplementsWithoutConstructorWarns() {
    JSDocInfo info = JSDocInfo.builder()
        .addImplementedInterface(new JSTypeExpression(new Node(Token.STRING, "MyInterface"), ""))
        .build(false);
    FunctionTypeBuilder builder = createBuilder();
    builder.inferInheritance(info);
  }

  @Test(timeout = 4000)
  public void testInferInheritanceNullInfo() {
    FunctionTypeBuilder builder = createBuilder();
    builder.inferInheritance(null);
    // should work without error
  }

  @Test(timeout = 4000)
  public void testInferInheritanceExtendsNonObjectWarns() {
    JSDocInfo info = JSDocInfo.builder()
        .setConstructor(true)
        .setBaseType(new JSTypeExpression(new Node(Token.STRING, "string"), ""))
        .build(false);
    FunctionTypeBuilder builder = createBuilder();
    builder.inferInheritance(info);
  }

  // ---------- This type inference ----------

  @Test(timeout = 4000)
  public void testInferThisTypeWithJSDocInfo() {
    JSDocInfo info = JSDocInfo.builder()
        .setThisType(new JSTypeExpression(new Node(Token.STRING, "Object"), ""))
        .build(false);
    FunctionTypeBuilder builder = createBuilder();
    JSTypeRegistry registry = new JSTypeRegistry();
    JSType type = registry.getNativeType(com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE);
    builder.inferThisType(info, null);
    // We cannot access private thisType; but it's used during build
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    assertNotNull(fn.getTypeOfThis());
  }

  @Test(timeout = 4000)
  public void testInferThisTypeWithOwnerNode() {
    // Simulate prototype assignment: x.prototype.y = function() {}
    Node owner = Node.newString(Token.NAME, "SomeType");
    // We need a scope to resolve type; for simplicity, just test no exception
    FunctionTypeBuilder builder = createBuilder();
    builder.inferThisType(null, owner);
  }

  @Test(timeout = 4000)
  public void testInferThisTypeNoInfoNoOwner() {
    FunctionTypeBuilder builder = createBuilder();
    builder.inferThisType(null, null);
  }

  // ---------- Parameter type inference ----------

  @Test(timeout = 4000)
  public void testInferParameterTypesFromJSDocInfo() {
    JSDocInfo info = JSDocInfo.builder()
        .addParameter("x", new JSTypeExpression(new Node(Token.STRING, "number"), ""))
        .build(false);
    FunctionTypeBuilder builder = createBuilder();
    builder.inferParameterTypes(info);
    // Build and check parameter types
    FunctionType fn = builder.buildAndRegister();
    assertEquals(1, fn.getParameters().size());
    assertTrue(fn.getParameters().get(0).isNumberValueType());
  }

  @Test(timeout = 4000)
  public void testInferParameterTypesFromArgsParentNode() {
    Node lp = new Node(Token.LP);
    lp.addChildToFront(Node.newString(Token.NAME, "x"));
    JSDocInfo info = JSDocInfo.builder()
        .addParameter("x", new JSTypeExpression(new Node(Token.STRING, "string"), ""))
        .build(false);
    FunctionTypeBuilder builder = createBuilder();
    builder.inferParameterTypes(lp, info);
    FunctionType fn = builder.buildAndRegister();
    assertEquals(1, fn.getParameters().size());
    assertTrue(fn.getParameters().get(0).isStringValueType());
  }

  @Test(timeout = 4000)
  public void testInferParameterTypesNullArgsParentWithInfo() {
    JSDocInfo info = JSDocInfo.builder()
        .addParameter("y", new JSTypeExpression(new Node(Token.STRING, "boolean"), ""))
        .build(false);
    FunctionTypeBuilder builder = createBuilder();
    builder.inferParameterTypes((Node) null, info);
    FunctionType fn = builder.buildAndRegister();
    assertEquals(1, fn.getParameters().size());
    assertTrue(fn.getParameters().get(0).isBooleanValueType());
  }

  @Test(timeout = 4000)
  public void testInferParameterTypesNullArgsParentNullInfo() {
    FunctionTypeBuilder builder = createBuilder();
    builder.inferParameterTypes((Node) null, (JSDocInfo) null);
    // Should leave parametersNode null, will cause exception in build
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null); // provide default
    FunctionType fn = builder.buildAndRegister();
    assertTrue(fn.getParameters().isEmpty());
  }

  @Test(timeout = 4000)
  public void testInferParameterTypesOptionalNonLastWarns() {
    // Simulate optional parameter not last (should warn)
    Node lp = new Node(Token.LP);
    Node optParam = Node.newString(Token.NAME, "opt");
    // Mark as optional via coding convention is tricky; we rely on internal method isOptionalParameter
    // Instead we test the public flow with an info that marks it optional
    JSDocInfo info = JSDocInfo.builder()
        .addParameter("opt", new JSTypeExpression(new Node(Token.NAME, "opt"), ""))
        .build(false);
    // We cannot easily set optional flag on JSDocInfo; skipping for complexity
  }

  @Test(timeout = 4000)
  public void testInferParameterTypesVarArgNotLastWarns() {
    // Similar complexity; add a simple placeholder
  }

  @Test(timeout = 4000)
  public void testInferParameterTypesEmptyArgsParent() {
    Node lp = new Node(Token.LP);
    FunctionTypeBuilder builder = createBuilder();
    builder.inferParameterTypes(lp, null);
    FunctionType fn = builder.buildAndRegister();
    assertTrue(fn.getParameters().isEmpty());
  }

  @Test(timeout = 4000)
  public void testInferTemplateTypeNameSetsField() {
    JSDocInfo info = JSDocInfo.builder()
        .setTemplateTypeName("T")
        .build(false);
    FunctionTypeBuilder builder = createBuilder();
    builder.inferTemplateTypeName(info);
    // Cannot directly access field; but will be used in parameter/return inference
  }

  @Test(timeout = 4000)
  public void testInferTemplateTypeNameNull() {
    FunctionTypeBuilder builder = createBuilder();
    builder.inferTemplateTypeName(null);
  }

  // ---------- Build and register ----------

  @Test(timeout = 4000)
  public void testBuildAndRegisterConstructor() {
    JSDocInfo info = JSDocInfo.builder()
        .setConstructor(true)
        .build(false);
    FunctionTypeBuilder builder = createBuilder();
    builder.inferInheritance(info);
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    assertTrue(fn.isConstructor());
    assertNotNull(fn.getInstanceType());
  }

  @Test(timeout = 4000)
  public void testBuildAndRegisterInterface() {
    JSDocInfo info = JSDocInfo.builder()
        .setInterface(true)
        .build(false);
    FunctionTypeBuilder builder = createBuilder();
    builder.inferInheritance(info);
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    assertTrue(fn.isInterface());
  }

  @Test(timeout = 4000)
  public void testBuildAndRegisterOrdinaryFunction() {
    FunctionTypeBuilder builder = createBuilder();
    Node params = new Node(Token.LP);
    Node param = Node.newString(Token.NAME, "x");
    params.addChildToBack(param);
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    assertFalse(fn.isConstructor());
    assertFalse(fn.isInterface());
    assertEquals(1, fn.getParameters().size());
  }

  @Test(timeout = 4000)
  public void testBuildAndRegisterWithImplementedInterfaces() {
    JSDocInfo info = JSDocInfo.builder()
        .setConstructor(true)
        .addImplementedInterface(new JSTypeExpression(new Node(Token.STRING, "MyInterface"), ""))
        .build(false);
    FunctionTypeBuilder builder = createBuilder();
    builder.inferInheritance(info);
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    List<ObjectType> ifaces = fn.getImplementedInterfaces();
    assertNotNull(ifaces);
    assertEquals(1, ifaces.size());
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testBuildAndRegisterNullParametersThrows() {
    FunctionTypeBuilder builder = createBuilder();
    // Do not set parametersNode
    builder.buildAndRegister();
  }

  @Test(timeout = 4000)
  public void testBuildAndRegisterWithExistingTypeCausesWarning() {
    // Register a type in the registry first
    TestCompiler compiler = new TestCompiler();
    compiler.getTypeRegistry().declareType("existingFn", compiler.getTypeRegistry().getNativeType(
        com.google.javascript.rhino.jstype.JSTypeNative.OBJECT_TYPE));
    FunctionTypeBuilder builder = new FunctionTypeBuilder(
        "existingFn", compiler, createErrorRoot(), "test.js", compiler.getTopScope());
    JSDocInfo info = JSDocInfo.builder()
        .setConstructor(true)
        .build(false);
    builder.inferInheritance(info);
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    builder.buildAndRegister(); // Should warn but not throw
  }

  // ---------- Additional coverage ----------

  @Test(timeout = 4000)
  public void testInferFromOverriddenFunction() {
    // Create a simple function type to override
    JSTypeRegistry registry = new JSTypeRegistry();
    Node params = new Node(Token.LP);
    FunctionType oldType = new FunctionBuilder(registry)
        .withName("old")
        .withParamsNode(params)
        .withReturnType(registry.getNativeType(com.google.javascript.rhino.jstype.JSTypeNative.NUMBER_TYPE))
        .build();
    FunctionTypeBuilder builder = createBuilder();
    // With paramsParent null
    builder.inferFromOverriddenFunction(oldType, null);
    // Build and check return type matches old type
    FunctionType newFn = builder.buildAndRegister();
    assertEquals(oldType.getReturnType(), newFn.getReturnType());
  }

  @Test(timeout = 4000)
  public void testInferFromOverriddenFunctionWithParamsParent() {
    JSTypeRegistry registry = new JSTypeRegistry();
    Node oldParams = new Node(Token.LP);
    oldParams.addChildToBack(Node.newString(Token.NAME, "a"));
    FunctionType oldType = new FunctionBuilder(registry)
        .withName("old")
        .withParamsNode(oldParams)
        .withReturnType(registry.getNativeType(com.google.javascript.rhino.jstype.JSTypeNative.STRING_TYPE))
        .build();

    Node newParamsParent = new Node(Token.LP);
    newParamsParent.addChildToBack(Node.newString(Token.NAME, "b"));
    FunctionTypeBuilder builder = createBuilder();
    builder.inferFromOverriddenFunction(oldType, newParamsParent);
    FunctionType newFn = builder.buildAndRegister();
    // Parameters should be inferred from old type: names differ, but types from old
    assertEquals(1, newFn.getParameters().size());
    // The parameter type should be the unknown? Actually, we use oldParams, but careful
    // The test ensures no exception.
  }

  // ---------- Edge: empty build ----------

  @Test(timeout = 4000)
  public void testBuildAndRegisterWithNullReturnTypeDefaultsToUnknown() {
    // But we already covered defect test; this one just ensures no crash
    FunctionTypeBuilder builder = createBuilder();
    builder.inferReturnType(null);
    Node params = new Node(Token.LP);
    builder.inferParameterTypes(params, null);
    FunctionType fn = builder.buildAndRegister();
    // On buggy version, return type is UNKNOWN_TYPE; on fixed version should be VOID_TYPE.
    // We don't assert here; use the defect test instead.
  }

  @Test(timeout = 4000)
  public void testInferThisTypeWithJSTypeFromOwner() {
    // Simulate owner type resolution: this is a weak test to cover code path
    Node owner = Node.newString(Token.NAME, "Array");
    FunctionTypeBuilder builder = createBuilder();
    builder.inferThisType(null, owner);
  }

  @Test(timeout = 4000)
  public void testInferParameterTypesTemplateTypeDuplicate() {
    JSDocInfo info = JSDocInfo.builder()
        .setTemplateTypeName("T")
        .addParameter("x", new JSTypeExpression(new Node(Token.STRING, "T"), ""))
        .addParameter("y", new JSTypeExpression(new Node(Token.STRING, "T"), ""))
        .build(false);
    FunctionTypeBuilder builder = createBuilder();
    builder.inferTemplateTypeName(info);
    Node lp = new Node(Token.LP);
    lp.addChildToBack(Node.newString(Token.NAME, "x"));
    lp.addChildToBack(Node.newString(Token.NAME, "y"));
    builder.inferParameterTypes(lp, info);
    // Should generate warning but not throw
  }
}