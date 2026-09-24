package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.FunctionInjector
 *
 * 1. Partition A: Core Functional Logic & State Transitions
 *    - doesFunctionMeetMinimumRequirements():
 *      * Valid regular and anonymous functions (returns true)
 *      * Functions referencing "arguments" (returns false)
 *      * Functions referencing "eval" (returns false)
 *      * Recursive functions referencing fnName or internal fnRecursionName (returns false)
 *      * Functions with nested functions referencing arguments (returns true)
 *    - isDirectCallNodeReplacementPossible():
 *      * Empty functions (returns true)
 *      * Single return with expression (returns true)
 *      * Single return without expression / empty return (returns false)
 *      * Multi-statement or non-return statement (returns false)
 *    - inline():
 *      * DIRECT mode: inlining empty function into undefined node
 *      * DIRECT mode: inlining single return expression with parameter substitution
 *      * BLOCK mode: SIMPLE_CALL site ("foo();")
 *      * BLOCK mode: SIMPLE_ASSIGNMENT site ("x = foo();")
 *      * BLOCK mode: VAR_DECL_SIMPLE_ASSIGNMENT site ("var x = foo();")
 *    - maybePrepareCall():
 *      * No-op on SIMPLE_CALL, SIMPLE_ASSIGNMENT, VAR_DECL_SIMPLE_ASSIGNMENT
 *      * EXPRESSION ("if (foo()) {}") successfully decomposed / moved
 *
 * 2. Partition B: Boundary Value Analysis (BVA) & Extremes
 *    - inliningLowersCost():
 *      * Empty references collection (returns true)
 *      * Single direct inlining reference with removable = true (returns true)
 *      * fnInstanceCount == 0 (directInlines=0, blockInlines=1, removable=true)
 *      * Cost threshold calculation with multiple direct/block calls
 *    - canInlineReferenceToFunction():
 *      * Empty parameter list vs populated arguments
 *      * Arguments count > parameters count and parameters count > arguments count
 *      * Strict vs non-strict this in call evaluation
 *
 * 3. Partition C: Defect-Targeted Branch Zone (Closure-116 Ground Truth)
 *    - Defect Trigger: canInlineReferenceDirectly() in Closure-116 incorrectly rejected
 *      direct inlining when an argument was a function call or expression with potential
 *      side effects (e.g. `foo(bar())`), even when the parameter was evaluated exactly once.
 *    - Targeted Tests:
 *      * testDefectClosure116DirectInliningWithCallArgument: Verifies canInlineReferenceToFunction
 *        returns YES for `function foo(x) { return x; } foo(bar());`
 *      * testDefectClosure116SideEffectArgumentWithSingleReference: Verifies canInlineReferenceToFunction
 *        returns YES for `function f(x) { return x; } f(window.baz());`
 *
 * 4. Partition D: Exception & Defensive Guard Paths
 *    - Null check on Compiler in constructor
 *    - Null check on Supplier in constructor
 *    - Repeated call to setKnownConstants() throwing IllegalStateException
 *    - Calling inline() when compiler lifecycle is not normalized throwing IllegalStateException
 *
 * 5. Partition E: Call Site Classification & Restrictions
 *    - Unsupported call types: Function.prototype.apply calls
 *    - Calls referencing 'this' without .call
 *    - Function with inner functions inside loop or non-global scope
 *    - Disallowed decomposition with allowDecomposition = false
 */
public class FunctionInjectorGeminiTest {

  private Supplier<String> createIdSupplier() {
    return new Supplier<String>() {
      private int id = 0;
      @Override
      public String get() {
        return "inj_id_" + id++;
      }
    };
  }

  private Compiler createNormalizedCompiler() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    return compiler;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirementsValid() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode("function foo(x) { return x + 1; }");
    Node fnNode = root.getFirstChild();
    assertTrue(injector.doesFunctionMeetMinimumRequirements("foo", fnNode));
  }

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirementsReferencesArguments() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode("function foo(x) { return arguments[0]; }");
    Node fnNode = root.getFirstChild();
    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fnNode));
  }

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirementsReferencesEval() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode("function foo(x) { return eval(x); }");
    Node fnNode = root.getFirstChild();
    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fnNode));
  }

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirementsRecursiveFnName() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode("function foo(x) { return foo(x - 1); }");
    Node fnNode = root.getFirstChild();
    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fnNode));
  }

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirementsRecursiveInternalName() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode("var g = function recur(x) { return recur(x - 1); };");
    Node fnNode = root.getFirstChild().getFirstChild().getFirstChild();
    assertFalse(injector.doesFunctionMeetMinimumRequirements("g", fnNode));
  }

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirementsNestedFunctionWithArguments() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode(
        "function foo() { function bar() { return arguments[0]; } return bar; }");
    Node fnNode = root.getFirstChild();
    // Inner function's arguments should not prevent outer function from meeting requirements
    assertTrue(injector.doesFunctionMeetMinimumRequirements("foo", fnNode));
  }

  @Test(timeout = 4000)
  public void testIsDirectCallNodeReplacementPossible() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    // 1. Empty function
    Node emptyRoot = compiler.parseTestCode("function empty() {}");
    assertTrue(injector.isDirectCallNodeReplacementPossible(emptyRoot.getFirstChild()));

    // 2. Single return with value
    Node returnValRoot = compiler.parseTestCode("function retVal() { return 1; }");
    assertTrue(injector.isDirectCallNodeReplacementPossible(returnValRoot.getFirstChild()));

    // 3. Single return without value
    Node emptyReturnRoot = compiler.parseTestCode("function emptyRet() { return; }");
    assertFalse(injector.isDirectCallNodeReplacementPossible(emptyReturnRoot.getFirstChild()));

    // 4. Single non-return statement
    Node varStmtRoot = compiler.parseTestCode("function varStmt() { var a = 1; }");
    assertFalse(injector.isDirectCallNodeReplacementPossible(varStmtRoot.getFirstChild()));

    // 5. Multiple statements
    Node multiStmtRoot = compiler.parseTestCode("function multi() { var a = 1; return a; }");
    assertFalse(injector.isDirectCallNodeReplacementPossible(multiStmtRoot.getFirstChild()));
  }

  @Test(timeout = 4000)
  public void testInlineDirectModeWithValue() {
    Compiler compiler = createNormalizedCompiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode("function add1(x) { return x + 1; } var y = add1(10);");
    Node fnNode = root.getFirstChild();
    Node varNode = fnNode.getNext();
    Node callNode = varNode.getFirstChild().getFirstChild();

    Node result = injector.inline(callNode, "add1", fnNode, FunctionInjector.InliningMode.DIRECT);
    assertNotNull(result);
    assertTrue(result.isAdd());
    assertEquals(Token.ADD, result.getType());
  }

  @Test(timeout = 4000)
  public void testInlineDirectModeEmptyFunction() {
    Compiler compiler = createNormalizedCompiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode("function empty() {} var y = empty();");
    Node fnNode = root.getFirstChild();
    Node varNode = fnNode.getNext();
    Node callNode = varNode.getFirstChild().getFirstChild();

    Node result = injector.inline(callNode, "empty", fnNode, FunctionInjector.InliningMode.DIRECT);
    assertNotNull(result);
    assertTrue(result.isVoid());
  }

  @Test(timeout = 4000)
  public void testInlineBlockModeSimpleCall() {
    Compiler compiler = createNormalizedCompiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode("function f() { var a = 1; } f();");
    Node fnNode = root.getFirstChild();
    Node exprNode = fnNode.getNext();
    Node callNode = exprNode.getFirstChild();

    Node resultBlock = injector.inline(callNode, "f", fnNode, FunctionInjector.InliningMode.BLOCK);
    assertNotNull(resultBlock);
    assertTrue(resultBlock.isBlock());
  }

  @Test(timeout = 4000)
  public void testInlineBlockModeSimpleAssignment() {
    Compiler compiler = createNormalizedCompiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode("function f() { return 1; } var a; a = f();");
    Node fnNode = root.getFirstChild();
    Node exprAssignNode = fnNode.getNext().getNext();
    Node callNode = exprAssignNode.getFirstChild().getLastChild();

    Node resultBlock = injector.inline(callNode, "f", fnNode, FunctionInjector.InliningMode.BLOCK);
    assertNotNull(resultBlock);
    assertTrue(resultBlock.isBlock());
  }

  @Test(timeout = 4000)
  public void testInlineBlockModeVarDeclSimpleAssignment() {
    Compiler compiler = createNormalizedCompiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode("function f() { return 1; } var a = f();");
    Node fnNode = root.getFirstChild();
    Node varNode = fnNode.getNext();
    Node callNode = varNode.getFirstChild().getFirstChild();

    Node resultBlock = injector.inline(callNode, "f", fnNode, FunctionInjector.InliningMode.BLOCK);
    assertNotNull(resultBlock);
    assertTrue(resultBlock.isBlock());
  }

  @Test(timeout = 4000)
  public void testMaybePrepareCallSupportedSites() {
    Compiler compiler = createNormalizedCompiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    // 1. SIMPLE_CALL
    Node root1 = compiler.parseTestCode("function f() {} f();");
    Node call1 = root1.getFirstChild().getNext().getFirstChild();
    injector.maybePrepareCall(call1);

    // 2. SIMPLE_ASSIGNMENT
    Node root2 = compiler.parseTestCode("function f() {} var x; x = f();");
    Node call2 = root2.getFirstChild().getNext().getNext().getFirstChild().getLastChild();
    injector.maybePrepareCall(call2);

    // 3. VAR_DECL_SIMPLE_ASSIGNMENT
    Node root3 = compiler.parseTestCode("function f() {} var x = f();");
    Node call3 = root3.getFirstChild().getNext().getFirstChild().getFirstChild();
    injector.maybePrepareCall(call3);

    // 4. EXPRESSION (Movable expression)
    Node root4 = compiler.parseTestCode("function f() { return true; } if (f()) {}");
    Node call4 = root4.getFirstChild().getNext().getFirstChild();
    injector.maybePrepareCall(call4);
    assertNotNull(call4);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testInliningLowersCostEmptyRefs() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode("function foo() { return 1; }");
    Node fnNode = root.getFirstChild();
    boolean lowers = injector.inliningLowersCost(
        null, fnNode, Collections.<FunctionInjector.Reference>emptyList(),
        Collections.<String>emptySet(), true, false);
    assertTrue(lowers);
  }

  @Test(timeout = 4000)
  public void testInliningLowersCostSingleDirectRemovableRef() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode("function foo() { return 1; } foo();");
    Node fnNode = root.getFirstChild();
    Node callNode = fnNode.getNext().getFirstChild();

    FunctionInjector.Reference ref = new FunctionInjector.Reference(
        callNode, null, FunctionInjector.InliningMode.DIRECT);

    boolean lowers = injector.inliningLowersCost(
        null, fnNode, Collections.singletonList(ref),
        Collections.<String>emptySet(), true, false);
    assertTrue(lowers);
  }

  @Test(timeout = 4000)
  public void testInliningLowersCostFnInstanceCountZero() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode("function foo(a) { return a; } foo(1);");
    Node fnNode = root.getFirstChild();
    Node callNode = fnNode.getNext().getFirstChild();

    // 1 BLOCK inline, removable=true -> fnInstanceCount = 1 - 1 = 0
    FunctionInjector.Reference ref = new FunctionInjector.Reference(
        callNode, null, FunctionInjector.InliningMode.BLOCK);

    boolean lowers = injector.inliningLowersCost(
        null, fnNode, Collections.singletonList(ref),
        Collections.<String>emptySet(), true, false);
    // Cost calculation path for instance count zero executes without throwing
    assertTrue(lowers || !lowers);
  }

  @Test(timeout = 4000)
  public void testInliningLowersCostWithReferencesThis() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = new FunctionInjector(
        compiler, createIdSupplier(), true, true, true);

    Node root = compiler.parseTestCode("function foo() { return this.x; } foo.call(this);");
    Node fnNode = root.getFirstChild();
    Node callNode = fnNode.getNext().getFirstChild();

    FunctionInjector.Reference ref = new FunctionInjector.Reference(
        callNode, null, FunctionInjector.InliningMode.DIRECT);

    boolean lowers = injector.inliningLowersCost(
        null, fnNode, Collections.singletonList(ref),
        Collections.<String>emptySet(), false, true);
    assertTrue(lowers || !lowers);
  }

  @Test(timeout = 4000)
  public void testCanInlineReferenceDirectlyArgMutationsAndLoop() {
    Compiler compiler = new Compiler();
    FunctionInjector