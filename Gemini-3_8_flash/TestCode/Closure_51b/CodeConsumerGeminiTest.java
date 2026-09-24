package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;
import com.google.javascript.rhino.Node;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.google.javascript.jscomp.CodeConsumer
 *
 * 1. Partition C: Defect-Targeted Branch Zone (Defects4J Issue 582):
 *    - addNumber(-0.0): (long) -0.0 == -0.0 evaluates to true in Java, but formatting
 *      negative zero through the integer branch emits "0" instead of "-0.0", losing
 *      critical JavaScript negative zero semantics.
 *
 * 2. Partition A: Core Functional Logic & State Transitions:
 *    - add(String): Word character boundary spacing ('_' | '$' | Alphanumeric) and escape '\\'
 *    - addOp(String, boolean):
 *        * Collisions on identical unary/binary operators ('+' after '+', '-' after '-')
 *        * HTML close comment guard: 'prev == '-' && first == '>''
 *        * Word operator spacing: 'isLetter(first) && isWordChar(prev)' (e.g. instanceof)
 *        * Line cut triggers when binOp is true
 *    - Statement lifecycle: statementStarted, statementNeedsEnded, maybeEndStatement, endStatement
 *    - Block lifecycle: beginBlock, endBlock(boolean), semicolon insertion on unended statements
 *    - Function lifecycle: endFunction(boolean), sawFunction state tracking
 *
 * 3. Partition B: Boundary Value Analysis (BVA) & Extremes:
 *    - addNumber(double):
 *        * Negative number preceded by '-' adds space to avoid "--" misparse
 *        * Abs(x) >= 100 with trailing zero reduction: exp > 2 emits scientific notation (e.g. 1E3)
 *        * Abs(x) >= 100 without enough trailing zeroes: exp <= 2 emits standard long representation
 *        * Fractional double values ((long) x != x)
 *        * Empty string inputs to add(String)
 *
 * 4. Partition D & E: Defensive Guards & Base Implementation Coverage:
 *    - Default no-op hooks: startNewLine, maybeCutLine, endLine, notePreferredLineBreak,
 *      endCaseBody, endFile, startSourceMapping, endSourceMapping
 *    - Query defaults: continueProcessing (true), shouldPreserveExtraBlocks (false),
 *      breakAfterBlockFor (echoes statementContext)
 */
public class CodeConsumerGeminiTest {

  /**
   * Complete concrete implementation allowing full inspection of line breaks,
   * cuts, and buffer modifications.
   */
  private static class TestCodeConsumer extends CodeConsumer {
    private final StringBuilder buffer = new StringBuilder();
    private int linesEnded = 0;
    private int linesCut = 0;
    private int newLinesStarted = 0;
    private int preferredBreaksNoted = 0;

    @Override
    char getLastChar() {
      return buffer.length() == 0 ? '\0' : buffer.charAt(buffer.length() - 1);
    }

    @Override
    void append(String str) {
      buffer.append(str);
    }

    @Override
    void startNewLine() {
      newLinesStarted++;
      buffer.append('\n');
    }

    @Override
    void maybeCutLine() {
      linesCut++;
    }

    @Override
    void endLine() {
      linesEnded++;
    }

    @Override
    void notePreferredLineBreak() {
      preferredBreaksNoted++;
    }

    String getOutput() {
      return buffer.toString();
    }
  }

  /**
   * Minimal consumer relying entirely on base class default implementations
   * for line hooks, mappings, and state checks.
   */
  private static class MinimalCodeConsumer extends CodeConsumer {
    private final StringBuilder buffer = new StringBuilder();

    @Override
    char getLastChar() {
      return buffer.length() == 0 ? '\0' : buffer.charAt(buffer.length() - 1);
    }

    @Override
    void append(String str) {
      buffer.append(str);
    }

    String getOutput() {
      return buffer.toString();
    }
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 582)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIssue582NegativeZeroHandling() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.addNumber(-0.0);
    // On the defective version, (long) -0.0 == -0.0 evaluates to true, emitting "0".
    // The correct behavior is to retain the negative sign: "-0.0".
    assertEquals("-0.0", consumer.getOutput());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testAddIdentifierAndWordSpacing() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.addIdentifier("var");
    consumer.addIdentifier("foo");
    assertEquals("var foo", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddEscapeCharacterSeparation() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.add("foo");
    consumer.add("\\u0041");
    assertEquals("foo \\u0041", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddNonWordCharacterDoesNotSeparate() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.add("foo");
    consumer.add(";");
    assertEquals("foo;", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddOpPlusCollision() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.addOp("+", false);
    consumer.addOp("+", false);
    assertEquals("+ +", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddOpMinusCollision() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.addOp("-", false);
    consumer.addOp("-", false);
    assertEquals("- -", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddOpHtmlCommentEndPrevention() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.addOp("-", false);
    consumer.addOp(">", false);
    assertEquals("- >", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddOpWordOperatorSpacing() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.add("x");
    consumer.addOp("instanceof", true);
    assertEquals("x instanceof", consumer.getOutput());
    assertEquals(1, consumer.linesCut);
  }

  @Test(timeout = 4000)
  public void testAddOpBinaryOpCutsLine() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.addOp("+", true);
    assertEquals(1, consumer.linesCut);

    consumer.addOp("*", false);
    assertEquals(1, consumer.linesCut);
  }

  @Test(timeout = 4000)
  public void testBeginBlockWithStatementNeedsEnded() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementNeedsEnded = true;
    consumer.beginBlock();

    assertEquals(";{", consumer.getOutput());
    assertFalse(consumer.statementNeedsEnded);
    assertEquals(1, consumer.linesEnded);
    assertEquals(1, consumer.linesCut);
  }

  @Test(timeout = 4000)
  public void testBeginBlockWithoutStatementNeedsEnded() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementNeedsEnded = false;
    consumer.beginBlock();

    assertEquals("{", consumer.getOutput());
    assertFalse(consumer.statementNeedsEnded);
    assertEquals(1, consumer.linesEnded);
  }

  @Test(timeout = 4000)
  public void testEndBlockVariants() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementNeedsEnded = true;
    consumer.endBlock();
    assertEquals("}", consumer.getOutput());
    assertFalse(consumer.statementNeedsEnded);
    assertEquals(0, consumer.linesEnded);

    consumer.statementNeedsEnded = true;
    consumer.endBlock(true);
    assertEquals("}}", consumer.getOutput());
    assertFalse(consumer.statementNeedsEnded);
    assertEquals(1, consumer.linesEnded);

    consumer.endBlock(false);
    assertEquals("}}}", consumer.getOutput());
    assertEquals(1, consumer.linesEnded);
  }

  @Test(timeout = 4000)
  public void testEndStatementWithNeedSemiColon() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementNeedsEnded = true;
    consumer.endStatement(true);

    assertEquals(";", consumer.getOutput());
    assertFalse(consumer.statementNeedsEnded);
    assertEquals(1, consumer.linesCut);
  }

  @Test(timeout = 4000)
  public void testEndStatementWithoutNeedSemiColon() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementStarted = true;
    consumer.endStatement(false);
    assertTrue(consumer.statementNeedsEnded);

    consumer.statementStarted = false;
    consumer.statementNeedsEnded = false;
    consumer.endStatement();
    assertFalse(consumer.statementNeedsEnded);
  }

  @Test(timeout = 4000)
  public void testMaybeEndStatementFlushesSemicolon() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementNeedsEnded = true;
    consumer.maybeEndStatement();

    assertEquals(";", consumer.getOutput());
    assertFalse(consumer.statementNeedsEnded);
    assertTrue(consumer.statementStarted);
    assertEquals(1, consumer.linesCut);
    assertEquals(1, consumer.linesEnded);
  }

  @Test(timeout = 4000)
  public void testMaybeEndStatementNoopWhenNotNeeded() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.statementNeedsEnded = false;
    consumer.maybeEndStatement();

    assertEquals("", consumer.getOutput());
    assertTrue(consumer.statementStarted);
    assertEquals(0, consumer.linesEnded);
  }

  @Test(timeout = 4000)
  public void testEndFunctionStatementContext() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    assertFalse(consumer.sawFunction);

    consumer.endFunction(false);
    assertTrue(consumer.sawFunction);
    assertEquals(0, consumer.linesEnded);

    consumer.endFunction(true);
    assertTrue(consumer.sawFunction);
    assertEquals(1, consumer.linesEnded);

    consumer.sawFunction = false;
    consumer.endFunction();
    assertTrue(consumer.sawFunction);
    assertEquals(1, consumer.linesEnded);
  }

  @Test(timeout = 4000)
  public void testCaseBodyAndListSeparator() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.beginCaseBody();
    consumer.endCaseBody();
    assertEquals(":", consumer.getOutput());

    consumer.listSeparator();
    assertEquals(":,", consumer.getOutput());
    assertEquals(1, consumer.linesCut);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testAddEmptyString() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.add("");
    assertEquals("", consumer.getOutput());
    assertTrue(consumer.statementStarted);
  }

  @Test(timeout = 4000)
  public void testAddNumberScientificNotationFormatting() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.addNumber(1000.0);
    assertEquals("1E3", consumer.getOutput());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    consumer2.addNumber(100000.0);
    assertEquals("1E5", consumer2.getOutput());

    TestCodeConsumer consumer3 = new TestCodeConsumer();
    consumer3.addNumber(-1000.0);
    assertEquals("-1E3", consumer3.getOutput());

    TestCodeConsumer consumer4 = new TestCodeConsumer();
    consumer4.addNumber(12000.0);
    assertEquals("12E3", consumer4.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddNumberSmallIntegerFormatting() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.addNumber(100.0);
    assertEquals("100", consumer.getOutput());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    consumer2.addNumber(0.0);
    assertEquals("0", consumer2.getOutput());

    TestCodeConsumer consumer3 = new TestCodeConsumer();
    consumer3.addNumber(99.0);
    assertEquals("99", consumer3.getOutput());

    TestCodeConsumer consumer4 = new TestCodeConsumer();
    consumer4.addNumber(105.0);
    assertEquals("105", consumer4.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddNumberDoubleWithFraction() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.addNumber(1.25);
    assertEquals("1.25", consumer.getOutput());

    TestCodeConsumer consumer2 = new TestCodeConsumer();
    consumer2.addNumber(-0.5);
    assertEquals("-0.5", consumer2.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddNumberNegativeCollisionWithMinus() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.append("-");
    consumer.addNumber(-5.0);
    assertEquals("- -5", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddNumberNegativeNoCollisionWithPlus() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.append("+");
    consumer.addNumber(-5.0);
    assertEquals("+-5", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testIsWordCharBoundaries() {
    assertTrue(CodeConsumer.isWordChar('_'));
    assertTrue(CodeConsumer.isWordChar('$'));
    assertTrue(CodeConsumer.isWordChar('a'));
    assertTrue(CodeConsumer.isWordChar('z'));
    assertTrue(CodeConsumer.isWordChar('A'));
    assertTrue(CodeConsumer.isWordChar('Z'));
    assertTrue(CodeConsumer.isWordChar('0'));
    assertTrue(CodeConsumer.isWordChar('9'));

    assertFalse(CodeConsumer.isWordChar('-'));
    assertFalse(CodeConsumer.isWordChar('+'));
    assertFalse(CodeConsumer.isWordChar(' '));
    assertFalse(CodeConsumer.isWordChar('\0'));
    assertFalse(CodeConsumer.isWordChar(';'));
    assertFalse(CodeConsumer.isWordChar('\\'));
    assertFalse(CodeConsumer.isWordChar(':'));
  }

  // =========================================================================
  // Partition D & E: Base Class Defaults & Defensive Queries
  // =========================================================================

  @Test(timeout = 4000)
  public void testMinimalConsumerBaseHooks() {
    MinimalCodeConsumer consumer = new MinimalCodeConsumer();

    assertTrue(consumer.continueProcessing());
    assertFalse(consumer.shouldPreserveExtraBlocks());

    // Break after block echoes the statementContext argument
    assertTrue(consumer.breakAfterBlockFor(null, true));
    assertFalse(consumer.breakAfterBlockFor(null, false));

    // Base default no-op methods must execute cleanly without exception
    consumer.startSourceMapping(null);
    consumer.endSourceMapping(null);
    consumer.startNewLine();
    consumer.maybeCutLine();
    consumer.maybeLineBreak();
    consumer.endLine();
    consumer.notePreferredLineBreak();
    consumer.endFile();
    consumer.endCaseBody();

    assertEquals('\0', consumer.getLastChar());
    assertEquals("", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testPreferredLineBreakHook() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.notePreferredLineBreak();
    assertEquals(1, consumer.preferredBreaksNoted);
  }

  @Test(timeout = 4000)
  public void testStartNewLineHook() {
    TestCodeConsumer consumer = new TestCodeConsumer();
    consumer.startNewLine();
    assertEquals(1, consumer.newLinesStarted);
    assertEquals("\n", consumer.getOutput());
  }
}