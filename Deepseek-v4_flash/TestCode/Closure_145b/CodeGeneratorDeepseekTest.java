package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Target: CodeGenerator (package-private class)
 *
 * Key decision branches and boundary conditions covered:
 * 1. Constructor with null charset -> outputCharsetEncoder = null (ASCII path)
 * 2. Constructor with US_ASCII charset -> outputCharsetEncoder = null
 * 3. Constructor with UTF-8 charset -> outputCharsetEncoder != null
 * 4. add(Node) with null consumer processing -> early return
 * 5. Binary operators (opstr != null && first != last) - precedence handling,
 *    associativity, assignment right-associativity
 * 6. Unary operators (TYPEOF, VOID, NOT, BITNOT, POS, NEG) - right-associative
 * 7. HOOK (ternary) operator - precedence and context
 * 8. String escaping: quote selection (single vs double), control chars,
 *    HTML comment breaking (--> and </script), non-Latin chars with/without encoder
 * 9. Regexp escaping: slash quote, backslash, charset encoder
 * 10. identifierEscape: Latin passthrough, non-Latin hex escaping
 * 11. addList with skipIndexes (array literal holes)
 * 12. addNonEmptyExpression: empty block, single child block, function/do wrapping
 * 13. Context handling: IN_FOR_INIT_CLAUSE, BEFORE_DANGLING_ELSE, START_OF_EXPR
 * 14. Defect target: Safari/IE compatibility - when a block with a single
 *     FUNCTION or DO child is inside an IF statement, the block must be
 *     preserved (wrapped in braces) to avoid browser bugs.
 *     The bug is in addNonEmptyExpression: when count == 1 and the child is
 *     FUNCTION or DO, it should call cc.beginBlock()/endBlock() but the
 *     defective version skips this when shouldPreserveExtraBlocks() is false.
 *     This test verifies that a FUNCTION or DO inside an IF gets braces.
 */
public class CodeGeneratorDeepseekTest {

  // Helper to create a simple CodeConsumer that captures output
  private static class StringBuilderConsumer extends CodeConsumer {
    final StringBuilder sb = new StringBuilder();
    boolean continueProcessing = true;
    boolean preserveExtraBlocks = false;

    @Override
    void add(String str) {
      sb.append(str);
    }

    @Override
    void addIdentifier(String identifier) {
      sb.append(identifier);
    }

    @Override
    void addOp(String op, boolean binOp) {
      sb.append(op);
    }

    @Override
    void addNumber(double x) {
      sb.append(x);
    }

    @Override
    void endStatement() {
      sb.append(";");
    }

    @Override
    void endStatement(boolean needSemiColon) {
      if (needSemiColon) {
        sb.append(";");
      }
    }

    @Override
    void beginBlock() {
      sb.append("{");
    }

    @Override
    void endBlock(boolean endStatement) {
      sb.append("}");
      if (endStatement) {
        sb.append(";");
      }
    }

    @Override
    void listSeparator() {
      sb.append(",");
    }

    @Override
    void beginCaseBody() {
      sb.append("{");
    }

    @Override
    void endCaseBody() {
      sb.append("}");
    }

    @Override
    void startSourceMapping(Node node) {}

    @Override
    void endSourceMapping(Node node) {}

    @Override
    void notePreferredLineBreak() {}

    @Override
    void maybeLineBreak() {}

    @Override
    boolean continueProcessing() {
      return continueProcessing;
    }

    @Override
    boolean shouldPreserveExtraBlocks() {
      return preserveExtraBlocks;
    }

    @Override
    boolean breakAfterBlockFor(Node n, boolean isStatementContext) {
      return isStatementContext;
    }
  }

  // Helper to create a simple Node tree
  private static Node createNameNode(String name) {
    Node n = new Node(Token.NAME, name);
    return n;
  }

  private static Node createStringNode(String value) {
    Node n = new Node(Token.STRING, value);
    return n;
  }

  private static Node createNumberNode(double value) {
    Node n = new Node(Token.NUMBER);
    n.setDouble(value);
    return n;
  }

  private static Node createBlock(Node... children) {
    Node block = new Node(Token.BLOCK);
    for (Node child : children) {
      block.addChildToBack(child);
    }
    return block;
  }

  private static Node createFunctionNode(String name) {
    // FUNCTION node with 3 children: NAME, LP (params), BLOCK (body)
    Node function = new Node(Token.FUNCTION);
    Node nameNode = createNameNode(name);
    Node params = new Node(Token.LP);
    Node body = createBlock(new Node(Token.EMPTY));
    function.addChildToBack(nameNode);
    function.addChildToBack(params);
    function.addChildToBack(body);
    return function;
  }

  private static Node createDoNode(Node body, Node condition) {
    Node doNode = new Node(Token.DO);
    doNode.addChildToBack(body);
    doNode.addChildToBack(condition);
    return doNode;
  }

  private static Node createIfNode(Node cond, Node thenBlock) {
    Node ifNode = new Node(Token.IF);
    ifNode.addChildToBack(cond);
    ifNode.addChildToBack(thenBlock);
    return ifNode;
  }

  // ========== Partition A: Core Functional Logic & State Transitions ==========

  @Test(timeout = 4000)
  public void testConstructorWithNullCharset() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    assertNotNull(cg);
    // Verify basic add works
    cg.add("test");
    assertEquals("test", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testConstructorWithAsciiCharset() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer, Charsets.US_ASCII);
    assertNotNull(cg);
    cg.add("hello");
    assertEquals("hello", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testConstructorWithUtf8Charset() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer, StandardCharsets.UTF_8);
    assertNotNull(cg);
    cg.add("hello");
    assertEquals("hello", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddWithContinueProcessingFalse() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    consumer.continueProcessing = false;
    CodeGenerator cg = new CodeGenerator(consumer);
    Node n = createNameNode("x");
    cg.add(n);
    assertEquals("", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddIdentifier() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node n = createNameNode("myVar");
    cg.add(n);
    assertEquals("myVar", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddNumber() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node n = createNumberNode(42.5);
    cg.add(n);
    assertEquals("42.5", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddString() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node n = createStringNode("hello");
    cg.add(n);
    assertEquals("\"hello\"", consumer.sb.toString());
  }

  // ========== Partition B: Boundary Value Analysis & Extremes ==========

  @Test(timeout = 4000)
  public void testStringEscapeWithQuotes() {
    // More single quotes -> use double quotes
    assertEquals("\"it's\"", CodeGenerator.jsString("it's", null));
    // More double quotes -> use single quotes
    assertEquals("'say \"hi\"'", CodeGenerator.jsString("say \"hi\"", null));
    // Equal quotes -> use double quotes (else branch)
    assertEquals("\"a'b\\\"c\"", CodeGenerator.jsString("a'b\"c", null));
  }

  @Test(timeout = 4000)
  public void testStringEscapeControlChars() {
    assertEquals("\"\\n\\r\\t\"", CodeGenerator.jsString("\n\r\t", null));
    assertEquals("\"back\\\\slash\"", CodeGenerator.jsString("back\\slash", null));
  }

  @Test(timeout = 4000)
  public void testStringEscapeHtmlComments() {
    // --> should become --\>
    assertEquals("\"--\\>\"", CodeGenerator.jsString("-->", null));
    // ]]> should become ]]\>
    assertEquals("\"]]\\>\"", CodeGenerator.jsString("]]>", null));
    // </script should become <\/script
    assertEquals("\"<\\/script>\"", CodeGenerator.jsString("</script>", null));
  }

  @Test(timeout = 4000)
  public void testStringEscapeNonLatinNoEncoder() {
    // Non-Latin chars should be unicode escaped
    assertEquals("\"\\u00e9\"", CodeGenerator.jsString("\u00e9", null));
    // Control chars should be escaped
    assertEquals("\"\\u0000\"", CodeGenerator.jsString("\u0000", null));
  }

  @Test(timeout = 4000)
  public void testStringEscapeWithEncoder() {
    CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
    // Latin-1 chars encodable in UTF-8
    assertEquals("\"\u00e9\"", CodeGenerator.jsString("\u00e9", encoder));
    // Supplementary chars should be escaped as surrogate pairs
    String supplementary = new String(Character.toChars(0x1F600));
    assertEquals("\"\\ud83d\\ude00\"", CodeGenerator.jsString(supplementary, encoder));
  }

  @Test(timeout = 4000)
  public void testRegexpEscape() {
    assertEquals("/a\\/b/", CodeGenerator.regexpEscape("a/b", null));
    assertEquals("/a\\\\b/", CodeGenerator.regexpEscape("a\\b", null));
    assertEquals("/\\n/", CodeGenerator.regexpEscape("\n", null));
  }

  @Test(timeout = 4000)
  public void testEscapeToDoubleQuotedJsString() {
    assertEquals("\"a\\\"b\"", CodeGenerator.escapeToDoubleQuotedJsString("a\"b"));
    assertEquals("\"a'b\"", CodeGenerator.escapeToDoubleQuotedJsString("a'b"));
    assertEquals("\"a\\\\b\"", CodeGenerator.escapeToDoubleQuotedJsString("a\\b"));
  }

  @Test(timeout = 4000)
  public void testIdentifierEscape() {
    assertEquals("latin", CodeGenerator.identifierEscape("latin"));
    assertEquals("\\u00e9", CodeGenerator.identifierEscape("\u00e9"));
    assertEquals("a\\u00e9b", CodeGenerator.identifierEscape("a\u00e9b"));
  }

  @Test(timeout = 4000)
  public void testAddListWithSkipIndexes() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node first = createNumberNode(1);
    Node second = createNumberNode(2);
    first.setNext(second);
    int[] skipIndexes = {1};
    cg.addList(first, skipIndexes);
    assertEquals("1,,2", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddListWithNullSkipIndexes() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    Node first = createNumberNode(1);
    Node second = createNumberNode(2);
    first.setNext(second);
    cg.addList(first, null);
    assertEquals("1,2", consumer.sb.toString());
  }

  // ========== Partition C: Defect-Targeted Branch Zone ==========

  /**
   * Defect target: Safari compatibility - a FUNCTION inside an IF statement
   * must be wrapped in a block even when shouldPreserveExtraBlocks() is false.
   * The bug is in addNonEmptyExpression: when a block has exactly one child
   * that is a FUNCTION or DO, the block must be preserved.
   */
  @Test(timeout = 4000)
  public void testFunctionSafariCompatibility() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    consumer.preserveExtraBlocks = false;
    CodeGenerator cg = new CodeGenerator(consumer);

    // Build: if(e1){A:function goo(){return true}}
    Node cond = createNameNode("e1");
    Node function = createFunctionNode("goo");
    // Add a return statement to the function body
    Node returnNode = new Node(Token.RETURN);
    returnNode.addChildToBack(new Node(Token.TRUE));
    function.getLastChild().addChildToBack(returnNode);

    // Create a labeled function: LABEL -> LABEL_NAME "A", FUNCTION
    Node label = new Node(Token.LABEL);
    Node labelName = new Node(Token.LABEL_NAME, "A");
    label.addChildToBack(labelName);
    label.addChildToBack(function);

    Node block = createBlock(label);
    Node ifNode = createIfNode(cond, block);

    cg.add(ifNode);
    // Expected: if(e1){A:function goo(){return true;}}
    // The defect produces: if(e1)A:function goo(){return true;}
    // Note: the exact output depends on CodeConsumer implementation details,
    // but the key is that braces must be present around the function.
    String output = consumer.sb.toString();
    assertTrue("Expected braces around function in if statement, got: " + output,
        output.contains("{A:function goo()"));
  }

  /**
   * Defect target: IE compatibility - a DO loop inside an IF statement
   * must be wrapped in a block even when shouldPreserveExtraBlocks() is false.
   */
  @Test(timeout = 4000)
  public void testDoLoopIECompatibility() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    consumer.preserveExtraBlocks = false;
    CodeGenerator cg = new CodeGenerator(consumer);

    // Build: if(x){A:do foo();while(y)}
    Node cond = createNameNode("x");
    Node doBody = createBlock(createNameNode("foo"));
    Node whileCond = createNameNode("y");
    Node doNode = createDoNode(doBody, whileCond);

    // Create a labeled do: LABEL -> LABEL_NAME "A", DO
    Node label = new Node(Token.LABEL);
    Node labelName = new Node(Token.LABEL_NAME, "A");
    label.addChildToBack(labelName);
    label.addChildToBack(doNode);

    Node block = createBlock(label);
    Node ifNode = createIfNode(cond, block);

    cg.add(ifNode);
    String output = consumer.sb.toString();
    assertTrue("Expected braces around do loop in if statement, got: " + output,
        output.contains("{A:do foo();"));
  }

  @Test(timeout = 4000)
  public void testNonEmptyExpressionWithEmptyBlock() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    consumer.preserveExtraBlocks = false;
    CodeGenerator cg = new CodeGenerator(consumer);

    Node emptyBlock = createBlock(new Node(Token.EMPTY));
    Node ifNode = createIfNode(createNameNode("x"), emptyBlock);

    cg.add(ifNode);
    // Should produce: if(x);
    assertEquals("if(x);", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testNonEmptyExpressionWithSingleNonFunctionChild() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    consumer.preserveExtraBlocks = false;
    CodeGenerator cg = new CodeGenerator(consumer);

    Node singleChildBlock = createBlock(createNameNode("foo"));
    Node ifNode = createIfNode(createNameNode("x"), singleChildBlock);

    cg.add(ifNode);
    // Should produce: if(x)foo;
    assertEquals("if(x)foo;", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testNonEmptyExpressionWithPreserveExtraBlocks() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    consumer.preserveExtraBlocks = true;
    CodeGenerator cg = new CodeGenerator(consumer);

    Node singleChildBlock = createBlock(createNameNode("foo"));
    Node ifNode = createIfNode(createNameNode("x"), singleChildBlock);

    cg.add(ifNode);
    // Should produce: if(x){foo;}
    assertEquals("if(x){foo;}", consumer.sb.toString());
  }

  // ========== Partition D: Exception & Defensive Guard Paths ==========

  @Test(timeout = 4000, expected = Error.class)
  public void testCatchWithConditionThrows() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node catchNode = new Node(Token.CATCH);
    Node param = createNameNode("e");
    Node condition = createNameNode("cond");
    Node block = createBlock(new Node(Token.EMPTY));
    catchNode.addChildToBack(param);
    catchNode.addChildToBack(condition);
    catchNode.addChildToBack(block);

    cg.add(catchNode);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testRegexpWithNonStringChildrenThrows() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node regexp = new Node(Token.REGEXP);
    regexp.addChildToBack(createNumberNode(1));
    regexp.addChildToBack(createStringNode("g"));

    cg.add(regexp);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testUnknownNodeTypeThrows() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node unknown = new Node(Token.ARRAYLIT); // Using a valid token but wrong structure
    // Actually, let's use an invalid token type
    Node invalid = new Node(9999);
    cg.add(invalid);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testExprVoidThrows() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node exprVoid = new Node(Token.EXPR_VOID);
    cg.add(exprVoid);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testContinueWithNonLabelThrows() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node continueNode = new Node(Token.CONTINUE);
    continueNode.addChildToBack(createNameNode("notLabel"));
    cg.add(continueNode);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testBreakWithNonLabelThrows() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node breakNode = new Node(Token.BREAK);
    breakNode.addChildToBack(createNameNode("notLabel"));
    cg.add(breakNode);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testLabelWithNonLabelNameThrows() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node label = new Node(Token.LABEL);
    label.addChildToBack(createNameNode("notLabelName"));
    label.addChildToBack(createBlock(new Node(Token.EMPTY)));
    cg.add(label);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testAddNonEmptyExpressionWithNonBlockChild() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node ifNode = createIfNode(createNameNode("x"), createNameNode("notBlock"));
    cg.add(ifNode);
  }

  // ========== Partition E: Object Lifecycle & Contract Integrity ==========

  @Test(timeout = 4000)
  public void testAddLeftExprWithContext() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node name = createNameNode("x");
    cg.addLeftExpr(name, 0, CodeGenerator.Context.OTHER);
    assertEquals("x", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddExprWithMinPrecedence() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node name = createNameNode("x");
    cg.addExpr(name, 0);
    assertEquals("x", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddListSimple() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node first = createNumberNode(1);
    Node second = createNumberNode(2);
    first.setNext(second);
    cg.addList(first);
    assertEquals("1,2", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddListWithArrayArgument() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node first = createNumberNode(1);
    Node second = createNumberNode(2);
    first.setNext(second);
    cg.addList(first, true);
    assertEquals("1,2", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddCaseBody() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node caseBody = createBlock(createNameNode("x"));
    cg.addCaseBody(caseBody);
    assertEquals("{x}", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddAllSiblings() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node first = createNameNode("a");
    Node second = createNameNode("b");
    first.setNext(second);
    cg.addAllSiblings(first);
    assertEquals("ab", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testContextHelpers() {
    CodeGenerator cg = new CodeGenerator(new StringBuilderConsumer());

    // getContextForNonEmptyExpression
    assertEquals(CodeGenerator.Context.OTHER,
        cg.getContextForNonEmptyExpression(CodeGenerator.Context.OTHER));
    assertEquals(CodeGenerator.Context.BEFORE_DANGLING_ELSE,
        cg.getContextForNonEmptyExpression(CodeGenerator.Context.BEFORE_DANGLING_ELSE));

    // getContextForNoInOperator
    assertEquals(CodeGenerator.Context.IN_FOR_INIT_CLAUSE,
        cg.getContextForNoInOperator(CodeGenerator.Context.IN_FOR_INIT_CLAUSE));
    assertEquals(CodeGenerator.Context.OTHER,
        cg.getContextForNoInOperator(CodeGenerator.Context.OTHER));

    // clearContextForNoInOperator
    assertEquals(CodeGenerator.Context.OTHER,
        cg.clearContextForNoInOperator(CodeGenerator.Context.IN_FOR_INIT_CLAUSE));
    assertEquals(CodeGenerator.Context.OTHER,
        cg.clearContextForNoInOperator(CodeGenerator.Context.OTHER));
  }

  @Test(timeout = 4000)
  public void testBinaryOperatorAddition() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    // Build: a + b
    Node addNode = new Node(Token.ADD);
    addNode.addChildToBack(createNameNode("a"));
    addNode.addChildToBack(createNameNode("b"));
    cg.add(addNode);
    assertEquals("a+b", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testUnaryOperatorNegation() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node negNode = new Node(Token.NEG);
    negNode.addChildToBack(createNumberNode(5));
    cg.add(negNode);
    assertEquals("-5", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testHookOperator() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node hookNode = new Node(Token.HOOK);
    hookNode.addChildToBack(createNameNode("cond"));
    hookNode.addChildToBack(createNameNode("a"));
    hookNode.addChildToBack(createNameNode("b"));
    cg.add(hookNode);
    assertEquals("cond?a:b", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testGetProp() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node getProp = new Node(Token.GETPROP);
    getProp.addChildToBack(createNameNode("obj"));
    getProp.addChildToBack(createStringNode("prop"));
    cg.add(getProp);
    assertEquals("obj.prop", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testGetElem() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node getElem = new Node(Token.GETELEM);
    getElem.addChildToBack(createNameNode("arr"));
    getElem.addChildToBack(createNumberNode(0));
    cg.add(getElem);
    assertEquals("arr[0]", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testCallFunction() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node callNode = new Node(Token.CALL);
    callNode.addChildToBack(createNameNode("foo"));
    callNode.addChildToBack(createNumberNode(1));
    callNode.addChildToBack(createNumberNode(2));
    cg.add(callNode);
    assertEquals("foo(1,2)", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testIndirectEvalCall() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node evalName = createNameNode("eval");
    evalName.putBooleanProp(Node.DIRECT_EVAL, false);
    Node callNode = new Node(Token.CALL);
    callNode.addChildToBack(evalName);
    callNode.addChildToBack(createStringNode("x"));
    cg.add(callNode);
    assertEquals("(0,eval)(\"x\")", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testDirectEvalCall() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node evalName = createNameNode("eval");
    evalName.putBooleanProp(Node.DIRECT_EVAL, true);
    Node callNode = new Node(Token.CALL);
    callNode.addChildToBack(evalName);
    callNode.addChildToBack(createStringNode("x"));
    cg.add(callNode);
    assertEquals("eval(\"x\")", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testNewExpression() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node newExpr = new Node(Token.NEW);
    newExpr.addChildToBack(createNameNode("Foo"));
    cg.add(newExpr);
    assertEquals("new Foo", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testNewExpressionWithArgs() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node newExpr = new Node(Token.NEW);
    newExpr.addChildToBack(createNameNode("Foo"));
    newExpr.addChildToBack(createNumberNode(1));
    cg.add(newExpr);
    assertEquals("new Foo(1)", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testObjectLiteral() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node objLit = new Node(Token.OBJECTLIT);
    objLit.addChildToBack(createStringNode("key"));
    objLit.addChildToBack(createNumberNode(1));
    cg.add(objLit);
    assertEquals("{key:1}", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testArrayLiteral() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node arrayLit = new Node(Token.ARRAYLIT);
    arrayLit.addChildToBack(createNumberNode(1));
    arrayLit.addChildToBack(createNumberNode(2));
    cg.add(arrayLit);
    assertEquals("[1,2]", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testVarDeclaration() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node varNode = new Node(Token.VAR);
    Node nameNode = createNameNode("x");
    nameNode.addChildToBack(createNumberNode(1));
    varNode.addChildToBack(nameNode);
    cg.add(varNode);
    assertEquals("var x=1", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testReturnStatement() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node returnNode = new Node(Token.RETURN);
    returnNode.addChildToBack(createNumberNode(42));
    cg.add(returnNode);
    assertEquals("return42;", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testThrowStatement() {
    StringBuilderConsumer consumer = new StringBuilderConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);

    Node throwNode = new Node(Token.THROW);
    throwNode.addChildToBack(createNameNode("e"));
    cg.add(throwNode);
    assertEquals("throwe;", consumer.sb.toString());
  }
}