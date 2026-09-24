package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.jscomp.FunctionToBlockMutator.LabelNameSupplier;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.FunctionToBlockMutator
 * Associated Defect: InlineFunctionsTest::testInlineFunctions31
 *
 * 1. Branch Coverage Analysis:
 *    - mutate(...):
 *      * hasArgs: true (with args) vs false (no args).
 *      * namesToAlias: null / empty vs non-empty (modified function parameters require local alias VARs).
 *      * isCallInLoop: true (triggers fixUnitializedVarDeclarations) vs false.
 *      * fnName: null, empty (""), and non-empty name (affects label generation).
 *    - replaceReturns(...):
 *      * returnCount == 0 vs returnCount == 1 vs returnCount > 1.
 *      * hasReturnAtExit: true vs false.
 *      * returnCount > 0 needing label and break statements.
 *      * resultMustBeSet: true vs false.
 *      * resultName: null vs non-null (assignments vs simple expression statements).
 *    - fixUnitializedVarDeclarations(...):
 *      * NodeUtil.isLoopStructure: skips recursion into nested loops (e.g. for, while, for-in).
 *      * NodeUtil.isVar: processes uninitialized declarations.
 *      * DEFECT VECTOR: In the original implementation, fixUnitializedVarDeclarations only checks
 *        the first child (`n.getFirstChild()`) and returns immediately. If a VAR node declares
 *        multiple variables (e.g., `var a, b;` or `var a = 1, b;`), subsequent variables are
 *        left uninitialized to undefined when inlined into loops, causing incorrect loop state reuse.
 *    - replaceReturnWithBreak(...):
 *      * nested FUNCTION or EXPR_RESULT: recursion stops without transforming inner returns.
 *      * RETURN node: converted to break (and optional result assignment) in statement block.
 *    - LabelNameSupplier:
 *      * Generates label prefixed with "JSCompiler_inline_label_".
 */
public class FunctionToBlockMutatorGeminiTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
  }

  private Supplier<String> createSequentialIdSupplier() {
    return new Supplier<String>() {
      private int counter = 0;

      @Override
      public String get() {
        return String.valueOf(counter++);
      }
    };
  }

  private Node parseFunction(String script) {
    Node root = compiler.parseTestCode(script);
    assertNotNull("Parsed root should not be null", root);
    Node fn = root.getFirstChild();
    assertEquals("Root child must be a FUNCTION node", Token.FUNCTION, fn.getType());
    return fn;
  }

  private Node parseCall(String script) {
    Node root = compiler.parseTestCode(script);
    assertNotNull("Parsed root should not be null", root);
    Node expr = root.getFirstChild();
    assertEquals("Root child must be an EXPR_RESULT node", Token.EXPR_RESULT, expr.getType());
    Node call = expr.getFirstChild();
    assertEquals("Expression child must be a CALL node", Token.CALL, call.getType());
    return call;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions (Returns & Results)
  // =========================================================================

  @Test(timeout = 4000)
  public void testMutateFunctionWithoutReturnNoResultName() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f() { var x = 1; }");
    Node callNode = parseCall("f();");

    Node block = mutator.mutate("f", fnNode, callNode, null, false, false);

    assertNotNull(block);
    assertEquals(Token.BLOCK, block.getType());
    assertEquals(1, block.getChildCount());
    assertEquals(Token.VAR, block.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testMutateFunctionWithReturnAtExitNoResultName() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f() { return 42; }");
    Node callNode = parseCall("f();");

    Node block = mutator.mutate("f", fnNode, callNode, null, false, false);

    assertNotNull(block);
    assertEquals(Token.BLOCK, block.getType());
    assertEquals(1, block.getChildCount());
    Node stmt = block.getFirstChild();
    assertEquals(Token.EXPR_RESULT, stmt.getType());
    assertEquals(Token.NUMBER, stmt.getFirstChild().getType());
    assertEquals(42.0, stmt.getFirstChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testMutateFunctionWithReturnAtExitWithResultName() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f() { return 42; }");
    Node callNode = parseCall("f();");

    Node block = mutator.mutate("f", fnNode, callNode, "res", false, false);

    assertNotNull(block);
    assertEquals(Token.BLOCK, block.getType());
    Node stmt = block.getFirstChild();
    assertEquals(Token.EXPR_RESULT, stmt.getType());
    Node assign = stmt.getFirstChild();
    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals("res", assign.getFirstChild().getString());
    assertEquals(42.0, assign.getLastChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testMutateFunctionWithEmptyReturnAtExitNoResultName() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f() { return; }");
    Node callNode = parseCall("f();");

    Node block = mutator.mutate("f", fnNode, callNode, null, false, false);

    assertNotNull(block);
    assertEquals(Token.BLOCK, block.getType());
    // An empty return with no result required should be removed completely
    assertEquals(0, block.getChildCount());
  }

  @Test(timeout = 4000)
  public void testMutateFunctionWithEmptyReturnAtExitWithResultName() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f() { return; }");
    Node callNode = parseCall("f();");

    Node block = mutator.mutate("f", fnNode, callNode, "res", false, false);

    assertNotNull(block);
    assertEquals(Token.BLOCK, block.getType());
    assertEquals(1, block.getChildCount());
    Node stmt = block.getFirstChild();
    assertEquals(Token.EXPR_RESULT, stmt.getType());
    Node assign = stmt.getFirstChild();
    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals("res", assign.getFirstChild().getString());
    assertEquals(Token.VOID, assign.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testMutateFunctionNoReturnWithNeedsDefaultResult() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f() { var x = 1; }");
    Node callNode = parseCall("f();");

    Node block = mutator.mutate("f", fnNode, callNode, "res", true, false);

    assertNotNull(block);
    assertEquals(Token.BLOCK, block.getType());
    assertEquals(2, block.getChildCount());
    Node dummyAssignStmt = block.getLastChild();
    assertEquals(Token.EXPR_RESULT, dummyAssignStmt.getType());
    Node assign = dummyAssignStmt.getFirstChild();
    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals("res", assign.getFirstChild().getString());
    assertEquals(Token.VOID, assign.getLastChild().getType());
  }

  // =========================================================================
  // Partition B: Argument Inlining & Parameter Aliasing
  // =========================================================================

  @Test(timeout = 4000)
  public void testMutateFunctionWithUnmodifiedArgumentsInlinedDirectly() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f(a, b) { return a + b; }");
    Node callNode = parseCall("f(10, 20);");

    Node block = mutator.mutate("f", fnNode, callNode, "res", false, false);

    assertNotNull(block);
    assertEquals(Token.BLOCK, block.getType());
    Node stmt = block.getFirstChild();
    assertEquals(Token.EXPR_RESULT, stmt.getType());
    Node assign = stmt.getFirstChild();
    assertEquals(Token.ASSIGN, assign.getType());
    Node add = assign.getLastChild();
    assertEquals(Token.ADD, add.getType());
    assertEquals(10.0, add.getFirstChild().getDouble(), 0.0);
    assertEquals(20.0, add.getLastChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testMutateFunctionWithModifiedArgumentAliasesCreated() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f(x) { x = x + 1; return x; }");
    Node callNode = parseCall("f(5);");

    Node block = mutator.mutate("f", fnNode, callNode, "res", false, false);

    assertNotNull(block);
    assertEquals(Token.BLOCK, block.getType());
    // First node must be the alias VAR created for x
    Node firstStmt = block.getFirstChild();
    assertEquals(Token.VAR, firstStmt.getType());
    Node varName = firstStmt.getFirstChild();
    assertNotNull(varName);
    assertEquals(Token.NUMBER, varName.getFirstChild().getType());
    assertEquals(5.0, varName.getFirstChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testMutateFunctionWithMultipleModifiedArgumentsPreservesOrder() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f(a, b) { a = a + 1; b = b + 1; return a + b; }");
    Node callNode = parseCall("f(1, 2);");

    Node block = mutator.mutate("f", fnNode, callNode, "res", false, false);

    assertNotNull(block);
    assertEquals(Token.BLOCK, block.getType());
    Node firstChild = block.getFirstChild();
    assertEquals(Token.VAR, firstChild.getType());
    Node secondChild = firstChild.getNext();
    assertEquals(Token.VAR, secondChild.getType());
  }

  // =========================================================================
  // Partition C: Multiple Returns, Breaks, Labels & Nested Functions
  // =========================================================================

  @Test(timeout = 4000)
  public void testMutateMultipleReturnsGeneratesLabelAndBreaks() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f(c) { if (c) { return 1; } return 2; }");
    Node callNode = parseCall("f(true);");

    Node root = mutator.mutate("f", fnNode, callNode, "res", false, false);

    assertNotNull(root);
    assertEquals(Token.BLOCK, root.getType());
    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelName = labelNode.getFirstChild();
    assertEquals(Token.LABEL_NAME, labelName.getType());
    assertTrue(labelName.getString().startsWith("JSCompiler_inline_label_f_"));

    Node labeledBlock = labelName.getNext();
    assertEquals(Token.BLOCK, labeledBlock.getType());

    // Verify early return was replaced with an assignment and a break
    Node ifNode = labeledBlock.getFirstChild();
    assertEquals(Token.IF, ifNode.getType());
    Node ifBody = ifNode.getLastChild();
    assertEquals(Token.BLOCK, ifBody.getType());
    assertEquals(Token.EXPR_RESULT, ifBody.getFirstChild().getType());
    assertEquals(Token.BREAK, ifBody.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testMutateEarlyReturnWithoutReturnAtExitWithNeedsDefaultResult() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f(c) { if (c) { return 10; } }");
    Node callNode = parseCall("f(true);");

    Node root = mutator.mutate("f", fnNode, callNode, "res", true, false);

    assertNotNull(root);
    assertEquals(Token.BLOCK, root.getType());
    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labeledBlock = labelNode.getLastChild();
    // A dummy assignment should be appended to the block
    Node lastStmt = labeledBlock.getLastChild();
    assertEquals(Token.EXPR_RESULT, lastStmt.getType());
    assertEquals(Token.ASSIGN, lastStmt.getFirstChild().getType());
    assertEquals("res", lastStmt.getFirstChild().getFirstChild().getString());
    assertEquals(Token.VOID, lastStmt.getFirstChild().getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testMutateMultipleReturnsNoResultName() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f(c) { if (c) { return 1; } return 2; }");
    Node callNode = parseCall("f(true);");

    Node root = mutator.mutate("f", fnNode, callNode, null, false, false);

    assertNotNull(root);
    assertEquals(Token.BLOCK, root.getType());
    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
  }

  @Test(timeout = 4000)
  public void testMutateMultipleVoidReturnsWithResultName() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f(c) { if (c) { return; } return; }");
    Node callNode = parseCall("f(true);");

    Node root = mutator.mutate("f", fnNode, callNode, "res", false, false);

    assertNotNull(root);
    assertEquals(Token.BLOCK, root.getType());
    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
  }

  @Test(timeout = 4000)
  public void testMutateMultipleVoidReturnsNoResultName() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f(c) { if (c) { return; } return; }");
    Node callNode = parseCall("f(true);");

    Node root = mutator.mutate("f", fnNode, callNode, null, false, false);

    assertNotNull(root);
    assertEquals(Token.BLOCK, root.getType());
    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labeledBlock = labelNode.getLastChild();
    Node ifNode = labeledBlock.getFirstChild();
    Node ifBody = ifNode.getLastChild();
    // Void return with null resultName produces only a BREAK node
    assertEquals(1, ifBody.getChildCount());
    assertEquals(Token.BREAK, ifBody.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testMutateNestedFunctionReturnsNotModified() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f() { function inner() { return 99; } return 1; }");
    Node callNode = parseCall("f();");

    Node block = mutator.mutate("f", fnNode, callNode, "res", false, false);

    assertNotNull(block);
    Node innerFn = block.getFirstChild();
    assertEquals(Token.FUNCTION, innerFn.getType());
    Node innerBody = innerFn.getLastChild();
    Node innerReturn = innerBody.getFirstChild();
    assertEquals("Inner function return statement must remain intact", Token.RETURN, innerReturn.getType());
  }

  // =========================================================================
  // Partition D: Call In Loop & Inner Loop Structures
  // =========================================================================

  @Test(timeout = 4000)
  public void testMutateCallInLoopSingleUninitializedVar() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f() { var x; }");
    Node callNode = parseCall("f();");

    Node block = mutator.mutate("f", fnNode, callNode, null, false, true);

    assertNotNull(block);
    Node varNode = block.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    Node varName = varNode.getFirstChild();
    assertTrue("x must be initialized to undefined when inlined into a loop", varName.hasChildren());
    assertEquals(Token.VOID, varName.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testMutateCallInLoopAlreadyInitializedVarRemainsUnchanged() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f() { var x = 100; }");
    Node callNode = parseCall("f();");

    Node block = mutator.mutate("f", fnNode, callNode, null, false, true);

    assertNotNull(block);
    Node varNode = block.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    Node varName = varNode.getFirstChild();
    assertEquals(Token.NUMBER, varName.getFirstChild().getType());
    assertEquals(100.0, varName.getFirstChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testMutateCallInLoopDoesNotModifyInnerLoops() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction(
        "function f() { " +
        "  for (var i = 0; i < 10; i++) {} " +
        "  for (var k in {}) {} " +
        "  while (false) { var w; } " +
        "}");
    Node callNode = parseCall("f();");

    Node block = mutator.mutate("f", fnNode, callNode, null, false, true);

    assertNotNull(block);
    assertEquals(Token.BLOCK, block.getType());
    assertEquals(3, block.getChildCount());
  }

  // =========================================================================
  // Partition E: Defect-Targeted Branch Zone (Defects4J testInlineFunctions31)
  // =========================================================================

  /**
   * Targets the defect exhibited in InlineFunctionsTest::testInlineFunctions31.
   * When isCallInLoop is true, fixUnitializedVarDeclarations must inspect and initialize
   * EVERY variable in a VAR statement, not just the first child.
   */
  @Test(timeout = 4000)
  public void testInlineFunctions31_multipleUninitializedVarsInLoop() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f() { var a, b; }");
    Node callNode = parseCall("f();");

    Node block = mutator.mutate("f", fnNode, callNode, null, false, true);

    assertNotNull(block);
    Node varNode = null;
    for (Node c = block.getFirstChild(); c != null; c = c.getNext()) {
      if (c.getType() == Token.VAR) {
        varNode = c;
        break;
      }
    }
    assertNotNull("VAR node must exist in mutated block", varNode);

    Node firstVar = varNode.getFirstChild();
    assertNotNull("First declared variable 'a' must exist", firstVar);
    assertTrue("First variable 'a' must be initialized to undefined in loop", firstVar.hasChildren());

    Node secondVar = firstVar.getNext();
    assertNotNull("Second declared variable 'b' must exist", secondVar);
    assertTrue(
        "FAILS ON DEFECTIVE IMPLEMENTATION: Second variable 'b' must also be initialized to undefined in loop",
        secondVar.hasChildren()
    );
    assertEquals(Token.VOID, secondVar.getFirstChild().getType());
  }

  /**
   * Targets the defect where the first variable is initialized but subsequent
   * variables in the same VAR node are uninitialized.
   */
  @Test(timeout = 4000)
  public void testInlineFunctions31_firstInitializedSecondUninitializedVarInLoop() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f() { var a = 1, b; }");
    Node callNode = parseCall("f();");

    Node block = mutator.mutate("f", fnNode, callNode, null, false, true);

    assertNotNull(block);
    Node varNode = null;
    for (Node c = block.getFirstChild(); c != null; c = c.getNext()) {
      if (c.getType() == Token.VAR) {
        varNode = c;
        break;
      }
    }
    assertNotNull("VAR node must exist in mutated block", varNode);

    Node firstVar = varNode.getFirstChild();
    assertEquals("a", firstVar.getString().split("\\$")[0].replace("inline_", ""));
    assertTrue(firstVar.hasChildren());
    assertEquals(Token.NUMBER, firstVar.getFirstChild().getType());

    Node secondVar = firstVar.getNext();
    assertNotNull("Second variable declaration 'b' must exist", secondVar);
    assertTrue(
        "FAILS ON DEFECTIVE IMPLEMENTATION: Variable 'b' must be initialized to undefined even if 'a' has a value",
        secondVar.hasChildren()
    );
    assertEquals(Token.VOID, secondVar.getFirstChild().getType());
  }

  // =========================================================================
  // Partition F: Label Naming & LabelNameSupplier
  // =========================================================================

  @Test(timeout = 4000)
  public void testLabelNameSupplierContract() {
    Supplier<String> idSupplier = createSequentialIdSupplier();
    LabelNameSupplier labelSupplier = new LabelNameSupplier(idSupplier);

    String label0 = labelSupplier.get();
    assertEquals("JSCompiler_inline_label_0", label0);

    String label1 = labelSupplier.get();
    assertEquals("JSCompiler_inline_label_1", label1);
  }

  @Test(timeout = 4000)
  public void testAnonymousFunctionLabelNamingNullName() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f(c) { if (c) { return 1; } return 2; }");
    Node callNode = parseCall("f(true);");

    Node root = mutator.mutate(null, fnNode, callNode, null, false, false);

    assertNotNull(root);
    assertEquals(Token.BLOCK, root.getType());
    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelName = labelNode.getFirstChild();
    assertTrue(
        "Label for null function name should use 'anon'",
        labelName.getString().startsWith("JSCompiler_inline_label_anon_")
    );
  }

  @Test(timeout = 4000)
  public void testAnonymousFunctionLabelNamingEmptyName() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f(c) { if (c) { return 1; } return 2; }");
    Node callNode = parseCall("f(true);");

    Node root = mutator.mutate("", fnNode, callNode, null, false, false);

    assertNotNull(root);
    assertEquals(Token.BLOCK, root.getType());
    Node labelNode = root.getFirstChild();
    assertEquals(Token.LABEL, labelNode.getType());
    Node labelName = labelNode.getFirstChild();
    assertTrue(
        "Label for empty function name should use 'anon'",
        labelName.getString().startsWith("JSCompiler_inline_label_anon_")
    );
  }

  // =========================================================================
  // Partition G: Defensive Boundary Conditions & Empty Bodies
  // =========================================================================

  @Test(timeout = 4000)
  public void testMutateEmptyFunctionBodyWithNeedsDefaultResult() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f() {}");
    Node callNode = parseCall("f();");

    Node block = mutator.mutate("f", fnNode, callNode, "out", true, false);

    assertNotNull(block);
    assertEquals(Token.BLOCK, block.getType());
    assertEquals(1, block.getChildCount());
    Node stmt = block.getFirstChild();
    assertEquals(Token.EXPR_RESULT, stmt.getType());
    Node assign = stmt.getFirstChild();
    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals("out", assign.getFirstChild().getString());
    assertEquals(Token.VOID, assign.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testMutateEmptyFunctionBodyWithoutNeedsDefaultResult() {
    FunctionToBlockMutator mutator =
        new FunctionToBlockMutator(compiler, createSequentialIdSupplier());
    Node fnNode = parseFunction("function f() {}");
    Node callNode = parseCall("f();");

    Node block = mutator.mutate("f", fnNode, callNode, "out", false, false);

    assertNotNull(block);
    assertEquals(Token.BLOCK, block.getType());
    assertEquals(0, block.getChildCount());
  }
}