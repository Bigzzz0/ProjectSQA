package org.apache.commons.csv;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.csv.ExtendedBufferedReader
 * Target Defect: CSV-75 / Csv-1 (Defects4J: getLineNumber() failure on standalone CR in read())
 * -----------------------------------------------------------------------------------------
 * Decision Branches & Conditions:
 * 1. read():
 *    - current == '\n' -> increment lineCounter
 *    - [DEFECT CSV-75]: current == '\r' -> should increment lineCounter (missing in buggy code)
 *    - CRLF sequence via read() -> should only increment lineCounter once across \r\n
 *    - EOF handling -> returns -1 (END_OF_STREAM), lastChar updated
 * 2. read(char[] buf, int offset, int length):
 *    - length == 0 -> early return 0, no state change
 *    - len > 0 -> character loop across offset to offset + len:
 *      * ch == '\n' when (i > 0 && buf[i-1] == '\r') -> no increment (CRLF split inside buffer)
 *      * ch == '\n' when (i > 0 && buf[i-1] != '\r') -> lineCounter++
 *      * ch == '\n' when (i == 0 && lastChar == '\r') -> no increment (CRLF split across reads)
 *      * ch == '\n' when (i == 0 && lastChar != '\r') -> lineCounter++
 *      * ch == '\r' -> lineCounter++
 *      * other characters -> lineCounter unaffected
 *      * lastChar updated to buf[offset + len - 1]
 *    - len == -1 -> lastChar set to END_OF_STREAM (-1)
 * 3. readLine():
 *    - line != null && line.length() > 0 -> lastChar = line.charAt(len-1), lineCounter++
 *    - line != null && line.length() == 0 -> lastChar unchanged, lineCounter++
 *    - line == null (EOF) -> lastChar = END_OF_STREAM, returns null
 * 4. lookAhead():
 *    - returns next char without consuming or altering lastChar / lineCounter
 *    - at EOF returns END_OF_STREAM (-1)
 * 5. readAgain():
 *    - initially UNDEFINED (-2)
 *    - returns lastChar after read(), read(char[], int, int), readLine(), or EOF
 * -----------------------------------------------------------------------------------------
 */

import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ExtendedBufferedReaderGeminiTest {

    private ExtendedBufferedReader createReader(String input) {
        return new ExtendedBufferedReader(new StringReader(input));
    }

    // =========================================================================
    // Partition A: Core Character & Lookahead Operations
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialState_ReadAgainIsUndefined() {
        ExtendedBufferedReader reader = createReader("abc");
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        assertEquals(0, reader.getLineNumber());
    }

    @Test(timeout = 4000)
    public void testRead_UpdatesLastCharAndReadAgain() throws IOException {
        ExtendedBufferedReader reader = createReader("xy");
        int first = reader.read();
        assertEquals('x', first);
        assertEquals('x', reader.readAgain());

        int second = reader.read();
        assertEquals('y', second);
        assertEquals('y', reader.readAgain());

        int eof = reader.read();
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, eof);
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
    }

    @Test(timeout = 4000)
    public void testLookAhead_DoesNotAdvanceReaderOrAlterState() throws IOException {
        ExtendedBufferedReader reader = createReader("xyz");
        assertEquals('x', reader.lookAhead());
        assertEquals('x', reader.lookAhead());
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        assertEquals(0, reader.getLineNumber());

        assertEquals('x', reader.read());
        assertEquals('x', reader.readAgain());

        assertEquals('y', reader.lookAhead());
        assertEquals('x', reader.readAgain()); // readAgain still returns 'x'
        assertEquals('y', reader.read());
    }

    @Test(timeout = 4000)
    public void testLookAhead_AtEndOfStream() throws IOException {
        ExtendedBufferedReader reader = createReader("");
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.lookAhead());
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.lookAhead());
    }

    // =========================================================================
    // Partition B: Block Buffer Operations (read(char[], int, int))
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadBlock_LengthZeroReturnsZero() throws IOException {
        ExtendedBufferedReader reader = createReader("hello");
        char[] buf = new char[5];
        int bytesRead = reader.read(buf, 0, 0);
        assertEquals(0, bytesRead);
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        assertEquals(0, reader.getLineNumber());
    }

    @Test(timeout = 4000)
    public void testReadBlock_PopulatesBufferAndUpdatesLastChar() throws IOException {
        ExtendedBufferedReader reader = createReader("abcdef");
        char[] buf = new char[10];
        int bytesRead = reader.read(buf, 2, 4);

        assertEquals(4, bytesRead);
        assertEquals('a', buf[2]);
        assertEquals('b', buf[3]);
        assertEquals('c', buf[4]);
        assertEquals('d', buf[5]);
        assertEquals('d', reader.readAgain());
    }

    @Test(timeout = 4000)
    public void testReadBlock_HandlesEofProperly() throws IOException {
        ExtendedBufferedReader reader = createReader("a");
        char[] buf = new char[4];
        assertEquals(1, reader.read(buf, 0, 4));
        assertEquals('a', reader.readAgain());

        int eofRead = reader.read(buf, 0, 4);
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, eofRead);
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
    }

    @Test(timeout = 4000)
    public void testReadBlock_LineCounting_WithLF() throws IOException {
        ExtendedBufferedReader reader = createReader("line1\nline2\n");
        char[] buf = new char[12];
        int count = reader.read(buf, 0, buf.length);
        assertEquals(12, count);
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
    }

    @Test(timeout = 4000)
    public void testReadBlock_LineCounting_WithCRLF_WithinBuffer() throws IOException {
        ExtendedBufferedReader reader = createReader("line1\r\nline2\r\n");
        char[] buf = new char[14];
        int count = reader.read(buf, 0, buf.length);
        assertEquals(14, count);
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
    }

    @Test(timeout = 4000)
    public void testReadBlock_LineCounting_WithCRLF_AcrossBufferBoundary() throws IOException {
        ExtendedBufferedReader reader = createReader("line1\r\nline2");
        char[] buf1 = new char[6]; // reads "line1\r"
        int count1 = reader.read(buf1, 0, 6);
        assertEquals(6, count1);
        assertEquals('\r', reader.readAgain());
        assertEquals(1, reader.getLineNumber());

        char[] buf2 = new char[6]; // reads "\nline2" where \n is index 0 and lastChar == '\r'
        int count2 = reader.read(buf2, 0, 6);
        assertEquals(6, count2);
        // The \n immediately after \r across buffer boundaries must NOT increment lineCounter again
        assertEquals(1, reader.getLineNumber());
        assertEquals('2', reader.readAgain());
    }

    @Test(timeout = 4000)
    public void testReadBlock_LineCounting_WithLF_AtBufferStart_WhenLastCharNotCR() throws IOException {
        ExtendedBufferedReader reader = createReader("line1\nline2");
        char[] buf1 = new char[5]; // reads "line1"
        assertEquals(5, reader.read(buf1, 0, 5));
        assertEquals('1', reader.readAgain());
        assertEquals(0, reader.getLineNumber());

        char[] buf2 = new char[6]; // reads "\nline2", index 0 is '\n' and lastChar is '1' (not '\r')
        assertEquals(6, reader.read(buf2, 0, 6));
        assertEquals(1, reader.getLineNumber());
        assertEquals('2', reader.readAgain());
    }

    @Test(timeout = 4000)
    public void testReadBlock_LineCounting_WithStandaloneCR() throws IOException {
        ExtendedBufferedReader reader = createReader("line1\rline2\r");
        char[] buf = new char[12];
        int count = reader.read(buf, 0, buf.length);
        assertEquals(12, count);
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
    }

    // =========================================================================
    // Partition C: Line Counting & Newline Semantics via readLine()
    // =========================================================================

    @Test(timeout = 4000)
    public void testReadLine_IncrementsLineNumberAndTracksLastChar() throws IOException {
        ExtendedBufferedReader reader = createReader("hello\r\nworld\nend");
        String line1 = reader.readLine();
        assertEquals("hello", line1);
        assertEquals('o', reader.readAgain());
        assertEquals(1, reader.getLineNumber());

        String line2 = reader.readLine();
        assertEquals("world", line2);
        assertEquals('d', reader.readAgain());
        assertEquals(2, reader.getLineNumber());

        String line3 = reader.readLine();
        assertEquals("end", line3);
        assertEquals('d', reader.readAgain());
        assertEquals(3, reader.getLineNumber());

        String line4 = reader.readLine();
        assertNull(line4);
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        assertEquals(3, reader.getLineNumber());
    }

    @Test(timeout = 4000)
    public void testReadLine_EmptyLines() throws IOException {
        ExtendedBufferedReader reader = createReader("\n\n");
        String line1 = reader.readLine();
        assertEquals("", line1);
        assertEquals(1, reader.getLineNumber());
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());

        String line2 = reader.readLine();
        assertEquals("", line2);
        assertEquals(2, reader.getLineNumber());

        String line3 = reader.readLine();
        assertNull(line3);
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        assertEquals(2, reader.getLineNumber());
    }

    @Test(timeout = 4000)
    public void testReadLine_StandaloneCarriageReturn() throws IOException {
        ExtendedBufferedReader reader = createReader("foo\rbar");
        assertEquals("foo", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals("bar", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        assertNull(reader.readLine());
    }

    // =========================================================================
    // Partition D: Defect-Targeted Branch Zone (CSV-75 / Csv-1)
    // =========================================================================

    /**
     * Dedicated defect reproduction test for CSV-75.
     * In the defective version of ExtendedBufferedReader, read() only checks
     * for (current == '\n') to increment lineCounter and completely ignores '\r'.
     * Consequently, character-by-character reading of standalone CR or CRLF fails
     * to update lineCounter properly.
     */
    @Test(timeout = 4000)
    public void testRead_IncrementsLineCounterOnCarriageReturn_CSV75() throws IOException {
        ExtendedBufferedReader reader = createReader("a\rb\rc");

        assertEquals('a', reader.read());
        assertEquals(0, reader.getLineNumber());

        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());

        assertEquals('b', reader.read());
        assertEquals(1, reader.getLineNumber());

        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());

        assertEquals('c', reader.read());
        assertEquals(2, reader.getLineNumber());

        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
    }

    @Test(timeout = 4000)
    public void testRead_IncrementsLineCounterOnCRLF_CSV75() throws IOException {
        ExtendedBufferedReader reader = createReader("a\r\nb\r\n");

        assertEquals('a', reader.read());
        assertEquals(0, reader.getLineNumber());

        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());

        // The following '\n' must NOT increment lineCounter again
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());

        assertEquals('b', reader.read());
        assertEquals(1, reader.getLineNumber());

        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());

        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());

        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
    }

    @Test(timeout = 4000)
    public void testRead_PureLF_IncrementsLineCounter() throws IOException {
        ExtendedBufferedReader reader = createReader("a\nb\nc\n");

        assertEquals('a', reader.read());
        assertEquals(0, reader.getLineNumber());

        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());

        assertEquals('b', reader.read());
        assertEquals(1, reader.getLineNumber());

        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());

        assertEquals('c', reader.read());
        assertEquals(2, reader.getLineNumber());

        assertEquals('\n', reader.read());
        assertEquals(3, reader.getLineNumber());

        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(3, reader.getLineNumber());
    }

    // =========================================================================
    // Partition E: Edge Cases, Boundaries & Stream Exhaustion
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyStream() throws IOException {
        ExtendedBufferedReader reader = createReader("");
        assertEquals(0, reader.getLineNumber());
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.lookAhead());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        assertNull(reader.readLine());
        assertEquals(0, reader.getLineNumber());
    }

    @Test(timeout = 4000)
    public void testSingleCharacterStream() throws IOException {
        ExtendedBufferedReader reader = createReader("z");
        assertEquals('z', reader.lookAhead());
        assertEquals(0, reader.getLineNumber());
        assertEquals('z', reader.read());
        assertEquals('z', reader.readAgain());
        assertEquals(0, reader.getLineNumber());

        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.lookAhead());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        assertEquals(0, reader.getLineNumber());
    }

    @Test(timeout = 4000)
    public void testRepeatedEofReads() throws IOException {
        ExtendedBufferedReader reader = createReader("x");
        assertEquals('x', reader.read());
        for (int i = 0; i < 5; i++) {
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
            assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.lookAhead());
        }
    }

    @Test(timeout = 4000)
    public void testMixedReadAndReadLine() throws IOException {
        ExtendedBufferedReader reader = createReader("first line\nsecond line\rthird line");
        assertEquals('f', reader.read());
        assertEquals('i', reader.read());
        assertEquals('r', reader.read());
        assertEquals('s', reader.read());
        assertEquals('t', reader.read());

        // readLine reads remainder of first line
        String remainder = reader.readLine();
        assertEquals(" line", remainder);
        assertEquals(1, reader.getLineNumber());
        assertEquals('e', reader.readAgain());

        String secondLine = reader.readLine();
        assertEquals("second line", secondLine);
        assertEquals(2, reader.getLineNumber());

        String thirdLine = reader.readLine();
        assertEquals("third line", thirdLine);
        assertEquals(3, reader.getLineNumber());

        assertNull(reader.readLine());
    }

    @Test(timeout = 4000)
    public void testMixedLookAheadAndReadBlock() throws IOException {
        ExtendedBufferedReader reader = createReader("abcdefgh");
        assertEquals('a', reader.lookAhead());

        char[] buf = new char[4];
        int count = reader.read(buf, 0, 4);
        assertEquals(4, count);
        assertEquals("abcd", new String(buf, 0, count));
        assertEquals('d', reader.readAgain());

        assertEquals('e', reader.lookAhead());
        assertEquals('d', reader.readAgain());

        int count2 = reader.read(buf, 0, 4);
        assertEquals(4, count2);
        assertEquals("efgh", new String(buf, 0, count2));
        assertEquals('h', reader.readAgain());
    }
}