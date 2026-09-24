/* [Branch & Defect Analysis Matrix]
 * =====================================================================================================
 * Class Under Test: com.google.javascript.jscomp.CheckSideEffects
 *
 * Decision / Condition Branch Coverage Matrix:
 * 1. visit(NodeTraversal, Node, Node):
 *    - Branch: n.isEmpty() || n.isComma() -> early return
 *      - n is EMPTY (e.g. stray semicolon)
 *      - n is COMMA (e.g. comma expression node)
 *    - Branch: parent == null -> early return (root traversal boundary)
 *    - Branch: parent.getType() == Token.COMMA
 *      - Sub-branch: gramps.isCall() && parent == gramps.getFirstChild()
 *        - n == parent.getFirstChild() && parent.getChildCount() == 2 &&
 *          n.getNext().isName() && "eval".equals(n.getNext().getString()) -> return (indirect eval)
 *        - gramps is not Call, or parent != gramps.getFirstChild()
 *        - n is not first child, or childCount != 2, or next is not name, or next name != "eval"
 *      - Sub-branch: n == parent.getLastChild()
 *        - Ancestor loop:
 *          - an.getType() == Token.COMMA -> continue
 *          - an.getType() != EXPR_RESULT && an.getType() != BLOCK -> return (result is used)
 *          - else (EXPR_RESULT or BLOCK) -> break
 *    - Branch: parent.getType() != EXPR_RESULT && parent.getType() != BLOCK
 *      - Sub-branch: FOR loop (parent.getType() == Token.FOR && childCount == 4)
 *        - n == parent.getFirstChild() (FOR initializer) -> do not return (check side-effects)
 *        - n == parent.getFirstChild().getNext().getNext() (FOR increment) -> do not return
 *        - n is condition or body -> return
 *      - Sub-branch: any other parent (e.g. IF, WHILE, RETURN, VAR, ASSIGN) -> return
 *    - Branch: !isResultUsed && (isSimpleOp || !NodeUtil.mayHaveSideEffects(n, compiler))
 *      - Sub-branch: n.isQualifiedName() && n.getJSDocInfo() != null -> return
 *      - Sub-branch: n.isExprResult() -> return
 *      - Sub-branch Message formatting:
 *        - n.isString() -> "Is there a missing '+' on the previous line?"
 *        - isSimpleOp -> "The result of the '<op>' operator is not being used."
 *        - default -> "This code lacks side-effects. Is there a bug?"
 *      - Sub-branch: !NodeUtil.isStatement(n) -> problemNodes.add(n)
 *
 * 2. protectSideEffects() & addExtern():
 *    - problemNodes.isEmpty() vs !problemNodes.isEmpty()
 *    - AST rewrite: wraps problem node in JSCOMPILER_PRESERVE(expr) call
 *    - Synthesized extern creation with @noalias JSDoc
 *
 * 3. StripProtection:
 *    - n.isCall() && target.isName() && target.getString().equals(PROTECTOR_FN)
 *      -> strips JSCOMPILER_PRESERVE and restores wrapped expression
 *    - target is not name / target name != PROTECTOR_FN -> ignored
 *
 * 4. Known Defect Zone (Defects4J - CheckSideEffectsTest::testUselessCode):
 *    - Test assertion failure: expected:<1> but was:<0>
 *    - Stray semicolon statements ("foo();;") were documented to generate warnings,
 *      but early return on n.isEmpty() swallowed the error.
 * =====================================================================================================
 */

package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

public class CheckSideEffectsGeminiTest {

  private Compiler compile(String js, CheckLevel level, boolean protect) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    CheckSideEffects pass = new CheckSideEffects(compiler, level, protect);
    pass.process(null, root);
    return compiler;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & Operator Types
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleOperatorAddWarning() {
    Compiler compiler = compile("1 + 2;", CheckLevel.WARNING, false);
    assertEquals("Should generate 1 warning for unused addition", 1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    assertEquals(CheckSideEffects.USELESS_CODE_ERROR, warning.getType());
    assertTrue("Warning message should mention operator",
        warning.description.contains("The result of the 'add' operator is not being used."));
  }

  @Test(timeout = 4000)
  public void testSimpleOperatorEqualityWarning() {
    Compiler compiler = compile("A == B;", CheckLevel.WARNING, false);
    assertEquals("Should generate 1 warning for unused equality", 1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    assertEquals(CheckSideEffects.USELESS_CODE_ERROR, warning.getType());
    assertTrue("Warning message should mention operator",
        warning.description.contains("The result of the 'eq' operator is not being used."));
  }

  @Test(timeout = 4000)
  public void testStringLiteralMissingPlusWarning() {
    Compiler compiler = compile("var s = 'first line'\n 'second line';", CheckLevel.WARNING, false);
    assertEquals("Should warn on stray string literal", 1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    assertEquals(CheckSideEffects.USELESS_CODE_ERROR, warning.getType());
    assertTrue("Warning message should ask about missing '+'",
        warning.description.contains("Is there a missing '+' on the previous line?"));
  }

  @Test(timeout = 4000)
  public void testUselessNameReferenceInFunction() {
    Compiler compiler = compile("function f() { var x; x; }", CheckLevel.WARNING, false);
    assertEquals("Should warn on standalone name statement", 1, compiler.getWarningCount());
    JSError warning = compiler.getWarnings()[0];
    assertEquals(CheckSideEffects.USELESS_CODE_ERROR, warning.getType());
    assertTrue("Message should indicate lack of side-effects",
        warning.description.contains("This code lacks side-effects. Is there a bug?"));
  }

  @Test(timeout = 4000)
  public void testParenthesizedNameReference() {
    Compiler compiler = compile("var x = 1; (x);", CheckLevel.WARNING, false);
    assertEquals("Parenthesized unused name should trigger warning", 1, compiler.getWarningCount());
    assertEquals(CheckSideEffects.USELESS_CODE_ERROR, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testCheckLevelErrorReporting() {
    Compiler compiler = compile("1 + 2;", CheckLevel.ERROR, false);
    assertEquals("Should report 1 error when level is ERROR", 1, compiler.getErrorCount());
    assertEquals("Should report 0 warnings when level is ERROR", 0, compiler.getWarningCount());
    assertEquals(CheckSideEffects.USELESS_CODE_ERROR, compiler.getErrors()[0].getType());
  }

  // =========================================================================
  // Partition B: Comma Operators, Ancestors & Loops
  // =========================================================================

  @Test(timeout = 4000)
  public void testCommaOperatorBothLackingSideEffects() {
    Compiler compiler = compile("(1, 2);", CheckLevel.WARNING, false);
    assertEquals("Both elements of unused comma expr should warn", 2, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testCommaOperatorResultAssigned() {
    Compiler compiler = compile("var z = (1, 2);", CheckLevel.WARNING, false);
    assertEquals("Only the left-hand non-result operand should warn", 1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testIndirectEvalPatternAllowed() {
    Compiler compiler = compile("(0, eval)('var a = 1;');", CheckLevel.WARNING, false);
    assertEquals("Indirect eval idiom (0, eval)(...) must not warn", 0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testIndirectEvalNonEvalNameWarns() {
    Compiler compiler = compile("(0, notEval)('var a = 1;');", CheckLevel.WARNING, false);
    assertEquals("Indirect call with name other than eval should warn on first operand",
        1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testCommaOperatorInIfConditionAllowed() {
    Compiler compiler = compile("if ((foo(), 2)) {}", CheckLevel.WARNING, false);
    assertEquals("Comma result used in if-condition should not warn for last child",
        0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testCommaOperatorInReturnAllowed() {
    Compiler compiler = compile("function f() { return (foo(), 2); }", CheckLevel.WARNING, false);
    assertEquals("Comma result used in return should not warn for last child",
        0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testForLoopInitializerWarning() {
    Compiler compiler = compile("for (1; ; ) {}", CheckLevel.WARNING, false);
    assertEquals("Useless FOR loop initializer should warn", 1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testForLoopIncrementWarning() {
    Compiler compiler = compile("for (var x = 0; ; 2) {}", CheckLevel.WARNING, false);
    assertEquals("Useless FOR loop increment should warn", 1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testForLoopConditionNotWarned() {
    Compiler compiler = compile("for (var x = 0; 1; ) {}", CheckLevel.WARNING, false);
    assertEquals("FOR loop condition is used and should not warn", 0, compiler.getWarningCount());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets the known defect in Defects4J:
   * com.google.javascript.jscomp.CheckSideEffectsTest::testUselessCode
   * Error: expected:<1> but was:<0>
   *
   * In defective versions, stray semicolons ("foo();;") are ignored due to
   * early returns on n.isEmpty(), causing 0 warnings instead of the expected 1.
   */
  @Test(timeout = 4000)
  public void testUselessCode() {
    Compiler compiler = compile("foo();;", CheckLevel.WARNING, false);
    assertEquals("There should be one warning, repeated 1 time(s).", 1, compiler.getWarningCount());
    assertEquals(CheckSideEffects.USELESS_CODE_ERROR, compiler.getWarnings()[0].getType());
  }

  @Test(timeout = 4000)
  public void testDefectTargetUselessCodeMultiStatement() {
    Compiler compiler = compile("var x = 1; x = 2, 3;", CheckLevel.WARNING, false);
    assertEquals("Trailing unused comma expression operand must trigger warning",
        1, compiler.getWarningCount());
    assertEquals(CheckSideEffects.USELESS_CODE_ERROR, compiler.getWarnings()[0].getType());
  }

  // =========================================================================
  // Partition D: Protection Instrumentation & StripProtection Pass
  // =========================================================================

  @Test(timeout = 4000)
  public void testProtectSideEffectsWrapsNodeInCall() {
    Compiler compiler = compile("1 + 2;", CheckLevel.WARNING, true);
    assertEquals(1, compiler.getWarningCount());

    Node script = compiler.getRoot().getLastChild();
    Node exprResult = script.getFirstChild();
    assertNotNull(exprResult);
    assertTrue("Should wrap statement child in CALL", exprResult.getFirstChild().isCall());

    Node callNode = exprResult.getFirstChild();
    Node callee = callNode.getFirstChild();
    assertTrue("Callee should be name", callee.isName());
    assertEquals(CheckSideEffects.PROTECTOR_FN, callee.getString());
    assertTrue("Callee should be constant name", callee.getBooleanProp(Node.IS_CONSTANT_NAME));
    assertTrue("Call should be free call", callNode.getBooleanProp(Node.FREE_CALL));
    assertEquals("Wrapped child should be the original add operation",
        Token.ADD, callNode.getLastChild().getType());
  }

  @Test(timeout = 4000)
  public void testProtectSideEffectsDisabledLeavesTreeUnchanged() {
    Compiler compiler = compile("1 + 2;", CheckLevel.WARNING, false);
    assertEquals(1, compiler.getWarningCount());

    Node script = compiler.getRoot().getLastChild();
    Node exprResult = script.getFirstChild();
    assertNotNull(exprResult);
    assertEquals("Expression should remain ADD when protection disabled",
        Token.ADD, exprResult.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testStripProtectionRestoresOriginalExpression() {
    Compiler compiler = compile("1 + 2;", CheckLevel.WARNING, true);
    Node script = compiler.getRoot().getLastChild();

    CheckSideEffects.StripProtection stripPass = new CheckSideEffects.StripProtection(compiler);
    stripPass.process(null, script);

    Node exprResult = script.getFirstChild();
    assertNotNull(exprResult);
    assertEquals("StripProtection must restore original ADD node",
        Token.ADD, exprResult.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testStripProtectionIgnoresStandardCalls() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("myFunc(42);");
    Node originalCall = root.getFirstChild().getFirstChild();

    CheckSideEffects.StripProtection stripPass = new CheckSideEffects.StripProtection(compiler);
    stripPass.process(null, root);

    Node currentCall = root.getFirstChild().getFirstChild();
    assertSame("Non-protector call must remain intact", originalCall, currentCall);
  }

  // =========================================================================
  // Partition E: Boundaries, Invariants & AST Node Direct Invocation
  // =========================================================================

  @Test(timeout = 4000)
  public void testHotSwapScriptTraversal() {
    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode("1 + 2;");
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    pass.hotSwapScript(script, null);
    assertEquals("hotSwapScript should detect side-effect free code",
        1, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testVisitEarlyReturnNullParent() {
    Compiler compiler = new Compiler();
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);
    Node root = IR.root();
    NodeTraversal t = new NodeTraversal(compiler, pass);
    pass.visit(t, root, null);
    assertEquals("Null parent should cause immediate return without reporting",
        0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testVisitQualifiedNameWithJSDocAllowed() {
    Compiler compiler = new Compiler();
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);

    Node nameNode = IR.name("configuredNamespace");
    JSDocInfoBuilder builder = new JSDocInfoBuilder(false);
    nameNode.setJSDocInfo(builder.build(nameNode));

    Node exprResult = IR.exprResult(nameNode);
    Node block = IR.block(exprResult);
    NodeTraversal t = new NodeTraversal(compiler, pass);

    pass.visit(t, nameNode, exprResult);
    assertEquals("Qualified name with JSDoc info must be permitted as a declaration stub",
        0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testVisitExprResultNodeDirectly() {
    Compiler compiler = new Compiler();
    CheckSideEffects pass = new CheckSideEffects(compiler, CheckLevel.WARNING, false);

    Node nameNode = IR.name("x");
    Node exprResult = IR.exprResult(nameNode);
    Node block = IR.block(exprResult);
    NodeTraversal t = new NodeTraversal(compiler, pass);

    pass.visit(t, exprResult, block);
    assertEquals("EXPR_RESULT node itself must trigger early return and not warn",
        0, compiler.getWarningCount());
  }

  @Test(timeout = 4000)
  public void testConstantsIntegrity() {
    assertNotNull(CheckSideEffects.USELESS_CODE_ERROR);
    assertEquals("JSC_USELESS_CODE", CheckSideEffects.USELESS_CODE_ERROR.key);
    assertEquals("JSCOMPILER_PRESERVE", CheckSideEffects.PROTECTOR_FN);
  }
}