package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.CodeConsumer
 *
 * 1. Defect-Targeted Zone (Issue 620):
 *    - In add(String newcode), the comment "// Do not allow a forward slash to appear after a DIV..."
 *      is mislocated inside the word-char condition instead of guarding regex literals after a division
 *      operator.
 *    - Defect symptom: `alert(/ // / /)` prints as `alert(/ /// /)`, turning the second regex and
 *      division into an invalid token stream or line comment delimiter.
 *    - Target tests: testIssue620ForwardSlashAfterDiv, testIssue620RegexpDivRegexpSpacing.
 *
 * 2. Decision Branches Covered:
 *    - beginBlock(): statementNeedsEnded (true / false)
 *    - endBlock(boolean): shouldEndLine (true / false)
 *    - endBlock(): default delegation to endBlock(false)
 *    - endStatement(boolean): needSemiColon true; needSemiColon false && statementStarted true;
 *      needSemiColon false && statementStarted false
 *    - endStatement(): default delegation to endStatement(false)
 *    - maybeEndStatement(): statementNeedsEnded true; statementNeedsEnded false
 *    - endFunction(boolean): statementContext true; statementContext false
 *    - endFunction(): default delegation to endFunction(false)
 *    - add(String):
 *        * length == 0
 *        * isWordChar(c) && isWordChar(prev) -> space injection
 *        * c == '\\' && isWordChar(prev) -> space injection
 *        * c == '/' && prev == '/' -> space injection (Defect 620)
 *        * non-word chars -> no space
 *    - addOp(String, boolean):
 *        * (first == '+' || first == '-') && prev == first -> space injection
 *        * Character.isLetter(first) && isWordChar(prev) -> space injection
 *        * prev == '-' && first == '>' -> space injection (preventing -->)
 *        * binOp true -> maybeCutLine called; binOp false -> not called
 *    - addNumber(double):
 *        * x < 0 && prev == '-' -> space injection (preventing x--4)
 *        * (long) x == x && !isNegativeZero(x):
 *            - Math.abs(x) >= 100:
 *                * exp > 2 -> scientific notation "mantissaEexp"
 *                * exp <= 2 -> regular long string
 *            - Math.abs(x) < 100 -> regular long string
 *        * isNegativeZero(-0.0) -> String.valueOf(x) -> "-0.0"
 *        * Non-integer doubles (e.g. 1.5, NaN, Infinity) -> String.valueOf(x)
 *    - isNegativeZero(double): x == 0.0 with sign bit negative vs positive, non-zero values
 *    - isWordChar(char): '_', '$', letter/digit, other characters
 *    - breakAfterBlockFor(Node, boolean): returns statementContext
 *    - continueProcessing(): returns true
 *    - shouldPreserveExtraBlocks(): returns false
 *    - startSourceMapping / endSourceMapping / endFile / startNewLine / notePreferredLineBreak / endCaseBody
 */
public class CodeConsumerGeminiTest {

  /**
   * Concrete subclass of CodeConsumer tracking buffer output and lifecycle hooks.
   */
  private static class ConcreteCodeConsumer extends CodeConsumer {
    final StringBuilder buffer = new StringBuilder();
    int cutLineCount = 0;
    int endLineCount = 0;
    int startNewLineCount = 0;
    int preferredBreakCount = 0;

    @Override
    char getLastChar() {
      return buffer.length() == 0 ? '\0' : buffer.charAt(buffer.length() - 1);
    }

    @Override
    void append(String str) {
      buffer.append(str);
    }

    @Override
    void maybeCutLine() {
      cutLineCount++;
    }

    @Override
    void endLine() {
      endLineCount++;
    }

    @Override
    void startNewLine() {
      startNewLineCount++;
    }

    @Override
    void notePreferredLineBreak() {
      preferredBreakCount++;
    }

    String getOutput() {
      return buffer.toString();
    }
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBeginBlockWithoutPrecedingStatement() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.statementNeedsEnded = false;
    consumer.beginBlock();

    assertEquals("{", consumer.getOutput());
    assertEquals(1, consumer.endLineCount);
    assertEquals(0, consumer.cutLineCount);
    assertFalse(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testBeginBlockWithStatementNeedsEnded() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.statementNeedsEnded = true;
    consumer.beginBlock();

    assertEquals(";{", consumer.getOutput());
    assertEquals(1, consumer.cutLineCount);
    assertEquals(1, consumer.endLineCount);
    assertFalse(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testEndBlockParameterlessDelegates() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.statementNeedsEnded = true;
    consumer.endBlock();

    assertEquals("}", consumer.getOutput());
    assertEquals(0, consumer.endLineCount);
    assertFalse(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testEndBlockWithShouldEndLineTrue() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.statementNeedsEnded = true;
    consumer.endBlock(true);

    assertEquals("}", consumer.getOutput());
    assertEquals(1, consumer.endLineCount);
    assertFalse(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testEndStatementNeedSemicolonTrue() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.statementNeedsEnded = true;
    consumer.endStatement(true);

    assertEquals(";", consumer.getOutput());
    assertEquals(1, consumer.cutLineCount);
    assertFalse(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testEndStatementNeedSemicolonFalseWithStatementStarted() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.statementStarted = true;
    consumer.statementNeedsEnded = false;
    consumer.endStatement(false);

    assertEquals("", consumer.getOutput());
    assertTrue(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testEndStatementNeedSemicolonFalseWithoutStatementStarted() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.statementStarted = false;
    consumer.statementNeedsEnded = false;
    consumer.endStatement();

    assertEquals("", consumer.getOutput());
    assertFalse(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testMaybeEndStatementWhenNeeded() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.statementNeedsEnded = true;
    consumer.statementStarted = false;

    consumer.maybeEndStatement();

    assertEquals(";", consumer.getOutput());
    assertEquals(1, consumer.cutLineCount);
    assertEquals(1, consumer.endLineCount);
    assertFalse(consumer.statementNeedsEnded);
    assertTrue(consumer.statementStarted);
  }

  @Test(timeout = 4000)
  public void testMaybeEndStatementWhenNotNeeded() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.statementNeedsEnded = false;
    consumer.statementStarted = false;

    consumer.maybeEndStatement();

    assertEquals("", consumer.getOutput());
    assertEquals(0, consumer.cutLineCount);
    assertEquals(0, consumer.endLineCount);
    assertFalse(consumer.statementNeedsEnded);
    assertTrue(consumer.statementStarted);
  }

  @Test(timeout = 4000)
  public void testEndFunctionParameterless() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    assertFalse(consumer.sawFunction);

    consumer.endFunction();

    assertTrue(consumer.sawFunction);
    assertEquals(0, consumer.endLineCount);
  }

  @Test(timeout = 4000)
  public void testEndFunctionWithStatementContext() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.endFunction(true);

    assertTrue(consumer.sawFunction);
    assertEquals(1, consumer.endLineCount);
  }

  @Test(timeout = 4000)
  public void testCaseBodyHooks() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.beginCaseBody();
    consumer.endCaseBody();

    assertEquals(":", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testListSeparator() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.listSeparator();

    assertEquals(",", consumer.getOutput());
    assertEquals(1, consumer.cutLineCount);
  }

  @Test(timeout = 4000)
  public void testAddIdentifierDelegation() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.addIdentifier("myVar");
    assertEquals("myVar", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testDefaultControlAndQueryMethods() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();

    assertTrue(consumer.continueProcessing());
    assertFalse(consumer.shouldPreserveExtraBlocks());
    assertTrue(consumer.breakAfterBlockFor(null, true));
    assertFalse(consumer.breakAfterBlockFor(null, false));

    consumer.startNewLine();
    assertEquals(1, consumer.startNewLineCount);

    consumer.maybeLineBreak();
    assertEquals(1, consumer.cutLineCount);

    consumer.notePreferredLineBreak();
    assertEquals(1, consumer.preferredBreakCount);

    consumer.startSourceMapping(null);
    consumer.endSourceMapping(null);
    consumer.endFile();
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Word Character Handling
  // =========================================================================

  @Test(timeout = 4000)
  public void testAddEmptyStringIsNoOp() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.add("");
    assertEquals("", consumer.getOutput());
    assertTrue(consumer.statementStarted);
  }

  @Test(timeout = 4000)
  public void testAddWordAfterWordInjectsSpace() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.add("return");
    consumer.add("foo");

    assertEquals("return foo", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddBackslashAfterWordInjectsSpace() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.add("var");
    consumer.add("\\u0061");

    assertEquals("var \\u0061", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddNonWordCharsDoNotInjectSpace() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.add("(");
    consumer.add("foo");
    consumer.add(")");

    assertEquals("(foo)", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testIsWordCharClassification() {
    assertTrue(CodeConsumer.isWordChar('_'));
    assertTrue(CodeConsumer.isWordChar('$'));
    assertTrue(CodeConsumer.isWordChar('a'));
    assertTrue(CodeConsumer.isWordChar('Z'));
    assertTrue(CodeConsumer.isWordChar('0'));
    assertTrue(CodeConsumer.isWordChar('9'));

    assertFalse(CodeConsumer.isWordChar('-'));
    assertFalse(CodeConsumer.isWordChar('+'));
    assertFalse(CodeConsumer.isWordChar('/'));
    assertFalse(CodeConsumer.isWordChar('\\'));
    assertFalse(CodeConsumer.isWordChar(' '));
    assertFalse(CodeConsumer.isWordChar('\0'));
    assertFalse(CodeConsumer.isWordChar('('));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Issue 620)
  // =========================================================================

  /**
   * Targets Closure Issue 620:
   * When a division operator '/' is followed by a regular expression literal starting with '/',
   * they MUST be separated by a space to prevent forming a line comment '//'.
   */
  @Test(timeout = 4000)
  public void testIssue620ForwardSlashAfterDiv() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.append("/");
    consumer.add("/ /");

    // Correct behavior: "/ / /"
    // Defective behavior: "// /" (comment formed due to missing space injection)
    assertEquals("/ / /", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testIssue620RegexpDivRegexpSpacing() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.add("/ /");
    consumer.addOp("/", true);
    consumer.add("/ /");

    // Under defective behavior, the output was alert(/ /// /) instead of alert(/ // / /)
    assertEquals("/ // / /", consumer.getOutput());
  }

  // =========================================================================
  // Partition D: Operator Handling Branches (addOp, appendOp)
  // =========================================================================

  @Test(timeout = 4000)
  public void testAddOpPlusAfterPlusInjectsSpace() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.add("x");
    consumer.addOp("+", false);
    consumer.addOp("+", false);

    assertEquals("x+ +", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddOpMinusAfterMinusInjectsSpace() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.add("x");
    consumer.addOp("-", false);
    consumer.addOp("-", false);

    assertEquals("x- -", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddOpPlusAfterMinusDoesNotInjectSpace() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.add("x");
    consumer.addOp("-", false);
    consumer.addOp("+", false);

    assertEquals("x-+", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddOpLetterAfterWordCharInjectsSpace() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.add("x");
    consumer.addOp("instanceof", true);

    assertEquals("x instanceof", consumer.getOutput());
    assertEquals(1, consumer.cutLineCount);
  }

  @Test(timeout = 4000)
  public void testAddOpLetterAfterNonWordCharDoesNotInjectSpace() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.add(")");
    consumer.addOp("instanceof", false);

    assertEquals(")instanceof", consumer.getOutput());
    assertEquals(0, consumer.cutLineCount);
  }

  @Test(timeout = 4000)
  public void testAddOpGreaterThanAfterMinusInjectsSpaceToPreventHtmlComment() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.add("-");
    consumer.addOp(">", false);

    assertEquals("- >", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddOpGreaterThanAfterPlusDoesNotInjectSpace() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.add("+");
    consumer.addOp(">", false);

    assertEquals("+>", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAppendOpDirectCall() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.appendOp("*=", true);

    assertEquals("*=", consumer.getOutput());
  }

  // =========================================================================
  // Partition E: Number Formatting & Numeric Boundary Value Analysis
  // =========================================================================

  @Test(timeout = 4000)
  public void testAddNegativeZero() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.addNumber(-0.0);

    assertEquals("-0.0", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddPositiveZero() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.addNumber(0.0);

    assertEquals("0", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testIsNegativeZeroHelper() {
    assertTrue(CodeConsumer.isNegativeZero(-0.0));
    assertFalse(CodeConsumer.isNegativeZero(0.0));
    assertFalse(CodeConsumer.isNegativeZero(-1.0));
    assertFalse(CodeConsumer.isNegativeZero(1.0));
    assertFalse(CodeConsumer.isNegativeZero(Double.NaN));
    assertFalse(CodeConsumer.isNegativeZero(Double.NEGATIVE_INFINITY));
  }

  @Test(timeout = 4000)
  public void testAddNegativeNumberAfterMinusInjectsSpace() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.append("-");
    consumer.addNumber(-4.0);

    // Prevents misparsing x- -4 as x--4
    assertEquals("- -4", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddPositiveNumberAfterMinusDoesNotInjectSpace() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.append("-");
    consumer.addNumber(4.0);

    assertEquals("-4", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddNumberScientificNotationLargeExactPowers() {
    ConcreteCodeConsumer consumer1 = new ConcreteCodeConsumer();
    consumer1.addNumber(1000.0);
    assertEquals("1E3", consumer1.getOutput());

    ConcreteCodeConsumer consumer2 = new ConcreteCodeConsumer();
    consumer2.addNumber(1000000.0);
    assertEquals("1E6", consumer2.getOutput());

    ConcreteCodeConsumer consumer3 = new ConcreteCodeConsumer();
    consumer3.addNumber(-20000.0);
    assertEquals("-2E4", consumer3.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddNumberScientificNotationExponentBoundary() {
    // Math.abs(x) >= 100, but exp <= 2 should NOT use scientific notation
    ConcreteCodeConsumer consumer1 = new ConcreteCodeConsumer();
    consumer1.addNumber(100.0);
    assertEquals("100", consumer1.getOutput());

    ConcreteCodeConsumer consumer2 = new ConcreteCodeConsumer();
    consumer2.addNumber(-100.0);
    assertEquals("-100", consumer2.getOutput());

    // Non-power of 10 within >= 100
    ConcreteCodeConsumer consumer3 = new ConcreteCodeConsumer();
    consumer3.addNumber(105.0);
    assertEquals("105", consumer3.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddNumberBelowOneHundred() {
    ConcreteCodeConsumer consumer = new ConcreteCodeConsumer();
    consumer.addNumber(99.0);
    assertEquals("99", consumer.getOutput());

    ConcreteCodeConsumer consumerNeg = new ConcreteCodeConsumer();
    consumerNeg.addNumber(-99.0);
    assertEquals("-99", consumerNeg.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddNumberFloatingPointAndSpecialDoubles() {
    ConcreteCodeConsumer consumerFloat = new ConcreteCodeConsumer();
    consumerFloat.addNumber(1.25);
    assertEquals("1.25", consumerFloat.getOutput());

    ConcreteCodeConsumer consumerNaN = new ConcreteCodeConsumer();
    consumerNaN.addNumber(Double.NaN);
    assertEquals("NaN", consumerNaN.getOutput());

    ConcreteCodeConsumer consumerInf = new ConcreteCodeConsumer();
    consumerInf.addNumber(Double.POSITIVE_INFINITY);
    assertEquals("Infinity", consumerInf.getOutput());

    ConcreteCodeConsumer consumerNegInf = new ConcreteCodeConsumer();
    consumerNegInf.addNumber(Double.NEGATIVE_INFINITY);
    assertEquals("-Infinity", consumerNegInf.getOutput());
  }
}