package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets CodeGenerator.java logic including AST traversal, operator precedence, string/identifier escaping,
 * context flags (Context.IN_FOR_INIT_CLAUSE, Context.START_OF_EXPR, Context.BEFORE_DANGLING_ELSE),
 * object literal keys (numeric vs string keys, getter/setter keys), unary/binary/ternary ops,
 * statements (TRY, CATCH, THROW, RETURN, VAR, IF, SWITCH, FOR, DO, WHILE, FUNCTION, NEW, etc.).
 *
 * Specific defect targeted from ground truth (Closure / Defects4J):
 * - In OBJECTLIT, numeric keys represented as NUMBER nodes in object literal keys (e.g., {1: 1} or {3e9: 1})
 *   must be output properly as numbers or unquoted numbers rather than strictly strings or failing number assertions.
 * - In OBJECTLIT, GET and SET node key handling for non-identifier / numeric properties.
 * - String escaping: HTML comment tags <!--, </script>, -->, ]]> boundary checks, surrogates, and Latin checks.
 */
public class CodeGeneratorGeminiTest {

  /**
   * Helper test double to capture code generation output.
   */
  private static class TestCodeConsumer extends CodeConsumer {
    private final StringBuilder buffer = new StringBuilder();
    private boolean preserveExtraBlocks = false;

    @Override
    void append(String str) {
      buffer.append(str);
    }

    @Override
    char getLastChar() {
      return buffer.length() > 0 ? buffer.charAt(buffer.length() - 1) : '\0';
    }

    void setPreserveExtraBlocks(boolean preserve) {
      this.preserveExtraBlocks = preserve;
    }

    @Override
    boolean shouldPreserveExtraBlocks() {
      return this.preserveExtraBlocks;
    }

    String getCode() {
      return buffer.toString();
    }
  }

  private String generate(Node node) {
    return generate(node, (Charset) null);
  }

  private String generate(Node node, Charset charset) {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer, charset);
    cg.add(node);
    return consumer.getCode();
  }

  private String generate(Node node, CodeGenerator.Context context) {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(node, context);
    return consumer.getCode();
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testTagAsStrict() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.tagAsStrict();
    assertEquals("'use strict';", consumer.getCode());
  }

  @Test(timeout = 4000)
  public void testLiteralsAndIdentifiers() {
    assertEquals("null", generate(new Node(Token.NULL)));
    assertEquals("this", generate(new Node(Token.THIS)));
    assertEquals("false", generate(new Node(Token.FALSE)));
    assertEquals("true", generate(new Node(Token.TRUE)));
    assertEquals("debugger;", generate(new Node(Token.DEBUGGER)));
    assertEquals("", generate(new Node(Token.EMPTY)));

    Node nameNode = Node.newString(Token.NAME, "myVar");
    assertEquals("myVar", generate(nameNode));

    Node numNode = Node.newNumber(42.5);
    assertEquals("42.5", generate(numNode));

    Node strNode = Node.newString("hello");
    assertEquals("\"hello\"", generate(strNode));
  }

  @Test(timeout = 4000)
  public void testBinaryOperatorsAndAssociativity() {
    // a + b
    Node plus = new Node(Token.ADD, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    assertEquals("a+b", generate(plus));

    // Associative: (a + b) + c => a+b+c
    Node leftPlus = new Node(Token.ADD, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    Node assocPlus = new Node(Token.ADD, leftPlus, Node.newString(Token.NAME, "c"));
    assertEquals("a+b+c", generate(assocPlus));

    // Non-associative on RHS: a - (b - c) => a-(b-c)
    Node rhsSub = new Node(Token.SUB, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node sub = new Node(Token.SUB, Node.newString(Token.NAME, "a"), rhsSub);
    assertEquals("a-(b-c)", generate(sub));

    // Right-associative assignments: a = (b = c) => a=b=c
    Node innerAssign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node outerAssign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "a"), innerAssign);
    assertEquals("a=b=c", generate(outerAssign));
  }

  @Test(timeout = 4000)
  public void testUnaryOperators() {
    Node not = new Node(Token.NOT, Node.newString(Token.NAME, "x"));
    assertEquals("!x", generate(not));

    Node typeof = new Node(Token.TYPEOF, Node.newString(Token.NAME, "x"));
    assertEquals("typeof x", generate(typeof));

    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    assertEquals("void 0", generate(voidNode));

    Node bitNot = new Node(Token.BITNOT, Node.newString(Token.NAME, "x"));
    assertEquals("~x", generate(bitNot));

    Node pos = new Node(Token.POS, Node.newString(Token.NAME, "x"));
    assertEquals("+x", generate(pos));

    // Negation with number: -(5) -> -5
    Node negNum = new Node(Token.NEG, Node.newNumber(5.0));
    assertEquals("-5", generate(negNum));

    // Negation with non-number: -x
    Node negVar = new Node(Token.NEG, Node.newString(Token.NAME, "x"));
    assertEquals("-x", generate(negVar));

    // Inc / Dec (Prefix vs Postfix)
    Node preInc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    assertEquals("++x", generate(preInc));

    Node postInc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    postInc.putIntProp(Node.INCRDECR_PROP, 1);
    assertEquals("x++", generate(postInc));

    Node preDec = new Node(Token.DEC, Node.newString(Token.NAME, "x"));
    assertEquals("--x", generate(preDec));

    Node postDec = new Node(Token.DEC, Node.newString(Token.NAME, "x"));
    postDec.putIntProp(Node.INCRDECR_PROP, 1);
    assertEquals("x--", generate(postDec));
  }

  @Test(timeout = 4000)
  public void testControlFlowStatements() {
    // If - Else
    Node cond = Node.newString(Token.NAME, "cond");
    Node thenBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a")));
    Node elseBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "b")));
    Node ifNode = new Node(Token.IF, cond, thenBlock, elseBlock);
    assertEquals("if(cond)a;else b;", generate(ifNode));

    // While loop
    Node whileNode = new Node(Token.WHILE, Node.newString(Token.NAME, "cond"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))));
    assertEquals("while(cond)a;", generate(whileNode));

    // Do-while loop
    Node doNode = new Node(Token.DO,
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))),
        Node.newString(Token.NAME, "cond"));
    assertEquals("do a;while(cond);", generate(doNode));

    // For loop (4 children)
    Node for4 = new Node(Token.FOR,
        Node.newString(Token.NAME, "init"),
        Node.newString(Token.NAME, "cond"),
        Node.newString(Token.NAME, "step"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "body"))));
    assertEquals("for(init;cond;step)body;", generate(for4));

    // For..in loop (3 children)
    Node forIn = new Node(Token.FOR,
        Node.newString(Token.NAME, "varName"),
        Node.newString(Token.NAME, "obj"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "body"))));
    assertEquals("for(varName in obj)body;", generate(forIn));

    // Try - Catch - Finally
    Node tryBody = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "t")));
    Node catchBlock = new Node(Token.BLOCK,
        new Node(Token.CATCH, Node.newString(Token.NAME, "e"),
            new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "c")))));
    Node finBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "f")));
    Node tryNode = new Node(Token.TRY, tryBody, catchBlock, finBlock);
    assertEquals("try{t;}catch(e){c;}finally{f;}", generate(tryNode));

    // Switch - Case - Default
    Node switchVal = Node.newString(Token.NAME, "x");
    Node case1 = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))));
    Node def = new Node(Token.DEFAULT, new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "b"))));
    Node switchNode = new Node(Token.SWITCH, switchVal, case1, def);
    assertEquals("switch(x){case 1:a;default:b;}", generate(switchNode));
  }

  @Test(timeout = 4000)
  public void testCallAndNew() {
    // Normal call: foo(a, b)
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"), Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    assertEquals("foo(a,b)", generate(call));

    // Indirect eval: eval(x) without DIRECT_EVAL prop -> (0,eval)(x)
    Node indirectEval = new Node(Token.CALL, Node.newString(Token.NAME, "eval"), Node.newString(Token.NAME, "x"));
    assertEquals("(0,eval)(x)", generate(indirectEval));

    // Direct eval
    Node directEvalTarget = Node.newString(Token.NAME, "eval");
    directEvalTarget.putBooleanProp(Node.DIRECT_EVAL, true);
    Node directEval = new Node(Token.CALL, directEvalTarget, Node.newString(Token.NAME, "x"));
    assertEquals("eval(x)", generate(directEval));

    // Free call on property: (0, a.b)(c)
    Node getprop = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b"));
    Node freeCall = new Node(Token.CALL, getprop, Node.newString(Token.NAME, "c"));
    freeCall.putBooleanProp(Node.FREE_CALL, true);
    assertEquals("(0,a.b)(c)", generate(freeCall));

    // New expression with arguments
    Node newExpr = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"), Node.newString(Token.NAME, "arg"));
    assertEquals("new Foo(arg)", generate(newExpr));

    // New expression without arguments
    Node newNoArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    assertEquals("new Foo", generate(newNoArgs));
  }

  @Test(timeout = 4000)
  public void testHookAndArrays() {
    // Hook: cond ? trueExpr : falseExpr
    Node hook = new Node(Token.HOOK, Node.newString(Token.NAME, "c"),
        Node.newString(Token.NAME, "t"),
        Node.newString(Token.NAME, "f"));
    assertEquals("c?t:f", generate(hook));

    // Array with empty slots: [1, , 2, ]
    Node arr = new Node(Token.ARRAYLIT,
        Node.newNumber(1),
        new Node(Token.EMPTY),
        Node.newNumber(2),
        new Node(Token.EMPTY));
    assertEquals("[1,,2,]", generate(arr));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & String Escaping
  // =========================================================================

  @Test(timeout = 4000)
  public void testHtmlTagEscapesInStrings() {
    // Tests "</script", "<!--", "-->", "]]>"
    String endScript = "</script>";
    String commentStart = "<!-- comment";
    String commentEnd = "-->";
    String cdataEnd = "]]>";

    String escapedEndScript = CodeGenerator.escapeToDoubleQuotedJsString(endScript);
    assertTrue(escapedEndScript.contains("<\\/script"));

    String escapedCommentStart = CodeGenerator.escapeToDoubleQuotedJsString(commentStart);
    assertTrue(escapedCommentStart.contains("<\\!--"));

    String escapedCommentEnd = CodeGenerator.escapeToDoubleQuotedJsString(commentEnd);
    assertTrue(escapedCommentEnd.contains("--\\>"));

    String escapedCdataEnd = CodeGenerator.escapeToDoubleQuotedJsString(cdataEnd);
    assertTrue(escapedCdataEnd.contains("]]\\>"));
  }

  @Test(timeout = 4000)
  public void testSpecialCharacterEscapes() {
    String input = "\0\n\r\t\\\"\'";
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString(input);
    assertTrue(escaped.contains("\\0"));
    assertTrue(escaped.contains("\\n"));
    assertTrue(escaped.contains("\\r"));
    assertTrue(escaped.contains("\\t"));
    assertTrue(escaped.contains("\\\\"));
    assertTrue(escaped.contains("\\\""));
  }

  @Test(timeout = 4000)
  public void testQuoteOptimization() {
    // More single quotes than double quotes => double-quoted wrapper
    String moreSingle = "''\"";
    String resDouble = CodeGenerator.jsString(moreSingle, null);
    assertTrue(resDouble.startsWith("\""));
    assertTrue(resDouble.endsWith("\""));

    // More double quotes than single quotes => single-quoted wrapper
    String moreDouble = "\"\"'";
    String resSingle = CodeGenerator.jsString(moreDouble, null);
    assertTrue(resSingle.startsWith("'"));
    assertTrue(resSingle.endsWith("'"));
  }

  @Test(timeout = 4000)
  public void testNonAsciiAndSupplementaryUnicodeEscapes() {
    // Supplementary unicode character requiring surrogate pair escaping
    String supplementary = new String(Character.toChars(0x10000));
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString(supplementary);
    assertEquals("\"\\ud800\\udc00\"", escaped);

    // Non-latin identifier escaping
    String nonLatinId = "var_\u00e9";
    String idEscaped = CodeGenerator.identifierEscape(nonLatinId);
    assertEquals("var_\\u00e9", idEscaped);

    // Latin identifier does not get modified
    assertEquals("latinVar_123", CodeGenerator.identifierEscape("latinVar_123"));
  }

  @Test(timeout = 4000)
  public void testRegexpEscape() {
    String regex = CodeGenerator.regexpEscape("a/b<!--");
    assertEquals("/a\\/b<\\!--/", regex);
  }

  @Test(timeout = 4000)
  public void testCharsetEncoderHandling() {
    Charset latin1 = Charset.forName("ISO-8859-1");
    // Character in ISO-8859-1: \u00e9
    String encoded = CodeGenerator.jsString("\u00e9", latin1.newEncoder());
    assertEquals("\"\u00e9\"", encoded);

    // Character outside ISO-8859-1: \u4e2d
    String escaped = CodeGenerator.jsString("\u4e2d", latin1.newEncoder());
    assertEquals("\"\\u4e2d\"", escaped);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (OBJECTLIT Keys & Get/Set)
  // =========================================================================

  /**
   * Targets defects in OBJECTLIT handling where numeric keys (NUMBER tokens as keys in OBJECTLIT)
   * must be emitted properly as numbers rather than being cast or formatted as invalid strings.
   * Defect ground truth: testObjectLit2, testObjectLit3
   */
  @Test(timeout = 4000)
  public void testDefectObjectLitNumericKeys() {
    Node objLit = new Node(Token.OBJECTLIT);
    Node numKey = Node.newNumber(1.0);
    Node val1 = Node.newNumber(1.0);
    numKey.addChildToBack(val1);
    objLit.addChildToBack(numKey);

    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    varNode.getFirstChild().addChildToBack(objLit);

    String result = generate(varNode);
    // In correct behavior, numeric literal 1 in object literal keys is printed as 1 (or 1:1)
    // and should not be quoted as "1":1
    assertEquals("var x={1:1};", result);

    // Large number key e.g. 3000000000 (3E9)
    Node objLitBig = new Node(Token.OBJECTLIT);
    Node bigNumKey = Node.newNumber(3E9);
    bigNumKey.addChildToBack(Node.newNumber(1.0));
    objLitBig.addChildToBack(bigNumKey);

    Node varBig = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    varBig.getFirstChild().addChildToBack(objLitBig);

    String resultBig = generate(varBig);
    assertEquals("var x={3E9:1};", resultBig);
  }

  /**
   * Targets defects in GET/SET property keys in OBJECTLIT.
   * Ground truth: testGetter, testSetter
   */
  @Test(timeout = 4000)
  public void testDefectObjectLitGetterSetterKeys() {
    // Getter: var x = { get a() { return 1; } }
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP),
        new Node(Token.BLOCK, new Node(Token.RETURN, Node.newNumber(1.0))));
    Node getProp = Node.newString(Token.GET, "a");
    getProp.addChildToBack(fn);

    Node objLit = new Node(Token.OBJECTLIT, getProp);
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    varNode.getFirstChild().addChildToBack(objLit);

    String result = generate(varNode);
    // Should preserve valid latin identifier name without quotes or brackets
    assertEquals("var x={get a(){return 1}};", result);
  }

  @Test(timeout = 4000)
  public void testObjectLitQuotedStringKey() {
    // Quoted string key should retain quotes: {"default": 1}
    Node objLit = new Node(Token.OBJECTLIT);
    Node strKey = Node.newString("default");
    strKey.setQuotedString();
    strKey.addChildToBack(Node.newNumber(1.0));
    objLit.addChildToBack(strKey);

    assertEquals("{\"default\":1}", generate(objLit));
  }

  // =========================================================================
  // Partition D: Context Flags & Special Statements
  // =========================================================================

  @Test(timeout = 4000)
  public void testContextInForInitClausePreventsInOperator() {
    // In for(a in b;;), binary "in" inside the init expression must be parenthesized: (a in b)
    Node inNode = new Node(Token.IN, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    String output = generate(inNode, CodeGenerator.Context.IN_FOR_INIT_CLAUSE);
    assertEquals("(a in b)", output);
  }

  @Test(timeout = 4000)
  public void testContextStartOfExprParenthesizesObjectAndFunction() {
    // Object literal at START_OF_EXPR should be wrapped in parens: ({a:1})
    Node key = Node.newString("a");
    key.addChildToBack(Node.newNumber(1.0));
    Node objLit = new Node(Token.OBJECTLIT, key);
    assertEquals("({a:1})", generate(objLit, CodeGenerator.Context.START_OF_EXPR));

    // Function expression at START_OF_EXPR should be wrapped in parens: (function(){})
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    assertEquals("(function(){})", generate(fn, CodeGenerator.Context.START_OF_EXPR));
  }

  @Test(timeout = 4000)
  public void testDanglingElseAmbiguity() {
    // if(x) if(y) a; else b; -> nested if-without-else needs block to avoid dangling else
    Node innerIf = new Node(Token.IF, Node.newString(Token.NAME, "y"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))));
    Node outerIf = new Node(Token.IF, Node.newString(Token.NAME, "x"),
        new Node(Token.BLOCK, innerIf),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "b"))));

    String code = generate(outerIf);
    assertEquals("if(x){if(y)a;}else b;", code);
  }

  @Test(timeout = 4000)
  public void testGetPropAndGetElem() {
    // (1).toString() needs parens around the number
    Node num = Node.newNumber(1.0);
    Node getPropNum = new Node(Token.GETPROP, num, Node.newString("toString"));
    assertEquals("(1).toString", generate(getPropNum));

    // a[b]
    Node getElem = new Node(Token.GETELEM, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    assertEquals("a[b]", generate(getElem));
  }

  @Test(timeout = 4000)
  public void testLabelsAndBreakContinue() {
    Node labelName = Node.newString(Token.LABEL_NAME, "loop");
    Node breakNode = new Node(Token.BREAK, labelName);
    assertEquals("break loop;", generate(breakNode));

    Node continueNode = new Node(Token.CONTINUE, Node.newString(Token.LABEL_NAME, "loop"));
    assertEquals("continue loop;", generate(continueNode));

    Node labeledStmt = new Node(Token.LABEL, Node.newString(Token.LABEL_NAME, "myLabel"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x"))));
    assertEquals("myLabel:x;", generate(labeledStmt));
  }

  @Test(timeout = 4000)
  public void testWithStatement() {
    Node withStmt = new Node(Token.WITH, Node.newString(Token.NAME, "obj"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"))));
    assertEquals("with(obj)a;", generate(withStmt));
  }

  @Test(timeout = 4000)
  public void testDeleteAndThrow() {
    Node del = new Node(Token.DELPROP, Node.newString(Token.NAME, "x"));
    assertEquals("delete x", generate(del));

    Node thrw = new Node(Token.THROW, Node.newString(Token.NAME, "err"));
    assertEquals("throw err;", generate(thrw));
  }

  // =========================================================================
  // Partition E: Defensive Exception Guards
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testInvalidBinaryOperatorThrowsException() {
    // Token.ADD with 3 children must throw IllegalStateException
    Node badAdd = new Node(Token.ADD,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"),
        Node.newString(Token.NAME, "c"));
    generate(badAdd);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testUnknownTokenTypeThrowsError() {
    Node badToken = new Node(Token.LAST_LINENO);
    generate(badToken);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testExprVoidThrowsError() {
    Node exprVoid = new Node(Token.EXPR_VOID);
    generate(exprVoid);
  }
}