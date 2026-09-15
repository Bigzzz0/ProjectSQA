package org.apache.commons.csv;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

public class LexerDeepseekTest {

    /* [Branch & Defect Analysis Matrix] */
    // Target class: Lexer (abstract) - tested via a concrete subclass that exposes package-private methods
    // Defect: readEscape() incorrectly handles escaped backslash ('\\') - returns the backslash itself
    //         instead of consuming the next character. This causes escaped quotes/backslashes to be lost.
    // Branches targeted:
    // - readEscape(): switch on c: 'r','n','t','b','f', CR, LF, FF, TAB, BACKSPACE, END_OF_STREAM, default
    // - trimTrailingSpaces(): while loop with Character.isWhitespace, length boundary
    // - readEndOfLine(): CR followed by LF, CR alone, LF alone, other chars
    // - isWhitespace(): delimiter vs whitespace, non-whitespace
    // - isStartOfLine(): LF, CR, UNDEFINED, other
    // - isEndOfFile(): END_OF_STREAM, other
    // - isDelimiter(), isEscape(), isQuoteChar(), isCommentStart(): equality checks
    // - mapNullToDisabled(): null vs non-null Character
    // - Constructor: null vs non-null format fields

    // Test subclass to instantiate abstract Lexer and expose package-private methods
    private static class TestLexer extends Lexer {
        TestLexer(CSVFormat format, ExtendedBufferedReader in) {
            super(format, in);
        }

        @Override
        Token nextToken(Token reusableToken) throws IOException {
            return null; // not used in these tests
        }

        // Expose package-private methods for testing
        int readEscape() throws IOException { return super.readEscape(); }
        void trimTrailingSpaces(StringBuilder buffer) { super.trimTrailingSpaces(buffer); }
        boolean readEndOfLine(int c) throws IOException { return super.readEndOfLine(c); }
        boolean isWhitespace(int c) { return super.isWhitespace(c); }
        boolean isStartOfLine(int c) { return super.isStartOfLine(c); }
        boolean isEndOfFile(int c) { return super.isEndOfFile(c); }
        boolean isDelimiter(int c) { return super.isDelimiter(c); }
        boolean isEscape(int c) { return super.isEscape(c); }
        boolean isQuoteChar(int c) { return super.isQuoteChar(c); }
        boolean isCommentStart(int c) { return super.isCommentStart(c); }
        long getLineNumber() { return super.getLineNumber(); }
    }

    private TestLexer createLexer(String input, CSVFormat format) throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(input));
        return new TestLexer(format, reader);
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testReadEscapeSimpleEscapes() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        TestLexer lexer = createLexer("rn", format);
        assertEquals("r escape", CR, lexer.readEscape());
        assertEquals("n escape", LF, lexer.readEscape());
    }

    @Test(timeout = 4000)
    public void testReadEscapeSpecialChars() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        TestLexer lexer = createLexer("tbf", format);
        assertEquals("t escape", TAB, lexer.readEscape());
        assertEquals("b escape", BACKSPACE, lexer.readEscape());
        assertEquals("f escape", FF, lexer.readEscape());
    }

    @Test(timeout = 4000)
    public void testReadEscapeLiteralControlChars() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        // Test CR, LF, FF, TAB, BACKSPACE as literal characters after escape
        String input = "" + CR + LF + FF + TAB + BACKSPACE;
        TestLexer lexer = createLexer(input, format);
        assertEquals("CR literal", CR, lexer.readEscape());
        assertEquals("LF literal", LF, lexer.readEscape());
        assertEquals("FF literal", FF, lexer.readEscape());
        assertEquals("TAB literal", TAB, lexer.readEscape());
        assertEquals("BACKSPACE literal", BACKSPACE, lexer.readEscape());
    }

    @Test(timeout = 4000)
    public void testReadEscapeEndOfStream() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        TestLexer lexer = createLexer("", format);
        try {
            lexer.readEscape();
            fail("Expected IOException for EOF after escape");
        } catch (IOException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReadEscapeDefaultChar() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        TestLexer lexer = createLexer("x", format);
        assertEquals("default char", 'x', lexer.readEscape());
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testTrimTrailingSpacesEmptyBuffer() {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("", format);
        StringBuilder sb = new StringBuilder("");
        lexer.trimTrailingSpaces(sb);
        assertEquals("empty buffer unchanged", "", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrimTrailingSpacesAllSpaces() {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("", format);
        StringBuilder sb = new StringBuilder("   ");
        lexer.trimTrailingSpaces(sb);
        assertEquals("all spaces trimmed", "", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrimTrailingSpacesMixed() {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("", format);
        StringBuilder sb = new StringBuilder("abc  \t\n");
        lexer.trimTrailingSpaces(sb);
        assertEquals("trailing whitespace trimmed", "abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrimTrailingSpacesNoTrailing() {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("", format);
        StringBuilder sb = new StringBuilder("abc");
        lexer.trimTrailingSpaces(sb);
        assertEquals("no trailing spaces", "abc", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReadEndOfLineCRLF() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("\r\n", format);
        assertTrue("CRLF is end of line", lexer.readEndOfLine(CR));
        assertEquals("CR consumed, LF read", LF, lexer.in.getLastChar());
    }

    @Test(timeout = 4000)
    public void testReadEndOfLineCR() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("\r", format);
        assertTrue("CR is end of line", lexer.readEndOfLine(CR));
    }

    @Test(timeout = 4000)
    public void testReadEndOfLineLF() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("\n", format);
        assertTrue("LF is end of line", lexer.readEndOfLine(LF));
    }

    @Test(timeout = 4000)
    public void testReadEndOfLineNotEOL() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("a", format);
        assertFalse("non-EOL char", lexer.readEndOfLine('a'));
    }

    @Test(timeout = 4000)
    public void testIsWhitespaceDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(' ');
        TestLexer lexer = createLexer("", format);
        assertFalse("delimiter is not whitespace", lexer.isWhitespace(' '));
        assertTrue("space is whitespace", lexer.isWhitespace('\t'));
        assertFalse("non-whitespace", lexer.isWhitespace('a'));
    }

    @Test(timeout = 4000)
    public void testIsStartOfLine() {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("", format);
        assertTrue("LF", lexer.isStartOfLine(LF));
        assertTrue("CR", lexer.isStartOfLine(CR));
        assertTrue("UNDEFINED", lexer.isStartOfLine(UNDEFINED));
        assertFalse("other", lexer.isStartOfLine('a'));
    }

    @Test(timeout = 4000)
    public void testIsEndOfFile() {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("", format);
        assertTrue("END_OF_STREAM", lexer.isEndOfFile(END_OF_STREAM));
        assertFalse("other", lexer.isEndOfFile('a'));
    }

    @Test(timeout = 4000)
    public void testIsDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',');
        TestLexer lexer = createLexer("", format);
        assertTrue("delimiter", lexer.isDelimiter(','));
        assertFalse("other", lexer.isDelimiter(';'));
    }

    @Test(timeout = 4000)
    public void testIsEscape() {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        TestLexer lexer = createLexer("", format);
        assertTrue("escape", lexer.isEscape('\\'));
        assertFalse("other", lexer.isEscape('/'));
    }

    @Test(timeout = 4000)
    public void testIsQuoteChar() {
        CSVFormat format = CSVFormat.DEFAULT.withQuote('"');
        TestLexer lexer = createLexer("", format);
        assertTrue("quote", lexer.isQuoteChar('"'));
        assertFalse("other", lexer.isQuoteChar('\''));
    }

    @Test(timeout = 4000)
    public void testIsCommentStart() {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker('#');
        TestLexer lexer = createLexer("", format);
        assertTrue("comment", lexer.isCommentStart('#'));
        assertFalse("other", lexer.isCommentStart(';'));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect: readEscape() when encountering a backslash followed by another backslash
     * should return the backslash itself (escaped backslash). But the buggy implementation
     * returns the backslash without consuming the next character, causing the next character
     * to be processed separately. This test verifies that the escaped backslash is handled
     * correctly by checking the returned value and the state of the reader.
     */
    @Test(timeout = 4000)
    public void testReadEscapeEscapedBackslash() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        // Input: "\\" (two backslashes) - first is escape, second should be returned
        TestLexer lexer = createLexer("\\", format);
        // The escape char is read by readEscape(), then it reads the next char which is '\'
        int result = lexer.readEscape();
        assertEquals("escaped backslash should return backslash", '\\', result);
        // After reading the escaped backslash, the reader should be at end of stream
        assertEquals("reader should be at EOF", END_OF_STREAM, lexer.in.read());
    }

    /**
     * Defect: When escape is followed by a quote character, the quote should be returned
     * as a literal character. The buggy implementation may mishandle this.
     */
    @Test(timeout = 4000)
    public void testReadEscapeEscapedQuote() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\').withQuote('"');
        // Input: "\"" (backslash followed by quote)
        TestLexer lexer = createLexer("\"", format);
        int result = lexer.readEscape();
        assertEquals("escaped quote should return quote", '"', result);
        assertEquals("reader should be at EOF", END_OF_STREAM, lexer.in.read());
    }

    /**
     * Defect: The testBackslashEscaping failure shows that when parsing a quoted string
     * with escaped quotes and backslashes, the backslash handling is incorrect.
     * This test simulates the scenario where a backslash escapes a quote inside a quoted field.
     */
    @Test(timeout = 4000)
    public void testBackslashEscapingInQuotedField() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\').withQuote('"');
        // Simulate reading: "quoted \" [\\] /" string"
        // The lexer should correctly handle the escaped quote and backslash
        TestLexer lexer = createLexer("\\\"", format);
        // First readEscape should return the quote (escaped quote)
        int first = lexer.readEscape();
        assertEquals("escaped quote", '"', first);
        // Now the reader should be at EOF
        assertEquals("EOF after escaped quote", END_OF_STREAM, lexer.in.read());
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testReadEscapeIOExceptionOnEOF() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        TestLexer lexer = createLexer("", format);
        try {
            lexer.readEscape();
            fail("Expected IOException");
        } catch (IOException expected) {
            assertTrue("IOException message", expected.getMessage().contains("EOF"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullEscape() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        TestLexer lexer = createLexer("", format);
        // When escape is null, it's mapped to DISABLED, so isEscape should return false for any char
        assertFalse("null escape disabled", lexer.isEscape('\\'));
        assertFalse("null escape disabled for other", lexer.isEscape('x'));
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullQuote() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        TestLexer lexer = createLexer("", format);
        assertFalse("null quote disabled", lexer.isQuoteChar('"'));
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullComment() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        TestLexer lexer = createLexer("", format);
        assertFalse("null comment disabled", lexer.isCommentStart('#'));
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testGetLineNumberInitial() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("", format);
        assertEquals("initial line number", 0, lexer.getLineNumber());
    }

    @Test(timeout = 4000)
    public void testGetLineNumberAfterRead() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("a\nb", format);
        lexer.in.read(); // read 'a'
        lexer.in.read(); // read '\n' - line number increments
        assertEquals("line number after newline", 1, lexer.getLineNumber());
    }

    @Test(timeout = 4000)
    public void testFormatFieldExposed() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(';');
        TestLexer lexer = createLexer("", format);
        assertSame("format field", format, lexer.format);
    }

    @Test(timeout = 4000)
    public void testIgnoreSurroundingSpacesField() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreSurroundingSpaces(true);
        TestLexer lexer = createLexer("", format);
        assertTrue("ignoreSurroundingSpaces", lexer.ignoreSurroundingSpaces);
    }

    @Test(timeout = 4000)
    public void testIgnoreEmptyLinesField() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withIgnoreEmptyLines(true);
        TestLexer lexer = createLexer("", format);
        assertTrue("ignoreEmptyLines", lexer.ignoreEmptyLines);
    }

    @Test(timeout = 4000)
    public void testInField() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("test"));
        TestLexer lexer = new TestLexer(format, reader);
        assertSame("in field", reader, lexer.in);
    }

    // Additional edge case tests for readEscape with various characters

    @Test(timeout = 4000)
    public void testReadEscapeWithUndefinedChar() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        // UNDEFINED is -2, but as a char it's 0xFFFE (DISABLED). Test with a normal char.
        TestLexer lexer = createLexer("a", format);
        assertEquals("default char a", 'a', lexer.readEscape());
    }

    @Test(timeout = 4000)
    public void testReadEscapeWithMultipleChars() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape('\\');
        TestLexer lexer = createLexer("ab", format);
        assertEquals("first char", 'a', lexer.readEscape());
        assertEquals("second char", 'b', lexer.readEscape());
    }

    @Test(timeout = 4000)
    public void testTrimTrailingSpacesWithNullBuffer() {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("", format);
        try {
            lexer.trimTrailingSpaces(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException expected) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testReadEndOfLineWithCRNotFollowedByLF() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("\ra", format);
        assertTrue("CR is EOL", lexer.readEndOfLine(CR));
        assertEquals("next char is a", 'a', lexer.in.getLastChar());
    }

    @Test(timeout = 4000)
    public void testIsWhitespaceWithDelimiterAndSpace() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter(' ');
        TestLexer lexer = createLexer("", format);
        assertFalse("space delimiter not whitespace", lexer.isWhitespace(' '));
        assertTrue("tab is whitespace", lexer.isWhitespace('\t'));
        assertTrue("newline is whitespace", lexer.isWhitespace('\n'));
    }

    @Test(timeout = 4000)
    public void testIsStartOfLineWithUndefined() {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("", format);
        assertTrue("UNDEFINED is start of line", lexer.isStartOfLine(UNDEFINED));
    }

    @Test(timeout = 4000)
    public void testIsEndOfFileWithOtherChar() {
        CSVFormat format = CSVFormat.DEFAULT;
        TestLexer lexer = createLexer("", format);
        assertFalse("not EOF", lexer.isEndOfFile('a'));
    }

    @Test(timeout = 4000)
    public void testIsDelimiterWithDifferentDelimiter() {
        CSVFormat format = CSVFormat.DEFAULT.withDelimiter('|');
        TestLexer lexer = createLexer("", format);
        assertTrue("pipe delimiter", lexer.isDelimiter('|'));
        assertFalse("comma not delimiter", lexer.isDelimiter(','));
    }

    @Test(timeout = 4000)
    public void testIsEscapeWithDisabledEscape() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withEscape(null);
        TestLexer lexer = createLexer("", format);
        assertFalse("disabled escape", lexer.isEscape('\\'));
    }

    @Test(timeout = 4000)
    public void testIsQuoteCharWithDisabledQuote() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withQuote(null);
        TestLexer lexer = createLexer("", format);
        assertFalse("disabled quote", lexer.isQuoteChar('"'));
    }

    @Test(timeout = 4000)
    public void testIsCommentStartWithDisabledComment() throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.withCommentMarker(null);
        TestLexer lexer = createLexer("", format);
        assertFalse("disabled comment", lexer.isCommentStart('#'));
    }
}