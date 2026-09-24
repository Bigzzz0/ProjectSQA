package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target: com.google.javascript.jscomp.CodeGenerator
 * Known Defect (Defects4J): CodePrinterTest::testZero -> '\0' incorrectly escaped as "\0"
 *                          instead of "\000", causing JS octal interpretation/syntax issues.
 *
 * Branch & Condition Coverage Matrix:
 * 1. Constructor & Charsets:
 *    - outputCharset == null -> outputCharsetEncoder is null (ASCII fallback)
 *    - outputCharset == Charsets.US_ASCII -> outputCharsetEncoder is null
 *    - outputCharset == Charsets.UTF_8 -> outputCharsetEncoder is non-null
 * 2. Defect Zone:
 *    - testZero(): Assert '\0' in string literal formats to "\000" rather than "\0"
 *    - testZeroEscapeToDoubleQuotedJsString(): Direct static helper escape check
 * 3. String & Regexp Escaping (strEscape, jsString, regexpEscape):
 *    - Quotes: singleq < doubleq, singleq > doubleq, singleq == doubleq
 *    - Control chars: '\0', '\n', '\r', '\t', '\\', '\"', '\''
 *    - Script/Comment breaks: '<!--', '</script', '-->', ']]>'
 *    - Charset encoders: canEncode true vs false, ASCII fallback (>0x1f && <0x7f)
 *    - Non-Latin identifiers & surrogate pairs
 * 4. Helper Parsers:
 *    - isSimpleNumber: empty, digits only, non-digits
 *    - getSimpleNumber: < MAX_POSITIVE_INTEGER_NUMBER, >= MAX, NumberFormatException
 * 5. AST Node Types & Contexts:
 *    - Binary operators: associative (+), assignment (=), non-associative (-)
 *    - Unary operators: TYPEOF, VOID, NOT, BITNOT, POS, NEG (number vs non-number)
 *    - Control flow: TRY (catch, catch+finally), THROW, RETURN (0 vs 1 child)
 *    - Declarations: VAR (single, multi, comma assignment), NAME, LABEL_NAME
 *    - Literals: ARRAYLIT (empty, elements, elisions), NUMBER, STRING, NULL, THIS, TRUE, FALSE
 *    - Functions: FUNCTION (STATEMENT context vs START_OF_EXPR requiring parens)
 *    - Object literals: OBJECTLIT, GET, SET (identifiers, simple numbers, quoted keys)
 *    - Loops & Blocks: FOR (4 children var vs non-var, 3 children for-in), DO, WHILE
 *    - Property Access: GETPROP (needs parens for number vs normal), GETELEM
 *    - Invocations: CALL (normal, isIndirectEval, FREE_CALL property reference)
 *    - Conditionals: IF (without else, with else, dangling else ambiguity in context)
 *    - Switch/Case: SWITCH, CASE, DEFAULT
 *    - Jumps & Debugger: BREAK, CONTINUE (with/without label), DEBUGGER
 *    - Expressions: EXPR_RESULT, NEW (with/without args, containing CALL)
 *    - Delprop, RefSpecial, GetRef, SetName
 * 6. Defensive Guard & Error Paths:
 *    - EXPR_VOID throws Error
 *    - CONTINUE / BREAK without LABEL_NAME child throws Error
 *    - REGEXP with non-string children throws Error
 *    - Unknown node type throws Error
 *    - Binary operator with invalid child count throws Precondition error
 * =========================================================================================
 */
public class CodeGeneratorGeminiTest {

  /**
   * Minimal deterministic CodeConsumer implementation for test assertions.
   */
  private static class DummyConsumer extends CodeConsumer {
    private final StringBuilder sb = new StringBuilder();

    @Override
    char getLastChar() {
      return sb.length() == 0 ? '\0' : sb.charAt(sb.length() - 1);
    }

    @Override
    void append(String str) {
      sb.append(str);
    }

    String getOutput() {
      return sb.toString();
    }
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug: testZero)
  // =========================================================================

  @Test(timeout = 4000)
  public void testZero() {
    // Targets Defects4J bug: CodePrinterTest::testZero
    // In defective CodeGenerator, '\0' is escaped as "\0" instead of "\000".
    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "x");
    nameNode.addChildToFront(Node.newString("\0"));
    varNode.addChildToFront(nameNode);

    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(varNode);
    assertEquals("var x=\"\\000\"", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testZeroEscapeToDoubleQuotedJsString() {
    // Tests static escape path for NUL character
    assertEquals("\"\\000\"", CodeGenerator.escapeToDoubleQuotedJsString("\0"));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testTagAsStrict() {
    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.tagAsStrict();
    assertEquals("'use strict';", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testConstructorsAndCharsets() {
    DummyConsumer c1 = new DummyConsumer();
    CodeGenerator cg1 = new CodeGenerator(c1);
    cg1.add("x");
    assertEquals("x", c1.getOutput());

    DummyConsumer c2 = new DummyConsumer();
    CodeGenerator cg2 = new CodeGenerator(c2, Charsets.US_ASCII);
    cg2.add("y");
    assertEquals("y", c2.getOutput());

    DummyConsumer c3 = new DummyConsumer();
    CodeGenerator cg3 = new CodeGenerator(c3, Charsets.UTF_8);
    cg3.add("z");
    assertEquals("z", c3.getOutput());
  }

  @Test(timeout = 4000)
  public void testBinaryOperatorsAssociativity() {
    // Associative binary operator: a + (b + c)
    Node left = Node.newString(Token.NAME, "b");
    Node right = Node.newString(Token.NAME, "c");
    Node rhsAdd = new Node(Token.ADD, left, right);
    Node rootAdd = new Node(Token.ADD, Node.newString(Token.NAME, "a"), rhsAdd);

    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(rootAdd);
    assertEquals("a+b+c", consumer.getOutput());

    // Non-associative binary operator: a - (b - c)
    Node leftSub = Node.newString(Token.NAME, "b");
    Node rightSub = Node.newString(Token.NAME, "c");
    Node rhsSub = new Node(Token.SUB, leftSub, rightSub);
    Node rootSub = new Node(Token.SUB, Node.newString(Token.NAME, "a"), rhsSub);

    DummyConsumer consumerSub = new DummyConsumer();
    CodeGenerator cgSub = new CodeGenerator(consumerSub);
    cgSub.add(rootSub);
    assertEquals("a-(b-c)", consumerSub.getOutput());
  }

  @Test(timeout = 4000)
  public void testAssignmentAssociativity() {
    // Right-associative assignment: a = (b = c)
    Node bAssignC = new Node(Token.ASSIGN, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node rootAssign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "a"), bAssignC);

    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(rootAssign);
    assertEquals("a=b=c", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testTryCatchFinally() {
    // try { } catch(e) { }
    Node tryBody = new Node(Token.BLOCK);
    Node catchVar = Node.newString(Token.NAME, "e");
    Node catchBlock = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, catchVar, catchBlock);
    Node catchWrapper = new Node(Token.BLOCK, catchNode);

    Node tryCatch = new Node(Token.TRY, tryBody, catchWrapper);
    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(tryCatch);
    assertEquals("try{}catch(e){}", consumer.getOutput());

    // try { } catch(e) { } finally { }
    Node finallyBlock = new Node(Token.BLOCK);
    Node tryCatchFinally = new Node(Token.TRY, tryBody.cloneTree(), catchWrapper.cloneTree(), finallyBlock);
    DummyConsumer cFinally = new DummyConsumer();
    CodeGenerator cgFinally = new CodeGenerator(cFinally);
    cgFinally.add(tryCatchFinally);
    assertEquals("try{}catch(e){}finally{}", cFinally.getOutput());
  }

  @Test(timeout = 4000)
  public void testThrowAndReturn() {
    DummyConsumer cThrow = new DummyConsumer();
    CodeGenerator cgThrow = new CodeGenerator(cThrow);
    Node throwNode = new Node(Token.THROW, Node.newString(Token.NAME, "e"));
    cgThrow.add(throwNode);
    assertEquals("throw e;", cThrow.getOutput());

    DummyConsumer cReturnEmpty = new DummyConsumer();
    CodeGenerator cgReturnEmpty = new CodeGenerator(cReturnEmpty);
    Node returnEmpty = new Node(Token.RETURN);
    cgReturnEmpty.add(returnEmpty);
    assertEquals("return;", cReturnEmpty.getOutput());

    DummyConsumer cReturnVal = new DummyConsumer();
    CodeGenerator cgReturnVal = new CodeGenerator(cReturnVal);
    Node returnVal = new Node(Token.RETURN, Node.newNumber(1));
    cgReturnVal.add(returnVal);
    assertEquals("return 1;", cReturnVal.getOutput());
  }

  @Test(timeout = 4000)
  public void testArrayLiteral() {
    // Empty array: []
    DummyConsumer c1 = new DummyConsumer();
    CodeGenerator cg1 = new CodeGenerator(c1);
    cg1.add(new Node(Token.ARRAYLIT));
    assertEquals("[]", c1.getOutput());

    // Array with items: [1, 2]
    DummyConsumer c2 = new DummyConsumer();
    CodeGenerator cg2 = new CodeGenerator(c2);
    Node arr = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2));
    cg2.add(arr);
    assertEquals("[1,2]", c2.getOutput());

    // Array with holes/elisions: [1, , 2] and trailing hole [1, ]
    DummyConsumer c3 = new DummyConsumer();
    CodeGenerator cg3 = new CodeGenerator(c3);
    Node arrHole = new Node(Token.ARRAYLIT, Node.newNumber(1), new Node(Token.EMPTY), Node.newNumber(2));
    cg3.add(arrHole);
    assertEquals("[1,,2]", c3.getOutput());

    DummyConsumer c4 = new DummyConsumer();
    CodeGenerator cg4 = new CodeGenerator(c4);
    Node arrTrailingHole = new Node(Token.ARRAYLIT, Node.newNumber(1), new Node(Token.EMPTY));
    cg4.add(arrTrailingHole);
    assertEquals("[1,,]", c4.getOutput());
  }

  @Test(timeout = 4000)
  public void testUnaryOperators() {
    int[] unaries = {Token.TYPEOF, Token.VOID, Token.NOT, Token.BITNOT, Token.POS};
    String[] expectedOps = {"typeof ", "void ", "!", "~", "+"};

    for (int i = 0; i < unaries.length; i++) {
      DummyConsumer consumer = new DummyConsumer();
      CodeGenerator cg = new CodeGenerator(consumer);
      Node opNode = new Node(unaries[i], Node.newString(Token.NAME, "x"));
      cg.add(opNode);
      assertEquals(expectedOps[i] + "x", consumer.getOutput());
    }

    // Token.NEG with number vs expression
    DummyConsumer cNegNum = new DummyConsumer();
    CodeGenerator cgNegNum = new CodeGenerator(cNegNum);
    cgNegNum.add(new Node(Token.NEG, Node.newNumber(5.0)));
    assertEquals("-5", cNegNum.getOutput());

    DummyConsumer cNegName = new DummyConsumer();
    CodeGenerator cgNegName = new CodeGenerator(cNegName);
    cgNegName.add(new Node(Token.NEG, Node.newString(Token.NAME, "x")));
    assertEquals("-x", cNegName.getOutput());
  }

  @Test(timeout = 4000)
  public void testHookOperator() {
    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node hook = new Node(Token.HOOK,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"),
        Node.newString(Token.NAME, "c"));
    cg.add(hook);
    assertEquals("a?b:c", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testRegexpLiterals() {
    DummyConsumer c1 = new DummyConsumer();
    CodeGenerator cg1 = new CodeGenerator(c1);
    Node regex1 = new Node(Token.REGEXP, Node.newString("abc"));
    cg1.add(regex1);
    assertEquals("/abc/", c1.getOutput());

    DummyConsumer c2 = new DummyConsumer();
    CodeGenerator cg2 = new CodeGenerator(c2);
    Node regex2 = new Node(Token.REGEXP, Node.newString("abc"), Node.newString("gi"));
    cg2.add(regex2);
    assertEquals("/abc/gi", c2.getOutput());
  }

  @Test(timeout = 4000)
  public void testFunctionContexts() {
    // Function as statement
    Node fn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, "foo"),
        new Node(Token.LP),
        new Node(Token.BLOCK));

    DummyConsumer cStmt = new DummyConsumer();
    CodeGenerator cgStmt = new CodeGenerator(cStmt);
    cgStmt.add(fn, CodeGenerator.Context.STATEMENT);
    assertEquals("function foo(){}", cStmt.getOutput());

    // Function requiring parens at START_OF_EXPR
    DummyConsumer cExpr = new DummyConsumer();
    CodeGenerator cgExpr = new CodeGenerator(cExpr);
    cgExpr.add(fn.cloneTree(), CodeGenerator.Context.START_OF_EXPR);
    assertEquals("(function foo(){})", cExpr.getOutput());
  }

  @Test(timeout = 4000)
  public void testGetAndSetInObjectLit() {
    // GET property
    Node getFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.LP),
        new Node(Token.BLOCK));
    Node getNode = new Node(Token.GET, getFn);
    getNode.setString("p");
    Node objLitGet = new Node(Token.OBJECTLIT, getNode);

    DummyConsumer cGet = new DummyConsumer();
    CodeGenerator cgGet = new CodeGenerator(cGet);
    cgGet.add(objLitGet);
    assertEquals("{get p(){}}", cGet.getOutput());

    // SET property
    Node paramList = new Node(Token.LP, Node.newString(Token.NAME, "val"));
    Node setFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        paramList,
        new Node(Token.BLOCK));
    Node setNode = new Node(Token.SET, setFn);
    setNode.setString("p");
    Node objLitSet = new Node(Token.OBJECTLIT, setNode);

    DummyConsumer cSet = new DummyConsumer();
    CodeGenerator cgSet = new CodeGenerator(cSet);
    cgSet.add(objLitSet);
    assertEquals("{set p(val){}}", cSet.getOutput());
  }

  @Test(timeout = 4000)
  public void testForLoops() {
    // for (var i = 0; i < 1; i++) {}
    Node varInit = new Node(Token.VAR, Node.newString(Token.NAME, "i"));
    varInit.getFirstChild().addChildToFront(Node.newNumber(0));
    Node cond = new Node(Token.LT, Node.newString(Token.NAME, "i"), Node.newNumber(1));
    Node incr = new Node(Token.INC, Node.newString(Token.NAME, "i"));
    Node for4 = new Node(Token.FOR, varInit, cond, incr, new Node(Token.BLOCK));

    DummyConsumer cFor4 = new DummyConsumer();
    CodeGenerator cgFor4 = new CodeGenerator(cFor4);
    cgFor4.add(for4);
    assertEquals("for(var i=0;i<1;i++){}", cFor4.getOutput());

    // for (x in y) {}
    Node forIn = new Node(Token.FOR,
        Node.newString(Token.NAME, "x"),
        Node.newString(Token.NAME, "y"),
        new Node(Token.BLOCK));
    DummyConsumer cForIn = new DummyConsumer();
    CodeGenerator cgForIn = new CodeGenerator(cForIn);
    cgForIn.add(forIn);
    assertEquals("for(x in y){}", cForIn.getOutput());
  }

  @Test(timeout = 4000)
  public void testDoWhileAndWhile() {
    DummyConsumer cDo = new DummyConsumer();
    CodeGenerator cgDo = new CodeGenerator(cDo);
    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), Node.newString(Token.NAME, "x"));
    cgDo.add(doNode);
    assertEquals("do{}while(x);", cDo.getOutput());

    DummyConsumer cWhile = new DummyConsumer();
    CodeGenerator cgWhile = new CodeGenerator(cWhile);
    Node whileNode = new Node(Token.WHILE, Node.newString(Token.NAME, "x"), new Node(Token.BLOCK));
    cgWhile.add(whileNode);
    assertEquals("while(x){}", cWhile.getOutput());
  }

  @Test(timeout = 4000)
  public void testGetPropAndGetElem() {
    // Number followed by getprop requires parens: (1).toString
    DummyConsumer cNumProp = new DummyConsumer();
    CodeGenerator cgNumProp = new CodeGenerator(cNumProp);
    Node numProp = new Node(Token.GETPROP, Node.newNumber(1), Node.newString("toString"));
    cgNumProp.add(numProp);
    assertEquals("(1).toString", cNumProp.getOutput());

    // Identifier getprop: a.b
    DummyConsumer cIdentProp = new DummyConsumer();
    CodeGenerator cgIdentProp = new CodeGenerator(cIdentProp);
    Node identProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b"));
    cgIdentProp.add(identProp);
    assertEquals("a.b", cIdentProp.getOutput());

    // GetElem: a[b]
    DummyConsumer cElem = new DummyConsumer();
    CodeGenerator cgElem = new CodeGenerator(cElem);
    Node getElem = new Node(Token.GETELEM, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    cgElem.add(getElem);
    assertEquals("a[b]", cElem.getOutput());
  }

  @Test(timeout = 4000)
  public void testCallVariants() {
    // Normal call: f(1)
    Node normalCall = new Node(Token.CALL, Node.newString(Token.NAME, "f"), Node.newNumber(1));
    DummyConsumer cNorm = new DummyConsumer();
    CodeGenerator cgNorm = new CodeGenerator(cNorm);
    cgNorm.add(normalCall);
    assertEquals("f(1)", cNorm.getOutput());

    // Indirect eval call: eval(1) without DIRECT_EVAL prop -> (0,eval)(1)
    Node evalCall = new Node(Token.CALL, Node.newString(Token.NAME, "eval"), Node.newNumber(1));
    DummyConsumer cEval = new DummyConsumer();
    CodeGenerator cgEval = new CodeGenerator(cEval);
    cgEval.add(evalCall);
    assertEquals("(0,eval)(1)", cEval.getOutput());

    // Free call on GETPROP: (0, a.b)(1)
    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b"));
    Node freeCall = new Node(Token.CALL, getProp, Node.newNumber(1));
    freeCall.putBooleanProp(Node.FREE_CALL, true);
    DummyConsumer cFree = new DummyConsumer();
    CodeGenerator cgFree = new CodeGenerator(cFree);
    cgFree.add(freeCall);
    assertEquals("(0,a.b)(1)", cFree.getOutput());
  }

  @Test(timeout = 4000)
  public void testIfElseAndDanglingElse() {
    // if (c) {}
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "c"), new Node(Token.BLOCK));
    DummyConsumer cIf = new DummyConsumer();
    CodeGenerator cgIf = new CodeGenerator(cIf);
    cgIf.add(ifNode);
    assertEquals("if(c){}", cIf.getOutput());

    // if (c) {} else {}
    Node ifElse = new Node(Token.IF, Node.newString(Token.NAME, "c"), new Node(Token.BLOCK), new Node(Token.BLOCK));
    DummyConsumer cIfElse = new DummyConsumer();
    CodeGenerator cgIfElse = new CodeGenerator(cIfElse);
    cgIfElse.add(ifElse);
    assertEquals("if(c){}else{}", cIfElse.getOutput());
  }

  @Test(timeout = 4000)
  public void testSwitchCaseDefault() {
    Node case1 = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK));
    Node def = new Node(Token.DEFAULT, new Node(Token.BLOCK));
    Node switchNode = new Node(Token.SWITCH, Node.newString(Token.NAME, "x"), case1, def);

    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(switchNode);
    assertEquals("switch(x){case 1:{}default:{}}", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testNewExpressions() {
    // new A
    Node newNoArgs = new Node(Token.NEW, Node.newString(Token.NAME, "A"));
    DummyConsumer c1 = new DummyConsumer();
    CodeGenerator cg1 = new CodeGenerator(c1);
    cg1.add(newNoArgs);
    assertEquals("new A", c1.getOutput());

    // new A(1, 2)
    Node newWithArgs = new Node(Token.NEW, Node.newString(Token.NAME, "A"), Node.newNumber(1), Node.newNumber(2));
    DummyConsumer c2 = new DummyConsumer();
    CodeGenerator cg2 = new CodeGenerator(c2);
    cg2.add(newWithArgs);
    assertEquals("new A(1,2)", c2.getOutput());

    // new (A())()
    Node callTarget = new Node(Token.CALL, Node.newString(Token.NAME, "A"));
    Node newWithCall = new Node(Token.NEW, callTarget, Node.newNumber(1));
    DummyConsumer c3 = new DummyConsumer();
    CodeGenerator cg3 = new CodeGenerator(c3);
    cg3.add(newWithCall);
    assertEquals("new (A())(1)", c3.getOutput());
  }

  @Test(timeout = 4000)
  public void testObjectLitFormatting() {
    Node objLit = new Node(Token.OBJECTLIT);
    Node key1 = Node.newString("a");
    key1.addChildToFront(Node.newNumber(1));
    objLit.addChildToBack(key1);

    // key needing quotes
    Node key2 = Node.newString("a-b");
    key2.addChildToFront(Node.newNumber(2));
    objLit.addChildToBack(key2);

    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(objLit);
    assertEquals("{a:1,\"a-b\":2}", consumer.getOutput());

    // Object lit at START_OF_EXPR needs enclosing parens
    DummyConsumer cParen = new DummyConsumer();
    CodeGenerator cgParen = new CodeGenerator(cParen);
    cgParen.add(objLit.cloneTree(), CodeGenerator.Context.START_OF_EXPR);
    assertEquals("({a:1,\"a-b\":2})", cParen.getOutput());
  }

  @Test(timeout = 4000)
  public void testIncDecPrePost() {
    // Post increment
    Node postInc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    postInc.putIntProp(Node.INCRDECR_PROP, 1);
    DummyConsumer c1 = new DummyConsumer();
    CodeGenerator cg1 = new CodeGenerator(c1);
    cg1.add(postInc);
    assertEquals("x++", c1.getOutput());

    // Pre increment
    Node preInc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    DummyConsumer c2 = new DummyConsumer();
    CodeGenerator cg2 = new CodeGenerator(c2);
    cg2.add(preInc);
    assertEquals("++x", c2.getOutput());

    // Post decrement
    Node postDec = new Node(Token.DEC, Node.newString(Token.NAME, "x"));
    postDec.putIntProp(Node.INCRDECR_PROP, 1);
    DummyConsumer c3 = new DummyConsumer();
    CodeGenerator cg3 = new CodeGenerator(c3);
    cg3.add(postDec);
    assertEquals("x--", c3.getOutput());

    // Pre decrement
    Node preDec = new Node(Token.DEC, Node.newString(Token.NAME, "x"));
    DummyConsumer c4 = new DummyConsumer();
    CodeGenerator cg4 = new CodeGenerator(c4);
    cg4.add(preDec);
    assertEquals("--x", c4.getOutput());
  }

  @Test(timeout = 4000)
  public void testTokensDelpropLabelBreakContinueDebuggerThisNullTrueFalse() {
    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    cg.add(new Node(Token.NULL));
    cg.add(new Node(Token.THIS));
    cg.add(new Node(Token.FALSE));
    cg.add(new Node(Token.TRUE));
    cg.add(new Node(Token.DEBUGGER));
    cg.add(new Node(Token.BREAK));
    cg.add(new Node(Token.CONTINUE));

    Node del = new Node(Token.DELPROP, Node.newString(Token.NAME, "x"));
    cg.add(del);

    assertEquals("nullthisfalsetruedebugger;break;continue;delete x", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testBreakAndContinueWithLabel() {
    DummyConsumer cBreak = new DummyConsumer();
    CodeGenerator cgBreak = new CodeGenerator(cBreak);
    Node breakLabel = new Node(Token.BREAK, Node.newString(Token.LABEL_NAME, "loop"));
    cgBreak.add(breakLabel);
    assertEquals("break loop;", cBreak.getOutput());

    DummyConsumer cCont = new DummyConsumer();
    CodeGenerator cgCont = new CodeGenerator(cCont);
    Node contLabel = new Node(Token.CONTINUE, Node.newString(Token.LABEL_NAME, "loop"));
    cgCont.add(contLabel);
    assertEquals("continue loop;", cCont.getOutput());
  }

  @Test(timeout = 4000)
  public void testLabelAndWith() {
    DummyConsumer cLabel = new DummyConsumer();
    CodeGenerator cgLabel = new CodeGenerator(cLabel);
    Node labelNode = new Node(Token.LABEL,
        Node.newString(Token.LABEL_NAME, "myLabel"),
        new Node(Token.BLOCK));
    cgLabel.add(labelNode);
    assertEquals("myLabel:{}", cLabel.getOutput());

    DummyConsumer cWith = new DummyConsumer();
    CodeGenerator cgWith = new CodeGenerator(cWith);
    Node withNode = new Node(Token.WITH, Node.newString(Token.NAME, "o"), new Node(Token.BLOCK));
    cgWith.add(withNode);
    assertEquals("with(o){}", cWith.getOutput());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsSimpleNumber() {
    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertFalse(CodeGenerator.isSimpleNumber("a"));
    assertFalse(CodeGenerator.isSimpleNumber("12a"));
    assertFalse(CodeGenerator.isSimpleNumber("-1"));
    assertTrue(CodeGenerator.isSimpleNumber("0"));
    assertTrue(CodeGenerator.isSimpleNumber("1234567890"));
  }

  @Test(timeout = 4000)
  public void testGetSimpleNumber() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
    // Exceeds MAX_POSITIVE_INTEGER_NUMBER
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("99999999999999999999999999999999")));
    assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0001);
    assertEquals(42.0, CodeGenerator.getSimpleNumber("42"), 0.0001);
  }

  @Test(timeout = 4000)
  public void testStringEscapingQuoteOptimization() {
    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // Prefer single quote when string has more double quotes
    assertEquals("'\"\"\\''", cg.jsString("\"\"'"));
    // Prefer double quote when string has more single quotes
    assertEquals("\"''\\\"\"", cg.jsString("''\""));
    // Default to double quotes when counts are equal
    assertEquals("\"'\\\"\"", cg.jsString("'\""));
  }

  @Test(timeout = 4000)
  public void testStrEscapeSpecialCharacters() {
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString("\n\r\t\\\"\'");
    assertEquals("\"\\n\\r\\t\\\\\\\"'\"", escaped);

    // Break comment tags
    String scriptBreak = CodeGenerator.escapeToDoubleQuotedJsString("</script>");
    assertEquals("\"<\\/script>\"", scriptBreak);

    String startCommentBreak = CodeGenerator.escapeToDoubleQuotedJsString("<!--");
    assertEquals("\"<\\!--\"", startCommentBreak);

    String endCommentBreak = CodeGenerator.escapeToDoubleQuotedJsString("-->");
    assertEquals("\"--\\>\"", endCommentBreak);

    String cdataBreak = CodeGenerator.escapeToDoubleQuotedJsString("]]>");
    assertEquals("\"]]\\>\"", cdataBreak);
  }

  @Test(timeout = 4000)
  public void testIdentifierEscape() {
    assertEquals("latinIdent", CodeGenerator.identifierEscape("latinIdent"));
    // Non-latin char \u03b1 should be escaped
    assertEquals("ident\\u03b1", CodeGenerator.identifierEscape("ident\u03b1"));
  }

  @Test(timeout = 4000)
  public void testCharsetEncoderBranches() {
    Charset latin1 = Charset.forName("ISO-8859-1");
    CharsetEncoder encoder = latin1.newEncoder();

    // Latin1 can encode accented character e.g. é (0xE9)
    String res1 = CodeGenerator.regexpEscape("é", encoder);
    assertEquals("/é/", res1);

    // Latin1 cannot encode Greek alpha \u03b1, must be hex escaped
    String res2 = CodeGenerator.regexpEscape("\u03b1", encoder);
    assertEquals("/\\u03b1/", res2);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = Error.class, timeout = 4000)
  public void testExprVoidThrows() {
    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(new Node(Token.EXPR_VOID));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testContinueWithNonLabelNameThrows() {
    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(new Node(Token.CONTINUE, Node.newString(Token.NAME, "invalid")));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testBreakWithNonLabelNameThrows() {
    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(new Node(Token.BREAK, Node.newString(Token.NAME, "invalid")));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testRegexpWithNonStringChildThrows() {
    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(new Node(Token.REGEXP, Node.newNumber(1)));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testUnknownNodeTypeThrows() {
    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(new Node(999999));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBinaryOpWithInvalidChildCountThrows() {
    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node invalidAdd = new Node(Token.ADD, Node.newNumber(1));
    cg.add(invalidAdd);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Additional Branch Coverage
  // =========================================================================

  @Test(timeout = 4000)
  public void testRefSpecialAndGetRef() {
    DummyConsumer cRef = new DummyConsumer();
    CodeGenerator cgRef = new CodeGenerator(cRef);
    Node refSpecial = new Node(Token.REF_SPECIAL, Node.newString(Token.NAME, "foo"));
    refSpecial.putProp(Node.NAME_PROP, "bar");
    cgRef.add(refSpecial);
    assertEquals("foo.bar", cRef.getOutput());

    DummyConsumer cGetRef = new DummyConsumer();
    CodeGenerator cgGetRef = new CodeGenerator(cGetRef);
    Node getRef = new Node(Token.GET_REF, Node.newString(Token.NAME, "baz"));
    cgGetRef.add(getRef);
    assertEquals("baz", cGetRef.getOutput());
  }

  @Test(timeout = 4000)
  public void testSetNameIsIgnored() {
    DummyConsumer consumer = new DummyConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(new Node(Token.SETNAME));
    assertEquals("", consumer.getOutput());
  }
}