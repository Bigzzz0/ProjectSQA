package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.CompilerOptions.AliasTransformation;
import com.google.javascript.jscomp.CompilerOptions.AliasTransformationHandler;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.SourcePosition;
import com.google.javascript.rhino.Token;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.ScopedAliases
 *
 * Core Decision Logic & Branches Targeted:
 * 1. Scope Validation:
 *    - parent.isExprResult() -> GOOG_SCOPE_USED_IMPROPERLY branch vs valid statement branch.
 *    - n.getChildCount() != 2 -> GOOG_SCOPE_HAS_BAD_PARAMETERS (0 or >1 arguments).
 *    - !anonymousFn.isFunction() -> GOOG_SCOPE_HAS_BAD_PARAMETERS (literal/object parameter).
 *    - fnName != null -> GOOG_SCOPE_HAS_BAD_PARAMETERS (named function parameter).
 *    - fnParams.hasChildren() -> GOOG_SCOPE_HAS_BAD_PARAMETERS (parameterized function).
 *
 * 2. Scope Traversal & AST Rewriting:
 *    - shouldTraverse: non-goog.scope global functions pruned vs entered.
 *    - enterScope / exitScope: Scope depth 2 management, alias caching, shadow state clearing.
 *    - Alias resolution: single child var removal vs multi-declarator var detachment.
 *    - Scope collapse: Detaching closure block and splicing into parent AST via NodeUtil.tryMergeBlock.
 *    - Transitive aliases: var g = goog; var d = g.dom; handling without premature cloning.
 *
 * 3. Diagnostic Warnings & Errors:
 *    - Token.RETURN at scope depth 2 -> GOOG_SCOPE_USES_RETURN.
 *    - Token.THIS at scope depth 2 -> GOOG_SCOPE_REFERENCES_THIS.
 *    - Token.THROW at scope depth 2 -> GOOG_SCOPE_USES_THROW.
 *    - NodeUtil.isLValue(n) && aliasVar.getNode() != n -> GOOG_SCOPE_ALIAS_REDEFINED.
 *    - Local var not initialized to qualified name -> GOOG_SCOPE_NON_ALIAS_LOCAL.
 *    - Inner scopes (depth > 2) allowing this, return, throw without diagnostic error.
 *
 * 4. Namespace Shadowing (forbiddenLocals):
 *    - Tracking qualified name root in forbiddenLocals.
 *    - Identifying inner scope declarations conflicting with forbidden namespaces.
 *    - Invoking MakeDeclaredNamesUnique to safely rename colliding inner variables.
 *
 * 5. Defects4J Targeted Defect (Issue 772):
 *    - testIssue772: Type information / JSDoc annotations preserved during alias elimination
 *      and collapsing of scopes without triggering AST tree inequality or type check warnings.
 */
public class ScopedAliasesGeminiTest {

  private static class CompileResult {
    final Compiler compiler;
    final Node root;

    CompileResult(Compiler compiler, Node root) {
      this.compiler = compiler;
      this.root = root;
    }
  }

  private CompileResult compile(String js) {
    return compile(js, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
  }

  private CompileResult compile(
      String js,
      PreprocessorSymbolTable preprocessorSymbolTable,
      AliasTransformationHandler transformationHandler) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseTestCode(js);
    Node externs = new Node(Token.BLOCK);

    ScopedAliases pass =
        new ScopedAliases(compiler, preprocessorSymbolTable, transformationHandler);
    pass.process(externs, root);
    return new CompileResult(compiler, root);
  }

  private void testScopedAliases(String js, String expected) {
    CompileResult result = compile(js);
    assertEquals(
        "Expected no errors but got: " + Arrays.toString(result.compiler.getErrors()),
        0,
        result.compiler.getErrorCount());
    Compiler expCompiler = new Compiler();
    expCompiler.initOptions(new CompilerOptions());
    Node expRoot = expCompiler.parseTestCode(expected);
    assertEquals(
        expCompiler.toSource(expRoot).trim(),
        result.compiler.toSource(result.root).trim());
  }

  private void testError(String js, DiagnosticType expectedError) {
    CompileResult result = compile(js);
    assertTrue(
        "Expected error " + expectedError.key + " but encountered 0 errors",
        result.compiler.getErrorCount() > 0);
    assertEquals(expectedError, result.compiler.getErrors()[0].getType());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleAliasReplacement() {
    testScopedAliases(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  dom.createElement('div');" +
        "});",
        "goog.dom.createElement('div');");
  }

  @Test(timeout = 4000)
  public void testTransitiveAliasReplacement() {
    testScopedAliases(
        "goog.scope(function() {" +
        "  var g = goog;" +
        "  var d = g.dom;" +
        "  d.createElement('div');" +
        "});",
        "goog.dom.createElement('div');");
  }

  @Test(timeout = 4000)
  public void testMultipleAliasesInScope() {
    testScopedAliases(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  var events = goog.events;" +
        "  dom.createElement('div');" +
        "  events.listen();" +
        "});",
        "goog.dom.createElement('div');" +
        "goog.events.listen();");
  }

  @Test(timeout = 4000)
  public void testMultipleUsagesOfSameAlias() {
    testScopedAliases(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  dom.createElement('div');" +
        "  dom.createElement('span');" +
        "});",
        "goog.dom.createElement('div');" +
        "goog.dom.createElement('span');");
  }

  @Test(timeout = 4000)
  public void testAliasWithPropertyAccess() {
    testScopedAliases(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  var tag = dom.TagName.DIV;" +
        "  dom.createElement(tag);" +
        "});",
        "goog.dom.createElement(goog.dom.TagName.DIV);");
  }

  @Test(timeout = 4000)
  public void testMultipleConsecutiveGoogScopeBlocks() {
    testScopedAliases(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  dom.createElement('div');" +
        "});" +
        "goog.scope(function() {" +
        "  var events = goog.events;" +
        "  events.listen();" +
        "});",
        "goog.dom.createElement('div');" +
        "goog.events.listen();");
  }

  @Test(timeout = 4000)
  public void testUnusedAliasDefinitionRemoved() {
    testScopedAliases(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "});",
        "");
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyGoogScopeBlock() {
    testScopedAliases("goog.scope(function() {});", "");
  }

  @Test(timeout = 4000)
  public void testScopeWithoutAnyAliases() {
    testScopedAliases(
        "goog.scope(function() {" +
        "  goog.dom.createElement('div');" +
        "});",
        "goog.dom.createElement('div');");
  }

  @Test(timeout = 4000)
  public void testMultipleDeclaratorsInSingleVarStatement() {
    testScopedAliases(
        "goog.scope(function() {" +
        "  var a = goog.a, b = goog.b;" +
        "  a();" +
        "  b();" +
        "});",
        "goog.a();" +
        "goog.b();");
  }

  @Test(timeout = 4000)
  public void testAliasTransformationHandlerLogging() {
    final List<String> aliasesLogged = new ArrayList<String>();
    AliasTransformationHandler handler = new AliasTransformationHandler() {
      @Override
      public AliasTransformation logAliasTransformation(
          String sourceFile, SourcePosition<AliasTransformation> position) {
        assertNotNull(position);
        return new AliasTransformation() {
          @Override
          public void addAlias(String alias, String definition) {
            aliasesLogged.add(alias + " -> " + definition);
          }
        };
      }
    };

    String js = "goog.scope(function() { var dom = goog.dom; dom.init(); });";
    CompileResult result = compile(js, null, handler);
    assertEquals(0, result.compiler.getErrorCount());
    assertEquals(1, aliasesLogged.size());
    assertEquals("dom -> goog.dom", aliasesLogged.get(0));
  }

  @Test(timeout = 4000)
  public void testPreprocessorSymbolTableReferenceRecording() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node root = compiler.parseTestCode(
        "goog.scope(function() { var dom = goog.dom; dom.init(); });");
    PreprocessorSymbolTable table = new PreprocessorSymbolTable(root);
    ScopedAliases pass = new ScopedAliases(
        compiler, table, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.process(new Node(Token.BLOCK), root);
    assertEquals(0, compiler.getErrorCount());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Issue 772)
  // =========================================================================

  /**
   * Targets Closure Issue 772 where alias definitions with type annotations or type
   * references inside goog.scope caused node tree inequality / invalid type checking.
   */
  @Test(timeout = 4000)
  public void testIssue772() {
    testScopedAliases(
        "var b = null;" +
        "goog.scope(function () {" +
        "  /** @type {Foo} */" +
        "  var a = b;" +
        "  a.c = 2;" +
        "});",
        "var b = null;" +
        "b.c = 2;");
  }

  @Test(timeout = 4000)
  public void testIssue772_typeAnnotationWithAliasedNamespace() {
    testScopedAliases(
        "var a = {};" +
        "goog.scope(function() {" +
        "  var b = a.b;" +
        "  /** @type {b} */" +
        "  a.b.c = 1;" +
        "});",
        "var a = {};" +
        "/** @type {a.b} */" +
        "a.b.c = 1;");
  }

  @Test(timeout = 4000)
  public void testIssue772_typeAnnotationWithSubproperty() {
    testScopedAliases(
        "var a = {};" +
        "goog.scope(function() {" +
        "  var b = a.b;" +
        "  /** @type {b.Sub} */" +
        "  a.b.c = 1;" +
        "});",
        "var a = {};" +
        "/** @type {a.b.Sub} */" +
        "a.b.c = 1;");
  }

  @Test(timeout = 4000)
  public void testIssue772_jsdocFunctionParameterAlias() {
    testScopedAliases(
        "var a = {};" +
        "goog.scope(function() {" +
        "  var b = a.b;" +
        "  /** @param {b} x */" +
        "  a.b.c = function(x) {};" +
        "});",
        "var a = {};" +
        "/** @param {a.b} x */" +
        "a.b.c = function(x) {};");
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths (Diagnostic Errors)
  // =========================================================================

  @Test(timeout = 4000)
  public void testErrorGoogScopeUsedImproperlyInVarAssignment() {
    testError("var x = goog.scope(function() {});", ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  @Test(timeout = 4000)
  public void testErrorGoogScopeUsedImproperlyInExpression() {
    testError("1 + goog.scope(function() {});", ScopedAliases.GOOG_SCOPE_USED_IMPROPERLY);
  }

  @Test(timeout = 4000)
  public void testErrorGoogScopeHasBadParameters_noArguments() {
    testError("goog.scope();", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testErrorGoogScopeHasBadParameters_tooManyArguments() {
    testError("goog.scope(function() {}, 123);", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testErrorGoogScopeHasBadParameters_nonFunctionArgument() {
    testError("goog.scope(123);", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testErrorGoogScopeHasBadParameters_namedFunction() {
    testError("goog.scope(function named() {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testErrorGoogScopeHasBadParameters_functionWithParameters() {
    testError("goog.scope(function(param) {});", ScopedAliases.GOOG_SCOPE_HAS_BAD_PARAMETERS);
  }

  @Test(timeout = 4000)
  public void testErrorGoogScopeReferencesThis() {
    testError(
        "goog.scope(function() {" +
        "  this.foo = 1;" +
        "});",
        ScopedAliases.GOOG_SCOPE_REFERENCES_THIS);
  }

  @Test(timeout = 4000)
  public void testErrorGoogScopeUsesReturn() {
    testError(
        "goog.scope(function() {" +
        "  return 1;" +
        "});",
        ScopedAliases.GOOG_SCOPE_USES_RETURN);
  }

  @Test(timeout = 4000)
  public void testErrorGoogScopeUsesThrow() {
    testError(
        "goog.scope(function() {" +
        "  throw 'error';" +
        "});",
        ScopedAliases.GOOG_SCOPE_USES_THROW);
  }

  @Test(timeout = 4000)
  public void testErrorGoogScopeAliasRedefined() {
    testError(
        "goog.scope(function() {" +
        "  var a = goog.a;" +
        "  a = goog.b;" +
        "});",
        ScopedAliases.GOOG_SCOPE_ALIAS_REDEFINED);
  }

  @Test(timeout = 4000)
  public void testErrorGoogScopeNonAliasLocal_primitiveLiteral() {
    testError(
        "goog.scope(function() {" +
        "  var x = 1;" +
        "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test(timeout = 4000)
  public void testErrorGoogScopeNonAliasLocal_uninitialized() {
    testError(
        "goog.scope(function() {" +
        "  var x;" +
        "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  @Test(timeout = 4000)
  public void testErrorGoogScopeNonAliasLocal_functionDeclaration() {
    testError(
        "goog.scope(function() {" +
        "  function f() {}" +
        "});",
        ScopedAliases.GOOG_SCOPE_NON_ALIAS_LOCAL);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Structural Integrations
  // =========================================================================

  @Test(timeout = 4000)
  public void testNamespaceShadowRenamingInInnerScope() {
    // When an inner scope declares a variable that shadows the root of an alias (goog),
    // MakeDeclaredNamesUnique is invoked to rename the inner variable safely.
    testScopedAliases(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  dom.fn = function() {" +
        "    var goog = 123;" +
        "    return goog;" +
        "  };" +
        "});",
        "goog.dom.fn = function() {" +
        "  var goog$jscomp$1 = 123;" +
        "  return goog$jscomp$1;" +
        "};");
  }

  @Test(timeout = 4000)
  public void testInnerScopeAllowsReturnThisAndThrow() {
    // Return, this, and throw are permitted inside nested functions within goog.scope.
    testScopedAliases(
        "goog.scope(function() {" +
        "  var dom = goog.dom;" +
        "  dom.fn = function() {" +
        "    if (this.error) throw 'err';" +
        "    return this.val;" +
        "  };" +
        "});",
        "goog.dom.fn = function() {" +
        "  if (this.error) throw 'err';" +
        "  return this.val;" +
        "};");
  }

  @Test(timeout = 4000)
  public void testGlobalFunctionOutsideGoogScopeNotTraversed() {
    testScopedAliases(
        "function globalFn() {" +
        "  var x = 1;" +
        "  return x;" +
        "}" +
        "goog.scope(function() {" +
        "  var a = goog.a;" +
        "  a.init();" +
        "});",
        "function globalFn() {" +
        "  var x = 1;" +
        "  return x;" +
        "}" +
        "goog.a.init();");
  }

  @Test(timeout = 4000)
  public void testHotSwapScriptDirectInvocation() {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node root = compiler.parseTestCode(
        "goog.scope(function() { var dom = goog.dom; dom.init(); });");
    ScopedAliases pass = new ScopedAliases(
        compiler, null, CompilerOptions.NULL_ALIAS_TRANSFORMATION_HANDLER);
    pass.hotSwapScript(root, null);
    assertEquals(0, compiler.getErrorCount());
    assertTrue(compiler.toSource(root).contains("goog.dom.init();"));
  }
}