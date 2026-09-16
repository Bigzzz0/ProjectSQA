package org.apache.commons.csv;

import static org.apache.commons.csv.Constants.BACKSPACE;
import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.END_OF_STREAM;
import static org.apache.commons.csv.Constants.FF;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.TAB;
import static org.apache.commons.csv.Constants.UNDEFINED;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import org.junit.Test;

/**
 * /* [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------------------------
 * TARGET CLASS: Lexer.java (org.apache.commons.csv)
 *
 * DECISION BRANCHES & COVERAGE TARGETS:
 * 1. readEscape():
 *    - case 'r': returns CR ('\r')
 *    - case 'n': returns LF ('\n')
 *    - case 't': returns TAB ('\t')
 *    - case 'b': returns BACKSPACE ('\b')
 *    - case 'f': returns FF ('\f')
 *    - case CR, LF, FF, TAB, BACKSPACE (literal control characters): returns c unmodified
 *    - case END_OF_STREAM: throws IOException("EOF whilst processing escape sequence")
 *    - default:
 *      * [DEFECT TARGET]: In defective versions (e.g. CSV-4 / CSVLexerTest failures), unhandled escaped characters
 *        (e.g., 'a', 'N' for MySQL null, '\\', '"', delimiters, punctuation) incorrectly returned END_OF_STREAM (-1)
 *        or were dropped instead of returning c. Must assert return value == c.
 *
 * 2. trimTrailingSpaces(StringBuilder):
 *    - buffer empty (length == 0) -> no modification, loop not entered
 *    - buffer with no trailing spaces -> while condition fails immediately, length == buffer.length(), no setLength
 *    - buffer with only whitespace -> while loop runs to length == 0, setLength(0) called
 *    - buffer with mixed content and trailing whitespace (spaces, tabs, newlines, form feeds) -> trimmed to content
 *    - buffer with leading/internal whitespace only -> internal whitespace preserved, length unmodified
 *
 * 3. readEndOfLine(int c):
 *    - c == CR and lookAhead() == LF: greedy \r\n consumption, stream advances past LF, returns true
 *    - c == CR and lookAhead() != LF: lone \r, next char not consumed, returns true
 *    - c == CR and lookAhead() == END_OF_STREAM: lone \r at EOF, returns true
 *    - c == LF: lone \n, lookAhead not evaluated, returns true
 *    - c == other character / whitespace / END_OF_STREAM: returns false
 *
 * 4. isWhitespace(int c):
 *    - c == delimiter: always returns false even if delimiter is Character.isWhitespace (e.g., TAB or SPACE)
 *    - c != delimiter and Character.isWhitespace(c): returns true
 *    - c != delimiter and !Character.isWhitespace(c): returns false
 *
 * 5. isStartOfLine(int c):
 *    - c == LF || c == CR || c == UNDEFINED (-2): returns true
 *    - c != LF && c != CR && c != UNDEFINED: returns false
 *
 * 6. isEndOfFile(int c):
 *    - c == END_OF_STREAM (-1): returns true
 *    - c != END_OF_STREAM: returns false
 *
 * 7. isDelimiter(int), isEscape(int), isQuoteChar(int), isCommentStart(int):
 *    - Matching character: returns true
 *    - Non-matching character: returns false
 *    - Null configuration mapping to DISABLED ('\ufffe'): never matches normal characters or EOF
 *
 * 8. getLineNumber():
 *    - Delegates directly to in.getLineNumber()
 * --------------------------------------------------------------------------------------------------------------------
 */
public class LexerGeminiTest {

    /**
     * Concrete test implementation of abstract Lexer.
     */
    private static class ConcreteTestLexer extends Lexer {
        ConcreteTestLexer(final CSVFormat format, final ExtendedBufferedReader in) {
            super(format, in);
        }

        @Override
        Token nextToken(final Token reusableToken) throws IOException {
            return reusableToken;
        }
    }

    private Lexer createLexer(final String input, final CSVFormat format) {
        final StringReader reader = new StringReader(input);
        final ExtendedBufferedReader in = new ExtendedBufferedReader(reader);
        return new ConcreteTestLexer(format, in);
    }

    // ================================================================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ================================================================================================================

    @Test(timeout = 4000)
    public void testReadEscapeStandardControlCharacters() throws IOException {
        assertEquals(CR, createLexer("r", CSVFormat.DEFAULT.withEscape('\\')).readEscape());
        assertEquals(LF, createLexer("n", CSVFormat.DEFAULT.withEscape('\\')).readEscape());
        assertEquals(TAB, createLexer("t", CSVFormat.DEFAULT.withEscape('\\')).readEscape());
        assertEquals(BACKSPACE, createLexer("b", CSVFormat.DEFAULT.withEscape('\\')).readEscape());
        assertEquals(FF, createLexer("f", CSVFormat.DEFAULT.withEscape('\\')).readEscape());
    }

    @Test(timeout = 4000)
    public void testReadEscapeLiteralControlCharacters() throws IOException {
        assertEquals(CR, createLexer("\r", CSVFormat.DEFAULT.withEscape('\\')).readEscape());
        assertEquals(LF, createLexer("\n", CSVFormat.DEFAULT.withEscape('\\')).readEscape());
        assertEquals(FF, createLexer("\f", CSVFormat.DEFAULT.withEscape('\\')).readEscape());
        assertEquals(TAB, createLexer("\t", CSVFormat.DEFAULT.withEscape('\\')).readEscape());
        assertEquals(BACKSPACE, createLexer("\b", CSVFormat.DEFAULT.withEscape('\\')).readEscape());
    }

    @Test(timeout = 4000)
    public void testReadEndOfLineCrLfGreedyAdvance() throws IOException {
        final Lexer lexer = createLexer("\nRemainingPayload", CSVFormat.DEFAULT);
        // c is CR, and next stream char is LF
        assertTrue("CR followed by LF should return true", lexer.readEndOfLine(CR));
        // The LF must have been greedily consumed, next char should be 'R'
        assertEquals('R', lexer.in.read());
    }

    @Test(timeout = 4000)
    public void testReadEndOfLineCrAloneDoesNotAdvance() throws IOException {
        final Lexer lexer = createLexer("TextAfterCr", CSVFormat.DEFAULT);
        // c is CR, but next stream char is 'T' (not LF)
        assertTrue("Lone CR should return true", lexer.readEndOfLine(CR));
        // 'T' must NOT have been consumed by readEndOfLine
        assertEquals('T', lexer.in.read());
    }

    @Test(timeout = 4000)
    public void testReadEndOfLineLfAlone() throws IOException {
        final Lexer lexer = createLexer("TextAfterLf", CSVFormat.DEFAULT);
        assertTrue("Lone LF should return true", lexer.readEndOfLine(LF));
        assertEquals('T', lexer.in.read());
    }

    @Test(timeout = 4000)
    public void testReadEndOfLineNonEolChars() throws IOException {
        final Lexer lexer = createLexer("abc", CSVFormat.DEFAULT);
        assertFalse("Regular character should return false", lexer.readEndOfLine('a'));
        assertFalse("Whitespace should return false", lexer.readEndOfLine(' '));
        assertFalse("Tab should return false", lexer.readEndOfLine('\t'));
        assertFalse("EOF marker should return false", lexer.readEndOfLine(END_OF_STREAM));
    }

    @Test(timeout = 4000)
    public void testTrimTrailingSpacesMixed() {
        final Lexer lexer = createLexer("", CSVFormat.DEFAULT);
        final StringBuilder sb = new StringBuilder("DataValue  \t \r \n \f ");
        lexer.trimTrailingSpaces(sb);
        assertEquals("DataValue", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrimTrailingSpacesInternalPreserved() {
        final Lexer lexer = createLexer("", CSVFormat.DEFAULT);
        final StringBuilder sb = new StringBuilder("  Value With Spaces  ");
        lexer.trimTrailingSpaces(sb);
        assertEquals("  Value With Spaces", sb.toString());
    }

    @Test(timeout = 4000)
    public void testIsWhitespaceStandardCommaDelimiter() {
        final Lexer lexer = createLexer("", CSVFormat.DEFAULT.withDelimiter(','));
        assertTrue("Space is whitespace", lexer.isWhitespace(' '));
        assertTrue("Tab is whitespace", lexer.isWhitespace('\t'));
        assertTrue("LF is whitespace", lexer.isWhitespace('\n'));
        assertTrue("CR is whitespace", lexer.isWhitespace('\r'));
        assertFalse("Delimiter comma must not be whitespace", lexer.isWhitespace(','));
        assertFalse("Alpha is not whitespace", lexer.isWhitespace('A'));
    }

    @Test(timeout = 4000)
    public void testIsWhitespaceWhenDelimiterIsWhitespace() {
        final Lexer tabLexer = createLexer("", CSVFormat.DEFAULT.withDelimiter('\t'));
        assertFalse("Tab delimiter must NOT be treated as whitespace", tabLexer.isWhitespace('\t'));
        assertTrue("Space is still whitespace", tabLexer.isWhitespace(' '));

        final Lexer spaceLexer = createLexer("", CSVFormat.DEFAULT.withDelimiter(' '));
        assertFalse("Space delimiter must NOT be treated as whitespace", spaceLexer.isWhitespace(' '));
        assertTrue("Tab is still whitespace", spaceLexer.isWhitespace('\t'));
    }

    @Test(timeout = 4000)
    public void testIsStartOfLine() {
        final Lexer lexer = createLexer("", CSVFormat.DEFAULT);
        assertTrue("LF indicates start of line", lexer.isStartOfLine(LF));
        assertTrue("CR indicates start of line", lexer.isStartOfLine(CR));
        assertTrue("UNDEFINED indicates start of file/line", lexer.isStartOfLine(UNDEFINED));
        assertFalse("Regular character is not start of line", lexer.isStartOfLine('x'));
        assertFalse("EOF is not start of line", lexer.isStartOfLine(END_OF_STREAM));
        assertFalse("Zero is not start of line", lexer.isStartOfLine(0));
    }

    @Test(timeout = 4000)
    public void testIsEndOfFile() {
        final Lexer lexer = createLexer("", CSVFormat.DEFAULT);
        assertTrue("END_OF_STREAM indicates EOF", lexer.isEndOfFile(END_OF_STREAM));
        assertFalse("UNDEFINED is not EOF", lexer.isEndOfFile(UNDEFINED));
        assertFalse("Character is not EOF", lexer.isEndOfFile('Z'));
        assertFalse("Zero is not EOF", lexer.isEndOfFile(0));
    }

    @Test(timeout = 4000)
    public void testCharacterMatchingPredicates() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withDelimiter(';')
                .withEscape('\\')
                .withQuoteChar('\'')
                .withCommentStart('#');
        final Lexer lexer = createLexer("", format);

        assertTrue("Matching delimiter", lexer.isDelimiter(';'));
        assertFalse("Mismatch delimiter", lexer.isDelimiter(','));

        assertTrue("Matching escape", lexer.isEscape('\\'));
        assertFalse("Mismatch escape", lexer.isEscape('/'));

        assertTrue("Matching quote", lexer.isQuoteChar('\''));
        assertFalse("Mismatch quote", lexer.isQuoteChar('"'));

        assertTrue("Matching comment start", lexer.isCommentStart('#'));
        assertFalse("Mismatch comment start", lexer.isCommentStart('!'));
    }

    @Test(timeout = 4000)
    public void testGetLineNumberTracking() throws IOException {
        final Lexer lexer = createLexer("line1\nline2\r\nline3", CSVFormat.DEFAULT);
        assertEquals("Initial line number is 0", 0L, lexer.getLineNumber());

        int c;
        while ((c = lexer.in.read()) != '\n' && c != END_OF_STREAM) {
            // consume line 1
        }
        assertEquals("Line number after reading line1", 1L, lexer.getLineNumber());

        while ((c = lexer.in.read()) != '\n' && c != END_OF_STREAM) {
            // consume line 2
        }
        assertEquals("Line number after reading line2", 2L, lexer.getLineNumber());
    }

    // ================================================================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ================================================================================================================

    @Test(timeout = 4000)
    public void testTrimTrailingSpacesEmptyBuffer() {
        final Lexer lexer = createLexer("", CSVFormat.DEFAULT);
        final StringBuilder sb = new StringBuilder();
        lexer.trimTrailingSpaces(sb);
        assertEquals(0, sb.length());
    }

    @Test(timeout = 4000)
    public void testTrimTrailingSpacesNoTrailingSpaces() {
        final Lexer lexer = createLexer("", CSVFormat.DEFAULT);
        final StringBuilder sb = new StringBuilder("NoTrailingWhitespace");
        lexer.trimTrailingSpaces(sb);
        assertEquals("NoTrailingWhitespace", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrimTrailingSpacesAllWhitespace() {
        final Lexer lexer = createLexer("", CSVFormat.DEFAULT);
        final StringBuilder sb = new StringBuilder("   \t\t \r\n ");
        lexer.trimTrailingSpaces(sb);
        assertEquals(0, sb.length());
        assertEquals("", sb.toString());
    }

    @Test(timeout = 4000)
    public void testTrimTrailingSpacesSingleTrailingSpace() {
        final Lexer lexer = createLexer("", CSVFormat.DEFAULT);
        final StringBuilder sb = new StringBuilder("X ");
        lexer.trimTrailingSpaces(sb);
        assertEquals("X", sb.toString());
    }

    @Test(timeout = 4000)
    public void testReadEndOfLineCrAtEof() throws IOException {
        final Lexer lexer = createLexer("", CSVFormat.DEFAULT);
        // c is CR, and lookAhead returns END_OF_STREAM (-1)
        assertTrue("CR at EOF should return true", lexer.readEndOfLine(CR));
        assertEquals(END_OF_STREAM, lexer.in.read());
    }

    @Test(timeout = 4000)
    public void testDisabledCharactersWhenNullInFormat() {
        // Formats with null escape, quote, and comment
        final CSVFormat format = CSVFormat.newFormat('|');
        final Lexer lexer = createLexer("", format);

        assertFalse("Disabled escape must not match backslash", lexer.isEscape('\\'));
        assertFalse("Disabled quote must not match double quote", lexer.isQuoteChar('"'));
        assertFalse("Disabled quote must not match single quote", lexer.isQuoteChar('\''));
        assertFalse("Disabled comment must not match hash", lexer.isCommentStart('#'));
        assertFalse("Disabled escape must not match EOF", lexer.isEscape(END_OF_STREAM));
        assertFalse("Disabled quote must not match EOF", lexer.isQuoteChar(END_OF_STREAM));
        assertFalse("Disabled comment must not match EOF", lexer.isCommentStart(END_OF_STREAM));
    }

    @Test(timeout = 4000)
    public void testIsWhitespaceNumericBoundaries() {
        final Lexer lexer = createLexer("", CSVFormat.DEFAULT);
        assertFalse("EOF (-1) is not whitespace", lexer.isWhitespace(END_OF_STREAM));
        assertFalse("Null char (0) is not whitespace", lexer.isWhitespace(0));
        assertFalse("Character.MAX_VALUE is not whitespace", lexer.isWhitespace(Character.MAX_VALUE));
    }

    // ================================================================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth Defect in readEscape)
    // ================================================================================================================

    /**
     * Target Defect Ground Truth:
     * - org.apache.commons.csv.CSVLexerTest::testEscapedCharacter
     *   readEscape() on "a" must return 'a', not END_OF_STREAM (-1).
     */
    @Test(timeout = 4000)
    public void testDefectEscapedCharacterA() throws IOException {
        final Lexer lexer = createLexer("a", CSVFormat.DEFAULT.withEscape('\\'));
        final int unescaped = lexer.readEscape();
        assertEquals("readEscape() must return the escaped character 'a'", 'a', unescaped);
    }

    /**
     * Target Defect Ground Truth:
     * - org.apache.commons.csv.CSVLexerTest::testEscapedMySqlNullValue
     *   MySQL null sequence \N must unescape 'N' correctly.
     */
    @Test(timeout = 4000)
    public void testDefectEscapedMySqlNullValue() throws IOException {
        final Lexer lexer = createLexer("N", CSVFormat.DEFAULT.withEscape('\\'));
        final int unescaped = lexer.readEscape();
        assertEquals("readEscape() must return 'N' for MySQL null indicator", 'N', unescaped);
    }

    /**
     * Target Defect Ground Truth:
     * - org.apache.commons.csv.CSVParserTest::testBackslashEscaping
     *   Escaping delimiters, quotes, backslashes, and structural symbols must return the literal character.
     */
    @Test(timeout = 4000)
    public void testDefectBackslashEscapingSpecialAndMetaChars() throws IOException {
        final String metaChars = "\\\"',#][/ 0123456789xyzXYZ";
        for (int i = 0; i < metaChars.length(); i++) {
            final char ch = metaChars.charAt(i);
            final Lexer lexer = createLexer(String.valueOf(ch), CSVFormat.DEFAULT.withEscape('\\'));
            final int actual = lexer.readEscape();
            assertEquals("Failed to unescape character: '" + ch + "'", (int) ch, actual);
        }
    }

    // ================================================================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ================================================================================================================

    @Test(timeout = 4000, expected = IOException.class)
    public void testReadEscapeAtEofThrowsException() throws IOException {
        final Lexer lexer = createLexer("", CSVFormat.DEFAULT.withEscape('\\'));
        lexer.readEscape();
    }

    @Test(timeout = 4000)
    public void testReadEscapeAtEofExceptionMessage() {
        final Lexer lexer = createLexer("", CSVFormat.DEFAULT.withEscape('\\'));
        try {
            lexer.readEscape();
            fail("Expected IOException when escape character occurs at EOF");
        } catch (final IOException e) {
            assertNotNull("Exception message must not be null", e.getMessage());
            assertTrue("Exception message should indicate EOF in escape sequence",
                    e.getMessage().contains("EOF whilst processing escape sequence"));
        }
    }

    // ================================================================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ================================================================================================================

    @Test(timeout = 4000)
    public void testLexerConfigurationPropagation() {
        final CSVFormat format = CSVFormat.DEFAULT
                .withDelimiter('\t')
                .withEscape('\\')
                .withQuoteChar('"')
                .withCommentStart('#')
                .withIgnoreSurroundingSpaces(true)
                .withIgnoreEmptyLines(false);

        final Lexer lexer = createLexer("dummy", format);

        assertSame("Format instance must be preserved", format, lexer.format);
        assertTrue("Delimiter must match configured value", lexer.isDelimiter('\t'));
        assertTrue("Escape must match configured value", lexer.isEscape('\\'));
        assertTrue("Quote must match configured value", lexer.isQuoteChar('"'));
        assertTrue("Comment start must match configured value", lexer.isCommentStart('#'));
        assertTrue("ignoreSurroundingSpaces must be true", lexer.ignoreSurroundingSpaces);
        assertFalse("ignoreEmptyLines must be false", lexer.ignoreEmptyLines);
    }

    @Test(timeout = 4000)
    public void testLexerWithMySqlFormatContract() {
        final CSVFormat format = CSVFormat.MYSQL;
        final Lexer lexer = createLexer("sample", format);

        assertTrue("MySQL uses tab delimiter", lexer.isDelimiter('\t'));
        assertTrue("MySQL uses backslash escape", lexer.isEscape('\\'));
        assertFalse("MySQL quote is disabled", lexer.isQuoteChar('"'));
        assertFalse("MySQL comment is disabled", lexer.isCommentStart('#'));
        assertFalse("MySQL does not ignore empty lines", lexer.ignoreEmptyLines);
    }
}