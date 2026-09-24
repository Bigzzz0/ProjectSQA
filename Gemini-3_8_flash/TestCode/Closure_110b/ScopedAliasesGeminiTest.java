package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.CompilerOptions.AliasTransformation;
import com.google.javascript.jscomp.CompilerOptions.AliasTransformationHandler;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/* [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: ScopedAliases.java (Closure Compiler)
 *
 * 1. DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth):
 *    - testFunctionDeclaration: function declaration inside goog.scope(function() { function f() {} })
 *      causes JSC_GOOG_SCOPE_NON_ALIAS_LOCAL error in the defective version because parent.isVar()
 *      is false and function declarations were not recognized as valid scope locals.
 *    - testHoistedFunctionDeclaration: hoisted function call + declaration inside goog.scope
 *      triggers the same defect.
 *
 * 2. STRUCTURAL BRANCH COVERAGE MATRIX:
 *    - Traversal.validateScopeCall:
 *      * !parent.isExprResult() -> GOOG_SCOPE_USED_IMPROPERLY
 *      * n.getChildCount() != 2 -> GOOG_SCOPE_HAS_BAD_PARAMETERS
 *      * !anonymousFnNode.isFunction() -> GOOG_SCOPE_HAS_BAD_PARAMETERS
 *      * NodeUtil.getFunctionName(anonymousFnNode) != null -> GOOG_SCOPE_HAS_BAD_PARAMETERS
 *      * NodeUtil.getFunctionParameters(anonymousFnNode).hasChildren() -> GOOG_SCOPE_HAS_BAD_PARAMETERS
 *      * preprocessorSymbolTable != null -> addReference invocation
 *    - Traversal.visit:
 *      * Depth 2 validation: RETURN -> GOOG_SCOPE_USES_RETURN, THIS -> GOOG_SCOPE_REFERENCES_THIS,
 *        THROW -> GOOG_SCOPE_USES_THROW. Inner scopes (depth > 2) allow return/this/throw.
 *      * LValue check on alias: first definition added to aliasDefinitionsInOrder,
 *        re-assignment triggers GOOG_SCOPE_ALIAS_REDEFINED.
 *    - Traversal.findAliases:
 *      * isVar && qualifiedName -> recordAlias
 *      * isVar && !qualifiedName -> injected "$jscomp.scope." creation, name collisions ($jscomp.scope.x$1)
 *      * value != null vs value == null (uninitialized var)
 *      * non-var local symbols -> GOOG_SCOPE_NON_ALIAS_LOCAL
 *    - Traversal.getSourceRegion:
 *      * next != null (followed by other statements) vs next == null (end of script -> Integer.MAX_VALUE)
 *    - Alias resolution work queue:
 *      * Transitive aliases resolution (referencesOtherAlias() == true deferred)
 *      * Alias cycles (newQueue.size() == aliasWorkQueue.size()) -> GOOG_SCOPE_ALIAS_CYCLE
 *    - Scope collapse & cleanup:
 *      * Var removal: hasOneChild() (parent detached) vs multi-var (child detached)
 *      * Scope block replacement and merging via NodeUtil.tryMergeBlock
 *    - Namespace shadowing & renaming:
 *      * forbiddenLocals detection and MakeDeclaredNamesUnique renaming
 *    - Type annotations (AliasedTypeNode):
 *      * JSDocInfo type nodes with dot (Type.Sub) vs without dot (Type)
 */
public class ScopedAliasesGeminiTest {

  private static final AliasTransformationHandler NULL_HANDLER =
      new AliasTransformationHandler() {
        @Override
        public AliasTransformation logAliasTransformation(
            String sourceFile, SourcePosition<AliasTransformation> position) {
          return new AliasTransformation() {
            @Override
            public void addAlias(String alias, String definition) {}
          };
        }
      };

  private Compiler runScopedAliases(String js) {
    return runScopedAliases(js, null, NULL_HANDLER);
  }

  private Compiler runScopedAliases(
      String js,
      PreprocessorSymbolTable symbolTable,
      AliasTransformationHandler handler) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.<SourceFile>emptyList(),
        Collections.singletonList(SourceFile.fromCode("testcode.js", js)),
        options);
    Node root = compiler.parseInputs();
    assertNotNull("Parsing root should not be null", root);
    Node externs = root.getFirstChild();
    Node mainScript = root.getLastChild();
    ScopedAliases pass =
        new ScopedAliases(compiler, symbolTable, handler != null ? handler : NULL_HANDLER);
    pass.process(externs, mainScript);
    return compiler;
  }

  private boolean hasError(Compiler compiler, DiagnosticType type) {
    for (JSError error : compiler.getErrors()) {
      if (error.getType().equals(type)) {
        return true;
      }
    }
    return false;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefectFunctionDeclaration() {
    String js =
        "goog.scope(function() {\n"
            + "  function f() {}\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertFalse(
        "Defect: function declaration should not be flagged as non-alias local",
        hasError(compiler, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL));
    assertEquals(
        "Defect: Expected zero errors for function declaration in goog.scope",
        0,
        compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testDefectHoistedFunctionDeclaration() {
    String js =
        "goog.scope(function() {\n"
            + "  f();\n"
            + "  function f() {}\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertFalse(
        "Defect: hoisted function declaration should not be flagged as non-alias local",
        hasError(compiler, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL));
    assertEquals(
        "Defect: Expected zero errors for hoisted function declaration in goog.scope",
        0,
        compiler.getErrorCount());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBasicAliasInliningAndScopeCollapse() {
    String js =
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  dom.createElement('div');\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals(0, compiler.getErrorCount());
    String source = compiler.toSource();
    assertTrue(source.contains("goog.dom.createElement"));
    assertFalse(source.contains("goog.scope"));
  }

  @Test(timeout = 4000)
  public void testTransitiveAliasResolution() {
    String js =
        "goog.scope(function() {\n"
            + "  var d = goog.dom;\n"
            + "  var el = d.createElement;\n"
            + "  el('div');\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals(0, compiler.getErrorCount());
    String source = compiler.toSource();
    assertTrue(source.contains("goog.dom.createElement"));
  }

  @Test(timeout = 4000)
  public void testMultiVarDeclarationDetaching() {
    String js =
        "goog.scope(function() {\n"
            + "  var a = goog.a, b = goog.b;\n"
            + "  a();\n"
            + "  b();\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals(0, compiler.getErrorCount());
    String source = compiler.toSource();
    assertTrue(source.contains("goog.a()"));
    assertTrue(source.contains("goog.b()"));
  }

  @Test(timeout = 4000)
  public void testNonQualifiedNameVarRewriteAndCollisionCounters() {
    String js =
        "goog.scope(function() {\n"
            + "  var x = 1 + 2;\n"
            + "  use(x);\n"
            + "});\n"
            + "goog.scope(function() {\n"
            + "  var x = 3 + 4;\n"
            + "  use(x);\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals(0, compiler.getErrorCount());
    String source = compiler.toSource();
    assertTrue(source.contains("$jscomp.scope.x"));
    assertTrue(source.contains("$jscomp.scope.x$1"));
  }

  @Test(timeout = 4000)
  public void testUninitializedVarDeclaration() {
    String js =
        "goog.scope(function() {\n"
            + "  var uninit;\n"
            + "  uninit = 5;\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals(0, compiler.getErrorCount());
    String source = compiler.toSource();
    assertTrue(source.contains("$jscomp.scope.uninit"));
  }

  @Test(timeout = 4000)
  public void testNamespaceShadowingRenamed() {
    String js =
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  function inner() {\n"
            + "    var goog = 1;\n"
            + "    return goog;\n"
            + "  }\n"
            + "  dom.create(inner());\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testMultipleInnerScopesShadowDetectionShortCircuit() {
    String js =
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  function f1() { var goog = 1; }\n"
            + "  function f2() { var goog = 2; }\n"
            + "  dom.init();\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testJSDocTypeFixingWithDotAndWithoutDot() {
    String js =
        "goog.scope(function() {\n"
            + "  var TypeA = ns.TypeA;\n"
            + "  /** @type {TypeA} */ var a;\n"
            + "  /** @type {TypeA.Sub} */ var b;\n"
            + "  /** @type {OtherType} */ var c;\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals(0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScript() {
    Compiler compiler = runScopedAliases("");
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testScriptWithoutGoogScope() {
    String js = "var x = 10; function regular() { return x; }";
    Compiler compiler = runScopedAliases(js);
    assertEquals(0, compiler.getErrorCount());
    assertTrue(compiler.toSource().contains("var x = 10"));
  }

  @Test(timeout = 4000)
  public void testEmptyGoogScopeCall() {
    String js = "goog.scope(function() {});";
    Compiler compiler = runScopedAliases(js);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGetSourceRegionWithSubsequentStatement() {
    String js =
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  dom.run();\n"
            + "});\n"
            + "var after = 1;";
    Compiler compiler = runScopedAliases(js);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGetSourceRegionAsLastStatement() {
    String js =
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  dom.run();\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testAliasingWithoutDotQualifiedName() {
    String js =
        "var myLocal = {};\n"
            + "goog.scope(function() {\n"
            + "  var alias = myLocal;\n"
            + "  alias.foo = 1;\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertEquals(0, compiler.getErrorCount());
    assertTrue(compiler.toSource().contains("myLocal.foo = 1"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths (Diagnostic Types)
  // =========================================================================

  @Test(timeout = 4000)
  public void testGoogScopeUsedImproperlyInExpression() {
    String js = "var notAlone = goog.scope(function() {});";
    Compiler compiler = runScopedAliases(js);
    assertTrue(
        "Should report GOOG_SCOPE_USED_IMPROPERLY when not single statement",
        hasError(compiler, ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY));
  }

  @Test(timeout = 4000)
  public void testGoogScopeHasBadParametersNoArgs() {
    String js = "goog.scope();";
    Compiler compiler = runScopedAliases(js);
    assertTrue(
        "Should report GOOG_SCOPE_HAS_BAD_PARAMETERS for 0 arguments",
        hasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testGoogScopeHasBadParametersExtraArgs() {
    String js = "goog.scope(function() {}, 123);";
    Compiler compiler = runScopedAliases(js);
    assertTrue(
        "Should report GOOG_SCOPE_HAS_BAD_PARAMETERS for 2 arguments",
        hasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testGoogScopeHasBadParametersNotAFunction() {
    String js = "goog.scope(42);";
    Compiler compiler = runScopedAliases(js);
    assertTrue(
        "Should report GOOG_SCOPE_HAS_BAD_PARAMETERS for non-function argument",
        hasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testGoogScopeHasBadParametersNamedFunction() {
    String js = "goog.scope(function named() {});";
    Compiler compiler = runScopedAliases(js);
    assertTrue(
        "Should report GOOG_SCOPE_HAS_BAD_PARAMETERS for named function",
        hasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testGoogScopeHasBadParametersFunctionWithParameters() {
    String js = "goog.scope(function(param) {});";
    Compiler compiler = runScopedAliases(js);
    assertTrue(
        "Should report GOOG_SCOPE_HAS_BAD_PARAMETERS when function takes params",
        hasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS));
  }

  @Test(timeout = 4000)
  public void testGoogScopeUsesReturnDirectly() {
    String js =
        "goog.scope(function() {\n"
            + "  return;\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertTrue(
        "Should report GOOG_SCOPE_USES_RETURN at top level of scope",
        hasError(compiler, ScopedAliases.GOOG_SCOPE_USES_RETURN));
  }

  @Test(timeout = 4000)
  public void testGoogScopeReferencesThisDirectly() {
    String js =
        "goog.scope(function() {\n"
            + "  this.foo = 1;\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertTrue(
        "Should report GOOG_SCOPE_REFERENCES_THIS at top level of scope",
        hasError(compiler, ScopedAliases.GOOG_SCOPE_REFERENCES_THIS));
  }

  @Test(timeout = 4000)
  public void testGoogScopeUsesThrowDirectly() {
    String js =
        "goog.scope(function() {\n"
            + "  throw 'err';\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertTrue(
        "Should report GOOG_SCOPE_USES_THROW at top level of scope",
        hasError(compiler, ScopedAliases.GOOG_SCOPE_USES_THROW));
  }

  @Test(timeout = 4000)
  public void testReturnAndThisAllowedInInnerFunctions() {
    String js =
        "goog.scope(function() {\n"
            + "  var dom = goog.dom;\n"
            + "  function inner() {\n"
            + "    this.prop = 1;\n"
            + "    if (true) throw 'inner err';\n"
            + "    return this.prop;\n"
            + "  }\n"
            + "  dom.call(inner);\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertFalse(hasError(compiler, ScopedAliases.GOOG_SCOPE_USES_RETURN));
    assertFalse(hasError(compiler, ScopedAliases.GOOG_SCOPE_REFERENCES_THIS));
    assertFalse(hasError(compiler, ScopedAliases.GOOG_SCOPE_USES_THROW));
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testGoogScopeAliasRedefined() {
    String js =
        "goog.scope(function() {\n"
            + "  var d = goog.dom;\n"
            + "  d = 2;\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertTrue(
        "Should report GOOG_SCOPE_ALIAS_REDEFINED when alias is re-assigned",
        hasError(compiler, ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED));
  }

  @Test(timeout = 4000)
  public void testGoogScopeAliasCycleDetected() {
    String js =
        "goog.scope(function() {\n"
            + "  var a = b.prop;\n"
            + "  var b = a.prop;\n"
            + "  a.run();\n"
            + "});";
    Compiler compiler = runScopedAliases(js);
    assertTrue(
        "Should report GOOG_SCOPE_ALIAS_CYCLE when aliases cyclically reference each other",
        hasError(compiler, ScopedAliases.GOOG_SCOPE_ALIAS_CYCLE));
  }

  @Test(timeout = 4000, expected = NullPointerException.class)
  public void testNullRootNodeThrowsException() {
    Compiler compiler = new Compiler();
    ScopedAliases pass = new ScopedAliases(compiler, null, NULL_HANDLER);
    pass.process(null, null);
  }

  // =========================================================================
  // Partition E: Handler and Symbol Table Integration
  // =========================================================================

  private static class RecordingAliasHandler implements AliasTransformationHandler {
    final Map<String, String> recordedAliases = new HashMap<>();
    int logCount = 0;

    @Override
    public AliasTransformation logAliasTransformation(
        String sourceFile, SourcePosition<AliasTransformation> position) {
      logCount++;
      return new AliasTransformation() {
        @Override
        public void addAlias(String alias, String definition) {
          recordedAliases.put(alias, definition);
        }
      };
    }
  }

  @Test(timeout = 4000)
  public void testAliasTransformationHandlerInvocations() {
    RecordingAliasHandler recordingHandler = new RecordingAliasHandler();
    String js =
        "goog.scope(function() {\n"
            + "  var myDom = goog.dom;\n"
            + "  var myDiv = goog.dom.TagName.DIV;\n"
            + "  myDom.create(myDiv);\n"
            + "});";
    Compiler compiler = runScopedAliases(js, null, recordingHandler);
    assertEquals(0, compiler.getErrorCount());
    assertEquals(1, recordingHandler.logCount);
    assertEquals("goog.dom", recordingHandler.recordedAliases.get("myDom"));
    assertEquals("goog.dom.TagName.DIV", recordingHandler.recordedAliases.get("myDiv"));
  }

  @Test(timeout = 4000)
  public void testPreprocessorSymbolTableReferenceLogged() {
    PreprocessorSymbolTable symbolTable = new PreprocessorSymbolTable(new Node(Token.SCRIPT));
    String js =
        "goog.scope(function() {\n"
            + "  var d = goog.dom;\n"
            + "  d.init();\n"
            + "});";
    Compiler compiler = runScopedAliases(js, symbolTable, NULL_HANDLER);
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testHotSwapScriptDirectExecution() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.init(
        Collections.<SourceFile>emptyList(),
        Collections.singletonList(
            SourceFile.fromCode("swap.js", "goog.scope(function() { var d = goog.dom; d(); });")),
        options);
    Node root = compiler.parseInputs();
    Node script = root.getLastChild().getFirstChild();

    ScopedAliases pass = new ScopedAliases(compiler, null, NULL_HANDLER);
    pass.hotSwapScript(script, null);
    assertEquals(0, compiler.getErrorCount());
  }
}