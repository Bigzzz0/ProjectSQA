package com.google.javascript.jscomp.parsing;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.jscomp.parsing.Config;
import com.google.javascript.jscomp.parsing.IRFactory;
import com.google.javascript.jscomp.mozilla.rhino.CompilerEnvirons;
import com.google.javascript.jscomp.mozilla.rhino.Context;
import com.google.javascript.jscomp.mozilla.rhino.ErrorReporter;
import com.google.javascript.jscomp.mozilla.rhino.EvaluatorException;
import com.google.javascript.jscomp.mozilla.rhino.Parser;
import com.google.javascript.jscomp.mozilla.rhino.ast.ArrayLiteral;
import com.google.javascript.jscomp.mozilla.rhino.ast.Assignment;
import com.google.javascript.jscomp.mozilla.rhino.ast.AstNode;
import com.google.javascript.jscomp.mozilla.rhino.ast.AstRoot;
import com.google.javascript.jscomp.mozilla.rhino.ast.Block;
import com.google.javascript.jscomp.mozilla.rhino.ast.CatchClause;
import com.google.javascript.jscomp.mozilla.rhino.ast.Comment;
import com.google.javascript.jscomp.mozilla.rhino.ast.ExpressionStatement;
import com.google.javascript.jscomp.mozilla.rhino.ast.FunctionNode;
import com.google.javascript.jscomp.mozilla.rhino.ast.Name;
import com.google.javascript.jscomp.mozilla.rhino.ast.NumberLiteral;
import com.google.javascript.jscomp.mozilla.rhino.ast.ObjectLiteral;
import com.google.javascript.jscomp.mozilla.rhino.ast.ObjectProperty;
import com.google.javascript.jscomp.mozilla.rhino.ast.UnaryExpression;
import com.google.javascript.jscomp.mozilla.rhino.ast.VariableDeclaration;
import com.google.javascript.jscomp.mozilla.rhino.ast.VariableInitializer;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.JSDocInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------------------
 * Target Class : com.google.javascript.jscomp.parsing.IRFactory
 * Benchmark    : Defects4J Closure Compiler
 * Known Defect : ParserTest::testUnnamedFunctionStatement (junit.framework.AssertionFailedError)
 *
 * Targeted Decision Branches & Conditions:
 * 1. Defect Branch (processFunctionNode / transform):
 *    - In defective IRFactory, transform() checks:
 *        if (irNode.getType() == Token.FUNCTION && irNode.getFirstChild().getLineno() != -1)
 *      When an unnamed function is transformed, newName has its lineno artificially populated:
 *        newName.setLineno(functionNode.getLineno());
 *      This triggers the branch intended only for NAMED functions, resetting the function's charno
 *      to newName.getCharno() (the left paren position) instead of 0 / start of 'function' keyword!
 *      Expected: FUNCTION node's charno is at the 'function' keyword position (0), not left paren.
 * 2. parseDirectives:
 *    - ES5 "use strict" directives removal, recording in node.setDirectives, and normal statements retention.
 * 3. transformBlock:
 *    - irNode.getType() == Token.BLOCK: unchanged.
 *    - irNode.getType() == Token.EMPTY: rewritten to Token.BLOCK with wasEmptyNode = true.
 *    - other statement: wrapped inside a synthetic Token.BLOCK.
 * 4. position2charno:
 *    - Single line position (lineIndex == -1).
 *    - Multi-line position (lineIndex != -1, subtracting line offset).
 * 5. transformAsString:
 *    - String token -> Token.STRING with QUOTED_PROP.
 *    - Name token -> Token.STRING.
 * 6. processArrayLiteral:
 *    - Standard elements vs sparse array (skipCount > 0, SKIP_INDEXES_PROP).
 *    - Destructuring array -> reportDestructuringAssign.
 * 7. processAssignment & validAssignmentTarget:
 *    - Valid targets: NAME, GETPROP, GETELEM.
 *    - Invalid target (e.g., number literal): triggers error reporter "invalid assignment target".
 * 8. processObjectLiteral (ES5 vs non-ES5):
 *    - Non-ES5 mode: getters and setters trigger reportGetter / reportSetter.
 *    - ES5 mode: getters with params -> reportGetterParam; setters with 0 or >1 params -> reportSetterParam.
 *    - Destructuring object literal -> reportDestructuringAssign.
 * 9. processUnaryExpression:
 *    - NEG with NUMBER -> folded directly via operand.setDouble(-operand.getDouble()).
 *    - INC / DEC with valid targets vs invalid targets (report "invalid increment/decrement target").
 *    - Prefix vs Postfix (INCRDECR_PROP).
 * 10. processTryStatement:
 *    - Try-catch-finally, try-catch, and try-finally (lineSet == false branch).
 * 11. processVariableDeclaration:
 *    - const declarations rejected when acceptConstKeyword == false.
 * 12. transformTokenType:
 *    - Unrecognized token throws IllegalStateException.
 * ---------------------------------------------------------------------------------------------------------------------
 */
public class IRFactoryGeminiTest {

  // ===================================================================================================================
  // Test Harness Helpers & Fixtures
  // ===================================================================================================================

  private static class TestErrorReporter implements ErrorReporter {
    final List<String> errors = new ArrayList<String>();
    final List<String> warnings = new ArrayList<String>();

    @Override
    public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
      warnings.add(message);
    }

    @Override
    public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
      errors.add(message);
    }

    @Override
    public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
      error(message, sourceName, line, lineSource, lineOffset);
      return new EvaluatorException(message, sourceName, line, lineSource, lineOffset);
    }
  }

  private static Config createTestConfig(boolean acceptES5, boolean acceptConst) {
    try {
      for (Constructor<?> c : Config.class.getDeclaredConstructors()) {
        c.setAccessible(true);
        Class<?>[] paramTypes = c.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
          Class<?> pt = paramTypes[i];
          if (pt.equals(boolean.class) || pt.equals(Boolean.class)) {
            args[i] = Boolean.valueOf(acceptES5);
          } else if (pt.equals(Set.class)) {
            args[i] = Collections.emptySet();
          } else if (pt.isEnum()) {
            Object[] enumConstants = pt.getEnumConstants();
            args[i] = (enumConstants != null && enumConstants.length > 0) ? enumConstants[enumConstants.length - 1] : null;
          } else {
            args[i] = null;
          }
        }
        try {
          Config cfg = (Config) c.newInstance(args);
          setFieldIfExists(cfg, "acceptES5", acceptES5);
          setFieldIfExists(cfg, "acceptConstKeyword", acceptConst);
          return cfg;
        } catch (Throwable ignored) {
        }
      }
    } catch (Throwable ignored) {
    }
    return null;
  }

  private static void setFieldIfExists(Object obj, String fieldName, Object val) {
    try {
      Field f = obj.getClass().getDeclaredField(fieldName);
      f.setAccessible(true);
      f.set(obj, val);
    } catch (Throwable ignored) {
    }
  }

  private AstRoot parseToRhinoAst(String source, TestErrorReporter reporter) {
    CompilerEnvirons env = new CompilerEnvirons();
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);
    env.setLanguageVersion(Context.VERSION_1_8);
    env.setStrictMode(false);
    env.setWarnTrailingComma(true);
    env.setIdeMode(true);
    Parser p = new Parser(env, reporter);
    return p.parse(source, "testcode.js", 1);
  }

  private Node parseAndTransform(String source, boolean acceptES5, boolean acceptConst, TestErrorReporter reporter) {
    AstRoot root = parseToRhinoAst(source, reporter);
    Config config = createTestConfig(acceptES5, acceptConst);
    return IRFactory.transformTree(root, source, config, reporter);
  }

  private Node parseAndTransform(String source) {
    TestErrorReporter reporter = new TestErrorReporter();
    return parseAndTransform(source, true, true, reporter);
  }

  private static Node findFirstNodeType(Node root, int tokenType) {
    if (root == null) return null;
    if (root.getType() == tokenType) return root;
    for (Node child = root.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findFirstNodeType(child, tokenType);
      if (found != null) return found;
    }
    return null;
  }

  // ===================================================================================================================
  // Partition C: Defect-Targeted Branch Zone (Ground Truth Defects4J Target)
  // ===================================================================================================================

  /**
   * Targets known defect: ParserTest::testUnnamedFunctionStatement
   * In defective IRFactory, transform() uses:
   *   if (irNode.getType() == Token.FUNCTION && irNode.getFirstChild().getLineno() != -1)
   * which erroneously matches unnamed functions (whose dummy empty name had its lineno set to functionNode.getLineno()).
   * This overrides the FUNCTION node's charno with the left paren position instead of the 'function' keyword position (0).
   */
  @Test(timeout = 4000)
  public void testUnnamedFunctionStatement_defect() {
    String source = "function() {}";
    TestErrorReporter reporter = new TestErrorReporter();
    AstRoot root = parseToRhinoAst(source, reporter);
    Config config = createTestConfig(true, true);
    Node ir = IRFactory.transformTree(root, source, config, reporter);

    Node fnNode = findFirstNodeType(ir, Token.FUNCTION);
    assertNotNull("FUNCTION node should be produced", fnNode);
    assertEquals("FUNCTION token expected", Token.FUNCTION, fnNode.getType());
    assertEquals("Unnamed function node charno must start at 'function' keyword (0), not left paren (8)",
        0, fnNode.getCharno());
  }

  @Test(timeout = 4000)
  public void testUnnamedFunctionExpression_charnoPosition() {
    String source = "(function() {});";
    TestErrorReporter reporter = new TestErrorReporter();
    AstRoot root = parseToRhinoAst(source, reporter);
    Config config = createTestConfig(true, true);
    Node ir = IRFactory.transformTree(root, source, config, reporter);

    Node fnNode = findFirstNodeType(ir, Token.FUNCTION);
    assertNotNull("FUNCTION node should be produced", fnNode);
    assertEquals("Unnamed function expression charno must start at 'function' keyword (1)",
        1, fnNode.getCharno());
  }

  @Test(timeout = 4000)
  public void testNamedFunctionStatement_charnoMatchesName() {
    String source = "function myNamedFunc() {}";
    Node ir = parseAndTransform(source);

    Node fnNode = findFirstNodeType(ir, Token.FUNCTION);
    assertNotNull("FUNCTION node should be produced", fnNode);
    Node nameNode = fnNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertEquals("myNamedFunc", nameNode.getString());
    assertEquals("Named function node charno must match the name node's charno",
        nameNode.getCharno(), fnNode.getCharno());
  }

  // ===================================================================================================================
  // Partition A: Core Functional Logic & State Transitions
  // ===================================================================================================================

  @Test(timeout = 4000)
  public void testScriptDirectivesExtraction() {
    String source = "'use strict'; var x = 1;";
    Node ir = parseAndTransform(source);

    assertEquals(Token.SCRIPT, ir.getType());
    Set<String> directives = ir.getDirectives();
    assertNotNull("Directives set should not be null", directives);
    assertTrue("Should contain 'use strict'", directives.contains("use strict"));
    assertEquals("Only the var statement should remain as child", 1, ir.getChildCount());
    assertEquals(Token.VAR, ir.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testFunctionDirectivesExtraction() {
    String source = "function f() { 'use strict'; return 42; }";
    Node ir = parseAndTransform(source);

    Node fnNode = findFirstNodeType(ir, Token.FUNCTION);
    assertNotNull(fnNode);
    Node bodyNode = fnNode.getLastChild();
    assertEquals(Token.BLOCK, bodyNode.getType());
    Set<String> directives = bodyNode.getDirectives();
    assertNotNull("Function body directives should not be null", directives);
    assertTrue("Function body should have 'use strict'", directives.contains("use strict"));
    assertEquals("Only the return statement should remain in function body", 1, bodyNode.getChildCount());
  }

  @Test(timeout = 4000)
  public void testNonDirectiveStringNotExtracted() {
    String source = "'other string'; var x = 1;";
    Node ir = parseAndTransform(source);

    Set<String> directives = ir.getDirectives();
    assertNull("Directives should be null for non-allowed directive strings", directives);
    assertEquals("Both expressions should remain", 2, ir.getChildCount());
  }

  @Test(timeout = 4000)
  public void testTransformBlock_fromEmptyStatement() {
    String source = "while (true);";
    Node ir = parseAndTransform(source);

    Node whileNode = findFirstNodeType(ir, Token.WHILE);
    assertNotNull(whileNode);
    Node bodyBlock = whileNode.getLastChild();
    assertEquals(Token.BLOCK, bodyBlock.getType());
    assertTrue("Should have wasEmptyNode set to true", bodyBlock.wasEmptyNode());
  }

  @Test(timeout = 4000)
  public void testTransformBlock_fromSingleStatement() {
    String source = "if (true) return 1;";
    Node ir = parseAndTransform(source);

    Node ifNode = findFirstNodeType(ir, Token.IF);
    assertNotNull(ifNode);
    Node thenBlock = ifNode.getChildAtIndex(1);
    assertEquals(Token.BLOCK, thenBlock.getType());
    assertEquals(Token.RETURN, thenBlock.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testIfStatementWithAndWithoutElse() {
    String sourceWithElse = "if (a) { b(); } else { c(); }";
    Node ir1 = parseAndTransform(sourceWithElse);
    Node ifNode1 = findFirstNodeType(ir1, Token.IF);
    assertNotNull(ifNode1);
    assertEquals(3, ifNode1.getChildCount());

    String sourceWithoutElse = "if (a) { b(); }";
    Node ir2 = parseAndTransform(sourceWithoutElse);
    Node ifNode2 = findFirstNodeType(ir2, Token.IF);
    assertNotNull(ifNode2);
    assertEquals(2, ifNode2.getChildCount());
  }

  @Test(timeout = 4000)
  public void testLoops_While_DoWhile_For_ForIn() {
    String source = "do { x++; } while (x < 10);\n" +
                    "for (var i = 0; i < 10; i++) {}\n" +
                    "for (var key in obj) {}";
    Node ir = parseAndTransform(source);

    assertNotNull("DO loop should be transformed", findFirstNodeType(ir, Token.DO));
    assertNotNull("FOR loop should be transformed", findFirstNodeType(ir, Token.FOR));
  }

  @Test(timeout = 4000)
  public void testSwitchStatementAndCases() {
    String source = "switch (x) {\n" +
                    "  case 1: y(); break;\n" +
                    "  case 2:\n" +
                    "  default: z();\n" +
                    "}";
    Node ir = parseAndTransform(source);

    Node switchNode = findFirstNodeType(ir, Token.SWITCH);
    assertNotNull(switchNode);
    assertNotNull(findFirstNodeType(switchNode, Token.CASE));
    assertNotNull(findFirstNodeType(switchNode, Token.DEFAULT));
  }

  @Test(timeout = 4000)
  public void testTryCatchFinallyVariations() {
    String source = "try { a(); } catch (e) { b(); }\n" +
                    "try { a(); } finally { c(); }";
    Node ir = parseAndTransform(source);

    Node try1 = ir.getFirstChild();
    assertEquals(Token.TRY, try1.getType());
    assertEquals(Token.BLOCK, try1.getFirstChild().getType()); // try block
    assertEquals(Token.BLOCK, try1.getChildAtIndex(1).getType()); // catch block

    Node try2 = ir.getChildAtIndex(1);
    assertEquals(Token.TRY, try2.getType());
    assertEquals(3, try2.getChildCount()); // try block, empty catch block, finally block
  }

  @Test(timeout = 4000)
  public void testBreakAndContinueWithAndWithoutLabels() {
    String source = "loop: for (;;) {\n" +
                    "  if (x) break;\n" +
                    "  if (y) continue;\n" +
                    "  if (z) break loop;\n" +
                    "  continue loop;\n" +
                    "}";
    Node ir = parseAndTransform(source);

    Node labelNode = findFirstNodeType(ir, Token.LABEL);
    assertNotNull(labelNode);
    Node breakNode = findFirstNodeType(ir, Token.BREAK);
    assertNotNull(breakNode);
    Node continueNode = findFirstNodeType(ir, Token.CONTINUE);
    assertNotNull(continueNode);
  }

  @Test(timeout = 4000)
  public void testMultipleLabelsOnStatement() {
    String source = "lbl1: lbl2: while (true) {}";
    Node ir = parseAndTransform(source);

    Node label1 = findFirstNodeType(ir, Token.LABEL);
    assertNotNull(label1);
    Node label2 = findFirstNodeType(label1, Token.LABEL);
    assertNotNull(label2);
  }

  @Test(timeout = 4000)
  public void testParenthesizedAndHookAndWith() {
    String source = "var a = (b ? c : d); with (obj) { f(); }";
    Node ir = parseAndTransform(source);

    Node hookNode = findFirstNodeType(ir, Token.HOOK);
    assertNotNull(hookNode);
    assertTrue("HOOK should have PARENTHESIZED_PROP set to true",
        hookNode.getBooleanProp(Node.PARENTHESIZED_PROP));
    assertNotNull("WITH node should be present", findFirstNodeType(ir, Token.WITH));
  }

  @Test(timeout = 4000)
  public void testNewExpressionAndCallAndElementGet() {
    String source = "var x = new Foo(1)[0];";
    Node ir = parseAndTransform(source);

    Node getElemNode = findFirstNodeType(ir, Token.GETELEM);
    assertNotNull(getElemNode);
    Node newNode = findFirstNodeType(getElemNode, Token.NEW);
    assertNotNull(newNode);
  }

  @Test(timeout = 4000)
  public void testRegExpLiteralWithAndWithoutFlags() {
    String source = "var r1 = /abc/; var r2 = /xyz/gi;";
    Node ir = parseAndTransform(source);

    Node r1 = ir.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.REGEXP, r1.getType());
    assertEquals(1, r1.getChildCount());

    Node r2 = ir.getChildAtIndex(1).getFirstChild().getFirstChild();
    assertEquals(Token.REGEXP, r2.getType());
    assertEquals(2, r2.getChildCount());
    assertEquals("gi", r2.getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testUnaryExpressionNumberFoldingAndIncDec() {
    String source = "var a = -42;\n" +
                    "var b = -x;\n" +
                    "x++; ++x; x--; --x;";
    Node ir = parseAndTransform(source);

    Node numNode = ir.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.NUMBER, numNode.getType());
    assertEquals(-42.0, numNode.getDouble(), 0.0001);

    Node negNode = ir.getChildAtIndex(1).getFirstChild().getFirstChild();
    assertEquals(Token.NEG, negNode.getType());

    Node postInc = ir.getChildAtIndex(2).getFirstChild();
    assertEquals(Token.INC, postInc.getType());
    assertTrue("Postfix increment should have INCRDECR_PROP",
        postInc.getBooleanProp(Node.INCRDECR_PROP));

    Node preInc = ir.getChildAtIndex(3).getFirstChild();
    assertEquals(Token.INC, preInc.getType());
    assertFalse("Prefix increment should not have INCRDECR_PROP",
        preInc.getBooleanProp(Node.INCRDECR_PROP));
  }

  @Test(timeout = 4000)
  public void testReturnStatementWithAndWithoutValue() {
    String source = "function f() { if (x) return; return 1; }";
    Node ir = parseAndTransform(source);

    Node fnNode = findFirstNodeType(ir, Token.FUNCTION);
    assertNotNull(fnNode);
    Node block = fnNode.getLastChild();
    Node returnWithoutVal = block.getFirstChild().getFirstChild().getLastChild().getFirstChild();
    assertEquals(Token.RETURN, returnWithoutVal.getType());
    assertEquals(0, returnWithoutVal.getChildCount());

    Node returnWithVal = block.getChildAtIndex(1);
    assertEquals(Token.RETURN, returnWithVal.getType());
    assertEquals(1, returnWithVal.getChildCount());
  }

  // ===================================================================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // ===================================================================================================================

  @Test(timeout = 4000)
  public void testSparseArrayLiteral_skipIndexes() {
    String source = "var arr = [1, , 3, , 5];";
    Node ir = parseAndTransform(source);

    Node arrayLit = findFirstNodeType(ir, Token.ARRAYLIT);
    assertNotNull(arrayLit);
    int[] skipIndexes = (int[]) arrayLit.getProp(Node.SKIP_INDEXES_PROP);
    assertNotNull("Sparse array must have SKIP_INDEXES_PROP", skipIndexes);
    assertArrayEquals(new int[]{1, 3}, skipIndexes);
    assertEquals(3, arrayLit.getChildCount());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralQuotedVsUnquotedKeys() {
    String source = "var obj = {'quoted': 1, unquoted: 2};";
    Node ir = parseAndTransform(source);

    Node objLit = findFirstNodeType(ir, Token.OBJECTLIT);
    assertNotNull(objLit);
    Node quotedKey = objLit.getFirstChild();
    assertEquals(Token.STRING, quotedKey.getType());
    assertTrue("Quoted key must have QUOTED_PROP", quotedKey.getBooleanProp(Node.QUOTED_PROP));

    Node unquotedKey = quotedKey.getNext();
    assertEquals(Token.STRING, unquotedKey.getType());
    assertFalse("Unquoted key must not have QUOTED_PROP", unquotedKey.getBooleanProp(Node.QUOTED_PROP));
  }

  @Test(timeout = 4000)
  public void testPosition2Charno_multilineCalculation() {
    String source = "var line1 = 1;\nvar line2 = 2;\nvar line3 = 3;";
    Node ir = parseAndTransform(source);

    Node var2 = ir.getChildAtIndex(1);
    assertEquals(Token.VAR, var2.getType());
    assertEquals(2, var2.getLineno());
    assertEquals(0, var2.getCharno());
  }

  @Test(timeout = 4000)
  public void testEmptyAstRoot() {
    String source = "";
    Node ir = parseAndTransform(source);
    assertNotNull(ir);
    assertEquals(Token.SCRIPT, ir.getType());
    assertEquals(0, ir.getChildCount());
  }

  @Test(timeout = 4000)
  public void testFileOverviewAndLicenseJSDocAttachment() {
    String source = "/**\n" +
                    " * @license Apache License 2.0\n" +
                    " */\n" +
                    "/**\n" +
                    " * @fileoverview Test overview description\n" +
                    " */\n" +
                    "var x = 1;";
    Node ir = parseAndTransform(source);

    JSDocInfo fileDoc = ir.getJSDocInfo();
    assertNotNull("Root SCRIPT node should have fileoverview JSDoc attached", fileDoc);
    assertNotNull("License should be merged into fileoverview JSDoc", fileDoc.getLicense());
  }

  // ===================================================================================================================
  // Partition D: Exception & Defensive Guard Paths
  // ===================================================================================================================

  @Test(timeout = 4000)
  public void testInvalidAssignmentTargetReported() {
    String source = "1 = 2;";
    TestErrorReporter reporter = new TestErrorReporter();
    parseAndTransform(source, true, true, reporter);

    assertTrue("Should report invalid assignment target",
        reporter.errors.contains("invalid assignment target"));
  }

  @Test(timeout = 4000)
  public void testInvalidIncrementDecrementTargetReported() {
    String source = "++42; --42;";
    TestErrorReporter reporter = new TestErrorReporter();
    parseAndTransform(source, true, true, reporter);

    assertTrue("Should report invalid increment target",
        reporter.errors.contains("invalid increment target"));
    assertTrue("Should report invalid decrement target",
        reporter.errors.contains("invalid decrement target"));
  }

  @Test(timeout = 4000)
  public void testGettersAndSettersNonES5ReportsError() {
    String source = "var o = { get a() { return 1; }, set b(v) {} };";
    TestErrorReporter reporter = new TestErrorReporter();
    parseAndTransform(source, false, true, reporter);

    assertTrue("Should report getters forbidden in non-ES5",
        reporter.errors.contains("getters are not supported in Internet Explorer"));
    assertTrue("Should report setters forbidden in non-ES5",
        reporter.errors.contains("setters are not supported in Internet Explorer"));
  }

  @Test(timeout = 4000)
  public void testGetterWithParamReportsError() {
    String source = "var o = { get a(param) { return param; } };";
    TestErrorReporter reporter = new TestErrorReporter();
    parseAndTransform(source, true, true, reporter);

    assertTrue("Should report getter with parameter",
        reporter.errors.contains("getters may not have parameters"));
  }

  @Test(timeout = 4000)
  public void testSetterWithZeroOrMultipleParamsReportsError() {
    String source = "var o = { set a() {}, set b(p1, p2) {} };";
    TestErrorReporter reporter = new TestErrorReporter();
    parseAndTransform(source, true, true, reporter);

    int count = 0;
    for (String err : reporter.errors) {
      if ("setters must have exactly one parameter".equals(err)) {
        count++;
      }
    }
    assertEquals("Should report setter parameter errors twice", 2, count);
  }

  @Test(timeout = 4000)
  public void testConstDeclarationWithoutConstKeywordAcceptance() {
    String source = "const c = 10;";
    TestErrorReporter reporter = new TestErrorReporter();
    parseAndTransform(source, true, false, reporter);

    boolean foundConstError = false;
    for (String err : reporter.errors) {
      if (err.contains("Unsupported syntax: CONST")) {
        foundConstError = true;
        break;
      }
    }
    assertTrue("Should report CONST unsupported syntax when acceptConstKeyword is false", foundConstError);
  }

  @Test(timeout = 4000)
  public void testDestructuringAssignmentReported() {
    AstRoot root = new AstRoot(0);
    ArrayLiteral arr = new ArrayLiteral(0);
    arr.setDestructuring(true);
    ExpressionStatement expr = new ExpressionStatement(arr);
    root.addChild(expr);

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createTestConfig(true, true);
    IRFactory.transformTree(root, "", config, reporter);

    assertTrue("Destructuring assignment error must be reported",
        reporter.errors.contains("destructuring assignment forbidden"));
  }

  @Test(timeout = 4000)
  public void testCatchClauseConditionReported() {
    AstRoot root = new AstRoot(0);
    CatchClause cc = new CatchClause(0);
    cc.setVarName(new Name(0, "e"));
    cc.setCatchCondition(new Name(0, "cond"));
    cc.setBody(new Block(0));
    root.addChild(cc);

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createTestConfig(true, true);
    IRFactory.transformTree(root, "", config, reporter);

    assertTrue("Conditional catch clauses must report unsupported",
        reporter.errors.contains("Catch clauses are not supported"));
  }

  @Test(timeout = 4000)
  public void testTransformTokenType_invalidTokenThrowsException() throws Throwable {
    Method m = IRFactory.class.getDeclaredMethod("transformTokenType", int.class);
    m.setAccessible(true);
    try {
      m.invoke(null, -99999);
      fail("Expected IllegalStateException for unknown token");
    } catch (InvocationTargetException e) {
      assertTrue("Cause must be IllegalStateException",
          e.getCause() instanceof IllegalStateException);
    }
  }

  // ===================================================================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // ===================================================================================================================

  @Test(timeout = 4000)
  public void testNodePropsCloneIntegrity() {
    String source = "var x = 1; var y = 'str';";
    Node ir = parseAndTransform(source);

    Node var1 = ir.getFirstChild();
    Node var2 = ir.getChildAtIndex(1);

    assertEquals("Source name prop should be cloned from templateNode to all nodes",
        var1.getProp(Node.SOURCENAME_PROP), var2.getProp(Node.SOURCENAME_PROP));
    assertEquals("testcode.js", var1.getProp(Node.SOURCENAME_PROP));
  }

  @Test(timeout = 4000)
  public void testCommentsNullSafety() {
    AstRoot root = new AstRoot(0);
    // root.getComments() returns null by default
    assertNull(root.getComments());

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createTestConfig(true, true);
    Node ir = IRFactory.transformTree(root, "", config, reporter);
    assertNotNull("Transforming AstRoot with null comments should complete safely", ir);
    assertEquals(Token.SCRIPT, ir.getType());
  }

  @Test(timeout = 4000)
  public void testNonJsdocCommentsIgnoredForFileOverview() {
    AstRoot root = new AstRoot(0);
    Comment blockComment = new Comment(0, 10,
        com.google.javascript.jscomp.mozilla.rhino.Token.CommentType.BLOCK_COMMENT,
        "/* not jsdoc */");
    root.addComment(blockComment);

    TestErrorReporter reporter = new TestErrorReporter();
    Config config = createTestConfig(true, true);
    Node ir = IRFactory.transformTree(root, "/* not jsdoc */", config, reporter);
    assertNotNull(ir);
    assertNull("Non-JSDOC comments must not set file overview JSDoc", ir.getJSDocInfo());
  }
}
