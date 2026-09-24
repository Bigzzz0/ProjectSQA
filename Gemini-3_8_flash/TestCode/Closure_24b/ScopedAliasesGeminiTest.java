package com.google.javascript.jscomp;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: ScopedAliases
 * Defect Reference: Defects4J ScopedAliasesTest::testNonAliasLocal
 *
 * Decision / Condition Matrix Covered:
 * 1. findAliases:
 *    - parent.isVar() == true && n.hasChildren() && n.getFirstChild().isQualifiedName()
 *      -> Normal alias registration + transformation.addAlias
 *    - parent.isVar() == true && (!n.hasChildren() || !n.getFirstChild().isQualifiedName())
 *      -> Target of Defect: GOOG_SCOPE_NON_ALIAS_LOCAL reported (e.g., primitives, null,
 *         binary expressions, uninitialized vars, object/array/function literals).
 * 2. validateScopeCall:
 *    - preprocessorSymbolTable != null vs null -> addReference() invocation.
 *    - !parent.isExprResult() -> GOOG_SCOPE_USED_IMPROPERLY reported.
 *    - n.getChildCount() != 2 -> GOOG_SCOPE_HAS_BAD_PARAMETERS (0 args, 2+ args).
 *    - anonymousFnNode is not FUNCTION -> GOOG_SCOPE_HAS_BAD_PARAMETERS.
 *    - anonymousFnNode has a name (named function) -> GOOG_SCOPE_HAS_BAD_PARAMETERS.
 *    - anonymousFnNode has parameter children -> GOOG_SCOPE_HAS_BAD_PARAMETERS.
 * 3. shouldTraverse:
 *    - n.isFunction() && t.inGlobalScope() && (parent == null || !isCallToScopeMethod)
 *      -> returns false (global non-goog.scope functions skipped).
 *    - goog.scope function traversed into.
 * 4. visit:
 *    - t.getScopeDepth() < 2 -> early return.
 *    - t.getScopeDepth() == 2 (Top-level of goog.scope):
 *      - aliasVar != null && NodeUtil.isLValue(n) && aliasVar.getNode() == n
 *        -> aliasDefinitionsInOrder recorded, early return.
 *      - aliasVar != null && NodeUtil.isLValue(n) && aliasVar.getNode() != n
 *        -> GOOG_SCOPE_ALIAS_REDEFINED reported.
 *      - type == Token.RETURN -> GOOG_SCOPE_USES_RETURN reported.
 *      - type == Token.THIS -> GOOG_SCOPE_REFERENCES_THIS reported.
 *      - type == Token.THROW -> GOOG_SCOPE_USES_THROW reported.
 *    - t.getScopeDepth() >= 2:
 *      - aliasVar != null (usage) -> AliasedNode created.
 *      - JSDocInfo != null with typeNodes -> fixTypeNode called (single, dot-separated, nested).
 *      - Inner functions (depth > 2) with RETURN, THIS, THROW -> permitted without error.
 * 5. hotSwapScript:
 *    - !traversal.hasErrors():
 *      - aliasDefinition.getParent().isVar() && hasOneChild() -> detach var.
 *      - aliasDefinition.getParent().isVar() && !hasOneChild() -> detach child name only.
 *      - scopeCalls collapsing -> replace expressionWithScopeCall with scopeClosureBlock.
 *      - code change reported if usages > 0 || definitions > 0 || scopeCalls > 0.
 *    - traversal.hasErrors():
 *      - AST remains unmodified, no aliases/scopes collapsed.
 */

import com.google.javascript.jscomp.CompilerOptions.AliasTransformation;
import com.google.javascript.jscomp.CompilerOptions.AliasTransformationHandler;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class ScopedAliasesGeminiTest {

  // =========================================================================
  // Test Helpers
  // =========================================================================

  private Compiler compile(String js, PreprocessorSymbolTable symbolTable,
                           AliasTransformationHandler handler) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, symbolTable, handler);
    pass.process(null, root);
    return compiler;
  }

  private Compiler compile(String js) {
    return compile(js, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
  }

  private void assertHasError(Compiler compiler, DiagnosticType type) {
    boolean found = false;
    for (JSError err : compiler.getErrors()) {
      if (err.getType() == type) {
        found = true;
        break;
      }
    }
    if (!found) {
      StringBuilder sb = new StringBuilder("Expected error " + type.key + " but got:\n");
      for (JSError err : compiler.getErrors()) {
        sb.append(" - ").append(err.toString()).append("\n");
      }
      fail(sb.toString());
    }
  }

  private void assertNoErrors(Compiler compiler) {
    if (compiler.getErrorCount() > 0) {
      StringBuilder sb = new StringBuilder("Expected 0 errors, but got " + compiler.getErrorCount() + ":\n");
      for (JSError err : compiler.getErrors()) {
        sb.append(" - ").append(err.toString()).append("\n");
      }
      fail(sb.toString());
    }
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // Targets: com.google.javascript.jscomp.ScopedAliasesTest::testNonAliasLocal
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefect_nonAliasLocal_numberLiteral() {
    Compiler compiler = compile("goog.scope(function() { var x = 10; });");
    assertEquals("Should report exactly 1 error for non-alias number literal", 1, compiler.getErrorCount());
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test(timeout = 4000)
  public void testDefect_nonAliasLocal_binaryExpression() {
    Compiler compiler = compile("goog.scope(function() { var x = goog.dom + 1; });");
    assertEquals(1, compiler.getErrorCount());
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test(timeout = 4000)
  public void testDefect_nonAliasLocal_functionExpression() {
    Compiler compiler = compile("goog.scope(function() { var x = function() {}; });");
    assertEquals(1, compiler.getErrorCount());
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test(timeout = 4000)
  public void testDefect_nonAliasLocal_nullLiteral() {
    Compiler compiler = compile("goog.scope(function() { var x = null; });");
    assertEquals(1, compiler.getErrorCount());
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test(timeout = 4000)
  public void testDefect_nonAliasLocal_uninitializedVar() {
    Compiler compiler = compile("goog.scope(function() { var x; });");
    assertEquals(1, compiler.getErrorCount());
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test(timeout = 4000)
  public void testDefect_nonAliasLocal_objectLiteral() {
    Compiler compiler = compile("goog.scope(function() { var x = {}; });");
    assertEquals(1, compiler.getErrorCount());
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test(timeout = 4000)
  public void testDefect_nonAliasLocal_arrayLiteral() {
    Compiler compiler = compile("goog.scope(function() { var x = []; });");
    assertEquals(1, compiler.getErrorCount());
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test(timeout = 4000)
  public void testDefect_nonAliasLocal_booleanLiteral() {
    Compiler compiler = compile("goog.scope(function() { var x = true; });");
    assertEquals(1, compiler.getErrorCount());
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCore_singleAliasReplacementAndScopeCollapse() {
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() { var dom = goog.dom; dom.createElement('div'); });";
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(null, root);

    assertNoErrors(compiler);
    String source = compiler.toSource(root);
    assertTrue("Alias 'dom' should be resolved to 'goog.dom'", source.contains("goog.dom.createElement"));
    assertFalse("Var declaration 'var dom' should be removed", source.contains("var dom"));
    assertFalse("goog.scope wrapper should be unwrapped", source.contains("goog.scope"));
  }

  @Test(timeout = 4000)
  public void testCore_transitiveAliasReplacement() {
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() { var g = goog; var d = g.dom; d.createElement('div'); });";
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(null, root);

    assertNoErrors(compiler);
    String source = compiler.toSource(root);
    assertTrue("Transitive alias 'd' should resolve to 'goog.dom'", source.contains("goog.dom.createElement"));
    assertFalse("Aliases should be removed", source.contains("var g"));
    assertFalse("Aliases should be removed", source.contains("var d"));
  }

  @Test(timeout = 4000)
  public void testCore_multipleAliasesInSingleVarStatement() {
    // Tests branch: aliasDefinition.getParent().hasOneChild() == false
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() { var dom = goog.dom, events = goog.events; dom.create(); events.listen(); });";
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(null, root);

    assertNoErrors(compiler);
    String source = compiler.toSource(root);
    assertTrue("dom should resolve to goog.dom", source.contains("goog.dom.create"));
    assertTrue("events should resolve to goog.events", source.contains("goog.events.listen"));
    assertFalse(source.contains("var dom"));
    assertFalse(source.contains("events ="));
  }

  @Test(timeout = 4000)
  public void testCore_aliasUsedInNestedFunction() {
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() { var dom = goog.dom; function helper() { return dom.create(); } });";
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(null, root);

    assertNoErrors(compiler);
    String source = compiler.toSource(root);
    assertTrue("dom in nested function should resolve to goog.dom", source.contains("goog.dom.create"));
  }

  @Test(timeout = 4000)
  public void testCore_multipleScopeBlocksInSameScript() {
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() { var dom = goog.dom; dom.create(); });\n" +
                "goog.scope(function() { var net = goog.net; net.fetch(); });";
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(null, root);

    assertNoErrors(compiler);
    String source = compiler.toSource(root);
    assertTrue(source.contains("goog.dom.create"));
    assertTrue(source.contains("goog.net.fetch"));
  }

  @Test(timeout = 4000)
  public void testCore_aliasTransformationLogging() {
    final Map<String, String> recordedAliases = new HashMap<>();
    AliasTransformationHandler handler = new AliasTransformationHandler() {
      @Override
      public AliasTransformation logAliasTransformation(
          String sourceFile, SourcePosition<AliasTransformation> position) {
        assertNotNull(position);
        return new AliasTransformation() {
          @Override
          public void addAlias(String alias, String definition) {
            recordedAliases.put(alias, definition);
          }
        };
      }
    };

    Compiler compiler = compile(
        "goog.scope(function() { var dom = goog.dom; var events = goog.events.Event; });",
        null, handler);
    assertNoErrors(compiler);
    assertEquals("goog.dom", recordedAliases.get("dom"));
    assertEquals("goog.events.Event", recordedAliases.get("events"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Type Node Transformations
  // =========================================================================

  @Test(timeout = 4000)
  public void testBva_emptyScope() {
    Compiler compiler = compile("goog.scope(function() {});");
    assertNoErrors(compiler);
  }

  @Test(timeout = 4000)
  public void testBva_scopeWithStatementsButNoAliases() {
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() { window.alert('hello'); });";
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(null, root);

    assertNoErrors(compiler);
    String source = compiler.toSource(root);
    assertTrue(source.contains("window.alert(\"hello\")") || source.contains("window.alert('hello')"));
    assertFalse(source.contains("goog.scope"));
  }

  @Test(timeout = 4000)
  public void testBva_scriptWithoutAnyScopeCalls() {
    Compiler compiler = new Compiler();
    String js = "var a = 1; function test() { return a; }";
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(null, root);

    assertNoErrors(compiler);
    String source = compiler.toSource(root);
    assertTrue(source.contains("var a = 1"));
  }

  @Test(timeout = 4000)
  public void testBva_globalFunctionNotTraversed() {
    // Tests shouldTraverse returning false for non-goog.scope global functions
    Compiler compiler = new Compiler();
    String js = "function regularGlobal() { var x = 10; return x; }";
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(null, root);

    // No error because regular functions outside goog.scope are not constrained
    assertNoErrors(compiler);
  }

  @Test(timeout = 4000)
  public void testBva_preprocessorSymbolTableIntegration() {
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() { var dom = goog.dom; dom.create(); });";
    Node root = compiler.parseTestCode(js);
    PreprocessorSymbolTable table = new PreprocessorSymbolTable(root);
    ScopedAliases pass = new ScopedAliases(compiler, table, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(null, root);

    assertNoErrors(compiler);
  }

  @Test(timeout = 4000)
  public void testBva_jsdocTypeAnnotation_singleAlias() {
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() {\n" +
                "  var MyClass = my.package.MyClass;\n" +
                "  /** @type {MyClass} */ var inst;\n" +
                "});";
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(null, root);

    assertNoErrors(compiler);
    // Find the VAR node for 'inst' and assert its JSDoc type was corrected
    Node instVar = findVarNode(root, "inst");
    assertNotNull(instVar);
    JSDocInfo info = instVar.getJSDocInfo();
    assertNotNull(info);
    for (Node typeNode : info.getTypeNodes()) {
      assertEquals("my.package.MyClass", typeNode.getString());
    }
  }

  @Test(timeout = 4000)
  public void testBva_jsdocTypeAnnotation_dottedSubtype() {
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() {\n" +
                "  var MyClass = my.package.MyClass;\n" +
                "  /** @type {MyClass.Subtype} */ var inst;\n" +
                "});";
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(null, root);

    assertNoErrors(compiler);
    Node instVar = findVarNode(root, "inst");
    assertNotNull(instVar);
    JSDocInfo info = instVar.getJSDocInfo();
    assertNotNull(info);
    for (Node typeNode : info.getTypeNodes()) {
      assertEquals("my.package.MyClass.Subtype", typeNode.getString());
    }
  }

  private Node findVarNode(Node n, String varName) {
    if (n.isVar()) {
      for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
        if (varName.equals(child.getString())) {
          return n;
        }
      }
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findVarNode(child, varName);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  // =========================================================================
  // Partition D: Diagnostic Error & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testError_scopeUsedImproperly_assignedToVar() {
    Compiler compiler = compile("var s = goog.scope(function() {});");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  @Test(timeout = 4000)
  public void testError_badParameters_zeroArgs() {
    Compiler compiler = compile("goog.scope();");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testError_badParameters_twoArgs() {
    Compiler compiler = compile("goog.scope(function() {}, 123);");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testError_badParameters_nonFunctionArg() {
    Compiler compiler = compile("goog.scope(42);");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testError_badParameters_namedFunction() {
    Compiler compiler = compile("goog.scope(function namedScope() {});");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testError_badParameters_functionWithParameters() {
    Compiler compiler = compile("goog.scope(function(param) {});");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testError_aliasRedefined() {
    Compiler compiler = compile("goog.scope(function() { var d = goog.dom; d = goog.events; });");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  @Test(timeout = 4000)
  public void testError_scopeReferencesThis_atTopLevel() {
    Compiler compiler = compile("goog.scope(function() { this.method(); });");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  @Test(timeout = 4000)
  public void testError_scopeUsesReturn_atTopLevel() {
    Compiler compiler = compile("goog.scope(function() { return 1; });");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  @Test(timeout = 4000)
  public void testError_scopeUsesThrow_atTopLevel() {
    Compiler compiler = compile("goog.scope(function() { throw 'error'; });");
    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  @Test(timeout = 4000)
  public void testNestedFunction_canUseThisReturnThrow() {
    // Inside inner functions (depth > 2), this, return, throw are allowed
    Compiler compiler = compile(
        "goog.scope(function() {\n" +
        "  function inner() {\n" +
        "    if (true) return this;\n" +
        "    throw 'fail';\n" +
        "  }\n" +
        "});");
    assertNoErrors(compiler);
  }

  @Test(timeout = 4000)
  public void testError_preventsTransformationExecution() {
    // When traversal has errors, AST should NOT be transformed
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() { var d = goog.dom; return 1; d.create(); });";
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(null, root);

    assertHasError(compiler, ScopedAliases.GOOG_SCOPE_USES_RETURN);
    String source = compiler.toSource(root);
    // goog.scope and return should still be in the AST since pass aborted transformations
    assertTrue(source.contains("goog.scope"));
    assertTrue(source.contains("return"));
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testLifecycle_scopingMethodNameConstant() {
    assertEquals("goog.scope", ScopedAliases.SCOPING_METHOD_NAME);
  }

  @Test(timeout = 4000)
  public void testLifecycle_hotSwapScriptDirectInvocation() {
    Compiler compiler = new Compiler();
    String js = "goog.scope(function() { var dom = goog.dom; dom.create(); });";
    Node root = compiler.parseTestCode(js);
    ScopedAliases pass = new ScopedAliases(compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    
    // Explicitly test hotSwapScript(root, originalRoot) with non-null roots
    pass.hotSwapScript(root, root);
    assertNoErrors(compiler);
    String source = compiler.toSource(root);
    assertTrue(source.contains("goog.dom.create"));
  }
}