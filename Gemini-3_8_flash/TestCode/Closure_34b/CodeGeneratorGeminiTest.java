package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.CodeGenerator
 * Known Defect (Closure-84/CodePrinterTest::testManyAdds):
 *   - StackOverflowError occurs when processing deeply nested binary operator chains
 *     (such as 10,000 left-associative ADD operations) because CodeGenerator recursively
 *     evaluates left-hand sub-expressions rather than unrolling them.
 *
 * Specific Decision Branches Covered:
 *   1. Constructors & Charset handling:
 *      - null or US_ASCII charset encoder (legacy path) vs UTF-8 / non-null encoder.
 *   2. Binary operator dispatch:
 *      - Associative operator chaining (e.g., MUL: last.getType() == type).
 *      - Assignment operator chaining (NodeUtil.isAssignmentOp).
 *      - Standard non-associative binary operators (first != last, childCount == 2).
 *      - Binary operator validation (childCount != 2 precondition check).
 *   3. Defect-Targeted Branch:
 *      - Deeply nested Token.ADD expressions to trigger/assert left-deep recursion handling.
 *   4. AST Token dispatch coverage:
 *      - Token.TRY, Token.CATCH, Token.THROW, Token.RETURN (with/without expr).
 *      - Token.VAR (empty, single init, comma-assigned init, in-for-init context).
 *      - Token.NAME (unassigned, assigned with comma, assigned standard).
 *      - Token.LABEL_NAME, Token.LABEL, Token.BREAK, Token.CONTINUE (with/without target).
 *      - Token.ARRAYLIT (normal list, empty slots, trailing empty slot).
 *      - Token.PARAM_LIST, Token.COMMA (unrollBinaryOperator recursion guard).
 *      - Token.NUMBER, Token.TYPEOF, Token.VOID, Token.NOT, Token.BITNOT, Token.POS.
 *      - Token.NEG (numeric child negation vs general expression negation).
 *      - Token.HOOK (ternary conditional ? :).
 *      - Token.REGEXP (childCount == 1 vs childCount == 2; string type validation error).
 *      - Token.FUNCTION (standard vs START_OF_EXPR requiring parens; subclass error).
 *      - Token.GETTER_DEF, Token.SETTER_DEF (latin identifier, simple number, string literal).
 *      - Token.SCRIPT, Token.BLOCK (PRESERVE_BLOCK context, top-level line breaks, var/fn handling).
 *      - Token.FOR (4-child standard vs 3-child for-in).
 *      - Token.DO, Token.WHILE, Token.EMPTY.
 *      - Token.GETPROP (parenthesizing numeric LHS, identifier RHS).
 *      - Token.GETELEM, Token.WITH.
 *      - Token.INC, Token.DEC (pre-increment vs post-increment via INCRDECR_PROP).
 *      - Token.CALL (indirect eval, FREE_CALL with getprop, standard function call).
 *      - Token.IF (with else, without else, dangling-else ambiguity resolution).
 *      - Token.NULL, Token.THIS, Token.FALSE, Token.TRUE, Token.DEBUGGER.
 *      - Token.EXPR_RESULT, Token.NEW (with CALL child claiming precedence, with/without args).
 *      - Token.STRING, Token.DELPROP, Token.OBJECTLIT (START_OF_EXPR parens, keys).
 *      - Token.SWITCH, Token.CASE, Token.DEFAULT_CASE.
 *   5. String & Identifier Escaping helpers:
 *      - regexpEscape (script tags </script, comments <!--, comment terminators -->, ]]>).
 *      - escapeToDoubleQuotedJsString (quotes, control chars, hex encoding).
 *      - identifierEscape (Latin vs non-Latin characters).
 *      - isSimpleNumber & getSimpleNumber boundaries (0, non-digits, leading zeros, overflow).
 *      - jsString (single-quote dominance vs double-quote dominance, slash-V support).
 *   6. Control flow & context preservation:
 *      - continueProcessing() early termination.
 *      - addNonEmptyStatement block stripping, single DO/FUNCTION preservation.
 */
public class CodeGeneratorGeminiTest {

  /**
   * Concrete test implementation of CodeConsumer that captures output and
   * exposes control over processing continuation and block preservation.
   */
  private static class TestCodeConsumer extends CodeConsumer {
    final StringBuilder sb = new StringBuilder();
    boolean continueProc = true;
    boolean preserveExtraBlocks = false;

    @Override
    char getLastChar() {
      return sb.length() == 0 ? '\0' : sb.charAt(sb.length() - 1);
    }

    @Override
    void append(String str) {
      sb.append(str);
    }

    @Override
    boolean continueProcessing() {
      return continueProc;
    }

    @Override
    boolean shouldPreserveExtraBlocks() {
      return preserveExtraBlocks;
    }
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testTagAsStrict() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.tagAsStrict();
    assertEquals("'use strict';", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testConstructorCharsetVariations() {
    TestCodeConsumer consumer1 = new TestCodeConsumer();
    CodeGenerator cgNull = new CodeGenerator(consumer1, null);
    cgNull.add("a");
    assertEquals("a", consumer1.sb.toString());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    CodeGenerator cgAscii = new CodeGenerator(consumer2, Charsets.US_ASCII);
    cgAscii.add("b");
    assertEquals("b", consumer2.sb.toString());

    TestCodeConsumer consumer3 = new TestCodeConsumer();
    CodeGenerator cgUtf8 = new CodeGenerator(consumer3, Charsets.UTF_8);
    cgUtf8.add("c");
    assertEquals("c", consumer3.sb.toString());
  }

  @Test(timeout = 4000)
  public void testBasicBinaryOperatorsAndAssociativity() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // Multiplication is associative: a * (b * c)
    Node mulInner = new Node(Token.MUL, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node mulOuter = new Node(Token.MUL, Node.newString(Token.NAME, "a"), mulInner);
    cg.add(mulOuter);
    assertEquals("a*b*c", consumer.sb.toString());

    // Assignment is right-associative: a = (b = c)
    consumer.sb.setLength(0);
    Node assignInner = new Node(Token.ASSIGN, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node assignOuter = new Node(Token.ASSIGN, Node.newString(Token.NAME, "a"), assignInner);
    cg.add(assignOuter);
    assertEquals("a=b=c", consumer.sb.toString());

    // Subtraction is non-associative: a - (b - c)
    consumer.sb.setLength(0);
    Node subInner = new Node(Token.SUB, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node subOuter = new Node(Token.SUB, Node.newString(Token.NAME, "a"), subInner);
    cg.add(subOuter);
    assertEquals("a-(b-c)", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testUnaryOperatorsAndNegation() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // Number negation: -42 -> directly negates number value
    Node negNum = new Node(Token.NEG, Node.newNumber(42.0));
    cg.add(negNum);
    assertEquals("-42", consumer.sb.toString());

    // Expression negation: -x
    consumer.sb.setLength(0);
    Node negVar = new Node(Token.NEG, Node.newString(Token.NAME, "x"));
    cg.add(negVar);
    assertEquals("-x", consumer.sb.toString());

    // Typeof, Void, Not, BitNot, Pos
    consumer.sb.setLength(0);
    cg.add(new Node(Token.TYPEOF, Node.newString(Token.NAME, "x")));
    cg.add(new Node(Token.VOID, Node.newNumber(0)));
    cg.add(new Node(Token.NOT, Node.newString(Token.NAME, "y")));
    cg.add(new Node(Token.BITNOT, Node.newString(Token.NAME, "z")));
    cg.add(new Node(Token.POS, Node.newString(Token.NAME, "w")));
    assertEquals("typeof xvoid 0!y~z+w", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testHookOperator() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node cond = Node.newString(Token.NAME, "a");
    Node trueExpr = Node.newString(Token.NAME, "b");
    Node falseExpr = Node.newString(Token.NAME, "c");
    Node hook = new Node(Token.HOOK, cond, trueExpr, falseExpr);
    cg.add(hook);
    assertEquals("a?b:c", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testVarStatements() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // Uninitialized var
    Node v1 = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    cg.add(v1);
    assertEquals("var x", consumer.sb.toString());

    // Initialized var
    consumer.sb.setLength(0);
    Node nameNode = Node.newString(Token.NAME, "y");
    nameNode.addChildToBack(Node.newNumber(10));
    Node v2 = new Node(Token.VAR, nameNode);
    cg.add(v2);
    assertEquals("var y=10", consumer.sb.toString());

    // Initialized var with comma expression
    consumer.sb.setLength(0);
    Node commaExpr = new Node(Token.COMMA, Node.newNumber(1), Node.newNumber(2));
    Node nameComma = Node.newString(Token.NAME, "z");
    nameComma.addChildToBack(commaExpr);
    Node v3 = new Node(Token.VAR, nameComma);
    cg.add(v3);
    assertEquals("var z=(1,2)", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testTryCatchFinally() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node tryBody = new Node(Token.BLOCK);
    Node catchVar = Node.newString(Token.NAME, "e");
    Node catchBody = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, catchVar, catchBody);
    Node catchBlockWrapper = new Node(Token.BLOCK, catchNode);

    // Try-Catch without Finally
    Node tryCatch = new Node(Token.TRY, tryBody, catchBlockWrapper);
    cg.add(tryCatch);
    assertEquals("try{}catch(e){}", consumer.sb.toString());

    // Try-Catch-Finally
    consumer.sb.setLength(0);
    Node finallyBody = new Node(Token.BLOCK);
    Node tryCatchFinally = new Node(Token.TRY, new Node(Token.BLOCK), catchBlockWrapper, finallyBody);
    cg.add(tryCatchFinally);
    assertEquals("try{}catch(e){}finally{}", consumer.sb.toString());

    // Try-Finally without Catch (empty catch block wrapper)
    consumer.sb.setLength(0);
    Node emptyCatchWrapper = new Node(Token.BLOCK);
    Node tryFinally = new Node(Token.TRY, new Node(Token.BLOCK), emptyCatchWrapper, new Node(Token.BLOCK));
    cg.add(tryFinally);
    assertEquals("try{}finally{}", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testControlFlowStatements() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // Return with and without expression
    cg.add(new Node(Token.RETURN));
    cg.add(new Node(Token.RETURN, Node.newNumber(5)));

    // Throw
    cg.add(new Node(Token.THROW, Node.newString(Token.NAME, "err")));

    // Continue and Break with and without label
    cg.add(new Node(Token.CONTINUE));
    cg.add(new Node(Token.CONTINUE, Node.newString(Token.LABEL_NAME, "loop")));
    cg.add(new Node(Token.BREAK));
    cg.add(new Node(Token.BREAK, Node.newString(Token.LABEL_NAME, "loop")));

    // Debugger
    cg.add(new Node(Token.DEBUGGER));

    // Literals: null, this, false, true
    cg.add(new Node(Token.NULL));
    cg.add(new Node(Token.THIS));
    cg.add(new Node(Token.FALSE));
    cg.add(new Node(Token.TRUE));

    String out = consumer.sb.toString();
    assertTrue(out.contains("return"));
    assertTrue(out.contains("return 5"));
    assertTrue(out.contains("throw err"));
    assertTrue(out.contains("continue"));
    assertTrue(out.contains("continue loop"));
    assertTrue(out.contains("break"));
    assertTrue(out.contains("break loop"));
    assertTrue(out.contains("debugger"));
    assertTrue(out.contains("nullthisfalsetrue"));
  }

  @Test(timeout = 4000)
  public void testLoopsAndBranches() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // while(cond) {}
    Node whileNode = new Node(Token.WHILE, Node.newString(Token.NAME, "c"), new Node(Token.BLOCK));
    cg.add(whileNode);
    assertEquals("while(c);", consumer.sb.toString());

    // do {} while(c);
    consumer.sb.setLength(0);
    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), Node.newString(Token.NAME, "c"));
    cg.add(doNode);
    assertEquals("do;while(c);", consumer.sb.toString());

    // for(var i; i < 10; i++) {}
    consumer.sb.setLength(0);
    Node forInit = new Node(Token.VAR, Node.newString(Token.NAME, "i"));
    Node forCond = Node.newString(Token.NAME, "cond");
    Node forIncr = Node.newString(Token.NAME, "incr");
    Node for4 = new Node(Token.FOR, forInit, forCond, forIncr, new Node(Token.BLOCK));
    cg.add(for4);
    assertEquals("for(var i;cond;incr);", consumer.sb.toString());

    // for(k in obj) {}
    consumer.sb.setLength(0);
    Node forIn = new Node(Token.FOR, Node.newString(Token.NAME, "k"), Node.newString(Token.NAME, "obj"), new Node(Token.BLOCK));
    cg.add(forIn);
    assertEquals("for(k in obj);", consumer.sb.toString());

    // with(obj) {}
    consumer.sb.setLength(0);
    Node withNode = new Node(Token.WITH, Node.newString(Token.NAME, "obj"), new Node(Token.BLOCK));
    cg.add(withNode);
    assertEquals("with(obj);", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testIfElseAndAmbiguousDanglingElse() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // Simple if without else
    Node ifSimple = new Node(Token.IF, Node.newString(Token.NAME, "c"), new Node(Token.BLOCK));
    cg.add(ifSimple);
    assertEquals("if(c);", consumer.sb.toString());

    // If-else
    consumer.sb.setLength(0);
    Node ifElse = new Node(Token.IF, Node.newString(Token.NAME, "c"), new Node(Token.BLOCK), new Node(Token.BLOCK));
    cg.add(ifElse);
    assertEquals("if(c);else;", consumer.sb.toString());

    // Dangling else ambiguity context
    consumer.sb.setLength(0);
    cg.add(ifSimple, CodeGenerator.Context.BEFORE_DANGLING_ELSE);
    assertEquals("{if(c);}", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testCallAndEvalForms() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // Direct eval: eval("x")
    Node directEvalName = Node.newString(Token.NAME, "eval");
    directEvalName.putBooleanProp(Node.DIRECT_EVAL, true);
    Node directEvalCall = new Node(Token.CALL, directEvalName, Node.newString("x"));
    cg.add(directEvalCall);
    assertEquals("eval(\"x\")", consumer.sb.toString());

    // Indirect eval: (0,eval)("x")
    consumer.sb.setLength(0);
    Node indirectEvalName = Node.newString(Token.NAME, "eval");
    Node indirectEvalCall = new Node(Token.CALL, indirectEvalName, Node.newString("x"));
    cg.add(indirectEvalCall);
    assertEquals("(0,eval)(\"x\")", consumer.sb.toString());

    // FREE_CALL with GETPROP: (0,obj.prop)()
    consumer.sb.setLength(0);
    Node propGet = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("fn"));
    Node freeCall = new Node(Token.CALL, propGet);
    freeCall.putBooleanProp(Node.FREE_CALL, true);
    cg.add(freeCall);
    assertEquals("(0,obj.fn)()", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testPropertyAndElementAccess() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // Number LHS requires parentheses: (1).toString
    Node numProp = new Node(Token.GETPROP, Node.newNumber(1.0), Node.newString("toString"));
    cg.add(numProp);
    assertEquals("(1).toString", consumer.sb.toString());

    // Standard prop access: a.b
    consumer.sb.setLength(0);
    Node idProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b"));
    cg.add(idProp);
    assertEquals("a.b", consumer.sb.toString());

    // Element access: arr[0]
    consumer.sb.setLength(0);
    Node elemAccess = new Node(Token.GETELEM, Node.newString(Token.NAME, "arr"), Node.newNumber(0));
    cg.add(elemAccess);
    assertEquals("arr[0]", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testIncDecPreAndPost() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // Pre-increment: ++x
    Node preInc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    cg.add(preInc);
    assertEquals("++x", consumer.sb.toString());

    // Post-increment: x++
    consumer.sb.setLength(0);
    Node postInc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    postInc.putIntProp(Node.INCRDECR_PROP, 1);
    cg.add(postInc);
    assertEquals("x++", consumer.sb.toString());

    // Pre-decrement: --y
    consumer.sb.setLength(0);
    Node preDec = new Node(Token.DEC, Node.newString(Token.NAME, "y"));
    cg.add(preDec);
    assertEquals("--y", consumer.sb.toString());

    // Post-decrement: y--
    consumer.sb.setLength(0);
    Node postDec = new Node(Token.DEC, Node.newString(Token.NAME, "y"));
    postDec.putIntProp(Node.INCRDECR_PROP, 1);
    cg.add(postDec);
    assertEquals("y--", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testNewOperator() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // new without arguments: new Foo
    Node newNoArg = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    cg.add(newNoArg);
    assertEquals("new Foo", consumer.sb.toString());

    // new with arguments: new Foo(1, 2)
    consumer.sb.setLength(0);
    Node newWithArg = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"), Node.newNumber(1), Node.newNumber(2));
    cg.add(newWithArg);
    assertEquals("new Foo(1,2)", consumer.sb.toString());

    // new with CALL as first child claiming precedence: new (Foo())()
    consumer.sb.setLength(0);
    Node callTarget = new Node(Token.CALL, Node.newString(Token.NAME, "getFoo"));
    Node newCall = new Node(Token.NEW, callTarget, Node.newNumber(1));
    cg.add(newCall);
    assertEquals("new (getFoo())(1)", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testArrayLiteralAndArrayList() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // Empty array: []
    Node emptyArr = new Node(Token.ARRAYLIT);
    cg.add(emptyArr);
    assertEquals("[]", consumer.sb.toString());

    // Array with values and empty slot: [1,,2,]
    consumer.sb.setLength(0);
    Node arr = new Node(Token.ARRAYLIT,
        Node.newNumber(1),
        new Node(Token.EMPTY),
        Node.newNumber(2),
        new Node(Token.EMPTY));
    cg.add(arr);
    assertEquals("[1,,2,]", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralAndGettersSetters() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // Standard object literal: {a:1, "2":3, 'k e y':4}
    Node keyA = Node.newString("a");
    keyA.addChildToBack(Node.newNumber(1));
    Node keyNum = Node.newString("2");
    keyNum.addChildToBack(Node.newNumber(3));
    Node keyStr = Node.newString("k e y");
    keyStr.addChildToBack(Node.newNumber(4));
    Node objLit = new Node(Token.OBJECTLIT, keyA, keyNum, keyStr);
    cg.add(objLit);
    assertEquals("{a:1,2:3,\"k e y\":4}", consumer.sb.toString());

    // Getter and Setter
    consumer.sb.setLength(0);
    Node getFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Node getter = Node.newString(Token.GETTER_DEF, "myProp");
    getter.addChildToBack(getFn);

    Node setParam = Node.newString(Token.NAME, "val");
    Node setFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.PARAM_LIST, setParam), new Node(Token.BLOCK));
    Node setter = Node.newString(Token.SETTER_DEF, "myProp");
    setter.addChildToBack(setFn);

    Node objLitAccessors = new Node(Token.OBJECTLIT, getter, setter);
    cg.add(objLitAccessors);
    assertEquals("{get myProp(){},set myProp(val){}}", consumer.sb.toString());

    // Object lit in START_OF_EXPR context requires parentheses
    consumer.sb.setLength(0);
    cg.add(new Node(Token.OBJECTLIT), CodeGenerator.Context.START_OF_EXPR);
    assertEquals("({})", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationAndExpression() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node fnName = Node.newString(Token.NAME, "foo");
    Node paramList = new Node(Token.PARAM_LIST, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    Node body = new Node(Token.BLOCK);
    Node fn = new Node(Token.FUNCTION, fnName, paramList, body);

    // Function declaration in statement context
    cg.add(fn, CodeGenerator.Context.STATEMENT);
    assertEquals("function foo(a,b){}", consumer.sb.toString());

    // Function expression in START_OF_EXPR context requires parentheses
    consumer.sb.setLength(0);
    cg.add(fn, CodeGenerator.Context.START_OF_EXPR);
    assertEquals("(function foo(a,b){})", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testSwitchStatement() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node case1 = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK));
    Node defCase = new Node(Token.DEFAULT_CASE, new Node(Token.BLOCK));
    Node switchNode = new Node(Token.SWITCH, Node.newString(Token.NAME, "x"), case1, defCase);

    cg.add(switchNode);
    assertEquals("switch(x){case 1:default:}", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testLabelAndLabeledStatements() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node labelName = Node.newString(Token.LABEL_NAME, "outer");
    Node labeledStmt = new Node(Token.LABEL, labelName, new Node(Token.BLOCK));
    cg.add(labeledStmt);
    assertEquals("outer:;", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testRegExpLiteral() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // RegExp with flags
    Node regexWithFlags = new Node(Token.REGEXP, Node.newString("abc.*"), Node.newString("gi"));
    cg.add(regexWithFlags);
    assertEquals("/abc.*/gi", consumer.sb.toString());

    // RegExp without flags
    consumer.sb.setLength(0);
    Node regexNoFlags = new Node(Token.REGEXP, Node.newString("xyz"));
    cg.add(regexNoFlags);
    assertEquals("/xyz/", consumer.sb.toString());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsSimpleNumber() {
    assertTrue(CodeGenerator.isSimpleNumber("1"));
    assertTrue(CodeGenerator.isSimpleNumber("1234567890"));
    assertFalse(CodeGenerator.isSimpleNumber("0"));
    assertFalse(CodeGenerator.isSimpleNumber("0123"));
    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertFalse(CodeGenerator.isSimpleNumber("12a3"));
    assertFalse(CodeGenerator.isSimpleNumber("-5"));
  }

  @Test(timeout = 4000)
  public void testGetSimpleNumber() {
    assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.0);
    assertEquals(9007199254740991.0, CodeGenerator.getSimpleNumber("9007199254740991"), 0.0);
    // Number too large or non-simple returns NaN
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("999999999999999999999999999999")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("012")));
  }

  @Test(timeout = 4000)
  public void testIdentifierEscape() {
    assertEquals("valid_id$123", CodeGenerator.identifierEscape("valid_id$123"));
    // Non-latin character escapes to \uXXXX
    String escaped = CodeGenerator.identifierEscape("var\u00A0name");
    assertTrue(escaped.contains("\\u00a0"));
  }

  @Test(timeout = 4000)
  public void testRegexpEscapeSecurityBoundaries() {
    // Escaping of HTML comment and script tags
    String scriptTag = CodeGenerator.regexpEscape("</script>");
    assertTrue(scriptTag.contains("<\\/script"));

    String htmlCommentStart = CodeGenerator.regexpEscape("<!--");
    assertTrue(htmlCommentStart.contains("<\\!--"));

    String htmlCommentEnd = CodeGenerator.regexpEscape("-->");
    assertTrue(htmlCommentEnd.contains("--\\>"));

    String cdataEnd = CodeGenerator.regexpEscape("]]>");
    assertTrue(cdataEnd.contains("]]\\>"));

    // Control characters
    String controls = CodeGenerator.regexpEscape("\0\n\r\t");
    assertTrue(controls.contains("\\x00"));
    assertTrue(controls.contains("\\n"));
    assertTrue(controls.contains("\\r"));
    assertTrue(controls.contains("\\t"));
  }

  @Test(timeout = 4000)
  public void testEscapeToDoubleQuotedJsString() {
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString("Hello \"World\"\n\\");
    assertEquals("\"Hello \\\"World\\\"\\n\\\\\"", escaped);
  }

  @Test(timeout = 4000)
  public void testJsStringQuoteOptimization() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // More single quotes than double quotes -> wraps in double quotes
    Node str1 = Node.newString("It's a 'test' with \"double\"");
    cg.add(str1);
    String out1 = consumer.sb.toString();
    assertTrue(out1.startsWith("\"") && out1.endsWith("\""));

    // More double quotes than single quotes -> wraps in single quotes
    consumer.sb.setLength(0);
    Node str2 = Node.newString("a \"b\" \"c\" 'd'");
    cg.add(str2);
    String out2 = consumer.sb.toString();
    assertTrue(out2.startsWith("'") && out2.endsWith("'"));

    // Slash-V property propagation
    consumer.sb.setLength(0);
    Node strSlashV = Node.newString("test\u000Bval");
    strSlashV.putBooleanProp(Node.SLASH_V, true);
    cg.add(strSlashV);
    assertTrue(consumer.sb.toString().contains("\\v"));
  }

  @Test(timeout = 4000)
  public void testContinueProcessingHalt() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.continueProc = false;
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(new Node(Token.RETURN));
    assertEquals("", consumer.sb.toString());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure-84 / testManyAdds)
  // =========================================================================

  /**
   * Targets the defect where chained left-deep binary operators cause a
   * StackOverflowError in recursive addExpr() calls.
   */
  @Test(timeout = 4000)
  public void testManyAddsDefectBranch() {
    final int addsCount = 10000;
    Node n = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    for (int i = 0; i < addsCount; i++) {
      n = new Node(Token.ADD, n, Node.newNumber(3));
    }

    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(n);

    String output = consumer.sb.toString();
    assertNotNull(output);
    assertTrue("Output should start with evaluated left operand", output.startsWith("1+2"));
  }

  @Test(timeout = 4000)
  public void testUnrollCommaOperatorDeepChaining() {
    // Nested left-recursive comma operators: ((1, 2), 3)
    Node comma1 = new Node(Token.COMMA, Node.newNumber(1), Node.newNumber(2));
    Node comma2 = new Node(Token.COMMA, comma1, Node.newNumber(3));
    Node comma3 = new Node(Token.COMMA, comma2, Node.newNumber(4));

    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(comma3);
    assertEquals("1,2,3,4", consumer.sb.toString());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBinaryOperatorMismatchedChildrenThrows() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    // Token.ADD requires exactly 2 children
    Node badAdd = new Node(Token.ADD, Node.newNumber(1));
    cg.add(badAdd);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testTryBlockChildCountValidationThrows() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    // Token.TRY requires 2 or 3 children
    Node badTry = new Node(Token.TRY, new Node(Token.BLOCK));
    cg.add(badTry);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testRegexpNonStringChildrenThrows() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node badRegex = new Node(Token.REGEXP, Node.newNumber(123), Node.newNumber(456));
    cg.add(badRegex);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testUnknownNodeTypeThrows() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    // Token -999 does not match any known grammar rule
    Node unknownNode = new Node(-999);
    cg.add(unknownNode);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testAddNonEmptyStatementMissingBlockChildThrows() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    // DO requires its first child to be a BLOCK unless allowNonBlockChild is enabled
    Node badDo = new Node(Token.DO, Node.newNumber(1), Node.newString(Token.NAME, "c"));
    cg.add(badDo);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Complex Block Invariants
  // =========================================================================

  @Test(timeout = 4000)
  public void testPreserveBlockAndSingleFunctionChildHandling() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // If block with single FUNCTION child must preserve surrounding block (Safari workaround)
    Node fnChild = new Node(Token.FUNCTION, Node.newString(Token.NAME, "fn"), new Node(Token.PARAM_LIST), new Node(Token.BLOCK));
    Node blockWithFn = new Node(Token.BLOCK, fnChild);
    Node ifWithFn = new Node(Token.IF, Node.newString(Token.NAME, "cond"), blockWithFn);

    cg.add(ifWithFn);
    String out = consumer.sb.toString();
    assertTrue(out.startsWith("if(cond){"));
    assertTrue(out.contains("function fn(){}"));
    assertTrue(out.endsWith("}"));
  }

  @Test(timeout = 4000)
  public void testPreserveExtraBlocksFlag() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.preserveExtraBlocks = true;
    CodeGenerator cg = new CodeGenerator(consumer);

    Node emptyBlock = new Node(Token.BLOCK);
    Node ifStmt = new Node(Token.IF, Node.newString(Token.NAME, "cond"), emptyBlock);
    cg.add(ifStmt);

    assertEquals("if(cond){}", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testScriptAndBlockLineBreakPreferences() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node varStmt = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    Node exprStmt = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    Node script = new Node(Token.SCRIPT, varStmt, exprStmt);

    cg.add(script);
    assertTrue(consumer.sb.toString().contains("var x;1;"));
  }

  @Test(timeout = 4000)
  public void testForInitClauseInOperatorParenthesizing() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // (x in y) inside for-init expression must be enclosed in parentheses
    Node inNode = new Node(Token.IN, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y"));
    Node for4 = new Node(Token.FOR, inNode, Node.newString(Token.NAME, "cond"), Node.newString(Token.NAME, "incr"), new Node(Token.BLOCK));
    cg.add(for4);

    assertTrue(consumer.sb.toString().startsWith("for((x in y);"));
  }
}
