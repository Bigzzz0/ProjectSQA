/*
 * Copyright 2024 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.CheckSideEffects
 *
 * Branches & Decision Points Analyzed:
 * 1. n.isEmpty() || n.isComma() -> returns immediately without reporting or protecting.
 * 2. parent == null -> defensive null check, returns immediately.
 * 3. n.isExprResult() -> handled at child level, returns immediately.
 * 4. n.isQualifiedName() && n.getJSDocInfo() != null -> allowed no-op statement with JSDoc; returns.
 * 5. parent.getType() == Token.COMMA:
 *    a. isResultUsed == true -> returns.
 *    b. n == parent.getLastChild() -> ancestor walk checks for EXPR_RESULT or BLOCK enclosing contexts.
 *    c. n != parent.getLastChild() -> left-hand side of comma expressions must warn if side-effect free.
 * 6. parent.getType() != Token.EXPR_RESULT && parent.getType() != Token.BLOCK:
 *    - Special FOR loop branch: FOR with 4 children, where n is init (child 0) or incr (child 2).
 *    - Condition expression in FOR loop (child 1) or other parents -> returns.
 * 7. (isSimpleOp || !NodeUtil.mayHaveSideEffects(n, t.getCompiler())):
 *    - n.isString() -> specific diagnostic message: "Is there a missing '+' on the previous line?".
 *    - isSimpleOp == true -> diagnostic message: "The result of the '<op>' operator is not being used.".
 *    - otherwise -> diagnostic message: "This code lacks side-effects. Is there a bug?".
 * 8. protectSideEffectFreeCode == true vs false:
 *    - Wraps non-statement side-effect-free nodes in JSCOMPILER_PRESERVE(...) and adds synthesized extern.
 * 9. StripProtection:
 *    - Traverses AST, unwraps calls to JSCOMPILER_PRESERVE and restores original expressions.
 *
 * Defect-Targeted Ground Truth:
 * - Defects4J failure: CheckSideEffectsTest::testUselessCode (expected:<1> but was:<0>).
 *   In comma expressions assigned to a variable, e.g., 'var a = (1, 2);', NodeUtil.isExpressionResultUsed(n)
 *   evaluated to true for left children because gramps.isExprResult() was false, bypassing side-effect checks.
 */
public class CheckSideEffectsGeminiTest {

  private Compiler createCompiler() {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    return compiler;
  }

  private Node runCheck(Compiler compiler, String js, CheckLevel level, boolean protect) {
    Node root = compiler.parseTestCode(js);
    CheckSideEffects pass = new CheckSideEffects(compiler, level, protect);
    pass.process(null, root);
    return root;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testNumericLiteralHasNoSideEffectsWarning() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "10;", CheckLevel.WARNING, false);

    assertEquals(1, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
    JSError warning = compiler.getWarnings()[0];
    assertEquals(CheckSideEffects.USELESS_CODE_ERROR, warning.getType());
    assertTrue(warning.description.contains("This code lacks side-effects. Is there a bug?"));
  }

  @Test(timeout = 4000)
  public void testStringLiteralMissingPlusWarning() {
    Compiler compiler = createCompiler();
    // Two adjacent string statements parsed as separate statements
    runCheck(compiler, "var s = 'part1'; 'part2';", CheckLevel.WARNING, false);

    assertEquals(1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    assertTrue(warning.description.contains("Is there a missing '+' on the previous line?"));
  }

  @Test(timeout = 4000)
  public void testSimpleOperatorEqualityWarning() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "x == 10;", CheckLevel.WARNING, false);

    assertEquals(1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    assertTrue(warning.description.contains("The result of the 'eq' operator is not being used."));
  }

  @Test(timeout = 4000)
  public void testSimpleOperatorArithmeticWarning() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "x + 10;", CheckLevel.WARNING, false);

    assertEquals(1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    assertTrue(warning.description.contains("The result of the 'add' operator is not being used."));
  }

  @Test(timeout = 4000)
  public void testValidSideEffectsProduceNoWarnings() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "var x = 10; x++; foo();", CheckLevel.WARNING, false);

    assertEquals(0, compiler.getWarningCount());
    assertEquals(0, compiler.getErrorCount());
  }

  @Test(timeout = 4000)
  public void testCheckLevelErrorReportsAsError() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "10;", CheckLevel.ERROR, false);

    assertEquals(0, compiler.getWarningCount());
    assertEquals(1, compiler.getErrorCount());
    assertEquals(CheckSideEffects.USELESS_CODE_ERROR, compiler.getErrors()[0].getType());
  }

  @Test(timeout = 4000)
  public void testProtectionModeWrapsProblemNodeAndAddsExtern() {
    Compiler compiler = createCompiler();
    Node root = runCheck(compiler, "10;", CheckLevel.WARNING, true);

    assertEquals(1, compiler.getWarningCount());
    Node exprResult = root.getFirstChild();
    assertTrue(exprResult.isExprResult());
    Node callNode = exprResult.getFirstChild();
    assertTrue("Node should be replaced by a CALL", callNode.isCall());
    assertTrue(callNode.getBooleanProp(Node.FREE_CALL));

    Node callee = callNode.getFirstChild();
    assertTrue(callee.isName());
    assertEquals(CheckSideEffects.PROTECTOR_FN, callee.getString());
    assertTrue(callee.getBooleanProp(Node.IS_CONSTANT_NAME));

    Node argument = callNode.getLastChild();
    assertTrue(argument.isNumber());
    assertEquals(10.0, argument.getDouble(), 0.0);

    // Verify synthesized extern input contains protector declaration
    CompilerInput externInput = compiler.getSynthesizedExternsInput();
    assertNotNull(externInput);
    Node externAst = externInput.getAstRoot(compiler);
    assertNotNull(externAst);
    boolean foundExtern = false;
    for (Node child = externAst.getFirstChild(); child != null; child = child.getNext()) {
      if (child.isVar() && child.getFirstChild().getString().equals(CheckSideEffects.PROTECTOR_FN)) {
        foundExtern = true;
        break;
      }
    }
    assertTrue("Protector function must be recorded in synthesized externs", foundExtern);
  }

  @Test(timeout = 4000)
  public void testStripProtectionRestoresOriginalNode() {
    Compiler compiler = createCompiler();
    Node root = runCheck(compiler, "10;", CheckLevel.WARNING, true);

    // Verify it is protected
    Node exprResult = root.getFirstChild();
    assertTrue(exprResult.getFirstChild().isCall());

    // Run StripProtection
    CheckSideEffects.StripProtection stripPass = new CheckSideEffects.StripProtection(compiler);
    stripPass.process(null, root);

    // Verify unwrap
    assertTrue("Node must be unwrapped back to NUMBER", exprResult.getFirstChild().isNumber());
    assertEquals(10.0, exprResult.getFirstChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testStripProtectionIgnoresNormalCalls() {
    Compiler compiler = createCompiler();
    Node root = compiler.parseTestCode("foo(10);");

    CheckSideEffects.StripProtection stripPass = new CheckSideEffects.StripProtection(compiler);
    stripPass.process(null, root);

    Node exprResult = root.getFirstChild();
    assertTrue(exprResult.getFirstChild().isCall());
    assertEquals("foo", exprResult.getFirstChild().getFirstChild().getString());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyStatementHasNoWarning() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "foo();;", CheckLevel.WARNING, false);

    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testQualifiedNameWithJSDocHasNoWarning() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "/** @type {number} */ x;", CheckLevel.WARNING, false);

    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testQualifiedNameWithoutJSDocHasWarning() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "x;", CheckLevel.WARNING, false);

    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testForLoopInitExpressionUseless() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "for (1; x < 10; x++) {}", CheckLevel.WARNING, false);

    assertEquals(1, compiler.getWarningCount());
    assertTrue(compiler.getWarnings()[0].description.contains("This code lacks side-effects"));
  }

  @Test(timeout = 4000)
  public void testForLoopIncrExpressionUseless() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "for (x = 1; x < 10; 1) {}", CheckLevel.WARNING, false);

    assertEquals(1, compiler.getWarningCount());
    assertTrue(compiler.getWarnings()[0].description.contains("This code lacks side-effects"));
  }

  @Test(timeout = 4000)
  public void testForLoopConditionNotFlagged() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "for (x = 1; 1; x++) {}", CheckLevel.WARNING, false);

    // Condition is used for branching, should not be flagged by CheckSideEffects
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testForLoopEmptyClausesNotFlagged() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "for ( ; ; ) {}", CheckLevel.WARNING, false);

    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testCommaExpressionInExprResult() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "(1, 2);", CheckLevel.WARNING, false);

    // In (1, 2); both 1 and 2 lack side-effects and are unused
    assertEquals(2, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testCommaExpressionWithSideEffectsOnRight() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "true, foo();", CheckLevel.WARNING, false);

    assertEquals(1, compiler.getWarningCount());
    assertTrue(compiler.getWarnings()[0].description.contains("This code lacks side-effects"));
  }

  @Test(timeout = 4000)
  public void testCommaExpressionWithSideEffectsOnLeft() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "foo(), true;", CheckLevel.WARNING, false);

    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testCommaExpressionUsedInIfConditionNotFlagged() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "if ((foo(), true)) {}", CheckLevel.WARNING, false);

    // The result 'true' is used by the IF branch condition
    assertEquals(0, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets the defect where useless expressions in a comma operator within a variable
   * declaration / assignment (e.g. 'var a = (1, 2);') were incorrectly considered as
   * having their result used, leading to expected:<1> but was:<0>.
   */
  @Test(timeout = 4000)
  public void testDefectUselessCodeInCommaAssignment() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "var a = (1, 2);", CheckLevel.WARNING, false);

    // '1' is side-effect free and its result is discarded by comma operator
    assertEquals("Useless code in comma assignment must generate 1 warning", 1, compiler.getWarningCount());
    assertEquals(CheckSideEffects.USELESS_CODE_ERROR, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testDefectMultipleUselessCodeInCommaAssignment() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "var a = (1, 2, 3);", CheckLevel.WARNING, false);

    // '1' and '2' are side-effect free and unused
    assertEquals(2, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testDefectSideEffectFollowedByUselessCodeInCommaAssignment() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "var a = (foo(), 1, 2);", CheckLevel.WARNING, false);

    // foo() has side-effects, 2 is assigned to a, but '1' is useless
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testDefectNestedCommaInAssignment() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "var a = (foo(), (1, 2));", CheckLevel.WARNING, false);

    assertEquals(1, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testDirectVisitWithNullParentDoesNotThrow() {
    Compiler compiler = createCompiler();
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    NodeTraversal t = new NodeTraversal(compiler, pass);
    Node numNode = IR.number(42);

    // Should return safely when parent is null
    pass.visit(t, numNode, null);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testDirectVisitWithEmptyNodeDoesNotThrow() {
    Compiler compiler = createCompiler();
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    NodeTraversal t = new NodeTraversal(compiler, pass);
    Node emptyNode = IR.empty();
    Node block = IR.block();

    pass.visit(t, emptyNode, block);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testDirectVisitWithCommaNodeDoesNotThrow() {
    Compiler compiler = createCompiler();
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    NodeTraversal t = new NodeTraversal(compiler, pass);
    Node comma = IR.comma(IR.number(1), IR.number(2));
    Node expr = IR.exprResult(comma);

    pass.visit(t, comma, expr);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testDirectVisitWithExprResultNodeDoesNotThrow() {
    Compiler compiler = createCompiler();
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    NodeTraversal t = new NodeTraversal(compiler, pass);
    Node expr = IR.exprResult(IR.number(1));
    Node block = IR.block();

    pass.visit(t, expr, block);
    assertEquals(0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testStripProtectionWithNonNameCallTargetDoesNotThrow() {
    Compiler compiler = createCompiler();
    // (function(){})() creates a call whose target is a FUNCTION, not a NAME
    Node root = compiler.parseTestCode("(function(){})();");

    CheckSideEffects.StripProtection stripPass = new CheckSideEffects.StripProtection(compiler);
    stripPass.process(null, root);

    Node exprResult = root.getFirstChild();
    assertTrue(exprResult.getFirstChild().isCall());
    assertTrue(exprResult.getFirstChild().getFirstChild().isFunction());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testDiagnosticTypeAndConstants() {
    assertNotNull(CheckSideEffects.USELESS_CODE_ERROR);
    assertEquals("JSC_USELESS_CODE", CheckSideEffects.USELESS_CODE_ERROR.key);
    assertEquals("JSCOMPILER_PRESERVE", CheckSideEffects.PROTECTOR_FN);
  }

  @Test(timeout = 4000)
  public void testHotSwapScriptExecutesTraversal() {
    Compiler compiler = createCompiler();
    Node scriptRoot = compiler.parseTestCode("10;");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);

    pass.hotSwapScript(scriptRoot, null);
    assertEquals(1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testProtectionPassWithNoProblemsDoesNotAddExtern() {
    Compiler compiler = createCompiler();
    runCheck(compiler, "var x = 10;", CheckLevel.WARNING, true);

    assertEquals(0, compiler.getWarningCount());
    // Since problemNodes is empty, no extern should be created
    CompilerInput externInput = compiler.getSynthesizedExternsInput();
    Node externAst = externInput.getAstRoot(compiler);
    for (Node child = externAst.getFirstChild(); child != null; child = child.getNext()) {
      if (child.isVar() && child.getFirstChild().getString().equals(CheckSideEffects.PROTECTOR_FN)) {
        fail("JSCOMPILER_PRESERVE should not be added when no problem nodes exist");
      }
    }
  }
}