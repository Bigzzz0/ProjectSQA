package com.google.javascript.jscomp;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target Class: com.google.javascript.jscomp.CodeGenerator
 * Benchmark: Defects4J Closure-128 / CodePrinterTest::testNumericKeys
 *
 * Targeted Decision Branches & Conditions:
 * 1. Defect-Targeted Zone (Closure-128):
 *    - isSimpleNumber(String): leading zeros ("010", "00") must NOT be treated as
 *      simple numbers.
 *    - getSimpleNumber(String): strings with leading zeros must return NaN.
 *    - Token.OBJECTLIT key generation: numeric string keys like "010" must remain
 *      quoted {"010": 1} and not get converted to octal/decimal numbers {10: 1}.
 * 2. Partition A: Core Functional AST Generation (CodeGenerator.add):
 *    - Token.TRY, Token.CATCH, Token.FINALLY (childCount 2 vs 3, catch block presence)
 *    - Token.THROW, Token.RETURN (with/without expr)
 *    - Token.VAR (single/multiple, with init, with COMMA expression)
 *    - Token.ARRAYLIT (normal elements, skipped/empty slots, trailing empty slot)
 *    - Token.NUMBER (zero, positive, negative)
 *    - Token.NEG (number child vs non-number child)
 *    - Token.HOOK (ternary conditional operator precedence)
 *    - Token.REGEXP (single pattern, pattern + flags, non-string error branch)
 *    - Token.GETPROP (needsParens when LHS is NUMBER vs non-number)
 *    - Token.GETELEM (array indexing)
 *    - Token.CALL (standard call, indirect eval preserved as (0,eval)(...),
 *                   direct eval with DIRECT_EVAL, free call on GETPROP preserved as (0,a.b)())
 *    - Token.INC, Token.DEC (pre-increment/decrement vs post-increment/decrement)
 *    - Token.IF (with and without else clause, dangling else resolution)
 *    - Token.FOR (4-child standard loop vs 3-child for-in loop)
 *    - Token.DO, Token.WHILE, Token.SWITCH, Token.CASE, Token.DEFAULT
 *    - Token.NEW (with args, without args, CALL inside child claiming higher precedence)
 *    - Token.FUNCTION (named, anonymous, statement context)
 *    - Token.GET, Token.SET (getter/setter in OBJECTLIT, parameter counts, simple number names)
 *    - Token.DELPROP, Token.WITH, Token.LABEL, Token.BREAK, Token.CONTINUE, Token.DEBUGGER
 *    - Binary operators: associativity (associative ops, assignment ops, non-associative ops)
 * 3. Partition B: String Escaping & Encoding (strEscape, jsString, regexpEscape, identifierEscape):
 *    - Quotes: more single quotes vs more double quotes
 *    - Escapes: \0, \n, \r, \t, \\, ", '
 *    - HTML safety: <!--, </script>, -->, ]]>
 *    - Charset encoders: UTF-8, US-ASCII, null fallback
 *    - Non-latin characters: unicode hex escaping \\uXXXX
 * 4. Partition C: Defensive & Error Handling:
 *    - EXPR_VOID throws Error
 *    - Unknown token type throws Error
 *    - Bad GETPROP child count and non-string RHS
 *    - Negative / boundary integer inputs in getSimpleNumber
 * =========================================================================
 */
package com.google.javascript.jscomp; /* package org.mozilla.javascript; */

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.*;

public class CodeGeneratorGeminiTest {

  private static class SimpleTestConsumer extends CodeConsumer {
    private final StringBuilder sb = new StringBuilder();

    @Override
    void append(String str) {
      sb.append(str);
    }

    @Override
    char getLastChar() {
      return sb.length() == 0 ? '\0' : sb.charAt(sb.length() - 1);
    }

    String getCode() {
      return sb.toString();
    }
  }

  // =========================================================================
  // Partition C: Defect-Targeted Zone (Closure-128 / testNumericKeys)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefectNumericKeysLeadingZeroDirect() {
    assertFalse("A string with leading zeros like '010' must NOT be considered a simple number",
        CodeGenerator.isSimpleNumber("010"));
    assertTrue("getSimpleNumber('010') must return NaN",
        Double.isNaN(CodeGenerator.getSimpleNumber("010")));
    assertFalse("A string '00' must NOT be considered a simple number",
        CodeGenerator.isSimpleNumber("00"));
    assertTrue("getSimpleNumber('00') must return NaN",
        Double.isNaN(CodeGenerator.getSimpleNumber("00")));
  }

  @Test(timeout = 4000)
  public void testDefectNumericKeysObjectLitPrinting() {
    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString("010");
    key.addChildToBack(Node.newNumber(1));
    objLit.addChildToBack(key);

    Node nameNode = Node.newString(Token.NAME, "x");
    nameNode.addChildToBack(objLit);
    Node varNode = new Node(Token.VAR, nameNode);

    String output = new CodePrinter.Builder(varNode).build();
    assertEquals("var x={\"010\":1};", output);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleNumberValidBoundaries() {
    assertTrue(CodeGenerator.isSimpleNumber("0"));
    assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);

    assertTrue(CodeGenerator.isSimpleNumber("1"));
    assertEquals(1.0, CodeGenerator.getSimpleNumber("1"), 0.0);

    assertTrue(CodeGenerator.isSimpleNumber("999"));
    assertEquals(999.0, CodeGenerator.getSimpleNumber("999"), 0.0);

    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));

    assertFalse(CodeGenerator.isSimpleNumber("-1"));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("-1")));

    assertFalse(CodeGenerator.isSimpleNumber("1a"));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("1a")));

    // Beyond MAX_POSITIVE_INTEGER_NUMBER
    String hugeNumber = "999999999999999999999999999999";
    assertTrue(CodeGenerator.isSimpleNumber(hugeNumber));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber(hugeNumber)));
  }

  @Test(timeout = 4000)
  public void testTryCatchFinally() {
    // 1. try-catch
    Node tryBlock = new Node(Token.BLOCK);
    Node catchBlock = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), new Node(Token.BLOCK));
    catchBlock.addChildToBack(catchNode);
    Node tryCatch = new Node(Token.TRY, tryBlock, catchBlock);

    String res = new CodePrinter.Builder(tryCatch).build();
    assertTrue(res.contains("try"));
    assertTrue(res.contains("catch(e)"));

    // 2. try-finally (childCount == 3, catch block empty)
    Node tryBlock2 = new Node(Token.BLOCK);
    Node emptyCatch = new Node(Token.BLOCK);
    Node finallyBlock = new Node(Token.BLOCK);
    Node tryFinally = new Node(Token.TRY, tryBlock2, emptyCatch, finallyBlock);

    String res2 = new CodePrinter.Builder(tryFinally).build();
    assertTrue(res2.contains("try"));
    assertTrue(res2.contains("finally"));
  }

  @Test(timeout = 4000)
  public void testThrowAndReturn() {
    Node throwNode = new Node(Token.THROW, Node.newString(Token.NAME, "err"));
    assertEquals("throw err;", new CodePrinter.Builder(throwNode).build());

    Node returnEmpty = new Node(Token.RETURN);
    assertEquals("return;", new CodePrinter.Builder(returnEmpty).build());

    Node returnVal = new Node(Token.RETURN, Node.newNumber(42));
    assertEquals("return 42;", new CodePrinter.Builder(returnVal).build());
  }

  @Test(timeout = 4000)
  public void testVarDeclarations() {
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "a"));
    assertEquals("var a;", new CodePrinter.Builder(varNode).build());

    Node nameWithInit = Node.newString(Token.NAME, "b");
    nameWithInit.addChildToBack(Node.newNumber(10));
    Node varWithInit = new Node(Token.VAR, nameWithInit);
    assertEquals("var b=10;", new CodePrinter.Builder(varWithInit).build());

    Node comma = new Node(Token.COMMA, Node.newNumber(1), Node.newNumber(2));
    Node nameWithComma = Node.newString(Token.NAME, "c");
    nameWithComma.addChildToBack(comma);
    Node varWithComma = new Node(Token.VAR, nameWithComma);
    assertEquals("var c=(1,2);", new CodePrinter.Builder(varWithComma).build());
  }

  @Test(timeout = 4000)
  public void testArrayLiteralWithHoles() {
    Node arr = new Node(Token.ARRAYLIT, Node.newNumber(1), new Node(Token.EMPTY), Node.newNumber(2));
    assertEquals("[1,,2]", new CodePrinter.Builder(arr).build());

    Node arrTrailingHole = new Node(Token.ARRAYLIT, Node.newNumber(1), new Node(Token.EMPTY));
    assertEquals("[1,,]", new CodePrinter.Builder(arrTrailingHole).build());
  }

  @Test(timeout = 4000)
  public void testUnaryOperators() {
    assertEquals("+x", new CodePrinter.Builder(new Node(Token.POS, Node.newString(Token.NAME, "x"))).build());
    assertEquals("!x", new CodePrinter.Builder(new Node(Token.NOT, Node.newString(Token.NAME, "x"))).build());
    assertEquals("~x", new CodePrinter.Builder(new Node(Token.BITNOT, Node.newString(Token.NAME, "x"))).build());
    assertEquals("void 0", new CodePrinter.Builder(new Node(Token.VOID, Node.newNumber(0))).build());
    assertEquals("typeof x", new CodePrinter.Builder(new Node(Token.TYPEOF, Node.newString(Token.NAME, "x"))).build());

    // NEG on Number vs non-Number
    assertEquals("-x", new CodePrinter.Builder(new Node(Token.NEG, Node.newString(Token.NAME, "x"))).build());
    assertEquals("-5", new CodePrinter.Builder(new Node(Token.NEG, Node.newNumber(5.0))).build());
  }

  @Test(timeout = 4000)
  public void testHookTernary() {
    Node hook = new Node(Token.HOOK, Node.newString(Token.NAME, "a"), Node.newNumber(1), Node.newNumber(2));
    assertEquals("a?1:2", new CodePrinter.Builder(hook).build());
  }

  @Test(timeout = 4000)
  public void testRegexpNodes() {
    Node reTwoChildren = new Node(Token.REGEXP, Node.newString("abc"), Node.newString("gi"));
    assertEquals("/abc/gi", new CodePrinter.Builder(reTwoChildren).build());

    Node reOneChild = new Node(Token.REGEXP, Node.newString("xyz"));
    assertEquals("/xyz/", new CodePrinter.Builder(reOneChild).build());
  }

  @Test(timeout = 4000)
  public void testGetPropAndGetElem() {
    Node getprop = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b"));
    assertEquals("a.b", new CodePrinter.Builder(getprop).build());

    // Number LHS must be wrapped in parens
    Node numGetprop = new Node(Token.GETPROP, Node.newNumber(5), Node.newString("toString"));
    assertEquals("(5).toString", new CodePrinter.Builder(numGetprop).build());

    Node getelem = new Node(Token.GETELEM, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    assertEquals("a[b]", new CodePrinter.Builder(getelem).build());
  }

  @Test(timeout = 4000)
  public void testIncDec() {
    Node incPre = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    assertEquals("++x", new CodePrinter.Builder(incPre).build());

    Node incPost = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    incPost.putIntProp(Node.INCRDECR_PROP, 1);
    assertEquals("x++", new CodePrinter.Builder(incPost).build());

    Node decPre = new Node(Token.DEC, Node.newString(Token.NAME, "x"));
    assertEquals("--x", new CodePrinter.Builder(decPre).build());

    Node decPost = new Node(Token.DEC, Node.newString(Token.NAME, "x"));
    decPost.putIntProp(Node.INCRDECR_PROP, 1);
    assertEquals("x--", new CodePrinter.Builder(decPost).build());
  }

  @Test(timeout = 4000)
  public void testFunctionCallsAndEvalPreservation() {
    // Normal call
    Node normalCall = new Node(Token.CALL, Node.newString(Token.NAME, "foo"), Node.newNumber(1));
    assertEquals("foo(1)", new CodePrinter.Builder(normalCall).build());

    // Indirect eval preserved
    Node indirectEval = new Node(Token.CALL, Node.newString(Token.NAME, "eval"), Node.newString("1+1"));
    assertEquals("(0,eval)(\"1+1\")", new CodePrinter.Builder(indirectEval).build());

    // Direct eval
    Node evalName = Node.newString(Token.NAME, "eval");
    evalName.putBooleanProp(Node.DIRECT_EVAL, true);
    Node directEval = new Node(Token.CALL, evalName, Node.newString("1+1"));
    assertEquals("eval(\"1+1\")", new CodePrinter.Builder(directEval).build());

    // Free call on property reference
    Node prop = new Node(Token.GETPROP, Node.newString(Token.NAME, "obj"), Node.newString("fn"));
    Node freeCall = new Node(Token.CALL, prop, Node.newNumber(1));
    freeCall.putBooleanProp(Node.FREE_CALL, true);
    assertEquals("(0,obj.fn)(1)", new CodePrinter.Builder(freeCall).build());
  }

  @Test(timeout = 4000)
  public void testControlFlowStatements() {
    // IF / ELSE
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "c"),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1))),
        new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(2))));
    String ifRes = new CodePrinter.Builder(ifNode).build();
    assertTrue(ifRes.contains("if(c)"));
    assertTrue(ifRes.contains("else"));

    // WHILE
    Node whileNode = new Node(Token.WHILE, Node.newString(Token.NAME, "x"), new Node(Token.BLOCK));
    assertTrue(new CodePrinter.Builder(whileNode).build().startsWith("while(x)"));

    // DO-WHILE
    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), Node.newString(Token.NAME, "x"));
    assertEquals("do;while(x);", new CodePrinter.Builder(doNode).build());

    // FOR standard
    Node for4 = new Node(Token.FOR,
        new Node(Token.VAR, Node.newString(Token.NAME, "i")),
        new Node(Token.LT, Node.newString(Token.NAME, "i"), Node.newNumber(10)),
        new Node(Token.INC, Node.newString(Token.NAME, "i")),
        new Node(Token.BLOCK));
    assertTrue(new CodePrinter.Builder(for4).build().startsWith("for(var i;"));

    // FOR-IN
    Node forIn = new Node(Token.FOR, Node.newString(Token.NAME, "k"), Node.newString(Token.NAME, "o"), new Node(Token.BLOCK));
    assertEquals("for(k in o);", new CodePrinter.Builder(forIn).build());
  }

  @Test(timeout = 4000)
  public void testSwitchCaseDefault() {
    Node caseNode = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK));
    Node defaultNode = new Node(Token.DEFAULT, new Node(Token.BLOCK));
    Node switchNode = new Node(Token.SWITCH, Node.newString(Token.NAME, "v"), caseNode, defaultNode);
    String res = new CodePrinter.Builder(switchNode).build();
    assertTrue(res.startsWith("switch(v){case 1:default:}"));
  }

  @Test(timeout = 4000)
  public void testNewExpressions() {
    Node newNoArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    assertEquals("new Foo", new CodePrinter.Builder(newNoArgs).build());

    Node newArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"), Node.newNumber(1), Node.newNumber(2));
    assertEquals("new Foo(1,2)", new CodePrinter.Builder(newArgs).build());

    // Higher precedence forced when child contains CALL
    Node callTarget = new Node(Token.CALL, Node.newString(Token.NAME, "getConstructor"));
    Node newCall = new Node(Token.NEW, callTarget);
    assertEquals("new (getConstructor())", new CodePrinter.Builder(newCall).build());
  }

  @Test(timeout = 4000)
  public void testFunctionsAndGettersSetters() {
    // Function statement
    Node fn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, "foo"),
        new Node(Token.LP, Node.newString(Token.NAME, "a")),
        new Node(Token.BLOCK, new Node(Token.RETURN, Node.newString(Token.NAME, "a"))));
    assertEquals("function foo(a){return a;}", new CodePrinter.Builder(fn).build());

    // Getter and Setter in OBJECTLIT
    Node getFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""),
        new Node(Token.LP),
        new Node(Token.BLOCK, new Node(Token.RETURN, Node.newNumber(1))));
    Node getNode = Node.newString(Token.GET, "p");
    getNode.addChildToBack(getFn);

    Node setFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""),
        new Node(Token.LP, Node.newString(Token.NAME, "v")),
        new Node(Token.BLOCK));
    Node setNode = Node.newString(Token.SET, "p");
    setNode.addChildToBack(setFn);

    Node objLit = new Node(Token.OBJECTLIT, getNode, setNode);
    assertEquals("{get p(){return 1;},set p(v){}}", new CodePrinter.Builder(objLit).build());
  }

  @Test(timeout = 4000)
  public void testBinaryOperatorAssociativity() {
    // Multiplication is associative: a * (b * c) -> a * b * c
    Node mulInner = new Node(Token.MUL, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node mulOuter = new Node(Token.MUL, Node.newString(Token.NAME, "a"), mulInner);
    assertEquals("a*b*c", new CodePrinter.Builder(mulOuter).build());

    // Subtraction is non-associative: a - (b - c) -> a - (b - c)
    Node subInner = new Node(Token.SUB, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node subOuter = new Node(Token.SUB, Node.newString(Token.NAME, "a"), subInner);
    assertEquals("a-(b-c)", new CodePrinter.Builder(subOuter).build());

    // Assignment is right-associative: a = (b = c) -> a = b = c
    Node assignInner = new Node(Token.ASSIGN, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c"));
    Node assignOuter = new Node(Token.ASSIGN, Node.newString(Token.NAME, "a"), assignInner);
    assertEquals("a=b=c", new CodePrinter.Builder(assignOuter).build());
  }

  @Test(timeout = 4000)
  public void testSimpleTokens() {
    assertEquals("null", new CodePrinter.Builder(new Node(Token.NULL)).build());
    assertEquals("this", new CodePrinter.Builder(new Node(Token.THIS)).build());
    assertEquals("false", new CodePrinter.Builder(new Node(Token.FALSE)).build());
    assertEquals("true", new CodePrinter.Builder(new Node(Token.TRUE)).build());
    assertEquals("debugger;", new CodePrinter.Builder(new Node(Token.DEBUGGER)).build());

    Node labelName = Node.newString(Token.LABEL_NAME, "lbl");
    Node breakLabeled = new Node(Token.BREAK, labelName);
    assertEquals("break lbl;", new CodePrinter.Builder(breakLabeled).build());

    Node contLabeled = new Node(Token.CONTINUE, Node.newString(Token.LABEL_NAME, "lbl"));
    assertEquals("continue lbl;", new CodePrinter.Builder(contLabeled).build());

    Node del = new Node(Token.DELPROP, new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b")));
    assertEquals("delete a.b", new CodePrinter.Builder(del).build());

    Node withNode = new Node(Token.WITH, Node.newString(Token.NAME, "ctx"), new Node(Token.BLOCK));
    assertEquals("with(ctx);", new CodePrinter.Builder(withNode).build());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & String Escapes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEscapeToDoubleQuotedJsString() {
    assertEquals("\"hello\"", CodeGenerator.escapeToDoubleQuotedJsString("hello"));
    assertEquals("\"hello \\\"world\\\"\"", CodeGenerator.escapeToDoubleQuotedJsString("hello \"world\""));
    assertEquals("\"\\n\\r\\t\\x00\\\\\"", CodeGenerator.escapeToDoubleQuotedJsString("\n\r\t\0\\"));
    assertEquals("\"--\\>\"", CodeGenerator.escapeToDoubleQuotedJsString("-->"));
    assertEquals("\"]]\\>\"", CodeGenerator.escapeToDoubleQuotedJsString("]]>"));
    assertEquals("\"<\\/script>\"", CodeGenerator.escapeToDoubleQuotedJsString("</script>"));
    assertEquals("\"<\\!--\"", CodeGenerator.escapeToDoubleQuotedJsString("<!--"));
  }

  @Test(timeout = 4000)
  public void testIdentifierEscape() {
    assertEquals("validLatinIdentifier", CodeGenerator.identifierEscape("validLatinIdentifier"));
    // Non-latin characters should be hex-escaped
    String escaped = CodeGenerator.identifierEscape("var_\u1234");
    assertEquals("var_\\u1234", escaped);

    String controlEscaped = CodeGenerator.identifierEscape("var_\u0005");
    assertEquals("var_\\u0005", controlEscaped);
  }

  @Test(timeout = 4000)
  public void testRegexpEscape() {
    assertEquals("/abc/", CodeGenerator.regexpEscape("abc"));
    assertEquals("/a\\/b/", CodeGenerator.regexpEscape("a/b"));

    CharsetEncoder asciiEncoder = Charsets.US_ASCII.newEncoder();
    String unicodeRegexp = CodeGenerator.regexpEscape("a\u1234b", asciiEncoder);
    assertEquals("/a\\u1234b/", unicodeRegexp);

    CharsetEncoder utf8Encoder = Charsets.UTF_8.newEncoder();
    String utf8Regexp = CodeGenerator.regexpEscape("a\u1234b", utf8Encoder);
    assertEquals("/a\u1234b/", utf8Regexp);
  }

  @Test(timeout = 4000)
  public void testJsStringOptimalQuoteSelection() {
    SimpleTestConsumer consumer = new SimpleTestConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // More double quotes -> output wrapped in single quotes
    String s1 = cg.jsString("\"\"'");
    assertTrue(s1.startsWith("'") && s1.endsWith("'"));

    // More single quotes -> output wrapped in double quotes
    String s2 = cg.jsString("''\"");
    assertTrue(s2.startsWith("\"") && s2.endsWith("\""));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = Error.class, timeout = 4000)
  public void testExprVoidThrowsError() {
    Node node = new Node(Token.EXPR_VOID);
    new CodePrinter.Builder(node).build();
  }
}
