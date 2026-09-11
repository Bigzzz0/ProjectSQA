package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Test;

/**
 * High-coverage JUnit 4 test suite for {@link RemoveUnusedVars}.
 *
 * These tests exercise the mark-and-sweep traversal logic, the various
 * configuration flags (removeGlobals, preserveFunctionExpressionNames,
 * modifyCallSites), and known edge cases (catch vars, for-in vars,
 * arguments object usage, prototype/property assigns, and function
 * argument trimming).
 */
public class RemoveUnusedVarsClaudeTest {

  private Compiler compiler;

  /**
   * Parses the given JS source, runs the RemoveUnusedVars pass with the
   * given configuration, and returns the resulting (mutated) SCRIPT node.
   */
  private Node parseAndProcess(String js, boolean removeGlobals,
      boolean preserveFunctionExpressionNames, boolean modifyCallSites) {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node script = compiler.parseTestCode(js);
    Node externsRoot = new Node(Token.BLOCK);
    Node jsRoot = new Node(Token.BLOCK, script);

    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(
        compiler, removeGlobals, preserveFunctionExpressionNames, modifyCallSites);
    pass.process(externsRoot, jsRoot);

    return script;
  }

  /**
   * Recursively searches for a NAME node with the given text anywhere
   * in the subtree rooted at n.
   */
  private boolean containsNameNode(Node n, String name) {
    if (n.isName() && name.equals(n.getString())) {
      return true;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      if (containsNameNode(c, name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Recursively searches for a FUNCTION node whose name matches the
   * given string.
   */
  private Node findFunctionNode(Node n, String name) {
    if (n.isFunction() && n.getFirstChild() != null
        && name.equals(n.getFirstChild().getString())) {
      return n;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      Node result = findFunctionNode(c, name);
      if (result != null) {
        return result;
      }
    }
    return null;
  }

  /**
   * @target RemoveUnusedVars#process(Node, Node)
   * @scenario Empty script processed with removeGlobals=true.
   * @defectRisk Ensures the pass does not throw or mutate an empty AST.
   */
  @Test(timeout = 4000)
  public void testProcessEmptyScript() {
    Node script = parseAndProcess("", true, false, false);
    assertNotNull(script);
    assertFalse(script.hasChildren());
  }

  /**
   * @target isRemovableVar / removeUnreferencedVars
   * @scenario Global var with literal initializer, never referenced,
   *           removeGlobals=true.
   * @defectRisk Ensures unreferenced global vars are fully removed while
   *             referenced ones are preserved.
   */
  @Test(timeout = 4000)
  public void testRemoveGlobalVar_RemoveGlobalsTrue() {
    String js = "var x = 1; var y = 2; alert(y);";
    Node script = parseAndProcess(js, true, false, false);
    assertFalse(containsNameNode(script, "x"));
    assertTrue(containsNameNode(script, "y"));
  }

  /**
   * @target isRemovableVar
   * @scenario Global var unreferenced with removeGlobals=false.
   * @defectRisk Ensures global vars are never removed when removeGlobals
   *             is disabled, regardless of usage.
   */
  @Test(timeout = 4000)
  public void testKeepGlobalVar_RemoveGlobalsFalse() {
    String js = "var x = 1;";
    Node script = parseAndProcess(js, false, false, false);
    assertTrue(containsNameNode(script, "x"));
  }

  /**
   * @target isRemovableVar (local variable branch)
   * @scenario Local unreferenced var inside a function, removeGlobals=false.
   * @defectRisk Confirms local var removal is independent of removeGlobals
   *             flag (only applies to global vars).
   */
  @Test(timeout = 4000)
  public void testRemoveLocalVarInFunction_RegardlessOfRemoveGlobals() {
    String js = "function f() { var unused = 1; return 2; }";
    Node script = parseAndProcess(js, false, false, false);
    assertFalse(containsNameNode(script, "unused"));
  }

  /**
   * @target removeUnreferencedVars (multi-name var declaration branch)
   * @scenario var a, b, c; where only b is used.
   * @defectRisk Ensures only unreferenced names are stripped from a
   *             multi-name var statement, not the entire statement.
   */
  @Test(timeout = 4000)
  public void testMultipleVarDeclaration_PartialRemoval() {
    String js = "var a = 1, b = 2, c = 3; alert(b);";
    Node script = parseAndProcess(js, true, false, false);
    assertFalse(containsNameNode(script, "a"));
    assertTrue(containsNameNode(script, "b"));
    assertFalse(containsNameNode(script, "c"));
  }

  /**
   * @target removeUnreferencedVars (side-effect initializer branch)
   * @scenario var a = foo(); where a is unused but foo() may have side
   *           effects.
   * @defectRisk Ensures the var wrapper is dropped but the side-effecting
   *             call expression is preserved as a standalone statement.
   */
  @Test(timeout = 4000)
  public void testVarWithSideEffectInitializer_ReplacedWithExprResult() {
    String js = "function foo() { return 1; } var a = foo();";
    Node script = parseAndProcess(js, true, false, false);
    assertFalse(containsNameNode(script, "a"));

    Node secondStmt = script.getChildAtIndex(1);
    assertEquals(Token.EXPR_RESULT, secondStmt.getType());
    assertTrue(secondStmt.getFirstChild().isCall());
  }

  /**
   * @target interpretAssigns (property-assign to literal-valued var)
   * @scenario var x = {}; x.foo = 1; where x is never otherwise used.
   * @defectRisk Ensures property assigns on a literal-valued, unescaped
   *             variable do not artificially keep the variable alive.
   */
  @Test(timeout = 4000)
  public void testPropertyAssignToUnusedVar_Removed() {
    String js = "var x = {}; x.foo = 1;";
    Node script = parseAndProcess(js, true, false, false);
    assertFalse(containsNameNode(script, "x"));
  }

  /**
   * @target traverseNode (Token.NAME, catch variable special case)
   * @scenario try/catch with an unused catch parameter.
   * @defectRisk Ensures catch variables are never removed even if unused,
   *             per the pass's documented design.
   */
  @Test(timeout = 4000)
  public void testCatchVariable_NeverRemoved() {
    String js = "function f() { try { bar(); } catch (e) { } }";
    Node script = parseAndProcess(js, true, false, false);
    assertTrue(containsNameNode(script, "e"));
  }

  /**
   * @target removeUnreferencedVars (for-in / 3-child FOR guard)
   * @scenario for (var i in obj) {} where i is unused inside the loop body.
   * @defectRisk Ensures for-in/for-of loop variables are left untouched
   *             (child count &lt; 4 guard).
   */
  @Test(timeout = 4000)
  public void testForInVariable_NeverRemoved() {
    String js = "for (var i in obj) { }";
    Node script = parseAndProcess(js, true, false, false);
    assertTrue(containsNameNode(script, "i"));
  }

  /**
   * @target traverseNode (Token.NAME, "arguments" special case)
   * @scenario Function references the `arguments` object; all declared
   *           params should be considered referenced as a result.
   * @defectRisk Ensures escaping via arguments object prevents removal of
   *             otherwise-unused parameters.
   */
  @Test(timeout = 4000)
  public void testArgumentsObjectReference_KeepsAllParams() {
    String js = "function f(a, b, c) { return arguments.length; }";
    Node script = parseAndProcess(js, true, false, false);
    Node function = findFunctionNode(script, "f");
    assertNotNull(function);
    Node paramList = function.getFirstChild().getNext();
    assertEquals(3, paramList.getChildCount());
  }

  /**
   * @target removeUnreferencedFunctionArgs (getter/setter guard)
   * @scenario Object literal setter with an unused parameter.
   * @defectRisk Ensures parameters of getter/setter functions are never
   *             stripped, since Function.prototype.length semantics differ.
   */
  @Test(timeout = 4000)
  public void testSetterFunction_UnusedParamNotRemoved() {
    String js = "var obj = { set foo(value) { } };";
    Node script = parseAndProcess(js, true, false, false);
    assertTrue(containsNameNode(script, "value"));
  }

  /**
   * @target removeUnreferencedFunctionArgs (trailing-arg trim, no modifyCallSites)
   * @scenario function f(a, b) { return a; } with removeGlobals=true.
   * @defectRisk Ensures unused trailing parameters are trimmed from the
   *             function declaration when globals may be removed.
   */
  @Test(timeout = 4000)
  public void testFunctionArgRemoval_WhenRemoveGlobalsTrue() {
    String js = "function f(a, b) { return a; }";
    Node script = parseAndProcess(js, true, false, false);
    Node function = findFunctionNode(script, "f");
    assertNotNull(function);
    Node paramList = function.getFirstChild().getNext();
    assertEquals(1, paramList.getChildCount());
    assertEquals("a", paramList.getFirstChild().getString());
  }

  /**
   * @target traverseAndRemoveUnusedReferences / removeUnreferencedFunctionArgs
   * @scenario function a(x, y) { return x; } with removeGlobals=false.
   *           The parameter y is unused.
   * @defectRisk KNOWN DEFECT: the loop that removes unreferenced function
   *             arguments in traverseAndRemoveUnusedReferences runs
   *             unconditionally instead of being gated on removeGlobals.
   *             This test MUST fail on the defective version (which
   *             erroneously strips 'y') and pass on the fixed version
   *             (which preserves 'y' when removeGlobals is false).
   */
  @Test(timeout = 4000)
  public void testFunctionArgs_NotRemoved_WhenRemoveGlobalsFalse_Issue168b() {
    String js = "function a(x, y) { return x; }";
    Node script = parseAndProcess(js, false, false, false);
    Node function = findFunctionNode(script, "a");
    assertNotNull(function);
    Node paramList = function.getFirstChild().getNext();
    assertEquals(
        "Unused parameter 'y' should NOT be removed when removeGlobals is false",
        2, paramList.getChildCount());
  }

  /**
   * @target removeUnreferencedVars (function-expression name handling)
   * @scenario Named function expression whose name is unreferenced,
   *           preserveFunctionExpressionNames=true.
   * @defectRisk Ensures the name is retained (not blanked) when the
   *             preserve flag is enabled.
   */
  @Test(timeout = 4000)
  public void testPreserveFunctionExpressionNames_True() {
    String js = "var f = function foo() { return 1; }; f();";
    Node script = parseAndProcess(js, true, true, false);
    assertTrue(containsNameNode(script, "foo"));
  }

  /**
   * @target removeUnreferencedVars (function-expression name handling)
   * @scenario Named function expression whose name is unreferenced,
   *           preserveFunctionExpressionNames=false.
   * @defectRisk Ensures the name is blanked out (setString("")) when the
   *             preserve flag is disabled.
   */
  @Test(timeout = 4000)
  public void testPreserveFunctionExpressionNames_False() {
    String js = "var f = function foo() { return 1; }; f();";
    Node script = parseAndProcess(js, true, false, false);
    assertFalse(containsNameNode(script, "foo"));
  }

  /**
   * @target CallSiteOptimizer#optimize / markUnreferencedFunctionArgs
   * @scenario Simple function with one call site, unused trailing param,
   *           modifyCallSites=true.
   * @defectRisk Ensures both the function signature and the call site
   *             argument list are trimmed together when call sites can be
   *             safely modified.
   */
  @Test(timeout = 4000)
  public void testModifyCallSites_RemovesParamFromCallSite() {
    String js = "function foo(a, b) { return a; } foo(1, 2);";
    Node script = parseAndProcess(js, true, false, true);

    Node function = findFunctionNode(script, "foo");
    assertNotNull(function);
    Node paramList = function.getFirstChild().getNext();
    assertEquals(1, paramList.getChildCount());

    Node callStmt = script.getChildAtIndex(1);
    Node call = callStmt.getFirstChild();
    assertTrue(call.isCall());
    assertEquals(1, call.getChildCount() - 1);
  }

  /**
   * @target CallSiteOptimizer (disabled path) / removeUnreferencedFunctionArgs
   * @scenario Same code as the modifyCallSites test, but modifyCallSites=false.
   * @defectRisk Ensures that without modifyCallSites, only the function
   *             declaration is trimmed and call sites remain untouched.
   */
  @Test(timeout = 4000)
  public void testModifyCallSites_False_CallSiteUnchanged() {
    String js = "function foo(a, b) { return a; } foo(1, 2);";
    Node script = parseAndProcess(js, true, false, false);

    Node function = findFunctionNode(script, "foo");
    assertNotNull(function);
    Node paramList = function.getFirstChild().getNext();
    assertEquals(1, paramList.getChildCount());

    Node callStmt = script.getChildAtIndex(1);
    Node call = callStmt.getFirstChild();
    assertTrue(call.isCall());
    assertEquals(2, call.getChildCount() - 1);
  }

  /**
   * @target traverseFunction / allFunctionScopes (nested scopes)
   * @scenario Nested function declarations, inner function has an unused
   *           local variable.
   * @defectRisk Ensures traversal correctly recurses into nested function
   *             scopes and cleans up locals at every depth.
   */
  @Test(timeout = 4000)
  public void testNestedFunctionScopes() {
    String js = "function outer() { function inner() { var unused = 1; "
        + "return 2; } return inner(); }";
    Node script = parseAndProcess(js, true, false, false);
    assertFalse(containsNameNode(script, "unused"));
    assertTrue(containsNameNode(script, "inner"));
  }

  /**
   * @target removeUnreferencedVars (function declaration removal)
   * @scenario Unused global function declaration, removeGlobals=true.
   * @defectRisk Ensures a completely unreferenced function declaration is
   *             removed entirely from the AST.
   */
  @Test(timeout = 4000)
  public void testFunctionDeclarationRemoved_WhenUnusedAndRemoveGlobalsTrue() {
    String js = "function unusedFn() { return 1; } var used = 5; alert(used);";
    Node script = parseAndProcess(js, true, false, false);
    assertFalse(containsNameNode(script, "unusedFn"));
  }

  /**
   * @target isRemovableVar (global function declaration + removeGlobals=false)
   * @scenario Unused global function declaration, removeGlobals=false.
   * @defectRisk Ensures unreferenced global function declarations are kept
   *             when the user disables global removal.
   */
  @Test(timeout = 4000)
  public void testFunctionDeclarationKept_WhenRemoveGlobalsFalse() {
    String js = "function unusedFn() { return 1; }";
    Node script = parseAndProcess(js, false, false, false);
    assertTrue(containsNameNode(script, "unusedFn"));
  }

  /**
   * @target interpretAssigns (parent not VAR -> assignedToUnknownValue=true)
   * @scenario Function declaration with a prototype property assign
   *           (Foo.prototype.bar = ...). The var's declaring parent is a
   *           FUNCTION node, not a VAR node.
   * @defectRisk Ensures that property assigns on function declarations
   *             (which cannot be classified via a var initializer) are
   *             treated conservatively and keep the symbol referenced.
   */
  @Test(timeout = 4000)
  public void testPrototypePropertyAssignOnFunctionDecl_KeepsFunctionAlive() {
    String js = "function Foo() {} Foo.prototype.bar = function() { return 1; };";
    Node script = parseAndProcess(js, true, false, false);
    assertTrue(containsNameNode(script, "Foo"));
  }

  /**
   * @target interpretAssigns (assignedToUnknownValue via non-literal init + property assign)
   * @scenario var x = bar(); x.foo = 1; where bar() is not a literal value.
   * @defectRisk Ensures a variable initialized to a non-literal (unknown)
   *             value that later has a property assigned to it is kept
   *             alive (fix-point iteration in interpretAssigns).
   */
  @Test(timeout = 4000)
  public void testUnknownValueWithPropertyAssign_KeepsVarReferenced() {
    String js = "function bar() { return {}; } var x = bar(); x.foo = 1;";
    Node script = parseAndProcess(js, true, false, false);
    assertTrue(containsNameNode(script, "x"));
    assertTrue(containsNameNode(script, "bar"));
  }

  /**
   * @target traverseNode (Token.ASSIGN continuation for removable var)
   * @scenario var x = 1; x = 2; where x is never read.
   * @defectRisk Ensures a simple, side-effect-free re-assignment to an
   *             otherwise-unused variable does not itself count as a use,
   *             allowing the entire var (and assign) to be removed.
   */
  @Test(timeout = 4000)
  public void testSimpleReassignWithoutRead_RemovesVar() {
    String js = "var x = 1; x = 2;";
    Node script = parseAndProcess(js, true, false, false);
    assertFalse(containsNameNode(script, "x"));
  }

  /**
   * @target RemoveUnusedVars constructor / basic field wiring
   * @scenario Construct the pass with all boolean flags flipped to true
   *           and run against a trivial referenced-var program.
   * @defectRisk Sanity check that pass construction and processing does
   *             not throw regardless of flag combination, and referenced
   *             vars always survive.
   */
  @Test(timeout = 4000)
  public void testAllFlagsTrue_ReferencedVarSurvives() {
    String js = "var used = 1; alert(used);";
    Node script = parseAndProcess(js, true, true, false);
    assertTrue(containsNameNode(script, "used"));
  }

  /**
   * @target removeUnreferencedVars (single-name var, non-side-effecting init)
   * @scenario var a = 5; where a is completely unused and initializer is a
   *           pure literal.
   * @defectRisk Ensures the entire var statement (not just a partial
   *             transform) is dropped when there are no side effects.
   */
  @Test(timeout = 4000)
  public void testSingleUnusedVarLiteralInit_WholeStatementRemoved() {
    String js = "var a = 5;";
    Node script = parseAndProcess(js, true, false, false);
    assertFalse(containsNameNode(script, "a"));
  }
}