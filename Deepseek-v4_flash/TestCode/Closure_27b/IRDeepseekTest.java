package com.google.javascript.rhino;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Known defect (Issue 727):
 *   IR.tryFinally(Node, Node) validates tryBody/finallyBody with isLabelName()
 *   instead of isBlock(). Valid BLOCK arguments therefore throw
 *   IllegalStateException, while invalid LABEL_NAME arguments are accepted.
 *
 * Fault-revealing tests:
 *   - testIssue727_1/2/3: pass real BLOCK children to IR.tryFinally; the buggy
 *     implementation throws before constructing the TRY node.
 *   - testTryFinallyRejectsLabelNames: passes LABEL_NAME children; the buggy
 *     implementation accepts them, while the fixed implementation rejects them.
 *
 * Coverage partitions:
 *   A. Core functional construction of every public IR factory.
 *   B. Boundary values: empty collections, empty blocks, invalid labels, null-ish
 *      AST shapes, and helper predicates.
 *   C. Defect-targeted branch zone: tryFinally and try/catch/finally builders.
 *   D. Defensive guards: all Preconditions.checkState failure paths.
 *   E. Object contract integrity: child identity, token types, and child counts.
 */
public class IRDeepseekTest {

  private static void assertType(int expected, Node actual) {
    assertNotNull(actual);
    assertEquals(expected, actual.getType());
  }

  private static void assertIllegalState(Runnable action) {
    try {
      action.run();
      fail("Expected IllegalStateException");
    } catch (IllegalStateException expected) {
      // Expected defensive precondition failure.
    }
  }

  @Test(timeout = 4000)
  public void testCoreEmptyBlockScript() {
    assertType(Token.EMPTY, IR.empty());
    Node block = IR.block();
    assertType(Token.BLOCK, block);
    assertFalse(block.hasChildren());
    Node script = IR.script();
    assertType(Token.SCRIPT, script);
    assertFalse(script.hasChildren());
  }

  @Test(timeout = 4000)
  public void testFunctionAndParamLists() {
    Node p0 = IR.paramList();
    assertType(Token.PARAM_LIST, p0);
    assertEquals(0, p0.getChildCount());

    Node p1 = IR.paramList(IR.name("a"));
    assertType(Token.PARAM_LIST, p1);
    assertEquals(1, p1.getChildCount());

    Node p2 = IR.paramList(IR.name("a"), IR.name("b"));
    assertType(Token.PARAM_LIST, p2);
    assertEquals(2, p2.getChildCount());

    List<Node> params = Arrays.asList(IR.name("x"), IR.name("y"));
    Node pList = IR.paramList(params);
    assertType(Token.PARAM_LIST, pList);
    assertEquals(2, pList.getChildCount());

    Node name = IR.name("f");
    Node body = IR.block(IR.returnNode(IR.name("a")));
    Node fn = IR.function(name, params, body);
    assertType(Token.FUNCTION, fn);
    assertEquals(3, fn.getChildCount());
    assertSame(name, fn.getFirstChild());
    assertSame(params, fn.getFirstChild().getNext());
    assertSame(body, fn.getLastChild());
  }

  @Test(timeout = 4000)
  public void testBlockScriptVarargs() {
    Node b = IR.block(IR.returnNode(), IR.breakNode());
    assertType(Token.BLOCK, b);
    assertEquals(2, b.getChildCount());

    Node s = IR.script(IR.returnNode(), IR.empty());
    assertType(Token.SCRIPT, s);
    assertEquals(2, s.getChildCount());
  }

  @Test(timeout = 4000)
  public void testVarForms() {
    Node v = IR.var(IR.name("x"));
    assertType(Token.VAR, v);
    assertTrue(v.getFirstChild().isName());

    Node name = IR.name("y");
    Node value = IR.number(0);
    Node varWithInit = IR.var(name, value);
    assertType(Token.VAR, varWithInit);
    assertSame(name, varWithInit.getFirstChild());
    assertSame(value, name.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testReturnThrowExprResult() {
    assertType(Token.RETURN, IR.returnNode());

    Node expr = IR.name("x");
    Node ret = IR.returnNode(expr);
    assertType(Token.RETURN, ret);
    assertSame(expr, ret.getFirstChild());

    Node thr = IR.throwNode(expr);
    assertType(Token.THROW, thr);
    assertSame(expr, thr.getFirstChild());

    Node er = IR.exprResult(expr);
    assertType(Token.EXPR_RESULT, er);
    assertSame(expr, er.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testIfDoForConstructs() {
    Node ifNode = IR.ifNode(IR.trueNode(), IR.block());
    assertType(Token.IF, ifNode);
    assertEquals(2, ifNode.getChildCount());

    Node ifElse = IR.ifNode(IR.trueNode(), IR.block(), IR.block());
    assertType(Token.IF, ifElse);
    assertEquals(3, ifElse.getChildCount());

    Node doNode = IR.doNode(IR.block(), IR.trueNode());
    assertType(Token.DO, doNode);

    Node forInVar = IR.forIn(IR.var(IR.name("x")), IR.name("arr"), IR.block());
    assertType(Token.FOR, forInVar);

    Node forInExpr = IR.forIn(IR.name("x"), IR.name("arr"), IR.block());
    assertType(Token.FOR, forInExpr);

    Node forEmpty = IR.forNode(IR.empty(), IR.empty(), IR.empty(), IR.block());
    assertType(Token.FOR, forEmpty);
    assertEquals(4, forEmpty.getChildCount());

    Node forFull = IR.forNode(
        IR.var(IR.name("i")), IR.trueNode(), IR.name("j"), IR.block());
    assertType(Token.FOR, forFull);
    assertEquals(4, forFull.getChildCount());
  }

  @Test(timeout = 4000)
  public void testSwitchAndCases() {
    Node caseNode = IR.caseNode(IR.number(1), IR.block(IR.returnNode()));
    assertType(Token.CASE, caseNode);

    Node defaultNode = IR.defaultCase(IR.block());
    assertType(Token.DEFAULT_CASE, defaultNode);

    Node switchNode = IR.switchNode(IR.name("x"), caseNode, defaultNode);
    assertType(Token.SWITCH, switchNode);
    assertEquals(3, switchNode.getChildCount());

    Node switchEmpty = IR.switchNode(IR.name("x"));
    assertType(Token.SWITCH, switchEmpty);
    assertEquals(1, switchEmpty.getChildCount());
  }

  @Test(timeout = 4000)
  public void testLabelBreakContinue() {
    Node labelName = IR.labelName("loop");
    assertType(Token.LABEL_NAME, labelName);

    Node label = IR.label(labelName, IR.block());
    assertType(Token.LABEL, label);
    assertSame(labelName, label.getFirstChild());

    assertType(Token.BREAK, IR.breakNode());
    Node namedBreak = IR.breakNode(labelName);
    assertType(Token.BREAK, namedBreak);
    assertSame(labelName, namedBreak.getFirstChild());

    assertType(Token.CONTINUE, IR.continueNode());
    Node namedContinue = IR.continueNode(labelName);
    assertType(Token.CONTINUE, namedContinue);
    assertSame(labelName, namedContinue.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testTryCatchAndTryCatchFinally() {
    Node tryBody = IR.block();
    Node catchNode = IR.catchNode(IR.name("e"), IR.block());
    Node tryCatch = IR.tryCatch(tryBody, catchNode);
    assertType(Token.TRY, tryCatch);
    assertEquals(2, tryCatch.getChildCount());

    Node finallyBody = IR.block();
    Node tryCatchFinally = IR.tryCatchFinally(tryBody, catchNode, finallyBody);
    assertType(Token.TRY, tryCatchFinally);
    assertEquals(3, tryCatchFinally.getChildCount());
    assertSame(finallyBody, tryCatchFinally.getLastChild());
  }

  @Test(timeout = 4000)
  public void testIssue727_1() {
    Node tryBody = IR.block();
    Node finallyBody = IR.block();
    Node tryNode = IR.tryFinally(tryBody, finallyBody);

    assertType(Token.TRY, tryNode);
    assertEquals(3, tryNode.getChildCount());
    assertSame(tryBody, tryNode.getFirstChild());
    assertSame(finallyBody, tryNode.getLastChild());
  }

  @Test(timeout = 4000)
  public void testIssue727_2() {
    Node tryBody = IR.block(IR.returnNode());
    Node finallyBody = IR.block(IR.returnNode());
    Node tryNode = IR.tryFinally(tryBody, finallyBody);

    assertType(Token.TRY, tryNode);
    assertEquals(3, tryNode.getChildCount());
    assertSame(tryBody, tryNode.getFirstChild());
    assertSame(finallyBody, tryNode.getLastChild());
  }

  @Test(timeout = 4000)
  public void testIssue727_3() {
    Node tryBody = IR.block(IR.throwNode(IR.name("e")));
    Node finallyBody = IR.block(IR.returnNode());
    Node tryNode = IR.tryFinally(tryBody, finallyBody);

    assertType(Token.TRY, tryNode);
    assertEquals(3, tryNode.getChildCount());
    assertSame(tryBody, tryNode.getFirstChild());
    assertSame(finallyBody, tryNode.getLastChild());
  }

  @Test(timeout = 4000)
  public void testTryFinallyRejectsLabelNames() {
    try {
      IR.tryFinally(IR.labelName("try"), IR.labelName("finally"));
      fail("Expected IllegalStateException because try/finally bodies must be BLOCK nodes");
    } catch (IllegalStateException expected) {
      // Correct behavior on the fixed implementation; missing on the buggy one.
    }
  }

  @Test(timeout = 4000)
  public void testCallNewGetpropGetelemAssign() {
    Node target = IR.name("f");
    Node call = IR.call(target, IR.name("a"), IR.number(1));
    assertType(Token.CALL, call);
    assertSame(target, call.getFirstChild());

    Node ctor = IR.name("F");
    Node newExpr = IR.newNode(ctor, IR.name("a"));
    assertType(Token.NEW, newExpr);
    assertSame(ctor, newExpr.getFirstChild());

    Node obj = IR.name("o");
    Node prop = IR.string("p");
    Node getprop = IR.getprop(obj, prop);
    assertType(Token.GETPROP, getprop);
    assertSame(prop, getprop.getLastChild());

    Node elem = IR.number(0);
    Node getelem = IR.getelem(obj, elem);
    assertType(Token.GETELEM, getelem);
    assertSame(elem, getelem.getLastChild());

    Node assign = IR.assign(IR.name("x"), IR.number(1));
    assertType(Token.ASSIGN, assign);

    IR.assign(getprop, IR.number(1));
    IR.assign(getelem, IR.number(1));
  }

  @Test(timeout = 4000)
  public void testBinaryAndUnaryOperators() {
    assertType(Token.HOOK, IR.hook(IR.trueNode(), IR.name("a"), IR.name("b")));
    assertType(Token.COMMA, IR.comma(IR.name("a"), IR.name("b")));
    assertType(Token.AND, IR.and(IR.trueNode(), IR.trueNode()));
    assertType(Token.OR, IR.or(IR.trueNode(), IR.trueNode()));
    assertType(Token.NOT, IR.not(IR.trueNode()));
    assertType(Token.EQ, IR.eq(IR.name("a"), IR.name("b")));
    assertType(Token.SHEQ, IR.sheq(IR.name("a"), IR.name("b")));
    assertType(Token.VOID, IR.voidNode(IR.name("a")));
    assertType(Token.NEG, IR.neg(IR.name("a")));
    assertType(Token.POS, IR.pos(IR.name("a")));
    assertType(Token.ADD, IR.add(IR.name("a"), IR.name("b")));
    assertType(Token.SUB, IR.sub(IR.name("a"), IR.name("b")));
  }

  @Test(timeout = 4000)
  public void testLiteralsAndObjectLit() {
    assertType(Token.OBJECTLIT, IR.objectlit());

    Node key = IR.stringKey("a");
    Node value = IR.number(1);
    Node prop = IR.propdef(key, value);
    assertSame(value, prop.getFirstChild());
    assertType(Token.OBJECTLIT, IR.objectlit(prop));

    Node getter = new Node(Token.GETTER_DEF);
    getter.addChildToBack(
        IR.function(IR.name("get"), IR.paramList(), IR.block()));
    assertType(Token.OBJECTLIT, IR.objectlit(getter));

    Node setter = new Node(Token.SETTER_DEF);
    setter.addChildToBack(
        IR.function(IR.name("set"), IR.paramList(IR.name("v")), IR.block()));
    assertType(Token.OBJECTLIT, IR.objectlit(setter));

    assertType(Token.ARRAYLIT, IR.arraylit());
    assertType(Token.ARRAYLIT, IR.arraylit(IR.empty(), IR.name("a")));

    assertType(Token.REGEXP, IR.regexp(IR.string("a")));
    assertType(Token.REGEXP, IR.regexp(IR.string("a"), IR.string("g")));

    assertType(Token.STRING, IR.string("s"));
    assertType(Token.STRING_KEY, IR.stringKey("s"));
    assertType(Token.NUMBER, IR.number(1.0));
    assertType(Token.THIS, IR.thisNode());
    assertType(Token.TRUE, IR.trueNode());
    assertType(Token.FALSE, IR.falseNode());
    assertType(Token.NULL, IR.nullNode());
    assertType(Token.NAME, IR.name("x"));
  }

  @Test(timeout = 4000)
  public void testMayBeStatementAcceptsAllStatementKinds() {
    Node tryCatch =
        IR.tryCatch(IR.block(), IR.catchNode(IR.name("e"), IR.block()));

    Node[] stmts = new Node[] {
      IR.empty(),
      IR.function(IR.name("f"), IR.paramList(), IR.block()),
      IR.block(),
      IR.breakNode(),
      new Node(Token.CONST),
      IR.continueNode(),
      new Node(Token.DEBUGGER),
      IR.doNode(IR.block(), IR.trueNode()),
      IR.exprResult(IR.name("x")),
      IR.forNode(IR.empty(), IR.empty(), IR.empty(), IR.block()),
      IR.ifNode(IR.trueNode(), IR.block()),
      IR.label(IR.labelName("l"), IR.block()),
      IR.returnNode(),
      IR.switchNode(IR.name("x")),
      IR.throwNode(IR.name("e")),
      tryCatch,
      IR.var(IR.name("x")),
      new Node(Token.WHILE, IR.trueNode(), IR.block()),
      new Node(Token.WITH, IR.name("x"), IR.block())
    };

    for (Node stmt : stmts) {
      Node block = IR.block(stmt);
      assertType(Token.BLOCK, block);
      assertSame(stmt, block.getFirstChild());
    }
  }

  @Test(timeout = 4000)
  public void testMayBeExpressionAcceptsAllExpressionKinds() {
    Node[] exprs = new Node[] {
      IR.function(IR.name("f"), IR.paramList(), IR.block()),
      IR.add(IR.name("a"), IR.name("b")),
      IR.and(IR.trueNode(), IR.trueNode()),
      IR.arraylit(),
      IR.assign(IR.name("a"), IR.name("b")),
      new Node(Token.ASSIGN_BITOR, IR.name("a"), IR.name("b")),
      new Node(Token.ASSIGN_BITXOR, IR.name("a"), IR.name("b")),
      new Node(Token.ASSIGN_BITAND, IR.name("a"), IR.name("b")),
      new Node(Token.ASSIGN_LSH, IR.name("a"), IR.name("b")),
      new Node(Token.ASSIGN_RSH, IR.name("a"), IR.name("b")),
      new Node(Token.ASSIGN_URSH, IR.name("a"), IR.name("b")),
      new Node(Token.ASSIGN_ADD, IR.name("a"), IR.name("b")),
      new Node(Token.ASSIGN_SUB, IR.name("a"), IR.name("b")),
      new Node(Token.ASSIGN_MUL, IR.name("a"), IR.name("b")),
      new Node(Token.ASSIGN_DIV, IR.name("a"), IR.name("b")),
      new Node(Token.ASSIGN_MOD, IR.name("a"), IR.name("b")),
      new Node(Token.BITAND, IR.name("a"), IR.name("b")),
      new Node(Token.BITOR, IR.name("a"), IR.name("b")),
      new Node(Token.BITNOT, IR.name("a")),
      new Node(Token.BITXOR, IR.name("a"), IR.name("b")),
      IR.call(IR.name("f")),
      IR.comma(IR.name("a"), IR.name("b")),
      new Node(Token.DEC, IR.name("a")),
      new Node(Token.DELPROP, IR.name("a")),
      new Node(Token.DIV, IR.name("a"), IR.name("b")),
      IR.eq(IR.name("a"), IR.name("b")),
      IR.falseNode(),
      new Node(Token.GE, IR.name("a"), IR.name("b")),
      IR.getprop(IR.name("o"), IR.string("p")),
      IR.getelem(IR.name("a"), IR.number(0)),
      new Node(Token.GT, IR.name("a"), IR.name("b")),
      IR.hook(IR.trueNode(), IR.name("a"), IR.name("b")),
      new Node(Token.IN, IR.name("a"), IR.name("b")),
      new Node(Token.INC, IR.name("a")),
      new Node(Token.INSTANCEOF, IR.name("a"), IR.name("b")),
      new Node(Token.LE, IR.name("a"), IR.name("b")),
      new Node(Token.LSH, IR.name("a"), IR.name("b")),
      new Node(Token.LT, IR.name("a"), IR.name("b")),
      new Node(Token.MOD, IR.name("a"), IR.name("b")),
      new Node(Token.MUL, IR.name("a"), IR.name("b")),
      IR.name("a"),
      new Node(Token.NE, IR.name("a"), IR.name("b")),
      IR.neg(IR.name("a")),
      IR.newNode(IR.name("F")),
      IR.not(IR.trueNode()),
      IR.number(1),
      IR.nullNode(),
      IR.objectlit(),
      IR.or(IR.trueNode(), IR.trueNode()),
      IR.pos(IR.name("a")),
      IR.regexp(IR.string("x")),
      new Node(Token.RSH, IR.name("a"), IR.name("b")),
      IR.sheq(IR.name("a"), IR.name("b")),
      new Node(Token.SHNE, IR.name("a"), IR.name("b")),
      IR.string("s"),
      IR.sub(IR.name("a"), IR.name("b")),
      IR.thisNode(),
      new Node(Token.TYPEOF, IR.name("a")),
      IR.trueNode(),
      new Node(Token.URSH, IR.name("a"), IR.name("b")),
      IR.voidNode(IR.name("a"))
    };

    for (Node expr : exprs) {
      Node result = IR.exprResult(expr);
      assertType(Token.EXPR_RESULT, result);
      assertSame(expr, result.getFirstChild());
    }
  }

  @Test(timeout = 4000)
  public void testInvalidArgumentsAndPreconditions() {
    // function
    assertIllegalState(() -> IR.function(IR.string("f"), IR.paramList(), IR.block()));
    assertIllegalState(() -> IR.function(IR.name("f"), IR.block(), IR.block()));
    assertIllegalState(() -> IR.function(IR.name("f"), IR.paramList(), IR.name("x")));

    // paramList
    assertIllegalState(() -> IR.paramList(IR.number(1)));
    assertIllegalState(() -> IR.paramList(IR.name("a"), IR.number(1)));
    assertIllegalState(() -> IR.paramList(Arrays.asList(IR.block())));

    // block/script
    assertIllegalState(() -> IR.block(IR.name("x")));
    assertIllegalState(() -> IR.script(IR.name("x")));

    // var
    assertIllegalState(() -> IR.var(IR.string("x")));
    assertIllegalState(() -> IR.var(IR.string("x"), IR.number(1)));
    assertIllegalState(() -> IR.var(IR.name("x"), IR.block()));
    Node usedName = IR.name("x");
    IR.var(usedName, IR.number(1));
    assertIllegalState(() -> IR.var(usedName, IR.number(2)));

    // return/throw/exprResult
    assertIllegalState(() -> IR.returnNode(IR.block()));
    assertIllegalState(() -> IR.throwNode(IR.block()));
    assertIllegalState(() -> IR.exprResult(IR.block()));
    assertIllegalState(() -> IR.exprResult(IR.empty()));

    // if
    assertIllegalState(() -> IR.ifNode(IR.block(), IR.block()));
    assertIllegalState(() -> IR.ifNode(IR.trueNode(), IR.name("x")));
    assertIllegalState(() -> IR.ifNode(IR.trueNode(), IR.block(), IR.name("x")));

    // do
    assertIllegalState(() -> IR.doNode(IR.name("x"), IR.trueNode()));
    assertIllegalState(() -> IR.doNode(IR.block(), IR.block()));

    // forIn
    assertIllegalState(() -> IR.forIn(IR.block(), IR.name("x"), IR.block()));
    assertIllegalState(() -> IR.forIn(IR.name("x"), IR.block(), IR.block()));
    assertIllegalState(() -> IR.forIn(IR.name("x"), IR.name("x"), IR.name("x")));

    // forNode
    assertIllegalState(() -> IR.forNode(IR.block(), IR.empty(), IR.empty(), IR.block()));
    assertIllegalState(() -> IR.forNode(IR.empty(), IR.block(), IR.empty(), IR.block()));
    assertIllegalState(() -> IR.forNode(IR.empty(), IR.empty(), IR.block(), IR.block()));
    assertIllegalState(() -> IR.forNode(IR.empty(), IR.empty(), IR.empty(), IR.name("x")));

    // switch/case/default
    assertIllegalState(() -> IR.switchNode(IR.block()));
    assertIllegalState(() -> IR.switchNode(IR.name("x"), IR.block()));
    assertIllegalState(() -> IR.caseNode(IR.block(), IR.block()));
    assertIllegalState(() -> IR.caseNode(IR.number(1), IR.name("x")));
    assertIllegalState(() -> IR.defaultCase(IR.name("x")));

    // label
    assertIllegalState(() -> IR.labelName(""));
    assertIllegalState(() -> IR.label(IR.name("l"), IR.block()));
    assertIllegalState(() -> IR.label(IR.labelName("l"), IR.name("x")));

    // break/continue
    assertIllegalState(() -> IR.breakNode(IR.name("x")));
    assertIllegalState(() -> IR.continueNode(IR.name("x")));

    // try/catch/finally
    assertIllegalState(() -> IR.tryCatch(IR.name("x"), IR.catchNode(IR.name("e"), IR.block())));
    assertIllegalState(() -> IR.tryCatch(IR.block(), IR.block()));
    Node validCatch = IR.catchNode(IR.name("e"), IR.block());
    assertIllegalState(() -> IR.tryCatchFinally(IR.block(), validCatch, IR.name("x")));
    assertIllegalState(() -> IR.catchNode(IR.string("e"), IR.block()));
    assertIllegalState(() -> IR.catchNode(IR.name("e"), IR.name("x")));

    // call/new
    assertIllegalState(() -> IR.call(IR.name("f"), IR.block()));
    assertIllegalState(() -> IR.newNode(IR.name("F"), IR.block()));

    // getprop/getelem
    assertIllegalState(() -> IR.getprop(IR.block(), IR.string("x")));
    assertIllegalState(() -> IR.getprop(IR.name("o"), IR.name("x")));
    assertIllegalState(() -> IR.getelem(IR.block(), IR.name("x")));
    assertIllegalState(() -> IR.getelem(IR.name("o"), IR.block()));

    // assign
    assertIllegalState(() -> IR.assign(IR.number(1), IR.number(2)));
    assertIllegalState(() -> IR.assign(IR.name("x"), IR.block()));

    // hook/binary/unary
    assertIllegalState(() -> IR.hook(IR.block(), IR.name("a"), IR.name("b")));
    assertIllegalState(() -> IR.add(IR.block(), IR.name("b")));
    assertIllegalState(() -> IR.not(IR.block()));

    // objectlit/propdef
    assertIllegalState(() -> IR.objectlit(IR.name("x")));
    assertIllegalState(() -> IR.objectlit(IR.stringKey("a")));
    assertIllegalState(() -> IR.propdef(IR.name("x"), IR.number(1)));
    assertIllegalState(() -> IR.propdef(IR.stringKey("a"), IR.block()));
    Node usedKey = IR.stringKey("a");
    usedKey.addChildToFront(IR.number(1));
    assertIllegalState(() -> IR.propdef(usedKey, IR.number(2)));

    // arraylit/regexp
    assertIllegalState(() -> IR.arraylit(IR.block()));
    assertIllegalState(() -> IR.regexp(IR.name("x")));
    assertIllegalState(() -> IR.regexp(IR.string("x"), IR.name("g")));
  }
}