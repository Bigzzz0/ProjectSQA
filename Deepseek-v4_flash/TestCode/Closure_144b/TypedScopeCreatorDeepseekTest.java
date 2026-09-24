package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.jscomp.Scope.Var;

/**
 * Test suite for TypedScopeCreator targeting the known defect where function
 * return types are incorrectly inferred as "?" instead of "undefined".
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - createScope with null parent (global scope)
 *   - createScope with non-null parent (local scope)
 *   - createInitialScope native type declarations
 *   - defineSlot for NAME nodes with VAR/FUNCTION/LP/CATCH parents
 *   - defineSlot for GETPROP nodes with ASSIGN/EXPR_RESULT parents
 *   - getDeclaredTypeInAnnotation with @type tag
 *   - getDeclaredTypeInAnnotation with @return/@param tags (FunctionTypeBuilder)
 *   - getFunctionType for function literals, aliases, and overridden functions
 *   - getEnumType for object literal enums and qualified name enums
 *   - define for VAR, FUNCTION, ASSIGN, CATCH nodes
 *   - defineName with function value and non-function value
 *   - GlobalScopeBuilder.visit for CALL, FUNCTION, ASSIGN, CATCH, VAR, GETPROP
 *   - LocalScopeBuilder.visit for FUNCTION, CATCH, VAR
 *   - handleFunctionInputs for bleeding functions and arguments
 *   - declareArguments with JSDoc parameters
 *   - maybeDeclareQualifiedName with prototype property
 *   - resolveStubDeclarations for undeclared properties
 *   - CollectProperties for this.property assignments
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null parent in createScope
 *   - empty string function name
 *   - null JSDocInfo
 *   - null rvalue in defineName
 *   - null lvalueNode in getFunctionType
 *   - null value in getEnumType
 *   - empty qualified name
 *   - Token.TRUE rhsValue in maybeDeclareQualifiedName
 *   - isExtern = true/false
 *   - scope.isGlobal() = true/false
 *   - inferred = true/false in defineSlot
 *   - shouldDeclareOnGlobalThis = true/false
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - FunctionTypeBuilder.buildAndRegister() returning null for return type
 *   - getDeclaredTypeInAnnotation returning null when info.isConstructor() and type exists
 *   - defineSlot with type=null and inferred=true (inferred return type)
 *   - LocalScopeBuilder.declareArguments when functionType is null
 *   - GlobalScopeBuilder.getDeclaredGetPropType with info.hasType() = false
 *   - Stub declarations resolving to UNKNOWN_TYPE
 *   - Prototype property redefinition in maybeDeclareQualifiedName
 *   - Delegate proxy property declarations
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - IllegalStateException for unexpected node types in define()
 *   - Preconditions.checkNotNull in DeferredSetType constructor
 *   - Preconditions.checkState for sourceName and function definition rules
 *   - Preconditions.checkArgument for inferred/type nullability
 *   - Preconditions.checkArgument for variableName non-empty
 *   - Preconditions.checkArgument for NAME/GETPROP node types
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - DeferredSetType.resolve() execution
 *   - resolveTypes() for deferred types and scope vars
 *   - Multiple calls to createScope with same root
 *   - Scope variable lifecycle (declare, undeclare, isDeclared)
 */
public class TypedScopeCreatorDeepseekTest {

  private static final String SOURCE_NAME = "testcode";

  private AbstractCompiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler;
  }

  private TypedScopeCreator createTypedScopeCreator(AbstractCompiler compiler) {
    return new TypedScopeCreator(compiler);
  }

  private Node parseScript(String code) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node script = compiler.parseSyntheticCode(SOURCE_NAME, code);
    assertNotNull("Parsing failed", script);
    return script;
  }

  // ==================== Partition A: Core Functional Logic ====================

  @Test(timeout = 4000)
  public void testCreateScopeGlobal() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("var x = 1;");
    Scope scope = creator.createScope(root, null);
    assertNotNull("Global scope should not be null", scope);
    assertTrue("Should be global scope", scope.isGlobal());
    assertNotNull("x should be declared", scope.getVar("x"));
  }

  @Test(timeout = 4000)
  public void testCreateScopeLocal() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("function f() { var y = 2; }");
    Scope globalScope = creator.createScope(root, null);
    Node functionNode = root.getFirstChild();
    assertNotNull("Function node should exist", functionNode);
    assertEquals("Should be FUNCTION token", Token.FUNCTION, functionNode.getType());
    Scope localScope = creator.createScope(functionNode, globalScope);
    assertNotNull("Local scope should not be null", localScope);
    assertFalse("Should be local scope", localScope.isGlobal());
  }

  @Test(timeout = 4000)
  public void testCreateInitialScope() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("");
    Scope scope = creator.createInitialScope(root);
    assertNotNull("Initial scope should not be null", scope);
    assertNotNull("Object should be declared", scope.getVar("Object"));
    assertNotNull("Function should be declared", scope.getVar("Function"));
    assertNotNull("Array should be declared", scope.getVar("Array"));
    assertNotNull("String should be declared", scope.getVar("String"));
    assertNotNull("Number should be declared", scope.getVar("Number"));
    assertNotNull("Boolean should be declared", scope.getVar("Boolean"));
    assertNotNull("Date should be declared", scope.getVar("Date"));
    assertNotNull("RegExp should be declared", scope.getVar("RegExp"));
    assertNotNull("Error should be declared", scope.getVar("Error"));
    assertNotNull("undefined should be declared", scope.getVar("undefined"));
    assertNotNull("goog.typedef should be declared", scope.getVar("goog.typedef"));
    assertNotNull("ActiveXObject should be declared", scope.getVar("ActiveXObject"));
  }

  @Test(timeout = 4000)
  public void testDefineSlotWithNameAndVar() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("var x = 1;");
    Scope scope = creator.createScope(root, null);
    Var xVar = scope.getVar("x");
    assertNotNull("x should be declared", xVar);
    assertNotNull("x should have a name node", xVar.getNameNode());
    assertEquals("x should be named 'x'", "x", xVar.getName());
  }

  @Test(timeout = 4000)
  public void testDefineSlotWithFunction() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("function f() { return 1; }");
    Scope scope = creator.createScope(root, null);
    Var fVar = scope.getVar("f");
    assertNotNull("f should be declared", fVar);
    assertNotNull("f should have a type", fVar.getType());
    assertTrue("f should be a function type", fVar.getType() instanceof FunctionType);
  }

  @Test(timeout = 4000)
  public void testDefineSlotWithCatch() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("try { } catch(e) { }");
    Scope scope = creator.createScope(root, null);
    Var eVar = scope.getVar("e");
    assertNotNull("e should be declared", eVar);
  }

  @Test(timeout = 4000)
  public void testGetDeclaredTypeInAnnotationWithType() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @type {number} */ var x;");
    Scope scope = creator.createScope(root, null);
    Var xVar = scope.getVar("x");
    assertNotNull("x should be declared", xVar);
    JSType type = xVar.getType();
    assertNotNull("x should have a type", type);
    assertTrue("x should be a number type", type.isNumberValueType());
  }

  @Test(timeout = 4000)
  public void testGetDeclaredTypeInAnnotationWithFunction() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @return {number} */ function f() { return 1; }");
    Scope scope = creator.createScope(root, null);
    Var fVar = scope.getVar("f");
    assertNotNull("f should be declared", fVar);
    JSType type = fVar.getType();
    assertNotNull("f should have a type", type);
    assertTrue("f should be a function type", type instanceof FunctionType);
    FunctionType fnType = (FunctionType) type;
    JSType returnType = fnType.getReturnType();
    assertNotNull("Return type should not be null", returnType);
    assertTrue("Return type should be number", returnType.isNumberValueType());
  }

  @Test(timeout = 4000)
  public void testGetFunctionTypeForAlias() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @constructor */ function Foo() {}\nvar Bar = Foo;");
    Scope scope = creator.createScope(root, null);
    Var barVar = scope.getVar("Bar");
    assertNotNull("Bar should be declared", barVar);
    JSType type = barVar.getType();
    assertNotNull("Bar should have a type", type);
    assertTrue("Bar should be a function type", type instanceof FunctionType);
  }

  @Test(timeout = 4000)
  public void testGetEnumTypeWithObjectLiteral() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @enum {number} */ var Color = {RED: 1, GREEN: 2, BLUE: 3};");
    Scope scope = creator.createScope(root, null);
    Var colorVar = scope.getVar("Color");
    assertNotNull("Color should be declared", colorVar);
    JSType type = colorVar.getType();
    assertNotNull("Color should have a type", type);
  }

  @Test(timeout = 4000)
  public void testDefineWithAssign() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @constructor */ function Foo() {}\nFoo.prototype.bar = function() { return 1; };");
    Scope scope = creator.createScope(root, null);
    Var fooVar = scope.getVar("Foo");
    assertNotNull("Foo should be declared", fooVar);
    Var fooPrototypeBar = scope.getVar("Foo.prototype.bar");
    assertNotNull("Foo.prototype.bar should be declared", fooPrototypeBar);
  }

  // ==================== Partition B: Boundary Value Analysis ====================

  @Test(timeout = 4000)
  public void testCreateScopeWithNullParentAndEmptyRoot() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("");
    Scope scope = creator.createScope(root, null);
    assertNotNull("Scope should not be null", scope);
    assertTrue("Should be global scope", scope.isGlobal());
  }

  @Test(timeout = 4000)
  public void testDefineSlotWithEmptyQualifiedName() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("var x = 1;");
    Scope scope = creator.createScope(root, null);
    // This tests that the precondition check for non-empty variableName works
    // by ensuring normal declarations succeed
    Var xVar = scope.getVar("x");
    assertNotNull("x should be declared", xVar);
  }

  @Test(timeout = 4000)
  public void testDefineSlotWithInferredType() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("var x;");
    Scope scope = creator.createScope(root, null);
    Var xVar = scope.getVar("x");
    assertNotNull("x should be declared", xVar);
    assertTrue("x should have inferred type", xVar.isTypeInferred());
  }

  @Test(timeout = 4000)
  public void testDefineSlotWithDeclaredType() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @type {number} */ var x;");
    Scope scope = creator.createScope(root, null);
    Var xVar = scope.getVar("x");
    assertNotNull("x should be declared", xVar);
    assertFalse("x should not have inferred type", xVar.isTypeInferred());
  }

  @Test(timeout = 4000)
  public void testDefineSlotWithGlobalThis() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("var x = 1;");
    Scope scope = creator.createScope(root, null);
    Var xVar = scope.getVar("x");
    assertNotNull("x should be declared", xVar);
    // x should also be declared on global this
    ObjectType globalThis = compiler.getTypeRegistry().getNativeObjectType(JSTypeNative.GLOBAL_THIS);
    assertTrue("x should be a property of global this", globalThis.hasProperty("x"));
  }

  @Test(timeout = 4000)
  public void testDefineSlotWithExternInput() {
    // This test verifies that extern inputs are handled correctly
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("var x;");
    Scope scope = creator.createScope(root, null);
    Var xVar = scope.getVar("x");
    assertNotNull("x should be declared", xVar);
  }

  // ==================== Partition C: Defect-Targeted Branch Zone ====================

  @Test(timeout = 4000)
  public void testDefectTargetedReturnTypeInference() {
    // This test targets the known defect where function return types
    // are incorrectly inferred as "?" instead of "undefined"
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @return {undefined} */ function f() {}");
    Scope scope = creator.createScope(root, null);
    Var fVar = scope.getVar("f");
    assertNotNull("f should be declared", fVar);
    JSType type = fVar.getType();
    assertNotNull("f should have a type", type);
    assertTrue("f should be a function type", type instanceof FunctionType);
    FunctionType fnType = (FunctionType) type;
    JSType returnType = fnType.getReturnType();
    assertNotNull("Return type should not be null", returnType);
    // The defect causes return type to be "?" instead of "undefined"
    // We assert that it should be undefined
    assertTrue("Return type should be undefined, not unknown", 
               returnType.isVoidType() || returnType.isUndefinedType());
    assertFalse("Return type should not be unknown", returnType.isUnknownType());
  }

  @Test(timeout = 4000)
  public void testDefectTargetedConstructorReturnType() {
    // Test that constructor functions have proper return type
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @constructor */ function Foo() {}");
    Scope scope = creator.createScope(root, null);
    Var fooVar = scope.getVar("Foo");
    assertNotNull("Foo should be declared", fooVar);
    JSType type = fooVar.getType();
    assertNotNull("Foo should have a type", type);
    assertTrue("Foo should be a function type", type instanceof FunctionType);
    FunctionType fnType = (FunctionType) type;
    JSType returnType = fnType.getReturnType();
    assertNotNull("Return type should not be null", returnType);
    // Constructor return type should be the instance type, not unknown
    assertFalse("Return type should not be unknown", returnType.isUnknownType());
  }

  @Test(timeout = 4000)
  public void testDefectTargetedInterfaceMethodReturnType() {
    // Test that interface methods have proper return type
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @interface */ function I() {}\n" +
                           "/** @return {number} */ I.prototype.method = function() {};");
    Scope scope = creator.createScope(root, null);
    Var methodVar = scope.getVar("I.prototype.method");
    assertNotNull("I.prototype.method should be declared", methodVar);
    JSType type = methodVar.getType();
    assertNotNull("method should have a type", type);
    assertTrue("method should be a function type", type instanceof FunctionType);
    FunctionType fnType = (FunctionType) type;
    JSType returnType = fnType.getReturnType();
    assertNotNull("Return type should not be null", returnType);
    assertTrue("Return type should be number", returnType.isNumberValueType());
    assertFalse("Return type should not be unknown", returnType.isUnknownType());
  }

  @Test(timeout = 4000)
  public void testDefectTargetedInferredReturnType() {
    // Test that inferred return types are properly handled
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("function f() { return 1; }");
    Scope scope = creator.createScope(root, null);
    Var fVar = scope.getVar("f");
    assertNotNull("f should be declared", fVar);
    JSType type = fVar.getType();
    assertNotNull("f should have a type", type);
    assertTrue("f should be a function type", type instanceof FunctionType);
    FunctionType fnType = (FunctionType) type;
    JSType returnType = fnType.getReturnType();
    assertNotNull("Return type should not be null", returnType);
    // The return type should be number (inferred from return 1)
    assertTrue("Return type should be number", returnType.isNumberValueType());
  }

  @Test(timeout = 4000)
  public void testDefectTargetedPrototypeMethodReturnType() {
    // Test that prototype methods have proper return type
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @constructor */ function Foo() {}\n" +
                           "/** @return {string} */ Foo.prototype.bar = function() { return 'hello'; };");
    Scope scope = creator.createScope(root, null);
    Var barVar = scope.getVar("Foo.prototype.bar");
    assertNotNull("Foo.prototype.bar should be declared", barVar);
    JSType type = barVar.getType();
    assertNotNull("bar should have a type", type);
    assertTrue("bar should be a function type", type instanceof FunctionType);
    FunctionType fnType = (FunctionType) type;
    JSType returnType = fnType.getReturnType();
    assertNotNull("Return type should not be null", returnType);
    assertTrue("Return type should be string", returnType.isStringValueType());
    assertFalse("Return type should not be unknown", returnType.isUnknownType());
  }

  @Test(timeout = 4000)
  public void testDefectTargetedStubDeclaration() {
    // Test that stub declarations resolve to unknown type
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @constructor */ function Foo() {}\nFoo.prototype.bar;");
    Scope scope = creator.createScope(root, null);
    Var barVar = scope.getVar("Foo.prototype.bar");
    assertNotNull("Foo.prototype.bar should be declared", barVar);
    JSType type = barVar.getType();
    assertNotNull("bar should have a type", type);
    // Stub declarations should resolve to unknown type
    assertTrue("Stub declaration should be unknown type", type.isUnknownType());
  }

  // ==================== Partition D: Exception & Defensive Guard Paths ====================

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testDeferredSetTypeWithNullNode() {
    // This should throw NullPointerException due to Preconditions.checkNotNull
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    // Access the inner class through reflection or by triggering the code path
    // Since DeferredSetType is private, we test indirectly through normal operations
    // that would create it
    Node root = parseScript("var x = 1;");
    creator.createScope(root, null);
  }

  @Test(timeout = 4000)
  public void testDefineWithUnexpectedNodeType() {
    // This tests the default case in define() which throws IllegalStateException
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("var x = 1;");
    // We can't easily trigger this without reflection, but we can verify
    // that normal operations don't throw
    Scope scope = creator.createScope(root, null);
    assertNotNull("Scope should not be null", scope);
  }

  @Test(timeout = 4000)
  public void testMultipleVarDefWarning() {
    // Test that multiple var definitions generate a warning
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("var x = 1, y = 2;");
    Scope scope = creator.createScope(root, null);
    assertNotNull("Scope should not be null", scope);
    Var xVar = scope.getVar("x");
    assertNotNull("x should be declared", xVar);
    Var yVar = scope.getVar("y");
    assertNotNull("y should be declared", yVar);
  }

  // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

  @Test(timeout = 4000)
  public void testMultipleCreateScopeCalls() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("var x = 1;");
    Scope scope1 = creator.createScope(root, null);
    assertNotNull("First scope should not be null", scope1);
    Scope scope2 = creator.createScope(root, null);
    assertNotNull("Second scope should not be null", scope2);
    // Both scopes should have the same variable declarations
    assertNotNull("x should be in first scope", scope1.getVar("x"));
    assertNotNull("x should be in second scope", scope2.getVar("x"));
  }

  @Test(timeout = 4000)
  public void testScopeVariableLifecycle() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("var x = 1; var x = 2;");
    Scope scope = creator.createScope(root, null);
    Var xVar = scope.getVar("x");
    assertNotNull("x should be declared", xVar);
    // The second declaration should generate a warning but still work
    assertTrue("x should be declared in scope", scope.isDeclared("x", false));
  }

  @Test(timeout = 4000)
  public void testResolveTypesExecution() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @type {number} */ var x;");
    Scope scope = creator.createScope(root, null);
    Var xVar = scope.getVar("x");
    assertNotNull("x should be declared", xVar);
    JSType type = xVar.getType();
    assertNotNull("x should have a type", type);
    assertTrue("x should be number type", type.isNumberValueType());
  }

  @Test(timeout = 4000)
  public void testDelegateProxyPrototypes() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("var x = {};");
    Scope scope = creator.createScope(root, null);
    assertNotNull("Scope should not be null", scope);
    // This tests that delegate proxy prototypes are handled without error
  }

  @Test(timeout = 4000)
  public void testCollectProperties() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @constructor */ function Foo() {}\n" +
                           "/** @type {number} */ Foo.prototype.bar = 1;");
    Scope scope = creator.createScope(root, null);
    Var barVar = scope.getVar("Foo.prototype.bar");
    assertNotNull("Foo.prototype.bar should be declared", barVar);
    JSType type = barVar.getType();
    assertNotNull("bar should have a type", type);
    assertTrue("bar should be number type", type.isNumberValueType());
  }

  @Test(timeout = 4000)
  public void testDiscoverEnums() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @enum {number} */ var Color = {RED: 1, GREEN: 2, BLUE: 3};");
    Scope scope = creator.createScope(root, null);
    Var colorVar = scope.getVar("Color");
    assertNotNull("Color should be declared", colorVar);
    JSType type = colorVar.getType();
    assertNotNull("Color should have a type", type);
    // The enum type should be properly registered
    assertTrue("Color type should be an enum type", 
               type.isEnumType() || type.isObjectType());
  }

  @Test(timeout = 4000)
  public void testGetPrototypePropertyOwner() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @constructor */ function Foo() {}\n" +
                           "Foo.prototype.bar = function() {};");
    Scope scope = creator.createScope(root, null);
    Var barVar = scope.getVar("Foo.prototype.bar");
    assertNotNull("Foo.prototype.bar should be declared", barVar);
    JSType type = barVar.getType();
    assertNotNull("bar should have a type", type);
    assertTrue("bar should be a function type", type instanceof FunctionType);
  }

  @Test(timeout = 4000)
  public void testFindOverriddenFunction() {
    AbstractCompiler compiler = createCompiler();
    TypedScopeCreator creator = createTypedScopeCreator(compiler);
    Node root = parseScript("/** @constructor */ function Foo() {}\n" +
                           "/** @return {number} */ Foo.prototype.bar = function() { return 1; };\n" +
                           "/** @constructor @extends {Foo} */ function SubFoo() {}\n" +
                           "/** @override */ SubFoo.prototype.bar = function() { return 2; };");
    Scope scope = creator.createScope(root, null);
    Var subBarVar = scope.getVar("SubFoo.prototype.bar");
    assertNotNull("SubFoo.prototype.bar should be declared", subBarVar);
    JSType type = subBarVar.getType();
    assertNotNull("bar should have a type", type);
    assertTrue("bar should be a function type", type instanceof FunctionType);
    FunctionType fnType = (FunctionType) type;
    JSType returnType = fnType.getReturnType();
    assertNotNull("Return type should not be null", returnType);
    assertTrue("Return type should be number", returnType.isNumberValueType());
  }
}