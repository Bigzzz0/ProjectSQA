package com.google.javascript.jscomp;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: RemoveUnusedVars
 * Defect Under Test: Closure-1 (Function parameters stripped when removeGlobals = false)
 *
 * ---------------------------------------------------------------------------------------------------------------
 * Branch / Condition                                       Target Partition                Expected Outcome
 * ---------------------------------------------------------------------------------------------------------------
 * Closure-1: removeGlobals == false                        Partition A (Closure-1 Defect)  Unused fn args PRESERVED
 * Closure-1: removeGlobals == true                         Partition A                     Unused trailing fn args REMOVED
 * NodeUtil.isGetOrSetKey(function.getParent()) == true     Partition A                     Object setter param PRESERVED
 * !hasFollowing && !referenced.contains(var)               Partition A                     Trailing unreferenced arg removed
 * hasFollowing && referenced.contains(nextVar)             Partition A                     Non-trailing unreferenced arg kept
 * "arguments".equals(name) && scope.isLocal()              Partition A                     All params marked referenced
 *
 * removeGlobals == true && unused global var               Partition B                     Global var removed completely
 * removeGlobals == false && unused global var              Partition B                     Global var preserved
 * unused local var (any removeGlobals setting)             Partition B                     Local var removed from scope
 * var a = sideEffect(); (value has side effect)            Partition B                     Var removed, exprResult kept
 * var a = 1, b = 2; (multi-var partial removal)            Partition B                     Unused var removed, used kept
 * Recursive function unreferenced externally               Partition B                     Recursive fn removed
 * for (var x in obj) (parent.isFor() childCount < 4)       Partition B                     Loop iterator var kept
 * assign.isPropertyAssign on literal obj (x.foo = 3)       Partition B                     Property assign eliminated
 * assign on unknown value (x = ext(); x.foo = 3)           Partition B                     Var kept alive to fixed point
 * Assign with secondary side effect in GETELEM             Partition B                     Replaced with comma expression
 * Assign result used (maybeAliased == true)                Partition B                     Replaced with RHS
 * goog.inherits call on unreferenced subclass              Partition B                     goog.inherits call removed
 * goog.addSingletonGetter on unreferenced class            Partition B                     Call statement removed
 *
 * modifyCallSites == true (trailing argument)              Partition C                     Caller arg & param removed
 * modifyCallSites == true (non-removable signature)        Partition C                     Caller arg replaced with 0
 *
 * preserveFunctionExpressionNames == false                 Partition D                     Function expr name cleared ("")
 * preserveFunctionExpressionNames == true                  Partition D                     Function expr name kept
 *
 * compiler.getLifeCycleStage().isNormalized() == false     Partition E                     Throws IllegalStateException
 * ---------------------------------------------------------------------------------------------------------------
 */

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class RemoveUnusedVarsGeminiTest {

  // =========================================================================
  // Partition A: Function Parameter Stripping & Closure-1 Defect Zone
  // =========================================================================

  /**
   * Primary Defect Exposure Test for Closure-1.
   * When removeGlobals is false (e.g. Simple Mode), unused function parameters
   * MUST NOT be removed because doing so breaks Function.prototype.length reflection.
   */
  @Test(timeout = 4000)
  public void testClosure1_DoNotRemoveFunctionArgsWhenNotRemovingGlobals() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("function foo(x, y) { return x; }");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    Node fnNode = root.getFirstChild();
    assertEquals(Token.FUNCTION, fnNode.getType());
    Node paramList = fnNode.getFirstChild().getNext();
    assertEquals(2, paramList.getChildCount());

    // removeGlobals = false: function parameters must be preserved
    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, false, false, false);
    pass.process(externs, root);

    assertEquals("Parameter count must remain 2 when removeGlobals is false",
        2, paramList.getChildCount());
    assertEquals("x", paramList.getFirstChild().getString());
    assertEquals("y", paramList.getFirstChild().getNext().getString());
  }

  @Test(timeout = 4000)
  public void testRemoveFunctionArgsWhenRemovingGlobals() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("function foo(x, y) { return x; } foo(1);");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    Node fnNode = root.getFirstChild();
    Node paramList = fnNode.getFirstChild().getNext();
    assertEquals(2, paramList.getChildCount());

    // removeGlobals = true: trailing unreferenced parameter 'y' should be stripped
    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    assertEquals(1, paramList.getChildCount());
    assertEquals("x", paramList.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testNonTrailingUnreferencedParamNotRemovedWithoutModifyCallSites() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    // 'x' is unreferenced, but 'y' is referenced and trailing
    Node root = compiler.parseTestCode("function foo(x, y) { return y; } foo(1, 2);");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    Node fnNode = root.getFirstChild();
    Node paramList = fnNode.getFirstChild().getNext();

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    // Without modifyCallSites, non-trailing argument 'x' cannot be removed off the end
    assertEquals(2, paramList.getChildCount());
    assertEquals("x", paramList.getFirstChild().getString());
    assertEquals("y", paramList.getFirstChild().getNext().getString());
  }

  @Test(timeout = 4000)
  public void testPreserveSetterParamInObjectLiteral() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("var obj = { set a(val) { this._a = 1; } }; use(obj);");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    Node varNode = root.getFirstChild();
    Node objLit = varNode.getFirstChild().getFirstChild();
    Node setterKey = objLit.getFirstChild();
    Node fnNode = setterKey.getFirstChild();
    Node paramList = fnNode.getFirstChild().getNext();

    // NodeUtil.isGetOrSetKey guard must prevent removing setter parameter 'val'
    assertEquals(1, paramList.getChildCount());
    assertEquals("val", paramList.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testArgumentsEscapedPreservesAllParams() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("function foo(a, b, c) { return arguments[0]; } foo(1);");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    Node fnNode = root.getFirstChild();
    Node paramList = fnNode.getFirstChild().getNext();
    assertEquals(3, paramList.getChildCount());

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    // 'arguments' usage marks all formal parameters referenced
    assertEquals(3, paramList.getChildCount());
    assertEquals("a", paramList.getFirstChild().getString());
    assertEquals("b", paramList.getFirstChild().getNext().getString());
    assertEquals("c", paramList.getLastChild().getString());
  }

  // =========================================================================
  // Partition B: Unused Global & Local Variable Elimination
  // =========================================================================

  @Test(timeout = 4000)
  public void testUnusedGlobalVarRemovedWhenRemoveGlobalsTrue() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("var unusedGlobal = 42;");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    assertFalse("Unused global variable declaration should be removed", root.hasChildren());
  }

  @Test(timeout = 4000)
  public void testUnusedGlobalVarKeptWhenRemoveGlobalsFalse() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("var unusedGlobal = 42;");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, false, false, false);
    pass.process(externs, root);

    assertTrue("Unused global variable declaration must be kept when removeGlobals=false",
        root.hasChildren());
    assertTrue(root.getFirstChild().isVar());
    assertEquals("unusedGlobal", root.getFirstChild().getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testUnusedLocalVarRemovedEvenWhenRemoveGlobalsFalse() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("function foo() { var localUnused = 1; return 2; }");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, false, false, false);
    pass.process(externs, root);

    Node fnNode = root.getFirstChild();
    Node body = fnNode.getLastChild();
    // 'var localUnused = 1' removed; only 'return 2' remains in body
    assertEquals(1, body.getChildCount());
    assertTrue(body.getFirstChild().isReturn());
  }

  @Test(timeout = 4000)
  public void testUnusedVarWithSideEffectsConvertsToExprResult() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("var a = sideEffect();");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    // VAR should be replaced with EXPR_RESULT containing sideEffect() call
    assertTrue(root.hasChildren());
    Node firstStmt = root.getFirstChild();
    assertTrue(firstStmt.isExprResult());
    assertTrue(firstStmt.getFirstChild().isCall());
    assertEquals("sideEffect", firstStmt.getFirstChild().getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testMultipleVarDeclarationPartialRemoval() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("var a = 1, b = 2; use(a);");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    Node varStmt = root.getFirstChild();
    assertTrue(varStmt.isVar());
    assertEquals("Only referenced 'a' should remain in the var declaration", 1, varStmt.getChildCount());
    assertEquals("a", varStmt.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testUnreferencedRecursiveFunctionRemoved() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("function rec() { rec(); }");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    assertFalse("Recursive function with no external references must be removed", root.hasChildren());
  }

  @Test(timeout = 4000)
  public void testForInLoopVarNotRemoved() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("for (var x in obj) { use(); }");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    assertTrue(root.hasChildren());
    Node forNode = root.getFirstChild();
    assertTrue(forNode.isFor());
    Node varNode = forNode.getFirstChild();
    assertTrue(varNode.isVar());
    assertEquals("x", varNode.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testPropertyAssignOnUnreferencedVarRemoved() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("var x = {}; x.foo = 3;");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    assertFalse("Assigns to unreferenced literal object must be removed", root.hasChildren());
  }

  @Test(timeout = 4000)
  public void testPropertyAssignOnPrototypePropertyRemoved() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("var x = {}; x.prototype.foo = 3;");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    assertFalse("Assigns to unreferenced prototype property must be removed", root.hasChildren());
  }

  @Test(timeout = 4000)
  public void testPropertyAssignOnUnknownValueKeepsVarAlive() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("var x = ext(); x.foo = 3;");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    // Variable assigned to unknown value with property assign is marked referenced
    assertTrue(root.hasChildren());
    assertTrue(root.getFirstChild().isVar());
    assertEquals("x", root.getFirstChild().getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testAssignWithGetElemSecondarySideEffects() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("var x = {}; x[sideEffect()] = 3;");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    // x[sideEffect()] = 3 should replace assign with comma expression (sideEffect(), 3)
    assertTrue(root.hasChildren());
    Node stmt = root.getFirstChild();
    assertTrue(stmt.isExprResult());
    Node expr = stmt.getFirstChild();
    assertTrue(expr.isComma());
    assertTrue(expr.getFirstChild().isCall());
    assertEquals("sideEffect", expr.getFirstChild().getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testAssignResultUsedReplacedWithRhs() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("var x; var y = (x = 10); use(y);");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    // x was unreferenced, (x = 10) was aliased, so x = 10 is replaced with 10 -> var y = 10;
    Node varStmt = root.getFirstChild();
    assertTrue(varStmt.isVar());
    assertEquals("y", varStmt.getFirstChild().getString());
    Node initVal = varStmt.getFirstChild().getFirstChild();
    assertTrue(initVal.isNumber());
    assertEquals(10.0, initVal.getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testInheritsCallWithUnreferencedSubclassRemoved() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new GoogleCodingConvention());
    compiler.initOptions(options);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    Node externs = IR.block();
    Node root = compiler.parseTestCode(
        "function Super() {} function Sub() {} goog.inherits(Sub, Super); Super();");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    // Sub and goog.inherits(Sub, Super) should be removed; Super remains
    assertEquals(2, root.getChildCount());
    assertTrue(root.getFirstChild().isFunction());
    assertEquals("Super", root.getFirstChild().getFirstChild().getString());
    assertTrue(root.getLastChild().isExprResult());
  }

  @Test(timeout = 4000)
  public void testSingletonGetterWithUnreferencedClassRemoved() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new GoogleCodingConvention());
    compiler.initOptions(options);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    Node externs = IR.block();
    Node root = compiler.parseTestCode("function Foo() {} goog.addSingletonGetter(Foo);");

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    assertFalse("Unreferenced class and its addSingletonGetter call must be removed", root.hasChildren());
  }

  // =========================================================================
  // Partition C: Call Site Optimization (modifyCallSites)
  // =========================================================================

  @Test(timeout = 4000)
  public void testModifyCallSitesRemovesUnusedArgsFromCallerAndCallee() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("function foo(a, b) { return a; } foo(1, 2);");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, true);
    pass.process(externs, root);

    // Function declaration parameter 'b' removed
    Node fnNode = root.getFirstChild();
    Node paramList = fnNode.getFirstChild().getNext();
    assertEquals(1, paramList.getChildCount());
    assertEquals("a", paramList.getFirstChild().getString());

    // Call site foo(1, 2) modified to foo(1)
    Node callExpr = root.getLastChild();
    Node callNode = callExpr.getFirstChild();
    assertEquals(2, callNode.getChildCount()); // NAME 'foo' and ARG '1'
    assertEquals("foo", callNode.getFirstChild().getString());
    assertEquals(1.0, callNode.getLastChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testModifyCallSitesReplacesWithZeroWhenSignatureCannotChange() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    // 'foo' is aliased, so signature cannot change, but argument '1' is unused by foo (which returns b)
    Node root = compiler.parseTestCode(
        "function foo(a, b) { return b; } var alias = foo; foo(1, 2); alias(3, 4);");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, true);
    pass.process(externs, root);

    // foo signature kept
    Node fnNode = root.getFirstChild();
    Node paramList = fnNode.getFirstChild().getNext();
    assertEquals(2, paramList.getChildCount());

    // In foo(1, 2), argument 1 is replaced with 0 -> foo(0, 2)
    Node callExpr = root.getChildAtIndex(2);
    Node callNode = callExpr.getFirstChild();
    Node firstArg = callNode.getChildAtIndex(1);
    assertTrue(firstArg.isNumber());
    assertEquals(0.0, firstArg.getDouble(), 0.0);
  }

  // =========================================================================
  // Partition D: Function Expression Names (preserveFunctionExpressionNames)
  // =========================================================================

  @Test(timeout = 4000)
  public void testPreserveFunctionExpressionNamesFalseClearsName() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("var f = function myName() {}; f();");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(externs, root);

    Node varStmt = root.getFirstChild();
    Node fnExpr = varStmt.getFirstChild().getFirstChild();
    assertTrue(fnExpr.isFunction());
    assertEquals("Function expression name should be cleared when preserve=false",
        "", fnExpr.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testPreserveFunctionExpressionNamesTrueKeepsName() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("var f = function myName() {}; f();");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, true, false);
    pass.process(externs, root);

    Node varStmt = root.getFirstChild();
    Node fnExpr = varStmt.getFirstChild().getFirstChild();
    assertTrue(fnExpr.isFunction());
    assertEquals("Function expression name must be preserved when preserve=true",
        "myName", fnExpr.getFirstChild().getString());
  }

  // =========================================================================
  // Partition E: Compiler Lifecycle Precondition Guard
  // =========================================================================

  @Test(timeout = 4000)
  public void testPreconditionNormalizedStageRequired() {
    Compiler compiler = new Compiler();
    Node externs = IR.block();
    Node root = compiler.parseTestCode("var x = 1;");
    // Explicitly do not set NORMALIZED lifecycle stage

    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    try {
      pass.process(externs, root);
      fail("process() must throw IllegalStateException if compiler stage is not normalized");
    } catch (IllegalStateException expected) {
      // Preconditions.checkState(compiler.getLifeCycleStage().isNormalized()) succeeded
      assertNotNull(expected);
    }
  }
}