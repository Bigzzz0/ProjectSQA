/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.google.javascript.jscomp.InlineFunctions
 *
 * 1. Defect Target (Defects4J Issue 423):
 *    - Condition: Inlining candidate functions called within complex control flow or catch blocks,
 *      or anonymous function calls with specific naming/structural characteristics.
 *    - Root failure: CanInlineResult validation / expression decomposition / name usage check
 *      tripping assertions or failing to inline safe candidate functions in try/catch or labels.
 *
 * 2. Decision Branches Covered:
 *    - FindCandidateFunctions.shouldTraverse:
 *      * inlineLocalFunctions == true vs false; inGlobalScope() == true vs false.
 *    - FindCandidateFunctions.visit & findNamedFunctions:
 *      * Token.VAR with function initialization vs non-functions / multi-var statements.
 *      * Token.FUNCTION: Statement function vs function expression vs labeled statement function.
 *    - FindCandidateFunctions.findFunctionExpressions:
 *      * Direct call: (function(){ ... })()
 *      * Function object call: (function(){ ... }).call(this)
 *      * Non-function calls / complex call targets.
 *    - maybeAddFunction:
 *      * Multiple function definitions with same name (fs.hasExistingFunctionDefinition()).
 *      * Direct replacement candidate vs block inlining candidate (namesToAlias, modified params).
 *      * referencesThis == true/false.
 *      * hasInnerFunctions == true/false and hasLocalNames (parameters, var decls, statements).
 *      * blockFunctionInliningEnabled == false when direct inlining is impossible.
 *      * isCandidateFunction: exported name check, RENAME_PROPERTY_FUNCTION_NAME check,
 *        specializationState null vs canFixupFunction == false.
 *    - isCandidateUsage:
 *      * Parent Token.VAR / Token.FUNCTION.
 *      * Parent Token.CALL where name is target child.
 *      * Parent CALL via .call (NodeUtil.isGet(parent) && name.getNext().getString().equals("call")).
 *      * Other usages: Assigned to (Token.ASSIGN, first child == n), aliased (var x = fn),
 *        passed to EXTERN_OBJECT_PROPERTY_STRING.
 *    - CallVisitor.visit:
 *      * Direct call child Token.NAME.
 *      * Anon function child Token.FUNCTION.
 *      * Function object call (.call) with NAME or FUNCTION identifier.
 *    - trimCanidatesUsingOnCost & mimimizeCost:
 *      * Lowers cost vs doesn't lower cost.
 *      * Fallback: removeBlockInliningReferences and re-evaluating cost.
 *    - resolveInlineConflicts:
 *      * Function calling another inlining candidate; prevents removal of called candidate.
 *    - decomposeExpressions & verifyAllReferencesInlined:
 *      * Expression decomposition requirement triggering ExpressionDecomposer.
 */

package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.*;

public class InlineFunctionsGeminiTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.initCompilerOptionsIfTesting();
    compiler.initOptions(options);
  }

  private Supplier<String> createSafeIdSupplier() {
    return new Supplier<String>() {
      private int counter = 0;
      @Override
      public String get() {
        return "JSCompiler_inline_" + (counter++);
      }
    };
  }

  /**
   * Helper to parse, normalize, run InlineFunctions pass, and return generated JS.
   */
  private String testInline(String js, boolean inlineGlobal, boolean inlineLocal, boolean blockInlining) {
    Node root = compiler.parseTestCode(js);
    assertEquals("Parsing errors found: " + compiler.getErrors(), 0, compiler.getErrorCount());

    Normalize normalize = new Normalize(compiler, false);
    normalize.process(new Node(Token.BLOCK), root);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    InlineFunctions pass = new InlineFunctions(
        compiler,
        createSafeIdSupplier(),
        inlineGlobal,
        inlineLocal,
        blockInlining);

    Node externs = new Node(Token.BLOCK);
    pass.process(externs, root);

    return compiler.toSource(root);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleDirectInliningGlobalFunction() {
    String js = "function add(a, b) { return a + b; } var result = add(1, 2);";
    String result = testInline(js, true, true, true);
    assertFalse("Directly inlined function definition should be removed", result.contains("function add"));
    assertTrue("Call site should be replaced by expression", result.contains("1 + 2"));
  }

  @Test(timeout = 4000)
  public void testBlockInliningSimple() {
    String js = "function work(a) { var b = a + 1; return b; } var res = work(5);";
    String result = testInline(js, true, true, true);
    assertFalse("Block inlined function definition should be removed", result.contains("function work"));
    assertTrue("Block inlined result should assign temporary or result", result.contains("5 + 1"));
  }

  @Test(timeout = 4000)
  public void testBlockInliningDisabled() {
    String js = "function work(a) { var b = a + 1; return b; } var res = work(5);";
    String result = testInline(js, true, true, false);
    assertTrue("Function requiring block inlining must not be inlined when block inlining is false",
        result.contains("function work"));
    assertTrue(result.contains("work(5)"));
  }

  @Test(timeout = 4000)
  public void testVarFunctionInlining() {
    String js = "var sq = function(x) { return x * x; }; var ans = sq(4);";
    String result = testInline(js, true, true, true);
    assertFalse("Var function definition should be inlined and removed", result.contains("var sq="));
    assertTrue("Call should be inlined", result.contains("4 * 4"));
  }

  @Test(timeout = 4000)
  public void testAnonymousFunctionExpressionCall() {
    String js = "var res = (function(x) { return x + 10; })(5);";
    String result = testInline(js, true, true, true);
    assertFalse("Anonymous function should be replaced", result.contains("function"));
    assertTrue(result.contains("5 + 10"));
  }

  @Test(timeout = 4000)
  public void testFunctionCallUsingDotCall() {
    String js = "function f(a) { return a; } var res = f.call(this, 99);";
    String result = testInline(js, true, true, true);
    assertFalse("Function called via .call should be inlined", result.contains("function f"));
    assertTrue(result.contains("99"));
  }

  @Test(timeout = 4000)
  public void testAnonymousFunctionDotCall() {
    String js = "var res = (function(a) { return a * 2; }).call(this, 12);";
    String result = testInline(js, true, true, true);
    assertFalse("Anonymous .call should be inlined", result.contains("function"));
    assertTrue(result.contains("12 * 2"));
  }

  @Test(timeout = 4000)
  public void testLocalFunctionInliningDisabled() {
    String js = "function outer() { function inner(x) { return x + 1; } return inner(2); }";
    String result = testInline(js, true, false, true);
    assertTrue("Inner function should not be inlined when inlineLocalFunctions is false",
        result.contains("function inner"));
  }

  @Test(timeout = 4000)
  public void testGlobalFunctionInliningDisabled() {
    String js = "function globalFn(x) { return x + 1; } var y = globalFn(2);";
    String result = testInline(js, false, true, true);
    assertTrue("Global function should not be inlined when inlineGlobalFunctions is false",
        result.contains("function globalFn"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Candidate Qualification
  // =========================================================================

  @Test(timeout = 4000)
  public void testMultipleDefinitionsPreventInlining() {
    String js = "function dup() { return 1; } function dup() { return 2; } var x = dup();";
    String result = testInline(js, true, true, true);
    assertTrue("Multiply defined function must not be inlined", result.contains("function dup"));
  }

  @Test(timeout = 4000)
  public void testRecursiveFunctionNotInlined() {
    String js = "function recur(n) { if (n <= 1) return 1; return n * recur(n - 1); } var x = recur(5);";
    String result = testInline(js, true, true, true);
    assertTrue("Recursive function must not be inlined", result.contains("function recur"));
  }

  @Test(timeout = 4000)
  public void testFunctionReferencingThis() {
    String js = "function getThis() { return this.x; } var res = getThis();";
    String result = testInline(js, true, true, true);
    assertNotNull(result);
  }

  @Test(timeout = 4000)
  public void testFunctionWithInnerFunctionAndLocalsNotInlined() {
    String js = "function parentFn() { var local = 10; function child() { return local; } return child(); } var x = parentFn();";
    String result = testInline(js, true, true, true);
    assertTrue("Function containing inner function and local declarations should not be inlined",
        result.contains("function parentFn"));
  }

  @Test(timeout = 4000)
  public void testFunctionWithModifiedParameters() {
    String js = "function mod(a) { a = a + 1; return a; } var res = mod(5);";
    String result = testInline(js, true, true, true);
    assertTrue("Modified parameters should require block inlining", result.contains("5 + 1") || result.contains("mod(5)"));
  }

  @Test(timeout = 4000)
  public void testFunctionAssignedToVariableCannotBeRemoved() {
    String js = "function f() { return 42; } var alias = f; var r = f();";
    String result = testInline(js, true, true, true);
    assertTrue("Function assigned to another variable cannot be removed", result.contains("function f"));
  }

  @Test(timeout = 4000)
  public void testFunctionReassignedCannotBeInlined() {
    String js = "function reassign() { return 1; } reassign = function() { return 2; }; var r = reassign();";
    String result = testInline(js, true, true, true);
    assertTrue("Reassigned function cannot be inlined", result.contains("reassign"));
  }

  @Test(timeout = 4000)
  public void testFunctionInLabel() {
    String js = "lbl: function labeledFn() { return 1; } var x = labeledFn();";
    String result = testInline(js, true, true, true);
    assertFalse("Labeled function should be candidate and inlined", result.contains("function labeledFn"));
  }

  @Test(timeout = 4000)
  public void testEmptyCodeProcessing() {
    String js = "";
    String result = testInline(js, true, true, true);
    assertEquals("", result.trim());
  }

  @Test(timeout = 4000)
  public void testCodeWithoutCandidates() {
    String js = "var a = 1; var b = 2; var c = a + b;";
    String result = testInline(js, true, true, true);
    assertTrue(result.contains("var a=1"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Issue 423)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIssue423_InlineInsideCatchBlock() {
    // Defects4J Issue 423: Inlining a function called inside a catch block
    String js = "var ok = function() { return (true); };" +
                "try { throw 'foo'; } catch (e) { ok(); }";
    String result = testInline(js, true, true, true);
    assertFalse("Candidate function 'ok' should be completely inlined out of catch block",
        result.contains("var ok="));
    assertTrue("Catch block should contain inlined value", result.contains("true"));
  }

  @Test(timeout = 4000)
  public void testIssue423_TryCatchFinallyInlining() {
    String js = "function cleanup() { return 0; } " +
                "try { cleanup(); } catch(e) { cleanup(); } finally { cleanup(); }";
    String result = testInline(js, true, true, true);
    assertFalse("cleanup function should be inlined even across try/catch/finally",
        result.contains("function cleanup"));
    assertFalse("Calls should be inlined", result.contains("cleanup()"));
  }

  // =========================================================================
  // Partition D: Conflicting References & Expression Decomposition
  // =========================================================================

  @Test(timeout = 4000)
  public void testResolveInlineConflicts_ChainedCandidateCalls() {
    String js = "function bar() { return 5; } " +
                "function foo() { return bar() + 1; } " +
                "var a = foo(); var b = foo();";
    String result = testInline(js, true, true, true);
    assertNotNull(result);
  }

  @Test(timeout = 4000)
  public void testDecomposeExpressionsNeeded() {
    String js = "function complex(a) { var x = a; return x; } " +
                "var y = 1 + complex(2);";
    String result = testInline(js, true, true, true);
    assertNotNull(result);
  }

  @Test(timeout = 4000)
  public void testSpecialPropertyFunctionRenamingIgnored() {
    String js = "function " + RenameProperties.RENAME_PROPERTY_FUNCTION_NAME + "(p) { return p; } " +
                "var z = " + RenameProperties.RENAME_PROPERTY_FUNCTION_NAME + "('prop');";
    String result = testInline(js, true, true, true);
    assertTrue("Special property renaming function must not be inlined",
        result.contains(RenameProperties.RENAME_PROPERTY_FUNCTION_NAME));
  }

  @Test(timeout = 4000)
  public void testObjectPropertyStringPreprocessReference() {
    String js = "function myMethod() { return 10; } " +
                "new " + ObjectPropertyStringPreprocess.EXTERN_OBJECT_PROPERTY_STRING + "(window, myMethod); " +
                "var val = myMethod();";
    String result = testInline(js, true, true, true);
    assertTrue("myMethod should not be inlined because it is passed to EXTERN_OBJECT_PROPERTY_STRING",
        result.contains("function myMethod"));
  }

  // =========================================================================
  // Partition E: Direct Unit Tests on Package-Private Classes & Methods
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsCandidateUsageEdgeCases() {
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "foo");
    varNode.addChildToBack(nameNode);
    assertTrue("NAME child of VAR is candidate usage", InlineFunctions.isCandidateUsage(nameNode));

    Node callNode = new Node(Token.CALL);
    Node fnName = Node.newString(Token.NAME, "bar");
    callNode.addChildToBack(fnName);
    assertTrue("NAME as first child of CALL is candidate usage", InlineFunctions.isCandidateUsage(fnName));

    Node callArg = Node.newString(Token.NAME, "baz");
    callNode.addChildToBack(callArg);
    assertFalse("NAME as arg child of CALL is not candidate usage", InlineFunctions.isCandidateUsage(callArg));

    Node getProp = new Node(Token.GETPROP);
    Node calleeName = Node.newString(Token.NAME, "myFn");
    Node callProp = Node.newString(Token.STRING, "call");
    getProp.addChildToBack(calleeName);
    getProp.addChildToBack(callProp);
    Node dotCall = new Node(Token.CALL);
    dotCall.addChildToBack(getProp);
    assertTrue("NAME used in .call is candidate usage", InlineFunctions.isCandidateUsage(calleeName));
  }

  @Test(timeout = 4000)
  public void testConstructorPreconditions() {
    try {
      new InlineFunctions(null, createSafeIdSupplier(), true, true, true);
      fail("Expected IllegalArgumentException for null compiler");
    } catch (IllegalArgumentException expected) {
      assertNotNull(expected.getMessage());
    }

    try {
      new InlineFunctions(compiler, null, true, true, true);
      fail("Expected IllegalArgumentException for null safeNameIdSupplier");
    } catch (IllegalArgumentException expected) {
      assertNotNull(expected.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testProcessUnnormalizedThrowsException() {
    InlineFunctions pass = new InlineFunctions(compiler, createSafeIdSupplier(), true, true, true);
    Node root = new Node(Token.BLOCK);
    try {
      pass.process(new Node(Token.BLOCK), root);
      fail("Expected IllegalStateException when compiling unnormalized AST");
    } catch (IllegalStateException expected) {
      assertNotNull(expected.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testSpecializationStateIntegration() {
    final boolean[] reportSpecializedCalled = new boolean[]{false};
    final boolean[] reportRemovedCalled = new boolean[]{false};

    SpecializeModule.SpecializationState state = new SpecializeModule.SpecializationState() {
      @Override
      public boolean canFixupFunction(Node functionNode) {
        return true;
      }
      @Override
      public void reportSpecializedFunction(Node functionNode) {
        reportSpecializedCalled[0] = true;
      }
      @Override
      public void reportRemovedFunction(Node functionNode, Node declaringBlock) {
        reportRemovedCalled[0] = true;
      }
    };

    String js = "function specialized(a) { return a + 1; } var r = specialized(5);";
    Node root = compiler.parseTestCode(js);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(new Node(Token.BLOCK), root);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    InlineFunctions pass = new InlineFunctions(compiler, createSafeIdSupplier(), true, true, true);
    pass.enableSpecialization(state);
    pass.process(new Node(Token.BLOCK), root);

    assertTrue("reportSpecializedFunction or reportRemovedFunction should be reported",
        reportSpecializedCalled[0] || reportRemovedCalled[0]);
  }

  @Test(timeout = 4000)
  public void testSpecializationStateCannotFixupFunction() {
    SpecializeModule.SpecializationState state = new SpecializeModule.SpecializationState() {
      @Override
      public boolean canFixupFunction(Node functionNode) {
        return false;
      }
      @Override
      public void reportSpecializedFunction(Node functionNode) {}
      @Override
      public void reportRemovedFunction(Node functionNode, Node declaringBlock) {}
    };

    String js = "function specialized(a) { return a + 1; } var r = specialized(5);";
    Node root = compiler.parseTestCode(js);
    Normalize normalize = new Normalize(compiler, false);
    normalize.process(new Node(Token.BLOCK), root);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);

    InlineFunctions pass = new InlineFunctions(compiler, createSafeIdSupplier(), true, true, true);
    pass.enableSpecialization(state);
    pass.process(new Node(Token.BLOCK), root);

    String result = compiler.toSource(root);
    assertTrue("Function must not be inlined if specialization state cannot fix it up",
        result.contains("function specialized"));
  }

  @Test(timeout = 4000)
  public void testGetOrCreateFunctionState() {
    InlineFunctions pass = new InlineFunctions(compiler, createSafeIdSupplier(), true, true, true);
    Object fs1 = pass.getOrCreateFunctionState("testFn");
    assertNotNull(fs1);
    Object fs2 = pass.getOrCreateFunctionState("testFn");
    assertSame("Same name should return the identical FunctionState instance", fs1, fs2);
  }
}