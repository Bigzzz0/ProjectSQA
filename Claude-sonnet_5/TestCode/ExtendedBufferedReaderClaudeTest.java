package org.apache.commons.csv;

import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExtendedBufferedReaderClaudeTest {

    /**
     * @target read() - single char read, lastChar/lineCounter update
     * @scenario Reading standalone \r characters one at a time via read()
     * @defectRisk The defective version's read() does not increment lineCounter
     * on '\r', causing getLineNumber() to remain 0 instead of incrementing.
     * This is the CSV-75 ground truth defect.
     */
    @Test(timeout = 4000)
    public void testRead_IncrementsLineCounterOnCarriageReturn_CSV75() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("a\rb\rc"));

        assertEquals(0, br.getLineNumber());

        int c1 = br.read();
        assertEquals('a', c1);
        assertEquals(0, br.getLineNumber());

        int c2 = br.read();
        assertEquals('\r', c2);
        // After reading past the first standalone CR, line number must be 1
        assertEquals(1, br.getLineNumber());

        int c3 = br.read();
        assertEquals('b', c3);
        assertEquals(1, br.getLineNumber());

        int c4 = br.read();
        assertEquals('\r', c4);
        // After reading past the second standalone CR, line number must be 2
        assertEquals(2, br.getLineNumber());

        int c5 = br.read();
        assertEquals('c', c5);
        assertEquals(2, br.getLineNumber());

        int c6 = br.read();
        assertEquals(-1, c6);
        assertEquals(2, br.getLineNumber());
    }

    /**
     * @target read() - CRLF sequence handling via single-char read()
     * @scenario Reading "a\r\nb" character by character; \r\n must increment
     * the line counter exactly once (not twice).
     * @defectRisk read() only increments on '\n', ignoring standalone '\r';
     * verifies CRLF still counts as a single line increment.
     */
    @Test(timeout = 4000)
    public void testRead_CRLF_IncrementsLineCounterOnce() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("a\r\nb"));

        assertEquals('a', br.read());
        assertEquals(0, br.getLineNumber());

        // NOTE: read() only increments lineCounter on '\n' per current source.
        // This documents actual read() behavior for CRLF via single-char read.
        int c2 = br.read();
        assertEquals('\r', c2);

        int c3 = br.read();
        assertEquals('\n', c3);
        assertEquals(1, br.getLineNumber());

        int c4 = br.read();
        assertEquals('b', c4);
        assertEquals(1, br.getLineNumber());
    }

    /**
     * @target read() - EOF behavior
     * @scenario Reading from an empty stream
     * @defectRisk lastChar should be set to END_OF_STREAM equivalent (-1) at EOF
     */
    @Test(timeout = 4000)
    public void testRead_EmptyStream_ReturnsEOF() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader(""));
        int c = br.read();
        assertEquals(-1, c);
        assertEquals(-1, br.readAgain());
    }

    /**
     * @target readAgain() - UNDEFINED initial state
     * @scenario No read has occurred yet on a freshly created reader
     * @defectRisk readAgain() should return UNDEFINED (-2) before any read call
     */
    @Test(timeout = 4000)
    public void testReadAgain_InitialStateIsUndefined() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("abc"));
        assertEquals(ExtendedBufferedReader.UNDEFINED, br.readAgain());
    }

    /**
     * @target readAgain() - tracks last read character
     * @scenario Multiple read() calls; readAgain() should reflect most recent char
     * @defectRisk readAgain() might not stay in sync with lastChar updates
     */
    @Test(timeout = 4000)
    public void testReadAgain_TracksLastReadCharacter() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("xy"));
        br.read();
        assertEquals('x', br.readAgain());
        br.read();
        assertEquals('y', br.readAgain());
    }

    /**
     * @target readAgain() - END_OF_STREAM after exhausting stream
     * @scenario Read all characters then read again to hit EOF
     * @defectRisk lastChar not properly set to END_OF_STREAM after EOF via read()
     */
    @Test(timeout = 4000)
    public void testReadAgain_EndOfStreamAfterExhaustion() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("a"));
        br.read();
        assertEquals('a', br.readAgain());
        br.read(); // EOF
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
    }

    /**
     * @target read(char[], int, int) - zero length request
     * @scenario length == 0 passed in; must return 0 immediately without side effects
     * @defectRisk Should short-circuit and not touch lastChar/lineCounter
     */
    @Test(timeout = 4000)
    public void testReadBuffer_ZeroLength_ReturnsZero() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("abc"));
        char[] buf = new char[5];
        int result = br.read(buf, 0, 0);
        assertEquals(0, result);
        assertEquals(ExtendedBufferedReader.UNDEFINED, br.readAgain());
        assertEquals(0, br.getLineNumber());
    }

    /**
     * @target read(char[], int, int) - EOF on empty stream
     * @scenario Attempt to read from an already-exhausted/empty stream
     * @defectRisk lastChar must be set to END_OF_STREAM when len == -1
     */
    @Test(timeout = 4000)
    public void testReadBuffer_EmptyStream_ReturnsMinusOne() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader(""));
        char[] buf = new char[5];
        int result = br.read(buf, 0, 5);
        assertEquals(-1, result);
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
    }

    /**
     * @target read(char[], int, int) - plain text without newlines
     * @scenario Reading "hello" into buffer; lineCounter stays 0, lastChar is last char read
     * @defectRisk Incorrect lastChar assignment or spurious line counting
     */
    @Test(timeout = 4000)
    public void testReadBuffer_NoNewlines() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("hello"));
        char[] buf = new char[10];
        int len = br.read(buf, 0, 10);
        assertEquals(5, len);
        assertEquals('o', br.readAgain());
        assertEquals(0, br.getLineNumber());
    }

    /**
     * @target read(char[], int, int) - LF only sequence increments lineCounter
     * @scenario Reading "a\nb\nc" into buffer in one shot
     * @defectRisk Each standalone \n (not preceded by \r) should increment lineCounter
     */
    @Test(timeout = 4000)
    public void testReadBuffer_LFOnly_IncrementsLineCounter() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("a\nb\nc"));
        char[] buf = new char[10];
        int len = br.read(buf, 0, 10);
        assertEquals(5, len);
        assertEquals(2, br.getLineNumber());
        assertEquals('c', br.readAgain());
    }

    /**
     * @target read(char[], int, int) - CRLF sequence within buffer counts once per pair
     * @scenario Reading "a\r\nb\r\nc" into buffer - each \r\n pair should only count once
     * (the \n check looks back at buf[i-1]=='\r' to avoid double count, but \r itself increments)
     * @defectRisk Off-by-one or double counting logic for CRLF combos within buffer reads
     */
    @Test(timeout = 4000)
    public void testReadBuffer_CRLF_WithinBuffer() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("a\r\nb\r\nc"));
        char[] buf = new char[10];
        int len = br.read(buf, 0, 10);
        assertEquals(7, len);
        // Each \r increments once; \n following \r (buf[i-1]=='\r') does not increment again.
        assertEquals(2, br.getLineNumber());
        assertEquals('c', br.readAgain());
    }

    /**
     * @target read(char[], int, int) - CR-only sequences within buffer
     * @scenario Reading "a\rb\rc" into buffer in one call; each standalone \r increments counter
     * @defectRisk Standalone \r must increment lineCounter in the block-read path too
     */
    @Test(timeout = 4000)
    public void testReadBuffer_CROnly_IncrementsLineCounter() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("a\rb\rc"));
        char[] buf = new char[10];
        int len = br.read(buf, 0, 10);
        assertEquals(5, len);
        assertEquals(2, br.getLineNumber());
        assertEquals('c', br.readAgain());
    }

    /**
     * @target read(char[], int, int) - lastChar lookback when i==0 and previous lastChar=='\r'
     * @scenario Read one char '\r' first, then read remaining buffer starting with '\n'
     * so that the boundary check uses lastChar field (i>0 false) instead of buf[i-1]
     * @defectRisk The ternary '(i > 0 ? buf[i-1] : lastChar)' must correctly reference
     * the previous lastChar field when i==0, avoiding double-count of CRLF split across reads
     */
    @Test(timeout = 4000)
    public void testReadBuffer_CRLFSplitAcrossReads_UsesLastCharField() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("\r\nb"));
        // First read consumes just the '\r'
        int first = br.read();
        assertEquals('\r', first);
        assertEquals(1, br.getLineNumber());

        // Now block-read remaining "\nb" - the '\n' at i==0 should see lastChar=='\r'
        // and NOT increment lineCounter again.
        char[] buf = new char[10];
        int len = br.read(buf, 0, 10);
        assertEquals(2, len);
        assertEquals(1, br.getLineNumber());
        assertEquals('b', br.readAgain());
    }

    /**
     * @target read(char[], int, int) - offset handling
     * @scenario Read into buffer starting at non-zero offset; verify correct placement
     * and correct lastChar computed from offset+len-1
     * @defectRisk Off-by-one errors in offset arithmetic for lastChar calculation
     */
    @Test(timeout = 4000)
    public void testReadBuffer_WithOffset() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("xyz"));
        char[] buf = new char[10];
        int len = br.read(buf, 3, 3);
        assertEquals(3, len);
        assertEquals('x', buf[3]);
        assertEquals('y', buf[4]);
        assertEquals('z', buf[5]);
        assertEquals('z', br.readAgain());
    }

    /**
     * @target readLine() - basic line reading with LF terminator
     * @scenario Reading "line1\nline2" via readLine()
     * @defectRisk lastChar should be set to last char of the line, lineCounter incremented
     */
    @Test(timeout = 4000)
    public void testReadLine_BasicLF() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("line1\nline2"));
        String line1 = br.readLine();
        assertEquals("line1", line1);
        assertEquals('1', br.readAgain());
        assertEquals(1, br.getLineNumber());

        String line2 = br.readLine();
        assertEquals("line2", line2);
        assertEquals('2', br.readAgain());
        assertEquals(2, br.getLineNumber());

        String line3 = br.readLine();
        assertNull(line3);
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
        assertEquals(2, br.getLineNumber());
    }

    /**
     * @target readLine() - empty line handling
     * @scenario Reading "\n\n" produces two empty lines; lastChar should remain unset
     * (not updated) for zero-length lines since length()>0 check fails
     * @defectRisk lastChar should not be modified for empty-string lines
     */
    @Test(timeout = 4000)
    public void testReadLine_EmptyLines() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("\n\n"));
        int initialLastChar = br.readAgain();
        assertEquals(ExtendedBufferedReader.UNDEFINED, initialLastChar);

        String line1 = br.readLine();
        assertEquals("", line1);
        // lastChar untouched because line.length() == 0
        assertEquals(ExtendedBufferedReader.UNDEFINED, br.readAgain());
        assertEquals(1, br.getLineNumber());

        String line2 = br.readLine();
        assertEquals("", line2);
        assertEquals(2, br.getLineNumber());

        String line3 = br.readLine();
        assertNull(line3);
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
    }

    /**
     * @target readLine() - EOF handling directly
     * @scenario Calling readLine() on an empty stream returns null immediately
     * @defectRisk lastChar must be set to END_OF_STREAM, lineCounter must not increment
     */
    @Test(timeout = 4000)
    public void testReadLine_EmptyStreamReturnsNull() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader(""));
        String line = br.readLine();
        assertNull(line);
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, br.readAgain());
        assertEquals(0, br.getLineNumber());
    }

    /**
     * @target readLine() - CRLF terminator stripped
     * @scenario Reading "abc\r\ndef" via readLine(); terminator should be stripped
     * @defectRisk readLine() must strip both CR and LF and correctly report last char
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFTerminator() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("abc\r\ndef"));
        String line1 = br.readLine();
        assertEquals("abc", line1);
        assertEquals('c', br.readAgain());
        assertEquals(1, br.getLineNumber());

        String line2 = br.readLine();
        assertEquals("def", line2);
        assertEquals('f', br.readAgain());
        assertEquals(2, br.getLineNumber());
    }

    /**
     * @target lookAhead() - peek without consuming
     * @scenario Peek at next char then read() should return the same value
     * @defectRisk lookAhead() might consume the character instead of just peeking
     */
    @Test(timeout = 4000)
    public void testLookAhead_DoesNotConsumeCharacter() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("ab"));
        int peeked = br.lookAhead();
        assertEquals('a', peeked);
        // lookAhead should not update lastChar via read() semantics directly,
        // but let's verify subsequent read() still returns 'a'
        int actual = br.read();
        assertEquals('a', actual);
        assertEquals('a', br.readAgain());

        int peeked2 = br.lookAhead();
        assertEquals('b', peeked2);
        int actual2 = br.read();
        assertEquals('b', actual2);
    }

    /**
     * @target lookAhead() - EOF behavior
     * @scenario Peeking at end of stream returns -1 (END_OF_STREAM marker)
     * @defectRisk lookAhead() might throw or misbehave at EOF boundary
     */
    @Test(timeout = 4000)
    public void testLookAhead_AtEndOfStream() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("x"));
        br.read(); // consume 'x'
        int peeked = br.lookAhead();
        assertEquals(-1, peeked);
        int actual = br.read();
        assertEquals(-1, actual);
    }

    /**
     * @target lookAhead() - repeated peeks return same value
     * @scenario Multiple consecutive lookAhead() calls without intervening read()
     * should consistently return the same next character
     * @defectRisk mark/reset misuse could cause state corruption on repeated peeks
     */
    @Test(timeout = 4000)
    public void testLookAhead_RepeatedPeeksConsistent() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("z"));
        assertEquals('z', br.lookAhead());
        assertEquals('z', br.lookAhead());
        assertEquals('z', br.lookAhead());
        assertEquals('z', br.read());
    }

    /**
     * @target getLineNumber() - initial state
     * @scenario Freshly constructed reader; no reads performed
     * @defectRisk getLineNumber() must return 0 initially, not some other default
     */
    @Test(timeout = 4000)
    public void testGetLineNumber_InitiallyZero() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("abc"));
        assertEquals(0, br.getLineNumber());
    }

    /**
     * @target getLineNumber() - combined mixed newline scenario across all read methods
     * @scenario Use read(), read(buf,...), and readLine() together on multi-line content
     * with mixed LF/CRLF terminators, verifying cumulative line count correctness
     * @defectRisk Cross-method state (lastChar) must be consistently shared to avoid
     * double counting or under counting when switching between read strategies
     */
    @Test(timeout = 4000)
    public void testGetLineNumber_MixedReadMethods() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("one\r\ntwo\nthree"));

        // read single chars through "one\r\n"
        assertEquals('o', br.read());
        assertEquals('n', br.read());
        assertEquals('e', br.read());
        assertEquals('\r', br.read());
        assertEquals(1, br.getLineNumber());
        assertEquals('\n', br.read());
        // \n immediately following \r (lastChar=='\r') should not double count
        assertEquals(1, br.getLineNumber());

        // now readLine() for "two"
        String line = br.readLine();
        assertEquals("two", line);
        assertEquals(2, br.getLineNumber());

        // now block read remaining "three"
        char[] buf = new char[10];
        int len = br.read(buf, 0, 10);
        assertEquals(5, len);
        assertEquals(2, br.getLineNumber());
        assertEquals('e', br.readAgain());
    }

    /**
     * @target read() - CR at very end of stream (no trailing char after \r)
     * @scenario Stream is just "\r"; read() should return '\r' and increment lineCounter,
     * subsequent read() returns EOF
     * @defectRisk Boundary condition where CR is the last character before EOF
     */
    @Test(timeout = 4000)
    public void testRead_LoneCRAtEndOfStream() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("\r"));
        int c = br.read();
        assertEquals('\r', c);
        assertEquals(1, br.getLineNumber());
        int eof = br.read();
        assertEquals(-1, eof);
        assertEquals(1, br.getLineNumber());
    }

    /**
     * @target read(char[], int, int) - buffer starts with '\n' and lastChar was '\n' from prior read
     * @scenario Verify that when previous lastChar is '\n' (not '\r'), a leading '\n'
     * in the new buffer read still increments the counter correctly (no false suppression)
     * @defectRisk Incorrect suppression of line count when lastChar was '\n' instead of '\r'
     */
    @Test(timeout = 4000)
    public void testReadBuffer_LeadingLFWithPriorNonCRLastChar() throws IOException {
        ExtendedBufferedReader br = new ExtendedBufferedReader(new StringReader("a\nb"));
        int first = br.read();
        assertEquals('a', first);
        assertEquals(0, br.getLineNumber());

        char[] buf = new char[10];
        int len = br.read(buf, 0, 10);
        assertEquals(2, len);
        // buf[0]='\n', i==0 so check lastChar which is 'a' (not '\r') -> increments
        assertEquals(1, br.getLineNumber());
        assertEquals('b', br.readAgain());
    }
}