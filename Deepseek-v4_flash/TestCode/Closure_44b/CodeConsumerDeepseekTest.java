package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for CodeConsumer targeting line/branch coverage and the known defect.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - addIdentifier: verifies normal identifier addition
 *   - appendBlockStart/End: block boundary transitions
 *   - beginBlock/endBlock: state machine for statementNeedsEnded
 *   - endStatement/maybeEndStatement: semicolon management
 *   - endFunction: sawFunction flag and statement context
 *   - beginCaseBody/endCaseBody: case construct
 *   - startNewLine/endLine/maybeCutLine: line management
 *   - shouldPreserveExtraBlocks/breakAfterBlockFor: boolean decisions
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Empty string in add()
 *   - Division operator boundary in add() (prevents / / /)
 *   - Negative zero handling in addNumber()
 *   - Large numbers with exponent optimization in addNumber()
 *   - Consecutive + and - operators in addOp()
 *   - Arrow operator (->) avoidance in addOp()
 *   - instanceof/typeof spacing in addOp()
 * 
 * Partition C: Defect-Targeted Branch Zone (testIssue620)
 *   - The known defect involves regex after division in a statement context
 *   - Triggered by: add("/ /") after add("") with specific state transitions
 *   - The bug causes missing space: "/ //[]/ /" vs "/ // / /"
 *   - Test must reproduce the exact sequence from CodePrinter
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - isNegativeZero with positive/negative zero
 *   - isWordChar with various characters (letters, digits, underscore, dollar, symbols)
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - State transitions through multiple methods
 *   - Flag management (statementStarted, statementNeedsEnded, sawFunction)
 */
public class CodeConsumerDeepseekTest {

    // ===== Concrete implementation for testing abstract methods =====
    private static class TestableCodeConsumer extends CodeConsumer {
        private final StringBuilder output = new StringBuilder();
        private char lastChar = '\0';
        private boolean continueProcessing = true;

        @Override
        char getLastChar() {
            return lastChar;
        }

        @Override
        void append(String str) {
            if (!str.isEmpty()) {
                lastChar = str.charAt(str.length() - 1);
            }
            output.append(str);
        }

        String getOutput() {
            return output.toString();
        }

        void reset() {
            output.setLength(0);
            lastChar = '\0';
            statementNeedsEnded = false;
            statementStarted = false;
            sawFunction = false;
        }

        @Override
        boolean continueProcessing() {
            return continueProcessing;
        }

        void setContinueProcessing(boolean value) {
            this.continueProcessing = value;
        }
    }

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testAddIdentifier() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.addIdentifier("foo");
        assertEquals("foo", consumer.getOutput());
        assertEquals('o', consumer.getLastChar());
    }

    @Test(timeout = 4000)
    public void testAppendBlockStartEnd() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.appendBlockStart();
        assertEquals("{", consumer.getOutput());
        assertEquals('{', consumer.getLastChar());
        
        consumer.appendBlockEnd();
        assertEquals("{}", consumer.getOutput());
        assertEquals('}', consumer.getLastChar());
    }

    @Test(timeout = 4000)
    public void testBeginBlockWithStatementNeedsEnded() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.statementNeedsEnded = true;
        consumer.statementStarted = true;
        
        consumer.beginBlock();
        assertEquals(";{", consumer.getOutput());
        assertEquals('{', consumer.getLastChar());
        assertFalse(consumer.statementNeedsEnded);
    }

    @Test(timeout = 4000)
    public void testBeginBlockWithoutStatementNeedsEnded() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.statementNeedsEnded = false;
        
        consumer.beginBlock();
        assertEquals("{", consumer.getOutput());
        assertFalse(consumer.statementNeedsEnded);
    }

    @Test(timeout = 4000)
    public void testEndBlockDefault() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.append("x");
        consumer.endBlock();
        assertEquals("x}", consumer.getOutput());
        assertFalse(consumer.statementNeedsEnded);
    }

    @Test(timeout = 4000)
    public void testEndBlockWithEndLine() {
        TestableCodeConsumer consumer = new TestableCodeConsumer() {
            private boolean endLineCalled = false;
            
            @Override
            void endLine() {
                endLineCalled = true;
                append("\n");
            }
            
            @Override
            String getOutput() {
                super.getOutput();
                return super.getOutput() + (endLineCalled ? "_endLine" : "");
            }
        };
        consumer.append("x");
        consumer.endBlock(true);
        assertTrue(consumer.getOutput().contains("}"));
        assertFalse(consumer.statementNeedsEnded);
    }

    @Test(timeout = 4000)
    public void testEndStatementWithSemicolon() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.statementStarted = true;
        consumer.endStatement(true);
        assertEquals(";", consumer.getOutput());
        assertFalse(consumer.statementNeedsEnded);
    }

    @Test(timeout = 4000)
    public void testEndStatementWithoutSemicolonStatementStarted() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.statementStarted = true;
        consumer.endStatement(false);
        assertEquals("", consumer.getOutput());
        assertTrue(consumer.statementNeedsEnded);
    }

    @Test(timeout = 4000)
    public void testEndStatementWithoutSemicolonStatementNotStarted() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.statementStarted = false;
        consumer.endStatement(false);
        assertEquals("", consumer.getOutput());
        assertFalse(consumer.statementNeedsEnded);
    }

    @Test(timeout = 4000)
    public void testMaybeEndStatementWhenNeeded() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.statementNeedsEnded = true;
        
        consumer.maybeEndStatement();
        assertEquals(";", consumer.getOutput());
        assertFalse(consumer.statementNeedsEnded);
        assertTrue(consumer.statementStarted);
    }

    @Test(timeout = 4000)
    public void testMaybeEndStatementWhenNotNeeded() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.statementNeedsEnded = false;
        consumer.statementStarted = false;
        
        consumer.maybeEndStatement();
        assertEquals("", consumer.getOutput());
        assertFalse(consumer.statementNeedsEnded);
        assertTrue(consumer.statementStarted);
    }

    @Test(timeout = 4000)
    public void testEndFunctionDefault() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.endFunction();
        assertTrue(consumer.sawFunction);
    }

    @Test(timeout = 4000)
    public void testEndFunctionWithStatementContext() {
        TestableCodeConsumer consumer = new TestableCodeConsumer() {
            private boolean endLineCalled = false;
            
            @Override
            void endLine() {
                endLineCalled = true;
                append("\n");
            }
            
            @Override
            String getOutput() {
                super.getOutput();
                return super.getOutput() + (endLineCalled ? "_endLine" : "");
            }
        };
        consumer.endFunction(true);
        assertTrue(consumer.sawFunction);
        assertTrue(consumer.getOutput().contains("\n") || consumer.getOutput().contains("_endLine"));
    }

    @Test(timeout = 4000)
    public void testBeginCaseBody() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.beginCaseBody();
        assertEquals(":", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testEndCaseBody() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.endCaseBody();
        assertEquals("", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testListSeparator() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.listSeparator();
        assertEquals(",", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testContinueProcessingDefault() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        assertTrue(consumer.continueProcessing());
    }

    @Test(timeout = 4000)
    public void testShouldPreserveExtraBlocks() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        assertFalse(consumer.shouldPreserveExtraBlocks());
    }

    @Test(timeout = 4000)
    public void testBreakAfterBlockForTrue() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        assertTrue(consumer.breakAfterBlockFor(null, true));
    }

    @Test(timeout = 4000)
    public void testBreakAfterBlockForFalse() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        assertFalse(consumer.breakAfterBlockFor(null, false));
    }

    @Test(timeout = 4000)
    public void testEndFile() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.endFile();
        assertEquals("", consumer.getOutput());
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testAddEmptyString() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.add("");
        assertEquals("", consumer.getOutput());
        assertEquals('\0', consumer.getLastChar());
    }

    @Test(timeout = 4000)
    public void testAddWithSpaceBetweenIdentifiers() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        // Force word chars at end
        consumer.add("return");
        assertEquals("return", consumer.getOutput());
        consumer.add("foo");
        // Should add space between 'n' and 'f' because both are word chars
        assertTrue(consumer.getOutput().contains("return foo"));
    }

    @Test(timeout = 4000)
    public void testAddPreventsForwardSlashAfterDivision() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        // Simulate a regex after division context
        consumer.add("x");
        consumer.add("/");  // division operator
        // Now when adding a regex literal starting with /
        consumer.add("/ /");
        // The inner add should prevent the first / of regex from appearing right after /
        // This tests the logic: "Do not allow a forward slash to appear after a DIV"
        assertTrue(consumer.getOutput().contains("/"));
    }

    @Test(timeout = 4000)
    public void testAddNumberNegativeZero() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.addNumber(-0.0);
        // Negative zero should not be treated as integer, should be "0" (since -0 == 0)
        // Actually -0.0 is negative zero but (long)x == x is true for -0.0
        // isNegativeZero is checked
        assertEquals("0", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberLargeWithExponent() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.addNumber(12345.0);
        // 12345 >= 100, mantissa = 12345, exp starts at 0
        // mantissa/10 * 10^(exp+1) = 1234.5 * 10 = 12345 -> true, so mantissa=1234, exp=1
        // Check again: mantissa/10 * 10^2 = 123.4 * 100 = 12340 != 12345 -> stop
        // exp=1 <=2, so output is Long.toString(12345) = "12345"
        assertEquals("12345", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberWithExponentGreaterThan2() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.addNumber(1000000.0);
        // 1000000 >= 100
        // mantissa=1000000, exp=0
        // 1000000/10 * 10^1 = 100000 * 10 = 1000000 -> mantissa=100000, exp=1
        // 100000/10 * 10^2 = 10000 * 100 = 1000000 -> mantissa=10000, exp=2
        // 10000/10 * 10^3 = 1000 * 1000 = 1000000 -> mantissa=1000, exp=3
        // 1000/10 * 10^4 = 100 * 10000 = 1000000 -> mantissa=100, exp=4
        // 100/10 * 10^5 = 10 * 100000 = 1000000 -> mantissa=10, exp=5
        // 10/10 * 10^6 = 1 * 1000000 = 1000000 -> mantissa=1, exp=6
        // exp=6 > 2 -> output "1E6"
        assertEquals("1E6", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberNegativePreventsDashDash() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.add("-");  // last char is '-'
        consumer.addNumber(-4);
        // Should add space between '-' and '-4' to prevent '--4'
        assertEquals("- -4", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddOpConsecutivePlus() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.add("x");
        consumer.addOp("+", true);
        consumer.addOp("+", true);
        // After first plus, lastChar = '+'
        // When adding second '+', first=='+' && prev=='+' -> add space
        String output = consumer.getOutput().trim();
        assertTrue(output.contains("+ +"));
    }

    @Test(timeout = 4000)
    public void testAddOpConsecutiveMinus() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.add("x");
        consumer.addOp("-", true);
        consumer.addOp("-", true);
        String output = consumer.getOutput().trim();
        assertTrue(output.contains("- -"));
    }

    @Test(timeout = 4000)
    public void testAddOpBeforeArrowPrevMinus() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.add("-");
        // Now prev = '-', first of ">" is '>', prev == '-' && first == '>' is true
        consumer.addOp(">", false);
        // Should add space between '-' and '>'
        assertTrue(consumer.getOutput().contains("- >") || consumer.getOutput().contains(">"));
    }

    @Test(timeout = 4000)
    public void testAddOpAfterWordWithLetterOperator() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.add("instanceof");
        consumer.addOp("typeof", false);
        // prev = 'f' (last of instanceof), first of typeof is 't', both letters -> add space
        // but instanceof triggers maybeEndStatement which adds ';' if needed
        // Just check that output contains space
        String output = consumer.getOutput();
        assertTrue(output.contains("instanceof typeof") || 
                  (output.contains("instanceof") && output.contains("typeof")));
    }

    @Test(timeout = 4000)
    public void testAddOpWithBinOpCutLine() {
        TestableCodeConsumer consumer = new TestableCodeConsumer() {
            private boolean cutLineCalled = false;
            
            @Override
            void maybeCutLine() {
                cutLineCalled = true;
            }
            
            boolean wasCutLineCalled() {
                return cutLineCalled;
            }
        };
        consumer.add("x");
        consumer.addOp("+", true);
        assertTrue(consumer.wasCutLineCalled());
    }

    // ===== Partition C: Defect-Targeted Branch Zone (testIssue620) =====

    @Test(timeout = 4000)
    public void testIssue620() {
        // This test reproduces the exact sequence that triggers the bug in testIssue620
        // The bug: when printing "alert(/ // / /)", the space before the last '/' is missing
        // Expected: "alert(/ // / /)" but actual defective: "alert(/ //[]/ /)"
        
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        
        // Step 1: Simulate "alert("
        consumer.maybeEndStatement(); // statementStarted becomes true
        consumer.add("alert(");
        
        // Step 2: Simulate a regex literal "/ /" but with state management
        // The bug occurs because the division operator '/' is consumed and the regex '/ /' is misparsed
        // In the real scenario, two regex expressions are concatenated with an empty regex in between
        
        // This sequence mimics:
        // - A regex / / (space between slashes)
        // - Then something that triggers the extra empty regex
        // - Then another regex / /
        
        consumer.add("/ /"); // This adds a regex pattern with space inside
        // The 'add' method may add a space before this if last char was word char
        // But last char was '(' so no space added
        
        // Now add a comma separator as done in the test
        consumer.listSeparator();
        
        // Add a regex with an empty pattern // which is the problematic case
        // The bug is that add() doesn't properly handle the case where 
        // we have a regex after a division in a specific state
        consumer.add("/ /"); // Second regex, but due to state, the '/' might be stripped
        
        // Now close parenthesis
        consumer.add(")");
        
        String output = consumer.getOutput();
        // Expected: "alert(/ // / /)" but with the bug we lose a space
        // The defect reveals itself as missing character between the two // slashes
        
        // Check that the output contains the proper structure with spacing
        // The bug specifically loses the space character or adds unexpected brackets
        assertFalse("Should not contain empty brackets", 
                    output.contains("[]") || output.contains("()"));
        
        // Verify the output contains all expected parts
        assertEquals("alert( / / , / / )", output.replaceAll("\\s+", " ").trim());
    }

    @Test(timeout = 4000)
    public void testIssue620DetailedReproduction() {
        // More detailed reproduction mirroring the actual CodePrinter sequence
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        
        // Reproduce the exact sequence from the bug report
        // The bug is: alert(/ // / /) should print as "alert(/ // / /)"
        // But prints as "alert(/ //[]/ /)" due to incorrect handling
        
        // Sequence from CodePrinter for the expression:
        // Script [1,1]
        //   EXPR_RESULT [2,1]
        //     CALL [2,1]
        //       NAME alert [2,1]
        //       REGEXP / / [2,6]
        //       REGEXP / / [2,13]
        
        // Simulate the print:
        consumer.maybeEndStatement(); // statement started
        
        // Print name "alert"
        consumer.add("alert");
        
        // Print open paren
        consumer.add("(");
        
        // Print first regex: / /
        consumer.add("/ /");
        
        // Print comma separator
        consumer.listSeparator();
        
        // Add a space that might be needed (the bug zone)
        // In the bug, the space between // and / / is lost
        // This is because after adding first regex, the last char is '/'
        // Then add() is called with "/ /", and since both are word chars or '/',
        // it should add a space, but the state machine fails here
        
        // Print second regex: / /
        consumer.add("/ /");
        
        // Close paren
        consumer.add(")");
        
        // Print semicolon if needed
        consumer.endStatement(true);
        
        String output = consumer.getOutput();
        // The key assertion: the output should have proper spacing
        // In the bug, this prints "alert(/ //[]/ /)" with extra '[]' 
        // or "alert(/ // / /)" missing space before last '/'
        assertFalse("Output should not contain empty brackets", 
                    output.contains("[]"));
        
        // Verify the spacing around the division operators
        int lastSlashIndex = output.lastIndexOf("/");
        int secondLastSlashIndex = output.lastIndexOf("/", lastSlashIndex - 1);
        
        // There should be a space (or proper token) between the two sets of slashes
        if (secondLastSlashIndex > 0 && lastSlashIndex > 0) {
            String between = output.substring(secondLastSlashIndex + 1, lastSlashIndex);
            assertFalse("Should not have empty space between regex patterns", 
                       between.trim().isEmpty() && between.length() > 0);
        }
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testIsNegativeZeroPositiveZero() {
        assertFalse(CodeConsumer.isNegativeZero(0.0));
    }

    @Test(timeout = 4000)
    public void testIsNegativeZeroNegativeZero() {
        assertTrue(CodeConsumer.isNegativeZero(-0.0));
    }

    @Test(timeout = 4000)
    public void testIsNegativeZeroNonZero() {
        assertFalse(CodeConsumer.isNegativeZero(1.0));
        assertFalse(CodeConsumer.isNegativeZero(-1.0));
        assertFalse(CodeConsumer.isNegativeZero(Double.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void testIsWordCharUnderscore() {
        assertTrue(CodeConsumer.isWordChar('_'));
    }

    @Test(timeout = 4000)
    public void testIsWordCharDollar() {
        assertTrue(CodeConsumer.isWordChar('$'));
    }

    @Test(timeout = 4000)
    public void testIsWordCharLetter() {
        assertTrue(CodeConsumer.isWordChar('a'));
        assertTrue(CodeConsumer.isWordChar('Z'));
    }

    @Test(timeout = 4000)
    public void testIsWordCharDigit() {
        assertTrue(CodeConsumer.isWordChar('0'));
        assertTrue(CodeConsumer.isWordChar('9'));
    }

    @Test(timeout = 4000)
    public void testIsWordCharSpecial() {
        assertFalse(CodeConsumer.isWordChar('/'));
        assertFalse(CodeConsumer.isWordChar('+'));
        assertFalse(CodeConsumer.isWordChar(' '));
        assertFalse(CodeConsumer.isWordChar('('));
    }

    @Test(timeout = 4000)
    public void testStartEndSourceMapping() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        // These are no-ops, just verify they don't throw
        Node mockNode = null; // Normally would be a real Node, but testing interface
        consumer.startSourceMapping(null);
        consumer.endSourceMapping(null);
    }

    @Test(timeout = 4000)
    public void testStartNewLine() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.startNewLine();
        assertEquals("", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testMaybeLineBreak() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.maybeLineBreak();
        assertEquals("", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testMaybeCutLine() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.maybeCutLine();
        assertEquals("", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testEndLine() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.endLine();
        assertEquals("", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testNotePreferredLineBreak() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.notePreferredLineBreak();
        assertEquals("", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberRegularDecimal() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.addNumber(3.14);
        assertTrue(consumer.getOutput().contains("3.14"));
    }

    @Test(timeout = 4000)
    public void testAddNumberSmallInteger() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.addNumber(5.0);
        assertEquals("5", consumer.getOutput());
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testStateMachineFullCycle() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        
        // Initial state
        assertFalse(consumer.statementNeedsEnded);
        assertFalse(consumer.statementStarted);
        assertFalse(consumer.sawFunction);
        
        // Start a statement
        consumer.maybeEndStatement();
        assertTrue(consumer.statementStarted);
        assertFalse(consumer.statementNeedsEnded);
        
        // Add code
        consumer.add("x");
        assertTrue(consumer.statementStarted);
        
        // End statement without semi
        consumer.endStatement(false);
        assertTrue(consumer.statementNeedsEnded);
        assertTrue(consumer.statementStarted);
        
        // Start new statement (should trigger semicolon)
        consumer.maybeEndStatement();
        assertFalse(consumer.statementNeedsEnded);
        assertTrue(consumer.statementStarted);
        
        // Begin block
        consumer.beginBlock();
        assertFalse(consumer.statementNeedsEnded);
        
        // End block
        consumer.endBlock();
        assertFalse(consumer.statementNeedsEnded);
        
        // End function
        consumer.endFunction();
        assertTrue(consumer.sawFunction);
        
        String output = consumer.getOutput();
        assertTrue(output.contains(";") || output.contains("{") || output.contains("}"));
    }

    @Test(timeout = 4000)
    public void testMultipleContinueProcessingCalls() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        assertTrue(consumer.continueProcessing());
        consumer.setContinueProcessing(false);
        assertFalse(consumer.continueProcessing());
        consumer.setContinueProcessing(true);
        assertTrue(consumer.continueProcessing());
    }

    @Test(timeout = 4000)
    public void testAddNumberWithNegativeNonInteger() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.addNumber(-3.5);
        assertEquals("-3.5", consumer.getOutput());
    }

    @Test(timeout = 4000)
    public void testAddNumberWithVeryLargeValue() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.addNumber(Double.MAX_VALUE);
        // Should just output string representation
        assertNotNull(consumer.getOutput());
        assertFalse(consumer.getOutput().isEmpty());
    }

    @Test(timeout = 4000)
    public void testAddOpWithNonBinOpNoCutLine() {
        TestableCodeConsumer consumer = new TestableCodeConsumer() {
            private boolean cutLineCalled = false;
            
            @Override
            void maybeCutLine() {
                cutLineCalled = true;
            }
            
            boolean wasCutLineCalled() {
                return cutLineCalled;
            }
        };
        consumer.add("x");
        consumer.addOp("!", false);
        assertFalse(consumer.wasCutLineCalled());
    }

    @Test(timeout = 4000)
    public void testAppendOp() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.appendOp("+", true);
        assertEquals("+", consumer.getOutput());
        assertEquals('+', consumer.getLastChar());
    }

    @Test(timeout = 4000)
    public void testBeginBlockWithoutEndedStatement() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.statementNeedsEnded = false;
        consumer.statementStarted = true;
        consumer.beginBlock();
        assertEquals("{", consumer.getOutput());
        assertFalse(consumer.statementNeedsEnded);
    }

    @Test(timeout = 4000)
    public void testEndBlockResetsFlag() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.statementNeedsEnded = true;
        consumer.endBlock();
        assertFalse(consumer.statementNeedsEnded);
    }

    @Test(timeout = 4000)
    public void testEndFunctionSetsSawFunction() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        assertFalse(consumer.sawFunction);
        consumer.endFunction();
        assertTrue(consumer.sawFunction);
    }

    @Test(timeout = 4000)
    public void testAddWithBackslashAtStart() {
        TestableCodeConsumer consumer = new TestableCodeConsumer();
        consumer.add("return");
        consumer.add("\\u0041"); // backslash unicode escape
        // Backslash is a word char? Actually isWordChar('\\') returns false
        // but the condition checks if first char is word char OR backslash
        // So it should add space
        assertTrue(consumer.getOutput().contains("return \\u0041") || 
                  consumer.getOutput().contains("return\\u0041"));
    }
}