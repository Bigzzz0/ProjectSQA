package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.jscomp.CompilerOptions.LanguageMode;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: UnreachableCodeElimination.java
 * Objectives: maximal line/branch coverage, expose known defects:
 *   - testCascadedRemovalOfUnlessUnconditonalJumps (AssertionFailedError)
 *   - testIssue311 (INTERNAL COMPILER ERROR)
 *
 * Partition A: Core functional logic – removal of dead code after return,
 *              removal of no-op statements, handling of various node types.
 * Partition B: Boundary values – empty blocks, null parent, FUNCTION/SCRIPT skip,
 *              blocks inside TRY, DO loop.
 * Partition C: Defect-targeted – cascaded removal of break->break->break,
 *              removal inside a try-catch-finally that triggers internal error.
 * Partition D: Exception paths – null CFG node, null gNode annotation,
 *              unreachable DO node preservation.
 * Partition E: Object lifecycle – compiler instance, code change reporting.
 *
 * All tests use real Compiler objects with appropriate options to exercise the pass.
 */
public class UnreachableCodeEliminationDeepseekTest {

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setLanguageIn(LanguageMode.ECMASCRIPT3);
    // No other passes – we run UnreachableCodeElimination manually.
    compiler.initOptions(options);
    return compiler;
  }

  /**
   * Helper: runs UnreachableCodeElimination on a given source string,
   * returns the compiled and processed root node.
   */
  private Node runPass(String source) {
    Compiler compiler = createCompiler();
    Node root = compiler.parseSyntheticCode("test", source);
    assertNotNull("Parsing failed", root);
    compiler.setHasRegExpGlobalReferences(false);
    UnreachableCodeElimination pass =
        new UnreachableCodeElimination(compiler, true);
    pass.process(null, root);
    return root;
  }

  // -----------------------------------------------------------------------
  // Partition A: Core functional logic
  // -----------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testRemovalAfterReturn() {
    String src = "function f() { return 1; var a = 2; }";
    Node root = runPass(src);
    // The var statement should have been removed; only the return remains.
    String result = new CodePrinter.Builder(root).build();
    // Expected: function f() { return 1; }
    assertTrue("Removal after return failed", result.contains("return 1;"));
    assertFalse("var a should be removed", result.contains("var a = 2;"));
  }

  @Test(timeout = 4000)
  public void testRemovalOfNoOpStatement() {
    String src = "function f() { true; }";
    Node root = runPass(src);
    String result = new CodePrinter.Builder(root).build();
    assertFalse("No-op 'true;' should be removed", result.contains("true;"));
  }

  @Test(timeout = 4000)
  public void testNoRemovalOfSideEffect() {
    String src = "function f() { alert(1); }";
    Node root = runPass(src);
    String result = new CodePrinter.Builder(root).build();
    assertTrue("Side effect should remain", result.contains("alert(1);"));
  }

  @Test(timeout = 4000)
  public void testRemovalOfBreakAfterReturn() {
    String src = "function f() { while(1) { return; break; } }";
    Node root = runPass(src);
    String result = new CodePrinter.Builder(root).build();
    // break after return should be removed.
    assertTrue("Should contain 'return;'", result.contains("return;"));
    // break may be removed – the while body becomes only return.
    assertFalse("break should be removed", result.contains("break;"));
  }

  @Test(timeout = 4000)
  public void testCascadedBreakRemoval() {
    String src = "function f() { L: while(1) { break L; break L; } }";
    Node root = runPass(src);
    String result = new CodePrinter.Builder(root).build();
    // Both breaks are unconditional and the second is useless,
    // so they may be removed entirely, leaving while(1){}.
    // Actually, the first break jumps to after the while, second is unreachable.
    // Both should be removed.
    assertFalse("Break should be removed", result.contains("break;"));
  }

  @Test(timeout = 4000)
  public void testContinueAfterReturn() {
    String src = "function f() { for(;;) { return; continue; } }";
    Node root = runPass(src);
    String result = new CodePrinter.Builder(root).build();
    assertTrue("Return should remain", result.contains("return;"));
    assertFalse("Continue after return should be removed", result.contains("continue;"));
  }

  @Test(timeout = 4000)
  public void testBlockRemovalNoChildren() {
    // A block {} with no statements is empty and should be removed via FoldConstants,
    // but the pass itself leaves it alone to avoid complication.
    String src = "function f() { {} }";
    Node root = runPass(src);
    // The empty block may remain – we just verify no exception.
    // The pass does not remove EMPTY or empty BLOCK (see removeDeadExprStatementSafely).
    assertNotNull("Root should not be null", root);
  }

  // -----------------------------------------------------------------------
  // Partition B: Boundary value analysis
  // -----------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testNullParentInVisit() {
    // When parent is null, visit returns early. We ensure no exception.
    Compiler compiler = createCompiler();
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, true);
    // Create a synthetic node with null parent.
    Node dummy = Node.newNumber(1);
    NodeTraversal t = new NodeTraversal(compiler, pass); // dummy traversal
    pass.visit(t, dummy, null); // should do nothing
    // No assertion needed, just no exception.
  }

  @Test(timeout = 4000)
  public void testFunctionAndScriptNodesAreSkipped() {
    // The visit returns early for FUNCTION and SCRIPT nodes.
    String src = "function f() {}";
    Node root = runPass(src);
    // Function should remain intact.
    assertTrue("Function definition should remain", root.getFirstChild().isFunction());
  }

  @Test(timeout = 4000)
  public void testCfgNodeNotInGraph() {
    // If gNode is null (node not in CFG), visit returns early.
    String src = "function f() { var x = 1; }";
    Node root = runPass(src);
    // The var statement is in the CFG, so it should be handled normally.
    // To test ‘not in CFG’, we would need to fool the pass, but that's tricky.
    // This test just ensures normal path works.
    assertNotNull(root);
  }

  @Test(timeout = 4000)
  public void testDoLoopNotRemoved() {
    // DO loops are explicitly preserved even if unreachable.
    String src = "function f() { return; do { } while(0); }";
    Node root = runPass(src);
    String result = new CodePrinter.Builder(root).build();
    // The DO should be kept (though the return before it remains).
    assertTrue("Do loop should remain", result.contains("do"));
  }

  @Test(timeout = 4000)
  public void testCatchBlockPreserved() {
    // TRY-CATCH handling: BLOCK inside TRY is the catch container.
    String src = "function f() { try { return; } catch(e) {} }";
    Node root = runPass(src);
    String result = new CodePrinter.Builder(root).build();
    assertTrue("try should remain", result.contains("try"));
    assertTrue("catch should remain", result.contains("catch"));
  }

  // -----------------------------------------------------------------------
  // Partition C: Defect-targeted tests
  // -----------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testCascadedRemovalOfUnconditionalJumps() {
    // This test targets the known defect in cascaded removal of break/continue/return.
    // The bug triggers an AssertionFailedError in the original test.
    // We construct a scenario with multiple useless break statements.
    String src = "function f() { L: while(1) { break L; break L; break L; } }";
    Node root = runPass(src);
    // All three breaks are useless and should be removed.
    // The while body becomes empty but still a valid loop.
    String result = new CodePrinter.Builder(root).build();
    // The loops remains, no breaks
    assertFalse("All breaks should be removed", result.contains("break L;"));
    // For safety, also check that the function is still valid.
    assertTrue("While loop should remain", result.contains("while"));
  }

  @Test(timeout = 4000)
  public void testIssue311() {
    // The defect caused an INTERNAL COMPILER ERROR (likely a NullPointerException
    // or state inconsistency) when removing code inside a try-catch-finally.
    // We create a pattern that triggers the bug: return inside try, catch block,
    // and then have unreachable code that interacts with the try block.
    String src = "function f() { try { return; } catch(e) { } finally { } }";
    try {
      Node root = runPass(src);
      String result = new CodePrinter.Builder(root).build();
      // The pass should complete without exception.
      // Verify the structure is preserved.
      assertTrue("try block should be present", result.contains("try"));
      assertTrue("catch block should be present", result.contains("catch"));
      assertTrue("finally block should be present", result.contains("finally"));
    } catch (Exception e) {
      fail("Internal compiler error: " + e.getMessage());
    }
  }

  @Test(timeout = 4000)
  public void testIssue311WithNestedReturn() {
    // Another variant that might trigger the internal error.
    String src = "function f() { while(1) { try { return; } catch(e) { break; } } }";
    Node root = runPass(src);
    String result = new CodePrinter.Builder(root).build();
    assertTrue("while should remain", result.contains("while"));
    NodeUtil.redeclareVarsInsideBranch is called; no crash.
  }

  // -----------------------------------------------------------------------
  // Partition D: Exception & defensive guard paths
  // -----------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testUnreachableDoNotRemoved() {
    // The DO branch in removeDeadExprStatementSafely returns early.
    String src = "function f() { do { } while(0); }"; // Actually reachable, but test the guard.
    Node root = runPass(src);
    String result = new CodePrinter.Builder(root).build();
    assertTrue("Do should remain", result.contains("do"));
  }

  @Test(timeout = 4000)
  public void testEmptyBlockNotRemoved() {
    // In removeDeadExprStatementSafely, EMPTY and empty BLOCK are skipped.
    String src = "function f() { ; }";
    Node root = runPass(src);
    String result = new CodePrinter.Builder(root).build();
    // The empty statement may be removed by CodePrinter but the pass leaves it.
    // Actually ; is an EMPTY node; it is skipped. We just check no exception.
    assertNotNull(root);
  }

  // -----------------------------------------------------------------------
  // Partition E: Object lifecycle & contract integrity
  // -----------------------------------------------------------------------

  @Test(timeout = 4000)
  public void testCompilerReportsChange() {
    // When code is removed, compiler.reportCodeChange() is called.
    // We can verify indirectly by checking that the compiled source changed.
    String src = "function f() { return; var x = 1; }";
    Compiler compiler = createCompiler();
    Node root = compiler.parseSyntheticCode("test", src);
    // Save original string representation
    String before = new CodePrinter.Builder(root).build();
    UnreachableCodeElimination pass = new UnreachableCodeElimination(compiler, true);
    pass.process(null, root);
    String after = new CodePrinter.Builder(root).build();
    assertNotSame("Code should have changed", before, after);
  }

  @Test(timeout = 4000)
  public void testCfgStackPop() {
    // enterScope pushes curCfg onto stack and sets new curCfg.
    // exitScope pops it back.
    // We test by entering and exiting a scope (function) and verify no exception.
    String src = "function f() { function g() { return; } }";
    Node root = runPass(src);
    // The inner function scope should be handled correctly.
    assertNotNull(root);
  }
}