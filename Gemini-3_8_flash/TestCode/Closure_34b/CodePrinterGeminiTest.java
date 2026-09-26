package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.debugging.sourcemap.FilePosition;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * /* [Branch & Defect Analysis Matrix]
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. Defect-Targeted (Closure Compiler StackOverflow on deep AST recursion):
 *    - testManyAdds: Builds deeply nested Token.ADD binary expression tree (10,000 nodes)
 *      to expose recursive traversal depth exhaustion (StackOverflowError).
 * 2. Partition A (Core Functional Logic & Formats):
 *    - CompactCodePrinter format execution.
 *    - PrettyCodePrinter format execution (indentation, block placement, function body).
 *    - TypedCodeGenerator format (Format.TYPED via outputTypes = true).
 *    - Strict ECMASCRIPT 5 tag injection ('use strict').
 * 3. Partition B (Boundary Value Analysis):
 *    - lineLengthThreshold <= 0 normalized to Integer.MAX_VALUE.
 *    - Line breaking triggers in CompactCodePrinter (semicolon upgrade, cut insertion).
 *    - Prefer line break at end of file (lineLength > threshold/2 vs shifted previous cut vs small file).
 *    - Custom Charset escaping (US_ASCII vs UTF-8).
 * 4. Partition C (Defensive Guard & Exception Paths):
 *    - Null root Node in Builder.build() throws IllegalStateException.
 *    - Null SourceMap.DetailLevel in Builder throws IllegalStateException.
 * 5. Partition D (AST Grammar & Block Invariant Coverage):
 *    - PrettyCodePrinter.breakAfterBlockFor:
 *      * Token.DO (do-while block suppresses break before while)
 *      * Token.FUNCTION (function blocks suppress break)
 *      * Token.TRY (try without catch / firstChild)
 *      * Token.CATCH (catch with and without finally)
 *      * Token.IF (then block vs else block)
 *    - Switch / Case body indentation.
 * 6. Partition E (Source Map Integration & Position Mapping):
 *    - startSourceMapping / endSourceMapping when sourceFileName and lineno are populated.
 *    - Source map cut reporting and position renormalization.
 *    - CodePrinter.Format enum values and valueOf coverage.
 */
public class CodePrinterGeminiTest {

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Compiler testManyAdds)
  // =========================================================================

  /**
   * Targets the known defect in Closure Compiler where deep recursion on
   * binary operators (e.g. nested ADD nodes) triggers a StackOverflowError.
   */
  @Test(timeout = 4000)
  public void testManyAdds() {
    Node n = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    for (int i = 0; i < 10000; i++) {
      n = new Node(Token.ADD, n, Node.newNumber(i));
    }

    String result = new CodePrinter.Builder(n).build();
    assertNotNull("Generated code must not be null", result);
    assertTrue("Result must contain addition operations", result.length() > 0);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testCompactPrintingSimpleExpression() {
    Node expr = new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "foo"));
    String code = new CodePrinter.Builder(expr).build();
    assertEquals("foo;", code);
  }

  @Test(timeout = 4000)
  public void testPrettyPrintingBlockIndentation() {
    Node block = new Node(Token.BLOCK);
    Node expr1 = new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "a"));
    Node expr2 = new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "b"));
    block.addChildToBack(expr1);
    block.addChildToBack(expr2);

    String code = new CodePrinter.Builder(block).setPrettyPrint(true).build();
    assertNotNull(code);
    assertTrue(code.contains("{\n"));
    assertTrue(code.contains("  a;\n"));
    assertTrue(code.contains("  b;\n"));
    assertTrue(code.contains("}"));
  }

  @Test(timeout = 4000)
  public void testTypedOutputFormat() {
    Node expr = new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x"));
    String code = new CodePrinter.Builder(expr).setOutputTypes(true).build();
    assertNotNull(code);
    assertTrue(code.contains("x;"));
  }

  @Test(timeout = 4000)
  public void testTagAsStrict() {
    Node expr = new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "x"));
    String code = new CodePrinter.Builder(expr).setTagAsStrict(true).build();
    assertNotNull(code);
    assertTrue(code.contains("'use strict'"));
  }

  @Test(timeout = 4000)
  public void testOutputCharsetAsciiEscaping() {
    Node str = Node.newString("unicode_\u00A9");
    Node expr = new Node(Token.EXPR_RESULT, str);

    String code = new CodePrinter.Builder(expr)
        .setOutputCharset(StandardCharsets.US_ASCII)
        .build();
    assertNotNull(code);
    assertTrue(code.contains("\\x"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testThresholdZeroAndNegativeBecomesMax() {
    Node expr = new Node(Token.EXPR_RESULT, Node.newNumber(123));

    String codeZero = new CodePrinter.Builder(expr)
        .setLineLengthThreshold(0)
        .build();
    assertEquals("123;", codeZero);

    String codeNeg = new CodePrinter.Builder(expr)
        .setLineLengthThreshold(-10)
        .build();
    assertEquals("123;", codeNeg);
  }

  @Test(timeout = 4000)
  public void testCompactLineLengthThresholdCut() {
    Node block = new Node(Token.BLOCK);
    for (int i = 0; i < 20; i++) {
      block.addChildToBack(new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "longIdentifierName" + i)));
    }

    String code = new CodePrinter.Builder(block)
        .setLineBreak(true)
        .setLineLengthThreshold(30)
        .build();
    assertNotNull(code);
    assertTrue(code.contains("\n"));
  }

  @Test(timeout = 4000)
  public void testPreferLineBreakAtEndOfFileWithLargeLineLength() {
    Node block = new Node(Token.BLOCK);
    for (int i = 0; i < 5; i++) {
      block.addChildToBack(new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "varName" + i)));
    }

    // Set threshold so that lineLength > threshold / 2 triggers the extra break
    String code = new CodePrinter.Builder(block)
        .setPreferLineBreakAtEndOfFile(true)
        .setLineLengthThreshold(20)
        .build();
    assertNotNull(code);
    assertTrue(code.endsWith(";\n"));
  }

  @Test(timeout = 4000)
  public void testPreferLineBreakAtEndOfFileWithShiftedCut() {
    Node block = new Node(Token.BLOCK);
    for (int i = 0; i < 15; i++) {
      block.addChildToBack(new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "v" + i)));
    }

    // Forces line breaks and checks prevCutPosition > 0 shift logic
    String code = new CodePrinter.Builder(block)
        .setPreferLineBreakAtEndOfFile(true)
        .setLineLengthThreshold(15)
        .build();
    assertNotNull(code);
    assertTrue(code.endsWith(";\n"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBuilderNullRootThrows() {
    new CodePrinter.Builder(null).build();
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBuilderNullDetailLevelThrows() {
    Node expr = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    new CodePrinter.Builder(expr).setSourceMapDetailLevel(null);
  }

  // =========================================================================
  // Partition E: Grammar, Pretty Printer AST Branches & Lifecycle
  // =========================================================================

  @Test(timeout = 4000)
  public void testPrettyDoWhileNoBreakBeforeWhile() {
    Node body = new Node(Token.BLOCK);
    body.addChildToBack(new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    Node condition = Node.newNumber(0);
    Node doWhile = new Node(Token.DO, body, condition);

    String code = new CodePrinter.Builder(doWhile).setPrettyPrint(true).build();
    assertNotNull(code);
    assertTrue(code.contains("do {"));
    assertTrue(code.contains("} while (0);"));
  }

  @Test(timeout = 4000)
  public void testPrettyFunctionNoBreak() {
    Node name = Node.newString(Token.NAME, "myFunc");
    Node params = new Node(Token.LP);
    Node body = new Node(Token.BLOCK);
    body.addChildToBack(new Node(Token.EXPR_RESULT, Node.newNumber(10)));
    Node fn = new Node(Token.FUNCTION, name, params, body);

    String code = new CodePrinter.Builder(fn).setPrettyPrint(true).build();
    assertNotNull(code);
    assertTrue(code.contains("function myFunc() {"));
  }

  @Test(timeout = 4000)
  public void testPrettyIfElseBreakAfterBlock() {
    Node cond = Node.newNumber(1);
    Node thenBlock = new Node(Token.BLOCK);
    thenBlock.addChildToBack(new Node(Token.EXPR_RESULT, Node.newNumber(2)));
    Node elseBlock = new Node(Token.BLOCK);
    elseBlock.addChildToBack(new Node(Token.EXPR_RESULT, Node.newNumber(3)));
    Node ifNode = new Node(Token.IF, cond, thenBlock, elseBlock);

    String code = new CodePrinter.Builder(ifNode).setPrettyPrint(true).build();
    assertNotNull(code);
    assertTrue(code.contains("if (1) {"));
    assertTrue(code.contains("else {"));
  }

  @Test(timeout = 4000)
  public void testPrettyTryCatchFinallyBreakBranches() {
    Node tryBlock = new Node(Token.BLOCK);
    tryBlock.addChildToBack(new Node(Token.EXPR_RESULT, Node.newNumber(1)));

    Node catchVar = Node.newString(Token.NAME, "err");
    Node catchBody = new Node(Token.BLOCK);
    catchBody.addChildToBack(new Node(Token.EXPR_RESULT, Node.newNumber(2)));
    Node catchNode = new Node(Token.CATCH, catchVar, catchBody);
    Node catchWrapper = new Node(Token.BLOCK, catchNode);

    Node finallyBlock = new Node(Token.BLOCK);
    finallyBlock.addChildToBack(new Node(Token.EXPR_RESULT, Node.newNumber(3)));

    Node tryNode = new Node(Token.TRY, tryBlock, catchWrapper, finallyBlock);

    String code = new CodePrinter.Builder(tryNode).setPrettyPrint(true).build();
    assertNotNull(code);
    assertTrue(code.contains("try {"));
    assertTrue(code.contains("catch (err) {"));
    assertTrue(code.contains("finally {"));
  }

  @Test(timeout = 4000)
  public void testPrettySwitchCaseIndentation() {
    Node switchVal = Node.newNumber(1);
    Node caseBody = new Node(Token.BLOCK);
    caseBody.addChildToBack(new Node(Token.EXPR_RESULT, Node.newNumber(100)));
    Node caseNode = new Node(Token.CASE, Node.newNumber(1), caseBody);
    Node switchNode = new Node(Token.SWITCH, switchVal, caseNode);

    String code = new CodePrinter.Builder(switchNode).setPrettyPrint(true).build();
    assertNotNull(code);
    assertTrue(code.contains("switch (1) {"));
    assertTrue(code.contains("case 1:"));
  }

  @Test(timeout = 4000)
  public void testSourceMapGenerationWithProxy() {
    Node block = new Node(Token.BLOCK);
    Node nameNode = Node.newString(Token.NAME, "varWithLocation");
    nameNode.setLineno(42);
    nameNode.setSourceFileName("source.js");
    block.addChildToBack(new Node(Token.EXPR_RESULT, nameNode));

    final AtomicInteger addMappingCalls = new AtomicInteger(0);
    SourceMap sourceMapProxy = (SourceMap) Proxy.newProxyInstance(
        SourceMap.class.getClassLoader(),
        new Class<?>[]{SourceMap.class},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("addMapping".equals(method.getName())) {
              addMappingCalls.incrementAndGet();
            }
            return null;
          }
        });

    String code = new CodePrinter.Builder(block)
        .setSourceMap(sourceMapProxy)
        .setSourceMapDetailLevel(SourceMap.DetailLevel.ALL)
        .build();

    assertNotNull(code);
    assertTrue("Source map addMapping must be invoked", addMappingCalls.get() > 0);
  }

  @Test(timeout = 4000)
  public void testCompactLineCutWithSourceMapPositionRenormalization() {
    Node block = new Node(Token.BLOCK);
    for (int i = 0; i < 20; i++) {
      Node nameNode = Node.newString(Token.NAME, "cutIdentifier" + i);
      nameNode.setLineno(i + 1);
      nameNode.setSourceFileName("source.js");
      block.addChildToBack(new Node(Token.EXPR_RESULT, nameNode));
    }

    final List<Object[]> mappings = new ArrayList<>();
    SourceMap sourceMapProxy = (SourceMap) Proxy.newProxyInstance(
        SourceMap.class.getClassLoader(),
        new Class<?>[]{SourceMap.class},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("addMapping".equals(method.getName())) {
              mappings.add(args);
            }
            return null;
          }
        });

    String code = new CodePrinter.Builder(block)
        .setLineLengthThreshold(30)
        .setPreferLineBreakAtEndOfFile(true)
        .setSourceMap(sourceMapProxy)
        .build();

    assertNotNull(code);
    assertFalse("Mappings must be captured during source map generation", mappings.isEmpty());
  }

  @Test(timeout = 4000)
  public void testFormatEnumValues() {
    for (CodePrinter.Format format : CodePrinter.Format.values()) {
      assertEquals(format, CodePrinter.Format.valueOf(format.name()));
    }
  }

  @Test(timeout = 4000)
  public void testDefaultLineLengthThresholdConstant() {
    assertEquals(500, CodePrinter.DEFAULT_LINE_LENGTH_THRESHOLD);
  }
}
}
