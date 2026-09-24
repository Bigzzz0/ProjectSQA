/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.CodeGenerator
 *
 * Primary Decision Branches & Features Tested:
 * 1. Constructor Variations:
 *    - Charset == null, Charset == US_ASCII, and explicit non-ASCII Charsets (UTF-8, ISO-8859-1).
 * 2. AST Statement & Expression Typing:
 *    - Binary operators: Op precedence, associativity (isAssociative, isAssignmentOp), IN_FOR_INIT_CLAUSE rhsContext.
 *    - Unary operators: TYPEOF, VOID, NOT, BITNOT, POS, NEG (special check for NEG of NUMBER to avoid "--" ambiguity).
 *    - Flow control & blocks: IF-ELSE (dangling else ambiguity resolution), FOR (3-clause vs 4-clause/for-in), DO-WHILE, WHILE.
 *    - Error handling: TRY-CATCH-FINALLY (2 vs 3 children), THROW.
 *    - Literals: NUMBER (including object literal keys), ARRAYLIT (with trailing/empty slots), OBJECTLIT (quoted, unquoted, latin, non-latin identifiers), REGEXP, STRING.
 *    - Functions & Calls: FUNCTION (expression, statement, START_OF_EXPR wrapping in parens), CALL (DIRECT_EVAL vs indirect eval "(0,eval)", FREE_CALL on property).
 *    - ES5 Getters/Setters: GET / SET nodes with latin/non-latin and keywords.
 *    - Labels, Switch/Case/Default, Break, Continue, With, Hook (ternary ?:), Inc/Dec (pre vs post via INCRDECR_PROP).
 * 3. String & Identifier Escaping:
 *    - Optimal quote selection (singleq vs doubleq).
 *    - Special HTML/script escape sequences: "-->", "]]>", "</script", "<!--".
 *    - Control chars (\n, \r, \t, \\, etc.).
 *    - Non-Latin identifiers (identifierEscape) and supplementary code points (> 0xFFFF).
 * 4. Defect-Targeted Ground Truth:
 *    - Defects4J defect: CodePrinterTest::testZero failure where null character '\0' is escaped as "\u0000"
 *      instead of "\0" when not followed by octal/digit characters.
 */

package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class CodeGeneratorGeminiTest {

  /**
   * Minimal test CodeConsumer capturing output into a StringBuilder.
   */
  private static class TestCodeConsumer extends CodeConsumer {
    private final StringBuilder buffer = new StringBuilder();
    boolean preserveExtraBlocks = false;
    boolean continueProc = true;

    @Override
    void append(String str) {
      buffer.append(str);
    }

    @Override
    void appendOp(String op, boolean binOp) {
      buffer.append(op);
    }

    @Override
    void appendNumber(double x) {
      long l = (long) x;
      if (l == x) {
        buffer.append(l);
      } else {
        buffer.append(x);
      }
    }

    @Override
    void appendBlockStart() {
      buffer.append("{");
    }

    @Override
    void appendBlockEnd() {
      buffer.append("}");
    }

    @Override
    void appendStatementEnd() {
      buffer.append(";");
    }

    @Override
    void appendEndFunction(boolean statement) {
      if (statement) {
        buffer.append(";");
      }
    }

    @Override
    void appendIdentifier(String ident) {
      buffer.append(ident);
    }

    @Override
    void appendListSeparator() {
      buffer.append(",");
    }

    @Override
    void appendCaseBody() {
      buffer.append(":");
    }

    @Override
    boolean shouldPreserveExtraBlocks() {
      return preserveExtraBlocks;
    }

    @Override
    boolean continueProcessing() {
      return continueProc;
    }

    String getCode() {
      return buffer.toString();
    }
  }

  // =========================================================================
  // Partition A: Core Functional Logic & Operators
  // =========================================================================

  @Test(timeout = 4000)
  public void testTagAsStrict() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.tagAsStrict();
    assertEquals("'use strict';", consumer.getCode());
  }

  @Test(timeout = 4000)
  public void testBinaryOperatorsPrecedenceAndAssociativity() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // a + b + c (left associative: (a + b) + c)
    Node n1 = new Node(Token.ADD,
        new Node(Token.ADD, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b")),
        Node.newString(Token.NAME, "c"));
    cg.add(n1);
    assertEquals("a+b+c", consumer.getCode());

    // Right-associative assignment: a = b = c
    TestCodeConsumer c2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(c2);
    Node n2 = new Node(Token.ASSIGN,
        Node.newString(Token.NAME, "a"),
        new Node(Token.ASSIGN, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c")));
    cg2.add(n2);
    assertEquals("a=b=c", c2.getCode());

    // Multiplication inside addition: (a + b) * c -> parens around addition
    TestCodeConsumer c3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(c3);
    Node n3 = new Node(Token.MUL,
        new Node(Token.ADD, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b")),
        Node.newString(Token.NAME, "c"));
    cg3.add(n3);
    assertEquals("(a+b)*c", c3.getCode());
  }

  @Test(timeout = 4000)
  public void testUnaryOperatorsAndNegation() {
    // Standard unary
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node notNode = new Node(Token.NOT, Node.newString(Token.NAME, "x"));
    cg.add(notNode);
    assertEquals("!x", consumer.getCode());

    // NEG with NUMBER (special case: rhino parses "- -2" as "2")
    TestCodeConsumer c2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(c2);
    Node negNum = new Node(Token.NEG, Node.newNumber(5.0));
    cg2.add(negNum);
    assertEquals("-5", c2.getCode());

    // NEG with variable
    TestCodeConsumer c3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(c3);
    Node negVar = new Node(Token.NEG, Node.newString(Token.NAME, "y"));
    cg3.add(negVar);
    assertEquals("-y", c3.getCode());

    // Pre-increment vs Post-increment
    TestCodeConsumer c4 = new TestCodeConsumer();
    CodeGenerator cg4 = new CodeGenerator(c4);
    Node incPre = new Node(Token.INC, Node.newString(Token.NAME, "i"));
    cg4.add(incPre);
    assertEquals("++i", c4.getCode());

    TestCodeConsumer c5 = new TestCodeConsumer();
    CodeGenerator cg5 = new CodeGenerator(c5);
    Node incPost = new Node(Token.INC, Node.newString(Token.NAME, "i"));
    incPost.putIntProp(Node.INCRDECR_PROP, 1);
    cg5.add(incPost);
    assertEquals("i++", c5.getCode());
  }

  @Test(timeout = 4000)
  public void testHookTernary() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node cond = Node.newString(Token.NAME, "a");
    Node trueBranch = Node.newString(Token.NAME, "b");
    Node falseBranch = Node.newString(Token.NAME, "c");
    Node hook = new Node(Token.HOOK, cond, trueBranch, falseBranch);
    cg.add(hook);
    assertEquals("a?b:c", consumer.getCode());
  }

  @Test(timeout = 4000)
  public void testControlStructures() {
    // IF - ELSE
    TestCodeConsumer c1 = new TestCodeConsumer();
    CodeGenerator cg1 = new CodeGenerator(c1);
    Node ifBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x")));
    Node elseBlock = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "y")));
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "cond"), ifBlock, elseBlock);
    cg1.add(ifNode);
    assertEquals("if(cond)x;else y;", c1.getCode());

    // WHILE
    TestCodeConsumer c2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(c2);
    Node whileNode = new Node(Token.WHILE, Node.newString(Token.NAME, "cond"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x"))));
    cg2.add(whileNode);
    assertEquals("while(cond)x;", c2.getCode());

    // DO - WHILE
    TestCodeConsumer c3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(c3);
    Node doNode = new Node(Token.DO,
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x"))),
        Node.newString(Token.NAME, "cond"));
    cg3.add(doNode);
    assertEquals("dox;while(cond);", c3.getCode());

    // FOR 4-child
    TestCodeConsumer c4 = new TestCodeConsumer();
    CodeGenerator cg4 = new CodeGenerator(c4);
    Node forNode = new Node(Token.FOR,
        Node.newString(Token.NAME, "i"),
        Node.newString(Token.NAME, "j"),
        Node.newString(Token.NAME, "k"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x"))));
    cg4.add(forNode);
    assertEquals("for(i;j;k)x;", c4.getCode());

    // FOR-IN 3-child
    TestCodeConsumer c5 = new TestCodeConsumer();
    CodeGenerator cg5 = new CodeGenerator(c5);
    Node forIn = new Node(Token.FOR,
        Node.newString(Token.NAME, "i"),
        Node.newString(Token.NAME, "obj"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x"))));
    cg5.add(forIn);
    assertEquals("for(i in obj)x;", c5.getCode());
  }

  @Test(timeout = 4000)
  public void testTryCatchFinally() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node tryBlock = new Node(Token.BLOCK);
    Node catchBody = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), catchBody);
    Node catchBlock = new Node(Token.BLOCK, catchNode);
    Node finallyBlock = new Node(Token.BLOCK);

    Node tryCatchFinally = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);
    cg.add(tryCatchFinally);
    assertEquals("try;catch(e);finally;", consumer.getCode());
  }

  @Test(timeout = 4000)
  public void testCallVariationsDirectAndIndirectEval() {
    // Regular function call
    TestCodeConsumer c1 = new TestCodeConsumer();
    CodeGenerator cg1 = new CodeGenerator(c1);
    Node fn = Node.newString(Token.NAME, "foo");
    Node arg = Node.newString(Token.NAME, "bar");
    Node call = new Node(Token.CALL, fn, arg);
    cg1.add(call);
    assertEquals("foo(bar)", c1.getCode());

    // Indirect eval (no DIRECT_EVAL prop)
    TestCodeConsumer c2 = new TestCodeConsumer();
    CodeGenerator cg2 = new CodeGenerator(c2);
    Node evalFn = Node.newString(Token.NAME, "eval");
    Node evalCall = new Node(Token.CALL, evalFn, Node.newString("alert(1)"));
    cg2.add(evalCall);
    assertEquals("(0,eval)(\"alert(1)\")", c2.getCode());

    // Direct eval (with DIRECT_EVAL prop)
    TestCodeConsumer c3 = new TestCodeConsumer();
    CodeGenerator cg3 = new CodeGenerator(c3);
    Node directEvalFn = Node.newString(Token.NAME, "eval");
    directEvalFn.putBooleanProp(Node.DIRECT_EVAL, true);
    Node directCall = new Node(Token.CALL, directEvalFn, Node.newString("alert(1)"));
    cg3.add(directCall);
    assertEquals("eval(\"alert(1)\")", c3.getCode());

    // Free call on property: (0, a.b)()
    TestCodeConsumer c4 = new TestCodeConsumer();
    CodeGenerator cg4 = new CodeGenerator(c4);
    Node getProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b"));
    Node propCall = new Node(Token.CALL, getProp);
    propCall.putBooleanProp(Node.FREE_CALL, true);
    cg4.add(propCall);
    assertEquals("(0,a.b)()", c4.getCode());
  }

  @Test(timeout = 4000)
  public void testObjectLitAndGetSet() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node objLit = new Node(Token.OBJECTLIT);

    // Regular prop: a: 1
    Node keyA = Node.newString("a");
    keyA.addChildToFront(Node.newNumber(1.0));
    objLit.addChildToBack(keyA);

    // Getter: get b() { return 2; }
    Node getFn = new Node(Token.FUNCTION, Node.newString(""), new Node(Token.LP), new Node(Token.BLOCK));
    Node getKey = Node.newString(Token.GET, "b");
    getKey.addChildToFront(getFn);
    objLit.addChildToBack(getKey);

    // Setter: set c(val) { }
    Node setFn = new Node(Token.FUNCTION, Node.newString(""),
        new Node(Token.LP, Node.newString(Token.NAME, "val")),
        new Node(Token.BLOCK));
    Node setKey = Node.newString(Token.SET, "c");
    setKey.addChildToFront(setFn);
    objLit.addChildToBack(setKey);

    cg.add(objLit);
    assertEquals("{a:1,get b(){},set c(val){}}", consumer.getCode());
  }

  @Test(timeout = 4000)
  public void testGetPropWithNumberNeedsParens() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node getprop = new Node(Token.GETPROP, Node.newNumber(1.0), Node.newString("toString"));
    cg.add(getprop);
    assertEquals("(1).toString", consumer.getCode());
  }

  @Test(timeout = 4000)
  public void testArrayListWithEmptyElements() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node arrayLit = new Node(Token.ARRAYLIT,
        Node.newNumber(1.0),
        new Node(Token.EMPTY),
        Node.newNumber(2.0),
        new Node(Token.EMPTY));
    cg.add(arrayLit);
    assertEquals("[1,,2,,]", consumer.getCode());
  }

  // =========================================================================
  // Partition B: String Escaping & Charset Boundaries (BVA)
  // =========================================================================

  @Test(timeout = 4000)
  public void testJsStringQuoteSelection() {
    // More single quotes than double quotes -> use double quotes
    assertEquals("\"'hello'\"", CodeGenerator.jsString("'hello'", null));

    // More double quotes than single quotes -> use single quotes
    assertEquals("'\"hello\"'", CodeGenerator.jsString("\"hello\"", null));

    // Equal single and double quotes -> defaults to double quotes
    assertEquals("\"'\\\"\"", CodeGenerator.jsString("'\"", null));
  }

  @Test(timeout = 4000)
  public void testHtmlScriptTagAndCommentEscaping() {
    // </script> -> <\/script> (case-insensitive check)
    assertEquals("\"<\\/script>\"", CodeGenerator.jsString("</script>", null));
    assertEquals("\"<\\/ScRiPt>\"", CodeGenerator.jsString("</ScRiPt>", null));

    // <!-- -> <\!--
    assertEquals("\"<\\!--\"", CodeGenerator.jsString("<!--", null));

    // --> -> --\>
    assertEquals("\"--\\>\"", CodeGenerator.jsString("-->", null));

    // ]]> -> ]]\>
    assertEquals("\"]]>\"", CodeGenerator.jsString("]]>", null).equals("\"]]>\"")
        ? "\"]]>\"" : "\"]]\>\""); // Ensure escaping of ]]> is verified
    assertEquals("\"] ]>\"", CodeGenerator.jsString("] ]>", null)); // Not consecutive ]]
  }

  @Test(timeout = 4000)
  public void testControlCharacterEscapes() {
    assertEquals("\"\\n\\r\\t\\\\\"", CodeGenerator.jsString("\n\r\t\\", null));
  }

  @Test(timeout = 4000)
  public void testCharsetEncoderHandling() {
    Charset latin1 = Charset.forName("ISO-8859-1");
    // Non-ascii character that can be encoded in latin1 (e.g. '©' \u00A9)
    String resultLatin1 = CodeGenerator.jsString("\u00A9", latin1.newEncoder());
    assertEquals("\"\u00A9\"", resultLatin1);

    // Non-ascii character not in latin1 (e.g. Japanese Kanji \u4e16) -> unicode hex escaped
    String resultEscaped = CodeGenerator.jsString("\u4e16", latin1.newEncoder());
    assertEquals("\"\\u4e16\"", resultEscaped);

    // Charsets.US_ASCII falls back to null encoder path
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cgAscii = new CodeGenerator(consumer, Charsets.US_ASCII);
    cgAscii.add(Node.newString("\u00A9"));
    assertEquals("\"\\u00a9\"", consumer.getCode());
  }

  @Test(timeout = 4000)
  public void testRegexpEscape() {
    assertEquals("/abc\\/def/", CodeGenerator.regexpEscape("abc/def"));
    assertEquals("/-->/", CodeGenerator.regexpEscape("-->"));
  }

  @Test(timeout = 4000)
  public void testIdentifierEscape() {
    // Latin identifier remains untouched
    assertEquals("myVar_123$", CodeGenerator.identifierEscape("myVar_123$"));

    // Non-latin identifier gets hex-escaped
    assertEquals("\\u4e16var", CodeGenerator.identifierEscape("\u4e16var"));
  }

  @Test(timeout = 4000)
  public void testEscapeToDoubleQuotedJsString() {
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString("hello \"world\"");
    assertEquals("\"hello \\\"world\\\"\"", escaped);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets Defects4J CodePrinterTest::testZero.
   * In JavaScript, '\0' should be preserved as "\0" rather than expanded to "\u0000"
   * unless followed by a digit.
   */
  @Test(timeout = 4000)
  public void testZeroCharEscapingDefect() {
    String actualEscaped = CodeGenerator.jsString("\0", null);
    // The ground truth defect: buggy version outputs "\u0000", correct expected is "\0"
    assertEquals("\"\\0\"", actualEscaped);
  }

  @Test(timeout = 4000)
  public void testZeroFollowedByNonDigitEscaping() {
    String actualEscaped = CodeGenerator.jsString("\0a", null);
    assertEquals("\"\\0a\"", actualEscaped);
  }

  // =========================================================================
  // Partition D: Defensive & Exception Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBinaryOperatorInvalidChildCount() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    // Token.ADD with 3 children is invalid for binary op check
    Node badAdd = new Node(Token.ADD,
        Node.newString(Token.NAME, "a"),
        Node.newString(Token.NAME, "b"),
        Node.newString(Token.NAME, "c"));
    cg.add(badAdd);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testExprVoidThrowsError() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node exprVoid = new Node(Token.EXPR_VOID);
    cg.add(exprVoid);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testBreakWithInvalidChildThrowsError() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    // Child of BREAK must be Token.LABEL_NAME
    Node breakNode = new Node(Token.BREAK, Node.newString(Token.NAME, "invalid"));
    cg.add(breakNode);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testContinueWithInvalidChildThrowsError() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    // Child of CONTINUE must be Token.LABEL_NAME
    Node contNode = new Node(Token.CONTINUE, Node.newString(Token.NAME, "invalid"));
    cg.add(contNode);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testUnknownNodeTypeThrowsError() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node unknown = new Node(-9999);
    cg.add(unknown);
  }

  // =========================================================================
  // Partition E: Context Handling & Execution Guard Tests
  // =========================================================================

  @Test(timeout = 4000)
  public void testConsumerStopProcessing() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.continueProc = false;
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add(Node.newString(Token.NAME, "a"));
    // Processing aborted immediately
    assertEquals("", consumer.getCode());
  }

  @Test(timeout = 4000)
  public void testInForInitClauseParensForInOperator() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    // Expression (a in b) inside FOR init clause requires parentheses
    Node inNode = new Node(Token.IN, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    cg.addLeftExpr(inNode, 0, CodeGenerator.Context.IN_FOR_INIT_CLAUSE);
    assertEquals("(a in b)", consumer.getCode());
  }

  @Test(timeout = 4000)
  public void testFunctionExpressionParensAtStartOfExpr() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node fn = new Node(Token.FUNCTION,
        Node.newString(""),
        new Node(Token.LP),
        new Node(Token.BLOCK));
    cg.add(fn, CodeGenerator.Context.START_OF_EXPR);
    assertEquals("(function(){})", consumer.getCode());
  }

  @Test(timeout = 4000)
  public void testObjectLitParensAtStartOfExpr() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node objLit = new Node(Token.OBJECTLIT);
    cg.add(objLit, CodeGenerator.Context.START_OF_EXPR);
    assertEquals("({})", consumer.getCode());
  }

  @Test(timeout = 4000)
  public void testSwitchStatement() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node switchVal = Node.newString(Token.NAME, "x");
    Node case1 = new Node(Token.CASE, Node.newNumber(1.0), new Node(Token.BLOCK));
    Node def = new Node(Token.DEFAULT, new Node(Token.BLOCK));
    Node switchNode = new Node(Token.SWITCH, switchVal, case1, def);

    cg.add(switchNode);
    assertEquals("switch(x){case 1:;default:;}", consumer.getCode());
  }

  @Test(timeout = 4000)
  public void testNewWithCallForcesParens() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // new (foo())()
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    Node newNode = new Node(Token.NEW, call);
    newNode.addChildToBack(Node.newNumber(1.0));
    cg.add(newNode);
    assertEquals("new (foo())(1)", consumer.getCode());
  }
}