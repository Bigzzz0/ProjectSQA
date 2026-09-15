package org.apache.commons.csv;

import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for ExtendedBufferedReader targeting the known defect where
 * read() does not increment lineCounter on standalone carriage returns.
 */
public class ExtendedBufferedReaderDeepseekTest {

    /**
     * @target read()
     * @scenario Reading "a\rb\rc" character by character
     * @defectRisk Standalone CR not incrementing lineCounter (CSV-75)
     */
    @Test(timeout = 4000)
    public void testRead_IncrementsLineCounterOnCarriageReturn_CSV75() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\rb\rc"));
        
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
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading "a\r\nb" character by character
     * @defectRisk CRLF sequence incrementing lineCounter twice
     */
    @Test(timeout = 4000)
    public void testRead_CRLFIncrementsLineCounterOnce() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\r\nb"));
        
        assertEquals('a', reader.read());
        assertEquals(0, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber()); // Should not increment again
        
        assertEquals('b', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading empty stream
     * @defectRisk Incorrect lastChar or lineCounter on EOF
     */
    @Test(timeout = 4000)
    public void testRead_EmptyStream() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(""));
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        assertEquals(0, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading "a\rb\rc" in one block
     * @defectRisk Block read not counting standalone CR
     */
    @Test(timeout = 4000)
    public void testReadBlock_StandaloneCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\rb\rc"));
        char[] buf = new char[10];
        
        int len = reader.read(buf, 0, 10);
        assertEquals(5, len);
        assertEquals("a\rb\rc", new String(buf, 0, len));
        assertEquals(2, reader.getLineNumber());
        assertEquals('c', reader.readAgain());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading "a\r\nb" in one block
     * @defectRisk CRLF counted twice in block read
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\r\nb"));
        char[] buf = new char[10];
        
        int len = reader.read(buf, 0, 10);
        assertEquals(4, len);
        assertEquals("a\r\nb", new String(buf, 0, len));
        assertEquals(1, reader.getLineNumber());
        assertEquals('b', reader.readAgain());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with length=0
     * @defectRisk Zero-length read should return 0 without state changes
     */
    @Test(timeout = 4000)
    public void testReadBlock_ZeroLength() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("abc"));
        char[] buf = new char[10];
        
        assertEquals(0, reader.read(buf, 0, 0));
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        assertEquals(0, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with offset and length
     * @defectRisk Incorrect line counting with offset
     */
    @Test(timeout = 4000)
    public void testReadBlock_WithOffset() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("xxa\rb"));
        char[] buf = new char[10];
        
        // Skip first two chars
        assertEquals(2, reader.read(buf, 0, 2));
        assertEquals(0, reader.getLineNumber());
        
        // Read "a\rb"
        int len = reader.read(buf, 2, 5);
        assertEquals(3, len);
        assertEquals(1, reader.getLineNumber());
        assertEquals('b', reader.readAgain());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading lines with LF only
     * @defectRisk Incorrect line counting or lastChar
     */
    @Test(timeout = 4000)
    public void testReadLine_LFOnly() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("line1\nline2\nline3"));
        
        assertEquals("line1", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('1', reader.readAgain());
        
        assertEquals("line2", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        assertEquals('2', reader.readAgain());
        
        assertEquals("line3", reader.readLine());
        assertEquals(3, reader.getLineNumber());
        assertEquals('3', reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        assertEquals(3, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading lines with CRLF
     * @defectRisk Incorrect line counting with CRLF
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("line1\r\nline2\r\n"));
        
        assertEquals("line1", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('1', reader.readAgain());
        
        assertEquals("line2", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        assertEquals('2', reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading empty lines
     * @defectRisk Empty line handling
     */
    @Test(timeout = 4000)
    public void testReadLine_EmptyLines() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readAgain()
     * @scenario Initial state and after reads
     * @defectRisk Incorrect lastChar tracking
     */
    @Test(timeout = 4000)
    public void testReadAgain_StateTracking() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("ab"));
        
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        
        assertEquals('a', reader.read());
        assertEquals('a', reader.readAgain());
        
        assertEquals('b', reader.read());
        assertEquals('b', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target lookAhead()
     * @scenario Peeking without consuming
     * @defectRisk Lookahead consuming characters or altering state
     */
    @Test(timeout = 4000)
    public void testLookAhead_DoesNotConsume() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("abc"));
        
        assertEquals('a', reader.lookAhead());
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        assertEquals(0, reader.getLineNumber());
        
        assertEquals('a', reader.read());
        assertEquals('a', reader.readAgain());
        
        assertEquals('b', reader.lookAhead());
        assertEquals('a', reader.readAgain()); // lookAhead doesn't change lastChar
        
        assertEquals('b', reader.read());
        assertEquals('b', reader.readAgain());
        
        reader.close();
    }

    /**
     * @target lookAhead()
     * @scenario Lookahead at EOF
     * @defectRisk Incorrect EOF handling in lookahead
     */
    @Test(timeout = 4000)
    public void testLookAhead_EOF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a"));
        
        assertEquals('a', reader.lookAhead());
        assertEquals('a', reader.read());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.lookAhead());
        assertEquals('a', reader.readAgain()); // lastChar unchanged by lookAhead
        
        reader.close();
    }

    /**
     * @target getLineNumber()
     * @scenario Mixed newline sequences
     * @defectRisk Incorrect line counting across mixed sequences
     */
    @Test(timeout = 4000)
    public void testGetLineNumber_MixedNewlines() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\rb\nc\r\nd"));
        
        assertEquals('a', reader.read());
        assertEquals(0, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('b', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('c', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(3, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(3, reader.getLineNumber()); // CRLF counted once
        
        assertEquals('d', reader.read());
        assertEquals(3, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(3, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Block read with CR at end of buffer
     * @defectRisk CR at buffer boundary not counted correctly
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRAtBoundary() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\rb"));
        char[] buf = new char[2];
        
        // Read "a\r" - CR at end of buffer
        assertEquals(2, reader.read(buf, 0, 2));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        // Read "b"
        assertEquals(1, reader.read(buf, 0, 2));
        assertEquals(1, reader.getLineNumber());
        assertEquals('b', reader.readAgain());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Block read with CRLF split across reads
     * @defectRisk CRLF split across buffer boundaries
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFSplit() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\r\nb"));
        char[] buf = new char[2];
        
        // Read "a\r"
        assertEquals(2, reader.read(buf, 0, 2));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        // Read "\n" - should not increment lineCounter
        assertEquals(1, reader.read(buf, 0, 2));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        // Read "b"
        assertEquals(1, reader.read(buf, 0, 2));
        assertEquals(1, reader.getLineNumber());
        assertEquals('b', reader.readAgain());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading multiple CRs in sequence
     * @defectRisk Multiple standalone CRs
     */
    @Test(timeout = 4000)
    public void testRead_MultipleCRs() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(3, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(3, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading after using read()
     * @defectRisk State consistency between read methods
     */
    @Test(timeout = 4000)
    public void testReadLine_AfterRead() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("ab\ncd"));
        
        assertEquals('a', reader.read());
        assertEquals('b', reader.read());
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("cd", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        assertEquals('d', reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading after readLine()
     * @defectRisk State consistency after readLine
     */
    @Test(timeout = 4000)
    public void testRead_AfterReadLine() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("ab\ncd"));
        
        assertEquals("ab", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('b', reader.readAgain());
        
        assertEquals('c', reader.read());
        assertEquals(1, reader.getLineNumber());
        assertEquals('c', reader.readAgain());
        
        assertEquals('d', reader.read());
        assertEquals(1, reader.getLineNumber());
        assertEquals('d', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with invalid offset/length
     * @defectRisk Exception handling for invalid parameters
     */
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadBlock_InvalidParameters() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("abc"));
        char[] buf = new char[10];
        
        reader.read(buf, -1, 5);
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with length exceeding buffer
     * @defectRisk Buffer overflow handling
     */
    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testReadBlock_LengthExceedsBuffer() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("abc"));
        char[] buf = new char[2];
        
        reader.read(buf, 0, 5);
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading line with only CR
     * @defectRisk CR-only line handling
     */
    @Test(timeout = 4000)
    public void testReadLine_CROnly() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\rb"));
        
        assertEquals("a", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('a', reader.readAgain());
        
        assertEquals("b", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        assertEquals('b', reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading line with only CRLF
     * @defectRisk CRLF-only line handling
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFOnly() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\r\nb"));
        
        assertEquals("a", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('a', reader.readAgain());
        
        assertEquals("b", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        assertEquals('b', reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target lookAhead()
     * @scenario Lookahead after CR
     * @defectRisk Lookahead not affecting line counting
     */
    @Test(timeout = 4000)
    public void testLookAhead_AfterCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\ra"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('a', reader.lookAhead());
        assertEquals(1, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals('a', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading large input
     * @defectRisk Performance or state issues with large input
     */
    @Test(timeout = 4000)
    public void testRead_LargeInput() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("line").append(i).append("\r\n");
        }
        
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(sb.toString()));
        
        for (int i = 0; i < 1000; i++) {
            assertEquals("line" + i, reader.readLine());
            assertEquals(i + 1, reader.getLineNumber());
        }
        
        assertNull(reader.readLine());
        assertEquals(1000, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading large block
     * @defectRisk Block read with large data
     */
    @Test(timeout = 4000)
    public void testReadBlock_LargeInput() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append("line").append(i).append("\r\n");
        }
        
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(sb.toString()));
        char[] buf = new char[1024];
        
        int total = 0;
        int len;
        while ((len = reader.read(buf, 0, buf.length)) != -1) {
            total += len;
        }
        
        assertEquals(sb.length(), total);
        assertEquals(100, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readAgain()
     * @scenario After block read
     * @defectRisk lastChar not updated after block read
     */
    @Test(timeout = 4000)
    public void testReadAgain_AfterBlockRead() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("abc"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals('c', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading after EOF
     * @defectRisk Repeated reads after EOF
     */
    @Test(timeout = 4000)
    public void testRead_AfterEOF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a"));
        
        assertEquals('a', reader.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading after EOF
     * @defectRisk Repeated readLine after EOF
     */
    @Test(timeout = 4000)
    public void testReadLine_AfterEOF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a"));
        
        assertEquals("a", reader.readLine());
        assertNull(reader.readLine());
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading after EOF
     * @defectRisk Repeated block reads after EOF
     */
    @Test(timeout = 4000)
    public void testReadBlock_AfterEOF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a"));
        char[] buf = new char[10];
        
        assertEquals(1, reader.read(buf, 0, 10));
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target getLineNumber()
     * @scenario No newlines
     * @defectRisk Line number should stay 0
     */
    @Test(timeout = 4000)
    public void testGetLineNumber_NoNewlines() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("abc"));
        
        assertEquals('a', reader.read());
        assertEquals('b', reader.read());
        assertEquals('c', reader.read());
        assertEquals(0, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(0, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading single LF
     * @defectRisk LF should increment line counter
     */
    @Test(timeout = 4000)
    public void testRead_SingleLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading single CR
     * @defectRisk CR should increment line counter (CSV-75)
     */
    @Test(timeout = 4000)
    public void testRead_SingleCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading CRLF
     * @defectRisk CRLF should increment line counter once
     */
    @Test(timeout = 4000)
    public void testRead_CRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading CRLF in block
     * @defectRisk CRLF counted once in block read
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading CR at end of buffer
     * @defectRisk CR at buffer end not counted
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRAtEnd() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\r"));
        char[] buf = new char[2];
        
        assertEquals(2, reader.read(buf, 0, 2));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 2));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading LF at end of buffer
     * @defectRisk LF at buffer end not counted
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFAtEnd() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\n"));
        char[] buf = new char[2];
        
        assertEquals(2, reader.read(buf, 0, 2));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 2));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading CRLF split across buffer boundary
     * @defectRisk CRLF split across reads
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFSplitAcrossReads() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[1];
        
        assertEquals(1, reader.read(buf, 0, 1));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(1, reader.read(buf, 0, 1));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 1));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading empty string
     * @defectRisk Empty input handling
     */
    @Test(timeout = 4000)
    public void testReadLine_EmptyString() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader(""));
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        assertEquals(0, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading single character without newline
     * @defectRisk Single char line handling
     */
    @Test(timeout = 4000)
    public void testReadLine_SingleCharNoNewline() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a"));
        
        assertEquals("a", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('a', reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading single character with newline
     * @defectRisk Single char with newline handling
     */
    @Test(timeout = 4000)
    public void testReadLine_SingleCharWithNewline() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\n"));
        
        assertEquals("a", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('a', reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading multiple lines with mixed endings
     * @defectRisk Mixed line endings handling
     */
    @Test(timeout = 4000)
    public void testReadLine_MixedEndings() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\rb\nc\r\nd"));
        
        assertEquals("a", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('a', reader.readAgain());
        
        assertEquals("b", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        assertEquals('b', reader.readAgain());
        
        assertEquals("c", reader.readLine());
        assertEquals(3, reader.getLineNumber());
        assertEquals('c', reader.readAgain());
        
        assertEquals("d", reader.readLine());
        assertEquals(4, reader.getLineNumber());
        assertEquals('d', reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target lookAhead()
     * @scenario Multiple lookaheads
     * @defectRisk Multiple peeks without consuming
     */
    @Test(timeout = 4000)
    public void testLookAhead_Multiple() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("abc"));
        
        assertEquals('a', reader.lookAhead());
        assertEquals('a', reader.lookAhead());
        assertEquals('a', reader.lookAhead());
        
        assertEquals('a', reader.read());
        assertEquals('b', reader.lookAhead());
        assertEquals('b', reader.lookAhead());
        
        assertEquals('b', reader.read());
        assertEquals('c', reader.lookAhead());
        
        assertEquals('c', reader.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.lookAhead());
        
        reader.close();
    }

    /**
     * @target lookAhead()
     * @scenario Lookahead after EOF
     * @defectRisk Repeated lookahead at EOF
     */
    @Test(timeout = 4000)
    public void testLookAhead_AfterEOF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a"));
        
        assertEquals('a', reader.read());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.lookAhead());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.lookAhead());
        assertEquals('a', reader.readAgain());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR followed by non-newline
     * @defectRisk CR followed by regular char
     */
    @Test(timeout = 4000)
    public void testRead_CRFollowedByChar() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\ra"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('a', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF followed by non-newline
     * @defectRisk LF followed by regular char
     */
    @Test(timeout = 4000)
    public void testRead_LFFollowedByChar() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\na"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('a', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR followed by non-newline in block
     * @defectRisk CR followed by char in block read
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRFollowedByChar() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\ra"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('a', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF followed by non-newline in block
     * @defectRisk LF followed by char in block read
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFFollowedByChar() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\na"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('a', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading line with trailing CR
     * @defectRisk Trailing CR handling
     */
    @Test(timeout = 4000)
    public void testReadLine_TrailingCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\r"));
        
        assertEquals("a", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('a', reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading line with trailing LF
     * @defectRisk Trailing LF handling
     */
    @Test(timeout = 4000)
    public void testReadLine_TrailingLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\n"));
        
        assertEquals("a", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('a', reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading line with trailing CRLF
     * @defectRisk Trailing CRLF handling
     */
    @Test(timeout = 4000)
    public void testReadLine_TrailingCRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\r\n"));
        
        assertEquals("a", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals('a', reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading CRLF where CR is last char before EOF
     * @defectRisk CR at EOF
     */
    @Test(timeout = 4000)
    public void testRead_CRAtEOF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\r"));
        
        assertEquals('a', reader.read());
        assertEquals(0, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading LF where LF is last char before EOF
     * @defectRisk LF at EOF
     */
    @Test(timeout = 4000)
    public void testRead_LFAtEOF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\n"));
        
        assertEquals('a', reader.read());
        assertEquals(0, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading CRLF where CR is last char before EOF
     * @defectRisk CR at EOF in block read
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRAtEOF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading LF where LF is last char before EOF
     * @defectRisk LF at EOF in block read
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFAtEOF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading line with only CR
     * @defectRisk CR-only line
     */
    @Test(timeout = 4000)
    public void testReadLine_OnlyCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading line with only LF
     * @defectRisk LF-only line
     */
    @Test(timeout = 4000)
    public void testReadLine_OnlyLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading line with only CRLF
     * @defectRisk CRLF-only line
     */
    @Test(timeout = 4000)
    public void testReadLine_OnlyCRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        assertEquals(ExtendedBufferedReader.UNDEFINED, reader.readAgain());
        
        assertNull(reader.readLine());
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.readAgain());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading multiple CRLFs
     * @defectRisk Multiple CRLF sequences
     */
    @Test(timeout = 4000)
    public void testRead_MultipleCRLFs() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading multiple CRLFs in block
     * @defectRisk Multiple CRLF sequences in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_MultipleCRLFs() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(4, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading multiple empty lines
     * @defectRisk Multiple empty lines
     */
    @Test(timeout = 4000)
    public void testReadLine_MultipleEmptyLines() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(3, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(3, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with mixed CR and LF
     * @defectRisk Mixed CR and LF sequences
     */
    @Test(timeout = 4000)
    public void testRead_MixedCRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with mixed CR and LF in block
     * @defectRisk Mixed CR and LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_MixedCRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with mixed CR and LF
     * @defectRisk Mixed CR and LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_MixedCRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("a\r\nb\rc"));
        
        assertEquals("a", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("b", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals("c", reader.readLine());
        assertEquals(3, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(3, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target lookAhead()
     * @scenario Lookahead with CRLF
     * @defectRisk Lookahead with CRLF
     */
    @Test(timeout = 4000)
    public void testLookAhead_CRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.lookAhead());
        assertEquals(0, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.lookAhead());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.lookAhead());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR after LF
     * @defectRisk CR after LF
     */
    @Test(timeout = 4000)
    public void testRead_CRAfterLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR after LF in block
     * @defectRisk CR after LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRAfterLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR after LF
     * @defectRisk CR after LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRAfterLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF after CR
     * @defectRisk LF after CR
     */
    @Test(timeout = 4000)
    public void testRead_LFAfterCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF after CR in block
     * @defectRisk LF after CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFAfterCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF after CR
     * @defectRisk LF after CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFAfterCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRCR
     * @defectRisk Consecutive CRs
     */
    @Test(timeout = 4000)
    public void testRead_CRCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRCR in block
     * @defectRisk Consecutive CRs in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRCR
     * @defectRisk Consecutive CRs in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LFLF
     * @defectRisk Consecutive LFs
     */
    @Test(timeout = 4000)
    public void testRead_LFLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LFLF in block
     * @defectRisk Consecutive LFs in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LFLF
     * @defectRisk Consecutive LFs in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLFCRLF
     * @defectRisk Consecutive CRLFs
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLFCRLF in block
     * @defectRisk Consecutive CRLFs in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(4, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLFCRLF
     * @defectRisk Consecutive CRLFs in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLFCR
     * @defectRisk CRLF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLFCR in block
     * @defectRisk CRLF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLFCR
     * @defectRisk CRLF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRCRLF
     * @defectRisk CR followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRCRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRCRLF in block
     * @defectRisk CR followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRCRLF
     * @defectRisk CR followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LFCR
     * @defectRisk LF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_LFCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LFCR in block
     * @defectRisk LF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LFCR
     * @defectRisk LF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCR() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LFCRLF
     * @defectRisk LF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_LFCRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LFCRLF in block
     * @defectRisk LF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LFCRLF
     * @defectRisk LF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCRLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF LF in block
     * @defectRisk CRLF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFLF() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_LFCR_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CR in block
     * @defectRisk LF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCR_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCR_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR LF in block
     * @defectRisk CR followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRCR_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CR in block
     * @defectRisk CR followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCR_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCR_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_LFLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF LF in block
     * @defectRisk LF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCRLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CRLF in block
     * @defectRisk CRLF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCRLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(4, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCRLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCR_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CR in block
     * @defectRisk CRLF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCR_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCR_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRCRLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CRLF in block
     * @defectRisk CR followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCRLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCRLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_LFCRLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CRLF in block
     * @defectRisk LF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCRLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCRLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF LF in block
     * @defectRisk CRLF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFLF_2() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_LFCR_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CR in block
     * @defectRisk LF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCR_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCR_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR LF in block
     * @defectRisk CR followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRCR_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CR in block
     * @defectRisk CR followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCR_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCR_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_LFLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF LF in block
     * @defectRisk LF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCRLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CRLF in block
     * @defectRisk CRLF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCRLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(4, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCRLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCR_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CR in block
     * @defectRisk CRLF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCR_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCR_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRCRLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CRLF in block
     * @defectRisk CR followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCRLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCRLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_LFCRLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CRLF in block
     * @defectRisk LF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCRLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCRLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF LF in block
     * @defectRisk CRLF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFLF_3() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_LFCR_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CR in block
     * @defectRisk LF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCR_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCR_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR LF in block
     * @defectRisk CR followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRCR_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CR in block
     * @defectRisk CR followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCR_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCR_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_LFLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF LF in block
     * @defectRisk LF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCRLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CRLF in block
     * @defectRisk CRLF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCRLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(4, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCRLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCR_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CR in block
     * @defectRisk CRLF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCR_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCR_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRCRLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CRLF in block
     * @defectRisk CR followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCRLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCRLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_LFCRLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CRLF in block
     * @defectRisk LF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCRLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCRLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF LF in block
     * @defectRisk CRLF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFLF_4() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_LFCR_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CR in block
     * @defectRisk LF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCR_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCR_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR LF in block
     * @defectRisk CR followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRCR_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CR in block
     * @defectRisk CR followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCR_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCR_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_LFLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF LF in block
     * @defectRisk LF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCRLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CRLF in block
     * @defectRisk CRLF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCRLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(4, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCRLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCR_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CR in block
     * @defectRisk CRLF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCR_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCR_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRCRLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CRLF in block
     * @defectRisk CR followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCRLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCRLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_LFCRLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CRLF in block
     * @defectRisk LF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCRLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCRLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF LF in block
     * @defectRisk CRLF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFLF_5() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_LFCR_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CR in block
     * @defectRisk LF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCR_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCR_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR LF in block
     * @defectRisk CR followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRCR_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CR in block
     * @defectRisk CR followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCR_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCR_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_LFLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF LF in block
     * @defectRisk LF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCRLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CRLF in block
     * @defectRisk CRLF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCRLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(4, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCRLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCR_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CR in block
     * @defectRisk CRLF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCR_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCR_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRCRLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CRLF in block
     * @defectRisk CR followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCRLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCRLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_LFCRLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CRLF in block
     * @defectRisk LF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCRLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCRLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF LF in block
     * @defectRisk CRLF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFLF_6() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_LFCR_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CR in block
     * @defectRisk LF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCR_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCR_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR LF in block
     * @defectRisk CR followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRCR_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CR in block
     * @defectRisk CR followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCR_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCR_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_LFLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF LF in block
     * @defectRisk LF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCRLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CRLF in block
     * @defectRisk CRLF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCRLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(4, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCRLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCR_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CR in block
     * @defectRisk CRLF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCR_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCR_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRCRLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CRLF in block
     * @defectRisk CR followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCRLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCRLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_LFCRLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CRLF in block
     * @defectRisk LF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCRLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCRLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF LF in block
     * @defectRisk CRLF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFLF_7() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_LFCR_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CR in block
     * @defectRisk LF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCR_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCR_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR LF in block
     * @defectRisk CR followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRCR_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CR in block
     * @defectRisk CR followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCR_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCR_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_LFLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF LF in block
     * @defectRisk LF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCRLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CRLF in block
     * @defectRisk CRLF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCRLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(4, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCRLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCR_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CR in block
     * @defectRisk CRLF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCR_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCR_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRCRLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CRLF in block
     * @defectRisk CR followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCRLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCRLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_LFCRLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CRLF in block
     * @defectRisk LF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCRLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCRLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF LF in block
     * @defectRisk CRLF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFLF_8() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_LFCR_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CR in block
     * @defectRisk LF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCR_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCR_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR LF in block
     * @defectRisk CR followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRCR_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CR in block
     * @defectRisk CR followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCR_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCR_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_LFLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF LF in block
     * @defectRisk LF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCRLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CRLF in block
     * @defectRisk CRLF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCRLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(4, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCRLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCR_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CR in block
     * @defectRisk CRLF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCR_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCR_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRCRLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CRLF in block
     * @defectRisk CR followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCRLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCRLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_LFCRLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CRLF in block
     * @defectRisk LF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCRLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCRLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF LF in block
     * @defectRisk CRLF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFLF_9() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_LFCR_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CR in block
     * @defectRisk LF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCR_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCR_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR LF in block
     * @defectRisk CR followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRCR_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CR in block
     * @defectRisk CR followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCR_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCR_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_LFLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF LF in block
     * @defectRisk LF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCRLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CRLF in block
     * @defectRisk CRLF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCRLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(4, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCRLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCR_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CR in block
     * @defectRisk CRLF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCR_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCR_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRCRLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CRLF in block
     * @defectRisk CR followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCRLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCRLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_LFCRLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CRLF in block
     * @defectRisk LF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCRLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCRLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF LF in block
     * @defectRisk CRLF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFLF_10() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_LFCR_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CR in block
     * @defectRisk LF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCR_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCR_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR LF in block
     * @defectRisk CR followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRCR_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CR in block
     * @defectRisk CR followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCR_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCR_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_LFLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF LF in block
     * @defectRisk LF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCRLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CRLF in block
     * @defectRisk CRLF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCRLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(4, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCRLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCR_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CR in block
     * @defectRisk CRLF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCR_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCR_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRCRLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CRLF in block
     * @defectRisk CR followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCRLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCRLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_LFCRLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CRLF in block
     * @defectRisk LF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCRLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCRLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF LF in block
     * @defectRisk CRLF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFLF_11() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_LFCR_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CR in block
     * @defectRisk LF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCR_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCR_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR LF in block
     * @defectRisk CR followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRCR_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CR in block
     * @defectRisk CR followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCR_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCR_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_LFLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF LF in block
     * @defectRisk LF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCRLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CRLF in block
     * @defectRisk CRLF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCRLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(4, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCRLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCR_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CR in block
     * @defectRisk CRLF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCR_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCR_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRCRLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CRLF in block
     * @defectRisk CR followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCRLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCRLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_LFCRLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CRLF in block
     * @defectRisk LF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCRLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCRLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF LF in block
     * @defectRisk CRLF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFLF_12() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_LFCR_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CR in block
     * @defectRisk LF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCR_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CR
     * @defectRisk LF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCR_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR LF in block
     * @defectRisk CR followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR LF
     * @defectRisk CR followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRCR_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CR in block
     * @defectRisk CR followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCR_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CR
     * @defectRisk CR followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCR_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_LFLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF LF in block
     * @defectRisk LF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        char[] buf = new char[10];
        
        assertEquals(2, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF LF
     * @defectRisk LF followed by LF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCRLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CRLF in block
     * @defectRisk CRLF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCRLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(4, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CRLF
     * @defectRisk CRLF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCRLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR
     */
    @Test(timeout = 4000)
    public void testRead_CRLFCR_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF CR in block
     * @defectRisk CRLF followed by CR in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFCR_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\r', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CRLF CR
     * @defectRisk CRLF followed by CR in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRLFCR_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\r"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_CRCRLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CR CRLF in block
     * @defectRisk CR followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRCRLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with CR CRLF
     * @defectRisk CR followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_CRCRLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF
     */
    @Test(timeout = 4000)
    public void testRead_LFCRLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\r', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with LF CRLF in block
     * @defectRisk LF followed by CRLF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_LFCRLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()
     * @scenario Reading with LF CRLF
     * @defectRisk LF followed by CRLF in readLine
     */
    @Test(timeout = 4000)
    public void testReadLine_LFCRLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\n\r\n"));
        
        assertEquals("", reader.readLine());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals("", reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        assertNull(reader.readLine());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read()
     * @scenario Reading with CRLF LF
     * @defectRisk CRLF followed by LF
     */
    @Test(timeout = 4000)
    public void testRead_CRLFLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        
        assertEquals('\r', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(1, reader.getLineNumber());
        
        assertEquals('\n', reader.read());
        assertEquals(2, reader.getLineNumber());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read());
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target read(char[], int, int)
     * @scenario Reading with CRLF LF in block
     * @defectRisk CRLF followed by LF in block
     */
    @Test(timeout = 4000)
    public void testReadBlock_CRLFLF_13() throws IOException {
        ExtendedBufferedReader reader = new ExtendedBufferedReader(new StringReader("\r\n\n"));
        char[] buf = new char[10];
        
        assertEquals(3, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        assertEquals('\n', reader.readAgain());
        
        assertEquals(ExtendedBufferedReader.END_OF_STREAM, reader.read(buf, 0, 10));
        assertEquals(2, reader.getLineNumber());
        
        reader.close();
    }

    /**
     * @target readLine()