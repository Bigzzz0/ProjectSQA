/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.CodeConsumer
 *
 * Decision / Branch Matrix:
 * 1. addNumber(double x)
 *    - prev == '-' && x < 0                   -> Add space before negative number (e.g., x- -5)
 *    - prev == '-' && x == -0.0 (DEFECT)     -> Fails in defective code (Math.copySign/-0.0 not treated as < 0)
 *    - (long) x == x && !negativeZero         -> Integer representation branch
 *    - Math.abs(x) >= 100 with exp > 2        -> Scientific notation (e.g. 1000 -> 1E3)
 *    - Math.abs(x) >= 100 with exp <= 2       -> Plain integer notation (e.g. 100 -> 100)
 *    - (long) x != x (floating points)        -> String.valueOf(x)
 *    - negativeZero == true                   -> String.valueOf(x) produces "-0.0"
 *
 * 2. add(String newcode)
 *    - newcode.length() == 0                  -> Early return
 *    - (isWordChar(c) || c == '\\') && isWordChar(getLastChar()) -> Add space separation
 *    - c == '/' && getLastChar() == '/'       -> Add space to avoid regex/comment confusion (/ // / /)
 *    - regular characters                     -> Direct append
 *
 * 3. addOp(String op, boolean binOp)
 *    - (first == '+' || first == '-') && prev == first           -> Space added (e.g. ++, --)
 *    - Character.isLetter(first) && isWordChar(prev)             -> Space added (e.g. 'instanceof')
 *    - prev == '-' && first == '>'                               -> Space added to prevent '-->'
 *    - binOp == true / false                                     -> Triggers maybeCutLine() conditionally
 *
 * 4. Statement & Block State Transitions:
 *    - beginBlock() with statementNeedsEnded == true/false
 *    - endBlock(boolean shouldEndLine)
 *    - endStatement(boolean needSemiColon)
 *    - maybeEndStatement() with statementNeedsEnded == true/false
 *    - endFunction(boolean statementContext)
 *
 * 5. Helper & Boundary Utility Methods:
 *    - isWordChar: '_', '$', letters, digits, punctuation boundaries
 *    - isNegativeZero: 0.0 vs -0.0 vs negative numbers
 *    - shouldPreserveExtraBlocks, breakAfterBlockFor, continueProcessing, source mapping hooks
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import org.junit.Test;
import static org.junit.Assert.*;

public class CodeConsumerGeminiTest {

  // Concrete test harness subclassing the abstract CodeConsumer
  private static class TestCodeConsumer extends CodeConsumer {
    final StringBuilder sb = new StringBuilder();
    boolean lineBreakMaybeCalled = false;
    boolean cutLineMaybeCalled = false;
    boolean lineEnded = false;
    boolean newLineStarted = false;
    boolean preferredLineBreakNoted = false;
    boolean fileEnded = false;

    @Override
    char getLastChar() {
      return sb.length() == 0 ? '\0' : sb.charAt(sb.length() - 1);
    }

    @Override
    void append(String str) {
      sb.append(str);
    }

    @Override
    void startNewLine() {
      newLineStarted = true;
    }

    @Override
    void maybeCutLine() {
      cutLineMaybeCalled = true;
    }

    @Override
    void maybeLineBreak() {
      lineBreakMaybeCalled = true;
      super.maybeLineBreak();
    }

    @Override
    void endLine() {
      lineEnded = true;
    }

    @Override
    void notePreferredLineBreak() {
      preferredLineBreakNoted = true;
    }

    @Override
    void endFile() {
      fileEnded = true;
    }
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBeginBlockWhenStatementNeedsEnded() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementStarted = true;
    consumer.endStatement(false);
    assertTrue(consumer.statementNeedsEnded);

    consumer.beginBlock();

    assertEquals(";{", consumer.sb.toString());
    assertTrue(consumer.lineBreakMaybeCalled);
    assertTrue(consumer.lineEnded);
    assertFalse(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testBeginBlockWhenStatementDoesNotNeedEnded() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    assertFalse(consumer.statementNeedsEnded);

    consumer.beginBlock();

    assertEquals("{", consumer.sb.toString());
    assertFalse(consumer.lineBreakMaybeCalled);
    assertTrue(consumer.lineEnded);
    assertFalse(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testEndBlockWithoutEndingLine() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementNeedsEnded = true;

    consumer.endBlock();

    assertEquals("}", consumer.sb.toString());
    assertFalse(consumer.lineEnded);
    assertFalse(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testEndBlockWithEndingLine() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementNeedsEnded = true;

    consumer.endBlock(true);

    assertEquals("}", consumer.sb.toString());
    assertTrue(consumer.lineEnded);
    assertFalse(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testEndStatementNeedSemicolon() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementNeedsEnded = true;

    consumer.endStatement(true);

    assertEquals(";", consumer.sb.toString());
    assertTrue(consumer.lineBreakMaybeCalled);
    assertFalse(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testEndStatementWithoutSemicolonStatementNotStarted() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementStarted = false;

    consumer.endStatement(false);

    assertEquals("", consumer.sb.toString());
    assertFalse(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testEndStatementWithoutSemicolonStatementStarted() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementStarted = true;

    consumer.endStatement(false);

    assertEquals("", consumer.sb.toString());
    assertTrue(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testMaybeEndStatementWhenNeeded() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementNeedsEnded = true;

    consumer.maybeEndStatement();

    assertEquals(";", consumer.sb.toString());
    assertTrue(consumer.lineBreakMaybeCalled);
    assertTrue(consumer.lineEnded);
    assertFalse(consumer.statementNeedsEnded);
    assertTrue(consumer.statementStarted);
  }

  @Test(timeout = 4000)
  public void testMaybeEndStatementWhenNotNeeded() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementNeedsEnded = false;

    consumer.maybeEndStatement();

    assertEquals("", consumer.sb.toString());
    assertFalse(consumer.lineBreakMaybeCalled);
    assertFalse(consumer.lineEnded);
    assertTrue(consumer.statementStarted);
  }

  @Test(timeout = 4000)
  public void testEndFunctionBranches() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    assertFalse(consumer.sawFunction);

    consumer.endFunction(false);
    assertTrue(consumer.sawFunction);
    assertFalse(consumer.lineEnded);

    consumer.endFunction(true);
    assertTrue(consumer.sawFunction);
    assertTrue(consumer.lineEnded);

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    consumer2.endFunction();
    assertTrue(consumer2.sawFunction);
    assertFalse(consumer2.lineEnded);
  }

  @Test(timeout = 4000)
  public void testListSeparator() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.listSeparator();

    assertEquals(",", consumer.sb.toString());
    assertTrue(consumer.lineBreakMaybeCalled);
  }

  @Test(timeout = 4000)
  public void testCaseBody() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.beginCaseBody();
    consumer.endCaseBody();
    assertEquals(":", consumer.sb.toString());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Operator Separation
  // =========================================================================

  @Test(timeout = 4000)
  public void testAddEmptyString() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.add("");
    assertEquals("", consumer.sb.toString());
    assertTrue(consumer.statementStarted);
  }

  @Test(timeout = 4000)
  public void testAddWordAfterWordAddsSpace() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.add("return");
    consumer.add("foo");
    assertEquals("return foo", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddEscapeSequenceAfterWordAddsSpace() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.add("var");
    consumer.add("\\u0041");
    assertEquals("var \\u0041", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddSlashAfterSlashAddsSpace() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.append("/");
    consumer.add("/");
    assertEquals("/ /", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddIdentifierCallsAdd() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.addIdentifier("myIdentifier");
    assertEquals("myIdentifier", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddOpPlusPlusAddsSpace() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.append("+");
    consumer.addOp("++", false);
    assertEquals("+ ++", consumer.sb.toString());
    assertFalse(consumer.cutLineMaybeCalled);
  }

  @Test(timeout = 4000)
  public void testAddOpMinusMinusAddsSpace() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.append("-");
    consumer.addOp("--", false);
    assertEquals("- --", consumer.sb.toString());
    assertFalse(consumer.cutLineMaybeCalled);
  }

  @Test(timeout = 4000)
  public void testAddOpWordBoundaryAddsSpace() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.append("x");
    consumer.addOp("instanceof", false);
    assertEquals("x instanceof", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddOpMinusGreaterThanAddsSpace() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.append("-");
    consumer.addOp(">", false);
    assertEquals("- >", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddOpBinaryOpTriggersCutLine() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.addOp("*", true);
    assertEquals("*", consumer.sb.toString());
    assertTrue(consumer.cutLineMaybeCalled);
  }

  @Test(timeout = 4000)
  public void testAddNumberScientificNotation() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.addNumber(1000.0);
    assertEquals("1E3", consumer.sb.toString());

    TestCodeConsumer consumer6 = new TestCodeConsumer();
    consumer6.addNumber(1000000.0);
    assertEquals("1E6", consumer6.sb.toString());

    TestCodeConsumer consumerNegExp = new TestCodeConsumer();
    consumerNegExp.addNumber(-1000.0);
    assertEquals("-1E3", consumerNegExp.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddNumberPlainIntegers() {
    TestCodeConsumer consumer100 = new TestCodeConsumer();
    consumer100.addNumber(100.0);
    assertEquals("100", consumer100.sb.toString());

    TestCodeConsumer consumer0 = new TestCodeConsumer();
    consumer0.addNumber(0.0);
    assertEquals("0", consumer0.sb.toString());

    TestCodeConsumer consumer99 = new TestCodeConsumer();
    consumer99.addNumber(99.0);
    assertEquals("99", consumer99.sb.toString());

    TestCodeConsumer consumerNonZeroMantissa = new TestCodeConsumer();
    consumerNonZeroMantissa.addNumber(1050.0);
    assertEquals("1050", consumerNonZeroMantissa.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddNumberFloatingPoint() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.addNumber(3.14159);
    assertEquals("3.14159", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddNegativeNumberAfterMinus() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.append("x-");
    consumer.addNumber(-4.0);
    assertEquals("x- -4", consumer.sb.toString());
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets known Defect: CodePrinterTest::testMinusNegativeZero
   * Expected: "x- -0.0"
   * Buggy behavior: "x--0.0" because (-0.0 < 0) evaluates to false in Java,
   * bypassing the negative check when prev == '-'.
   */
  @Test(timeout = 4000)
  public void testMinusNegativeZeroSpaceAdded() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.append("x-");
    consumer.addNumber(-0.0);
    assertEquals("x- -0.0", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testNegativeZeroFormattingWithoutMinusPrev() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.addNumber(-0.0);
    assertEquals("-0.0", consumer.sb.toString());
  }

  // =========================================================================
  // Partition D: Static Predicates & Boundary Conditions
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsNegativeZero() {
    assertTrue(CodeConsumer.isNegativeZero(-0.0));
    assertFalse(CodeConsumer.isNegativeZero(0.0));
    assertFalse(CodeConsumer.isNegativeZero(-1.0));
    assertFalse(CodeConsumer.isNegativeZero(1.0));
    assertFalse(CodeConsumer.isNegativeZero(Double.MIN_VALUE));
    assertFalse(CodeConsumer.isNegativeZero(-Double.MIN_VALUE));
  }

  @Test(timeout = 4000)
  public void testIsWordChar() {
    assertTrue(CodeConsumer.isWordChar('_'));
    assertTrue(CodeConsumer.isWordChar('$'));
    assertTrue(CodeConsumer.isWordChar('a'));
    assertTrue(CodeConsumer.isWordChar('z'));
    assertTrue(CodeConsumer.isWordChar('A'));
    assertTrue(CodeConsumer.isWordChar('Z'));
    assertTrue(CodeConsumer.isWordChar('0'));
    assertTrue(CodeConsumer.isWordChar('9'));

    assertFalse(CodeConsumer.isWordChar(' '));
    assertFalse(CodeConsumer.isWordChar('+'));
    assertFalse(CodeConsumer.isWordChar('-'));
    assertFalse(CodeConsumer.isWordChar(';'));
    assertFalse(CodeConsumer.isWordChar('/'));
    assertFalse(CodeConsumer.isWordChar('\\'));
    assertFalse(CodeConsumer.isWordChar('\0'));
  }

  // =========================================================================
  // Partition E: Default Methods & Lifecycle Hooks Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefaultImplementations() {
    TestCodeConsumer consumer = new TestCodeConsumer();

    assertTrue(consumer.continueProcessing());
    assertFalse(consumer.shouldPreserveExtraBlocks());

    Node dummyNode = new Node(0);
    assertTrue(consumer.breakAfterBlockFor(dummyNode, true));
    assertFalse(consumer.breakAfterBlockFor(dummyNode, false));

    // Void methods should execute without exceptions
    consumer.startSourceMapping(dummyNode);
    consumer.endSourceMapping(dummyNode);
    consumer.startNewLine();
    consumer.notePreferredLineBreak();
    consumer.endFile();

    assertTrue(consumer.newLineStarted);
    assertTrue(consumer.preferredLineBreakNoted);
    assertTrue(consumer.fileEnded);
  }
}