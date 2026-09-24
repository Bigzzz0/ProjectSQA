/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: com.google.javascript.jscomp.CodeGenerator
 *
 * 1. Defects4J Known Defect Under Test:
 *    - Defect ID: CodePrinterTest::testUnicode / String escaping of 0x7f (DEL).
 *    - Fault Condition: In strEscape(String, char, String, String, String, CharsetEncoder),
 *      when outputCharsetEncoder is null, the ASCII fast-path condition checks:
 *        `if (c > 0x1f && c <= 0x7f)`
 *      Because 0x7f (ASCII DEL) satisfies `<= 0x7f`, it is output verbatim rather than
 *      being hex-escaped to "\\u007f".
 *    - Target Test: `testDefectUnicodeEscape0x7F()` asserts that "\u007f" is escaped as "\\u007f".
 *
 * 2. Equivalence Partitions & Decision Logic:
 *    - Partition A: String & RegExp Escaping
 *      * Quote selection: singleq < doubleq, singleq > doubleq, singleq == doubleq.
 *      * Special escape sequences: '\0', '\n', '\r', '\t', '\\', '\"', '\''.
 *      * HTML/SGML breaker sequences: "-->", "]]>", "</script", "<!--" (case-insensitive for script).
 *      * CharsetEncoder branch: null/US_ASCII (ASCII fallback) vs UTF-8 / non-ASCII encodable.
 *      * Identifier escaping: Latin fast path vs non-Latin hex escaping.
 *      * Simple number detection: isSimpleNumber, getSimpleNumber boundary tests (< MAX_POSITIVE_INTEGER_NUMBER).
 *    - Partition B: Binary Operators & Precedence/Associativity
 *      * Normal binary operator (2 children) vs invalid child count (< 2 or > 2).
 *      * Associative operators (e.g., ADD, MUL: a * (b * c)).
 *      * Right-associative assignment operators (e.g., a = b = c).
 *      * Non-associative operators forcing RHS parens (e.g., a - (b - c)).
 *    - Partition C: Control Structures & AST Node Types
 *      * TRY / CATCH / FINALLY: try-catch, try-finally, try-catch-finally.
 *      * THROW, RETURN (with/without expr).
 *      * VAR declarations in normal and IN_FOR_INIT_CLAUSE contexts.
 *      * NAME, LABEL_NAME, GETPROP (with numeric and identifier LHS), GETELEM.
 *      * ARRAYLIT with sparse/elided slots and trailing comma handling.
 *      * Unary operators: TYPEOF, VOID, NOT, BITNOT, POS, NEG (number vs non-number).
 *      * HOOK (ternary operator).
 *      * FUNCTION (statement vs START_OF_EXPR requiring parens).
 *      * OBJECTLIT, GET, SET (property names: identifier, simple number, quoted, keywords).
 *      * FOR (4-clause standard, 3-clause for-in), WHILE, DO-WHILE.
 *      * IF-ELSE (dangling else ambiguity resolution in BEFORE_DANGLING_ELSE context).
 *      * SWITCH / CASE / DEFAULT.
 *      * CALL (direct call, indirect eval, free-call property preservation).
 *      * NEW (with args, without args, target containing CALL).
 *      * Literals: NULL, THIS, FALSE, TRUE, NUMBER, STRING.
 *      * BREAK, CONTINUE (with and without label).
 *    - Partition D: Defensive & Exception Guard Paths
 *      * Binary op childCount != 2 precondition.
 *      * GETPROP / GETELEM malformed AST child count.
 *      * REGEXP non-string children -> throws Error.
 *      * Subclassed Node instances for FUNCTION / BLOCK / SCRIPT -> throws Error.
 *      * EXPR_VOID -> throws Error.
 *      * Unknown token types -> throws Error.
 *      * BREAK/CONTINUE/LABEL with non-LABEL_NAME child -> throws Error.
 */

package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.junit.Assert.*;

public class CodeGeneratorGeminiTest {

  // Concrete test harness subclass of abstract CodeConsumer
  private static class TestCodeConsumer extends CodeConsumer {
    private final StringBuilder buffer = new StringBuilder();
    private char lastChar = '\0';
    private boolean continueProcessing = true;
    private boolean preserveExtraBlocks = false;

    @Override
    char getLastChar() {
      return lastChar;
    }

    @Override
    void append(String str) {
      buffer.append(str);
      if (str.length() > 0) {
        lastChar = str.charAt(str.length() - 1);
      }
    }

    @Override
    boolean continueProcessing() {
      return continueProcessing;
    }

    void setContinueProcessing(boolean val) {
      this.continueProcessing = val;
    }

    @Override
    boolean shouldPreserveExtraBlocks() {
      return preserveExtraBlocks;
    }

    void setPreserveExtraBlocks(boolean preserve) {
      this.preserveExtraBlocks = preserve;
    }

    String getCode() {
      return buffer.toString();
    }
  }

  private String generate(Node node) {
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
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
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefectUnicodeEscape0x7F() {
    // Ground truth defect: 0x7f (DEL) must be unicode escaped to \u007f,
    // not emitted literally as byte 0x7f.
    String escaped = CodeGenerator.jsString("\u007f", null);
    assertEquals("\"\\u007f\"", escaped);
  }

  @Test(timeout = 4000)
  public void testDefectUnicodeEscape0x7FInRegexp() {
    String escaped = CodeGenerator.regexpEscape("\u007f");
    assertEquals("/\\u007f/", escaped);
  }

  @Test(timeout = 4000)
  public void testDefectUnicodeEscape0x7FInDoubleQuotedJsString() {
    String escaped = CodeGenerator.escapeToDoubleQuotedJsString("\u007f");
    assertEquals("\"\\u007f\"", escaped);
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
  public void testContinueProcessingFalseAbortsAdd() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.setContinueProcessing(false);
    CodeGenerator cg = new CodeGenerator(consumer);
    Node num = Node.newNumber(42);
    cg.add(num);
    assertEquals("", consumer.getCode());
  }

  @Test(timeout = 4000)
  public void testSimpleNumberHelpers() {
    assertTrue(CodeGenerator.isSimpleNumber("0"));
    assertTrue(CodeGenerator.isSimpleNumber("123456789"));
    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertFalse(CodeGenerator.isSimpleNumber("-1"));
    assertFalse(CodeGenerator.isSimpleNumber("1.5"));
    assertFalse(CodeGenerator.isSimpleNumber("12a"));
    assertFalse(CodeGenerator.isSimpleNumber("a12"));

    assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0001);
    assertEquals(42.0, CodeGenerator.getSimpleNumber("42"), 0.0001);
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("abc")));
    // Boundary: value greater than or equal to MAX_POSITIVE_INTEGER_NUMBER
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("9007199254740993")));
  }

  @Test(timeout = 4000)
  public void testJsStringQuotingPreferences() {
    // single quotes escaped when more double quotes
    String s1 = CodeGenerator.jsString("\"\"'", null);
    assertEquals("'\"\"\\''", s1);

    // double quotes escaped when more single quotes
    String s2 = CodeGenerator.jsString("''\"", null);
    assertEquals("\"''\\\"\"", s2);

    // equal quotes defaults to double quotes wrapping
    String s3 = CodeGenerator.jsString("'\"", null);
    assertEquals("\"'\\\"\"", s3);
  }

  @Test(timeout = 4000)
  public void testStrEscapeSpecialCharacters() {
    assertEquals("\"\\0\"", CodeGenerator.jsString("\0", null));
    assertEquals("\"\\n\"", CodeGenerator.jsString("\n", null));
    assertEquals("\"\\r\"", CodeGenerator.jsString("\r", null));
    assertEquals("\"\\t\"", CodeGenerator.jsString("\t", null));
    assertEquals("\"\\\\\"", CodeGenerator.jsString("\\", null));
  }

  @Test(timeout = 4000)
  public void testStrEscapeHtmlAndCommentBreaks() {
    // Break --> into --\>
    assertEquals("\"--\\>\"", CodeGenerator.jsString("-->", null));
    assertEquals("\"foo--\\>bar\"", CodeGenerator.jsString("foo-->bar", null));

    // Break ]]> into ]]\>
    assertEquals("\"]]\\>\"", CodeGenerator.jsString("]]>", null));
    assertEquals("\"x]]\\>y\"", CodeGenerator.jsString("x]]>y", null));

    // Break </script into <\/script (case insensitive)
    assertEquals("\"<\\/script>\"", CodeGenerator.jsString("</script>", null));
    assertEquals("\"<\\/SCRIPT>\"", CodeGenerator.jsString("</SCRIPT>", null));

    // Break <!-- into <\!-- (case sensitive)
    assertEquals("\"<\\!--\"", CodeGenerator.jsString("<!--", null));

    // Standard < and > without dangerous sequences
    assertEquals("\"<tag>\"", CodeGenerator.jsString("<tag>", null));
    assertEquals("\"->\"", CodeGenerator.jsString("->", null));
    assertEquals("\"]>\"", CodeGenerator.jsString("]>", null));
  }

  @Test(timeout = 4000)
  public void testStrEscapeWithCharsetEncoder() {
    Charset latin1 = Charset.forName("ISO-8859-1");
    // Character within ISO-8859-1 (e.g. \u00e9) is kept
    String resLatin = CodeGenerator.jsString("\u00e9", latin1.newEncoder());
    assertEquals("\"\u00e9\"", resLatin);

    // Character outside ISO-8859-1 (e.g. \u0100) is escaped
    String resNonLatin = CodeGenerator.jsString("\u0100", latin1.newEncoder());
    assertEquals("\"\\u0100\"", resNonLatin);

    // US_ASCII constructor path sets outputCharsetEncoder to null
    TestCodeConsumer consumer = new TestCodeConsumer();
    CodeGenerator cgAscii = new CodeGenerator(consumer, Charsets.US_ASCII);
    assertNotNull(cgAscii);

    CodeGenerator cgNull = new CodeGenerator(consumer, null);
    assertNotNull(cgNull);
  }

  @Test(timeout = 4000)
  public void testIdentifierEscape() {
    // Latin fast path
    assertEquals("fooBar123", CodeGenerator.identifierEscape("fooBar123"));
    // Non-latin character escaped
    String escaped = CodeGenerator.identifierEscape("foo\u00A9bar");
    assertEquals("foo\\u00a9bar", escaped);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Operators
  // =========================================================================

  @Test(timeout = 4000)
  public void testBinaryOperatorAssociativity() {
    // Associative operator: a * (b * c) -> a*b*c without inner parens
    Node mulInner = new Node(Token.MUL, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node mulOuter = new Node(Token.MUL, Node.newString(Token.NAME, "a"), mulInner);
    assertEquals("a*b*c", generate(mulOuter));

    // Right-associative assignments: a = b = c
    Node assignInner = new Node(Token.ASSIGN, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node assignOuter = new Node(Token.ASSIGN, Node.newString(Token.NAME, "a"), assignInner);
    assertEquals("a=b=c", generate(assignOuter));

    // Non-associative operator: a - (b - c) -> parentheses required on RHS
    Node subInner = new Node(Token.SUB, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node subOuter = new Node(Token.SUB, Node.newString(Token.NAME, "a"), subInner);
    assertEquals("a-(b-c)", generate(subOuter));
  }

  @Test(timeout = 4000)
  public void testInOperatorInForInitClauseContext() {
    // `in` operator inside for init clause requires parens
    Node inNode = new Node(Token.IN, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y"));
    String result = generate(inNode, CodeGenerator.Context.IN_FOR_INIT_CLAUSE);
    assertEquals("(x in y)", result);
  }

  @Test(timeout = 4000)
  public void testUnaryOperators() {
    assertEquals("!x", generate(new Node(Token.NOT, Node.newString(Token.NAME, "x"))));
    assertEquals("typeof x", generate(new Node(Token.TYPEOF, Node.newString(Token.NAME, "x"))));
    assertEquals("void 0", generate(new Node(Token.VOID, Node.newNumber(0))));
    assertEquals("~x", generate(new Node(Token.BITNOT, Node.newString(Token.NAME, "x"))));
    assertEquals("+x", generate(new Node(Token.POS, Node.newString(Token.NAME, "x"))));

    // Token.NEG: Number child vs non-number child
    assertEquals("-5", generate(new Node(Token.NEG, Node.newNumber(5))));
    assertEquals("-x", generate(new Node(Token.NEG, Node.newString(Token.NAME, "x"))));
  }

  @Test(timeout = 4000)
  public void testIncDecPreAndPost() {
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
  public void testHookTernaryOperator() {
    Node hook = new Node(Token.HOOK,
        Node.newString(Token.NAME, "cond"),
        Node.newNumber(1),
        Node.newNumber(2));
    assertEquals("cond?1:2", generate(hook));
  }

  // =========================================================================
  // Partition C: AST Node Syntax Trees Coverage
  // =========================================================================

  @Test(timeout = 4000)
  public void testTryCatchFinally() {
    // Try - Catch
    Node tryBlock1 = new Node(Token.BLOCK);
    Node catchBlock1 = new Node(Token.BLOCK,
        new Node(Token.CATCH, Node.newString(Token.NAME, "e"), new Node(Token.BLOCK)));
    Node tryNode1 = new Node(Token.TRY, tryBlock1, catchBlock1);
    assertEquals("try{}catch(e){}", generate(tryNode1));

    // Try - Catch - Finally
    Node tryBlock2 = new Node(Token.BLOCK);
    Node catchBlock2 = new Node(Token.BLOCK,
        new Node(Token.CATCH, Node.newString(Token.NAME, "err"), new Node(Token.BLOCK)));
    Node finallyBlock2 = new Node(Token.BLOCK);
    Node tryNode2 = new Node(Token.TRY, tryBlock2, catchBlock2, finallyBlock2);
    assertEquals("try{}catch(err){}finally{}", generate(tryNode2));
  }

  @Test(timeout = 4000)
  public void testThrowAndReturn() {
    Node throwNode = new Node(Token.THROW, Node.newString(Token.NAME, "e"));
    assertEquals("throw e;", generate(throwNode));

    Node returnEmpty = new Node(Token.RETURN);
    assertEquals("return;", generate(returnEmpty));

    Node returnVal = new Node(Token.RETURN, Node.newNumber(10));
    assertEquals("return 10;", generate(returnVal));
  }

  @Test(timeout = 4000)
  public void testVarAndNameNodes() {
    // Empty VAR
    assertEquals("", generate(new Node(Token.VAR)));

    // var x;
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "x"));
    assertEquals("var x", generate(varNode));

    // var x = 1;
    Node varWithInit = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "x");
    nameNode.addChildToBack(Node.newNumber(1));
    varWithInit.addChildToBack(nameNode);
    assertEquals("var x=1", generate(varWithInit));

    // var x = (1, 2) [COMMA child of NAME]
    Node nameComma = Node.newString(Token.NAME, "x");
    nameComma.addChildToBack(new Node(Token.COMMA, Node.newNumber(1), Node.newNumber(2)));
    assertEquals("x=(1,2)", generate(nameComma));
  }

  @Test(timeout = 4000)
  public void testArrayLitWithHoles() {
    // [1, 2]
    Node arr = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newNumber(2));
    assertEquals("[1,2]", generate(arr));

    // Empty array: []
    assertEquals("[]", generate(new Node(Token.ARRAYLIT)));

    // Sparse array with trailing empty: [1, ,]
    Node sparseArr = new Node(Token.ARRAYLIT,
        Node.newNumber(1),
        new Node(Token.EMPTY));
    assertEquals("[1,,]", generate(sparseArr));
  }

  @Test(timeout = 4000)
  public void testRegexpNodes() {
    Node reg1 = new Node(Token.REGEXP, Node.newString("abc"));
    assertEquals("/abc/", generate(reg1));

    Node reg2 = new Node(Token.REGEXP, Node.newString("abc"), Node.newString("gi"));
    assertEquals("/abc/gi", generate(reg2));
  }

  @Test(timeout = 4000)
  public void testFunctionContexts() {
    Node fn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, "foo"),
        new Node(Token.LP),
        new Node(Token.BLOCK));

    // Normal statement context
    assertEquals("function foo(){}", generate(fn, CodeGenerator.Context.STATEMENT));

    // START_OF_EXPR context requires parentheses
    assertEquals("(function foo(){})", generate(fn, CodeGenerator.Context.START_OF_EXPR));
  }

  @Test(timeout = 4000)
  public void testGetAndSetInObjectLit() {
    // Getter
    Node getFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.LP),
        new Node(Token.BLOCK));
    Node getNode = Node.newString(Token.GET, "prop");
    getNode.addChildToBack(getFn);

    // Setter
    Node setFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.LP, Node.newString(Token.NAME, "val")),
        new Node(Token.BLOCK));
    Node setNode = Node.newString(Token.SET, "prop");
    setNode.addChildToBack(setFn);

    Node objLit = new Node(Token.OBJECTLIT, getNode, setNode);
    assertEquals("{get prop(){},set prop(val){}}", generate(objLit));
  }

  @Test(timeout = 4000)
  public void testObjectLitKeys() {
    // Identifier key
    Node k1 = Node.newString("a");
    k1.addChildToBack(Node.newNumber(1));

    // Number key
    Node k2 = Node.newString("123");
    k2.addChildToBack(Node.newNumber(2));

    // Quoted string / keyword key
    Node k3 = Node.newString("default");
    k3.addChildToBack(Node.newNumber(3));

    Node obj = new Node(Token.OBJECTLIT, k1, k2, k3);
    assertEquals("{a:1,123:2,\"default\":3}", generate(obj));

    // START_OF_EXPR context wraps object lit in parens
    assertEquals("({a:1,123:2,\"default\":3})", generate(obj, CodeGenerator.Context.START_OF_EXPR));
  }

  @Test(timeout = 4000)
  public void testControlFlowStatements() {
    // FOR 4-child
    Node for4 = new Node(Token.FOR,
        new Node(Token.VAR, Node.newString(Token.NAME, "i")),
        Node.newString(Token.NAME, "i"),
        Node.newString(Token.NAME, "i"),
        new Node(Token.BLOCK));
    assertEquals("for(var i;i;i);", generate(for4));

    // FOR 3-child (for-in)
    Node forIn = new Node(Token.FOR,
        Node.newString(Token.NAME, "x"),
        Node.newString(Token.NAME, "obj"),
        new Node(Token.BLOCK));
    assertEquals("for(x in obj);", generate(forIn));

    // WHILE
    Node whileNode = new Node(Token.WHILE,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.BLOCK));
    assertEquals("while(cond);", generate(whileNode));

    // DO-WHILE
    Node doNode = new Node(Token.DO,
        new Node(Token.BLOCK),
        Node.newString(Token.NAME, "cond"));
    assertEquals("do;while(cond);", generate(doNode));

    // WITH
    Node withNode = new Node(Token.WITH,
        Node.newString(Token.NAME, "ctx"),
        new Node(Token.BLOCK));
    assertEquals("with(ctx);", generate(withNode));
  }

  @Test(timeout = 4000)
  public void testIfElseAndDanglingElse() {
    // If without else
    Node ifNode = new Node(Token.IF,
        Node.newString(Token.NAME, "c"),
        new Node(Token.BLOCK));
    assertEquals("if(c);", generate(ifNode));

    // If with else
    Node ifElse = new Node(Token.IF,
        Node.newString(Token.NAME, "c"),
        new Node(Token.BLOCK),
        new Node(Token.BLOCK));
    assertEquals("if(c);else;", generate(ifElse));

    // Dangling else ambiguity
    String ambiguous = generate(ifNode, CodeGenerator.Context.BEFORE_DANGLING_ELSE);
    assertEquals("{if(c);}", ambiguous);
  }

  @Test(timeout = 4000)
  public void testCallSpecialCases() {
    // Normal call: foo(1, 2)
    Node normalCall = new Node(Token.CALL,
        Node.newString(Token.NAME, "foo"),
        Node.newNumber(1),
        Node.newNumber(2));
    assertEquals("foo(1,2)", generate(normalCall));

    // Indirect eval: eval("code") without DIRECT_EVAL prop
    Node evalCall = new Node(Token.CALL,
        Node.newString(Token.NAME, "eval"),
        Node.newString("code"));
    assertEquals("(0,eval)(\"code\")", generate(evalCall));

    // Free call: (0, obj.method)()
    Node getprop = new Node(Token.GETPROP,
        Node.newString(Token.NAME, "obj"),
        Node.newString("method"));
    Node freeCall = new Node(Token.CALL, getprop);
    freeCall.putBooleanProp(Node.FREE_CALL, true);
    assertEquals("(0,obj.method)()", generate(freeCall));
  }

  @Test(timeout = 4000)
  public void testNewExpressions() {
    // new Foo() with arguments
    Node newWithArgs = new Node(Token.NEW,
        Node.newString(Token.NAME, "Foo"),
        Node.newNumber(1));
    assertEquals("new Foo(1)", generate(newWithArgs));

    // new Foo without arguments
    Node newNoArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    assertEquals("new Foo", generate(newNoArgs));

    // new with target containing CALL: new (foo())()
    Node callTarget = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    Node newWithCall = new Node(Token.NEW, callTarget);
    assertEquals("new (foo())", generate(newWithCall));
  }

  @Test(timeout = 4000)
  public void testSwitchCaseDefault() {
    Node case1 = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK));
    Node def = new Node(Token.DEFAULT, new Node(Token.BLOCK));
    Node sw = new Node(Token.SWITCH, Node.newString(Token.NAME, "x"), case1, def);
    assertEquals("switch(x){case 1:default:}", generate(sw));
  }

  @Test(timeout = 4000)
  public void testLabelsBreakContinue() {
    Node lblName = Node.newString(Token.LABEL_NAME, "loop");
    Node label = new Node(Token.LABEL, lblName, new Node(Token.BLOCK));
    assertEquals("loop:;", generate(label));

    assertEquals("break;", generate(new Node(Token.BREAK)));
    assertEquals("break loop;", generate(new Node(Token.BREAK, Node.newString(Token.LABEL_NAME, "loop"))));

    assertEquals("continue;", generate(new Node(Token.CONTINUE)));
    assertEquals("continue loop;", generate(new Node(Token.CONTINUE, Node.newString(Token.LABEL_NAME, "loop"))));

    assertEquals("debugger;", generate(new Node(Token.DEBUGGER)));
  }

  @Test(timeout = 4000)
  public void testSimpleLiteralsAndProperties() {
    assertEquals("null", generate(new Node(Token.NULL)));
    assertEquals("this", generate(new Node(Token.THIS)));
    assertEquals("false", generate(new Node(Token.FALSE)));
    assertEquals("true", generate(new Node(Token.TRUE)));
    assertEquals("delete x", generate(new Node(Token.DELPROP, Node.newString(Token.NAME, "x"))));

    // GETPROP with number needs parens: (1).toString
    Node numProp = new Node(Token.GETPROP, Node.newNumber(1), Node.newString("toString"));
    assertEquals("(1).toString", generate(numProp));

    // GETELEM: a[1]
    Node getelem = new Node(Token.GETELEM, Node.newString(Token.NAME, "a"), Node.newNumber(1));
    assertEquals("a[1]", generate(getelem));

    // REF_SPECIAL
    Node refSpecial = new Node(Token.REF_SPECIAL, Node.newString(Token.NAME, "x"));
    refSpecial.putProp(Node.NAME_PROP, "specialProp");
    assertEquals("x.specialProp", generate(refSpecial));

    // GET_REF
    Node getRef = new Node(Token.GET_REF, Node.newString(Token.NAME, "ref"));
    assertEquals("ref", generate(getRef));

    // SETNAME is a no-op
    assertEquals("", generate(new Node(Token.SETNAME)));
  }

  // =========================================================================
  // Partition D: Defensive & Exception Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBinaryOperatorInvalidChildCount() {
    Node badAdd = new Node(Token.ADD, Node.newNumber(1));
    generate(badAdd);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testRegexpNonStringChildrenThrows() {
    Node badReg = new Node(Token.REGEXP, Node.newNumber(1));
    generate(badReg);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testExprVoidThrows() {
    generate(new Node(Token.EXPR_VOID));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testUnknownTokenTypeThrows() {
    generate(new Node(Token.EMPTY + 9999));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testBreakInvalidTokenThrows() {
    generate(new Node(Token.BREAK, Node.newNumber(1)));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testContinueInvalidTokenThrows() {
    generate(new Node(Token.CONTINUE, Node.newNumber(1)));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testLabelInvalidFirstChildThrows() {
    generate(new Node(Token.LABEL, Node.newNumber(1), new Node(Token.BLOCK)));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testFunctionSubclassedNodeThrows() {
    Node subFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, "f"),
        new Node(Token.LP),
        new Node(Token.BLOCK)) {};
    generate(subFn);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testBlockSubclassedNodeThrows() {
    Node subBlock = new Node(Token.BLOCK) {};
    generate(subBlock);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testStringUnexpectedChildrenThrows() {
    Node strNode = Node.newString("str");
    strNode.addChildToBack(Node.newNumber(1));
    generate(strNode);
  }
}