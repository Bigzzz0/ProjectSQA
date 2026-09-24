package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import org.junit.Test;

/**
 * White-box JUnit 4 tests for {@link RemoveConstantExpressions}.
 *
 * [Branch & Defect Analysis Matrix]
 * - EXPR_RESULT statements are the only simplification trigger.
 * - PRESERVED branches: CALL, NEW, ASSIGN, INC, DELPROP, and any node type
 *   that NodeUtil.nodeTypeMayHaveSideEffects() considers side-effecting.
 * - REPLACED branches: constant-only expressions (NUMBER, NAME, STRING),
 *   and expressions containing extractable side-effect subexpressions.
 * - Defect targets from ground truth:
 *     * RemoveConstantExpressionsTest::testCall1 => calls must be preserved.
 *     * RemoveConstantExpressionsTest::testNew1  => new expressions must be preserved.
 *   Both direct and nested call/new subexpressions are covered so any
 *   regression in side-effect collection is revealed.
 */
public class RemoveConstantExpressionsDeepseekTest {

  private static final class Processed {
    final Compiler compiler;
    final Node root;

    Processed(Compiler compiler, Node root) {
      this.compiler = compiler;
      this.root = root;
    }
  }

  private static Processed process(String js) {
    Compiler compiler = new Compiler();
    compiler.initOptions(new CompilerOptions());
    Node root = compiler.parseTestCode(js);
    new RemoveConstantExpressions(compiler).process(null, root);
    return new Processed(compiler, root);
  }

  private static int childCount(Node n) {
    int count = 0;
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      count++;
    }
    return count;
  }

  private static void assertExprResult(Node stmt, int childType) {
    assertNotNull("Expected an EXPR_RESULT statement", stmt);
    assertEquals(Token.EXPR_RESULT, stmt.getType());
    assertNotNull(stmt.getFirstChild());
    assertEquals(childType, stmt.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testCall1() {
    Processed p = process("foo();");
    assertEquals(1, childCount(p.root));
    assertExprResult(p.root.getFirstChild(), Token.CALL);
  }

  @Test(timeout = 4000)
  public void testNew1() {
    Processed p = process("new Foo();");
    assertEquals(1, childCount(p.root));
    assertExprResult(p.root.getFirstChild(), Token.NEW);
  }

  @Test(timeout = 4000)
  public void testCallWithConstantArgumentPreserved() {
    Processed p = process("foo(1 + 2);");
    assertEquals(1, childCount(p.root));
    assertExprResult(p.root.getFirstChild(), Token.CALL);
  }

  @Test(timeout = 4000)
  public void testNewWithArgumentsPreserved() {
    Processed p = process("new Foo(1 + 2);");
    assertEquals(1, childCount(p.root));
    assertExprResult(p.root.getFirstChild(), Token.NEW);
  }

  @Test(timeout = 4000)
  public void testNoSideEffectConstantRemoved() {
    Processed p = process("1;");
    assertEquals(0, childCount(p.root));
    assertTrue(p.compiler.hasChanged());
  }

  @Test(timeout = 4000)
  public void testStringConstantRemoved() {
    Processed p = process("'x';");
    assertEquals(0, childCount(p.root));
    assertTrue(p.compiler.hasChanged());
  }

  @Test(timeout = 4000)
  public void testNameReferenceRemoved() {
    Processed p = process("x;");
    assertEquals(0, childCount(p.root));
    assertTrue(p.compiler.hasChanged());
  }

  @Test(timeout = 4000)
  public void testSingleCallInAdditionSimplifiesToOneStatement() {
    Processed p = process("1 + foo();");
    assertEquals(1, childCount(p.root));
    assertExprResult(p.root.getFirstChild(), Token.CALL);
    assertTrue(p.compiler.hasChanged());
  }

  @Test(timeout = 4000)
  public void testSingleNewInAdditionSimplifiesToOneStatement() {
    Processed p = process("1 + new Foo();");
    assertEquals(1, childCount(p.root));
    assertExprResult(p.root.getFirstChild(), Token.NEW);
    assertTrue(p.compiler.hasChanged());
  }

  @Test(timeout = 4000)
  public void testTwoCallsInAdditionBecomeTwoStatements() {
    Processed p = process("foo() + bar();");
    assertEquals(2, childCount(p.root));
    assertExprResult(p.root.getFirstChild(), Token.CALL);
    assertExprResult(p.root.getFirstChild().getNext(), Token.CALL);
  }

  @Test(timeout = 4000)
  public void testCallAndNewInAdditionBecomeStatements() {
    Processed p = process("foo() + new Bar();");
    assertEquals(2, childCount(p.root));
    assertExprResult(p.root.getFirstChild(), Token.CALL);
    assertExprResult(p.root.getFirstChild().getNext(), Token.NEW);
  }

  @Test(timeout = 4000)
  public void testAssignmentPreserved() {
    Processed p = process("x = 1;");
    assertEquals(1, childCount(p.root));
    assertExprResult(p.root.getFirstChild(), Token.ASSIGN);
  }

  @Test(timeout = 4000)
  public void testIncrementPreserved() {
    Processed p = process("x++;");
    assertEquals(1, childCount(p.root));
    assertExprResult(p.root.getFirstChild(), Token.INC);
  }

  @Test(timeout = 4000)
  public void testDeletePreserved() {
    Processed p = process("delete x;");
    assertEquals(1, childCount(p.root));
    assertExprResult(p.root.getFirstChild(), Token.DELPROP);
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationIgnored() {
    Processed p = process("function f(){}");
    assertEquals(1, childCount(p.root));
    assertEquals(Token.FUNCTION, p.root.getFirstChild().getType());
    assertFalse(p.compiler.hasChanged());
  }

  @Test(timeout = 4000)
  public void testEmptyStatementIgnored() {
    Processed p = process(";");
    assertEquals(1, childCount(p.root));
    assertFalse(p.compiler.hasChanged());
  }

  @Test(timeout = 4000)
  public void testEmptyInputNoCrash() {
    Processed p = process("");
    assertEquals(0, childCount(p.root));
    assertFalse(p.compiler.hasChanged());
  }

  @Test(timeout = 4000)
  public void testMultipleStatementsPartiallySimplified() {
    Processed p = process("1; foo(); 2;");
    assertEquals(1, childCount(p.root));
    assertExprResult(p.root.getFirstChild(), Token.CALL);
  }

  @Test(timeout = 4000)
  public void testNestedFunctionBodySimplified() {
    Processed p = process("function f(){1; foo();}");
    Node function = p.root.getFirstChild();
    Node body = function.getLastChild();
    assertNotNull(body);
    assertEquals(1, childCount(body));
    assertExprResult(body.getFirstChild(), Token.CALL);
  }

  @Test(timeout = 4000)
  public void testSideEffectInsideNestedCallPreserved() {
    Processed p = process("foo(bar());");
    assertEquals(1, childCount(p.root));
    assertExprResult(p.root.getFirstChild(), Token.CALL);
    Node call = p.root.getFirstChild().getFirstChild();
    assertEquals(Token.CALL, call.getFirstChild().getType());
  }
}