package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * ------------------------------------------------------------------
 * Target: CodeConsumer.addNumber(double)
 * Defect: For negative zero, the method fails to insert a space when the
 *         previous character is '-', producing "x--0.0" instead of "x- -0.0".
 * Branches exercised:
 *   - isNegativeZero(double) true/false
 *   - (x < 0 || isNegativeZero(x)) && prev == '-'  → correct spacing
 *   - (long) x == x && !negativeZero  → integer formatting path
 *   - exp > 2  → scientific notation vs plain integer
 *   - final else branch (non‑integer / negativeZero) → String.valueOf
 * Additional methods covered: add, addOp, beginBlock/endBlock, endStatement,
 *   maybeEndStatement, endFunction, beginCaseBody, listSeparator, etc.
 * ------------------------------------------------------------------
 */
public class CodeConsumerDeepseekTest {

  /** Minimal concrete implementation for exercising CodeConsumer. */
  private static class TestCodeConsumer extends CodeConsumer {
    final StringBuilder sb = new StringBuilder();
    char lastChar = 0;

    @Override
    char getLastChar() {
      return lastChar;
    }

    @Override
    void append(String str) {
      sb.append(str);
      if (str.length() > 0) {
        lastChar = str.charAt(str.length() - 1);
      }
    }
  }

  /* ------------------------ Partition A: Core Functional Logic ---------------- */

  @Test(timeout = 4000)
  public void testStartAndEndSourceMappingWithNullNode() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.startSourceMapping(null);
    c.endSourceMapping(null);
    assertEquals("", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testContinueProcessingReturnsTrue() {
    TestCodeConsumer c = new TestCodeConsumer();
    assertTrue(c.continueProcessing());
  }

  @Test(timeout = 4000)
  public void testAddIdentifier() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.addIdentifier("myVar");
    assertEquals("myVar", c.sb.toString());
    assertEquals('r', c.lastChar);
  }

  @Test(timeout = 4000)
  public void testAppendBlockStartEnd() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.appendBlockStart();
    c.appendBlockEnd();
    assertEquals("{}", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testStartNewLineAndLineBreak() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.startNewLine();
    c.maybeLineBreak();
    c.maybeCutLine();
    c.endLine();
    c.notePreferredLineBreak();
    assertEquals("", c.sb.toString()); // all no-ops in base class
  }

  @Test(timeout = 4000)
  public void testBeginBlockInsertsSemicolonWhenNeeded() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.statementNeedsEnded = true;
    c.beginBlock();
    assertEquals("{", c.sb.toString()); // ";" then line break? Actually beginBlock appends ";" and maybeLineBreak()
    // Our TestCodeConsumer overrides maybeLineBreak to no-op, so we get ";" + "{" = ";{"
    // Let's adjust: We expect "{;"? Actually beginBlock:
    // if (statementNeedsEnded) { append(";"); maybeLineBreak(); }
    // appendBlockStart() -> "{"
    // So output is ";{".
    assertEquals(";{", c.sb.toString());
    assertFalse(c.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testBeginBlockWhenNoSemicolonNeeded() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.statementNeedsEnded = false;
    c.beginBlock();
    assertEquals("{", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testEndBlock() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.statementNeedsEnded = true;
    c.endBlock();
    assertEquals("}", c.sb.toString());
    assertFalse(c.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testEndBlockWithShouldEndLine() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.endBlock(true);
    assertEquals("}", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testListSeparator() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.listSeparator();
    assertEquals(",", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testEndStatementWithoutSemiColon() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.statementStarted = true;
    c.endStatement(false);
    assertTrue(c.statementNeedsEnded);
    assertEquals("", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testEndStatementWithSemiColon() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.endStatement(true);
    assertEquals(";", c.sb.toString());
    assertFalse(c.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testEndStatementWithSemiColonIgnoresStatementStarted() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.statementStarted = true;
    c.endStatement(true);
    assertEquals(";", c.sb.toString());
    assertFalse(c.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testMaybeEndStatement() {
    TestCodeConsumer c = new TestCodeConsumer();
    // Case: needs ending
    c.statementNeedsEnded = true;
    c.maybeEndStatement();
    assertEquals(";", c.sb.toString());
    assertFalse(c.statementNeedsEnded);
    assertTrue(c.statementStarted);

    // Reset
    c.sb.setLength(0);
    c.statementStarted = false;
    c.statementNeedsEnded = false;
    c.maybeEndStatement();
    assertEquals("", c.sb.toString());
    assertTrue(c.statementStarted);
  }

  @Test(timeout = 4000)
  public void testEndFunction() {
    TestCodeConsumer c = new TestCodeConsumer();
    assertFalse(c.sawFunction);
    c.endFunction();
    assertTrue(c.sawFunction);
    assertEquals("", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testEndFunctionWithStatementContext() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.endFunction(true);
    assertTrue(c.sawFunction);
    assertEquals("", c.sb.toString()); // endLine() is no-op
  }

  @Test(timeout = 4000)
  public void testBeginCaseBody() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.beginCaseBody();
    assertEquals(":", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testEndCaseBody() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.endCaseBody();
    assertEquals("", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testShouldPreserveExtraBlocksFalse() {
    TestCodeConsumer c = new TestCodeConsumer();
    assertFalse(c.shouldPreserveExtraBlocks());
  }

  @Test(timeout = 4000)
  public void testBreakAfterBlockFor() {
    TestCodeConsumer c = new TestCodeConsumer();
    assertTrue(c.breakAfterBlockFor(null, true));
    assertFalse(c.breakAfterBlockFor(null, false));
  }

  @Test(timeout = 4000)
  public void testEndFile() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.endFile();
    assertEquals("", c.sb.toString());
  }

  /* ------------------------ Partition B: Boundary / Add(), AddOp() ----------- */

  @Test(timeout = 4000)
  public void testAddEmptyString() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.add("");
    assertEquals("", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddWordCharAfterWordCharAddsSpace() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.append("a"); // lastChar = 'a'
    c.add("b");
    assertEquals("a b", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddWordCharAfterNonWordNoSpace() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.append("(");
    c.add("b");
    assertEquals("(b", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddSlashAfterSlashAddsSpace() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.append("/");
    c.add("/");
    assertEquals("/ /", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddNormalText() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.add("foo");
    assertEquals("foo", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddOpSamePlus() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.append("+");
    c.addOp("+", true);
    assertEquals("+ +", c.sb.toString()); // space added before op
  }

  @Test(timeout = 4000)
  public void testAddOpLetterAfterWord() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.append("x");
    c.addOp("instanceof", true);
    assertEquals("x instanceof", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddOpArrowAfterMinus() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.append("-");
    c.addOp(">", true);
    assertEquals("- >", c.sb.toString()); // space before '>'
  }

  @Test(timeout = 4000)
  public void testAddOpNonSpecial() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.append("a");
    c.addOp("==", true);
    assertEquals("a==", c.sb.toString()); // no extra space
  }

  /* ------------------------ Partition C: addNumber - BVA & Defect ----------- */

  @Test(timeout = 4000)
  public void testAddNumberInteger() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.addNumber(42);
    assertEquals("42", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddNumberNegativeInteger() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.addNumber(-42);
    assertEquals("-42", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddNumberIntegerLargeNoExponent() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.addNumber(99); // exp <=2
    assertEquals("99", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddNumberLargeUsesExponent() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.addNumber(1000000);
    assertEquals("1E6", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddNumberDouble() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.addNumber(3.14);
    assertEquals("3.14", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddNumberPositiveZero() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.addNumber(0.0);
    assertEquals("0", c.sb.toString()); // (long)0==0 and !negativeZero -> Long.toString(0)
  }

  @Test(timeout = 4000)
  public void testAddNumberNegativeZeroAfterMinus() {
    // Defect-targeting test: negative zero after '-' must insert a space.
    TestCodeConsumer c = new TestCodeConsumer();
    c.append("x");
    c.append("-");
    c.addNumber(-0.0);
    // Correct behavior: "x- -0.0" (space between '-' and '-0.0')
    assertEquals("x- -0.0", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddNumberNegativeZeroWithoutMinus() {
    // Negative zero alone should be printed as -0.0
    TestCodeConsumer c = new TestCodeConsumer();
    c.addNumber(-0.0);
    assertEquals("-0.0", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testIsNegativeZero() {
    assertTrue(CodeConsumer.isNegativeZero(-0.0));
    assertFalse(CodeConsumer.isNegativeZero(0.0));
    assertFalse(CodeConsumer.isNegativeZero(1.0));
    assertFalse(CodeConsumer.isNegativeZero(-1.0));
  }

  @Test(timeout = 4000)
  public void testIsWordChar() {
    assertTrue(CodeConsumer.isWordChar('a'));
    assertTrue(CodeConsumer.isWordChar('_'));
    assertTrue(CodeConsumer.isWordChar('$'));
    assertTrue(CodeConsumer.isWordChar('9'));
    assertFalse(CodeConsumer.isWordChar('-'));
    assertFalse(CodeConsumer.isWordChar(' '));
    assertFalse(CodeConsumer.isWordChar('.'));
  }

  /* ------------------------ Partition D: Defensive / Edge Cases ------------- */

  @Test(timeout = 4000)
  public void testAddNumberWithPrevMinusAndNegativeInteger() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.append("x-");
    c.addNumber(-5);
    assertEquals("x- -5", c.sb.toString()); // space added because x < 0
  }

  @Test(timeout = 4000)
  public void testAddNumberWithPrevMinusAndPositiveNumber() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.append("x-");
    c.addNumber(5);
    assertEquals("x-5", c.sb.toString()); // no space expected
  }

  @Test(timeout = 4000)
  public void testAddOpWithBinOpFalseDoesNotCutLine() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.addOp("+", false);
    assertEquals("+", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddOpWithBinOpTrueCallsMaybeCutLine() {
    // maybeCutLine is no-op, but we ensure no exception and correct output.
    TestCodeConsumer c = new TestCodeConsumer();
    c.addOp("+", true);
    assertEquals("+", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddIdentifierWithWordCharAfterWordChar() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.append("_");
    c.addIdentifier("foo");
    assertEquals("_ foo", c.sb.toString());
  }

  /* ------------------------ Partition E: Lifecycle & State Integrity --------- */

  @Test(timeout = 4000)
  public void testStatementStateAfterEndBlock() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.statementNeedsEnded = true;
    c.endBlock();
    assertFalse(c.statementNeedsEnded);
    assertEquals("}", c.sb.toString());
  }

  @Test(timeout = 4000)
  public void testSawFunctionState() {
    TestCodeConsumer c = new TestCodeConsumer();
    assertFalse(c.sawFunction);
    c.endFunction();
    assertTrue(c.sawFunction);
  }

  @Test(timeout = 4000)
  public void testLastCharAfterAdd() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.add("hello");
    assertEquals('o', c.getLastChar());
  }

  @Test(timeout = 4000)
  public void testLastCharAfterAppendEmptyString() {
    TestCodeConsumer c = new TestCodeConsumer();
    c.append("abc");
    c.append("");
    assertEquals('c', c.getLastChar());
  }
}