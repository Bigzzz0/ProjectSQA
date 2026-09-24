package com.google.javascript.jscomp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive test suite for {@link CodeConsumer} targeting maximum line/branch coverage
 * and the known defect regarding negative zero (issue #582).
 *
 * <pre>/* [Branch & Defect Analysis Matrix]
 *
 * =========== Branch Analysis ===========
 * 1. CodeConsumer.add(String newcode) :
 *    - if (newcode.length() == 0) -> early return
 *    - if (isWordChar(c) || c == '\\') && isWordChar(getLastChar()) -> insert space
 *    - calls append(newcode)
 *
 * 2. CodeConsumer.addOp(String op, boolean binOp) :
 *    - if ((first == '+' || first == '-') && prev == first) -> append(" ") (avoid misparse)
 *    - else if (Character.isLetter(first) && isWordChar(prev)) -> append(" ")
 *    - else if (prev == '-' && first == '>') -> append(" ")
 *    - calls appendOp(op, binOp)
 *    - if (binOp) -> maybeCutLine()
 *
 * 3. CodeConsumer.addNumber(double x) :
 *    - if (x < 0 && prev == '-') -> add(" ")
 *    - if ((long)x == x) :
 *        - while (mantissa/10 * Math.pow(10, exp+1) == value) -> reduce mantissa/exponent
 *        - if (exp > 2) -> add(Long.toString(mantissa) + "E" + Integer.toString(exp))
 *        - else -> add(Long.toString(value))
 *    - else -> add(String.valueOf(x))
 *
 * 4. CodeConsumer.beginBlock():
 *    - if (statementNeedsEnded) -> append(";"), maybeLineBreak()
 *    - appendBlockStart(), endLine(), statementNeedsEnded = false
 *
 * 5. CodeConsumer.endBlock(), endBlock(boolean shouldEndLine):
 *    - appendBlockEnd(), if shouldEndLine -> endLine()
 *    - statementNeedsEnded = false
 *
 * 6. CodeConsumer.endStatement(), endStatement(boolean needSemiColon):
 *    - if needSemiColon -> append(";"), maybeLineBreak(), statementNeedsEnded = false
 *    - else if statementStarted -> statementNeedsEnded = true
 *
 * 7. CodeConsumer.maybeEndStatement():
 *    - if statementNeedsEnded -> append(";"), maybeLineBreak(), endLine(), statementNeedsEnded = false
 *    - statementStarted = true
 *
 * 8. CodeConsumer.endFunction(), endFunction(boolean statementContext):
 *    - sawFunction = true
 *    - if statementContext -> endLine()
 *
 * 9. CodeConsumer.isWordChar(): ch == '_' || ch == '$' || Character.isLetterOrDigit(ch)
 *
 * 10. Other trivial methods: startSourceMapping, endSourceMapping, continueProcessing,
 *     appendBlockStart, appendBlockEnd, startNewLine, maybeLineBreak, maybeCutLine,
 *     endLine, notePreferredLineBreak, listSeparator, beginCaseBody, endCaseBody,
 *     addIdentifier, shouldPreserveExtraBlocks, breakAfterBlockFor, endFile.
 *
 * =========== Defect #582 (Negative Zero) ===========
 * - addNumber(-0.0) uses (long)x == x branch because (long)-0.0 == 0 && -0.0 == 0.0.
 *   It then adds Long.toString(0) producing "0" instead of "-0.0".
 * - Expected correct behavior: negative zero should be printed as "-0.0".
 * - Tests: testAddNumberNegativeZero, testAddNumberNegativeZeroAfterMinus.
 */
public class CodeConsumerDeepseekTest {
    private FakeCodeConsumer consumer;

    @Before
    public void setUp() {
        consumer = new FakeCodeConsumer();
    }

    // ========== Helper inner class ==========
    private static class FakeCodeConsumer extends CodeConsumer {
        private StringBuilder sb = new StringBuilder();
        private char lastChar = 0;  // initial dummy

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

        // Expose for assertions
        String getOutput() {
            return sb.toString();
        }

        void clear() {
            sb.setLength(0);
            lastChar = 0;
        }
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testBeginBlockTriggersSemicolon() {
        consumer.statementNeedsEnded = true;
        consumer.beginBlock();
        assertEquals(";{", consumer.getOutput());
        assertFalse(consumer.statementNeedsEnded);
    }

    @Test(timeout = 4000)
    public void testBeginBlockNoSemicolon() {
        consumer.statementNeedsEnded = false;
        consumer.beginBlock();
        assertEquals("{", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testEndBlockDefault() {
        consumer.append("a");
        consumer.endBlock();
        assertEquals("a}", consumer.getOutput());
        assertFalse(consumer.statementNeedsEnded);
    }

    @Test(timeout = 4000)
    public void testEndBlockWithEndLine() {
        consumer.append("a");
        consumer.endBlock(true);
        assertEquals("a}", consumer.getOutput()); // endLine() is no-op, but shouldEndLine flag set
        assertFalse(consumer.statementNeedsEnded);
    }

    @Test(timeout = 4000)
    public void testEndStatementWithSemicolon() {
        consumer.endStatement(true);
        assertEquals(";", consumer.getOutput());
        assertFalse(consumer.statementNeedsEnded);
    }

    @Test(timeout = 4000)
    public void testEndStatementWithoutSemicolonStatementStarted() {
        consumer.statementStarted = true;
        consumer.endStatement(false);
        assertTrue(consumer.statementNeedsEnded);
        assertEquals("", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testEndStatementWithoutSemicolonNotStarted() {
        consumer.statementStarted = false;
        consumer.endStatement(false);
        assertFalse(consumer.statementNeedsEnded);
    }

    @Test(timeout = 4000)
    public void testMaybeEndStatementAddsSemicolon() {
        consumer.statementNeedsEnded = true;
        consumer.maybeEndStatement();
        assertEquals(";", consumer.getOutput());
        assertFalse(consumer.statementNeedsEnded);
        assertTrue(consumer.statementStarted);
    }

    @Test(timeout = 4000)
    public void testMaybeEndStatementNoSemicolonNeeded() {
        consumer.statementNeedsEnded = false;
        consumer.maybeEndStatement();
        assertEquals("", consumer.getOutput());
        assertTrue(consumer.statementStarted);
    }

    @Test(timeout = 4000)
    public void testEndFunctionDefault() {
        consumer.endFunction();
        assertTrue(consumer.sawFunction);
    }

    @Test(timeout = 4000)
    public void testEndFunctionStatementContext() {
        consumer.endFunction(true);
        assertTrue(consumer.sawFunction);
        // endLine() is no-op, but we can call to ensure no exception
    }

    @Test(timeout = 4000)
    public void testStartSourceMappingNoOp() {
        // Just ensure no exception
        consumer.startSourceMapping(null);
    }

    @Test(timeout = 4000)
    public void testEndSourceMappingNoOp() {
        consumer.endSourceMapping(null);
    }

    @Test(timeout = 4000)
    public void testContinueProcessingDefault() {
        assertTrue(consumer.continueProcessing());
    }

    @Test(timeout = 4000)
    public void testShouldPreserveExtraBlocksDefault() {
        assertFalse(consumer.shouldPreserveExtraBlocks());
    }

    @Test(timeout = 4000)
    public void testBreakAfterBlockForStatementContextTrueReturnsTrue() {
        assertTrue(consumer.breakAfterBlockFor(null, true));
    }

    @Test(timeout = 4000)
    public void testBreakAfterBlockForStatementContextFalseReturnsFalse() {
        assertFalse(consumer.breakAfterBlockFor(null, false));
    }

    @Test(timeout = 4000)
    public void testEndFile() {
        consumer.endFile(); // no-op
    }

    @Test(timeout = 4000)
    public void testStartNewLine() {
        consumer.startNewLine(); // no-op
    }

    @Test(timeout = 4000)
    public void testMaybeLineBreak() {
        consumer.maybeLineBreak(); // no-op
    }

    @Test(timeout = 4000)
    public void testMaybeCutLine() {
        consumer.maybeCutLine(); // no-op
    }

    @Test(timeout = 4000)
    public void testEndLine() {
        consumer.endLine(); // no-op
    }

    @Test(timeout = 4000)
    public void testNotePreferredLineBreak() {
        consumer.notePreferredLineBreak(); // no-op
    }

    @Test(timeout = 4000)
    public void testListSeparatorAppendsComma() {
        consumer.listSeparator();
        assertEquals(",", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testBeginCaseBody() {
        consumer.beginCaseBody();
        assertEquals(":", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testEndCaseBody() {
        consumer.endCaseBody(); // no-op
    }

    @Test(timeout = 4000)
    public void testAddIdentifier() {
        consumer.add("x");
        consumer.clear();
        consumer.addIdentifier("foo");
        assertEquals("foo", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAppendBlockStart() {
        consumer.appendBlockStart();
        assertEquals("{", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAppendBlockEnd() {
        consumer.appendBlockEnd();
        assertEquals("}", consumer.getOutput());
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testAddEmptyString() {
        consumer.add("");
        assertEquals("", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddSpaceBetweenWordChars() {
        // Simulate previous char is word char
        consumer.append("a");
        consumer.lastChar = 'a'; // ensure lastChar is 'a'
        consumer.add("b");
        assertEquals("a b", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNoSpaceWhenPrevNotWordChar() {
        consumer.append("{");
        consumer.lastChar = '{';
        consumer.add("b");
        assertEquals("{b", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddWithEscapeChar() {
        consumer.append("a");
        consumer.lastChar = 'a';
        consumer.add("\\n");
        assertEquals("a \\n", consumer.getOutput()); // '\\' is word char? isWordChar('\\') returns false because backslash is not _, $, letter, or digit. Actually isWordChar checks only _, $, letter/digit, so backslash is false. But the condition (isWordChar(c) || c == '\\') includes '\\', so space is added. Correct.
    }

    @Test(timeout = 4000)
    public void testAddNumberZero() {
        consumer.addNumber(0.0);
        assertEquals("0", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberPositiveSmall() {
        consumer.addNumber(5.0);
        assertEquals("5", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberNegativeSmall() {
        consumer.addNumber(-5.0);
        assertEquals("-5", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberFraction() {
        consumer.addNumber(0.5);
        assertEquals("0.5", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberLargeExponent() {
        consumer.addNumber(1000.0);
        assertEquals("1E3", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberLargeNoExponent() {
        consumer.addNumber(99.0);
        assertEquals("99", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberBoundaryExponent() {
        consumer.addNumber(100.0);
        assertEquals("1E2", consumer.getOutput()); // 100: mantissa=1, exp=2, exp>2? No, exp=2 not >2, so else: add(Long.toString(100))? Wait, 100: value=100, mantissa=100, exp=0; while: mantissa/10=10, pow(10,1)=10, product=100 equal value => mantissa=10, exp=1; while: mantissa/10=1, pow(10,2)=100, product=100 => mantissa=1, exp=2; while: mantissa/10=0, pow(10,3)=1000, product=0 not equal 100 -> stop. exp=2, not >2, so add(Long.toString(100)) => "100"? Actually the algorithm after loop sets mantissa=1, exp=2, but condition (exp>2) is false, so it goes to else add(Long.toString(value)) which is 100. So output is "100". But the test might expect "1E2"? Need to check source: if (exp > 2) { add(Long.toString(mantissa) + "E" + Integer.toString(exp)); } else { add(Long.toString(value)); }. So for 100, exp=2 not >2, so it adds "100". So correct expected is "100". Let me confirm with example: The comment says "if (exp > 2) ... else ...". So 1000 gives exp=3 >2 => "1E3". 100 gives exp=2 => "100". I'll adjust test.
        // Actually 100 => value=100, loop reduces to mantissa=1, exp=2, but exp=2 not >2, so else branch => "100".
        assertEquals("100", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberVeryLargeExponent() {
        consumer.addNumber(1000000.0);
        // value=1000000, mantissa=1, exp=6, exp>2 => "1E6"
        assertEquals("1E6", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberInfinity() {
        consumer.addNumber(Double.POSITIVE_INFINITY);
        assertEquals("Infinity", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberNaN() {
        consumer.addNumber(Double.NaN);
        assertEquals("NaN", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberNegativeInfinity() {
        consumer.addNumber(Double.NEGATIVE_INFINITY);
        assertEquals("-Infinity", consumer.getOutput());
    }

    // ========== Partition C: Defect-Targeted Branch Zone (Issue #582) ==========

    @Test(timeout = 4000)
    public void testAddNumberNegativeZero() {
        // The known defect: -0.0 is printed as "0". Assert expected correct output "-0.0".
        consumer.addNumber(-0.0);
        assertEquals("Expected -0.0 for negative zero", "-0.0", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberNegativeZeroAfterMinus() {
        // Simulate scenario where previous char is '-' and we add -0.0 -> should avoid misparse
        consumer.append("-");
        consumer.addNumber(-0.0);
        // Expected: "- -0.0"? Actually addNumber first checks if (x < 0 && prev == '-') add(" ");
        // Since x=-0.0 <0 and prev='-', it adds a space before the number, then adds "-0.0".
        // So output should be "- -0.0". But the number is -0.0, so after space, it adds -0.0.
        assertEquals("- -0.0", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberPositiveZero() {
        consumer.addNumber(0.0);
        assertEquals("0", consumer.getOutput());
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testAddOpDoublePlusMisparse() {
        consumer.append("+");
        consumer.lastChar = '+';
        consumer.addOp("+", false);
        // first==', prev=='+', so append(" ") before operator
        assertEquals("+ +", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddOpDoubleMinusMisparse() {
        consumer.append("-");
        consumer.addOp("-", false);
        assertEquals("- -", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddOpInstanceofSpace() {
        consumer.append("a");
        consumer.lastChar = 'a';
        consumer.addOp("instanceof", false);
        // first='i' is letter, prev='a' is word char, so space inserted
        assertEquals("a instanceof", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddOpArrowMisparse() {
        consumer.append("-");
        consumer.lastChar = '-';
        consumer.addOp(">", false);
        // prev='-', first='>', so append(" ") then ">"
        assertEquals("- >", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddOpBinaryCutsLine() {
        consumer.addOp("+", true);
        assertTrue(consumer.getOutput().contains("+"));
        // maybeCutLine is no-op, but method called
    }

    @Test(timeout = 4000)
    public void testAddOpUnaryNoCutLine() {
        consumer.addOp("+", false);
        assertTrue(consumer.getOutput().contains("+"));
    }

    @Test(timeout = 4000)
    public void testAddNumberNegativeZeroAfterMinusNoSpaceIfPrevNotMinus() {
        consumer.append("x");
        consumer.lastChar = 'x';
        consumer.addNumber(-0.0);
        // x < 0 true, prev is 'x' not '-', so no extra space
        assertEquals("x-0.0", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberLargeNegativeExponent() {
        consumer.addNumber(-1000.0);
        // negative large number: x<0, prev not '-', so no space, then (long)x == -1000, goes to integer branch.
        // -1000: value=-1000, mantissa=-1000, exp=0; loop: mantissa/10 = -100, pow(10,1)=10, product=-1000 == value, so mantissa=-100, exp=1; loop: -100/10=-10, pow(10,2)=100, product=-1000, mantissa=-10, exp=2; loop: -10/10=-1, pow(10,3)=1000, product=-1000, mantissa=-1, exp=3; exp>2 -> add("-1E3")
        assertEquals("-1E3", consumer.getOutput());
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    // No significant lifecycle methods in this abstract class, but we can test method chaining.
}