/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.FunctionInjector
 *
 * 1. Defects4J Defect Focus (Closure-115 / Issue 1101):
 *    - canInlineReferenceDirectly must reject inlining when a call argument has side effects
 *      (e.g., `foo(a++)`, `foo(x = 1)`), even if the parameter in the target function is
 *      referenced at most once.
 *    - In the defective version, missing side-effect verification in canInlineReferenceDirectly
 *      causes `foo(a++)` to evaluate to YES instead of NO.
 *
 * 2. Decision Branches Covered:
 *    - doesFunctionMeetMinimumRequirements:
 *        * Convention check: isInlinableFunction
 *        * References "arguments" directly (MATCH_NOT_FUNCTION)
 *        * References "eval"
 *        * Self-recursion by assigned name (fnName) or function name (fnRecursionName)
 *        * Anonymous functions vs named functions
 *    - isSupportedCallType & canInlineReferenceToFunction:
 *        * Call type: regular call (NAME), .call (GETPROP), .apply (GETPROP)
 *        * assumeStrictThis: true vs false
 *        * containsFunctions: !assumeMinimumCapture && !inGlobalScope vs within loop
 *        * referencesThis: regular call vs function object call (.call)
 *        * InliningMode: DIRECT vs BLOCK
 *    - isDirectCallNodeReplacementPossible:
 *        * Empty function body (hasChildren() == false)
 *        * Single return statement with return value
 *        * Single return statement without return value
 *        * Multiple statements / non-return statements
 *    - canInlineReferenceDirectly:
 *        * Non-replaceable direct calls
 *        * .call missing 'this' argument
 *        * Mutable argument with param count > 1
 *        * Call argument with side effects (Issue 1101 defect zone)
 *    - canInlineReferenceAsStatementBlock & callMeetsBlockInliningRequirements:
 *        * Unsupported call sites
 *        * allowDecomposition: true vs false on movable/decomposable expressions
 *        * forbidTemps in non-global scope (eval, inner functions, !assumeMinimumCapture)
 *        * Aliasing requirements for parameters
 *    - inline:
 *        * NORMALIZED life cycle stage precondition check
 *        * DIRECT inlining with return expr and empty body (undefined node)
 *        * BLOCK inlining: SIMPLE_CALL, SIMPLE_ASSIGNMENT, VAR_DECL_SIMPLE_ASSIGNMENT
 *        * BLOCK inlining with non-decomposed EXPRESSION throwing IllegalStateException
 *    - inliningLowersCost & doesLowerCost:
 *        * Empty references
 *        * Single reference direct inlining with removable == true
 *        * referencesThis cost adjustment
 *        * Multi-module dependency checks
 *        * fnInstanceCount == 0 boundary and threshold calculations
 *    - setKnownConstants:
 *        * First invocation (succeeds)
 *        * Second invocation (Precondition failure)
 *    - Defensive guard paths: null checks in constructor
 */

package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.Node;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class FunctionInjectorGeminiTest {

  // =========================================================================
  // Test Helpers
  // =========================================================================

  private Supplier<String> createSafeNameIdSupplier() {
    return new Supplier<String>() {
      private int counter = 0;
      @Override
      public String get() {
        return "safeId_" + (counter++);
      }
    };
  }

  private FunctionInjector createInjector(
      Compiler compiler,
      boolean allowDecomposition,
      boolean assumeStrictThis,
      boolean assumeMinimumCapture) {
    return new FunctionInjector(
        compiler,
        createSafeNameIdSupplier(),
        allowDecomposition,
        assumeStrictThis,
        assumeMinimumCapture);
  }

  private Node findFunctionNode(Node root, final String name) {
    final Node[] found = new Node[1];
    NodeTraversal.traverse(new Compiler(), root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isFunction()) {
          if (name == null || name.equals(NodeUtil.getFunctionName(n))) {
            if (found[0] == null) {
              found[0] = n;
            }
          }
        }
      }
    });
    return found[0];
  }

  private Node findCallNode(Node root) {
    final Node[] found = new Node[1];
    NodeTraversal.traverse(new Compiler(), root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall() && found[0] == null) {
          found[0] = n;
        }
      }
    });
    return found[0];
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure-115 / Issue 1101)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIssue1101a_directInlineArgumentWithSideEffectsRejected() {
    // Tests that calls with argument side-effects (e.g. a++) are not directly inlined,
    // exposing the defect where side effects were not checked if param count <= 1.
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(a){return Boolean(a);} foo(a++);");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        null,
        callNode,
        fnNode,
        Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT,
        false,
        false);

    assertEquals(FunctionInjector.CanInlineResult.NO, result);
  }

  @Test(timeout = 4000)
  public void testIssue1101b_directInlineAssignmentArgumentRejected() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(a){return Boolean(a);} foo(x = 1);");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        null,
        callNode,
        fnNode,
        Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT,
        false,
        false);

    assertEquals(FunctionInjector.CanInlineResult.NO, result);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirements_validCleanFunction() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(x, y) { return x + y; }");
    Node fnNode = findFunctionNode(root, "foo");

    FunctionInjector injector = createInjector(compiler, true, false, false);
    assertTrue(injector.doesFunctionMeetMinimumRequirements("foo", fnNode));
  }

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirements_referencesArguments() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(x) { return arguments[0]; }");
    Node fnNode = findFunctionNode(root, "foo");

    FunctionInjector injector = createInjector(compiler, true, false, false);
    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fnNode));
  }

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirements_referencesEval() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(x) { eval('x'); return x; }");
    Node fnNode = findFunctionNode(root, "foo");

    FunctionInjector injector = createInjector(compiler, true, false, false);
    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fnNode));
  }

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirements_recursiveNamedFunction() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(x) { return foo(x - 1); }");
    Node fnNode = findFunctionNode(root, "foo");

    FunctionInjector injector = createInjector(compiler, true, false, false);
    assertFalse(injector.doesFunctionMeetMinimumRequirements("foo", fnNode));
  }

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirements_anonymousAssignedRecursion() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var bar = function rec(x) { return rec(x); };");
    Node fnNode = findFunctionNode(root, "rec");

    FunctionInjector injector = createInjector(compiler, true, false, false);
    assertFalse(injector.doesFunctionMeetMinimumRequirements("bar", fnNode));
  }

  @Test(timeout = 4000)
  public void testIsDirectCallNodeReplacementPossible_emptyFunction() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo() {}");
    Node fnNode = findFunctionNode(root, "foo");

    FunctionInjector injector = createInjector(compiler, true, false, false);
    assertTrue(injector.isDirectCallNodeReplacementPossible(fnNode));
  }

  @Test(timeout = 4000)
  public void testIsDirectCallNodeReplacementPossible_singleReturnExpr() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(x) { return x + 1; }");
    Node fnNode = findFunctionNode(root, "foo");

    FunctionInjector injector = createInjector(compiler, true, false, false);
    assertTrue(injector.isDirectCallNodeReplacementPossible(fnNode));
  }

  @Test(timeout = 4000)
  public void testIsDirectCallNodeReplacementPossible_emptyReturn() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo() { return; }");
    Node fnNode = findFunctionNode(root, "foo");

    FunctionInjector injector = createInjector(compiler, true, false, false);
    assertFalse(injector.isDirectCallNodeReplacementPossible(fnNode));
  }

  @Test(timeout = 4000)
  public void testIsDirectCallNodeReplacementPossible_multiStatement() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(x) { var y = 2; return x + y; }");
    Node fnNode = findFunctionNode(root, "foo");

    FunctionInjector injector = createInjector(compiler, true, false, false);
    assertFalse(injector.isDirectCallNodeReplacementPossible(fnNode));
  }

  @Test(timeout = 4000)
  public void testCanInlineReferenceDirectly_pureArgumentsAllowed() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(a, b){return a + b;} foo(1, 2);");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        null,
        callNode,
        fnNode,
        Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT,
        false,
        false);

    assertEquals(FunctionInjector.CanInlineResult.YES, result);
  }

  @Test(timeout = 4000)
  public void testCanInlineReferenceDirectly_mutableArgumentMultipleReferencesRejected() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(a){return a + a;} foo([1]);");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        null,
        callNode,
        fnNode,
        Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT,
        false,
        false);

    assertEquals(FunctionInjector.CanInlineResult.NO, result);
  }

  @Test(timeout = 4000)
  public void testCanInlineReferenceDirectly_mutableArgumentSingleReferenceAllowed() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(a){return a;} foo([1]);");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        null,
        callNode,
        fnNode,
        Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT,
        false,
        false);

    assertEquals(FunctionInjector.CanInlineResult.YES, result);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Supported Call Types
  // =========================================================================

  @Test(timeout = 4000)
  public void testSupportedCallType_functionObjectApplyUnsupported() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(){} foo.apply(this, [1]);");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        null,
        callNode,
        fnNode,
        Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT,
        false,
        false);

    assertEquals(FunctionInjector.CanInlineResult.NO, result);
  }

  @Test(timeout = 4000)
  public void testSupportedCallType_functionObjectCallWithThis() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(){return 1;} foo.call(this);");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        null,
        callNode,
        fnNode,
        Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT,
        false,
        false);

    assertEquals(FunctionInjector.CanInlineResult.YES, result);
  }

  @Test(timeout = 4000)
  public void testSupportedCallType_functionObjectCallWithoutThis_nonStrict() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(){return 1;} foo.call(obj);");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        null,
        callNode,
        fnNode,
        Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT,
        false,
        false);

    assertEquals(FunctionInjector.CanInlineResult.NO, result);
  }

  @Test(timeout = 4000)
  public void testSupportedCallType_functionObjectCallWithoutThis_strictThis() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(){return 1;} foo.call(obj);");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    // When assumeStrictThis = true, isSupportedCallType allows it
    FunctionInjector injector = createInjector(compiler, true, true, false);
    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        null,
        callNode,
        fnNode,
        Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT,
        false,
        false);

    // Note: DIRECT inlining check still requires 'this' for cArg if not supported
    assertEquals(FunctionInjector.CanInlineResult.NO, result);
  }

  @Test(timeout = 4000)
  public void testReferencesThisWithoutFunctionObjectCallRejected() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(){return this.x;} foo();");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        null,
        callNode,
        fnNode,
        Collections.<String>emptySet(),
        FunctionInjector.InliningMode.DIRECT,
        true, // referencesThis
        false);

    assertEquals(FunctionInjector.CanInlineResult.NO, result);
  }

  @Test(timeout = 4000)
  public void testContainsFunctionsWithinLoopRejected() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(){ function bar(){} } while(1) { foo(); }");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, true);
    FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
        null,
        callNode,
        fnNode,
        Collections.<String>emptySet(),
        FunctionInjector.InliningMode.BLOCK,
        false,
        true // containsFunctions
    );

    assertEquals(FunctionInjector.CanInlineResult.NO, result);
  }

  // =========================================================================
  // Partition D: Block Inlining & Call Site Classification
  // =========================================================================

  @Test(timeout = 4000)
  public void testCanInlineReferenceAsStatementBlock_simpleCall() {
    final Compiler compiler = new Compiler();
    final Node root = compiler.parseTestCode("function foo(){var z = 1;} foo();");
    final Node fnNode = findFunctionNode(root, "foo");
    final Node callNode = findCallNode(root);

    final FunctionInjector injector = createInjector(compiler, true, false, false);

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n == callNode) {
          FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
              t,
              callNode,
              fnNode,
              Collections.<String>emptySet(),
              FunctionInjector.InliningMode.BLOCK,
              false,
              false);
          assertEquals(FunctionInjector.CanInlineResult.YES, result);
        }
      }
    });
  }

  @Test(timeout = 4000)
  public void testCanInlineReferenceAsStatementBlock_simpleAssignment() {
    final Compiler compiler = new Compiler();
    final Node root = compiler.parseTestCode("function foo(){return 1;} var x; x = foo();");
    final Node fnNode = findFunctionNode(root, "foo");
    final Node callNode = findCallNode(root);

    final FunctionInjector injector = createInjector(compiler, true, false, false);

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n == callNode) {
          FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
              t,
              callNode,
              fnNode,
              Collections.<String>emptySet(),
              FunctionInjector.InliningMode.BLOCK,
              false,
              false);
          assertEquals(FunctionInjector.CanInlineResult.YES, result);
        }
      }
    });
  }

  @Test(timeout = 4000)
  public void testCanInlineReferenceAsStatementBlock_varDeclSimpleAssignment() {
    final Compiler compiler = new Compiler();
    final Node root = compiler.parseTestCode("function foo(){return 1;} var x = foo();");
    final Node fnNode = findFunctionNode(root, "foo");
    final Node callNode = findCallNode(root);

    final FunctionInjector injector = createInjector(compiler, true, false, false);

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n == callNode) {
          FunctionInjector.CanInlineResult result = injector.canInlineReferenceToFunction(
              t,
              callNode,
              fnNode,
              Collections.<String>emptySet(),
              FunctionInjector.InliningMode.BLOCK,
              false,
              false);
          assertEquals(FunctionInjector.CanInlineResult.YES, result);
        }
      }
    });
  }

  @Test(timeout = 4000)
  public void testCanInlineReferenceAsStatementBlock_movableExpression() {
    final Compiler compiler = new Compiler();
    final Node root = compiler.parseTestCode("function foo(){return 1;} var x = 1 + foo();");
    final Node fnNode = findFunctionNode(root, "foo");
    final Node callNode = findCallNode(root);

    final FunctionInjector injectorDecomposing = createInjector(compiler, true, false, false);
    final FunctionInjector injectorNonDecomposing = createInjector(compiler, false, false, false);

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n == callNode) {
          FunctionInjector.CanInlineResult res1 = injectorDecomposing.canInlineReferenceToFunction(
              t, callNode, fnNode, Collections.<String>emptySet(),
              FunctionInjector.InliningMode.BLOCK, false, false);
          assertEquals(FunctionInjector.CanInlineResult.AFTER_PREPARATION, res1);

          FunctionInjector.CanInlineResult res2 = injectorNonDecomposing.canInlineReferenceToFunction(
              t, callNode, fnNode, Collections.<String>emptySet(),
              FunctionInjector.InliningMode.BLOCK, false, false);
          assertEquals(FunctionInjector.CanInlineResult.NO, res2);
        }
      }
    });
  }

  @Test(timeout = 4000)
  public void testCanInlineReferenceAsStatementBlock_forbidTempsInLocalScope() {
    final Compiler compiler = new Compiler();
    // outer function contains inner function, and foo contains a VAR declaration
    final Node root = compiler.parseTestCode(
        "function outer() { function inner(){} foo(); } function foo() { var local = 1; }");
    final Node fnNode = findFunctionNode(root, "foo");
    final Node callNode = findCallNode(root);

    final FunctionInjector injector = createInjector(compiler, true, false, false);

    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n == callNode) {
          FunctionInjector.CanInlineResult res = injector.canInlineReferenceToFunction(
              t, callNode, fnNode, Collections.<String>emptySet(),
              FunctionInjector.InliningMode.BLOCK, false, false);
          assertEquals(FunctionInjector.CanInlineResult.NO, res);
        }
      }
    });
  }

  // =========================================================================
  // Partition E: Inlining Mutation & AST Transformation
  // =========================================================================

  @Test(timeout = 4000)
  public void testInlineDirect_returnValue() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = compiler.parseTestCode("function foo(a){return a + 1;} var x = foo(2);");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    Node inlined = injector.inline(callNode, "foo", fnNode, FunctionInjector.InliningMode.DIRECT);

    assertNotNull(inlined);
    assertTrue(inlined.isAdd());
    assertEquals(2, inlined.getFirstChild().getDouble(), 0.0);
    assertEquals(1, inlined.getLastChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testInlineDirect_emptyFunctionBody() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = compiler.parseTestCode("function foo(){} var x = foo();");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    Node inlined = injector.inline(callNode, "foo", fnNode, FunctionInjector.InliningMode.DIRECT);

    assertNotNull(inlined);
    assertTrue(NodeUtil.isUndefined(inlined));
  }

  @Test(timeout = 4000)
  public void testInlineBlock_simpleCall() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = compiler.parseTestCode("function foo(){var y = 1;} foo();");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    Node inlinedBlock = injector.inline(callNode, "foo", fnNode, FunctionInjector.InliningMode.BLOCK);

    assertNotNull(inlinedBlock);
    assertTrue(inlinedBlock.isBlock());
  }

  @Test(timeout = 4000)
  public void testInlineBlock_simpleAssignment() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = compiler.parseTestCode("function foo(){return 1;} var x; x = foo();");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    Node inlinedBlock = injector.inline(callNode, "foo", fnNode, FunctionInjector.InliningMode.BLOCK);

    assertNotNull(inlinedBlock);
    assertTrue(inlinedBlock.isBlock());
  }

  @Test(timeout = 4000)
  public void testInlineBlock_varDeclSimpleAssignment() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = compiler.parseTestCode("function foo(){return 1;} var x = foo();");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    Node inlinedBlock = injector.inline(callNode, "foo", fnNode, FunctionInjector.InliningMode.BLOCK);

    assertNotNull(inlinedBlock);
    assertTrue(inlinedBlock.isBlock());
  }

  @Test(timeout = 4000)
  public void testMaybePrepareCall_movableExpression() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = compiler.parseTestCode("function foo(){return 1;} var x = 1 + foo();");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    injector.maybePrepareCall(callNode);

    // After maybePrepareCall, the call is moved out of the binary expression
    assertNotNull(callNode.getParent());
    assertTrue(callNode.getParent().isName() || callNode.getParent().isAssign() || callNode.getParent().isVar());
  }

  // =========================================================================
  // Partition F: Cost Estimation & inliningLowersCost
  // =========================================================================

  @Test(timeout = 4000)
  public void testInliningLowersCost_emptyReferencesReturnsTrue() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(){return 1;}");
    Node fnNode = findFunctionNode(root, "foo");

    FunctionInjector injector = createInjector(compiler, true, false, false);
    boolean lowers = injector.inliningLowersCost(
        null,
        fnNode,
        Collections.<FunctionInjector.Reference>emptyList(),
        Collections.<String>emptySet(),
        true,
        false);

    assertTrue(lowers);
  }

  @Test(timeout = 4000)
  public void testInliningLowersCost_singleDirectReferenceRemovableReturnsTrue() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("function foo(){return 1;} foo();");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    FunctionInjector.Reference ref = new FunctionInjector.Reference(
        callNode, null, FunctionInjector.InliningMode.DIRECT);

    boolean lowers = injector.inliningLowersCost(
        null,
        fnNode,
        Collections.singletonList(ref),
        Collections.<String>emptySet(),
        true,
        false);

    assertTrue(lowers);
  }

  @Test(timeout = 4000)
  public void testInliningLowersCost_blockInliningCostDecision() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(
        "function foo(a, b, c){ var x = a; var y = b; return x + y + c; } foo(1, 2, 3); foo(4, 5, 6);");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    FunctionInjector.Reference ref1 = new FunctionInjector.Reference(
        callNode, null, FunctionInjector.InliningMode.BLOCK);
    FunctionInjector.Reference ref2 = new FunctionInjector.Reference(
        callNode, null, FunctionInjector.InliningMode.BLOCK);

    Set<String> aliases = new HashSet<String>();
    aliases.add("a");
    aliases.add("b");

    boolean lowers = injector.inliningLowersCost(
        null,
        fnNode,
        Sets.newHashSet(ref1, ref2),
        aliases,
        false,
        true // referencesThis
    );

    // Inlining a large function twice with block mode and aliases generally does not lower cost
    assertFalse(lowers);
  }

  // =========================================================================
  // Partition G: Exception Guards & Invariants
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructor_nullCompilerThrowsNpe() {
    new FunctionInjector(null, createSafeNameIdSupplier(), true, false, false);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructor_nullSupplierThrowsNpe() {
    new FunctionInjector(new Compiler(), null, true, false, false);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testSetKnownConstants_cannotSetTwice() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = createInjector(compiler, true, false, false);

    injector.setKnownConstants(Sets.newHashSet("CONST_A"));
    // Second invocation must throw IllegalStateException
    injector.setKnownConstants(Sets.newHashSet("CONST_B"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testInline_notNormalizedThrowsIllegalStateException() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.RAW);
    Node root = compiler.parseTestCode("function foo(){return 1;} foo();");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    injector.inline(callNode, "foo", fnNode, FunctionInjector.InliningMode.DIRECT);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testInlineBlock_movableExpressionWithoutPreparationThrows() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    Node root = compiler.parseTestCode("function foo(){return 1;} var x = 1 + foo();");
    Node fnNode = findFunctionNode(root, "foo");
    Node callNode = findCallNode(root);

    FunctionInjector injector = createInjector(compiler, true, false, false);
    // Directly attempting to inline movable expression without prepareCall throws IllegalStateException
    injector.inline(callNode, "foo", fnNode, FunctionInjector.InliningMode.BLOCK);
  }

  @Test(timeout = 4000)
  public void testReferenceDataHolder() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("foo();");
    Node callNode = findCallNode(root);

    FunctionInjector.Reference ref = new FunctionInjector.Reference(
        callNode, null, FunctionInjector.InliningMode.DIRECT);

    assertSame(callNode, ref.callNode);
    assertNull(ref.module);
    assertEquals(FunctionInjector.InliningMode.DIRECT, ref.mode);
  }
}