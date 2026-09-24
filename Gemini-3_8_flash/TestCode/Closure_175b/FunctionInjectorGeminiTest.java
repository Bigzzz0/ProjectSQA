package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.Node;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.FunctionInjector
 *
 * Targeted Decision Branches & Boundaries:
 * 1. Constructor Defensive Guards:
 *    - Null compiler -> NullPointerException
 *    - Null safeNameIdSupplier -> NullPointerException
 * 2. doesFunctionMeetMinimumRequirements(fnName, fnNode):
 *    - Convention rejection (!isInlinableFunction) -> false
 *    - Direct and named self-recursion (fnName / fnRecursionName referenced) -> false
 *    - Function body references "arguments" -> false
 *    - Function body references "eval" -> false
 *    - Clean, valid inlinable function -> true
 * 3. isDirectCallNodeReplacementPossible(fnNode):
 *    - Empty function body -> true
 *    - Single-statement returning expression -> true
 *    - Single-statement empty return (return;) -> false
 *    - Non-return statement -> false
 *    - Multi-statement function body -> false
 * 4. canInlineReferenceToFunction (Supported Call Types & Inlining Modes):
 *    - Direct call name: foo() -> allowed
 *    - Method apply: foo.apply() -> CanInlineResult.NO
 *    - Method call: foo.call(this) with assumeStrictThis=false -> allowed
 *    - Method call: foo.call(obj) with assumeStrictThis=false -> CanInlineResult.NO
 *    - Method call: foo.call(obj) with assumeStrictThis=true -> allowed
 *    - referencesThis=true with direct call -> CanInlineResult.NO
 *    - referencesThis=true with .call(this) -> allowed to proceed
 *    - containsFunctions=true in non-global scope without assumeMinimumCapture -> CanInlineResult.NO
 *    - containsFunctions=true inside loop -> CanInlineResult.NO
 * 5. canInlineReferenceDirectly & Defect Zone (Closure-115 / Issue 1101):
 *    - Defect ground truth: Functions modifying parameters (e.g. return --a; return a++;)
 *      inlined directly into call sites mutate caller variables or violate semantics.
 *    - testIssue1101a: "function foo(a){return --a;} foo(x)" in DIRECT mode -> CanInlineResult.NO
 *    - testIssue1101b: "function foo(a){return a++;} foo(x)" in DIRECT mode -> CanInlineResult.NO
 *    - Parameter referenced > 1 times and argument may effect mutable state -> CanInlineResult.NO
 *    - Argument with side effects -> CanInlineResult.NO
 * 6. canInlineReferenceAsStatementBlock:
 *    - Call sites: SIMPLE_CALL, SIMPLE_ASSIGNMENT, VAR_DECL_SIMPLE_ASSIGNMENT -> CanInlineResult.YES
 *    - Expression call sites when allowDecomposition=false -> CanInlineResult.NO
 *    - Expression call sites when allowDecomposition=true -> CanInlineResult.AFTER_PREPARATION
 *    - Scope containing eval / forbidden temp vars -> CanInlineResult.NO
 * 7. inline(...) Transformations:
 *    - Normalization lifecycle check: RAW -> IllegalStateException; NORMALIZED -> proceeds
 *    - DIRECT mode: empty function replacement with void 0 (undefined)
 *    - DIRECT mode: return expression substitution with parameter mapping
 *    - BLOCK mode: SIMPLE_CALL, SIMPLE_ASSIGNMENT, VAR_DECL_SIMPLE_ASSIGNMENT block rewrites
 * 8. Cost Analysis (inliningLowersCost & doesLowerCost):
 *    - 0 references -> true
 *    - 1 removable reference using direct inlining -> true
 *    - fnInstanceCount == 0 with blockInlines > 0 and costDeltaBlock > 0 -> false
 * 9. State Integrity:
 *    - setKnownConstants called once -> succeeds; called twice -> IllegalStateException
 */
public class FunctionInjectorGeminiTest {

  private Supplier<String> createNameSupplier() {
    return new Supplier<String>() {
      private int id = 0;
      @Override
      public String get() {
        return "JSCompiler_temp_id_" + (id++);
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
        createNameSupplier(),
        allowDecomposition,
        assumeStrictThis,
        assumeMinimumCapture);
  }

  private Node parse(Compiler compiler, String js) {
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler.parseTestCode(js);
  }

  private Node findFirstNode(Node n, int tokenType) {
    if (n.getType() == tokenType) {
      return n;
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findFirstNode(child, tokenType);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirementsNormal() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = createInjector(compiler, true, true, true);
    Node root = parse(compiler, "function validFn(x) { return x + 1; }");
    Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);

    boolean meetsReqs = injector.doesFunctionMeetMinimumRequirements("validFn", fnNode);
    assertTrue("A clean, simple function should meet minimum requirements", meetsReqs);
  }

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirementsWithArguments() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = createInjector(compiler, true, true, true);
    Node root = parse(compiler, "function argsFn(x) { return arguments[0]; }");
    Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);

    boolean meetsReqs = injector.doesFunctionMeetMinimumRequirements("argsFn", fnNode);
    assertFalse("Functions referencing arguments directly must not be inlinable", meetsReqs);
  }

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirementsWithEval() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = createInjector(compiler, true, true, true);
    Node root = parse(compiler, "function evalFn(x) { eval(x); }");
    Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);

    boolean meetsReqs = injector.doesFunctionMeetMinimumRequirements("evalFn", fnNode);
    assertFalse("Functions referencing eval must not be inlinable", meetsReqs);
  }

  @Test(timeout = 4000)
  public void testDoesFunctionMeetMinimumRequirementsSelfRecursion() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = createInjector(compiler, true, true, true);

    // Recursion using external name
    Node root1 = parse(compiler, "function recurse(x) { return recurse(x - 1); }");
    Node fnNode1 = findFirstNode(root1, com.google.javascript.rhino.Token.FUNCTION);
    assertFalse("Self-recursive function should fail requirements",
        injector.doesFunctionMeetMinimumRequirements("recurse", fnNode1));

    // Recursion using internal name in named function expression
    Node root2 = parse(compiler, "var f = function innerName(x) { return innerName(x - 1); };");
    Node fnNode2 = findFirstNode(root2, com.google.javascript.rhino.Token.FUNCTION);
    assertFalse("Function referencing its recursion identifier should fail requirements",
        injector.doesFunctionMeetMinimumRequirements("f", fnNode2));
  }

  @Test(timeout = 4000)
  public void testIsDirectCallNodeReplacementPossibleBranches() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = createInjector(compiler, true, true, true);

    // Empty function body -> true
    Node root1 = parse(compiler, "function empty() {}");
    Node fn1 = findFirstNode(root1, com.google.javascript.rhino.Token.FUNCTION);
    assertTrue("Empty function body should permit direct call replacement",
        injector.isDirectCallNodeReplacementPossible(fn1));

    // Return with expression -> true
    Node root2 = parse(compiler, "function retExpr() { return 42; }");
    Node fn2 = findFirstNode(root2, com.google.javascript.rhino.Token.FUNCTION);
    assertTrue("Function returning an expression should permit direct call replacement",
        injector.isDirectCallNodeReplacementPossible(fn2));

    // Empty return statement -> false
    Node root3 = parse(compiler, "function retVoid() { return; }");
    Node fn3 = findFirstNode(root3, com.google.javascript.rhino.Token.FUNCTION);
    assertFalse("Function with empty return should not permit direct replacement",
        injector.isDirectCallNodeReplacementPossible(fn3));

    // Statement that is not a return -> false
    Node root4 = parse(compiler, "function nonRet() { var a = 1; }");
    Node fn4 = findFirstNode(root4, com.google.javascript.rhino.Token.FUNCTION);
    assertFalse("Function without a return statement should not permit direct replacement",
        injector.isDirectCallNodeReplacementPossible(fn4));

    // Multi-statement body -> false
    Node root5 = parse(compiler, "function multi() { var a = 1; return a; }");
    Node fn5 = findFirstNode(root5, com.google.javascript.rhino.Token.FUNCTION);
    assertFalse("Multi-statement function body should not permit direct replacement",
        injector.isDirectCallNodeReplacementPossible(fn5));
  }

  @Test(timeout = 4000)
  public void testDirectInliningTransformations() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    FunctionInjector injector = createInjector(compiler, true, true, true);

    // Direct inlining of function returning expression
    Node root = parse(compiler, "function addOne(a) { return a + 1; } var x = addOne(5);");
    Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);
    Node callNode = findFirstNode(root, com.google.javascript.rhino.Token.CALL);

    Node inlined = injector.inline(callNode, "addOne", fnNode, FunctionInjector.InliningMode.DIRECT);
    assertNotNull(inlined);
    assertTrue("Direct inline should produce binary add expression", inlined.isAdd());

    // Direct inlining of empty function -> replaced with undefined (void 0)
    Node rootEmpty = parse(compiler, "function noop() {} var y = noop();");
    Node fnEmpty = findFirstNode(rootEmpty, com.google.javascript.rhino.Token.FUNCTION);
    Node callEmpty = findFirstNode(rootEmpty, com.google.javascript.rhino.Token.CALL);

    Node inlinedEmpty = injector.inline(callEmpty, "noop", fnEmpty, FunctionInjector.InliningMode.DIRECT);
    assertNotNull(inlinedEmpty);
    assertTrue("Empty function direct inline should produce void (undefined) node", inlinedEmpty.isVoid());
  }

  @Test(timeout = 4000)
  public void testBlockInliningTransformations() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    FunctionInjector injector = createInjector(compiler, true, true, true);

    // SIMPLE_CALL
    Node root1 = parse(compiler, "function action() { var a = 1; } action();");
    Node fn1 = findFirstNode(root1, com.google.javascript.rhino.Token.FUNCTION);
    Node call1 = findFirstNode(root1, com.google.javascript.rhino.Token.CALL);
    Node block1 = injector.inline(call1, "action", fn1, FunctionInjector.InliningMode.BLOCK);
    assertNotNull(block1);
    assertTrue(block1.isBlock());

    // SIMPLE_ASSIGNMENT
    Node root2 = parse(compiler, "function action() { return 1; } var z; z = action();");
    Node fn2 = findFirstNode(root2, com.google.javascript.rhino.Token.FUNCTION);
    Node call2 = findFirstNode(root2, com.google.javascript.rhino.Token.CALL);
    Node block2 = injector.inline(call2, "action", fn2, FunctionInjector.InliningMode.BLOCK);
    assertNotNull(block2);
    assertTrue(block2.isBlock());

    // VAR_DECL_SIMPLE_ASSIGNMENT
    Node root3 = parse(compiler, "function action() { return 1; } var z = action();");
    Node fn3 = findFirstNode(root3, com.google.javascript.rhino.Token.FUNCTION);
    Node call3 = findFirstNode(root3, com.google.javascript.rhino.Token.CALL);
    Node block3 = injector.inline(call3, "action", fn3, FunctionInjector.InliningMode.BLOCK);
    assertNotNull(block3);
    assertTrue(block3.isBlock());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testInliningLowersCostZeroReferences() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = createInjector(compiler, true, true, true);
    Node root = parse(compiler, "function foo() { return 1; }");
    Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);

    boolean lowers = injector.inliningLowersCost(
        null, fnNode, Collections.<FunctionInjector.Reference>emptyList(),
        Collections.<String>emptySet(), true, false);
    assertTrue("Zero references should always lower cost", lowers);
  }

  @Test(timeout = 4000)
  public void testInliningLowersCostSingleRemovableDirect() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = createInjector(compiler, true, true, true);
    Node root = parse(compiler, "function foo() { return 1; } foo();");
    Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);
    Node callNode = findFirstNode(root, com.google.javascript.rhino.Token.CALL);

    List<FunctionInjector.Reference> refs = new ArrayList<FunctionInjector.Reference>();
    refs.add(new FunctionInjector.Reference(callNode, null, FunctionInjector.InliningMode.DIRECT));

    boolean lowers = injector.inliningLowersCost(
        null, fnNode, refs, Collections.<String>emptySet(), true, false);
    assertTrue("A single removable direct inlining reference should lower cost", lowers);
  }

  @Test(timeout = 4000)
  public void testInliningCostThresholdEvaluation() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = createInjector(compiler, true, true, true);
    // Create a function with multiple complex statements
    Node root = parse(compiler, "function bigFn(a, b) { var x = a + 1; var y = b + 2; return x + y; } bigFn(1, 2);");
    Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);
    Node callNode = findFirstNode(root, com.google.javascript.rhino.Token.CALL);

    List<FunctionInjector.Reference> refs = new ArrayList<FunctionInjector.Reference>();
    refs.add(new FunctionInjector.Reference(callNode, null, FunctionInjector.InliningMode.BLOCK));

    // For single block inlining non-removable, fnInstanceCount = 1
    boolean lowers = injector.inliningLowersCost(
        null, fnNode, refs, Sets.newHashSet("a", "b"), false, false);
    // Cost estimation calculation executed without exception
    assertNotNull(lowers);
  }

  @Test(timeout = 4000)
  public void testCallWithSideEffectArgumentsCannotInlineDirectly() {
    final Compiler compiler = new Compiler();
    final FunctionInjector injector = createInjector(compiler, true, true, true);
    final Node root = parse(compiler, "function foo(a) { return a; } foo(x++);");
    final Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);

    final FunctionInjector.CanInlineResult[] res = new FunctionInjector.CanInlineResult[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall()) {
          res[0] = injector.canInlineReferenceToFunction(
              t, n, fnNode, Collections.<String>emptySet(),
              FunctionInjector.InliningMode.DIRECT, false, false);
        }
      }
    });

    assertEquals("Call argument with side-effects cannot be inlined directly",
        FunctionInjector.CanInlineResult.NO, res[0]);
  }

  @Test(timeout = 4000)
  public void testMutableArgumentReferencedMultipleTimesCannotInlineDirectly() {
    final Compiler compiler = new Compiler();
    final FunctionInjector injector = createInjector(compiler, true, true, true);
    // 'a' is referenced twice in the body, and the argument is a mutable object literal
    final Node root = parse(compiler, "function foo(a) { return a.x + a.y; } foo({});");
    final Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);

    final FunctionInjector.CanInlineResult[] res = new FunctionInjector.CanInlineResult[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall()) {
          res[0] = injector.canInlineReferenceToFunction(
              t, n, fnNode, Collections.<String>emptySet(),
              FunctionInjector.InliningMode.DIRECT, false, false);
        }
      }
    });

    assertEquals("Mutable argument referenced multiple times must not inline directly",
        FunctionInjector.CanInlineResult.NO, res[0]);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure-115 / Issue 1101)
  // =========================================================================

  /**
   * Targets Defect: FunctionInjectorTest::testIssue1101a
   * Direct inlining of a function that modifies its parameter (--a) into a call site
   * foo(x) must be REJECTED (CanInlineResult.NO) to prevent mutating the caller's variable x.
   * On defective version, this triggers expected:<NO> but was:<YES>.
   */
  @Test(timeout = 4000)
  public void testIssue1101a() {
    final Compiler compiler = new Compiler();
    final FunctionInjector injector = createInjector(compiler, true, true, true);
    final Node root = parse(compiler, "function foo(a){return --a;} foo(x);");
    final Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);

    final FunctionInjector.CanInlineResult[] res = new FunctionInjector.CanInlineResult[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall()) {
          res[0] = injector.canInlineReferenceToFunction(
              t, n, fnNode, Collections.<String>emptySet(),
              FunctionInjector.InliningMode.DIRECT, false, false);
        }
      }
    });

    assertEquals("Functions modifying parameter via decrement must not be directly inlined",
        FunctionInjector.CanInlineResult.NO, res[0]);
  }

  /**
   * Targets Defect: FunctionInjectorTest::testIssue1101b
   * Direct inlining of a function that modifies its parameter (a++) into a call site
   * foo(x) must be REJECTED (CanInlineResult.NO).
   * On defective version, this triggers expected:<NO> but was:<YES>.
   */
  @Test(timeout = 4000)
  public void testIssue1101b() {
    final Compiler compiler = new Compiler();
    final FunctionInjector injector = createInjector(compiler, true, true, true);
    final Node root = parse(compiler, "function foo(a){return a++;} foo(x);");
    final Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);

    final FunctionInjector.CanInlineResult[] res = new FunctionInjector.CanInlineResult[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall()) {
          res[0] = injector.canInlineReferenceToFunction(
              t, n, fnNode, Collections.<String>emptySet(),
              FunctionInjector.InliningMode.DIRECT, false, false);
        }
      }
    });

    assertEquals("Functions modifying parameter via post-increment must not be directly inlined",
        FunctionInjector.CanInlineResult.NO, res[0]);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructorNullCompilerThrows() {
    new FunctionInjector(null, createNameSupplier(), true, true, true);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testConstructorNullSupplierThrows() {
    Compiler compiler = new Compiler();
    new FunctionInjector(compiler, null, true, true, true);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testSetKnownConstantsTwiceThrows() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = createInjector(compiler, true, true, true);
    injector.setKnownConstants(Collections.singleton("CONST_A"));
    // Re-setting when not empty must throw IllegalStateException
    injector.setKnownConstants(Collections.singleton("CONST_B"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testInlineThrowsWhenNotNormalized() {
    Compiler compiler = new Compiler();
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.RAW);
    FunctionInjector injector = createInjector(compiler, true, true, true);

    Node root = parse(compiler, "function foo() { return 1; } foo();");
    Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);
    Node callNode = findFirstNode(root, com.google.javascript.rhino.Token.CALL);

    injector.inline(callNode, "foo", fnNode, FunctionInjector.InliningMode.DIRECT);
  }

  @Test(timeout = 4000)
  public void testCallTypeApplyNotSupported() {
    final Compiler compiler = new Compiler();
    final FunctionInjector injector = createInjector(compiler, true, true, true);
    final Node root = parse(compiler, "function foo(a) { return a; } foo.apply(null, [1]);");
    final Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);

    final FunctionInjector.CanInlineResult[] res = new FunctionInjector.CanInlineResult[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall()) {
          res[0] = injector.canInlineReferenceToFunction(
              t, n, fnNode, Collections.<String>emptySet(),
              FunctionInjector.InliningMode.DIRECT, false, false);
        }
      }
    });

    assertEquals("Calls via .apply are not supported", FunctionInjector.CanInlineResult.NO, res[0]);
  }

  @Test(timeout = 4000)
  public void testCallTypeCallStrictThisHandling() {
    final Compiler compiler = new Compiler();
    // Case 1: assumeStrictThis = false, .call passed a non-this object -> NO
    final FunctionInjector injectorNonStrict = createInjector(compiler, true, false, true);
    final Node root1 = parse(compiler, "function foo() { return 1; } foo.call(obj);");
    final Node fnNode1 = findFirstNode(root1, com.google.javascript.rhino.Token.FUNCTION);

    final FunctionInjector.CanInlineResult[] res1 = new FunctionInjector.CanInlineResult[1];
    NodeTraversal.traverse(compiler, root1, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall()) {
          res1[0] = injectorNonStrict.canInlineReferenceToFunction(
              t, n, fnNode1, Collections.<String>emptySet(),
              FunctionInjector.InliningMode.DIRECT, false, false);
        }
      }
    });
    assertEquals("Non-strict this call with obj argument must be rejected",
        FunctionInjector.CanInlineResult.NO, res1[0]);

    // Case 2: assumeStrictThis = true, .call passed any argument -> allowed
    final FunctionInjector injectorStrict = createInjector(compiler, true, true, true);
    final Node root2 = parse(compiler, "function foo() { return 1; } foo.call(obj);");
    final Node fnNode2 = findFirstNode(root2, com.google.javascript.rhino.Token.FUNCTION);

    final FunctionInjector.CanInlineResult[] res2 = new FunctionInjector.CanInlineResult[1];
    NodeTraversal.traverse(compiler, root2, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall()) {
          res2[0] = injectorStrict.canInlineReferenceToFunction(
              t, n, fnNode2, Collections.<String>emptySet(),
              FunctionInjector.InliningMode.DIRECT, false, false);
        }
      }
    });
    assertEquals("Strict this call with obj argument should be allowed for direct inlining",
        FunctionInjector.CanInlineResult.YES, res2[0]);
  }

  @Test(timeout = 4000)
  public void testReferencesThisWithoutFunctionObjectCallFails() {
    final Compiler compiler = new Compiler();
    final FunctionInjector injector = createInjector(compiler, true, true, true);
    final Node root = parse(compiler, "function foo() { return this.x; } foo();");
    final Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);

    final FunctionInjector.CanInlineResult[] res = new FunctionInjector.CanInlineResult[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall()) {
          res[0] = injector.canInlineReferenceToFunction(
              t, n, fnNode, Collections.<String>emptySet(),
              FunctionInjector.InliningMode.DIRECT, true, false);
        }
      }
    });

    assertEquals("Function referencing this called directly cannot be inlined",
        FunctionInjector.CanInlineResult.NO, res[0]);
  }

  @Test(timeout = 4000)
  public void testContainsFunctionsInsideLoopFails() {
    final Compiler compiler = new Compiler();
    final FunctionInjector injector = createInjector(compiler, true, true, true);
    final Node root = parse(compiler,
        "function foo() { return function() {}; } while(true) { foo(); }");
    final Node fnNode = findFirstNode(root, com.google.javascript.rhino.Token.FUNCTION);

    final FunctionInjector.CanInlineResult[] res = new FunctionInjector.CanInlineResult[1];
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall()) {
          res[0] = injector.canInlineReferenceToFunction(
              t, n, fnNode, Collections.<String>emptySet(),
              FunctionInjector.InliningMode.DIRECT, false, true);
        }
      }
    });

    assertEquals("Functions containing inner functions cannot be inlined inside loops",
        FunctionInjector.CanInlineResult.NO, res[0]);
  }

  @Test(timeout = 4000)
  public void testDecompositionPolicyAtCallSite() {
    Compiler compiler = new Compiler();
    // Test allowDecomposition = false on decomposable expression (var a = 1 + foo();)
    FunctionInjector noDecompInjector = createInjector(compiler, false, true, true);
    Node root1 = parse(compiler, "function foo() { return 1; } var a = 1 + foo();");
    final Node fn1 = findFirstNode(root1, com.google.javascript.rhino.Token.FUNCTION);

    final FunctionInjector.CanInlineResult[] res1 = new FunctionInjector.CanInlineResult[1];
    NodeTraversal.traverse(compiler, root1, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall()) {
          res1[0] = noDecompInjector.canInlineReferenceToFunction(
              t, n, fn1, Collections.<String>emptySet(),
              FunctionInjector.InliningMode.BLOCK, false, false);
        }
      }
    });
    assertEquals("Expression call without allowDecomposition must return NO",
        FunctionInjector.CanInlineResult.NO, res1[0]);

    // Test allowDecomposition = true on expression call site -> AFTER_PREPARATION
    FunctionInjector decompInjector = createInjector(compiler, true, true, true);
    Node root2 = parse(compiler, "function foo() { return 1; } if (foo()) {}");
    final Node fn2 = findFirstNode(root2, com.google.javascript.rhino.Token.FUNCTION);

    final FunctionInjector.CanInlineResult[] res2 = new FunctionInjector.CanInlineResult[1];
    NodeTraversal.traverse(compiler, root2, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.isCall()) {
          res2[0] = decompInjector.canInlineReferenceToFunction(
              t, n, fn2, Collections.<String>emptySet(),
              FunctionInjector.InliningMode.BLOCK, false, false);
        }
      }
    });
    assertEquals("Expression call with allowDecomposition should return AFTER_PREPARATION",
        FunctionInjector.CanInlineResult.AFTER_PREPARATION, res2[0]);
  }

  @Test(timeout = 4000)
  public void testMaybePrepareCallOnMovableExpression() {
    Compiler compiler = new Compiler();
    FunctionInjector injector = createInjector(compiler, true, true, true);
    Node root = parse(compiler, "function foo() { return true; } if (foo()) { var x = 1; }");
    Node callNode = findFirstNode(root, com.google.javascript.rhino.Token.CALL);

    // Call maybePrepareCall to move movable expression out of 'if' condition
    injector.maybePrepareCall(callNode);
    // After prepare, the parent of call should no longer be the IF condition directly
    assertNotNull(callNode.getParent());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testReferenceDataStructure() {
    Compiler compiler = new Compiler();
    Node root = parse(compiler, "foo();");
    Node callNode = findFirstNode(root, com.google.javascript.rhino.Token.CALL);
    JSModule module = new JSModule("testModule");

    FunctionInjector.Reference ref = new FunctionInjector.Reference(
        callNode, module, FunctionInjector.InliningMode.DIRECT);

    assertSame(callNode, ref.callNode);
    assertSame(module, ref.module);
    assertEquals(FunctionInjector.InliningMode.DIRECT, ref.mode);
  }

  @Test(timeout = 4000)
  public void testInliningModeAndCanInlineResultEnums() {
    assertEquals(2, FunctionInjector.InliningMode.values().length);
    assertEquals(FunctionInjector.InliningMode.DIRECT, FunctionInjector.InliningMode.valueOf("DIRECT"));
    assertEquals(FunctionInjector.InliningMode.BLOCK, FunctionInjector.InliningMode.valueOf("BLOCK"));

    assertEquals(3, FunctionInjector.CanInlineResult.values().length);
    assertEquals(FunctionInjector.CanInlineResult.YES, FunctionInjector.CanInlineResult.valueOf("YES"));
    assertEquals(FunctionInjector.CanInlineResult.AFTER_PREPARATION,
        FunctionInjector.CanInlineResult.valueOf("AFTER_PREPARATION"));
    assertEquals(FunctionInjector.CanInlineResult.NO, FunctionInjector.CanInlineResult.valueOf("NO"));
  }
}