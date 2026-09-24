package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Target Class: com.google.javascript.jscomp.InlineVariables
 * Target Defects4J Ground Truth: IntegrationTest::testSingletonGetter1 / Issue 668
 * ----------------------------------------------------------------------------------------------------
 * Decision / Branch Matrix:
 * 1. getFilterForMode():
 *    - Mode.ALL: Predicates.alwaysTrue() -> inlines both local and global candidates.
 *    - Mode.LOCALS_ONLY: IdentifyLocals -> filters out non-local scope vars.
 *    - Mode.CONSTANTS_ONLY: IdentifyConstants -> filters out non-constants.
 * 2. InliningBehavior.collectAliasCandidates():
 *    - mode != Mode.CONSTANTS_ONLY: populates aliasCandidates map for valid name references.
 *    - mode == Mode.CONSTANTS_ONLY: bypasses alias candidate collection.
 * 3. maybeEscapedOrModifiedArguments():
 *    - scope.isLocal() & arguments variable accessed.
 *    - arguments property read vs LValue modification vs full escape.
 * 4. isVarInlineForbidden():
 *    - var.isExtern()
 *    - CodingConvention.isExported()
 *    - RenameProperties.RENAME_PROPERTY_FUNCTION_NAME
 *    - staleVars.contains(var)
 * 5. inlineNonConstants():
 *    - Branch 1: refCount > 1 && isImmutableAndWellDefinedVariable (init != null vs init == null).
 *    - Branch 2: refCount == firstRefAfterInit && canInline() (single read candidate).
 *    - Branch 3: declaration != init && refCount == 2 (assigned once, never read, removed).
 *    - Branch 4: Alias inlining (!maybeModifiedArguments && !staleVars && aliasCandidates match).
 * 6. canInline():
 *    - Validation guards: isValidDeclaration, isValidInitialization, isValidReference.
 *    - Basic block boundary checks (same block requirement for non-aggressive moves).
 *    - Method call context: value.isGetProp() and reference is direct callee (preserves 'this').
 *    - Function value & call node:
 *        a) Subclass relationship check (convention.getClassesDefinedByCall != null).
 *        b) [DEFECT TARGET] Singleton getter check (convention.getSingletonGetterClassName != null).
 *    - canMoveAggressively() vs canMoveModerately() (NodeIterators.LocalVarMotion).
 * 7. isInlineableDeclaredConstant() / isStringWorthInlining():
 *    - Constants byte threshold calculation (inlineAllStrings == false vs true).
 * ====================================================================================================
 */
public class InlineVariablesGeminiTest {

  private String compile(String js, InlineVariables.Mode mode, boolean inlineAllStrings) {
    Compiler compiler = new Compiler();
    Compiler.setLoggingLevel(java.util.logging.Level.OFF);
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    compiler.initOptions(options);

    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode(js);
    assertNotNull("Parsing failed for input: " + js, root);

    // Anchor AST under a shared parent container
    Node block = new Node(Token.BLOCK, externs, root);

    try {
      Normalize normalize = new Normalize(compiler, false);
      normalize.process(externs, root);
    } catch (Throwable ignored) {
      // Best-effort normalization for test code
    }

    InlineVariables pass = new InlineVariables(compiler, mode, inlineAllStrings);
    pass.process(externs, root);

    return compiler.toSource(root);
  }

  // ==================================================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testSimpleVariableInlinedInAllMode() {
    String js = "function f() { var x = 1; var y = x; return y; }";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertFalse("Variable x should have been inlined and removed", result.contains("var x"));
    assertTrue("Value 1 should have been inlined into y", result.contains("y = 1") || result.contains("y=1"));
  }

  @Test(timeout = 4000)
  public void testImmutableVariableMultipleReadsInlined() {
    String js = "function f() { var x = 10; var a = x; var b = x; return a + b; }";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertFalse("Variable x should be inlined into all reads", result.contains("var x"));
    assertTrue(result.contains("a = 10") || result.contains("a=10"));
    assertTrue(result.contains("b = 10") || result.contains("b=10"));
  }

  @Test(timeout = 4000)
  public void testDeclarationDifferentFromInitializationInlined() {
    String js = "function f() { var x; x = 5; var y = x; return y; }";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertFalse("Variable declaration x should be removed", result.contains("var x"));
    assertFalse("Separate assignment to x should be eliminated", result.contains("x = 5") || result.contains("x=5"));
    assertTrue("Value 5 should be inlined directly into y", result.contains("y = 5") || result.contains("y=5"));
  }

  @Test(timeout = 4000)
  public void testAssignedOnceNeverReadEliminated() {
    String js = "function f() { var x; x = 42; return 0; }";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertFalse("Unread declared variable x should be eliminated", result.contains("var x"));
    assertFalse("Unread assignment to x should be removed", result.contains("42"));
  }

  @Test(timeout = 4000)
  public void testUninitializedVariableInlinedToUndefined() {
    String js = "function f() { var x; var y = x; var z = x; return y + z; }";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertFalse("Uninitialized variable x should be inlined", result.contains("var x"));
    assertTrue("Uninitialized variable should be inlined as void 0", result.contains("void 0"));
  }

  @Test(timeout = 4000)
  public void testThisAliasInlining() {
    String js = "function f() { var self = this; self.foo(); self.bar(); }";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertFalse("Variable self aliasing this should be inlined", result.contains("var self"));
    assertTrue("this should directly replace self.foo()", result.contains("this.foo()"));
    assertTrue("this should directly replace self.bar()", result.contains("this.bar()"));
  }

  @Test(timeout = 4000)
  public void testAliasCandidateInlining() {
    String js = "function f(p) { var a = p; var b = a; use(a); use(b); }";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertFalse("Alias variable b should be inlined to reference a", result.contains("var b"));
  }

  // ==================================================================================================
  // Partition B: Boundary Value Analysis & Heuristics
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testStringInliningHeuristicThresholdFalse() {
    // A long string referenced repeatedly should NOT be inlined when inlineAllStrings is false
    String js = "function f() {" +
        "var STR = 'a_very_long_string_constant_that_would_expand_code_size_if_duplicated';" +
        "var a = STR; var b = STR; var c = STR; var d = STR; var e = STR;" +
        "return a + b + c + d + e;" +
        "}";
    String result = compile(js, InlineVariables.Mode.ALL, false);
    assertTrue("Long string should not be inlined when inlineAllStrings is false", result.contains("var STR"));
  }

  @Test(timeout = 4000)
  public void testStringInliningHeuristicThresholdTrue() {
    // When inlineAllStrings is true, size heuristic is bypassed
    String js = "function f() {" +
        "var STR = 'a_very_long_string_constant_that_would_expand_code_size_if_duplicated';" +
        "var a = STR; var b = STR; var c = STR; var d = STR; var e = STR;" +
        "return a + b + c + d + e;" +
        "}";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertFalse("Long string should be inlined when inlineAllStrings is true", result.contains("var STR"));
  }

  @Test(timeout = 4000)
  public void testMethodCallContextPreventsInlining() {
    String js = "function f(obj) { var a = obj.method; a(); }";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertTrue("Method call context must prevent inlining to preserve 'this'", result.contains("var a"));
    assertTrue("Call to a() must not be converted into obj.method()", result.contains("a()"));
  }

  @Test(timeout = 4000)
  public void testCrossControlStructureBoundaryPreventsInliningNonLiteral() {
    String js = "function ext() {} function f(cond) { var x = ext(); if (cond) { var y = x; } }";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertTrue("Non-literal variable must not cross basic block boundary", result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testNonImmutableVariableMultipleReadsNotWorthInlining() {
    String js = "function ext() {} function f() { var x = ext(); var a = x; var b = x; return a + b; }";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertTrue("Non-immutable variable read multiple times must not be inlined", result.contains("var x"));
  }

  // ==================================================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // ==================================================================================================

  /**
   * Targets issue 668 / Defects4J IntegrationTest::testSingletonGetter1.
   * Under defective versions of InlineVariables, functions passed to goog.addSingletonGetter()
   * are aggressively inlined into anonymous functions because canInline() fails to verify
   * convention.getSingletonGetterClassName(callNode).
   */
  @Test(timeout = 4000)
  public void testSingletonGetterBugDefects4J() {
    String js = "function Foo() {}\n" +
                "goog.addSingletonGetter(Foo);";
    String result = compile(js, InlineVariables.Mode.ALL, true);

    assertTrue("Class Foo declaration must NOT be inlined into goog.addSingletonGetter",
        result.contains("function Foo") || result.contains("var Foo"));
    assertTrue("goog.addSingletonGetter(Foo) must retain named reference Foo",
        result.contains("goog.addSingletonGetter(Foo)"));
  }

  @Test(timeout = 4000)
  public void testSubclassDefinitionCallPreventsInlining() {
    String js = "var Super = function() {};\n" +
                "var Sub = function() {};\n" +
                "goog.inherits(Sub, Super);";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertTrue("Subclass definition must not be inlined into goog.inherits call",
        result.contains("var Sub"));
  }

  // ==================================================================================================
  // Partition D: Mode Filters & Defensive Guard Paths
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testModeConstantsOnlySkipsNonConstants() {
    String js = "var x = 1; var y = x;";
    String result = compile(js, InlineVariables.Mode.CONSTANTS_ONLY, true);
    assertTrue("Non-constant variable x must not be inlined under CONSTANTS_ONLY mode",
        result.contains("var x"));
  }

  @Test(timeout = 4000)
  public void testModeLocalsOnlyInlinesLocalsNotGlobals() {
    String globalJs = "var x = 1; var y = x;";
    String globalResult = compile(globalJs, InlineVariables.Mode.LOCALS_ONLY, true);
    assertTrue("Global variable must not be inlined under LOCALS_ONLY mode",
        globalResult.contains("var x"));

    String localJs = "function f() { var a = 1; var b = a; return b; }";
    String localResult = compile(localJs, InlineVariables.Mode.LOCALS_ONLY, true);
    assertFalse("Local variable must be inlined under LOCALS_ONLY mode",
        localResult.contains("var a"));
  }

  @Test(timeout = 4000)
  public void testSpecialRenamePropertyVariableForbiddenFromInlining() {
    String js = "var JSCompiler_renameProperty = function(a) { return a; };" +
                "var res = JSCompiler_renameProperty('prop');";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertTrue("JSCompiler_renameProperty is explicitly forbidden from inlining",
        result.contains("JSCompiler_renameProperty"));
  }

  @Test(timeout = 4000)
  public void testArgumentsEscapedPreventsAliasInlining() {
    String js = "function f(p) {" +
                "  var a = p;" +
                "  var b = a;" +
                "  var escaped = arguments;" +
                "  use(a);" +
                "  use(b);" +
                "}";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertTrue("Escaped arguments object must prevent alias inlining", result.contains("var b"));
  }

  @Test(timeout = 4000)
  public void testArgumentsModifiedViaLValuePreventsAliasInlining() {
    String js = "function f(p) {" +
                "  var a = p;" +
                "  var b = a;" +
                "  arguments[0] = 99;" +
                "  use(a);" +
                "  use(b);" +
                "}";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertTrue("Modified arguments object must prevent alias inlining", result.contains("var b"));
  }

  // ==================================================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // ==================================================================================================

  @Test(timeout = 4000)
  public void testModeEnumIntegrity() {
    InlineVariables.Mode[] modes = InlineVariables.Mode.values();
    assertEquals("InlineVariables.Mode must define exactly 3 modes", 3, modes.length);
    assertSame(InlineVariables.Mode.CONSTANTS_ONLY, InlineVariables.Mode.valueOf("CONSTANTS_ONLY"));
    assertSame(InlineVariables.Mode.LOCALS_ONLY, InlineVariables.Mode.valueOf("LOCALS_ONLY"));
    assertSame(InlineVariables.Mode.ALL, InlineVariables.Mode.valueOf("ALL"));
  }

  @Test(timeout = 4000)
  public void testEmptyAstProcessing() {
    String result = compile("", InlineVariables.Mode.ALL, true);
    assertNotNull("Processing empty AST must succeed gracefully", result);
    assertEquals("", result.trim());
  }

  @Test(timeout = 4000)
  public void testDeadDeclarationsOnlyProcessing() {
    String js = "var a; var b; var c;";
    String result = compile(js, InlineVariables.Mode.ALL, true);
    assertNotNull("Processing purely uninitialized variables must not crash", result);
  }
}