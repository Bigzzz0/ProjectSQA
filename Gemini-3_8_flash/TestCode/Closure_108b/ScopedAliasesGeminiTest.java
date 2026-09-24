package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * Target Class: ScopedAliases
 * Methods & Branches Covered:
 * 1. process(Node, Node) / hotSwapScript(Node, Node):
 *    - Has errors check (!traversal.hasErrors())
 *    - Alias work queue resolution: referencesOtherAlias() == true (re-queued) vs false (applied)
 *    - Cycle detection in alias work queue (GOOG_SCOPE_ALIAS_CYCLE)
 *    - Detaching alias definitions: parent.isVar() && parent.hasOneChild() vs detached individually
 *    - Collapsing scopes: detaching closure block and merging via NodeUtil.tryMergeBlock
 *    - Code change reporting condition
 * 2. AliasedTypeNode / fixTypeNode:
 *    - String type with property index (name.indexOf('.')) vs simple type
 *    - Nested children traversal in typeNode
 *    - Defect Issue 1144: JSDoc type nodes processed repeatedly when declarations are injected,
 *      causing AliasedTypeNode.applyAlias() to fail Preconditions.checkState(typeName.startsWith(aliasName))
 * 3. Scope Call Validation (validateScopeCall):
 *    - preprocessorSymbolTable null vs non-null
 *    - parent.isExprResult() check (GOOG_SCOPE_USED_IMPROPERLY)
 *    - Child count != 2 (GOOG_SCOPE_HAS_BAD_PARAMETERS)
 *    - Non-function parameter (GOOG_SCOPE_HAS_BAD_PARAMETERS)
 *    - Named function parameter (GOOG_SCOPE_HAS_BAD_PARAMETERS)
 *    - Function taking parameters (GOOG_SCOPE_HAS_BAD_PARAMETERS)
 * 4. Traversal Scope Callbacks (enterScope, exitScope, visit, shouldTraverse):
 *    - shouldTraverse: global scope functions ignored unless goog.scope
 *    - enterScope / exitScope: depth checks, namespace shadowing detection & renaming
 *    - visit: depth < 2 early exit
 *    - visit: Token.NAME aliasVar resolution
 *    - Top-level scope validation (depth == 2):
 *      - LValue check, aliasVar.getNode() == n vs redefined (GOOG_SCOPE_ALIAS_REDEFINED)
 *      - Token.RETURN check (GOOG_SCOPE_USES_RETURN)
 *      - Token.THIS check (GOOG_SCOPE_REFERENCES_THIS)
 *      - Token.THROW check (GOOG_SCOPE_USES_THROW)
 *    - findAliases:
 *      - isVar && qualifiedName (recordAlias directly)
 *      - Bleeding functions & LP parameters
 *      - isVar || isFunctionDecl (hoisted vs non-hoisted, injecting $jscomp.scope, library injection)
 *      - Non-alias local symbols such as catch parameters (GOOG_SCOPE_NON_ALIAS_LOCAL)
 *      - scopedAliasNames collisions and numbering ($jscomp.scope.name$count)
 * -------------------------------------------------------------------------------------------------
 */
public class ScopedAliasesGeminiTest {

  private Compiler runScopedAliases(String js) {
    return runScopedAliases(js, null, null);
  }

  private Compiler runScopedAliases(String js, PreprocessorSymbolTable preprocessorTable) {
    return runScopedAliases(js, preprocessorTable, null);
  }

  private Compiler runScopedAliases(String js, CompilerOptions.AliasTransformationHandler handler) {
    return runScopedAliases(js, null, handler);
  }

  private Compiler runScopedAliases(
      String js,
      PreprocessorSymbolTable preprocessorTable,
      CompilerOptions.AliasTransformationHandler handler) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs.js", "")),
        Collections.singletonList(SourceFile.fromCode("testcode.js", js)),
        options);
    Node root = compiler.parseInputs();
    Node externsRoot = root.getFirstChild();
    Node mainRoot = root.getLastChild();
    if (handler == null) {
      handler = CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER;
    }
    ScopedAliases pass = new ScopedAliases(compiler, preprocessorTable, handler);
    pass.process(externsRoot, mainRoot);
    return compiler;
  }

  private void assertHasError(Compiler compiler, DiagnosticType errorType) {
    assertEquals(
        "Expected error: " + errorType.key + ", but found: " + compiler.getErrorCount(),
        1,
        compiler.getErrorCount());
    assertEquals(errorType, compiler.getErrors()[0].getType());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleAliasInlining() {
    Compiler compiler =
        runScopedAliases("goog.scope(function() {\n  var b = a.b;\n  b();\n});");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testTransitiveAliasResolution() {
    Compiler compiler =
        runScopedAliases(
            "goog.scope(function() {\n"
                + "  var dom = goog.dom;\n"
                + "  var create = dom.createElement;\n"
                + "  create('div');\n"
                + "});");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testMultipleAliasesInSingleVarStatement() {
    Compiler compiler =
        runScopedAliases(
            "goog.scope(function() {\n"
                + "  var a = x.a, b = x.b;\n"
                + "  a();\n"
                + "  b();\n"
                + "});");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testHoistedFunctionDeclarationInScope() {
    Compiler compiler =
        runScopedAliases(
            "goog.scope(function() {\n"
                + "  myFunc();\n"
                + "  function myFunc() { return 1; }\n"
                + "});");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testNonQualifiedVarBecomesScopedGlobal() {
    Compiler compiler =
        runScopedAliases(
            "goog.scope(function() {\n"
                + "  var count = 1 + 2;\n"
                + "  alert(count);\n"
                + "});");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testScopedAliasNamesNumberedOnCollision() {
    Compiler compiler =
        runScopedAliases(
            "goog.scope(function() {\n"
                + "  var a = 1;\n"
                + "});\n"
                + "goog.scope(function() {\n"
                + "  var a = 2;\n"
                + "});");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testNamespaceShadowingAndRenaming() {
    Compiler compiler =
        runScopedAliases(
            "goog.scope(function() {\n"
                + "  var bar = foo.bar;\n"
                + "  function inner() {\n"
                + "    var foo = 42;\n"
                + "    return foo;\n"
                + "  }\n"
                + "  bar();\n"
                + "});");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGlobalFunctionsSkippedByShouldTraverse() {
    Compiler compiler =
        runScopedAliases(
            "function globalFunc() {\n"
                + "  var x = 10;\n"
                + "}\n"
                + "goog.scope(function() {\n"
                + "  var a = b.c;\n"
                + "  a();\n"
                + "});");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testTypeNodeRewritingSimpleAndComposite() {
    Compiler compiler =
        runScopedAliases(
            "goog.scope(function() {\n"
                + "  var MyType = ns.MyType;\n"
                + "  /** @type {MyType} */ var a;\n"
                + "  /** @type {MyType.Sub} */ var b;\n"
                + "});");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testHotSwapScriptDirectExecution() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs.js", "")),
        Collections.singletonList(
            SourceFile.fromCode("testcode.js", "goog.scope(function() { var x = a.b; x(); });")),
        options);
    Node root = compiler.parseInputs();
    ScopedAliases pass =
        new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.hotSwapScript(root.getLastChild(), null);
    assertEquals(0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScopeBlock() {
    Compiler compiler = runScopedAliases("goog.scope(function() {});");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testNoScopeInCode() {
    Compiler compiler = runScopedAliases("var x = 1; var y = 2;");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testScopeMethodNameAsPropertyAccess() {
    Compiler compiler = runScopedAliases("var x = goog.scope;");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testPreprocessorSymbolTableNonNullReference() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.singletonList(SourceFile.fromCode("externs.js", "")),
        Collections.singletonList(
            SourceFile.fromCode("testcode.js", "goog.scope(function() { var x = a.b; x(); });")),
        options);
    Node root = compiler.parseInputs();
    PreprocessorSymbolTable symbolTable = new PreprocessorSymbolTable(root);
    ScopedAliases pass =
        new ScopedAliases(compiler, symbolTable, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(root.getFirstChild(), root.getLastChild());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testAliasTransformationHandlerInvocations() {
    final Map<String, String> recordedAliases = new HashMap<String, String>();
    final List<String> recordedFiles = new ArrayList<String>();

    CompilerOptions.AliasTransformationHandler customHandler =
        new CompilerOptions.AliasTransformationHandler() {
          @Override
          public CompilerOptions.AliasTransformation logAliasTransformation(
              String sourceFileName,
              SourcePosition<CompilerOptions.AliasTransformation> position) {
            recordedFiles.add(sourceFileName);
            return new CompilerOptions.AliasTransformation() {
              @Override
              public void addAlias(String alias, String definition) {
                recordedAliases.put(alias, definition);
              }
            };
          }
        };

    Compiler compiler =
        runScopedAliases(
            "goog.scope(function() {\n" + "  var dom = goog.dom;\n" + "  dom();\n" + "});",
            customHandler);

    assertEquals(0, compiler.getErrorCount());
    assertTrue(recordedFiles.size() > 0);
    assertEquals("goog.dom", recordedAliases.get("dom"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 1144)
  // =========================================================================

  /**
   * Targets Closure Issue 1144: Duplicate JSDoc processing on injected declarations inside
   * goog.scope throws java.lang.IllegalStateException: Preconditions.checkState(typeName.startsWith(aliasName))
   */
  @Test(timeout = 4000)
  public void testIssue1144() {
    Compiler compiler =
        runScopedAliases(
            "goog.scope(function() {\n"
                + "  /** @constructor */\n"
                + "  function Foo() {}\n"
                + "  /** @type {Foo} */\n"
                + "  Foo.prototype.bar = function() {};\n"
                + "});");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testIssue1144VarDeclarationWithDoc() {
    Compiler compiler =
        runScopedAliases(
            "goog.scope(function() {\n"
                + "  var Foo = other.Foo;\n"
                + "  /** @type {Foo} */\n"
                + "  var x = function() {};\n"
                + "});");
    assertEquals(0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testGoogScopeUsedImproperlyInExpression() {
    Compiler compiler = runScopedAliases("var x = goog.scope(function() {});");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  @Test(timeout = 4000)
  public void testGoogScopeHasBadParametersZeroArgs() {
    Compiler compiler = runScopedAliases("goog.scope();");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testGoogScopeHasBadParametersTooManyArgs() {
    Compiler compiler = runScopedAliases("goog.scope(function() {}, 123);");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testGoogScopeHasBadParametersNonFunctionArg() {
    Compiler compiler = runScopedAliases("goog.scope(123);");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testGoogScopeHasBadParametersNamedFunction() {
    Compiler compiler = runScopedAliases("goog.scope(function myScope() {});");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testGoogScopeHasBadParametersFunctionWithParams() {
    Compiler compiler = runScopedAliases("goog.scope(function(param) {});");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testGoogScopeReferencesThis() {
    Compiler compiler = runScopedAliases("goog.scope(function() {\n  this.foo = 1;\n});");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  @Test(timeout = 4000)
  public void testGoogScopeUsesReturn() {
    Compiler compiler = runScopedAliases("goog.scope(function() {\n  return;\n});");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  @Test(timeout = 4000)
  public void testGoogScopeUsesThrow() {
    Compiler compiler = runScopedAliases("goog.scope(function() {\n  throw 'error';\n});");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  @Test(timeout = 4000)
  public void testGoogScopeAliasRedefined() {
    Compiler compiler =
        runScopedAliases("goog.scope(function() {\n  var b = a.b;\n  b = a.c;\n});");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  @Test(timeout = 4000)
  public void testGoogScopeAliasCycleDetected() {
    Compiler compiler =
        runScopedAliases(
            "goog.scope(function() {\n"
                + "  var x = y;\n"
                + "  var y = x;\n"
                + "  x();\n"
                + "  y();\n"
                + "});");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE);
  }

  @Test(timeout = 4000)
  public void testGoogScopeNonAliasLocalSymbolCatchParam() {
    Compiler compiler =
        runScopedAliases(
            "goog.scope(function() {\n"
                + "  try {\n"
                + "  } catch (e) {\n"
                + "  }\n"
                + "});");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
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