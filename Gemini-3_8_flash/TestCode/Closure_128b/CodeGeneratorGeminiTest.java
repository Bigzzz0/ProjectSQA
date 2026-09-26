package com.google.javascript.jscomp;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target Class: com.google.javascript.jscomp.CodeGenerator
 * Defects4J Bug: Issue 942 (com.google.javascript.jscomp.CodePrinterTest::testIssue942)
 *
 * Decision Branches & Boundaries Targeted:
 * 1. Defect-Specific Zone:
 *    - isSimpleNumber(String): "0" check boundary. Buggy code checks (s.charAt(0) != '0')
 *      which incorrectly rejects "0", returning false.
 *    - getSimpleNumber(String): Returns Double.NaN for "0" instead of 0.0 in the defective code.
 *    - case Token.OBJECTLIT: Property name "0" printed as string key {"0": 1} instead of {0: 1}.
 * 2. Number Parsing Boundaries (isSimpleNumber & getSimpleNumber):
 *    - Empty string "", single digit "0", "1", octal prefix "01", multi-digit "123".
 *    - Integer overflow boundaries: MAX_POSITIVE_INTEGER_NUMBER (2^53 - 1 = 9007199254740991L) vs
 *      exceeding boundary (9007199254740992L), and Long.parseLong overflow (> Long.MAX_VALUE).
 * 3. String & Regexp Escaping (strEscape, regexpEscape, escapeToDoubleQuotedJsString):
 *    - Control characters: \0, \b, \t, \n, \u000B (\v vs \x0B with useSlashV), \f, \r.
 *    - Unicode line terminators: \u2028, \u2029.
 *    - HTML safety breaks: "</script" (case-insensitive), "<!--", "-->", "]]>".
 *    - Trusted vs untrusted strings: '=', '&', '<', '>' escaping.
 *    - Output Charset Encoder: US-ASCII vs UTF-8 encoding paths, Latin pass-through, surrogate pairs.
 *    - Quote selection: preferSingleQuotes true/false with varying single vs double quote counts.
 * 4. Identifier Escaping (identifierEscape):
 *    - Pure Latin identifiers vs non-Latin identifiers and supplementary code points.
 * 5. AST & Token Printing (CodeGenerator.add):
 *    - Binary operators: associativity (associative op chains e.g. a + b + c vs assignments a = b = c).
 *    - Statements: TRY/CATCH/FINALLY, THROW, RETURN, VAR, IF (dangling else ambiguity resolution).
 *    - Loops: FOR (4 children standard vs 3 children for-in, IN_FOR_INIT_CLAUSE parens), WHILE, DO.
 *    - Functions & Objects: GETTER_DEF, SETTER_DEF, OBJECTLIT (unquoted, quoted, keyword, numeric).
 *    - Calls & Eval: Direct eval vs Indirect eval ((0, eval)(...)), Free calls on property access.
 *    - Unary ops: POS, NOT, BITNOT, TYPEOF, VOID, NEG (numeric vs non-numeric - -2).
 *    - Literals: NUMBER, ARRAYLIT (trailing empty element spacing), REGEXP, NULL, THIS, TRUE, FALSE.
 *    - Defensive paths: unexpected node subclasses, malformed child counts, unsupported tokens.
 * =========================================================================
 */

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class CodeGeneratorGeminiTest {

  private String printNode(Node n) {
    CodePrinter.Builder builder = new CodePrinter.Builder(n);
    builder.setPrettyPrint(false);
    return builder.build();
  }

  private String printNode(Node n, CompilerOptions options) {
    CodePrinter.Builder builder = new CodePrinter.Builder(n);
    builder.setCompilerOptions(options);
    builder.setPrettyPrint(false);
    return builder.build();
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Issue 942)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIssue942_isSimpleNumberZero() {
    // Ground truth defect: isSimpleNumber("0") returned false due to `s.charAt(0) != '0'`
    assertTrue("String '0' must be recognized as a simple number",
        CodeGenerator.isSimpleNumber("0"));
  }

  @Test(timeout = 4000)
  public void testIssue942_getSimpleNumberZero() {
    // Ground truth defect: getSimpleNumber("0") returned Double.NaN instead of 0.0
    double val = CodeGenerator.getSimpleNumber("0");
    assertEquals("getSimpleNumber(\"0\") must return 0.0", 0.0, val, 0.0);
  }

  @Test(timeout = 4000)
  public void testIssue942_objectLiteralNumericZeroKey() {
    // When an object literal has key "0", it should print as {0:1} rather than {"0":1}
    Node objLit = new Node(Token.OBJECTLIT);
    Node stringKey = Node.newString(Token.STRING_KEY, "0");
    stringKey.addChildToFront(Node.newNumber(1.0));
    objLit.addChildToFront(stringKey);

    Node varNode = new Node(Token.VAR);
    Node nameNode = Node.newString(Token.NAME, "x");
    nameNode.addChildToFront(objLit);
    varNode.addChildToFront(nameNode);

    String actual = printNode(varNode);
    assertFalse("Object literal key '0' should not be quoted as \"0\"",
        actual.contains("\"0\""));
    assertTrue("Object literal should print {0:1}",
        actual.contains("{0:1}"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsSimpleNumber_boundaries() {
    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertFalse(CodeGenerator.isSimpleNumber("01")); // leading zero
    assertFalse(CodeGenerator.isSimpleNumber("00"));
    assertFalse(CodeGenerator.isSimpleNumber("-1"));
    assertFalse(CodeGenerator.isSimpleNumber("+1"));
    assertFalse(CodeGenerator.isSimpleNumber("1.5"));
    assertFalse(CodeGenerator.isSimpleNumber("1a"));
    assertFalse(CodeGenerator.isSimpleNumber("a1"));
    assertTrue(CodeGenerator.isSimpleNumber("1"));
    assertTrue(CodeGenerator.isSimpleNumber("9"));
    assertTrue(CodeGenerator.isSimpleNumber("10"));
    assertTrue(CodeGenerator.isSimpleNumber("1234567890"));
  }

  @Test(timeout = 4000)
  public void testGetSimpleNumber_boundaries() {
    assertEquals(Double.NaN, CodeGenerator.getSimpleNumber(""), 0.0);
    assertEquals(Double.NaN, CodeGenerator.getSimpleNumber("01"), 0.0);
    assertEquals(Double.NaN, CodeGenerator.getSimpleNumber("abc"), 0.0);

    assertEquals(1.0, CodeGenerator.getSimpleNumber("1"), 0.0);
    assertEquals(42.0, CodeGenerator.getSimpleNumber("42"), 0.0);

    // MAX_POSITIVE_INTEGER_NUMBER is 2^53 = 9007199254740992L
    assertEquals(9007199254740991.0, CodeGenerator.getSimpleNumber("9007199254740991"), 0.0);
    assertEquals(Double.NaN, CodeGenerator.getSimpleNumber("9007199254740992"), 0.0);

    // Exceeding Long.MAX_VALUE triggers NumberFormatException inside getSimpleNumber
    assertEquals(Double.NaN, CodeGenerator.getSimpleNumber("999999999999999999999999999999"), 0.0);
  }

  @Test(timeout = 4000)
  public void testIdentifierEscape_latinAndNonLatin() {
    assertEquals("simpleIdentifier", CodeGenerator.identifierEscape("simpleIdentifier"));
    assertEquals("$var_123", CodeGenerator.identifierEscape("$var_123"));

    // Non-latin character should be Unicode-escaped
    String nonLatin = "var_\u00e9";
    String escaped = CodeGenerator.identifierEscape(nonLatin);
    assertTrue(escaped.startsWith("var_\\u"));

    // Supplementary code point
    String supplementary = "prefix_" + new String(Character.toChars(0x10000));
    String escapedSupp = CodeGenerator.identifierEscape(supplementary);
    assertTrue(escapedSupp.contains("\\u"));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testEscapeToDoubleQuotedJsString_controlCharacters() {
    CodeGenerator cg = CodeGenerator.forCostEstimation(null);

    assertEquals("\"hello world\"", cg.escapeToDoubleQuotedJsString("hello world"));
    assertEquals("\"\\\"quotes\\\"\"", cg.escapeToDoubleQuotedJsString("\"quotes\""));
    assertEquals("\"'single'\"", cg.escapeToDoubleQuotedJsString("'single'"));
    assertEquals("\"\\\\backslash\\\\\"", cg.escapeToDoubleQuotedJsString("\\backslash\\"));
    assertEquals("\"\\x00\"", cg.escapeToDoubleQuotedJsString("\0"));
    assertEquals("\"\\b\"", cg.escapeToDoubleQuotedJsString("\b"));
    assertEquals("\"\\f\"", cg.escapeToDoubleQuotedJsString("\f"));
    assertEquals("\"\\n\"", cg.escapeToDoubleQuotedJsString("\n"));
    assertEquals("\"\\r\"", cg.escapeToDoubleQuotedJsString("\r"));
    assertEquals("\"\\t\"", cg.escapeToDoubleQuotedJsString("\t"));
    assertEquals("\"\\x0B\"", cg.escapeToDoubleQuotedJsString("\u000B"));
    assertEquals("\"\\u2028\"", cg.escapeToDoubleQuotedJsString("\u2028"));
    assertEquals("\"\\u2029\"", cg.escapeToDoubleQuotedJsString("\u2029"));
  }

  @Test(timeout = 4000)
  public void testStrEscape_htmlScriptAndCommentBreaks() {
    CodeGenerator cg = CodeGenerator.forCostEstimation(null);

    // Breaking </script>
    String escapedScript = cg.escapeToDoubleQuotedJsString("</script>");
    assertTrue(escapedScript.contains("\\x3c/script>"));

    String escapedScriptUpper = cg.escapeToDoubleQuotedJsString("</SCRIPT>");
    assertTrue(escapedScriptUpper.contains("\\x3c/SCRIPT>"));

    // Breaking <!--
    String escapedComment = cg.escapeToDoubleQuotedJsString("<!-- comment");
    assertTrue(escapedComment.contains("\\x3c!--"));

    // Breaking -->
    String escapedEndComment = cg.escapeToDoubleQuotedJsString("-->");
    assertTrue(escapedEndComment.contains("--\\x3e"));

    // Breaking ]]>
    String escapedCdata = cg.escapeToDoubleQuotedJsString("]]>");
    assertTrue(escapedCdata.contains("]]\\x3e"));

    // Normal < and >
    assertEquals("\"a < b and c > d\"", cg.escapeToDoubleQuotedJsString("a < b and c > d"));
  }

  @Test(timeout = 4000)
  public void testStrEscape_untrustedStrings() {
    CompilerOptions options = new CompilerOptions();
    options.trustedStrings = false;
    CodeGenerator cg = new CodeGenerator(null, options);

    String escaped = cg.escapeToDoubleQuotedJsString("a=b&c<d>e");
    assertTrue(escaped.contains("\\x3d")); // =
    assertTrue(escaped.contains("\\x26")); // &
    assertTrue(escaped.contains("\\x3c")); // <
    assertTrue(escaped.contains("\\x3e")); // >
  }

  @Test(timeout = 4000)
  public void testRegexpEscape() {
    CodeGenerator cg = CodeGenerator.forCostEstimation(null);

    assertEquals("/abc/", cg.regexpEscape("abc"));
    assertEquals("/a=b&c/", cg.regexpEscape("a=b&c"));
    assertEquals("/\\x3c/script>/", cg.regexpEscape("</script>"));

    CharsetEncoder encoder = Charset.forName("US-ASCII").newEncoder();
    String encoded = cg.regexpEscape("test\u00e9", encoder);
    assertTrue(encoded.contains("\\u00e9"));
  }

  @Test(timeout = 4000)
  public void testQuotePreferences() {
    CompilerOptions options = new CompilerOptions();
    options.preferSingleQuotes = true;

    Node str1 = Node.newString("simple");
    assertEquals("'simple'", printNode(str1, options));

    // String with single quote should prefer double quotes
    Node str2 = Node.newString("it's");
    assertEquals("\"it's\"", printNode(str2, options));

    // String with more double quotes should prefer single quotes
    Node str3 = Node.newString("\"hello\" 'world'");
    assertEquals("'\"hello\" \\'world\\''", printNode(str3, options));
  }

  @Test(timeout = 4000)
  public void testAstPrinting_binaryOperatorsAndAssociativity() {
    // Associative binary operator: a + (b + c) unrolls to a + b + c
    Node addChain = new Node(Token.ADD,
        Node.newString(Token.NAME, "a"),
        new Node(Token.ADD, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c")));
    assertEquals("a+b+c", printNode(addChain));

    // Right-associative assignment: a = b = c
    Node assignChain = new Node(Token.ASSIGN,
        Node.newString(Token.NAME, "a"),
        new Node(Token.ASSIGN, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c")));
    assertEquals("a=b=c", printNode(assignChain));

    // Non-associative binary operator: a - (b - c)
    Node subChain = new Node(Token.SUB,
        Node.newString(Token.NAME, "a"),
        new Node(Token.SUB, Node.newString(Token.NAME, "b"), Node.newString(Token.NAME, "c")));
    assertEquals("a-(b-c)", printNode(subChain));
  }

  @Test(timeout = 4000)
  public void testAstPrinting_unaryOperators() {
    assertEquals("!x", printNode(new Node(Token.NOT, Node.newString(Token.NAME, "x"))));
    assertEquals("+x", printNode(new Node(Token.POS, Node.newString(Token.NAME, "x"))));
    assertEquals("~x", printNode(new Node(Token.BITNOT, Node.newString(Token.NAME, "x"))));
    assertEquals("typeof x", printNode(new Node(Token.TYPEOF, Node.newString(Token.NAME, "x"))));
    assertEquals("void 0", printNode(new Node(Token.VOID, Node.newNumber(0))));

    // NEG with number vs NEG with expression
    assertEquals("-5", printNode(new Node(Token.NEG, Node.newNumber(5))));
    assertEquals("-x", printNode(new Node(Token.NEG, Node.newString(Token.NAME, "x"))));
  }

  @Test(timeout = 4000)
  public void testAstPrinting_incrementsAndDecrements() {
    Node preInc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    preInc.putIntProp(Node.INCRDECR_PROP, 0);
    assertEquals("++x", printNode(preInc));

    Node postInc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    postInc.putIntProp(Node.INCRDECR_PROP, 1);
    assertEquals("x++", printNode(postInc));

    Node preDec = new Node(Token.DEC, Node.newString(Token.NAME, "x"));
    preDec.putIntProp(Node.INCRDECR_PROP, 0);
    assertEquals("--x", printNode(preDec));

    Node postDec = new Node(Token.DEC, Node.newString(Token.NAME, "x"));
    postDec.putIntProp(Node.INCRDECR_PROP, 1);
    assertEquals("x--", printNode(postDec));
  }

  @Test(timeout = 4000)
  public void testAstPrinting_hookAndRegexp() {
    Node hook = new Node(Token.HOOK,
        Node.newString(Token.NAME, "a"),
        Node.newNumber(1),
        Node.newNumber(2));
    assertEquals("a?1:2", printNode(hook));

    Node regex1 = new Node(Token.REGEXP, Node.newString("abc"));
    assertEquals("/abc/", printNode(regex1));

    Node regex2 = new Node(Token.REGEXP, Node.newString("abc"), Node.newString("gi"));
    assertEquals("/abc/gi", printNode(regex2));
  }

  @Test(timeout = 4000)
  public void testAstPrinting_tryCatchFinally() {
    Node tryBlock = new Node(Token.BLOCK);
    Node catchBody = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), catchBody);
    Node catchBlock = new Node(Token.BLOCK, catchNode);

    Node tryCatch = new Node(Token.TRY, tryBlock, catchBlock);
    assertEquals("try{}catch(e){}", printNode(tryCatch));

    Node finallyBlock = new Node(Token.BLOCK);
    Node tryCatchFinally = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);
    assertEquals("try{}catch(e){}finally{}", printNode(tryCatchFinally));
  }

  @Test(timeout = 4000)
  public void testAstPrinting_functionsAndCalls() {
    // Standard named function
    Node fn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, "f"),
        new Node(Token.PARAM_LIST, Node.newString(Token.NAME, "a")),
        new Node(Token.BLOCK));
    assertEquals("function f(a){}", printNode(fn));

    // Anonymous function at start of expression needing parens
    Node anonFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.PARAM_LIST),
        new Node(Token.BLOCK));
    Node exprResult = new Node(Token.EXPR_RESULT, anonFn);
    assertEquals("(function(){});", printNode(exprResult));

    // Indirect eval call
    Node evalCall = new Node(Token.CALL,
        Node.newString(Token.NAME, "eval"),
        Node.newString("1"));
    assertEquals("(0,eval)(\"1\")", printNode(evalCall));

    // Direct eval call
    Node directEvalName = Node.newString(Token.NAME, "eval");
    directEvalName.putBooleanProp(Node.DIRECT_EVAL, true);
    Node directEvalCall = new Node(Token.CALL, directEvalName, Node.newString("1"));
    assertEquals("eval(\"1\")", printNode(directEvalCall));

    // Free call on property
    Node prop = new Node(Token.GETPROP, Node.newString(Token.NAME, "o"), Node.newString("m"));
    Node freeCall = new Node(Token.CALL, prop);
    freeCall.putBooleanProp(Node.FREE_CALL, true);
    assertEquals("(0,o.m)()", printNode(freeCall));
  }

  @Test(timeout = 4000)
  public void testAstPrinting_gettersAndSetters() {
    Node obj = new Node(Token.OBJECTLIT);

    // Getter
    Node getFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.PARAM_LIST),
        new Node(Token.BLOCK));
    Node getter = Node.newString(Token.GETTER_DEF, "x");
    getter.addChildToFront(getFn);
    obj.addChildToFront(getter);

    // Setter
    Node setFn = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, ""),
        new Node(Token.PARAM_LIST, Node.newString(Token.NAME, "v")),
        new Node(Token.BLOCK));
    Node setter = Node.newString(Token.SETTER_DEF, "y");
    setter.addChildToFront(setFn);
    obj.addChildToFront(setter);

    assertEquals("{get x(){},set y(v){}}", printNode(obj));
  }

  @Test(timeout = 4000)
  public void testAstPrinting_controlFlowStatements() {
    // While & Do
    assertEquals("while(x);",
        printNode(new Node(Token.WHILE, Node.newString(Token.NAME, "x"), new Node(Token.BLOCK))));
    assertEquals("do;while(x);",
        printNode(new Node(Token.DO, new Node(Token.BLOCK), Node.newString(Token.NAME, "x"))));

    // For loop (4 children)
    Node for4 = new Node(Token.FOR,
        new Node(Token.VAR, Node.newString(Token.NAME, "i")),
        Node.newString(Token.NAME, "cond"),
        Node.newString(Token.NAME, "inc"),
        new Node(Token.BLOCK));
    assertEquals("for(var i;cond;inc);", printNode(for4));

    // For-in loop (3 children)
    Node forIn = new Node(Token.FOR,
        Node.newString(Token.NAME, "x"),
        Node.newString(Token.NAME, "y"),
        new Node(Token.BLOCK));
    assertEquals("for(x in y);", printNode(forIn));

    // If-else
    Node ifElse = new Node(Token.IF,
        Node.newString(Token.NAME, "a"),
        new Node(Token.BLOCK),
        new Node(Token.BLOCK));
    assertEquals("if(a);else;", printNode(ifElse));

    // Dangling else ambiguity wrapping
    Node innerIf = new Node(Token.IF,
        Node.newString(Token.NAME, "b"),
        new Node(Token.BLOCK));
    Node outerIf = new Node(Token.IF,
        Node.newString(Token.NAME, "a"),
        new Node(Token.BLOCK, innerIf),
        new Node(Token.BLOCK));
    String ambiguous = printNode(outerIf);
    assertTrue(ambiguous.contains("if(a){if(b);}else;"));
  }

  @Test(timeout = 4000)
  public void testAstPrinting_constantsAndBasicKeywords() {
    assertEquals("null", printNode(new Node(Token.NULL)));
    assertEquals("this", printNode(new Node(Token.THIS)));
    assertEquals("false", printNode(new Node(Token.FALSE)));
    assertEquals("true", printNode(new Node(Token.TRUE)));
    assertEquals("debugger;", printNode(new Node(Token.DEBUGGER)));
    assertEquals("return;", printNode(new Node(Token.RETURN)));
    assertEquals("return 1;", printNode(new Node(Token.RETURN, Node.newNumber(1))));
    assertEquals("throw err;", printNode(new Node(Token.THROW, Node.newString(Token.NAME, "err"))));
    assertEquals("break;", printNode(new Node(Token.BREAK)));
    assertEquals("continue;", printNode(new Node(Token.CONTINUE)));

    Node breakLabel = new Node(Token.BREAK, Node.newString(Token.LABEL_NAME, "loop"));
    assertEquals("break loop;", printNode(breakLabel));

    Node contLabel = new Node(Token.CONTINUE, Node.newString(Token.LABEL_NAME, "loop"));
    assertEquals("continue loop;", printNode(contLabel));
  }

  @Test(timeout = 4000)
  public void testAstPrinting_arraysAndProperties() {
    // Array with empty slots
    Node arr = new Node(Token.ARRAYLIT,
        Node.newNumber(1),
        new Node(Token.EMPTY),
        Node.newNumber(2));
    assertEquals("[1,,2]", printNode(arr));

    // Array with trailing empty slot
    Node arrTrailing = new Node(Token.ARRAYLIT,
        Node.newNumber(1),
        new Node(Token.EMPTY));
    assertEquals("[1,,]", printNode(arrTrailing));

    // GETPROP: standard vs number needing parens
    Node prop = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString("b"));
    assertEquals("a.b", printNode(prop));

    Node numProp = new Node(Token.GETPROP, Node.newNumber(1), Node.newString("b"));
    assertEquals("(1).b", printNode(numProp));

    // GETELEM
    Node elem = new Node(Token.GETELEM, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "i"));
    assertEquals("a[i]", printNode(elem));
  }

  @Test(timeout = 4000)
  public void testAstPrinting_newExpressions() {
    Node newWithoutArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    assertEquals("new Foo", printNode(newWithoutArgs));

    Node newWithArgs = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"), Node.newNumber(1));
    assertEquals("new Foo(1)", printNode(newWithArgs));

    // NEW with call in target requiring precedence parentheses
    Node callTarget = new Node(Token.CALL, Node.newString(Token.NAME, "getBar"));
    Node newCall = new Node(Token.NEW, callTarget, Node.newNumber(1));
    assertEquals("new (getBar())(1)", printNode(newCall));
  }

  @Test(timeout = 4000)
  public void testAstPrinting_switchCaseAndLabels() {
    Node case1 = new Node(Token.CASE, Node.newNumber(1), new Node(Token.BLOCK));
    Node defCase = new Node(Token.DEFAULT_CASE, new Node(Token.BLOCK));
    Node switchNode = new Node(Token.SWITCH, Node.newString(Token.NAME, "x"), case1, defCase);
    assertEquals("switch(x){case 1:default:}", printNode(switchNode));

    Node label = new Node(Token.LABEL,
        Node.newString(Token.LABEL_NAME, "lbl"),
        new Node(Token.BLOCK));
    assertEquals("lbl:;", printNode(label));

    Node cast = new Node(Token.CAST, Node.newString(Token.NAME, "x"));
    assertEquals("(x)", printNode(cast));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefensive_binaryOperatorExpectedTwoChildren() {
    Node addWithThree = new Node(Token.ADD,
        Node.newNumber(1),
        Node.newNumber(2),
        Node.newNumber(3));
    try {
      printNode(addWithThree);
      fail("Expected IllegalStateException for binary operator with 3 children");
    } catch (IllegalStateException expected) {
      assertTrue(expected.getMessage().contains("expected 2 arguments"));
    }
  }

  @Test(timeout = 4000)
  public void testDefensive_getPropExpectedTwoChildren() {
    Node badGetProp = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"));
    try {
      printNode(badGetProp);
      fail("Expected IllegalStateException for GETPROP with 1 child");
    } catch (IllegalStateException expected) {
      assertTrue(expected.getMessage().contains("Bad GETPROP"));
    }
  }

  @Test(timeout = 4000)
  public void testDefensive_unknownNodeTypeThrowsError() {
    Node unknown = new Node(9999);
    try {
      printNode(unknown);
      fail("Expected Error for unknown AST token type");
    } catch (Error expected) {
      assertTrue(expected.getMessage().contains("Unknown type 9999"));
    }
  }

  @Test(timeout = 4000)
  public void testDefensive_regexpNonStringChildrenThrowsError() {
    Node badRegex = new Node(Token.REGEXP, Node.newNumber(123));
    try {
      printNode(badRegex);
      fail("Expected Error for regexp node with non-string child");
    } catch (Error expected) {
      assertTrue(expected.getMessage().contains("Expected children to be strings"));
    }
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testConstructor_charsetEncoderBranching() {
    CompilerOptions asciiOptions = new CompilerOptions();
    asciiOptions.setOutputCharset(Charsets.US_ASCII);
    CodeGenerator cgAscii = new CodeGenerator(null, asciiOptions);
    assertNotNull(cgAscii);

    CompilerOptions utf8Options = new CompilerOptions();
    utf8Options.setOutputCharset(Charsets.UTF_8);
    CodeGenerator cgUtf8 = new CodeGenerator(null, utf8Options);
    assertNotNull(cgUtf8);

    CodeGenerator cgEstimate = CodeGenerator.forCostEstimation(null);
    assertNotNull(cgEstimate);
  }
}
