package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link DeadAssignmentsElimination}.
 * 
 * <p>This test suite targets the known defect described in issue 297, where
 * dead assignment elimination incorrectly removes assignments that are read
 * within the same expression (e.g., logical AND). It also covers other branches
 * of the code to improve line and branch coverage.</p>
 *
 * <p>Branch and Defect Analysis Matrix:</p>
 * <ul>
 *   <li>Part A: Core functional logic – basic removal, global scope skip, inner function skip.</li>
 *   <li>Part B: Boundary values – null/empty inputs, no assign ops.</li>
 *   <li>Part C: Defect-targeted – assignments with reads before kills (issue 297).</li>
 *   <li>Part D: Exception paths – not applicable (no public API).</li>
 *   <li>Part E: Lifecycle – covered via compiler integration.</li>
 * </ul>
 */
public class DeadAssignmentsEliminationDeepseekTest extends CompilerTestCase {

  @Override
  protected CompilerPass getProcessor(Compiler compiler) {
    return new DeadAssignmentsElimination(compiler);
  }

  @Test(timeout = 4000)
  public void testIssue297a() {
    // The assignment a = 1 inside the condition is not dead because it is read
    // by (b = a). It must not be removed.
    test("function f() { var a = 0, b; if (a = 1 && (b = a)) {} return b; }",
         "function f() { var a = 0, b; if (a = 1 && (b = a)) {} return b; }");
  }

  @Test(timeout = 4000)
  public void testIssue297b() {
    // Variant with assignment on the right after left assignment.
    test("function f() { var a = 0, b; if (a = (b = a, 1)) {} return b; }",
         "function f() { var a = 0, b; if (a = (b = a, 1)) {} return b; }");
  }

  @Test(timeout = 4000)
  public void testIssue297c() {
    // Using the variable in a nested expression before overwrite.
    test("function f() { var a = 0, b; if (a = (a = 1, 2)) {} return a; }",
         "function f() { var a = 0, b; if (a = (a = 1, 2)) {} return a; }");
  }

  @Test(timeout = 4000)
  public void testBasicDeadAssignmentElimination() {
    test("function f() { var a; a = 1; a = 2; return a; }",
         "function f() { var a; a = 2; return a; }");
  }

  @Test(timeout = 4000)
  public void testGlobalScopeNotProcessed() {
    test("var a; a = 1; a = 2;", "var a; a = 1; a = 2;");
  }

  @Test(timeout = 4000)
  public void testInnerFunctionCausesSkip() {
    test("function f() { var a; a = 1; a = 2; function g() {} }",
         "function f() { var a; a = 1; a = 2; function g() {} }");
  }

  @Test(timeout = 4000)
  public void testNoAssignOpsSkips() {
    test("function f() { var a; return a; }",
         "function f() { var a; return a; }");
  }

  @Test(timeout = 4000)
  public void testVariableLiveOnExitNotRemoved() {
    test("function f() { var a; a = 1; return a; }",
         "function f() { var a; a = 1; return a; }");
  }

  @Test(timeout = 4000)
  public void testWhileLoopCondition() {
    test("function f() { var a = 0; while (a = 1) { break; } }",
         "function f() { var a = 0; while (1) { break; } }");
  }

  @Test(timeout = 4000)
  public void testDoWhileLoopCondition() {
    test("function f() { var a = 0; do { } while (a = 1); }",
         "function f() { var a = 0; do { } while (1); }");
  }

  @Test(timeout = 4000)
  public void testSwitchCondition() {
    test("function f() { var a = 0; switch (a = 1) { case 1: break; } }",
         "function f() { var a = 0; switch (1) { case 1: break; } }");
  }

  @Test(timeout = 4000)
  public void testIfConditionRemoval() {
    test("function f() { var a; a = 1; if (a = 2) {} a = 3; }",
         "function f() { var a; a = 1; if (2) {} a = 3; }");
  }
}