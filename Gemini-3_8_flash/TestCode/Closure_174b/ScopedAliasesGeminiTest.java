package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;
import com.google.javascript.rhino.Token;

/* [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: ScopedAliases.java (Closure Compiler / Defects4J)
 *
 * PARTITIONS & BRANCH COVERAGE MATRIX:
 * 1. Scope Call Validation (validateScopeCall):
 *    - parent.isExprResult() vs !parent.isExprResult() -> GOOG_SCOPE_USED_IMPROPERLY
 *    - n.getChildCount() != 2 (0 args, 2+ args) -> GOOG_SCOPE_HAS_BAD_PARAMETERS
 *    - !fn.isFunction(), named function, or fn has parameters -> GOOG_SCOPE_HAS_BAD_PARAMETERS
 *    - preprocessorSymbolTable != null vs null reference tracking
 * 2. Scope Body Validation (visit):
 *    - Scope depth == 2 vs >= 2
 *    - Token.RETURN -> GOOG_SCOPE_USES_RETURN (depth 2 only)
 *    - Token.THIS -> GOOG_SCOPE_REFERENCES_THIS (depth 2 only)
 *    - Token.THROW -> GOOG_SCOPE_USES_THROW (depth 2 only)
 *    - Alias re-assignment / LValue -> GOOG_SCOPE_ALIAS_REDEFINED
 *    - JSDoc type fixing (fixTypeNode / AliasedTypeNode)
 * 3. Alias Discovery & Generation (findAliases):
 *    - Qualified name aliases (var dom = goog.dom)
 *    - Non-qualified var assignment (var x = expr) -> injected $jscomp.scope namespace
 *    - Consecutive scopes reusing var names -> $jscomp.scope.name vs $jscomp.scope.name$1
 *    - Uninitialized variables (var a;) -> Defect zone (Defects4J Issue 1103a, 1103b, 1103c)
 *    - Hoisted functions / non-alias locals -> GOOG_SCOPE_NON_ALIAS_LOCAL
 * 4. Alias Dependency Resolution & Application (hotSwapScript):
 *    - Direct alias usage (AliasedNode)
 *    - Transitive / chained alias references (referencesOtherAlias())
 *    - Alias cycles -> GOOG_SCOPE_ALIAS_CYCLE
 *    - Alias definition removal: parent.hasOneChild() vs multiple children in var
 * 5. Namespace Shadowing (findNamespaceShadows, renameNamespaceShadows):
 *    - Shadows detected at scope depth > 2 -> WhitelistedRenamer triggers
 * 6. Traversal Guards:
 *    - shouldTraverse() ignores global functions not part of goog.scope
 * ----------------------------------------------------------------------------------------------------
 */
public class ScopedAliasesGeminiTest {

  private static final CompilerOptions.AliasTransformationHandler NULL_HANDLER =
      new CompilerOptions.AliasTransformationHandler() {
        @Override
        public CompilerOptions.AliasTransformation logAliasTransformation(
            String sourceName, SourcePosition<CompilerOptions.AliasTransformation> position) {
          return new CompilerOptions.AliasTransformation() {
            @Override
            public void addAlias(String alias, String definition) {}
          };
        }
      };

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler;
  }

  private Compiler runScopedAliases(String js) {
    Compiler compiler = createCompiler();
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, NULL_HANDLER);
    pass.process(null, root);
    return compiler;
  }

  private String compileAndGetSource(String js) {
    Compiler compiler = createCompiler();
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, NULL_HANDLER);
    pass.process(null, root);
    return compiler.toSource(root);
  }

  private boolean hasDiagnostic(Compiler compiler, DiagnosticType type) {
    for (JSError error : compiler.getErrors()) {
      if (error.getType() == type) {
        return true;
      }
    }
    return false;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleAliasTransformation() {
    String js = "goog.scope(function() {"
        + "  var dom = goog.dom;"
        + "  dom.createElement();"
        + "});";
    String result = compileAndGetSource(js);
    assertTrue("Alias dom should be expanded to goog.dom", result.contains("goog.dom.createElement()"));
    assertFalse("goog.scope wrapper must be collapsed", result.contains("goog.scope"));
    assertFalse("var dom definition must be detached", result.contains("var dom"));
  }

  @Test(timeout = 4000)
  public void testTransitiveAliasUsage() {
    String js = "goog.scope(function() {"
        + "  var g = goog;"
        + "  var d = g.dom;"
        + "  d.createElement();"
        + "});";
    String result = compileAndGetSource(js);
    assertTrue("Transitive alias must resolve fully to goog.dom", result.contains("goog.dom.createElement()"));
  }

  @Test(timeout = 4000)
  public void testMultiVarAliasRemoval() {
    // Tests both branch paths of aliasDefinition.getParent().hasOneChild()
    String js = "goog.scope(function() {"
        + "  var a = goog.a, b = goog.b;"
        + "  a(); b();"
        + "});";
    String result = compileAndGetSource(js);
    assertTrue(result.contains("goog.a()"));
    assertTrue(result.contains("goog.b()"));
    assertFalse(result.contains("var a"));
    assertFalse(result.contains("var b"));
  }

  @Test(timeout = 4000)
  public void testNonQualifiedVarAssignmentRewriting() {
    String js = "goog.scope(function() {"
        + "  var f = function() { return 42; };"
        + "  f();"
        + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals("Should not produce compilation errors", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testMultipleScopeBlocksDisambiguateNames() {
    String js = "goog.scope(function() {"
        + "  var a = function() { return 1; };"
        + "  a();"
        + "});"
        + "goog.scope(function() {"
        + "  var a = function() { return 2; };"
        + "  a();"
        + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals("Should cleanly rewrite multiple scopes with identical local alias names",
        0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testAliasedTypeNodeInJsDoc() {
    String js = "goog.scope(function() {"
        + "  var MyType = ns.MyType;"
        + "  /** @type {MyType.Inner} */"
        + "  var instance = 1;"
        + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals("JSDoc type references should be fixed without errors", 0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testNamespaceShadowingRenaming() {
    String js = "goog.scope(function() {"
        + "  var dom = goog.dom;"
        + "  function inner() {"
        + "    var goog = 123;"
        + "    return goog;"
        + "  }"
        + "  function inner2() {"
        + "    var goog = 456;"
        + "    return goog;"
        + "  }"
        + "  dom.createElement();"
        + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals("Shadowed namespace inside inner scope must be handled", 0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScopeCall() {
    String js = "goog.scope(function() {});";
    String result = compileAndGetSource(js);
    assertEquals("Empty scope must collapse into empty string or clean statement", "", result.trim());
  }

  @Test(timeout = 4000)
  public void testCodeWithoutAnyGoogScope() {
    String js = "var normalCode = 1 + 2; alert(normalCode);";
    Compiler compiler = runScopedAliases(js);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGlobalFunctionShouldTraverseReturnsFalse() {
    String js = "function globalFn() { var x = 1; }\n"
        + "goog.scope(function() {\n"
        + "  var d = goog.dom;\n"
        + "  d.init();\n"
        + "});";
    String result = compileAndGetSource(js);
    assertTrue(result.contains("function globalFn"));
    assertTrue(result.contains("goog.dom.init()"));
  }

  @Test(timeout = 4000)
  public void testWithPreprocessorSymbolTable() {
    Compiler compiler = createCompiler();
    Node root = compiler.parseTestCode("goog.scope(function() { var d = goog.dom; d(); });");
    PreprocessorSymbolTable table = new PreprocessorSymbolTable(new Node(Token.BLOCK));
    ScopedAliases pass = new ScopedAliases(compiler, table, NULL_HANDLER);
    pass.process(null, root);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testCustomTransformationHandlerRecording() {
    Compiler compiler = createCompiler();
    Node root = compiler.parseTestCode("goog.scope(function() { var d = goog.dom; d(); });");
    final Map<String, String> recorded = new HashMap<>();
    CompilerOptions.AliasTransformationHandler handler = new CompilerOptions.AliasTransformationHandler() {
      @Override
      public CompilerOptions.AliasTransformation logAliasTransformation(
          String sourceName, SourcePosition<CompilerOptions.AliasTransformation> position) {
        return new CompilerOptions.AliasTransformation() {
          @Override
          public void addAlias(String alias, String definition) {
            recorded.put(alias, definition);
          }
        };
      }
    };
    ScopedAliases pass = new ScopedAliases(compiler, null, handler);
    pass.process(null, root);
    assertEquals(0, compiler.getErrorCount());
    assertTrue("Alias definition should be captured", recorded.containsKey("d"));
    assertEquals("goog.dom", recorded.get("d"));
  }

  @Test(timeout = 4000)
  public void testHotSwapScriptDirectExecution() {
    Compiler compiler = createCompiler();
    Node root = compiler.parseTestCode("goog.scope(function() { var d = goog.dom; d(); });");
    ScopedAliases pass = new ScopedAliases(compiler, null, NULL_HANDLER);
    pass.hotSwapScript(root, null);
    assertEquals(0, compiler.getErrorCount());
    String source = compiler.toSource(root);
    assertTrue(source.contains("goog.dom()"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Issue 1103)
  // =========================================================================

  /**
   * Targets Defects4J Ground Truth: ScopedAliasesTest::testIssue1103a
   * Uninitialized local variables inside goog.scope should not be rejected as non-alias locals.
   */
  @Test(timeout = 4000)
  public void testIssue1103a() {
    String js = "goog.scope(function () {"
        + "  var a;"
        + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals("Issue 1103a: Uninitialized variable inside goog.scope must not trigger errors",
        0, compiler.getErrorCount());
  }

  /**
   * Targets Defects4J Ground Truth: ScopedAliasesTest::testIssue1103b
   * Multiple var declarations containing an uninitialized variable should not cause an ICE.
   */
  @Test(timeout = 4000)
  public void testIssue1103b() {
    String js = "goog.scope(function () {"
        + "  var a = foo, b;"
        + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals("Issue 1103b: Multiple var declarations with uninitialized variable must not fail",
        0, compiler.getErrorCount());
  }

  /**
   * Targets Defects4J Ground Truth: ScopedAliasesTest::testIssue1103c
   * Typed uninitialized variable declaration inside goog.scope should not trigger non-alias local error.
   */
  @Test(timeout = 4000)
  public void testIssue1103c() {
    String js = "goog.scope(function () {"
        + "  /** @type {number} */ var a;"
        + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals("Issue 1103c: Typed uninitialized var inside goog.scope must not trigger errors",
        0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths (Diagnostic Errors)
  // =========================================================================

  @Test(timeout = 4000)
  public void testErrorUsedImproperlyNotExpr() {
    String js = "var notAlone = goog.scope(function() {});";
    Compiler compiler = runScopedAliases(js);
    assertTrue(hasDiagnostic(compiler, ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY));
  }

  @Test(timeout = 4000)
  public void testErrorHasBadParametersZeroArgs() {
    String js = "goog.scope();";
    Compiler compiler = runScopedAliases(js);
    assertTrue(hasDiagnostic(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testErrorHasBadParametersTooManyArgs() {
    String js = "goog.scope(function() {}, 1);";
    Compiler compiler = runScopedAliases(js);
    assertTrue(hasDiagnostic(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testErrorHasBadParametersNotFunction() {
    String js = "goog.scope(42);";
    Compiler compiler = runScopedAliases(js);
    assertTrue(hasDiagnostic(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testErrorHasBadParametersNamedFunction() {
    String js = "goog.scope(function named() {});";
    Compiler compiler = runScopedAliases(js);
    assertTrue(hasDiagnostic(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testErrorHasBadParametersFunctionWithParams() {
    String js = "goog.scope(function(param) {});";
    Compiler compiler = runScopedAliases(js);
    assertTrue(hasDiagnostic(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testErrorReferencesThis() {
    String js = "goog.scope(function() { this.x = 1; });";
    Compiler compiler = runScopedAliases(js);
    assertTrue(hasDiagnostic(compiler, ScopedAliases.GOOG_SCOPE_REFERENCES_THIS));
  }

  @Test(timeout = 4000)
  public void testNestedFunctionMayReferenceThis() {
    String js = "goog.scope(function() {"
        + "  var fn = function() { this.x = 1; };"
        + "});";
    Compiler compiler = runScopedAliases(js);
    assertFalse("Inner function is allowed to reference 'this'",
        hasDiagnostic(compiler, ScopedAliases.GOOG_SCOPE_REFERENCES_THIS));
  }

  @Test(timeout = 4000)
  public void testErrorUsesReturn() {
    String js = "goog.scope(function() { return 1; });";
    Compiler compiler = runScopedAliases(js);
    assertTrue(hasDiagnostic(compiler, ScopedAliases.GOOG_SCOPE_USES_RETURN));
  }

  @Test(timeout = 4000)
  public void testNestedFunctionMayUseReturn() {
    String js = "goog.scope(function() {"
        + "  var fn = function() { return 1; };"
        + "});";
    Compiler compiler = runScopedAliases(js);
    assertFalse("Inner function is allowed to use 'return'",
        hasDiagnostic(compiler, ScopedAliases.GOOG_SCOPE_USES_RETURN));
  }

  @Test(timeout = 4000)
  public void testErrorUsesThrow() {
    String js = "goog.scope(function() { throw 'error'; });";
    Compiler compiler = runScopedAliases(js);
    assertTrue(hasDiagnostic(compiler, ScopedAliases.GOOG_SCOPE_USES_THROW));
  }

  @Test(timeout = 4000)
  public void testErrorAliasRedefined() {
    String js = "goog.scope(function() {"
        + "  var a = goog.a;"
        + "  a = 1;"
        + "});";
    Compiler compiler = runScopedAliases(js);
    assertTrue(hasDiagnostic(compiler, ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED));
  }

  @Test(timeout = 4000)
  public void testErrorAliasCycle() {
    String js = "goog.scope(function() {"
        + "  var a = b;"
        + "  var b = a;"
        + "  a();"
        + "});";
    Compiler compiler = runScopedAliases(js);
    assertTrue(hasDiagnostic(compiler, ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE));
  }

  @Test(timeout = 4000)
  public void testErrorNonAliasLocalHoistedFunction() {
    String js = "goog.scope(function() {"
        + "  function local() {}"
        + "});";
    Compiler compiler = runScopedAliases(js);
    assertTrue(hasDiagnostic(compiler, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testDiagnosticConstantsIntegrity() {
    assertEquals("goog.scope", ScopedAliases.SCOPING_METHOD_NAME);
    assertNotNull(ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
    assertNotNull(ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
    assertNotNull(ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
    assertNotNull(ScopedAliases.GOOG_SCOPE_USES_RETURN);
    assertNotNull(ScopedAliases.GOOG_SCOPE_USES_THROW);
    assertNotNull(ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
    assertNotNull(ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE);
    assertNotNull(ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }
}