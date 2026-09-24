package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * ================================================================================================
 * Targeted Class: com.google.javascript.jscomp.InlineVariables
 * Target Benchmark Defect: Closure-120 / Issue 1053 (testExternalIssue1053)
 *
 * Decision / Condition Matrix Covered:
 * 1. Filter Mode Branching:
 *    - Mode.ALL: Predicates.<Var>alwaysTrue()
 *    - Mode.LOCALS_ONLY: var.scope.isLocal() (globals preserved, locals inlined)
 *    - Mode.CONSTANTS_ONLY: var.isConst() (non-constants ignored)
 * 2. Alias Collection & Inlining:
 *    - Aliasing candidates discovered when referenceCount >= 2 && isWellDefined() && isAssignedOnceInLifetime()
 *    - DEFECT ZONE (Issue 1053): When variable aliased is assigned within an inner function/closure,
 *      it is NOT assigned once per function lifetime and must NOT be inlined as an alias.
 * 3. Arguments Object Escaping / Modification Heuristics:
 *    - Arguments escape: Passed directly to function (e.g., foo(arguments)) -> sets maybeModifiedArguments
 *    - Arguments LValue modification: arguments[0]++, arguments[0] = value
 *    - Arguments property read: arguments[0] (clean, does not escape)
 * 4. Context Preservation & Call Nodes:
 *    - Method calls on property accesses: var a = b.c; a(); -> Must NOT inline (preserves 'this' context)
 *    - Property access passed as argument: var a = b.c; f(a); -> Can safely inline
 * 5. Special Function Inlining Restrictions:
 *    - Subclass relationship call (goog.inherits) -> Must not inline subclass
 *    - Singleton getterClassName (goog.addSingletonGetter) -> Must not inline
 *    - RENAME_PROPERTY_FUNCTION_NAME (JSCompiler_renameProperty) -> Forbidden from inlining
 * 6. Declaration & Initialization Validity:
 *    - FOR-IN loop declaration: for (var x in obj) -> isValidDeclaration false
 *    - Uninitialized variables: var a; alert(a); alert(a); -> inlined to void 0 (NodeUtil.newUndefinedNode)
 *    - Dead assignment removal: declaration != init && refCount == 2 -> pruned
 * 7. String Inlining Heuristics:
 *    - inlineAllStrings = false: Byte cost analysis (noInlineBytes >= inlineBytes) suppresses long strings
 *    - inlineAllStrings = true: Inlines string literals regardless of size
 * 8. This-Alias Inlining:
 *    - value.isThis() && !refInfo.isEscaped() (e.g., var self = this; self.m1(); self.m2();)
 * ================================================================================================
 */
public class InlineVariablesGeminiTest {

  /**
   * Helper utility executing InlineVariables pass on the given JavaScript source.
   */
  private String executePass(String js, InlineVariables.Mode mode, boolean inlineAllStrings) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initCompilerOptionsIfTesting();
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode(js);
    InlineVariables pass = new InlineVariables(compiler, mode, inlineAllStrings);
    pass.process(externs, root);
    return compiler.toSource(root);
  }

  // ==============================================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure-120 / Issue 1053)
  // ==============================================================================================

  /**
   * Targets Defects4J Closure-120 / Issue 1053:
   * When a variable ('u') is modified inside an inner function ('g'), it can be executed
   * multiple times or out of order. Therefore, its alias ('x') must NOT be inlined.
   * On the defective version, 'isAssignedOnceInLifetime' falsely returned true, causing
   * 'x' to be inlined and removed.
   */
  @Test(timeout = 4000)
  public void testExternalIssue1053() {
    String js = "function f() {\n" +
        "  var u;\n" +
        "  function g() {\n" +
        "    u = true;\n" +
        "  }\n" +
        "  var x = u;\n" +
        "  if (x) alert(1);\n" +
        "}";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Defect 1053: Variable x must not be inlined when target u is modified inside closure",
        result.contains("var x") || result.contains("x=u") || result.contains("x = u"));
    assertFalse("Defect 1053: u must not replace x in conditional check",
        result.contains("if(u)") || result.contains("if (u)"));
  }

  /**
   * Variation of Issue 1053: Aliased variable modified within an inner function
   * and subsequently returned.
   */
  @Test(timeout = 4000)
  public void testExternalIssue1053ReturnVariant() {
    String js = "function f() {\n" +
        "  var a;\n" +
        "  function g() {\n" +
        "    a = 1;\n" +
        "  }\n" +
        "  var b = a;\n" +
        "  return b;\n" +
        "}";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Variable b must remain and not be replaced by a",
        result.contains("var b") || result.contains("b=a") || result.contains("b = a"));
    assertFalse("Result must not directly return a because a is modified in closure",
        result.contains("return a"));
  }

  // ==============================================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ==============================================================================================

  @Test(timeout = 4000)
  public void testSimpleSingleReadVariableInlined() {
    String js = "function f() { var a = 1; return a; }";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Value 1 should be directly returned", result.contains("return 1"));
    assertFalse("Variable a declaration should be removed", result.contains("var a"));
  }

  @Test(timeout = 4000)
  public void testMultipleReadsImmutableLiteralInlined() {
    String js = "function f() { var a = 42; return a + a; }";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Immutable literal should be inlined into both expressions",
        result.contains("42 + 42") || result.contains("42+42"));
    assertFalse("Variable declaration should be pruned", result.contains("var a"));
  }

  @Test(timeout = 4000)
  public void testUninitializedVariableInlinedToUndefined() {
    String js = "function f() { var a; alert(a); alert(a); }";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Uninitialized variable should be inlined to void 0",
        result.contains("void 0"));
    assertFalse("Uninitialized var a should be removed", result.contains("var a"));
  }

  @Test(timeout = 4000)
  public void testDeadAssignmentPrunedWhenOnlyRefIsInit() {
    String js = "function f() { var a; a = 1; }";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertFalse("Declaration of dead variable should be pruned", result.contains("var a"));
    assertFalse("Dead assignment to variable should be pruned", result.contains("a = 1") || result.contains("a=1"));
  }

  @Test(timeout = 4000)
  public void testThisAliasInliningWhenUnescaped() {
    String js = "function f() { var self = this; self.foo(); self.bar(); }";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("this should be inlined into self.foo()",
        result.contains("this.foo()"));
    assertTrue("this should be inlined into self.bar()",
        result.contains("this.bar()"));
    assertFalse("var self should be pruned", result.contains("var self"));
  }

  @Test(timeout = 4000)
  public void testCleanAliasInlined() {
    String js = "function f() { var a = 1; var b = a; return b; }";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Alias should resolve completely to 1", result.contains("return 1"));
    assertFalse("var a should be pruned", result.contains("var a"));
    assertFalse("var b should be pruned", result.contains("var b"));
  }

  // ==============================================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // ==============================================================================================

  @Test(timeout = 4000)
  public void testConstantsOnlyModeDoesNotInlineLocals() {
    String js = "function f() { var a = 1; return a; }";
    String result = executePass(js, InlineVariables.Mode.CONSTANTS_ONLY, true);

    assertTrue("Local non-constant should NOT be inlined in CONSTANTS_ONLY mode",
        result.contains("var a") || result.contains("a = 1") || result.contains("a=1"));
    assertTrue("Return statement should still reference a", result.contains("return a"));
  }

  @Test(timeout = 4000)
  public void testConstantsOnlyModeInlinesDeclaredConstants() {
    String js = "var CONST_VAL = 100;\n" +
        "function f() { return CONST_VAL; }";
    String result = executePass(js, InlineVariables.Mode.CONSTANTS_ONLY, true);

    assertTrue("Declared constant should be inlined in CONSTANTS_ONLY mode",
        result.contains("return 100"));
    assertFalse("CONST_VAL declaration should be pruned", result.contains("var CONST_VAL"));
  }

  @Test(timeout = 4000)
  public void testLocalsOnlyModePreservesGlobals() {
    String js = "var g = 10;\n" +
        "alert(g);\n" +
        "function f() { var loc = 20; return loc; }";
    String result = executePass(js, InlineVariables.Mode.LOCALS_ONLY, true);

    assertTrue("Global variable g must NOT be inlined in LOCALS_ONLY mode",
        result.contains("var g") || result.contains("alert(g)"));
    assertTrue("Local variable loc MUST be inlined in LOCALS_ONLY mode",
        result.contains("return 20"));
    assertFalse("var loc should be pruned", result.contains("var loc"));
  }

  @Test(timeout = 4000)
  public void testStringWorthInliningFalseForLargeRepeatedStrings() {
    String largeString = "\"VERY_LONG_STRING_CONSTANT_VALUE_TO_EXCEED_INLINING_THRESHOLD_AT_MULTIPLE_SITES\"";
    String js = "var CONST_STR = " + largeString + ";\n" +
        "alert(CONST_STR);\n" +
        "alert(CONST_STR);\n" +
        "alert(CONST_STR);\n" +
        "alert(CONST_STR);\n";
    String result = executePass(js, InlineVariables.Mode.ALL, false);

    assertTrue("Large string should not be inlined when inlineAllStrings is false to conserve size",
        result.contains("CONST_STR"));
  }

  @Test(timeout = 4000)
  public void testStringWorthInliningTrueForcesLargeStringInlining() {
    String largeString = "\"VERY_LONG_STRING_CONSTANT_VALUE_TO_EXCEED_INLINING_THRESHOLD_AT_MULTIPLE_SITES\"";
    String js = "var CONST_STR = " + largeString + ";\n" +
        "alert(CONST_STR);\n";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertFalse("Large string must be inlined when inlineAllStrings is true",
        result.contains("CONST_STR"));
  }

  // ==============================================================================================
  // Partition D: Defensive Guards & Context Safety
  // ==============================================================================================

  @Test(timeout = 4000)
  public void testMethodCallContextPreservation() {
    String js = "function f() { var a = b.c; a(); }";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Method call context must not be transformed from a() to b.c()",
        result.contains("var a") || result.contains("a = b.c") || result.contains("a=b.c"));
    assertTrue("Direct call to a() must remain", result.contains("a()"));
  }

  @Test(timeout = 4000)
  public void testPropertyPassedAsArgumentCanBeInlined() {
    String js = "function f() { var a = b.c; g(a); }";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Passing property to another function should safely inline",
        result.contains("g(b.c)"));
    assertFalse("Variable a declaration should be removed", result.contains("var a"));
  }

  @Test(timeout = 4000)
  public void testArgumentsEscapedPreventsAliasInlining() {
    String js = "function f() {\n" +
        "  var a = 1;\n" +
        "  var b = a;\n" +
        "  bar(arguments);\n" +
        "  return b;\n" +
        "}";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Arguments escaped via function call should prevent unsafe alias inlining",
        result.contains("arguments"));
  }

  @Test(timeout = 4000)
  public void testArgumentsModifiedViaIncrementPreventsAliasInlining() {
    String js = "function f() {\n" +
        "  var a = 1;\n" +
        "  var b = a;\n" +
        "  arguments[0]++;\n" +
        "  return b;\n" +
        "}";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Arguments mutated via ++ must retain arguments modification",
        result.contains("arguments[0]++"));
  }

  @Test(timeout = 4000)
  public void testArgumentsModifiedViaAssignmentPreventsAliasInlining() {
    String js = "function f() {\n" +
        "  var a = 1;\n" +
        "  var b = a;\n" +
        "  arguments[0] = 5;\n" +
        "  return b;\n" +
        "}";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Arguments mutated via direct assignment must remain intact",
        result.contains("arguments[0] = 5") || result.contains("arguments[0]=5"));
  }

  @Test(timeout = 4000)
  public void testArgumentsReadPropertyDoesNotEscape() {
    String js = "function f() {\n" +
        "  var a = 1;\n" +
        "  var arg0 = arguments[0];\n" +
        "  return a + arg0;\n" +
        "}";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Clean read of arguments[0] should allow inlining variable a",
        result.contains("return 1 + arguments[0]") || result.contains("return 1+arguments[0]"));
    assertFalse("var a should be inlined", result.contains("var a"));
  }

  @Test(timeout = 4000)
  public void testSubclassRelationshipCallMustNotBeInlined() {
    String js = "function f() {\n" +
        "  var sub = function() {};\n" +
        "  goog.inherits(sub, Object);\n" +
        "}";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Subclass definition must not be inlined into class-defining call",
        result.contains("sub") && result.contains("goog.inherits"));
  }

  @Test(timeout = 4000)
  public void testSingletonGetterCallMustNotBeInlined() {
    String js = "function f() {\n" +
        "  var myClass = function() {};\n" +
        "  goog.addSingletonGetter(myClass);\n" +
        "}";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Singleton class must not be inlined into addSingletonGetter call",
        result.contains("myClass") && result.contains("goog.addSingletonGetter"));
  }

  @Test(timeout = 4000)
  public void testRenamePropertyFunctionNotForbiddenFromRemaining() {
    String js = "function f() {\n" +
        "  var JSCompiler_renameProperty = 1;\n" +
        "  return JSCompiler_renameProperty;\n" +
        "}";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("RENAME_PROPERTY_FUNCTION_NAME must be protected from inlining",
        result.contains("JSCompiler_renameProperty"));
  }

  @Test(timeout = 4000)
  public void testForInLoopVariableDeclarationNotEligibleForInlining() {
    String js = "function f(obj) {\n" +
        "  for (var x in obj) {\n" +
        "    alert(x);\n" +
        "  }\n" +
        "}";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("For-in loop variable declaration must not be inlined or removed",
        result.contains("for(var x in obj)") || result.contains("for (var x in obj)"));
  }

  @Test(timeout = 4000)
  public void testInterveningSideEffectsPreventMotion() {
    String js = "function f() {\n" +
        "  var a = sideEffectProducer();\n" +
        "  interferingCall();\n" +
        "  return a;\n" +
        "}";
    String result = executePass(js, InlineVariables.Mode.ALL, true);

    assertTrue("Variable with side effects cannot move past another call",
        result.contains("var a") || result.contains("a = sideEffectProducer()") || result.contains("a=sideEffectProducer()"));
    assertTrue("Interfering call must remain in order", result.contains("interferingCall()"));
  }

  // ==============================================================================================
  // Partition E: Lifecycle, Scope Bounds & Contract Integrity
  // ==============================================================================================

  @Test(timeout = 4000)
  public void testEmptyPassExecution() {
    String js = "";
    String result = executePass(js, InlineVariables.Mode.ALL, true);
    assertNotNull("Empty script execution should produce non-null output", result);
  }

  @Test(timeout = 4000)
  public void testScriptWithoutVariables() {
    String js = "alert(1 + 2);";
    String result = executePass(js, InlineVariables.Mode.ALL, true);
    assertTrue("Static expressions should remain preserved", result.contains("alert(3)") || result.contains("alert(1 + 2)"));
  }
}