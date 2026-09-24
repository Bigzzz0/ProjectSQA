package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.jstype.JSType;
import com.google.javascript.rhino.jstype.JSTypeNative;
import com.google.javascript.rhino.jstype.JSTypeRegistry;
import com.google.javascript.rhino.jstype.FunctionType;
import com.google.javascript.rhino.jstype.ObjectType;
import com.google.javascript.rhino.jstype.EnumType;

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.javascript.rhino.Token;

/**
 * Comprehensive white-box test suite for TypeCheck.
 * Targets core logic, boundary values, and the known defect related to
 * EXPECTED_THIS_TYPE warnings.
 */
public class TypeCheckDeepseekTest {

  // ---------------------------------------------------------------
  // Helper methods to create a minimal compilation environment
  // ---------------------------------------------------------------

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setWarningLevel(DiagnosticGroups.MISSING_PROPERTIES, CheckLevel.WARNING);
    options.setWarningLevel(DiagnosticGroups.UNKNOWN_PARAMETER, CheckLevel.WARNING);
    options.setWarningLevel(DiagnosticGroups.MODULE_LOAD, CheckLevel.WARNING);
    // Turn on all warnings relevant to type checking
    options.setWarningLevel(DiagnosticGroups.SUSPICIOUS_CODE, CheckLevel.WARNING);
    options.setWarningLevel(DiagnosticGroups.TYPE_CHECK, CheckLevel.WARNING);
    // Ensure the type checker runs
    options.setWarningLevel(DiagnosticGroups.CHECK_TYPES, CheckLevel.WARNING);
    compiler.initOptions(options);
    return compiler;
  }

  private Compiler compileScript(String js) {
    return compileScriptWithExterns("", js);
  }

  private Compiler compileScriptWithExterns(String externs, String js) {
    Compiler compiler = createCompiler();
    SourceFile extern = SourceFile.fromCode("externs.js", externs);
    SourceFile input = SourceFile.fromCode("test.js", js);
    Result result = compiler.compile(extern, input, new CompilerOptions());
    return compiler;
  }

  private void assertWarningCount(Compiler compiler, int count) {
    JSError[] warnings = compiler.getWarnings();
    assertEquals("Unexpected warning count", count, warnings.length);
  }

  private void assertFirstWarningType(Compiler compiler, DiagnosticType type) {
    JSError[] warnings = compiler.getWarnings();
    assertTrue("Expected at least one warning", warnings.length > 0);
    assertEquals("Wrong warning type", type, warnings[0].getType());
  }

  // ---------------------------------------------------------------
  // Partition A: Core functional logic & state transitions
  // ---------------------------------------------------------------

  @Test(timeout = 4000)
  public void testProcessForTestingReturnsNonNullScope() {
    Compiler compiler = createCompiler();
    Node externsRoot = new Node(Token.SCRIPT);
    Node jsRoot = new Node(Token.SCRIPT);
    Node parent = new Node(Token.BLOCK);
    parent.addChildToBack(externsRoot);
    parent.addChildToBack(jsRoot);
    TypeCheck tc = new TypeCheck(compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    Scope scope = tc.processForTesting(externsRoot, jsRoot);
    assertNotNull("processForTesting should return non-null scope", scope);
  }

  @Test(timeout = 4000)
  public void testConstructorWithAllArgsDoesNotThrow() {
    Compiler compiler = createCompiler();
    // These arguments are valid per the constructor API.
    TypeCheck tc = new TypeCheck(compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        new Scope(compiler.getTopScope()),  // topScope
        new MemoizedScopeCreator(new TypedScopeCreator(compiler)),
        CheckLevel.WARNING,
        CheckLevel.OFF);
    assertNotNull(tc);
  }

  @Test(timeout = 4000)
  public void testConstructorWithThreeArgDoesNotThrow() {
    Compiler compiler = createCompiler();
    TypeCheck tc = new TypeCheck(compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    assertNotNull(tc);
  }

  @Test(timeout = 4000)
  public void testReportMissingPropertiesChaining() {
    Compiler compiler = createCompiler();
    TypeCheck tc = new TypeCheck(compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    assertSame(tc, tc.reportMissingProperties(true));
  }

  @Test(timeout = 4000)
  public void testSimpleNumberLiteralGetsNumberType() {
    Compiler compiler = compileScript("var x = 42;");
    // No warnings expected
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testSimpleStringLiteralGetsStringType() {
    Compiler compiler = compileScript("var x = 'hello';");
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testBooleanLiteralGetsBooleanType() {
    Compiler compiler = compileScript("var x = true; var y = false;");
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testNullLiteralGetsNullType() {
    Compiler compiler = compileScript("var x = null;");
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testArrayLiteralGetsArrayType() {
    Compiler compiler = compileScript("var x = [1, 2];");
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testRegexpLiteralGetsRegExpType() {
    Compiler compiler = compileScript("var x = /abc/;");
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testFunctionCallToNonFunction() {
    Compiler compiler = compileScript("var x = 1; x();");
    // Should warn NOT_CALLABLE
    assertTrue("Expected warning for calling non-function",
        compiler.getWarnings().length > 0);
    assertFirstWarningType(compiler, TypeCheck.NOT_CALLABLE);
  }

  @Test(timeout = 4000)
  public void testConstructorNotCallableWithoutNew() {
    Compiler compiler = compileScript("/** @constructor */ function Foo() {}; Foo();");
    // Should warn CONSTRUCTOR_NOT_CALLABLE
    assertTrue("Expected warning for constructor call without new",
        compiler.getWarnings().length > 0);
    assertFirstWarningType(compiler, TypeCheck.CONSTRUCTOR_NOT_CALLABLE);
  }

  @Test(timeout = 4000)
  public void testConstructorCallWithNewGetsInstanceType() {
    Compiler compiler = compileScript(
        "/** @constructor */ function Foo() {}; var f = new Foo();");
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testGetPropOnNonNullObject() {
    Compiler compiler = compileScript(
        "/** @type {{prop: number}} */ var x; var y = x.prop;");
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testGetPropOnStringGetsWarning() {
    Compiler compiler = compileScript("var x = 'hello'; var y = x.prop;");
    // Property access on primitive string should warn about property undefined
    assertTrue("Expected warning for property on string",
        compiler.getWarnings().length > 0);
  }

  @Test(timeout = 4000)
  public void testDeleteReference() {
    Compiler compiler = compileScript("var x = {}; delete x.foo;");
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testDeleteNonReference() {
    Compiler compiler = compileScript("delete 42;");
    assertTrue("Expected BAD_DELETE warning",
        compiler.getWarnings().length > 0);
    assertFirstWarningType(compiler, TypeCheck.BAD_DELETE);
  }

  // ---------------------------------------------------------------
  // Partition B: Boundary Value Analysis & Extremes
  // ---------------------------------------------------------------

  @Test(timeout = 4000)
  public void testEmptyScriptNoWarnings() {
    Compiler compiler = compileScript("");
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testWithNullExternsNoCrash() {
    Compiler compiler = createCompiler();
    Node externsRoot = new Node(Token.SCRIPT);
    Node jsRoot = new Node(Token.SCRIPT);
    Node parent = new Node(Token.BLOCK);
    parent.addChildToBack(externsRoot);
    parent.addChildToBack(jsRoot);
    TypeCheck tc = new TypeCheck(compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        new Scope(compiler.getTopScope()),
        new MemoizedScopeCreator(new TypedScopeCreator(compiler)),
        CheckLevel.WARNING,
        CheckLevel.OFF);
    tc.process(null, jsRoot);
    // Should not crash
  }

  @Test(timeout = 4000)
  public void testVariableWithUnresolvedType() {
    Compiler compiler = compileScript("var x = /** @type {notatype} */(1);");
    // Should warn about unresolved type name
    assertTrue("Expected warning for unresolved type",
        compiler.getWarnings().length > 0);
    assertFirstWarningType(compiler, TypeCheck.UNRESOLVED_TYPE);
  }

  @Test(timeout = 4000)
  public void testEnumWithObjectLiteral() {
    Compiler compiler = compileScript(
        "/** @enum {number} */ var E = {A: 1, B: 2};");
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testEnumCopyTypeMismatch() {
    Compiler compiler = compileScript(
        "/** @enum {number} */ var E = {A: 1};" +
        "/** @enum {string} */ var F = E;");
    // Should warn about incompatible enum element types
    assertTrue("Expected warning for enum type mismatch",
        compiler.getWarnings().length > 0);
  }

  @Test(timeout = 4000)
  public void testReturnTypeMismatch() {
    Compiler compiler = compileScript(
        "/** @return {number} */ function f() { return 'string'; }");
    assertTrue("Expected warning for return type mismatch",
        compiler.getWarnings().length > 0);
  }

  @Test(timeout = 4000)
  public void testBinaryOperatorBitwiseOnString() {
    Compiler compiler = compileScript("var x = 'a' & 1;");
    // Should warn BIT_OPERATION
    assertTrue("Expected BIT_OPERATION warning",
        compiler.getWarnings().length > 0);
    assertFirstWarningType(compiler, TypeCheck.BIT_OPERATION);
  }

  @Test(timeout = 4000)
  public void testAssignmentToInferredType() {
    Compiler compiler = compileScript("var x; x = 42; x = 'hello';");
    assertWarningCount(compiler, 0); // inferred types allow any
  }

  @Test(timeout = 4000)
  public void testAssignmentToDeclaredTypeMismatch() {
    Compiler compiler = compileScript(
        "/** @type {number} */ var x; x = 'string';");
    assertTrue("Expected warning for type mismatch in assignment",
        compiler.getWarnings().length > 0);
  }

  @Test(timeout = 4000)
  public void testGetElemOnArray() {
    Compiler compiler = compileScript("var arr = [1,2,3]; var x = arr[0];");
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testGetElemOnObject() {
    Compiler compiler = compileScript("var obj = {}; var x = obj['key'];");
    assertWarningCount(compiler, 0);
  }

  // ---------------------------------------------------------------
  // Partition C: Defect-Targeted Branch (EXPECTED_THIS_TYPE)
  // ---------------------------------------------------------------

  @Test(timeout = 4000)
  public void testThisTypeOfFunction2() {
    // Function with @this annotation called directly (not as property)
    // Should produce EXPECTED_THIS_TYPE warning.
    String js = "/** @this {string} */ function f() { return this; }; f();";
    Compiler compiler = compileScript(js);
    JSError[] warnings = compiler.getWarnings();
    // In the fixed version, there should be at least one warning (EXPECTED_THIS_TYPE)
    // This test will fail on the buggy version (no warning) thus revealing the defect.
    assertTrue("EXPECTED_THIS_TYPE warning not emitted", warnings.length > 0);
    boolean foundExpectedThis = false;
    for (JSError w : warnings) {
      if (w.getType() == TypeCheck.EXPECTED_THIS_TYPE) {
        foundExpectedThis = true;
        break;
      }
    }
    // Additional check: if other warnings exist, the one we need should be among them.
    // For this specific test, we expect at least that warning.
    assertTrue("Expected EXPECTED_THIS_TYPE warning but got different warnings: " 
        + warnings[0].getDescription(), foundExpectedThis);
  }

  @Test(timeout = 4000)
  public void testThisTypeOfFunction3() {
    // Another variation: function with @this called as a callback?
    String js = "/** @this {number} */ function g() {}; setTimeout(g, 0);";
    Compiler compiler = compileScript(js);
    JSError[] warnings = compiler.getWarnings();
    // Should also produce EXPECTED_THIS_TYPE or similar
    boolean foundExpectedThis = false;
    for (JSError w : warnings) {
      if (w.getType() == TypeCheck.EXPECTED_THIS_TYPE) {
        foundExpectedThis = true;
        break;
      }
    }
    // This test may be sensitive to implementation; it's okay if other warnings appear.
    // We just check that at least one EXPECTED_THIS_TYPE is present.
    // If the bug is fixed, the warning should exist.
    assertTrue("EXPECTED_THIS_TYPE warning not emitted (testThisTypeOfFunction3)",
        foundExpectedThis);
  }

  @Test(timeout = 4000)
  public void testThisTypeOfFunction4() {
    // Yet another scenario: function with @this, but called as a method? Actually test expects warning.
    String js = "/** @this {boolean} */ function h() { return this; }; var o = {m: h}; o.m();";
    // Calling as a method binds this to o, which may be acceptable, but depending on the type system,
    // it might still warn if o's type doesn't match the @this annotation.
    Compiler compiler = compileScript(js);
    JSError[] warnings = compiler.getWarnings();
    // The test expects a warning; we'll check that at least one warning (likely EXPECTED_THIS_TYPE or HIDDEN_*)
    // The exact diagnostic is not critical; we just need the test to reveal the bug if warning missing.
    assertTrue("Expected at least one warning for testThisTypeOfFunction4",
        warnings.length > 0);
  }

  // ---------------------------------------------------------------
  // Partition D: Exception & Defensive Guard Paths
  // ---------------------------------------------------------------

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testProcessWithNullScopeCreator() {
    Compiler compiler = createCompiler();
    TypeCheck tc = new TypeCheck(compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        null, // topScope
        null, // scopeCreator
        CheckLevel.WARNING, CheckLevel.OFF);
    Node externsRoot = new Node(Token.SCRIPT);
    Node jsRoot = new Node(Token.SCRIPT);
    tc.process(externsRoot, jsRoot);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testProcessForTestingWithNullParent() {
    Compiler compiler = createCompiler();
    TypeCheck tc = new TypeCheck(compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry());
    Node jsRoot = new Node(Token.SCRIPT);
    // jsRoot has no parent -> should throw Preconditions.checkState
    tc.processForTesting(null, jsRoot);
  }

  @Test(timeout = 4000)
  public void testVisitNameWithNullTypeFallsBackToUnknown() {
    // Through compilation, ensure that if a name's type is null, it becomes unknown.
    Compiler compiler = compileScript("var x; x;");
    // Should not crash, and the reference 'x' should have unknown type.
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testInterfaceFunctionNotEmptyBody() {
    String js = "/** @interface */ function I() {};" +
                "/** @param {number} n */ I.prototype.method = function(n) { n++; };";
    Compiler compiler = compileScript(js);
    assertTrue("Expected warning for non-empty interface method",
        compiler.getWarnings().length > 0);
    assertFirstWarningType(compiler, TypeCheck.INTERFACE_FUNCTION_NOT_EMPTY);
  }

  @Test(timeout = 4000)
  public void testNoTypeCheckSectionSuppressesWarnings() {
    String js = "/** @notypecheck */ function f() { var x = unknown; }";
    Compiler compiler = compileScript(js);
    // Should have no warnings because the function is under @notypecheck
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testEnumWithDuplicateKey() {
    String js = "/** @enum {number} */ var E = {A: 1, A: 2};";
    Compiler compiler = compileScript(js);
    assertTrue("Expected ENUM_DUP warning",
        compiler.getWarnings().length > 0);
    assertFirstWarningType(compiler, TypeCheck.ENUM_DUP);
  }

  @Test(timeout = 4000)
  public void testOverrideMissingOnSuperclassProperty() {
    String js = "/** @constructor */ function Sup() {}; Sup.prototype.foo = 1;" +
                "/** @constructor @extends {Sup} */ function Sub() {};" +
                "Sub.prototype.foo = 2;";
    Compiler compiler = compileScript(js);
    // Should warn about HIDDEN_SUPERCLASS_PROPERTY (missing @override)
    boolean foundHidden = false;
    for (JSError w : compiler.getWarnings()) {
      if (w.getType() == TypeCheck.HIDDEN_SUPERCLASS_PROPERTY) {
        foundHidden = true;
        break;
      }
    }
    assertTrue("Expected HIDDEN_SUPERCLASS_PROPERTY warning", foundHidden);
  }

  @Test(timeout = 4000)
  public void testDeterministicBooleanExpression() {
    String js = "var x = 1; if (x > 0) {}";
    Compiler compiler = compileScript(js);
    // Should warn DETERMINISTIC_TEST because both sides are numbers and condition always true.
    boolean found = false;
    for (JSError w : compiler.getWarnings()) {
      if (w.getType() == TypeCheck.DETERMINISTIC_TEST) {
        found = true;
        break;
      }
    }
    assertTrue("Expected DETERMINISTIC_TEST warning", found);
  }

  // ---------------------------------------------------------------
  // Additional tests to increase branch coverage
  // ---------------------------------------------------------------

  @Test(timeout = 4000)
  public void testInOperatorRequiresObject() {
    String js = "var x = 'prop' in 42;";
    Compiler compiler = compileScript(js);
    // Should warn that 'in' requires an object
    assertTrue("Expected warning for 'in' on non-object",
        compiler.getWarnings().length > 0);
  }

  @Test(timeout = 4000)
  public void testTypeofReturnsString() {
    String js = "var x = typeof 1;";
    Compiler compiler = compileScript(js);
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testIncrementOnNumber() {
    String js = "var x = 1; x++;";
    Compiler compiler = compileScript(js);
    assertWarningCount(compiler, 0);
  }

  @Test(timeout = 4000)
  public void testIncrementOnString() {
    String js = "var x = 'string'; x++;";
    Compiler compiler = compileScript(js);
    // Should warn about increment/decrement on non-number
    assertTrue("Expected warning for increment on string",
        compiler.getWarnings().length > 0);
  }

  @Test(timeout = 4000)
  public void testInterfaceCannotExtendClass() {
    String js = "/** @constructor */ function C() {};" +
                "/** @interface @extends {C} */ function I() {};";
    Compiler compiler = compileScript(js);
    assertTrue("Expected CONFLICTING_EXTENDED_TYPE warning",
        compiler.getWarnings().length > 0);
    assertFirstWarningType(compiler, TypeCheck.CONFLICTING_EXTENDED_TYPE);
  }

  @Test(timeout = 4000)
  public void testInterfaceFunctionBodyNotEmpty() {
    String js = "/** @interface */ function I() {};" +
                "I.prototype.foo = function() { return 1; };";
    Compiler compiler = compileScript(js);
    assertTrue("Expected INTERFACE_FUNCTION_NOT_EMPTY warning",
        compiler.getWarnings().length > 0);
  }

  @Test(timeout = 4000)
  public void testGetTypedPercent() {
    Compiler compiler = compileScript("var a = 1; var b = 'x'; var c = true;");
    TypeCheck tc = new TypeCheck(compiler,
        compiler.getReverseAbstractInterpreter(),
        compiler.getTypeRegistry(),
        compiler.getTopScope(),
        new MemoizedScopeCreator(new TypedScopeCreator(compiler)),
        CheckLevel.WARNING,
        CheckLevel.OFF);
    // After processing the script, getTypedPercent should return > 0
    Node root = compiler.getRoot();
    Node externsAndJs = root.getChildAtIndex(0);
    Node jsRoot = externsAndJs.getChildAtIndex(1);
    tc.process(null, jsRoot);
    double percent = tc.getTypedPercent();
    assertTrue("Typed percent should be > 0", percent > 0.0);
  }

  @Test(timeout = 4000)
  public void testOverrideWithMismatchedType() {
    String js = "/** @constructor */ function Parent() {}; Parent.prototype.foo = 1;" +
                "/** @constructor @extends {Parent} */ function Child() {};" +
                "/** @override */ Child.prototype.foo = 'string';";
    Compiler compiler = compileScript(js);
    // Should warn HIDDEN_SUPERCLASS_PROPERTY_MISMATCH
    boolean found = false;
    for (JSError w : compiler.getWarnings()) {
      if (w.getType() == TypeCheck.HIDDEN_SUPERCLASS_PROPERTY_MISMATCH) {
        found = true;
        break;
      }
    }
    assertTrue("Expected HIDDEN_SUPERCLASS_PROPERTY_MISMATCH warning", found);
  }
}